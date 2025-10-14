package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import ph.com.guanzongroup.cas.inv.warehouse.t4.DeliverySchedule;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.services.DeliveryIssuanceControllers;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.DeliveryScheduleStatus;
import ph.com.guanzongroup.cas.inv.warehouse.t4.constant.DeliveryScheduleTruck;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Detail;
import ph.com.guanzongroup.cas.inv.warehouse.t4.model.Model_Delivery_Schedule_Master;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Cluster_Delivery;
import ph.com.guanzongroup.cas.inv.warehouse.t4.parameter.model.Model_Branch_Others;

/**
 *
 * @author 12mnv
 */
public class DeliverySchedule_EntryControllerMC_SP implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private JSONObject poJSON;
    private LogWrapper poLogWrapper;
    private int pnEditMode;
    private DeliverySchedule poAppController;
    private String psFormName = "Delivery Schedule Entry MC SP";

    private String psClusterNameOld = "";

    private int pnClusterDetail = -1;
    private int pnTransaction = -1;
    private int pnBranchList = -1;
    private Control lastFocusedControl = null;

    private ObservableList<Model_Delivery_Schedule_Detail> laTransactionDetail;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton,
            apCenter, apDetailField, apMaster,
            apDetail, apDetailTable, apTransactionTable, apBranchTable;
    @FXML
    private TextField tfSearchCluster, tfTransactionNo, tfClusterName, tfAllocation;
    @FXML
    private ComboBox cbTruckSize;
    @FXML
    private DatePicker dpSearchDate, dpSearchScheduleDate, dpTransactionDate,
            dpScheduleDate;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnNew, btnUpdate, btnSearch, btnSave,
            btnCancel, btnHistory, btnRetrieve,
            btnClose, btnBrowse;
    @FXML
    private TextArea taRemarks, taNotes;
    @FXML
    private TableView<Model_Delivery_Schedule_Detail> tblClusterDetail;
    @FXML
    private TableView<Model_Delivery_Schedule_Master> tblTransaction;
    @FXML
    private TableView<Model_Branch_Others> tblBranchList;
    @FXML
    private TableColumn<Model_Delivery_Schedule_Master, String> tblColDeliveryNo, tblColDeliveryTransaction, tblColDeliveryDate, tblColDeliveryScheduledDate; //Transaction List
    @FXML
    private TableColumn<Model_Delivery_Schedule_Detail, String> tblColDetailNo, tblColDetailName, tblColDetailTruckSize, tblColDetailAllocation;//Detail Table
    @FXML
    private TableColumn<Model_Branch_Others, String> tblColBranchNo, tblColBranchName, tblColBranchAddress;//BranchList

    //FOR OVERLAY
    private int currentBranchLoadVersion = 0;
    private Task<Void> currentBranchLoadTask;

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
            poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).DeliverySchedule();
            poAppController.setTransactionStatus(DeliveryScheduleStatus.OPEN);
            if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            Platform.runLater(() -> {
                poAppController.setTransactionStatus("0");
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID
                        + "\nCompany :" + psCompanyID
                        + "\nCategory:" + psCategoryID);

                btnNew.fire();
            });

            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliverySchedule_EntryControllerMC_SP.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        try {
            switch (lsButton) {
                case "btnUpdate":
                    if (poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Delivery Schedule Encoding", "");
                        break;
                    }

                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize Update Transaction")) {
                        break;
                    }
                    pnEditMode = poAppController.getEditMode();
                    break;
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }
                    switch (lastFocusedControl.getId()) {
                        //Browse Transaction 
                        case "tfSearchCluster":

                            if (tfSearchCluster.getText().isEmpty()) {
                                ShowMessageFX.Information(null, psFormName,
                                        "Search unavailable. Please ensure the selected or focused field is not empty");
                                break;
                            }

                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Do you want to disregard changes?") == false) {
                                        break;
                                    }
                                }
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchCluster.getText(), true, true),
                                    "Search Transaction!")) {

                                ShowMessageFX.Information(null, psFormName,
                                        "Search unavailable. Transaction not found");
                                break;
                            }

                            clearAllInputs();
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            pnEditMode = poAppController.getEditMode();
                            break;

                        case "dpSearchDate":

                            LocalDate loTransDate = dpSearchDate.getValue();
                            String lsTransValue = "";
                            if (loTransDate != null) {
                                lsTransValue = loTransDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } else {
                                ShowMessageFX.Information("Please entsure date field is not empty", "Delivery Schedule Encoding", "");
                                break;
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(lsTransValue, false, true),
                                    "Search Transaction!! BY Date")) {
                                ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                break;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            break;

                        case "dpSearchScheduleDate":

                            LocalDate loSched = dpSearchScheduleDate.getValue();
                            String lsSched = "";
                            if (loSched != null) {
                                lsSched = loSched.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } else {
                                ShowMessageFX.Information("Please entsure date field is not empty", "Delivery Schedule Encoding", "");
                                break;
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(lsSched, false, true),
                                    "Search Transaction!! BY Schedule Date")) {
                                ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                break;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            break;

                        default:
                            ShowMessageFX.Information(null, psFormName,
                                    "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");

                            break;
                    }
                    break;
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }
                    switch (lastFocusedControl.getId()) {
                        //Search Detail 
                        case "tfClusterName":

                            if (pnClusterDetail >= 0) {
                                if (!isJSONSuccess(poAppController.searchClusterBranch(pnClusterDetail, tfClusterName.getText(), false),
                                        "Search Cluster! ")) {
                                    break;
                                }
                                loadSelectedTransactionDetail(pnClusterDetail);
                                pnEditMode = poAppController.getEditMode();
                                break;
                            }
                            break;

                        //Browse Transaction 
                        case "tfSearchCluster":

                            if (tfSearchCluster.getText().isEmpty()) {
                                ShowMessageFX.Information(null, psFormName,
                                        "Search unavailable. Please ensure the selected or focused field is not empty");
                                break;
                            }

                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Do you want to disregard changes?") == false) {
                                        break;
                                    }
                                }
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchCluster.getText(), true, true),
                                    "Search Transaction!")) {

                                ShowMessageFX.Information(null, psFormName,
                                        "Search unavailable. Transaction not found");
                                break;
                            }

                            clearAllInputs();
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            pnEditMode = poAppController.getEditMode();
                            break;

                        case "dpSearchDate":

                            LocalDate loTransDate = dpSearchDate.getValue();
                            String lsTransValue = "";
                            if (loTransDate != null) {
                                lsTransValue = loTransDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } else {
                                ShowMessageFX.Information("Please entsure date field is not empty", "Delivery Schedule Encoding", "");
                                break;
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(lsTransValue, false, true),
                                    "Search Transaction!! BY Date")) {
                                ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                break;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            break;

                        case "dpSearchScheduleDate":

                            LocalDate loSched = dpSearchScheduleDate.getValue();
                            String lsSched = "";
                            if (loSched != null) {
                                lsSched = loSched.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } else {
                                ShowMessageFX.Information("Please entsure date field is not empty", "Delivery Schedule Encoding", "");
                                break;
                            }

                            if (!isJSONSuccess(poAppController.searchTransaction(lsSched, false, true),
                                    "Search Transaction!! BY Schedule Date")) {
                                ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                break;
                            }
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());

                            break;

                        default:
                            ShowMessageFX.Information(null, psFormName,
                                    "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");

                            break;
                    }
                    break;
                case "btnNew":
                    if (!isJSONSuccess(poAppController.newTransaction(), "Initialize New Transaction")) {
                        break;
                    }

                    clearAllInputs();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;
                case "btnSave":
                    if (poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Delivery Schedule Encoding", "");
                        break;
                    }

                    if (!isJSONSuccess(poAppController.saveTransaction(), "Initialize Save Transaction")) {
                        break;
                    }
                    reloadTableDetail();
