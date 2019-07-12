import java.util.List;

public class Industry implements PathNode<Industry> {
	private Long id;
	private Long parentId;
	private Industry parent;
	private List<Industry> children;
	private String path;
	private String name;
	private String code;
	public Industry(){
		
	}
	public Industry(String name,String code){
		this.name= name;
		this.code = code;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public Industry getParent() {
		return parent;
	}
	public void setParent(Industry parent) {
		this.parent = parent;
	}
	public List<Industry> getChildren() {
		return children;
	}
	public void setChildren(List<Industry> children) {
		this.children = children;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
}
