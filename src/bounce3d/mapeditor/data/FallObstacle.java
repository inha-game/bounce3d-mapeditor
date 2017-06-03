package bounce3d.mapeditor.data;

import bounce3d.mapeditor.ObstacleType;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public class FallObstacle extends AbstractObstacle {
    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_BIG = 1;

    private int x = 0;
    private int y = 0;
    private int size = SIZE_NORMAL;

    @Override
    public ObstacleType getType() {
        return ObstacleType.FALL;
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
        if(!(obj instanceof FallObstacle)) return false;
        FallObstacle obstacle = (FallObstacle) obj;
        return obstacle.size == size && obstacle.x == x && obstacle.y == y;
    }
}
