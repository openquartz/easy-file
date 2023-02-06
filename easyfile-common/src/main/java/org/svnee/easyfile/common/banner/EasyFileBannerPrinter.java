package org.svnee.easyfile.common.banner;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.common.util.VersionUtils;

/**
 * EasyFile logo.
 *
 * @author svnee
 */
public class EasyFileBannerPrinter {

    private static final String EASY_FILE_BANNER = "\n" +
        "  ______                ______ _ _      \n"
        + " |  ____|              |  ____(_) |     \n"
        + " | |__   __ _ ___ _   _| |__   _| | ___ \n"
        + " |  __| / _` / __| | | |  __| | | |/ _ \\\n"
        + " | |___| (_| \\__ \\ |_| | |    | | |  __/\n"
        + " |______\\__,_|___/\\__, |_|    |_|_|\\___|\n"
        + "                   __/ |                \n"
        + "                  |___/                   ";

    /**
     * banner println
     */
    public void printBanner(PrintStream out) {
        String bannerText = buildBannerText();
        out.println(bannerText);
        out.println();
    }

    /**
     * banner println
     */
    public void printBanner(Logger logger) {
        String bannerText = buildBannerText();
        logger.info(bannerText);
    }

    /**
     * print banner
     *
     * @param logger log
     */
    public void print(Logger logger) {
        if (logger.isInfoEnabled()) {
            printBanner(logger);
            return;
        }
        printBanner(System.out);
    }

    private String buildBannerText() {
        return Constants.LINE_SEPARATOR
            + Constants.LINE_SEPARATOR
            + EASY_FILE_BANNER
            + Constants.LINE_SEPARATOR
            + " :: EasyFile :: (v" + VersionUtils.getVersion() + ")  @author svnee"
            + Constants.LINE_SEPARATOR;
    }

}
