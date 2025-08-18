package com.example.storeservice.dto;


import com.example.storeservice.entity.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class StoreRegisterDto {

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String storeName;

    private String owerId;
    private String category;

    private String storeDescription;

    @NotBlank(message = "가게 주소는 필수입니다.")
    private String address1;

    private String address2;


    private String zipCd;

    @NotBlank(message = "가게 전화번호는 필수입니다.")
    private String storePhone;


    private BigDecimal storeLatitude;
    private BigDecimal storeLongitude;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime;


    public Store toEntity(UUID storeId) {
        return Store.builder()
                .storeId(storeId) // audit에서 받은 PK
                .ownerId(UUID.fromString(this.owerId))                // 문자열 → UUID
                .storesCategoryId(UUID.fromString(this.category))     // 문자열 → UUID
                .storeName(this.storeName)
                .storeDescription(this.storeDescription)
                .address1(this.address1)
                .address2(this.address2)
                .zipCd(this.zipCd)
                .storePhone(this.storePhone)
                .storeLatitude(this.storeLatitude)
                .storeLongitude(this.storeLongitude)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .isBanned(false)
                .build();
    }
}