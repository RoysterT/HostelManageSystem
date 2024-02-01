package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Activity;
import com.hostelms.entity.dto.Identity;
import com.hostelms.mapper.IdentityMapper;
import com.hostelms.service.IdentityService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Resource
    IdentityService identityService;

    @Resource
    MessageHandle message;

    @Autowired
    IdentityMapper identityMapper;

    @GetMapping("/permission/all")
    public RestBean<Map<String, Object>> getIdenetity(@RequestParam("page") int pageNum,
                                                      @RequestParam("pageSize") int pageSize) {
        Page<Identity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Identity> queryWrapper = new QueryWrapper<>();
        identityMapper.selectPage(page, queryWrapper);
        List<Identity> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }

    @GetMapping("/permission/info/{identity}")
    public String getIdentity(@PathVariable("identity") int id){
        Identity identity = identityService.getIdentityById(id);
        return identity == null ?
                RestBean.failure(400, "用户组不存在").asJsonString() :
                RestBean.success(identity).asJsonString();
    }

    @GetMapping("/permission/delete")
    public RestBean<Void> deleteIdentity(@RequestParam("id") int id){
        return message.messageHandle(id,identityService::removeIdentityById);
    }

    @PostMapping("/permission/edit")
    public RestBean<Void> editIdentity(@RequestBody Identity identity){
        return message.messageHandle(identity,identityService::updateIdentityById);
    }

    @PostMapping("/permission/add")
    public RestBean<Void> addIdentity(@RequestBody Identity identity){
        return message.messageHandle(identity,identityService::addIdentity);
    }

    @GetMapping("/permission/array")
    public RestBean<Map<String, Object>[]> getIdentityArray(){
        try {
            List<Identity> identities = identityMapper.selectList(null);
            Map<String, Object>[] results = new Map[identities.size()];
            for (int i = 0; i < identities.size(); i++) {
                results[i] = Map.of(
                        "value", identities.get(i).getId(),
                        "label", identities.get(i).getName());
            }
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }
}
