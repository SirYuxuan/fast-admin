package cc.oofo.system.dept.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cc.oofo.framework.core.entity.BaseQuery;
import cc.oofo.framework.core.service.BaseService;
import cc.oofo.framework.exception.BizException;
import cc.oofo.system.dept.entity.SysDept;
import cc.oofo.system.dept.entity.dto.SysDeptDto;
import cc.oofo.system.dept.entity.dto.SysDeptSaveDto;

/**
 * 系统部门服务
 * 
 * @author Sir丶雨轩
 * @since 2025/11/14
 */
@Service
@Transactional
public class SysDeptService extends BaseService<SysDept> {

    /**
     * 获取部门列表（树形结构）
     * 
     * @param query 查询条件
     * @return 部门树列表
     */
    public List<SysDeptDto> list(BaseQuery<SysDept> query) {
        List<SysDept> departments = list(query.getQueryWrapper());
        return buildDeptTree(departments);
    }

    /**
     * 检查部门名称是否存在
     * 
     * @param id   部门ID（编辑时传入）
     * @param name 部门名称
     * @return 是否存在
     */
    public boolean nameExists(String id, String name) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getName, name);
        if (StringUtils.hasText(id)) {
            queryWrapper.ne(SysDept::getId, id);
        }
        return count(queryWrapper) > 0;
    }

    /**
     * 添加部门
     * 
     * @param deptSaveDto 部门保存DTO
     */
    public void add(SysDeptSaveDto deptSaveDto) {
        // 验证部门名称是否重复
        if (nameExists(null, deptSaveDto.getName())) {
            throw new BizException("部门名称已存在");
        }

        // 验证上级部门是否存在
        if (StringUtils.hasText(deptSaveDto.getPid())) {
            SysDept parentDept = getById(deptSaveDto.getPid());
            if (parentDept == null) {
                throw new BizException("上级部门不存在");
            }
        }

        SysDept dept = new SysDept();
        dept.setName(deptSaveDto.getName());
        dept.setPid(deptSaveDto.getPid());
        dept.setStatus(deptSaveDto.getStatus() != null ? deptSaveDto.getStatus() : true);
        dept.setRemark(deptSaveDto.getRemark());
        dept.setIsEnabled(deptSaveDto.getIsEnabled() != null ? deptSaveDto.getIsEnabled() : true);
        dept.setIsDeleted(false);

        save(dept);
    }

    /**
     * 更新部门
     * 
     * @param deptSaveDto 部门保存DTO
     */
    public void update(SysDeptSaveDto deptSaveDto) {
        SysDept existingDept = getById(deptSaveDto.getId());
        if (existingDept == null) {
            throw new BizException("部门不存在");
        }

        // 验证部门名称是否重复
        if (nameExists(deptSaveDto.getId(), deptSaveDto.getName())) {
            throw new BizException("部门名称已存在");
        }

        // 验证上级部门
        if (StringUtils.hasText(deptSaveDto.getPid())) {
            // 不能将自己设为上级部门
            if (deptSaveDto.getId().equals(deptSaveDto.getPid())) {
                throw new BizException("不能将自己设为上级部门");
            }

            SysDept parentDept = getById(deptSaveDto.getPid());
            if (parentDept == null) {
                throw new BizException("上级部门不存在");
            }
        }

        SysDept dept = new SysDept();
        dept.setId(deptSaveDto.getId());
        dept.setName(deptSaveDto.getName());
        dept.setPid(deptSaveDto.getPid());
        dept.setStatus(deptSaveDto.getStatus() != null ? deptSaveDto.getStatus() : existingDept.getStatus());
        dept.setRemark(deptSaveDto.getRemark());
        dept.setIsEnabled(
                deptSaveDto.getIsEnabled() != null ? deptSaveDto.getIsEnabled() : existingDept.getIsEnabled());

        updateById(dept);
    }

    /**
     * 删除部门
     * 
     * @param id 部门ID
     */
    public void del(String id) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }

        // 检查是否有子部门
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getPid, id);
        long childrenCount = count(queryWrapper);
        if (childrenCount > 0) {
            throw new BizException("存在子部门，无法删除");
        }

        // 逻辑删除
        removeById(id);
    }

    /**
     * 查询所有部门（用于下拉选择）
     * 
     * @return 部门列表
     */
    public List<SysDept> selectAll() {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getStatus, true)
                .eq(SysDept::getIsEnabled, true)
                .orderByAsc(SysDept::getName);
        return list(queryWrapper);
    }

    /**
     * 构建部门树
     * 
     * @param deptList 部门列表
     * @return 部门树
     */
    public List<SysDeptDto> buildDeptTree(List<SysDept> deptList) {
        if (deptList == null || deptList.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为DTO并按ID分组，便于查找
        Map<String, SysDeptDto> deptMap = deptList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toMap(SysDeptDto::getId, dto -> dto));

        // 构建树形结构
        List<SysDeptDto> rootDepts = new ArrayList<>();

        for (SysDeptDto dept : deptMap.values()) {
            if (StringUtils.hasText(dept.getPid())) {
                // 有父部门，添加到父部门的children中
                SysDeptDto parent = deptMap.get(dept.getPid());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dept);
                }
            } else {
                // 顶级部门
                rootDepts.add(dept);
            }
        }

        // 对每个层级进行排序
        sortDepts(rootDepts);
        return rootDepts;
    }

    /**
     * 将 SysDept 转换为 SysDeptDto
     * 
     * @param dept 部门实体
     * @return 部门DTO
     */
    private SysDeptDto convertToDto(SysDept dept) {
        if (dept == null) {
            return new SysDeptDto();
        }

        SysDeptDto dto = new SysDeptDto();
        BeanUtils.copyProperties(dept, dto);

        // 设置创建时间，让 Jackson 自动格式化
        dto.setCreateTime(dept.getCreatedAt());

        // 设置上级部门名称
        if (StringUtils.hasText(dept.getPid())) {
            SysDept parentDept = getById(dept.getPid());
            if (parentDept != null) {
                dto.setPname(parentDept.getName());
            }
        }

        return dto;
    }

    /**
     * 递归排序部门
     * 
     * @param depts 部门列表
     */
    private void sortDepts(List<SysDeptDto> depts) {
        if (depts == null || depts.isEmpty()) {
            return;
        }

        // 按名称排序
        depts.sort((a, b) -> {
            if (a.getName() == null && b.getName() == null) {
                return 0;
            }
            if (a.getName() == null) {
                return 1;
            }
            if (b.getName() == null) {
                return -1;
            }
            return a.getName().compareTo(b.getName());
        });

        // 递归排序子部门
        for (SysDeptDto dept : depts) {
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                sortDepts(dept.getChildren());
            }
        }
    }

}