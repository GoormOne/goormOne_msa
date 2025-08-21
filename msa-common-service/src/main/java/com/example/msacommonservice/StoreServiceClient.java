package com.example.msacommonservice;

import com.example.common.dto.ApiResponse;
import com.example.msacommonservice.dto.StoreDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(
        name = "msa-store-service",
        path = "/stores"
)
public interface StoreServiceClient {

    @GetMapping("/{storeId}")
    ApiResponse<StoreDto> getStore(@PathVariable("storeId") String storeId);

}