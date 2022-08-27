package com.lin.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class PromData implements Serializable {
    private static final long serialVersionUID = -123456L;

    private String result;
    private String system;
    private String service;
    private String url;
    private String port;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime promTime;
}
