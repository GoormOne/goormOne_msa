package com.example.storeservice.batch;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.dto.ReviewQueryFlatRow;
import com.example.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

@Slf4j
public class QueryFlatRowsPageReader implements ItemStreamReader<List<ReviewQueryFlatRow>> {

    private final StoreRepository repo;
    private final int pageSize;
    private int page = 0;
    private boolean exhausted = false;

    public QueryFlatRowsPageReader(StoreRepository repo, int pageSize) {
        this.repo = repo;
        this.pageSize = pageSize;
    }

    @Override
    public List<ReviewQueryFlatRow> read() {
        if (exhausted) return null;

        List<ReviewQueryFlatRow> rows = repo.findQueryFlatRows(page, pageSize);
        for (ReviewQueryFlatRow row : rows) {
            log.info("ezra QueryFlatRowsPageReader read result: {}", row.toString());

        }
        if (rows == null || rows.isEmpty()) {
            exhausted = true;
            return null;
        }

        page++;
        return rows; // 한 페이지 전체를 아이템 하나로 반환
    }

    // ExecutionContext 저장해서 재시작 지원하려면 open/update/close 구현 추가
    @Override
    public void open(ExecutionContext executionContext) {
        if (executionContext.containsKey("flat.page")) {
            this.page = executionContext.getInt("flat.page");
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.putInt("flat.page", this.page);
    }

    @Override
    public void close() { }
}
