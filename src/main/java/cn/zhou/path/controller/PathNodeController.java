package cn.zhou.path.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
    public int insert(Long pid,PathNode node){
        return pathNodeService.insert(pid, node);
    }
    
    @RequestMapping(value="{id}",method=RequestMethod.GET)
    @ResponseBody
    public PathNode queryTree(@PathVariable Long id){
        return pathNodeService.queryTree(id);
    }
    @RequestMapping(value="",method=RequestMethod.GET)
    @ResponseBody
    public PathNode queryRootTree(){
        return pathNodeService.queryTree(null);
    }
    
    @RequestMapping(value="{id}",method=RequestMethod.DELETE)
    @ResponseBody
    public int delete(@PathVariable Long id){
        return pathNodeService.delete(id);
    }
    @RequestMapping(value="{id}",method=RequestMethod.PUT)
    @ResponseBody
    public int update(@PathVariable Long id,PathNode node){
        return pathNodeService.update(node);
    }
}
