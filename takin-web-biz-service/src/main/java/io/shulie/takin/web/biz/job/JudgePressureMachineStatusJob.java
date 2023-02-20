package io.shulie.takin.web.biz.job;

import com.pamirs.takin.common.util.DateUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.shulie.takin.common.beans.page.PagingList;
import io.shulie.takin.web.biz.pojo.response.perfomanceanaly.PressureMachineResponse;
import io.shulie.takin.web.biz.service.perfomanceanaly.PressureMachineService;
import io.shulie.takin.web.common.enums.config.ConfigServerKeyEnum;
import io.shulie.takin.web.data.param.machine.PressureMachineQueryParam;
import io.shulie.takin.web.data.util.ConfigServerHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author 无涯
 * @date 2021/6/15 5:57 下午
 * 一分钟统计一次，压力机没更新时间距离当前超过3分钟，记为离线状态
 */
@Component
@Slf4j
public class JudgePressureMachineStatusJob  {

    @Autowired
    private PressureMachineService pressureMachineService;

    @XxlJob("judgePressureMachineStatusJobExecute")
    public void execute() {
        doJudgePressureMachineStatus();
    }

    private void doJudgePressureMachineStatus() {
        PressureMachineQueryParam param = new PressureMachineQueryParam();
        param.setCurrent(0);
        param.setPageSize(Integer.MAX_VALUE);
        PagingList<PressureMachineResponse> pressureMachineResponsePagingList = pressureMachineService.queryByExample(param);
        List<PressureMachineResponse> list = pressureMachineResponsePagingList.getList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (PressureMachineResponse machine : list) {
            if (machine.getStatus() == -1) {
                continue;
            }
            Date updateTime = DateUtils.strToDate(machine.getGmtUpdate(), null);
            long machineUploadIntervalTime = Long.parseLong(
                ConfigServerHelper.getValueByKey(ConfigServerKeyEnum.TAKIN_PRESSURE_MACHINE_UPLOAD_INTERVAL_TIME));
            if (updateTime != null && System.currentTimeMillis() - updateTime.getTime() > machineUploadIntervalTime) {
                //认为机器处于离线状态
                pressureMachineService.updatePressureMachineStatus(machine.getId(), -1);
            }
        }
    }
}
