/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPurchaseOrder;
import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.F4;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.purchasing.model.Model_PO_Detail;
import org.guanzon.cas.purchasing.services.PurchaseOrderControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderStaticData;
import org.guanzon.cas.purchasing.status.PurchaseOrderStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 *
 * @author User
 */
public class PurchaseOrder_EntryLPController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private PurchaseOrderControllers poPurchasingController;
    private String psFormName = "Purchase Order LP";
    private LogWrapper logWrapper;
    private JSONObject poJSON;

    private int pnEditMode;
    private int pnTblMainRow = -1;
    private int pnTblDetailRow = -1;
    private int pnTblMain_Page = 50;

    private String prevSupplier = "";
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private String psOldDate = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelPurchaseOrder> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPurchaseOrderDetail> detail_data = FXCollections.observableArrayList();

    private TextField activeField;
    @FXML
    private AnchorPane AnchorMaster, AnchorDetails, AnchorMain, apBrowse, apButton;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel,
            btnPrint, btnRetrieve, btnTransHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfDestination, tfReferenceNo,
            tfTerm, tfDiscountRate, tfDiscountAmount, tfAdvancePRate, tfAdvancePAmount, tfTotalAmount, tfNetAmount;
    @FXML
    private Label lblTransactionStatus, lblSource;
    @FXML
    private CheckBox chkbAdvancePayment;
    @FXML
    private DatePicker dpTransactionDate, dpExpectedDlvrDate;
    @FXML
    private TextField tfBarcode, tfDescription, tfBrand, tfModel, tfColor, tfCategory, tfInventoryType,
            tfMeasure, tfClass, tfAMC, tfROQ, tfRO, tfBO, tfQOH, tfCost, tfRequestQuantity, tfOrderQuantity;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView<ModelPurchaseOrderDetail> tblVwOrderDetails;
    @FXML
    private TableColumn<ModelPurchaseOrderDetail, String> tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail,
            tblCostDetail, tblROQDetail, tblRequestQuantityDetail, tblOrderQuantityDetail, tblTotalAmountDetail;
    @FXML
    private TableView<ModelPurchaseOrder> tblVwStockRequest;
    @FXML
    private TableColumn<ModelPurchaseOrder, String> tblRowNo, tblBranchName, tblDate, tblReferenceNo, tblNoOfItems;
    @FXML
    private AnchorPane apTableStockRequestLoading, apTableDetailLoading;
    @FXML
    private ProgressIndicator piTableStockRequestLoading, piTableDetailLoading;
    @FXML
    private Pagination pagination;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poPurchasingController = new PurchaseOrderControllers(poApp, logWrapper);
            poPurchasingController.PurchaseOrder().setTransactionStatus(PurchaseOrderStatus.OPEN + PurchaseOrderStatus.RETURNED);
            poJSON = poPurchasingController.PurchaseOrder().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }

            tblVwOrderDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
            Platform.runLater((() -> {
                poPurchasingController.PurchaseOrder().Master().setIndustryID(psIndustryID);
                poPurchasingController.PurchaseOrder().Master().setCompanyID(psCompanyID);
                poPurchasingController.PurchaseOrder().Master().setCategoryCode(psCategoryID);
                loadRecordSearch();
            }));
            Platform.runLater(() -> btnNew.fire());
            initAll();
        } catch (ExceptionInInitializerError ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poPurchasingController.PurchaseOrder().Master().Company().getCompanyName() + " - " + poPurchasingController.PurchaseOrder().Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        initButtonsClickActions();
        initTextFieldFocus();
        initTextAreaFocus();
        initTextFieldKeyPressed();
        initTextFieldsProperty();
        initCheckBoxActions();
        initDatePickerActions();
        initTextFieldPattern();
        initTableStockRequest();
        initTableDetail();
        tblVwStockRequest.setOnMouseClicked(this::tblVwMain_Clicked);
        tblVwOrderDetails.setOnMouseClicked(this::tblVwDetail_Clicked);
        initButtons(pnEditMode);
        initFields(pnEditMode);
    }

    private int moveToNextRow(TableView<?> table, TablePosition<?, ?> focusedCell) {
        if (table.getItems().isEmpty()) {
            return -1; // No movement possible
        }
        int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
        table.getSelectionModel().select(nextRow);
        return nextRow;
    }

    private int moveToPreviousRow(TableView<?> table, TablePosition<?, ?> focusedCell) {
        if (table.getItems().isEmpty()) {
            return -1; // No movement possible
        }
        int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
        table.getSelectionModel().select(previousRow);
        return previousRow;
    }

    private void tableKeyEvents(KeyEvent event) {
        TableView<?> currentTable = (TableView<?>) event.getSource();
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

        if (focusedCell != null && "tblVwOrderDetails".equals(currentTable.getId())) {
            switch (event.getCode()) {
                case TAB:
                case DOWN:
                    pnTblDetailRow = pnTblDetailRow;
                    if (pnEditMode != EditMode.ADDNEW || pnEditMode != EditMode.UPDATE) {
                        pnTblDetailRow = moveToNextRow(currentTable, focusedCell);
                    }
                    break;
                case UP:
                    pnTblDetailRow = pnTblDetailRow;
                    if (pnEditMode != EditMode.ADDNEW || pnEditMode != EditMode.UPDATE) {
                        pnTblDetailRow = moveToPreviousRow(currentTable, focusedCell);
                    }
                    break;
                default:
                    return;
            }
            currentTable.getSelectionModel().select(pnTblDetailRow);
            currentTable.getFocusModel().focus(pnTblDetailRow);
            loadRecordDetail();
            initDetailFocus();
            event.consume();
        }
    }

    private void loadRecordMaster() {
        try {
            tfTransactionNo.setText(poPurchasingController.PurchaseOrder().Master().getTransactionNo());
            String lsStatus = "";
            switch (poPurchasingController.PurchaseOrder().Master().getTransactionStatus()) {
                case PurchaseOrderStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case PurchaseOrderStatus.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
                case PurchaseOrderStatus.APPROVED:
                    lsStatus = "APPROVED";
                    break;
                case PurchaseOrderStatus.RETURNED:
                    lsStatus = "RETURNED";
                    break;
                case PurchaseOrderStatus.CANCELLED:
                    lsStatus = "CANCELLED";
                    break;
                case PurchaseOrderStatus.VOID:
                    lsStatus = "VOID";
                    break;
            }
            lblTransactionStatus.setText(lsStatus);
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poPurchasingController.PurchaseOrder().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName() != null ? poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName() : "");
            tfDestination.setText(poPurchasingController.PurchaseOrder().Master().Branch().getBranchName() != null ? poPurchasingController.PurchaseOrder().Master().Branch().getBranchName() : "");
            tfReferenceNo.setText(poPurchasingController.PurchaseOrder().Master().getReference());
            tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription() != null ? poPurchasingController.PurchaseOrder().Master().Term().getDescription() : "");
            taRemarks.setText(poPurchasingController.PurchaseOrder().Master().getRemarks());
            dpExpectedDlvrDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poPurchasingController.PurchaseOrder().Master().getExpectedDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount()));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getAdditionalDiscount(), true));
            chkbAdvancePayment.setSelected(poPurchasingController.PurchaseOrder().Master().getWithAdvPaym() == true);
            tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage()));
            tfAdvancePAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesAmount(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getTranTotal(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void loadRecordDetail() {
        try {
            if (pnTblDetailRow < 0 || pnTblDetailRow > poPurchasingController.PurchaseOrder().getDetailCount() - 1) {
                return;
            }
            if (pnTblDetailRow >= 0) {
                tfBarcode.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getBarCode() != null ? poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getBarCode() : "");
                tfDescription.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getDescription() != null ? poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getDescription() : "");
                tfBrand.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Brand().getDescription());
                tfModel.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Model().getDescription());
                tfColor.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Color().getDescription());
                tfCategory.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Category().getDescription());
                tfInventoryType.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().InventoryType().getDescription());
                tfMeasure.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Measure().getDescription());
                tfClass.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InventoryMaster().getInventoryClassification());
                tfAMC.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InventoryMaster().getAverageCost()));
                tfROQ.setText("0.00");
                double lnRO = 0, lnBO = 0, lnQOH = 0, lnRequestQuantity = 0;
                switch (poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getSouceCode()) {
                    case PurchaseOrderStatus.SourceCode.STOCKREQUEST:
                        lnRO = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getReceived();
                        lnBO = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getBackOrder();
                        lnQOH = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getQuantityOnHand();
                        lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getApproved();
                        break;
                    case PurchaseOrderStatus.SourceCode.POQUOTATION:
                        lnRO = 0;
                        lnBO = 0;
                        lnQOH = 0;
                        lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).POQuotationDetail().getQuantity();
                        break;
                }
                tfRO.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(lnRO));
                tfBO.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(lnBO));
                tfQOH.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(lnQOH));
                tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getUnitPrice().doubleValue(), true));
                tfRequestQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnRequestQuantity));
                tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getQuantity().doubleValue()));
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel,
                btnPrint, btnRetrieve, btnTransHistory, btnClose);

        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poPurchasingController.PurchaseOrder().SearchTransaction("", poPurchasingController.PurchaseOrder().Master().getSupplierID(), "");
                    if (!"error".equals((String) poJSON.get("result"))) {
                        tblVwStockRequest.getSelectionModel().clearSelection(pnTblDetailRow);
                        pnTblDetailRow = -1;
                        loadRecordMaster();
                        pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                        loadRecordDetail();
                        loadTableDetail();

                        selectTheExistedDetailFromMainTable();
                        if (!tfCost.getText().isEmpty() && !tfSupplier.getText().isEmpty()) {
                            loadTableMain();
                        }
                    } else {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Search Information", null);
                    }
                    break;
                case "btnNew":
                    clearDetailFields();
                    clearMasterFields();
                    detail_data.clear();
                    poJSON = poPurchasingController.PurchaseOrder().NewTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        poPurchasingController.PurchaseOrder().Master().setSupplierID(prevSupplier);
                        poPurchasingController.PurchaseOrder().Master().setIndustryID(psIndustryID);
                        poPurchasingController.PurchaseOrder().Master().setCompanyID(psCompanyID);
                        poPurchasingController.PurchaseOrder().Master().setCategoryCode(psCategoryID);
                        poPurchasingController.PurchaseOrder().Master().setDestinationID(poApp.getBranchCode());
                        poPurchasingController.PurchaseOrder().Master().setInventoryTypeCode(poPurchasingController.PurchaseOrder().getInventoryTypeCode());
                        loadRecordMaster();
                        pnTblDetailRow = 0;
                        pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                        loadTableDetail();
                        loadTableDetailAndSelectedRow();
                    } else {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                    }
                    break;
                case "btnUpdate":
                    poJSON = poPurchasingController.PurchaseOrder().UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                        break;
                    }
                    pnTblDetailRow = -1;
                    loadRecordMaster();
                    pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                    loadTableDetail();
                    selectTheExistedDetailFromMainTable();
                    if (!tfSupplier.getText().isEmpty()) {
                        loadTableMain();
                    }
                    break;
                case "btnSearch":
                    if (activeField != null) {
                        String loTextFieldId = activeField.getId();
                        String lsValue = activeField.getText().trim();
                        switch (loTextFieldId) {
                            case "tfSupplier":
                                if (!isExchangingSupplier()) {
                                    return;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchSupplier(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfSupplier.setText("");
                                    break;
                                }
                                prevSupplier = poPurchasingController.PurchaseOrder().Master().getSupplierID();
                                tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                                if (tfSupplier.getText().isEmpty()) {
                                    tfSupplier.requestFocus();
                                }
                                if (!tfSupplier.getText().isEmpty()) {
                                    loadTableMain();
                                }
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfDestination":
                                poJSON = poPurchasingController.PurchaseOrder().SearchDestination(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDestination.setText("");
                                    break;
                                }
                                tfDestination.setText(poPurchasingController.PurchaseOrder().Master().Branch().getBranchName());
                                if (tfDestination.getText().isEmpty()) {
                                    tfDestination.requestFocus();
                                }
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfTerm":
                                poJSON = poPurchasingController.PurchaseOrder().SearchTerm(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfTerm.setText("");
                                    break;
                                }
                                tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                                if (tfTerm.getText().isEmpty()) {
                                    tfTerm.requestFocus();
                                }
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfBarcode":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeGeneral(lsValue, false, pnTblDetailRow,
                                        true);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfBarcode.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfDescription":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeDescriptionGeneral(lsValue, false, pnTblDetailRow,
                                        true);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDescription.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;
                            default:
                                System.out.println("Unknown TextField");
                        }
                    }
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save?")) {
                        return;
                    }
                    LocalDate selectedLocalDate = dpTransactionDate.getValue();
                    if (pnEditMode == EditMode.UPDATE) {
                        if (!psOldDate.isEmpty()) {
                            if (!CustomCommonUtil.formatLocalDateToShortString(selectedLocalDate).equals(psOldDate) && tfReferenceNo.getText().isEmpty()) {
                                ShowMessageFX.Warning("A reference number is required for backdated transactions.", psFormName, null);
                                return;
                            }
                        }
                    }

                    prevSupplier = poPurchasingController.PurchaseOrder().Master().getSupplierID();

                    // Validate Detail Count Before Backend Processing
                    int detailCount = poPurchasingController.PurchaseOrder().getDetailCount();
                    boolean hasValidItem = false; // True if at least one valid item exists

                    if (detailCount == 0) {
                        ShowMessageFX.Warning("Your order is empty. Please add at least one item.", psFormName, null);
                        return;
                    }

                    for (int lnCntr = 0; lnCntr <= detailCount - 1; lnCntr++) {
                        double quantity = poPurchasingController.PurchaseOrder().Detail(lnCntr).getQuantity().doubleValue();
                        String stockID = poPurchasingController.PurchaseOrder().Detail(lnCntr).getStockID();

                        // If any stock ID is empty OR quantity is 0, show an error and prevent saving
                        if (detailCount == 1) {
                            if (stockID == null || stockID.trim().isEmpty() || quantity == 0) {
                                ShowMessageFX.Warning("Invalid item in order. Ensure all items have a valid Stock ID and quantity greater than 0.", psFormName, null);
                                return;
                            }
                        }

                        hasValidItem = true;
                    }

                    // If no valid items exist, prevent saving
                    if (!hasValidItem) {
                        ShowMessageFX.Warning("Your order must have at least one valid item with a Stock ID and quantity greater than 0.", psFormName, null);
                        return;
                    }

                    // Assign modification details for Update Mode
                    if (pnEditMode == EditMode.UPDATE) {
                        poPurchasingController.PurchaseOrder().Master().setModifiedDate(poApp.getServerDate());
                        poPurchasingController.PurchaseOrder().Master().setModifyingId(poApp.getUserID());
                    }

                    // Assign modification date to all details
                    for (int lnCntr = 0; lnCntr < detailCount; lnCntr++) {
                        poPurchasingController.PurchaseOrder().Detail(lnCntr).setOldPrice(poPurchasingController.PurchaseOrder().Detail(lnCntr).Inventory().getCost());
                        poPurchasingController.PurchaseOrder().Detail(lnCntr).setModifiedDate(poApp.getServerDate());
                    }

                    // Save Transaction
                    poJSON = poPurchasingController.PurchaseOrder().isDetailHasZeroQty();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        if ("true".equals((String) poJSON.get("warning"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnTblDetailRow = (int) poJSON.get("tableRow");
                            loadTableDetail();
                            loadRecordDetail();
                            initDetailFocus();
                            return;
                        } else {
                            if (!ShowMessageFX.YesNo((String) poJSON.get("message"), psFormName, null)) {
                                pnTblDetailRow = (int) poJSON.get("tableRow");
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                return;
                            }
                        }
                    }
                    poJSON = poPurchasingController.PurchaseOrder().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        loadTableDetail();
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    poJSON = poPurchasingController.PurchaseOrder().OpenTransaction(poPurchasingController.PurchaseOrder().Master().getTransactionNo());
                    if ("success".equals(poJSON.get("result"))) {
                        if (poPurchasingController.PurchaseOrder().Master().getTransactionStatus().equals(PurchaseOrderStatus.OPEN)) {
                            if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm this transaction?")) {
                                poPurchasingController.PurchaseOrder().setWithUI(true);
                                if ("success".equals((poJSON = poPurchasingController.PurchaseOrder().ConfirmTransaction("")).get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                                } else {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                            }
                        }
                    }

                    // Print Transaction Prompt
                    if (ShowMessageFX.YesNo(null, psFormName, "Do you want to print this transaction?")) {
                        poJSON = poPurchasingController.PurchaseOrder().printTransaction(PurchaseOrderStaticData.Printing_Pedritos);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Print Purchase Order", null);
                        }
                    }

                    Platform.runLater(() -> btnNew.fire());
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, "Cancel Confirmation", "Are you sure you want to cancel?")) {
                        if (pnEditMode == EditMode.ADDNEW) {
                            clearDetailFields();
                            clearMasterFields();
                            detail_data.clear();
                            tblVwOrderDetails.getItems().clear();
                            pnEditMode = EditMode.UNKNOWN;
                            poPurchasingController.PurchaseOrder().Master().setIndustryID(psIndustryID);
                            poPurchasingController.PurchaseOrder().Master().setCompanyID(psCompanyID);
                            prevSupplier = poPurchasingController.PurchaseOrder().Master().getSupplierID();
                            poPurchasingController.PurchaseOrder().Master().setSupplierID(prevSupplier);
                            tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                            pnTblMainRow = -1;
                            tblVwStockRequest.getItems().clear();
                            tblVwStockRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                            if (!tfSupplier.getText().isEmpty()) {
                                loadTableMain();
                            }
                        } else {
                            clearMasterFields();
                            clearDetailFields();
                            detail_data.clear();
                            poJSON = poPurchasingController.PurchaseOrder().OpenTransaction(poPurchasingController.PurchaseOrder().Master().getTransactionNo());
                            if ("success".equals((String) poJSON.get("result"))) {
                                pnTblDetailRow = -1;
                                loadRecordMaster();
                                clearDetailFields();
                                pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                                loadTableDetail();
                            }
                        }
                    }
                    tblVwOrderDetails.getSelectionModel().clearSelection();
                    tblVwStockRequest.getSelectionModel().clearSelection();
                    if (pnTblMainRow >= 0) {
                        tblVwStockRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex07(PurchaseOrderStatus.OPEN);
                    }
                    break;
                case "btnPrint":
                    poJSON = poPurchasingController.PurchaseOrder().printTransaction(PurchaseOrderStaticData.Printing_Pedritos);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }
                    break;
                case "btnRetrieve":
                    if (tfSupplier.getText().isEmpty()) {
                        ShowMessageFX.Warning("Invalid to retrieve stock request, supplier is empty.", psFormName, null);
                        return;
                    }
                    if (!tfSupplier.getText().isEmpty() && !tfSupplier.getText().isEmpty()) {
                        loadTableMain();
                    }
                    break;
                case "btnTransHistory":
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initButtons(pnEditMode);
            initFields(pnEditMode);
        } catch (CloneNotSupportedException | ExceptionInInitializerError | SQLException | GuanzonException | ParseException | NullPointerException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTextFieldFocus() {
        List<TextField> loTxtField = Arrays.asList(tfReferenceNo, tfDiscountRate, tfDiscountAmount,
                tfAdvancePRate, tfAdvancePAmount, tfCost, tfOrderQuantity);
        loTxtField.forEach(tf -> tf.focusedProperty().addListener(txtField_Focus));

        tfBarcode.setOnMouseClicked(e -> activeField = tfBarcode);
        tfDescription.setOnMouseClicked(e -> activeField = tfDescription);
        tfSupplier.setOnMouseClicked(e -> activeField = tfSupplier);
        tfDestination.setOnMouseClicked(e -> activeField = tfDestination);
        tfTerm.setOnMouseClicked(e -> activeField = tfTerm);
    }

    private void initTextAreaFocus() {
        taRemarks.focusedProperty().addListener(txtArea_Focus);
    }
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
                case "tfReferenceNo":
                    poPurchasingController.PurchaseOrder().Master().setReference(lsValue);
                    break;
                case "tfDiscountRate":
                    lsValue = JFXUtil.removeComma(lsValue);
                    poJSON = poPurchasingController.PurchaseOrder().setDiscountRate(lsValue);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount()));
                    tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                    break;
                case "tfDiscountAmount":
                    lsValue = JFXUtil.removeComma(lsValue);
                    poJSON = poPurchasingController.PurchaseOrder().setDiscountAmount(lsValue);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }
                    tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getAdditionalDiscount(), true));
                    tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                    break;
                case "tfAdvancePRate":
                    lsValue = JFXUtil.removeComma(lsValue);
                    poJSON = poPurchasingController.PurchaseOrder().setAdvancePaymentRate(lsValue);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }

                    tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage()));
                    tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                    break;
                case "tfAdvancePAmount":
                    lsValue = JFXUtil.removeComma(lsValue);
                    poJSON = poPurchasingController.PurchaseOrder().setAdvancePaymentAmount(lsValue);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    }
                    tfAdvancePAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesAmount(), true));
                    tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                    break;
                case "tfOrderQuantity":
                    break;
            }
        } else {
            loTextField.selectAll();
        }
    };
    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextAreaID = loTextArea.getId();
        String lsValue = loTextArea.getText();
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTextAreaID) {
                case "taRemarks":
                    poPurchasingController.PurchaseOrder().Master().setRemarks(lsValue);
                    break;
            }
        } else {
            loTextArea.selectAll();
        }
    };

    private void initTextFieldKeyPressed() {
        List<TextField> loTxtField = Arrays.asList(tfAdvancePAmount, tfSupplier,
                tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount, tfTotalAmount,
                tfDestination, tfAdvancePRate,
                tfBarcode, tfDescription,
                tfBO, tfRO,
                tfCost, tfOrderQuantity);

        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSupplier":
                                if (!isExchangingSupplier()) {
                                    return;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchSupplier(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfSupplier.setText("");
                                    break;
                                }
                                prevSupplier = poPurchasingController.PurchaseOrder().Master().getSupplierID();
                                tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                                if (!tfSupplier.getText().isEmpty()) {
                                    loadTableMain();
                                }
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfDestination":
                                poJSON = poPurchasingController.PurchaseOrder().SearchDestination(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDestination.setText("");
                                    break;
                                }
                                tfDestination.setText(poPurchasingController.PurchaseOrder().Master().Branch().getBranchName());
                                break;
                            case "tfTerm":
                                poJSON = poPurchasingController.PurchaseOrder().SearchTerm(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfTerm.setText("");
                                    break;
                                }
                                tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfBarcode":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeGeneral(lsValue, false, pnTblDetailRow, true
                                );
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfBarcode.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfDescription":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeDescriptionGeneral(lsValue, false, pnTblDetailRow,
                                        true);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDescription.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;
                        }
                        switch (txtFieldID) {
                            case "tfCompany":
                            case "tfSupplier":
                            case "tfDestination":
                            case "tfTerm":
                            case "tfAdvancePAmount":
                            case "tfAdvancePRate":
                            case "tfDiscountRate":
                            case "tfDiscountAmount":
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                break;
                            case "tfOrderQuantity":
                                setOrderQuantityToDetail(tfOrderQuantity.getText().replace(",", ""));
                                poJSON = poPurchasingController.PurchaseOrder().netTotalChecker(pnTblDetailRow);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfOrderQuantity.setText("0");
                                    loadRecordMaster();
                                    loadTableDetailAndSelectedRow();
                                    break;
                                }
                                if (!detail_data.isEmpty() && pnTblDetailRow < detail_data.size() - 1) {
                                    pnTblDetailRow++;
                                }
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                loadTableDetailAndSelectedRow();
                                break;
                        }
                        event.consume();
                        break;
                    case F4:
                        switch (txtFieldID) {
                            case "tfBarcode":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeGeneral(lsValue, false, pnTblDetailRow, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfBarcode.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;
                            case "tfDescription":
                                if (pnTblDetailRow < 0) {
                                    ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                    clearDetailFields();
                                    break;
                                }
                                poJSON = poPurchasingController.PurchaseOrder().SearchBarcodeDescriptionGeneral(lsValue, false, pnTblDetailRow, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDescription.setText("");
                                    if (poJSON.get("tableRow") != null) {
                                        pnTblDetailRow = (int) poJSON.get("tableRow");
                                    } else {
                                        break;
                                    }
                                }
                                loadTableDetail();
                                loadRecordDetail();
                                initDetailFocus();
                                selectTheExistedDetailFromMainTable();
                                break;

                        }
                        event.consume();
                        break;
                    case UP:
                        setOrderQuantityToDetail(tfOrderQuantity.getText().replace(",", ""));
                        poJSON = poPurchasingController.PurchaseOrder().netTotalChecker(pnTblDetailRow);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            tfOrderQuantity.setText("0");
                            loadRecordMaster();
                            loadTableDetailAndSelectedRow();
                            break;
                        }
                        if (!lsTxtField.equals("tfBarcode") && !lsTxtField.equals("tfDescription")) {
                            if (pnTblDetailRow > 0 && !detail_data.isEmpty()) {
                                pnTblDetailRow--;
                            }
                        }

                        // Prevent going from 'tfOrderQuantity' to 'taRemarks'
                        if (!lsTxtField.equals("tfBarcode") && !lsTxtField.equals("tfOrderQuantity")) {
                            CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        }
                        loadTableDetailAndSelectedRow();
                        event.consume();
                        break;
                    case DOWN:
                        setOrderQuantityToDetail(tfOrderQuantity.getText().replace(",", ""));
                        poJSON = poPurchasingController.PurchaseOrder().netTotalChecker(pnTblDetailRow);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            tfOrderQuantity.setText("0");
                            loadRecordMaster();
                            loadTableDetailAndSelectedRow();
                            break;
                        }
                        if ("tfOrderQuantity".equals(lsTxtField.getId())) {
                            if (!detail_data.isEmpty() && pnTblDetailRow < detail_data.size() - 1) {
                                pnTblDetailRow++;
                            }
                        }
                        CommonUtils.SetNextFocus(lsTxtField);
                        loadTableDetailAndSelectedRow();
                        event.consume(); // Consume event after handling focus
                        break;
                    default:
                        break;

                }
            }
        } catch (ExceptionInInitializerError | SQLException | CloneNotSupportedException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setOrderQuantityToDetail(String fsValue) {
        if (fsValue.isEmpty()) {
            fsValue = "0.00";
        }
        if (Double.parseDouble(fsValue) < 0.00) {
            ShowMessageFX.Warning("Invalid Order Quantity", psFormName, null);
            fsValue = "0.00";
        }
        if (tfOrderQuantity.isFocused()) {
            if (tfBarcode.getText().isEmpty()) {
                ShowMessageFX.Warning("Invalid action, Please enter barcode first. ", psFormName, null);
                tfBarcode.requestFocus();
                fsValue = "0.00";
            }
        }

        if (pnTblDetailRow < 0) {
            fsValue = "0.00";
            ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
            clearDetailFields();
            int detailCount = poPurchasingController.PurchaseOrder().getDetailCount();
            pnTblDetailRow = detailCount > 0 ? detailCount - 1 : 0;
        }
        double lnRequestQuantity = 0.00;
        try {
            lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getApproved();
            if (!poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getSouceNo().isEmpty()) {
                if (Double.parseDouble(tfOrderQuantity.getText().replace(",", "")) > lnRequestQuantity) {
                    ShowMessageFX.Warning("Invalid order quantity entered. The item is from a stock request, and the order quantity must not be greater than the requested quantity.", psFormName, null);
                    fsValue = "0.00";
                }
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
        poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).setQuantity(Double.valueOf(fsValue));
        tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getQuantity()));
    }

    private void initTextFieldPattern() {
        JFXUtil.inputDecimalOnly(tfDiscountRate, tfAdvancePRate);
        JFXUtil.setCommaFormatter(tfCost, tfDiscountAmount, tfAdvancePAmount, tfOrderQuantity);
    }

    private void initDatePickerActions() {
        dpTransactionDate.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                LocalDate selectedLocalDate = dpTransactionDate.getValue();
                LocalDate transactionDate = new java.sql.Date(poPurchasingController.PurchaseOrder().Master().getTransactionDate().getTime()).toLocalDate();
                if (selectedLocalDate == null) {
                    return;
                }

                LocalDate dateNow = LocalDate.now();
                psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                String lsReferNo = tfReferenceNo.getText().trim();
                boolean approved = true;
                if (pnEditMode == EditMode.UPDATE) {
                    psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                    if (selectedLocalDate.isAfter(dateNow)) {
                        ShowMessageFX.Warning("Invalid to future date.", psFormName, null);
                        approved = false;
                    }

                    if (selectedLocalDate.isBefore(transactionDate) && lsReferNo.isEmpty()) {
                        ShowMessageFX.Warning("Invalid to backdate. Please enter a reference number first.", psFormName, null);
                        approved = false;
                    }
                    if (selectedLocalDate.isBefore(transactionDate) && !lsReferNo.isEmpty()) {
                        boolean proceed = ShowMessageFX.YesNo(
                                "You are changing the transaction date\n"
                                + "If YES, seek approval to proceed with the changed date.\n"
                                + "If NO, the transaction date will be remain.",
                                psFormName, null
                        );
                        if (proceed) {
                            if (poApp.getUserLevel() <= UserRight.ENCODER) {
                                poJSON = ShowDialogFX.getUserApproval(poApp);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    approved = false;
                                    return;
                                } else {
                                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                        ShowMessageFX.Warning("User is not an authorized approving officer..", psFormName, null);
                                        approved = false;
                                        return;
                                    }
                                }
                            }
                        } else {
                            approved = false;
                        }
                    }
                }
                if (pnEditMode == EditMode.ADDNEW) {
                    if (selectedLocalDate.isAfter(dateNow)) {
                        ShowMessageFX.Warning("Invalid to future date.", psFormName, null);
                        approved = false;
                    }
                    if (selectedLocalDate.isBefore(dateNow) && lsReferNo.isEmpty()) {
                        ShowMessageFX.Warning("Invalid to backdate. Please enter a reference number first.", psFormName, null);
                        approved = false;
                    }

                    if (selectedLocalDate.isBefore(dateNow) && !lsReferNo.isEmpty()) {
                        boolean proceed = ShowMessageFX.YesNo(
                                "You selected a backdate with a reference number.\n\n"
                                + "If YES, seek approval to proceed with the backdate.\n"
                                + "If NO, the transaction date will be reset to today.",
                                "Backdate Confirmation", null
                        );
                        if (proceed) {
                            if (poApp.getUserLevel() <= UserRight.ENCODER) {
                                poJSON = ShowDialogFX.getUserApproval(poApp);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    approved = false;
                                    return;
                                } else {
                                    if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                        ShowMessageFX.Warning("User is not an authorized approving officer..", psFormName, null);
                                        approved = false;
                                        return;
                                    }
                                }
                            }
                        } else {
                            approved = false;
                        }
                    }
                }
                if (approved) {
                    poPurchasingController.PurchaseOrder().Master().setTransactionDate(
                            SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                } else {
                    if (pnEditMode == EditMode.ADDNEW) {
                        dpTransactionDate.setValue(dateNow);
                        poPurchasingController.PurchaseOrder().Master().setTransactionDate(
                                SQLUtil.toDate(dateNow.toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } else if (pnEditMode == EditMode.UPDATE) {
                        poPurchasingController.PurchaseOrder().Master().setTransactionDate(
                                SQLUtil.toDate(psOldDate, SQLUtil.FORMAT_SHORT_DATE));
                    }

                }
                dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                        SQLUtil.dateFormat(poPurchasingController.PurchaseOrder().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            }
        }
        );

        dpExpectedDlvrDate.setOnAction(e
                -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (dpExpectedDlvrDate.getValue() != null) {
                    LocalDate selectedLocalDate = dpExpectedDlvrDate.getValue();
                    Date selectedDate = SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE);
                    Date transactionDate = poPurchasingController.PurchaseOrder().Master().getTransactionDate();
                    LocalDate transactionLocalDate = LocalDate.now();

                    if (selectedDate.before(transactionDate)) {
                        ShowMessageFX.Warning("Please select an expected delivery date that is on or after the transaction date.", "Invalid Expected Delivery Date", null);
                        dpExpectedDlvrDate.setValue(transactionLocalDate);
                        poPurchasingController.PurchaseOrder().Master().setExpectedDate(transactionDate);
                        return;
                    }

                    poPurchasingController.PurchaseOrder().Master().setExpectedDate(selectedDate);
                }
            }
        }
        );
    }

    private void initCheckBoxActions() {
        chkbAdvancePayment.setOnAction(event -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poJSON = new JSONObject();
                if (tfTotalAmount.getText().isEmpty()
                        || Double.parseDouble(tfTotalAmount.getText().replace(",", "")) > 0.0000) {

                    if (chkbAdvancePayment.isSelected()) {
                        chkbAdvancePayment.setSelected(true);
                        poPurchasingController.PurchaseOrder().Master().setWithAdvPaym(true);
                    } else {
                        if (Double.parseDouble(tfAdvancePAmount.getText().replace(",", "")) > 0.0000
                                || Double.parseDouble(tfAdvancePRate.getText().replace(",", "")) > 0.00) {
                            if (ShowMessageFX.YesNo("An advance payment amount has already been entered. Are you sure you want to unselect it?", psFormName, null)) {
                                poPurchasingController.PurchaseOrder().Master().setWithAdvPaym(false);
                                chkbAdvancePayment.setSelected(false);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesAmount(0.0000);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesPercentage(0.00);
                                tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage()));
                                tfAdvancePAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesAmount(), true));
                                poPurchasingController.PurchaseOrder().computeNetTotal();
                                tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                            } else {
                                chkbAdvancePayment.setSelected(true);
                                poPurchasingController.PurchaseOrder().Master().setWithAdvPaym(true);
                                return;
                            }
                        } else {
                            poPurchasingController.PurchaseOrder().Master().setWithAdvPaym(false);
                            chkbAdvancePayment.setSelected(false);
                        }
                    }
                } else {
                    ShowMessageFX.Warning("Advance payment cannot be entered until the total amount is greater than 0.0000.", psFormName, null);
                    poPurchasingController.PurchaseOrder().Master().setWithAdvPaym(false);
                    chkbAdvancePayment.setSelected(false);
                }
                initFields(pnEditMode);
            }
        });
    }

    private void clearMasterFields() {
        /* Master Fields*/
        pnTblDetailRow = -1;
        dpTransactionDate.setValue(null);
        dpExpectedDlvrDate.setValue(null);
        taRemarks.setText("");
        psOldDate = "";
        CustomCommonUtil.setSelected(false, chkbAdvancePayment);
        CustomCommonUtil.setText("", tfTransactionNo,
                tfDestination, tfReferenceNo, tfTerm);
        CustomCommonUtil.setText("0.0000", tfTotalAmount, tfAdvancePAmount, tfAdvancePRate,
                tfDiscountAmount, tfDiscountRate, tfNetAmount);
    }

    private void clearDetailFields() {
        CustomCommonUtil.setText("", tfBarcode, tfDescription, tfBrand, tfModel,
                tfColor, tfCategory, tfInventoryType, tfMeasure, tfClass, tfAMC
        );
        CustomCommonUtil.setText("0.00", tfOrderQuantity, tfQOH, tfRequestQuantity, tfBO, tfRO, tfROQ);
        tfCost.setText("0.0000");
    }

    private void initButtons(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setVisible(!lbShow, btnBrowse, btnClose, btnNew);
        CustomCommonUtil.setManaged(!lbShow, btnBrowse, btnClose, btnNew);

        CustomCommonUtil.setVisible(lbShow, btnSearch, btnSave, btnCancel);
        CustomCommonUtil.setManaged(lbShow, btnSearch, btnSave, btnCancel);

        CustomCommonUtil.setVisible(false, btnUpdate, btnPrint);
        CustomCommonUtil.setManaged(false, btnUpdate, btnPrint);

        btnTransHistory.setVisible(fnEditMode != EditMode.ADDNEW && fnEditMode != EditMode.UNKNOWN);
        btnTransHistory.setManaged(fnEditMode != EditMode.ADDNEW && fnEditMode != EditMode.UNKNOWN);
        if (poPurchasingController.PurchaseOrder().Master().getPrint().equals("1")) {
            btnPrint.setText("Reprint");
        } else {
            btnPrint.setText("Print");
        }
        if (fnEditMode == EditMode.READY) {
            switch (poPurchasingController.PurchaseOrder().Master().getTransactionStatus()) {
                case PurchaseOrderStatus.OPEN:
                case PurchaseOrderStatus.CONFIRMED:
                case PurchaseOrderStatus.RETURNED:
                    CustomCommonUtil.setVisible(true, btnPrint, btnUpdate);
                    CustomCommonUtil.setManaged(true, btnPrint, btnUpdate);
                    break;
                case PurchaseOrderStatus.APPROVED:
                    btnPrint.setVisible(true);
                    btnPrint.setManaged(true);
                    break;
            }

        }
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);
        /*Master Fields */
        CustomCommonUtil.setDisable(!lbShow,
                dpTransactionDate, tfDestination, taRemarks,
                dpExpectedDlvrDate, tfReferenceNo, tfTerm,
                chkbAdvancePayment);

        CustomCommonUtil.setDisable(true, tfDiscountRate, tfDiscountAmount,
                tfAdvancePRate, tfAdvancePAmount, tfOrderQuantity, tfBarcode, tfDescription);

        if (!tfReferenceNo.getText().isEmpty()) {
            dpTransactionDate.setDisable(!lbShow);
        }
        if (pnTblDetailRow >= 0 && pnTblDetailRow < poPurchasingController.PurchaseOrder().Detail().size()) {
            try {
                if (!poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getInventoryTypeId().equals("0007")) {
                    if (pnTblDetailRow != -1) {
                        CustomCommonUtil.setDisable(!lbShow, tfBarcode, tfDescription);
                    }
                    boolean isSourceNotEmpty = !poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getSouceNo().isEmpty();
                    tfBarcode.setDisable(isSourceNotEmpty);
                    tfDescription.setDisable(isSourceNotEmpty);
                }
                if (pnTblDetailRow != -1) {
                    CustomCommonUtil.setDisable(!lbShow, tfOrderQuantity);
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (chkbAdvancePayment.isSelected()) {
            CustomCommonUtil.setDisable(!lbShow, tfAdvancePRate, tfAdvancePAmount);
        }
        if (tblVwStockRequest.getItems().isEmpty()) {
            pagination.setVisible(false);
            pagination.setManaged(false);
        }
        if (poPurchasingController.PurchaseOrder().Master().getTranTotal().doubleValue() > 0.0000) {
            tfDiscountRate.setDisable(!lbShow);
            tfDiscountAmount.setDisable(!lbShow);
        }

        tfSupplier.setDisable(fnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setVisible(false, piTableDetailLoading, piTableStockRequestLoading, apTableDetailLoading, apTableStockRequestLoading);
        CustomCommonUtil.setManaged(false, piTableDetailLoading, piTableStockRequestLoading, apTableDetailLoading, apTableStockRequestLoading);
    }

    private void loadTableMain() {
        btnRetrieve.setDisable(true);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50); // Set size to 200x200
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER); // Center it

        tblVwStockRequest.setPlaceholder(loadingPane); // Show while loading
        progressIndicator.setVisible(true); // Make sure it's visible
        progressIndicator.setManaged(true); // Make sure it's visible

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Simulate loading delay
                    main_data.clear();
                    JSONObject poJSON = poPurchasingController.PurchaseOrder().getApprovedStockRequests();

                    if ("success".equals(poJSON.get("result"))) {
                        JSONArray approvedRequests = (JSONArray) poJSON.get("data");
                        main_data.clear();  // Ensure old data is removed
                        if (approvedRequests != null && !approvedRequests.isEmpty()) {
                            for (Object requestObj : approvedRequests) {
                                JSONObject obj = (JSONObject) requestObj;
                                ModelPurchaseOrder loApprovedStockRequest = new ModelPurchaseOrder(
                                        String.valueOf(main_data.size() + 1),
                                        obj.get("sBranchNm") != null ? obj.get("sBranchNm").toString() : "",
                                        obj.get("dTransact") != null ? obj.get("dTransact").toString() : "",
                                        obj.get("sTransNox") != null ? obj.get("sTransNox").toString() : "",
                                        obj.get("total_details") != null ? obj.get("total_details").toString() : "",
                                        obj.get("sTransNox") != null ? obj.get("sTransNox").toString() : "",
                                        "0",
                                        obj.get("request_type") != null ? obj.get("request_type").toString() : "",
                                        "",
                                        ""
                                );
                                main_data.add(loApprovedStockRequest);
                            }
                        } else {
                            // Ensure main_data remains empty
                            main_data.clear();
                        }
                    }

                    Platform.runLater(() -> {
                        if (main_data.isEmpty()) {
                            tblVwStockRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                            tblVwStockRequest.setItems(FXCollections.observableArrayList(main_data));
                        } else {
                            tblVwStockRequest.setItems(FXCollections.observableArrayList(main_data));
                        }
                        selectTheExistedDetailFromMainTable();
                    });

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PurchaseOrder_EntryLPController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                btnRetrieve.setDisable(false);
                if (main_data == null || main_data.isEmpty()) {
                    tblVwStockRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                } else {
                    if (pagination != null) {
                        pagination.setPageCount((int) Math.ceil((double) main_data.size() / pnTblMain_Page));
                        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                            createPage(newIndex.intValue());
                        });
                    }
                    createPage(0);
                    pagination.setVisible(true);
                    pagination.setManaged(true);
                    progressIndicator.setVisible(false);
                    progressIndicator.setManaged(false);
                    tblVwStockRequest.toFront();
                }
            }

            @Override
            protected void failed() {
                btnRetrieve.setDisable(false);
                pagination.setVisible(true);
                pagination.setManaged(true);
                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);
                tblVwStockRequest.toFront();
            }
        };
        new Thread(task).start(); // Run task in background
    }

    private Node createPage(int pageIndex) {
        int totalPages = (int) Math.ceil((double) main_data.size() / pnTblMain_Page);
        if (totalPages == 0) {
            totalPages = 1;
        }

        pageIndex = Math.max(0, Math.min(pageIndex, totalPages - 1));
        int fromIndex = pageIndex * pnTblMain_Page;
        int toIndex = Math.min(fromIndex + pnTblMain_Page, main_data.size());

        if (!main_data.isEmpty()) {
            tblVwStockRequest.setItems(FXCollections.observableArrayList(main_data.subList(fromIndex, toIndex)));
        }

        if (pagination != null) { // Replace with your actual Pagination variable
            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(pageIndex);
        }

        return tblVwStockRequest;
    }

    private void initTableStockRequest() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            tblVwStockRequest.setEditable(true);
        } else {
            tblVwStockRequest.setEditable(false);
        }

        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo, tblNoOfItems);
        JFXUtil.setColumnLeft(tblBranchName);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwStockRequest);

        initTableHighlithers();
    }

    private void initTableHighlithers() {
        tblVwStockRequest.setRowFactory(tv -> new TableRow<ModelPurchaseOrder>() {
            @Override
            protected void updateItem(ModelPurchaseOrder item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Assuming empIndex05 corresponds to an employee status
                    String status = item.getIndex07(); // Replace with actual getter
                    switch (status) {
                        case PurchaseOrderStatus.CONFIRMED:
                            setStyle("-fx-background-color: #A7C7E7;");
                            break;
                        default:
                            setStyle("");
                    }
                    tblVwStockRequest.refresh();
                }
            }
        });
        tblVwOrderDetails.setRowFactory(tv -> new TableRow<ModelPurchaseOrderDetail>() {
            @Override
            protected void updateItem(ModelPurchaseOrderDetail item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    String status = item.getIndex10(); // Replace with actual getter
                    switch (status) {
                        case "1":
                            setStyle("-fx-background-color: #FAA0A0;");
                            break;
                        default:
                            setStyle("");
                    }
                    tblVwStockRequest.refresh();
                }
            }
        });
    }

    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        detail_data.clear();
        tblVwOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelPurchaseOrderDetail>> task = new Task<List<ModelPurchaseOrderDetail>>() {
            @Override
            protected List<ModelPurchaseOrderDetail> call() throws Exception {
                try {
                    int detailCount = poPurchasingController.PurchaseOrder().getDetailCount();
                    if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
                        Model_PO_Detail lastDetail = poPurchasingController.PurchaseOrder().Detail(detailCount - 1);
                        if (lastDetail.getStockID() != null && !lastDetail.getStockID().isEmpty()) {
                            poPurchasingController.PurchaseOrder().AddDetail();
                            detailCount++;
                        }
                    }
                    double grandTotalAmount = 0.0000;
                    List<ModelPurchaseOrderDetail> detailsList = new ArrayList<>();

                    for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
                        Model_PO_Detail orderDetail = poPurchasingController.PurchaseOrder().Detail(lnCtr);
                        double lnTotalAmount = orderDetail.getUnitPrice().doubleValue() * orderDetail.getQuantity().intValue();
                        grandTotalAmount += lnTotalAmount;
                        double lnRequestQuantity = 0.00;
                        String status = "0";
                        double lnTotalQty = 0.0000;
                        switch (poPurchasingController.PurchaseOrder().Detail(lnCtr).getSouceCode()) {
                            case PurchaseOrderStatus.SourceCode.STOCKREQUEST:
                                lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(lnCtr).InvStockRequestDetail().getApproved();
                                lnTotalQty = (poPurchasingController.PurchaseOrder().Detail(lnCtr).InvStockRequestDetail().getPurchase()
                                        + poPurchasingController.PurchaseOrder().Detail(lnCtr).InvStockRequestDetail().getIssued()
                                        + poPurchasingController.PurchaseOrder().Detail(lnCtr).InvStockRequestDetail().getCancelled());
                                if (!poPurchasingController.PurchaseOrder().Detail(lnCtr).getSouceNo().isEmpty()) {
                                    if (lnRequestQuantity != lnTotalQty) {
                                        status = "1";
                                    }
                                }
                                break;
                            case PurchaseOrderStatus.SourceCode.POQUOTATION:
                                lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(lnCtr).POQuotationDetail().getQuantity();
                                break;
                        }
                        detailsList.add(new ModelPurchaseOrderDetail(
                                String.valueOf(lnCtr + 1),
                                orderDetail.getSouceNo(),
                                orderDetail.Inventory().getBarCode(),
                                orderDetail.Inventory().getDescription(),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(orderDetail.getUnitPrice(), true),
                                "0",
                                CustomCommonUtil.setIntegerValueToDecimalFormat(lnRequestQuantity),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(orderDetail.getQuantity()),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalAmount, true),
                                status
                        ));
                    }
                    final double totalAmountFinal = grandTotalAmount;
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblVwOrderDetails.setItems(detail_data);
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (totalAmountFinal <= 0.0) {
                                tfDiscountRate.setText("0.00");
                                tfAdvancePRate.setText("0.00");
                                tfDiscountAmount.setText("0.0000");
                                tfAdvancePAmount.setText("0.0000");
                                poPurchasingController.PurchaseOrder().Master().setAdditionalDiscount(0.0000);
                                poPurchasingController.PurchaseOrder().Master().setDiscount(0.0);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesAmount(0.0000);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesPercentage(0.0);
                            }
                            poPurchasingController.PurchaseOrder().Master().setTranTotal(totalAmountFinal);
                            poPurchasingController.PurchaseOrder().computeNetTotal();
                            tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount()));
                            tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage()));
                            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getTranTotal(), true));
                            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                        }
                        reselectLastDetailRow();
                        initFields(pnEditMode);
                    });

                    return detailsList;

                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void initTableDetail() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblRequestQuantityDetail, tblOrderQuantityDetail, tblTotalAmountDetail, tblROQDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwOrderDetails);
        initTableHighlithers();
    }

