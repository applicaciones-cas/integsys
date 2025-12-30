/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
import javafx.geometry.Bounds;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;
import ph.com.guanzongroup.integsys.model.ModelBIR_Detail;

/**
 * FXML Controller class
 *
 * @author Team 1 & Team 2
 */
public class DisbursementVoucher_VerificationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON, poJSONVAT;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private int pnDetail = 0;
    private int pnDetailJE = 0;
    private int pnDetailBIR = 0;
    private boolean pbIsCheckedJournalTab = false;
    private boolean pbIsCheckedBIRTab = false;
    private final String pxeModuleName = "Disbursement Voucher Verification";
    private DisbursementVoucher poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierPayeeId = "";
    private String psSearchSupplierID = "";
    private String psSearchTransactionNo = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;

    private ObservableList<ModelDisbursementVoucher_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private ObservableList<ModelBIR_Detail> BIR_data = FXCollections.observableArrayList();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEnteredDV = false;
    private boolean pbEnteredJE = false;
    private boolean pbEnteredBIR = false;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableDetailJE, loadTableDetailBIR;

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

    private EventHandler<ActionEvent> claimantTypeHandler;
    /* DV  & Journal */
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMasterDetail, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVMaster2, apDVMaster3, apDVDetail, apJournalMaster, apJournalDetails, apBIRDetail;
    @FXML
    private Label lblSource, lblDVTransactionStatus, lblJournalTransactionStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchTransaction, tfSearchSupplier, tfDVTransactionNo, tfSupplier, tfVoucherNo, tfBankNameCheck, tfBankAccountCheck, tfPayeeName, tfCheckNo, tfCheckAmount, tfAuthorizedPerson, tfBankNameBTransfer, tfBankAccountBTransfer, tfPaymentAmountBTransfer, tfSupplierBank, tfSupplierAccountNoBTransfer, tfBankTransReferNo, tfBankNameOnlinePayment, tfBankAccountOnlinePayment, tfPaymentAmount, tfSupplierServiceName, tfSupplierAccountNo, tfPaymentReferenceNo, tfTotalAmount, tfVatableSales, tfVatAmountMaster, tfVatZeroRatedSales, tfVatExemptSales, tfLessWHTax, tfTotalNetAmount, tfRefNoDetail, tfVatableSalesDetail, tfVatExemptDetail, tfVatZeroRatedSalesDetail, tfVatRateDetail, tfVatAmountDetail, tfPurchasedAmountDetail, tfNetAmountDetail, tfJournalTransactionNo, tfTotalDebitAmount, tfTotalCreditAmount, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount, tfBIRTransactionNo, tfTaxCode, tfParticular, tfBaseAmount, tfTaxRate, tfTotalTaxAmount;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnVerify, btnVoid, btnRetrieve, btnHistory, btnClose;
    @FXML
    private TabPane tabPaneMain, tabPanePaymentMode;
    @FXML
    private Tab tabDetails, tabCheck, tabBankTransfer, tabOnlinePayment, tabJournal, tabBIR;
    @FXML
    private DatePicker dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpPeriodFrom, dpPeriodTo;
    @FXML
    private ComboBox cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbOtherPaymentBTransfer, cmbOtherPayment;
    @FXML
    private CheckBox chbkPrintByBank, chbkIsCrossCheck, chbkIsPersonOnly, chbkVatClassification, cbReverse, cbBIRReverse;
    @FXML
    private TextArea taDVRemarks, taJournalRemarks;
    @FXML
    private TableView tblVwDetails, tblVwJournalDetails, tblVwBIRDetails, tblViewMainList;
    @FXML
    private TableColumn tblDVRowNo, tblReferenceNo, tblTransactionTypeDetail, tblPurchasedAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount, tblJournalRowNo, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblJournalReportMonthYear, tblBIRRowNo, tblBIRParticular, tblTaxCode, tblBaseAmount, tblTaxRate, tblTaxAmount, tblRowNo, tblSupplier, tblPaymentForm, tblTransDate, tblReferNo;
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
            poController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initButtonsClickActions();
            initTextFields();
            initComboBoxes();
            initDatePicker();
            initDetailGrid();
            initMainGrid();
            initDetailJEGrid();
            initDetailBIRGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initDVMasterTabs();
            initButton(pnEditMode);
            pagination.setPageCount(1);
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

            Platform.runLater(() -> {
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
                poController.setIndustryID(psIndustryId);
                poController.setCompanyID(psCompanyId);
                poController.setCategoryID(psCategoryId);
                poController.Master().setBranchCode(oApp.getBranchCode());
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTabPane() {
        tabPaneMain.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabTitle = newTab.getText();
                switch (tabTitle) {
                    case "Disbursement Voucher":
                        if (pnEditMode == EditMode.UNKNOWN) {
                            pnDetailJE = 0;
                            pnDetailBIR = 0;
                        } else {
                            loadRecordMaster();
                        }
                        break;
                    case "Journal":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                                pbIsCheckedJournalTab = true;
                                populateJE();
                            } else {
                                JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                                ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                            }
                        }
                        break;
                    case "BIR 2307":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            if (poController.Detail(0).getSourceNo() != null && !poController.Detail(0).getSourceNo().isEmpty()) {
                                pbIsCheckedBIRTab = true;
                                populateBIR();
                            } else {
                                JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                                ShowMessageFX.Warning(null, pxeModuleName, "Please provide at least one valid disbursement detail to proceed.");
                            }
                        }
                        break;
                }
            }
        });

        tabPanePaymentMode.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            tabPanePaymentMode.lookupAll(".tab").forEach(node -> {
                if (node.localToScene(node.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
                    String tabName = ((javafx.scene.control.Label) node.lookup(".tab-label")).getText();
                    for (Tab tab : tabPanePaymentMode.getTabs()) {
                        if (tab.getText().equals(tabName) && tab.isDisable()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "This tab has been disabled as only one option applies based on the selected payment form.");
                            event.consume();
                        }
                    }
                }
            });
        });
    }

    //Disables/ Enables tabs
    private void initDVMasterTabs() {
        boolean lbShow = JFXUtil.isObjectEqualTo(pnEditMode, EditMode.READY, EditMode.ADDNEW, EditMode.UPDATE);
        JFXUtil.setDisabled(true, tabCheck, tabOnlinePayment, tabBankTransfer);
        switch (poController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                JFXUtil.setDisabled(!lbShow, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                loadRecordMasterCheck();
                //must reset data of check
                break;
            case DisbursementStatic.DisbursementType.WIRED:
                JFXUtil.setDisabled(!lbShow, tabBankTransfer);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Bank Transfer");
                loadRecordMasterBankTransfer();
                //must reset data of btransfer
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                JFXUtil.setDisabled(!lbShow, tabOnlinePayment);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "E-Wallet");
                loadRecordMasterOnlinePayment();
                //must reset data of online payment
                break;
            default:
                JFXUtil.setDisabled(false, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                break;
        }
        initButton(pnEditMode);
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVerify, btnVoid, btnRetrieve, btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pbIsCheckedJournalTab = false;
                    pbIsCheckedBIRTab = false;
                    pnEditMode = poController.getEditMode();
                    CustomCommonUtil.switchToTab(tabDetails, tabPaneMain);
                    loadTableDetail.reload();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVDetail, apJournalDetails);
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }

                    if (pnEditMode == EditMode.UPDATE) {
                        if (!pbIsCheckedJournalTab) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry before saving.");
                            return;
                        }
                        if (!pbIsCheckedBIRTab) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before saving.");
                            return;
                        }
                        poController.Master().setModifiedDate(oApp.getServerDate());
                        poController.Master().setModifyingId(oApp.getUserID());
                    }
                    poJSON = poController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to verify this transaction?")) {
                        poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                        if ("success".equals(poJSON.get("result"))) {
                            poJSON = poController.VerifyTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                        break;
                    } else {
                        return;
                    }
                case "btnHistory":
                    ShowMessageFX.Warning(null, pxeModuleName, "Button History is under development.");
                    break;
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnVerify":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to verify transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (!poController.existJournal().equals("")) {
                                if (!pbIsCheckedJournalTab) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Please check the Journal Entry before verifying.");
                                    return;
                                } else if (!pbIsCheckedBIRTab) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Please check the BIR 2307 before verifying.");
                                    return;
                                } else {
                                    poJSON = poController.VerifyTransaction("Verified");
                                    if ("error".equals((String) poJSON.get("result"))) {
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                        return;
                                    } else {
                                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                        JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                        JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                    }
                                }
                            } else {
                                ShowMessageFX.Warning(null, pxeModuleName, "This transaction has no journal entry. Please add a journal entry by updating the transaction to enable verification.");
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    String lsTransaction = "";
                    if (DisbursementStatic.OPEN.equals(poController.Master().getTransactionStatus())) {
                        lsTransaction = "void";
                    } else if (DisbursementStatic.VERIFIED.equals(poController.Master().getTransactionStatus())) {
                        lsTransaction = "cancel";
                    } else {
                    }
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to " + lsTransaction + " transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            if (!poController.existJournal().equals("")) {
                                switch (poController.Master().getTransactionStatus()) {
                                    case DisbursementStatic.OPEN:
                                        poJSON = poController.VoidTransaction("");
                                        break;
                                    case DisbursementStatic.VERIFIED:
                                    default:
                                        poJSON = poController.CancelTransaction("");
                                        break;
                                }
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    pnEditMode = poController.getEditMode();
                                    JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                    JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                                }
                            } else {
                                ShowMessageFX.Warning(null, pxeModuleName, "This transaction has no journal entry. Please add a journal entry by updating the transaction to enable void.");
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered, Please contact admin to assist about the unregistered button");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid", "btnVerify", "btnDVCancel")) {
                pbIsCheckedJournalTab = false;
                pbIsCheckedBIRTab = false;
                poController.resetTransaction();
                poController.Master().setSupplierClientID(psSupplierPayeeId);
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Disbursement Voucher");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
                loadTableDetailJE.reload();
                loadTableDetailBIR.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNext(false, false);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void populateBIR() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apBIRDetail);
            poJSON = poController.populateWithholdingTaxDeduction();
            if (JFXUtil.isJSONSuccess(poJSON)) {
                loadTableDetailBIR.reload();
            } else {
                BIR_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void populateJE() {
        try {
            poJSON = new JSONObject();
            JFXUtil.clearTextFields(apJournalMaster, apJournalDetails);
            poController.getEditMode();
            poJSON = poController.populateJournal();
            if (JFXUtil.isJSONSuccess(poJSON)) {
                loadTableDetailJE.reload();
            } else {
                journal_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();
        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
        ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                String lsTransactionNo = selected.getIndex06();
                clearTextFields();
                poJSON = poController.OpenTransaction(lsTransactionNo);

                if ("error".equals((String) poJSON.get("result"))) {
                    poController.resetTransaction();
                    pnEditMode = EditMode.UNKNOWN;
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                } else {
                    pnEditMode = poController.getEditMode();
                }
                Platform.runLater(() -> {
                    loadTableDetail.reload();
                    loadTableDetailJE.reload();
                    loadTableDetailBIR.reload();
                });
                moveNext(false, false);
            } catch (CloneNotSupportedException | SQLException | ScriptException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            try {
                                main_data.clear();
                                JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                                poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchSupplier.getText(), tfSearchTransaction.getText(), "", false);
                                int lnRowNo = 0;
                                if (poController.getMasterList().size() > 0) {
                                    for (int lnCtr = 0; lnCtr <= poController.getMasterList().size() - 1; lnCtr++) {
                                        String lsPaymentForm = "";
                                        //Retrieve Open or Returned DV transactions only
                                        if (!JFXUtil.isObjectEqualTo(poController.getMaster(lnCtr).getTransactionStatus(),
                                                DisbursementStatic.OPEN, DisbursementStatic.RETURNED, DisbursementStatic.VERIFIED)) {
                                            continue;
                                        }
                                        lsPaymentForm = JFXUtil.setStatusValue(null, DisbursementStatic.DisbursementType.class, poController.getMaster(lnCtr).getDisbursementType());
                                        lnRowNo += 1;
                                        main_data.add(new ModelDisbursementVoucher_Main(
                                                String.valueOf(lnRowNo),
                                                poController.getMaster(lnCtr).Payee().getPayeeName(),
                                                lsPaymentForm,
                                                CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCtr).getTransactionDate()),
                                                poController.getMaster(lnCtr).getVoucherNo(),
                                                poController.getMaster(lnCtr).getTransactionNo()
                                        ));
                                        if (poController.getMaster(lnCtr).getTransactionStatus().equals(DisbursementStatic.RETURNED)) {
                                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnRowNo), "#FAA0A0", highlightedRowsMain);
                                        }
                                        if (poController.getMaster(lnCtr).getTransactionStatus().equals(DisbursementStatic.VERIFIED)) {
                                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnRowNo), "#C1E1C1", highlightedRowsMain);
                                        }
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
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblVwDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            pbEnteredDV = false;
                            details_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                                poJSON = poController.computeDetailFields();
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).getAmountApplied(), null, "")) {
                                    if (Double.valueOf(poController.Detail(lnCtr).getAmountApplied()) <= 0) {
                                        continue;
                                    }
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelDisbursementVoucher_Detail(String.valueOf(lnRowCount),
                                                poController.Detail(lnCtr).getSourceNo(),
                                                poController.getSourceCodeDescription(poController.Detail(lnCtr).getSourceCode()),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmountApplied(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatSales(), true),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatAmount(), true)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatRates(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailZeroVat(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatExempt(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmount(), true),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 11); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex11());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblVwDetails.getSelectionModel().getSelectedIndex()).getIndex11());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableDetailJE = new JFXUtil.ReloadableTableTask(
                tblVwJournalDetails,
                journal_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEnteredJE = false;
                        journal_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadJournal();
                            }
                            for (int lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
                                journal_data.add(new ModelJournalEntry_Detail(String.valueOf(lnCtr + 1),
                                        poController.Journal().Detail(lnCtr).getAccountCode() != null ? poController.Journal().Detail(lnCtr).getAccountCode() : "",
                                        poController.Journal().Detail(lnCtr).Account_Chart().getDescription() != null ? poController.Journal().Detail(lnCtr).Account_Chart().getDescription() : "",
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getDebitAmount(), true),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getCreditAmount(), true),
                                        CustomCommonUtil.formatDateToShortString(poController.Journal().Detail(lnCtr).getForMonthOf())
                                ));
                            }
                            if (pnDetailJE < 0 || pnDetailJE
                                    >= journal_data.size()) {
                                if (!journal_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwJournalDetails, 0);
                                    pnDetailJE = tblVwJournalDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetailJE();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwJournalDetails, pnDetailJE);
                                loadRecordDetailJE();
                            }
                            loadRecordMasterJE();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
        loadTableDetailBIR = new JFXUtil.ReloadableTableTask(
                tblVwBIRDetails,
                BIR_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEnteredBIR = false;
                        BIR_data.clear();
                        int lnCtr;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadWTDeductions();
                            }
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getWTaxDeductionsCount(); lnCtr++) {
                                if (poController.WTaxDeduction(lnCtr).getModel().isReverse()) {
                                    lnRowCount += 1;
                                    BIR_data.add(new ModelBIR_Detail(String.valueOf(lnRowCount),
                                            poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().AccountChart().getDescription(),
                                            poController.WTaxDeduction(lnCtr).getModel().getTaxCode(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().getBaseAmount(), false),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().WithholdingTax().getTaxRate(), false),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(lnCtr).getModel().getTaxAmount(), false),
                                            String.valueOf(lnCtr))
                                    );
                                }
                            }
                            int lnTempRow = JFXUtil.getDetailRow(BIR_data, pnDetailBIR, 7); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= BIR_data.size()) {
                                if (!BIR_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwBIRDetails, 0);
                                    int lnRow = Integer.parseInt(BIR_data.get(0).getIndex07());
                                    pnDetailBIR = lnRow;
                                    loadRecordDetailBIR();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwBIRDetails, lnTempRow);
                                int lnRow = Integer.parseInt(BIR_data.get(tblVwBIRDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailBIR = lnRow;
                                loadRecordDetailBIR();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransDate, tblReferNo);
        JFXUtil.setColumnLeft(tblSupplier, tblPaymentForm);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDVRowNo, tblReferenceNo);
        JFXUtil.setColumnLeft(tblTransactionTypeDetail);
        JFXUtil.setColumnRight(tblPurchasedAmount, tblNetAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetails);
        tblVwDetails.setItems(details_data);
    }

    private void initDetailJEGrid() {
        JFXUtil.setColumnCenter(tblJournalRowNo, tblJournalReportMonthYear);
        JFXUtil.setColumnLeft(tblJournalAccountCode, tblJournalAccountDescription);
        JFXUtil.setColumnRight(tblJournalDebitAmount, tblJournalCreditAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalDetails);
        tblVwJournalDetails.setItems(journal_data);
    }

    private void initDetailBIRGrid() {
        JFXUtil.setColumnCenter(tblBIRRowNo);
        JFXUtil.setColumnLeft(tblBIRParticular, tblTaxCode);
        JFXUtil.setColumnRight(tblBaseAmount, tblTaxRate, tblTaxAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwBIRDetails);
        tblVwBIRDetails.setItems(BIR_data);
    }

    private void initTableOnClick() {
        tblVwDetails.setOnMouseClicked(event -> {
            if (!details_data.isEmpty() && event.getClickCount() == 1) {
                ModelDisbursementVoucher_Detail selected = (ModelDisbursementVoucher_Detail) tblVwDetails.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    pnDetail = tblVwDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0 && event.getClickCount() == 2) {
                loadTableDetailFromMain();
                initButton(pnEditMode);
            }
        }
        );
        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                pnDetailJE = tblVwJournalDetails.getSelectionModel().getSelectedIndex();
                loadRecordDetailJE();
                moveNextJE(false, false);
            }
        }
        );
        tblVwBIRDetails.setOnMouseClicked(event -> {
            if (!BIR_data.isEmpty() && event.getClickCount() == 1) { // Detect single click (or use another condition for double click)
                int lnRow = Integer.parseInt(BIR_data.get(tblVwBIRDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailBIR = lnRow;
                loadRecordDetailBIR();
                moveNextBIR(false, false);
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDisbursementVoucher_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblVwDetails, tblVwJournalDetails, tblVwBIRDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblVwDetails, tblVwJournalDetails, tblVwBIRDetails);
    }

    private void tableKeyEvents(KeyEvent event) {
        TableView<?> currentTable = (TableView<?>) event.getSource();
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell == null) {
            return;
        }
        boolean moveDown = event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.DOWN;
        boolean moveUp = event.getCode() == KeyCode.UP;
        int newIndex = 0;

        if (moveDown || moveUp) {
            newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
            switch (currentTable.getId()) {
                case "tblVwDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex11())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex11());
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;
                case "tblVwJournalDetails":
                    if (journal_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
                    pnDetailJE = newIndex;
                    loadRecordDetailJE();
                    break;
                case "tblVwBIRDetails":
                    if (BIR_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(BIR_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(BIR_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailBIR = newIndex;
                    loadRecordDetailBIR();
                    break;
            }
            event.consume();
        }
    }

    private void initTextFields() {
        //Initialise  TextField Focus
        JFXUtil.setFocusListener(txtSearch_Focus, tfSearchIndustry, tfSearchSupplier, tfSearchTransaction);
        JFXUtil.setFocusListener(txtArea_Focus, taDVRemarks, taJournalRemarks);
        //apDVMaster1
        JFXUtil.setFocusListener(txtMaster_Focus, tfSupplier);
        //apDVDetail
        JFXUtil.setFocusListener(txtDetail_Focus, tfPurchasedAmountDetail, tfVatExemptDetail);
        //apCheck
        JFXUtil.setFocusListener(txtMasterCheck_Focus, tfBankNameCheck, tfBankAccountCheck, tfPayeeName, tfAuthorizedPerson);
        // apMasterDVBTransfer
        JFXUtil.setFocusListener(txtMasterBankTransfer_Focus, tfBankNameBTransfer, tfBankAccountBTransfer);
        //apMasterDVOp
        JFXUtil.setFocusListener(txtMasterOnlinePayment_Focus, tfBankNameOnlinePayment, tfBankAccountOnlinePayment);
        //apJournalDetails
        JFXUtil.setFocusListener(txtDetailJE_Focus, tfAccountCode, tfAccountDescription, tfDebitAmount, tfCreditAmount);
        JFXUtil.setFocusListener(txtBIRDetail_Focus, tfBaseAmount, tfTaxCode, tfParticular, tfTaxCode);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVDetail, apJournalDetails, apBIRDetail);
        JFXUtil.inputDecimalOnly(tfVatZeroRatedSales, tfVatZeroRatedSalesDetail, tfVatRateDetail, tfTaxRate);
        JFXUtil.setCommaFormatter(tfCheckAmount, tfPaymentAmountBTransfer, tfPaymentAmount, tfTotalAmount, tfVatAmountMaster, tfTotalNetAmount, tfVatAmountDetail, tfPurchasedAmountDetail, tfNetAmountDetail, tfTotalCreditAmount, tfTotalDebitAmount, tfDebitAmount, tfCreditAmount, tfTotalTaxAmount, tfBaseAmount);
        JFXUtil.setCheckboxHoverCursor(chbkPrintByBank, chbkIsCrossCheck, chbkIsPersonOnly, chbkVatClassification);
    }
    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchSupplier":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                            psSearchSupplierID = "";
                            loadTableMain.reload();
                        }
                        break;
                    case "tfSearchTransaction":
                        if (lsValue.isEmpty()) {
                            psSearchTransactionNo = "";
                            loadTableMain.reload();
                        }
                        break;
                }
                loadRecordSearch();
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taDVRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "taJournalRemarks":
                        poJSON = poController.Journal().Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterJE();
                        break;
                }
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.setSearchClient("");
                                poController.setSearchPayee("");
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getSupplierClientID(), null, "")
                                && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeID(), null, "")) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                                poController.removeDetails();
                                                if (poController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                                    poController.CheckPayments().getModel().setPayeeID("");
                                                    loadRecordMasterCheck();
                                                }
                                                poController.Master().setSupplierClientID("");
                                                poController.Master().setPayeeID("");
                                                psSupplierPayeeId = "";
                                                loadTableDetail.reload();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            poController.Master().setSupplierClientID("");
                            poController.Master().setPayeeID("");
                        }
                        break;
                }
                loadRecordMaster();
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfVatExemptDetail":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Detail(pnDetail).setDetailVatExempt(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        if (pbEnteredDV) {
                            pbEnteredDV = false;
                        }
                        break;
                    case "tfPurchasedAmountDetail":
                        lsValue = JFXUtil.removeComma(lsValue);
                        Double ldblOrigAmt = poController.Detail(pnDetail).getAmountApplied();
                        poJSON = poController.Detail(pnDetail).setAmountApplied(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        poJSON = poController.computeFields();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poController.Detail(pnDetail).setAmountApplied(0.00);
                            tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmountApplied(), true));
                            return;
                        }
                        if (pbEnteredDV) {
                            moveNext(false, true);
                            pbEnteredDV = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    ChangeListener<Boolean> txtMasterCheck_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBankNameCheck":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankID("");
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfBankAccountCheck":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfPayeeName":
                        //Similar as the Supplier Name, if deleted; then?
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (!JFXUtil.isObjectEqualTo(poController.Master().getSupplierClientID(), null, "")
                                && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeID(), null, "")) {
                                    if (poController.getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                                poController.removeDetails();
                                                if (poController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                                                    poController.CheckPayments().getModel().setPayeeID("");
                                                    loadRecordMasterCheck();
                                                }
                                                poController.Master().setSupplierClientID("");
                                                poController.Master().setPayeeID("");
                                                psSupplierPayeeId = "";
                                                loadTableDetail.reload();
                                            } else {
                                                loadRecordMaster();
                                                return;
                                            }
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }
                            poController.Master().setSupplierClientID("");
                            poController.Master().setPayeeID("");
                        }
                        break;
                    case "tfAuthorizedPerson":
                        poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMasterCheck();
            });
    ChangeListener<Boolean> txtMasterBankTransfer_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfBankNameBTransfer":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankID("");
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfBankAccountBTransfer":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankAcountID("");
                        }
                        break;
                    case "tfSupplierBank":
                        if (lsValue.isEmpty()) {
//                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                                if (!JFXUtil.isObjectEqualTo(poController.Master().getSupplierClientID(), null, "")
//                                && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeID(), null, "")) {
//                                    if (poController.getDetailCount() > 1) {
//                                        if (!pbKeyPressed) {
//                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
//                                                    "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
//                                                poController.removeDetails();
//                                                if (poController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
//                                                    poController.CheckPayments().getModel().setPayeeID("");
//                                                    loadRecordMasterCheck();
//                                                }
//                                                poController.Master().setSupplierClientID("");
//                                                poController.Master().setPayeeID("");
//                                                tfSupplier.setText("");
//                                                psSupplierPayeeId = "";
//                                                loadTableDetail.reload();
//                                            } else {
//                                                loadRecordMaster();
//                                                return;
//                                            }
//                                        } else {
//                                            loadRecordMaster();
//                                            return;
//                                        }
//                                    }
//                                }
//                            }
//                            poController.Master().setSupplierClientID("");
                        }
                        break;
                    case "tfSupplierAccountNoBTransfer":
                        poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMasterBankTransfer();
            });
    ChangeListener<Boolean> txtMasterOnlinePayment_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    /*Lost Focus*/
                    switch (lsID) {
                        case "tfSupplierServiceName":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Banks().setBankCode("");
                            }
                            break;
                        case "tfSupplierAccountNo":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Bank_Account_Master().setAccountCode("");
                            }
                            break;
                        case "tfBankAccountOnlinePayment":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Bank_Account_Master().setAccountCode("");
                            }
                            break;
                        case "tfBankNameOnlinePayment":
                            if (lsValue.isEmpty()) {
                                poController.OtherPayments().getModel().Banks().setBankCode("");
                            }
                            break;
                    }
                    loadRecordMasterOnlinePayment();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
    ChangeListener<Boolean> txtDetailJE_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
