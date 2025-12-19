/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderReturn_Detail;
import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderReturn_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReturnControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReturnStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import javafx.animation.PauseTransition;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReturn;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;

/**
 * FXML Controller class
 *
 * @author Team 2 : Arsiela & Aldrich
 */
public class PurchaseOrderReturn_ConfirmationAppliancesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private final String pxeModuleName = "Purchase Order Return Confirmation Appliances";
    static PurchaseOrderReturnControllers poPurchaseReturnController;
    public int pnEditMode;
    boolean isPrinted = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psTransactionNo = "";
    private boolean pbEntered = false;
    private ObservableList<ModelPurchaseOrderReturn_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPurchaseOrderReturn_Detail> details_data = FXCollections.observableArrayList();

    private FilteredList<ModelPurchaseOrderReturn_Main> filteredData;
    private FilteredList<ModelPurchaseOrderReturn_Detail> filteredDataDetail;

    private int pnAttachment;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;

    @FXML
    private HBox hbButtons, hboxid;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnPrint, btnReturn, btnHistory, btnRetrieve, btnClose;

    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfTransactionNo, tfSupplier, tfReferenceNo, tfPOReceivingNo,
            tfTotal, tfIMEINo, tfBarcode, tfDescription, tfReturnQuantity, tfColor,
            tfInventoryType, tfMeasure, tfCost, tfBrand, tfModel, tfModelVariant, tfReceiveQuantity;

    @FXML
    private DatePicker dpTransactionDate;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView tblViewDetails, tblViewPuchaseOrderReturn;

    @FXML
    private TableColumn tblRowNoDetail, tblImeiNoDetail, tblBarcodeDetail,
            tblDescriptionDetail, tblCostDetail, tblReceiveQuantityDetail, tblReturnQuantityDetail, tblTotalDetail,
            tblRowNo, tblSupplier, tblDate, tblReferenceNo;

    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poPurchaseReturnController = new PurchaseOrderReturnControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poPurchaseReturnController.PurchaseOrderReturn().InitTransaction(); // Initialize transaction
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

        Platform.runLater(() -> {
            poPurchaseReturnController.PurchaseOrderReturn().Master().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().Master().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setCategoryId(psCategoryId);
            poPurchaseReturnController.PurchaseOrderReturn().initFields();
            poPurchaseReturnController.PurchaseOrderReturn().setWithUI(true);
            loadRecordSearch();
        });

        pgPagination.setPageCount(1);

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
                    case "btnPrint":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().printRecord(() -> {
                            if (isPrinted) {
                                JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                                poPurchaseReturnController.PurchaseOrderReturn().resetMaster();
                                poPurchaseReturnController.PurchaseOrderReturn().Detail().clear();
                                pnEditMode = EditMode.UNKNOWN;
                                clearTextFields();
                                initButton(pnEditMode);
                            }
                            Platform.runLater(() -> {
                                try {
                                    if (!isPrinted) {
                                        poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(pnMain).getTransactionNo());
                                    }
                                    loadRecordMaster();
                                    loadTableDetail();
                                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                }
                                isPrinted = false;
                            });
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
                    case "btnUpdate":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(psTransactionNo);
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField != null)) {
                            if (lastFocusedTextField instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField;
                                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", apBrowse, apMaster, apDetail).contains(tf.getId())) {
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
                            JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrievePOR();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(psTransactionNo);
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus().equals(PurchaseOrderReturnStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poPurchaseReturnController.PurchaseOrderReturn().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                // Print Transaction Prompt
                                loJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(psTransactionNo);
                                loadRecordMaster();
                                isPrinted = false;
                                if ("success".equals(loJSON.get("result"))) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to print this transaction?")) {
                                        isPrinted = true;
                                        btnPrint.fire();
                                    }
                                }
                                if (!isPrinted) {
                                    JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                                }
                            }
                        } else {
                            return;
                        }

                        break;

                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (PurchaseOrderReturnStatus.CONFIRMED.equals(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus())) {
                                poJSON = poPurchaseReturnController.PurchaseOrderReturn().CancelTransaction("");
                            } else {
                                poJSON = poPurchaseReturnController.PurchaseOrderReturn().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnReturn":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to return transaction?") == true) {
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().ReturnTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(pnMain + 1), "#FAC898", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;

                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnReturn", "btnVoid", "btnCancel")) {
                    poPurchaseReturnController.PurchaseOrderReturn().resetMaster();
                    poPurchaseReturnController.PurchaseOrderReturn().Detail().clear();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poPurchaseReturnController.PurchaseOrderReturn().Master().setIndustryId(psIndustryId);
                    poPurchaseReturnController.PurchaseOrderReturn().Master().setCompanyId(psCompanyId);
                    poPurchaseReturnController.PurchaseOrderReturn().Master().setCategoryCode(psCategoryId);
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnPrint", "btnAddAttachment", "btnRemoveAttachment",
                        "btnArrowRight", "btnArrowLeft", "btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                }
                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId())) {
                        tfReturnQuantity.requestFocus();
                    } else {
                        tfIMEINo.requestFocus();
                    }
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrievePOR() {
        poJSON = new JSONObject();
        poJSON = poPurchaseReturnController.PurchaseOrderReturn().loadPurchaseOrderReturn("confirmation", psSupplierId, tfSearchReferenceNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain();
        }
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
                    poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setRemarks(lsValue);
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
                case "tfBarcode":
                case "tfIMEINo":
                case "tfDescription":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setStockId("");
                    }
                    break;
                case "tfReturnQuantity":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity() != null
                            && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity())) {
                        if (poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(pnDetail).intValue() < Integer.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Return quantity cannot be greater than the order quantity.");
                            poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setQuantity(0);
                            tfReturnQuantity.requestFocus();
                            break;
                        }
                    }

                    poJSON = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setQuantity((Integer.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
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

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
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
                case "tfSearchSupplier":
                    if (lsValue.equals("")) {
                        psSupplierId = "";
                    }
                    break;
                case "tfSearchReferenceNo":
                    break;
            }
            if (lsTxtFieldID.equals("tfSearchSupplier")
                    || lsTxtFieldID.equals("tfSearchReferenceNo")) {
                loadRecordSearch();
            }
        }
    };

    public void moveNext() {
        int lnReceiveQty = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
        apDetail.requestFocus();
        int lnNewvalue = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
        if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                && poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null
                && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId()))) {
            tfReturnQuantity.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewDetails);
            loadRecordDetail();
            if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                tfReturnQuantity.requestFocus();
            } else {
                tfIMEINo.requestFocus();
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

            TableView<?> currentTable = tblViewDetails;
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
                        case "tfIMEINo":
                        case "tfBarcode":
                        case "tfReturnQuantity":
                            int lnReceiveQty = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
                            apDetail.requestFocus();
                            int lnNewvalue = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
                            if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                                    && poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId()))) {
                                tfReturnQuantity.requestFocus();
                            } else {
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                loadRecordDetail();
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                                    tfReturnQuantity.requestFocus();
                                } else {
                                    tfIMEINo.requestFocus();
                                }
                                event.consume();
                            }
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfIMEINo":
                        case "tfBarcode":
                        case "tfReturnQuantity":
                            moveNext();
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchSupplier":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();
                            }
                            retrievePOR();
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            retrievePOR();
                            return;
                        case "tfIMEINo":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchIMEINo(lsValue, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReturnQuantity.requestFocus();
                                    return;
                                }
                                tfIMEINo.setText("");
                                break;
                            }
                            loadTableDetail();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReturnQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;

                        case "tfBarcode":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchBarcode(lsValue, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReturnQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReturnQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfDescription":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchDescription(lsValue, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReturnQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReturnQuantity.requestFocus();
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
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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
                LocalDate selectedDate = null;
                LocalDate receivingDate = null;
                String lsServerDate = "";
                String lsTransDate = "";
                String lsSelectedDate = "";
                String lsReceivingDate = "";
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
                        if (poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo())) {
                                lsReceivingDate = sdfFormat.format(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getTransactionDate());
                                receivingDate = LocalDate.parse(lsReceivingDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                                if (selectedDate.isBefore(receivingDate)) {
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Transaction date cannot be before the receiving date.");
                                    pbSuccess = false;
                                }
                            } else {
                                if (pbSuccess && !lsServerDate.equals(lsSelectedDate) && pnEditMode == EditMode.ADDNEW) {
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Select PO Receiving before changing the transaction date.");
                                    pbSuccess = false;
                                }
                            }

                            if (pbSuccess && ((poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
                                pbSuccess = false;
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
                                            } else {
                                                poPurchaseReturnController.PurchaseOrderReturn().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                            }
                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                } else {
                                    poPurchaseReturnController.PurchaseOrderReturn().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                }
                            }

                            if (pbSuccess) {
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableMain() {
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewPuchaseOrderReturn.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
//                Thread.sleep(1000);

                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    main_data.clear();
                    JFXUtil.disableAllHighlight(tblViewPuchaseOrderReturn, highlightedRowsMain);
                    if (poPurchaseReturnController.PurchaseOrderReturn().getPurchaseOrderReturnCount() > 0) {
                        //pending
                        //retreiving using column index
                        for (int lnCtr = 0; lnCtr <= poPurchaseReturnController.PurchaseOrderReturn().getPurchaseOrderReturnCount() - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelPurchaseOrderReturn_Main(String.valueOf(lnCtr + 1),
                                        String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(lnCtr).Supplier().getCompanyName()),
                                        String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(lnCtr).getTransactionDate()),
                                        String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(lnCtr).getTransactionNo())
                                ));
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }

                            if (poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(lnCtr).getTransactionStatus().equals(PurchaseOrderReturnStatus.CONFIRMED)) {
                                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        }
                    }

                    if (pnMain < 0 || pnMain
                            >= main_data.size()) {
                        if (!main_data.isEmpty()) {
                            /* FOCUS ON FIRST ROW */
                            tblViewPuchaseOrderReturn.getSelectionModel().select(0);
                            tblViewPuchaseOrderReturn.getFocusModel().focus(0);
                            pnMain = tblViewPuchaseOrderReturn.getSelectionModel().getSelectedIndex();
                        }
                    } else {
                        /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                        tblViewPuchaseOrderReturn.getSelectionModel().select(pnMain);
                        tblViewPuchaseOrderReturn.getFocusModel().focus(pnMain);
                    }
