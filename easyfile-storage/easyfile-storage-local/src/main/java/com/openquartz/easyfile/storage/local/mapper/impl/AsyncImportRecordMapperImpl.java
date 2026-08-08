package com.openquartz.easyfile.storage.local.mapper.impl;

import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.ImportRecordQueryCondition;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncImportRecordMapperImpl implements AsyncImportRecordMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String COLUMNS =
        "id,import_task_id, app_id,import_code,import_status, source_file_url, source_file_name, file_type, file_hash,"
            + "business_unique_key, result_file_url, total_count, success_count, fail_count, execute_process, resume_position,"
            + "notify_enable_status,notify_email, max_server_retry, current_retry,last_execute_time,"
            + "execute_param,error_msg, version, create_time, update_time, create_by, update_by,locale";

    private static final String INSERT_SQL =
        "insert into {0}(import_task_id, app_id,import_code,import_status, source_file_url, source_file_name, file_type,"
            + "file_hash, business_unique_key, result_file_url, total_count, success_count, fail_count, execute_process,"
            + "resume_position, notify_enable_status,notify_email, max_server_retry, current_retry,last_execute_time,"
            + "execute_param,error_msg, version, create_time, update_time, create_by, update_by,locale) "
            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String FIND_BY_ID_SQL = "select " + COLUMNS + " from {0} where id = ?";

    private static final String REFRESH_IMPORT_STATUS_SQL =
        "update {0} set import_status = ?,update_by = ?,update_time = ? where id = ? AND import_status = ?";

    private static final String UPDATE_EXECUTE_PROCESS_SQL =
        "update {0} set execute_process = ?, resume_position = ?, import_status=?, update_time = ? "
            + "where id = ? and execute_process <= ? ";

    private static final String RESET_EXECUTE_PROCESS_SQL =
        "update {0} set execute_process = ?, resume_position = ?, update_time = ? where id = ?";

    private static final String UPDATE_TOTAL_COUNT_SQL =
        "update {0} set total_count = ?, update_time = ? where id = ?";

    private static final String UPDATE_FILE_HASH_SQL =
        "update {0} set file_hash = ?, update_time = ? where id = ?";

    private static final String UPDATE_RESULT_FILE_URL_SQL =
        "update {0} set result_file_url = ?, update_time = ? where id = ?";

    private static final String COMPLETE_SQL =
        "update {0} set import_status = ?, execute_process = ?, total_count = ?, success_count = ?, fail_count = ?, "
            + "resume_position = ?, error_msg = ?, last_execute_time = ?, update_time = ? where id = ?";

    private static final String SELECT_ALL_SQL = "select " + COLUMNS + " from {0} ";

    private static final String SELECT_COUNT_SQL = "select count(*) from {0}";

    @Override
    public int insertSelective(AsyncImportRecord importRecord) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, importRecord.getImportTaskId());
            ps.setString(2, importRecord.getAppId());
            ps.setString(3, importRecord.getImportCode());
            ps.setString(4, importRecord.getImportStatus().getCode());
            ps.setString(5, importRecord.getSourceFileUrl());
            ps.setString(6, importRecord.getSourceFileName());
            ps.setString(7, importRecord.getFileType());
            ps.setString(8, importRecord.getFileHash());
            ps.setString(9, importRecord.getBusinessUniqueKey());
            ps.setString(10, importRecord.getResultFileUrl());
            ps.setLong(11, nullToZero(importRecord.getTotalCount()));
            ps.setLong(12, nullToZero(importRecord.getSuccessCount()));
            ps.setLong(13, nullToZero(importRecord.getFailCount()));
            ps.setInt(14, importRecord.getExecuteProcess() == null ? 0 : importRecord.getExecuteProcess());
            ps.setLong(15, nullToZero(importRecord.getResumePosition()));
            ps.setInt(16, importRecord.getNotifyEnableStatus());
            ps.setString(17, importRecord.getNotifyEmail());
            ps.setInt(18, importRecord.getMaxServerRetry());
            ps.setInt(19, importRecord.getCurrentRetry());
            if (importRecord.getLastExecuteTime() != null) {
                ps.setTimestamp(20, new Timestamp(importRecord.getLastExecuteTime().getTime()));
            } else {
                ps.setTimestamp(20, null);
            }
            ps.setString(21, importRecord.getExecuteParam());
            ps.setString(22, importRecord.getErrorMsg());
            ps.setInt(23, importRecord.getVersion());
            ps.setTimestamp(24, new Timestamp(importRecord.getCreateTime().getTime()));
            ps.setTimestamp(25, new Timestamp(importRecord.getUpdateTime().getTime()));
            ps.setString(26, importRecord.getCreateBy());
            ps.setString(27, importRecord.getUpdateBy());
            ps.setString(28, importRecord.getLocale());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            importRecord.setId(key.longValue());
            return 1;
        }
        return 0;
    }

    @Override
    public AsyncImportRecord findById(Long id) {
        String sql = MessageFormat
            .format(FIND_BY_ID_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        List<AsyncImportRecord> importRecordList = jdbcTemplate
            .query(sql, new Object[]{id}, new AsyncImportRecordRowMapper());
        if (CollectionUtils.isNotEmpty(importRecordList)) {
            return importRecordList.get(0);
        }
        return null;
    }

    @Override
    public int refreshImportStatus(Long id, ImportStatusEnum oriImportStatus, ImportStatusEnum tagImportStatus,
                                   String updateBy) {
        String sql = MessageFormat.format(REFRESH_IMPORT_STATUS_SQL,
            EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate
            .update(sql, tagImportStatus.getCode(), updateBy, new Date(), id, oriImportStatus.getCode());
    }

    @Override
    public int refreshExecuteProcess(Long registerId, Integer executeProcess, Long resumePosition,
                                     ImportStatusEnum nextStatus) {
        String sql = MessageFormat
            .format(UPDATE_EXECUTE_PROCESS_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, executeProcess, resumePosition, nextStatus.getCode(), new Date(),
            registerId, executeProcess);
    }

    @Override
    public int resetExecuteProcess(Long registerId) {
        String sql = MessageFormat
            .format(RESET_EXECUTE_PROCESS_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, 0, 0L, new Date(), registerId);
    }

    @Override
    public int updateTotalCount(Long registerId, Long totalCount) {
        String sql = MessageFormat
            .format(UPDATE_TOTAL_COUNT_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, totalCount, new Date(), registerId);
    }

    @Override
    public int updateFileHash(Long registerId, String fileHash) {
        String sql = MessageFormat
            .format(UPDATE_FILE_HASH_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, fileHash, new Date(), registerId);
    }

    @Override
    public int updateResultFileUrl(Long registerId, String resultFileUrl) {
        String sql = MessageFormat
            .format(UPDATE_RESULT_FILE_URL_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, resultFileUrl, new Date(), registerId);
    }

    @Override
    public int complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                        Long failCount, Long resumePosition, String errorMsg) {
        boolean successLike = status == ImportStatusEnum.SUCCESS || status == ImportStatusEnum.PARTIAL_SUCCESS;
        int process = successLike ? 100 : 0;
        String sql = MessageFormat.format(COMPLETE_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        Date now = new Date();
        return jdbcTemplate.update(sql,
            status.getCode(),
            process,
            nullToZero(totalCount),
            nullToZero(successCount),
            nullToZero(failCount),
            nullToZero(resumePosition),
            errorMsg == null ? StringUtils.EMPTY : errorMsg,
            now,
            now,
            registerId);
    }

    @Override
    public List<AsyncImportRecord> selectByCondition(ImportRecordQueryCondition condition) {
        Pair<String, Map<String, Object>> pair = queryByCondition(condition, SELECT_ALL_SQL);
        return new NamedParameterJdbcTemplate(jdbcTemplate)
            .query(pair.getKey(), pair.getValue(), new AsyncImportRecordRowMapper());
    }

    @Override
    public int countByCondition(ImportRecordQueryCondition condition) {
        Pair<String, Map<String, Object>> pair = queryByCondition(condition, SELECT_COUNT_SQL);
        Integer total = new NamedParameterJdbcTemplate(jdbcTemplate)
            .queryForObject(pair.getKey(), pair.getValue(), Integer.class);
        return Objects.nonNull(total) ? total : 0;
    }

    private Pair<String, Map<String, Object>> queryByCondition(ImportRecordQueryCondition condition,
                                                               String sqlTemplate) {
        String sql = MessageFormat
            .format(sqlTemplate, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());

        StringJoiner conditionWhere = new StringJoiner(" and ");
        Map<String, Object> paramMap = new HashMap<>(10);
        if (CollectionUtils.isNotEmpty(condition.getLimitedAppIdList())) {
            conditionWhere.add("app_id in (:appId)");
            paramMap.put("appId", condition.getLimitedAppIdList());
        }
        if (StringUtils.isNotBlank(condition.getImportCode())) {
            conditionWhere.add("import_code= :importCode");
            paramMap.put("importCode", condition.getImportCode());
        }
        if (StringUtils.isNotBlank(condition.getCreateBy())) {
            conditionWhere.add("create_by= :createBy");
            paramMap.put("createBy", condition.getCreateBy());
        }
        if (StringUtils.isNotBlank(condition.getBusinessUniqueKey())) {
            conditionWhere.add("business_unique_key= :businessUniqueKey");
            paramMap.put("businessUniqueKey", condition.getBusinessUniqueKey());
        }
        if (Objects.nonNull(condition.getStartCreateTime())) {
            conditionWhere.add("create_time>= :startCreateTime");
            paramMap.put("startCreateTime", condition.getStartCreateTime());
        }
        if (Objects.nonNull(condition.getEndCreateTime())) {
            conditionWhere.add("create_time< :endCreateTime");
            paramMap.put("endCreateTime", condition.getEndCreateTime());
        }
        if (Objects.nonNull(condition.getImportStatus())) {
            conditionWhere.add("import_status= :importStatus");
            paramMap.put("importStatus", condition.getImportStatus().getCode());
        }
        if (conditionWhere.length() > 0) {
            sql = sql + " where " + conditionWhere;
        }
        sql = sql + " order by create_time desc";
        if (Objects.nonNull(condition.getStartOffset())) {
            sql = sql + " limit :startIndex,:offset";
            paramMap.put("startIndex", condition.getStartOffset().getKey());
            paramMap.put("offset", condition.getStartOffset().getValue());
        }
        return Pair.of(sql, paramMap);
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private static class AsyncImportRecordRowMapper implements RowMapper<AsyncImportRecord> {

        @Override
        public AsyncImportRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncImportRecord importRecord = new AsyncImportRecord();
            importRecord.setId(rs.getLong("id"));
            importRecord.setImportTaskId(rs.getLong("import_task_id"));
            importRecord.setAppId(rs.getString("app_id"));
            importRecord.setImportCode(rs.getString("import_code"));
            importRecord.setImportStatus(ImportStatusEnum.fromCode(rs.getString("import_status")));
            importRecord.setSourceFileUrl(rs.getString("source_file_url"));
            importRecord.setSourceFileName(rs.getString("source_file_name"));
            importRecord.setFileType(rs.getString("file_type"));
            importRecord.setFileHash(rs.getString("file_hash"));
            importRecord.setBusinessUniqueKey(rs.getString("business_unique_key"));
            importRecord.setResultFileUrl(rs.getString("result_file_url"));
            importRecord.setTotalCount(rs.getLong("total_count"));
            importRecord.setSuccessCount(rs.getLong("success_count"));
            importRecord.setFailCount(rs.getLong("fail_count"));
            importRecord.setExecuteProcess(rs.getInt("execute_process"));
            importRecord.setResumePosition(rs.getLong("resume_position"));
            importRecord.setNotifyEnableStatus(rs.getInt("notify_enable_status"));
            importRecord.setNotifyEmail(rs.getString("notify_email"));
            importRecord.setMaxServerRetry(rs.getInt("max_server_retry"));
            importRecord.setCurrentRetry(rs.getInt("current_retry"));
            importRecord.setExecuteParam(rs.getString("execute_param"));
            importRecord.setErrorMsg(rs.getString("error_msg"));
            importRecord.setLastExecuteTime(rs.getTimestamp("last_execute_time"));
            importRecord.setVersion(rs.getInt("version"));
            importRecord.setCreateTime(rs.getTimestamp("create_time"));
            importRecord.setUpdateTime(rs.getTimestamp("update_time"));
            importRecord.setCreateBy(rs.getString("create_by"));
            importRecord.setUpdateBy(rs.getString("update_by"));
            importRecord.setLocale(rs.getString("locale"));
            return importRecord;
        }
    }
}
