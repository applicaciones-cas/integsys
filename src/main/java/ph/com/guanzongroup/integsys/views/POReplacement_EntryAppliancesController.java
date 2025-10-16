/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.List;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import org.json.simple.parser.ParseException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FXML Controller class
 *
 * @author Team 2
 */
public class POReplacement_EntryAppliancesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static PurchaseOrderReceivingControllers poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private boolean pbEntered = false;
    boolean lbresetpredicate = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    List<String> plOrderNo = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSerials, btnSave, btnCancel, btnPrint, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount, tfTotal, tfOrderNo, tfBrand, tfModel, tfDescription, tfBarcode, tfModelVariant, tfColor, tfInventoryType, tfMeasure, tfCost, tfOrderQuantity, tfReceiveQuantity;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail, tblRowNo, tblSupplier, tblDate, tblReferenceNo;
    @FXML
    private Pagination pgPagination;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        poController = new PurchaseOrderReceivingControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poController.PurchaseOrderReceiving().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        loadRecordMaster();
        loadTableDetail.reload();
        pgPagination.setPageCount(1);
        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
        initButton(pnEditMode);
        Platform.runLater(() -> {
            poController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
            poController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
            poController.PurchaseOrderReceiving().setIndustryId(psIndustryId);
            poController.PurchaseOrderReceiving().setCompanyId(psCompanyId);
            poController.PurchaseOrderReceiving().setCategoryId(psCategoryId);
            poController.PurchaseOrderReceiving().setWithUI(true);
            poController.PurchaseOrderReceiving().setPurpose(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT);
            loadRecordSearch();
            btnNew.fire();
        });
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

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
                        poController.PurchaseOrderReceiving().setTransactionStatus(PurchaseOrderReceivingStatus.RETURNED + "" + PurchaseOrderReceivingStatus.OPEN);
                        poJSON = poController.PurchaseOrderReceiving().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        showRetainedHighlight(false);
                        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
//                        psCompanyId = poController.PurchaseOrderReceiving().Master().getCompanyId();
                        psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();
                        break;

                    case "btnPrint":
                        poJSON = poController.PurchaseOrderReceiving().printRecord(() -> {
                            if (lsIsSaved) {
                                Platform.runLater(() -> {
                                    btnNew.fire();
                                });
                            } else {
                                loadRecordMaster();
                            }
                            lsIsSaved = false;
                        });
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnSerials":
                        if (!poController.PurchaseOrderReceiving().Detail(pnDetail).isSerialized()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Selected item is not serialized.");
                            return;
                        }
                        return;
                    case "btnNew":
                        //Clear data
                        poController.PurchaseOrderReceiving().resetMaster();
                        poController.PurchaseOrderReceiving().resetOthers();
                        poController.PurchaseOrderReceiving().Detail().clear();
                        clearTextFields();

                        poJSON = poController.PurchaseOrderReceiving().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poController.PurchaseOrderReceiving().initFields();

                        if (!psCompanyId.isEmpty()) {
                            poController.PurchaseOrderReceiving().SearchCompany(psCompanyId, true);
                        }
                        if (!psSupplierId.isEmpty()) {
                            poController.PurchaseOrderReceiving().SearchSupplier(psSupplierId, true);
                        }
                        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                        showRetainedHighlight(false);
                        break;
                    case "btnUpdate":
                        poJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
                        poJSON = poController.PurchaseOrderReceiving().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        //Populate purhcase receiving serials
                        for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getDetailCount() - 1; lnCtr++) {
                            poController.PurchaseOrderReceiving().getPurchaseOrderReceivingSerial(poController.PurchaseOrderReceiving().Detail(lnCtr).getEntryNo());
                        }

                        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //get last retrieved Company and Supplier
