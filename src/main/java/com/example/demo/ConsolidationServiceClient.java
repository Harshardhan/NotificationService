package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consolidation-service", path = "/api/consolidations")
public interface ConsolidationServiceClient {

    @GetMapping("/{id}")
    Consolidation getConsolidationDetails(@PathVariable("id") Long id);

}
