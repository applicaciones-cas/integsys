/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.integsys.model.ModelBIR_Detail;

/**
 * FXML Controller class
 *
 * @author Team 1 & Team 2
 */
public class DisbursementVoucher_ViewController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private int pnDetail = 0;
    private int pnDetailBIR = 0;
    private final String pxeModuleName = "Disbursement Voucher View";
    private DisbursementVoucher poController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psTransactionNo = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Detail> filteredDataDetailDV;

    private ObservableList<ModelBIR_Detail> BIR_data = FXCollections.observableArrayList();
    private FilteredList<ModelJournalEntry_Detail> filteredJournal_Data;

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableDetailBIR;

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
    private AnchorPane AnchorMain, apButton, apMasterDetail, apDVMaster1, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apDVMaster2, apDVMaster3, apDVDetail, apBIRDetail;
    @FXML
    private StackPane StackPane;
    @FXML
    private Button btnClose;
    @FXML
    private TabPane tabPaneMain, tabPanePaymentMode;
    @FXML
    private Tab tabDisbursement, tabCheck, tabBankTransfer, tabOnlinePayment, tabBIR;
    @FXML
    private TextField tfDVTransactionNo, tfVoucherNo, tfSupplier, tfCheckNo, tfCheckAmount, tfBankNameCheck, tfBankAccountCheck, tfPayeeName, tfAuthorizedPerson, tfPaymentAmountBTransfer, tfBankTransReferNo, tfSupplierBank, tfSupplierAccountNoBTransfer, tfBankNameBTransfer, tfBankAccountBTransfer, tfPaymentAmount, tfPaymentReferenceNo, tfSupplierServiceName, tfSupplierAccountNo, tfBankNameOnlinePayment, tfBankAccountOnlinePayment, tfVatAmountMaster, tfVatExemptSales, tfLessWHTax, tfTotalNetAmount, tfTotalAmount, tfVatZeroRatedSales, tfVatableSales, tfRefNoDetail, tfVatableSalesDetail, tfVatExemptDetail, tfVatZeroRatedSalesDetail, tfVatRateDetail, tfVatAmountDetail, tfPurchasedAmountDetail, tfNetAmountDetail, tfBIRTransactionNo, tfTotalTaxAmount, tfBaseAmount, tfTaxRate, tfTaxCode, tfParticular;
    @FXML
    private DatePicker dpDVTransactionDate, dpCheckDate, dpPeriodFrom, dpPeriodTo;
    @FXML
    private ComboBox cmbPaymentMode, cmbPayeeType, cmbDisbursementMode, cmbClaimantType, cmbCheckStatus, cmbOtherPaymentBTransfer, cmbOtherPayment;
    @FXML
    private CheckBox chbkPrintByBank, chbkIsCrossCheck, chbkIsPersonOnly, chbkVatClassification;
    @FXML
    private Label lblDVTransactionStatus, lblBIRTransactionStatus;
    @FXML
    private TextArea taDVRemarks;
    @FXML
    private TableView tblVwDetails, tblVwBIRDetails;
    @FXML
    private TableColumn tblDVRowNo, tblReferenceNo, tblTransactionTypeDetail, tblPurchasedAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales, tblNetAmount, tblBIRRowNo, tblBIRParticular, tblTaxCode, tblTaxRate, tblTaxAmount;

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

    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }

    public void setDisbursement(DisbursementVoucher foValue) {
        poController = foValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!psTransactionNo.isEmpty()) {
            try {
                poController = new CashflowControllers(oApp, null).DisbursementVoucher();
                poJSON = new JSONObject();
                poJSON = poController.InitTransaction(); // Initialize transaction
                if (!"success".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                }
                poJSON = poController.OpenTransaction(psTransactionNo);
                if (!"error".equals((String) poJSON.get("result"))) {
                    if (poController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
//                        poController.setCheckpayment();
                    }
                    initLoadTable();
                    initButtonsClickActions();
                    initComboBoxes();
                    initDatePicker();
                    initDetailGrid();
                    initDetailBIRGrid();
                    initTableOnClick();
                    initTabPane();
                    clearTextFields();
                    pnEditMode = EditMode.UNKNOWN;
                    loadTableDetail.reload();
                    initButton(pnEditMode);
                } else {
                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                    CommonUtils.closeStage(btnClose);
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        initTableOnClick();
        btnClose.setOnAction(this::cmdButton_Click);
    }

    public void initTabPane() {
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
        tabPaneMain.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String tabTitle = newTab.getText();
                switch (tabTitle) {
                    case "Disbursement Voucher":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            pnDetailBIR = 0;
                        }
                        break;
                    case "BIR 2307":
                        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                            populateBIR();
                        }
                        break;
                }
            }
        });
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
        }
    }

    //Disables/ Enables tabs
    private void initDVMasterTabs() {
        JFXUtil.setDisabled(true, tabCheck, tabOnlinePayment, tabBankTransfer);
        switch (poController.Master().getDisbursementType()) {
            case DisbursementStatic.DisbursementType.CHECK:
                JFXUtil.setDisabled(false, tabCheck);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Check");
                loadRecordMasterCheck();
                //must reset data of check
                break;
            case DisbursementStatic.DisbursementType.WIRED:
                JFXUtil.setDisabled(false, tabBankTransfer);
                JFXUtil.clickTabByTitleText(tabPanePaymentMode, "Bank Transfer");
                loadRecordMasterBankTransfer();
                //must reset data of btransfer
                break;
            case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                JFXUtil.setDisabled(false, tabOnlinePayment);
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
        List<Button> buttons = Arrays.asList(btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnClose":
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                    CommonUtils.closeStage(btnClose);
                } else {
                    return;
                }
                break;
            default:
                ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered");
                break;
        }

        loadRecordMaster();
        loadTableDetail.reload();
        initButton(pnEditMode);
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblVwDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            details_data.clear();
                            int lnCtr;
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lnCtr = poController.getDetailCount() - 1;
                                while (lnCtr >= 0) {
                                    if (poController.Detail(lnCtr).getSourceNo() == null || poController.Detail(lnCtr).getSourceNo().equals("")) {
                                        poController.Detail().remove(lnCtr);
                                    }
                                    lnCtr--;
                                }

                                if ((poController.getDetailCount() - 1) >= 0) {
                                    if (poController.Detail(poController.getDetailCount() - 1).getSourceNo() != null && !poController.Detail(poController.getDetailCount() - 1).getSourceNo().equals("")) {
                                        poController.AddDetail();
                                    }
                                }

                                if ((poController.getDetailCount() - 1) < 0) {
                                    poController.AddDetail();
                                }
                            }

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lnCtr = poController.getDetailCount() - 1;
                                if (lnCtr >= 0) {
                                    String lsSourceNo = poController.Detail(lnCtr).getSourceNo();
                                    if (!lsSourceNo.isEmpty() || poController.Detail(lnCtr).getSourceNo() == null) {
                                        try {
                                            poController.AddDetail();

                                        } catch (CloneNotSupportedException ex) {
                                            Logger.getLogger(getClass()
                                                    .getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }

                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                details_data.add(
                                        new ModelDisbursementVoucher_Detail(String.valueOf(lnCtr + 1),
                                                poController.Detail(lnCtr).getSourceNo(),
                                                JFXUtil.getSourceType(poController.Detail(lnCtr).getSourceCode(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getAmountApplied(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatSales(), true),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatAmount(), true)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatRates(), false),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailZeroVat(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDetailVatExempt(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getDetailNetTotal(lnCtr), true)
                                        ));
                            }
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwDetails, 0);
                                    pnDetail = tblVwDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwDetails, pnDetail);
                                loadRecordDetail();
                            }

                            loadRecordMaster();

                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                });
        loadTableDetailBIR = new JFXUtil.ReloadableTableTask(
                tblVwBIRDetails,
                BIR_data,
                () -> {
                    Platform.runLater(() -> {
                        BIR_data.clear();
                        int lnCtr;
                        try {

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lnCtr = poController.getWTaxDeductionsCount() - 1;
                                while (lnCtr >= 0) {
                                    if (poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode() == null
                                            || poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode().equals("")) {
                                        poController.WTaxDeduction().remove(lnCtr);
                                    }
                                    lnCtr--;
                                }

                                if ((poController.getWTaxDeductionsCount() - 1) >= 0) {
                                    if (poController.WTaxDeduction(poController.getWTaxDeductionsCount() - 1).getModel().getTaxCode() != null
                                            && !poController.WTaxDeduction(poController.getWTaxDeductionsCount() - 1).getModel().getTaxCode().equals("")) {
                                        poController.AddWTaxDeduction();
                                    }
                                }

                                if ((poController.getWTaxDeductionsCount() - 1) < 0) {
                                    poController.AddWTaxDeduction();
                                }
                            }
                            for (lnCtr = 0; lnCtr < poController.getWTaxDeductionsCount(); lnCtr++) {
                                BIR_data.add(new ModelBIR_Detail(String.valueOf(lnCtr + 1),
                                        poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription(),
                                        poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode(),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().getTaxRate(), false),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxAmount(), false))
                                );

                            }
                            if (pnDetailBIR < 0 || pnDetailBIR
                                    >= BIR_data.size()) {
                                if (!BIR_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwBIRDetails, 0);
                                    pnDetailBIR = tblVwBIRDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetailBIR();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwBIRDetails, pnDetailBIR);
                                loadRecordDetailBIR();
                            }
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblDVRowNo, tblReferenceNo);
        JFXUtil.setColumnLeft(tblTransactionTypeDetail);
        JFXUtil.setColumnRight(tblPurchasedAmount, tblNetAmount, tblVatableSales, tblVatAmt, tblVatRate, tblVatZeroRatedSales, tblVatExemptSales);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetails);
        tblVwDetails.setItems(details_data);
    }

    private void initDetailBIRGrid() {
        JFXUtil.setColumnCenter(tblBIRRowNo);
        JFXUtil.setColumnLeft(tblBIRParticular, tblTaxCode);
        JFXUtil.setColumnRight(tblTaxRate, tblTaxAmount);
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
                }
            }
        });
        tblVwBIRDetails.setOnMouseClicked(event -> {
            if (!BIR_data.isEmpty() && event.getClickCount() == 1) {
                pnDetailBIR = tblVwBIRDetails.getSelectionModel().getSelectedIndex();
            }
        }
        );
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblVwDetails, tblVwBIRDetails);
        JFXUtil.adjustColumnForScrollbar(tblVwDetails, tblVwBIRDetails);
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
            switch (currentTable.getId()) {
                case "tblVwDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;
                case "tblVwBIRDetails":
                    if (BIR_data.isEmpty()) {
                        return;
                    }
                    pnDetailBIR = newIndex;
                    loadRecordDetailBIR();
                    break;
            }
            event.consume();
        }
    }

    private void loadRecordMaster() {
        try {
            initDVMasterTabs();
            poJSON = new JSONObject();
            poController.computeTaxAmount();
            poJSON = poController.computeFields();
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }
            JFXUtil.setStatusValue(lblDVTransactionStatus, DisbursementStatic.class, poController.Master().getTransactionStatus());

            tfDVTransactionNo.setText(poController.Master().getTransactionNo() != null ? poController.Master().getTransactionNo() : "");
            dpDVTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));

            JFXUtil.setCmbValue(cmbPaymentMode, !poController.Master().getDisbursementType().equals("") ? Integer.valueOf(poController.Master().getDisbursementType()) : -1);

            tfVoucherNo.setText(poController.Master().getVoucherNo());
            tfSupplier.setText(poController.Master().Payee().Client().getCompanyName() != null ? poController.Master().Payee().Client().getCompanyName() : "");

            poJSON = poController.computeFields();
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                return;
            }
