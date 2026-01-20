package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class OtherPaymentStatusController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private final String pxeModuleName = "Check Status Update";
    private CheckStatusUpdate poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankName = "";
    private String psSearchBankAccount = "";
    private String psSearchCheckNo = "";

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    ObservableList<String> cCheckState = FXCollections.observableArrayList("OPEN", "CLEAR", "CANCELLATION", "STALE", "HOLD",
            "BOUNCED / DISCHONORED");

    JFXUtil.ReloadableTableTask loadTableMain;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount, tfSearchDVNo, tfSearchIndustry, tfBankName, tfBankAccount, tfTransactionNo, tfCheckNo, tfPayeeName, tfCheckAmount, tfVoucherNo;
    @FXML
    private Label lblSource;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnRetrieve, btnHistory, btnClose;
    @FXML
    private DatePicker dpTransactionDate, dpCheckDate;
    @FXML
    private ComboBox cmbPaymentStatus;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblBankName, tblBankAccount, tblCheckNo, tblReferenceNo;
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
            poController = new CashflowControllers(oApp, null).CheckStatusUpdate();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initMainGrid();
            initComboBoxes();
            initDatePicker();
            initTextFields();
            initLoadTable();
            initTableOnClick();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            pagination.setPageCount(0);
            Platform.runLater(() -> {
                poController.setIndustryID(psIndustryId);
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
                poController.setCompanyID(psCompanyId);
                poController.CheckPayments().getModel().setIndustryID(psIndustryId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    poJSON = poController.setCheckpayment();
                    if ("error".equals((String) poJSON.get("message"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
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
//                    if (!isSavingValid()) {
//                        return;
//                    }
                    if (pnEditMode == EditMode.UPDATE) {
                        poController.Master().setModifiedDate(oApp.getServerDate());
                        poController.Master().setModifyingId(oApp.getUserID());
                    }
                    switch (poController.CheckPayments().getModel().getTransactionStatus()) {
                        case CheckStatus.CANCELLED:
                        case CheckStatus.STALED:
                        case CheckStatus.BOUNCED:
                            poJSON = poController.ReturnTransaction("");
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            break;
                        default:
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            break;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                    pnEditMode = EditMode.UNKNOWN;
                    JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                    break;
                case "btnRetrieve":
                    loadTableMain.reload();
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
                clearTextFields();
                pnEditMode = EditMode.UNKNOWN;
            }
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpCheckDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpCheckDate);
    }

    private void initTextFields() {
        //Initialise  TextField KeyPressed
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchBankName, tfSearchBankAccount, tfSearchDVNo, tfSearchIndustry);
        JFXUtil.setFocusListener(txtMaster_Focus, tfCheckAmount);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfSearchBankName":
                            poJSON = poController.SearchBanks(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            break;
                        case "tfSearchBankAccount":
                            poJSON = poController.SearhBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                            break;
                        case "tfSearchDVNo":
                            break;
                        case "tfSearchIndustry":
                            break;
                    }
                    loadRecordSearch();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfCheckAmount":
                        break;
                }
            });

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbPaymentStatus":
                        poController.CheckPayments().getModel().setTransactionStatus(String.valueOf(selectedValue));
                        break;
                }
                loadRecordMaster();
            });

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
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poJSON = poController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                psSearchBankName = poController.CheckPayments().getModel().getBankID();
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poController.SearhBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                psSearchBankAccount = poController.CheckPayments().getModel().getBankAcountID();
                                loadRecordSearch();
                                loadTableMain.reload();
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
            Logger.getLogger(OtherPaymentStatusController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");
        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null, transactionDate = null, referenceDate = null, selectedDate = null;
                String lsServerDate = "", lsTransDate = "", lsRefDate = "", lsSelectedDate = "";

                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                lsServerDate = sdfFormat.format(oApp.getServerDate());
                currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpCheckDate":
                        //back date not allowed
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                JFXUtil.setJSONError(poJSON, "Check date cannot be later than the transaction date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.CheckPayments().getModel().setCheckDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
//            tfSearchIndustry.setText(""); poController.getSearchIndustry()
//            tfSearchBankName.setText("");
//            tfSearchBankAccount.setText("");
//            tfSearchDVNo.setText("");
            tfSearchBankName.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfSearchBankAccount.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void loadRecordMaster() {
        try {
            poJSON = poController.setCheckpayment();
            if ("error".equals((String) poJSON.get("message"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                return;
            }
            tfTransactionNo.setText(poController.Master().getTransactionNo());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));

            tfBankName.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccount.setText(poController.CheckPayments().getModel().getBankAcountID() != null ? poController.CheckPayments().getModel().getBankAcountID() : "");
            tfPayeeName.setText(poController.Master().CheckPayments().Payee().getPayeeName() != null ? poController.Master().CheckPayments().Payee().getPayeeName() : "");
            JFXUtil.setCmbValue(cmbPaymentStatus, Integer.parseInt(poController.CheckPayments().getModel().getTransactionStatus()));
            tfVoucherNo.setText(poController.Master().getVoucherNo());
            tfCheckNo.setText(poController.Master().CheckPayments().getCheckNo());
            dpCheckDate.setValue(poController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.CheckPayments().getModel().getAmount(), true));

        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cCheckState, cmbPaymentStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentStatus);
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            try {
                                main_data.clear();
                                poJSON = poController.getDisbursement(psSearchBankName, psSearchBankAccount, psSearchCheckNo);
                                if ("success".equals(poJSON.get("result"))) {
                                    if (poController.getDisbursementMasterCount() > 0) {
                                        for (int lnCntr = 0; lnCntr <= poController.getDisbursementMasterCount() - 1; lnCntr++) {
                                            main_data.add(new ModelDisbursementVoucher_Main(
                                                    String.valueOf(lnCntr + 1),
                                                    poController.poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                                    poController.poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                                    poController.poDisbursementMaster(lnCntr).CheckPayments().getCheckNo(),
                                                    poController.poDisbursementMaster(lnCntr).getTransactionNo()
                                            ));
                                            if (poController.poDisbursementMaster(lnCntr).CheckPayments().getTransactionStatus().equals(CheckStatus.POSTED)) {
                                                plOrderNoPartial.add(new Pair<>(String.valueOf(lnCntr + 1), "1"));
                                            }
                                        }
                                    } else {
                                        main_data.clear();
                                        filteredMain_Data.clear();
                                    }
                                }
                                if (pnMain < 0 || pnMain
                                        >= main_data.size()) {
                                    if (!main_data.isEmpty()) {
                                        /* FOCUS ON FIRST ROW */
                                        JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                                    }
                                } else {
                                    /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                    JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                                }
                                JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredMain_Data);
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblCheckNo, tblReferenceNo);
        JFXUtil.setColumnLeft(tblBankName, tblBankAccount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (pnEditMode != EditMode.UPDATE) {
                pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    if (event.getClickCount() == 2) {
                        loadTableDetailFromMain();
                    }
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDisbursementVoucher_Main) item).getIndex01(), highlightedRowsMain);
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                String lsTransactionNo = selected.getIndex05();
                clearTextFields();
                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                pnEditMode = poController.getEditMode();
                loadRecordMaster();
                initButton(pnEditMode);
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(OtherPaymentStatusController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate);
        JFXUtil.setButtonsVisibility(fnEditMode != EditMode.UNKNOWN, btnHistory);

        if (fnEditMode == EditMode.READY) {
            switch (poController.CheckPayments().getModel().getTransactionStatus()) {
                case CheckStatus.FLOAT:
                case CheckStatus.OPEN:
                case CheckStatus.STOP_PAYMENT:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    break;
            }
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(null, previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);
    }

}
