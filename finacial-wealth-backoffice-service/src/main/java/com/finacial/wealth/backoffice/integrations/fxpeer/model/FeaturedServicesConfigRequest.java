package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FeaturedServicesConfigRequest {

    private List<FeaturedServiceConfigItem> items = new ArrayList<>();
}
