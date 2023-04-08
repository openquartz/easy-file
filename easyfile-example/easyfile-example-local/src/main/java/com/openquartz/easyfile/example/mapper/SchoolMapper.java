package com.openquartz.easyfile.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.openquartz.easyfile.example.model.School;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SchoolMapper extends BaseMapper<School> {

    int insertSelective(School school);

    List<School> selectAll();

    School selectById(@Param("id") Long id);

    List<School> selectByIdList(@Param("idList") List<Long> idList);

}
