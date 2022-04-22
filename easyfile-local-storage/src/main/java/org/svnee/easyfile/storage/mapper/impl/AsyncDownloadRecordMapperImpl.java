package org.svnee.easyfile.storage.mapper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.svnee.easyfile.common.dictionary.UploadStatusEnum;
import org.svnee.easyfile.storage.entity.AsyncDownloadRecord;
import org.svnee.easyfile.storage.mapper.AsyncDownloadRecordMapper;
import org.svnee.easyfile.storage.mapper.condition.UploadInfoChangeCondition;

/**
 * @author svnee
 **/
@Slf4j
@RequiredArgsConstructor
public class AsyncDownloadRecordMapperImpl implements AsyncDownloadRecordMapper {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int insertSelective(AsyncDownloadRecord asyncDownloadRecord) {
        return 0;
    }

    @Override
    public AsyncDownloadRecord findById(Long id) {
        return null;
    }

    @Override
    public int changeUploadInfo(UploadInfoChangeCondition condition) {
        return 0;
    }

    @Override
    public int refreshUploadStatus(Long id, UploadStatusEnum oriUploadStatus, UploadStatusEnum tagUploadStatus,
        String updateBy) {
        return 0;
    }

    @Override
    public void download(Long id, UploadStatusEnum uploadStatus) {

    }
}
