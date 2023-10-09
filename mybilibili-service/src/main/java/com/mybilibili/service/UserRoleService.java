package com.mybilibili.service;

import com.mybilibili.dao.UserRoleDao;
import com.mybilibili.domain.auth.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleService {
    //获取用户角色
    @Autowired
    private UserRoleDao userRoleDao;
    //因为一个用户可以关联多个角色，所以是列表的形式
    public List<UserRole> getUserRoleByUserId(Long userId) {
        return userRoleDao.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRoleDao.addUserRole(userRole);
    }
}
