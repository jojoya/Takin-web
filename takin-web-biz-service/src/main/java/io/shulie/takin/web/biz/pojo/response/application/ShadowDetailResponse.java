package io.shulie.takin.web.biz.pojo.response.application;

import com.pamirs.attach.plugin.dynamic.one.template.Info;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: 南风
 * @Date: 2021/9/6 8:58 下午
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShadowDetailResponse implements Serializable {

    @ApiModelProperty("记录id")
    private Long id;

    @ApiModelProperty("应用id")
    private String applicationId;

    @ApiModelProperty("中间件类型")
    private String middlewareType;

    @ApiModelProperty("中间件名称")
    private String connectionPool;

    @ApiModelProperty("影子方案")
    private Integer dsType;

    @ApiModelProperty("缓存模式")
    private String cacheType;


    @ApiModelProperty("业务数据源")
    private String url;

    @ApiModelProperty("业务数据源用户名")
    private String username;

    @ApiModelProperty("业务数据源密码")
    private String password;

    @ApiModelProperty("影子数据源额外配置")
    private String shadowInfo;

    @ApiModelProperty("影子数据源额外配置")
    private List<TableInfo> tables;

    /**
     * 0:amdb 1:手动录入
     */
    @ApiModelProperty("是否手动")
    private Integer isManual;

    //-------缓存字段拆分------------
    private String cacheNodes;
    private String cacheMaster;
    private String cacheDatabase;
    private String cacheShadowNodes;
    private String cacheShadowMaster;
    private String cacheShadowPassword;
    private String cacheShadowDatabase;
    //-------缓存字段拆分------------

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableInfo {
        @Info(describe = "压测资源准备-资源Id")
        private Long resourceId;

        @Info(describe = "压测资源准备-详情Id")
        private Long detailId;

        @Info(describe = "业务库")
        private String bizDatabase;

        @Info(describe = "业务表")
        private String bizTableName;

        @Info(describe = "影子表")
        private String shaDowTableName;

        @Info(describe = "手动添加标识")
        private Boolean isManual;

        @Info(describe = "选中标识")
        private Boolean isCheck;

        @Info(describe = "读写状态")
        private String readWriteStatus;

        public TableInfo(String bizDatabase, String bizTableName, Integer canRead, Integer canWrite) {
            this.bizDatabase = bizDatabase;
            this.bizTableName = bizTableName;
            this.shaDowTableName = "";
            this.isManual = false;
            this.isCheck = false;
            if (canRead != null && canRead == 1) {
                readWriteStatus = "只读";
            }
            if (canWrite != null && canWrite == 1) {
                readWriteStatus = "只写";
            }
            if (canRead != null && canRead == 1 && canWrite != null && canWrite == 1){
                readWriteStatus = "读写";
            }
        }

        public TableInfo(String bizDatabase, String bizTableName, Integer manualTag, String shaDowTableName, Integer isCheck) {
            this.bizDatabase = bizDatabase;
            this.bizTableName = bizTableName;
            this.shaDowTableName = shaDowTableName;
            this.isManual = manualTag == 1;
            this.isCheck = isCheck == 1;
        }
    }
}



