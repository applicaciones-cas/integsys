/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
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
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Pair;
import org.json.simple.parser.ParseException;
import java.text.SimpleDateFormat;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DeliveryAcceptance_EntryLPController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = "Purchase Order Receiving Entry LP";
    static PurchaseOrderReceivingControllers poPurchaseReceivingController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean lbresetpredicate = false;
    boolean pbEntered = false;
    boolean pbKeyPressed = false;

    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();

    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;

    @FXML
    private HBox hbButtons, hboxid;

    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnPrint, btnHistory, btnRetrieve, btnClose;

    @FXML
    private Label lblStatus, lblSource;

    @FXML
    private TextField tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate,
            tfDiscountAmount, tfTotal, tfOrderNo, tfBarcode, tfSupersede, tfDescription, tfBrand, tfModel, tfColor, tfInventoryType,
            tfMeasure, tfCost, tfOrderQuantity, tfReceiveQuantity;

    @FXML
    private TextArea taRemarks;

    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpExpiryDate;

    @FXML
    private TableView tblViewOrderDetails, tblViewPuchaseOrder;

    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail,
            tblReceiveQuantityDetail, tblTotalDetail, tblRowNo, tblSupplier, tblDate, tblReferenceNo;

    @FXML
    private Pagination pgPagination;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        poPurchaseReceivingController = new PurchaseOrderReceivingControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        loadRecordMaster();
        loadTableDetail();
        pgPagination.setPageCount(1);
        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
        initButton(pnEditMode);
        Platform.runLater(() -> {
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCategoryId(psCategoryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setWithUI(true);
            loadRecordSearch();

            btnNew.fire();
        });

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

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poPurchaseReceivingController.PurchaseOrderReceiving().setTransactionStatus(PurchaseOrderReceivingStatus.RETURNED + "" + PurchaseOrderReceivingStatus.OPEN);
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        showRetainedHighlight(false);
                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
//                        psCompanyId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId();
                        psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                        break;
                    case "btnPrint":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().printRecord(() -> {
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
                    case "btnNew":
                        //Clear data
                        poPurchaseReceivingController.PurchaseOrderReceiving().resetMaster();
                        poPurchaseReceivingController.PurchaseOrderReceiving().resetOthers();
                        poPurchaseReceivingController.PurchaseOrderReceiving().Detail().clear();
                        clearTextFields();

                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poPurchaseReceivingController.PurchaseOrderReceiving().initFields();

                        if (!psCompanyId.isEmpty()) {
                            poPurchaseReceivingController.PurchaseOrderReceiving().SearchCompany(psCompanyId, true);
                        }
                        if (!psSupplierId.isEmpty()) {
                            poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupplier(psSupplierId, true);
                        }
                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        showRetainedHighlight(false);
                        break;
                    case "btnUpdate":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField != null)) {
                            if (lastFocusedTextField instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField;
                                if (Arrays.asList("tfSupplier", "tfTrucking", "tfTerm", "tfBarcode",
                                        "tfDescription", "tfSupersede").contains(tf.getId())) {

                                    if (lastFocusedTextField == previousSearchedTextField) {

                                        break;
                                    }
                                    previousSearchedTextField = lastFocusedTextField;
                                    // Create a simulated KeyEvent for F3 key press
                                    KeyEvent keyEvent = new KeyEvent(
                                            KeyEvent.KEY_PRESSED,
                                            "",
                                            "",
                                            KeyCode.F3,
                                            false, false, false, false
                                    );
                                    tf.fireEvent(keyEvent);
                                } else {
                                    ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                                }
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                            }
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                        }
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //get last retrieved Company and Supplier
//                            psCompanyId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId();
                            psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();

                            //Clear data
                            poPurchaseReceivingController.PurchaseOrderReceiving().resetMaster();
                            poPurchaseReceivingController.PurchaseOrderReceiving().resetOthers();
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail().clear();
                            clearTextFields();

                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSupplierId(psSupplierId);
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
                            tfOrderNo.textProperty().removeListener(mainSearchListener);
                            mainSearchListener = null; // Clear reference to avoid memory leaks
                        }
                        poJSON = retrievePO();
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
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                                loadTableDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                //get last retrieved Company and Supplier
//                                psCompanyId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId();
                                psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();

                                // Confirmation Prompt
                                JSONObject loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().ConfirmTransaction("");
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
                                loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
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
                    loadTableDetail();
                }

                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                        tfReceiveQuantity.requestFocus();
                    } else {
                        tfBarcode.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (ParseException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
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
        disableAllHighlightByKey(tblViewPuchaseOrder, highlightedRowsMain);

        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {

                highlightByKey(tblViewPuchaseOrder, pair.getKey(), "#A7C7E7", highlightedRowsMain);

            }
        }
    }

    public JSONObject retrievePO() {
        poJSON = new JSONObject();
        String lsMessage = "";
        poJSON.put("result", "success");

        if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getIndustryId().equals("")) {
            poJSON.put("result", "error");
            lsMessage = "Industry";
        }
        if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId().equals("")) {
            poJSON.put("result", "error");
            lsMessage += lsMessage.isEmpty() ? "Company" : " & Company";
        }