//                            psCompanyId = poController.PurchaseOrderReceiving().Master().getCompanyId();
                            psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();

                            //Clear data
                            poController.PurchaseOrderReceiving().resetMaster();
                            poController.PurchaseOrderReceiving().resetOthers();
                            poController.PurchaseOrderReceiving().Detail().clear();
                            clearTextFields();

                            poController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                            poController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                            poController.PurchaseOrderReceiving().Master().setSupplierId(psSupplierId);
                            pnEditMode = EditMode.UNKNOWN;
                            showRetainedHighlight(false);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        //Retrieve data from purchase order to table main
                        if (mainSearchListener != null) {
                            JFXUtil.removeTextFieldListener(mainSearchListener, tfOrderNo);
                            mainSearchListener = null; // Clear reference to avoid memory leaks
                        }
                        poJSON = retrievePOReturn();
                        if ("error".equals((String) poJSON.get("result"))) {
                            if (!(boolean) poJSON.get("continue")) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        if (pnEditMode != EditMode.ADDNEW && pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                            showRetainedHighlight(false);
                        }
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.PurchaseOrderReceiving().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.PurchaseOrderReceiving().AddDetail();
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                //get last retrieved Company and Supplier
//                                psCompanyId = poController.PurchaseOrderReceiving().Master().getCompanyId();
                                psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();

                                // Confirmation Prompt
                                JSONObject loJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());

                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.PurchaseOrderReceiving().Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.PurchaseOrderReceiving().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                showRetainedHighlight(true);
                                // Print Transaction Prompt
                                lsIsSaved = false;
                                loJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
                                poController.PurchaseOrderReceiving().loadAttachments();
                                loadRecordMaster();
                                if ("success".equals(loJSON.get("result"))) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to print this transaction?")) {
                                        lsIsSaved = true;
                                        btnPrint.fire();
                                    } else {
                                        btnNew.fire();
                                    }
                                } else {
                                    btnNew.fire();
                                }
                            }
                        } else {
                            return;
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (lsButton.equals("btnPrint") || lsButton.equals("btnRetrieve")) { // || lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }

                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                        tfReceiveQuantity.requestFocus();
                    } else {
                        tfBrand.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {

                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);

        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {

                JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);

            }
        }
    }

    public void loadHighlightFromDetail() {
        for (int lnCtr = 0; lnCtr < poController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
            String lsHighlightbasis = poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo();
            if (!JFXUtil.isObjectEqualTo(poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity(), null, "")) {
                if (poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() > 0.0000) {
                    plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "1"));
                } else {
                    plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "0"));
                }
            }
        }
        for (Pair<String, String> pair : plOrderNoPartial) {
            if (!"".equals(pair.getKey()) && pair.getKey() != null) {
                JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);
            }
        }
        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, false);
    }

    public JSONObject retrievePOReturn() {
        poJSON = new JSONObject();
        String lsMessage = "";
        poJSON.put("result", "success");

        if (poController.PurchaseOrderReceiving().Master().getCompanyId().equals("")) {
            poJSON.put("result", "error");
            lsMessage += lsMessage.isEmpty() ? "Company" : " & Company";
        }

        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = poController.PurchaseOrderReceiving().getPurchaseOrderReturn(tfSupplier.getText());
            loadTableMain.reload();
        } else {
            poJSON.put("result", "error");
            poJSON.put("continue", false);
            poJSON.put("message", lsMessage + " cannot be empty.");
        }
        return poJSON;
    }
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {

                    case "taRemarks"://Remarks
                        poJSON = poController.PurchaseOrderReceiving().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfCost":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.parseDouble(lsValue) < 0.00) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Cost Amount");
                            return;
                        }

                        double ldblOldVal = poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue();
                        poJSON = poController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                            return;
                        }

                        try {
                            poJSON = poController.PurchaseOrderReceiving().computeFields();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce(ldblOldVal);
                                tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                                return;
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }

                        break;
                    case "tfReceiveQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() != null
                        && !"".equals(poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo())) {
                            if (poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().doubleValue() < Double.valueOf(lsValue)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Receive quantity cannot be greater than the order quantity.");
                                JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                                break;
                            }
                        }
                        poJSON = poController.PurchaseOrderReceiving().checkPurchaseOrderReceivingSerial(pnDetail + 1, Integer.valueOf(lsValue));
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        }

                        poJSON = poController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity((Integer.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        }

                        try {
                            poJSON = poController.PurchaseOrderReceiving().computeFields();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                                break;
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();

                });
            });

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.PurchaseOrderReceiving().Master().getSupplierId() != null && !"".equals(poController.PurchaseOrderReceiving().Master().getSupplierId())) {
                                    if (poController.PurchaseOrderReceiving().getDetailCount() > 1) {
                                        if (!pbKeyPressed) {
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                                poController.PurchaseOrderReceiving().removePORDetails();
                                                showRetainedHighlight(false);
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

                            poJSON = poController.PurchaseOrderReceiving().Master().setSupplierId("");
                        }
                        break;
                    case "tfTrucking":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setTruckingId("");
                        }
                        break;
                    case "tfAreaRemarks":
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setTermCode("");
                        }
                        break;
                    case "tfReferenceNo":
                        if (!lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setReferenceNo(lsValue);
                        } else {
                            poJSON = poController.PurchaseOrderReceiving().Master().setReferenceNo("");
                        }
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfReferenceNo.setText("");
                            break;
                        }
                        break;
                    case "tfDiscountRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PurchaseOrderReceiving().computeDiscount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.PurchaseOrderReceiving().Master().setDiscountRate((Double.valueOf(lsValue)));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        break;
                    case "tfDiscountAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PurchaseOrderReceiving().computeDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.PurchaseOrderReceiving().Master().setDiscount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        break;

                }

                loadRecordMaster();
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        if (details_data.size() <= 0) {
            return;
        }

        if (continueNext) {
            apDetail.requestFocus();

            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewTransDetails) : JFXUtil.moveToNextRow(tblViewTransDetails);
        }
        loadRecordDetail();
        tfReceiveQuantity.requestFocus();
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;
            TableView<?> currentTable = tblViewTransDetails;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfReceiveQuantity":
                            moveNext(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfReceiveQuantity":
                            moveNext(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case BACK_SPACE:
                    switch (lsID) {
                        case "tfOrderNo":
                            if (mainSearchListener != null) {
                                JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                                mainSearchListener = null; // Clear reference to avoid memory leaks
                                initDetailsGrid();
                                initMainGrid();
                                goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                            }
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSupplier":
                            if (poController.PurchaseOrderReceiving().Master().getCompanyId() == null
                                    || "".equals(poController.PurchaseOrderReceiving().Master().getCompanyId())) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Company Name is not set.");
                                return;
                            }

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.PurchaseOrderReceiving().getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                        poController.PurchaseOrderReceiving().removePORDetails();
                                        loadTableDetail.reload();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }

                            poJSON = poController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupplier.setText("");
                                psSupplierId = "";
                                break;
                            }
                            psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(e -> {
                                    if (!"".equals(poController.PurchaseOrderReceiving().Master().getCompanyId())) {
                                        poJSON = retrievePOReturn();
                                        if ("error".equals((String) poJSON.get("result"))) {
                                            if (!(boolean) poJSON.get("continue")) {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                            }
                                        }
                                    }
                                });
                                delay.play();
                            });
                            loadRecordMaster();
                            break;
                        case "tfTrucking":
                            poJSON = poController.PurchaseOrderReceiving().SearchTrucking(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTrucking.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfTerm":
                            poJSON = poController.PurchaseOrderReceiving().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTerm.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfOrderNo":
                            break;
                    }
                    break;
                case F4:
                    switch (lsID) {
                        case "tfBarcode":
                            poJSON = poController.PurchaseOrderReceiving().SearchBarcode(lsValue, true, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        case "tfDescription":
                            poJSON = poController.PurchaseOrderReceiving().SearchDescription(lsValue, true, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        case "tfBrand":
                            tfModel.requestFocus();
                        case "tfModel":
                            poJSON = poController.PurchaseOrderReceiving().SearchModel(lsValue, false, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfModel.setText("");
                                break;
                            }
                            loadTableDetail.reload();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(e -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount);
        JFXUtil.setFocusListener(txtDetail_Focus, tfOrderNo, tfModel, tfCost, tfReceiveQuantity);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail);
        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity);
        CustomCommonUtil.inputDecimalOnly(tfDiscountRate);
        JFXUtil.setCommaFormatter(tfDiscountRate, tfDiscountAmount, tfCost);
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null;
                LocalDate transactionDate = null;
                LocalDate referenceDate = null;
                LocalDate selectedDate = null;
                String lsServerDate = "";
                String lsTransDate = "";
                String lsRefDate = "";
                String lsSelectedDate = "";

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                selectedDate = ldtResult.selectedDate;

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getReferenceDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            referenceDate = LocalDate.parse(lsRefDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isBefore(referenceDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Receiving date cannot be before reference date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
                                if (oApp.getUserLevel() <= UserRight.ENCODER) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Change in Transaction Date Detected\n\n"
                                            + "If YES, please seek approval to proceed with the new selected date.\n"
                                            + "If NO, the previous transaction date will be retained.") == true) {
                                        poJSON = ShowDialogFX.getUserApproval(oApp);
                                        if (!"success".equals((String) poJSON.get("result"))) {
                                            pbSuccess = false;
                                        } else {
                                            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                                poJSON.put("result", "error");
                                                poJSON.put("message", "User is not an authorized approving officer.");
                                                pbSuccess = false;
                                            }
                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                }
                            }

                            if (pbSuccess) {
                                poController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpReferenceDate":
                        if (poController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getReferenceDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Reference date cannot be later than the receiving date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poController.PurchaseOrderReceiving().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    default:

                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEntered = false;
                        // Setting data to table detail
                        JFXUtil.disableAllHighlight(tblViewTransDetails, highlightedRowsDetail);
                        if (lbresetpredicate) {
                            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                            filteredDataDetail.setPredicate(null);
                            lbresetpredicate = false;
                            JFXUtil.removeTextFieldListener(detailSearchListener, tfOrderNo);

                            mainSearchListener = null;
                            filteredData.setPredicate(null);
                            initMainGrid();
                            initDetailsGrid();
                            Platform.runLater(() -> {
                                tfOrderNo.setText("");
                            });
                            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                        }

                        int lnCtr;
                        details_data.clear();
                        plOrderNoPartial.clear();
                        try {

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                String lsBrandId = "";
                                lnCtr = poController.PurchaseOrderReceiving().getDetailCount() - 1;
                                while (lnCtr >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() == null || "".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId())) {
                                        if (poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId() != null
                                                || !"".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId())) {
                                            lsBrandId = poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId();
                                        }
                                        //remove por detail
                                        poController.PurchaseOrderReceiving().Detail().remove(lnCtr);
                                        //remove por serial
                                        poController.PurchaseOrderReceiving().removePurchaseOrderReceivingSerial(lnCtr + 1);
                                    }
                                    lnCtr--;
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId() != null && !"".equals(poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId())) {
                                        poController.PurchaseOrderReceiving().AddDetail();
                                    }
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) < 0) {
                                    poController.PurchaseOrderReceiving().AddDetail();
                                }

                                //Set brand Id to last row
                                if (!lsBrandId.isEmpty()) {
                                    poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).setBrandId(lsBrandId);
                                }
                                //Check for PO Serial Update Entry No TODO
                            }

                            double lnTotal = 0.00;
                            for (lnCtr = 0; lnCtr < poController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                                if (poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() == null
                                        || "".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo())) {
                                    continue;
                                }
                                lnTotal = poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue();

                                if ((!poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                    JFXUtil.highlightByKey(tblViewTransDetails, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                                }

                                plOrderNoPartial.add(new Pair<>(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo(), String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue())));

                                String lsBrand = "";
                                if (poController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription() != null) {
                                    lsBrand = poController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription();
                                }
                                details_data.add(
                                        new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().intValue()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                        ));
                            }

                            for (Pair<String, String> pair : plOrderNoPartial) {
                                if (!"".equals(pair.getKey()) && pair.getKey() != null) {

                                    JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);
                                }
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewTransDetails.getSelectionModel().select(0);
                                    tblViewTransDetails.getFocusModel().focus(0);
                                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewTransDetails.getSelectionModel().select(pnDetail);
                                tblViewTransDetails.getFocusModel().focus(pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        } catch (GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        main_data.clear();
                        String lsMainDate = "";
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Define the format

                        try {
                            Thread.sleep(100);

                            if (!poController.PurchaseOrderReceiving().Master().getTransactionDate().equals("")) {
                                Object loDate = poController.PurchaseOrderReceiving().Master().getTransactionDate();
                                if (loDate == null) {
                                    lsMainDate = LocalDate.now().format(formatter); // Convert to String

                                } else if (loDate instanceof Timestamp) {
                                    Timestamp timestamp = (Timestamp) loDate;
                                    LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();

                                    lsMainDate = localDate.format(formatter);
                                } else if (loDate instanceof Date) {
                                    Date sqlDate = (Date) loDate;
                                    LocalDate localDate = sqlDate.toLocalDate();

                                    lsMainDate = localDate.format(formatter);
                                } else {
                                }
                            }
                        } catch (Exception e) {

                        }
                        if (poController.PurchaseOrderReceiving().getPurchaseOrderReturnCount() > 0) {
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getPurchaseOrderReturnCount() - 1; lnCtr++) {
                                try {
                                    main_data.add(new ModelDeliveryAcceptance_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReturnList(lnCtr).Supplier().getCompanyName()),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReturnList(lnCtr).getTransactionDate()),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReturnList(lnCtr).getTransactionNo())
                                    ));
                                } catch (Exception e) {

                                }

                            }
                            if (pnMain < 0 || pnMain
                                    >= main_data.size()) {
                                if (!main_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewMainList.getSelectionModel().select(0);
                                    tblViewMainList.getFocusModel().focus(0);
                                    pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();

                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewMainList.getSelectionModel().select(pnMain);
                                tblViewMainList.getFocusModel().focus(pnMain);
                            }

                        }
                        JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                    });
                });
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        autoSearch(tfOrderNo);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetails.comparatorProperty());
        tblViewTransDetails.setItems(sortedData);
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);

    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail);

        loadRecordMaster();
        loadTableDetail.reload();
        loadTableMain.reload();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poController.PurchaseOrderReceiving().Master().Industry().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass()
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.PurchaseOrderReceiving().getDetailCount() - 1) {
                return;
            }
            boolean lbFields = (poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo().equals("") || poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() == null) && poController.PurchaseOrderReceiving().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;
            poJSON = poController.PurchaseOrderReceiving().checkExistingSerialId(pnDetail + 1);
            if ("error".equals((String) poJSON.get("result"))) {
                lbFields = false;
            }
            JFXUtil.setDisabled(!lbFields, tfBrand, tfModel);
            JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfCost);

            if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                poController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBrandId());
            }
            tfBarcode.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Brand().getDescription());
            tfModelVariant.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Variant().getDescription());

            tfModel.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
