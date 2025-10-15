/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPurchaseOrder;
import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import org.json.simple.parser.ParseException;

/**
 * FXML Controller class
 *
 * @author User
 */
public class PurchaseOrder_ApprovalMCController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private PurchaseOrderControllers poPurchasingController;
    private String psFormName = "Purchase Order Approval MC";
    private LogWrapper logWrapper;
    private JSONObject poJSON;

    private int pnEditMode;
    private int pnTblMainRow = -1;
    private int pnTblDetailRow = -1;
    private int pnTblMain_Page = 50;

    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private String psSupplierID = "";
    private String psReferID = "";
    private String psOldDate = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelPurchaseOrder> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPurchaseOrderDetail> detail_data = FXCollections.observableArrayList();
    @FXML
    private AnchorPane AnchorMaster, AnchorDetails, AnchorMain, apBrowse, apButton;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSave, btnCancel, btnApprove, btnReturn, btnVoid, btnPrint,
            btnRetrieve, btnTransHistory, btnClose;
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
    private TextField tfBrand, tfModel, tfVariant, tfInventoryType, tfColor, tfClass, tfAMC, tfROQ,
            tfRO, tfBO, tfQOH, tfCost, tfRequestQuantity, tfOrderQuantity;
    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView<ModelPurchaseOrderDetail> tblVwOrderDetails;
    @FXML
    private TableColumn<ModelPurchaseOrderDetail, String> tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail,
            tblCostDetail, tblROQDetail, tblRequestQuantityDetail, tblOrderQuantityDetail, tblTotalAmountDetail;
    @FXML
    private TableView<ModelPurchaseOrder> tblVwPurchaseOrder;
    @FXML
    private TableColumn<ModelPurchaseOrder, String> tblRowNo, tblTransactionNo, tblDate, tblSupplier;
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
            poPurchasingController.PurchaseOrder().setTransactionStatus(PurchaseOrderStatus.CONFIRMED);
            poPurchasingController.PurchaseOrder().setWithUI(true);
            poJSON = poPurchasingController.PurchaseOrder().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            Platform.runLater((() -> {
                poPurchasingController.PurchaseOrder().Master().setIndustryID(psIndustryID);
                poPurchasingController.PurchaseOrder().Master().setCompanyID(psCompanyID);
                poPurchasingController.PurchaseOrder().Master().setCategoryCode(psCategoryID);
                loadRecordSearch();
            }));
            tblVwOrderDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
            initAll();

        } catch (ExceptionInInitializerError ex) {
            Logger.getLogger(PurchaseOrder_ApprovalMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poPurchasingController.PurchaseOrder().Master().Company().getCompanyName() + " - " + poPurchasingController.PurchaseOrder().Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_ApprovalMCController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initAll() {
        initButtonsClickActions();
        initTextFieldFocus();
        initTextAreaFocus();
        initTextFieldKeyPressed();
        initDatePickerActions();
        initTextFieldPattern();
        initTableMain();
        initTableDetail();
        initTextFieldsProperty();
        initCheckBoxActions();
        tblVwPurchaseOrder.setOnMouseClicked(this::tblVwMain_Clicked);
        tblVwOrderDetails.setOnMouseClicked(this::tblVwDetail_Clicked);
        pnEditMode = EditMode.UNKNOWN;
        initButtons(pnEditMode);
        initFields(pnEditMode);
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
            tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount().doubleValue()));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getAdditionalDiscount(), true));
            chkbAdvancePayment.setSelected((poPurchasingController.PurchaseOrder().Master().getWithAdvPaym() == true));
            tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage().doubleValue()));
            tfAdvancePAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesAmount(), true));
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getTranTotal(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryCarController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void loadRecordDetail() {
        try {
            if (pnTblDetailRow < 0 || pnTblDetailRow > poPurchasingController.PurchaseOrder().getDetailCount() - 1) {
                return;
            }
            if (pnTblDetailRow >= 0) {
                tfBrand.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Brand().getDescription() != null ? poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Brand().getDescription() : "");
                tfModel.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Model().getDescription() != null ? poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Model().getDescription() : "");
                tfVariant.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Variant().getDescription());
                tfInventoryType.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().InventoryType().getDescription());
                tfColor.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).Inventory().Color().getDescription());
                tfClass.setText(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InventoryMaster().getInventoryClassification());
                tfAMC.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InventoryMaster().getAverageCost()));
                tfROQ.setText("0");
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
                tfRequestQuantity.setText(CustomCommonUtil.setDecimalValueToIntegerFormat(lnRequestQuantity));
                tfOrderQuantity.setText(String.valueOf(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getQuantity().intValue()));
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_EntryCarController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVoid, btnReturn,
                btnPrint, btnRetrieve, btnTransHistory, btnClose, btnApprove);

        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));

    }

    private void handleButtonAction(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poPurchasingController.PurchaseOrder().UpdateTransaction();
                    pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                    }
                    clearDetailFields();
                    pnTblDetailRow = -1;
                    loadTableDetail();
                    pagination.toFront();
                    break;
                case "btnApprove":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to approve transaction?")) {
                        poJSON = poPurchasingController.PurchaseOrder().ApproveTransaction("");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        if (!"success".equals((poJSON = poPurchasingController.PurchaseOrder().OpenTransaction(poPurchasingController.PurchaseOrder().Master().getTransactionNo())).get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            return;
                        }
                        if (ShowMessageFX.YesNo(null, psFormName, "Do you want to print this transaction?")) {
                            poJSON = poPurchasingController.PurchaseOrder().printTransaction(PurchaseOrderStaticData.Printing_CAR_MC_MPUnit_Appliance);
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            }
                        }
                        clearMasterFields();
                        clearDetailFields();
                        detail_data.clear();
                        pnEditMode = EditMode.UNKNOWN;
                        pnTblDetailRow = -1;
                        //this code below use to highlight tblpurchase
                        tblVwPurchaseOrder.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PurchaseOrderStatus.APPROVED);
                        pagination.toBack();
                    } else {
                        return;
                    }
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save?")) {
                        return;
                    }
                    LocalDate selectedLocalDate = dpTransactionDate.getValue();
                    if (!psOldDate.isEmpty()) {
                        if (!CustomCommonUtil.formatLocalDateToShortString(selectedLocalDate).equals(psOldDate) && tfReferenceNo.getText().isEmpty()) {
                            ShowMessageFX.Warning("A reference number is required for backdated transactions.", psFormName, null);
                            return;
                        }
                    }

                    pnTblDetailRow = -1;

                    if (pnEditMode == EditMode.UPDATE) {
                        poPurchasingController.PurchaseOrder().Master().setModifiedDate(poApp.getServerDate());
                        poPurchasingController.PurchaseOrder().Master().setModifyingId(poApp.getUserID());
                        for (int i = 0; i < poPurchasingController.PurchaseOrder().getDetailCount(); i++) {
                            poPurchasingController.PurchaseOrder().Detail(i).setModifiedDate(poApp.getServerDate());
                        }
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
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        return;
                    } else {
                        if (poPurchasingController.PurchaseOrder().Master().getTransactionStatus().equals(PurchaseOrderStatus.CONFIRMED)) {
                            if (ShowMessageFX.YesNo(null, psFormName, "Do you want to approve this transaction?")) {
                                if ("success".equals((poJSON = poPurchasingController.PurchaseOrder().ApproveTransaction("")).get("result"))) {
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
                        poJSON = poPurchasingController.PurchaseOrder().printTransaction(PurchaseOrderStaticData.Printing_CAR_MC_MPUnit_Appliance);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Print Purchase Order", null);
                        }
                    }
                    poJSON = poPurchasingController.PurchaseOrder().OpenTransaction(poPurchasingController.PurchaseOrder().Master().getTransactionNo());
                    loadRecordMaster();
                    loadRecordDetail();
                    loadTableDetail();
                    pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                    pagination.toBack();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, "Cancel Confirmation", "Are you sure you want to cancel?")) {
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
                            pagination.toBack();
                        }
                    }
                    break;
                case "btnPrint":
                    poJSON = poPurchasingController.PurchaseOrder().printTransaction(PurchaseOrderStaticData.Printing_CAR_MC_MPUnit_Appliance);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        break;
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain();
                    break;
                case "btnTransHistory":
                    break;
                case "btnReturn":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to return transaction?")) {
                        poJSON = poPurchasingController.PurchaseOrder().ReturnTransaction("");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        clearMasterFields();
                        clearDetailFields();
                        detail_data.clear();
                        pnEditMode = EditMode.UNKNOWN;

                        tblVwPurchaseOrder.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PurchaseOrderStatus.RETURNED);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to return transaction?")) {
                        poJSON = poPurchasingController.PurchaseOrder().VoidTransaction("");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        clearMasterFields();
                        clearDetailFields();
                        detail_data.clear();
                        pnEditMode = EditMode.UNKNOWN;

                        //this code below use to highlight tblpurchase
                        tblVwPurchaseOrder.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PurchaseOrderStatus.VOID);
                    } else {
                        return;
                    }
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
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(PurchaseOrder_ApprovalMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTextFieldFocus() {
        List<TextField> loTxtField = Arrays.asList(tfReferenceNo, tfDiscountRate, tfDiscountAmount,
                tfAdvancePRate, tfAdvancePAmount, tfOrderQuantity, tfSearchReferenceNo);
        loTxtField.forEach(tf -> tf.focusedProperty().addListener(txtField_Focus));
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
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount().doubleValue()));
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

                    tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage().doubleValue()));
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
                case "tfSearchReferenceNo":
                    psReferID = tfSearchReferenceNo.getText();
                    loadTableMain();
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
        List<TextField> loTxtField = Arrays.asList(tfAdvancePAmount,
                tfReferenceNo, tfDiscountRate, tfDiscountAmount,
                tfAdvancePRate,
                tfOrderQuantity, tfSearchSupplier, tfSearchReferenceNo, tfCost);

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
        poJSON = new JSONObject();
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfDestination":
                                poJSON = poPurchasingController.PurchaseOrder().SearchDestination(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDestination.setText("");
                                    break;
                                }
                                tfDestination.setText(poPurchasingController.PurchaseOrder().Master().Branch().getBranchName());
                                break;
                            case "tfSearchSupplier":
                                poJSON = poPurchasingController.PurchaseOrder().SearchSupplier(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfSupplier.setText("");
                                    break;
                                }
                                psSupplierID = poPurchasingController.PurchaseOrder().Master().getSupplierID();
                                tfSearchSupplier.setText(poPurchasingController.PurchaseOrder().Master().Supplier().getCompanyName());
                                loadTableMain();
                                break;
                            case "tfTerm":
                                poJSON = poPurchasingController.PurchaseOrder().SearchTerm(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfTerm.setText("");
                                    break;
                                }
                                tfTerm.setText(poPurchasingController.PurchaseOrder().Master().Term().getDescription());
                                break;

                        }
                        event.consume();
                        switch (txtFieldID) {
                            case "tfDestination":
                            case "tfTerm":
                            case "tfAdvancePAmount":
                            case "tfAdvancePRate":
                            case "tfDiscountRate":
                            case "tfDiscountAmount":
                            case "tfSearchReferenceNo":
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                break;
                            case "tfCost":
                                setOrderCost(tfCost.getText().replace(",", ""));
                                loadTableDetailAndSelectedRow();
                                break;
                            case "tfOrderQuantity":
                                setOrderQuantityToDetail(tfOrderQuantity.getText());
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
                    case UP:
                        setOrderQuantityToDetail(tfOrderQuantity.getText());
                        poJSON = poPurchasingController.PurchaseOrder().netTotalChecker(pnTblDetailRow);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            tfOrderQuantity.setText("0");
                            loadRecordMaster();
                            loadTableDetailAndSelectedRow();
                            break;
                        }
                        if (!lsTxtField.equals("tfBrand") && !lsTxtField.equals("tfModel")) {
                            if (pnTblDetailRow > 0 && !detail_data.isEmpty()) {
                                pnTblDetailRow--;
                            }
                        }

                        // Prevent going from 'tfOrderQuantity' to 'taRemarks'
                        if (!lsTxtField.equals("tfBrand") && !lsTxtField.equals("tfOrderQuantity")) {
                            CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        }
                        loadTableDetailAndSelectedRow();
                        event.consume();
                        break;
                    case DOWN:
                        setOrderQuantityToDetail(tfOrderQuantity.getText());
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
        } catch (ExceptionInInitializerError | SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PurchaseOrder_ApprovalMCController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setOrderCost(String fsValue) {
        if (fsValue.isEmpty()) {
            fsValue = "0.0000";
        }

        double lnCostDetail = Double.parseDouble(fsValue.replace(",", ""));
        if (lnCostDetail < 0.0000) {
            ShowMessageFX.Warning("Invalid Order Cost", psFormName, null);
            fsValue = "0.0000";
        }

        poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).setUnitPrice(lnCostDetail);
        tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).setUnitPrice(lnCostDetail), true));
    }

    private void setOrderQuantityToDetail(String fsValue) {
        if (fsValue.isEmpty()) {
            fsValue = "0";
        }
        if (Integer.parseInt(fsValue) < 0) {
            ShowMessageFX.Warning("Invalid Order Quantity", psFormName, null);
            fsValue = "0";
        }
        if (pnTblDetailRow < 0) {
            fsValue = "0";
            ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
            clearDetailFields();
            int detailCount = poPurchasingController.PurchaseOrder().getDetailCount();
            pnTblDetailRow = detailCount > 0 ? detailCount - 1 : 0;
        }
        double lnRequestQuantity = 0;
        try {
            lnRequestQuantity = poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).InvStockRequestDetail().getApproved();
            if (!poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getSouceNo().isEmpty()) {
                if (Integer.parseInt(fsValue) > lnRequestQuantity) {
                    ShowMessageFX.Warning("Invalid order quantity entered. The item is from a stock request, and the order quantity must not be greater than the requested quantity.", psFormName, null);
                    fsValue = "0";

                }
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrder_ApprovalMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
        poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).setQuantity(Double.valueOf(fsValue));
        tfOrderQuantity.setText(String.valueOf(poPurchasingController.PurchaseOrder().Detail(pnTblDetailRow).getQuantity().intValue()));
    }

    private void initTextFieldPattern() {
        JFXUtil.inputDecimalOnly(tfDiscountRate, tfAdvancePRate);
        JFXUtil.setCommaFormatter(tfCost, tfDiscountAmount, tfAdvancePAmount);
        CustomCommonUtil.inputIntegersOnly(tfOrderQuantity);
    }

    private void initDatePickerActions() {
        dpTransactionDate.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE) {
                LocalDate selectedLocalDate = dpTransactionDate.getValue();
                LocalDate transactionDate = new java.sql.Date(poPurchasingController.PurchaseOrder().Master().getTransactionDate().getTime()).toLocalDate();
                if (selectedLocalDate == null) {
                    return;
                }
                LocalDate dateNow = LocalDate.now();
                String lsReferNo = tfReferenceNo.getText().trim();
                boolean approved = true;
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
                if (approved) {
                    poPurchasingController.PurchaseOrder().Master().setTransactionDate(
                            SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                } else {
                    poPurchasingController.PurchaseOrder().Master().setTransactionDate(
                            SQLUtil.toDate(psOldDate, SQLUtil.FORMAT_SHORT_DATE));
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
                                tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage().doubleValue()));
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
                    ShowMessageFX.Warning("Advance payment cannot be entered until the total amount is greater than 0.00.", psFormName, null);
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
        CustomCommonUtil.setText("", tfTransactionNo, tfDestination, tfReferenceNo, tfTerm);
        CustomCommonUtil.setText("0.00", tfAdvancePRate, tfDiscountRate);
        CustomCommonUtil.setText("0.0000", tfTotalAmount, tfAdvancePAmount, tfDiscountAmount, tfNetAmount);
    }

    private void clearDetailFields() {
        /* Detail Fields*/
        CustomCommonUtil.setText("", tfBrand, tfModel, tfBrand, tfVariant, tfColor, tfInventoryType, tfClass, tfAMC);
        CustomCommonUtil.setText("0", tfOrderQuantity, tfQOH, tfRequestQuantity, tfBO, tfRO, tfROQ);
        tfCost.setText("0.0000");
    }

    private void initButtons(int fnEditMode) {
        boolean lbShow = (pnEditMode == EditMode.UPDATE);

        btnClose.setVisible(!lbShow);
        btnClose.setManaged(!lbShow);

        CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel);
        CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel);

        CustomCommonUtil.setVisible(false, btnApprove, btnReturn, btnVoid, btnUpdate, btnPrint);
        CustomCommonUtil.setManaged(false, btnApprove, btnReturn, btnVoid, btnUpdate, btnPrint);

        btnTransHistory.setVisible(fnEditMode != EditMode.UNKNOWN);
        btnTransHistory.setManaged(fnEditMode != EditMode.UNKNOWN);
        if (poPurchasingController.PurchaseOrder().Master().getPrint().equals("1")) {
            btnPrint.setText("Reprint");
        } else {
            btnPrint.setText("Print");
        }
        if (fnEditMode == EditMode.READY) {
            switch (poPurchasingController.PurchaseOrder().Master().getTransactionStatus()) {
                case PurchaseOrderStatus.CONFIRMED:
                    CustomCommonUtil.setVisible(true, btnApprove, btnVoid, btnUpdate, btnPrint);
                    CustomCommonUtil.setManaged(true, btnApprove, btnVoid, btnUpdate, btnPrint);
                    break;
                case PurchaseOrderStatus.APPROVED:
                    btnPrint.setVisible(true);
                    btnPrint.setManaged(true);
                    break;
            }
        }
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        if (poPurchasingController.PurchaseOrder().Master().getTransactionStatus().equals(PurchaseOrderStatus.CONFIRMED)) {
            CustomCommonUtil.setDisable(!lbShow, AnchorMaster, AnchorDetails);
            CustomCommonUtil.setDisable(!lbShow,
                    dpTransactionDate, tfDestination, taRemarks,
                    dpExpectedDlvrDate, tfReferenceNo, tfTerm,
                    chkbAdvancePayment);

            CustomCommonUtil.setDisable(true, tfSupplier, tfDiscountRate, tfDiscountAmount,
                    tfAdvancePRate, tfAdvancePAmount, tfBrand, tfModel, tfCost, tfOrderQuantity);

            CustomCommonUtil.setDisable(!lbShow, tfOrderQuantity, tfCost);

            if (!tfReferenceNo.getText().isEmpty()) {
                dpTransactionDate.setDisable(!lbShow);
            }
            if (chkbAdvancePayment.isSelected()) {
                CustomCommonUtil.setDisable(!lbShow, tfAdvancePRate, tfAdvancePAmount);
            }
            if (poPurchasingController.PurchaseOrder().Master().getTranTotal().doubleValue() > 0.0000) {
                CustomCommonUtil.setDisable(!lbShow, tfDiscountRate, tfDiscountAmount);
            }
        } else {
            CustomCommonUtil.setDisable(true, AnchorMaster, AnchorDetails);
        }
        if (tblVwPurchaseOrder.getItems().isEmpty()) {
            pagination.setVisible(false);
            pagination.setManaged(false);
        }
    }

    private void loadTableMain() {
        btnRetrieve.setDisable(true);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50); // Set size to 200x200
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER); // Center it

        tblVwPurchaseOrder.setPlaceholder(loadingPane); // Show while loading
        progressIndicator.setVisible(true); // Make sure it's visible

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    main_data.clear();
                    poJSON = poPurchasingController.PurchaseOrder().getPurchaseOrder(
                            psSupplierID,
                            psReferID);
                    if ("success".equals(poJSON.get("result"))) {
                        if (poPurchasingController.PurchaseOrder().getPOMasterCount() > 0) {
                            for (int lnCntr = 0; lnCntr <= poPurchasingController.PurchaseOrder().getPOMasterCount() - 1; lnCntr++) {
                                main_data.add(new ModelPurchaseOrder(
                                        String.valueOf(lnCntr + 1),
                                        poPurchasingController.PurchaseOrder().POMaster(lnCntr).Supplier().getCompanyName(),
                                        SQLUtil.dateFormat(poPurchasingController.PurchaseOrder().POMaster(lnCntr).getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE),
                                        poPurchasingController.PurchaseOrder().POMaster(lnCntr).getTransactionNo(),
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""));
                            }
                        } else {
                            main_data.clear();
                        }
                    }

                    Platform.runLater(() -> {
                        if (main_data.isEmpty()) {
                            tblVwPurchaseOrder.setPlaceholder(new Label("NO RECORD TO LOAD"));
                            tblVwPurchaseOrder.setItems(FXCollections.observableArrayList(main_data));
                        } else {
                            tblVwPurchaseOrder.setItems(FXCollections.observableArrayList(main_data));
                        }
                    });

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PurchaseOrder_ApprovalMCController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
                if (main_data == null || main_data.isEmpty()) {
                    tblVwPurchaseOrder.setPlaceholder(new Label("NO RECORD TO LOAD"));
                } else {
                    if (pagination != null) {
                        int pageCount = (int) Math.ceil((double) main_data.size() / pnTblMain_Page);
                        pagination.setPageCount(pageCount);
                        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> createPage(newIndex.intValue()));
                    }
                    createPage(0);
                    pagination.setVisible(true);
                    pagination.setManaged(true);
                    tblVwPurchaseOrder.toFront();
                }
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
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
            tblVwPurchaseOrder.setItems(FXCollections.observableArrayList(main_data.subList(fromIndex, toIndex)));
        }

        if (pagination != null) { // Replace with your actual Pagination variable
            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(pageIndex);
        }

        return tblVwPurchaseOrder;
    }

    private void initTableMain() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblTransactionNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPurchaseOrder);
        initTableHighlithers();
    }

    private void initTableHighlithers() {
        tblVwPurchaseOrder.setRowFactory(tv -> new TableRow<ModelPurchaseOrder>() {
            @Override
            protected void updateItem(ModelPurchaseOrder item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Assuming empIndex05 corresponds to an employee status
                    String status = item.getIndex05(); // Replace with actual getter
                    switch (status) {
                        case PurchaseOrderStatus.CONFIRMED:
                            setStyle("-fx-background-color: #C1E1C1;");
                            break;
                        case PurchaseOrderStatus.VOID:
                            setStyle("-fx-background-color: #FAA0A0;");
                            break;
                        case PurchaseOrderStatus.RETURNED:
                            setStyle("-fx-background-color: #FAC898;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                    tblVwPurchaseOrder.refresh();
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
                    tblVwOrderDetails.refresh();
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
                        double lnRequestQuantity = 0;
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
                                CustomCommonUtil.setDecimalValueToIntegerFormat(lnRequestQuantity),
                                CustomCommonUtil.setDecimalValueToIntegerFormat(orderDetail.getQuantity().doubleValue()),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalAmount, true),
                                status
                        ));
                    }
                    final double totalAmountFinal = grandTotalAmount;
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblVwOrderDetails.setItems(detail_data);
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (totalAmountFinal <= 0.0000) {
                                tfDiscountRate.setText("0.00");
                                tfAdvancePRate.setText("0.00");
                                tfDiscountAmount.setText("0.0000");
                                tfAdvancePAmount.setText("0.0000");
                                poPurchasingController.PurchaseOrder().Master().setAdditionalDiscount(0.0000);
                                poPurchasingController.PurchaseOrder().Master().setDiscount(0.00);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesAmount(0.0000);
                                poPurchasingController.PurchaseOrder().Master().setDownPaymentRatesPercentage(0.00);
                            }
                            poPurchasingController.PurchaseOrder().Master().setTranTotal(totalAmountFinal);
                            poPurchasingController.PurchaseOrder().computeNetTotal();
                            tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDiscount().doubleValue()));
                            tfAdvancePRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getDownPaymentRatesPercentage().doubleValue()));
                            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getTranTotal(), true
                            ));
                            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchasingController.PurchaseOrder().Master().getNetTotal(), true));
                        }
                        reselectLastDetailRow();
                        initFields(pnEditMode);
                    });

                    return detailsList;

                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PurchaseOrder_EntryAppliancesController.class
                            .getName()).log(Level.SEVERE, null, ex);
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

    private void reselectLastDetailRow() {
        if (pnTblDetailRow >= 0 && pnTblDetailRow < tblVwOrderDetails.getItems().size()) {
            tblVwOrderDetails.getSelectionModel().clearAndSelect(pnTblDetailRow);
            tblVwOrderDetails.getSelectionModel().focus(pnTblDetailRow);
        }
    }

    private void tblVwDetail_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
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

    private void tblVwMain_Clicked(MouseEvent event) {
        poJSON = new JSONObject();
        pnTblMainRow = tblVwPurchaseOrder.getSelectionModel().getSelectedIndex();
        if (pnTblMainRow < 0 || pnTblMainRow >= tblVwPurchaseOrder.getItems().size()) {
            ShowMessageFX.Warning("Please select valid purchase order information.", "Warning", null);
            return;
        }

        if (event.getClickCount() == 2) {
            ModelPurchaseOrder loSelectedPurchaseOrder = (ModelPurchaseOrder) tblVwPurchaseOrder.getSelectionModel().getSelectedItem();
            if (loSelectedPurchaseOrder != null) {
                String lsTransactionNo = loSelectedPurchaseOrder.getIndex04();
                try {
                    poJSON = poPurchasingController.PurchaseOrder().InitTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        poJSON = poPurchasingController.PurchaseOrder().OpenTransaction(lsTransactionNo);
                        if ("success".equals((String) poJSON.get("result"))) {
                            loadRecordMaster();
                            initTableDetail();
                            loadTableDetail();
                            pnTblDetailRow = -1;
                            clearDetailFields();
                            pnEditMode = poPurchasingController.PurchaseOrder().getEditMode();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButtons(pnEditMode);
                        initFields(pnEditMode);

                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(PurchaseOrder_ApprovalMCController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                }
            }
        }
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

    private void initTextFieldsProperty() {
        tfSearchSupplier.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    poPurchasingController.PurchaseOrder().Master().setSupplierID("");
                    poPurchasingController.PurchaseOrder().Master().setAddressID("");
                    poPurchasingController.PurchaseOrder().Master().setContactID("");
                    tfSearchSupplier.setText("");
                    psSupplierID = "";
                    loadTableMain();
                }

            }
        });
        tfSearchReferenceNo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    poPurchasingController.PurchaseOrder().Master().setReference("");
                    tfSearchReferenceNo.setText("");
                    psReferID = "";
                    loadTableMain();
                }
            }
        });
    }

    private void initDetailFocus() {
        if (pnEditMode == EditMode.UPDATE) {
            if (pnTblDetailRow >= 0) {
                if (!tfBrand.getText().isEmpty()) {
                    tfOrderQuantity.requestFocus();
                }
            }
        }
    }

    private void loadTableDetailAndSelectedRow() {
        if (pnTblDetailRow >= 0) {
            Platform.runLater(() -> {
                PauseTransition delay = new PauseTransition(Duration.millis(10));
                delay.setOnFinished(event -> {
                    Platform.runLater(() -> {
                        loadTableDetail();

                    });
                });
                delay.play();
            });
            loadRecordDetail();
            initDetailFocus();
        }
    }
}