//                try {
                switch (lsID) {
                    case "tfAccountCode":
                        if (lsValue.isEmpty()) {
                            poController.Journal().Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfAccountDescription":
                        if (lsValue.isEmpty()) {
                            poController.Journal().Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfDebitAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Journal().Detail(pnDetailJE).getCreditAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Journal().Detail(pnDetailJE).setCreditAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfDebitAmount);
                            break;
                        } else {
                            poJSON = poController.Journal().Detail(pnDetailJE).setDebitAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.runWithDelay(0.50, () -> {
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfDebitAmount);
                                });
                                return;
                            }
                        }
                        break;
                    case "tfCreditAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Journal().Detail(pnDetailJE).getDebitAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Journal().Detail(pnDetailJE).setDebitAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfCreditAmount);
                            break;
                        } else {
                            poJSON = poController.Journal().Detail(pnDetailJE).setCreditAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.runWithDelay(0.50, () -> {
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfCreditAmount);
                                });
                                return;
                            }
                        }
                        if (pbEnteredJE) {
                            moveNextJE(false, true);
                            pbEnteredJE = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetailJE.reload();
                });
//                } catch (SQLException | GuanzonException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });

//    private void baseAmountError() {
//        if ("error".equals(poJSON.get("result"))) {
//            pbEnteredBIR = false;
//            int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
//            JFXUtil.runWithDelay(0.70, () -> {
//                int lnTempRow = JFXUtil.getDetailTempRow(BIR_data, lnReturned, 7);
//                pnDetailBIR = lnTempRow;
//                poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(0.0);
//                loadTableDetailBIR.reload();
//            });
//            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//            throw new JFXUtil.BreakLoopException();
//        } else {
//            int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
//            JFXUtil.runWithDelay(0.80, () -> {
////          int lnTempRow = JFXUtil.getDetailTempRow(BIR_data, lnReturned, 7); // comment intentional
//                pnDetailBIR = lnReturned;
//                loadTableDetailBIR.reload();
//            });
//            loadTableDetail.reload();
//        }
//    }
    ChangeListener<Boolean> txtBIRDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfBaseAmount":
                            if (JFXUtil.isObjectEqualTo(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription(), null, "")) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Particular is blank, unable to input value!");
                                break;
                            }
                            lsValue = JFXUtil.removeComma(lsValue);
                            poJSON = poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(Double.valueOf(lsValue));
                            if ("error".equals((String) poJSON.get("result"))) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(0.0);
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxAmount(0.0);
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }

                            poJSON = poController.computeTaxAmount();
                            if ("error".equals((String) poJSON.get("result"))) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setBaseAmount(0.0);
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxAmount(0.0);
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            if (pbEnteredBIR) {
                                moveNextBIR(false, true);
                                pbEnteredBIR = false;
                            }
                            break;
                        case "tfTaxCode":
                            if (lsValue.isEmpty()) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxCode("");
                            }
                            break;
                        case "tfParticular":
                            if (lsValue.isEmpty()) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setTaxRateId("");
                            }
                            break;
                    }
                    JFXUtil.runWithDelay(0.50, () -> {
                        loadTableDetailBIR.reload();
                    });
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
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
                        if (tfPurchasedAmountDetail.isFocused()) {
                            pbEnteredDV = true;
                        }
                        if (tfCreditAmount.isFocused()) {
                            pbEnteredJE = true;
                        }
                        if (tfBaseAmount.isFocused()) {
                            pbEnteredBIR = true;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            //apBrowse?
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            case "tfSearchTransaction":
                                psSearchTransactionNo = tfSearchTransaction.getText();
                                loadTableMain.reload();
                                break;
                            case "tfSearchSupplier":
                                poJSON = poController.SearchSupplier(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    psSearchSupplierID = poController.Master().getPayeeID();
                                    loadRecordSearch();
                                    loadTableMain.reload();
                                }
                                break;
                            //apMasterDV1
                            case "tfSupplier":
                                //implement auto reload in similar supplier in tableMain
                                if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                    if (poController.getDetailCount() > 1) {
                                        pbKeyPressed = true;
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the supplier name?\nPlease note that this action will delete all Disbursement voucher details.\n\nDo you wish to proceed?") == true) {
                                            poController.removeDetails();
                                            loadTableDetail.reload();
                                        } else {
                                            return;
                                        }
                                        pbKeyPressed = false;
                                    }
                                }

                                poJSON = poController.SearchSupplier(lsValue, false, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMaster();
                                    break;
                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMaster();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    loadTableMain.reload();
                                });
                                break;

                            //apMasterDVCheck
                            case "tfBankNameCheck":
                                poJSON = poController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountCheck);
                                }
                                loadRecordMasterCheck();
                                break;
                            case "tfBankAccountCheck":
                                poJSON = poController.SearchBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfPayeeName);
                                }
                                loadRecordMasterCheck();
                                break;
                            case "tfPayeeName":
                                poJSON = poController.SearchPayee(poController.Master().getSupplierClientID(), true, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    loadRecordMasterCheck();
                                    break;
                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMasterCheck();
                                break;
                            case "tfAuthorizedPerson":
                                poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                }
                                loadRecordMasterCheck();
                                break;

                            //apMasterDVBTransfer
                            case "tfBankNameBTransfer":
                                poJSON = poController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfBankAccountBTransfer);
                                }
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfBankAccountBTransfer":
                                poJSON = poController.SearchBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfSupplierBank);
                                }
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfSupplierBank":
//                                poJSON = poController.SearchPayee(poController.Master().getSupplierClientID(), true, false);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                    loadRecordMasterCheck();
//                                    break;
//                                } else {
//                                    JFXUtil.textFieldMoveNext(tfSupplierAccountNoBTransfer);
//                                }
                                psSupplierPayeeId = poController.Master().getSupplierClientID();
                                loadRecordMasterBankTransfer();
                                break;
                            case "tfSupplierAccountNoBTransfer":