//            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce()));
            tfOrderQuantity.setText(String.valueOf(poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().intValue()));
            tfReceiveQuantity.setText(String.valueOf(poController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue()));

            JFXUtil.updateCaretPositions(apDetail);

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass()
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.UPDATE;
        JFXUtil.setDisabled(lbDisable, tfSupplier);
        try {
            JFXUtil.setStatusValue(lblStatus, PurchaseOrderReceivingStatus.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poController.PurchaseOrderReceiving().Master().getTransactionStatus());
            boolean lbPrintStat = pnEditMode == EditMode.READY && poController.PurchaseOrderReceiving().Master().getTransactionStatus() != PurchaseOrderReceivingStatus.VOID;

            btnPrint.setVisible(lbPrintStat);
            btnPrint.setManaged(lbPrintStat);

            if (poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue() > 0.00) {
                poController.PurchaseOrderReceiving().computeDiscount(poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
            } else {
                if (poController.PurchaseOrderReceiving().Master().getDiscount().doubleValue() > 0.00) {
                    poController.PurchaseOrderReceiving().computeDiscountRate(poController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
                }
            }
            poController.PurchaseOrderReceiving().computeFields();

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.PurchaseOrderReceiving().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            //ReferenceDate
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.PurchaseOrderReceiving().Master().getReferenceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            tfTransactionNo.setText(poController.PurchaseOrderReceiving().Master().getTransactionNo());

            tfSupplier.setText(poController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfTrucking.setText(poController.PurchaseOrderReceiving().Master().Trucking().getCompanyName());
            tfTerm.setText(poController.PurchaseOrderReceiving().Master().Term().getDescription());
            tfReferenceNo.setText(poController.PurchaseOrderReceiving().Master().getReferenceNo());
            taRemarks.setText(poController.PurchaseOrderReceiving().Master().getRemarks());

            Platform.runLater(() -> {
                double lnValue = poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue();
                if (!Double.isNaN(lnValue)) {
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getDiscountRate(), false));
                } else {
                    tfDiscountRate.setText("0.00");
                }
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getDiscount(), true));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getTransactionTotal(), true));

            JFXUtil.updateCaretPositions(apMaster);

        } catch (SQLException ex) {
            Logger.getLogger(getClass()
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);

        } catch (GuanzonException ex) {
            Logger.getLogger(getClass()
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        pnDetail = JFXUtil.moveToNextRow(tblViewTransDetails);
                        break;
                    case UP:
                        pnDetail = JFXUtil.moveToPreviousRow(tblViewTransDetails);
                        break;
                    default:
                        break;
                }
                loadRecordDetail();
                tfOrderNo.setText("");
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewTransDetails.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                            tfReceiveQuantity.requestFocus();
                        } else {
                            tfBrand.requestFocus();
                        }
                    }
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    tfOrderNo.setText("");
                    loadTableDetailFromMain();
                    initButton(pnEditMode);
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDeliveryAcceptance_Main) item).getIndex04(), highlightedRowsMain);
        JFXUtil.applyRowHighlighting(tblViewTransDetails, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblViewMainList);  // need to use computed-size as min-width on particular column to work
    }

    private void goToPageBasedOnSelectedRow(String pnRowMain) {
        if (mainSearchListener != null) {
            JFXUtil.removeTextFieldListener(mainSearchListener, tfOrderNo);
            mainSearchListener = null;
        }
        if (detailSearchListener != null) {
            JFXUtil.removeTextFieldListener(detailSearchListener, tfOrderNo);
            detailSearchListener = null;
        }
        filteredDataDetail.setPredicate(null);
        filteredData.setPredicate(null);
        lbresetpredicate = false;
        int realIndex = Integer.parseInt(pnRowMain);

        if (realIndex == -1) {
            return; // Not found
        }
        int targetPage = realIndex / ROWS_PER_PAGE;
        int indexInPage = realIndex % ROWS_PER_PAGE;

        initMainGrid();
        initDetailsGrid();
        int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(targetPage);
        JFXUtil.changeTableView(indexInPage, ROWS_PER_PAGE, tblViewMainList, main_data.size(), filteredData);
        Platform.runLater(() -> {
            if (lbresetpredicate) {
                tblViewMainList.scrollTo(indexInPage);
                lbresetpredicate = false;
            }
        });
    }

    public void loadTableDetailFromMain() {
        try {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poJSON = new JSONObject();
                ModelDeliveryAcceptance_Main selected = (ModelDeliveryAcceptance_Main) tblViewMainList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    pnMain = pnRowMain;

                    poJSON = poController.PurchaseOrderReceiving().addPurchaseOrderReturnToPORDetail(selected.getIndex04());
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                }
                loadTableDetail.reload();
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "Data can only be viewed when in ADD or UPDATE mode.");

            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass()
                    .getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButton(int fnValue) {
        // Manage visibility and managed state of other buttons
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnPrint, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow, dpTransactionDate, dpReferenceDate, tfTrucking, taRemarks, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount,
                apDetail);

        switch (poController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.POSTED:
            case PurchaseOrderReceivingStatus.PAID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnPrint);
                break;
        }

    }

    private void autoSearch(TextField txtField) {
        detailSearchListener = (observable, oldValue, newValue) -> {
            int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
            pgPagination.setPageCount(totalPage);
            filteredDataDetail.setPredicate(orders -> {
                lbresetpredicate = true;
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                if (mainSearchListener != null) {
                    JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                    mainSearchListener = null; // Clear reference to avoid memory leaks
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex02().toLowerCase().contains(lowerCaseFilter);
            });
            // If no results and autoSearchMain is enabled, remove listener and trigger autoSearchMain
            if (filteredDataDetail.isEmpty()) {
                if (main_data.size() > 0) {
                    JFXUtil.removeTextFieldListener(detailSearchListener, txtField);
                    filteredData = new FilteredList<>(main_data, b -> true);
                    autoSearchMain(txtField); // Trigger autoSearchMain if no results
                    tblViewMainList.setItems(filteredData);

                    String currentText = txtField.getText();
                    txtField.setText(currentText + " "); // Add a space
                    txtField.setText(currentText);       // Set back to original
                }
            } else {
                if (filteredDataDetail.size() == details_data.size()) {
                    tblViewTransDetails.getSelectionModel().select(pnDetail);
                    tblViewTransDetails.getFocusModel().focus(pnDetail);
                }
            }
        };
        txtField.textProperty().addListener(detailSearchListener);
    }

    private void autoSearchMain(TextField txtField) {
        mainSearchListener = (observable, oldValue, newValue) -> {
            filteredData.setPredicate(orders -> {
                lbresetpredicate = true;
                if (newValue == null || newValue.isEmpty()) {
                    if (mainSearchListener != null) {
                        JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                        mainSearchListener = null; // Clear reference to avoid memory leaks
                        initDetailsGrid();
                    }
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex04().toLowerCase().contains(lowerCaseFilter);
            });
            pgPagination.setPageCount(1);
        };
        txtField.textProperty().addListener(mainSearchListener);
    }
}
