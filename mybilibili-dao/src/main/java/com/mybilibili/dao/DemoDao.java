package com.mybilibili.dao;

import org.apache.ibatis.annotations.Mapper;

import javax.annotation.ManagedBean;

@Mapper
public interface DemoDao {

    public Long query(Long id);
}
