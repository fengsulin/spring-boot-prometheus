package com.lin.demo.prometheus.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodeInfo {
    private String cpu;
    private String memory;
    private String disk;
    private String swap;
    private String runTime;
}
