package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.views.ScreenInterface;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.client.account.AP_Client_Master;
import org.guanzon.cas.client.model.Model_AP_Client_Ledger;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author User
 */
public class AccountsPayablexController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Account Payable";
    private Control lastFocusedControl;
    private AP_Client_Master poAppController;
    private ObservableList<Model_AP_Client_Ledger> laLedger;
    private int pnLedger;

    private unloadForm poUnload = new unloadForm();

    @FXML
    private AnchorPane apMainAnchor, apRecord, apLedger;
    @FXML
    private Label lblStatus;

    @FXML
    private Button btnSearch, btnBrowse, btnCancel, btnUpdate, btnSave,
            btnRetrieve, btnClose;

    @FXML
    private TableView<Model_AP_Client_Ledger> tblLedger;

    @FXML
    private TableColumn<Model_AP_Client_Ledger, String> tblColNo, tblColDate, tblColSourceNo, tblColSourceCode, tblColAmountIn, tblColAmountOut;

    @FXML
    private TextField tfSearchCompanyName, tfSearchClient, tfClientID, tfContactPerson,
            tfCompanyName, tfCreditLimit, tfBegBalanace, tfCategory,
            tfDiscount, tfTerm, tfAvailBalance, tfOutStandingBalance,
            tfAddress, tfContactNo, tfTINNo;

    @FXML
    private DatePicker dpBegBalance, dpClientSince;

    @FXML
    private CheckBox cbVatRegistered;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
//        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

            //initlalize and validate record objects from class controller
            //background thread
            Platform.runLater(() -> {
                poAppController.setRecordStatus("10");
            });
            initializeTableLedger();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(AccountsPayablexController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    void tblLedger_Clicked(MouseEvent e) {
//        try {
        pnLedger = tblLedger.getSelectionModel().getSelectedIndex();
        if (pnLedger < 0) {
            return;
        }
//show ui or something todo on ledger
//        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//                poLogWrapper.severe(psFormName + " :" + ex.getMessage());
//        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfTerm":
                            if (!isJSONSuccess(poAppController.searchTerm(tfTerm.getText() == null ? "" : tfTerm.getText(), false),
                                    "Initialize Search Category! ")) {
                                return;
                            }
                            loadClientMaster();
                            break;

                    }
                    break;

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfClientID.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                "")) {
                            return;
                        }

                        getLoadedClient();
                        initButtonDisplay(poAppController.getEditMode());
                        return;
                    }

                    switch (lastFocusedControl.getId()) {

                        case "tfSearchClient":
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                    "Initialize Search Client! ")) {
                                return;
                            }

                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchCompanyName":
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompanyName.getText(), false),
                                    "Initialize Search Client! ")) {
                                return;
                            }
                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;

                        default:
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                    "Initialize Search Client! ")) {
                                return;
                            }

                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnUpdate":
                    if (poAppController.getModel().getClientId() == null || poAppController.getModel().getClientId().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    poAppController.openRecord(poAppController.getModel().getClientId());
                    if (!isJSONSuccess(poAppController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    getLoadedClient();
                    initButtonDisplay(poAppController.getEditMode());
                    break;

                case "btnSave":
                    if (tfClientID.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.saveRecord(), "Initialize Save Record")) {
                        return;
                    }

                    getLoadedClient();
                    initButtonDisplay(poAppController.getEditMode());
                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

                        Platform.runLater(() -> {
                            poAppController.setRecordStatus("01");
                            poAppController.setRecordStatus("07");

                            clearAllInputs();
                        });
                        break;
                    }
                    break;

                case "btnRetrieve":
                    loadLedgerList();
                    reloadTableLedger();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        lsValue.replace(",", "");

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
                case "tfCreditLimit":
                    if (poAppController.getModel().getClientId() == null
                            || poAppController.getModel().getClientId() == null) {
                        if (Double.parseDouble(lsValue) > 0.0) {
                            tfCreditLimit.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Credit Limit! No Client Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnCreditAmount;
                    try {
                        lnCreditAmount = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnCreditAmount = 0.0; // default if parsing fails
                        poAppController.getModel().setCreditLimit(lnCreditAmount);
                        loadClientMaster();
                        loTextField.requestFocus();
                    }
                    if (lnCreditAmount < 0.00) {
                        return;
                    }

                    poAppController.getModel().setCreditLimit(lnCreditAmount);
                    loadClientMaster();
                    break;

                case "tfDiscount":
                    if (poAppController.getModel().getClientId() == null
                            || poAppController.getModel().getClientId() == null) {
                        if (Double.parseDouble(lsValue) > 0.0) {
                            tfDiscount.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Client Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount;
                    try {
                        lnDiscount = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount = 0.0; // default if parsing fails
                        poAppController.getModel().setDiscount(lnDiscount);
                        loadClientMaster();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount < 0.00) {
                        return;
                    }

                    poAppController.getModel().setDiscount(lnDiscount);
                    loadClientMaster();
                    break;

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
                            case "tfSearchClient":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                        "")) {
                                    return;
                                }

                                getLoadedClient();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchCompanyName":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompanyName.getText(), false),
                                        "")) {
                                    return;
                                }
                                getLoadedClient();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                            case "tfTerm":
                                if (!isJSONSuccess(poAppController.searchTerm(tfTerm.getText() == null ? "" : tfTerm.getText(), false),
                                        "Initialize Search Category! ")) {
                                    return;
                                }
                                loadClientMaster();
                                break;

                        }
                        break;
                }
            }
        } catch (Exception ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
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
                case "dpClientSince":
                    poAppController.getModel().setdateClientSince(ldDateValue);
                    return;
//                case "dpBegBalance":
//                    poAppController.getModel().setBeginningDate(ldDateValue);
//                    return;

            }
        }
    };

    private void loadClientMaster() {
        try {
            lblStatus.setText(poAppController.getModel().getRecordStatus().equals("0") == false ? "ACTIVE" : "INACTIVE");

            tfClientID.setText(poAppController.getModel().getClientId());

            tfCategory.setText(poAppController.getModel().Category().getDescription());
            tfCompanyName.setText(poAppController.getModel().Client().getCompanyName());
            tfAddress.setText(poAppController.getModel().ClientAddress().getAddress());
            tfContactPerson.setText(poAppController.getModel().ClientInstitutionContact().getContactPersonName());
            tfContactNo.setText(poAppController.getModel().ClientInstitutionContact().getMobileNo());
            tfTINNo.setText(poAppController.getModel().Client().getTaxIdNumber());

            dpClientSince.setValue(poAppController.getModel().getdateClientSince() == null ? null : ParseDate(poAppController.getModel().getdateClientSince()));
            dpBegBalance.setValue(poAppController.getModel().getBeginningDate() == null ? null : ParseDate(poAppController.getModel().getBeginningDate()));
            tfDiscount.setText(CommonUtils.NumberFormat(poAppController.getModel().getDiscount(), "###,###,##0.0000"));
            tfTerm.setText(poAppController.getModel().Term().getDescription());
            tfCreditLimit.setText(CommonUtils.NumberFormat(poAppController.getModel().getCreditLimit(), "###,###,##0.0000"));
            tfBegBalanace.setText(CommonUtils.NumberFormat(poAppController.getModel().getBeginningBalance(), "###,###,##0.0000"));
            tfAvailBalance.setText(CommonUtils.NumberFormat(poAppController.getModel().getAccountBalance(), "###,###,##0.0000"));
            tfOutStandingBalance.setText(CommonUtils.NumberFormat(poAppController.getModel().getOBalance(), "###,###,##0.0000"));

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
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
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
            }
        }
        cbVatRegistered.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                poAppController.getModel().setVatable(cbVatRegistered.isSelected() == true
                        ? "1" : "0");
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
        initButtonDisplay(poAppController.getEditMode());
