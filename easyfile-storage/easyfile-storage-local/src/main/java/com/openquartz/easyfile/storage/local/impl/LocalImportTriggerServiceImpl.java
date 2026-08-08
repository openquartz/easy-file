package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.storage.imports.ImportTriggerService;
import com.openquartz.easyfile.storage.local.dictionary.ImportTriggerStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTriggerMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.QueryImportTriggerCondition;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.common.util.StringUtils;

/**
 * 本地DB导入触发执行
 *
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class LocalImportTriggerServiceImpl implements ImportTriggerService {

    private final AsyncImportTriggerMapper asyncImportTriggerMapper;

    @Override
    public void trigger(ImportTriggerRequest request) {
        try {
            AsyncImportTrigger importTrigger = asyncImportTriggerMapper
                .selectByRegisterId(request.getRegisterId());
            if (Objects.nonNull(importTrigger)) {
                // Retry / re-trigger: reset terminal SUCCESS/FAIL back to INIT for db_schedule pickup
                if (importTrigger.getTriggerStatus() == ImportTriggerStatusEnum.SUCCESS
                    || importTrigger.getTriggerStatus() == ImportTriggerStatusEnum.FAIL) {
                    asyncImportTriggerMapper.refreshStatus(request.getRegisterId(), ImportTriggerStatusEnum.INIT,
                        CollectionUtils.newArrayList(ImportTriggerStatusEnum.SUCCESS, ImportTriggerStatusEnum.FAIL));
                }
                return;
            }
            AsyncImportTrigger trigger = new AsyncImportTrigger();
            trigger.setTriggerCount(0);
            trigger.setTriggerStatus(ImportTriggerStatusEnum.INIT);
            trigger.setStartTime(new Date());
            trigger.setLastExecuteTime(new Date());
            trigger.setRegisterId(request.getRegisterId());
            trigger.setCreatingOwner(Objects.nonNull(IpUtil.getIp()) ? IpUtil.getIp() : "hostname-unknown");
            trigger.setProcessingOwner(StringUtils.EMPTY);
            asyncImportTriggerMapper.insert(trigger);
        } catch (Exception ex) {
            log.error("[LocalImportTriggerServiceImpl#trigger] save error! request:{}", request);
        }
    }

    @Override
    public List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset) {
        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(ImportTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now);
        // 触发专属IP
        triggerCondition.setCreatingOwner(IpUtil.getIp());

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            ImportTriggerEntry triggerResult = new ImportTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer minReaperSeconds, Integer triggerOffset) {
        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(ImportTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now.plusSeconds(-minReaperSeconds));

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            ImportTriggerEntry triggerResult = new ImportTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public List<ImportTriggerEntry> getTriggerCompensate(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset) {
        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(ImportTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteEndTime(lookBackTime);

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            ImportTriggerEntry triggerResult = new ImportTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public ImportTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount) {
        AsyncImportTrigger trigger = asyncImportTriggerMapper.selectByRegisterId(registerId);
        if (Objects.isNull(trigger) || trigger.isSuccess() || trigger.getTriggerCount() > maxTriggerCount) {
            return null;
        }
        return ImportTriggerEntry.of(trigger.getRegisterId(), trigger.getTriggerCount());
    }

    @Override
    public boolean startExecute(Long registerId, Integer triggerCount) {
        int execute = asyncImportTriggerMapper.execute(registerId, ImportTriggerStatusEnum.EXECUTING,
            CollectionUtils.newArrayList(ImportTriggerStatusEnum.INIT, ImportTriggerStatusEnum.WAITING,
                ImportTriggerStatusEnum.FAIL), triggerCount);
        return execute > 0;
    }

    @Override
    public void exeSuccess(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, ImportTriggerStatusEnum.SUCCESS,
            CollectionUtils.newArrayList(ImportTriggerStatusEnum.EXECUTING, ImportTriggerStatusEnum.FAIL,
                ImportTriggerStatusEnum.WAITING,
                ImportTriggerStatusEnum.INIT));
    }

    @Override
    public void exeFail(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, ImportTriggerStatusEnum.FAIL,
            CollectionUtils.newArrayList(ImportTriggerStatusEnum.EXECUTING, ImportTriggerStatusEnum.INIT,
                ImportTriggerStatusEnum.WAITING));
    }

    @Override
    public void enterWaiting(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, ImportTriggerStatusEnum.WAITING,
            CollectionUtils.newArrayList(ImportTriggerStatusEnum.FAIL, ImportTriggerStatusEnum.INIT));
    }

    @Override
    public void handleExpirationTrigger(Integer maxExpireSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxExpireSeconds);

        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(ImportTriggerStatusEnum.EXECUTING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);
        triggerCondition.setLastExecuteStartTime(now.plusHours(-48));

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);

        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.refreshStatus(trigger.getRegisterId(), ImportTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(ImportTriggerStatusEnum.EXECUTING));
        }
    }

    @Override
    public void handleWaitingExpirationTrigger(Integer maxWaitingSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxWaitingSeconds);

        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(ImportTriggerStatusEnum.WAITING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);

        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.refreshStatus(trigger.getRegisterId(), ImportTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(ImportTriggerStatusEnum.WAITING));
        }
    }

    @Override
    public void archiveHistoryTrigger(Integer archiveHours, Integer maxTriggerCount) {

        // 当归档配置小于等于0 时默认不归档
        if (archiveHours <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeTime = now.plusHours(-archiveHours);

        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(100);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(ImportTriggerStatusEnum.SUCCESS));

        triggerCondition.setLastExecuteEndTime(beforeTime);

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);

        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.deleteById(trigger.getId());
        }

        triggerCondition.setMinTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(ImportTriggerStatusEnum.FAIL));
        triggerList = asyncImportTriggerMapper.select(triggerCondition);
        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.deleteById(trigger.getId());
        }
    }
}
