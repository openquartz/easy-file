package org.svnee.easyfile.storage.mapper.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.storage.entity.AsyncDownloadRecord;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.condition.UploadInfoChangeCondition;

/**
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncDownloadRecordMapperImpl implements AsyncDownloadRecordMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into ef_async_download_record (download_task_id, app_id,download_code,upload_status, file_url, file_system, download_operate_by,download_operate_name, remark, notify_enable_status,notify_email, max_server_retry, current_retry,download_num,last_execute_time,"
            + "invalid_time,execute_param,error_msg, version, create_time, update_time, create_by, update_by) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String FIND_BY_ID_SQL =
        "select id,download_task_id, app_id,download_code,upload_status, file_url, file_system, download_operate_by,"
            + "download_operate_name, remark, notify_enable_status,notify_email, max_server_retry, current_retry,download_num,last_execute_time,"
            + "invalid_time,execute_param,error_msg, version, create_time, update_time, create_by, update_by from ef_async_download_record where id = ?";

    private static final String REFRESH_UPLOAD_STATUS_SQL = "update ef_async_download_record set upload_status = ?,update_by = ? where id = ? AND upload_status = ?";

    private static final String UPDATE_DOWNLOAD_SQL = "update ef_async_download_record set download_num = download_num + 1 where id = ? and upload_status = ?";

    private static final String LIST_SQL =
        "select id,download_task_id, app_id,download_code,upload_status, file_url, file_system, download_operate_by,"
            + "download_operate_name, remark, notify_enable_status,notify_email, max_server_retry, current_retry,download_num,last_execute_time,"
            + "invalid_time,execute_param,error_msg, version, create_time, update_time, create_by, update_by from ef_async_download_record "
            + "where download_task_id = ? and upload_status=? order by update_time desc limit ?";

    @Override
    public int insertSelective(AsyncDownloadRecord downloadRecord) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, downloadRecord.getDownloadTaskId());
            ps.setString(2, downloadRecord.getAppId());
            ps.setString(3, downloadRecord.getDownloadCode());
            ps.setString(4, downloadRecord.getUploadStatus().getCode());
            ps.setString(5, downloadRecord.getFileUrl());
            ps.setString(6, downloadRecord.getFileSystem());
            ps.setString(7, downloadRecord.getDownloadOperateBy());
            ps.setString(8, downloadRecord.getDownloadOperateName());
            ps.setString(9, downloadRecord.getRemark());
            ps.setInt(10, downloadRecord.getNotifyEnableStatus());
            ps.setString(11, downloadRecord.getNotifyEmail());
            ps.setInt(12, downloadRecord.getMaxServerRetry());
            ps.setInt(13, downloadRecord.getCurrentRetry());
            ps.setInt(14, downloadRecord.getDownloadNum());
            ps.setTimestamp(15, new Timestamp(downloadRecord.getLastExecuteTime().getTime()));
            ps.setTimestamp(16, new Timestamp(downloadRecord.getInvalidTime().getTime()));
            ps.setString(17, downloadRecord.getExecuteParam());
            ps.setString(18, downloadRecord.getErrorMsg());
            ps.setInt(19, downloadRecord.getVersion());
            ps.setDate(20, new Date(downloadRecord.getCreateTime().getTime()));
            ps.setDate(21, new Date(downloadRecord.getUpdateTime().getTime()));
            ps.setString(22, downloadRecord.getCreateBy());
            ps.setString(23, downloadRecord.getUpdateBy());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            downloadRecord.setId(key.longValue());
            return 1;
        }
        return 0;
    }

    @Override
    public AsyncDownloadRecord findById(Long id) {
        List<AsyncDownloadRecord> downloadRecordList = jdbcTemplate
            .query(FIND_BY_ID_SQL, new Object[]{id}, new AsyncDownloadRecordRowMapper());
        if (CollectionUtils.isNotEmpty(downloadRecordList)) {
            return downloadRecordList.get(0);
        }
        return null;
    }

    @Override
    public int changeUploadInfo(UploadInfoChangeCondition condition) {
        List<Object> params = new ArrayList<>();
        final StringBuilder sql = new StringBuilder(
            "update ef_async_download_record set version=version+1,upload_status = ? ");
        params.add(condition.getUploadStatus().getCode());
        if (StringUtils.isNotBlank(condition.getFileUrl())) {
            sql.append(",file_url = ?");
            params.add(condition.getFileUrl());
        }
        if (StringUtils.isNotBlank(condition.getFileSystem())) {
            sql.append(",file_system = ?");
            params.add(condition.getFileSystem());
        }
        if (StringUtils.isNotBlank(condition.getErrorMsg())) {
            sql.append(",error_msg = ?");
            params.add(condition.getErrorMsg());
        }
        if (Objects.nonNull(condition.getLastExecuteTime())) {
            sql.append(",last_execute_time = ?");
            params.add(condition.getLastExecuteTime());
        }
        if (Objects.nonNull(condition.getInvalidTime())) {
            sql.append(",invalid_time = ?");
            params.add(condition.getInvalidTime());
        }
        sql.append(" where id = ?");
        params.add(condition.getId());
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public int refreshUploadStatus(Long id, UploadStatusEnum oriUploadStatus, UploadStatusEnum tagUploadStatus,
        String updateBy) {
        return jdbcTemplate
            .update(REFRESH_UPLOAD_STATUS_SQL, tagUploadStatus.getCode(), updateBy, id, oriUploadStatus.getCode());
    }

    @Override
    public int download(Long id, UploadStatusEnum uploadStatus) {
        return jdbcTemplate.update(UPDATE_DOWNLOAD_SQL, id, uploadStatus.getCode());
    }

    @Override
    public List<AsyncDownloadRecord> listByTaskIdAndStatus(Long downloadTaskId, UploadStatusEnum uploadStatus,
        Integer offset) {
        return jdbcTemplate
            .query(LIST_SQL, new Object[]{downloadTaskId, uploadStatus.getCode(), offset},
                new AsyncDownloadRecordRowMapper());
    }

    private static class AsyncDownloadRecordRowMapper implements RowMapper<AsyncDownloadRecord> {

        @Override
        public AsyncDownloadRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncDownloadRecord downloadRecord = new AsyncDownloadRecord();
            downloadRecord.setId(rs.getLong("id"));
            downloadRecord.setDownloadTaskId(rs.getLong("download_task_id"));
            downloadRecord.setAppId(rs.getString("app_id"));
            downloadRecord.setDownloadCode(rs.getString("download_code"));
            downloadRecord.setUploadStatus(UploadStatusEnum.fromCode(rs.getString("upload_status")));
            downloadRecord.setFileUrl(rs.getString("file_url"));
            downloadRecord.setFileSystem(rs.getString("file_system"));
            downloadRecord.setDownloadOperateBy(rs.getString("download_operate_by"));
            downloadRecord.setDownloadOperateName(rs.getString("download_operate_name"));
            downloadRecord.setRemark(rs.getString("remark"));
            downloadRecord.setNotifyEnableStatus(rs.getInt("notify_enable_status"));
            downloadRecord.setNotifyEmail(rs.getString("notify_email"));
            downloadRecord.setMaxServerRetry(rs.getInt("max_server_retry"));
            downloadRecord.setCurrentRetry(rs.getInt("current_retry"));
            downloadRecord.setExecuteParam(rs.getString("execute_param"));
            downloadRecord.setErrorMsg(rs.getString("error_msg"));
            downloadRecord.setLastExecuteTime(rs.getDate("last_execute_time"));
            downloadRecord.setInvalidTime(rs.getDate("invalid_time"));
            downloadRecord.setDownloadNum(rs.getInt("download_num"));
            downloadRecord.setVersion(rs.getInt("version"));
            downloadRecord.setCreateTime(rs.getDate("create_time"));
            downloadRecord.setUpdateTime(rs.getDate("update_time"));
            downloadRecord.setCreateBy(rs.getString("create_by"));
            downloadRecord.setUpdateBy(rs.getString("update_by"));
            return downloadRecord;
        }
    }
}
