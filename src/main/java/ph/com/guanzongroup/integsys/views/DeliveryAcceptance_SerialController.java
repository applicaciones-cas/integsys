package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_SerialMP;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
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
 * @author User
 */
public class DeliveryAcceptance_SerialController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnEntryNo = 0;
    int pnDetail = 0;
    private final String pxeModuleName = "Delivery Acceptance Serial";
    static PurchaseOrderReceiving poPurchaseReceivingController;
    public int pnEditMode;
    public boolean pbIsFinancing = false;
    private ObservableList<ModelDeliveryAcceptance_SerialMP> details_data = FXCollections.observableArrayList();

    @FXML
    private AnchorPane apBrowse, apButton, apDetail;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnOkay, btnClose;
    @FXML
    private TextField tfIMEI1, tfIMEI2;
    @FXML
    private CheckBox cbApplyToAll;
    @FXML
    private TableView<ModelDeliveryAcceptance_SerialMP> tblViewDetail;
    @FXML
    private TableColumn tblRowNoDetail, tblIMEI1Detail, tblIMEI2Detail;

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
                if (tfIMEI1.getText() == null || "".equals(tfIMEI1.getText())) {
                    tfIMEI1.requestFocus();
                }
            });
            delay.play();

            JFXUtil.setButtonsVisibility(!pbIsFinancing, btnOkay);
            JFXUtil.setDisabled(pbIsFinancing, apDetail);
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
                    lsMessage = "IMEI 1 at row " + lnRow + " cannot be empty.";
                    inform = true;
                    break;

                }
                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02() == null || poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02().equals("")) {
                    poJSON.put("result", "error");
                    lsMessage = "IMEI 2 at row " + lnRow + " cannot be empty.";
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

        ModelDeliveryAcceptance_SerialMP selectedItem = tblViewDetail.getItems().get(pnDetail);
        int pnDetail2 = Integer.valueOf(selectedItem.getIndex04());

        if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getEntryNo() != pnEntryNo) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfIMEI1":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "serial01", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfIMEI1.setText("");
                        tfIMEI1.requestFocus();
                        return;
                    }

                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
//                        if(!lsValue.isEmpty()){
//                            poJSON = poPurchaseReceivingController.CheckSerial(lsValue, pnDetail2);
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                if(!(boolean) poJSON.get("continue")){
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfIMEI1.setText("");
//                                    tfIMEI1.requestFocus();
//                                    return;
//                                }
//                            }
//                            
//                            //If no record found manually set the serial 01
//                            if((boolean) poJSON.get("continue")){
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
//                            }
//                        } else{
//                            if(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerialId() != null 
//                                && !"".equals(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerialId())){
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerialId("");
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
//                            } else {
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
//                            }
//                        }
                    }
                    break;
                case "tfIMEI2":
                    poJSON = poPurchaseReceivingController.checkExistingSerialNo(pnDetail2, "serial02", lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfIMEI2.setText("");
                        tfIMEI2.requestFocus();
                        return;
                    }

                    if ((boolean) poJSON.get("set")) {
                        loadRecordDetail();
                    } else {
                        poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
//                        if(!lsValue.isEmpty()){
//                            poJSON = poPurchaseReceivingController.CheckSerial(lsValue, pnDetail2);
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                if(!(boolean) poJSON.get("continue")){
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    tfIMEI2.setText("");
//                                    tfIMEI2.requestFocus();
//                                    return;
//                                }
//                            }
//                            
//                            //If no record found manually set the serial 01
//                            if((boolean) poJSON.get("continue")){
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
//                            }
//                        } else{
//                            if(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerialId() != null 
//                                && !"".equals(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerialId())){
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerialId("");
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial01(lsValue);
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
//                            } else {
//                                poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).setSerial02(lsValue);
//                            }
//                        }
                    }
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

    public void loadRecordDetail() {
        if (details_data.size() > 0) {
            ModelDeliveryAcceptance_SerialMP selectedItem = tblViewDetail.getItems().get(pnDetail);
            int pnDetail2 = Integer.valueOf(selectedItem.getIndex04());

//            if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                tfIMEI1.promptTextProperty().set("Press F3: Search");
//                tfIMEI2.promptTextProperty().set("Press F3: Search");
//            }
            
            tfIMEI1.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerial01());
            tfIMEI2.setText(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(pnDetail2).getSerial02());
            updateCaretPositions(apDetail);
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
                        poJSON = poPurchaseReceivingController.getPurchaseOrderReceivingSerial(pnEntryNo);
                        for (lnCtr = 0; lnCtr <= poPurchaseReceivingController.getPurchaseOrderReceivingSerialCount() - 1; lnCtr++) {
                            if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getEntryNo() == pnEntryNo) {
                                if (poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).Location().getDescription() != null) {
                                    lsLocation = poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).Location().getDescription();
                                }
                                details_data.add(
                                        new ModelDeliveryAcceptance_SerialMP(
                                                String.valueOf(lnRow + 1),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial01()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceivingSerialList(lnCtr).getSerial02()),
                                                String.valueOf(lnCtr)
                                        ));
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
                        tfIMEI1.requestFocus();
                        if (tfIMEI1.getText() == null || "".equals(tfIMEI1.getText())) {
                            tfIMEI1.requestFocus();
                        }
                        break;
                    case UP:
                        pnDetail = moveToPreviousRow(currentTable, focusedCell);
                        loadRecordDetail();
                        tfIMEI1.requestFocus();
                        if (tfIMEI1.getText() == null || "".equals(tfIMEI1.getText())) {
                            tfIMEI1.requestFocus();
                        }
                        break;

                    default:
                        break;
                }
                event.consume();
            }
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        ModelDeliveryAcceptance_SerialMP selectedItem = tblViewDetail.getItems().get(pnDetail);
        int pnDetail2 = Integer.valueOf(selectedItem.getIndex04());
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
//                if(poPurchaseReceivingController.Master().getPurpose().equals(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT)){
//                    switch (lsID) {
//                        case "tfIMEI1":
//                            poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                tfIMEI1.setText("");
//                            }
//                            loadTableDetail();
//                            break;
//                        case "tfIMEI2":
//                            poJSON = poPurchaseReceivingController.SearchSerial(lsValue, pnDetail2);
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                tfIMEI2.setText("");
//                            }
//                            loadTableDetail();
//                            break;
//                    }
//                }
                break;
            default:
                break;
        }
    }

    public void initTableOnClick() {
        tblViewDetail.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                loadRecordDetail();
                if (tfIMEI1.getText() == null || "".equals(tfIMEI1.getText())) {
                    tfIMEI1.requestFocus();
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
        tfIMEI1.focusedProperty().addListener(txtDetail_Focus);
        tfIMEI2.focusedProperty().addListener(txtDetail_Focus);

        tfIMEI1.setOnKeyPressed(this::txtField_KeyPressed);
        tfIMEI2.setOnKeyPressed(this::txtField_KeyPressed);

    }

    public void initDetailsGrid() {
        tblRowNoDetail.setStyle("-fx-alignment: CENTER;");
        tblIMEI1Detail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblIMEI2Detail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");

        tblRowNoDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblIMEI1Detail.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblIMEI2Detail.setCellValueFactory(new PropertyValueFactory<>("index03"));

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
