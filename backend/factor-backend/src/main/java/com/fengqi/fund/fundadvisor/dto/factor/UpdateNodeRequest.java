package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

@Data
public class UpdateNodeRequest {
    private String nodeName;
    private String description;
    private Integer sortOrder;
}