package cn.zhou.path.vo;

import java.util.List;

public class Tree<T>{
    private T parent;
    private List<T> children;
    
    public T getParent() {
        return parent;
    }
    
    public void setParent(T parent) {
        this.parent = parent;
    }
    
    public List<T> getChildren() {
        return children;
    }
    
    public void setChildren(List<T> children) {
        this.children = children;
    }
}
