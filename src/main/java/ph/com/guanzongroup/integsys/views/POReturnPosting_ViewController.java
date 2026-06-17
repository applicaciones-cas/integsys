/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
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
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.script.ScriptException;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReturn;
import org.guanzon.cas.purchasing.services.PurchaseOrderReturnControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReturnStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

/**
 *
 * @author Team 1
 */
public class POReturnPosting_ViewController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static PurchaseOrderReturn poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private static final int ROWS_PER_PAGE = 50;
    int pnJEDetail = 0;
    int pnDetail = 0;
    int pnMain = 0;
    private String psTransactionNo = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psBranchId = "";
    private String psSearchSupplierId = "";
    private String psSearchBranchId = "";
    private String openedAttachment = "";
    private boolean pbEntered = false;
    private boolean pbEnteredJE = false;

    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> JEdetails_data = FXCollections.observableArrayList();
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    private FileChooser fileChooser;

    private int currentIndex = 0;
    boolean lbSelectTabJE = false;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean tooltipShown = false;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apJEMaster, apJEDetail;
    @FXML
    private Label lblSource, lblStatus, lblJEStatus;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfReferenceNo, tfPOReceivingNo, tfTransactionTotal, tfDiscountRate, tfDiscountAmount, tfFreightAmt, tfVatRate, tfVatSales, tfVatAmount, tfZeroVatSales, tfVatExemptSales, tfNetTotal, tfBarcode, tfDescription, tfSerialNo, tfReceiveQuantity, tfReturnQuantity, tfCost, tfFreightDetail, tfJETransactionNo, tfTotalCreditAmt, tfTotalDebitAmt, tfJEAcctCode, tfJEAcctDescription, tfCreditAmt, tfDebitAmt;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnHistory, btnClose;
    @FXML
    private TabPane tabPaneForm;
    @FXML
    private Tab tabPOReturnPosting, tabJE;
    @FXML
    private DatePicker dpTransactionDate, dpJETransactionDate, dpReportMonthYear;
    @FXML
    private CheckBox cbVatInclusive, cbVatable, cbJEReverse;
    @FXML
    private TextArea taRemarks, taJERemarks;
    @FXML
    private TableView tblViewDetails, tblViewJEDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblReceiveQuantityDetail, tblReturnQuantityDetail, tblTotalDetail, tblJERowNoDetail, tblReportMonthDetail, tblJEAcctCodeDetail, tblJEAcctDescriptionDetail, tblJECreditAmtDetail, tblJEDebitAmtDetail;
    
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableJEDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poController = new PurchaseOrderReturnControllers(oApp, null).PurchaseOrderReturn();
        poJSON = new JSONObject();
        poJSON = poController.InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }

        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initJEDetailsGrid();
        initTableOnClick();
        initTabSelection();
        clearTextFields();
        initLoadTable();
        Platform.runLater(() -> {
            poController.Master().setIndustryId(psIndustryId);
            poController.Master().setCompanyId(psCompanyId);
            poController.setIndustryId(psIndustryId);
            poController.setCompanyId(psCompanyId);
            poController.setCategoryId(psCategoryId);
            poController.isFinance(true);
            poController.initFields();
            poController.setWithUI(true);
            
            
            if(psTransactionNo != null && !"".equals(psTransactionNo)){
                try {
                    poJSON = poController.OpenTransaction(psTransactionNo);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        CommonUtils.closeStage(btnClose);
                    }
                    loadRecordSearch();
                    loadRecordMaster();
                    loadTableDetail.reload();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
    }

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

    private void populateJE() {
        try {
            JSONObject pnJSON = new JSONObject();
            JFXUtil.setValueToNull(dpJETransactionDate, dpReportMonthYear);
            JFXUtil.clearTextFields(apJEMaster, apJEDetail);
            pnJSON = poController.populateJournal();
            if (JFXUtil.isJSONSuccess(pnJSON)) {
                Platform.runLater(() -> {
                    loadTableJEDetail.reload();
                });
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
    
    
    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
            }
        }
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
            if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
                
                Tab currentTab = tabPaneForm.getSelectionModel().getSelectedItem();
                if (currentTab.getId().equals("tabJE")) {
                    populateJE();
                }
            }
            initButton(pnEditMode);
        }
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
//                try {
                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        break;
                    case "dpJETransactionDate":
                        break;
                    case "dpReportMonthYear":
                        if (poController.getEditMode() == EditMode.ADDNEW
                        || poController.getEditMode() == EditMode.UPDATE) {
                            if (pbSuccess) {
                                poController.Journal().Detail(pnJEDetail).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (pbSuccess) {
                } else {
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                }
                pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                if (JFXUtil.isObjectEqualTo(datePicker.getId(), "dpJETransactionDate", "dpReportMonthYear")) {
                    loadTableJEDetail.reload();
                } else {
                    loadRecordMaster();
                }
                pbSuccess = true; //Set to original valueF
//                } catch (SQLException ex) {
//                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//                }
            });
    

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            JFXUtil.setStatusValue(lblStatus, PurchaseOrderReturnStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
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
            tfReferenceNo.setText(poController.Master().PurchaseOrderReceivingMaster().getReferenceNo());

            tfPOReceivingNo.setText(poController.Master().getSourceNo());
            taRemarks.setText(poController.Master().getRemarks());
            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(getGrossTotal()), true));

            Platform.runLater(() -> {
                double lnValue = poController.Master().getDiscountRate().doubleValue();
                tfDiscountRate.setText(String.format("%.2f", Double.isNaN(lnValue) ? 0.00 : lnValue));
                double lnVat = poController.Master().getVatRate().doubleValue();
                tfVatRate.setText(String.format("%.2f", Double.isNaN(lnVat) ? 0.00 : lnVat));
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscount().doubleValue(), true));
            cbVatInclusive.setSelected(poController.Master().isVatTaxable());

            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatSales().doubleValue(), true));

            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatAmount().doubleValue(), true));
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getZeroVatSales().doubleValue(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getVatExemptSales().doubleValue(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getNetTotal(), true));
            tfFreightAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getFreight().doubleValue()));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            tfReturnQuantity.setText(String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getQuantity())));
            String lsSerialNo = poController.Detail(pnDetail).InventorySerial().getSerial01();
            if(lsSerialNo == null || "".equals(lsSerialNo)){
                lsSerialNo = "";
            }
            tfSerialNo.setText(lsSerialNo);
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getUnitPrce(), true));
            tfReceiveQuantity.setText(String.valueOf( CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getReceiveQty(pnDetail), false)));
            cbVatable.setSelected(poController.Detail(pnDetail).isVatable());
            tfFreightDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getFreight().doubleValue(), false));
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
                if (!poController.Journal().Detail(lnCtr).isReverse()) {
                    continue;
                }
                lnTotalDebit += poController.Journal().Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poController.Journal().Detail(lnCtr).getCreditAmount();
            }

            tfTotalCreditAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            tfTotalDebitAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            JFXUtil.updateCaretPositions(apJEMaster);
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
            cbJEReverse.setSelected(poController.Journal().Detail(pnJEDetail).isReverse());

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

    private double getGrossTotal() {
        double ldblGrossTotal = 0.0000;
        for (int lnCtr = 0; lnCtr <= poController.getDetailCount() - 1; lnCtr++) {
            ldblGrossTotal = ldblGrossTotal + (poController.Detail(lnCtr).getUnitPrce().doubleValue()
                    * poController.Detail(lnCtr).getQuantity().doubleValue());
        }
        return ldblGrossTotal;
    }

    private void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        details_data.clear();
                        int lnCtr;
                        try {
                            boolean lbShow1 = (pnEditMode == EditMode.UPDATE);
                            boolean lbShow2 = (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE);
                            boolean lbShow4 = lbShow2 && JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(), PurchaseOrderReturnStatus.POSTED, PurchaseOrderReturnStatus.PAID);

                            double lnTotal = 0.00;
                            for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                lnTotal = poController.Detail(lnCtr).getUnitPrce().doubleValue() * poController.Detail(lnCtr).getQuantity().intValue();
                                String lsSerialNo = "";
                                if(poController.Detail(lnCtr).getSerialId() != null && !"".equals(poController.Detail(lnCtr).getSerialId())){
                                    lsSerialNo = poController.Detail(lnCtr).InventorySerial().getSerial01() + " : " + poController.Detail(lnCtr).Inventory().getDescription();
                                } else {
                                    lsSerialNo = poController.Detail(lnCtr).Inventory().getDescription();
                                }
                                if (poController.Detail(lnCtr).getStockId() != null && !"".equals(poController.Detail(lnCtr).getStockId())) {
                                    details_data.add(
                                            new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                    String.valueOf(poController.Detail(lnCtr).Inventory().getBarCode()),
                                                    String.valueOf(lsSerialNo),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getUnitPrce(), true)),
                                                    String.valueOf( CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getReceiveQty(lnCtr), false)),
                                                    String.valueOf( CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getQuantity(), false)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                            ));
                                   
                                }
                            }
                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetails, 0);
                                    pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetails, pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableJEDetail = new JFXUtil.ReloadableTableTask(
                tblViewJEDetails,
                JEdetails_data,
                () -> {
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
                                if (!poController.Journal().Detail(lnCtr).isReverse()) {
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
                });
    }

    public void initDatePickers() {
        // DatePicker setup
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpJETransactionDate, dpReportMonthYear);
        JFXUtil.setActionListener(datepicker_Action, dpTransactionDate, dpJETransactionDate, dpReportMonthYear);
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    
                }
                if (JFXUtil.isObjectEqualTo(lsID, "tfTotalCreditAmt", "tfTotalDebitAmt")) {
                    loadRecordJEMaster();
                } else {
                    loadRecordMaster();
                }
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                }
            });
    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                }

                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });

            });
    ChangeListener<Boolean> txtJEDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableJEDetail.reload();
                });
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taJERemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setFocusListener(txtMaster_Focus, apMaster);
        JFXUtil.setFocusListener(txtDetail_Focus, apDetail);
        JFXUtil.setFocusListener(txtJEDetail_Focus, apJEDetail);

        JFXUtil.inputDecimalOnly(tfDiscountRate);
        JFXUtil.setCommaFormatter(tfDiscountAmount, tfFreightAmt, tfFreightDetail,tfCost, tfCreditAmt, tfDebitAmt);

        JFXUtil.adjustColumnForScrollbar(tblViewDetails, tblViewJEDetails);

        JFXUtil.handleDisabledNodeClick(apMaster, pnEditMode, nodeID -> {
            if (JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(), PurchaseOrderReturnStatus.POSTED, PurchaseOrderReturnStatus.PAID)) {
                ShowMessageFX.Warning(null, pxeModuleName, "Only the Invoice Date, To Follow Invoice, and Invoice No. are editable\nfor posted and paid transactions.");
                return;
            }
            switch (nodeID) {
                case "cbVatInclusive":
                    if (!JFXUtil.isObjectEqualTo(poController.Master().getTransactionStatus(), PurchaseOrderReturnStatus.POSTED, PurchaseOrderReturnStatus.PAID)) {
                        ShowMessageFX.Warning(null, pxeModuleName,
                                "Only available when Invoice No is provided or set \"To-follow\".");
                    }
                    break;
            }
        });
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

        tblViewDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewDetails.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        tfCost.requestFocus();
                    }
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewDetails, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetails, tblViewJEDetails);
        JFXUtil.adjustColumnForScrollbar( tblViewDetails, tblViewJEDetails);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
