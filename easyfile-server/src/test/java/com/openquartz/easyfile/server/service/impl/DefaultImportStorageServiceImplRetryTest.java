package com.openquartz.easyfile.server.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.service.IdempotentImportManager;
import java.sql.ResultSet;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * DefaultImportStorageServiceImpl.retry resets resume_position and clears idempotent marks.
 */
public class DefaultImportStorageServiceImplRetryTest {

    private JdbcTemplate jdbcTemplate;
    private IdempotentImportManager idempotentImportManager;
    private DefaultImportStorageServiceImpl service;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        idempotentImportManager = mock(IdempotentImportManager.class);
        ObjectProvider<IdempotentImportManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(idempotentImportManager);
        service = new DefaultImportStorageServiceImpl(jdbcTemplate, provider);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void retry_resetsResumePositionAndClearsIdempotent() throws Exception {
        stubFindById(40L, ImportStatusEnum.PARTIAL_SUCCESS, "biz-key");
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        assertTrue(service.retry(40L, "op"));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any());
        assertTrue(sqlCaptor.getValue().contains("resume_position = 0"));
        assertTrue(sqlCaptor.getValue().contains("execute_process = 0"));
        verify(idempotentImportManager).clear("biz-key");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void retry_skipsClearWhenBusinessUniqueKeyBlank() throws Exception {
        stubFindById(41L, ImportStatusEnum.FAIL, "");
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(1);

        assertTrue(service.retry(41L, "op"));
        verify(idempotentImportManager, never()).clear(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void retry_returnsFalseWhenRecordMissing() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(42L)))
            .thenReturn(Collections.emptyList());

        assertFalse(service.retry(42L, "op"));
        verify(jdbcTemplate, never()).update(contains("resume_position"), any(), any(), any(), any(), any(),
            any(), any(), any());
        verify(idempotentImportManager, never()).clear(anyString());
    }

    @SuppressWarnings("unchecked")
    private void stubFindById(Long id, ImportStatusEnum status, String businessUniqueKey) throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(id)))
            .thenAnswer(invocation -> {
                RowMapper<?> mapper = invocation.getArgument(1);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("id")).thenReturn(id);
                when(rs.getString("app_id")).thenReturn("app");
                when(rs.getString("import_code")).thenReturn("code");
                when(rs.getString("import_status")).thenReturn(status.getCode());
                when(rs.getString("source_file_url")).thenReturn("");
                when(rs.getString("source_file_name")).thenReturn("");
                when(rs.getString("file_type")).thenReturn("");
                when(rs.getString("business_unique_key")).thenReturn(businessUniqueKey);
                when(rs.getString("result_file_url")).thenReturn("");
                when(rs.getLong("total_count")).thenReturn(0L);
                when(rs.getLong("success_count")).thenReturn(0L);
                when(rs.getLong("fail_count")).thenReturn(0L);
                when(rs.getInt("execute_process")).thenReturn(50);
                when(rs.getLong("resume_position")).thenReturn(100L);
                when(rs.getInt("notify_enable_status")).thenReturn(0);
                when(rs.getString("notify_email")).thenReturn("");
                when(rs.getInt("max_server_retry")).thenReturn(0);
                when(rs.getString("execute_param")).thenReturn("{}");
                when(rs.getString("error_msg")).thenReturn("");
                when(rs.getString("create_by")).thenReturn("op");
                when(rs.getString("locale")).thenReturn("zh");
                return Collections.singletonList(mapper.mapRow(rs, 0));
            });
    }
}
