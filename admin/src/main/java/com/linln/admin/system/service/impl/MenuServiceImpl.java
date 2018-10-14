package com.linln.admin.system.service.impl;

import com.linln.admin.core.enums.StatusEnum;
import com.linln.admin.system.domain.Menu;
import com.linln.admin.system.repository.MenuRepository;
import com.linln.admin.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author 小懒虫
 * @date 2018/8/14
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 根据菜单ID查询菜单数据
     * @param id 菜单ID
     */
    @Override
    @Transactional
    public Menu getId(Long id) {
        return menuRepository.findByIdAndStatus(id, StatusEnum.OK.getCode());
    }

    /**
     * 根据菜单ID列表查询多个菜单数据
     * @param ids 菜单ID列表
     */
    @Override
    @Transactional
    public List<Menu> getIdList(List<Long> ids) {
        return menuRepository.findByIdInAndStatus(ids, StatusEnum.OK.getCode());
    }

    /**
     * 获取菜单列表数据
     * @param example 查询实例
     * @param sort 排序对象
     */
    @Override
    public List<Menu> getList(Example<Menu> example, Sort sort) {
        return menuRepository.findAll(example, sort);
    }

    /**
     * 获取菜单列表数据
     * @param sort 排序对象
     */
    @Override
    public List<Menu> getList(Sort sort) {
        return menuRepository.findAllByStatus(sort, StatusEnum.OK.getCode());
    }

    /**
     * 获取排序最大值
     * @param pid 父菜单ID
     */
    @Override
    public Integer getSortMax(Long pid){
        return menuRepository.findSortMax(pid);
    }

    /**
     * 保存菜单
     * @param menu 菜单实体类
     */
    @Override
    public Menu save(Menu menu){
        return menuRepository.save(menu);
    }

    /**
     * 状态(启用，冻结，删除)/批量状态处理
     */
    @Override
    @Transactional
    public Integer updateStatus(StatusEnum statusEnum, List<Long> idList){

        // 获取与之关联的所有菜单
        Set<Menu> treeMenuList = new HashSet<>();
        idList.forEach(id -> {
            Optional<Menu> menu = menuRepository.findById(id);
            List<Menu> list = menuRepository.findByPidsLikeAndStatus("%["+id+"]%", menu.get().getStatus());
            treeMenuList.add(menu.get());
            treeMenuList.addAll(list);
        });

        treeMenuList.forEach(menu -> {
            // 删除菜单状态时，同时更新角色的权限
            if(statusEnum == StatusEnum.DELETE){
                menu.getRoles().forEach(role -> {
                    role.getMenus().remove(menu);
                });
            }
            // 更新关联的所有菜单状态
            menu.setStatus(statusEnum.getCode());
        });

        return treeMenuList.size();
    }


}
