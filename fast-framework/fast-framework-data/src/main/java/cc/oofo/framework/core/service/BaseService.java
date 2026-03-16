package cc.oofo.framework.core.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 基础服务类<br/>
 * 提供通用的增删改查
 * 
 * @author Sir丶雨轩
 * @since 2025-11-13
 */
public class BaseService<T> extends ServiceImpl<BaseMapper<T>, T> {

}