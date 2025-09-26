/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.CheckBox;
import org.guanzon.appdriver.base.GRiderCAS;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
import ph.com.guanzongroup.integsys.model.ModelPOQuotationRequest_Detail;

/**
 *
 * @author Team 2
 */
public class POQuotationRequest_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    boolean lsIsSaved = false;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    static QuotationControllers poController;
    public int pnEditMode;
    boolean pbKeyPressed = false;
    String psSearchClientId = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private ObservableList<ModelPOQuotationRequest_Detail> details_data = FXCollections.observableArrayList();

    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private boolean pbEntered = false;

    JFXUtil.ReloadableTableTask loadTableDetail;

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
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblQuantityDetail, tblTotalDetail;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

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
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        pnEditMode = poController.POQuotationRequest().getEditMode();
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poController.POQuotationRequest().Master().setIndustryId(psIndustryId);
            poController.POQuotationRequest().Master().setCategoryCode(psCategoryId);
            poController.POQuotationRequest().setIndustryId(psIndustryId);
            poController.POQuotationRequest().setCompanyId(psCompanyId);
            poController.POQuotationRequest().setCategoryId(psCategoryId);
            poController.POQuotationRequest().setWithUI(true);
            loadRecordSearch();
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

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                        String inputText = JFXUtil.isObjectEqualTo(dpSearchTransactionDate.getEditor().getText(), "") ? "01/01/1900" : dpSearchTransactionDate.getEditor().getText();
                        String lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                        LocalDate selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                        poJSON = poController.POQuotationRequest().searchTransaction(oApp.getBranchName(),
                                tfSearchDepartment.getText(), tfSearchCategory.getText(),
                                lsSelectedDate, tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.POQuotationRequest().getEditMode();
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

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                }
                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    moveNext(false, false);
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        try {
            boolean lbShow = (pnEditMode == EditMode.UPDATE);
            JFXUtil.setDisabled(lbShow, tfBrand, tfModel, tfBarcode, tfDescription);

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

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.POQuotationRequest().getDetailCount() - 1) {
                return;
            }
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

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
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

        tblViewTransDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails); // need to use computed-size in min-width of the column to work
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

                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 7); //this method is used only when Reverse is applied
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
    }

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
            }, tfBrand); // default
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
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfBarcode":
                        case "tfDescription":
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
                            return;
                        case "tfSearchReferenceNo":
                            SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                            String inputText = JFXUtil.isObjectEqualTo(dpSearchTransactionDate.getEditor().getText(), "") ? "01/01/1900" : dpSearchTransactionDate.getEditor().getText();
                            String lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            LocalDate selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            poJSON = poController.POQuotationRequest().searchTransaction(poController.POQuotationRequest().getSearchBranch(),
                                    poController.POQuotationRequest().getSearchDepartment(), poController.POQuotationRequest().getSearchCategory(),
                                    lsSelectedDate, tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                break;
                            } else {
                                pnEditMode = poController.POQuotationRequest().getEditMode();
                                loadRecordMaster();
                                loadTableDetail.reload();
                                initButton(pnEditMode);
                            }
                            loadRecordSearch();
                            return;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

//        try {
        Object source = event.getSource();
        if (source instanceof DatePicker) {
            DatePicker datePicker = (DatePicker) source;
            String inputText = datePicker.getEditor().getText();
            SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);

            if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                return;
            }
            switch (datePicker.getId()) {
                case "dpSearchTransactionDate":
                    break;
                default:
                    break;
            }
        }
//        } catch (SQLException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//        }
    }

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

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpExpectedDate, dpSearchTransactionDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpExpectedDate, dpSearchTransactionDate);

    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchDepartment, tfSearchCategory, tfSearchReferenceNo);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCheckboxHoverCursor(apDetail);
    }

    private void initButton(int fnValue) {
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow2, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(true, taRemarks, apMaster, apDetail);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        tblViewTransDetails.setItems(details_data);
        tblViewTransDetails.autosize();
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

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }
}
