package com.openquartz.easyfile.common.i18n;

import com.openquartz.easyfile.common.util.StringUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * LocaleContextHolder
 *
 * @author svnee
 */
public class LocaleContextHolder {

    private static final ThreadLocal<Locale> currentLocaleContext = new NamedThreadLocal<>("Current Locale");

    private LocaleContextHolder() {
    }

    public static Locale currentLocale() throws IllegalStateException {
        return currentLocaleContext.get();
    }

    public static String currentLocaleLanguage() throws IllegalStateException {
        Locale locale = currentLocaleContext.get();
        return Optional.ofNullable(locale).map(Locale::getLanguage).orElse(StringUtils.EMPTY);
    }

    public static void setCurrentLocale(@Nullable Locale locale) {

        if (Objects.nonNull(locale)) {
            currentLocaleContext.set(locale);
        }

    }

    public static void clear() {
        currentLocaleContext.remove();
    }
}
