package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.server.common.LockSupport;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.openquartz.easyfile.common.bean.Pair;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.dictionary.EnableStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.server.common.lock.LockBizType;
import com.openquartz.easyfile.server.entity.AsyncDownloadTask;
import com.openquartz.easyfile.server.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.server.service.AsyncDownloadTaskService;

/**
 * 异步下载任务服务
 *
 * @author svnee
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncDownloadTaskServiceImpl implements AsyncDownloadTaskService {

    private final AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    private final LockSupport lockSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoRegister(AutoTaskRegisterRequest request) {
        lockSupport
            .consumeIfTryLock(Pair.of(request.getAppId(), LockBizType.TASK_AUTO_REGISTER_LOCK), () -> {

                List<String> downloadCodeList = CollectionUtils
                    .newArrayList(request.getDownloadCodeMap().keySet());
                List<AsyncDownloadTask> taskList = asyncDownloadTaskMapper
                    .listByDownloadCode(downloadCodeList, CollectionUtils.newArrayList(request.getAppId()));

                Map<String, AsyncDownloadTask> downloadTaskMap = taskList.stream()
                    .collect(Collectors.toMap(AsyncDownloadTask::getTaskCode, e -> e));
                List<AsyncDownloadTask> toInitDownloadTaskList = CollectionUtils.newArrayList();

                request.getDownloadCodeMap().entrySet().forEach(e -> {
                    if (!downloadTaskMap.containsKey(e.getKey())) {
                        AsyncDownloadTask task = convert(request, e);
                        toInitDownloadTaskList.add(task);
                    } else if (!Objects.equals(downloadTaskMap.get(e.getKey()).getTaskDesc(), e.getValue())) {
                        AsyncDownloadTask task = downloadTaskMap.get(e.getKey());
                        asyncDownloadTaskMapper.refreshTaskDesc(task.getId(), e.getValue());
                    }
                });

                if (CollectionUtils.isNotEmpty(toInitDownloadTaskList)) {
                    asyncDownloadTaskMapper.insertList(toInitDownloadTaskList);
                }
            });
    }

    private AsyncDownloadTask convert(AutoTaskRegisterRequest request, Entry<String, String> entry) {
        AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask();
        asyncDownloadTask.setTaskCode(entry.getKey());
        asyncDownloadTask.setTaskDesc(entry.getValue());
        asyncDownloadTask.setAppId(request.getAppId());
        asyncDownloadTask.setUnifiedAppId(request.getUnifiedAppId());
        asyncDownloadTask.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
        asyncDownloadTask.setLimitingStrategy(Constants.DEFAULT_LIMITING_STRATEGY);
        asyncDownloadTask.setVersion(Constants.DATA_INIT_VERSION);
        asyncDownloadTask.setCreateTime(new Date());
        asyncDownloadTask.setUpdateTime(new Date());
        asyncDownloadTask.setCreateBy(Constants.SYSTEM_USER);
        asyncDownloadTask.setUpdateBy(Constants.SYSTEM_USER);
        asyncDownloadTask.setIsDeleted(0L);
        return asyncDownloadTask;
    }
}
