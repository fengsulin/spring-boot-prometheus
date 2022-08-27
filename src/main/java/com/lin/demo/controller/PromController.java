package com.lin.demo.controller;

import com.lin.demo.dto.PromDo;
import com.lin.demo.prometheus.service.PromService;
import com.lin.demo.vo.ResultVo;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/prom")
public class PromController {
    private final PromService promService;

    public PromController(PromService promService) {
        this.promService = promService;
    }

    /**
     * 获取cpu使用率
     * @param instance
     * @return
     */
    @GetMapping("/query/cpuUsedRate")
    public ResultVo getCpuUsedRate(String instance){
        return promService.getCpuUsedRate(instance);
    }

    /**
     * 内存使用率
     * @param instance
     * @return
     */
    @GetMapping("/query/memoryUsedRate")
    public ResultVo getMemoryUsedRate(String instance){
        return promService.getMemoryUsedRate(instance);
    }

    /**
     * 磁盘使用率
     * @param instance
     * @return
     */
    @GetMapping("/query/diskUsedRate")
    public ResultVo getDiskUsedRate(String instance){
        return promService.getDiskUsedRate(instance);
    }

    /**
     * 获取主机的基本信息（cpu，内存，磁盘。。。）
     * @param instance
     * @return
     */
    @GetMapping("/query/nodeInfo")
    public ResultVo getNodeInfo(String instance){
        return promService.getNodeInfo(instance);
    }

    /**
     * 获取每个服务的巡检次数
     * @return
     */
    @GetMapping("/query/inspection/number")
    public ResultVo getInspectionNumber(){
        return promService.getInspectionCount();
    }

    /**
     * 获取每个服务巡检成功率
     * @return
     */
    @GetMapping("/query/inspection/rate")
    public ResultVo getInspectionSuccessRate(){
        return promService.getInspectionSuccessRate();
    }

    @PostMapping("/query/inspection/list")
    public ResultVo getInspectionList(@RequestBody(required = false) PromDo promDo){
        return promService.getInspectionList(promDo);
    }
}
