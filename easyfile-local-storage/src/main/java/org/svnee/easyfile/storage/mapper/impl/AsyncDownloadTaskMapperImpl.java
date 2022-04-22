package org.svnee.easyfile.storage.mapper.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.svnee.easyfile.storage.entity.AsyncDownloadTask;
import org.svnee.easyfile.storage.mapper.AsyncDownloadTaskMapper;

/**
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncDownloadTaskMapperImpl implements AsyncDownloadTaskMapper {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int insertSelective(AsyncDownloadTask asyncDownloadTask) {
        return 0;
    }

    @Override
    public AsyncDownloadTask selectByDownloadCode(String taskCode, String appId) {
        return null;
    }

    @Override
    public List<AsyncDownloadTask> listByDownloadCode(List<String> downloadCodeList, List<String> appIdList) {
        return null;
    }

    @Override
    public int refreshTaskDesc(Long id, String taskDesc) {
        return 0;
    }
}
