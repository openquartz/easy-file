package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.exception.DataExecuteErrorCode;
import com.openquartz.easyfile.common.exception.EasyFileException;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.JSONUtil;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

/**
 * 服务端导入存储（JdbcTemplate，状态码对齐 ImportStatusEnum）
 *
 * @author svnee
 */
@Service
@RequiredArgsConstructor
public class DefaultImportStorageServiceImpl implements ImportStorageService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean enableRunning(Long registerId) {
        ImportRecordRow row = findById(registerId);
        if (row == null || row.importStatus != ImportStatusEnum.NONE) {
            return false;
        }
        int affect = jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, update_time = ? "
                + "WHERE id = ? AND import_status = ?",
            ImportStatusEnum.EXECUTING.getCode(), new Date(), registerId, ImportStatusEnum.NONE.getCode());
        return affect > 0;
    }

    @Override
    public Long register(ImportRegisterRequest request) {
        Long taskId = findTaskId(request.getAppId(), request.getImportCode());
        if (taskId == null) {
            throw new EasyFileException(DataExecuteErrorCode.NOT_EXIST_ERROR);
        }
        Date now = new Date();
        String operator = StringUtils.isNotBlank(request.getCreateBy()) ? request.getCreateBy() : Constants.SYSTEM_USER;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO ef_async_import_record("
                    + "import_task_id, app_id, import_code, import_status, source_file_url, source_file_name, "
                    + "file_type, file_hash, business_unique_key, result_file_url, total_count, success_count, "
                    + "fail_count, execute_process, resume_position, notify_enable_status, notify_email, "
                    + "max_server_retry, current_retry, execute_param, error_msg, last_execute_time, version, "
                    + "create_time, update_time, create_by, update_by, locale) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setLong(i++, taskId);
            ps.setString(i++, request.getAppId());
            ps.setString(i++, request.getImportCode());
            ps.setString(i++, ImportStatusEnum.NONE.getCode());
            ps.setString(i++, blankToEmpty(request.getOssUrl()));
            ps.setString(i++, blankToEmpty(request.getFileName()));
            ps.setString(i++, blankToEmpty(request.getFileType()));
            ps.setString(i++, StringUtils.EMPTY);
            ps.setString(i++, blankToEmpty(request.getBusinessUniqueKey()));
            ps.setString(i++, StringUtils.EMPTY);
            ps.setLong(i++, 0L);
            ps.setLong(i++, 0L);
            ps.setLong(i++, 0L);
            ps.setInt(i++, 0);
            ps.setLong(i++, 0L);
            ps.setInt(i++, Boolean.TRUE.equals(request.getEnableNotify())
                ? EnableStatusEnum.ENABLE.getCode() : EnableStatusEnum.DISABLE.getCode());
            ps.setString(i++, blankToEmpty(request.getNotifyEmail()));
            ps.setInt(i++, request.getMaxRetry() == null ? 0 : request.getMaxRetry());
            ps.setInt(i++, 0);
            ps.setString(i++, JSONUtil.toClassJson(request));
            ps.setString(i++, StringUtils.EMPTY);
            ps.setTimestamp(i++, new java.sql.Timestamp(now.getTime()));
            ps.setInt(i++, Constants.DATA_INIT_VERSION);
            ps.setTimestamp(i++, new java.sql.Timestamp(now.getTime()));
            ps.setTimestamp(i++, new java.sql.Timestamp(now.getTime()));
            ps.setString(i++, operator);
            ps.setString(i++, operator);
            ps.setString(i, blankToEmpty(request.getLocale()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new EasyFileException(DataExecuteErrorCode.INSERT_ERROR);
        }
        return key.longValue();
    }

    @Override
    public void autoRegisterTask(AutoTaskRegisterRequest request) {
        if (request == null || MapUtils.isEmpty(request.getImportCodeMap())) {
            return;
        }
        Date now = new Date();
        for (Map.Entry<String, String> entry : request.getImportCodeMap().entrySet()) {
            Long taskId = findTaskId(request.getAppId(), entry.getKey());
            if (taskId == null) {
                jdbcTemplate.update(
                    "INSERT INTO ef_async_import_task(app_id, unified_app_id, task_code, task_desc, "
                        + "enable_status, version, create_time, update_time, create_by, update_by, is_deleted) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    request.getAppId(),
                    StringUtils.isNotBlank(request.getUnifiedAppId()) ? request.getUnifiedAppId() : request.getAppId(),
                    entry.getKey(),
                    entry.getValue(),
                    EnableStatusEnum.ENABLE.getCode(),
                    Constants.DATA_INIT_VERSION,
                    now, now, Constants.SYSTEM_USER, Constants.SYSTEM_USER, 0L);
            } else {
                String existingDesc = findTaskDesc(taskId);
                if (!entry.getValue().equals(existingDesc)) {
                    jdbcTemplate.update(
                        "UPDATE ef_async_import_task SET task_desc = ?, update_time = ? WHERE id = ?",
                        entry.getValue(), now, taskId);
                }
            }
        }
    }

    @Override
    public ImportRequestInfo getRequestInfoByRegisterId(Long registerId) {
        ImportRecordRow row = findById(registerId);
        if (row == null) {
            return null;
        }
        ImportRegisterRequest registerRequest = JSONUtil.parseClassObject(row.executeParam, ImportRegisterRequest.class);
        if (registerRequest == null) {
            registerRequest = ImportRegisterRequest.builder()
                .appId(row.appId)
                .importCode(row.importCode)
                .ossUrl(row.sourceFileUrl)
                .fileName(row.sourceFileName)
                .fileType(row.fileType)
                .businessUniqueKey(row.businessUniqueKey)
                .executeParam(row.executeParam)
                .createBy(row.createBy)
                .maxRetry(row.maxServerRetry)
                .notifyEmail(row.notifyEmail)
                .locale(row.locale)
                .enableNotify(Objects.equals(row.notifyEnableStatus, EnableStatusEnum.ENABLE.getCode()))
                .build();
        } else if (StringUtils.isBlank(registerRequest.getOssUrl())) {
            registerRequest.setOssUrl(row.sourceFileUrl);
        }
        if (registerRequest.getEnableNotify() == null) {
            registerRequest.setEnableNotify(Objects.equals(row.notifyEnableStatus, EnableStatusEnum.ENABLE.getCode()));
        }

        ImportRequestInfo requestInfo = new ImportRequestInfo();
        requestInfo.setImportCode(row.importCode);
        requestInfo.setRegisterRequest(registerRequest);
        requestInfo.setResumePosition(row.resumePosition);
        requestInfo.setImportStatus(row.importStatus);
        requestInfo.setExecuteProcess(row.executeProcess);
        requestInfo.setTotalCount(row.totalCount);
        requestInfo.setSuccessCount(row.successCount);
        requestInfo.setFailCount(row.failCount);
        requestInfo.setResultFileUrl(row.resultFileUrl);
        requestInfo.setErrorMsg(row.errorMsg);
        return requestInfo;
    }

    @Override
    public void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                       ImportStatusEnum nextStatus) {
        if (executeProcess == null || executeProcess <= 0) {
            return;
        }
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, execute_process = ?, resume_position = ?, "
                + "update_time = ? WHERE id = ?",
            nextStatus == null ? ImportStatusEnum.EXECUTING.getCode() : nextStatus.getCode(),
            executeProcess,
            resumePosition == null ? 0L : resumePosition,
            new Date(),
            registerId);
    }

    @Override
    public void resetExecuteProcess(Long registerId) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET execute_process = 0, resume_position = 0, update_time = ? WHERE id = ?",
            new Date(), registerId);
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
            blankToEmpty(fileHash), new Date(), registerId);
    }

    @Override
    public void updateResultFileUrl(Long registerId, String resultFileUrl) {
        jdbcTemplate.update(
            "UPDATE ef_async_import_record SET result_file_url = ?, update_time = ? WHERE id = ?",
            blankToEmpty(resultFileUrl), new Date(), registerId);
    }

    @Override
    public void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                         Long failCount, Long resumePosition, String errorMsg) {
        int process = (status == ImportStatusEnum.SUCCESS || status == ImportStatusEnum.PARTIAL_SUCCESS) ? 100 : 0;
        jdbcTemplate.update(
            "UPDATE ef_async_import_record "
                + "SET import_status = ?, execute_process = ?, total_count = ?, success_count = ?, fail_count = ?, "
                + "resume_position = ?, error_msg = ?, update_time = ? WHERE id = ?",
            status.getCode(),
            process,
            totalCount,
            successCount,
            failCount,
            resumePosition == null ? 0L : resumePosition,
            blankToEmpty(errorMsg),
            new Date(),
            registerId);
    }

    @Override
    public CancelImportResult cancelImport(CancelImportRequest request) {
        CancelImportResult cancelResult = new CancelImportResult();
        ImportRecordRow row = findById(request.getRegisterId());
        if (row == null) {
            cancelResult.setCancelResult(false);
            cancelResult.setCancelMsg("任务不存在");
            return cancelResult;
        }
        if (row.importStatus != ImportStatusEnum.NONE) {
            cancelResult.setCancelResult(false);
            cancelResult.setCancelMsg("已经执行开始执行无法撤销");
            return cancelResult;
        }
        int affect = jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, update_by = ?, update_time = ? "
                + "WHERE id = ? AND import_status = ?",
            ImportStatusEnum.CANCEL.getCode(), request.getCancelBy(), new Date(),
            request.getRegisterId(), ImportStatusEnum.NONE.getCode());
        cancelResult.setCancelResult(affect > 0);
        cancelResult.setCancelMsg(affect > 0 ? "撤销成功" : "撤销失败");
        return cancelResult;
    }

    @Override
    public Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        if (StringUtils.isBlank(request.getAppId())) {
            Pagination<ImportResult> empty = Pagination.empty();
            empty.setPage(Long.valueOf(pageNum));
            empty.setPageSize(Long.valueOf(pageSize));
            empty.setTotalRecords(0L);
            empty.setModelList(Collections.emptyList());
            return empty;
        }
        int startIndex = (pageNum - 1) * pageSize;

        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ef_async_import_record WHERE app_id = ? "
                + "AND (? IS NULL OR import_status = ?) "
                + "AND (? IS NULL OR create_time >= ?) "
                + "AND (? IS NULL OR create_time <= ?)",
            Long.class,
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

        List<ImportResult> modelList = jdbcTemplate.query(
            "SELECT id, import_status, total_count, success_count, fail_count, execute_process, "
                + "result_file_url, error_msg, create_time, last_execute_time "
                + "FROM ef_async_import_record WHERE app_id = ? "
                + "AND (? IS NULL OR import_status = ?) "
                + "AND (? IS NULL OR create_time >= ?) "
                + "AND (? IS NULL OR create_time <= ?) "
                + "ORDER BY create_time DESC LIMIT ?, ?",
            new ImportResultRowMapper(),
            request.getAppId(),
            request.getImportStatus(), request.getImportStatus(),
            request.getStartTime(), request.getStartTime(),
            request.getEndTime(), request.getEndTime(),
            startIndex, pageSize);

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
                + "update_by = ?, update_time = ? WHERE id = ? AND import_status IN (?, ?, ?)",
            ImportStatusEnum.NONE.getCode(), StringUtils.EMPTY, operator, new Date(), registerId,
            ImportStatusEnum.FAIL.getCode(), ImportStatusEnum.CANCEL.getCode(),
            ImportStatusEnum.PARTIAL_SUCCESS.getCode());
        return affect > 0;
    }

    @Override
    public boolean revoke(Long registerId, String operator) {
        int affect = jdbcTemplate.update(
            "UPDATE ef_async_import_record SET import_status = ?, update_by = ?, update_time = ? "
                + "WHERE id = ? AND import_status = ?",
            ImportStatusEnum.CANCEL.getCode(), operator, new Date(), registerId, ImportStatusEnum.NONE.getCode());
        return affect > 0;
    }

    @Override
    public DownloadUrlResult resultFile(Long registerId) {
        ImportRecordRow row = findById(registerId);
        if (row == null || StringUtils.isBlank(row.resultFileUrl)) {
            return null;
        }
        DownloadUrlResult downloadUrlResult = new DownloadUrlResult();
        downloadUrlResult.setUrl(row.resultFileUrl);
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

    private Long findTaskId(String appId, String importCode) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id FROM ef_async_import_task WHERE app_id = ? AND task_code = ? AND is_deleted = 0",
                Long.class, appId, importCode);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private String findTaskDesc(Long taskId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT task_desc FROM ef_async_import_task WHERE id = ?",
                String.class, taskId);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private ImportRecordRow findById(Long registerId) {
        List<ImportRecordRow> rows = jdbcTemplate.query(
            "SELECT id, app_id, import_code, import_status, source_file_url, source_file_name, file_type, "
                + "business_unique_key, result_file_url, total_count, success_count, fail_count, execute_process, "
                + "resume_position, notify_enable_status, notify_email, max_server_retry, execute_param, error_msg, "
                + "create_by, locale "
                + "FROM ef_async_import_record WHERE id = ?",
            new ImportRecordRowMapper(),
            registerId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static String blankToEmpty(String value) {
        return StringUtils.isNotBlank(value) ? value : StringUtils.EMPTY;
    }

    private static final class ImportResultRowMapper implements RowMapper<ImportResult> {
        @Override
        public ImportResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            ImportStatusEnum status = ImportStatusEnum.fromCode(rs.getString("import_status"));
            boolean success = status == ImportStatusEnum.SUCCESS || status == ImportStatusEnum.PARTIAL_SUCCESS;
            return ImportResult.builder()
                .registerId(rs.getLong("id"))
                .success(success)
                .status(status == null ? rs.getString("import_status") : status.getCode())
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

    private static final class ImportRecordRowMapper implements RowMapper<ImportRecordRow> {
        @Override
        public ImportRecordRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            ImportRecordRow row = new ImportRecordRow();
            row.id = rs.getLong("id");
            row.appId = rs.getString("app_id");
            row.importCode = rs.getString("import_code");
            row.importStatus = ImportStatusEnum.fromCode(rs.getString("import_status"));
            row.sourceFileUrl = rs.getString("source_file_url");
            row.sourceFileName = rs.getString("source_file_name");
            row.fileType = rs.getString("file_type");
            row.businessUniqueKey = rs.getString("business_unique_key");
            row.resultFileUrl = rs.getString("result_file_url");
            row.totalCount = rs.getLong("total_count");
            row.successCount = rs.getLong("success_count");
            row.failCount = rs.getLong("fail_count");
            row.executeProcess = rs.getInt("execute_process");
            row.resumePosition = rs.getLong("resume_position");
            row.notifyEnableStatus = rs.getInt("notify_enable_status");
            row.notifyEmail = rs.getString("notify_email");
            row.maxServerRetry = rs.getInt("max_server_retry");
            row.executeParam = rs.getString("execute_param");
            row.errorMsg = rs.getString("error_msg");
            row.createBy = rs.getString("create_by");
            row.locale = rs.getString("locale");
            return row;
        }
    }

    private static final class ImportRecordRow {
        private Long id;
        private String appId;
        private String importCode;
        private ImportStatusEnum importStatus;
        private String sourceFileUrl;
        private String sourceFileName;
        private String fileType;
        private String businessUniqueKey;
        private String resultFileUrl;
        private Long totalCount;
        private Long successCount;
        private Long failCount;
        private Integer executeProcess;
        private Long resumePosition;
        private Integer notifyEnableStatus;
        private String notifyEmail;
        private Integer maxServerRetry;
        private String executeParam;
        private String errorMsg;
        private String createBy;
        private String locale;
    }
}
