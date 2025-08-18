package com.example.storeservice.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireStoreOwner {
    String storeIdParam() default "storeId"; // PathVariable
    boolean allowAdmin() default true;  // 관리자 우회 허용
}
