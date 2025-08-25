package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.dto.ReviewQueryFlatRow;

import java.util.*;

public interface CustomStoreRepository {
    List<AiFlatRow> findFlatRows(int page, int size);
    List<ReviewQueryFlatRow> findQueryFlatRows(int page, int size);
    List<UUID> findFlatRowsPage(int page, int size);
    }
