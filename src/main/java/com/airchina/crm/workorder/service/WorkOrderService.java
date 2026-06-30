package com.airchina.crm.workorder.service;

import com.airchina.crm.workorder.dto.WorkOrderCreateDTO;
import com.airchina.crm.workorder.entity.WorkOrder;
import com.airchina.crm.workorder.entity.WorkOrderFlow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 工单服务接口
 */
public interface WorkOrderService extends IService<WorkOrder> {

    /**
     * 创建工单（自动匹配模板、计算SLA）
     */
    WorkOrder createOrder(WorkOrderCreateDTO dto);

    /**
     * 查询工单详情
     */
    WorkOrder getOrderDetail(Long orderId);

    /**
     * 指派工单
     */
    void assignOrder(Long orderId, String assignedTo, String operator);

    /**
     * 开始处理
     */
    void startProcessing(Long orderId, String operator);

    /**
     * 完成工单
     */
    void completeOrder(Long orderId, String operator);

    /**
     * 关闭工单
     */
    void closeOrder(Long orderId, String operator, String remark);

    /**
     * 查询工单流转记录
     */
    List<WorkOrderFlow> getFlowHistory(Long orderId);

    /**
     * 查询会员的活跃工单
     */
    List<WorkOrder> getActiveOrdersByMember(Long memberId);
}
