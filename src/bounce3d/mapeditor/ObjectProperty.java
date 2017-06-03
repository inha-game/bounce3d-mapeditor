package bounce3d.mapeditor;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public class ObjectProperty<T> {
    private String key;
    private T value;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
