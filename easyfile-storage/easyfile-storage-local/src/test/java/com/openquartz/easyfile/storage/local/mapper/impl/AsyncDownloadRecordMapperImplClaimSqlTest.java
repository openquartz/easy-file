package com.openquartz.easyfile.storage.local.mapper.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import org.junit.Test;

public class AsyncDownloadRecordMapperImplClaimSqlTest {

    private static final String TABLE = "ef_async_download_record";

    @Test
    public void claimRetrySql_usesPlaceholdersNotQuotedLiterals() {
        String sql = MessageFormat.format(AsyncDownloadRecordMapperImpl.CLAIM_RETRY_SQL, TABLE);

        assertTrue(sql.contains("upload_status = ?"));
        assertTrue(sql.contains("error_msg = ?"));
        assertFalse(sql.contains("upload_status = fail"));
        assertFalse(sql.contains("upload_status = none"));
        assertFalse(sql.contains("error_msg = '"));
    }

    @Test
    public void selectRetryableSql_usesNamedParametersNotQuotedLiterals() {
        String sql = MessageFormat.format(AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_SQL, TABLE)
            + AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_ORDER_BY_CLAUSE
            + AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_LIMIT_CLAUSE;

        assertTrue(sql.contains("upload_status = :failStatus"));
        assertTrue(sql.contains("upload_status = :noneStatus"));
        assertTrue(sql.contains("order by case when upload_status = :failStatus then 0 else 1 end"));
        assertTrue(sql.contains("last_execute_time asc"));
        assertFalse(sql.contains("upload_status = fail"));
        assertFalse(sql.contains("upload_status = none"));
        assertFalse(sql.contains("upload_status = 'fail'"));
    }

    @Test
    public void selectRetryableSql_withAppIdIncludesAppIdBind() {
        String sql = MessageFormat.format(AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_SQL, TABLE)
            + AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_APP_ID_CLAUSE
            + AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_ORDER_BY_CLAUSE
            + AsyncDownloadRecordMapperImpl.SELECT_RETRYABLE_LIMIT_CLAUSE;

        assertTrue(sql.contains("app_id = :appId"));
        assertTrue(sql.contains("upload_status = :failStatus"));
        assertTrue(sql.contains("order by case when upload_status = :failStatus then 0 else 1 end"));
    }
}
