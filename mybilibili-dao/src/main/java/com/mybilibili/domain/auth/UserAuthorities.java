package com.mybilibili.domain.auth;

import java.util.List;

//用户权限实体类
public class UserAuthorities {
    //存放对前端元素的控制类型，后续可按需求增加
    //前端元素
    List<AuthRoleElementOperation> roleElementOperationList;
    //前端按钮
    List<AuthRoleMenu> roleMenuList;

    public List<AuthRoleElementOperation> getRoleElementOperationList() {
        return roleElementOperationList;
    }

    public void setRoleElementOperationList(List<AuthRoleElementOperation> roleElementOperationList) {
        this.roleElementOperationList = roleElementOperationList;
    }

    public List<AuthRoleMenu> getRoleMenuList() {
        return roleMenuList;
    }

    public void setRoleMenuList(List<AuthRoleMenu> roleMenuList) {
        this.roleMenuList = roleMenuList;
    }
}
