package com.lin.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PromDo implements Serializable {
    private static final long serialVersionUID = 97654L;

    /**
     * 巡检结果
     */
    private String result;

    /**
     * 选件系统名称
     */
    private String system;

    /**
     * 巡检服务名称
     */
    private String service;

    /**
     * 巡检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String promTime;

    /**
     * 每页展示条数
     */
    private Integer pageSize;

    /**
     * 页码
     */
    private Integer pageNumber;
}
