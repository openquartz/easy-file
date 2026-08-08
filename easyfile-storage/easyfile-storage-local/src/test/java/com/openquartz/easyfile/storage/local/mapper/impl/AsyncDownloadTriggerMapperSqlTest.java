package com.openquartz.easyfile.storage.local.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyfile.storage.local.prop.EasyFileTableGeneratorSupplier;
import java.text.MessageFormat;
import org.junit.After;
import org.junit.Test;

/**
 * selectByRegisterId SQL must honor custom table prefix.
 */
public class AsyncDownloadTriggerMapperSqlTest {

    private static final String SELECT_BY_ID_SQL =
        "select id,register_id, trigger_status, start_time, last_execute_time,trigger_count,creating_owner,processing_owner from {0} where register_id = ?";

    @After
    public void resetPrefix() {
        EasyFileTableGeneratorSupplier.setPrefix("ef");
    }

    @Test
    public void selectByRegisterIdSqlShouldUseConfiguredTableName() {
        EasyFileTableGeneratorSupplier.setPrefix("custom");
        String table = EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable();
        assertEquals("custom_async_download_trigger", table);
        String sql = MessageFormat.format(SELECT_BY_ID_SQL, table);
        assertTrue(sql.contains("from custom_async_download_trigger where"));
        assertTrue(sql.contains("where register_id = ?"));
    }
}
