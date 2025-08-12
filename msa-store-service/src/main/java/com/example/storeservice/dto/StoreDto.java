package com.example.storeservice.dto;


import com.example.storeservice.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDto {

    private String storeId;
    private String storeName;
    private String description;
    private String category;
    private AddressDto address;           // 주소 객체
    private String zip_cd;
    private String store_phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private StoreLocationDto store_location;  // 위치 객체

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String address1;  // 상세 주소 제외
        private String address2;  // 상세 주소
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreLocationDto {
        private String store_latitude;
        private String store_longtitude;
    }


    public static StoreDto fromEntity(Store store) {
        if (store == null) {
            return null;
        }

        return StoreDto.builder()
                .storeId(store.getStoreId().toString())
                .storeName(store.getStoreName())
                .description(store.getStoreDescription())
                .category(store.getStoresCategoryId() != null ? store.getStoresCategoryId().toString() : null) // 카테고리 id를 String으로
                .address(AddressDto.builder()
                        .address1(store.getAddress1())
                        .address2(store.getAddress2())
                        .build())
                .zip_cd(store.getZipCd())
                .store_phone(store.getStorePhone())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .store_location(StoreLocationDto.builder()
                        .store_latitude(store.getStoreLatitude() != null ? store.getStoreLatitude().toString() : null)
                        .store_longtitude(store.getStoreLongitude() != null ? store.getStoreLongitude().toString() : null)
                        .build())
                .build();
    }

}

