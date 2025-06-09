package com.openquartz.easyfile.example.model;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.openquartz.easyfile.common.annotations.ExcelProperty;

/**
 * @author svnee
 **/
@Table(name = "test_student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ExcelProperty("ID")
    @com.alibaba.excel.annotation.ExcelProperty("ID")
    private Long id;

    @ExcelProperty(value = "${student.name}", width = 8 * 512)
    @com.alibaba.excel.annotation.ExcelProperty("姓名")
    private String name;

    @ExcelProperty(value = "${student.age}")
    @com.alibaba.excel.annotation.ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty(value = "${student.school}", width = 8 * 1024)
    @com.alibaba.excel.annotation.ExcelProperty("学校")
    private String school;

    private Long schoolId;

    @ExcelProperty(value = "${student.address}", width = 8 * 1024)
    @com.alibaba.excel.annotation.ExcelProperty("地址")
    private String address;

    @ExcelProperty("${student.facture}")
    //    @com.alibaba.excel.annotation.ExcelProperty("特征")
    private List<Feature> featureList;

    //新地址信息
    @ExcelProperty("${student.addr}")
    //    @com.alibaba.excel.annotation.ExcelProperty("")
    private Address addr;

    // 學校等級
    @ExcelProperty("${student.schoolGrade}")
    @com.alibaba.excel.annotation.ExcelProperty("学校等级")
    private String schoolGrade;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Feature> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<Feature> featureList) {
        this.featureList = featureList;
    }

    public Address getAddr() {
        return addr;
    }

    public void setAddr(Address addr) {
        this.addr = addr;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolGrade() {
        return schoolGrade;
    }

    public void setSchoolGrade(String schoolGrade) {
        this.schoolGrade = schoolGrade;
    }
}
