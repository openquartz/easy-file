package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.common.request.ImportTriggerRequest;
import com.openquartz.easyfile.common.response.ImportTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import com.openquartz.easyfile.storage.importer.ImportTriggerService;
import com.openquartz.easyfile.storage.local.dictionary.DownloadTriggerStatusEnum;
import com.openquartz.easyfile.storage.local.entity.AsyncImportTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncImportTriggerMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.QueryImportTriggerCondition;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地DB触发执行
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
                return;
            }
            AsyncImportTrigger trigger = new AsyncImportTrigger();
            trigger.setTriggerCount(0);
            trigger.setTriggerStatus(DownloadTriggerStatusEnum.INIT);
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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

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
        if (Objects.isNull(trigger)) {
            return null;
        }
        if (trigger.getTriggerCount() >= maxTriggerCount) {
            return null;
        }
        ImportTriggerEntry triggerResult = new ImportTriggerEntry();
        triggerResult.setRegisterId(trigger.getRegisterId());
        triggerResult.setTriggerCount(trigger.getTriggerCount());
        return triggerResult;
    }

    @Override
    public boolean startExecute(Long registerId, Integer triggerCount) {
        int execute = asyncImportTriggerMapper.execute(registerId, DownloadTriggerStatusEnum.EXECUTING,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.INIT, DownloadTriggerStatusEnum.WAITING,
                DownloadTriggerStatusEnum.FAIL), triggerCount);
        return execute > 0;
    }

    @Override
    public void exeSuccess(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.SUCCESS,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.FAIL,
                DownloadTriggerStatusEnum.WAITING,
                DownloadTriggerStatusEnum.INIT));
    }

    @Override
    public void exeFail(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.FAIL,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.INIT,
                DownloadTriggerStatusEnum.WAITING));
    }

    @Override
    public void enterWaiting(Long registerId) {
        asyncImportTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.WAITING,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.FAIL, DownloadTriggerStatusEnum.INIT));
    }

    @Override
    public void handleExpirationTrigger(Integer maxExpireSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beforeSeconds = now.plusSeconds(-maxExpireSeconds);

        QueryImportTriggerCondition triggerCondition = new QueryImportTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);
        triggerCondition.setLastExecuteStartTime(now.plusHours(-48));

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);

        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.refreshStatus(trigger.getRegisterId(), DownloadTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING));
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
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.SUCCESS));

        triggerCondition.setLastExecuteEndTime(beforeTime);

        List<AsyncImportTrigger> triggerList = asyncImportTriggerMapper.select(triggerCondition);

        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.deleteById(trigger.getId());
        }

        triggerCondition.setMinTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.FAIL));
        triggerList = asyncImportTriggerMapper.select(triggerCondition);
        for (AsyncImportTrigger trigger : triggerList) {
            asyncImportTriggerMapper.deleteById(trigger.getId());
        }
    }
}
