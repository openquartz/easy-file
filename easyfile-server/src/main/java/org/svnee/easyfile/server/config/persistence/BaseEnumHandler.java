package org.svnee.easyfile.server.config.persistence;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.svnee.easyfile.common.dictionary.BaseEnum;

/**
 * BaseEnumHandler
 *
 * @author svnee
 **/
public class BaseEnumHandler<T extends Enum<?> & BaseEnum<?>> extends BaseTypeHandler<BaseEnum<?>> {

    private Class<T> type;
    private T[] enums;

    public BaseEnumHandler() {
    }

    public BaseEnumHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = this.type.getEnumConstants();
        if (this.enums == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, BaseEnum enumerable,
        JdbcType jdbcType) throws SQLException {
        if (jdbcType == null) {
            preparedStatement.setObject(i, enumerable.getCode());
        } else {
            preparedStatement.setObject(i, enumerable.getCode(), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public BaseEnum<?> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        Object code = resultSet.getObject(s);
        if (resultSet.wasNull()) {
            return null;
        }
        return getEnmByCode(code);
    }

    @Override
    public BaseEnum<?> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        Object code = resultSet.getObject(i);
        if (resultSet.wasNull()) {
            return null;
        }
        return getEnmByCode(code);
    }

    @Override
    public BaseEnum<?> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        Object code = callableStatement.getObject(i);
        if (callableStatement.wasNull()) {
            return null;
        }
        return getEnmByCode(code);
    }

    private T getEnmByCode(Object code) {
        if (code == null) {
            return null;
        }
        if (code instanceof Boolean) {
            if ((boolean) code) {
                code = 1;
            } else {
                code = 0;
            }
        }
        for (T e : enums) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}