package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.guanzon.cas.inv.warehouse.InventoryStockIssuanceNeo;
import org.guanzon.cas.inv.warehouse.status.DeliveryIssuanceType;
import org.guanzon.cas.inv.warehouse.status.InventoryStockIssuanceStatus;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Transfer_Detail;
import org.guanzon.cas.inv.warehouse.model.Model_Inventory_Transfer_Master;
import org.guanzon.cas.inv.warehouse.services.DeliveryIssuanceControllers;

/**
 *
 * @author 12mnv 08-22-2025
 */
public class InventoryStockIssuance_PostingControllerCar_SP implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private JSONObject poJSON;
    private LogWrapper poLogWrapper;
    private int pnEditMode;
    private InventoryStockIssuanceNeo poAppController;
    private String psFormName = "Issuance Posting";

    private String psTransactionNoOld = "";

    private int pnDetailRow = -1;
    private int pnTransaction = -1;
    private int pnBranchList = -1;
    private Control lastFocusedControl = null;

    private ObservableList<Model_Inventory_Transfer_Detail> laTransactionDetail;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail,
            apTransaction;

    @FXML
    private TextField tfSearchSource, tfSearchTransaction,
            tfColor, tfTransactionNo, tfTrucking, tfBranch,
            tfFreight, tfDiscount, tfTotal, tfOrderNo, tfQuantity,
            tfInvType, tfBarcode, tfDescription, tfSupersede,
            tfBrand, tfCost, tfMeasure, tfVariant, tfSerialNo,
            tfModel, tfReceiveQuantity;
    @FXML
    private ComboBox cbDeliveryTp;
    @FXML
    private DatePicker dpTransactionDate, dpReceivedDate;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnHistory, btnRetrieve, btnPost, btnClose;
    @FXML
    private TextArea taRemarks, taNote;
    @FXML
    private TableView<Model_Inventory_Transfer_Detail> tblViewTransactionDetail;
    @FXML
    private TableView<Model_Inventory_Transfer_Master> tblViewTransaction;
    @FXML
    private TableColumn<Model_Inventory_Transfer_Master, String> tblColMasterNo, tblColMasterTransaction,
            tblColMasterDate, tblColMasterEntryNo, tblColMasterBranch;
    @FXML
    private TableColumn<Model_Inventory_Transfer_Detail, String> tblColDetailNo, tblColDetailOrderNo, tblColDetailSerialNo,
            tblColDetailBarcode, tblColDetailDescription, tblColDetailBrand, tblColDetailVariant, tblColDetailCost, tblColDetailQty,
            tblColDetailRecQty;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName + "Log");
            poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).InventoryStockIssuanceNeo();
            
           
            if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            Platform.runLater(() -> {
                poAppController.setTransactionStatus("1");
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);
            });

            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        try {
            switch (lsButton) {

                case "btnPost":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Inventory Stock Issuance Posting", "");
                        return;
                    }

                    if (dpReceivedDate.getValue() == null) {
                        ShowMessageFX.Information("Please input date received before proceeding..", "Inventory Stock Issuance Posting", "");
                        return;
                    }
                    if (!isJSONSuccess(poAppController.PostTransaction(), "Initialize Post Transaction")) {
                        return;
                    }
                    reloadTableDetail();
                    clearAllInputs();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    return;
                case "btnRetrieve":
                    switch (lastFocusedControl.getId()) {
                        //Master Retrieve
                        case "tfSearchSource":
                            loadTransaction(tfSearchSource.getText(), "d.sBranchNm");
                            break;

                        case "tfSearchTransaction":
                            loadTransaction(tfSearchTransaction.getText(), "a.sTransNox");
                            break;
                    }
                    break;
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                    }
                    break;

                default:
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
            }
            initButtonDisplay(poAppController.getEditMode());

        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

        }
    }

    @FXML
    private void tblViewTransaction_MouseClicked(MouseEvent event) {
        pnTransaction = tblViewTransaction.getSelectionModel().getSelectedIndex();
        if (pnTransaction < 0) {
            return;
        }
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") != true) {
                    return;
                }
            }
            try {
                event.consume();
                if (!isJSONSuccess(poAppController.OpenTransaction(tblColMasterTransaction.getCellData(pnTransaction)),
                        "Initialize Open Transaction")) {
                    return;

                }
                getLoadedTransaction();
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class.getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }

        }
        return;
    }

    @FXML
    private void tblViewTransactionDetail_MouseClicked(MouseEvent event) {

        try {
            pnDetailRow = tblViewTransactionDetail.getSelectionModel().getSelectedIndex() + 1;
            if (pnDetailRow <= 0) {
                return;
            }

            loadSelectedTransactionDetail(pnDetailRow);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    //Fetching All Controller 
    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControl);
                loControlField.setOnKeyPressed(this::txtArea_KeyPressed);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
                loControlField.getEditor().setOnKeyPressed(this::dPicker_KeyPressed);
            }
        }

        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            }
        }

        cbDeliveryTp.setItems(FXCollections.observableArrayList(DeliveryIssuanceType.DeliveryType));
        cbDeliveryTp.getSelectionModel().select(0);
        pnEditMode = poAppController.getEditMode();

        initButtonDisplay(poAppController.getEditMode());
        lblStatus.setText("UNKNOWN");
        laTransactionDetail.clear();

    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnHistory", "btnPost", "btnRetrieve", "btnClose");
        apMaster.setDisable(!lbShow);
        apDetail.setDisable(!lbShow);
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();

        if (lsValue == null) {
            return;
        }
        try {

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "tfReceiveQuantity":
                        double lnReceived;
                        try {
                            lnReceived = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnReceived = 0.0; // default if parsing fails
                            poAppController.getDetail(pnDetailRow).setReceivedQuantity(lnReceived);
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnDetailRow);
                            loTextField.requestFocus();
                        }
                        if (lnReceived < 0.00) {
                            return;
                        }
                        // check if serialized
                        if (poAppController.getDetail(pnDetailRow).Inventory().isSerialized()) {
                            // must be whole number AND exactly 1
                            if (lnReceived != 1 || lnReceived % 1 != 0) {
                                ShowMessageFX.Information("Invalid quantity for serialized item", psFormName, null);
                                lnReceived = 1; // force to 1
                            }
                        }
                        poAppController.getDetail(pnDetailRow).setReceivedQuantity(lnReceived);

                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnDetailRow);

                        break;
                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchSource":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Transaction ", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransactionPosting(lsValue, false, true),
                                        "Initialize Search Source! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());

                                return;

                            case "tfSearchTransaction":
                                if (!isJSONSuccess(poAppController.searchTransactionPosting(lsValue, true, true),
                                        "Initialize Search Source! ")) {
                                    return;
                                }
                            default:
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                return;
                        }
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextAreaID = loTextArea.getId();
        String lsValue = loTextArea.getText();
        if (lsValue == null) {
            return;
        }

        try {

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextAreaID) {
                    case "taNote":

                        poAppController.getDetail(pnDetailRow).setNote(lsValue);

                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnDetailRow);
                        break;
                }
            } else {
                loTextArea.selectAll();
            }

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea loTxtArea = (TextArea) event.getSource();
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    CommonUtils.SetNextFocus((TextArea) event.getSource());
                    return;
                case UP:
                    CommonUtils.SetPreviousFocus((TextArea) event.getSource());
                    return;
                case DOWN:
                    CommonUtils.SetNextFocus(loTxtArea);
                    return;

            }
        }
    }
    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }

        LocalDateTime ldDateTimeValue = loValue.atTime(LocalTime.now());
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpReceivedDate":
                    poAppController.getMaster().setReceivedDate((ldDateTimeValue));
                    return;

            }
        }
    };

    private void dPicker_KeyPressed(KeyEvent event) {

        TextField loTxtField = (TextField) event.getSource();
        String loDatePickerID = ((DatePicker) loTxtField.getParent()).getId(); // cautious cast
        String loValue = loTxtField.getText();
        String lsValue = "";
        if (loValue != null && !loValue.isEmpty()) {
            Date toDateValue = SQLUtil.toDate(loValue, "dd/MM/yyyy");
            lsValue = SQLUtil.dateFormat(toDateValue, SQLUtil.FORMAT_SHORT_DATE);

        }

        if (event.getCode() != null) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    event.consume();
                    switch (loDatePickerID) {

                    }
            }
        }
        event.consume();

    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            poLogWrapper.severe(psFormName + " :" + message);
            Platform.runLater(() -> {
                ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        poLogWrapper.severe(psFormName + " :" + message);
        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
            }
        });
        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;

    }

    private void loadTransactionMaster() throws GuanzonException, SQLException {
        tfTransactionNo.setText(poAppController.getMaster().getTransactionNo());
        cbDeliveryTp.getSelectionModel().select((poAppController.getMaster().getDeliveryType() != null
                && !poAppController.getMaster().getDeliveryType().isEmpty())
                ? Integer.parseInt(poAppController.getMaster().getDeliveryType())
                : 0);
        tfBranch.setText(poAppController.getMaster().Branch().getBranchName());
        tfTrucking.setText(poAppController.getMaster().TruckingCompany().getCompanyName());
        tfFreight.setText(String.valueOf(poAppController.getMaster().getFreight()));
        tfDiscount.setText(String.valueOf(poAppController.getMaster().getDiscount()));
        tfTotal.setText(String.valueOf(poAppController.getMaster().getTransactionTotal()));
        dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));

        dpReceivedDate.setValue(
                poAppController.getMaster().getReceivedDate() != null ? ParseDate(poAppController.getMaster().getReceivedDate()) : LocalDate.now());
        taRemarks.setText(poAppController.getMaster().getRemarks());
        lblStatus.setText(InventoryStockIssuanceStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));
        lblSource.setText(poAppController.getMaster().Company().getCompanyName() + " - " + poAppController.getMaster().Industry().getDescription());
        dpReceivedDate.requestFocus();
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfOrderNo.setText(tblColDetailOrderNo.getCellData(tblIndex));
        tfSerialNo.setText(tblColDetailSerialNo.getCellData(tblIndex));
        tfBarcode.setText(tblColDetailBarcode.getCellData(tblIndex));
        tfDescription.setText(tblColDetailDescription.getCellData(tblIndex));
        tfBrand.setText(tblColDetailBrand.getCellData(tblIndex));
        tfVariant.setText(tblColDetailVariant.getCellData(tblIndex));
        tfCost.setText(tblColDetailCost.getCellData(tblIndex));
        tfQuantity.setText(tblColDetailQty.getCellData(tblIndex));
        tfReceiveQuantity.setText(tblColDetailRecQty.getCellData(tblIndex));

        taNote.setText(poAppController.getDetail(fnRow).getNote());
        tfSupersede.setText(poAppController.getDetail(fnRow).InventorySupersede().getBarCode());
        tfModel.setText(poAppController.getDetail(fnRow).Inventory().Model().getDescription());
        tfColor.setText(poAppController.getDetail(fnRow).Inventory().Color().getDescription());
        tfMeasure.setText(poAppController.getDetail(fnRow).Inventory().Measure().getDescription());
        tfInvType.setText(poAppController.getDetail(fnRow).Inventory().InventoryType().getDescription());

    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();
            tblViewTransactionDetail.setItems(laTransactionDetail);
            tblColDetailQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailRecQty.setStyle("-fx-alignment: CENTER; -fx-padding: 0 5 0 0;");

            // Set column factories ONCE
            tblColDetailNo.setCellValueFactory(loModel -> {
                int index = tblViewTransactionDetail.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailOrderNo.setCellValueFactory(loModel -> {
                String orderNo = loModel.getValue().getOrderNo();
                return new SimpleStringProperty(orderNo != null ? orderNo : "");

            });

            tblColDetailSerialNo.setCellValueFactory(loModel -> {
                try {
                    String s1 = loModel.getValue().InventorySerial().getSerial01();
                    String s2 = loModel.getValue().InventorySerial().getSerial02();

                    String xserialname = (s1 != null && !s1.isEmpty() ? s1 : "")
                            + ((s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty()) ? " / " : "")
                            + (s2 != null && !s2.isEmpty() ? s2 : "");

                    return new SimpleStringProperty(xserialname);
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBarcode.setCellValueFactory(loModel -> {
                try {
                    String barcode = loModel.getValue().Inventory().getBarCode();
                    return new SimpleStringProperty(barcode != null ? barcode : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailDescription.setCellValueFactory(loModel -> {
                try {
                    String description = loModel.getValue().Inventory().getDescription();
                    return new SimpleStringProperty(description != null ? description : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBrand.setCellValueFactory(loModel -> {
                try {
                    String lsObject = loModel.getValue().Inventory().Brand().getDescription();
                    return new SimpleStringProperty(lsObject != null ? lsObject : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailVariant.setCellValueFactory(loModel -> {
                try {
                    String lsObject = loModel.getValue().Inventory().Variant().getDescription();
                    return new SimpleStringProperty(lsObject != null ? lsObject : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCost.setCellValueFactory(loModel -> {
                try {
                    String lsObject = loModel.getValue().Inventory().getSellingPrice().toString();
                    return new SimpleStringProperty(lsObject != null ? lsObject : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailQty.setCellValueFactory(loModel -> {
                String lsObject = loModel.getValue().getQuantity().toString();
                return new SimpleStringProperty(lsObject != null ? lsObject : "");

            });

            tblColDetailRecQty.setCellValueFactory(loModel -> {
                String lsObject = loModel.getValue().getReceivedQuantity().toString();
                return new SimpleStringProperty(lsObject != null ? lsObject : "");

            });

        }
    }

    private void reloadTableDetail() {
        List<Model_Inventory_Transfer_Detail> rawDetail = poAppController.getDetailList();

        //remove auto added detail
        for (int lnCtr = 0; lnCtr < rawDetail.size(); lnCtr++) {
            if (rawDetail.get(lnCtr).getStockId() == null
                    || rawDetail.get(lnCtr).getStockId().isEmpty()) {
                rawDetail.remove(lnCtr);
            }
        }
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnDetailRow >= 1 && pnDetailRow < laTransactionDetail.size())
                ? pnDetailRow - 1
                : laTransactionDetail.size() - 1;

        tblViewTransactionDetail.getSelectionModel().select(indexToSelect);
        pnDetailRow = tblViewTransactionDetail.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex

        tblViewTransactionDetail.refresh();
    }

    private void loadTransaction(String value, String fsColumn) throws SQLException, CloneNotSupportedException, GuanzonException {
        StackPane overlay = getOverlayProgress(apTransaction);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Inventory_Transfer_Master>> loadTransactionTask = new Task<ObservableList<Model_Inventory_Transfer_Master>>() {
            @Override
            protected ObservableList<Model_Inventory_Transfer_Master> call() throws Exception {

                if (!isJSONSuccess(poAppController.loadTransactionListPosting(value, fsColumn),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Inventory_Transfer_Master> rawList = poAppController.getMasterList();
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Inventory_Transfer_Master> laMasterList = getValue();
                tblViewTransaction.setItems(laMasterList);

                tblColMasterNo.setCellValueFactory(loModel -> {
                    int index = tblViewTransaction.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });

                tblColMasterTransaction.setCellValueFactory(loModel
                        -> new SimpleStringProperty(loModel.getValue().getTransactionNo()));
                tblColMasterDate.setCellValueFactory(loModel
                        -> new SimpleStringProperty(SQLUtil.dateFormat(loModel.getValue().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE)));
                tblColMasterEntryNo.setCellValueFactory(loModel
                        -> new SimpleStringProperty(String.valueOf(loModel.getValue().getEntryNo())));
                tblColMasterBranch.setCellValueFactory(loModel -> {
                    try {
                        String lsObject = loModel.getValue().Branch().getBranchName();
                        return new SimpleStringProperty(lsObject != null ? lsObject : "");

                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                                .getName()).log(Level.SEVERE, null, ex);
                        poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                        return new SimpleStringProperty("");
                    }
                });
                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(InventoryStockIssuance_PostingControllerCar_SP.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };

        Thread thread = new Thread(loadTransactionTask);
        thread.setDaemon(true);
        thread.start();
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private void getLoadedTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnDetailRow);
    }
}
