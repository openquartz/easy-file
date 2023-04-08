package com.openquartz.easyfile.example.mapper;

import com.openquartz.easyfile.example.model.Student;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;

public interface StudentMapper {

    /**
     * 查询
     */
    @Select("select * from test_student limit #{limit} ")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(Student.class)
    Cursor<Student> scan(@Param("limit") int limit);
}
