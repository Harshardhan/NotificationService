package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consolidation-service")
public interface ConsolidationServiceClient {

    @GetMapping("/consolidations/{orderId}")
    Consolidation getConsolidationDetails(@PathVariable("orderId") Long orderId);

}
