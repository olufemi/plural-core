/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.fxpeer;

import org.springframework.cloud.openfeign.FeignClient;

/**
 *
 * @author olufemioshin
 */
@FeignClient(
        name = "profiling-service",
        contextId = "pofilingClient",
        configuration = com.finacial.wealth.backoffice.config.FeignConfig.class
)
public interface ProfilingManagementClient {

}
