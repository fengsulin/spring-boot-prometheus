package com.lin.demo.prometheus.service;

import com.lin.demo.dto.PromDo;
import com.lin.demo.vo.ResultVo;

public interface PromService {

    /**
     * 获取cpu平均使用率，默认5m
     * @param ip
     * @return
     */
    public ResultVo getCpuUsedRate(String ip);

    /**
     * 获取内存的平均使用率，默认5m内
     * @param ip
     * @return
     */
    public ResultVo getMemoryUsedRate(String ip);

    /**
     * 获取磁盘平均使用率，默认5m内
     * @param ip
     * @return
     */
    public ResultVo getDiskUsedRate(String ip);

    /**
     * 获取主机信息
     * @param ip
     * @return
     */
    public ResultVo getNodeInfo(String ip);

    /**
     * 获取告警数量,1天和7天内
     * @param days
     * @param systemName
     * @return
     */
    public ResultVo getAlertCount(int days,String systemName);

    /**
     * 获取web应用1天内巡检的次数
     * @return
     */
    public ResultVo getInspectionCount();

    /**
     * 获取web应用1天内巡检成功率
     * @return
     */
    public ResultVo getInspectionSuccessRate();

    /**
     * 获取巡检列表
     * @param promDo
     * @return
     */
    public ResultVo getInspectionList(PromDo promDo);
}
