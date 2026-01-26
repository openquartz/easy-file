package com.openquartz.easyfile.storage.local.mapper.impl;

import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTask;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTaskMapper;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * 异步导入任务Mapper实现
 *
 * @author svnee
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncImportTaskMapperImpl implements AsyncImportTaskMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into {0}(task_code, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,"
            + "create_by, update_by, is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_BY_TASK_CODE_SQL =
        "select id, task_code, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,\n"
            + "create_by, update_by, is_deleted from {0} where task_code=? and app_id=? and is_deleted = 0";
    private static final String REFRESH_TASK_DESC_SQL = "update {0} set task_desc = ? where id = ? and is_deleted = 0";

    @Override
    public int insertSelective(AsyncImportTask task) {
        String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncImportTaskTable());
        return jdbcTemplate
            .update(sql, task.getTaskCode(), task.getTaskDesc(),
                task.getLimitingStrategy(), task.getAppId(),
                task.getUnifiedAppId(), task.getEnableStatus(),
                task.getVersion(), task.getCreateTime(), task.getUpdateTime(),
                task.getCreateBy(), task.getUpdateBy(), task.getIsDeleted());
    }

    @Override
    public AsyncImportTask selectByTaskCode(String taskCode, String appId) {
        String sql = MessageFormat
            .format(SELECT_BY_TASK_CODE_SQL, EasyFileTableGeneratorSupplier.genAsyncImportTaskTable());
        List<AsyncImportTask> taskList = jdbcTemplate
            .query(sql, new Object[]{taskCode, appId}, new AsyncImportTaskRowMapper());
        if (CollectionUtils.isEmpty(taskList)) {
            return null;
        }
        return taskList.get(0);
    }

    @Override
    public int refreshTaskDesc(Long id, String taskDesc) {
        String sql = MessageFormat
            .format(REFRESH_TASK_DESC_SQL, EasyFileTableGeneratorSupplier.genAsyncImportTaskTable());
        return jdbcTemplate.update(sql, taskDesc, id);
    }

    public static class AsyncImportTaskRowMapper implements RowMapper<AsyncImportTask> {

        @Override
        public AsyncImportTask mapRow(ResultSet resultSet, int i) throws SQLException {
            AsyncImportTask asyncImportTask = new AsyncImportTask();
            asyncImportTask.setId(resultSet.getLong("id"));
            asyncImportTask.setTaskCode(resultSet.getString("task_code"));
            asyncImportTask.setTaskDesc(resultSet.getString("task_desc"));
            asyncImportTask.setAppId(resultSet.getString("app_id"));
            asyncImportTask.setUnifiedAppId(resultSet.getString("unified_app_id"));
            asyncImportTask.setEnableStatus(resultSet.getInt("enable_status"));
            asyncImportTask.setLimitingStrategy(resultSet.getString("limiting_strategy"));
            asyncImportTask.setVersion(resultSet.getInt("version"));
            asyncImportTask.setCreateTime(resultSet.getTimestamp("create_time"));
            asyncImportTask.setUpdateTime(resultSet.getTimestamp("update_time"));
            asyncImportTask.setCreateBy(resultSet.getString("create_by"));
            asyncImportTask.setUpdateBy(resultSet.getString("update_by"));
            asyncImportTask.setIsDeleted(resultSet.getLong("is_deleted"));
            return asyncImportTask;
        }
    }
}
