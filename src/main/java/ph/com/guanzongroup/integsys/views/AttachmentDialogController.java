/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.util.Duration;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Aldrich & Arsiela Team 2 05242025
 */
public class AttachmentDialogController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private int currentIndex = 0;
    private double xOffset = 0;
    private double yOffset = 0;
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private int pnAttachment = 0;
    Map<String, Pair<String, String>> cloned;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();

    private PurchaseOrderReceiving poModel;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apAttachment;

    @FXML
    private HBox hbButtons;

    @FXML
    private Button btnClose, btnArrowLeft, btnArrowRight;

    @FXML
    private Label lblTitle, lblAttachmentType;

    @FXML
    private TextField tfAttachmentNo, tfAttachmentType;

    @FXML
    private StackPane stackPane1;

    @FXML
    private ImageView imageView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        JFXUtil.checkIfFolderExists(poJSON, System.getProperty("sys.default.path.temp") + "/Attachments//");
        initAttachmentPreviewPane();
        Platform.runLater(() -> {
            loadTableAttachment();
            JFXUtil.stackPaneClip(stackPane1);
            loadRecordAttachment(true);
        });
    }

    public void setObject(PurchaseOrderReceiving foValue) {
        poModel = foValue;
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String tabText = "";

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":
                        CommonUtils.closeStage(btnClose);
                        break;
                    case "btnArrowLeft":
                        slideImage(-1);
                        break;
                    case "btnArrowRight":
                        slideImage(1);
                        break;
                }
            }
        } catch (Exception e) {

        }
    }

    public void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }
        currentIndex = pnAttachment;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            ModelDeliveryAcceptance_Attachment image = attachment_data.get(newIndex);
            String filePath2 = System.getProperty("sys.default.path.temp") + "/Attachments//" + image.getIndex02();
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            pnAttachment = newIndex;
            loadRecordAttachment(false);

            // Create a transition animation
            slideOut.setOnFinished(event -> {
                imageView.setTranslateX(direction * 400);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imageView);
                slideIn.setToX(0);
                slideIn.play();

                loadRecordAttachment(true);
            });

            slideOut.play();
        }
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackPane1)) {
            JFXUtil.resetImageBounds(imageView, stackPane1);
        }
    }

    private void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(String.valueOf(pnAttachment + 1));
                String lsAttachmentType = (String) attachment_data.get(pnAttachment).getIndex03();
                if (lsAttachmentType.equals("")) {
                    lsAttachmentType = DocumentType.OTHER;
                }

                if (lsAttachmentType != null) {
                    tfAttachmentType.setText(documentType.get(Integer.valueOf(lsAttachmentType)));
                } else {
                    tfAttachmentType.setText("");
                }

                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(pnAttachment).getIndex02();
                        String filePath2 = "";
//                        if (imageinfo_temp.containsKey((String) attachment_data.get(pnAttachment).getIndex02())) {
//                            filePath2 = imageinfo_temp.get((String) attachment_data.get(pnAttachment).getIndex02());
//                        } else {
                        // in server
                        filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(pnAttachment).getIndex02();
//                        }
                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            // Clear previous content
                            stackPane1.getChildren().clear();

                            if (!isPdf) {
                                // ----- IMAGE VIEW -----
                                Image loimage = new Image(convertedPath);
                                imageView.setImage(loimage);
                                JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackPane1.getChildren().add(imageView);
                                stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);

                                // Align buttons on top
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                            } else {

                                // ----- PDF VIEW -----
                                PDDocument document = PDDocument.load(new File(filePath2));
                                PDFRenderer renderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();

                                // 🔑 FORCE stackPane size (CRITICAL for popup & 1-page PDFs)
                                stackPane1.setMinWidth(imageviewerutil.ldstackPaneWidth);
                                stackPane1.setMinHeight(imageviewerutil.ldstackPaneHeight);
                                stackPane1.setPrefSize(
                                        imageviewerutil.ldstackPaneWidth,
                                        imageviewerutil.ldstackPaneHeight
                                );

                                // Container for PDF pages
                                VBox pdfContainer = new VBox(10);
                                pdfContainer.setAlignment(Pos.CENTER);
                                pdfContainer.setPrefWidth(imageviewerutil.ldstackPaneWidth);

                                for (int i = 0; i < pageCount; i++) {
                                    BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);
                                    Image fxImage = SwingFXUtils.toFXImage(pageImage, null);
                                    ImageView pageView = new ImageView(fxImage);

                                    pageView.setPreserveRatio(true);
                                    pageView.setFitWidth(imageviewerutil.ldstackPaneWidth);

                                    pdfContainer.getChildren().add(pageView);
                                }

                                // Wrap VBox in a Group for scaling
                                Group pdfGroup = new Group(pdfContainer);

                                // Wrap Group in a StackPane to center content
                                StackPane centerPane = new StackPane(pdfGroup);
                                centerPane.setAlignment(Pos.CENTER);

                                // ScrollPane wraps the centerPane
                                ScrollPane scrollPane = new ScrollPane(centerPane);
                                scrollPane.setPannable(true);
                                scrollPane.setFitToWidth(true);
                                scrollPane.setFitToHeight(true);

                                // 🔑 FIX #1 — FORCE CONTENT ≥ VIEWPORT (1-page PDF FIX)
                                scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                                    if (newBounds != null) {
                                        pdfContainer.setMinWidth(newBounds.getWidth());
                                        pdfContainer.setMinHeight(newBounds.getHeight());
                                    }
                                });

                                // Stack PDF and buttons
                                stackPane1.getChildren().setAll(scrollPane, btnArrowLeft, btnArrowRight);
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                                // 🔑 CENTER content initially (important for 1 page)
                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
