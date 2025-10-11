package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
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
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import org.guanzon.appdriver.constant.DisbursementStatic;
import org.guanzon.appdriver.constant.JournalStatus;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DisbursementVoucher_VerificationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON,poJSONVAT;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private int pnDetailDV = 0;
    private int pnDetailJE = 0;

    private boolean lsIsSaved = false;
    private boolean pbIsCheckedJournalTab = false;
    private final String pxeModuleName = "Disbursement Voucher Verification";
    private Disbursement poDisbursementController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchSupplierID = "";
    private String psSearchTransactionNo = "";
    private String psOldDate = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Detail> detailsdv_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Detail> filteredDataDetailDV;

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private FilteredList<ModelJournalEntry_Detail> filteredJournal_Data;

    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;
    private boolean pbEnteredDV = false;
    private boolean pbEnteredJournal = false;

    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;

    ObservableList<String> cPaymentMode = FXCollections.observableArrayList(
            "CHECK", "WIRED", "DIGITAL PAYMENT");
    ObservableList<String> cDisbursementMode = FXCollections.observableArrayList("DELIVER", "PICK-UP");
    ObservableList<String> cPayeeType = FXCollections.observableArrayList("INDIVIDUAL", "CORPORATION");
    ObservableList<String> cClaimantType = FXCollections.observableArrayList("AUTHORIZED REPRESENTATIVE", "PAYEE");
    ObservableList<String> cCheckStatus = FXCollections.observableArrayList("FLOATING", "OPEN",
            "CLEARED  / POSTED", "CANCELLED", "STALED", "HOLD / STOP PAYMENT",
            "BOUNCED / DISCHONORED", "VOID");
    ObservableList<String> cOtherPayment = FXCollections.observableArrayList("FLOATING");
    ObservableList<String> cOtherPaymentBTransfer = FXCollections.observableArrayList("FLOATING");

    private EventHandler<ActionEvent> disbursementModeHandler;
    private EventHandler<ActionEvent> claimantTypeHandler;
    /* DV  & Journal */
    @FXML
    private TextField tfSearchTransaction, tfSearchSupplier;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private AnchorPane AnchorMain, apButton;
    @FXML
    private Tab tabDetails, tabJournal;
    @FXML
    private Label lblSource;
    @FXML
    private AnchorPane apBrowse;

    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnVerify, btnVoid, btnDVCancel, btnRetrieve, btnHistory, btnClose;

    /*DV Master*/
    @FXML
    private AnchorPane apDVMaster1, apDVMaster2, apDVMaster3;
    @FXML
    private TabPane tabPanePaymentMode;
    @FXML
    private TextField tfDVTransactionNo, tfSupplier, tfVoucherNo;
    @FXML
    private ComboBox<String> cmbPaymentMode;
    @FXML
    private TableView tblVwDisbursementVoucher;
    @FXML
    private TableColumn tblRowNo, tblSupplier, tblPaymentForm, tblTransDate, tblReferNo;
    @FXML
    private Pagination pagination;

    /*DV Master Payment Mode Tabs */
    @FXML
    private Tab tabCheck, tabBankTransfer, tabOnlinePayment;

    @FXML
    private DatePicker dpDVTransactionDate;
    @FXML
    private Label lblDVTransactionStatus;
    @FXML
    private TextField tfVatableSales, tfVatRate, tfVatAmountMaster, tfVatZeroRatedSales, tfVatExemptSales, tfTotalAmount, tfLessWHTax, tfTotalNetAmount;
    @FXML
    private TextArea taDVRemarks;

    /*DV Master Payment Mode Tabs */
 /*DV Master Payment Mode Tabs  = Check*/
    @FXML
    private AnchorPane apMasterDVCheck;
    @FXML
    private TextField tfPayeeName, tfCheckNo, tfCheckAmount, tfBankNameCheck, tfBankAccountCheck;
    @FXML
    private DatePicker dpCheckDate;
    @FXML
    private CheckBox chbkPrintByBank;
    @FXML
    private ComboBox<String> cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus;
    @FXML
    private TextField tfAuthorizedPerson;
    @FXML
    private CheckBox chbkIsCrossCheck, chbkIsPersonOnly;

    /*DV Master Payment Mode Tabs  = Bank Transfer /Other Payment*/
    @FXML
    private AnchorPane apMasterDVBTransfer;
    @FXML
    private TextField tfPaymentAmountBTransfer, tfSupplierBank, tfSupplierAccountNoBTransfer, tfBankTransReferNo, tfBankNameBTransfer, tfBankAccountBTransfer;
    @FXML
    private ComboBox<String> cmbOtherPaymentBTransfer;

    /*DV Master Payment Mode Tabs  = Online Payment/Other Payment*/
    @FXML
    private AnchorPane apMasterDVOp;
    @FXML
    private TextField tfPaymentAmount, tfSupplierServiceName, tfSupplierAccountNo, tfPaymentReferenceNo, tfBankNameOnlinePayment, tfBankAccountOnlinePayment;
    @FXML
    private ComboBox<String> cmbOtherPayment;

    /*DV Detail*/
    @FXML
    private AnchorPane apDVDetail;
    @FXML
    private TextField tfRefNoDetail, tfParticularsDetail, tfPurchasedAmountDetail, tfTaxCodeDetail, tfTaxRateDetail, tfTaxAmountDetail, tfNetAmountDetail,
            tfVatableSalesDetail, tfPartialPayment, tfVatAmountDetail, tfVatRateDetail, tfVatZeroRatedSalesDetail, tfVatExemptDetail;
    @FXML
    private CheckBox chbkVatClassification;
    @FXML
    private TableView tblVwDetails;
    @FXML
    private TableColumn tblDVRowNo, tblReferenceNo, tblAccountCode, tblTransactionTypeDetail, tblParticulars, tblPurchasedAmount, tblTaxCode, tblTaxAmount, tblNetAmount,
            tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales;
    /*Journal Master */
    @FXML
    private AnchorPane apJournalMaster, apJournalDetails;
    @FXML
    private TextField tfJournalTransactionNo, tfAccountCode, tfAccountDescription, tfCreditAmount, tfDebitAmount, tfTotalDebitAmount, tfTotalCreditAmount;
    @FXML
    private DatePicker dpJournalTransactionDate, dpReportMonthYear;
    @FXML
    private TextArea taJournalRemarks;
    @FXML
    private Label lblJournalTransactionStatus;
    @FXML
    private TableView tblVwJournalDetails;
    @FXML
    private Label txtAccountCode;
    @FXML
    private TableColumn tblJournalRowNo, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblJournalReportMonthYear;

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
            txtAccountCode.setVisible(false);
