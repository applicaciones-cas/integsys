package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Serial;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author Arsiela
 */
public class DeliveryAcceptance_SerialCarController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnEntryNo = 0;
    int pnDetail = -1;
    private final String pxeModuleName = "Delivery Acceptance Serial Car";
    static PurchaseOrderReceiving poPurchaseReceivingController;
    public int pnEditMode;
    public boolean pbIsFinancing = false;

    private ObservableList<ModelDeliveryAcceptance_Serial> details_data = FXCollections.observableArrayList();

    @FXML
    private AnchorPane apBrowse, apButton, apDetail;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnOkay, btnClose;
    @FXML
    private TextField tfEngineNo, tfFrameNo, tfCSNo, tfPlateNo, tfLocation;
    @FXML
    private Label lblApplyToAll;
    @FXML
    private CheckBox cbApplyToAll;
    @FXML
    private TableView<ModelDeliveryAcceptance_Serial> tblViewDetail;
    @FXML
    private TableColumn tblRowNoDetail, tblEngineNoDetail, tblFrameNoDetail, tblConductionStickerNoDetail, tblPlateNoDetail, tblLocationDetail;
    private final Map<Integer, String> originalValues = new HashMap<>();

    public void setObject(PurchaseOrderReceiving foObject) {
        poPurchaseReceivingController = foObject;
    }

    public void setEntryNo(int entryNo) {
        pnEntryNo = entryNo;
    }

    private Stage getStage() {
        return (Stage) btnClose.getScene().getWindow();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initTextFields();
        initDetailsGrid();
        initTableOnClick();
        loadTableDetail();
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(0.05));
            delay.setOnFinished(event -> {
                loadRecordDetail();
                if (tfEngineNo.getText() == null || "".equals(tfEngineNo.getText())) {
                    tfEngineNo.requestFocus();
                }
            });
            delay.play();
            
            JFXUtil.setButtonsVisibility(!pbIsFinancing, btnOkay);
            JFXUtil.setDisabled(pbIsFinancing,  apDetail);
            cbApplyToAll.setVisible(!pbIsFinancing);
            lblApplyToAll.setVisible(!pbIsFinancing);
            
        });
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }
    
    public void isFinancing(boolean fbValue) {
        pbIsFinancing = fbValue;
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnOkay":
                    //if the user clicked okay all rows must be fill up else remaining row will be allowed to remain empty.
                    //check for empty serial 1 || serial 2 is empty delete the excess row
                    poJSON = checkSerialNo(lsButton);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    CommonUtils.closeStage(btnClose);
                case "btnClose":
                    //if the user clicked okay all rows must be fill up else remaining row will be allowed to remain empty.
                    //check for empty serial 1 || serial 2 is empty delete the excess row
                    poJSON = checkSerialNo(lsButton);
                    if ("error".equals((String) poJSON.get("result"))) {
                        return;
                    }

                    CommonUtils.closeStage(btnClose);
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }
        }
    }

    private JSONObject checkSerialNo(String lsButton) {
        poJSON = new JSONObject();
        int lnRow = 1;
        String lsMessage = "";
        String lsSerialId = "";
        boolean inform = false;
        for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.getPurchaseOrderReceivingSerialCount() - 1; lnCtr++) {
            if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getEntryNo() == pnEntryNo) {
                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial01() == null || poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial01().equals("")) {
                    poJSON.put("result", "error");
                    lsMessage = "Engine No at row " + lnRow + " cannot be empty.";
                    inform = true;
                    break;
                }
                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02() == null || poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02().equals("")) {
                    poJSON.put("result", "error");
                    lsMessage = "Frame No at row " + lnRow + " cannot be empty.";
                    inform = true;
                    break;
                }
                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getLocationId() == null || poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getLocationId().equals("")) {
                    poJSON.put("result", "error");
                    lsMessage = "Location No at row " + lnRow + " cannot be empty.";
                    inform = true;
                    break;
                }
                
                if (lsButton.equals("btnOkay")) {
//                    if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                        if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerialId() == null || "".equals(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerialId())) {
//                            lsSerialId = poPurchaseReceivingController.getSerialId(lnCtr);
//                            if(!lsSerialId.isEmpty()){
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).setSerialId(lsSerialId);
//                            } else {
//                                poJSON.put("result", "error");
//                                lsMessage = "Please select serial that exists in Purchase Order Return transaction at row "+lnRow+".";
//                                inform = true;
//                            }
//                            break;
//                        }
//                    }
                }
                lnRow++;
            }
        }
        if (lsButton.equals("btnOkay")) {
            if ("error".equals((String) poJSON.get("result"))) {
                poJSON.put("message", lsMessage);
                return poJSON;
            }
        } else {
            lsMessage = inform ? "There are still remaining rows that have not been filled. Are you sure you want to close without completing them?" : "Are you sure you want to close the serial?";
            if (ShowMessageFX.OkayCancel(null, pxeModuleName,
                    lsMessage) == false) {
                poJSON.put("result", "error");
                return poJSON;
            } else {
                poJSON.put("result", "success");
                return poJSON;
            }
        }
        return poJSON;
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();

        if (source instanceof CheckBox) {
            CheckBox checkbox = (CheckBox) source;
            boolean isChecked = checkbox.isSelected(); // Check if checked or unchecked
            String lsCheckBox = checkbox.getId();

            ModelDeliveryAcceptance_Serial selectedItem = tblViewDetail.getItems().get(pnDetail);
            int pnDetail2 = Integer.valueOf(selectedItem.getIndex07());
            String lsLocation = poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getLocationId();

            if (lsLocation == null || lsLocation.isEmpty()) {
                checkbox.setSelected(false);
                ShowMessageFX.Warning(null, pxeModuleName, "Location cannot be empty.");
                return;
            }

            if (lsCheckBox.equals("cbApplyToAll")) {
                if (isChecked) {
                    // Store original values before modifying
                    for (int lnCtr = 0; lnCtr < poPurchaseReceivingController.getPurchaseOrderReceivingSerialCount(); lnCtr++) {
                        if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getEntryNo() == pnEntryNo) {
                            if (!originalValues.containsKey(lnCtr)) { // Store only once
                                originalValues.put(lnCtr, poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getLocationId());
                            }
                            poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).setLocationId(lsLocation);
                        }
                    }
                } else {
                    // Revert to original values when checkbox is unchecked
                    for (Map.Entry<Integer, String> entry : originalValues.entrySet()) {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(entry.getKey()).setLocationId(entry.getValue());
                    }
                    originalValues.clear(); // Clear stored values after reverting
                }
                loadTableDetail();
                loadRecordDetail();
            }
        }
    }

    final ChangeListener<? super Boolean> txtDetail_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        if (lsValue == null) {
            return;
        }

        if (pnDetail < 0) {
            return;
        }
        ModelDeliveryAcceptance_Serial selectedItem = tblViewDetail.getItems().get(pnDetail);
        int pnDetail2 = Integer.valueOf(selectedItem.getIndex07());

        if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getEntryNo() != pnEntryNo) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfEngineNo":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "serial01", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfEngineNo.setText("");
                        tfEngineNo.requestFocus();
                        return;
                    }
                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
                    }
                    break;
                case "tfFrameNo":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "serial02", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfFrameNo.setText("");
                        tfFrameNo.requestFocus();
                        return;
                    }
                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
                    }
                    break;
                case "tfCSNo":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "csno", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfCSNo.setText("");
                        tfCSNo.requestFocus();
                        return;
                    }
                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setConductionStickerNo(lsValue);
                    }
                    break;
                case "tfPlateNo":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "plateno", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfPlateNo.setText("");
                        tfPlateNo.requestFocus();
                        return;
                    }
                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setPlateNo(lsValue);
                    }
                    break;
                case "tfLocation":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setLocationId("");
                    }
                    loadRecordDetail();
                    break;
            }
            Platform.runLater(() -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                delay.setOnFinished(event -> {
                    loadTableDetail();
                });
                delay.play();
            });
        }

    };

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            ModelDeliveryAcceptance_Serial selectedItem = tblViewDetail.getItems().get(pnDetail);
            int pnDetail2 = Integer.valueOf(selectedItem.getIndex07());

            TableView<?> currentTable = tblViewDetail;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    apDetail.requestFocus();
                    pnDetail = moveToPreviousRow(currentTable, focusedCell);
                    loadRecordDetail();
                    event.consume();
                    break;
                case DOWN:
                    apDetail.requestFocus();
                    pnDetail = moveToNextRow(currentTable, focusedCell);
                    loadRecordDetail();
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfLocation":
                            /*search location*/
                            String lnLocationOldVal = poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getLocationId();

                            poJSON = poPurchaseReceivingController.SearchLocation(lsValue, false, pnDetail2);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfLocation.setText("");
                                break;
                            }
                            if (!lnLocationOldVal.equals(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getLocationId())
                                    || (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getLocationId() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getLocationId()))) {
                                cbApplyToAll.setSelected(false);
                                originalValues.clear();
                            }

                            loadTableDetail();
                            loadRecordDetail();
                            break;
