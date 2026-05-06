/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javax.script.ScriptException;
import org.guanzon.cas.purchasing.controller.PurchaseOrder;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReturn;
import org.guanzon.cas.purchasing.services.PurchaseOrderControllers;
import org.guanzon.cas.purchasing.services.PurchaseOrderReturnControllers;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

/**
 *
 * @author Team 1
 */
public class SIPosting_ViewController implements Initializable {

    private GRiderCAS oApp;
    static PurchaseOrderReceiving poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    int pnJEDetail = 0;
    int pnDetail = 0;
    private String psTransactionNo = "";

    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> JEdetails_data = FXCollections.observableArrayList();
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    boolean lbSelectTabJE = false;

    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();

    private Stage dialogStage = null;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    JFXUtil.StageManager stageSerial = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;

    @FXML
    private AnchorPane apMainAnchor, apButton, apMaster, apDetail, apJEMaster, apJEDetail, apBrowse;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnHistory, btnClose;
    @FXML
    private TabPane tabPaneForm;
    @FXML
    private Tab tabSIPosting, tabJE;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfBranch, tfTrucking, tfTerm, tfReferenceNo, tfSINo, tfTransactionTotal, tfDiscountRate, tfDiscountAmount, tfFreightAmt, tfVatRate, tfVatSales, tfVatAmount, tfZeroVatSales, tfVatExemptSales, tfNetTotal, tfAdvancePayment, tfOrderNo, tfBarcode, tfDescription, tfSupersede, tfMeasure, tfOrderQuantity, tfReceiveQuantity, tfSRPAmount, tfDiscRateDetail, tfAddlDiscAmtDetail, tfCost, tfJETransactionNo, tfTotalCreditAmt, tfTotalDebitAmt, tfJEAcctCode, tfJEAcctDescription, tfCreditAmt, tfDebitAmt;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpSIDate, dpExpiryDate, dpJETransactionDate, dpReportMonthYear;
    @FXML
    private Label lblStatus, lblJEStatus, lblSource;
    @FXML
    private CheckBox cbVatInclusive, cbVatable, cbJEReverse;
    @FXML
    private TextArea taRemarks, taJERemarks;
    @FXML
    private TableView tblViewTransDetailList, tblViewJEDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail, tblJERowNoDetail, tblReportMonthDetail, tblJEAcctCodeDetail, tblJEAcctDescriptionDetail, tblJECreditAmtDetail, tblJEDebitAmtDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            poJSON = new JSONObject();
            poController = new PurchaseOrderReceivingControllers(oApp, null).PurchaseOrderReceiving();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                CommonUtils.closeStage(btnClose);
            }
            initTextFields();
            initDatePickers();
            initDetailsGrid();
            initJEDetailsGrid();
            initTableOnClick();
            initTabSelection();
            clearTextFields();
            
            Platform.runLater(() -> {
                try {
                    poJSON = poController.OpenTransaction(psTransactionNo);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        CommonUtils.closeStage(btnClose);
                    } 
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                    loadRecordSearch();
                    loadTableDetail();
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            });
    }
    
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }
    
    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }
    
    public void loadRecordSearch() {
        try {
            if (poController.Master().Industry().getDescription() != null && !"".equals(poController.Master().Industry().getDescription())) {
                lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            } else {
                lblSource.setText(poController.Master().Company().getCompanyName() + " - General");
            }
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void populateJE() {
        try {
            JSONObject pnJSON = new JSONObject();
            JFXUtil.setValueToNull(dpJETransactionDate, dpReportMonthYear);
            JFXUtil.clearTextFields(apJEMaster, apJEDetail);
            pnJSON = poController.populateJournal();
            if (JFXUtil.isJSONSuccess(pnJSON)) {
                loadTableJEDetail();
            } else {
                lblJEStatus.setText("UNKNOWN");
                JEdetails_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initTabSelection() {
        tabJE.setOnSelectionChanged(event -> {
            if (tabJE.isSelected()) {
                lbSelectTabJE = true;
                populateJE();
            }
        });
    }

    private void closeDialog() {
        if (stageAttachment != null) {
            stageAttachment.closeDialog();
        }
        if (stageSerial != null) {
            stageSerial.closeDialog();
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        CommonUtils.closeStage(btnClose);
                    } else {
                        return;
                    }
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                        return;
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }
        }

    }
    
    public void loadRecordJEDetail() {
        try {
            //DISABLING
            if (!JFXUtil.isObjectEqualTo(poController.Journal().Detail(pnJEDetail).getAccountCode(), null, "")) {
                JFXUtil.setDisabled(poController.Journal().Detail(pnJEDetail).getEditMode() != EditMode.ADDNEW, tfJEAcctCode, tfJEAcctDescription);
            } else {
                JFXUtil.setDisabled(false, tfJEAcctCode, tfJEAcctDescription);
            }
            boolean lbNotZero = poController.Journal().Detail(pnJEDetail).getDebitAmount() > 0 || poController.Journal().Detail(pnJEDetail).getCreditAmount() > 0;
            cbJEReverse.selectedProperty().set(lbNotZero);
            tfJEAcctCode.setText(poController.Journal().Detail(pnJEDetail).getAccountCode());
            tfJEAcctDescription.setText(poController.Journal().Detail(pnJEDetail).Account_Chart().getDescription());
            String lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poController.Journal().Detail(pnJEDetail).getForMonthOf());
            dpReportMonthYear.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd"));
            tfCreditAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnJEDetail).getCreditAmount(), true));
            tfDebitAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(pnJEDetail).getDebitAmount(), true));
            JFXUtil.updateCaretPositions(apJEDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            // Expiry Date
            String lsExpiryDate = CustomCommonUtil.formatDateToShortString(poController.Detail(pnDetail).getExpiryDate());
            dpExpiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsExpiryDate, "yyyy-MM-dd"));

            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poController.Detail(pnDetail).Supersede().getBarCode());
            tfMeasure.setText(poController.Detail(pnDetail).Inventory().Measure().getDescription());
            tfOrderNo.setText(poController.Detail(pnDetail).getOrderNo());
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getUnitPrce(), true));
            Platform.runLater(() -> {
                double lnValue = 0.00;
                if (poController.Detail(pnDetail).getDiscountRate() != null) {
                    lnValue = poController.Detail(pnDetail).getDiscountRate().doubleValue();
                }
                tfDiscRateDetail.setText(String.format("%.2f", Double.isNaN(lnValue) ? 0.00 : lnValue));
            });
            double ldblDiscountRate = poController.Detail(pnDetail).getUnitPrce().doubleValue()
                    * (poController.Detail(pnDetail).getDiscountRate().doubleValue() / 100);
            tfAddlDiscAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getDiscountAmount(), true));
            double lnTotal = poController.Detail(pnDetail).getUnitPrce().doubleValue()
                    + poController.Detail(pnDetail).getDiscountAmount().doubleValue()
                    + ldblDiscountRate;
            tfSRPAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true));
            tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getOrderQty().doubleValue()));
            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getQuantity().doubleValue()));

            cbVatable.setSelected(poController.Detail(pnDetail).isVatable());

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordJEMaster() {
        JFXUtil.setStatusValue(lblJEStatus, JournalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Journal().Master().getTransactionStatus());
        if (poController.Journal().Master().getTransactionNo() != null) {
            tfJETransactionNo.setText(poController.Journal().Master().getTransactionNo());
            String lsJETransactionDate = CustomCommonUtil.formatDateToShortString(poController.Journal().Master().getTransactionDate());
            dpJETransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsJETransactionDate, "yyyy-MM-dd"));

            taJERemarks.setText(poController.Journal().Master().getRemarks());
            double lnTotalDebit = 0;
            double lnTotalCredit = 0;
            for (int lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
                lnTotalDebit += poController.Journal().Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poController.Journal().Detail(lnCtr).getCreditAmount();
            }

            tfTotalCreditAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            tfTotalDebitAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            JFXUtil.updateCaretPositions(apJEMaster);
        }
    }

    public void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(PurchaseOrderReceivingStatus.POSTED, "POSTED");
                statusMap.put(PurchaseOrderReceivingStatus.PAID, "PAID");
                statusMap.put(PurchaseOrderReceivingStatus.CONFIRMED, "CONFIRMED");
                statusMap.put(PurchaseOrderReceivingStatus.OPEN, "OPEN");
                statusMap.put(PurchaseOrderReceivingStatus.RETURNED, "RETURNED");
                statusMap.put(PurchaseOrderReceivingStatus.VOID, "VOIDED");
                statusMap.put(PurchaseOrderReceivingStatus.CANCELLED, "CANCELLED");

                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN");
                lblStatus.setText(lsStat);
            });

            if (poController.Master().getDiscountRate().doubleValue() > 0.00) {
                poController.computeDiscount(poController.Master().getDiscountRate().doubleValue());
            } else {
                if (poController.Master().getDiscount().doubleValue() > 0.00) {
                    poController.computeDiscountRate(poController.Master().getDiscount().doubleValue());
                }
            }
            poController.computeFields();

            tfTransactionNo.setText(poController.Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfSupplier.setText(poController.Master().Supplier().getCompanyName());
            tfBranch.setText(poController.Master().Branch().getBranchName());
            tfTrucking.setText(poController.Master().Trucking().getCompanyName());

            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.Master().getReferenceDate());
            String lsSIDate = CustomCommonUtil.formatDateToShortString(poController.Master().getSalesInvoiceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));
            dpSIDate.setValue(JFXUtil.isObjectEqualTo(lsSIDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsSIDate, "yyyy-MM-dd"));
            tfReferenceNo.setText(poController.Master().getReferenceNo());
            boolean lbShow = "To-follow".equals(poController.Master().getSalesInvoice());
            JFXUtil.setDisabled(lbShow, tfSINo);
            if (lbShow) {
                tfSINo.setTextFormatter(null);
            } else {
                CustomCommonUtil.inputIntegersOnly(tfSINo);
            }
            tfSINo.setText(poController.Master().getSalesInvoice());
            tfTerm.setText(poController.Master().Term().getDescription());
            taRemarks.setText(poController.Master().getRemarks());

            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(getGrossTotal(), true));
            Platform.runLater(() -> {
                double lnValue = poController.Master().getDiscountRate().doubleValue();
                tfDiscountRate.setText(String.format("%.2f", Double.isNaN(lnValue) ? 0.00 : lnValue));
                double lnVat = poController.Master().getVatRate().doubleValue();
                tfVatRate.setText(String.format("%.2f", Double.isNaN(lnVat) ? 0.00 : lnVat));
            });
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscount().doubleValue(), true));
            tfFreightAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getFreight().doubleValue()));

            cbVatInclusive.setSelected(poController.Master().isVatTaxable());
            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatSales().doubleValue(), true));
            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatAmount().doubleValue(), true));
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getZeroVatSales().doubleValue(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatExemptSales().doubleValue(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getNetTotal(), true));
            tfAdvancePayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getAdvancePayment(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private double getGrossTotal() {
        double ldblGrossTotal = 0.0000;
        for (int lnCtr = 0; lnCtr <= poController.getDetailCount() - 1; lnCtr++) {
            ldblGrossTotal = ldblGrossTotal + (poController.Detail(lnCtr).getUnitPrce().doubleValue()
                    * poController.Detail(lnCtr).getQuantity().doubleValue());
        }
        return ldblGrossTotal;
    }

    public void loadTableJEDetail() {
//        pbEntered = false;
        // Setting data to table detail

        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewJEDetails.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    JEdetails_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            poController.Journal().ReloadDetail();
                        }

                        String lsReportMonthYear = "";
                        String lsAcctCode = "";
                        String lsAccDesc = "";
                        int lnRowCount = 0;
                        for (lnCtr = 0; lnCtr < poController.Journal().getDetailCount(); lnCtr++) {
                            lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poController.Journal().Detail(lnCtr).getForMonthOf());
                            lsAcctCode = poController.Journal().Detail(lnCtr).getAccountCode();
                            lsAccDesc = poController.Journal().Detail(lnCtr).Account_Chart().getDescription();
                            if (lsAcctCode == null) {
                                lsAcctCode = "";
                            }
                            if (lsAccDesc == null) {
                                lsAccDesc = "";
                            }
                            if (poController.Journal().Detail(lnCtr).getCreditAmount() <= 0.0000
                                    && poController.Journal().Detail(lnCtr).getDebitAmount() <= 0.0000
                                    && !"".equals(lsAcctCode)
                                    && poController.Journal().Detail(lnCtr).getEditMode() != EditMode.ADDNEW) {
                                continue;
                            }
                            lnRowCount += 1;
                            JEdetails_data.add(
                                    new ModelJournalEntry_Detail(
                                            String.valueOf(lnRowCount),
                                            String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd")),
                                            String.valueOf(lsAcctCode),
                                            String.valueOf(lsAccDesc),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getCreditAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Journal().Detail(lnCtr).getDebitAmount(), true)),
                                            String.valueOf(lnCtr)
                                    ));

                            lsReportMonthYear = "";
                            lsAcctCode = "";
                            lsAccDesc = "";
                        }
                        int lnTempRow = JFXUtil.getDetailRow(JEdetails_data, pnJEDetail, 7); //this method is used only when Reverse is applied
                        if (lnTempRow < 0 || lnTempRow
                                >= JEdetails_data.size()) {
                            if (!JEdetails_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewJEDetails, 0);
                                int lnRow = Integer.parseInt(JEdetails_data.get(0).getIndex07());
                                pnJEDetail = lnRow;
                                loadRecordJEDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewJEDetails, lnTempRow);
                            int lnRow = Integer.parseInt(JEdetails_data.get(tblViewJEDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                            pnJEDetail = lnRow;
                            loadRecordJEDetail();
                        }
                        loadRecordJEMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (JEdetails_data == null || JEdetails_data.isEmpty()) {
                    tblViewJEDetails.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewJEDetails.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (JEdetails_data == null || JEdetails_data.isEmpty()) {
                    tblViewJEDetails.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void loadTableDetail() {
        // Setting data to table detail
        JFXUtil.disableAllHighlight(tblViewTransDetailList, highlightedRowsDetail);

        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewTransDetailList.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    try {
                        double lnTotal = 0.00;
                        double lnDiscountAmt = 0.00;
                        for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                            if (JFXUtil.isObjectEqualTo(poController.Master().getSalesInvoice(), null, "")) {
                                poController.Detail(lnCtr).isVatable(false);
                                JFXUtil.setDisabled(true, cbVatable);
                            } else {
                                JFXUtil.setDisabled(false, cbVatable);
                            }

                            try {
                                lnTotal = poController.Detail(lnCtr).getUnitPrce().doubleValue() * poController.Detail(lnCtr).getQuantity().doubleValue();
                                lnDiscountAmt = poController.Detail(lnCtr).getDiscountAmount().doubleValue()
                                        + (lnTotal * (poController.Detail(lnCtr).getDiscountRate().doubleValue() / 100));
                            } catch (Exception e) {
                            }

                            if ((!poController.Detail(lnCtr).getOrderNo().equals("") && poController.Detail(lnCtr).getOrderNo() != null)
                                    && poController.Detail(lnCtr).getOrderQty().doubleValue() != poController.Detail(lnCtr).getQuantity().doubleValue()
                                    && poController.Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                JFXUtil.highlightByKey(tblViewTransDetailList, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                            }

                            if (poController.Detail(lnCtr).getStockId() != null && !"".equals(poController.Detail(lnCtr).getStockId())) {
                                details_data.add(
                                        new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.Detail(lnCtr).getOrderNo()),
                                                String.valueOf(poController.Detail(lnCtr).Inventory().getBarCode()),
                                                String.valueOf(poController.Detail(lnCtr).Inventory().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getUnitPrce(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getOrderQty().doubleValue())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getQuantity().doubleValue())),
                                                //                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnDiscountAmt, true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                        ));
                            }
                        }
                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewTransDetailList, 0);
                                pnDetail = tblViewTransDetailList.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewTransDetailList, pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewTransDetailList.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
                dpTransactionDate, dpReferenceDate, dpSIDate, dpExpiryDate, dpJETransactionDate, dpReportMonthYear);
    }

    public void initTextFields() {

        JFXUtil.setCommaFormatter(tfDiscountAmount, tfFreightAmt, tfVatSales,
                tfVatAmount, tfZeroVatSales, tfVatExemptSales, tfCost, tfCreditAmt,
                tfDebitAmt, tfAddlDiscAmtDetail, tfSRPAmount);

        CustomCommonUtil.inputIntegersOnly(tfSINo);
        CustomCommonUtil.inputDecimalOnly(tfReceiveQuantity, tfDiscountRate, tfDiscRateDetail, tfVatRate);
        // Combobox
        JFXUtil.setCheckboxHoverCursor(apMaster, apDetail);
    }

    public void initTableOnClick() {
        tblViewJEDetails.setOnMouseClicked(event -> {
            ModelJournalEntry_Detail selected = (ModelJournalEntry_Detail) tblViewJEDetails.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int lnRow = Integer.parseInt(JEdetails_data.get(tblViewJEDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnJEDetail = lnRow;
                loadRecordJEDetail();
                if (JFXUtil.isObjectEqualTo(poController.Journal().Detail(pnJEDetail).getAccountCode(), null, "")) {
                    tfJEAcctCode.requestFocus();
                } else {
                    if (poController.Journal().Detail(pnJEDetail).getCreditAmount() > 0) {
                        tfCreditAmt.requestFocus();
                    } else {
                        tfDebitAmt.requestFocus();
                    }
                }
            }
        });

        tblViewTransDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewTransDetailList.getSelectionModel().getSelectedItem();
                switch(event.getClickCount()){
                    case 1:
                        if (selected != null) {
                            stageSerial.closeDialog();
                            pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                            loadRecordDetail();
                            tfCost.requestFocus();
                        }
                    break;
                    case 2:
                        if (selected != null) {
                            try {
                                pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                                if(poController.Detail(pnDetail).getOrderNo() != null && !"".equals(poController.Detail(pnDetail).getOrderNo())){
                                    switch(poController.Master().getPurpose()){
                                        case PurchaseOrderReceivingStatus.Purpose.REGULAR:
                                            //load order history
                                            PurchaseOrder loPOController = new PurchaseOrderControllers(oApp, null).PurchaseOrder();
                                            poJSON = loPOController.InitTransaction(); // Initialize transaction
                                            if (!"success".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                CommonUtils.closeStage(btnClose);
                                            }
                                            poJSON = loPOController.OpenTransaction(poController.Detail(pnDetail).getOrderNo()); 
                                            if (!"success".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                return;
                                            }
                                            try {
                                                loPOController.ShowStatusHistory();
                                            } catch (NullPointerException npe) {
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                                                ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                                            } catch (Exception ex) {
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                                ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                                            }
                                        break;
                                        case PurchaseOrderReceivingStatus.Purpose.REPLACEMENT:
                                            //load order history
                                            PurchaseOrderReturn loPOReturnController = new PurchaseOrderReturnControllers(oApp, null).PurchaseOrderReturn();
                                            poJSON = loPOReturnController.InitTransaction(); // Initialize transaction
                                            if (!"success".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                CommonUtils.closeStage(btnClose);
                                            }
                                            poJSON = loPOReturnController.OpenTransaction(poController.Detail(pnDetail).getOrderNo()); 
                                            if (!"success".equals((String) poJSON.get("result"))) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                                return;
                                            }
                                            try {
                                                loPOReturnController.ShowStatusHistory();
                                            } catch (NullPointerException npe) {
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                                                ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                                            } catch (Exception ex) {
                                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                                ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                                            }
                                        break;

                                    }
                                }
                                
                            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName,MiscUtil.getException(ex));
                            }
                        }
                    break;
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewTransDetailList, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetailList, tblViewJEDetails);
    }

    private void initButton(int fnValue) {
        boolean lbShow = fnValue == EditMode.READY;

        JFXUtil.setDisabled(true, apMaster, apDetail, apJEMaster, apJEDetail);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow, btnHistory);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail);
        JFXUtil.setColumnLeft(tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetailList);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetailList.comparatorProperty());
        tblViewTransDetailList.setItems(sortedData);
        tblViewTransDetailList.autosize();
    }

    public void initJEDetailsGrid() {
        JFXUtil.setColumnCenter(tblJERowNoDetail, tblReportMonthDetail);
        JFXUtil.setColumnLeft(tblJEAcctCodeDetail, tblJEAcctDescriptionDetail);
        JFXUtil.setColumnRight(tblJECreditAmtDetail, tblJEDebitAmtDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewJEDetails);

        tblViewJEDetails.setItems(JEdetails_data);
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblViewTransDetailList":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnDetail = JFXUtil.moveToNextRow(currentTable);
                                break;
                            case UP:
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                break;

                            default:
                                break;
                        }
                        loadRecordDetail();
                        event.consume();
                    }
                    break;
                case "tblViewJEDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnJEDetail = Integer.parseInt(JEdetails_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07());
                                break;
                            case UP:
                                pnJEDetail = Integer.parseInt(JEdetails_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                                break;

                            default:
                                break;
                        }
                        loadRecordJEDetail();
                        event.consume();
                    }
                    break;
            }
        }
    }

    public void clearTextFields() {
        Platform.runLater(() -> {
            imageinfo_temp.clear();
            JFXUtil.clearTextFields(apMaster, apDetail, apJEDetail, apJEMaster);
            closeDialog();
        });
    }
}
