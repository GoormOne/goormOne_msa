package com.example.msacommonservice.controller;



import com.example.common.ApiResponse;
import com.example.msacommonservice.StoreServiceClient;
import com.example.msacommonservice.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common")
@Slf4j
@RequiredArgsConstructor
public class CommonController {
    private final StoreServiceClient  storeServiceClient;

    @GetMapping("/")
    public StoreDto getCommon(){
        log.info("getCommon");
        String storeId = "56191524-d0af-4e47-a597-63577138e263";

        ApiResponse<StoreDto> storeDto = storeServiceClient.getStore(storeId);

        return storeDto.getData();
    }

}
