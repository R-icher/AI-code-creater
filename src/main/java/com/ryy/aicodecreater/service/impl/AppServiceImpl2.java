package com.ryy.aicodecreater.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ryy.aicodecreater.ai.AiCodeGenTypeRoutingService;
import com.ryy.aicodecreater.ai.AiCodeGenTypeRoutingServiceFactory;
import com.ryy.aicodecreater.constant.AppConstant;
import com.ryy.aicodecreater.core.AiCodeGeneratorFacade;
import com.ryy.aicodecreater.core.builder.VueProjectBuilder;
import com.ryy.aicodecreater.core.handler.StreamHandlerExecutor;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.exception.ThrowUtils;
import com.ryy.aicodecreater.mapper.AppMapper;
import com.ryy.aicodecreater.model.dto.app.AppAddRequest;
import com.ryy.aicodecreater.model.dto.app.AppQueryRequest;
import com.ryy.aicodecreater.model.entity.App;
import com.ryy.aicodecreater.model.entity.AppVersion;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.model.enums.ChatHistoryMessageTypeEnum;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;
import com.ryy.aicodecreater.model.vo.AppVO;
import com.ryy.aicodecreater.model.vo.UserVO;
import com.ryy.aicodecreater.monitor.MonitorContext;
import com.ryy.aicodecreater.monitor.MonitorContextHolder;
import com.ryy.aicodecreater.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现
 * <p>
 * 版本化改造说明：
 * 1. chatToGenCode() 既负责流式返回，也负责版本目录生成
 * 2. AI 成功生成结束后，才会保存 app_version 并更新 currentVersion
 * 3. deployApp() 不再读固定目录，而是根据 currentVersion 去 app_version 表中查真实路径
 */
@Service
@Slf4j
public class AppServiceImpl2 extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 校验权限：只有应用创建者才可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }

        // 4. 获取代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }

        // 5. 先记录用户消息
        chatHistoryService.addChatMessage(
                appId,
                message,
                ChatHistoryMessageTypeEnum.USER.getValue(),
                loginUser.getId()
        );

        // 6. 设置监控上下文
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .userId(loginUser.getId().toString())
                        .appId(appId.toString())
                        .build()
        );

        // 7. 计算新版本号
        //    例如当前是 v2，则本次生成的是 v3
        int nextVersion = (app.getCurrentVersion() == null ? 0 : app.getCurrentVersion()) + 1;

        // 8. 构建本次生成对应的“版本目录”
        //    示例：/tmp/code_output/multi_file_2038144454829350912_v3
        String sourceDirName = app.getCodeGenType() + "_" + appId + "_v" + nextVersion;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        // 9. 如果目录已存在，先删除，避免脏数据
        File versionDir = new File(sourceDirPath);
        if (versionDir.exists()) {
            FileUtil.del(versionDir);
        }

        // 10. 调用 AI 核心能力：
        //    与老逻辑不同，这里必须把输出目录传下去，
        //    让底层把代码直接保存到“版本目录”，而不是固定目录
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                message,
                codeGenTypeEnum,
                appId,
                sourceDirPath
        );

        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum).doFinally(signalType -> {
            // 流结束时清理（无论成功/失败/取消）
            MonitorContextHolder.clearContext();
        });

