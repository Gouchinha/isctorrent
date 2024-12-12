import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class ListStringVector implements Serializable {

    private List<String[]> list;

    public ListStringVector() {
        this.list = new ArrayList<String[]>();
    }

    public void add(String[] node) {
        list.add(node);
    }

    public List<String[]> getList() {
        return list;
    }

public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String[] array : list) {
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]\n");
    }
    return sb.toString();
}
}
