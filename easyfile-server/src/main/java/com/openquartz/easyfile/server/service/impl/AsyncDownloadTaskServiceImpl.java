package com.openquartz.easyfile.server.service.impl;

import com.openquartz.easyfile.server.common.lock.LockSupport;
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
import com.openquartz.easyfile.server.entity.AsyncFileTask;
import com.openquartz.easyfile.server.mapper.AsyncFileTaskMapper;
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

    private final AsyncFileTaskMapper asyncFileTaskMapper;
    private final LockSupport lockSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoRegister(AutoTaskRegisterRequest request) {
        lockSupport
            .consumeIfTryLock(Pair.of(request.getAppId(), LockBizType.TASK_AUTO_REGISTER_LOCK), () -> {

                List<String> downloadCodeList = CollectionUtils
                    .newArrayList(request.getDownloadCodeMap().keySet());
                List<AsyncFileTask> taskList = asyncFileTaskMapper
                    .listByDownloadCode(downloadCodeList, CollectionUtils.newArrayList(request.getAppId()));

                Map<String, AsyncFileTask> downloadTaskMap = taskList.stream()
                    .collect(Collectors.toMap(AsyncFileTask::getTaskCode, e -> e));
                List<AsyncFileTask> toInitDownloadTaskList = CollectionUtils.newArrayList();

                request.getDownloadCodeMap().entrySet().forEach(e -> {
                    if (!downloadTaskMap.containsKey(e.getKey())) {
                        AsyncFileTask task = convert(request, e);
                        toInitDownloadTaskList.add(task);
                    } else if (!Objects.equals(downloadTaskMap.get(e.getKey()).getTaskDesc(), e.getValue())) {
                        AsyncFileTask task = downloadTaskMap.get(e.getKey());
                        asyncFileTaskMapper.refreshTaskDesc(task.getId(), e.getValue());
                    }
                });

                if (CollectionUtils.isNotEmpty(toInitDownloadTaskList)) {
                    asyncFileTaskMapper.insertList(toInitDownloadTaskList);
                }
            });
    }

    private AsyncFileTask convert(AutoTaskRegisterRequest request, Entry<String, String> entry) {
        AsyncFileTask asyncFileTask = new AsyncFileTask();
        asyncFileTask.setTaskCode(entry.getKey());
        asyncFileTask.setTaskType(request.getHandleType());
        asyncFileTask.setTaskDesc(entry.getValue());
        asyncFileTask.setAppId(request.getAppId());
        asyncFileTask.setUnifiedAppId(request.getUnifiedAppId());
        asyncFileTask.setEnableStatus(EnableStatusEnum.ENABLE.getCode());
        asyncFileTask.setLimitingStrategy(Constants.DEFAULT_LIMITING_STRATEGY);
        asyncFileTask.setVersion(Constants.DATA_INIT_VERSION);
        asyncFileTask.setCreateTime(new Date());
        asyncFileTask.setUpdateTime(new Date());
        asyncFileTask.setCreateBy(Constants.SYSTEM_USER);
        asyncFileTask.setUpdateBy(Constants.SYSTEM_USER);
        asyncFileTask.setDeleted(0L);
        return asyncFileTask;
    }
}
