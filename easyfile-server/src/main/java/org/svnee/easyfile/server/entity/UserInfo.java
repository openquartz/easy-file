package org.svnee.easyfile.server.entity;

import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * User info.
 *
 * @author svnee
 */
@Data
@Table(name = "ef_user_info")
public class UserInfo {

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * role
     */
    private String role;

    /**
     * gmtCreate
     */
    private Date gmtCreate;

    /**
     * gmtModified
     */
    private Date gmtModified;

    /**
     * delFlag
     */
    private Long isDeleted;

}
