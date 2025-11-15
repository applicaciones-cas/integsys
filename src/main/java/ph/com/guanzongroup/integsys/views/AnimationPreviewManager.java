package ph.com.guanzongroup.integsys.views;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javafx.geometry.Pos;

public class AnimationPreviewManager {

    private AnchorPane animationPane;
    private StackPane containerPane;
    private String animationFilePath;
    private double baseWidth = 550;
    private double baseHeight = 550;
    private double durationSeconds = 3.0;
    private double fadeInDuration = 0.0;
    private boolean simultaneousFade = false;

    private final List<ParallelTransition> transitions = new ArrayList<>();
    private final List<ImageView> animatedImages = new ArrayList<>();
    private final List<FrameDataPair> framePairs = new ArrayList<>();
    String GLOBAL_PATH =System.getProperty("sys.default.path.config") + "/Images/Animation/";
    private String displayAlternativePath;

    // ----------------------  Set file path ----------------------
    public void setAnimationFilePath(String filePath) {
        this.animationFilePath = filePath;
    }

    // ---------------------- Identify container ----------------------
    public void setContainer(Node container) {
        if (container instanceof StackPane) {
            this.containerPane = (StackPane) container;
        } else if (container instanceof AnchorPane) {
            StackPane wrapper = new StackPane(container);
            this.containerPane = wrapper;
        } else {
            throw new IllegalArgumentException("Container must be StackPane or AnchorPane");
        }
    }

    // ---------------------- Create transparent animation pane ----------------------
    public void createAnimationPane(double width, double height) {
        animationPane = new AnchorPane();
        animationPane.setPrefSize(width, height);
        animationPane.setMaxSize(width, height);
        animationPane.setMinSize(width, height);
        animationPane.setStyle("-fx-background-color: transparent;");
    }

