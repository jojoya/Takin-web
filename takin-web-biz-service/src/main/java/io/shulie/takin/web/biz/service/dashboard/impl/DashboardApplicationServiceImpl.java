package io.shulie.takin.web.biz.service.dashboard.impl;

import javax.annotation.Resource;

import com.pamirs.takin.common.constant.AppSwitchEnum;
import io.shulie.takin.web.biz.cache.AgentConfigCacheManager;
import io.shulie.takin.web.biz.constant.DashboardExceptionCode;
import io.shulie.takin.web.biz.pojo.response.dashboard.AppPressureSwitchSetResponse;
import io.shulie.takin.web.biz.pojo.response.dashboard.ApplicationStatusResponse;
import io.shulie.takin.web.biz.pojo.response.dashboard.ApplicationSwitchStatusResponse;
import io.shulie.takin.web.biz.service.ApplicationService;
import io.shulie.takin.web.biz.service.dashboard.DashboardApplicationService;
import io.shulie.takin.web.common.common.Separator;
import io.shulie.takin.web.common.exception.TakinWebException;
import io.shulie.takin.web.common.util.CommonUtil;
import io.shulie.takin.web.data.dao.application.ApplicationDAO;
import io.shulie.takin.web.ext.util.WebPluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service(value = "dashboard-ApplicationServiceImpl")
public class DashboardApplicationServiceImpl implements DashboardApplicationService {

    private static final String NEED_VERIFY_USER_MAP = "NEED_VERIFY_USER_MAP";
    private static final String PRADAR_SWITCH_STATUS = "PRADAR_SWITCH_STATUS_";
    private static final String PRADAR_SWITCH_STATUS_VO = "PRADAR_SWITCH_STATUS_VO_";

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    AgentConfigCacheManager agentConfigCacheManager;
    @Resource
    private ApplicationService applicationService;
    @Autowired
    private ApplicationDAO applicationDAO;

    @Override
    public ApplicationSwitchStatusResponse getUserAppSwitchInfo() {
        ApplicationSwitchStatusResponse response = new ApplicationSwitchStatusResponse() {{
            setSwitchStatus(applicationService.getUserSwitchStatusForVo());
        }};
        response.setUserId(WebPluginUtils.traceUserId());
        response.setDeptId(WebPluginUtils.traceDeptId());
        WebPluginUtils.fillQueryResponse(response);
        return response;
    }

    /**
     * 设置用户的全局压测开关
     *
     * @return 开关状态
     */
    @Override
    public AppPressureSwitchSetResponse setUserAppPressureSwitch(Boolean enable) {
        //全局开关只保留 开/关
        if (enable == null) {
            throw new TakinWebException(DashboardExceptionCode.DEFAULT, "开关状态不能为空");
        }
        final String envCode = WebPluginUtils.traceEnvCode();
        final String tenantAppKey = WebPluginUtils.traceTenantAppKey();
        final String statusVoRedisKey = CommonUtil.generateRedisKeyWithSeparator(Separator.Separator3,
            PRADAR_SWITCH_STATUS_VO + tenantAppKey, envCode);
        final String statusRedisKey = CommonUtil.generateRedisKeyWithSeparator(Separator.Separator3,
            PRADAR_SWITCH_STATUS + tenantAppKey, envCode);
        String realStatus = getUserPressureSwitchFromRedis(statusVoRedisKey, statusRedisKey);
        AppPressureSwitchSetResponse result = new AppPressureSwitchSetResponse();
        if (realStatus.equals(AppSwitchEnum.CLOSING.getCode()) || realStatus.equals(AppSwitchEnum.OPENING.getCode())) {
            result.setSwitchStutus(realStatus);
        } else {
            String status = (enable ? AppSwitchEnum.OPENED : AppSwitchEnum.CLOSED).getCode();
            String voStatus = (enable ? AppSwitchEnum.OPENING : AppSwitchEnum.CLOSING).getCode();
            //开关状态、开关开启、关闭的时间存放在redis
            redisTemplate.opsForValue().set(statusRedisKey, status);
            redisTemplate.opsForValue().set(statusVoRedisKey, voStatus);
            redisTemplate.opsForHash().put(NEED_VERIFY_USER_MAP,
                tenantAppKey + Separator.Separator3.getValue() + envCode, System.currentTimeMillis());
            result.setSwitchStutus(voStatus);
        }
        agentConfigCacheManager.evictPressureSwitch();
        return result;
    }

    @Override
    public ApplicationStatusResponse getAppStatusCount() {
        ApplicationStatusResponse response = new ApplicationStatusResponse();
        response.setApplicationNum(applicationDAO.getApplicationCount());
        response.setAccessErrorNum(applicationService.getAccessErrorNum());
        return response;
    }

    /**
     * 从redis获取用户全局开关状态
     *
     * @return 开关状态
     */
    private String getUserPressureSwitchFromRedis(String statusVoRedisKey, String statusRedisKey) {
        // 返回缓存中的值
        Object status = redisTemplate.opsForValue().get(statusVoRedisKey);
        if (status != null) {
            return (String)status;
        }
        // 默认返回开启
        else {
            redisTemplate.opsForValue().set(statusVoRedisKey,
                AppSwitchEnum.OPENED.getCode());
            redisTemplate.opsForValue().set(statusRedisKey, AppSwitchEnum.OPENED.getCode());
            return AppSwitchEnum.OPENED.getCode();
        }
    }
}
