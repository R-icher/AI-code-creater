package com.ryy.aicodecreater.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ryy.aicodecreater.model.dto.app.AppQueryRequest;
import com.ryy.aicodecreater.model.entity.App;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层
 *
 * @author <a href="https://github.com/R-icher">Richer</a>
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用
     *
     * @param app 应用
     * @param add 是否为创建
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图
     *
     * @param app 应用
     * @return 视图
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图列表
     *
     * @param appList 应用列表
     * @return 视图列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
}