//                    if (poPurchaseReturnController.PurchaseOrderReturn().getPurchaseOrderReturnCount() < 1) {
//                        JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewPuchaseOrderReturn, filteredData);
//                    }
                    JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewPuchaseOrderReturn, filteredData);
                });

                return null;
            }

            @Override
            protected void succeeded() {
                placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrderReturn.setPlaceholder(placeholderLabel);
                } else {
                    tblViewPuchaseOrderReturn.toFront();
                }
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrderReturn.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Company().getCompanyName() + " - " + poPurchaseReturnController.PurchaseOrderReturn().Master().Industry().getDescription());

            if (psSupplierId.equals("")) {
                tfSearchSupplier.setText("");
            } else {
                tfSearchSupplier.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Supplier().getCompanyName());
            }
            try {
                if (tfSearchReferenceNo.getText() == null || tfSearchReferenceNo.getText().equals("")) {
                    tfSearchReferenceNo.setText("");
                } else {
                }
            } catch (Exception e) {
                tfSearchReferenceNo.setText("");
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) {
                return;
            }
            boolean lbDisable = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;

            JFXUtil.setDisabled(!lbDisable, tfIMEINo, tfBarcode, tfDescription);

            tfIMEINo.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).InventorySerial().getSerial01());
            tfBarcode.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Model().getDescription());
            tfModelVariant.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Variant().getDescription());
            tfColor.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getUnitPrce(), true));
            tfReceiveQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(pnDetail).intValue()));
            tfReturnQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue()));

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        boolean lbIsReprint = poPurchaseReturnController.PurchaseOrderReturn().Master().getPrint().equals("1") ? true : false;
        if (lbIsReprint) {
            btnPrint.setText("Reprint");
        } else {
            btnPrint.setText("Print");
        }

        try {
            Platform.runLater(() -> {
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus();
                String lsStat = "UNKNOWN";
                switch (lsActive) {
                    case PurchaseOrderReturnStatus.POSTED:
                        lsStat = "POSTED";
                        break;
                    case PurchaseOrderReturnStatus.PAID:
                        lsStat = "PAID";
                        break;
                    case PurchaseOrderReturnStatus.CONFIRMED:
                        lsStat = "CONFIRMED";
                        break;
                    case PurchaseOrderReturnStatus.OPEN:
                        lsStat = "OPEN";
                        break;
                    case PurchaseOrderReturnStatus.RETURNED:
                        lsStat = "RETURNED";
                        break;
                    case PurchaseOrderReturnStatus.VOID:
                        lsStat = "VOIDED";
                        lbPrintStat = false;
                        break;
                    case PurchaseOrderReturnStatus.CANCELLED:
                        lsStat = "CANCELLED";
                        break;
                    default:
                        lsStat = "UNKNOWN";
                        break;
                }
                lblStatus.setText(lsStat);
                JFXUtil.setButtonsVisibility(lbPrintStat, btnPrint);
            });

            poPurchaseReturnController.PurchaseOrderReturn().computeFields();

            // Transaction Date
            tfTransactionNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfSupplier.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Supplier().getCompanyName());
            tfReferenceNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getReferenceNo());
            tfPOReceivingNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo());
            taRemarks.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getRemarks());

            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionTotal().doubleValue(), true));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelPurchaseOrderReturn_Main selected = (ModelPurchaseOrderReturn_Main) tblViewPuchaseOrderReturn.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewPuchaseOrderReturn, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewPuchaseOrderReturn, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                psTransactionNo = poPurchaseReturnController.PurchaseOrderReturn().PurchaseOrderReturnList(pnMain).getTransactionNo();
                poJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(psTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
            }
            Platform.runLater(() -> {
                loadTableDetail();
            });
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail

        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId() == null || poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                                }
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) < 0) {
                                poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                            }
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReturnController.PurchaseOrderReturn().getDetailCount(); lnCtr++) {
                            try {
                                lnTotal = poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue();
                            } catch (Exception e) {
                            }
                            String lsImeiNo = "";
                            if (poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).InventorySerial().getSerial01() != null
                                    && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).InventorySerial().getSerial01())) {
                                lsImeiNo = poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).InventorySerial().getSerial01();
                            } else {
                                lsImeiNo = poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).InventorySerial().getSerial02();
                            }

                            details_data.add(
                                    new ModelPurchaseOrderReturn_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(lsImeiNo),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(lnCtr).intValue()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)))
                            );
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewDetails.getSelectionModel().select(0);
                                tblViewDetails.getFocusModel().focus(0);
                                pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewDetails.getSelectionModel().select(pnDetail);
                            tblViewDetails.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                } else {
                    tblViewDetails.toFront();
                }
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
                dpTransactionDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate);
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtField_Focus, tfSearchSupplier, tfSearchReferenceNo);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBarcode, tfIMEINo, tfDescription, tfReturnQuantity);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);

        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity, tfReturnQuantity);
        CustomCommonUtil.inputDecimalOnly(tfCost);
    }

    public void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                        tfReturnQuantity.requestFocus();
                    } else {
                        tfIMEINo.requestFocus();
                    }
                }
            }
        });

        tblViewPuchaseOrderReturn.setOnMouseClicked(event -> {
            pnMain = tblViewPuchaseOrderReturn.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });

        tblViewPuchaseOrderReturn.setRowFactory(tv -> new TableRow<ModelPurchaseOrderReturn_Main>() {
            @Override
            protected void updateItem(ModelPurchaseOrderReturn_Main item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle(""); // Reset for empty rows
                } else {
                    String key = item.getIndex01(); // defines the ReferenceNo
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

        tblViewDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewDetails, tblViewPuchaseOrderReturn); // need to use computed-size in min-width of the column to work
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
//        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);

        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnPrint, btnUpdate, btnHistory, btnConfirm, btnVoid);

        //Unkown || Ready
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setButtonsVisibility(false, btnReturn);

        switch (poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus()) {
            case PurchaseOrderReturnStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                if (poPurchaseReturnController.PurchaseOrderReturn().Master().isProcessed()) {
                    JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                } else {
                    //JFXUtil.setButtonsVisibility(lbShow3, btnReturn);
                }
                break;
            case PurchaseOrderReturnStatus.POSTED:
            case PurchaseOrderReturnStatus.PAID:
            case PurchaseOrderReturnStatus.RETURNED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnReturn, btnVoid);
                break;
            case PurchaseOrderReturnStatus.VOID:
            case PurchaseOrderReturnStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnReturn, btnVoid, btnPrint);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblReceiveQuantityDetail, tblReturnQuantityDetail);
        JFXUtil.setColumnLeft(tblImeiNoDetail, tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelPurchaseOrderReturn_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetails.comparatorProperty());
        tblViewDetails.setItems(sortedData);
        tblViewDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewPuchaseOrderReturn);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewPuchaseOrderReturn.setItems(filteredData);
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblViewDetails":
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
            }
        }
    }

    public void clearTextFields() {
        psTransactionNo = "";
        previousSearchedTextField = null;
        lastFocusedTextField = null;
        dpTransactionDate.setValue(null);

        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }
}
