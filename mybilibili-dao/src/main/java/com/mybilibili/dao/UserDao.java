package com.mybilibili.dao;

import com.alibaba.fastjson.JSONObject;
import com.mybilibili.domain.RefreshTokenDetail;
import com.mybilibili.domain.User;
import com.mybilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserDao {

    User getUserByPhone(String phone);

    Integer addUser(User user);

    void addUserInfo(UserInfo userInfo);

    User getUserById(Long userId);

    Integer updateUsers(User user);

    UserInfo getUserInfoById(Long userId);

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

//    Integer pageCountUserInfos(JSONObject params);JSONObject底层由map实现
     Integer pageCountUserInfos(Map<String,Object> params);

    List<UserInfo> pageListUserInfos(Map<String,Object> params);

    User getUserByPhoneOrEmail(String phone, String email);

    void deleteRefreshToken(@Param("refreshToken") String refreshToken,
                            @Param("userId") Long userId);
    void deleteRefreshTokenByUserId(@Param("refreshToken") String refreshToken,
                                    @Param("userId") Long userId);

    void addRefreshToken(@Param("refreshToken") String refreshToken,
                         @Param("userId") Long userId,
                         @Param("createTime") Date createTime);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);

    String getRefreshTokenByUserId(Long userId);

    Integer deleteRefreshTokenByUserId(Long userId);


}
