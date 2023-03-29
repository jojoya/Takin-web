package com.pamirs.takin.entity.domain.vo.excel;

import java.util.List;

import com.alibaba.excel.annotation.ExcelProperty;


/**
 * @author  vernon
 * @date 2019/11/1 01:17
 */
public class WhiteListExcelVo  {
    @ExcelProperty(value = "负责人工号", index = 0)
    private String principalNo;
    @ExcelProperty(value = "应用名称", index = 1)
    private String applicationName;
    @ExcelProperty(value = "白名单类型", index = 2)
    private String type;
    @ExcelProperty(value = "HTTP 类型", index = 3)
    private String httpType;
    @ExcelProperty(value = "JOB 调度间隔", index = 4)
    private String jobInterval;
    @ExcelProperty(value = "MQ类型选择框", index = 5)
    private String mqType;
    @ExcelProperty(value = "是否可用", index = 6)
    private String useYn;
    @ExcelProperty(value = "接口列表", index = 7)
    private List<String> list;

    public String getPrincipalNo() {
        return principalNo;
    }

    public void setPrincipalNo(String principalNo) {
        this.principalNo = principalNo;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHttpType() {
        return httpType;
    }

    public void setHttpType(String httpType) {
        this.httpType = httpType;
    }

    public String getJobInterval() {
        return jobInterval;
    }

    public void setJobInterval(String jobInterval) {
        this.jobInterval = jobInterval;
    }

    public String getMqType() {
        return mqType;
    }

    public void setMqType(String mqType) {
        this.mqType = mqType;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
