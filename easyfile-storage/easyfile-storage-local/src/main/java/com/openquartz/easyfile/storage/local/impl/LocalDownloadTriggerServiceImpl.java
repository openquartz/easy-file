package com.openquartz.easyfile.storage.local.impl;

import com.openquartz.easyfile.storage.local.dictionary.DownloadTriggerStatusEnum;
import com.openquartz.easyfile.storage.download.DownloadTriggerService;
import com.openquartz.easyfile.storage.local.entity.AsyncDownloadTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTriggerMapper;
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
public class LocalDownloadTriggerServiceImpl implements DownloadTriggerService {

    private final AsyncDownloadTriggerMapper asyncDownloadTriggerMapper;

    @Override
    public void trigger(DownloadTriggerRequest request) {
        try {
            AsyncDownloadTrigger downloadTrigger = asyncDownloadTriggerMapper
                .selectByRegisterId(request.getRegisterId());
            if (Objects.nonNull(downloadTrigger)) {
                return;
            }
            AsyncDownloadTrigger trigger = new AsyncDownloadTrigger();
            trigger.setTriggerCount(0);
            trigger.setTriggerStatus(DownloadTriggerStatusEnum.INIT);
            trigger.setStartTime(new Date());
            trigger.setLastExecuteTime(new Date());
            trigger.setRegisterId(request.getRegisterId());
            trigger.setCreatingOwner(Objects.nonNull(IpUtil.getIp()) ? IpUtil.getIp() : "hostname-unknown");
            trigger.setProcessingOwner(StringUtils.EMPTY);
            asyncDownloadTriggerMapper.insert(trigger);
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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now);
        // 触发专属IP
        triggerCondition.setCreatingOwner(IpUtil.getIp());

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);
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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now.plusSeconds(-minReaperSeconds));

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);
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
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteEndTime(lookBackTime);

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            DownloadTriggerEntry triggerResult = new DownloadTriggerEntry();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public DownloadTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount) {
        AsyncDownloadTrigger trigger = asyncDownloadTriggerMapper.selectByRegisterId(registerId);
        if (Objects.isNull(trigger) || trigger.isSuccess() || trigger.getTriggerCount() > maxTriggerCount) {
            return null;
        }
        return DownloadTriggerEntry.of(trigger.getRegisterId(), trigger.getTriggerCount());
    }

    @Override
    public boolean startExecute(Long registerId, Integer triggerCount) {
        int execute = asyncDownloadTriggerMapper.execute(registerId, DownloadTriggerStatusEnum.EXECUTING,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.INIT, DownloadTriggerStatusEnum.WAITING,
                DownloadTriggerStatusEnum.FAIL), triggerCount);
        return execute > 0;
    }

    @Override
    public void exeSuccess(Long registerId) {
        asyncDownloadTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.SUCCESS,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.FAIL,
                DownloadTriggerStatusEnum.WAITING,
                DownloadTriggerStatusEnum.INIT));
    }

    @Override
    public void exeFail(Long registerId) {
        asyncDownloadTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.FAIL,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.INIT,
                DownloadTriggerStatusEnum.WAITING));
    }

    @Override
    public void enterWaiting(Long registerId) {
        asyncDownloadTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.WAITING,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.FAIL, DownloadTriggerStatusEnum.INIT));
    }

    @Override
    public void handleExpirationTrigger(Integer maxExpireSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxExpireSeconds);

        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);
        triggerCondition.setLastExecuteStartTime(now.plusHours(-48));

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);

        for (AsyncDownloadTrigger trigger : triggerList) {
            asyncDownloadTriggerMapper.refreshStatus(trigger.getRegisterId(), DownloadTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING));
        }
    }

    @Override
    public void handleWaitingExpirationTrigger(Integer maxWaitingSeconds) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime beforeSeconds = now.plusSeconds(-maxWaitingSeconds);

        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(10);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.WAITING));

        triggerCondition.setLastExecuteEndTime(beforeSeconds);

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);

        for (AsyncDownloadTrigger trigger : triggerList) {
            asyncDownloadTriggerMapper.refreshStatus(trigger.getRegisterId(), DownloadTriggerStatusEnum.FAIL,
                CollectionUtils.newArrayList(DownloadTriggerStatusEnum.WAITING));
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
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.SUCCESS));

        triggerCondition.setLastExecuteEndTime(beforeTime);

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);

        for (AsyncDownloadTrigger trigger : triggerList) {
            asyncDownloadTriggerMapper.deleteById(trigger.getId());
        }

        triggerCondition.setMinTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(CollectionUtils.newArrayList(DownloadTriggerStatusEnum.FAIL));
        triggerList = asyncDownloadTriggerMapper.select(triggerCondition);
        for (AsyncDownloadTrigger trigger : triggerList) {
            asyncDownloadTriggerMapper.deleteById(trigger.getId());
        }
    }
}
