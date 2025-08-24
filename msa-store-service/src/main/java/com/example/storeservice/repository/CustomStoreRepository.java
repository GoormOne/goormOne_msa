package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import java.util.*;

public interface CustomStoreRepository {
    List<AiFlatRow> findFlatRows(Collection<UUID> storeIds);
    List<AiFlatRow> findFlatRowsPage(int page, int size);
}
