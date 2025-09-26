/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPOQuotationRequest_Detail;
import ph.com.guanzongroup.integsys.model.ModelPOQuotationRequest_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
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
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.CheckBox;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
import java.time.ZoneId;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;

/**
 * FXML Controller class
 *
 * @author Team 2 : Arsiela & Aldrich
 */
public class POQuotationRequest_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static QuotationControllers poController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchClientId = "";
    private String psTransactionNo = "";
    private boolean pbEntered = false;
    private boolean pbKeyPressed = false;

    private ObservableList<ModelPOQuotationRequest_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPOQuotationRequest_Detail> details_data = FXCollections.observableArrayList();

    private FilteredList<ModelPOQuotationRequest_Main> filteredData;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchDepartment, tfSearchCategory, tfSearchReferenceNo, tfTransactionNo, tfReferenceNo, tfDepartment, tfDestination, tfCategory, tfBrand, tfModel, tfBarcode, tfDescription, tfCost, tfQuantity;
    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpExpectedDate;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblViewTransDetails, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblQuantityDetail, tblTotalDetail, tblRowNo, tblDepartment, tblDate, tblReferenceNo;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        poController = new QuotationControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poController.POQuotationRequest().InitTransaction(); // Initialize transaction
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
        Platform.runLater(() -> {
            poController.POQuotationRequest().Master().setIndustryId(psIndustryId);
            poController.POQuotationRequest().Master().setCategoryCode(psCategoryId);
            poController.POQuotationRequest().setIndustryId(psIndustryId);
            poController.POQuotationRequest().setCompanyId(psCompanyId);
            poController.POQuotationRequest().setCategoryId(psCategoryId);
//            poController.POQuotationRequest().initFields();
            poController.POQuotationRequest().setWithUI(true);
            loadRecordSearch();
            loadRecordMaster();
        });

        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
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
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbReverse": // this is the id
                    if (poController.POQuotationRequest().Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        if (!checkedBox.isSelected()) {
                            poController.POQuotationRequest().Detail().remove(pnDetail);
                        }
                    } else {
                        poController.POQuotationRequest().Detail(pnDetail).isReverse(checkedBox.isSelected());
                    }

                    loadTableDetail.reload();
                    break;
            }
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String tabText = "";

            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poController.POQuotationRequest().OpenTransaction(poController.POQuotationRequest().Master().getTransactionNo());
                        poJSON = poController.POQuotationRequest().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.POQuotationRequest().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrievePOQuotationRequest();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.POQuotationRequest().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.POQuotationRequest().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON;

                                loJSON = poController.POQuotationRequest().OpenTransaction(psTransactionNo);

                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.POQuotationRequest().Master().getTransactionStatus().equals(POQuotationRequestStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.POQuotationRequest().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                            }
                        } else {
                            return;
                        }

                        break;

                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poController.POQuotationRequest().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (POQuotationRequestStatus.CONFIRMED.equals(poController.POQuotationRequest().Master().getTransactionStatus())) {
                                poJSON = poController.POQuotationRequest().CancelTransaction("");
                            } else {
                                poJSON = poController.POQuotationRequest().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
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
                    poController.POQuotationRequest().resetMaster();
                    poController.POQuotationRequest().Detail().clear();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poController.POQuotationRequest().Master().setIndustryId(psIndustryId);
                    poController.POQuotationRequest().setCompanyId(psCompanyId);
                    poController.POQuotationRequest().Master().setCategoryCode(psCategoryId);
//                    poController.POQuotationRequest().initFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnPrint", "btnAddAttachment", "btnRemoveAttachment",
                        "btnArrowRight", "btnArrowLeft", "btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    if (poController.POQuotationRequest().Detail(pnDetail).getStockId() == null || "".equals(poController.POQuotationRequest().Detail(pnDetail).getStockId())) {
                        tfBarcode.requestFocus();
                    }
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void retrievePOQuotationRequest() {
        poJSON = new JSONObject();
        poController.POQuotationRequest().setTransactionStatus(POQuotationRequestStatus.OPEN + POQuotationRequestStatus.CONFIRMED);

        SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
        String inputText = JFXUtil.isObjectEqualTo(dpSearchTransactionDate.getEditor().getText(), "") ? "01/01/1900" : dpSearchTransactionDate.getEditor().getText();
        String lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
        LocalDate selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
        
        poJSON = poController.POQuotationRequest().loadPOQuotationRequestList(oApp.getBranchName(), tfSearchDepartment.getText(),
                tfSearchCategory.getText(), java.sql.Date.valueOf(selectedDate),
                tfSearchReferenceNo.getText(), false);
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poController.POQuotationRequest().Master().setRemarks(lsValue);
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
                    case "tfBrand":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poController.POQuotationRequest().Detail(pnDetail).setBrandId("");
                        }
                        break;
                    case "tfModel":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poController.POQuotationRequest().Detail(pnDetail).setModelId("");
                        }
                        break;
                    case "tfBarcode":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poController.POQuotationRequest().Detail(pnDetail).setStockId("");
                        }
                        break;
                    case "tfDescription":
                        //if value is blank then reset
                        if (lsValue.equals("")) {
                            poJSON = poController.POQuotationRequest().Detail(pnDetail).setStockId("");
                            poJSON = poController.POQuotationRequest().Detail(pnDetail).setDescription("");
                        } else {
                            if (!lsValue.equals(poController.POQuotationRequest().Detail(pnDetail).getDescription())) {
                                try {
                                    poJSON = poController.POQuotationRequest().SearchInventory(lsValue, false, pnDetail);
                                    if ("error".equals(poJSON.get("result"))) {
                                        int lnTempRow = JFXUtil.getDetailTempRow(details_data, Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1, 7);
                                        pnDetail = lnTempRow;
                                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    } else {
                                        int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                        JFXUtil.runWithDelay(0.80, () -> {
                                            pnDetail = lnReturned;
                                            loadTableDetail.reload();
                                        });
                                        if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).getDescription(), null, "")) {
                                            JFXUtil.textFieldMoveNext(tfQuantity);
                                        }
                                    }
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                }
                            }
                        }
                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotationRequest().Detail(pnDetail).setQuantity(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
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
                    case "tfReferenceNo":
                        poJSON = poController.POQuotationRequest().Master().setReferenceNo(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "tfDepartment":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotationRequest().Master().setDepartmentId("");
                        }
                        break;
                    case "tfDestination":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotationRequest().Master().setDestination("");
                        }
                        break;
                    case "tfCategory":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.POQuotationRequest().Master().getCategoryLevel2() != null && !"".equals(poController.POQuotationRequest().Master().getCategoryLevel2())) {
                                    if (poController.POQuotationRequest().getDetailCount() > 0) {
                                        if (poController.POQuotationRequest().checkExistStockInventory()) {
                                            if (!pbKeyPressed) {
                                                if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                        "Are you sure you want to change the category?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                                    poController.POQuotationRequest().removeWithInvDetails();
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
                            }

                            poJSON = poController.POQuotationRequest().Master().setCategoryLevel2("");
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                if (lsValue == null) {
                    return;
                }
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchDepartment":
                        if (lsValue.equals("")) {
                            poController.POQuotationRequest().setSearchDepartment("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchCategory":
                        if (lsValue.equals("")) {
                            poController.POQuotationRequest().setSearchCategory("");
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchReferenceNo":
                        break;
                }
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewTransDetails)).getIndex07())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewTransDetails)).getIndex07());
            }
            loadRecordDetail();
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.POQuotationRequest().Detail(pnDetail).Brand().getBrandId(), tfBrand}, // if null or empty, then requesting focus to the txtfield
                {poController.POQuotationRequest().Detail(pnDetail).Inventory().Model().getModelId(), tfModel},
                {poController.POQuotationRequest().Detail(pnDetail).Inventory().getBarCode(), tfBarcode}
            }, tfQuantity); // default
            if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).getDescription(), null, "")) {
                tfQuantity.requestFocus();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = tfQuantity.isFocused() ? true : false;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfBarcode":
                        case "tfDescription":
                        case "tfQuantity":
                            moveNext(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfBarcode":
                        case "tfDescription":
                        case "tfQuantity":
                            moveNext(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchDepartment":
                            poJSON = poController.POQuotationRequest().SearchDepartment(lsValue, false, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                psSearchClientId = "";
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotationRequest();
                            return;
                        case "tfSearchCategory":
                            poJSON = poController.POQuotationRequest().SearchCategory(lsValue, false, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                psSearchClientId = "";
                                break;
                            }
                            loadRecordSearch();
                            retrievePOQuotationRequest();
                            return;
                        case "tfSearchReferenceNo":
                            retrievePOQuotationRequest();
                            return;
                        case "tfDestination":
                            poJSON = poController.POQuotationRequest().SearchDestination(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfDepartment);
                            }
                            loadRecordMaster();
                            return;
                        case "tfDepartment":
                            poJSON = poController.POQuotationRequest().SearchDepartment(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfCategory);
                            }
                            loadRecordMaster();
                            return;
                        case "tfCategory":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poController.POQuotationRequest().Master().getCategoryLevel2() != null && !"".equals(poController.POQuotationRequest().Master().getCategoryLevel2())) {
                                    if (poController.POQuotationRequest().getDetailCount() > 0) {
                                        if (poController.POQuotationRequest().checkExistStockInventory()) {
                                            pbKeyPressed = true;
                                            if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                    "Are you sure you want to change the category?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                                poController.POQuotationRequest().removeWithInvDetails();
                                                loadTableDetail.reload();
                                            } else {
                                                return;
                                            }
                                            pbKeyPressed = false;
                                        }
                                    }
                                }
                            }

                            poJSON = poController.POQuotationRequest().SearchCategory(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(dpExpectedDate);
                            }
                            loadRecordMaster();
                            return;
                        case "tfBrand":
                            poJSON = poController.POQuotationRequest().SearchBrand(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                JFXUtil.textFieldMoveNext(tfModel);
                            }
                            loadRecordMaster();
                            return;
                        case "tfModel":
                            poJSON = poController.POQuotationRequest().SearchModel(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                loadTableDetail.reload();
                                JFXUtil.textFieldMoveNext(tfBarcode);
                            }
                            return;
                        case "tfBarcode":
                            poJSON = poController.POQuotationRequest().SearchInventory(lsValue, true, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfQuantity);
                                }
                            }
                            break;
                        case "tfDescription":
                            poJSON = poController.POQuotationRequest().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfQuantity);
                                }
                            }
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

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);

                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    switch (datePicker.getId()) {
                        case "dpExpectedDate":
                            poJSON = poController.POQuotationRequest().Master().setExpectedPurchaseDate(null);
                            break;
                    }
                    return;
                }

                String lsServerDate, lsTransDate, lsSelectedDate;
                LocalDate currentDate, selectedDate, transactionDate;
                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        lsServerDate = sdfFormat.format(oApp.getServerDate());
                        lsTransDate = sdfFormat.format(poController.POQuotationRequest().Master().getTransactionDate());
                        lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                        currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                        if (poController.POQuotationRequest().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotationRequest().getEditMode() == EditMode.UPDATE) {

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poController.POQuotationRequest().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
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
                                poController.POQuotationRequest().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpExpectedDate":
                        lsServerDate = sdfFormat.format(oApp.getServerDate());
                        lsTransDate = sdfFormat.format(poController.POQuotationRequest().Master().getTransactionDate());
                        lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                        currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                        if (poController.POQuotationRequest().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotationRequest().getEditMode() == EditMode.UPDATE) {

                            if (selectedDate.isBefore(transactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Expected Purchase Date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else {
                                poController.POQuotationRequest().Master().setExpectedPurchaseDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpSearchTransactionDate":
                        retrievePOQuotationRequest();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordSearch() {
        try {
            if (poController.POQuotationRequest().Master().Industry().getDescription() != null && !"".equals(poController.POQuotationRequest().Master().Industry().getDescription())) {
                lblSource.setText(poController.POQuotationRequest().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
//            String lsSearchTransactionDate = CustomCommonUtil.formatDateToShortString(poController.POQuotationRequest().Master().getExpectedPurchaseDate());
//            dpSearchTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsSearchTransactionDate, "yyyy-MM-dd"));

            tfSearchDepartment.setText(poController.POQuotationRequest().getSearchDepartment());
            tfSearchCategory.setText(poController.POQuotationRequest().getSearchCategory());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.POQuotationRequest().getDetailCount() - 1) {
                return;
            }

            boolean lbShow = (poController.POQuotationRequest().Detail(pnDetail).getEditMode() == EditMode.UPDATE);
            JFXUtil.setDisabled(lbShow, tfBrand, tfModel, tfBarcode, tfDescription);

            String lsBrand = "";
            if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).Brand().getDescription(), null, "")) {
                lsBrand = poController.POQuotationRequest().Detail(pnDetail).Brand().getDescription();
            } else {
                lsBrand = poController.POQuotationRequest().Detail(pnDetail).Inventory().Brand().getDescription();
            }
            String lsModel = "";
            if (!JFXUtil.isObjectEqualTo(poController.POQuotationRequest().Detail(pnDetail).Model().getDescription(), null, "")) {
                lsModel = poController.POQuotationRequest().Detail(pnDetail).Model().getDescription();
            } else {
                lsModel = poController.POQuotationRequest().Detail(pnDetail).Inventory().Model().getDescription();
            }
            tfBrand.setText(lsBrand);
            tfModel.setText(lsModel);
            tfBarcode.setText(poController.POQuotationRequest().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.POQuotationRequest().Detail(pnDetail).getDescription());
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotationRequest().Detail(pnDetail).Inventory().getCost(), true));
            tfQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotationRequest().Detail(pnDetail).getQuantity(), false));

            cbReverse.setSelected(poController.POQuotationRequest().Detail(pnDetail).isReverse());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        try {
            boolean lbShow = (pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(!lbShow, dpTransactionDate);

            JFXUtil.setStatusValue(lblStatus, POQuotationRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.POQuotationRequest().Master().getTransactionStatus());

            // Transaction Date
            tfTransactionNo.setText(poController.POQuotationRequest().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.POQuotationRequest().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            String lsExpectedDate = CustomCommonUtil.formatDateToShortString(poController.POQuotationRequest().Master().getExpectedPurchaseDate());
            dpExpectedDate.setValue(JFXUtil.isObjectEqualTo(lsExpectedDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsExpectedDate, "yyyy-MM-dd"));

            tfReferenceNo.setText(poController.POQuotationRequest().Master().getReferenceNo());
            tfDepartment.setText(poController.POQuotationRequest().Master().Department().getDescription());
            tfDestination.setText(poController.POQuotationRequest().Master().Destination().getBranchName());
            tfCategory.setText(poController.POQuotationRequest().Master().Category2().getDescription());

            taRemarks.setText(poController.POQuotationRequest().Master().getRemarks());

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelPOQuotationRequest_Main selected = (ModelPOQuotationRequest_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                psTransactionNo = poController.POQuotationRequest().POQuotationRequestList(pnMain).getTransactionNo();
                poJSON = poController.POQuotationRequest().OpenTransaction(psTransactionNo);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
            }
            Platform.runLater(() -> {
                loadTableDetail.reload();
            });

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.POQuotationRequest().ReloadDetail();
                            }
                            String lsBarcode = "";
                            String lsDescription = "";
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.POQuotationRequest().getDetailCount(); lnCtr++) {
                                if (poController.POQuotationRequest().Detail(lnCtr).isReverse()) {
                                    if (poController.POQuotationRequest().Detail(lnCtr).getStockId() != null) {
                                        lsBarcode = poController.POQuotationRequest().Detail(lnCtr).Inventory().getBarCode();
                                        lsDescription = poController.POQuotationRequest().Detail(lnCtr).getDescription();
                                    }
                                    lnRowCount += 1;
                                    double lnTotal = poController.POQuotationRequest().Detail(lnCtr).getQuantity() * poController.POQuotationRequest().Detail(lnCtr).Inventory().getCost().doubleValue();
                                    details_data.add(
                                            new ModelPOQuotationRequest_Detail(
                                                    String.valueOf(lnRowCount),
                                                    String.valueOf(lsBarcode),
                                                    lsDescription,
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotationRequest().Detail(lnCtr).Inventory().getCost(), true)),
                                                    String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotationRequest().Detail(lnCtr).getQuantity(), false)),
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(lnTotal), true), String.valueOf(lnCtr)
                                            ));
                                    lsBarcode = "";
                                    lsDescription = "";
                                }
                            }

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 7); //this method is only used when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex07());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
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
                        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                        if (poController.POQuotationRequest().getPOQuotationRequestCount() > 0) {
                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.POQuotationRequest().getPOQuotationRequestCount() - 1; lnCtr++) {
                                try {
                                    main_data.add(new ModelPOQuotationRequest_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.POQuotationRequest().POQuotationRequestList(lnCtr).Department().getDescription()),
                                            String.valueOf(poController.POQuotationRequest().POQuotationRequestList(lnCtr).getTransactionDate()),
                                            String.valueOf(poController.POQuotationRequest().POQuotationRequestList(lnCtr).getTransactionNo())
                                    ));
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                }
                                if (poController.POQuotationRequest().POQuotationRequestList(lnCtr).getTransactionStatus().equals(POQuotationRequestStatus.CONFIRMED)) {
                                    JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                                }
                            }
                        }

                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                                pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                        }

                        JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                    });
                });

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpExpectedDate, dpSearchTransactionDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpExpectedDate, dpSearchTransactionDate);

    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchDepartment, tfSearchCategory);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfReferenceNo, tfDepartment, tfDestination, tfCategory, tfDepartment, tfDestination, tfCategory);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBrand, tfModel, tfBarcode, tfDescription, tfCost, tfQuantity);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCheckboxHoverCursor(apDetail);
        JFXUtil.setCommaFormatter(tfQuantity);
    }

    public void initTableOnClick() {
        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                    pnDetail = lnRow;
                    moveNext(false, false);
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.POQuotationRequest().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });

        tblViewTransDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblViewMainList); // need to use computed-size in min-width of the column to work
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelPOQuotationRequest_Main) item).getIndex01(), highlightedRowsMain);
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);

        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnUpdate, btnHistory, btnConfirm, btnVoid);

        //Unkown || Ready
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);

        switch (poController.POQuotationRequest().Master().getTransactionStatus()) {
            case POQuotationRequestStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
//                if (poController.POQuotationRequest().Master().isProcessed()) {
//                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
//                }
                break;
//            case POQuotationRequestStatus.QUOTED:
//            case POQuotationRequestStatus.SALE:
//            case POQuotationRequestStatus.LOST:
            case POQuotationRequestStatus.VOID:
            case POQuotationRequestStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid);
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        tblViewTransDetails.setItems(details_data);
        tblViewTransDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblDepartment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);

    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView currentTable = (TableView) event.getSource();
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();
            int lnRow = 0;
            if (focusedCell != null) {
                switch (event.getCode()) {
                    case TAB:
                    case DOWN:
                        lnRow = Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07());
                        pnDetail = lnRow;
                        break;
                    case UP:
                        lnRow = Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                        pnDetail = lnRow;
                        break;

                    default:
                        break;
                }
                loadRecordDetail();
                event.consume();
            }
        }
    }

    public void clearTextFields() {
        psSearchClientId = "";
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }

}