//                                poJSON = poController.CheckPayments().getModel().setAuthorize(lsValue);
//                                if (!JFXUtil.isJSONSuccess(poJSON)) {
//                                    ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
//                                } else {
//                                    JFXUtil.textFieldMoveNext(tfSupplierAccountNoBTransfer);
//                                }
                                loadRecordMasterBankTransfer();
                                break;

                            //apMasterDVOp
                            case "tfSupplierServiceName":
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfSupplierAccountNo":
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfBankNameOnlinePayment":
                                loadRecordMasterOnlinePayment();
                                break;
                            case "tfBankAccountOnlinePayment":
                                loadRecordMasterOnlinePayment();
                                break;

                            //apDVDetail
                            //apJournalDetails
                            case "tfAccountCode":
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, true, poController.Master().getIndustryID(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        loadTableDetailJE.reload();
                                    });
                                    break;
                                }

                                poJSON = poController.checkExistAcctCode(pnDetailJE, poController.Journal().Detail(pnDetailJE).getAccountCode());
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnRow = (int) poJSON.get("row");
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    if (pnDetailJE != lnRow) {
                                        pnDetailJE = lnRow;
                                        JFXUtil.runWithDelay(0.50, () -> {
                                            loadTableDetailJE.reload();
                                        });
                                        return;
                                    }
                                    break;
                                } else {
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfDebitAmount);
                                }

                                break;
                            case "tfAccountDescription":
                                poJSON = poController.Journal().SearchAccountCode(pnDetailJE, lsValue, false, poController.Master().getIndustryID(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        loadTableDetailJE.reload();
                                    });
                                    break;
                                }
                                poJSON = poController.checkExistAcctCode(pnDetailJE, poController.Journal().Detail(pnDetailJE).getAccountCode());
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnRow = (int) poJSON.get("row");
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    if (pnDetailJE != lnRow) {
                                        pnDetailJE = lnRow;
                                        JFXUtil.runWithDelay(0.50, () -> {
                                            loadTableDetailJE.reload();
                                        });
                                        return;
                                    }
                                    break;
                                } else {
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfDebitAmount);
                                }

                                break;
                            //apBIRDetail
                            case "tfTaxCode":
                                poJSON = poController.SearchTaxCode(lsValue, pnDetailBIR, true);
                                if ("error".equals(poJSON.get("result"))) {
//                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        loadTableDetailBIR.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    pnDetailBIR = lnReturned;
                                    loadTableDetailBIR.reload();
                                    JFXUtil.textFieldMoveNext(tfParticular);
                                }
                                break;
                            case "tfParticular":
                                poJSON = poController.SearchParticular(lsValue, pnDetailBIR, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    if (poJSON.get("row") == null) {
                                        return;
                                    }
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.50, () -> {
                                        pnDetailBIR = lnReturned;
                                        loadTableDetailBIR.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    pnDetailBIR = lnReturned;
                                    loadTableDetailBIR.reload();
                                    JFXUtil.textFieldMoveNext(tfBaseAmount);
                                }
                                break;
                        }
                        event.consume();
                        break;
                    case UP:
                        switch (lsID) {
                            case "tfPurchasedAmountDetail":
                            case "tfTaxCodeDetail":
                            case "tfParticularsDetail":
                                moveNext(true, true);
                                break;
                            case "tfAccountCode":
                            case "tfAccountDescription":
                            case "tfDebitAmount":
                            case "tfCreditAmount":
                                moveNextJE(true, true);
                                break;
                            case "tfTaxCode":
                            case "tfParticular":
                            case "tfBaseAmount":
                            case "tfTaxRate":
                                moveNextBIR(true, true);
                                break;
                        }
                        event.consume();
                        break;
                    case DOWN:
                        switch (lsID) {
                            case "tfPurchasedAmountDetail":
                            case "tfTaxCodeDetail":
                            case "tfParticularsDetail":
                                moveNext(false, true);
                                break;
                            case "tfAccountCode":
                            case "tfAccountDescription":
                            case "tfDebitAmount":
                            case "tfCreditAmount":
                                moveNextJE(false, true);
                                break;
                            case "tfTaxCode":
                            case "tfParticular":
                            case "tfBaseAmount":
                            case "tfTaxRate":
                                moveNextBIR(false, true);
                                break;
                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDVDetail.requestFocus();
            pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblVwDetails)).getIndex11())
                    : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblVwDetails)).getIndex11());
        }
        loadRecordDetail();
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.Detail(pnDetail).getAmountApplied(), tfPurchasedAmountDetail},}, tfPurchasedAmountDetail); // default
    }

    public void moveNextJE(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apJournalDetails.requestFocus();
                pnDetailJE = isUp ? JFXUtil.moveToPreviousRow(tblVwJournalDetails) : JFXUtil.moveToNextRow(tblVwJournalDetails);
            }
            loadRecordDetailJE();
            if (pnDetailJE < 0 || pnDetailJE > poController.Journal().getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Journal().Detail(pnDetailJE).getAccountCode(), tfAccountCode},
                {poController.Journal().Detail(pnDetailJE).Account_Chart().getDescription(), tfAccountDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.Journal().Detail(pnDetailJE).getDebitAmount(), tfDebitAmount},
                {poController.Journal().Detail(pnDetailJE).getCreditAmount(), tfCreditAmount},}, tfCreditAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void moveNextBIR(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apBIRDetail.requestFocus();
                pnDetailBIR = isUp ? Integer.parseInt(BIR_data.get(JFXUtil.moveToPreviousRow(tblVwBIRDetails)).getIndex07())
                        : Integer.parseInt(BIR_data.get(JFXUtil.moveToNextRow(tblVwBIRDetails)).getIndex07());
            }
            loadRecordDetailBIR();
            if (pnDetailBIR < 0 || pnDetailBIR > poController.getWTaxDeductionsCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode(), tfTaxCode},
                {poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription(), tfParticular}, // if null or empty, then requesting focus to the txtfield
                {poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount(), tfBaseAmount},}, tfBaseAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchSupplier.setText(poController.getSearchPayee());
