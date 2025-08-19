package com.example.storeservice.dto;

import com.example.storeservice.entity.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 제외
public class StoreDto {

    private UUID storeId;
    private UUID ownerId;

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    private String description;
    @NotBlank(message = "상점 카테고리는 필수입니다.")
    private UUID storeCategoryId;

    @NotBlank(message = "주소(address1)는 필수입니다.")
    private String address1;  // 상세 제외
    private String address2;  // 상세 주소

    @JsonProperty("zip_cd")
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCd;

    @JsonProperty("store_phone")
    @Pattern(
            regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다. 예) 02-1234-5678 또는 010-1234-5678"
    )
    private String storePhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime openTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime closeTime;

    @JsonProperty("store_latitude")
    @Pattern(
            regexp = "^-?\\d{1,2}(\\.\\d+)?$",
            message = "위도 형식이 올바르지 않습니다."
    )
    private String storeLatitude;

    @JsonProperty("store_longitude")
    @Pattern(
            regexp = "^-?\\d{1,3}(\\.\\d+)?$",
            message = "경도 형식이 올바르지 않습니다."
    )
    private String storeLongitude;
    private boolean isDeleted = Boolean.FALSE;

    public static StoreDto fromEntity(Store store) {
        if (store == null) return null;

        return StoreDto.builder()
                .storeId(store.getStoreId() != null ? store.getStoreId() : null)
                .storeName(store.getStoreName())
                .description(store.getStoreDescription())
                .storeCategoryId(store.getStoresCategoryId() != null ? store.getStoresCategoryId() : null)
                .address1(store.getAddress1())
                .address2(store.getAddress2())
                .zipCd(store.getZipCd())
                .storePhone(store.getStorePhone())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .storeLatitude(store.getStoreLatitude() != null ? store.getStoreLatitude().toString() : null)
                .storeLongitude(store.getStoreLongitude() != null ? store.getStoreLongitude().toString() : null)
                .isDeleted(store.getIsDeleted())
                .build();
    }
}
