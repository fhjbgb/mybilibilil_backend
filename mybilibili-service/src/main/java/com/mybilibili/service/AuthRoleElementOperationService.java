package com.mybilibili.service;


import com.mybilibili.dao.AuthRoleElementOperationDao;
import com.mybilibili.domain.auth.AuthRoleElementOperation;
import com.mybilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleElementOperationService {

    @Autowired
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        System.out.println("测试set");
        if(roleIdSet.size() == 0){
            System.out.println("roleIdSet为空");
        }
        return  authRoleElementOperationDao.getRoleElementOperationsByRoleIds(roleIdSet);


    }


}
