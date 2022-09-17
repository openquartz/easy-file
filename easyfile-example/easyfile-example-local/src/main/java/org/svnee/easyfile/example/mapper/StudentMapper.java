package org.svnee.easyfile.example.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;
import org.svnee.easyfile.example.model.Student;

/**
 * @author svnee
 */
@Mapper
public interface StudentMapper {

    /**
     * 查询
     */
    @Select("select id,`name`,age,school_id,school,address from test_student limit #{limit} ")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(Student.class)
    Cursor<Student> scan(@Param("limit") int limit);

    /**
     * 查询
     */
    @Select("select id,`name`,age,school_id,school,address from test_student where school_id =#{schoolId} limit #{limit} ")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(Student.class)
    Cursor<Student> scanBySchool(@Param("limit") int limit, @Param("schoolId") Long schoolId);

    Student findById(@Param("id") Long id);

    List<Student> findByMinIdLimit(@Param("id") Long id, @Param("offset") Integer offset);

    List<Student> findByMinIdAndSchoolIdLimit(@Param("id") Long id, @Param("schoolId") Long schoolId,
        @Param("offset") Integer offset);

    int count();

    int countBySchool(Long schoolId);

    /**
     * all
     */
    List<Student> selectAll();

}
