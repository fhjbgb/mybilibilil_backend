package com.mybilibili.service;


import com.mybilibili.domain.auth.*;
import com.mybilibili.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    //获取用户关联的角色
    @Autowired
    private UserRoleService userRoleService;
    //获取角色相关联的权限
    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {
        //用户角色列表
//        List<UserRole> userRoleList = userRoleService.getUserRoleByUser(userId);
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        //获取角色Id
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole :: getRoleId).collect(Collectors.toSet());
        //查询按钮操作权限
        //页面访问权限和按钮权限的关联列表
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {
        //用户和权限的关联
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
