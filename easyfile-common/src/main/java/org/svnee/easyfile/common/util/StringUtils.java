package org.svnee.easyfile.common.util;

/**
 * StringUtils
 *
 * @author svnee
 **/
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * The empty String {@code ""}.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * 不为空
     *
     * @param cs char
     * @return 是否不为空
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 是否为空
     *
     * @param cs cs
     * @return 是否为空
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
