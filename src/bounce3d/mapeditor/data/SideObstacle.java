package bounce3d.mapeditor.data;

import bounce3d.mapeditor.ObstacleType;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public class SideObstacle extends AbstractObstacle {
    public static final int SIDE_LEFT = 0;
    public static final int SIDE_RIGHT = 1;
    public static final int SIDE_TOP = 2;

    public static final int SUBTYPE_DOWN = 0;
    public static final int SUBTYPE_UP = 1;
    public static final int SUBTYPE_SHORT = 2;

    private int side = SIDE_LEFT;
    private int place = 0;
    private int subtype = SUBTYPE_DOWN;


    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    @Override
    public ObstacleType getType() {
        return ObstacleType.SIDE;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SideObstacle)) return false;
        SideObstacle obstacle = (SideObstacle) obj;
        return obstacle.place == place && obstacle.side == side && obstacle.subtype == subtype;
    }
}
