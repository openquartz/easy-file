package com.openquartz.easyfile.core.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Lombok builder must honor default resumePosition.
 */
public class ImportContextTest {

    @Test
    public void builderShouldDefaultResumePositionToZero() {
        ImportContext context = ImportContext.builder()
            .registerId(1L)
            .appId("app")
            .importCode("code")
            .build();
        assertNotNull(context.getResumePosition());
        assertEquals(Long.valueOf(0L), context.getResumePosition());
    }

    @Test
    public void builderShouldKeepExplicitResumePosition() {
        ImportContext context = ImportContext.builder()
            .registerId(1L)
            .resumePosition(50L)
            .build();
        assertEquals(Long.valueOf(50L), context.getResumePosition());
    }
}
