package com.mybilibili.service;

import com.mybilibili.dao.FollowingGroupDao;
import com.mybilibili.dao.UserFollowDao;
import com.mybilibili.domain.FollowingGroup;
import com.mybilibili.domain.User;
import com.mybilibili.domain.UserFollowing;
import com.mybilibili.domain.UserInfo;
import com.mybilibili.domain.constant.UserConstant;
import com.mybilibili.domain.exception.ConditionException;
import com.mybilibili.service.FollowingGroupService;
import com.mybilibili.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private UserFollowDao userFollowDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    //添加用户关注
    //事务管理，防止只成功一半
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing){
        //获取关注分组id
        Long groupId = userFollowing.getGroupId();
        //如果为空设置为默认
        if(groupId == null){
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setUserId(followingGroup.getId());
        }else {
            //id非空获取对应分组
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if(followingGroup == null){
                throw new ConditionException("关注分组不存在！");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        //添加的具体逻辑
        //获取被关注的用户
        User user = userService.getUserById(followingId);
        if(user == null){
            throw new ConditionException("用户不存在");
        }
        //删除原来的双方关系，再新建关系从而实现关系的更新而不用多写一个方法
        userFollowDao.deleteUserFollowing(userFollowing.getUserId(),followingId);
        userFollowing.setCreateTime(new Date());
        userFollowDao.addUserFollowing(userFollowing);
    }

    //获取用户关注列表

    //第一步:获取关注的用户列表
    //第二步:根据关注用户的id查询关注用户的基本信息
    // 第三步:将关注用户按关注分组进行分类,方便前端展示
    public List<FollowingGroup> getUserFollowings(Long userId){
        //获取列表
        List<UserFollowing> list =userFollowDao.getUserFollowings(userId);
        //根据lambda抽取属性
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        //创建用户信息列表并获取内容
        List<UserInfo> userInfoList = new ArrayList<>();
        //获取关注对象的信息
        //大于0说明有信息
        if(followingIdSet.size() >0){
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        //比较，如果相同则添加关注信息
        for(UserFollowing userFollowing : list){
            for (UserInfo userInfo : userInfoList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }
        //用户关注分组
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        //获取所有关注对象的分组
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingInfList(userInfoList);
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);
        for(FollowingGroup group : groupList){
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing : list){
                if(group.getId().equals(userFollowing.getGroupId())){
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingInfList(infoList);
            result.add(group);
        }
        return result;
    }


//    第一步:获取当前用户的粉丝列表
//    第二步:根据粉丝的用户id查询基本信息
//    第三步:篮询当前用户是否已经关注该粉丝
    //获取用户粉丝列表
    public List<UserFollowing> getUserFans(Long userId){
        List<UserFollowing> fanList = userFollowDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        if(fanIdSet.size() > 0){
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        List<UserFollowing> followingList = userFollowDao.getUserFollowings(userId);
        for(UserFollowing fan : fanList){
            for(UserInfo userInfo : userInfoList){
                if(fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for(UserFollowing following : followingList){
                if(following.getFollowingId().equals(fan.getUserId())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    //添加用户的关注分组
    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
        List<UserFollowing> userFollowingList = userFollowDao.getUserFollowings(userId);
        for(UserInfo userInfo : userInfoList){
            userInfo.setFollowed(false);
            for(UserFollowing userFollowing : userFollowingList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
