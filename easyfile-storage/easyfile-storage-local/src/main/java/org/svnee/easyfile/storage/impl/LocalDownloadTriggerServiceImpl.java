package org.svnee.easyfile.storage.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.request.DownloadTriggerRequest;
import org.svnee.easyfile.common.response.DownloadTriggerResult;
import org.svnee.easyfile.common.util.CollectionUtils;
import org.svnee.easyfile.storage.dictionary.DownloadTriggerStatusEnum;
import org.svnee.easyfile.storage.download.DownloadTriggerService;
import org.svnee.easyfile.storage.entity.AsyncDownloadTrigger;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTriggerMapper;
import org.svnee.easyfile.storage.mapper.condition.QueryDownloadTriggerCondition;

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
            asyncDownloadTriggerMapper.insert(trigger);
        } catch (Exception ex) {
            log.error("[LocalDownloadTriggerServiceImpl#trigger] save error! request:{}", request);
        }
    }

    @Override
    public List<DownloadTriggerResult> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
        Integer triggerOffset) {
        QueryDownloadTriggerCondition triggerCondition = new QueryDownloadTriggerCondition();
        triggerCondition.setOffset(triggerOffset);
        triggerCondition.setMaxTriggerCount(maxTriggerCount);
        triggerCondition.setTriggerStatusList(DownloadTriggerStatusEnum.EXE_TRIGGER_STATUS_LIST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookBackTime = now.plusHours(-lookBackHours);
        triggerCondition.setLastExecuteStartTime(lookBackTime);
        triggerCondition.setLastExecuteEndTime(now);

        List<AsyncDownloadTrigger> triggerList = asyncDownloadTriggerMapper.select(triggerCondition);
        return triggerList.stream().map(e -> {
            DownloadTriggerResult triggerResult = new DownloadTriggerResult();
            triggerResult.setRegisterId(e.getRegisterId());
            triggerResult.setTriggerCount(e.getTriggerCount());
            return triggerResult;
        }).distinct().collect(Collectors.toList());
    }

    @Override
    public boolean startExecute(Long registerId, Integer triggerCount) {
        int execute = asyncDownloadTriggerMapper.execute(registerId, DownloadTriggerStatusEnum.EXECUTING,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.INIT, DownloadTriggerStatusEnum.FAIL), triggerCount);
        return execute > 0;
    }

    @Override
    public void exeSuccess(Long registerId) {
        asyncDownloadTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.SUCCESS,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.FAIL,
                DownloadTriggerStatusEnum.INIT));
    }

    @Override
    public void exeFail(Long registerId) {
        asyncDownloadTriggerMapper.refreshStatus(registerId, DownloadTriggerStatusEnum.FAIL,
            CollectionUtils.newArrayList(DownloadTriggerStatusEnum.EXECUTING, DownloadTriggerStatusEnum.INIT));
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
    public void archiveHistoryTrigger(Integer archiveHours, Integer maxTriggerCount) {
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