//        // 10. 收集 AI 的完整回复内容，流结束后写入聊天记录
//        StringBuilder aiResponseBuilder = new StringBuilder();
//
//        return contentFlux
//                .map(chunk -> {
//                    // 10.1 实时收集 AI 返回内容
//                    aiResponseBuilder.append(chunk);
//                    // 10.2 同时继续把 chunk 返回前端，实现流式展示
//                    return chunk;
//                })
//                // 11. 只有当前面的 AI 流完整成功结束，才会执行这里
//                .doOnComplete(() -> {
//                    String aiResponse = aiResponseBuilder.toString();
//
//                    // 11.1 保存 AI 回复到聊天记录
//                    if (StrUtil.isNotBlank(aiResponse)) {
//                        chatHistoryService.addChatMessage(
//                                appId,
//                                aiResponse,
//                                ChatHistoryMessageTypeEnum.AI.getValue(),
//                                loginUser.getId()
//                        );
//                    }
//
//                    // 11.2 再次确认版本目录确实生成成功
//                    File generatedDir = new File(sourceDirPath);
//                    ThrowUtils.throwIf(!generatedDir.exists() || !generatedDir.isDirectory(),
//                            ErrorCode.SYSTEM_ERROR, "代码生成失败，版本目录不存在");
//
//                    // 11.3 保存版本记录，并更新 app.currentVersion
//                    saveVersionRecordAfterGenerate(appId, nextVersion, message, sourceDirPath);
//                })
//                .doOnError(error -> {
//                    // 12. 如果 AI 生成失败，记录错误消息
//                    String errorMessage = "AI回复失败: " + error.getMessage();
//                    chatHistoryService.addChatMessage(
//                            appId,
//                            errorMessage,
//                            ChatHistoryMessageTypeEnum.AI.getValue(),
//                            loginUser.getId()
//                    );
//
//                    // 13. 清理本次失败生成的目录，避免留下半成品
//                    File failedDir = new File(sourceDirPath);
//                    if (failedDir.exists()) {
//                        FileUtil.del(failedDir);
//                    }
//                });
    }


    /**
     * AI 代码生成成功后，保存版本记录并更新 app 主表当前版本号
     * <p>
     * 注意：
     * 1. 这里只做“版本落库”
     * 2. 不再负责调用 AI
     * 3. 该方法由 chatToGenCode() 在流式生成成功后调用
     */
    private void saveVersionRecordAfterGenerate(Long appId, Integer nextVersion, String prompt, String sourceDirPath) {
        transactionTemplate.executeWithoutResult(status -> {
            // 1. 保存一条新版本记录到 app_version 表
            AppVersion appVersion = new AppVersion();
            appVersion.setAppId(appId);
            appVersion.setVersion(nextVersion);
            appVersion.setPrompt(prompt);
            appVersion.setSourceDirPath(sourceDirPath);

            boolean saveResult = appVersionService.save(appVersion);
            ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "保存应用版本记录失败");

            // 2. 更新 app 表当前版本号
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCurrentVersion(nextVersion);

            boolean updateResult = this.updateById(updateApp);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用当前版本号失败");
        });
    }


    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 只有应用创建者可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }

        // 4. 应用必须已经存在可部署版本
        ThrowUtils.throwIf(app.getCurrentVersion() == null || app.getCurrentVersion() <= 0,
                ErrorCode.SYSTEM_ERROR, "当前应用还没有可部署的版本");

        // 5. 检查 deployKey，若为空则生成
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        // 6. 根据 currentVersion 动态查询当前版本记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("version", app.getCurrentVersion())
                .eq("isDelete", 0);

        AppVersion currentVersionInfo = appVersionService.getOne(queryWrapper);
        ThrowUtils.throwIf(currentVersionInfo == null, ErrorCode.SYSTEM_ERROR, "找不到当前版本的代码记录");

        // 7. 检查源目录是否存在
        String sourceDirPath = currentVersionInfo.getSourceDirPath();
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        // 补充：Vue 项目特殊处理：执行构建
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {

            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath, buildStatus -> {
                log.info("部署前构建状态: stage={}, message={}",
                        buildStatus.getStage(),
                        buildStatus.getMessage());
            });
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");

            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");

            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }

        // 8. 部署目录：先清空旧目录，再复制当前版本代码
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            File deployDir = new File(deployDirPath);
            // 防止老目录中还残留有旧文件，影响最终项目上线
            if (deployDir.exists()) {
                FileUtil.clean(deployDir);
            } else {
                FileUtil.mkdir(deployDir);
            }
            FileUtil.copyContent(sourceDir, deployDir, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }

        // 9. 更新部署信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());

        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        // 10. 返回访问地址
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 11. 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);

            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }

    /**
     * 把当前应用的 currentVersion 切换到某个历史版本，实现“版本回退”
     * <p>
     * 注意：
     * 这里只是改“当前指向的版本号”
     * 真正让线上生效，仍然需要重新调用 deployApp()
     */
    @Override
    public Boolean rollbackVersion(Long appId, Integer targetVersion, User loginUser) {
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("version", targetVersion)
                .eq("isDelete", 0);

        long count = appVersionService.count(queryWrapper);
        if (count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标回退版本不存在");
        }

        // 【关键注释 7：轻量级回退机制】
        // 回退操作不需要移动任何物理文件，只需要把 app 主表的 currentVersion 字段修改为目标版本号即可。
        // 等用户下次点击“部署”时，deployApp 方法会自动读取这个旧版本目录里的代码覆盖到线上。
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setCurrentVersion(targetVersion);
        return this.updateById(updateApp);
    }


    /**
     * 读取某个应用某个版本下某个文件的代码内容，供前端做 Diff 对比
     */
    @Override
    public String getVersionFileContent(Long appId, Integer version, String relativeFilePath, User loginUser) {
        // 1. 防止路径穿越攻击，例如 ../../../../etc/passwd
        if (StrUtil.isBlank(relativeFilePath) || relativeFilePath.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法的文件路径");
        }

        // 2. 应用必须存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 当前用户必须有权查看代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 4. 查询指定版本记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("version", version)
                .eq("isDelete", 0);

        AppVersion appVersion = appVersionService.getOne(queryWrapper);
        ThrowUtils.throwIf(appVersion == null, ErrorCode.NOT_FOUND_ERROR, "指定版本的代码记录不存在");

        // 5. 拼接绝对路径
        String absolutePath = appVersion.getSourceDirPath() + File.separator + relativeFilePath;
        File file = new File(absolutePath);

        // 6. 文件必须存在，且不能是目录
        if (!file.exists() || file.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在: " + relativeFilePath);
        }

        // 7. 返回文件内容给前端
        return FileUtil.readUtf8String(file);
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);

        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());

        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));

        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 删除应用时，关联删除对话历史
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用关联对话历史失败: {}", e.getMessage());
        }
        return super.removeById(id);
    }


    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");

        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());

        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));

        // 使用 AI 智能选择代码生成类型（多例模式）
        AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = routingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(selectedCodeGenType.getValue());

        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }
}
