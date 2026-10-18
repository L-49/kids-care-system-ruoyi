package com.kids.care.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.kids.care.domain.MenuPlan;

/**
 * MenuPlan Mapper 接口
 *
 * @author kids
 * @date 2026-10-18
 */
public interface MenuPlanMapper
{
    MenuPlan selectMenuPlanById(Long id);

    List<MenuPlan> selectMenuPlanList(MenuPlan menuPlan);

    int insertMenuPlan(MenuPlan menuPlan);

    int updateMenuPlan(MenuPlan menuPlan);

    int deleteMenuPlanById(Long id);

    int deleteMenuPlanByIds(Long[] ids);

    /** 周视图：按日期区间查询 */
    List<MenuPlan> selectMenuWeek(@Param("start") Date start, @Param("end") Date end);

    /** 查已软删的同日同餐次记录ID */
    Long selectDeletedId(@Param("menuDate") Date menuDate, @Param("mealType") String mealType);

    /** 统计未删除的同日同餐次记录数 */
    int countByDateAndMeal(@Param("menuDate") Date menuDate, @Param("mealType") String mealType);

    /** 复活软删记录 */
    int reviveMenuPlan(@Param("id") Long id, @Param("dishes") String dishes,
                       @Param("note") String note, @Param("publisher") String publisher);
}
