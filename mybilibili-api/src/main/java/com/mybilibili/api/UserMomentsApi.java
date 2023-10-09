package com.mybilibili.api;

import com.mybilibili.api.support.UserSupport;
import com.mybilibili.domain.JsonResponse;
import com.mybilibili.domain.UserMoment;
import com.mybilibili.domain.annotation.ApiLimitedRole;
import com.mybilibili.domain.annotation.DataLimited;
import com.mybilibili.domain.constant.AuthRoleConstant;
import com.mybilibili.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.sql.PreparedStatement;
import java.util.List;


@RestController
public class UserMomentsApi {


   @Autowired
    private UserMomentsService userMomentsService;

   @Autowired
    private UserSupport userSupport;

   //sop对用户的权限进行限制
   @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV1})
   //添加用户动态
   @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse <String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception{
        Long userId = userSupport.getCurrentUserId();

        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    //获取用户关注对象的信息
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }


}
