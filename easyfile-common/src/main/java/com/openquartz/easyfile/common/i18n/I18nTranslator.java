package com.openquartz.easyfile.common.i18n;

import com.openquartz.easyfile.common.util.SpringContextUtil;
import com.openquartz.easyfile.common.util.StringUtils;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多语言翻译
 *
 * @author svnee
 */
public class I18nTranslator {

    private I18nTranslator(){
    }

    // 匹配 ${key} 格式的占位符
    private static final Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");

    /**
     * 翻译当前模版
     *
     * @param template 模版
     * @return 翻译结果
     */
    public static String translate(String template) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        Matcher matcher = pattern.matcher(template);
        if (!matcher.find()) {
            return template;
        }
        // i18n key
        String key = matcher.group(1);
        if (StringUtils.isBlank(key)) {
            return template;
        }

        // 获取当前语言
        Locale locale = LocaleContextHolder.currentLocale();

        MessageSource messageSource = SpringContextUtil.getBean(MessageSource.class);
        return messageSource.getMessage(key, null, locale);
    }

}
