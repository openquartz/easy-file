package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.storage.local.dictionary.FileTriggerStatusEnum;
import com.openquartz.easyfile.storage.download.FileTriggerService;
import com.openquartz.easyfile.storage.local.entity.AsyncFileTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncFileTriggerMapper;
import com.openquartz.easyfile.storage.local.mapper.condition.QueryDownloadTriggerCondition;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.common.response.DownloadTriggerEntry;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.common.util.IpUtil;
import com.openquartz.easyfile.common.util.StringUtils;

/**
 * 本地DB触发执行
 *
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class LocalFileTriggerServiceImpl implements FileTriggerService {

    private final AsyncFileTriggerMapper asyncFileTriggerMapper;

    @Override
    public void trigger(DownloadTriggerRequest request) {
        try {
            AsyncFileTrigger downloadTrigger = asyncFileTriggerMapper
                .selectByRegisterId(request.getRegisterId());
            if (Objects.nonNull(downloadTrigger)) {
                return;
            }
            AsyncFileTrigger trigger = new AsyncFileTrigger();
            trigger.setTriggerCount(0);
            trigger.setTriggerStatus(FileTriggerStatusEnum.INIT);
            trigger.setStartTime(new Date());
            trigger.setLastExecuteTime(new Date());
            trigger.setRegisterId(request.getRegisterId());
            trigger.setCreatingOwner(Objects.nonNull(IpUtil.getIp()) ? IpUtil.getIp() : "hostname-unknown");
            trigger.setProcessingOwner(StringUtils.EMPTY);
            asyncFileTriggerMapper.insert(trigger);
        } catch (Exception ex) {
            log.error("[LocalDownloadTriggerServiceImpl#trigger] save error! request:{}", request);
        }
    }

    @Override
    public List<DownloadTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset) {
        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(FileTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now);
        // 触发专属IP
        triggerCondition.setCreatingOwner(IpUtil.getIp());

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            DownloadTriggerEntry triggerResult = new DownloadTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public List<DownloadTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer minReaperSeconds, Integer triggerOffset) {
        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(FileTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now.plusSeconds(-minReaperSeconds));

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            DownloadTriggerEntry triggerResult = new DownloadTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public List<DownloadTriggerEntry> getTriggerCompensate(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset) {
        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(FileTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteEndTime(lookBackTime);

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            DownloadTriggerEntry triggerResult = new DownloadTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public DownloadTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount) {
        AsyncFileTrigger trigger = asyncFileTriggerMapper.selectByRegisterId(registerId);
        if (Objects.isNull(trigger) || trigger.isSuccess() || trigger.getTriggerCount() > maxTriggerCount) {
            return null;
        }
        return DownloadTriggerEntry.of(trigger.getRegisterId(), trigger.getTriggerCount());
    }

    @Override
    public boolean startExecute(Long registerId, Integer triggerCount) {
        int execute = asyncFileTriggerMapper.execute(registerId, FileTriggerStatusEnum.EXECUTING,
            CollectionUtils.newArrayList(FileTriggerStatusEnum.INIT, FileTriggerStatusEnum.WAITING,
                FileTriggerStatusEnum.FAIL), triggerCount);
        return execute > 0;
    }

    @Override
    public void exeSuccess(Long registerId) {
        asyncFileTriggerMapper.refreshStatus(registerId, FileTriggerStatusEnum.SUCCESS,
            CollectionUtils.newArrayList(FileTriggerStatusEnum.EXECUTING, FileTriggerStatusEnum.FAIL,
                FileTriggerStatusEnum.WAITING,
                FileTriggerStatusEnum.INIT));
    }

    @Override
    public void exeFail(Long registerId) {
        asyncFileTriggerMapper.refreshStatus(registerId, FileTriggerStatusEnum.FAIL,
            CollectionUtils.newArrayList(FileTriggerStatusEnum.EXECUTING, FileTriggerStatusEnum.INIT,
                FileTriggerStatusEnum.WAITING));
    }

    @Override
    public void enterWaiting(Long registerId) {
        asyncFileTriggerMapper.refreshStatus(registerId, FileTriggerStatusEnum.WAITING,
            CollectionUtils.newArrayList(FileTriggerStatusEnum.FAIL, FileTriggerStatusEnum.INIT));
    }

    @Override
    public void handleExpirationTrigger(Integer maxExpireSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxExpireSeconds);

        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(FileTriggerStatusEnum.EXECUTING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);
        triggerCondition.setLastExecuteStartTime(now.plusHours(-48));

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);

        for (AsyncFileTrigger trigger : triggerList) {
            asyncFileTriggerMapper.refreshStatus(trigger.getRegisterId(), FileTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(FileTriggerStatusEnum.EXECUTING));
        }
    }

    @Override
    public void handleWaitingExpirationTrigger(Integer maxWaitingSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxWaitingSeconds);

        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(FileTriggerStatusEnum.WAITING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);

        for (AsyncFileTrigger trigger : triggerList) {
            asyncFileTriggerMapper.refreshStatus(trigger.getRegisterId(), FileTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(FileTriggerStatusEnum.WAITING));
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

        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(100);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(FileTriggerStatusEnum.SUCCESS));

        triggerCondition.setLastExecuteEndTime(beforeTime);

        List<AsyncFileTrigger> triggerList = asyncFileTriggerMapper.select(triggerCondition);

        for (AsyncFileTrigger trigger : triggerList) {
            asyncFileTriggerMapper.deleteById(trigger.getId());
        }

        triggerCondition.setMinTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(FileTriggerStatusEnum.FAIL));
        triggerList = asyncFileTriggerMapper.select(triggerCondition);
        for (AsyncFileTrigger trigger : triggerList) {
            asyncFileTriggerMapper.deleteById(trigger.getId());
        }
    }
}
