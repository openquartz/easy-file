package org.svnee.easyfile.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.bean.auth.AuthUserRequest;
import org.svnee.easyfile.common.bean.auth.TokenInfo;
import org.svnee.easyfile.common.constants.Constants;
import org.svnee.easyfile.server.bean.constants.AuthConstants;
import org.svnee.easyfile.server.security.AuthManager;

/**
 * UserController
 *
 * @author svnee
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.BASE_PATH + "/auth/users")
public class UserController {

    private final AuthManager authManager;

    @PostMapping("/apply/token")
    public ResponseResult<TokenInfo> applyToken(@RequestBody AuthUserRequest userRequest) {
        String accessToken = authManager.resolveTokenFromUser(userRequest.getUsername(), userRequest.getPassword());
        TokenInfo tokenInfo = new TokenInfo(accessToken, AuthConstants.TOKEN_VALIDITY_IN_SECONDS);
        return ResponseResult.ok(tokenInfo);
    }

}
