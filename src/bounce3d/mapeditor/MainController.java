package bounce3d.mapeditor;

import bounce3d.mapeditor.data.*;
import com.google.gson.Gson;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    private final Stage stage;

    @FXML
    private WebView web;

    @FXML
    private Label timeLabel, musicPathLabel;

    @FXML
    private Button newButton, openButton, saveButton, applyButton, playButton, pauseButton, stopButton, copyButton;

    @FXML
    private Slider timelineSlider;

    @FXML
    private TextField titleField, bpmField, skinField, mainCoverField, selectCoverField, musicPathField, musicIntroPathField, copyFrom, copyTo, copyDest;

    @FXML
    private GridPane obstacleToolGridPane;

    @FXML
    private AnchorPane gridAnchorPane;

    @FXML
    private GridPane obstacleGridPane;
    private Pane[][] obstaclePaneCell = new Pane[LevelObstacleData.MAP_HEIGHT][LevelObstacleData.MAP_WIDTH];

    private static final String levelMetaDataFileName = "metadata.json";
    private static final String levelObstacleFileName = "obstacle.json";
    private ObstacleToolType selectedObstacle;
    private LevelMetaData levelMetaData;
    private LevelObstacleData levelObstacleData;
    private int currentTick = 0;
    private Node prevPickNode;
    private MediaPlayer musicPlayer;
    private double tickDuration;
    private Duration totalDuration;
    private long currentMillis;
    private Gson gson = new Gson();

    public MainController(Stage stage) {
        this.stage = stage;
    }

    public void init() {

        timelineSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(musicPlayer == null || musicPlayer.getStatus() == MediaPlayer.Status.DISPOSED) {
                timelineSlider.setValue(0);
                return;
            }
            if (! timelineSlider.isValueChanging()) {
                double sliderTime = newVal.doubleValue() * this.tickDuration;
                if (Math.abs(currentMillis - sliderTime) > 500) {
                    seekByTick(newVal.intValue());
                }
            } else if(newVal.intValue() == 0 || newVal.intValue() == (int) timelineSlider.getMax()) {
                seekByTick(newVal.intValue());
            }
            setCurrentTick(newVal.intValue());
        });

        for (Node node : obstacleToolGridPane.getChildren()) {
            if(node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                button.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue) {
                        onClick(new Event(button, null, EventType.ROOT));
                    }
                });
            }
        }

        createNewMap();
    }

    private void seekByTick(int tick) {
        musicPlayer.seek(Duration.millis(tick * tickDuration));
    }

    private static String toFileURI(File file) {
        return "file:///" + file.getAbsolutePath().replace("\\", "/").replace(" ", "%20");
    }

    public void onKeyPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.UP) {
            if(timelineSlider.getValue() >= timelineSlider.getMax())
                return;
            double adjust = Math.round(timelineSlider.getValue()) + 1;
            seekByTick((int) adjust);
            event.consume();
        } else if(event.getCode() == KeyCode.DOWN) {
            if(timelineSlider.getValue() <= timelineSlider.getMin())
                return;
            double adjust = Math.round(timelineSlider.getValue()) - 1;
            seekByTick((int) adjust);
            event.consume();
        } else if(event.getCode() == KeyCode.LEFT) {
            if(timelineSlider.getValue() <= timelineSlider.getMin())
                return;
            double adjust = Math.max(Math.round(timelineSlider.getValue()) - 4, timelineSlider.getMin());
            seekByTick((int) adjust);
            event.consume();
        } else if(event.getCode() == KeyCode.RIGHT) {
            if(timelineSlider.getValue() >= timelineSlider.getMax())
                return;
            double adjust = Math.min(Math.round(timelineSlider.getValue()) + 4, timelineSlider.getMax());
            seekByTick((int) adjust);
            event.consume();
        }

        int i = -1;
        switch(event.getCode()) {
            case DIGIT1:
                i = 0;
                break;
            case DIGIT2:
                i = 1;
                break;
            case Q:
                i = 2;
                break;
            case W:
                i = 3;
                break;
            case A:
                i = 4;
                break;
            case S:
                i = 5;
                break;
            case Z:
                i = 6;
                break;
            case X:
                i = 7;
                break;
        }

        if(i >= 0)
            ((ToggleButton)obstacleToolGridPane.getChildren().get(i)).setSelected(true);
    }


    public void onClick(Event event) {
        Node source = (Node) event.getSource();

        boolean isObstacleTool = source.getStyleClass().contains("obstacle-button");
        if(source == newButton) {
            createNewMap();
        } else if(source == openButton) {
            openMap();
        } else if(source == saveButton) {
            saveMap();
        } else if(isObstacleTool) {
            String caseConvertRegex = "([a-z])([A-Z]+)";
            String replacement = "$1_$2";
            Pattern pattern = Pattern.compile("(.*)Button");
            String id = source.getId();
            Matcher matcher = pattern.matcher(id);
            if(matcher.matches()) {
                String obstacleTypeName = matcher.group(1).replaceAll(caseConvertRegex, replacement).toUpperCase();
                ObstacleToolType obstacleType = ObstacleToolType.valueOf(obstacleTypeName);
                selectObstacle(obstacleType);
            }
            ((ToggleButton) source).setSelected(true);
        } else if(source == applyButton) {
            levelMetaData.setTitle(titleField.getText());
            levelMetaData.setBpm(Integer.parseInt(bpmField.getText()));
            levelMetaData.setMainCover(mainCoverField.getText());
            levelMetaData.setSelectCover(selectCoverField.getText());
            levelMetaData.setMusicPath(musicPathField.getText());
            levelMetaData.setMusicIntroPath(musicIntroPathField.getText());
            levelMetaData.setSkin(skinField.getText());

            applyMusic(levelMetaData.getMusicPath());
        } else if(source == playButton) {
            if(musicPlayer != null)
                musicPlayer.play();
        } else if(source == pauseButton) {
            if(musicPlayer != null)
                musicPlayer.pause();
        } else if(source == stopButton) {
            if(musicPlayer != null)
                musicPlayer.pause();
        } else if(source == copyButton) {
            Integer from = Integer.parseInt(copyFrom.getText());
            Integer to = Integer.parseInt(copyTo.getText());
            Integer dest = Integer.parseInt(copyDest.getText());
            levelObstacleData.copyTick(from, to, dest);
            refreshGrid();
        }
    }

    private void applyMusic(String musicPath) {
        if(musicPlayer != null)
            musicPlayer.dispose();
        musicPlayer = new MediaPlayer(new Media(toFileURI(new File(musicPath))));
        musicPlayer.setOnReady(()->{
            this.musicPathLabel.setText(musicPath);
            this.totalDuration = musicPlayer.getTotalDuration();
            this.tickDuration = 60d / (levelMetaData.getBpm() * 4d) * 1000d;
            this.timelineSlider.setMin(0);
            this.timelineSlider.setMax(totalDuration.toMillis() / tickDuration);
            this.levelObstacleData.setMaxTick((int) this.timelineSlider.getMax());
            this.timelineSlider.setMinorTickCount(1);
            this.timelineSlider.setMajorTickUnit(16);
            setCurrentTick(0);
        });
        musicPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) ->{
            currentMillis = (long) newVal.toMillis();
            int tick = (int) Math.round(currentMillis / tickDuration);
            setCurrentTick(tick);
            if (!timelineSlider.isValueChanging()) {
                timelineSlider.setValue(tick);
            }
        });
    }


    private void selectObstacle(ObstacleToolType obstacleType) {
        selectedObstacle = obstacleType;
        prevPickNode = null;
    }

    private void saveMap() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            directoryChooser.setTitle("Save Map");
            File directory = directoryChooser.showDialog(stage);
            if (directory != null) {
                File metadataFile = new File(directory, levelMetaDataFileName);
                File obstacleFile = new File(directory, levelObstacleFileName);
                String metadataJson = gson.toJson(levelMetaData);
                String obstacleJson = gson.toJson(levelObstacleData);
                FileWriter writer = new FileWriter(metadataFile);
                writer.write(metadataJson);
                writer.flush();
                writer.close();
                writer = new FileWriter(obstacleFile);
                writer.write(obstacleJson);
                writer.flush();
                writer.close();
                new Alert(Alert.AlertType.CONFIRMATION, "Save finished.").show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private void openMap() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            directoryChooser.setTitle("Open Map");
            File directory = directoryChooser.showDialog(stage);
            if (directory != null) {
                File metadataFile = new File(directory, levelMetaDataFileName);
                File obstacleFile = new File(directory, levelObstacleFileName);

                String metadataJson = new String(Files.readAllBytes(Paths.get(metadataFile.toURI())));
                String obstacleJson = new String(Files.readAllBytes(Paths.get(obstacleFile.toURI())));
                this.levelMetaData = gson.fromJson(metadataJson, LevelMetaData.class);
                this.levelObstacleData = gson.fromJson(obstacleJson, LevelObstacleData.class);
                syncMetaDataToField();
                applyMusic(levelMetaData.getMusicPath());
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private void createNewMap() {
        obstacleGridPane.getChildren().clear();
        int i, j;
        for(i=0; i< LevelObstacleData.MAP_HEIGHT; i++) {
            for(j=0; j< LevelObstacleData.MAP_WIDTH; j++) {
                Pane pane = new Pane();
                pane.getStyleClass().add("obstacle-cell");
                obstaclePaneCell[i][j] = pane;
                pane.setId("obstacle-cell-" + i + "-" + j);
                pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
                pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
                pane.setUserData(new Pair<>(j, i));
                obstacleGridPane.add(pane, j, i);
                GridPane.setHgrow(pane, Priority.ALWAYS);
                GridPane.setVgrow(pane, Priority.ALWAYS);
            }
        }

        levelObstacleData = new LevelObstacleData();
        levelMetaData = new LevelMetaData();
        if(musicPlayer != null)
            musicPlayer.dispose();
        totalDuration = null;
        currentMillis = 0;
        syncMetaDataToField();
        setCurrentTick(0);
        refreshGrid();
    }

    private void syncMetaDataToField() {
        titleField.setText(levelMetaData.getTitle());
        bpmField.setText(String.valueOf(levelMetaData.getBpm()));
        mainCoverField.setText(levelMetaData.getMainCover());
        musicIntroPathField.setText(levelMetaData.getMusicIntroPath());
        musicPathField.setText(levelMetaData.getMusicPath());
        selectCoverField.setText(levelMetaData.getSelectCover());
        skinField.setText(levelMetaData.getSkin());
        musicPathLabel.setText(musicPathField.getText());
    }

    /**
     * 특정 위치에 장애물 배치
     * @param coord 배치할 위치 Pair<X, Y>
     * @param obstacleType 배치할 장애물 타입
     */
    private void fillObstacle(Pair<Integer, Integer> coord, ObstacleToolType obstacleType) {
        int x = coord.getKey();
        int y = coord.getValue();

        List<AbstractObstacle> pickList = levelObstacleData.pickObstacleList(currentTick, coord);
        switch(obstacleType) {
            case NONE:
                if(pickList.size() > 0) {
                    levelObstacleData.removeObstacle(currentTick, pickList);
                }
                break;
            case SIDE_DOWN:
            case SIDE_UP:
            case SIDE_SHORT:
                SideObstacle sideObstacle = new SideObstacle();
                if((x == 0 || x == LevelObstacleData.MAP_WIDTH - 1) && y > 0) {
                    if(x == 0)
                        sideObstacle.setSide(SideObstacle.SIDE_LEFT);
                    else
                        sideObstacle.setSide(SideObstacle.SIDE_RIGHT);
                    sideObstacle.setPlace(y - 1);
                } else if(x > 0 && x < LevelObstacleData.MAP_WIDTH - 1 && y == 0) {
                    sideObstacle.setSide(SideObstacle.SIDE_TOP);
                    sideObstacle.setPlace(x - 1);
                } else {
                    return;
                }

                if(obstacleType == ObstacleToolType.SIDE_DOWN)
                    sideObstacle.setSubtype(SideObstacle.SUBTYPE_DOWN);
                else if(obstacleType == ObstacleToolType.SIDE_UP)
                    sideObstacle.setSubtype(SideObstacle.SUBTYPE_UP);
                else if(obstacleType == ObstacleToolType.SIDE_SHORT)
                    sideObstacle.setSubtype(SideObstacle.SUBTYPE_SHORT);

                // FILL하는 지점에 같은 장애물이 있으면 무시
                if(!pickList.contains(sideObstacle))
                    levelObstacleData.addObstacle(currentTick, sideObstacle);
                break;
            case FLOOR_NORMAL:
            case FLOOR_SMALL:
                FloorObstacle floorObstacle = new FloorObstacle();
                if(x > 0 && x < LevelObstacleData.MAP_WIDTH - 1
                        && y > 0) {
                    floorObstacle.setX(x - 1);
                    floorObstacle.setY(y - 1);
                } else {
                    return;
                }

                if(obstacleType == ObstacleToolType.FLOOR_NORMAL)
                    floorObstacle.setSize(FloorObstacle.SIZE_NORMAL);
                else if(obstacleType == ObstacleToolType.FLOOR_SMALL)
                    floorObstacle.setSize(FloorObstacle.SIZE_SMALL);

                // FILL하는 지점에 같은 장애물이 있으면 무시
                if(!pickList.contains(floorObstacle))
                    levelObstacleData.addObstacle(currentTick, floorObstacle);
                break;
            case FALL_NORMAL:
            case FALL_BIG:
                FallObstacle fallObstacle = new FallObstacle();
                if(x > 0 && x < LevelObstacleData.MAP_WIDTH - 1
                        && y > 0) {
                    fallObstacle.setX(x - 1);
                    fallObstacle.setY(y - 1);
                } else {
                    return;
                }

                if(obstacleType == ObstacleToolType.FALL_NORMAL)
                    fallObstacle.setSize(FallObstacle.SIZE_NORMAL);
                else if(obstacleType == ObstacleToolType.FALL_BIG)
                    fallObstacle.setSize(FallObstacle.SIZE_BIG);

                // FILL하는 지점에 같은 장애물이 있으면 무시
                if(!pickList.contains(fallObstacle))
                    levelObstacleData.addObstacle(currentTick, fallObstacle);
                break;
        }
    }

    public void onMousePressed(MouseEvent event) {
        onMouseDragged(event);
    }

    public void onMouseDragged(MouseEvent event) {
        Node pickNode = event.getPickResult().getIntersectedNode();
        if(prevPickNode == pickNode || selectedObstacle == null)
            return;

        Pair<Integer, Integer> coord = (Pair<Integer, Integer>) pickNode.getUserData();
        if(coord == null)
            return;

        fillObstacle(coord, selectedObstacle);
        prevPickNode = pickNode;
        refreshGrid();
    }

    public void onMouseReleased(MouseEvent event) {

    }

    private void refreshGrid() {
        int x, y;
        for(y = 0; y < LevelObstacleData.MAP_HEIGHT; y++) {
            for(x = 0; x < LevelObstacleData.MAP_WIDTH; x++) {
                _refreshGrid(x, y);
            }
        }
    }

    private void _refreshGrid(int x, int y) {
        if(x >= LevelObstacleData.MAP_WIDTH || y >= LevelObstacleData.MAP_HEIGHT)
            return;
        Pane pane = obstaclePaneCell[y][x];
        List<AbstractObstacle> obstacleList = levelObstacleData.pickObstacleList(currentTick, new Pair<Integer, Integer>(x, y));
        int mixedColor = 0;
        for (AbstractObstacle abstractObstacle : obstacleList) {
            int color = getObstacleColor(abstractObstacle);
            mixedColor = blend(mixedColor, color, color == 0 ? 0 : 0.5f);
        }
        Color color =  intToColor(mixedColor);
        pane.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private int getObstacleColor(AbstractObstacle abstractObstacle) {
        if(abstractObstacle instanceof SideObstacle) {
            SideObstacle sideObstacle = (SideObstacle) abstractObstacle;
            switch(sideObstacle.getSubtype()) {
                case SideObstacle.SUBTYPE_UP:
                    return ObstacleToolType.SIDE_UP.getColor();
                case SideObstacle.SUBTYPE_DOWN:
                    return ObstacleToolType.SIDE_DOWN.getColor();
                case SideObstacle.SUBTYPE_SHORT:
                    return ObstacleToolType.SIDE_SHORT.getColor();
            }
        } else if(abstractObstacle instanceof FloorObstacle) {
            FloorObstacle floorObstacle = (FloorObstacle) abstractObstacle;
            switch(floorObstacle.getSize()) {
                case FloorObstacle.SIZE_SMALL:
                    return ObstacleToolType.FLOOR_SMALL.getColor();
                case FloorObstacle.SIZE_NORMAL:
                    return ObstacleToolType.FLOOR_NORMAL.getColor();
            }
        } else if(abstractObstacle instanceof FallObstacle) {
            FallObstacle fallObstacle = (FallObstacle) abstractObstacle;
            switch(fallObstacle.getSize()) {
                case FallObstacle.SIZE_BIG:
                    return ObstacleToolType.FALL_BIG.getColor();
                case FallObstacle.SIZE_NORMAL:
                    return ObstacleToolType.FALL_NORMAL.getColor();
            }
        }

        return 0;
    }

    private void setCurrentTick(int tick) {
        this.currentTick = tick;
        if(musicPlayer == null || musicPlayer.getStatus() == MediaPlayer.Status.DISPOSED)
            return;
        double currentSecond = this.currentTick * this.tickDuration;
        double totalSecond = this.totalDuration.toMillis();
        this.timeLabel.setText(currentTick + " tick, " + millisToMinuteString((int) currentSecond) + " / " + millisToMinuteString((int) totalSecond));
        refreshGrid();
    }

    private String millisToMinuteString(int millis) {
        int minute = millis / 1000 / 60;
        int second = (millis % (1000 * 60)) / 1000;

        return String.format("%02d:%02d", minute, second);
    }

    public static Color intToColor(int color)
    {
        int b = (color)&0xFF;
        int g = (color>>8)&0xFF;
        int r = (color>>16)&0xFF;
        float a = (color>>24)&0xFF / 255;
        return Color.rgb(r, g, b, a);
    }

    int blend (int a, int b, float ratio) {
        if (ratio > 1f) {
            ratio = 1f;
        } else if (ratio < 0f) {
            ratio = 0f;
        }
        float iRatio = 1.0f - ratio;

        int aA = (a >> 24 & 0xff);
        int aR = ((a & 0xff0000) >> 16);
        int aG = ((a & 0xff00) >> 8);
        int aB = (a & 0xff);

        int bA = (b >> 24 & 0xff);
        int bR = ((b & 0xff0000) >> 16);
        int bG = ((b & 0xff00) >> 8);
        int bB = (b & 0xff);

        int A = (int)((aA * iRatio) + (bA * ratio));
        int R = (int)((aR * iRatio) + (bR * ratio));
        int G = (int)((aG * iRatio) + (bG * ratio));
        int B = (int)((aB * iRatio) + (bB * ratio));

        return A << 24 | R << 16 | G << 8 | B;
    }

}