    // ---------------------- Load animation from TXT ----------------------
    public void loadAnimation() {
        if (animationFilePath == null) {
            showAlternativeImage();
            return;
        }

        if (!Files.exists(Paths.get(animationFilePath))) {
            showAlternativeImage();
            return;
        }

        transitions.clear();
        animatedImages.clear();
        framePairs.clear();
        animationPane.getChildren().clear();

        List<FrameData> frameDataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(animationFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("FADEIN=")) {
                    fadeInDuration = Double.parseDouble(line.split("=")[1]);
                    continue;
                }
                if (line.startsWith("SIMULTANEOUS_FADE=")) {
                    simultaneousFade = Boolean.parseBoolean(line.split("=")[1]);
                    continue;
                }
                if (line.startsWith("DURATION=")) {
                    durationSeconds = Double.parseDouble(line.split("=")[1]);
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length < 8) {
                    continue;
                }

                String type = parts[0];
                String file = parts[1];
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                double w = Double.parseDouble(parts[4]);
                double h = Double.parseDouble(parts[5]);
                double opacity = Double.parseDouble(parts[6]);
                int zIndex = Integer.parseInt(parts[7]);
                boolean excludeFade = parts.length > 8 && Boolean.parseBoolean(parts[8]);

                frameDataList.add(new FrameData(type, file, x, y, w, h, opacity, zIndex, excludeFade));
            }
        } catch (IOException e) {
            showAlternativeImage();
            return;
        }

        // Group frames by file + zIndex
        Map<String, FrameDataPair> imageMap = new LinkedHashMap<>();
        for (FrameData fd : frameDataList) {
            String key = fd.file + "_" + fd.zIndex;
            imageMap.putIfAbsent(key, new FrameDataPair());
            if ("FROM".equalsIgnoreCase(fd.type)) {
                imageMap.get(key).from = fd;
            } else {
                imageMap.get(key).to = fd;
            }
        }
        List<FadeTransition> fadeIns = new ArrayList<>();

        boolean allExist = imageMap.values().stream()
                .sorted(Comparator.comparingInt(p
                        -> p.from != null ? p.from.zIndex : (p.to != null ? p.to.zIndex : 0)))
                .allMatch(pair -> {
                    String filePath = pair.from.file;
                    String normalizedPath = filePath.replaceFirst("^file:/+", "");
                    String fileName = Paths.get(normalizedPath).getFileName().toString();
                    return Files.exists(Paths.get(GLOBAL_PATH + fileName));
                });
        if (allExist) {
            imageMap.values().stream()
                    .sorted(Comparator.comparingInt(p -> p.from != null ? p.from.zIndex : (p.to != null ? p.to.zIndex : 0)))
                    .forEach(pair -> {
                        if (pair.from == null || pair.to == null) {
                            return;
                        }
                        String filePath = pair.from.file;
                        String normalizedPath = filePath.replaceFirst("^file:/+", "");
                        String fileName = Paths.get(normalizedPath).getFileName().toString();
                        String newPath = "file:/" + GLOBAL_PATH + fileName;

                        framePairs.add(pair);
                        ImageView iv = new ImageView(new Image(newPath));
                        iv.setFitWidth(pair.from.width);
                        iv.setFitHeight(pair.from.height);

                        // Set initial opacity
                        if (fadeInDuration > 0 && !pair.from.excludeFade) {
                            iv.setOpacity(0.0);
                        } else {
                            iv.setOpacity(pair.from.opacity);
                        }

                        // Center using from center
                        double fromCenterX = pair.from.x + pair.from.width / 2.0;
                        double fromCenterY = pair.from.y + pair.from.height / 2.0;
                        iv.setLayoutX(fromCenterX - iv.getFitWidth() / 2.0);
                        iv.setLayoutY(fromCenterY - iv.getFitHeight() / 2.0);

                        animatedImages.add(iv);
                        animationPane.getChildren().add(iv);

                        // ---------------------- Transitions ----------------------
                        TranslateTransition translate = new TranslateTransition(Duration.seconds(durationSeconds), iv);
                        double toCenterX = pair.to.x + pair.to.width / 2.0;
                        double toCenterY = pair.to.y + pair.to.height / 2.0;
                        translate.setToX(toCenterX - fromCenterX);
                        translate.setToY(toCenterY - fromCenterY);

                        ScaleTransition scale = new ScaleTransition(Duration.seconds(durationSeconds), iv);
                        scale.setToX(pair.to.width / pair.from.width);
                        scale.setToY(pair.to.height / pair.from.height);

                        FadeTransition ft = new FadeTransition(Duration.seconds(fadeInDuration), iv);
                        boolean skipFade = pair.from.excludeFade || fadeInDuration == 0;
                        ft.setFromValue(skipFade ? pair.from.opacity : 0);
                        ft.setToValue(pair.from.opacity);
                        fadeIns.add(ft);

                        ParallelTransition fadeInAll = new ParallelTransition();
                        fadeInAll.getChildren().addAll(fadeIns);

                        if (simultaneousFade || fadeInDuration == 0) {
                            FadeTransition fade = null;
                            if (!pair.from.excludeFade) {
                                fade = new FadeTransition(Duration.seconds(fadeInDuration), iv);
                                fade.setFromValue(iv.getOpacity());
                                fade.setToValue(pair.to.opacity);
                            } else {
                                iv.setOpacity(pair.to.opacity);
                            }

                            ParallelTransition pt = fade != null ? new ParallelTransition(translate, scale, fade)
                                    : new ParallelTransition(translate, scale);
                            pt.play();
                        } else {
                            fadeInAll.setOnFinished(e -> {
                                FadeTransition fade = null;
                                if (!pair.from.excludeFade) {
                                    fade = new FadeTransition(Duration.seconds(fadeInDuration), iv);
                                    fade.setFromValue(iv.getOpacity());
                                    fade.setToValue(pair.to.opacity);
                                } else {
                                    iv.setOpacity(pair.to.opacity);
                                }

                                ParallelTransition pt = fade != null ? new ParallelTransition(translate, scale, fade)
                                        : new ParallelTransition(translate, scale);
                                pt.play();
                            });
                            fadeInAll.play();
                        }
                    });
        } else {
            showAlternativeImage();
        }

    }

    // ---------------------- Display FROM static ----------------------
    public void loadFromWindow() {
        displayStaticFrame(true);
    }

    // ---------------------- Display TO static ----------------------
    public void loadEndWindow() {
        displayStaticFrame(false);
    }

    private void displayStaticFrame(boolean isFrom) {
        if (animationFilePath == null || !Files.exists(Paths.get(animationFilePath))) {
            showAlternativeImage();
            return;
        }

        animationPane.getChildren().clear();

        class ImageLayer {

            final ImageView imageView;
            final int zIndex;

            ImageLayer(ImageView iv, int z) {
                this.imageView = iv;
                this.zIndex = z;
            }
        }

        List<ImageLayer> layers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(animationFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.startsWith("FROM|") && !line.startsWith("TO|")) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length < 8) {
                    continue;
                }

                String type = parts[0];
                if ((isFrom && !type.equalsIgnoreCase("FROM"))
                        || (!isFrom && !type.equalsIgnoreCase("TO"))) {
                    continue;
                }

                String file = parts[1];
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                double w = Double.parseDouble(parts[4]);
                double h = Double.parseDouble(parts[5]);
                double opacity = Double.parseDouble(parts[6]);
                int zIndex = Integer.parseInt(parts[7]);

                String normalizedPath = file.replaceFirst("^file:/+", "");
                if (!Files.exists(Paths.get(GLOBAL_PATH + normalizedPath))) {
                    showAlternativeImage();
                    return;
                }

                ImageView iv = new ImageView(new Image("file:/" + GLOBAL_PATH + file));
                iv.setFitWidth(w);
                iv.setFitHeight(h);
                iv.setOpacity(opacity);
                iv.setLayoutX(x);
                iv.setLayoutY(y);

                layers.add(new ImageLayer(iv, zIndex));
            }
        } catch (IOException e) {
            showAlternativeImage();
            return;
        }

        // Sort by zIndex ascending before displaying
        layers.sort(Comparator.comparingInt(layer -> layer.zIndex));

        // Add in correct visual order
        for (ImageLayer layer : layers) {
            animationPane.getChildren().add(layer.imageView);
        }
    }

    // ---------------------- Set alternative display ----------------------
    public void setDisplayAlternative(String imagePath) {
        this.displayAlternativePath = imagePath;
    }

    public void showAlternativeImage() {
        if (containerPane == null || displayAlternativePath == null) {
            System.err.println("Container or alternative path not set.");
            return;
        }

        Platform.runLater(() -> {
            try {
                Image altImage = new Image("file:\\" + displayAlternativePath, 990, 421, true, true);
                ImageView altView = new ImageView(altImage);
                altView.setPreserveRatio(true);
                altView.setSmooth(true);

                containerPane.getChildren().clear();
                containerPane.getChildren().add(altView);
                StackPane.setAlignment(altView, Pos.CENTER);

                System.out.println("Alternative image displayed successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // ---------------------- 5. Attach to container ----------------------
    public void attachToContainer() {
        if (containerPane == null || animationPane == null) {
            return;
        }

        Platform.runLater(() -> {
            containerPane.getChildren().clear();

            StackPane wrapper = new StackPane(animationPane);
            wrapper.setAlignment(javafx.geometry.Pos.CENTER);
            wrapper.setPickOnBounds(false);

            if (containerPane instanceof StackPane) {
                ((StackPane) containerPane).setAlignment(javafx.geometry.Pos.CENTER);
                containerPane.getChildren().add(wrapper);
            } else {
                containerPane.getChildren().add(wrapper);
                AnchorPane.setTopAnchor(wrapper, 0.0);
                AnchorPane.setBottomAnchor(wrapper, 0.0);
                AnchorPane.setLeftAnchor(wrapper, 0.0);
                AnchorPane.setRightAnchor(wrapper, 0.0);
            }
        });
    }

    public AnchorPane getAnimationPane() {
        return animationPane;
    }

    // ---------------------- Helper classes ----------------------
    private static class FrameData {

        String type, file;
        double x, y, width, height, opacity;
        int zIndex;
        boolean excludeFade;

        FrameData(String type, String file, double x, double y, double w, double h,
                double opacity, int zIndex, boolean excludeFade) {
            this.type = type;
            this.file = file;
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.opacity = opacity;
            this.zIndex = zIndex;
            this.excludeFade = excludeFade;
        }
    }

    private static class FrameDataPair {

        FrameData from;
        FrameData to;
    }
}
