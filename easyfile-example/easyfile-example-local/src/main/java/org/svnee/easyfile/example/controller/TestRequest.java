package org.svnee.easyfile.example.controller;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;

/**
 * @author svnee
 **/
@Data
@JsonTypeInfo(use = Id.CLASS)
public class TestRequest {

    private Long id;


    public TestRequest() {
    }

    public TestRequest(Long id) {
        this.id = id;
    }
}
