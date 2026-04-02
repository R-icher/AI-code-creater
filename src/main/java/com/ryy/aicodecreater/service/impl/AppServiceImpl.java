//package com.ryy.aicodecreater.service.impl;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.util.RandomUtil;
//import cn.hutool.core.util.StrUtil;
//import com.mybatisflex.core.query.QueryWrapper;
//import com.mybatisflex.spring.service.impl.ServiceImpl;
//import com.ryy.aicodecreater.constant.AppConstant;
//import com.ryy.aicodecreater.core.AiCodeGeneratorFacade;
//import com.ryy.aicodecreater.exception.BusinessException;
//import com.ryy.aicodecreater.exception.ErrorCode;
//import com.ryy.aicodecreater.exception.ThrowUtils;
//import com.ryy.aicodecreater.mapper.AppMapper;
//import com.ryy.aicodecreater.model.dto.app.AppQueryRequest;
//import com.ryy.aicodecreater.model.entity.AppVersion;
//import com.ryy.aicodecreater.model.entity.App;
//import com.ryy.aicodecreater.model.entity.User;
//import com.ryy.aicodecreater.model.enums.ChatHistoryMessageTypeEnum;
//import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;
//import com.ryy.aicodecreater.model.vo.AppVO;
//import com.ryy.aicodecreater.model.vo.UserVO;
//import com.ryy.aicodecreater.service.AppService;
//import com.ryy.aicodecreater.service.AppVersionService;
//import com.ryy.aicodecreater.service.ChatHistoryService;
//import com.ryy.aicodecreater.service.UserService;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import reactor.core.publisher.Flux;
//
//import java.io.File;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * 应用 服务层实现
// *
// * @author <a href="https://github.com/R-icher">Richer</a>
// */
//@Service
//@Slf4j
//public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
//
//    @Resource
//    private UserService userService;
//    @Resource
//    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
//    @Resource
//    private ChatHistoryService chatHistoryService;
//    @Resource
//    private AppVersionService appVersionService; // 注入应用版本 Service
//
//
//    @Override
//    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
//        // 1. 参数校验
//        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
//        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
//
//        // 2. 查询应用消息
//        App app = this.getById(appId);
//        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
//
//        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
//        if (!app.getUserId().equals(loginUser.getId())) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
//        }
//
//        // 4. 获取应用的代码生成类型【是单个 html 类型还是多文件类型】
//        String codeGenTypeStr = app.getCodeGenType();
//        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
//        if (codeGenTypeEnum == null) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
//        }
//
//        // 5. 通过校验后，添加用户消息到对话历史
//        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
//
//        // 6. 调用 AI 生成代码（流式）
//        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
//
//        // 7. 收集 AI 相应的内容，并且在完成后保存记录到对话历史
//        StringBuilder aiResponseBuilder = new StringBuilder();
//        return contentFlux.map(chunk -> {
//                    // 实时收集 AI 的响应内容
//                    aiResponseBuilder.append(chunk);
//                    return chunk;
//                })
//                .doOnComplete(() -> {
//                    // 流式响应完成后，添加AI消息到对话历史
//                    String aiResponse = aiResponseBuilder.toString();
//                    if (StrUtil.isNotBlank(aiResponse)) {
//                        chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                    }
//                })
//                .doOnError(error -> {
//                    // 如果AI回复失败，也要记录错误消息
//                    String errorMessage = "AI回复失败: " + error.getMessage();
//                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                });
//    }
//
//
//    /**
//     * 根据应用查询请求参数构建查询条件包装器
//     *
//     * @param appQueryRequest 应用查询请求对象，包含查询条件
//     * @return 返回构建好的QueryWrapper对象，用于数据库查询
//     * @throws BusinessException 当请求参数为空时抛出业务异常
//     */
//    @Override
//    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
//        // 检查请求参数是否为空，为空则抛出业务异常
//        if (appQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
//        }
//
//        // 从请求对象中获取各个查询条件
//        Long id = appQueryRequest.getId();                           // 应用ID
//        String appName = appQueryRequest.getAppName();                // 应用名称
//        String cover = appQueryRequest.getCover();                    // 应用封面
//        String initPrompt = appQueryRequest.getInitPrompt();          // 初始提示词
//        String codeGenType = appQueryRequest.getCodeGenType();        // 代码生成类型
//        String deployKey = appQueryRequest.getDeployKey();            // 部署密钥
//        Integer priority = appQueryRequest.getPriority();             // 优先级
//        Long userId = appQueryRequest.getUserId();                    // 用户ID
//        String sortField = appQueryRequest.getSortField();            // 排序字段
//        String sortOrder = appQueryRequest.getSortOrder();            // 排序方式
//        // 创建QueryWrapper并设置查询条件
//        return QueryWrapper.create()
//                .eq("id", id)                                        // 精确匹配ID
//                .like("appName", appName)                            // 模糊匹配应用名称
//                .like("cover", cover)                                // 模糊匹配封面
//                .like("initPrompt", initPrompt)                      // 模糊匹配初始提示词
//                .eq("codeGenType", codeGenType)                      // 精确匹配代码生成类型
//                .eq("deployKey", deployKey)                          // 精确匹配部署密钥
//                .eq("priority", priority)                            // 精确匹配优先级
//                .eq("userId", userId)                                // 精确匹配用户ID
//                .orderBy(sortField, "ascend".equals(sortOrder));      // 根据指定字段和排序方式排序
//    }
//
//
//    @Override
//    public AppVO getAppVO(App app) {
//        if (app == null) {
//            return null;
//        }
//        AppVO appVO = new AppVO();
//        BeanUtil.copyProperties(app, appVO);
//        // 关联查询用户信息
//        Long userId = app.getUserId();
//        if (userId != null) {
//            User user = userService.getById(userId);
//            UserVO userVO = userService.getUserVO(user);
//            appVO.setUser(userVO);
//        }
//        return appVO;
//    }
//
//
//    /**
//     * - 先收集所有 userId 到集合中
//     * - 根据 userId 集合批量查询所有用户信息
//     * - 构建 Map 映射关系 userId => UserVO
//     * - 一次性组装所有 AppVO，根据 userId 从 Map 中取到需要的用户信息
//     *
//     * @param appList 应用列表
//     * @return
//     */
//    @Override
//    public List<AppVO> getAppVOList(List<App> appList) {
//        if (CollUtil.isEmpty(appList)) {
//            return new ArrayList<>();
//        }
//        // 批量获取用户信息，避免 N+1 查询问题
//        Set<Long> userIds = appList.stream()
//                .map(App::getUserId)
//                .collect(Collectors.toSet());
//        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
//                .collect(Collectors.toMap(User::getId, userService::getUserVO));
//        return appList.stream().map(app -> {
//            AppVO appVO = getAppVO(app);
//            UserVO userVO = userVOMap.get(app.getUserId());
//            appVO.setUser(userVO);
//            return appVO;
//        }).collect(Collectors.toList());
//    }
//
//
//    @Override
//    public String deployApp(Long appId, User loginUser) {
//        // 1. 参数校验
//        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
//        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
//
//        // 2. 查询应用信息
//        App app = this.getById(appId);
//        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
//
//        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
//        if (!app.getUserId().equals(loginUser.getId())) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
//        }
//
//        // 4. 检查是否已经有 deployKey【给每个应用生成一个对外访问时使用的唯一部署标识。】
//        String deployKey = app.getDeployKey();
//        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
//        if (StrUtil.isBlank(deployKey)) {
//            deployKey = RandomUtil.randomString(6);
//        }
//
//        // 5. 获取代码生成类型，构建源目录路径
//        String codeGenType = app.getCodeGenType();
//        String sourceDirName = codeGenType + "_" + appId;
//        // 生成类似：/tmp/code_output/multi_file_2038144454829350912
//        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
//
////        // 【动态获取当前版本代码】
////        // 以前是写死读取固定目录，现在必须先查 app 表的 currentVersion，
////        // 再去 app_version 表找到对应版本的物理路径。这保证了无论怎么回退，部署的总是“当前指向”的版本。
////        QueryWrapper queryWrapper = QueryWrapper.create()
////                .eq("appId", appId)
////                .eq("version", app.getCurrentVersion());
////        AppVersion currentVersionInfo = appVersionService.getOne(queryWrapper);
////        ThrowUtils.throwIf(currentVersionInfo == null, ErrorCode.SYSTEM_ERROR, "找不到当前版本的代码记录");
//
//        // 6. 检查源目录是否存在
//        File sourceDir = new File(sourceDirPath);
//        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
//        }
//
//        // 7. 复制文件到部署目录
//        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
//        try {
//            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
//        } catch (Exception e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
//        }
//
//        // 8. 更新应用的 deployKey 和部署时间
//        App updateApp = new App();
//        updateApp.setId(appId);
//        updateApp.setDeployKey(deployKey);
//        updateApp.setDeployedTime(LocalDateTime.now());
//        boolean updateResult = this.updateById(updateApp);
//        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
//
//        // 9. 返回可访问的 URL
//        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
//    }
//
//
//
//    /**
//     * 删除应用时关联删除对话历史【重写 MyBatisFlux 的根据 Id 移除方法，实现覆盖】
//     * 在删除应用之前，需要先删除关联的对话历史
//     *
//     * @param id 应用ID
//     * @return 是否成功
//     */
//    @Override
//    public boolean removeById(Serializable id) {
//        if (id == null) {
//            return false;
//        }
//        // 转换为 Long 类型
//        Long appId = Long.valueOf(id.toString());
//        if (appId <= 0) {
//            return false;
//        }
//        // 先删除关联的对话历史
//        try {
//            chatHistoryService.deleteByAppId(appId);
//        } catch (Exception e) {
//            // 记录日志但不阻止应用删除
//            log.error("删除应用关联对话历史失败: {}", e.getMessage());
//        }
//        // 删除应用
//        return super.removeById(id);
//    }
//
//}
