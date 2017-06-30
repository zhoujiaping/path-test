package cn.zhou.path.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.zhou.path.model.PathNode;
import cn.zhou.path.service.PathNodeService;

@Controller
@RequestMapping("path-nodes")
public class PathNodeController {
    @Resource private PathNodeService pathNodeService;
    
    @RequestMapping(value="",method=RequestMethod.POST)
    @ResponseBody
    public int insert(long pid,PathNode node){
        return pathNodeService.insert(pid, node);
    }
}