//            poJSON = poController.modeOfPayment(poController.Master().getDisbursementType());
//            if ("error".equals((String) poJSON.get("message"))) {
//                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
//            }

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
        }
    }

    private void loadRecordDetail() {
        if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
            return;
        }
        poJSON = poController.computeDetailFields();
        if ("error".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
            return;
        }
        tfRefNoDetail.setText(poController.Detail(pnDetail).getSourceNo());
        chbkVatClassification.setSelected(poController.Detail(pnDetail).isWithVat());
        tfVatableSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatSales(), true));
        tfVatExemptDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatExempt(), true));
        tfVatZeroRatedSalesDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailZeroVat(), true));
        tfVatRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatRates(), false));
        tfVatAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDetailVatAmount(), true));
        tfPurchasedAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getAmountApplied(), true));
        tfNetAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getDetailNetTotal(pnDetail), true));
        JFXUtil.updateCaretPositions(apDVDetail);

    }

    public void loadRecordDetailBIR() {
        try {
            if (pnDetailBIR < 0 || pnDetailBIR > poController.getWTaxDeductionsCount() - 1) {
                return;
            }
            tfBIRTransactionNo.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTransactionNo());
            String lsPeriodFromDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
            dpPeriodFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodFromDate, "yyyy-MM-dd"));

            String lsPeriodToDate = CustomCommonUtil.formatDateToShortString(poController.WTaxDeduction(pnDetailBIR).getModel().getPeriodFrom());
            dpPeriodTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsPeriodToDate, "yyyy-MM-dd"));

            tfTaxCode.setText(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxCode());
            tfParticular.setText(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().AccountChart().getDescription());
            tfBaseAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getBaseAmount(), false));
            tfTaxRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().WithholdingTax().getTaxRate()));
            tfTotalTaxAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.WTaxDeduction(pnDetailBIR).getModel().getTaxAmount(), false));
            JFXUtil.updateCaretPositions(apBIRDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterCheck() {
        try {
            tfCheckNo.setText(poController.CheckPayments().getModel().getCheckNo());
            if (JFXUtil.isObjectEqualTo(poController.CheckPayments().getModel().getCheckNo(), null, "")) {
                poController.CheckPayments().getModel().setCheckDate(null);
            }
            dpCheckDate.setValue(poController.CheckPayments().getModel().getCheckDate() != null
                    ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                    : null);
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

            JFXUtil.updateCaretPositions(apMasterDVCheck);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterBankTransfer() {
        try {
            tfBankNameBTransfer.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfBankAccountBTransfer.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            tfPaymentAmountBTransfer.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.OtherPayments().getModel().getTotalAmount(), true));
            tfSupplierBank.setText(poController.CheckPayments().getModel().Supplier().getCompanyName() != null ? poController.CheckPayments().getModel().Supplier().getCompanyName() : "");
            tfSupplierAccountNoBTransfer.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");

            tfBankTransReferNo.setText(poController.OtherPayments().getModel().getReferNox() != null ? poController.OtherPayments().getModel().getReferNox() : "");
            JFXUtil.setCmbValue(cmbOtherPaymentBTransfer, !poController.OtherPayments().getModel().getTransactionStatus().equals("") ? Integer.valueOf(poController.OtherPayments().getModel().getTransactionStatus()) : -1);

            JFXUtil.updateCaretPositions(apMasterDVBTransfer);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
        }
    }

    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cPaymentMode, cmbPaymentMode), new JFXUtil.Pairs<>(cPayeeType, cmbPayeeType),
                new JFXUtil.Pairs<>(cDisbursementMode, cmbDisbursementMode), new JFXUtil.Pairs<>(cClaimantType, cmbClaimantType),
                new JFXUtil.Pairs<>(cCheckStatus, cmbCheckStatus), new JFXUtil.Pairs<>(cOtherPaymentBTransfer, cmbOtherPaymentBTransfer),
                new JFXUtil.Pairs<>(cOtherPayment, cmbOtherPayment));
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVTransactionDate, dpCheckDate, dpPeriodFrom, dpPeriodTo);
    }

    private void initButton(int fnEditMode) {
        JFXUtil.setDisabled(true, apDVMaster1, apDVMaster2, apDVMaster3, apDVDetail, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apBIRDetail);
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apDVMaster1, apDVDetail, apDVMaster2, apDVMaster3, apMasterDVCheck, apMasterDVBTransfer, apMasterDVOp, apBIRDetail);
    }
}
