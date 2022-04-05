package org.svnee.easyfile.common.bean.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token info.
 *
 * @author svnee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    /**
     * accessToken
     */
    private String accessToken;

    /**
     * tokenTtl
     */
    private Long tokenTtl;

}
