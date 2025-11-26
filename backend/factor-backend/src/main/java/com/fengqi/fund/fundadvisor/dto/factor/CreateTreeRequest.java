package com.fengqi.fund.fundadvisor.dto.factor;

import lombok.Data;

@Data
public class CreateTreeRequest {
    private String treeName;
    private String description;
    private String sceneId;
}