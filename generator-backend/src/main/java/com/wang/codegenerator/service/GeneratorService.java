package com.wang.codegenerator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.codegenerator.model.dto.generator.GeneratorQueryRequest;
import com.wang.codegenerator.model.entity.Generator;
import com.wang.codegenerator.model.vo.GeneratorVO;
import jakarta.servlet.http.HttpServletRequest;


/**
* @author wlbin
* @description 针对表【t_generator(代码生成器)】的数据库操作Service
* @createDate 2024-04-02 15:40:24
*/
public interface GeneratorService extends IService<Generator> {
    public void validGenerator(Generator generator, boolean add);
    public QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest);
    /**
     * 获取帖子封装
     *
     * @param generator
     * @param request
     * @return
     */
    GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param generatorPage
     * @param request
     * @return
     */
    Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request);
}
