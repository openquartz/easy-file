package com.openquartz.easyfile.example.model;

import java.util.Objects;
import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class School {

    private Long id;

    private String name;

    private String grade;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        School school = (School) o;
        return Objects.equals(id, school.id) && Objects.equals(name, school.name) && Objects
            .equals(grade, school.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, grade);
    }
}
