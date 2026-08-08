package com.openquartz.easyfile.common.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.util.JSONUtil;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.Test;

/**
 * Tests for ResponseResult parametric TypeReference (fixes type erasure).
 */
public class TypeReferenceTest {

    @Test
    public void responseResultTypeReferenceShouldCarryDataClass() {
        TypeReference<ResponseResult<Long>> ref = TypeReference.responseResult(Long.class);
        Type type = ref.getType();
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertEquals(ResponseResult.class, parameterizedType.getRawType());
        assertEquals(Long.class, parameterizedType.getActualTypeArguments()[0]);
    }

    @Test
    public void shouldDeserializeResponseResultDataAsLong() {
        String json = "{\"success\":true,\"code\":\"0\",\"message\":\"ok\",\"data\":123}";
        ResponseResult<Long> result = JSONUtil.parseObject(json, TypeReference.responseResult(Long.class));
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(123L, result.getData().longValue());
    }

    @Test
    public void shouldDeserializeResponseResultDataAsBoolean() {
        String json = "{\"success\":true,\"data\":true}";
        ResponseResult<Boolean> result = JSONUtil.parseObject(json, TypeReference.responseResult(Boolean.class));
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result.getData());
    }
}
