package com.kids.care.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kids.care.domain.MenuPlan;
import com.kids.care.mapper.MenuPlanMapper;
import com.kids.care.service.IMenuPlanService;
import com.kids.common.exception.ServiceException;

/**
 * 每周食谱 Service 实现
 *
 * <p>menu_plan 上有 (menu_date, meal_type) 唯一键，且表是软删：
 * 同一天同一餐次删掉后再录会撞唯一键，因此新增与复制都先查软删记录并「复活」。</p>
 *
 * @author kids
 * @date 2026-10-18
 */
@Service
public class MenuPlanServiceImpl implements IMenuPlanService
{
    @Autowired
    private MenuPlanMapper menuPlanMapper;

    @Override
    public List<MenuPlan> selectMenuPlanList(MenuPlan menuPlan)
    {
        return menuPlanMapper.selectMenuPlanList(menuPlan);
    }

    @Override
    public MenuPlan selectMenuPlanById(Long id)
    {
        return menuPlanMapper.selectMenuPlanById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMenuPlan(MenuPlan menuPlan)
    {
        if (menuPlan.getMenuDate() == null || menuPlan.getMealType() == null)
        {
            throw new ServiceException("食谱日期与餐次不能为空");
        }
        if (menuPlanMapper.countByDateAndMeal(menuPlan.getMenuDate(), menuPlan.getMealType()) > 0)
        {
            throw new ServiceException("当天该餐次已存在食谱，请直接修改");
        }
        Long deletedId = menuPlanMapper.selectDeletedId(menuPlan.getMenuDate(), menuPlan.getMealType());
        if (deletedId != null)
        {
            return menuPlanMapper.reviveMenuPlan(deletedId, menuPlan.getDishes(),
                    menuPlan.getNote(), menuPlan.getPublisher());
        }
        return menuPlanMapper.insertMenuPlan(menuPlan);
    }

    @Override
    public int updateMenuPlan(MenuPlan menuPlan)
    {
        return menuPlanMapper.updateMenuPlan(menuPlan);
    }

    @Override
    public int deleteMenuPlanByIds(Long[] ids)
    {
        return menuPlanMapper.deleteMenuPlanByIds(ids);
    }

    @Override
    public List<MenuPlan> listWeek(String weekStart)
    {
        LocalDate start = parseWeekStart(weekStart);
        return menuPlanMapper.selectMenuWeek(Date.valueOf(start), Date.valueOf(start.plusDays(6)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int copyFromLastWeek(String weekStart)
    {
        LocalDate start = parseWeekStart(weekStart);
        LocalDate srcStart = start.minusDays(7);
        LocalDate srcEnd = start.minusDays(1);
        List<MenuPlan> source = menuPlanMapper.selectMenuWeek(Date.valueOf(srcStart), Date.valueOf(srcEnd));
        if (source == null || source.isEmpty())
        {
            throw new ServiceException(String.format("上一周（%s ~ %s）没有食谱，无法复制", srcStart, srcEnd));
        }

        int copied = 0;
        for (MenuPlan src : source)
        {
            // 菜单日期是 java.util.Date，按天平移需要先转成 LocalDate
            LocalDate srcDate = new java.sql.Date(src.getMenuDate().getTime()).toLocalDate();
            Date target = Date.valueOf(srcDate.plusDays(7));
            if (menuPlanMapper.countByDateAndMeal(target, src.getMealType()) > 0)
            {
                continue;
            }
            Long deletedId = menuPlanMapper.selectDeletedId(target, src.getMealType());
            if (deletedId != null)
            {
                menuPlanMapper.reviveMenuPlan(deletedId, src.getDishes(), src.getNote(), src.getPublisher());
            }
            else
            {
                MenuPlan plan = new MenuPlan();
                plan.setMenuDate(target);
                plan.setMealType(src.getMealType());
                plan.setDishes(src.getDishes());
                plan.setNote(src.getNote());
                plan.setPublisher(src.getPublisher());
                menuPlanMapper.insertMenuPlan(plan);
            }
            copied++;
        }
        return copied;
    }

    /** 接受 yyyy-MM-dd，非法或为空时直接报错，避免静默取当天 */
    private LocalDate parseWeekStart(String weekStart)
    {
        if (weekStart == null || weekStart.trim().isEmpty())
        {
            throw new ServiceException("缺少周起始日期");
        }
        try
        {
            return LocalDate.parse(weekStart.trim());
        }
        catch (Exception e)
        {
            throw new ServiceException("周起始日期格式应为 yyyy-MM-dd");
        }
    }
}
