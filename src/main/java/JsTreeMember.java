import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JsTreeMember {
    private String id;
    private String name;
    private String title;
    private List<JsTreeMember> children = new LinkedList<>();
    private List<Double> position = Arrays.asList(-87.6297980, 41.8781140);

    public JsTreeMember(String id, String name, String title) {
        this.id = id;
        this.name = name;
        this.title = title;
    }

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<JsTreeMember> getChildren() {
        return children;
    }

    public void setChildren(LinkedList<JsTreeMember> children) {
        this.children = children;
    }

    public void addChild(final JsTreeMember child) {
        this.children.add(child);
    }
}
