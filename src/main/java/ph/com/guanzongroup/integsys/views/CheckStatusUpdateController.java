package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckStatusUpdateController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private final String pxeModuleName = "Check Status Update";
    private CheckStatusUpdate poCheckStatusUpdateController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psTransactionNo = "";
    private String psOldDate = "";

    private String psSearchBankName = "";
    private String psSearchBankAccount = "";
    private String psSearchCheckNo = "";

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;

    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;

    ObservableList<String> cCheckState = FXCollections.observableArrayList("OPEN", "CLEAR", "CANCELLATION", "STALE", "HOLD",
            "BOUNCED / DISCHONORED");
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount, tfSearchCheckno;
    @FXML
    private Label lblSource;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnRetrieve, btnHistory, btnClose;
    @FXML
    private DatePicker dpTransactionDate, dpCheckDate, dpClearDate, dpHoldUntil;
    @FXML
    private TextField tfTransactionNo, tfBankName, tfBankAccount, tfCheckAmount, tfPayeeName, tfCheckNo, tfVoucherNo;
    @FXML
    private Label lblRemarks;
    @FXML
    private TextArea taRemarks;
    @FXML
    private ComboBox<String> cmbCheckState;
    @FXML
    private Label lblHoldUntil, lblClearingDate;
    @FXML
    private TableView tblVwMain;
    @FXML
    private TableColumn tblRowNo, tblBankName, tblBankAccount, tblCheckNo, tblReferenceNo;
    @FXML
    private RowConstraints row09;
    @FXML
    private Pagination pagination;

    @Override
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poCheckStatusUpdateController = new CashflowControllers(oApp, null).CheckStatusUpdate();
            poCheckStatusUpdateController.setWithUI(true);
            poJSON = poCheckStatusUpdateController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                poCheckStatusUpdateController.setIndustryID(psIndustryId);
                poCheckStatusUpdateController.Master().setIndustryID(psIndustryId);
                poCheckStatusUpdateController.Master().setCompanyID(psCompanyId);
                poCheckStatusUpdateController.setCompanyID(psCompanyId);
                poCheckStatusUpdateController.CheckPayments().getModel().setIndustryID(psIndustryId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poCheckStatusUpdateController.Master().Company().getCompanyName() + " - " + poCheckStatusUpdateController.Master().Industry().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initAll() {
        initButtonsClickActions();
        initTextAreaFields();
        initComboBox();
        initDatePicker();
        initTableMain();
        initTableOnClick();
        initTextFields();
        clearFields();
        initTextFieldsProperty();
        pnEditMode = EditMode.UNKNOWN;
        initFields(pnEditMode);
        initButton(pnEditMode);
        pagination.setPageCount(0);
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnRetrieve, btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poCheckStatusUpdateController.OpenTransaction(poCheckStatusUpdateController.Master().getTransactionNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    poJSON = poCheckStatusUpdateController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    poJSON = poCheckStatusUpdateController.setCheckpayment();
                    if ("error".equals((String) poJSON.get("message"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    pnEditMode = poCheckStatusUpdateController.getEditMode();
                    pagination.toFront();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblVwMain, "#A7C7E7", highlightedRowsMain);
                        break;
                    } else {
                        return;
                    }
                case "btnHistory":
                    ShowMessageFX.Warning("Button History is Underdevelopment.", pxeModuleName, null);
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    if (!isSavingValid()) {
                        return;
                    }
                    if (pnEditMode == EditMode.UPDATE) {
                        poCheckStatusUpdateController.Master().setModifiedDate(oApp.getServerDate());
                        poCheckStatusUpdateController.Master().setModifyingId(oApp.getUserID());
                    }
                    switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
                        case CheckStatus.CANCELLED:
                        case CheckStatus.STALED:
                        case CheckStatus.BOUNCED:
                            poJSON = poCheckStatusUpdateController.ReturnTransaction("");
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            JFXUtil.disableAllHighlightByColor(tblVwMain, "#A7C7E7", highlightedRowsMain);
                            JFXUtil.highlightByKey(tblVwMain, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            break;
                        default:
                            poJSON = poCheckStatusUpdateController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                    pnEditMode = EditMode.UNKNOWN;
                    JFXUtil.disableAllHighlightByColor(tblVwMain, "#A7C7E7", highlightedRowsMain);
                    pagination.toBack();
                    break;
                case "btnRetrieve":
                    loadTableMain();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
            if (lsButton.equals("btnSave") || lsButton.equals("btnCancel")) {
                clearFields();
                pnEditMode = EditMode.UNKNOWN;
            }
            initFields(pnEditMode);
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ScriptException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isSavingValid() {
//        if (cmbCheckState.getSelectionModel().getSelectedIndex() == 0) {
//            ShowMessageFX.Warning("", pxeModuleName, null);
//            return false;
//        }

        switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
            case CheckStatus.CANCELLED:
            case CheckStatus.BOUNCED:
            case CheckStatus.STOP_PAYMENT:
                if (taRemarks.getText().trim().isEmpty()) {
                    ShowMessageFX.Warning("Please enter remarks", pxeModuleName, null);
                    return false;
                }
                break;
        }
        return true;
    }

    private void initTextFields() {
        //Initialise  TextField KeyPressed
        List<TextField> loTxtFieldKeyPressed = Arrays.asList(tfSearchBankName, tfSearchBankAccount, tfSearchCheckno);
        loTxtFieldKeyPressed.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();

        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        CommonUtils.SetNextFocus(txtField);
                        switch (lsID) {
                            case "tfSearchCheckno":
                                psSearchCheckNo = tfSearchCheckno.getText();
                                loadTableMain();
                                event.consume();
                                break;
                        }
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poJSON = poCheckStatusUpdateController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankName.setText(poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() : "");
                                psSearchBankName = poCheckStatusUpdateController.CheckPayments().getModel().getBankID();
                                loadTableMain();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poCheckStatusUpdateController.SearhBankAccount(lsValue, poCheckStatusUpdateController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankAccount.setText(poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
                                psSearchBankAccount = poCheckStatusUpdateController.CheckPayments().getModel().getBankAcountID();
                                loadTableMain();
                                break;

                        }
                        CommonUtils.SetNextFocus((TextField) event.getSource());
                        event.consume();
                        break;
                    default:
                        break;

                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTextAreaFields() {
        //Initialise  TextArea Focus
        taRemarks.focusedProperty().addListener(txtArea_Focus);

        //Initialise  TextArea KeyPressed
        taRemarks.setOnKeyPressed(event -> txtArea_KeyPressed(event));
    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtArea.getId());
        String lsValue = txtArea.getText();

        lastFocusedTextField = txtArea;
        previousSearchedTextField = null;

        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            switch (lsID) {
                case "taRemarks":
                    poCheckStatusUpdateController.CheckPayments().getModel().setRemarks(lsValue);
                    break;
            }
        } else {
            txtArea.selectAll();
        }
    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea txtArea = (TextArea) event.getSource();
        String lsID = txtArea.getId();
        if ("taDVRemarks".equals(lsID) && "taJournalRemarks".equals(lsID)) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case DOWN:
                    CommonUtils.SetNextFocus(txtArea);
                    event.consume();
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtArea);
                    event.consume();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadRecordMaster() {
        try {
            tfTransactionNo.setText(poCheckStatusUpdateController.Master().getTransactionNo());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfVoucherNo.setText(poCheckStatusUpdateController.Master().getVoucherNo());
            poJSON = poCheckStatusUpdateController.setCheckpayment();
            if ("error".equals((String) poJSON.get("message"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                return;
            }
            loadRecordMasterCheck();
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterCheck() {
        try {
            tfBankName.setText(poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccount.setText(poCheckStatusUpdateController.CheckPayments().getModel().getBankAcountID() != null ? poCheckStatusUpdateController.CheckPayments().getModel().getBankAcountID() : "");
            tfPayeeName.setText(poCheckStatusUpdateController.Master().CheckPayments().Payee().getPayeeName() != null ? poCheckStatusUpdateController.Master().CheckPayments().Payee().getPayeeName() : "");
            tfCheckNo.setText(poCheckStatusUpdateController.Master().CheckPayments().getCheckNo());
            dpCheckDate.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckStatusUpdateController.CheckPayments().getModel().getAmount(), true));
            int selectedItem = -1;
            switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
                case "1": //OPEN
                    selectedItem = 0;
                    break;
                case "2": //CLEAR
                    selectedItem = 1;
                    break;
                case "3"://CANCELLATION
                    selectedItem = 2;
                    break;
                case "4": // STALE
                    selectedItem = 3;
                    break;
                case "5": //HOLD
                    selectedItem = 4;
                    break;
                case "6": //BOUNCED / DISCHONORED
                    selectedItem = 5;
                    break;
            }
            cmbCheckState.getSelectionModel().select(selectedItem);
            switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
                case CheckStatus.POSTED:
//                    dpClearDate.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate() != null
//                            ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate(), SQLUtil.FORMAT_SHORT_DATE))
//                            : null);
                    break;
                case CheckStatus.STOP_PAYMENT:
//                    dpHoldUntil.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate() != null
//                            ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate(), SQLUtil.FORMAT_SHORT_DATE))
//                            : null);
                    break;
            }
            taRemarks.setText(poCheckStatusUpdateController.CheckPayments().getModel().getRemarks());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComboBox() {
        cmbCheckState.setItems(cCheckState);
        cmbCheckState.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE && cmbCheckState.getSelectionModel().getSelectedIndex() >= 0) {
                String selectedItem = null;
                switch (cmbCheckState.getSelectionModel().getSelectedItem()) {
                    case "OPEN":
                        selectedItem = "1";
                        break;
                    case "CLEAR":
                        selectedItem = "2";
                        break;
                    case "CANCELLATION":
                        selectedItem = "3";
                        break;
                    case "STALE":
                        selectedItem = "4";
                        break;
                    case "HOLD":
                        selectedItem = "5";
                        break;
                    case "BOUNCED / DISCHONORED":
                        selectedItem = "6";
                        break;
                }
                poCheckStatusUpdateController.CheckPayments().getModel().setTransactionStatus(String.valueOf(selectedItem));
            }
            initFields(pnEditMode);
        });
    }

    private void initDatePicker() {
        dpCheckDate.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE) {
                LocalDate selectedLocalDate = dpCheckDate.getValue();
                LocalDate transactionDate = new java.sql.Date(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate().getTime()).toLocalDate();
                if (selectedLocalDate == null) {
                    return;
                }

                LocalDate dateNow = LocalDate.now();
                psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                boolean approved = true;
                if (pnEditMode == EditMode.UPDATE) {
                    if (!DisbursementStatic.VERIFIED.equals(poCheckStatusUpdateController.Master().getTransactionStatus())) {
                        psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                        if (selectedLocalDate.isBefore(dateNow)) {
                            ShowMessageFX.Warning("Invalid to back date.", pxeModuleName, null);
                            approved = false;
                        }
                    }
                }
                if (pnEditMode == EditMode.ADDNEW) {
                    if (selectedLocalDate.isBefore(dateNow)) {
                        ShowMessageFX.Warning("Invalid to back date.", pxeModuleName, null);
                        approved = false;
                    }
                }
                if (approved) {
                    poCheckStatusUpdateController.CheckPayments().getModel().setCheckDate(
                            SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                } else {
                    if (pnEditMode == EditMode.ADDNEW) {
                        dpCheckDate.setValue(dateNow);
                        poCheckStatusUpdateController.CheckPayments().getModel().setCheckDate(
                                SQLUtil.toDate(dateNow.toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } else if (pnEditMode == EditMode.UPDATE) {
                        poCheckStatusUpdateController.CheckPayments().getModel().setCheckDate(
                                SQLUtil.toDate(psOldDate, SQLUtil.FORMAT_SHORT_DATE));
                    }
                }
                dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                        SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE)));
            }
        }
        );
    }

    private void loadTableMain() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblVwMain.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
                Platform.runLater(() -> {
                    try {
                        main_data.clear();
                        plOrderNoFinal.clear();
                        plOrderNoPartial.clear();
                        poJSON = poCheckStatusUpdateController.getDisbursement(psSearchBankName, psSearchBankAccount, psSearchCheckNo);
                        if ("success".equals(poJSON.get("result"))) {
                            if (poCheckStatusUpdateController.getDisbursementMasterCount() > 0) {
                                for (int lnCntr = 0; lnCntr <= poCheckStatusUpdateController.getDisbursementMasterCount() - 1; lnCntr++) {
                                    main_data.add(new ModelDisbursementVoucher_Main(
                                            String.valueOf(lnCntr + 1),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().getCheckNo(),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).getTransactionNo()
                                    ));
                                    if (poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().getTransactionStatus().equals(CheckStatus.POSTED)) {
                                        plOrderNoPartial.add(new Pair<>(String.valueOf(lnCntr + 1), "1"));
                                    }
                                }
                            } else {
                                main_data.clear();
                                filteredMain_Data.clear();
                            }
                        }
                        showRetainedHighlight(true);
                        if (main_data.isEmpty()) {
                            tblVwMain.setPlaceholder(loading.placeholderLabel);
                        }
                        JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblVwMain, filteredMain_Data);
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(CheckStatusUpdateController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
                );
                return null;
            }

            @Override
            protected void succeeded() {
                btnRetrieve.setDisable(false);
                if (main_data == null || main_data.isEmpty()) {
                    tblVwMain.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblVwMain.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblVwMain.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

            }
        };
        new Thread(task).start(); // Run task in background
    }

    private void initTableMain() {
        JFXUtil.setColumnCenter(tblRowNo, tblBankName, tblBankAccount, tblCheckNo, tblReferenceNo);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwMain);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblVwMain.setItems(filteredMain_Data);
    }

    private void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {

                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlightByColor(tblVwMain, "#C1E1C1", highlightedRowsMain);
        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwMain, pair.getKey(), "#C1E1C1", highlightedRowsMain);
            }
        }
    }

    private void initTableOnClick() {
        tblVwMain.setOnMouseClicked(event -> {
            if (pnEditMode != EditMode.UPDATE) {
                pnMain = tblVwMain.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    if (event.getClickCount() == 2) {
                        loadTableRecordFromMain();
                    }
                }
            }
        });

        tblVwMain.setRowFactory(tv -> new TableRow<ModelDisbursementVoucher_Main>() {
            @Override
            protected void updateItem(ModelDisbursementVoucher_Main item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String key = item.getIndex01();
                    if (highlightedRowsMain.containsKey(key)) {
                        List<String> colors = highlightedRowsMain.get(key);
                        if (!colors.isEmpty()) {
                            setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";"); // Apply latest color
                        }
                    } else {
                        setStyle(""); // Default style
                    }
                }
            }
        }
        );

        JFXUtil.adjustColumnForScrollbar(tblVwMain);
    }

    private void loadTableRecordFromMain() {
        poJSON = new JSONObject();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblVwMain.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                String lsTransactionNo = selected.getIndex05();
                clearFields();
                poJSON = poCheckStatusUpdateController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                JFXUtil.disableAllHighlightByColor(tblVwMain, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblVwMain, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                pnEditMode = poCheckStatusUpdateController.getEditMode();
                loadRecordMaster();
                initFields(pnEditMode);
                initButton(pnEditMode);
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(CheckStatusUpdateController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearFields() {
        previousSearchedTextField = null;
        lastFocusedTextField = null;
        JFXUtil.setValueToNull(null, dpTransactionDate, dpCheckDate, dpClearDate, dpHoldUntil, cmbCheckState);
        JFXUtil.clearTextFields(apMaster);
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apMaster);
        CustomCommonUtil.setVisible(false, dpClearDate, lblClearingDate, lblHoldUntil, dpHoldUntil, lblRemarks, taRemarks);
        CustomCommonUtil.setManaged(false, dpClearDate, lblClearingDate, lblHoldUntil, dpHoldUntil, lblRemarks, taRemarks);
        switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
            case CheckStatus.POSTED:
                row09.setPrefHeight(30);
                row09.setMaxHeight(30);
                CustomCommonUtil.setVisible(true, dpClearDate, lblClearingDate);
                CustomCommonUtil.setManaged(true, dpClearDate, lblClearingDate);
                CustomCommonUtil.setDisable(!lbShow, dpClearDate);
                dpClearDate.setValue(LocalDate.now());
                break;
            case CheckStatus.CANCELLED:
            case CheckStatus.BOUNCED:
                row09.setPrefHeight(5);
                row09.setMaxHeight(5);
                CustomCommonUtil.setVisible(true, taRemarks, lblRemarks);
                CustomCommonUtil.setManaged(true, taRemarks, lblRemarks);
                break;
            case CheckStatus.STOP_PAYMENT:
                row09.setPrefHeight(30);
                row09.setMaxHeight(30);
                CustomCommonUtil.setVisible(true, dpHoldUntil, lblHoldUntil, taRemarks, lblRemarks);
                CustomCommonUtil.setManaged(true, dpHoldUntil, lblHoldUntil, taRemarks, lblRemarks);
                dpHoldUntil.setValue(LocalDate.now());
                CustomCommonUtil.setDisable(!lbShow, dpHoldUntil);
                break;
            default:
                row09.setPrefHeight(30);
                row09.setMaxHeight(30);
                CustomCommonUtil.setVisible(false, dpClearDate, lblClearingDate, lblHoldUntil, dpHoldUntil, lblRemarks, taRemarks);
                CustomCommonUtil.setManaged(false, dpClearDate, lblClearingDate, lblHoldUntil, dpHoldUntil, lblRemarks, taRemarks);
                break;
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate);
        JFXUtil.setButtonsVisibility(fnEditMode != EditMode.UNKNOWN, btnHistory);

        if (fnEditMode == EditMode.READY) {
            switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
                case CheckStatus.FLOAT:
                case CheckStatus.OPEN:
                case CheckStatus.STOP_PAYMENT:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    break;
            }
        }
    }

    private void initTextFieldsProperty() {
        tfSearchBankName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    tfSearchBankName.setText("");
                    psSearchBankName = "";
                    tfSearchBankAccount.setText("");
                    psSearchBankAccount = "";
                    loadTableMain();
                }
            }
        }
        );
        tfSearchBankAccount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    tfSearchBankAccount.setText("");
                    psSearchBankAccount = "";
                    loadTableMain();
                }
            }
        }
        );
        tfSearchCheckno.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    tfSearchCheckno.setText("");
                    psSearchCheckNo = "";
                    loadTableMain();
                }
            }
        }
        );

    }
}
