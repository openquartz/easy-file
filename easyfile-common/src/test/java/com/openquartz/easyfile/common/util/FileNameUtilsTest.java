package com.openquartz.easyfile.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for zip filename detection used by compress path.
 */
public class FileNameUtilsTest {

    @Test
    public void shouldDetectZipWhenExtensionIsZip() {
        assertTrue(FileNameUtils.isZipCompress("report.zip"));
        assertTrue(FileNameUtils.isZipCompress("a.b.ZIP"));
        assertTrue(FileNameUtils.isZipCompress("archive.Zip"));
    }

    @Test
    public void shouldNotDetectZipForNonZipFiles() {
        assertFalse(FileNameUtils.isZipCompress("report.xlsx"));
        assertFalse(FileNameUtils.isZipCompress("report"));
        assertFalse(FileNameUtils.isZipCompress("zip"));
        assertFalse(FileNameUtils.isZipCompress("report.zip.bak"));
    }

    @Test
    public void shouldHandleNullAndBlank() {
        assertFalse(FileNameUtils.isZipCompress(null));
        assertFalse(FileNameUtils.isZipCompress(""));
        assertFalse(FileNameUtils.isZipCompress("   "));
    }
}
