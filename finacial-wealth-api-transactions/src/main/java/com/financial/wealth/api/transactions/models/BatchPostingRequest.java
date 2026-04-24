package com.financial.wealth.api.transactions.models;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchPostingRequest {

    @NotNull(message = "the field \"groupRef\" is not nillable")
    private String groupRef;

    private String productCode;

    @Valid
    @NotEmpty(message = "the field \"legs\" must contain at least one item")
    private List<BatchPostingLegRequest> legs = new ArrayList<>();
}
