package io.shulie.takin.web.data.dao.baseserver;

import java.util.Collection;
import java.util.List;

import io.shulie.takin.web.data.param.baseserver.*;
import io.shulie.takin.web.data.result.baseserver.BaseServerResult;
import io.shulie.takin.web.data.result.baseserver.InfluxAvgResult;
import io.shulie.takin.web.data.result.baseserver.LinkDetailResult;
import io.shulie.takin.web.data.result.risk.BaseRiskResult;
import io.shulie.takin.web.data.result.risk.LinkDataResult;

/**
 * @author mubai
 * @date 2020-10-26 15:47
 */
public interface BaseServerDao {

    Collection<BaseServerResult> queryBaseServer(BaseServerParam param);

    Collection<InfluxAvgResult> queryTraceId(InfluxAvgParam param);

    LinkDetailResult queryTimeMetricsDetail(TimeMetricsDetailParam param);

    LinkDataResult queryTimeMetrics(TimeMetricsParam param);

    List<BaseRiskResult> queryProcessBaseRisk(ProcessBaseRiskParam param);
    List<BaseRiskResult> queryProcessOverRisk(ProcessOverRiskParam param);

    Collection<BaseServerResult> queryBaseData(BaseServerParam param);

    List<BaseServerResult> listBaseServerResult(AppBaseDataQuery query);
}
