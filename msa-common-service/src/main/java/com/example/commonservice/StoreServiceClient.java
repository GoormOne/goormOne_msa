package com.example.commonservice;

import com.example.common.dto.ApiResponse;
import com.example.commonservice.dto.StoreDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(
        name = "msa-store-service",
        path = "/stores"
)
public interface StoreServiceClient {

    @GetMapping("/{storeId}")
    ApiResponse<StoreDto> getStore(@PathVariable("storeId") String storeId);

}