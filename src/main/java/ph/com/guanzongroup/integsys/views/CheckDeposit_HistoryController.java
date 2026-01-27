package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Deposit_Detail;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckDeposit_HistoryController implements Initializable, ScreenInterface {
    
    private GRiderCAS poApp;
    private JSONObject poJSON;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Deposit History";
    private String psIndustryID;
    private Control lastFocusedControl;
    private CheckDeposit poAppController;
    private ObservableList<Model_Check_Deposit_Detail> laTransactionDetail;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail = 0;
    
    private unloadForm poUnload = new unloadForm();
    @FXML
    private CheckBox cbReverse;
    
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apMaster, apDetail, apButton, apTransaction;
    
    @FXML
    private TextField tfSearchBankAccountNo, tfSearchTransNo, tfTransactionNo,
            tfBankAccountNo, tfBankAccountName, tfTotal, tfPayee,
            tfBank, tfCheckAmount, tfCheckTransNo,
            tfCheckNo, tfNote, tfBankMaster;
    
    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpTransactionReferDate, dpCheckDate;
    
    @FXML
    private Label lblSource, lblStatus;
    
    @FXML
    private Button btnBrowse, btnPost, btnPrint, btnClose;
    
    @FXML
    private TextArea taRemarks;
    
    @FXML
    private TableView<Model_Check_Deposit_Detail> tblViewDetails;
    
    @FXML
    private TableColumn<Model_Check_Deposit_Detail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailBank,
            tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount;
    
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
            poAppController = new CheckController(poApp, poLogWrapper).CheckDeposit();

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("012347");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);
                
            });
            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(CheckDeposit_HistoryController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    
    @FXML
    void ontblDetailClicked(MouseEvent e) {
        try {
            pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
            if (pnTransactionDetail < 0) {
                return;
            }
            loadSelectedTransactionDetail(pnTransactionDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }
    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            try {
                CheckBox checkedBox = (CheckBox) source;
                switch (checkedBox.getId()) {
                    case "cbReverse": // this is the id
                        if (poAppController.getEditMode() == EditMode.ADDNEW
                                || poAppController.getEditMode() == EditMode.UPDATE
                                && poAppController.getMaster().getTransactionStatus().equals(CheckDepositStatus.OPEN)
                                || poAppController.getMaster().getTransactionStatus().equals(CheckDepositStatus.CONFIRMED)) {
                            if (poAppController.Detail(pnTransactionDetail).getSourceNo() != null
                                    || !poAppController.Detail(pnTransactionDetail).getSourceNo().isEmpty()) {
                                if (!checkedBox.isSelected()) {
                                    poAppController.Detail().remove(pnTransactionDetail);
                                    pnTransactionDetail = pnTransactionDetail - 1;
                                }
                            }
                        } else {
                            poAppController.Detail(pnTransactionDetail).isReverse(checkedBox.isSelected());
                        }
                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnTransactionDetail);
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfTransactionNo.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                "Initialize Search Source No! ")) {
                            return;
                        }
                        
                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        return;
                    }
                    
                    switch (lastFocusedControl.getId()) {
                        case "tfSearchTransNo":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }
                            
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchBankAccountNo":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchBankAccountNo.getText(), true, false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }
                            
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "dpSearchTransactionDate":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(String.valueOf(dpSearchTransactionDate.getValue()), false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }
                            
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        default:
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }
                            
                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            return;
                        
                    }
                    break;
                
                case "btnPost":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, "");
                        return;
                    }
                    if (!isJSONSuccess(poAppController.PostTransaction(), "Initialize Post Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    break;
                case "btnPrint":
                    if (poAppController.getMaster().getTransactionStatus().equalsIgnoreCase(CheckTransferStatus.OPEN)) {
                        if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to close the transaction ?") == true) {
                            if (!isJSONSuccess(poAppController.CloseTransaction(),
                                    "Initialize Close Transaction")) {
                                return;
                            }
                        }
                    }
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Stock Request Approval", "");
                        return;
                    }
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to print the transaction ?") == true) {
                        if (!isJSONSuccess(poAppController.printDepositSlip(),
                                "Initialize Print Transaction")) {
                            return;
                        }
                    }
                    getLoadedTransaction();
                    
                    pnEditMode = poAppController.getEditMode();
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
            
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            e.printStackTrace();
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
                            case "tfSearchTransNo":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }
                                
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchBankAccountNo":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchBankAccountNo.getText(), true, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "dpSearchTransactionDate":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(String.valueOf(dpSearchTransactionDate.getValue()), false, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            
                        }
                        break;
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }
    
    private void loadTransactionMaster() {
        try {
            lblSource.setText(poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription());
            lblStatus.setText(CheckDepositStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : CheckDepositStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));
            
            tfTransactionNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            dpTransactionReferDate.setValue(ParseDate(poAppController.getMaster().getTransactionReferDate()));
            tfBankMaster.setText(poAppController.getMaster().BankAccount().Banks().getBankName());
            tfBankAccountNo.setText(poAppController.getMaster().BankAccount().getAccountNo());
            tfBankAccountName.setText(poAppController.getMaster().BankAccount().getAccountName());
            taRemarks.setText(String.valueOf(poAppController.getMaster().getRemarks()));
            tfTotal.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotalDeposit(), "###,##0.0000"));
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }
    
    private void loadSelectedTransactionDetail(int fnrow)
            throws SQLException, GuanzonException, CloneNotSupportedException {

        tfCheckTransNo.setText(poAppController.Detail(pnTransactionDetail).CheckPayment().getTransactionNo() != null
                ? poAppController.Detail(pnTransactionDetail).CheckPayment().getTransactionNo()
                : ""
        );

        tfBank.setText( poAppController.Detail(pnTransactionDetail).CheckPayment().Banks() != null
                ? poAppController.Detail(pnTransactionDetail).CheckPayment().Banks().getBankName()
                : ""
        );

        tfPayee.setText( poAppController.Detail(pnTransactionDetail).CheckPayment().Payee() != null
                ? poAppController.Detail(pnTransactionDetail).CheckPayment().Payee().getPayeeName()
                : ""
        );

        tfCheckNo.setText( poAppController.Detail(pnTransactionDetail).CheckPayment().getCheckNo() != null
                ? poAppController.Detail(pnTransactionDetail).CheckPayment().getCheckNo()
                : ""
        );

        tfCheckAmount.setText(
                poAppController.Detail(pnTransactionDetail) != null
                && poAppController.Detail(pnTransactionDetail).CheckPayment() != null
                ? CustomCommonUtil.setIntegerValueToDecimalFormat(
                        poAppController.Detail(pnTransactionDetail).CheckPayment().getAmount(), true)
                : ""
        );

        tfNote.setText(poAppController.Detail(pnTransactionDetail).getRemarks() != null
                ? poAppController.Detail(pnTransactionDetail).getRemarks()
                : ""
        );

        dpCheckDate.setValue( poAppController.Detail(pnTransactionDetail).CheckPayment().getTransactionDate() != null
                ? ParseDate(
                        poAppController.Detail(pnTransactionDetail)
                                .CheckPayment()
                                .getTransactionDate())
                : null
        );

        cbReverse.setSelected(
                poAppController.Detail(pnTransactionDetail) != null
                && poAppController.Detail(pnTransactionDetail).isReverse()
        );

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
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());
        
    }
    
    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnBrowse", "btnPost", "btnPrint", "btnClose");
        
        apMaster.setDisable(!lbShow);
        apDetail.setDisable(!lbShow);
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
    
    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();
            
            tblViewDetails.setItems(laTransactionDetail);
            
            tblColDetailCheckAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            
            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });
            
            tblColDetailReference.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getTransactionNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            
            tblColDetailPayee.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Payee().Client().getCompanyName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            
            tblColDetailBank.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Banks().getBankName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            tblColDetailDate.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getCheckDate()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            
            tblColDetailCheckNo.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getCheckNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            
            tblColDetailCheckAmount.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().CheckPayment().getAmount(), "###,##0.0000"));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
        }
    }
    
    private void reloadTableDetail() {
        try {
            List<Model_Check_Deposit_Detail> rawDetail = poAppController.getDetailList();
            List<Model_Check_Deposit_Detail> displayList = new ArrayList<>();

            boolean hasEmptyRow = false; // track if we already added a new empty row

            for (Model_Check_Deposit_Detail detail : rawDetail) {
                boolean isEmptyRow = (detail.getSourceNo() == null || detail.getSourceNo().trim().isEmpty());

                if (isEmptyRow) {
                    // Add only one empty row for new transaction
                    if (!hasEmptyRow) {
                        displayList.add(detail);
                        hasEmptyRow = true;
                    }
                } else if (detail.isReverse()) {
                    // Add existing rows where cReverse = "+"
                    displayList.add(detail);
                }
            }
            laTransactionDetail.setAll(displayList);
            tblViewDetails.getSelectionModel().select(pnTransactionDetail);

            tblViewDetails.refresh();
            poJSON = poAppController.computeMasterFields();
            if ("success".equals((String) poJSON.get("result"))) {
                tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poAppController.getMaster().getTransactionTotalDeposit(), true));
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnTransactionDetail);
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
