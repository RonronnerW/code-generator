package com.wang.codegenerator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.codegenerator.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
* @author wlbin
* @description 针对表【t_generator(代码生成器)】的数据库操作Mapper
* @createDate 2024-04-02 15:40:24
* @Entity generator.entity.Generator
*/
public interface GeneratorMapper extends BaseMapper<Generator> {
    @Select("select id, dist_path from t_generator where is_delete = 1")
    List<Generator> listIsDelete();
}




