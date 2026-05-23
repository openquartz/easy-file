package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * 导入任务状态存储默认实现
 *
 * @author svnee
 */
@Service
@RequiredArgsConstructor
public class DefaultImportStorageServiceImpl implements ImportStorageService {

    private static final String STATUS_EXECUTING = "EXECUTING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_NONE = "NONE";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        int startIndex = (pageNum - 1) * pageSize;

        String countSql = "SELECT COUNT(*) FROM ef_async_import_record WHERE app_id = ? "
            + "AND (? IS NULL OR import_status = ?) "
            + "AND (? IS NULL OR create_time >= ?) "
            + "AND (? IS NULL OR create_time <= ?)";
        Long total = jdbcTemplate.queryForObject(
            countSql, Long.class,
            request.getAppId(),
            request.getImportStatus(), request.getImportStatus(),
            request.getStartTime(), request.getStartTime(),
            request.getEndTime(), request.getEndTime());

        if (total == null || total <= 0) {
            Pagination<ImportResult> empty = Pagination.empty();
            empty.setPage(Long.valueOf(pageNum));
            empty.setPageSize(Long.valueOf(pageSize));
            empty.setTotalRecords(0L);
            empty.setModelList(Collections.emptyList());
            return empty;
        }

        String listSql = "SELECT id, import_status, total_count, success_count, fail_count, execute_process, "
            + "result_file_url, error_msg, create_time, last_execute_time "
            + "FROM ef_async_import_record WHERE app_id = ? "
            + "AND (? IS NULL OR import_status = ?) "
            + "AND (? IS NULL OR create_time >= ?) "
            + "AND (? IS NULL OR create_time <= ?) "
            + "ORDER BY create_time DESC LIMIT ?, ?";

        List<ImportResult> modelList = jdbcTemplate.query(
            listSql,
            new ImportResultRowMapper(),
            request.getAppId(),
            request.getImportStatus(), request.getImportStatus(),
            request.getStartTime(), request.getStartTime(),
            request.getEndTime(), request.getEndTime(),
            startIndex, pageSize
        );

        Pagination<ImportResult> pagination = new Pagination<>();
        pagination.setPage(Long.valueOf(pageNum));
        pagination.setPageSize(Long.valueOf(pageSize));
        pagination.setTotalRecords(total);
        pagination.setModelList(modelList);
        return pagination;
    }

    @Override
    public boolean retry(Long registerId, String operator) {
        int affect = jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, execute_process = 0, error_msg = ?, "
                + "update_by = ?, update_time = ? WHERE id = ? AND import_status = ?",
            STATUS_NONE, StringUtils.EMPTY, operator, new Date(), registerId, STATUS_FAILED
        );
        return affect > 0;
    }

    @Override
    public boolean revoke(Long registerId, String operator) {
        int affect = jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, update_by = ?, update_time = ? "
                + "WHERE id = ? AND (import_status = ? OR import_status = ?)",
            STATUS_CANCELLED, operator, new Date(), registerId, STATUS_NONE, STATUS_EXECUTING
        );
        return affect > 0;
    }

    @Override
    public DownloadUrlResult resultFile(Long registerId) {
        String sql = "SELECT result_file_url FROM ef_async_import_record WHERE id = ?";
        List<String> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("result_file_url"), registerId);
        if (result.isEmpty() || StringUtils.isBlank(result.get(0))) {
            return null;
        }

        DownloadUrlResult downloadUrlResult = new DownloadUrlResult();
        downloadUrlResult.setUrl(result.get(0));
        downloadUrlResult.setFileName("import_result_" + registerId + ".xlsx");
        downloadUrlResult.setFileSystem("local");
        return downloadUrlResult;
    }

    @Override
    public List<AppTree> getAppTree() {
        List<String> appIdList = jdbcTemplate.query(
            "SELECT DISTINCT app_id FROM ef_async_import_record WHERE app_id <> ''",
            (rs, rowNum) -> rs.getString("app_id"));
        return appIdList.stream()
            .map(appId -> new AppTree(appId, Collections.singletonList(appId)))
            .collect(Collectors.toList());
    }

    @Override
    public void markExecuting(Long registerId) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record "
                + "SET import_status = ?, execute_process = ?, resume_position = ?, error_msg = ?, update_time = ? "
                + "WHERE id = ? AND import_status <> ? AND import_status <> ?",
            STATUS_EXECUTING, 0, 0L, "", new Date(), registerId, STATUS_COMPLETED, STATUS_CANCELLED);
    }

    @Override
    public void refreshProgress(Long registerId, Integer process, Long resumePosition) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record "
                + "SET import_status = ?, execute_process = ?, resume_position = ?, update_time = ? "
                + "WHERE id = ?",
            STATUS_EXECUTING, process, resumePosition, new Date(), registerId);
    }

    @Override
    public void updateTotalCount(Long registerId, Long totalCount) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET total_count = ?, update_time = ? WHERE id = ?",
            totalCount, new Date(), registerId);
    }

    @Override
    public void updateFileHash(Long registerId, String fileHash) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET file_hash = ?, update_time = ? WHERE id = ?",
            fileHash, new Date(), registerId);
    }

    @Override
    public void updateResultFileUrl(Long registerId, String resultFileUrl) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET result_file_url = ?, update_time = ? WHERE id = ?",
            resultFileUrl, new Date(), registerId);
    }

    @Override
    public void complete(Long registerId, boolean success, Long totalCount, Long successCount,
                         Long failCount, Long resumePosition, String errorMsg) {
        String status = success ? STATUS_COMPLETED : STATUS_FAILED;
        int process = success ? 100 : 0;
        jdbcTemplate.update(
            "UPDATE ef_async_import_record "
                + "SET import_status = ?, execute_process = ?, total_count = ?, success_count = ?, fail_count = ?, "
                + "resume_position = ?, error_msg = ?, update_time = ? "
                + "WHERE id = ?",
            status,
            process,
            totalCount,
            successCount,
            failCount,
            resumePosition,
            errorMsg,
            new Date(),
            registerId);
    }

    private static final class ImportResultRowMapper implements RowMapper<ImportResult> {
        @Override
        public ImportResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ImportResult.builder()
                .registerId(rs.getLong("id"))
                .success(STATUS_COMPLETED.equals(rs.getString("import_status")))
                .status(rs.getString("import_status"))
                .process(rs.getInt("execute_process"))
                .totalCount(rs.getLong("total_count"))
                .successCount(rs.getLong("success_count"))
                .failCount(rs.getLong("fail_count"))
                .resultFileUrl(rs.getString("result_file_url"))
                .errorMsg(rs.getString("error_msg"))
                .startTime(rs.getTimestamp("create_time"))
                .endTime(rs.getTimestamp("last_execute_time"))
                .build();
        }
    }
}
