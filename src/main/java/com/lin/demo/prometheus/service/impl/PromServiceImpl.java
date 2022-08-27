package com.lin.demo.prometheus.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.lin.demo.dto.PromDo;
import com.lin.demo.prometheus.entity.NodeInfo;
import com.lin.demo.prometheus.service.PromService;
import com.lin.demo.utils.PromUtils;
import com.lin.demo.vo.PageHelper;
import com.lin.demo.vo.ResultVo;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PromServiceImpl implements PromService {
    @Override
    public ResultVo getCpuUsedRate(String ip) {
        String format = String.format("(((count(count(node_cpu_seconds_total{instance='%s',job='%s'}) by (cpu))) " +
                "- avg(sum by (mode)(rate(node_cpu_seconds_total{mode='idle',instance='%s',job='%s'}[5m])))) * 100) " +
                "/ count(count(node_cpu_seconds_total{instance='%s',job='%s'}) " +
                "by (cpu))", ip, "node", ip, "node", ip, "node");
        String value = (String) PromUtils.getPromSingleValue(format);
        return ResultVo.success(value);
    }

    @Override
    public ResultVo getMemoryUsedRate(String ip) {
        String format = String.format("100 - ((node_memory_MemAvailable_bytes{instance='%s',job='%s'} * 100) " +
                "/ node_memory_MemTotal_bytes{instance='%s',job='%s'})", ip, "node", ip, "node");
        String value = (String) PromUtils.getPromSingleValue(format);
        return ResultVo.success(value);
    }

    @Override
    public ResultVo getDiskUsedRate(String ip) {
        String format = String.format("100 - ((node_filesystem_avail_bytes{instance='%s',job='%s'" +
                ",mountpoint=\"/\",fstype!=\"rootfs\"} * 100) " +
                "/ node_filesystem_size_bytes{instance='%s',job='%s',mountpoint=\"/\",fstype!=\"rootfs\"})",
                ip, "node", ip, "node", ip, "node");
        String value = (String) PromUtils.getPromSingleValue(format);
        return ResultVo.success(value);
    }

    @Override
    public ResultVo getNodeInfo(String ip) {
        String formatCpu = String.format("count(count(node_cpu_seconds_total{instance='%s',job='%s'}) by (cpu))",
                ip, "node");
        String formatDisk = String.format("node_filesystem_size_bytes{instance='%s',job='%s',mountpoint=\"/\",fstype!=\"rootfs\"}",
                ip, "node");
        String formatMem = String.format("node_memory_MemTotal_bytes{instance='%s',job='%s'}",
                ip, "node");
        String formatSwap = String.format("node_memory_SwapTotal_bytes{instance='%s',job='%s'}",
                ip, "node");
        String formatRun = String.format("node_time_seconds{instance='%s',job='%s'} - node_boot_time_seconds{instance='%s',job='%s'}",
                ip, "node", ip,"node");

        String cpu = (String) PromUtils.getPromSingleValue(formatCpu);
        String disk = (String) PromUtils.getPromSingleValue(formatDisk);
        String memory = (String) PromUtils.getPromSingleValue(formatMem);
        String swap = (String) PromUtils.getPromSingleValue(formatSwap);
        String running = (String) PromUtils.getPromSingleValue(formatRun);
        NodeInfo nodeInfo = new NodeInfo(cpu, memory, disk, swap, running);
        return ResultVo.success(nodeInfo);

    }

    @Override
    public ResultVo getAlertCount(int days, String systemName) {
        return null;
    }

    @Override
    public ResultVo getInspectionCount() {
        String format = String.format("count_over_time(probe_success[1d])");
        JSONArray result = PromUtils.getPromResult(format);
        return ResultVo.success(PromUtils.getPromResultMap(result));
    }

    @Override
    public ResultVo getInspectionSuccessRate() {
        String format = String.format("(sum_over_time((probe_success > bool 0)[5m:10s])/count_over_time(probe_success [5m]))");
        JSONArray promResult = PromUtils.getPromResult(format);
        return ResultVo.success(PromUtils.getPromResultMap(promResult));
    }

    @Override
    public ResultVo getInspectionList(PromDo promDo) {
        Integer pageCounts = 0;
        Integer recordTotal = 0;
        if(Objects.isNull(promDo)) return ResultVo.success(new PageHelper<>(pageCounts,pageCounts,null));

        String format = String.format("probe_success");
        JSONArray promResult = PromUtils.getPromResult(format);
        return ResultVo.success(PromUtils.getPromResultDate(promResult));
    }
}
