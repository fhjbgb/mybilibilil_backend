package com.mybilibili.dao;


import com.mybilibili.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao {


    Integer addUserMoments(UserMoment userMoment);
}
