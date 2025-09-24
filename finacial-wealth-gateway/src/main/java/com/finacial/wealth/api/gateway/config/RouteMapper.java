package com.finacial.wealth.api.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;

public class RouteMapper implements ServiceRouteMapper {

   private Logger logger = LoggerFactory.getLogger(RouteMapper.class);
  
  /**
   * The service name patterns is <name>-service e.g. sample-service
   * then the URL will be accessed as http://localhost:9002/api/sample
   * this mapper removes the "-service" prefix if it's part of the service name
   */
  @Override
  public String apply(String serviceId) {

      if(serviceId.endsWith("-service"))
        return serviceId.substring(0, serviceId.lastIndexOf("-service"));

      return serviceId;

  }

}