//        if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId().equals("")) {
//            poJSON.put("result", "error");
//            lsMessage += lsMessage.isEmpty() ? "Supplier" : " & Supplier";
//        }

        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().getApprovedPurchaseOrder();
            loadTableMain();
        } else {
            poJSON.put("result", "error");
            poJSON.put("continue", false);
            poJSON.put("message", lsMessage + " cannot be empty.");
        }

        return poJSON;
    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtField.getId());
        String lsValue = txtField.getText();

        lastFocusedTextField = txtField;
        previousSearchedTextField = null;

        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            /*Lost Focus*/
            lsValue = lsValue.trim();
            switch (lsID) {

                case "taRemarks"://Remarks
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setRemarks(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    break;
            }
            loadRecordMaster();
        } else {
            txtField.selectAll();
        }
    };

    // Method to handle focus change and track the last focused TextField
    final ChangeListener<? super Boolean> txtDetail_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfDescription":
                case "tfBarcode":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setStockId("");
                    }

                    break;
                case "tfSupersede":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setReplaceId("");
                    }

                    break;
                case "tfCost":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    if (Double.parseDouble(lsValue.replace(",", "")) < 0.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid Cost Amount");
                        return;
                    }

                    double ldblOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue();
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                        return;
                    }

                    try {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce(ldblOldVal);
                            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                            return;
                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                    break;
                case "tfReceiveQuantity":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() != null
                            && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo())) {
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().doubleValue() < Double.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Receive quantity cannot be greater than the order quantity.");
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity(0);
                            tfReceiveQuantity.requestFocus();
                            break;
                        }
                    }

                    double lnOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }

                    try {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity(lnOldVal);
                            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity()));
                            return;
                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                    if (pbEntered) {
                        moveNext();
                        pbEntered = false;
                    }
                    break;
            }
            Platform.runLater(() -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                delay.setOnFinished(event -> {
                    loadTableDetail();
                });
                delay.play();
            });
        }
    };

    final ChangeListener<? super Boolean> txtMaster_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;

        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSupplier":
                    if (lsValue.isEmpty()) {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId() != null && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId())) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() > 1) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                            poPurchaseReceivingController.PurchaseOrderReceiving().removePORDetails();
                                            showRetainedHighlight(false);
                                            loadTableDetail();
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

                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSupplierId("");
                    }
                    break;
                case "tfTrucking":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTruckingId("");
                    }
                    break;
                case "tfAreaRemarks":
                    break;
                case "tfTerm":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTermCode("");
                    }
                    break;
                case "tfReferenceNo":
                    if (!lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceNo(lsValue);
                    } else {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceNo("");
                    }
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfReferenceNo.setText("");
                        break;
                    }
                    break;
                case "tfDiscountRate":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscountRate((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
                case "tfDiscountAmount":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscount(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;

            }

            loadRecordMaster();
        }

    };

    public void moveNext() {
        double lnReceiveQty = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
        apDetail.requestFocus();
        double lnNewvalue = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
        if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0.00
                && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
            tfReceiveQuantity.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewOrderDetails);
            loadRecordDetail();
            tfOrderNo.setText("");
            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                tfReceiveQuantity.requestFocus();
            } else {
                tfBarcode.requestFocus();
            }
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;
            TableView<?> currentTable = tblViewOrderDetails;
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
                        case "tfBarcode":
                        case "tfReceiveQuantity":
                            double lnReceiveQty = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
                            apDetail.requestFocus();
                            double lnNewvalue = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
                            if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
                                tfReceiveQuantity.requestFocus();
                            } else {
                                pnDetail = moveToPreviousRow(currentTable, focusedCell);
                                loadRecordDetail();
                                tfOrderNo.setText("");
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                                    tfReceiveQuantity.requestFocus();
                                } else {
                                    tfBarcode.requestFocus();
                                }
                                event.consume();
                            }
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfReceiveQuantity":
                            moveNext();
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
                                txtField.textProperty().removeListener(mainSearchListener);
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
//                        case "tfCompany":
//                            if (pnEditMode == EditMode.ADDNEW) {
//                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId() != null
//                                        && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId())) {
//                                    if (poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() > 1) {
//                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
//                                                "Are you sure you want to change the company name? Please note that doing so will delete all purchase order receiving details. Do you wish to proceed?") == true) {
//                                            poPurchaseReceivingController.PurchaseOrderReceiving().removePORDetails();
//                                            loadTableDetail();
//                                        } else {
//                                            return;
//                                        }
//                                    }
//                                }
//                            }
//
//                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchCompany(lsValue, false);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//
//                                psCompanyId = "";
//                                break;
//                            }
//                            psCompanyId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId();
//                            Platform.runLater(() -> {
//                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
//                                delay.setOnFinished(e -> {
//                                    if (!"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId())) {
//                                        poJSON = retrievePO();
//                                        if ("error".equals((String) poJSON.get("result"))) {
//                                            if (!(boolean) poJSON.get("continue")) {
//                                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                            }
//                                        }
//                                    }
//                                });
//                                delay.play();
//                            });
//                            loadRecordMaster();
//                            break;

                        case "tfSupplier":
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId() == null
                                    || "".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId())) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Company Name is not set.");
                                return;
                            }

                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                        poPurchaseReceivingController.PurchaseOrderReceiving().removePORDetails();
                                        loadTableDetail();
                                    } else {
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }

                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupplier.setText("");
                                psSupplierId = "";
                                break;
                            }
                            psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(e -> {
                                    if (!"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId())) {
                                        poJSON = retrievePO();
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
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchTrucking(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTrucking.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfTerm":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTerm.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfOrderNo":

                            break;
                        case "tfBarcode":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchBarcode(lsValue, true, pnDetail, true);
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
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;

                        case "tfDescription":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchDescription(lsValue, true, pnDetail, true);
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
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfSupersede":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupersede(lsValue, true, pnDetail, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupersede.setText("");
                                break;
                            }
                            loadRecordDetail();
                            break;
                    }
                    break;

                case F4:
                    switch (lsID) {
                        case "tfBarcode":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchBarcode(lsValue, true, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfDescription":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchDescription(lsValue, false, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfSupersede":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupersede(lsValue, true, pnDetail, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupersede.setText("");
                                break;
                            }
                            loadRecordDetail();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initTextFields() {

        tfSupplier.focusedProperty().addListener(txtMaster_Focus);
        tfTrucking.focusedProperty().addListener(txtMaster_Focus);
        taRemarks.focusedProperty().addListener(txtArea_Focus);
        tfReferenceNo.focusedProperty().addListener(txtMaster_Focus);
        tfTerm.focusedProperty().addListener(txtMaster_Focus);
        tfDiscountRate.focusedProperty().addListener(txtMaster_Focus);
        tfDiscountAmount.focusedProperty().addListener(txtMaster_Focus);

        tfBarcode.focusedProperty().addListener(txtDetail_Focus);
        tfSupersede.focusedProperty().addListener(txtDetail_Focus);
        tfDescription.focusedProperty().addListener(txtDetail_Focus);
        tfCost.focusedProperty().addListener(txtDetail_Focus);
        tfReceiveQuantity.focusedProperty().addListener(txtDetail_Focus);

        TextField[] textFields = {
            tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate,
            tfDiscountAmount, tfTotal, tfOrderNo, tfBarcode, tfSupersede, tfDescription,
            tfBrand, tfModel, tfColor, tfInventoryType, tfMeasure, tfCost, tfOrderQuantity,
            tfReceiveQuantity
        };

        for (TextField textField : textFields) {
            textField.setOnKeyPressed(this::txtField_KeyPressed);
        }
        CustomCommonUtil.inputDecimalOnly(tfDiscountRate, tfDiscountAmount, tfCost, tfReceiveQuantity);
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

                lastFocusedTextField = datePicker;
                previousSearchedTextField = null;

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
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
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

                            if (pbSuccess && ((poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
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
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
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
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpExpiryDate":
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "The selected date cannot be earlier than the current date.");
                                pbSuccess = false;
                            }
                            if (pbSuccess) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setExpiryDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetail();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:

                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ChangeListener<Boolean> datepicker_Focus = (observable, oldValue, newValue) -> {
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        try {
            if (!newValue) { // Lost focus
                DatePicker datePicker = (DatePicker) ((javafx.beans.property.ReadOnlyBooleanProperty) observable).getBean();
                String lsID = datePicker.getId();
                String inputText = datePicker.getEditor().getText();
                LocalDate currentDate = LocalDate.now();
                LocalDate selectedDate = null;

                lastFocusedTextField = datePicker;
                previousSearchedTextField = null;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (inputText != null && !inputText.trim().isEmpty()) {
                    try {
                        LocalDate parsedDate = LocalDate.parse(inputText, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        datePicker.setValue(parsedDate);
                        datePicker.getEditor().setText(formatter.format(parsedDate));
                        inputText = datePicker.getEditor().getText();
                    } catch (DateTimeParseException ignored) {
                    }
                }

                // Check if the user typed something in the text field
                if (inputText != null && !inputText.trim().isEmpty()) {
                    try {
                        selectedDate = LocalDate.parse(inputText, formatter);
                        datePicker.setValue(selectedDate); // Update the DatePicker with the valid date
                    } catch (Exception ex) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid date format. Please use MM/dd/yyyy format.");
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        loadRecordMaster();
                        // datePicker.requestFocus();
                        return;
                    }
                } else {
                    selectedDate = datePicker.getValue(); // Fallback to selected date if nothing was typed
                }

                String formattedDate = selectedDate.toString();

                switch (lsID) {
                    case "dpTransactionDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isAfter(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Future dates are not allowed.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(formattedDate, "yyyy-MM-dd")));
                        }
                        break;
                    case "dpReferenceDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isAfter(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Future dates are not allowed.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceDate(SQLUtil.toDate(formattedDate, "yyyy-MM-dd"));
                        }
                        break;
                    case "dpExpiryDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isBefore(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "The selected date cannot be earlier than the current date.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setExpiryDate(SQLUtil.toDate(formattedDate, "yyyy-MM-dd"));
                        }
                        break;
                    default:

                        break;
                }
                datePicker.getEditor().setText(formattedDate);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    // datePicker.requestFocus();
                }
                Platform.runLater(() -> {
                    if (lsID.equals("dpExpiryDate")) {
                        loadRecordDetail();
                    } else {
                        loadRecordMaster();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private void setDatePickerFormat(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });
    }

    private void addKeyEventFilter(DatePicker datePicker) {
        datePicker.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Node source = (Node) event.getSource();
                source.fireEvent(new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        false,
                        false,
                        false,
                        false
                ));
                event.consume();
            }
        });
    }

    public void initDatePickers() {

        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpExpiryDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate, dpExpiryDate);
    }

    public void initDetailsGrid() {

        tblRowNoDetail.setStyle("-fx-alignment: CENTER;");
        tblOrderNoDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblBarcodeDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblDescriptionDetail.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblCostDetail.setStyle("-fx-alignment: CENTER-RIGHT;-fx-padding: 0 5 0 5;");
        tblOrderQuantityDetail.setStyle("-fx-alignment: CENTER;");
        tblReceiveQuantityDetail.setStyle("-fx-alignment: CENTER;");
        tblTotalDetail.setStyle("-fx-alignment: CENTER-RIGHT;-fx-padding: 0 5 0 5;");

        tblRowNoDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblOrderNoDetail.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblBarcodeDetail.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblDescriptionDetail.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblCostDetail.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblOrderQuantityDetail.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblReceiveQuantityDetail.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblTotalDetail.setCellValueFactory(new PropertyValueFactory<>("index08"));

        tblViewOrderDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewOrderDetails.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        autoSearch(tfOrderNo);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewOrderDetails.comparatorProperty());
        tblViewOrderDetails.setItems(sortedData);
    }

    public void initMainGrid() {
        tblRowNo.setStyle("-fx-alignment: CENTER;");
        tblSupplier.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        tblDate.setStyle("-fx-alignment: CENTER;");
        tblReferenceNo.setStyle("-fx-alignment: CENTER;");

        tblRowNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblSupplier.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblDate.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblReferenceNo.setCellValueFactory(new PropertyValueFactory<>("index04"));

        tblViewPuchaseOrder.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewPuchaseOrder.lookup("TableHeaderRow");
            try {
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
            } catch (Exception e) {
            }
        });

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewPuchaseOrder.setItems(filteredData);

    }

    public void clearTextFields() {
        previousSearchedTextField = null;
        lastFocusedTextField = null;

        dpTransactionDate.setValue(null);
        dpReferenceDate.setValue(null);
        dpExpiryDate.setValue(null);

        tfTransactionNo.clear();

        tfSupplier.clear();
        tfTrucking.clear();
        taRemarks.clear();
        tfReferenceNo.clear();
        tfTerm.clear();
        tfDiscountRate.clear();
        tfDiscountAmount.clear();
        tfTotal.clear();
        tfOrderNo.clear();
        tfBarcode.clear();
        tfSupersede.clear();
        tfDescription.clear();
        tfBrand.clear();
        tfModel.clear();
        tfColor.clear();
        tfInventoryType.clear();
        tfMeasure.clear();
        tfCost.clear();
        tfOrderQuantity.clear();
        tfReceiveQuantity.clear();

        loadRecordMaster();
        loadTableDetail();
        loadTableMain();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poPurchaseReceivingController.PurchaseOrderReceiving().Master().Industry().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) {
                return;
            }
            boolean lbFields = (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo().equals("") || poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() == null) && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;
            tfBarcode.setDisable(!lbFields);
            tfDescription.setDisable(!lbFields);
            if (lbFields) {
                while (tfBarcode.getStyleClass().contains("DisabledTextField") || tfDescription.getStyleClass().contains("DisabledTextField")) {
                    tfBarcode.getStyleClass().remove("DisabledTextField");
                    tfDescription.getStyleClass().remove("DisabledTextField");
                }
            } else {
                tfBarcode.getStyleClass().add("DisabledTextField");
                tfDescription.getStyleClass().add("DisabledTextField");
            }

            if (oApp.getUserLevel() <= UserRight.ENCODER) {
                tfCost.getStyleClass().add("DisabledTextField");
                tfCost.setDisable(true);
            } else {
                while (tfCost.getStyleClass().contains("DisabledTextField")) {
                    tfCost.getStyleClass().remove("DisabledTextField");
                }
                tfCost.setDisable(false);
            }

            // Expiry Date
            String lsExpiryDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getExpiryDate());
            dpExpiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsExpiryDate, "yyyy-MM-dd"));

            tfBarcode.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Supersede().getBarCode());
            tfBrand.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