//        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnHistory);

        //Unkown || Ready
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apJEMaster, apJEDetail);
    }

    public void initDetailsGrid() {

        JFXUtil.setColumnCenter(tblRowNoDetail, tblReceiveQuantityDetail, tblReturnQuantityDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetails.comparatorProperty());
        tblViewDetails.setItems(sortedData);
        tblViewDetails.autosize();
    }

    public void initJEDetailsGrid() {
        JFXUtil.setColumnCenter(tblJERowNoDetail, tblReportMonthDetail);
        JFXUtil.setColumnLeft(tblJEAcctCodeDetail, tblJEAcctDescriptionDetail);
        JFXUtil.setColumnRight(tblJECreditAmtDetail, tblJEDebitAmtDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewJEDetails);

        tblViewJEDetails.setItems(JEdetails_data);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetails":
                    if (!details_data.isEmpty()) {
                        pnDetail = isMovedDown ? JFXUtil.moveToNextRow(currentTable) : JFXUtil.moveToPreviousRow(currentTable);
                        loadRecordDetail();
                    }
                    break;
                case "tblViewJEDetails":
                    if (!details_data.isEmpty()) {
                        pnJEDetail = Integer.parseInt(JEdetails_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07());
                        loadRecordDetail();
                    }
                    break;
            }
        }
    };

    public void clearTextFields() {
        Platform.runLater(() -> {
            imageinfo_temp.clear();
            JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
            psSearchSupplierId = "";
            psSearchBranchId = "";
            psSupplierId = "";
            psBranchId = "";

            JFXUtil.clearTextFields(apMaster, apDetail, apJEDetail, apJEMaster);
        });
    }
}
