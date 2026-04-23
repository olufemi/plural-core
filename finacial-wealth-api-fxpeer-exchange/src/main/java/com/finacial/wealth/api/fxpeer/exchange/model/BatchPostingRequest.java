package com.finacial.wealth.api.fxpeer.exchange.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BatchPostingRequest {
    private String groupRef;
    private List<BatchPostingLegRequest> legs = new ArrayList<>();
}
