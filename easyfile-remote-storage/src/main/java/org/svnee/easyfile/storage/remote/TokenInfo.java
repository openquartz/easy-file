package org.svnee.easyfile.storage.remote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token info.
 *
 * @author svnee
 * @date 2021/12/20 20:02
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
