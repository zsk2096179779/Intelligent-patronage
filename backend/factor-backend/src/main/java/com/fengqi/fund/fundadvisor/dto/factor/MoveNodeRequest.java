package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

@Data
public class MoveNodeRequest {
    private Integer nodeId;
    private Integer newParentId;
    private Integer sortOrder;
}