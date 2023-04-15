package com.openquartz;

import java.util.HashMap;
import java.util.Map;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.openquartz.easyfile.common.response.ExportResult;

/**
 * @author svnee
 **/
public class SpelTest {

    public static void main(String[] args) {

        Map<String, Object> paramMap = new HashMap<>();

        ExportResult exportResult = new ExportResult();
        exportResult.setRegisterId(100L);
        paramMap.put("cache", exportResult);

        StandardEvaluationContext tarEvaluationContext = new StandardEvaluationContext();
        tarEvaluationContext.setVariables(paramMap);

        SpelExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("#cache.registerId");
        Object oriValue = expression.getValue(tarEvaluationContext);
        System.out.println(oriValue);
    }


}
