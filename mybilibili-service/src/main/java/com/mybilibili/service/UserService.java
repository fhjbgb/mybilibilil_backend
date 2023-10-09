package com.mybilibili.service;


import com.alibaba.fastjson.JSONObject;
import com.mybilibili.dao.UserDao;
import com.mybilibili.domain.PageResult;
import com.mybilibili.domain.RefreshTokenDetail;
import com.mybilibili.domain.User;
import com.mybilibili.domain.UserInfo;
import com.mybilibili.domain.constant.UserConstant;
import com.mybilibili.domain.exception.ConditionException;
import com.mybilibili.service.util.MD5Util;
import com.mybilibili.service.util.RSAUtil;
import com.mybilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthService userAuthService;

    //用户注册方法
    public void addUser(User user) {
        //手机号判断
        String phone = user.getPhone();
        if(StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if(dbUser != null){
            throw new ConditionException("该手机号已注册");
        }
        //获取盐值,密码判断、加密，使用当前时间进行加盐
        Date now =new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);}
        catch (Exception e){
            throw new ConditionException("密码解密失败");
        }
        String md5password = MD5Util.sign(rawPassword,salt,"UTF-8");
        user.setSalt(salt);
        user.setPassword(md5password);
        user.setCreateTime(now);
        userDao.addUser(user);

        //添加完用户后再创建用户信息
        //添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
//        userInfo.setNick(UserConstant.DEFAULT_NICK +  + "-" + userDao.getUserNum);
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        //从constant类中获取默认值
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_UNKNOW);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
        //添加用户默认角色
        userAuthService.addUserDefaultRole(user.getId());
    }

    public User getUserByPhone(String phone){
        return userDao.getUserByPhone(phone);
    }

////    //用户登录方法
//    public String login(User user) throws Exception{
//        //手机号判断
//        String phone = user.getPhone();
//        if(StringUtils.isNullOrEmpty(phone)){
//            throw new ConditionException("手机号不能为空!");
//        }
//        User dbUser = this.getUserByPhone(phone);
//        if (dbUser == null){
//            throw new ConditionException("用户不存在");
//        }
//        String password = user.getPassword();
//        String rawPassword;
//        try{
//            rawPassword = RSAUtil.decrypt(password);}
//        catch (Exception e){
//            throw new ConditionException("密码解密失败");
//        }
//        String salt = dbUser.getSalt();
//        String md5password = MD5Util.sign(rawPassword,salt,"UTF-8");
//        if(!md5password.equals(dbUser.getPassword())){
//            throw new ConditionException("密码错误");
//        }
////    生成用户令牌
//        TokenUtil tokenUtil = new TokenUtil();
//        return  TokenUtil.generateToken(dbUser.getId());
//
//    }

    public String login(User user) throws Exception{
        //验证手机与邮箱
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionException("参数异常！");
        }
        //获取用户对象
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if(dbUser == null){
            throw new ConditionException("当前用户不存在！");
        }
        //获取密码并验证
        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误！");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }


    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoById(userId);
        user.setUserInfo(userInfo);
        return user;
    }


    public void updateUsers(User user) throws Exception{
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if(dbUser == null){
            throw new ConditionException("用户不存在！");
        }
        if(!StringUtils.isNullOrEmpty(user.getPassword())){
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }


    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        params.put("start",(no-1)*size);
        params.put("limit",size);
        Integer total = userDao.pageCountUserInfos(params);
        List <UserInfo> list = new ArrayList<>();
        if(total > 0){
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total,list);
    }

    public Map<String, Object> loginForDts(User user) throws Exception{
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if(dbUser == null){
            throw new ConditionException("当前用户不存在！");
        }
        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误！");
        }
        Long userId = dbUser.getId();
        //创建token
        String accessToken = TokenUtil.generateToken(userId);
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        //保存refresh token到数据库.先删除、再新建、再保存
        //删除
        userDao.deleteRefreshTokenByUserId(refreshToken,userId);
        userDao.deleteRefreshToken(refreshToken,userId);
        //添加新生成的token
        userDao.addRefreshToken(refreshToken, userId, new Date());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;

    }

    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if(refreshTokenDetail == null){
            //不存在的情况
            throw new ConditionException("555","token过期！");
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId);
    }

    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.batchGetUserInfoByUserIds(userIdList);
    }

    public String getRefreshTokenByUserId(Long userId) {
        return userDao.getRefreshTokenByUserId(userId);
    }
}
