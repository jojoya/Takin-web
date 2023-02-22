package io.shulie.takin.web.biz.service.elasticjoblite;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.pamirs.takin.common.constant.VerifyResultStatusEnum;
import com.pamirs.takin.common.constant.VerifyTypeEnum;
import com.pamirs.takin.common.util.JdbcConnection;
import com.pamirs.takin.common.util.SpringUtil;
import io.shulie.takin.web.biz.pojo.request.leakverify.LeakVerifyTaskJobParameter;
import io.shulie.takin.web.biz.pojo.request.leakverify.VerifyTaskConfig;
import io.shulie.takin.web.biz.service.VerifyTaskService;
import io.shulie.takin.web.common.exception.ExceptionCode;
import io.shulie.takin.web.common.exception.TakinWebException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author fanxx
 * @date 2021/1/7 8:31 下午
 */
public class VerifyJob  {

    private static final Logger logger = LoggerFactory.getLogger(VerifyJob.class);
    
    public void execute(LeakVerifyTaskJobParameter jobParameter) {
        Integer refType = jobParameter.getRefType();
        Long refId = jobParameter.getRefId();
        VerifyTypeEnum typeEnum = VerifyTypeEnum.getTypeByCode(refType);

        logger.info("开始执行验证任务[refType:{},refId:{}]", Objects.requireNonNull(typeEnum).name(), refId);
        List<VerifyTaskConfig> taskConfigs = jobParameter.getVerifyTaskConfigList();
        Map<Integer, Integer> resultMap = verify(refType, refId, taskConfigs);

        logger.info("验证任务已完成，验证结果入库:[refType:{},refId:{}]", typeEnum.name(), refId);
        VerifyTaskService verifyTaskService = (VerifyTaskService)SpringUtil.getBean("verifyTaskServiceImpl");
        verifyTaskService.saveVerifyResult(jobParameter, resultMap);


    }

    public Map<Integer, Integer> run(LeakVerifyTaskJobParameter jobParameter) {
        Integer refType = jobParameter.getRefType();
        Long refId = jobParameter.getRefId();
        VerifyTypeEnum typeEnum = VerifyTypeEnum.getTypeByCode(refType);

        logger.info("开始运行验证任务[refType:{},refId:{}]", Objects.requireNonNull(typeEnum).name(), refId);
        logger.info("任务参数:[{}]", JSON.toJSONString(jobParameter));
        List<VerifyTaskConfig> taskConfigs = jobParameter.getVerifyTaskConfigList();
        return verify(refType, refId, taskConfigs);
    }

    private Map<Integer, Integer> verify(Integer refType, Long refId, List<VerifyTaskConfig> taskConfigs) {
        Map<Integer, Integer> resultMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(taskConfigs)) {
            taskConfigs.forEach(taskConfig -> {
                String jdbcUrl = taskConfig.getJdbcUrl();
                String username = taskConfig.getUsername();
                String password = taskConfig.getPassword();
                Long datasourceId = taskConfig.getDatasourceId();
                List<String> sqls = taskConfig.getSqls();
                Connection connection = null;
                try {
                    connection = JdbcConnection.generateConnection(jdbcUrl, username, password);
                    if (Objects.isNull(connection)) {
                        throw new TakinWebException(ExceptionCode.VERIFY_TASK_RUN_FAILED, "获取数据库连接失败");
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    logger.error("Verify job run error:", e);
                }
                try {
                    Connection finalConnection = connection;
                    sqls.forEach(sql -> {
                        String keyString = refType + refId + datasourceId + sql;
                        Integer key = keyString.hashCode();
                        try {
                            Statement statement = finalConnection.createStatement();
                            statement.setQueryTimeout(30);
                            ResultSet resultSet = statement.executeQuery(sql);
                            Integer count = resultSet.next() ? VerifyResultStatusEnum.LEAKED.getCode()
                                : VerifyResultStatusEnum.NORMAL.getCode();
                            logger.info("sql漏数验证结果:[jdbcUrl:{},sql:{},count:{}]", jdbcUrl, sql, count);
                            resultMap.put(key, count);
                            resultSet.close();
                            statement.close();
                        } catch (SQLException throwables) {
                            resultMap.put(key, VerifyResultStatusEnum.FAILED.getCode());
                            logger.error("Verify job run error:", throwables);
                        }
                    });
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            logger.error("error:", e);
                        }
                    }
                }
            });
        } else {
            logger.warn("漏数验证配置为空，不予检测");
        }
        return resultMap;
    }
}