//            tfSearchTransaction.setText(poController.getSearchTransaction());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMaster() {
        try {
            initDVMasterTabs();
            poController.computeFields();
            JFXUtil.setStatusValue(lblDVTransactionStatus, DisbursementStatic.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            JFXUtil.setDisabled(true, tfSupplier);
            tfDVTransactionNo.setText(poController.Master().getTransactionNo() != null ? poController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            JFXUtil.setCmbValue(cmbPaymentMode, !poController.Master().getDisbursementType().equals("") ? Integer.valueOf(poController.Master().getDisbursementType()) : -1);
            tfVoucherNo.setText(poController.Master().getVoucherNo());
            tfSupplier.setText(poController.Master().Payee().Client().getCompanyName() != null ? poController.Master().Payee().Client().getCompanyName() : "");
            tfVatAmountMaster.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATAmount(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATExmpt(), true));
            tfLessWHTax.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getWithTaxTotal(), true));
            tfTotalNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getNetTotal(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));
            tfVatZeroRatedSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getZeroVATSales(), true));
            tfVatableSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVATSale(), true));
            taDVRemarks.setText(poController.Master().getRemarks());

            JFXUtil.updateCaretPositions(apDVMaster1, apDVMaster2, apDVMaster3);
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordDetail() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        boolean lbNotNull = !JFXUtil.isObjectEqualTo(poController.Detail(pnDetail).getDetailVatAmount(), null, "");
        boolean lbNotZero = poController.Detail(pnDetail).getAmountApplied() > 0;
        cbReverse.selectedProperty().set(lbNotNull && lbNotZero);

        boolean lbShow = (poController.Detail(pnDetail).getSourceCode()).equals(DisbursementStatic.SourceCode.PAYMENT_REQUEST);
        JFXUtil.setDisabled(!lbShow, chbkVatClassification, tfVatExemptDetail);

        tfRefNoDetail.setText(poController.Detail(pnDetail).getSourceNo());
        chbkVatClassification.setSelected(poController.Detail(pnDetail).isWithVat());
        tfVatableSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatSales(), true));
        tfVatExemptDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatExempt(), true));
        tfVatZeroRatedSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailZeroVat(), true));
        tfVatRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatRates(), false));
        tfVatAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatAmount(), true));
        tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmountApplied(), true));
        tfNetAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmount(), true));

        JFXUtil.updateCaretPositions(apDVDetail);
    }

    private void loadRecordMasterCheck() {
        try {
            JFXUtil.setDisabled(true, tfCheckNo, tfCheckAmount);
            tfCheckNo.setText(poController.CheckPayments().getModel().getCheckNo());
            if (JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getCheckNo(), null, "")) {
                poController.CheckPayments().getModel().setCheckDate(null);
            }
            dpCheckDate.setValue(poController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
            JFXUtil.setDisabled(JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getCheckNo(), null, ""), dpCheckDate);
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.CheckPayments().getModel().getAmount(), true));
            chbkIsCrossCheck.setSelected(poController.CheckPayments().getModel().isCross());
            chbkIsPersonOnly.setSelected(poController.CheckPayments().getModel().isPayee());
            tfBankNameCheck.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
