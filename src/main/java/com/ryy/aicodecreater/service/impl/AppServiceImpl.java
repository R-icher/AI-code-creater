package com.ryy.aicodecreater.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.mapper.AppMapper;
import com.ryy.aicodecreater.model.dto.app.AppQueryRequest;
import com.ryy.aicodecreater.model.entity.App;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;
import com.ryy.aicodecreater.model.vo.AppVO;
import com.ryy.aicodecreater.model.vo.UserVO;
import com.ryy.aicodecreater.service.AppService;
import com.ryy.aicodecreater.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现
 *
 * @author <a href="https://github.com/R-icher">Richer</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        String codeGenType = app.getCodeGenType();
        Integer priority = app.getPriority();

        if (add && StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        if (StrUtil.isNotBlank(appName) && appName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 过长");
        }
        if (StrUtil.isNotBlank(codeGenType) && CodeGenTypeEnum.getEnumByValue(codeGenType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        }
        if (priority != null && priority < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "优先级不能小于 0");
        }
    }

    /**
     * 根据应用查询请求参数构建查询条件包装器
     *
     * @param appQueryRequest 应用查询请求对象，包含查询条件
     * @return 返回构建好的QueryWrapper对象，用于数据库查询
     * @throws BusinessException 当请求参数为空时抛出业务异常
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        // 检查请求参数是否为空，为空则抛出业务异常
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        // 从请求对象中获取各个查询条件
        Long id = appQueryRequest.getId();                           // 应用ID
        String appName = appQueryRequest.getAppName();                // 应用名称
        String cover = appQueryRequest.getCover();                    // 应用封面
        String initPrompt = appQueryRequest.getInitPrompt();          // 初始提示词
        String codeGenType = appQueryRequest.getCodeGenType();        // 代码生成类型
        String deployKey = appQueryRequest.getDeployKey();            // 部署密钥
        Integer priority = appQueryRequest.getPriority();             // 优先级
        Long userId = appQueryRequest.getUserId();                    // 用户ID
        String sortField = appQueryRequest.getSortField();            // 排序字段
        String sortOrder = appQueryRequest.getSortOrder();            // 排序方式
        // 创建QueryWrapper并设置查询条件
        return QueryWrapper.create()
                .eq("id", id)                                        // 精确匹配ID
                .like("appName", appName)                            // 模糊匹配应用名称
                .like("cover", cover)                                // 模糊匹配封面
                .like("initPrompt", initPrompt)                      // 模糊匹配初始提示词
                .eq("codeGenType", codeGenType)                      // 精确匹配代码生成类型
                .eq("deployKey", deployKey)                          // 精确匹配部署密钥
                .eq("priority", priority)                            // 精确匹配优先级
                .eq("userId", userId)                                // 精确匹配用户ID
                .orderBy(sortField, "ascend".equals(sortOrder));      // 根据指定字段和排序方式排序
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }


    /**
     * - 先收集所有 userId 到集合中
     * - 根据 userId 集合批量查询所有用户信息
     * - 构建 Map 映射关系 userId => UserVO
     * - 一次性组装所有 AppVO，根据 userId 从 Map 中取到需要的用户信息
     *
     * @param appList 应用列表
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
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

}
