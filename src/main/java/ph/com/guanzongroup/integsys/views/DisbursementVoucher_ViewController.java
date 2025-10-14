package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DisbursementVoucher_ViewController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON,poJSONVAT;    
    private final String pxeModuleName = "Disbursement Voucher View";
    private Disbursement poDisbursementController;
    private String psTransactionNo = "";
    private int pnDetailDV = 0;
    private ObservableList<ModelDisbursementVoucher_Detail> detailsdv_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Detail> filteredDataDetailDV;

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

    /* DV  */
    @FXML
    private AnchorPane AnchorMain, apButton;
    @FXML
    private StackPane StackPane;
    @FXML
    private Button btnClose;
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
    private TextField tfRefNoDetail, tfParticularsDetail, tfPurchasedAmountDetail, tfTaxCodeDetail, tfTaxRateDetail, tfTaxAmountDetail, tfNetAmountDetail,
            tfVatableSalesDetail, tfPartialPayment, tfVatAmountDetail, tfVatRateDetail, tfVatZeroRatedSalesDetail, tfVatExemptDetail;
    
    @FXML
    private CheckBox chbkVatClassification;
    @FXML
    private TableView tblVwDetails;
    @FXML
     private TableColumn tblDVRowNo, tblReferenceNo, tblAccountCode, tblTransactionTypeDetail, tblParticulars, tblPurchasedAmount, tblTaxCode, tblTaxAmount, tblNetAmount,
            tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales;
    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setDisbursement(Disbursement foValue) {
        poDisbursementController = foValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        txtAccountCode.setVisible(false);
//            tfAccountCodeDetail.setVisible(false);
        CustomCommonUtil.setDropShadow(AnchorMain, StackPane);
        if (!psTransactionNo.isEmpty()) {
            try {
                poDisbursementController = new CashflowControllers(oApp, null).Disbursement();
                poJSON = new JSONObject();
                poJSON = poDisbursementController.InitTransaction(); // Initialize transaction
                if (!"success".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                }
                poJSON = poDisbursementController.OpenTransaction(psTransactionNo);
                if (!"error".equals((String) poJSON.get("result"))) {

                    if (poDisbursementController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                        poDisbursementController.setCheckpayment();
                    }
                    loadRecordMasterDV();
                    initComboBox();
                    initTableDetailDV();
                    loadTableDetail();
                    initFields();
                } else {
                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                    CommonUtils.closeStage(btnClose);
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(DisbursementVoucher_ViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        initTableOnClick();
        btnClose.setOnAction(this::cmdButton_Click);
    }

    private void cmdButton_Click(ActionEvent event) {
        if ("btnClose".equals(((Button) event.getSource()).getId()) && ShowMessageFX.YesNo("Are you sure you want to close?", "Close Tab", null)) {
            CommonUtils.closeStage(btnClose);
        }
        initFields();
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
            Logger.getLogger(DisbursementVoucher_ViewController.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DisbursementVoucher_ViewController.class
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

    private void initComboBox() {
        cmbPaymentMode.setItems(cPaymentMode);
        cmbPayeeType.setItems(cPayeeType);
        cmbDisbursementMode.setItems(cDisbursementMode);
        cmbClaimantType.setItems(cClaimantType);
        cmbCheckStatus.setItems(cCheckStatus);
        cmbOtherPayment.setItems(cOtherPayment);
        cmbOtherPaymentBTransfer.setItems(cOtherPaymentBTransfer);

    }

    private void loadTableDetail() {
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
                            Logger.getLogger(DisbursementVoucher_ViewController.class
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

    private void initTableOnClick() {
        tblVwDetails.setOnMouseClicked(event -> {
            if (!detailsdv_data.isEmpty()) {
                if (event.getClickCount() == 1) {
                    pnDetailDV = tblVwDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetailDV();
                    if (poDisbursementController.Detail(pnDetailDV).getAmount() != 0.0000) {
                        tfTaxCodeDetail.requestFocus();
                    } else {
                        tfPurchasedAmountDetail.requestFocus();
                    }
                }
            }
        });
        tblVwDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);

    }

    private void tableKeyEvents(KeyEvent event) {
        if (!detailsdv_data.isEmpty()) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblVwDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnDetailDV = JFXUtil.moveToNextRow(currentTable);
                                break;
                            case UP:
                                pnDetailDV = JFXUtil.moveToPreviousRow(currentTable);
                                break;
                            default:
                                break;
                        }
                        loadRecordDetailDV();
                        event.consume();
                    }
                    break;
            }
        }
    }

}
