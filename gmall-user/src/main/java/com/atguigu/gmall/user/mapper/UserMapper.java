package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//继承tk.mybatis.mapper.common.Mapper  的Mapper<T>  不用再写实现方法
public interface UserMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUser();

}
