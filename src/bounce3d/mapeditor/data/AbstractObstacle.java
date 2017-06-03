package bounce3d.mapeditor.data;

import bounce3d.mapeditor.ObstacleType;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public abstract class AbstractObstacle implements Cloneable {
    public abstract ObstacleType getType();
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