//                    clearAllInputs();
                    pnEditMode = poAppController.getEditMode();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new DeliveryIssuanceControllers(poApp, poLogWrapper).DeliverySchedule();
                        poAppController.setTransactionStatus(DeliveryScheduleStatus.OPEN);

                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {
                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);
                        });

                        clearAllInputs();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;
                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    break;
                case "btnRetrieve":
                    switch (lastFocusedControl.getId()) {
                        //Master Retrieve
                        case "tfSearchCluster":
                            loadTransaction(tfSearchCluster.getText(), "sTransNox");
                            break;
                        case "dpSearchDate":
                            String lsDate = dpSearchDate.getValue() != null ? dpSearchDate.getValue().toString() : "";
                            loadTransaction(lsDate, "dTransact");
                            break;
                        case "dpSearchScheduleDate":
                            String lsScheduleDate = dpSearchDate.getValue() != null ? dpSearchScheduleDate.getValue().toString() : "";
                            loadTransaction(lsScheduleDate, "dSchedule");
                            break;
                        //detail only
                        default:
                            if (pnClusterDetail < 0) {
                                break;
                            }
                            if (!tfClusterName.getText().isEmpty()) {
                                loadSelectedBranch(pnClusterDetail);
                                break;
                            }
                            ShowMessageFX.Information(null, psFormName,
                                    "No Cluster detected. Please search to retrieve branch list.");

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
            Logger.getLogger(DeliverySchedule_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

        }
    }

    @FXML
    private void tblTransaction_MouseClicked(MouseEvent event
    ) {

        if (event.getClickCount() == 1 && !event.isConsumed()) {

            try {

                if (!tfTransactionNo.getText().isEmpty()) {
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Do you want to disregard changes?") == false) {
                            return;
                        }
                    }
                }

                pnTransaction = tblTransaction.getSelectionModel().getSelectedIndex();
                pnClusterDetail = 0;
                if (pnTransaction < 0) {
                    return;
                }

                event.consume();
                if (!isJSONSuccess(poAppController.openTransaction(tblColDeliveryTransaction.getCellData(pnTransaction)),
                        "Initialize Open Transaction")) {
                    return;

                }

                getLoadedTransaction();
                initButtonDisplay(poAppController.getEditMode());
                pnEditMode = poAppController.getEditMode();
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(DeliverySchedule_EntryControllerMC.class.getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }

        }
        return;
    }

    @FXML
    private void tblClusterDetail_MouseClicked(MouseEvent event
    ) {

        try {

            if (event.getClickCount() == 1 && !event.isConsumed()) {

                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Do you want to disregard changes?") == false) {
                        return;
                    }
                }

                pnClusterDetail = tblClusterDetail.getSelectionModel().getSelectedIndex();
                if (pnClusterDetail < 0) {
                    return;
                }

                event.consume();
                loadSelectedTransactionDetail(pnClusterDetail);
                loadSelectedBranch(pnClusterDetail);
            } else {
                loadSelectedTransactionDetail(pnClusterDetail);
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(DeliverySchedule_EntryControllerMC.class.getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void tblBranchList_MouseClicked(MouseEvent event
    ) {
        pnBranchList = tblBranchList.getSelectionModel().getSelectedIndex();
        if (pnBranchList < 0) {
            return;
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

        cbTruckSize.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int index = getDeliverySize(String.valueOf(newVal)); // converts "MEDIUM" → 2
                poAppController.getDetail(pnClusterDetail).setTruckSize(String.valueOf(index));
            }
        });
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

        cbTruckSize.setItems(FXCollections.observableArrayList(DeliveryScheduleTruck.SIZE));
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
        initButtonControls(true, "btnRetrieve", "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnNew", "btnUpdate", "btnHistory");
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

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
            }
        } else {
            loTextField.selectAll();
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

                            //Search Detail 
                            case "tfClusterName":
                                if (pnClusterDetail >= 0) {
                                    if (!isJSONSuccess(poAppController.searchClusterBranch(pnClusterDetail, tfClusterName.getText(), false),
                                            "Search Cluster! ")) {
                                        break;
                                    }
                                    loadSelectedTransactionDetail(pnClusterDetail);
                                    pnEditMode = poAppController.getEditMode();
                                    break;
                                }
                                break;

                            //Browse Transaction 
                            case "tfSearchCluster":

                                if (tfSearchCluster.getText().isEmpty()) {
                                    ShowMessageFX.Information(null, psFormName,
                                            "Search unavailable. Please ensure the selected or focused field is not empty");
                                    break;
                                }

                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                        if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Do you want to disregard changes?") == false) {
                                            break;
                                        }
                                    }
                                }

                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchCluster.getText(), true, true),
                                        "Search Transaction!")) {

                                    ShowMessageFX.Information(null, psFormName,
                                            "Search unavailable. Transaction not found");
                                    break;
                                }

                                clearAllInputs();
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                pnEditMode = poAppController.getEditMode();
                                break;

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
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(DeliverySchedule_EntryController.class
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
        if (!nv) {
            /*Lost Focus*/
            switch (lsTextAreaID) {
                case "taRemarks":
                    poAppController.getMaster().setRemarks(lsValue);
                    break;

                case "taNotes":
                    poAppController.getDetail(pnClusterDetail).setRemarks(lsValue);
                    break;
            }
        } else {
            loTextArea.selectAll();
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
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpTransactionDate":
                    poAppController.getMaster().setTransactionDate((ldDateValue));
                    return;
                case "dpScheduleDate":
                    poAppController.getMaster().setScheduleDate((ldDateValue));
                    return;

            }
        }
    };

    private void dPicker_KeyPressed(KeyEvent event) {

        TextField loDateField = (TextField) event.getSource();
        String loDatePickerID = loDateField.getParent().getId(); // cautious cast
        String loValue = loDateField.getText().toString();
        String lsValue = "";

        try {
            if (loValue != null && !loValue.isEmpty()) {
                lsValue = LocalDate.parse(loValue, DateTimeFormatter.ofPattern("M/d/yyyy")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            if (event.getCode() != null) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        event.consume();
                        switch (loDatePickerID) {
                            case "dpSearchDate":

                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, false, true),
                                        "Search Transaction!! BY Date")) {
                                    ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                    break;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());

                                break;

                            case "dpSearchScheduleDate":

                                if (!isJSONSuccess(poAppController.searchTransaction(lsValue, false, true),
                                        "Search Transaction!! BY Schedule Date")) {
                                    ShowMessageFX.Information("No transactions found", "Delivery Schedule Encoding", "");
                                    break;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());

                                break;
                        }
                }
            }
            event.consume();

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliverySchedule_EntryControllerMC.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
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
        dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
        dpScheduleDate.setValue(ParseDate(poAppController.getMaster().getScheduleDate()));
        taRemarks.setText(poAppController.getMaster().getRemarks());
        lblStatus.setText(DeliveryScheduleStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));
        lblSource.setText(poAppController.getMaster().Company().getCompanyName() + " - " + poAppController.getMaster().Industry().getDescription());
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        if (fnRow >= 0) {
            tfClusterName.setText(tblColDetailName.getCellData(fnRow));
            taNotes.setText(poAppController.getDetail(fnRow).getRemarks() == null ? "" : poAppController.getDetail(fnRow).getRemarks());
            tfAllocation.setText(tblColDetailAllocation.getCellData(fnRow));

            if (tfClusterName.getText() == null || tfClusterName.getText().isEmpty()) {
                tblBranchList.getItems().clear();
                psClusterNameOld = tfClusterName.getText();
                cbTruckSize.getItems().clear();
                return;
            } else if (tfClusterName.getText() != null
                    && !tfClusterName.getText().isEmpty()
                    && !tfClusterName.getText().equals(psClusterNameOld)) {
                psClusterNameOld = tfClusterName.getText();
                loadSelectedBranchClusterDelivery(fnRow);
            }
        }
    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();
            tblClusterDetail.setItems(laTransactionDetail);
            tblColDetailAllocation.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailTruckSize.setStyle("-fx-alignment: CENTER; -fx-padding: 0 5 0 0;");

            // Set column factories ONCE
            tblColDetailNo.setCellValueFactory(loModel -> {
                int index = tblClusterDetail.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailName.setCellValueFactory(loModel -> {
                try {
                    String desc = loModel.getValue().BranchCluster().getClusterDescription();
                    return new SimpleStringProperty(desc != null ? desc : "");

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(DeliverySchedule_EntryControllerMC_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailTruckSize.setCellValueFactory(loModel -> {
                try {

                    Model_Delivery_Schedule_Detail detail = loModel.getValue();
                    if (detail.BranchCluster().getClusterDescription() != null && !detail.BranchCluster().getClusterDescription().isEmpty()) {
                        detail.BranchCluster().loadBranchClusterDeliveryList();
                        if (detail.BranchCluster().getBranchClusterDeliverysCount() > 0) {
                            int index = tblClusterDetail.getItems().indexOf(detail);
                            int Size = Integer.parseInt(detail.BranchCluster().BranchClusterDelivery(0).getTruckSize() != null ? detail.BranchCluster().BranchClusterDelivery(0).getTruckSize() : "NONE");
                            cbTruckSize.getSelectionModel().select(DeliveryScheduleTruck.SIZE.get(Size));
                            return new SimpleStringProperty(DeliveryScheduleTruck.SIZE.get(Size));

                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DeliverySchedule_EntryControllerMC.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("UNKNOWN");
                }
                return new SimpleStringProperty("UNKNOWN");
            });

            tblColDetailAllocation.setCellValueFactory(loModel -> {
                try {
                    Model_Delivery_Schedule_Detail detail = loModel.getValue();
                    if (detail.BranchCluster().getClusterDescription() != null && !detail.BranchCluster().getClusterDescription().isEmpty()) {
                        detail.BranchCluster().loadBranchClusterDeliveryList();
                        if (detail.BranchCluster().getBranchClusterDeliverysCount() > 0) {
                            int index = tblClusterDetail.getItems().indexOf(detail);
                            Number allocation = detail.BranchCluster().BranchClusterDelivery(0).getAllocation() != null ? detail.BranchCluster().BranchClusterDelivery(0).getAllocation() : 0;
                            return new SimpleStringProperty(allocation.toString());

                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(DeliverySchedule_EntryControllerMC.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                }
                return new SimpleStringProperty("0");
            });
        }
    }

    private void reloadTableDetail() {
        List<Model_Delivery_Schedule_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnClusterDetail >= 0 && pnClusterDetail < laTransactionDetail.size())
                ? pnClusterDetail
                : laTransactionDetail.size() - 1;

        tblClusterDetail.getSelectionModel().select(indexToSelect);
        pnClusterDetail = tblClusterDetail.getSelectionModel().getSelectedIndex(); // Not focusedIndex

        tblClusterDetail.refresh();
    }

    private void loadSelectedBranchClusterDelivery(int fnSelectedRow) throws CloneNotSupportedException {
        StackPane overlay = getOverlayProgress(apDetail);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<Void> clusterDeliveryTask = new Task<Void>() {
            private ObservableList<Model_Branch_Cluster_Delivery> laSelectedBranchDelivery;
            private List<String> laValidSizes;
            private String lsSelectedSizeName;

            @Override
            protected Void call() throws Exception {

                // Load and convert delivery list
                laSelectedBranchDelivery = FXCollections.observableArrayList(
                        poAppController.getDeliveryBranchClusterDeliveryList(fnSelectedRow)
                );

                laValidSizes = new ArrayList<>();
                for (Model_Branch_Cluster_Delivery delivery : laSelectedBranchDelivery) {
                    int lnTruckSize = Integer.parseInt(delivery.getTruckSize()); // 0 to 4 expected
                    if (lnTruckSize >= 0 && lnTruckSize < DeliveryScheduleTruck.SIZE.size()) {
                        String lsTruckSizeName = DeliveryScheduleTruck.SIZE.get(lnTruckSize);
                        if (!laValidSizes.contains(lsTruckSizeName)) {
                            laValidSizes.add(lsTruckSizeName);
                        }
                    }
                }
                int lnSelectedTruckSizeIndex = Integer.parseInt(poAppController.getDetail(fnSelectedRow).getTruckSize());
                lsSelectedSizeName = DeliveryScheduleTruck.SIZE.get(lnSelectedTruckSizeIndex);
                return null;
            }

            @Override
            protected void succeeded() {
                try {
                    overlay.setVisible(false);
                    pi.setVisible(false);
                    poAppController.getDetail(fnSelectedRow).setTruckSize(String.valueOf(getDeliverySize(lsSelectedSizeName)));

                    cbTruckSize.setItems(FXCollections.observableArrayList(laValidSizes));
                    loadSelectedTransactionDetail(fnSelectedRow);

                    // Select current truck size
                    // Store the selected name (or index as string if preferred)
                    if (!isJSONSuccess(poAppController.LoadBranchClusterDelivery(fnSelectedRow),
                            "Initialize : Load of Branch Cluster Delivery List")) {
                    }
                    reloadTableDetail();

                } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                    Logger.getLogger(DeliverySchedule_EntryControllerMC_SP.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                }
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };

        Thread thread = new Thread(clusterDeliveryTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadSelectedBranch(int fnSelectedRow) throws CloneNotSupportedException {
        StackPane overlay = getOverlayProgress(apBranchTable);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        // Increment version to invalidate previous tasks
        final int taskVersion = ++currentBranchLoadVersion;

        // Cancel previous task if still running
        if (currentBranchLoadTask != null && currentBranchLoadTask.isRunning()) {
            currentBranchLoadTask.cancel(true);
        }

        tblClusterDetail.setDisable(true);
        currentBranchLoadTask = new Task<Void>() {
            private ObservableList<Model_Branch_Others> laBranchList;

            @Override
            protected Void call() throws Exception {
                if (isCancelled()) {
                    return null;
                }

                if (!isJSONSuccess(poAppController.LoadBranchOthers(fnSelectedRow),
                        "Initialize : Load of Branch List")) {
                    return null;
                }

                if (isCancelled()) {
                    return null;
                }

                // Clone the list 
                List<Model_Branch_Others> rawList = poAppController.getDeliveryBranchOtherList(fnSelectedRow);
                laBranchList = FXCollections.observableArrayList(new ArrayList<>(rawList));
                return null;
            }

            @Override
            protected void succeeded() {
                if (taskVersion != currentBranchLoadVersion) {
                    return; // Ignore outdated task
                }
                overlay.setVisible(false);
                pi.setVisible(false);
                //tblBranchList.getItems().clear();// Clear and update table
                tblBranchList.setItems(laBranchList);

                tblColBranchNo.setCellValueFactory(loModel -> {
                    int index = tblBranchList.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });

                tblColBranchName.setCellValueFactory(loModel -> {
                    try {
                        return new SimpleStringProperty(loModel.getValue().Branch().getBranchName());

                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliverySchedule_EntryControllerMC_SP.class
                                .getName()).log(Level.SEVERE, null, ex);
                        poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                tblColBranchAddress.setCellValueFactory(loModel -> {
                    try {
                        return new SimpleStringProperty(loModel.getValue().Branch().getAddress());

                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliverySchedule_EntryControllerMC_SP.class
                                .getName()).log(Level.SEVERE, null, ex);
                        poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                tblClusterDetail.setDisable(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                tblClusterDetail.setDisable(false);
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
                tblClusterDetail.setDisable(false);
            }
        };

        Thread thread = new Thread(currentBranchLoadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadTransaction(String value, String fsColumn) throws SQLException, CloneNotSupportedException, GuanzonException {
        StackPane overlay = getOverlayProgress(apTransactionTable);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Delivery_Schedule_Master>> loadTransactionTask = new Task<ObservableList<Model_Delivery_Schedule_Master>>() {
            @Override
            protected ObservableList<Model_Delivery_Schedule_Master> call() throws Exception {

                if (!isJSONSuccess(poAppController.loadTransactionList(value, fsColumn),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Delivery_Schedule_Master> rawList = poAppController.getMasterList();
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Delivery_Schedule_Master> laMasterList = getValue();
                tblTransaction.setItems(laMasterList);

                tblColDeliveryNo.setCellValueFactory(loModel -> {
                    int index = tblTransaction.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });

                tblColDeliveryTransaction.setCellValueFactory(loModel
                        -> new SimpleStringProperty(loModel.getValue().getTransactionNo()));
                tblColDeliveryDate.setCellValueFactory(loModel
                        -> new SimpleStringProperty(SQLUtil.dateFormat(loModel.getValue().getTransactionDate(), SQLUtil.FORMAT_LONG_DATE)));
                tblColDeliveryScheduledDate.setCellValueFactory(loModel
                        -> new SimpleStringProperty(SQLUtil.dateFormat(loModel.getValue().getScheduleDate(), SQLUtil.FORMAT_LONG_DATE)));

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(DeliverySchedule_EntryControllerMC_SP.class
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

    private int getDeliverySize(String fsSizeName) {
        if (fsSizeName == null) {
            return 0;
        }
        for (int lnCtr = 0; lnCtr <= DeliveryScheduleTruck.SIZE.size(); lnCtr++) {
            if (DeliveryScheduleTruck.SIZE.get(lnCtr).equalsIgnoreCase(fsSizeName)) {
                return lnCtr;
            }
        }
        return 0;
    }

    private void getLoadedTransaction() throws CloneNotSupportedException, SQLException, GuanzonException {
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnClusterDetail);
    }
}