//            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce()));
            tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty()));
            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity()));

            updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.UPDATE;
        if (lbDisable) {

            tfSupplier.getStyleClass().add("DisabledTextField");
        } else {
            while (tfSupplier.getStyleClass().contains("DisabledTextField")) {

                tfSupplier.getStyleClass().remove("DisabledTextField");
            }
        }

        tfSupplier.setDisable(lbDisable);

//        boolean lbIsReprint = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getPrint().equals("1") ? true : false;
//        if (lbIsReprint) {
//            btnPrint.setText("Reprint");
//        } else {
//            btnPrint.setText("Print");
//        }
        try {

            Platform.runLater(() -> {
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus();
                String lsStat = "UNKNOWN";
                switch (lsActive) {
//                case PurchaseOrderReceivingStatus.APPROVED:
//                    lblStatus.setText("APPROVED");
//                    break;
                    case PurchaseOrderReceivingStatus.POSTED:
                        lsStat = "POSTED";
                        break;
                    case PurchaseOrderReceivingStatus.PAID:
                        lsStat = "PAID";
                        break;
                    case PurchaseOrderReceivingStatus.CONFIRMED:
                        lsStat = "CONFIRMED";
                        break;
                    case PurchaseOrderReceivingStatus.OPEN:
                        lsStat = "OPEN";
                        break;
                    case PurchaseOrderReceivingStatus.RETURNED:
                        lsStat = "RETURNED";
                        break;
                    case PurchaseOrderReceivingStatus.VOID:
                        lsStat = "VOIDED";
                        lbPrintStat = false;
                        break;
                    case PurchaseOrderReceivingStatus.CANCELLED:
                        lsStat = "CANCELLED";
                        break;
                    default:
                        lsStat = "UNKNOWN";
                        break;

                }
                lblStatus.setText(lsStat);
                btnPrint.setVisible(lbPrintStat);
                btnPrint.setManaged(lbPrintStat);
            });

            if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue() > 0.00) {
                poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
            } else {
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue() > 0.00) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
                }
            }
            poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            //ReferenceDate
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            tfTransactionNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());

            tfSupplier.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfTrucking.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Trucking().getCompanyName());
            tfTerm.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Term().getDescription());
            tfReferenceNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceNo());
            taRemarks.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getRemarks());

            Platform.runLater(() -> {
                double lnValue = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue();
                if (!Double.isNaN(lnValue)) {
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate(), false));
                } else {
                    tfDiscountRate.setText("0.00");
                }
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount(), true));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionTotal(), true));

            updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void updateCaretPositions(AnchorPane anchorPane) {
        List<TextField> textFields = getAllTextFields(anchorPane);
        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                Pos alignment = textField.getAlignment();
                if (alignment == Pos.CENTER_RIGHT || alignment == Pos.BASELINE_RIGHT
                        || alignment == Pos.TOP_RIGHT || alignment == Pos.BOTTOM_RIGHT) {
                    textField.positionCaret(0); // Caret at start
                } else {
                    if (textField.isFocused()) {
                        textField.positionCaret(text.length()); // Caret at end if focused
                    } else {
                        textField.positionCaret(0); // Caret at start if not focused
                    }
                }
            }
        }
    }

    private List<TextField> getAllTextFields(Parent parent) {
        List<TextField> textFields = new ArrayList<>();

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                textFields.add((TextField) node);
            } else if (node instanceof DatePicker) {
                // Try to find the internal TextField of DatePicker
                Node datePickerEditor = ((DatePicker) node).lookup(".text-field");
                if (datePickerEditor instanceof TextField) {
                    textFields.add((TextField) datePickerEditor);
                }
            } else if (node instanceof Parent) {
                textFields.addAll(getAllTextFields((Parent) node));
            }
        }
        return textFields;
    }

    private int moveToNextRow(TableView table, TablePosition focusedCell) {
        int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
        table.getSelectionModel().select(nextRow);
        return nextRow;
    }

    private int moveToPreviousRow(TableView table, TablePosition focusedCell) {
        int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
        table.getSelectionModel().select(previousRow);
        return previousRow;
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        pnDetail = moveToNextRow(currentTable, focusedCell);
                        break;
                    case UP:
                        pnDetail = moveToPreviousRow(currentTable, focusedCell);
                        break;

                    default:
                        break;
                }
                loadRecordDetail();
                tfOrderNo.setText("");
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                    tfReceiveQuantity.requestFocus();
                } else {
                    tfBrand.requestFocus();
                }
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewOrderDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewOrderDetails.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                            tfReceiveQuantity.requestFocus();
                        } else {
                            tfBarcode.requestFocus();
                        }
                    }
                }
            }
        });

        tblViewPuchaseOrder.setOnMouseClicked(event -> {
            pnMain = tblViewPuchaseOrder.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    tfOrderNo.setText("");
                    loadTableDetailFromMain();
                    initButton(pnEditMode);
                }
            }
        });

        tblViewPuchaseOrder.setRowFactory(tv -> new TableRow<ModelDeliveryAcceptance_Main>() {
            @Override
            protected void updateItem(ModelDeliveryAcceptance_Main item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle(""); // Reset for empty rows
                } else {
                    String key = item.getIndex04(); // defines the ReferenceNo
                    if (highlightedRowsMain.containsKey(key)) {
                        List<String> colors = highlightedRowsMain.get(key);
                        if (!colors.isEmpty()) {
                            setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";"); // Apply latest color
                        }
                    } else {
                        setStyle(""); // Default style
                    }
                }
            }
        });
        tblViewOrderDetails.setRowFactory(tv -> new TableRow<ModelDeliveryAcceptance_Detail>() {
            @Override
            protected void updateItem(ModelDeliveryAcceptance_Detail item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle(""); // Reset for empty rows
                } else {
                    try {
                        int rowNo = Integer.parseInt(item.getIndex01()); // Assuming getIndex01() returns RowNo
                        List<String> colors = highlightedRowsDetail.get(rowNo);
                        if (colors != null && !colors.isEmpty()) {
                            setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";");
                        } else {
                            setStyle(""); // Default style
                        }
                    } catch (NumberFormatException e) {
                        setStyle(""); // Safe fallback if index is invalid
                    }
                }
            }
        });
        tblViewOrderDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewOrderDetails, tblViewPuchaseOrder);  // need to use computed-size as min-width on particular column to work

    }

    public void loadTableMain() {
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewPuchaseOrder.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
//                Thread.sleep(1000);
                Platform.runLater(() -> {
                    main_data.clear();
                    String lsMainDate = "";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Define the format
                    // Simulate loading delay
                    try {
                        if (!poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate().equals("")) {
                            Object loDate = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate();
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
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderCount() > 0) {
                        for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderCount() - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelDeliveryAcceptance_Main(String.valueOf(lnCtr + 1),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderList(lnCtr).Supplier().getCompanyName()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderList(lnCtr).getTransactionDate()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderList(lnCtr).getTransactionNo())
                                ));
                            } catch (Exception e) {

                            }

                        }
                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewPuchaseOrder.getSelectionModel().select(0);
                                tblViewPuchaseOrder.getFocusModel().focus(0);
                                pnMain = tblViewPuchaseOrder.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewPuchaseOrder.getSelectionModel().select(pnMain);
                            tblViewPuchaseOrder.getFocusModel().focus(pnMain);
                        }
                    }
                    loadTab();
                });
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrder.setPlaceholder(placeholderLabel);
                } else {
                    tblViewPuchaseOrder.toFront();
                }
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrder.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

    }

    private void goToPageBasedOnSelectedRow(String pnRowMain) {
        if (mainSearchListener != null) {
            tfOrderNo.textProperty().removeListener(mainSearchListener);
            mainSearchListener = null;
        }
        if (detailSearchListener != null) {
            tfOrderNo.textProperty().removeListener(detailSearchListener);
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
        changeTableView(targetPage, ROWS_PER_PAGE);

        Platform.runLater(() -> {
            if (lbresetpredicate) {
                tblViewPuchaseOrder.scrollTo(indexInPage);
                lbresetpredicate = false;
            }
        });
    }

    public void loadTableDetailFromMain() {
        try {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                poJSON = new JSONObject();

                ModelDeliveryAcceptance_Main selected = (ModelDeliveryAcceptance_Main) tblViewPuchaseOrder.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    pnMain = pnRowMain;

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().addPurchaseOrderToPORDetail(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderList(pnMain).getTransactionNo());
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                }
                loadTableDetail();
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "Data can only be viewed when in ADD or UPDATE mode.");
            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail
        disableAllHighlight(tblViewOrderDetails, highlightedRowsDetail);

        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
        if (lbresetpredicate) {
            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
            filteredDataDetail.setPredicate(null);
            lbresetpredicate = false;
            tfOrderNo.textProperty().removeListener(detailSearchListener);

            mainSearchListener = null;
            filteredData.setPredicate(null);
            initMainGrid();
            initDetailsGrid();
            Platform.runLater(() -> {
                tfOrderNo.setText("");
            });
            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
        }
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    int lnCtr;
                    details_data.clear();
                    plOrderNoPartial.clear();
                    try {

                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() == null || poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                                }
                            }

                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) < 0) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                            }
                        }

                        List<String> lOrderNo = new ArrayList<>();
                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                            try {
                                lnTotal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue();
                            } catch (Exception e) {
                            }

                            if ((!poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                highlight(tblViewOrderDetails, lnCtr + 1, "#FAA0A0", highlightedRowsDetail);
                            }

                            plOrderNoPartial.add(new Pair<>(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo(), String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue())));

                            details_data.add(
                                    new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty())),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity())),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                    ));
                        }

                        for (Pair<String, String> pair : plOrderNoPartial) {
                            if (!"".equals(pair.getKey()) && pair.getKey() != null) {

                                highlightByKey(tblViewPuchaseOrder, pair.getKey(), "#A7C7E7", highlightedRowsMain);
                            }
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewOrderDetails.getSelectionModel().select(0);
                                tblViewOrderDetails.getFocusModel().focus(0);
                                pnDetail = tblViewOrderDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewOrderDetails.getSelectionModel().select(pnDetail);
                            tblViewOrderDetails.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(DeliveryAcceptance_EntryLPController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewOrderDetails.setPlaceholder(placeholderLabel);
                } else {
                    tblViewOrderDetails.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewOrderDetails.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        btnNew.setVisible(!lbShow);
        btnNew.setManaged(!lbShow);
        btnSearch.setVisible(lbShow);
        btnSearch.setManaged(lbShow);
        btnSave.setVisible(lbShow);
        btnSave.setManaged(lbShow);
        btnCancel.setVisible(lbShow);
        btnCancel.setManaged(lbShow);

        btnUpdate.setVisible(lbShow2);
        btnUpdate.setManaged(lbShow2);
        btnPrint.setVisible(lbShow2);
        btnPrint.setManaged(lbShow2);
        btnHistory.setVisible(lbShow2);
        btnHistory.setManaged(lbShow2);

        btnBrowse.setVisible(lbShow3);
        btnBrowse.setManaged(lbShow3);
        btnClose.setVisible(lbShow3);
        btnClose.setManaged(lbShow3);

//        apMaster.setDisable(!lbShow);
        dpTransactionDate.setDisable(!lbShow);
        dpReferenceDate.setDisable(!lbShow);
        tfTrucking.setDisable(!lbShow);
        taRemarks.setDisable(!lbShow);
        tfReferenceNo.setDisable(!lbShow);
        tfTerm.setDisable(!lbShow);
        tfDiscountRate.setDisable(!lbShow);
        tfDiscountAmount.setDisable(!lbShow);

        apDetail.setDisable(!lbShow);

        switch (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.POSTED:
            case PurchaseOrderReceivingStatus.PAID:
                btnUpdate.setVisible(false);
                btnUpdate.setManaged(false);
                break;
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
                btnUpdate.setVisible(false);
                btnUpdate.setManaged(false);
                btnPrint.setVisible(false);
                btnPrint.setManaged(false);
                break;
        }
    }

//    private void initButton(int fnValue) {
//        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
//        // Manage visibility and managed state of other buttons
//        boolean lbShow3 = fnValue == EditMode.ADDNEW;
//
//        btnBrowse.setVisible(!lbShow3); // Requires no change to state
//
//        btnNew.setVisible(!lbShow);
////        btnRetrieve.setVisible(!lbShow);
//        btnClose.setVisible(!lbShow);
//
//        btnSearch.setVisible(lbShow);
//        btnSave.setVisible(lbShow);
//        btnCancel.setVisible(lbShow);
//
//        btnBrowse.setManaged(!lbShow3);
//
//        btnNew.setManaged(!lbShow);
////        btnRetrieve.setManaged(!lbShow);
//        btnClose.setManaged(!lbShow);
//
//        btnSearch.setManaged(lbShow);
//        btnSave.setManaged(lbShow);
//        btnCancel.setManaged(lbShow);
//
//        boolean lbShow2 = fnValue == EditMode.READY;
//
//        btnUpdate.setVisible(lbShow2);
//        btnPrint.setVisible(lbShow2);
//        btnHistory.setVisible(lbShow2);
//
//        btnUpdate.setManaged(lbShow2);
//        btnPrint.setManaged(lbShow2);
//        btnHistory.setManaged(lbShow2);
//
//        btnClose.setVisible(lbShow2);
//        btnClose.setManaged(lbShow2);
//
////        apBrowse.setDisable(lbShow); // no usage
//        apMaster.setDisable(!lbShow);
//        apDetail.setDisable(!lbShow);
////        apTable.setDisable(!lbShow); // disable upon for viewing?
////        if (Integer.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().getMasterModel().getTransactionStatus()) != 0) {
////            btnVoid.setDisable(false);
////        } else {
////            btnVoid.setDisable(true);
////        }
////        poPurchaseReceivingController.PurchaseOrderReceiving().setTransType("SP");
//
//
//    }
    private void loadTab() {
        int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(0);
        changeTableView(0, ROWS_PER_PAGE);
        pgPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            changeTableView(newValue.intValue(), ROWS_PER_PAGE);
            tblViewPuchaseOrder.scrollTo(0);
        });
    }

    private void changeTableView(int index, int limit) {
        tblViewPuchaseOrder.getSelectionModel().clearSelection();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, main_data.size());
        int minIndex = Math.min(toIndex, main_data.size());
        try {
            SortedList<ModelDeliveryAcceptance_Main> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tblViewPuchaseOrder.comparatorProperty());
        } catch (Exception e) {
        }
        try {
            tblViewPuchaseOrder.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        } catch (Exception e) {
        }
    }
