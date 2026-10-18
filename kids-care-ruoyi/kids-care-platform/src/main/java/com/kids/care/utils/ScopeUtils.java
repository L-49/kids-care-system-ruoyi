package com.kids.care.utils;

import com.kids.common.core.domain.model.LoginUser;
import com.kids.common.exception.ServiceException;
import com.kids.common.utils.SecurityUtils;

/**
 * 幼儿园数据范围工具。
 *
 * <p>若依自带的数据权限是按部门（部门树）过滤，无法表达"家长只能看自己孩子"这一维度，
 * 因此这里基于 sys_user.child_id 做一层业务数据范围控制，由各 Service 显式调用。</p>
 *
 * @author kids
 */
public class ScopeUtils
{
    /** 园长（若依超级管理员） */
    public static final String ROLE_ADMIN = "admin";

    /** 教师 */
    public static final String ROLE_TEACHER = "teacher";

    /** 家长 */
    public static final String ROLE_PARENT = "parent";

    private ScopeUtils()
    {
    }

    /**
     * 判断当前登录人是否拥有指定角色。
     *
     * <p>角色标识由 UserDetailsServiceImpl 在登录时写入 LoginUser.roles 并随令牌缓存在 Redis 中，
     * 与菜单权限（permissions）分开存放，避免权限串与角色标识混淆。</p>
     */
    public static boolean hasRole(String role)
    {
        LoginUser loginUser = currentLoginUser();
        return loginUser != null && loginUser.getRoles() != null && loginUser.getRoles().contains(role);
    }

    /**
     * 当前登录人是否为家长
     */
    public static boolean isParent()
    {
        return hasRole(ROLE_PARENT);
    }

    /**
     * 当前登录人是否为园长
     */
    public static boolean isAdmin()
    {
        return hasRole(ROLE_ADMIN);
    }

    /**
     * 当前登录人是否为教师
     */
    public static boolean isTeacher()
    {
        return hasRole(ROLE_TEACHER);
    }

    /**
     * 当前登录人用于数据过滤的幼儿ID。
     *
     * @return 家长返回其绑定的幼儿ID；园长、教师返回 null（表示不做幼儿维度过滤）
     */
    public static Long scopeChildId()
    {
        if (!isParent())
        {
            return null;
        }
        LoginUser loginUser = currentLoginUser();
        if (loginUser == null || loginUser.getUser() == null)
        {
            return null;
        }
        return loginUser.getUser().getChildId();
    }

    /**
     * 强制取当前家长的幼儿ID，未绑定时直接拒绝访问。
     *
     * <p>用于家长端接口：宁可报错，也不能因为 childId 为空而退化成"看到全部数据"。</p>
     */
    public static Long requireParentChildId()
    {
        Long childId = scopeChildId();
        if (childId == null)
        {
            throw new ServiceException("当前家长账号未绑定幼儿，请联系园方管理员处理");
        }
        return childId;
    }

    /**
     * 校验家长只能操作自己孩子的数据，越权直接抛 403 语义的业务异常。
     */
    public static void checkChildAccessible(Long childId)
    {
        Long scope = scopeChildId();
        if (scope != null && childId != null && !scope.equals(childId))
        {
            throw new ServiceException("无权查看其他幼儿的数据");
        }
    }

    /**
     * 游客/未登录场景下返回 username 供审计字段使用，避免 NPE。
     */
    public static String currentUsername()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "system";
        }
    }

    private static LoginUser currentLoginUser()
    {
        try
        {
            return SecurityUtils.getLoginUser();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
