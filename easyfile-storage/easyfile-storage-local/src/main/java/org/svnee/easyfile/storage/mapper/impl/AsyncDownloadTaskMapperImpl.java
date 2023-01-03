package org.svnee.easyfile.storage.mapper.impl;

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
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.MapUtils;
import org.svnee.easyfile.storage.entity.AsyncDownloadTask;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;
import org.svnee.easyfile.storage.prop.EasyFileTableGeneratorSupplier;

/**
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncDownloadTaskMapperImpl implements AsyncDownloadTaskMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into {0}(task_code, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,"
            + "create_by, update_by, is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_BY_DOWNLOAD_CODE_SQL =
        "select id, task_code, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,\n"
            + "create_by, update_by, is_deleted from {0} where task_code=? and app_id=?";
    private static final String LIST_BY_DOWNLOAD_SQL =
        "select id, task_code, task_desc, limiting_strategy, app_id,unified_app_id, enable_status, version, create_time, update_time,\n"
            + "create_by, update_by, is_deleted from {0} where task_code in (:taskCodeList) and app_id in (:appIdList)";
    private static final String REFRESH_TASK_DESC_SQL = "update {0} set task_desc = ? where id = ?";

    @Override
    public int insertSelective(AsyncDownloadTask task) {
        String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return jdbcTemplate
            .update(sql, task.getTaskCode(), task.getTaskDesc(),
                task.getLimitingStrategy(), task.getAppId(),
                task.getUnifiedAppId(), task.getEnableStatus(),
                task.getVersion(), task.getCreateTime(), task.getUpdateTime(),
                task.getCreateBy(), task.getUpdateBy(), task.getIsDeleted());
    }

    @Override
    public AsyncDownloadTask selectByDownloadCode(String taskCode, String appId) {
        String sql = MessageFormat
            .format(SELECT_BY_DOWNLOAD_CODE_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        List<AsyncDownloadTask> taskList = jdbcTemplate
            .query(sql, new Object[]{taskCode, appId}, new AsyncDownloadTaskRowMapper());
        if (CollectionUtils.isEmpty(taskList)) {
            return null;
        }
        return taskList.get(0);
    }

    @Override
    public List<AsyncDownloadTask> listByDownloadCode(List<String> downloadCodeList, List<String> appIdList) {

        Map<String, Object> paramMap = MapUtils.newHashMapWithExpectedSize(2);
        paramMap.put("taskCodeList", downloadCodeList);
        paramMap.put("appIdList", appIdList);

        String sql = MessageFormat
            .format(LIST_BY_DOWNLOAD_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return new NamedParameterJdbcTemplate(jdbcTemplate)
            .query(sql, paramMap, new AsyncDownloadTaskRowMapper());
    }

    @Override
    public int refreshTaskDesc(Long id, String taskDesc) {
        String sql = MessageFormat
            .format(REFRESH_TASK_DESC_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTaskTable());
        return jdbcTemplate.update(sql, taskDesc, id);
    }

    public static class AsyncDownloadTaskRowMapper implements RowMapper<AsyncDownloadTask> {

        @Override
        public AsyncDownloadTask mapRow(ResultSet resultSet, int i) throws SQLException {
            AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask();
            asyncDownloadTask.setId(resultSet.getLong("id"));
            asyncDownloadTask.setTaskCode(resultSet.getString("task_code"));
            asyncDownloadTask.setTaskDesc(resultSet.getString("task_desc"));
            asyncDownloadTask.setAppId(resultSet.getString("app_id"));
            asyncDownloadTask.setUnifiedAppId(resultSet.getString("unified_app_id"));
            asyncDownloadTask.setEnableStatus(resultSet.getInt("enable_status"));
            asyncDownloadTask.setLimitingStrategy(resultSet.getString("limiting_strategy"));
            asyncDownloadTask.setVersion(resultSet.getInt("version"));
            asyncDownloadTask.setCreateTime(resultSet.getDate("create_time"));
            asyncDownloadTask.setUpdateTime(resultSet.getDate("update_time"));
            asyncDownloadTask.setCreateBy(resultSet.getString("create_by"));
            asyncDownloadTask.setUpdateBy(resultSet.getString("update_by"));
            asyncDownloadTask.setIsDeleted(resultSet.getLong("is_deleted"));
            return asyncDownloadTask;
        }
    }


}
