package org.svnee.easyfile.example.model;

import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.svnee.easyfile.common.annotations.ExcelProperty;

/**
 * @author svnee
 **/
@Table(name = "test_student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty(value = "姓名", width = 8 * 512)
    private String name;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty(value = "学校", width = 8 * 1024)
    private String school;

    private Long schoolId;

    @ExcelProperty(value = "地址", width = 8 * 1024)
    private String address;

    @ExcelProperty("特征")
    private List<Feature> featureList;

    @ExcelProperty("新地址信息")
    private Address addr;

    @ExcelProperty("學校等級")
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
