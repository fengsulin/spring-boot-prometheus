package com.lin.demo.prometheus.constants;

public class PromConstants {
    public static final String SUCCESS = "success";
    public static final String QUERY = "query";
    public static final String PROM_URL = "http://192.168.2.131:9090/api/v1/query";
    public static final String NODE_MEMORY_MEMTOTAL_BYTES = "node_memory_MemTotal_bytes";
    public static final String AVG_MEMORY_USED = "(((count(count(node_cpu_seconds_total{instance=\"192.168.2.131:9100\",job=\"node\"}) by (cpu))) - avg(sum by (mode)(rate(node_cpu_seconds_total{mode='idle',instance=\"192.168.2.131:9100\",job=\"node\"}[1m])))) * 100) / count(count(node_cpu_seconds_total{instance=\"192.168.2.131:9100\",job=\"node\"}) by (cpu))";

    /**主机系统信息*/
    public static final String NODE_UNAME_INFO = "node_uname_info";

}
