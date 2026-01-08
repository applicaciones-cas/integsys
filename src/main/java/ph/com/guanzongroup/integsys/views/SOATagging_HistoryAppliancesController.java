/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelSOATagging_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
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
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatus;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;

/**
 * FXML Controller class
 *
 * @author Arsiela & Aldrich Team 2 06102025
 */
public class SOATagging_HistoryAppliancesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    static CashflowControllers poSOATaggingController;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    public int pnDetail = 0;
    private String psIndustryId = "";
    private boolean isGeneral = false;
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psTransactionNo = "";

    private ObservableList<ModelSOATagging_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelSOATagging_Detail> filteredDataDetail;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfSOANo, tfClient, tfIssuedTo, tfTransactionTotal, tfVatAmount, tfNonVatSales, tfZeroVatSales, tfVatExemptSales,
            tfNetTotal, tfCompany, tfDiscountAmount, tfFreight, tfSourceNo, tfSourceCode, tfReferenceNo, tfCreditAmount, tfDebitAmount, tfAppliedAmtDetail,
            tfSearchCompany, tfSearchSupplier, tfSearchReferenceNo;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetailList;
    @FXML
    private TableColumn tblRowNoDetail, tblSourceNoDetail, tblSourceCodeDetail, tblReferenceNoDetail, tblCreditAmtDetail, tblDebitAmtDetail, tblAppliedAmtDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poSOATaggingController = new CashflowControllers(oApp, null);
        poJSON = poSOATaggingController.SOATagging().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }

        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();

        Platform.runLater(() -> {
            poSOATaggingController.SOATagging().Master().setIndustryId(psIndustryId);;
            poSOATaggingController.SOATagging().setIndustryId(psIndustryId);
            poSOATaggingController.SOATagging().setCompanyId(psCompanyId);
            poSOATaggingController.SOATagging().initFields();
            poSOATaggingController.SOATagging().setWithUI(true);
            loadRecordSearch();
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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String tabText = "";

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poSOATaggingController.SOATagging().setTransactionStatus(SOATaggingStatus.OPEN
                                + SOATaggingStatus.CONFIRMED
                                + SOATaggingStatus.CANCELLED
                                + SOATaggingStatus.VOID
                                + SOATaggingStatus.PAID);
                        poJSON = poSOATaggingController.SOATagging().searchTransaction(psIndustryId, tfSearchCompany.getText(),
                                tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnHistory":
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                loadRecordMaster();
                loadTableDetail();
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lsID) {
                        case "tfSearchCompany":
                            poJSON = poSOATaggingController.SOATagging().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchCompany.setText("");
                                psCompanyId = "";
                                break;
                            } else {
                                psCompanyId = poSOATaggingController.SOATagging().Master().getCompanyId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchSupplier":
                            poJSON = poSOATaggingController.SOATagging().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poSOATaggingController.SOATagging().Master().getClientId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poSOATaggingController.SOATagging().setTransactionStatus(SOATaggingStatus.OPEN
                                    + SOATaggingStatus.CONFIRMED
                                    + SOATaggingStatus.CANCELLED
                                    + SOATaggingStatus.VOID
                                    + SOATaggingStatus.PAID);
                            poJSON = poSOATaggingController.SOATagging().searchTransaction(psIndustryId, tfSearchCompany.getText(),
                                    tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                return;
                            } else {
                                pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                                loadRecordMaster();
                                loadTableDetail();
                                initButton(pnEditMode);
                            }
                            loadRecordSearch();
                            return;
                    }
                    break;
                default:
                    break;
            }

            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchSupplier":
                        if (lsValue.equals("")) {
                            psSupplierId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchCompany":
                        if (lsValue.equals("")) {
                            psCompanyId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchReferenceNo":
                        break;
                }
            });

    public void loadRecordSearch() {
        try {
            if (poSOATaggingController.SOATagging().Master().Industry().getDescription() != null && !"".equals(poSOATaggingController.SOATagging().Master().Industry().getDescription())) {
                lblSource.setText(poSOATaggingController.SOATagging().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }

            tfSearchSupplier.setText(psSupplierId.equals("") ? "" : poSOATaggingController.SOATagging().Master().Supplier().getCompanyName());
            tfSearchCompany.setText(psCompanyId.equals("") ? "" : poSOATaggingController.SOATagging().Master().Company().getCompanyName());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public String getSourceCodeDescription(boolean isCode, String lsSource) {
        final Map<String, String> sourceCodes = new HashMap<>();
        sourceCodes.put(SOATaggingStatic.PaymentRequest, "PRF");
        sourceCodes.put(SOATaggingStatic.APPaymentAdjustment, "AP Payment Adjustment");
        sourceCodes.put(SOATaggingStatic.POReceiving, "PO Receiving");
        if (isCode) {
            return sourceCodes.getOrDefault(lsSource, "");
        } else {
            return sourceCodes.entrySet().stream()
                    .filter(e -> e.getValue().equals(lsSource))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("");
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSOATaggingController.SOATagging().getDetailCount() - 1) {
                return;
            }
            tfSourceNo.setText(poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo());
            tfSourceCode.setText(getSourceCodeDescription(true, poSOATaggingController.SOATagging().Detail(pnDetail).getSourceCode()));
            String lsReferenceDate = "";
            String lsReferenceNo = "";
            switch (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceCode()) {
                case SOATaggingStatic.PaymentRequest:
                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(pnDetail).PaymentRequestMaster().getSeriesNo();
                    lsReferenceDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Detail(pnDetail).PaymentRequestMaster().getTransactionDate());
                    break;
                case SOATaggingStatic.APPaymentAdjustment:
                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(pnDetail).APPaymentAdjustmentMaster().getReferenceNo();
                    lsReferenceDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Detail(pnDetail).APPaymentAdjustmentMaster().getTransactionDate());
                    break;
                case SOATaggingStatic.POReceiving:
                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(pnDetail).PurchasOrderReceivingMaster().getReferenceNo();
                    lsReferenceDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Detail(pnDetail).PurchasOrderReceivingMaster().getTransactionDate());
                    break;
            }
            tfReferenceNo.setText(lsReferenceNo);
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getCreditAmount(), true));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getDebitAmount(), true));
            tfAppliedAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount(), true));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            JFXUtil.setStatusValue(lblStatus, SOATaggingStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poSOATaggingController.SOATagging().Master().getTransactionStatus());
            poSOATaggingController.SOATagging().computeFields();

            tfTransactionNo.setText(poSOATaggingController.SOATagging().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            tfSOANo.setText(poSOATaggingController.SOATagging().Master().getSOANumber());
            tfCompany.setText(poSOATaggingController.SOATagging().Master().Company().getCompanyName());
            tfClient.setText(poSOATaggingController.SOATagging().Master().Supplier().getCompanyName());
            tfIssuedTo.setText(poSOATaggingController.SOATagging().Master().Payee().getPayeeName());
            taRemarks.setText(poSOATaggingController.SOATagging().Master().getRemarks());

            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getTransactionTotal(), true));
            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getVatAmount(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getDiscountAmount(), true));
            tfFreight.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getFreightAmount(), false));
            tfNonVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(), true)); //As per ma'am she
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getVatExempt(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getNetTotal(), true));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetail() {
        // Setting data to table detail
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
                    int lnRowCount = 0;
                    String lsReferenceNo = "";
                    for (lnCtr = 0; lnCtr < poSOATaggingController.SOATagging().getDetailCount(); lnCtr++) {
                        try {
                            if (!poSOATaggingController.SOATagging().Detail(lnCtr).isReverse()) {
                                continue;
                            }
                            switch (poSOATaggingController.SOATagging().Detail(lnCtr).getSourceCode()) {
                                case SOATaggingStatic.PaymentRequest:
                                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(lnCtr).PaymentRequestMaster().getSeriesNo();
                                    break;
                                case SOATaggingStatic.APPaymentAdjustment:
                                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(lnCtr).APPaymentAdjustmentMaster().getReferenceNo();
                                    break;
                                case SOATaggingStatic.POReceiving:
                                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(lnCtr).PurchasOrderReceivingMaster().getReferenceNo();
                                    break;
                            }
                            lnRowCount += 1;
                            details_data.add(
                                    new ModelSOATagging_Detail(String.valueOf(lnRowCount),
                                            String.valueOf(poSOATaggingController.SOATagging().Detail(lnCtr).getSourceNo()),
                                            String.valueOf(getSourceCodeDescription(true, poSOATaggingController.SOATagging().Detail(lnCtr).getSourceCode())),
                                            String.valueOf(lsReferenceNo),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getCreditAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getDebitAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getAppliedAmount(), true)),
                                            String.valueOf(lnCtr)
                                    ));
                            lsReferenceNo = "";
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    }
                    int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 8); //this method is used only when Reverse is applied
                    if (lnTempRow < 0 || lnTempRow
                            >= details_data.size()) {
                        if (!details_data.isEmpty()) {
                            /* FOCUS ON FIRST ROW */
                            JFXUtil.selectAndFocusRow(tblViewTransDetailList, 0);
                            int lnRow = Integer.parseInt(details_data.get(0).getIndex08());
                            pnDetail = lnRow;
                            loadRecordDetail();
                        }
                    } else {
                        /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                        JFXUtil.selectAndFocusRow(tblViewTransDetailList, lnTempRow);
                        int lnRow = Integer.parseInt(details_data.get(tblViewTransDetailList.getSelectionModel().getSelectedIndex()).getIndex08());
                        pnDetail = lnRow;
                        loadRecordDetail();
                    }
                    loadRecordMaster();
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
                dpTransactionDate, dpReferenceDate);
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtMaster_Focus, tfSearchCompany, tfSearchSupplier);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
    }

    public void initTableOnClick() {
        tblViewTransDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewTransDetailList.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                }
            }
        });
        tblViewTransDetailList.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetailList); // need to use computed-size in min-width of the column to work
    }

    private void initButton(int fnValue) {
        boolean lbShow3 = (fnValue == EditMode.READY);
        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnHistory);

        //Unkown || Ready
        JFXUtil.setDisabled(true, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(lbShow3, btnHistory);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblSourceNoDetail, tblSourceCodeDetail, tblReferenceNoDetail);
        JFXUtil.setColumnRight(tblCreditAmtDetail, tblDebitAmtDetail, tblAppliedAmtDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetailList);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelSOATagging_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetailList.comparatorProperty());
        tblViewTransDetailList.setItems(sortedData);
        tblViewTransDetailList.autosize();
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
                                pnDetail = Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex08());
                                break;
                            case UP:
                                pnDetail = Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex08());
                                break;
                            default:
                                break;
                        }
                        loadRecordDetail();
                        event.consume();
                    }
                    break;
            }
        }
    }

    public void clearTextFields() {
        psTransactionNo = "";
        dpTransactionDate.setValue(null);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }
}
