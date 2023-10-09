package com.mybilibili.api;

import com.mybilibili.api.support.UserSupport;
import com.mybilibili.domain.JsonResponse;
import com.mybilibili.domain.auth.UserAuthorities;
import com.mybilibili.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthApi {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;
    //获取用户权限
    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        //获取用户权限
        Long userId = userSupport.getCurrentUserId();
        //获取权限
        UserAuthorities userAuthorities =  userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }

}
