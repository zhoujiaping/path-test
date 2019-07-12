import java.util.List;

public interface PathNode<T extends PathNode<T>>{
	
	Long getId();
	
	void setId(Long id);
	
	Long getParentId();
	
	void setParentId(Long parentId);
	
	T getParent();
	
    void setParent(T parent);
    
    String getPath();
    
    void setPath(String path);
    
    List<T> getChildren();
    
    void setChildren(List<T> children);
}