//                                        scrollPane.setHvalue(0.0);
//                                        scrollPane.setVvalue(0.0);
                                    });
                                });
                                delay.play();

                                document.close();

                                // ----- ZOOM & PAN -----
                                final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
                                scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                                    if (event.isControlDown()) {
                                        event.consume(); // stop default scroll behavior

                                        double delta = event.getDeltaY() > 0 ? 1.1 : 0.9; // multiplier for smooth zoom in/out
                                        double oldZoom = zoomFactor.get();
                                        zoomFactor.set(oldZoom * delta); // scale by multiplier

                                        // Apply scale
                                        pdfGroup.setScaleX(zoomFactor.get());
                                        pdfGroup.setScaleY(zoomFactor.get());

                                        // Keep mouse position centered during zoom
                                        Bounds viewportBounds = scrollPane.getViewportBounds();
                                        Bounds contentBounds = pdfGroup.getBoundsInParent();
                                        double mouseX = event.getX();
                                        double mouseY = event.getY();

                                        double hRatio = (scrollPane.getHvalue() * (contentBounds.getWidth() - viewportBounds.getWidth()) + mouseX) / contentBounds.getWidth();
                                        double vRatio = (scrollPane.getVvalue() * (contentBounds.getHeight() - viewportBounds.getHeight()) + mouseY) / contentBounds.getHeight();

                                        Platform.runLater(() -> {
                                            Bounds newBounds = pdfGroup.getBoundsInParent();
                                            double newH = (hRatio * newBounds.getWidth() - mouseX) / (newBounds.getWidth() - viewportBounds.getWidth());
                                            double newV = (vRatio * newBounds.getHeight() - mouseY) / (newBounds.getHeight() - viewportBounds.getHeight());

                                            scrollPane.setHvalue(Double.isNaN(newH) ? 0.5 : Math.min(Math.max(0, newH), 1.0));
                                            scrollPane.setVvalue(Double.isNaN(newV) ? 0.5 : Math.min(Math.max(0, newV), 1.0));
                                        });
                                    }
                                });

                                // Pan with mouse drag
                                final ObjectProperty<Point2D> lastMouse = new SimpleObjectProperty<>();
                                pdfGroup.setOnMousePressed(e -> lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY())));

                                pdfGroup.setOnMouseDragged(e -> {
                                    if (lastMouse.get() != null) {
                                        double deltaX = e.getSceneX() - lastMouse.get().getX();
                                        double deltaY = e.getSceneY() - lastMouse.get().getY();

                                        pdfGroup.setTranslateX(pdfGroup.getTranslateX() + deltaX);
                                        pdfGroup.setTranslateY(pdfGroup.getTranslateY() + deltaY);

                                        lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY()));
                                    }
                                });
                                pdfGroup.setOnMouseReleased(e -> lastMouse.set(null));
                            }

                        } else {
                            imageView.setImage(null);
                        }

                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }
            } else {
                if (!lbloadImage) {
                    imageView.setImage(null);
                    JFXUtil.stackPaneClip(stackPane1);
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
        }

    }

    public void enableDrag(AnchorPane... panes) {
        for (AnchorPane pane : panes) {
            pane.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            pane.setOnMouseDragged(event -> {
                Stage stage = (Stage) pane.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackPane1, imageView);
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment();
            loadRecordAttachment(true);
        });
        enableDrag(apButton, apAttachment);
    }

    public void addData(Map<String, Pair<String, String>> dataMap) {
        cloned = new HashMap<>(dataMap);
        loadTableAttachment();
    }

    public void setOpenedImage(int lsAttachmentNo) {
        pnAttachment = lsAttachmentNo;
    }

    public void loadTableAttachment() {
        attachment_data.clear();
        if (cloned == null) {
            return;
        }
        List<Map.Entry<String, Pair<String, String>>> entryList = new ArrayList<>(cloned.entrySet());

        for (int i = 0; i < entryList.size(); i++) {
            Map.Entry<String, Pair<String, String>> entry = entryList.get(i);
            String lsRowNo = entry.getKey();
            String lsFileName = entryList.get(i).getValue().getKey();
            String lsAttachmentType = entryList.get(i).getValue().getValue();
            attachment_data.add(
                    new ModelDeliveryAcceptance_Attachment(lsRowNo, lsFileName,
                            lsAttachmentType
                    ));
        }
        loadRecordAttachment(true);
    }

    private void initTextFields() {

    }

    private void clearTextFields() {
        Platform.runLater(() -> {
            lblTitle.setText("");
            lblAttachmentType.setText("");
            imageView.setImage(null);
        });
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }

}
