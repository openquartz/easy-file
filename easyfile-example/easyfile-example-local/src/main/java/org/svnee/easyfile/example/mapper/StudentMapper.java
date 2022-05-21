package org.svnee.easyfile.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;
import org.svnee.easyfile.example.model.Student;

public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 查询
     */
    @Select("select * from test_student limit #{limit} ")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(Student.class)
    Cursor<Student> scan(@Param("limit") int limit);

    Student findById(@Param("id") Long id);

    List<Student> findByMinIdLimit(@Param("id") Long id, @Param("offset") Integer offset);

    int count();
}
