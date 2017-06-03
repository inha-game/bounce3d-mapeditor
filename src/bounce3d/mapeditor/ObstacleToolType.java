package bounce3d.mapeditor;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public enum ObstacleToolType {
    NONE(0), SIDE_UP(0xff29a784), SIDE_DOWN(0xff99b902), SIDE_SHORT(0xff1092aa), FLOOR_NORMAL(0xffa929a9), FLOOR_SMALL(0xff440900), FALL_NORMAL(0xff487723), FALL_BIG(0xff0220a9);

    int color;

    private ObstacleToolType(int color) {
        this.color = color;
    }
    public int getColor() {
        return color;
    }
}