//                        case "tfEngineNo":
//                            if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                                poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfEngineNo.setText("");
//                                }
//                                loadTableDetail();
//                            }
//                            break;
//                        case "tfFrameNo":
//                            if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                                poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfFrameNo.setText("");
//                                }
//                                loadTableDetail();
//                            }
//                            break;
//                        case "tfCSNo":
//                            if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                                poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfCSNo.setText("");
//                                }
//                                loadTableDetail();
//                            }
//                            break;
//                        case "tfPlateNo":
//                            if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                                poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfPlateNo.setText("");
//                                }
//                                loadTableDetail();
//                            }
//                            break;
                    }
                    
                default:
                    break;
            }

        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (details_data.size() > 0) {
                ModelDeliveryAcceptance_Serial selectedItem = tblViewDetail.getItems().get(pnDetail);
                int pnDetail2 = Integer.valueOf(selectedItem.getIndex07());

//                if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                    tfEngineNo.promptTextProperty().set("Press F3: Search");
//                    tfFrameNo.promptTextProperty().set("Press F3: Search");
//                    tfCSNo.promptTextProperty().set("Press F3: Search");
//                    tfPlateNo.promptTextProperty().set("Press F3: Search");
//                }
                tfEngineNo.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerial01());
                tfFrameNo.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerial02());
                tfCSNo.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getConductionStickerNo());
                tfPlateNo.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getPlateNo());
                tfLocation.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).Location().getDescription());
                
                updateCaretPositions(apDetail);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_SerialCarController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTableDetail() {

        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewDetail.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    poJSON = new JSONObject();
                    int lnCtr = 0;
                    int lnRow = 0;
                    String lsLocation = "";
                    details_data.clear();

                    try {
                        for (lnCtr = 0; lnCtr <= poPurchaseReceivingController.getPurchaseOrderReceivingSerialCount() - 1; lnCtr++) {
                            if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getEntryNo() == pnEntryNo) {
                                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).Location().getDescription() != null) {
                                    lsLocation = poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).Location().getDescription();
                                }

                                details_data.add(
                                        new ModelDeliveryAcceptance_Serial(
                                                String.valueOf(lnRow + 1),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial01()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getConductionStickerNo()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getPlateNo()),
                                                String.valueOf(lsLocation),
                                                String.valueOf(lnCtr),
                                                String.valueOf(""),
                                                String.valueOf(""),
                                                String.valueOf("")
                                        ));
                                lsLocation = "";
                                lnRow++;
                            }
                        }

                        if (pnDetail < 0) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewDetail.getSelectionModel().select(0);
                                tblViewDetail.getFocusModel().focus(0);
                                pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            // Check if the item matches the value of pnDetail
                            tblViewDetail.getSelectionModel().select(pnDetail);
                            tblViewDetail.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }

                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetail.setPlaceholder(placeholderLabel);
                } else {
                    tblViewDetail.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetail.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

    }

    public void adjustLastColumnForScrollbar(TableView<?> tableView) {
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (!(newSkin instanceof TableViewSkin<?>)) {
                return;
            }

            TableViewSkin<?> skin = (TableViewSkin<?>) newSkin;
            VirtualFlow<?> flow = skin.getChildren().stream()
                    .filter(node -> node instanceof VirtualFlow<?>)
                    .map(node -> (VirtualFlow<?>) node)
                    .findFirst().orElse(null);

            if (flow == null) {
                return;
            }

            ScrollBar vScrollBar = flow.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof ScrollBar && ((ScrollBar) node).getOrientation() == Orientation.VERTICAL)
                    .map(node -> (ScrollBar) node)
                    .findFirst().orElse(null);

            if (vScrollBar == null || tableView.getColumns().isEmpty()) {
                return;
            }

            TableColumn<?, ?> lastColumn = (TableColumn<?, ?>) tableView.getColumns()
                    .get(tableView.getColumns().size() - 1);

            vScrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    double scrollBarWidth = newValue ? vScrollBar.getWidth() : 0;
                    double remainingWidth = tableView.getWidth() - scrollBarWidth;

                    double totalFixedWidth = tableView.getColumns().stream()
                            .filter(col -> col != lastColumn)
                            .mapToDouble(col -> ((TableColumn<?, ?>) col).getWidth())
                            .sum();

                    double newWidth = Math.max(0, remainingWidth - totalFixedWidth);
                    lastColumn.setPrefWidth(newWidth - 5);
                });
            });
        });
    }

    private int moveToNextRow(TableView table, TablePosition focusedCell) {
        int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
        table.getSelectionModel().select(nextRow);
        return nextRow;
    }

    private int moveToPreviousRow(TableView table, TablePosition focusedCell) {
        int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
        table.getSelectionModel().select(previousRow);
        return previousRow;
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        pnDetail = moveToNextRow(currentTable, focusedCell);
                        loadRecordDetail();
                        if (tfEngineNo.getText() == null || "".equals(tfEngineNo.getText())) {
                            tfEngineNo.requestFocus();
                        }
                        break;
                    case UP:
                        pnDetail = moveToPreviousRow(currentTable, focusedCell);
                        loadRecordDetail();
                        if (tfEngineNo.getText() == null || "".equals(tfEngineNo.getText())) {
                            tfEngineNo.requestFocus();
                        }
                        break;

                    default:
                        break;
                }
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                loadRecordDetail();
                if (tfEngineNo.getText() == null || "".equals(tfEngineNo.getText())) {
                    tfEngineNo.requestFocus();
                }
            }
        });
        tblViewDetail.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        adjustLastColumnForScrollbar(tblViewDetail);
    }

    public void updateCaretPositions(AnchorPane anchorPane) {
        List<TextField> textFields = getAllTextFields(anchorPane);
        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                Pos alignment = textField.getAlignment();
                if (alignment == Pos.CENTER_RIGHT || alignment == Pos.BASELINE_RIGHT
                        || alignment == Pos.TOP_RIGHT || alignment == Pos.BOTTOM_RIGHT) {
                    textField.positionCaret(0); // Caret at start
                } else {
                    if (textField.isFocused()) {
                        textField.positionCaret(text.length()); // Caret at end if focused
                    } else {
                        textField.positionCaret(0); // Caret at start if not focused
                    }
                }
            }
        }
    }

    private List<TextField> getAllTextFields(Parent parent) {
        return parent.lookupAll(".text-field").stream()
                .filter(node -> node instanceof TextField)
                .map(node -> (TextField) node)
                .collect(Collectors.toList());
    }

    public void initTextFields() {
        tfEngineNo.focusedProperty().addListener(txtDetail_Focus);
        tfFrameNo.focusedProperty().addListener(txtDetail_Focus);
        tfCSNo.focusedProperty().addListener(txtDetail_Focus);
        tfPlateNo.focusedProperty().addListener(txtDetail_Focus);
        tfLocation.focusedProperty().addListener(txtDetail_Focus);

        tfEngineNo.setOnKeyPressed(this::txtField_KeyPressed);
        tfFrameNo.setOnKeyPressed(this::txtField_KeyPressed);
        tfCSNo.setOnKeyPressed(this::txtField_KeyPressed);
        tfPlateNo.setOnKeyPressed(this::txtField_KeyPressed);
        tfLocation.setOnKeyPressed(this::txtField_KeyPressed);

    }

    public void initDetailsGrid() {
        tblRowNoDetail.setStyle("-fx-alignment: CENTER;");
        tblEngineNoDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblFrameNoDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblConductionStickerNoDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblPlateNoDetail.setStyle("-fx-alignment: CENTER-RIGHT;-fx-padding: 0 5 0 5;");
        tblLocationDetail.setStyle("-fx-alignment: CENTER 0 5 0 5;");

        tblRowNoDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblEngineNoDetail.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblFrameNoDetail.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblConductionStickerNoDetail.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblPlateNoDetail.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblLocationDetail.setCellValueFactory(new PropertyValueFactory<>("index06"));

        tblViewDetail.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewDetail.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });

        tblViewDetail.setItems(details_data);
        tblViewDetail.autosize();
    }

}