//            tfBankAccountCheck.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfBankAccountCheck.setText(poController.Master().getDisbursementType().equals(
                    DisbursementStatic.DisbursementType.CHECK)
                            ? (poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null
                            ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "") : "");
            chbkPrintByBank.setSelected(poController.Master().getBankPrint().equals(Logical.YES));

            tfPayeeName.setText(poController.Master().Payee().getPayeeName() != null ? poController.Master().Payee().getPayeeName() : "");
            JFXUtil.setCmbValue(cmbPayeeType, !poController.CheckPayments().getModel().getPayeeType().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getPayeeType()) : -1);
            JFXUtil.setCmbValue(cmbDisbursementMode, !poController.CheckPayments().getModel().getDesbursementMode().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getDesbursementMode()) : -1);
            JFXUtil.setCmbValue(cmbClaimantType, !poController.CheckPayments().getModel().getClaimant().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getClaimant()) : -1);

            tfAuthorizedPerson.setText(poController.CheckPayments().getModel().getAuthorize() != null ? poController.CheckPayments().getModel().getAuthorize() : "");
            JFXUtil.setCmbValue(cmbCheckStatus, !poController.CheckPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poController.CheckPayments().getModel().getTransactionStatus()) : -1);

            boolean lbValidation01 = poController.Master().getBankPrint().equals(Logical.YES);
            JFXUtil.setDisabled(!lbValidation01, cmbPayeeType, cmbDisbursementMode);

            boolean lbValidation02 = poController.Master().getBankPrint().equals(Logical.YES) && poController.CheckPayments().getModel().getDesbursementMode().equals("1");
            JFXUtil.setDisabled(!lbValidation02, cmbClaimantType);

            boolean lbValidation03 = poController.Master().getBankPrint().equals(Logical.YES)
                    && poController.CheckPayments().getModel().getDesbursementMode().equals("1")
                    && poController.CheckPayments().getModel().getClaimant().equals("0");
            JFXUtil.setDisabled(!lbValidation03, tfAuthorizedPerson);

            JFXUtil.updateCaretPositions(apMasterDVCheck);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterBankTransfer() {
        try {
            tfBankNameBTransfer.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccountBTransfer.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            if (true) {
                return; //temporarily as there is no getTotalAmount yet
            }
            tfPaymentAmountBTransfer.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));
            tfSupplierBank.setText(poController.CheckPayments().getModel().Supplier().getCompanyName() != null ? poController.CheckPayments().getModel().Supplier().getCompanyName() : "");
            tfSupplierAccountNoBTransfer.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");

            tfBankTransReferNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            JFXUtil.setCmbValue(cmbOtherPaymentBTransfer, !poController.OtherPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poController.OtherPayments().getModel().getTransactionStatus()) : -1);

            JFXUtil.updateCaretPositions(apMasterDVBTransfer);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterOnlinePayment() {
        try {
            if (true) {
                return;
            }
            tfPaymentAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));
            tfPaymentReferenceNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            JFXUtil.setCmbValue(cmbOtherPayment, !poController.OtherPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poController.OtherPayments().getModel().getTransactionStatus()) : -1);
            tfSupplierServiceName.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfSupplierAccountNo.setText(poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfBankNameOnlinePayment.setText(poController.OtherPayments().getModel().Banks().getBankName() != null ? poController.OtherPayments().getModel().Banks().getBankName() : "");
            tfBankAccountOnlinePayment.setText(poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.OtherPayments().getModel().Bank_Account_Master().getAccountNo() : "");

            JFXUtil.updateCaretPositions(apMasterDVOp);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMasterJE() {
        JFXUtil.setStatusValue(lblJournalTransactionStatus, JournalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Journal().Master().getTransactionStatus());
        tfJournalTransactionNo.setText(poController.Journal().Master().getTransactionNo());
        dpJournalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Journal().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
        double lnTotalDebit = 0;
        double lnTotalCredit = 0;
        for (int lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
            lnTotalDebit += poController.Journal().Detail(lnCtr).getDebitAmount();
            lnTotalCredit += poController.Journal().Detail(lnCtr).getCreditAmount();
        }
        tfTotalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
        tfTotalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
        taJournalRemarks.setText(poController.Journal().Master().getRemarks());

        JFXUtil.updateCaretPositions(apJournalMaster);
    }

    public void loadRecordDetailJE() {
        try {
            if (pnDetailJE < 0 || pnDetailJE > poController.Journal().getDetailCount() - 1) {
                return;
            }

            tfAccountCode.setText(poController.Journal().Detail(pnDetailJE).getAccountCode());
            tfAccountDescription.setText(poController.Journal().Detail(pnDetailJE).Account_Chart().getDescription());
            dpReportMonthYear.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Journal().Detail(pnDetailJE).getForMonthOf(), SQLUtil.FORMAT_SHORT_DATE)));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnDetailJE).getDebitAmount(), true));
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnDetailJE).getCreditAmount(), true));

            JFXUtil.updateCaretPositions(apJournalDetails);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetailBIR() {
        try {
            if (pnDetailBIR < 0 || pnDetailBIR > poController.getWTaxDeductionsCount() - 1) {
                return;
            }
            tfBIRTransactionNo.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTransactionNo());
            String lsPeriodFromDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
            dpPeriodFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodFromDate, "yyyy-MM-dd"));
            String lsPeriodToDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodTo());
            dpPeriodTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodToDate, "yyyy-MM-dd"));
            tfTaxCode.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode());
            tfParticular.setText(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription());
            tfBaseAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount(), false));
            tfTaxRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().getTaxRate()));
            tfTotalTaxAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxAmount(), false));
            cbBIRReverse.setSelected(poController.WTaxDeduction(pnDetailBIR).getModel().isReverse());

            JFXUtil.updateCaretPositions(apBIRDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbPaymentMode":
                        poController.Master().setOldDisbursementType(poController.Master().getDisbursementType());
                        poJSON = poController.Master().setDisbursementType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "cmbPayeeType":
                        poJSON = poController.CheckPayments().getModel().setPayeeType(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbDisbursementMode":
                        //define if claimant is not empty or the authorized person
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getClaimant(), null, "")
                        || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Claimant Type is not empty, \n"
                                    + "Are you sure you want to change Disbursement Type?")) {
                                poController.CheckPayments().getModel().setClaimant("");
                                poController.CheckPayments().getModel().setAuthorize("");
                                loadRecordMasterCheck();
                            } else {
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                        poJSON = poController.CheckPayments().getModel().setDesbursementMode(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbClaimantType":
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Authorized Person is not Empty, \n"
                                    + "Are you sure you want to change Claimant Type?")) {
                                poController.CheckPayments().getModel().setAuthorize("");
                                loadRecordMasterCheck();
                                return;
                            } else {
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                        poJSON = poController.CheckPayments().getModel().setClaimant(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterCheck();
                        break;
                    case "cmbOtherPaymentBTransfer":
                        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) && cmbOtherPaymentBTransfer.getSelectionModel().getSelectedIndex() >= 0) {
                        }
                        break;
                    case "cmbOtherPayment":
                        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) && cmbOtherPayment.getSelectionModel().getSelectedIndex() >= 0) {
                        }
                        break;
                }
            }
    );

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cPaymentMode, cmbPaymentMode), new JFXUtil.Pairs<>(cPayeeType, cmbPayeeType),
                new JFXUtil.Pairs<>(cDisbursementMode, cmbDisbursementMode), new JFXUtil.Pairs<>(cClaimantType, cmbClaimantType),
                new JFXUtil.Pairs<>(cCheckStatus, cmbCheckStatus), new JFXUtil.Pairs<>(cOtherPaymentBTransfer, cmbOtherPaymentBTransfer),
                new JFXUtil.Pairs<>(cOtherPayment, cmbOtherPayment));

        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbOtherPaymentBTransfer, cmbOtherPayment);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbOtherPaymentBTransfer, cmbOtherPayment);

        apMasterDVCheck.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE)) {
                return;
            }
            try {
                Set<Node> nodes = new HashSet<>();
                nodes.addAll(apMasterDVCheck.lookupAll(".text-field"));
                nodes.addAll(apMasterDVCheck.lookupAll(".combo-box"));
                for (Node node : nodes) {
                    if (!node.isDisabled()) {
                        continue;
                    }
                    Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());
                    if (boundsInScene.contains(event.getSceneX(), event.getSceneY())) {
                        if (node instanceof TextField) {
                            switch (node.getId()) {
                                case "tfAuthorizedPerson":
                                    ShowMessageFX.Warning(null, pxeModuleName, "Authorized Person field is only available when the \"Claimant Type\" is Authorized Representative.");
                                    break;
                            }
                        } else if (node instanceof ComboBox<?>) {
                            switch (node.getId()) {
                                case "cmbPayeeType":
                                    ShowMessageFX.Warning(null, pxeModuleName, "Payee Type is only available when \"Check Print by Bank\" is selected.");
                                    break;
                                case "cmbDisbursementMode":
                                    ShowMessageFX.Warning(null, pxeModuleName, "Disbursement mode is only available when \"Check Print by Bank\" is selected.");
                                    break;
                                case "cmbClaimantType":
                                    ShowMessageFX.Warning(null, pxeModuleName, "Claimant Type is only available when the \"Disbursement Mode\" is Pick-up.");
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
        });
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
                LocalDate currentDate = null, transactionDate = null, referenceDate = null, selectedDate = null, periodToDate = null, periodFromDate = null;
                String lsServerDate = "", lsTransDate = "", lsRefDate = "", lsSelectedDate = "", lsPeriodToDate = "", lsPeriodFromDate = "";

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
                    case "dpReportMonthYear":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                JFXUtil.setJSONError(poJSON, "Report date cannot be later than the transaction date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.Journal().Detail(pnDetailJE).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableDetailJE.reload();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpPeriodFrom":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsPeriodToDate = sdfFormat.format(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodTo());
                            periodToDate = LocalDate.parse(lsPeriodToDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (selectedDate.isAfter(periodToDate))) {
                                JFXUtil.setJSONError(poJSON, "Period From cannot be later than the \"Period To\" date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setPeriodFrom(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetailBIR();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpPeriodTo":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsPeriodFromDate = sdfFormat.format(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
                            periodFromDate = LocalDate.parse(lsPeriodFromDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (pbSuccess && (selectedDate.isBefore(periodFromDate))) {
                                JFXUtil.setJSONError(poJSON, "Period To cannot be before than the \"Period From\" date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.WTaxDeduction(pnDetailBIR).getModel().setPeriodTo(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetailBIR();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:

                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpPeriodFrom, dpPeriodTo);
        JFXUtil.setActionListener(this::datepicker_Action, dpDVTransactionDate, dpCheckDate, dpJournalTransactionDate, dpReportMonthYear, dpPeriodFrom, dpPeriodTo);
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "chbkPrintByBank":
                    if (poController.Master().getBankPrint().equals(Logical.YES)) {
                        if (!JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getPayeeType(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getDesbursementMode(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getClaimant(), null, "")
                                || !JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getAuthorize(), null, "")) {
                            //asks if should proceed
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Modes are not empty, changing will reset other check information fields, proceed?")) {
                                poController.CheckPayments().getModel().setPayeeType("");
                                poController.CheckPayments().getModel().setDesbursementMode("");
                                poController.CheckPayments().getModel().setClaimant("");
                                poController.CheckPayments().getModel().setAuthorize(null);
                            } else {
                                loadRecordMasterCheck();
                                return;
                            }
                        }
                    }

                    poJSON = poController.Master().setBankPrint(checkedBox.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }

                    loadRecordMasterCheck();
                    break;
                case "chbkIsCrossCheck":
                    poJSON = poController.CheckPayments().getModel().isCross(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMasterCheck();
                    break;
                case "chbkIsPersonOnly":
                    poJSON = poController.CheckPayments().getModel().isPayee(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMasterCheck();
                    break;
                case "chbkVatClassification":
                    if (poController.getEditMode() == EditMode.ADDNEW || poController.getEditMode() == EditMode.UPDATE) {
                        if (checkedBox.isSelected() && !poController.Detail(pnDetail).isWithVat()) {
                            poController.Detail(pnDetail).setDetailVatExempt(0.0000);
                        }
                    }

                    poJSON = poController.Detail(pnDetail).isWithVat(checkedBox.isSelected());
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadTableDetail.reload();
                    break;
                case "cbReverse":
                    if (!checkedBox.isSelected()) {
                        poController.Detail(pnDetail).setAmountApplied(0.0000);
                    }
                    loadRecordMaster();
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
                case "cbBIRReverse":
                    poJSON = poController.removeWTDeduction(pnDetailBIR);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMaster();
                    loadTableDetailBIR.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
                    break;
            }
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);
        boolean lbShow2 = (fnEditMode == EditMode.READY);
        JFXUtil.setButtonsVisibility(!lbShow, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow2, btnVerify);
        JFXUtil.setButtonsVisibility(fnEditMode != EditMode.ADDNEW && fnEditMode != EditMode.UNKNOWN, btnHistory);

        JFXUtil.setDisabled(!lbShow, apDVMaster1, apDVMaster2, apDVMaster3, apDVDetail,
                apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails, apBIRDetail);
        if (fnEditMode == EditMode.READY) {
            switch (poController.Master().getTransactionStatus()) {
                case DisbursementStatic.OPEN:
                    JFXUtil.setButtonsVisibility(true, btnUpdate, btnVoid);
                    break;
                case DisbursementStatic.VERIFIED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    JFXUtil.setButtonsVisibility(false, btnVerify);
                    break;
                case DisbursementStatic.RETURNED:
                    JFXUtil.setButtonsVisibility(true, btnUpdate);
                    break;
                case DisbursementStatic.VOID:
                case DisbursementStatic.CANCELLED:
                default:
                    JFXUtil.setButtonsVisibility(false, btnVerify, btnUpdate);
                    break;
            }
            if (JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(),
                    DisbursementStatic.OPEN, DisbursementStatic.VERIFIED)) {
                JFXUtil.setButtonsVisibility(true, btnVoid);
            }
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apDVMaster1, apDVDetail, apDVMaster2, apDVMaster3, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails, apBIRDetail);
    }
}
