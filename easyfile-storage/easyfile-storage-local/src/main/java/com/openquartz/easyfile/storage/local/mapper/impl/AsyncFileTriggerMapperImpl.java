package com.openquartz.easyfile.storage.local.mapper.impl;

import com.openquartz.easyfile.storage.local.dictionary.FileTriggerStatusEnum;
import com.openquartz.easyfile.storage.local.mapper.condition.QueryDownloadTriggerCondition;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
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
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.local.entity.AsyncFileTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncFileTriggerMapper;
import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;

/**
 * AsyncFileTriggerMapperImpl
 *
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncFileTriggerMapperImpl implements AsyncFileTriggerMapper {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into {0}(register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner) values (?,?,?,?,?,?,?)";
    private static final String REFRESH_STATUS_SQL =
        "update {0} set trigger_status= :triggerStatus,last_execute_time= :lastExecuteTime where register_id = :registerId and trigger_status in (:taskStatusList) ";
    private static final String TRIGGER_EXECUTE_SQL =
        "update {0} set trigger_status= :triggerStatus,last_execute_time= :lastExecuteTime,trigger_count=trigger_count+1,processing_owner= :processingOwner where register_id = :registerId and trigger_status in (:taskStatusList) and trigger_count= :triggerCount";
    private static final String SELECT_SQL =
        "select id,register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner from {0}";
    private static final String SELECT_BY_ID_SQL =
        "select id,register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner from ef_async_download_trigger where register_id = ?";
    private static final String DELETE_BY_ID_SQL = "delete from {0} where id = ?";

    @Override
    public int insert(AsyncFileTrigger trigger) {

        String sql = MessageFormat.format(INSERT_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
    public int refreshStatus(Long registerId, FileTriggerStatusEnum triggerStatus,
        List<FileTriggerStatusEnum> delineateStatusList) {
        LocalDateTime now = LocalDateTime.now();

        List<String> taskStatusList = delineateStatusList.stream().map(FileTriggerStatusEnum::getCode)
            .collect(Collectors.toList());

        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("triggerStatus", triggerStatus.getCode());
        paramMap.put("lastExecuteTime", now);
        paramMap.put("registerId", registerId);

        String sql = MessageFormat
            .format(REFRESH_STATUS_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable());

        return new NamedParameterJdbcTemplate(jdbcTemplate).update(sql, paramMap);
    }

    @Override
    public int execute(Long registerId, FileTriggerStatusEnum triggerStatus,
        List<FileTriggerStatusEnum> delineateStatusList, Integer triggerCount) {
        LocalDateTime now = LocalDateTime.now();

        List<String> taskStatusList = delineateStatusList.stream().map(FileTriggerStatusEnum::getCode)
            .collect(Collectors.toList());
        Map<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("triggerStatus", triggerStatus.getCode());
        paramMap.put("lastExecuteTime", now);
        paramMap.put("registerId", registerId);
        paramMap.put("triggerCount", triggerCount);
        paramMap.put("processingOwner", Objects.nonNull(IpUtil.getIp()) ? IpUtil.getIp() : "hostname-unknow");

        String sql = MessageFormat
            .format(TRIGGER_EXECUTE_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable());

        return new NamedParameterJdbcTemplate(jdbcTemplate).update(sql, paramMap);
    }

    @Override
    public List<AsyncFileTrigger> select(QueryDownloadTriggerCondition condition) {

        List<String> taskStatusList = condition.getTriggerStatusList().stream().map(FileTriggerStatusEnum::getCode)
            .collect(Collectors.toList());

        Map<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("taskStatusList", taskStatusList);
        paramMap.put("creatingOwner", condition.getCreatingOwner());
        paramMap.put("lastExecuteStartTime", condition.getLastExecuteStartTime());
        paramMap.put("lastExecuteEndTime", condition.getLastExecuteEndTime());
        paramMap.put("maxTriggerCount", condition.getMaxTriggerCount());
        paramMap.put("minTriggerCount", condition.getMinTriggerCount());
        paramMap.put("offset", condition.getOffset());

        StringBuilder sqlBuilder = new StringBuilder(MessageFormat.format(SELECT_SQL,
            EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable()));
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
    public AsyncFileTrigger selectByRegisterId(Long registerId) {
        String sql = MessageFormat
            .format(SELECT_BY_ID_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable());
        List<AsyncFileTrigger> triggerList = jdbcTemplate
            .query(sql, new AsyncDownloadTriggerRowMapper(), registerId);
        return CollectionUtils.isNotEmpty(triggerList) ? triggerList.get(0) : null;
    }

    private static class AsyncDownloadTriggerRowMapper implements RowMapper<AsyncFileTrigger> {

        @Override
        public AsyncFileTrigger mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            AsyncFileTrigger downloadTrigger = new AsyncFileTrigger();
            downloadTrigger.setId(resultSet.getLong("id"));
            downloadTrigger.setRegisterId(resultSet.getLong("register_id"));
            downloadTrigger.setTriggerStatus(FileTriggerStatusEnum.ofCode(resultSet.getString("trigger_status")));
            downloadTrigger.setStartTime(resultSet.getTimestamp("start_time"));
            downloadTrigger.setLastExecuteTime(resultSet.getTimestamp("last_execute_time"));
            downloadTrigger.setTriggerCount(resultSet.getInt("trigger_count"));
            downloadTrigger.setCreatingOwner(resultSet.getString("creating_owner"));
            downloadTrigger.setProcessingOwner(resultSet.getString("processing_owner"));
            return downloadTrigger;
        }
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(
            MessageFormat.format(DELETE_BY_ID_SQL, EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable()), id);
    }
}
