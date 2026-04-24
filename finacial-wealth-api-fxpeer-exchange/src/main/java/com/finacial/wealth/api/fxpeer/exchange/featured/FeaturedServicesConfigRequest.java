package com.finacial.wealth.api.fxpeer.exchange.featured;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FeaturedServicesConfigRequest {

    @Valid
    private List<FeaturedServiceConfigItem> items = new ArrayList<>();
}
