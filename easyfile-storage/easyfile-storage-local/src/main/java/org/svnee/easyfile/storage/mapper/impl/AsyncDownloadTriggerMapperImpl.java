package org.svnee.easyfile.storage.mapper.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.common.util.IpUtil;
import org.svnee.easyfile.common.util.StringUtils;
import org.svnee.easyfile.storage.dictionary.DownloadTriggerStatusEnum;
import org.svnee.easyfile.storage.entity.AsyncDownloadTrigger;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTriggerMapper;
import org.svnee.easyfile.storage.mapper.condition.QueryDownloadTriggerCondition;

/**
 * AsyncDownloadTriggerMapperImpl
 *
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncDownloadTriggerMapperImpl implements AsyncDownloadTriggerMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into ef_async_download_trigger(register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner) values (?,?,?,?,?,?,?)";
    private static final String REFRESH_STATUS_SQL =
        "update ef_async_download_trigger set trigger_status= :triggerStatus,last_execute_time= :lastExecuteTime where register_id = :registerId and trigger_status in (:taskStatusList) ";
    private static final String TRIGGER_EXECUTE_SQL =
        "update ef_async_download_trigger set trigger_status= :triggerStatus,last_execute_time= :lastExecuteTime,trigger_count=trigger_count+1,processing_owner= :processingOwner where register_id = :registerId and trigger_status in (:taskStatusList) and trigger_count= :triggerCount";
    private static final String SELECT_SQL =
        "select id,register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner from ef_async_download_trigger";
    private static final String SELECT_BY_ID_SQL =
        "select id,register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner from ef_async_download_trigger where register_id = ?";
    private static final String DELETE_BY_ID_SQL = "delete from ef_async_download_trigger where id = ?";

    @Override
    public int insert(AsyncDownloadTrigger trigger) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, trigger.getRegisterId());
            ps.setString(2, trigger.getTriggerStatus().getCode());
            ps.setTimestamp(3, new Timestamp(trigger.getStartTime().getTime()));
            ps.setTimestamp(4, new Timestamp(trigger.getLastExecuteTime().getTime()));
            ps.setInt(5, trigger.getTriggerCount());
            ps.setString(6, trigger.getCreatingOwner());
            ps.setString(7, trigger.getProcessingOwner());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (Objects.nonNull(key)) {
            trigger.setId(key.longValue());
            return 1;
        }
        return 0;
    }

    @Override
    public int refreshStatus(Long registerId, DownloadTriggerStatusEnum triggerStatus,
        List<DownloadTriggerStatusEnum> delineateStatusList) {
        LocalDateTime now = LocalDateTime.now();

        List<String> taskStatusList = delineateStatusList.stream().map(DownloadTriggerStatusEnum::getCode)
            .collect(Collectors.toList());

        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("triggerStatus", triggerStatus.getCode());
        paramMap.put("lastExecuteTime", now);
        paramMap.put("registerId", registerId);
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(REFRESH_STATUS_SQL, paramMap);
    }

    @Override
    public int execute(Long registerId, DownloadTriggerStatusEnum triggerStatus,
        List<DownloadTriggerStatusEnum> delineateStatusList, Integer triggerCount) {
        LocalDateTime now = LocalDateTime.now();

        List<String> taskStatusList = delineateStatusList.stream().map(DownloadTriggerStatusEnum::getCode)
            .collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("triggerStatus", triggerStatus.getCode());
        paramMap.put("lastExecuteTime", now);
        paramMap.put("registerId", registerId);
        paramMap.put("triggerCount", triggerCount);
        paramMap.put("processingOwner", Objects.nonNull(IpUtil.getIp()) ? IpUtil.getIp() : "hostname-unknow");
        return new NamedParameterJdbcTemplate(jdbcTemplate).update(TRIGGER_EXECUTE_SQL, paramMap);
    }

    @Override
    public List<AsyncDownloadTrigger> select(QueryDownloadTriggerCondition condition) {

        List<String> taskStatusList = condition.getTriggerStatusList().stream().map(DownloadTriggerStatusEnum::getCode)
            .collect(Collectors.toList());

        Map<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("creatingOwner", condition.getCreatingOwner());
        paramMap.put("lastExecuteStartTime", condition.getLastExecuteStartTime());
        paramMap.put("lastExecuteEndTime", condition.getLastExecuteEndTime());
        paramMap.put("maxTriggerCount", condition.getMaxTriggerCount());
        paramMap.put("minTriggerCount", condition.getMinTriggerCount());
        paramMap.put("offset", condition.getOffset());

        StringBuilder sqlBuilder = new StringBuilder(SELECT_SQL);
        sqlBuilder.append(" where trigger_status in (:taskStatusList)");
        if (Objects.nonNull(condition.getLastExecuteStartTime())) {
            sqlBuilder.append(" and last_execute_time>= :lastExecuteStartTime");
        }
        if (Objects.nonNull(condition.getLastExecuteEndTime())) {
            sqlBuilder.append(" and last_execute_time< :lastExecuteEndTime");
        }
        if (Objects.nonNull(condition.getMaxTriggerCount())) {
            sqlBuilder.append(" and trigger_count<= :maxTriggerCount");
        }
        if (Objects.nonNull(condition.getMinTriggerCount())) {
            sqlBuilder.append(" and trigger_count> :minTriggerCount");
        }
        if (StringUtils.isNotBlank(condition.getCreatingOwner())) {
            sqlBuilder.append(" and creating_owner= :creatingOwner");
        }
        sqlBuilder.append(" order by register_id limit :offset");
        return new NamedParameterJdbcTemplate(jdbcTemplate)
            .query(sqlBuilder.toString(), paramMap, new AsyncDownloadTriggerRowMapper());
    }

    @Override
    public AsyncDownloadTrigger selectByRegisterId(Long registerId) {
        List<AsyncDownloadTrigger> triggerList = jdbcTemplate
            .query(SELECT_BY_ID_SQL, new AsyncDownloadTriggerRowMapper(), registerId);
        return CollectionUtils.isNotEmpty(triggerList) ? triggerList.get(0) : null;
    }

    private static class AsyncDownloadTriggerRowMapper implements RowMapper<AsyncDownloadTrigger> {

        @Override
        public AsyncDownloadTrigger mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            AsyncDownloadTrigger downloadTrigger = new AsyncDownloadTrigger();
            downloadTrigger.setId(resultSet.getLong("id"));
            downloadTrigger.setRegisterId(resultSet.getLong("register_id"));
            downloadTrigger.setTriggerStatus(DownloadTriggerStatusEnum.ofCode(resultSet.getString("trigger_status")));
            downloadTrigger.setStartTime(resultSet.getDate("start_time"));
            downloadTrigger.setLastExecuteTime(resultSet.getDate("last_execute_time"));
            downloadTrigger.setTriggerCount(resultSet.getInt("trigger_count"));
            downloadTrigger.setCreatingOwner(resultSet.getString("creating_owner"));
            downloadTrigger.setProcessingOwner(resultSet.getString("processing_owner"));
            return downloadTrigger;
        }
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }
}
