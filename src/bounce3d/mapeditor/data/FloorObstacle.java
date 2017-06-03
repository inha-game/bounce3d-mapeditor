package bounce3d.mapeditor.data;

import bounce3d.mapeditor.ObstacleType;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public class FloorObstacle extends AbstractObstacle {
    public static final int SIZE_NORMAL = 1;
    public static final int SIZE_SMALL = 0;

    private int x = 0;
    private int y = 0;
    private int size = SIZE_NORMAL;

    @Override
    public ObstacleType getType() {
        return ObstacleType.FLOOR;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FloorObstacle)) return false;
        FloorObstacle obstacle = (FloorObstacle) obj;
        return obstacle.size == size && obstacle.x == x && obstacle.y == y;
    }
}
