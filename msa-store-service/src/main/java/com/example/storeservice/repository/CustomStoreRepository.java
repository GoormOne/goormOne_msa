package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import java.util.*;

public interface CustomStoreRepository {
    List<AiFlatRow> findFlatRows(Collection<UUID> storeIds);

    // 페이지로 Store를 자를 때 편의 메서드(선택)
    List<AiFlatRow> findFlatRowsPage(int page, int size);
    long countActiveStores();
}
