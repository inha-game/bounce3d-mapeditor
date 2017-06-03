package bounce3d.mapeditor.data;

/**
 * Created by bdh92123 on 2017-03-21.
 */
public class LevelMetaData {

    private int bpm = 120;
    private String title = "";
    private String musicPath = "";
    private String skin = "";
    private String selectCover = "";
    private String mainCover = "";
    private String musicIntroPath = "";

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSelectCover() {
        return selectCover;
    }

    public void setSelectCover(String selectCover) {
        this.selectCover = selectCover;
    }

    public String getMainCover() {
        return mainCover;
    }

    public void setMainCover(String mainCover) {
        this.mainCover = mainCover;
    }

    public void setMusicIntroPath(String musicIntroPath) {
        this.musicIntroPath = musicIntroPath;
    }

    public String getMusicIntroPath() {
        return musicIntroPath;
    }
}