//            tfAccountCodeDetail.setVisible(false);
            poDisbursementController = new CashflowControllers(oApp, null).Disbursement();
            poDisbursementController.setTransactionStatus(DisbursementStatic.OPEN + DisbursementStatic.VERIFIED + DisbursementStatic.RETURNED);
            poJSON = new JSONObject();
            poDisbursementController.setWithUI(true);
            poJSON = poDisbursementController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                poDisbursementController.Master().setIndustryID(psIndustryId);
                poDisbursementController.Master().setCompanyID(psCompanyId);
                poDisbursementController.setCategoryCd(psCategoryId);
                poDisbursementController.setIndustryID(psIndustryId);
                poDisbursementController.setCompanyID(psCompanyId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        initButtonsClickActions();
        initTextFields();
        initTextAreaFields();
        initComboBox();
        initCheckBox();
        initDatePicker();
        initTableDetailDV();
        initTableMain();
        initTableDetailJE();
        initTableOnClick();
        initTextFieldsProperty();
        initTabSelection();
        clearFields();
        pnEditMode = EditMode.UNKNOWN;
        initFields(pnEditMode);
        initButton(pnEditMode);
        pagination.setPageCount(0);
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poDisbursementController.Master().Company().getCompanyName() + " - " + poDisbursementController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void populateJE() {
        try {
            poJSON = new JSONObject();
            JFXUtil.setValueToNull(dpJournalTransactionDate, dpReportMonthYear);
            JFXUtil.clearTextFields(apJournalMaster, apJournalDetails);
            poJSON = poDisbursementController.populateJournal();
            if (JFXUtil.isJSONSuccess(poJSON)) {
                loadTableDetailJE();
            } else {
                journal_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVerify, btnVoid, btnDVCancel, btnRetrieve, btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poDisbursementController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    pbIsCheckedJournalTab = false;
                    pnEditMode = poDisbursementController.getEditMode();
                    CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                    loadTableDetailDV();
                    pagination.toFront();
                    break;
                case "btnSearch":
                    String lsMessage = "Focus a searchable textfield to search";
                    if ((lastFocusedTextField != null)) {
                        if (lastFocusedTextField instanceof TextField) {
                            TextField tf = (TextField) lastFocusedTextField;
                            if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", apDVDetail, apDVDetail, apDVMaster2, apMasterDVCheck, apMasterDVOp, apDVDetail).contains(tf.getId())) {
                                if (lastFocusedTextField == previousSearchedTextField) {
                                    break;
                                }
                                previousSearchedTextField = lastFocusedTextField;
                                KeyEvent keyEvent = new KeyEvent(
                                        KeyEvent.KEY_PRESSED,
                                        "",
                                        "",
                                        KeyCode.F3,
                                        false, false, false, false
                                );
                                tf.fireEvent(keyEvent);
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                            }
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                        }
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                    }
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    if (!isSavingValid()) {
                        return;
                    }
                    poJSON = poDisbursementController.validateTAXandVat();
                    if("error".equals((String)poJSON.get("result"))){
                        ShowMessageFX.Information((String)poJSON.get("message"), pxeModuleName, null);
                        pnDetailDV = (int) poJSON.get("pnDetailDV");
                        JFXUtil.selectAndFocusRow(tblVwDetails, pnDetailDV);
                        loadRecordDetailDV();
                        return;
                        
                    }
               
                    if (pnEditMode == EditMode.UPDATE) {
                        if (!pbIsCheckedJournalTab) {
                            ShowMessageFX.Warning("Please see the Journal Entry, before save", pxeModuleName, null);
                            return;
                        }
                        poDisbursementController.Master().setModifiedDate(oApp.getServerDate());
                        poDisbursementController.Master().setModifyingId(oApp.getUserID());
                    }
                    poJSON = poDisbursementController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to verify this transaction?")) {
                        poJSON = poDisbursementController.OpenTransaction(poDisbursementController.Master().getTransactionNo());
                        if ("success".equals(poJSON.get("result"))) {
                            poJSON = poDisbursementController.VerifyTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            } else {
                                ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#A7C7E7", highlightedRowsMain);
                                plOrderNoPartial.add(new Pair<>(poDisbursementController.Master().getTransactionNo(), "1"));
                                showRetainedHighlight(true);
                            }
                        }
                    }
                    pnEditMode = poDisbursementController.getEditMode();
                    loadTableDetailDV();
                    loadRecordDetailDV();
                    loadTableMain();
                    pagination.toBack();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#A7C7E7", highlightedRowsMain);
                        break;
                    } else {
                        return;
                    }
                case "btnHistory":
                    ShowMessageFX.Warning("Button History is Underdevelopment.", pxeModuleName, null);
                    break;
                case "btnRetrieve":
                    loadTableMain();
                    break;
                case "btnVerify":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to verify transaction?")) {
                        pnEditMode = poDisbursementController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (!poDisbursementController.existJournal().equals("")) {
                                if (!pbIsCheckedJournalTab) {
                                    ShowMessageFX.Warning("Please see the Journal Entry, before save", pxeModuleName, null);
                                    return;
                                } else {
                                    poJSON = poDisbursementController.VerifyTransaction("Verified");
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                        return;
                                    } else {
                                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                        JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#A7C7E7", highlightedRowsMain);
                                        plOrderNoPartial.add(new Pair<>(poDisbursementController.Master().getTransactionNo(), "1"));
                                        showRetainedHighlight(true);
                                    }
                                }
                            } else {
                                ShowMessageFX.Warning("No journal entry exist, please add journal entry click edit and click tab journal then save before verify.", pxeModuleName, null);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnDVCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to cancel transaction?")) {
                        pnEditMode = poDisbursementController.getEditMode();
                        poJSON = poDisbursementController.CancelTransaction("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                            pnEditMode = poDisbursementController.getEditMode();
                            JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#C1E1C1", highlightedRowsMain);
                            JFXUtil.highlightByKey(tblVwDisbursementVoucher, poDisbursementController.Master().getTransactionNo(), "#FAA0A0", highlightedRowsMain);
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?")) {
                        pnEditMode = poDisbursementController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (!poDisbursementController.existJournal().equals("")) {
                                poJSON = poDisbursementController.VoidTransaction("");
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                } else {
                                    ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                                    pnEditMode = poDisbursementController.getEditMode();
                                    JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#A7C7E7", highlightedRowsMain);
                                    JFXUtil.highlightByKey(tblVwDisbursementVoucher, poDisbursementController.Master().getTransactionNo(), "#FAA0A0", highlightedRowsMain);
                                }
                            } else {
                                ShowMessageFX.Warning("No journal entry exist, please add journal entry click edit and click tab journal then save before void.", pxeModuleName, null);
                                return;
                            }
                        }
                    } else {
                        return;
                    }
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
            if (lsButton.equals("btnSave") || lsButton.equals("btnVerify") || lsButton.equals("btnVoid") || lsButton.equals("btnCancel") || lsButton.equals("btnDVCancel")) {
                pbIsCheckedJournalTab = false;
                poDisbursementController.resetMaster();
                poDisbursementController.resetOthers();
                poDisbursementController.Detail().clear();
                poDisbursementController.resetJournal();
                pnDetailDV = -1;
                pnDetailJE = -1;
                clearFields();
                detailsdv_data.clear();
                journal_data.clear();
                CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                CustomCommonUtil.switchToTab(tabCheck, tabPanePaymentMode);
                pnEditMode = EditMode.UNKNOWN;
            }
            initFields(pnEditMode);
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isSavingValid() {
        switch (poDisbursementController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                if (tfBankNameCheck.getText().isEmpty()) {
                    ShowMessageFX.Warning("Please enter Bank Name.", pxeModuleName, null);
                    return false;
                }
//                if (tfBankAccountCheck.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Bank Account.", pxeModuleName, null);
//                    return false;
//                }
                if (tfPayeeName.getText().isEmpty()) {
                    ShowMessageFX.Warning("Please enter Payee Name.", pxeModuleName, null);
                    return false;
                }
                if (chbkPrintByBank.isSelected()) {
                    if (cmbPayeeType.getSelectionModel().getSelectedIndex() < 0) {
                        ShowMessageFX.Warning("Please select Payee Type.", pxeModuleName, null);
                        return false;
                    }
                    if (cmbDisbursementMode.getSelectionModel().getSelectedIndex() < 0) {
                        ShowMessageFX.Warning("Please select Disbursement Mode.", pxeModuleName, null);
                        return false;
                    }
                    if (cmbDisbursementMode.getSelectionModel().getSelectedIndex() == 1) {
                        if (cmbClaimantType.getSelectionModel().getSelectedIndex() < 0) {
                            ShowMessageFX.Warning("Please select Claimant Type.", pxeModuleName, null);
                            return false;
                        }
                        if (cmbClaimantType.getSelectionModel().getSelectedIndex() == 0) {
                            if (tfAuthorizedPerson.getText().isEmpty()) {
                                ShowMessageFX.Warning("Please enter Authorized Person.", pxeModuleName, null);
                                return false;
                            }
                        }

                    }

                }

                break;
            case DisbursementStatic.DisbursementType.WIRED:
//                if (tfBankNameBTransfer.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Bank Name.", pxeModuleName, null);
//                    return false;
//                }
//                if (tfBankAccountBTransfer.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Bank Account.", pxeModuleName, null);
//                    return false;
//                }
//                if (tfSupplierBank.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Supplier Bank.", pxeModuleName, null);
//                    return false;
//                }
//                if (tfSupplierAccountNoBTransfer.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Supplier Account No.", pxeModuleName, null);
//                    return false;
//                }
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
//                if (tfBankNameOnlinePayment.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Bank Name.", pxeModuleName, null);
//                    return false;
//                }
//                if (tfBankAccountOnlinePayment.getText().isEmpty()) {
//                    ShowMessageFX.Warning("Please enter Bank Account.", pxeModuleName, null);
//                    return false;
//                }
//                    if (tfSupplierServiceName.getText().isEmpty()) {
//                        ShowMessageFX.Warning("Please enter Supplier Service Name.", pxeModuleName, null);
//                        return false;
//                    }
//                    if (tfSupplierAccountNo.getText().isEmpty()) {
//                        ShowMessageFX.Warning("Please enter Supplier Account No.", pxeModuleName, null);
//                        return false;
//                    }
                break;
        }
        return true;
    }

    private void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {
                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#C1E1C1", highlightedRowsMain);
        JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#FAC898", highlightedRowsMain);
        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if ("1".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwDisbursementVoucher, pair.getKey(), "#C1E1C1", highlightedRowsMain);
            }
            if ("7".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwDisbursementVoucher, pair.getKey(), "#FAC898", highlightedRowsMain);
            }
            if ("0".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwDisbursementVoucher, pair.getKey(), "", highlightedRowsMain);
            }
        }
    }

    private void loadTableMain() {
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblVwDisbursementVoucher.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
                Platform.runLater(() -> {
                    try {
                        main_data.clear();
                        plOrderNoFinal.clear();
                        poJSON = poDisbursementController.getDisbursementForVerification(psSearchTransactionNo, psSearchSupplierID);
                        if ("success".equals(poJSON.get("result"))) {
                            if (poDisbursementController.getDisbursementMasterCount() > 0) {
                                for (int lnCntr = 0; lnCntr <= poDisbursementController.getDisbursementMasterCount() - 1; lnCntr++) {
                                    String lsPaymentForm = "";
                                    switch (poDisbursementController.poDisbursementMaster(lnCntr).getDisbursementType()) {
                                        case DisbursementStatic.DisbursementType.CHECK:
                                            lsPaymentForm = "CHECK";
                                            break;
                                        case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                                            lsPaymentForm = "ONLINE PAYMENT";
                                            break;
                                        case DisbursementStatic.DisbursementType.WIRED:
                                            lsPaymentForm = "BANK TRANSFER";
                                            break;
                                    }
                                    main_data.add(new ModelDisbursementVoucher_Main(
                                            String.valueOf(lnCntr + 1),
                                            poDisbursementController.poDisbursementMaster(lnCntr).Payee().getPayeeName(),
                                            lsPaymentForm,
                                            CustomCommonUtil.formatDateToShortString(poDisbursementController.poDisbursementMaster(lnCntr).getTransactionDate()),
                                            poDisbursementController.poDisbursementMaster(lnCntr).getTransactionNo()
                                    ));
                                    if (poDisbursementController.poDisbursementMaster(lnCntr).getTransactionStatus().equals(DisbursementStatic.VERIFIED)) {
                                        plOrderNoPartial.add(new Pair<>(poDisbursementController.poDisbursementMaster(lnCntr).getTransactionNo(), "1"));
                                    }
                                    if (poDisbursementController.poDisbursementMaster(lnCntr).getTransactionStatus().equals(DisbursementStatic.RETURNED)) {
                                        plOrderNoPartial.add(new Pair<>(poDisbursementController.poDisbursementMaster(lnCntr).getTransactionNo(), "7"));
                                    }
                                }
                            }
                            showRetainedHighlight(true);
                            if (main_data.isEmpty()) {
                                tblVwDisbursementVoucher.setPlaceholder(loading.placeholderLabel);
                            }
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblVwDisbursementVoucher, filteredMain_Data);

                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DisbursementVoucher_VerificationController.class
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
                    tblVwDisbursementVoucher.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblVwDisbursementVoucher.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblVwDisbursementVoucher.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);

            }
        };
        new Thread(task).start(); // Run task in background
    }

    private void initTableMain() {
        JFXUtil.setColumnCenter(tblRowNo, tblSupplier, tblPaymentForm, tblTransDate, tblReferNo);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDisbursementVoucher);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblVwDisbursementVoucher.setItems(filteredMain_Data);
    }

    private void loadRecordMasterDV() {
        try {
            poJSON = new JSONObject();
            tfSupplier.setText(poDisbursementController.Master().Payee().Client().getCompanyName() != null ? poDisbursementController.Master().Payee().Client().getCompanyName() : "");
            tfDVTransactionNo.setText(poDisbursementController.Master().getTransactionNo() != null ? poDisbursementController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poDisbursementController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfVoucherNo.setText(poDisbursementController.Master().getVoucherNo());
            lblDVTransactionStatus.setText(getStatus(poDisbursementController.Master().getTransactionStatus()));
            cmbPaymentMode.getSelectionModel().select(!poDisbursementController.Master().getDisbursementType().equals("") ? Integer.valueOf(poDisbursementController.Master().getDisbursementType()) : -1);
            switch (poDisbursementController.Master().getDisbursementType()) {
                case DisbursementStatic.DisbursementType.CHECK:
                    poJSON = poDisbursementController.setCheckpayment();
                    if ("error".equals((String) poJSON.get("message"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        break;
                    }
                    loadRecordMasterCheck();
                    break;
                case DisbursementStatic.DisbursementType.WIRED:
                    poJSON = poDisbursementController.setCheckpayment();
                    if ("error".equals((String) poJSON.get("message"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        break;
                    }
                    loadRecordMasterBankTransfer();
                    break;
                case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                    poJSON = poDisbursementController.setCheckpayment();
                    if ("error".equals((String) poJSON.get("message"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        break;
                    }
                    loadRecordMasterOnlinePayment();
                    break;
            }
            taDVRemarks.setText(poDisbursementController.Master().getRemarks());
            tfVatableSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getVATSale(), true));
//            tfVatRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getVATRates(), false));
            tfVatAmountMaster.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getVATAmount(), true));
            tfVatZeroRatedSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getZeroVATSales(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getVATExmpt(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getTransactionTotal(), true));
            tfLessWHTax.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getWithTaxTotal(), true));
            tfTotalNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Master().getNetTotal(), true));

        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getStatus(String lsValueStatus) {
        String lsStatus;
        switch (lsValueStatus) {
            case DisbursementStatic.OPEN:
                lsStatus = "OPEN";
                break;
            case DisbursementStatic.VERIFIED:
                lsStatus = "VERIFIED";
                break;
            case DisbursementStatic.CERTIFIED:
                lsStatus = "CERTIFIED";
                break;
            case DisbursementStatic.CANCELLED:
                lsStatus = "CANCELLED";
                break;
            case DisbursementStatic.AUTHORIZED:
                lsStatus = "AUTHORIZED";
                break;
            case DisbursementStatic.VOID:
                lsStatus = "VOID";
                break;
            case DisbursementStatic.DISAPPROVED:
                lsStatus = "DISAPPROVED";
                break;
            case DisbursementStatic.RETURNED:
                lsStatus = "RETURNED";
                break;
            default:
                lsStatus = "UNKNOWN";
                break;
        }
        return lsStatus;
    }

    private void loadRecordMasterCheck() {
        try {
            tfBankNameCheck.setText(poDisbursementController.CheckPayments().getModel().Banks().getBankName() != null ? poDisbursementController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccountCheck.setText(poDisbursementController.CheckPayments().getModel().getBankAcountID() != null ? poDisbursementController.CheckPayments().getModel().getBankAcountID() : "");
            tfPayeeName.setText(poDisbursementController.Master().Payee().getPayeeName() != null ? poDisbursementController.Master().Payee().getPayeeName() : "");
            tfCheckNo.setText(poDisbursementController.CheckPayments().getModel().getCheckNo());
            dpCheckDate.setValue(poDisbursementController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poDisbursementController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.CheckPayments().getModel().getAmount(), true));
            chbkPrintByBank.setSelected(poDisbursementController.Master().getBankPrint().equals(Logical.YES));
            cmbPayeeType.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getPayeeType().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getPayeeType()) : -1);
            cmbDisbursementMode.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getDesbursementMode().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getDesbursementMode()) : -1);
            cmbClaimantType.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getClaimant()) : -1);
            tfAuthorizedPerson.setText(poDisbursementController.CheckPayments().getModel().getAuthorize() != null ? poDisbursementController.CheckPayments().getModel().getAuthorize() : "");
            chbkIsCrossCheck.setSelected(poDisbursementController.CheckPayments().getModel().isCross());
            chbkIsPersonOnly.setSelected(poDisbursementController.CheckPayments().getModel().isPayee());
            cmbCheckStatus.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getTransactionStatus()) : -1);

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterBankTransfer() {
        tfBankNameBTransfer.setText("");
        tfBankAccountBTransfer.setText("");
        tfPaymentAmountBTransfer.setText("");
        tfSupplierBank.setText("");
        tfSupplierAccountNoBTransfer.setText("");
        tfBankTransReferNo.setText("");
        cmbOtherPaymentBTransfer.getSelectionModel().select(null);
    }

    private void loadRecordMasterOnlinePayment() {
        tfPaymentAmount.setText("");
        tfSupplierServiceName.setText("");
        tfSupplierAccountNo.setText("");
        tfPaymentReferenceNo.setText("");
        tfBankNameOnlinePayment.setText("");
        tfBankAccountOnlinePayment.setText("");
        cmbOtherPayment.getSelectionModel().select(null);
    }

    private void loadRecordDetailDV() {
        if (pnDetailDV >= 0) {
            try {
                tfRefNoDetail.setText(poDisbursementController.Detail(pnDetailDV).getSourceNo());
                tfParticularsDetail.setText(poDisbursementController.Detail(pnDetailDV).Particular().getDescription());
//                tfAccountCodeDetail.setText(poDisbursementController.Detail(pnDetailDV).Particular().getAccountCode());
                tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getAmountApplied(), true));
                tfTaxCodeDetail.setText(poDisbursementController.Detail(pnDetailDV).TaxCode().getTaxCode());
                tfTaxRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getTaxRates(), false));
                tfTaxAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getTaxAmount(), true));
                tfNetAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getAmount()
                        - poDisbursementController.Detail(pnDetailDV).getTaxAmount(), true));
                
                tfVatRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getDetailVatRates(), false));
                tfPartialPayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poJSONVAT.get("totalApplied"), true));
                tfVatAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getDetailVatAmount(), true));
                tfVatableSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getDetailVatSales(), true));
                tfVatZeroRatedSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getDetailZeroVat(), true));
                tfVatExemptDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getDetailVatExempt(), true));
                
                    chbkVatClassification.setSelected(poDisbursementController.Detail(pnDetailDV).isWithVat());
                

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(DisbursementVoucher_EntryController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableRecordFromMain() {
        poJSON = new JSONObject();
        pnMain = tblVwDisbursementVoucher.getSelectionModel().getSelectedIndex();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblVwDisbursementVoucher.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                String lsTransactionNo = selected.getIndex05();
                clearFields();
                poJSON = poDisbursementController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                pbIsCheckedJournalTab = false;
                JFXUtil.disableAllHighlightByColor(tblVwDisbursementVoucher, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblVwDisbursementVoucher, lsTransactionNo, "#A7C7E7", highlightedRowsMain);
                pnEditMode = poDisbursementController.getEditMode();
                loadTableDetailDV();
                initFields(pnEditMode);
                initButton(pnEditMode);

            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(DisbursementVoucher_VerificationController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableDetailDV() {
        pbEnteredDV = false;
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblVwDetails.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    detailsdv_data.clear();
                    int lnCtr;

                    double lnNetTotal = 0.0000;
                    for (lnCtr = 0; lnCtr < poDisbursementController.getDetailCount(); lnCtr++) {
                        try {
                            lnNetTotal = poDisbursementController.Detail(lnCtr).getAmount() - poDisbursementController.Detail(lnCtr).getTaxAmount();
                            String lsTransactionType;
                            switch (poDisbursementController.Detail(lnCtr).getSourceCode()) {
                                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                                    lsTransactionType = DisbursementStatic.SourceCode.PAYMENT_REQUEST;
                                    break;
                                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                                    lsTransactionType = DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE;
                                    break;
                                case DisbursementStatic.SourceCode.CASH_PAYABLE:
                                    lsTransactionType = DisbursementStatic.SourceCode.CASH_PAYABLE;
                                    break;
                                default:
                                    lsTransactionType = "";
                                    break;
                            }
                            detailsdv_data.add(
                                    new ModelDisbursementVoucher_Detail(String.valueOf(lnCtr + 1),
                                            poDisbursementController.Detail(lnCtr).getSourceNo(),
                                            poDisbursementController.Detail(lnCtr).Particular().getAccountCode(),
                                            poDisbursementController.Detail(lnCtr).getSourceCode(),
                                            poDisbursementController.Detail(lnCtr).Particular().getDescription(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getAmountApplied(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getDetailVatSales(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getDetailVatAmount(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getDetailVatRates(), false),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getDetailZeroVat(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getDetailVatExempt(), true),
                                            poDisbursementController.Detail(lnCtr).TaxCode().getTaxCode(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getTaxAmount(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(lnNetTotal, true)
                                    ));

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(DisbursementVoucher_VerificationController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (pnDetailDV < 0 || pnDetailDV
                            >= detailsdv_data.size()) {
                        if (!detailsdv_data.isEmpty()) {
                            JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                            pnDetailDV = tblVwDetails.getSelectionModel().getSelectedIndex();
                            loadRecordDetailDV();
                            
                        }
                    } else {
                        try {
                            JFXUtil.selectAndFocusRow(tblVwDetails, pnDetailDV);
                            poJSONVAT = poDisbursementController.validateDetailVATAndTAX(poDisbursementController.Detail(pnDetailDV).getSourceCode(),
                                    poDisbursementController.Detail(pnDetailDV).getSourceNo());
                            poDisbursementController.computeVat(pnDetailDV,
                                    poDisbursementController.Detail(pnDetailDV).getAmountApplied(),
                                    poDisbursementController.Detail(pnDetailDV).getDetailVatRates(),
                               Double.parseDouble(JFXUtil.removeComma(tfPartialPayment.getText())),
                                    true);
                            
                            loadRecordDetailDV();
                            tblVwDetails.refresh();
                        } catch (SQLException ex) {
                            Logger.getLogger(DisbursementVoucher_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                    poJSON = poDisbursementController.computeFields();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    loadRecordMasterDV();
                });
                return null;
            }

            @Override

            protected void succeeded() {
                if (detailsdv_data == null || detailsdv_data.isEmpty()) {
                    tblVwDetails.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblVwDetails.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (detailsdv_data == null || detailsdv_data.isEmpty()) {
                    tblVwDetails.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();

    }

    private void initTableDetailDV() {
        
        tblAccountCode.setVisible(false);
        JFXUtil.setColumnCenter(tblDVRowNo, tblReferenceNo, tblTransactionTypeDetail, tblAccountCode, tblParticulars, tblTaxCode,
                tblVatAmt, tblVatExemptSales, tblVatRate, tblVatZeroRatedSales, tblVatableSales);
        JFXUtil.setColumnRight(tblTaxAmount, tblPurchasedAmount, tblNetAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetails);
        filteredDataDetailDV = new FilteredList<>(detailsdv_data, b -> true);

        SortedList<ModelDisbursementVoucher_Detail> sortedData = new SortedList<>(filteredDataDetailDV);
        sortedData.comparatorProperty().bind(tblVwDetails.comparatorProperty());
        tblVwDetails.setItems(sortedData);
        tblVwDetails.autosize();
    }

    private void initTableOnClick() {
        tblVwDetails.setOnMouseClicked(event -> {
            if (!detailsdv_data.isEmpty() && event.getClickCount() == 1) {
                pnDetailDV = tblVwDetails.getSelectionModel().getSelectedIndex();
                switch (poDisbursementController.Detail(pnDetailDV).getSourceCode()) {
                    case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                        initDetailFocusDV(true);
                        break;
                    case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                        initDetailFocusDV(false);
                        break;
                    case DisbursementStatic.SourceCode.CASH_PAYABLE:
                        initDetailFocusDV(true);
                        break;
                    default:
                        loadRecordDetailDV();
                        initFields(pnEditMode);
                        break;
                }
            }
        });

        tblVwDisbursementVoucher.setOnMouseClicked(event -> {
            if (pnEditMode != EditMode.UPDATE) {
                pnMain = tblVwDisbursementVoucher.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0 && event.getClickCount() == 2) {
                    loadTableRecordFromMain();
                }
            }
        });

        tblVwDisbursementVoucher.setRowFactory(tv -> new TableRow<ModelDisbursementVoucher_Main>() {
            @Override
            protected void updateItem(ModelDisbursementVoucher_Main item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String key = item.getIndex05();
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

        tblVwDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        tblVwJournalDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                pnDetailJE = tblVwJournalDetails.getSelectionModel().getSelectedIndex();
                initDetailFocusJE();
            }
        }
        );
        JFXUtil.adjustColumnForScrollbar(tblVwDisbursementVoucher, tblVwDetails, tblVwJournalDetails);
    }

    private void initTabSelection() {
        tabJournal.setOnSelectionChanged(event -> {
            if (tabJournal.isSelected()) {
                if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE) {
                    if (poDisbursementController.Detail(0).getSourceNo() != null && !poDisbursementController.Detail(0).getSourceNo().isEmpty()) {
                        pbIsCheckedJournalTab = true;
                        pnDetailDV = -1;
                        pnDetailJE = -1;
                        populateJE();
                    } else {
                        CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                        ShowMessageFX.Warning("You need atleast valid disbursement detail before proceed.", pxeModuleName, null);
                    }
                }
            }
        }
        );
        tabDetails.setOnSelectionChanged(event -> {
            if (tabDetails.isSelected()) {
                if (pnEditMode == EditMode.UPDATE) {
                    pnDetailJE = -1;
                }
            }
        }
        );
    }

    private void tableKeyEvents(KeyEvent event) {
        if (!detailsdv_data.isEmpty()) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblVwDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case UP:
                                pnDetailDV = JFXUtil.moveToPreviousRow(currentTable);
                                break;
                            case TAB:
                            case DOWN:
                                pnDetailDV = JFXUtil.moveToNextRow(currentTable);
                                break;
                            default:
                                break;
                        }
                        loadRecordDetailDV();
                        initFields(pnEditMode);
                        event.consume();
                    }
                    break;
                case "tblVwJournalDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case UP:
                                pnDetailJE = JFXUtil.moveToPreviousRow(currentTable);
                                break;
                            case TAB:
                            case DOWN:
                                pnDetailJE = JFXUtil.moveToNextRow(currentTable);
                                break;
                            default:
                                break;
                        }
                        loadRecordDetailJE();
                        initFields(pnEditMode);
                        event.consume();
                    }
                    break;
            }
        }
    }

    private void initTextFields() {
        //Initialise  TextField Focus
        JFXUtil.setFocusListener(txtMasterCheck_Focus, tfAuthorizedPerson);
        JFXUtil.setFocusListener(txtMasterBankTransfer_Focus, tfBankNameBTransfer, tfBankAccountBTransfer, tfSupplierBank, tfSupplierAccountNoBTransfer);
        JFXUtil.setFocusListener(txtMasterOnlinePayment_Focus, tfBankNameOnlinePayment, tfBankAccountOnlinePayment, tfSupplierServiceName, tfSupplierAccountNo);

        JFXUtil.setFocusListener(txtDetailJE_Focus, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount);

        //Initialise  TextField KeyPressed
        List<TextField> loTxtFieldKeyPressed = Arrays.asList(tfSearchTransaction, tfSearchSupplier, tfPayeeName, tfBankNameCheck, tfBankAccountCheck, tfPurchasedAmountDetail, tfTaxCodeDetail, tfParticularsDetail, tfAuthorizedPerson,
                tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount);
        loTxtFieldKeyPressed.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
         Node[] txtField_Focusx = {
            tfTaxCodeDetail
            
        };
        for (Node txtInput : txtField_Focusx) {
            txtInput.focusedProperty().addListener(txtField_Focus);
        }

    }
    final ChangeListener<Boolean> txtField_Focus = (obs, oldVal, newVal) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) obs).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        
//        if (lsValue == null) {
//            return;
//        }

        if (!newVal) {
            try {
                switch (lsTextFieldID) {
                    case "tfTaxCodeDetail":
//                        if(poDisbursementController.Detail(pnDetailDV).getTaxCode().isEmpty() && poDisbursementController.Detail(pnDetailDV).isWithVat() ){
//                            if(ShowMessageFX.YesNo("No Tax Code has been assigned. \n "
//                                    + " VAT will not be applied, and this transaction requires user approval before proceeding.\n "
//                                    + " Do you want to proceed?", pxeModuleName, null)){
//                                poJSON = poDisbursementController.callapproval();
//                                if("error".equals(poJSON.get("result"))){
//                                    ShowMessageFX.Information((String)poJSON.get("message"), pxeModuleName, lsValue);
//                                    return;
//                                }
//                                
//                                poDisbursementController.Detail(pnDetailDV).isWithVat(false);
//                                poDisbursementController.Detail(pnDetailDV).setDetailVatRates(DisbursementStatic.DefaultValues.default_value_double);
//                                poDisbursementController.computeVat(pnDetailDV, 
//                                        poDisbursementController.Detail(pnDetailDV).getAmount(), 
//                                        poDisbursementController.Detail(pnDetailDV).getDetailVatRates(), 
//                                        (double) poJSONVAT.get("totalApplied"), 
//                                        true);
//                                
//                                loadTableDetailDV();
//                            }
//                        }
                        break;
                    default:
                        break;
                }
                
            } catch (Exception e) {
                System.err.println("Error processing input [" + lsTextFieldID + "]: " + e.getMessage());
            }
        } 
    };

    final ChangeListener<? super Boolean> txtMasterCheck_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtMasterCheck = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtMasterCheck.getId());
        String lsValue = (txtMasterCheck.getText() == null ? "" : txtMasterCheck.getText());

        lastFocusedTextField = txtMasterCheck;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfAuthorizedPerson":
                    poDisbursementController.CheckPayments().getModel().setAuthorize(lsValue);
                    break;
            }
        }
    };
    final ChangeListener<? super Boolean> txtMasterOnlinePayment_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtMasterOnlinePayment = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtMasterOnlinePayment.getId());
        String lsValue = (txtMasterOnlinePayment.getText() == null ? "" : txtMasterOnlinePayment.getText());

        lastFocusedTextField = txtMasterOnlinePayment;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfBankNameOnlinePayment":
                    break;
                case "tfBankAccountOnlinePayment":
                    break;
                case "tfSupplierServiceName":
                    break;
                case "tfSupplierAccountNo":
                    break;
            }
        }
    };
    final ChangeListener<? super Boolean> txtMasterBankTransfer_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtMasterBankTransfer = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtMasterBankTransfer.getId());
        String lsValue = (txtMasterBankTransfer.getText() == null ? "" : txtMasterBankTransfer.getText());

        lastFocusedTextField = txtMasterBankTransfer;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfBankNameBTransfer":
                    break;
                case "tfBankAccountBTransfer":
                    break;
                case "tfSupplierBank":
                    break;
                case "tfSupplierAccountNoBTransfer":
                    break;
            }
        }
    };
    final ChangeListener<? super Boolean> txtDetailJE_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            try {
                switch (lsTxtFieldID) {
                    case "tfCreditAmount":
                        if (lsValue.isEmpty()) {
                            lsValue = "0.0000";
                        }
                        if (pnDetailJE >= 0) {
                            lsValue = JFXUtil.removeComma(lsValue);
                            if (poDisbursementController.Journal().Detail(pnDetailJE).getDebitAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                                poDisbursementController.Journal().Detail(pnDetailJE).setDebitAmount(0.0000);
                                tfCheckAmount.setText("0.0000");
                                tfCheckAmount.requestFocus();
                                break;
                            } else {
                                poJSON = poDisbursementController.Journal().Detail(pnDetailJE).setCreditAmount((Double.parseDouble(lsValue)));
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            if (pbEnteredDV) {
                                initDetailFocusJE();
                                pbEnteredDV = false;
                            }
                        }
                        break;
                    case "tfDebitAmount":
                        if (lsValue.isEmpty()) {
                            lsValue = "0.0000";
                        }
                        if (pnDetailJE >= 0) {
                            if (poDisbursementController.Journal().Detail(pnDetailJE).getCreditAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                                poDisbursementController.Journal().Detail(pnDetailJE).setCreditAmount(0.0000);
                                tfDebitAmount.setText("0.0000");
                                tfDebitAmount.requestFocus();
                                break;
                            } else {
                                poJSON = poDisbursementController.Journal().Detail(pnDetailJE).setDebitAmount((Double.parseDouble(lsValue)));
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            if (pbEnteredDV) {
                                initDetailFocusJE();
                                pbEnteredDV = false;
                            }
                        }
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(DisbursementVoucher_VerificationController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    };

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
                        pbEnteredDV = true;
                        CommonUtils.SetNextFocus(txtField);
                        switch (lsID) {
                            case "tfSearchTransaction":
                                psSearchTransactionNo = tfSearchTransaction.getText();
                                loadTableMain();
                                break;
                            case "tfPurchasedAmountDetail":
                            case "tfTaxCodeDetail":
                            case "tfParticularsDetail":
                                if(poDisbursementController.Detail(pnDetailDV).getTaxCode().isEmpty() && poDisbursementController.Detail(pnDetailDV).isWithVat() ){
                                if (ShowMessageFX.YesNo("No Tax Code has been assigned. \n "
                                            + " VAT will not be applied, and this transaction requires user approval before proceeding.\n "
                                            + " Do you want to proceed?", pxeModuleName, null)) {
                                        poJSON = poDisbursementController.callapproval();
                                        if ("error".equals(poJSON.get("result"))) {
                                            ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, lsValue);
                                            return;
                                        }

                                        poDisbursementController.Detail(pnDetailDV).isWithVat(false);
                                        poDisbursementController.Detail(pnDetailDV).setDetailVatRates(DisbursementStatic.DefaultValues.default_value_double);
                                        poDisbursementController.computeVat(pnDetailDV,
                                                poDisbursementController.Detail(pnDetailDV).getAmount(),
                                                poDisbursementController.Detail(pnDetailDV).getDetailVatRates(),
                                                (double) poJSONVAT.get("totalApplied"),
                                                true);

                                        loadTableDetailDV();
                                    }
                                }
                                poDisbursementController.Detail(pnDetailDV).setAmount(Double.parseDouble(JFXUtil.removeComma(tfPurchasedAmountDetail.getText())));
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailDV = JFXUtil.moveToNextRow(tblVwDetails);
                                        moveNextFocusDV();
                                    });
                                    delay.play();
                                });
                                loadTableDetailDV();
                                event.consume();
                                break;
                        }
                        switch (lsID) {
                            case "tfAccountCode":
                            case "tfAccountDescription":
                            case "tfDebitAmount":
                            case "tfCreditAmount":
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailJE = JFXUtil.moveToNextRow(tblVwJournalDetails);
                                        initDetailFocusJE();
                                    });
                                    delay.play();
                                });
                                loadTableDetailJE();
                                event.consume();
                                break;
                        }
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchSupplier":
                                poJSON = poDisbursementController.SearchSupplier(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchSupplier.setText(poDisbursementController.Master().Payee().getPayeeName() != null ? poDisbursementController.Master().Payee().getPayeeName() : "");
                                psSearchSupplierID = poDisbursementController.Master().getPayeeID();
                                loadTableMain();
                                break;
                            case "tfBankNameCheck":
                                poJSON = poDisbursementController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }

                                tfBankNameCheck.setText(poDisbursementController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK) ? (poDisbursementController.CheckPayments().getModel().Banks().getBankName() != null ? poDisbursementController.CheckPayments().getModel().Banks().getBankName() : "") : "");
                                break;
                            case "tfBankAccountCheck":
                                poJSON = poDisbursementController.SearchBankAccount(lsValue, poDisbursementController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfBankAccountCheck.setText(poDisbursementController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK) ? (poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "") : "");
                                break;
                            case "tfPayeeName":
                                poJSON = poDisbursementController.SearchPayee(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }

                                tfPayeeName.setText(poDisbursementController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK) ? poDisbursementController.Master().Payee().getPayeeName() : "");
                                break;
                            case "tfParticularsDetail":
                                poJSON = poDisbursementController.SearchParticular(lsValue, pnDetailDV, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailDV = JFXUtil.moveToNextRow(tblVwDetails);
                                        moveNextFocusDV();
                                    });
                                    delay.play();
                                });
                                loadTableDetailDV();
                                break;
                            case "tfAuthorizedPerson":
                                break;
                            case "tfTaxCodeDetail":
                                poJSON = poDisbursementController.SearchTaxCode(lsValue, pnDetailDV, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailDV = JFXUtil.moveToNextRow(tblVwDetails);
                                        moveNextFocusDV();
                                    });
                                    delay.play();
                                });
                                loadTableDetailDV();
                                break;
                            case "tfAccountCode":
                                poJSON = poDisbursementController.Journal().SearchAccountCode(pnDetailJE, lsValue, true, null, null);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    tfAccountCode.setText("");
                                    break;
                                }
                                poJSON = poDisbursementController.checkExistAcctCode(pnDetailJE, poDisbursementController.Journal().Detail(pnDetailJE).getAccountCode());
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnRow = (int) poJSON.get("row");
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    if (pnDetailJE != lnRow) {
                                        pnDetailJE = lnRow;
                                        loadTableDetailJE();
                                        return;
                                    }
                                    break;
                                }
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailJE = JFXUtil.moveToNextRow(tblVwJournalDetails);
                                        initDetailFocusJE();
                                    });
                                    delay.play();
                                });
                                loadTableDetailJE();
                                break;
                            case "tfAccountDescription":
                                poJSON = poDisbursementController.Journal().SearchAccountCode(pnDetailJE, lsValue, false, null, null);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    tfAccountCode.setText("");
                                    break;
                                }
                                poJSON = poDisbursementController.checkExistAcctCode(pnDetailJE, poDisbursementController.Journal().Detail(pnDetailJE).getAccountCode());
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnRow = (int) poJSON.get("row");
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    if (pnDetailJE != lnRow) {
                                        pnDetailJE = lnRow;
                                        loadTableDetailJE();
                                        return;
                                    }
                                    break;
                                }
                                Platform.runLater(() -> {
                                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                    delay.setOnFinished(event1 -> {
                                        pnDetailJE = JFXUtil.moveToNextRow(tblVwJournalDetails);
                                        initDetailFocusJE();
                                    });
                                    delay.play();
                                });
                                loadTableDetailJE();
                                break;
                        }
                        CommonUtils.SetNextFocus((TextField) event.getSource());
                        event.consume();
                        break;
                    case UP:
                        switch (lsID) {
                            case "tfPurchasedAmountDetail":
                            case "tfTaxCodeDetail":
                            case "tfParticularsDetail":
                                pnDetailDV = JFXUtil.moveToPreviousRow(tblVwDetails);
                                movePreviousFocusDV();
                                event.consume();
                                break;
                        }
                        switch (lsID) {
                            case "tfAccountCode":
                            case "tfAccountDescription":
                            case "tfDebitAmount":
                            case "tfCreditAmount":
                                pnDetailJE = JFXUtil.moveToPreviousRow(tblVwJournalDetails);
                                initDetailFocusJE();
                                event.consume();
                                break;
                        }
                        event.consume();
                        break;
                    case DOWN:
                        switch (lsID) {
                            case "tfPurchasedAmountDetail":
                            case "tfTaxCodeDetail":
                            case "tfParticularsDetail":
                                pnDetailDV = JFXUtil.moveToNextRow(tblVwDetails);
                                moveNextFocusDV();
                                event.consume();
                                break;
                        }
                        switch (lsID) {
                            case "tfAccountCode":
                            case "tfAccountDescription":
                            case "tfDebitAmount":
                            case "tfCreditAmount":
                                pnDetailJE = JFXUtil.moveToNextRow(tblVwJournalDetails);
                                initDetailFocusJE();
                                event.consume();
                                break;
                        }
                        event.consume();
                        break;
                    default:
                        break;

                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void movePreviousFocusDV() {
        if (pnDetailDV >= 0) {
            switch (poDisbursementController.Detail(pnDetailDV).getSourceCode()) {
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                    initDetailFocusDV(true);
                    break;
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    initDetailFocusDV(false);
                    break;
                case DisbursementStatic.SourceCode.CASH_PAYABLE:
                    initDetailFocusDV(true);
                    break;
                default:
                    loadRecordDetailDV();
                    initFields(pnEditMode);
                    break;
            }
        }
    }

    private void moveNextFocusDV() {
        if (pnDetailDV >= 0) {
            switch (poDisbursementController.Detail(pnDetailDV).getSourceCode()) {
                case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                    initDetailFocusDV(true);
                    break;
                case DisbursementStatic.SourceCode.PAYMENT_REQUEST:
                    initDetailFocusDV(false);
                    break;
                case DisbursementStatic.SourceCode.CASH_PAYABLE:
                    initDetailFocusDV(true);
                    break;
                default:
                    loadRecordDetailDV();
                    initFields(pnEditMode);
                    break;
            }
        }
    }

    private void initDetailFocusDV(boolean isSOACache) {
        apDVDetail.requestFocus();
        loadRecordDetailDV();
        initFields(pnEditMode);
        String lsSourceNo = poDisbursementController.Detail(pnDetailDV).getSourceNo();
        double amount = poDisbursementController.Detail(pnDetailDV).getAmount();
        String lsTaxCode = poDisbursementController.Detail(pnDetailDV).getTaxCode();
        String lsParticular = poDisbursementController.Detail(pnDetailDV).getParticularID();

        if (lsSourceNo.isEmpty()) {
            return;
        }
        if (isSOACache) {
            if (!lsTaxCode.isEmpty() && amount > 0.0000) {
                tfParticularsDetail.requestFocus();
            } else if (lsTaxCode.isEmpty() && lsParticular.isEmpty()) {
                tfParticularsDetail.requestFocus();
            } else if (!lsTaxCode.isEmpty() && amount <= 0.0000 && !lsParticular.isEmpty()) {
                tfPurchasedAmountDetail.requestFocus();
            } else {
                tfTaxCodeDetail.requestFocus();
            }
        } else {
            if (lsTaxCode.isEmpty() && amount > 0.0000) {
                tfTaxCodeDetail.requestFocus();
            } else if (lsTaxCode.isEmpty() && amount <= 0.0000) {
                tfPurchasedAmountDetail.requestFocus();
            }
        }
    }

    public void loadTableDetailJE() {
//        pbEntered = false;
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblVwJournalDetails.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    journal_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.UPDATE) {
                            if ((poDisbursementController.Journal().getDetailCount() - 1) >= 0) {
                                if (poDisbursementController.Journal().Detail(poDisbursementController.Journal().getDetailCount() - 1).getAccountCode() != null
                                        && !poDisbursementController.Journal().Detail(poDisbursementController.Journal().getDetailCount() - 1).getAccountCode().equals("")) {
                                    poDisbursementController.Journal().AddDetail();
                                    poDisbursementController.Journal().Detail(poDisbursementController.Journal().getDetailCount() - 1).setForMonthOf(oApp.getServerDate());
                                }
                            }
                        }
                        for (lnCtr = 0; lnCtr < poDisbursementController.Journal().getDetailCount(); lnCtr++) {
                            journal_data.add(new ModelJournalEntry_Detail(
                                    String.valueOf(lnCtr + 1),
                                    poDisbursementController.Journal().Detail(lnCtr).getAccountCode() != null ? poDisbursementController.Journal().Detail(lnCtr).getAccountCode() : "",
                                    poDisbursementController.Journal().Detail(lnCtr).Account_Chart().getDescription() != null ? poDisbursementController.Journal().Detail(lnCtr).Account_Chart().getDescription() : "",
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Journal().Detail(lnCtr).getDebitAmount(), true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Journal().Detail(lnCtr).getCreditAmount(), true),
                                    CustomCommonUtil.formatDateToShortString(poDisbursementController.Journal().Detail(lnCtr).getForMonthOf())
                            ));

                        }
                        if (pnDetailJE <= 0) {
                            if (!journal_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblVwJournalDetails, 0);
                                pnDetailJE = tblVwJournalDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetailJE();
                                if (poDisbursementController.Journal().Detail(pnDetailJE).getAccountCode() == null) {
                                    tfAccountCode.requestFocus();
                                }
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblVwJournalDetails, pnDetailJE);
                            loadRecordDetailJE();
                        }
                        loadRecordMasterJE();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (journal_data == null || journal_data.isEmpty()) {
                    tblVwJournalDetails.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblVwJournalDetails.toFront();
                }
                loading.progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (journal_data == null || journal_data.isEmpty()) {
                    tblVwJournalDetails.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    private void loadRecordMasterJE() {
        try {
            lblJournalTransactionStatus.setText(getStatusJE(poDisbursementController.Journal().Master().getTransactionStatus()));
            tfJournalTransactionNo.setText(poDisbursementController.Journal().Master().getTransactionNo());
            dpJournalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poDisbursementController.Journal().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            double lnTotalDebit = 0;
            double lnTotalCredit = 0;
            for (int lnCtr = 0; lnCtr < poDisbursementController.Journal().getDetailCount(); lnCtr++) {
                lnTotalDebit += poDisbursementController.Journal().Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poDisbursementController.Journal().Detail(lnCtr).getCreditAmount();
            }
            tfTotalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            tfTotalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            taJournalRemarks.setText(poDisbursementController.Journal().Master().getRemarks()
            );

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getStatusJE(String lsValueStatus) {
        String lsStatus;
        switch (lsValueStatus) {
            case DisbursementStatic.OPEN:
                lsStatus = "OPEN";
                break;
            case JournalStatus.CONFIRMED:
                lsStatus = "CONFIRMED";
                break;
            case JournalStatus.POSTED:
                lsStatus = "POSTED";
                break;
            case JournalStatus.CANCELLED:
                lsStatus = "CANCELLED";
                break;
            case JournalStatus.VOID:
                lsStatus = "VOID";
                break;
            default:
                lsStatus = "UNKNOWN";
                break;
        }
        return lsStatus;
    }

    public void loadRecordDetailJE() {
        try {
            if (pnDetailJE >= 0) {
                tfAccountCode.setText(poDisbursementController.Journal().Detail(pnDetailJE).getAccountCode());
                tfAccountDescription.setText(poDisbursementController.Journal().Detail(pnDetailJE).Account_Chart().getDescription());
                dpReportMonthYear.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poDisbursementController.Journal().Detail(pnDetailJE).getForMonthOf(), SQLUtil.FORMAT_SHORT_DATE)));
                tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Journal().Detail(pnDetailJE).getDebitAmount(), true));
                tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Journal().Detail(pnDetailJE).getCreditAmount(), true));
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initDetailFocusJE() {
        apJournalDetails.requestFocus();
        loadRecordDetailJE();
        initFields(pnEditMode);
        if (tfAccountCode.getText() == null) {
            tfAccountCode.requestFocus();
        } else if (tfAccountDescription.getText() == null) {
            tfAccountDescription.requestFocus();
        } else if (Double.parseDouble(tfCreditAmount.getText().replace(",", "")) > 0.0000) {
            tfDebitAmount.requestFocus();
        } else {
            tfCreditAmount.requestFocus();
        }
    }

    private void initTableDetailJE() {
        JFXUtil.setColumnCenter(tblJournalRowNo, tblJournalAccountCode, tblJournalAccountDescription, tblJournalReportMonthYear);
        JFXUtil.setColumnRight(tblJournalCreditAmount, tblJournalDebitAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalDetails);
        filteredJournal_Data = new FilteredList<>(journal_data, b -> true);

        SortedList<ModelJournalEntry_Detail> sortedData = new SortedList<>(filteredJournal_Data);
        sortedData.comparatorProperty().bind(tblVwJournalDetails.comparatorProperty());
        tblVwJournalDetails.setItems(sortedData);
        tblVwJournalDetails.autosize();
    }

    private void initTextAreaFields() {
        //Initialise  TextArea Focus
        JFXUtil.setFocusListener(txtArea_Focus, taDVRemarks, taJournalRemarks);
        //Initialise  TextArea KeyPressed
        taDVRemarks.setOnKeyPressed(event -> txtArea_KeyPressed(event));
        taJournalRemarks.setOnKeyPressed(event -> txtArea_KeyPressed(event));
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
            try {
                switch (lsID) {
                    case "taDVRemarks":
                        poDisbursementController.Master().setRemarks(lsValue);
                        break;
                    case "taJournalRemarks":
                        poDisbursementController.Journal().Master().setRemarks(lsValue);
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(DisbursementVoucher_VerificationController.class
                        .getName()).log(Level.SEVERE, null, ex);
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

    private void initComboBox() {
        // Set Items
        cmbPaymentMode.setItems(cPaymentMode);
        cmbPayeeType.setItems(cPayeeType);
        cmbDisbursementMode.setItems(cDisbursementMode);
        cmbClaimantType.setItems(cClaimantType);
        cmbCheckStatus.setItems(cCheckStatus);
        cmbOtherPayment.setItems(cOtherPayment);
        cmbOtherPaymentBTransfer.setItems(cOtherPaymentBTransfer);

        cmbPaymentMode.setOnAction(e -> {
            if ((pnEditMode == EditMode.UPDATE) && cmbPaymentMode.getSelectionModel().getSelectedIndex() >= 0) {
                poDisbursementController.Master().setOldDisbursementType(poDisbursementController.Master().getDisbursementType());
                poDisbursementController.Master().setDisbursementType(String.valueOf(cmbPaymentMode.getSelectionModel().getSelectedIndex()));
                loadRecordMasterDV();
            }
            initFields(pnEditMode);
        });

        cmbPayeeType.setOnAction(event -> {
            if ((pnEditMode == EditMode.UPDATE) && cmbPayeeType.getSelectionModel().getSelectedIndex() >= 0) {
                poDisbursementController.CheckPayments().getModel().setPayeeType(String.valueOf(cmbPayeeType.getSelectionModel().getSelectedIndex()));
                initFields(pnEditMode);
            }
        }
        );
        disbursementModeHandler = event -> {
            if (pnEditMode == EditMode.UPDATE && cmbDisbursementMode.getSelectionModel().getSelectedIndex() >= 0) {
                if (poDisbursementController.CheckPayments().getModel().getDesbursementMode().equals("1")
                        && poDisbursementController.CheckPayments().getModel().getClaimant().equals("0")
                        && !tfAuthorizedPerson.getText().isEmpty()) {
                    if (ShowMessageFX.YesNo("You have Claimant Type & Authorized Person are not empty, \n"
                            + "Are you sure you want to change Disbursement Type?", pxeModuleName, null)) {
                        poDisbursementController.CheckPayments().getModel().setClaimant("");
                        poDisbursementController.CheckPayments().getModel().setAuthorize("");
                        cmbClaimantType.setValue(null);
                        tfAuthorizedPerson.setText("");
                    } else {
                        // Temporarily remove the listener
                        cmbDisbursementMode.setOnAction(null);
                        Platform.runLater(() -> {
                            cmbDisbursementMode.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getDesbursementMode().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getDesbursementMode()) : -1);
                            cmbClaimantType.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getClaimant()) : -1);
                            tfAuthorizedPerson.setText(poDisbursementController.CheckPayments().getModel().getAuthorize());
                            cmbDisbursementMode.setOnAction(disbursementModeHandler);
                        });
                        return;
                    }
                } else if (poDisbursementController.CheckPayments().getModel().getDesbursementMode().equals("1")
                        && poDisbursementController.CheckPayments().getModel().getClaimant().equals("0")) {
                    if (ShowMessageFX.YesNo("You have Claimant Type is not empty, \n"
                            + "Are you sure you want to change Disbursement Type?", pxeModuleName, null)) {
                        poDisbursementController.CheckPayments().getModel().setClaimant("");
                        cmbClaimantType.setValue(null);
                    } else {
                        cmbClaimantType.setOnAction(null);
                        Platform.runLater(() -> {
                            cmbDisbursementMode.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getDesbursementMode().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getDesbursementMode()) : -1);
                            cmbClaimantType.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getClaimant()) : -1);
                            cmbDisbursementMode.setOnAction(disbursementModeHandler);
                        });
                        return;
                    }

                }

                poDisbursementController.CheckPayments().getModel().setDesbursementMode(String.valueOf(cmbDisbursementMode.getSelectionModel().getSelectedIndex()));
                initFields(pnEditMode);
            }
        };

        cmbDisbursementMode.setOnAction(disbursementModeHandler);
        claimantTypeHandler = event -> {
            if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) && cmbClaimantType.getSelectionModel().getSelectedIndex() >= 0) {
                if (poDisbursementController.CheckPayments().getModel().getClaimant().equals("0") && !tfAuthorizedPerson.getText().isEmpty()) {
                    if (ShowMessageFX.YesNo("You have Authorized Person is not Empty, \n"
                            + "Are you sure you want to change Claimant Type?", pxeModuleName, null)) {

                        poDisbursementController.CheckPayments().getModel().setAuthorize("");
                        tfAuthorizedPerson.setText("");
                    } else {
                        // Temporarily remove the listener
                        cmbClaimantType.setOnAction(null);
                        Platform.runLater(() -> {
                            cmbClaimantType.getSelectionModel().select(!poDisbursementController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poDisbursementController.CheckPayments().getModel().getClaimant()) : -1);
                            tfAuthorizedPerson.setText(poDisbursementController.CheckPayments().getModel().getAuthorize());
                            cmbClaimantType.setOnAction(claimantTypeHandler);
                        });
                        return;
                    }
                }

                poDisbursementController.CheckPayments().getModel().setClaimant(String.valueOf(cmbClaimantType.getSelectionModel().getSelectedIndex()));
                initFields(pnEditMode);
            }
        };

        cmbClaimantType.setOnAction(claimantTypeHandler);
        cmbOtherPayment.setOnAction(event
                -> {
            if ((pnEditMode == EditMode.UPDATE) && cmbOtherPayment.getSelectionModel().getSelectedIndex() >= 0) {
            }
        }
        );
        cmbOtherPaymentBTransfer.setOnAction(event
                -> {
            if ((pnEditMode == EditMode.UPDATE) && cmbOtherPaymentBTransfer.getSelectionModel().getSelectedIndex() >= 0) {
            }
        }
        );
    }

    private int safeParseIndex(String indexStr, int maxSize) {
        try {
            int index = Integer.parseInt(indexStr);
            if (index >= 0 && index < maxSize) {
                return index;
            }
        } catch (NumberFormatException e) {
            // Ignore parsing error
        }
        return -1; // Invalid index
    }

    private void initDatePicker() {
        dpCheckDate.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE) {
                LocalDate selectedLocalDate = dpCheckDate.getValue();
                LocalDate transactionDate = new java.sql.Date(poDisbursementController.CheckPayments().getModel().getCheckDate().getTime()).toLocalDate();
                if (selectedLocalDate == null) {
                    return;
                }

                LocalDate dateNow = LocalDate.now();
                psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                boolean approved = true;
                if (pnEditMode == EditMode.UPDATE) {
                    if (!DisbursementStatic.VERIFIED.equals(poDisbursementController.Master().getTransactionStatus())) {
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
                    poDisbursementController.CheckPayments().getModel().setCheckDate(
                            SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                } else {
                    if (pnEditMode == EditMode.ADDNEW) {
                        dpCheckDate.setValue(dateNow);
                        poDisbursementController.CheckPayments().getModel().setCheckDate(
                                SQLUtil.toDate(dateNow.toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } else if (pnEditMode == EditMode.UPDATE) {
                        poDisbursementController.CheckPayments().getModel().setCheckDate(
                                SQLUtil.toDate(psOldDate, SQLUtil.FORMAT_SHORT_DATE));
                    }
                }
                dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                        SQLUtil.dateFormat(poDisbursementController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE)));
            }
        }
        );
        dpReportMonthYear.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE) {
                if (pnDetailJE >= 0) {
                    try {
                        LocalDate selectedLocalDate = dpReportMonthYear.getValue();
                        if (selectedLocalDate == null) {
                            return;
                        }
                        poDisbursementController.Journal().Detail(pnDetailJE).setForMonthOf(SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                        Platform.runLater(() -> {
                            PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                            delay.setOnFinished(event1 -> {
                                initDetailFocusJE();
                            });
                            delay.play();
                        });
                        loadTableDetailJE();

                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DisbursementVoucher_VerificationController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }

    private void initCheckBox() {
        chbkIsCrossCheck.setOnAction(event -> {
            if ((pnEditMode == EditMode.UPDATE)) {
                poDisbursementController.CheckPayments().getModel().isCross(chbkIsCrossCheck.isSelected());
            }
        });
        chbkIsPersonOnly.setOnAction(event -> {
            if ((pnEditMode == EditMode.UPDATE)) {
                poDisbursementController.CheckPayments().getModel().isPayee(chbkIsPersonOnly.isSelected());
            }
        });
        chbkPrintByBank.setOnAction(event -> {
            if ((pnEditMode == EditMode.UPDATE)) {
                poDisbursementController.Master().setBankPrint(chbkPrintByBank.isSelected() == true ? "1" : "0");
                initFields(pnEditMode);
            }
        });
        chbkVatClassification.setOnAction(event -> {
            if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
                if (pnDetailDV >= 0) {
                    poDisbursementController.Detail(pnDetailDV).isWithVat(chbkVatClassification.isSelected() == true);
                    loadTableDetailDV();
                }
            }
        });
    }

    private void clearFields() {
        previousSearchedTextField = null;
        lastFocusedTextField = null;
        CustomCommonUtil.setText("", tfDVTransactionNo, tfVoucherNo);
        JFXUtil.setValueToNull(null, dpDVTransactionDate, dpJournalTransactionDate, dpCheckDate);
        JFXUtil.setValueToNull(null, cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbOtherPayment, cmbOtherPaymentBTransfer, cmbCheckStatus);
        JFXUtil.clearTextFields(apDVDetail, apDVMaster1, apDVMaster2, apDVMaster3, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails);
        CustomCommonUtil.setSelected(false, chbkIsCrossCheck, chbkPrintByBank, chbkVatClassification, chbkIsPersonOnly);
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apDVMaster1, apDVMaster2, apDVMaster3, apJournalMaster, apJournalDetails);
        JFXUtil.setDisabled(true, apDVDetail, apMasterDVCheck, apMasterDVOp, apMasterDVBTransfer, tfParticularsDetail, tfAuthorizedPerson);
        tabJournal.setDisable(false);
        if (!detailsdv_data.isEmpty()) {
            if (oApp.getUserLevel() >= UserRight.ENCODER) {
                tabJournal.setDisable(fnEditMode == EditMode.UNKNOWN || fnEditMode == EditMode.ADDNEW);
            }
        }

        tabCheck.setDisable(true);
        tabOnlinePayment.setDisable(true);
        tabBankTransfer.setDisable(true);
        if (main_data.isEmpty()) {
            Label placeholderLabel = new Label("NO RECORD TO LOAD");
            tblVwDisbursementVoucher.setPlaceholder(placeholderLabel);
            pagination.setManaged(false);
            pagination.setVisible(false);
        }
        switch (poDisbursementController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                tabCheck.setDisable(!lbShow);
                apMasterDVOp.setDisable(!lbShow);
                CustomCommonUtil.switchToTab(tabCheck, tabPanePaymentMode);

                boolean isDisbursementModeSelected = cmbDisbursementMode.getSelectionModel().getSelectedIndex() >= 0;
                boolean isClaimantTypeSelected = cmbClaimantType.getSelectionModel().getSelectedIndex() == 0;

                if (isDisbursementModeSelected && isClaimantTypeSelected) {
                    tfAuthorizedPerson.setDisable(!lbShow);
                }

                break;
            case DisbursementStatic.DisbursementType.WIRED:
                tabBankTransfer.setDisable(!lbShow);
                apMasterDVBTransfer.setDisable(!lbShow);
                CustomCommonUtil.switchToTab(tabBankTransfer, tabPanePaymentMode);
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                tabOnlinePayment.setDisable(!lbShow);
                apMasterDVOp.setDisable(!lbShow);
                CustomCommonUtil.switchToTab(tabOnlinePayment, tabPanePaymentMode);
                break;
        }
        if (tabDetails.isSelected() && pnDetailDV >= 0) {
            if (tfRefNoDetail.getText() != null) {
                if (!tfRefNoDetail.getText().isEmpty()) {
                    JFXUtil.setDisabled(!lbShow, apDVDetail);
                    switch (poDisbursementController.Detail(pnDetailDV).getSourceCode()) {
                        case DisbursementStatic.SourceCode.ACCOUNTS_PAYABLE:
                        case DisbursementStatic.SourceCode.CASH_PAYABLE:
                            tfParticularsDetail.setDisable(!lbShow);
                            break;
                    }
                }
            }
        }

        if (tabJournal.isSelected() && pnDetailJE >= 0) {
            JFXUtil.setDisabled(!lbShow, apJournalDetails);
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (pnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate, btnDVCancel, btnVoid, btnVerify);
        JFXUtil.setButtonsVisibility(fnEditMode != EditMode.ADDNEW && fnEditMode != EditMode.UNKNOWN, btnHistory);
        if (fnEditMode == EditMode.READY) {
            switch (poDisbursementController.Master().getTransactionStatus()) {
                case DisbursementStatic.OPEN:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnVoid, btnVerify);
                    break;
                case DisbursementStatic.RETURNED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    break;
                case DisbursementStatic.VERIFIED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnDVCancel);
                    break;
            }
        }
    }

    private void initTextFieldsProperty() {
        tfSearchSupplier.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    if (pnDetailDV >= 0) {
                        poDisbursementController.Master().setPayeeID("");
                        tfSearchSupplier.setText("");
                        psSearchSupplierID = "";
                        loadTableMain();
                    }
                }
            }
        }
        );
        tfSearchTransaction.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    if (pnDetailDV >= 0) {
                        psSearchTransactionNo = "";
                        loadTableMain();
                    }
                }
            }
        }
        );
        tfTaxCodeDetail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    if (pnDetailDV >= 0) {
                        poDisbursementController.Detail(pnDetailDV).setTaxCode("");
                        poDisbursementController.Detail(pnDetailDV).setTaxRates(0.00);
                        poDisbursementController.Detail(pnDetailDV).setTaxAmount(0.0000);
                        tfTaxCodeDetail.setText("");
                        tfTaxRateDetail.setText("0.00");
                        tfTaxAmountDetail.setText("0.0000");
                        loadTableDetailDV();
                    }
                }
            }
        }
        );
        tfParticularsDetail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    if (pnDetailDV >= 0) {
                        poDisbursementController.Detail(pnDetailDV).setParticularID("");
                        tfParticularsDetail.setText("");
                        loadTableDetailDV();
                    }
                }
            }
        }
        );
        tfBankNameCheck.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    poDisbursementController.CheckPayments().getModel().setBankID("");
                    poDisbursementController.CheckPayments().getModel().setBankAcountID("");
                    tfBankNameCheck.setText("");
                    tfBankAccountCheck.setText("");
                }
            }
        }
        );
        tfBankAccountCheck.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (newValue.isEmpty()) {
                            poDisbursementController.CheckPayments().getModel().setBankAcountID("");
                            tfBankAccountCheck.setText("");
                        }
                    }
                }
                );
        tfPayeeName.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (newValue.isEmpty()) {
                            poDisbursementController.CheckPayments().getModel().setPayeeID("");
                            tfPayeeName.setText("");
                        }
                    }
                }
                );
        tfAuthorizedPerson.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (newValue.isEmpty()) {
                            poDisbursementController.CheckPayments().getModel().setAuthorize("");
                            tfAuthorizedPerson.setText("");
                        }
                    }
                }
                );
        tfAccountCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        if (pnDetailJE >= 0) {
                            poDisbursementController.Journal().Detail(pnDetailJE).setAccountCode("");
                            tfAccountCode.setText("");
                            loadTableDetailJE();

                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DisbursementVoucher_VerificationController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }
}