// Generic method to highlight with specific color

    public <T> void highlight(TableView<T> table, int rowIndex, String color, Map<Integer, List<String>> highlightMap) {
        highlightMap.computeIfAbsent(rowIndex, k -> new ArrayList<>()).add(color);
        table.refresh(); // Refresh to apply changes
    }

// Generic method to remove highlight from a specific row
    public <T> void disableHighlight(TableView<T> table, int rowIndex, Map<Integer, List<String>> highlightMap) {
        highlightMap.remove(rowIndex);
        table.refresh();
    }

// Generic method to remove all highlights
    public <T> void disableAllHighlight(TableView<T> table, Map<Integer, List<String>> highlightMap) {
        highlightMap.clear();
        table.refresh();
    }

// Generic method to remove all highlights of a specific color
    public <T> void disableAllHighlightByColor(TableView<T> table, String color, Map<Integer, List<String>> highlightMap) {
        highlightMap.forEach((key, colors) -> colors.removeIf(c -> c.equals(color)));
        highlightMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        table.refresh();
    }

    public <T> void highlightByKey(TableView<T> table, String key, String color, Map<String, List<String>> highlightMap) {
        List<String> colors = highlightMap.computeIfAbsent(key, k -> new ArrayList<>());
        // Add the color only if it doesn't already exist for that key
        if (!colors.contains(color)) {
            colors.add(color);

            table.refresh(); // Refresh to apply changes
        }
    }

    public <T> void disableHighlightByKey(TableView<T> table, String key, Map<String, List<String>> highlightMap) {
        highlightMap.remove(key);
        table.refresh();

    }

    public <T> void disableAllHighlightByKey(TableView<T> table, Map<String, List<String>> highlightMap) {
        highlightMap.clear();
        table.refresh();

    }

    public <T> void disableAllHighlightByColorForKey(TableView<T> table, String color, Map<String, List<String>> highlightMap) {
        highlightMap.forEach((key, colors) -> colors.removeIf(c -> c.equals(color)));
        highlightMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        table.refresh();

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
                    txtField.textProperty().removeListener(mainSearchListener);
                    mainSearchListener = null; // Clear reference to avoid memory leaks
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex02().toLowerCase().contains(lowerCaseFilter);
            });
            // If no results and autoSearchMain is enabled, remove listener and trigger autoSearchMain
            if (filteredDataDetail.isEmpty()) {
                if (main_data.size() > 0) {
                    txtField.textProperty().removeListener(detailSearchListener);
                    filteredData = new FilteredList<>(main_data, b -> true);
                    autoSearchMain(txtField); // Trigger autoSearchMain if no results
                    tblViewPuchaseOrder.setItems(filteredData);

                    String currentText = txtField.getText();
                    txtField.setText(currentText + " "); // Add a space
                    txtField.setText(currentText);       // Set back to original
                }
            } else {
                if (filteredDataDetail.size() == details_data.size()) {
                    tblViewOrderDetails.getSelectionModel().select(pnDetail);
                    tblViewOrderDetails.getFocusModel().focus(pnDetail);
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
                        txtField.textProperty().removeListener(mainSearchListener);
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
