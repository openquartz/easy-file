package com.openquartz.easyfile.storage.file.url;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * URL download scheme restriction and stream handling.
 */
public class UrlDownloadServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final UrlDownloadServiceImpl downloadService = new UrlDownloadServiceImpl();

    @Test
    public void shouldRejectFileScheme() throws Exception {
        File target = temporaryFolder.newFile("out.bin");
        try {
            downloadService.download("file:///etc/hosts", target);
            fail("expected RuntimeException wrapping IllegalArgumentException");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException
                || (ex.getMessage() != null && ex.getMessage().contains("http")));
        }
    }

    @Test
    public void shouldRejectFtpScheme() throws Exception {
        File target = temporaryFolder.newFile("out2.bin");
        try {
            downloadService.download("ftp://example.com/a.txt", target);
            fail("expected failure for non-http scheme");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException
                || (ex.getMessage() != null && ex.getMessage().contains("http")));
        }
    }
}