//        try {
////            dpBegBalance.setValue((ParseDate((Date) poApp.getServerDate())));
////            dpClientSince.setValue(ParseDate((Date) poApp.getServerDate()));
//        } catch (SQLException ex) {
//            Logger.getLogger(AccountsPayablexController.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnRetrieve", "btnUpdate");

        apRecord.setDisable(!lbShow);
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

    private void initializeTableLedger() {
        if (laLedger == null) {
            laLedger = FXCollections.observableArrayList();

            tblLedger.setItems(laLedger);

            tblColAmountIn.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColAmountOut.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColNo.setCellValueFactory((loModel) -> {
                int index = tblLedger.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDate.setCellValueFactory((loModel) -> {

                return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
            });

            tblColSourceNo.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getSourceNo());
            });

            tblColSourceCode.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getSourceCode());

            });

            tblColAmountIn.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getAmountOt(), "###,##0.0000"));
            });

            tblColAmountOut.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getAmountIn(), "###,##0.0000"));
            });
        }
    }

    private void reloadTableLedger() {
        List<Model_AP_Client_Ledger> rawDetail = poAppController.getLedgerList();
        laLedger.setAll(rawDetail);

//         Restore or select last row
        int indexToSelect = (pnLedger >= 1 && pnLedger < laLedger.size())
                ? pnLedger - 1
                : laLedger.size() - 1;

        tblLedger.getSelectionModel().select(indexToSelect);

        pnLedger = tblLedger.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblLedger.refresh();
    }

    private void loadLedgerList() {
        StackPane overlay = getOverlayProgress(apLedger);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_AP_Client_Ledger>> loadLedger = new Task<ObservableList<Model_AP_Client_Ledger>>() {
            @Override
            protected ObservableList<Model_AP_Client_Ledger> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadLedgerList(),
                        "Initialize : Load of Ledger List")) {
                    return null;
                }

                List<Model_AP_Client_Ledger> rawList = poAppController.getLedgerList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_AP_Client_Ledger> laListLoader = getValue();
                tblLedger.setItems(laListLoader);

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadLedger);
        thread.setDaemon(true);
        thread.start();
    }

    private void getLoadedClient() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadClientMaster();
        reloadTableLedger();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, fsModule + ": " + message));
                }
            }
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
}
