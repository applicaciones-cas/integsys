package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
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
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DisbursementVoucher_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private int pnDetailDV = 0;
    private int pnDetailJE = 0;
    private final String pxeModuleName = "Disbursement Voucher History";
    private Disbursement poDisbursementController;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchSupplierID = "";
    private String psSearchRefNo = "";
    public int pnEditMode;

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Detail> detailsdv_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Detail> filteredDataDetailDV;

    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    private FilteredList<ModelJournalEntry_Detail> filteredJournal_Data;

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
    private Button btnBrowse, btnHistory, btnClose;

    /*DV Master*/
    @FXML
    private AnchorPane apDVMaster1, apDVMaster2, apDVMaster3;
    @FXML
    private TabPane tabPanePaymentMode;
    @FXML
    private TextField tfDVTransactionNo, tfSupplier, tfVoucherNo;
    @FXML
    private ComboBox<String> cmbPaymentMode;

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
    private TextField tfRefNoDetail, tfParticularsDetail, tfAccountCodeDetail, tfPurchasedAmountDetail, tfTaxCodeDetail, tfTaxRateDetail, tfTaxAmountDetail, tfNetAmountDetail;
    @FXML
    private CheckBox chbkVatClassification;
    @FXML
    private TableView tblVwDetails;
    @FXML
    private TableColumn tblDVRowNo, tblReferenceNo, tblAccountCode, tblTransactionTypeDetail, tblParticulars, tblPurchasedAmount, tblTaxCode, tblTaxAmount, tblNetAmount;

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
            tfAccountCodeDetail.setVisible(false);
            poDisbursementController = new CashflowControllers(oApp, null).Disbursement();
            poDisbursementController.setTransactionStatus(
                    DisbursementStatic.OPEN
                    + DisbursementStatic.VERIFIED
                    + DisbursementStatic.AUTHORIZED
                    + DisbursementStatic.DISAPPROVED
                    + DisbursementStatic.VOID
                    + DisbursementStatic.RETURNED
                    + DisbursementStatic.CANCELLED
                    + DisbursementStatic.CERTIFIED);
            poJSON = new JSONObject();
            poJSON = poDisbursementController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                poDisbursementController.Master().setIndustryID(psIndustryId);
                poDisbursementController.Master().setCompanyID(psCompanyId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        initButtonsClickActions();
        initComboBox();
        initTextFieldsDV();
        initTableDetailDV();
        initTableDetailJE();
        initTableOnClick();
        clearFields();
        initTabSelection();
        initTextFieldsProperty();
        pnEditMode = EditMode.UNKNOWN;
        initFields();
        initButton();
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poDisbursementController.Master().Company().getCompanyName() + " - " + poDisbursementController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_HistoryController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
        List<Button> buttons = Arrays.asList(btnBrowse, btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnHistory":
                    ShowMessageFX.Warning("Button History is Underdevelopment.", pxeModuleName, null);
                    break;
                case "btnBrowse":
                    poJSON = poDisbursementController.SearchTransactionForDVHistory("", psSearchRefNo, psSearchSupplierID);
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    clearFields();
                    pnEditMode = poDisbursementController.getEditMode();
                    loadTableDetailDV();
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
            initFields();
            initButton();
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterDV() {
        try {
            poJSON = new JSONObject();
            tfSupplier.setText(poDisbursementController.Master().Payee().Client().getCompanyName() != null ? poDisbursementController.Master().Payee().Client().getCompanyName() : "");
            tfDVTransactionNo.setText(poDisbursementController.Master().getTransactionNo() != null ? poDisbursementController.Master().getTransactionNo() : "");
            tfSupplier.setText(poDisbursementController.Master().Payee().Client().getCompanyName() != null ? poDisbursementController.Master().Payee().Client().getCompanyName() : "");
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
            Logger.getLogger(DisbursementVoucher_EntryController.class.getName()).log(Level.SEVERE, null, ex);
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
            tfBankAccountCheck.setText(poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
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
            Logger.getLogger(DisbursementVoucher_EntryController.class
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
                tfAccountCodeDetail.setText(poDisbursementController.Detail(pnDetailDV).Particular().getAccountCode());
                tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getAmount(), true));
                tfTaxCodeDetail.setText(poDisbursementController.Detail(pnDetailDV).TaxCode().getTaxCode());
                tfTaxRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getTaxRates(), false));
                tfTaxAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getTaxAmount(), true));
                tfNetAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(pnDetailDV).getAmount()
                        - poDisbursementController.Detail(pnDetailDV).getTaxAmount(), true));
                chbkVatClassification.setSelected(poDisbursementController.Detail(pnDetailDV).isWithVat());

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(DisbursementVoucher_EntryController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableDetailDV() {
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
                                            lsTransactionType,
                                            poDisbursementController.Detail(lnCtr).Particular().getDescription(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getAmount(), true),
                                            poDisbursementController.Detail(lnCtr).TaxCode().getTaxCode(),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.Detail(lnCtr).getTaxAmount(), true),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(lnNetTotal, true)
                                    ));

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(DisbursementVoucher_EntryController.class
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
                        JFXUtil.selectAndFocusRow(tblVwDetails, pnDetailDV);
                        loadRecordDetailDV();
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
        JFXUtil.setColumnCenter(tblDVRowNo, tblReferenceNo, tblTransactionTypeDetail, tblAccountCode, tblParticulars, tblTaxCode);
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
                        initFields();
                        break;
                }
            }
        });

        tblVwDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        tblVwJournalDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);

        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                pnDetailJE = tblVwJournalDetails.getSelectionModel().getSelectedIndex();
                initDetailFocusJE();
            }
        }
        );
        JFXUtil.adjustColumnForScrollbar(tblVwDetails, tblVwJournalDetails);
    }

    private void initTabSelection() {
        tabJournal.setOnSelectionChanged(event -> {
            if (tabJournal.isSelected()) {
                pnDetailDV = -1;
                pnDetailJE = -1;
                populateJE();
            }
        }
        );
        tabDetails.setOnSelectionChanged(event -> {
            if (tabDetails.isSelected()) {
                pnDetailJE = -1;
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
                        event.consume();
                    }
                    break;
            }
        }
    }

    private void initTextFieldsDV() {

        JFXUtil.setFocusListener(txtDetailDV_Focus, tfSearchTransaction);
        //Initialise  TextField KeyPressed
        List<TextField> loTxtFieldKeyPressed = Arrays.asList(tfSearchTransaction, tfSearchSupplier);
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
                            case "tfSearchTransaction":
                                psSearchRefNo = tfSearchTransaction.getText();
                                break;
                        }
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchSupplier":
                                poJSON = poDisbursementController.SearchPayee(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchSupplier.setText(poDisbursementController.Master().Payee().getPayeeName() != null ? poDisbursementController.Master().Payee().getPayeeName() : "");
                                psSearchSupplierID = poDisbursementController.Master().getPayeeID();
                                break;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        event.consume();
                        break;
                    case UP:
                        CommonUtils.SetPreviousFocus(txtField);
                        event.consume();
                        break;
                    case DOWN:
                        CommonUtils.SetNextFocus(txtField);
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
    final ChangeListener<? super Boolean> txtDetailDV_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());

        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSearchTransaction":
                    psSearchRefNo = tfSearchTransaction.getText();
                    break;
            }
        }
    };

    private void initDetailFocusDV(boolean isSOACache) {
        apDVDetail.requestFocus();
        loadRecordDetailDV();
        initFields();
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
                    } catch (SQLException | GuanzonException ex) {
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
            Logger.getLogger(DisbursementVoucher_EntryController.class
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

    private void initComboBox() {
        cmbPaymentMode.setItems(cPaymentMode);
        cmbPayeeType.setItems(cPayeeType);
        cmbDisbursementMode.setItems(cDisbursementMode);
        cmbClaimantType.setItems(cClaimantType);
        cmbCheckStatus.setItems(cCheckStatus);
        cmbOtherPayment.setItems(cOtherPayment);
        cmbOtherPaymentBTransfer.setItems(cOtherPaymentBTransfer);
    }

    private void clearFields() {
        CustomCommonUtil.setText("", tfDVTransactionNo, tfVoucherNo);
        JFXUtil.setValueToNull(null, dpDVTransactionDate, dpJournalTransactionDate, dpCheckDate);
        JFXUtil.setValueToNull(null, cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbOtherPayment, cmbOtherPaymentBTransfer, cmbCheckStatus);
        JFXUtil.clearTextFields(apDVDetail, apDVMaster2, apDVMaster3, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apJournalMaster, apJournalDetails);
        CustomCommonUtil.setSelected(false, chbkIsCrossCheck, chbkPrintByBank, chbkVatClassification);
    }

    private void initFields() {
        JFXUtil.setDisabled(true, apDVDetail, apDVMaster1, apDVMaster2, apDVMaster3);
        JFXUtil.setDisabled(true, apMasterDVCheck, apMasterDVOp, apMasterDVBTransfer);
        tabCheck.setDisable(true);
        tabOnlinePayment.setDisable(true);
        tabBankTransfer.setDisable(true);
        switch (poDisbursementController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                tabCheck.setDisable(false);
                CustomCommonUtil.switchToTab(tabCheck, tabPanePaymentMode);
                loadRecordMasterCheck();
                break;
            case DisbursementStatic.DisbursementType.WIRED:
                tabBankTransfer.setDisable(false);
                CustomCommonUtil.switchToTab(tabBankTransfer, tabPanePaymentMode);
                loadRecordMasterBankTransfer();
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                tabOnlinePayment.setDisable(false);
                CustomCommonUtil.switchToTab(tabOnlinePayment, tabPanePaymentMode);
                loadRecordMasterOnlinePayment();
                break;
        }
    }

    private void initButton() {
        CustomCommonUtil.setVisible(true, btnBrowse, btnClose);
        CustomCommonUtil.setManaged(true, btnBrowse, btnClose);
        btnHistory.setVisible(false);
        btnHistory.setManaged(false);
    }

    private void initTextFieldsProperty() {
        tfSearchTransaction.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    psSearchRefNo = "";
                }
            }
        }
        );
        tfSearchSupplier.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    poDisbursementController.Master().setPayeeID("");
                    tfSearchSupplier.setText("");
                    psSearchSupplierID = "";
                }
            }
        }
        );
    }
}
