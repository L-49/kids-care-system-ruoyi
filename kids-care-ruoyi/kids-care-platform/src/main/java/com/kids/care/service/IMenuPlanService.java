package com.kids.care.service;

import java.util.List;
import com.kids.care.domain.MenuPlan;

/**
 * 每周食谱 Service 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface IMenuPlanService
{
    List<MenuPlan> selectMenuPlanList(MenuPlan menuPlan);

    MenuPlan selectMenuPlanById(Long id);

    int insertMenuPlan(MenuPlan menuPlan);

    int updateMenuPlan(MenuPlan menuPlan);

    int deleteMenuPlanByIds(Long[] ids);

    /** 周视图：weekStart 为周一，格式 yyyy-MM-dd */
    List<MenuPlan> listWeek(String weekStart);

    /** 把上一周食谱复制到本周，返回复制条数 */
    int copyFromLastWeek(String weekStart);
}
