package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

@Data
public class AddNodeRequest {
    private Integer parentId;
    private String nodeName;
    private String nodeType;
    private Integer factorId;
    private String description;
}