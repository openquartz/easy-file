package com.openquartz.easyfile.common.util.json;

import com.openquartz.easyfile.common.bean.ResponseResult;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Json TypReference
 * @author svnee
 **/
public abstract class TypeReference<T> implements Comparable<TypeReference<T>> {

    protected final Type type;

    protected TypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        /* 22-Dec-2008, tatu: Not sure if this case is safe -- I suspect
         *   it is possible to make it fail?
         *   But let's deal with specific
         *   case when we know an actual use case, and thereby suitable
         *   workarounds for valid case(s) and/or error to throw
         *   on invalid one(s).
         */
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * Construct with an explicit {@link Type} (avoids generic type-variable erasure).
     */
    protected TypeReference(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        this.type = type;
    }

    public Type getType() { return type; }

    /**
     * Build {@code ResponseResult<D>} type reference with a concrete data class.
     */
    public static <D> TypeReference<ResponseResult<D>> responseResult(Class<D> dataClass) {
        if (dataClass == null) {
            throw new IllegalArgumentException("dataClass must not be null");
        }
        ParameterizedType parameterizedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{dataClass};
            }

            @Override
            public Type getRawType() {
                return ResponseResult.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return new TypeReference<ResponseResult<D>>(parameterizedType) {
        };
    }

    /**
     * The only reason we define this method (and require implementation
     * of <code>Comparable</code>) is to prevent constructing a
     * reference without type information.
     */
    @Override
    public int compareTo(TypeReference<T> o) { return 0; }
}

