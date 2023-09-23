package com.openquartz.easyfile.storage.local.mapper.impl;

import com.openquartz.easyfile.common.dictionary.FileHandleTypeEnum;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.MapUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncFileAppEntity;
import com.openquartz.easyfile.storage.local.entity.AsyncFileTask;
import com.openquartz.easyfile.storage.local.mapper.AsyncFileTaskMapper;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;

/**
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncFileTaskMapperImpl implements AsyncFileTaskMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into {0}(task_code, task_type, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,"
            + "create_by, update_by, deleted) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_BY_TASK_CODE_SQL =
        "select id, task_code, task_type, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,\n"
            + "create_by, update_by, deleted from {0} where task_code=? and app_id=? and deleted = 0";
    private static final String LIST_BY_TASK_SQL =
        "select id, task_code, task_type, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,\n"
            + "create_by, update_by, deleted from {0} where task_code in (:taskCodeList) and app_id in (:appIdList) and deleted = 0";
    private static final String REFRESH_TASK_DESC_SQL = "update {0} set task_desc = ? where id = ? and deleted = 0";
    private static final String SELECT_APP_ID_SQL = "select distinct app_id from {0} where unified_app_id = ? and deleted = 0";
    private static final String SELECT_APP_ENTITY_SQL = "select distinct app_id,unified_app_id from {0} where deleted = 0";

    @Override
    public int insertSelective(AsyncFileTask task) {
        String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return jdbcTemplate
            .update(sql, task.getTaskCode(), task.getTaskType(), task.getTaskDesc(),
                task.getLimitingStrategy(), task.getAppId(),
                task.getUnifiedAppId(), task.getEnableStatus(),
                task.getVersion(), task.getCreateTime(), task.getUpdateTime(),
                task.getCreateBy(), task.getUpdateBy(), task.getDeleted());
    }

    @Override
    public AsyncFileTask selectByTaskCode(String taskCode, String appId) {
        String sql = MessageFormat
            .format(SELECT_BY_TASK_CODE_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        List<AsyncFileTask> taskList = jdbcTemplate
            .query(sql, new Object[]{taskCode, appId}, new AsyncDownloadTaskRowMapper());
        if (CollectionUtils.isEmpty(taskList)) {
            return null;
        }
        return taskList.get(0);
    }

    @Override
    public List<AsyncFileTask> listByDownloadCode(List<String> downloadCodeList, List<String> appIdList) {

        Map<String, Object> paramMap = MapUtils.newHashMapWithExpectedSize(2);
        paramMap.put("taskCodeList", downloadCodeList);
        paramMap.put("appIdList", appIdList);

        String sql = MessageFormat
            .format(LIST_BY_TASK_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return new NamedParameterJdbcTemplate(jdbcTemplate)
            .query(sql, paramMap, new AsyncDownloadTaskRowMapper());
    }

    @Override
    public int refreshTaskDesc(Long id, String taskDesc) {
        String sql = MessageFormat
            .format(REFRESH_TASK_DESC_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return jdbcTemplate.update(sql, taskDesc, id);
    }

    public static class AsyncDownloadTaskRowMapper implements RowMapper<AsyncFileTask> {

        @Override
        public AsyncFileTask mapRow(ResultSet resultSet, int i) throws SQLException {
            AsyncFileTask asyncFileTask = new AsyncFileTask();
            asyncFileTask.setId(resultSet.getLong("id"));
            asyncFileTask.setTaskCode(resultSet.getString("task_code"));
            asyncFileTask.setTaskType(FileHandleTypeEnum.fromCode(resultSet.getInt("task_type")));
            asyncFileTask.setTaskDesc(resultSet.getString("task_desc"));
            asyncFileTask.setAppId(resultSet.getString("app_id"));
            asyncFileTask.setUnifiedAppId(resultSet.getString("unified_app_id"));
            asyncFileTask.setEnableStatus(resultSet.getInt("enable_status"));
            asyncFileTask.setLimitingStrategy(resultSet.getString("limiting_strategy"));
            asyncFileTask.setVersion(resultSet.getInt("version"));
            asyncFileTask.setCreateTime(resultSet.getTimestamp("create_time"));
            asyncFileTask.setUpdateTime(resultSet.getTimestamp("update_time"));
            asyncFileTask.setCreateBy(resultSet.getString("create_by"));
            asyncFileTask.setUpdateBy(resultSet.getString("update_by"));
            asyncFileTask.setDeleted(resultSet.getLong("deleted"));
            return asyncFileTask;
        }
    }

    @Override
    public List<String> getByUnifiedAppId(String unifiedAppId) {
        String sql = MessageFormat
            .format(SELECT_APP_ID_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return jdbcTemplate.queryForList(sql, String.class, unifiedAppId);
    }

    @Override
    public List<AsyncFileAppEntity> getAsyncDownloadAppEntity() {
        String sql = MessageFormat
            .format(SELECT_APP_ENTITY_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return new NamedParameterJdbcTemplate(jdbcTemplate).query(sql, new AsyncDownloadAppEntityMapper());
    }

    public static class AsyncDownloadAppEntityMapper implements RowMapper<AsyncFileAppEntity> {

        @Override
        public AsyncFileAppEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            AsyncFileAppEntity asyncDownloadTask = new AsyncFileAppEntity();
            asyncDownloadTask.setAppId(resultSet.getString("app_id"));
            asyncDownloadTask.setUnifiedAppId(resultSet.getString("unified_app_id"));
            return asyncDownloadTask;
        }
    }

}
