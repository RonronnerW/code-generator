package com.wang.codegenerator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.codegenerator.model.dto.user.UserQueryRequest;
import com.wang.codegenerator.model.vo.LoginUserVO;
import com.wang.codegenerator.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import com.wang.codegenerator.model.entity.User;
import java.util.List;

/**
* @author wlbin
* @description 针对表【t_user(用户)】的数据库操作Service
* @createDate 2024-04-02 15:40:24
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    com.wang.codegenerator.model.entity.User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    com.wang.codegenerator.model.entity.User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(com.wang.codegenerator.model.entity.User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(com.wang.codegenerator.model.entity.User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(com.wang.codegenerator.model.entity.User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<com.wang.codegenerator.model.entity.User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<com.wang.codegenerator.model.entity.User> getQueryWrapper(UserQueryRequest userQueryRequest);

}
