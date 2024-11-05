package com.openquartz.easyfile.common.i18n;

import com.openquartz.easyfile.common.util.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * LocaleContextHolder
 *
 * @author svnee
 */
public class LocaleContext {

    private LocaleContext() {
    }

    public static Locale currentLocale() throws IllegalStateException {
        return LocaleContextHolder.getLocale();
    }

    public static String currentLocaleLanguage() throws IllegalStateException {
        Locale locale = currentLocale();
        return Optional.of(locale).map(Locale::getLanguage).orElse(StringUtils.EMPTY);
    }

    public static void setCurrentLocale(@Nullable Locale locale) {

        if (Objects.nonNull(locale)) {
            LocaleContextHolder.setLocale(locale);
        }

    }

    public static void clear() {
        LocaleContextHolder.resetLocaleContext();
    }
}
