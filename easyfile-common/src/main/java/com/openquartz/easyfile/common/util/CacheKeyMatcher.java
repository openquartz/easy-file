package com.openquartz.easyfile.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

/**
 * Match export cache keys with a restricted SpEL context (no type references / RCE).
 *
 * @author svnee
 */
@Slf4j
public final class CacheKeyMatcher {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private CacheKeyMatcher() {
    }

    /**
     * @param cacheKeyList SpEL expressions or variable names (e.g. age, #request.id)
     * @param originalMap stored export param map
     * @param targetMap current export param map
     * @return true if all keys match
     */
    public static boolean match(List<String> cacheKeyList,
                                Map<String, Object> originalMap,
                                Map<String, Object> targetMap) {
        if (CollectionUtils.isEmpty(cacheKeyList)) {
            return true;
        }
        Map<String, Object> ori = originalMap == null ? Collections.emptyMap() : originalMap;
        Map<String, Object> tar = targetMap == null ? Collections.emptyMap() : targetMap;
        if (Objects.equals(ori, tar)) {
            return true;
        }

        SimpleEvaluationContext oriContext = buildContext(ori);
        SimpleEvaluationContext tarContext = buildContext(tar);

        try {
            for (String cacheKey : cacheKeyList) {
                Expression expression = PARSER.parseExpression(normalizeExpression(cacheKey));
                Object oriValue = expression.getValue(oriContext);
                Object targetValue = expression.getValue(tarContext);
                if (!Objects.equals(oriValue, targetValue)) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("[CacheKeyMatcher#match] parse matchKey error! cacheKey:{},ori:{},tar:{}",
                cacheKeyList, ori, tar, ex);
            return false;
        }
    }

    /**
     * Bare names (age) become variables (#age). Expressions with #, ., or ( are left as-is.
     */
    private static String normalizeExpression(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        String trimmed = cacheKey.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (trimmed.startsWith("#") || trimmed.indexOf('.') >= 0 || trimmed.indexOf('(') >= 0) {
            return trimmed;
        }
        return "#" + trimmed;
    }

    private static SimpleEvaluationContext buildContext(Map<String, Object> variables) {
        SimpleEvaluationContext context = SimpleEvaluationContext
            .forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
            .build();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getKey() != null) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }
}