// Method to reselect the last clicked row
    private void reselectLastDetailRow() {
        if (pnTblDetailRow >= 0 && pnTblDetailRow < tblVwOrderDetails.getItems().size()) {
            tblVwOrderDetails.getSelectionModel().clearAndSelect(pnTblDetailRow);
            tblVwOrderDetails.getSelectionModel().focus(pnTblDetailRow); // Scroll to the selected row if needed
        }
    }

    private void initTextFieldsProperty() {
        tfSupplier.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        if (!isExchangingSupplier()) {
                            return;
                        }
                        poPurchasingController.PurchaseOrder().Master().setSupplierID("");
                        poPurchasingController.PurchaseOrder().Master().setAddressID("");
                        poPurchasingController.PurchaseOrder().Master().setContactID("");
                        tfSupplier.setText("");
                        prevSupplier = "";
                        tblVwStockRequest.getItems().clear();
                        main_data.clear();
                        tblVwStockRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            poPurchasingController.PurchaseOrder().Master().setTermCode("0000004");
                            tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                        }
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(PurchaseOrder_EntryLPController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }

    private boolean isExchangingSupplier() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            boolean isHaveQuantityAndStockId = false;
            if (poPurchasingController.PurchaseOrder().getDetailCount() >= 1) {
                if (poPurchasingController.PurchaseOrder().Detail(0).getStockID() != null && poPurchasingController.PurchaseOrder().Detail(0).getQuantity() != null) {
                    if (!poPurchasingController.PurchaseOrder().Detail(0).getStockID().isEmpty()
                            || poPurchasingController.PurchaseOrder().Detail(0).getQuantity().intValue() != 0) {
                        isHaveQuantityAndStockId = true;
                    }
                }
            }
            if (isHaveQuantityAndStockId) {
                if (ShowMessageFX.YesNo("PO Details have already items, are you sure you want to change supplier?", psFormName, null)) {
                    try {
                        int detailCount = poPurchasingController.PurchaseOrder().getDetailCount();
                        for (int lnCtr = detailCount - 1; lnCtr >= 0; lnCtr--) {
                            if (poPurchasingController.PurchaseOrder().Detail(lnCtr).getSouceNo().isEmpty()
                                    && poPurchasingController.PurchaseOrder().Detail(lnCtr).getStockID().isEmpty()
                                    && poPurchasingController.PurchaseOrder().Detail(lnCtr).getQuantity().intValue() == 0) {
                                continue; // Skip deleting this row
                            }
                            poPurchasingController.PurchaseOrder().Detail().remove(lnCtr);
                        }
                        pnTblDetailRow = -1;
                        pnTblMainRow = -1;
                        tblVwStockRequest.getSelectionModel().clearSelection();
                        poPurchasingController.PurchaseOrder().Master().setTermCode("0000004");
                        tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                        clearDetailFields();
                        loadTableDetail();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(PurchaseOrder_EntryMPController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    if (poPurchasingController.PurchaseOrder().Master().getSupplierID().isEmpty()) {
                        return false;
                    } else {
                        try {
                            poJSON = new JSONObject();
                            poJSON = poPurchasingController.PurchaseOrder().SearchSupplier(poPurchasingController.PurchaseOrder().Master().getSupplierID(), true);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return false;
                            }
                            tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                            poPurchasingController.PurchaseOrder().Master().setTermCode("0000004");
                            tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                            selectTheExistedDetailFromMainTable();
                            return false;

                        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                            Logger.getLogger(PurchaseOrder_EntryMPController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            }
        }
        if (pnEditMode == EditMode.READY) {
            try {
                if (!tfTransactionNo.getText().isEmpty()
                        && !tfDestination.getText().isEmpty()) {
                    if (ShowMessageFX.YesNo("You have an open transaction. Are you sure you want to change the supplier?", psFormName, null)) {
                        clearDetailFields();
                        clearMasterFields();
                        detail_data.clear();
                        tblVwOrderDetails.getItems().clear();
                        pnEditMode = EditMode.UNKNOWN;
                        poPurchasingController.PurchaseOrder().Master().setSupplierID(prevSupplier);
                        tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                        pnTblMainRow = -1;
                        tblVwStockRequest.getItems().clear();
                        tblVwStockRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        initButtons(pnEditMode);
                        initFields(pnEditMode);
                        return true;
                    } else {
                        poJSON = new JSONObject();
                        poJSON = poPurchasingController.PurchaseOrder().SearchSupplier(poPurchasingController.PurchaseOrder().Master().getSupplierID(), true);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            return false;
                        }
                        tfSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                        selectTheExistedDetailFromMainTable();
                        return false;

                    }
                }
            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(PurchaseOrder_EntryMPController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
    }

    private void tblVwMain_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            pnTblMainRow = tblVwStockRequest.getSelectionModel().getSelectedIndex();
            if (event.getClickCount() == 2) {
                if (!tfSupplier.getText().isEmpty()) {
                    ModelPurchaseOrder loSelectedStockRequest = (ModelPurchaseOrder) tblVwStockRequest.getSelectionModel().getSelectedItem();
                    if (loSelectedStockRequest != null) {
                        String lsTransactionNo = loSelectedStockRequest.getIndex06();
                        String lsSource = loSelectedStockRequest.getIndex08();
                        try {
                            switch (lsSource) {
                                case PurchaseOrderStatus.SourceCode.STOCKREQUEST:
                                    poJSON = poPurchasingController.PurchaseOrder().addStockRequestOrdersToPODetail(lsTransactionNo);
                                    break;
                                case PurchaseOrderStatus.SourceCode.POQUOTATION:
                                    poJSON = poPurchasingController.PurchaseOrder().addPOQuotationToPODetail(lsTransactionNo);
                                    break;
                            }
                            if ("success".equals(poJSON.get("result"))) {
                                if (poPurchasingController.PurchaseOrder().getDetailCount() > 0) {
                                    pnTblDetailRow = poPurchasingController.PurchaseOrder().getDetailCount() - 1;
                                    loadTableDetailAndSelectedRow();
                                    main_data.get(pnTblMainRow).setIndex07(PurchaseOrderStatus.CONFIRMED);
                                    selectTheExistedDetailFromMainTable();
                                }
                            } else {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);

                            }
                        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(PurchaseOrder_EntryLPController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                        }
                    }
                } else {
                    ShowMessageFX.Warning("Can't allow to add this transaction if the company or supplier is empty.", psFormName, null);
                }
            }
        }
    }

    private void loadTableDetailAndSelectedRow() {
        if (pnTblDetailRow >= 0) {
            Platform.runLater(() -> {
                // Run a delay after the UI thread is free
                PauseTransition delay = new PauseTransition(Duration.millis(10));
                delay.setOnFinished(event -> {
                    Platform.runLater(() -> { // Run UI updates in the next cycle
                        loadTableDetail();
                    });
                });
                delay.play();
            });
            loadRecordDetail();
            initDetailFocus();
        }
    }

    private void selectTheExistedDetailFromMainTable() {
        if (!main_data.isEmpty()) {
            Set<String> existingDetailIds = detail_data.stream()
                    .map(ModelPurchaseOrderDetail::getIndex02)
                    .collect(Collectors.toSet());

            if (pnEditMode != EditMode.ADDNEW) {
                for (ModelPurchaseOrder master : main_data) {
                    master.setIndex07(existingDetailIds.contains(master.getIndex06()) ? PurchaseOrderStatus.CONFIRMED : PurchaseOrderStatus.OPEN);
                }
            }

            tblVwStockRequest.refresh();
        }
    }

    private void tblVwDetail_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnTblDetailRow = tblVwOrderDetails.getSelectionModel().getSelectedIndex();
            ModelPurchaseOrderDetail selectedItem = tblVwOrderDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                clearDetailFields();
                if (selectedItem != null) {
                    if (pnTblDetailRow >= 0) {
                        loadRecordDetail();
                        initDetailFocus();

                    }
                }
                initFields(pnEditMode);
            }
        }
    }

    private void initDetailFocus() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            if (pnTblDetailRow >= 0) {
                try {
                    boolean isSourceNotEmpty = !poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getSouceNo().isEmpty();
                    tfBarcode.setDisable(isSourceNotEmpty);
                    tfDescription.setDisable(isSourceNotEmpty);
                    if (poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().getInventoryTypeId().equals("0007")) {
                        CustomCommonUtil.setDisable(false, tfBarcode, tfDescription);
                        tfOrderQuantity.requestFocus();
                    }
                    if (isSourceNotEmpty && !tfBarcode.getText().isEmpty()) {
                        tfOrderQuantity.requestFocus();
                    } else {
                        if (!tfBarcode.getText().isEmpty() && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                            tfOrderQuantity.requestFocus();
                        } else {
                            tfBarcode.requestFocus();
                        }
                    }
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PurchaseOrder_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
