package com.openquartz.easyfile.storage.local.mapper.impl;

import com.openquartz.easyfile.common.dictionary.UploadStatusEnum;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncImportRecord;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportRecordMapper;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 异步导入记录Mapper实现
 *
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncImportRecordMapperImpl implements AsyncImportRecordMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into {0}(import_task_id, app_id, import_code, upload_status, file_url, file_name, file_system, import_operate_by, import_operate_name, remark, notify_enable_status, notify_email, max_server_retry, current_retry, last_execute_time,"
            + "invalid_time, execute_param, error_msg, execute_process, version, create_time, update_time, create_by, update_by, locale, success_rows, fail_rows, total_rows, error_file_url) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String FIND_BY_ID_SQL =
        "select id, import_task_id, app_id, import_code, upload_status, file_url, file_name, file_system, import_operate_by, import_operate_name, remark, notify_enable_status, notify_email, max_server_retry, current_retry, last_execute_time,"
            + "invalid_time, execute_param, error_msg, execute_process, version, create_time, update_time, create_by, update_by, locale, success_rows, fail_rows, total_rows, error_file_url from {0} where id = ?";

    private static final String REFRESH_UPLOAD_STATUS_SQL = "update {0} set upload_status = ?, update_by = ? where id = ?";
    
    private static final String UPDATE_IMPORT_RESULT_SQL = "update {0} set upload_status = ?, error_file_url = ?, success_rows = ?, fail_rows = ?, total_rows = ? where id = ?";

    @Override
    public int insertSelective(AsyncImportRecord asyncImportRecord) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, asyncImportRecord.getImportTaskId());
            ps.setString(2, asyncImportRecord.getAppId());
            ps.setString(3, asyncImportRecord.getImportCode());
            ps.setString(4, asyncImportRecord.getUploadStatus().getCode());
            ps.setString(5, asyncImportRecord.getFileUrl());
            ps.setString(6, asyncImportRecord.getFileName());
            ps.setString(7, asyncImportRecord.getFileSystem());
            ps.setString(8, asyncImportRecord.getImportOperateBy());
            ps.setString(9, asyncImportRecord.getImportOperateName());
            ps.setString(10, asyncImportRecord.getRemark());
            ps.setInt(11, asyncImportRecord.getNotifyEnableStatus());
            ps.setString(12, asyncImportRecord.getNotifyEmail());
            ps.setInt(13, asyncImportRecord.getMaxServerRetry());
            ps.setInt(14, asyncImportRecord.getCurrentRetry());
            ps.setTimestamp(15, asyncImportRecord.getLastExecuteTime() != null ? new Timestamp(asyncImportRecord.getLastExecuteTime().getTime()) : null);
            ps.setTimestamp(16, asyncImportRecord.getInvalidTime() != null ? new Timestamp(asyncImportRecord.getInvalidTime().getTime()) : null);
            ps.setString(17, asyncImportRecord.getExecuteParam());
            ps.setString(18, asyncImportRecord.getErrorMsg());
            ps.setInt(19, asyncImportRecord.getExecuteProcess());
            ps.setInt(20, asyncImportRecord.getVersion());
            ps.setTimestamp(21, new Timestamp(asyncImportRecord.getCreateTime().getTime()));
            ps.setTimestamp(22, new Timestamp(asyncImportRecord.getUpdateTime().getTime()));
            ps.setString(23, asyncImportRecord.getCreateBy());
            ps.setString(24, asyncImportRecord.getUpdateBy());
            ps.setString(25, asyncImportRecord.getLocale());
            ps.setInt(26, asyncImportRecord.getSuccessRows() != null ? asyncImportRecord.getSuccessRows() : 0);
            ps.setInt(27, asyncImportRecord.getFailRows() != null ? asyncImportRecord.getFailRows() : 0);
            ps.setInt(28, asyncImportRecord.getTotalRows() != null ? asyncImportRecord.getTotalRows() : 0);
            ps.setString(29, asyncImportRecord.getErrorFileUrl());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            asyncImportRecord.setId(key.longValue());
            return 1;
        }
        return 0;
    }

    @Override
    public AsyncImportRecord findById(Long id) {
        String sql = MessageFormat.format(FIND_BY_ID_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        List<AsyncImportRecord> list = jdbcTemplate.query(sql, new Object[]{id}, new AsyncImportRecordRowMapper());
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public int updateUploadStatus(Long id, UploadStatusEnum uploadStatus, String updateBy) {
        String sql = MessageFormat.format(REFRESH_UPLOAD_STATUS_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, uploadStatus.getCode(), updateBy, id);
    }

    @Override
    public int updateImportResult(Long id, UploadStatusEnum uploadStatus, String errorFileUrl, int successRows, int failRows, int totalRows) {
        String sql = MessageFormat.format(UPDATE_IMPORT_RESULT_SQL, EasyFileTableGeneratorSupplier.genAsyncImportRecordTable());
        return jdbcTemplate.update(sql, uploadStatus.getCode(), errorFileUrl, successRows, failRows, totalRows, id);
    }

    private static class AsyncImportRecordRowMapper implements RowMapper<AsyncImportRecord> {

        @Override
        public AsyncImportRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncImportRecord record = new AsyncImportRecord();
            record.setId(rs.getLong("id"));
            record.setImportTaskId(rs.getLong("import_task_id"));
            record.setAppId(rs.getString("app_id"));
            record.setImportCode(rs.getString("import_code"));
            record.setUploadStatus(UploadStatusEnum.fromCode(rs.getString("upload_status")));
            record.setFileUrl(rs.getString("file_url"));
            record.setFileName(rs.getString("file_name"));
            record.setFileSystem(rs.getString("file_system"));
            record.setImportOperateBy(rs.getString("import_operate_by"));
            record.setImportOperateName(rs.getString("import_operate_name"));
            record.setRemark(rs.getString("remark"));
            record.setNotifyEnableStatus(rs.getInt("notify_enable_status"));
            record.setNotifyEmail(rs.getString("notify_email"));
            record.setMaxServerRetry(rs.getInt("max_server_retry"));
            record.setCurrentRetry(rs.getInt("current_retry"));
            record.setLastExecuteTime(rs.getTimestamp("last_execute_time"));
            record.setInvalidTime(rs.getTimestamp("invalid_time"));
            record.setExecuteParam(rs.getString("execute_param"));
            record.setErrorMsg(rs.getString("error_msg"));
            record.setExecuteProcess(rs.getInt("execute_process"));
            record.setVersion(rs.getInt("version"));
            record.setCreateTime(rs.getTimestamp("create_time"));
            record.setUpdateTime(rs.getTimestamp("update_time"));
            record.setCreateBy(rs.getString("create_by"));
            record.setUpdateBy(rs.getString("update_by"));
            record.setLocale(rs.getString("locale"));
            record.setSuccessRows(rs.getInt("success_rows"));
            record.setFailRows(rs.getInt("fail_rows"));
            record.setTotalRows(rs.getInt("total_rows"));
            record.setErrorFileUrl(rs.getString("error_file_url"));
            return record;
        }
    }
}
