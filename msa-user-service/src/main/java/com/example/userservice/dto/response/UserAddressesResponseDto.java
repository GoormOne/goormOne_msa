package com.example.userservice.dto.response;

import com.profect.delivery.global.entity.UserAddress;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserAddressesResponseDto {

    private String address_name;
    private String address1;
    private String address2;
    private String zip_cd;




    public static UserAddressesResponseDto fromEntity(UserAddress address) {
        UserAddressesResponseDto dto = new UserAddressesResponseDto();
        dto.address_name = address.getAddressName();
        dto.address1 = address.getAddress1();
        dto.address2 = address.getAddress2();
        dto.zip_cd = address.getZipCd();
        return dto;
    }


}