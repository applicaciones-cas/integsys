package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
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
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;
import ph.com.guanzongroup.integsys.model.ModelCashAdvance;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1 : Aldrich & Arsiela 02032026
 */
public class CashAdvance_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashAdvance poController;
    private JSONObject poJSON;
    public int pnEditMode;

    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private static final int ROWS_PER_PAGE = 50;
    int pnMain = 0;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private ObservableList<ModelCashAdvance> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCashAdvance> filteredData;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();

    JFXUtil.ReloadableTableTask loadTableMain;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchPayee, tfSearchVoucherNo, tfTransactionNo, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnHistory, btnRetrieve, btnClose;
    @FXML
    private DatePicker dpAdvanceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblVoucherNo, tblDate, tblPayee, tblRequestingDepartment;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poJSON = new JSONObject();
        poController = new CashflowControllers(oApp, null).CashAdvance();
        poController.initialize(); // Initialize transaction
        initLoadTable();
        initTextFields();
        initDatePickers();
        clearTextFields();
        initMainGrid();
        initTableOnClick();
        pgPagination.setPageCount(1);
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        Platform.runLater(() -> {
            poController.getModel().setIndustryId(psIndustryId);
            poController.getModel().setCompanyId(psCompanyId);
            poController.setIndustryId(psIndustryId);
            poController.setCompanyId(psCompanyId);
            poController.setWithUI(true);
            loadRecordSearch();
        });
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
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
        //No Category
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelCashAdvance selected = (ModelCashAdvance) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = poController.OpenTransaction(poController.CashAdvanceList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                loadRecordMaster();
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelCashAdvance) item).getIndex01(), highlightedRowsMain);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = txtField.getId();
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnMain;

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchIndustry":
                            poJSON = poController.SearchIndustry(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchIndustry.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrieveCashAdvance();
                            return;
                        case "tfSearchPayee":
                            poJSON = poController.SearchPayee(lsValue, false, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchPayee.setText("");
                                break;
                            }
                            loadRecordSearch();
                            retrieveCashAdvance();
                            return;
                        case "tfSearchVoucherNo":
                            retrieveCashAdvance();
                            return;
                        case "tfPayee":
                            poJSON = poController.SearchPayee(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfPayee.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfCreditedTo);
                            }
                            loadRecordMaster();
                            break;
                        case "tfCreditedTo":
                            poJSON = poController.SearchCreditedTo(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfCreditedTo.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfRequestingDepartment);
                            }
                            loadRecordMaster();
                            break;
                        case "tfRequestingDepartment":
                            poJSON = poController.SearchDepartment(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfRequestingDepartment.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfAmountToAdvance);
                            }
                            loadRecordMaster();
                            break;

                    }
                    break;
            }
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordSearch() {
        try {
            poController.getModel().setCompanyId(psCompanyId);
            if (poController.getModel().Company().getCompanyName() != null && !"".equals(poController.getModel().Company().getCompanyName())) {
                lblSource.setText(poController.getModel().Company().getCompanyName());
            } else {
                lblSource.setText("");
            }
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchPayee.setText(poController.getSearchPayee());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                        }
                        break;
                }
                loadTableMain.reload();
                loadRecordSearch();
            });
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfVoucherNo":
                        poJSON = poController.getModel().setVoucher(lsValue);
                        break;
                    case "tfPayee":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.getModel().setClientId("");
                            poJSON = poController.getModel().setPayeeName("");
                        }
                        break;
                    case "tfCreditedTo":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.getModel().setCreditedTo("");
                        }
                        break;
                    case "tfRequestingDepartment":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.getModel().setDepartmentRequest("");
                        }
                        break;
                    case "tfAmountToAdvance":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setAdvanceAmount(Double.valueOf(lsValue));
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.getModel().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordMaster();
                        break;
                }
            });

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
                LocalDate currentDate = null, transactionDate = null, referenceDate = null, selectedDate = null;
                String lsServerDate = "", lsTransDate = "", lsRefDate = "", lsSelectedDate = "";

                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }

                lsServerDate = sdfFormat.format(oApp.getServerDate());
                currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpAdvanceDate":

                        if (selectedDate.isAfter(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Future dates are not allowed.");
                            pbSuccess = false;
                        }

                        if (pbSuccess) {
                            poController.getModel().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }

                        pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                        loadRecordMaster();
                        pbSuccess = true; //Set to original value
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchIndustry, tfSearchPayee, tfSearchVoucherNo);
        JFXUtil.setFocusListener(txtMaster_Focus, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        JFXUtil.setCommaFormatter(tfAmountToAdvance);

        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAdvanceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpAdvanceDate);
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblVoucherNo, tblDate);
        JFXUtil.setColumnLeft(tblPayee, tblRequestingDepartment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(100);
                            main_data.clear();
                            JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);

                            if (poController.getCashAdvanceCount() > 0) {
                                //retreiving using column index
                                for (int lnCtr = 0; lnCtr <= poController.getCashAdvanceCount() - 1; lnCtr++) {
                                    main_data.add(new ModelCashAdvance(String.valueOf(lnCtr + 1),
                                            CustomCommonUtil.formatDateToShortString(poController.CashAdvanceList(lnCtr).getTransactionDate()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getVoucher()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getPayeeName()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).Department().getDescription())
                                    ));

                                    if (poController.CashAdvanceList(lnCtr).getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
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
                        } catch (InterruptedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });

                });
    }

    public void loadRecordMaster() {
        try {
            lblStatus.setText(poController.getStatus(poController.getModel().getTransactionStatus()).toUpperCase());
            tfTransactionNo.setText(poController.getModel().getTransactionNo());

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.getModel().getTransactionDate());
            dpAdvanceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfVoucherNo.setText(poController.getModel().getVoucher());
            tfPayee.setText(poController.getModel().Payee().getPayeeName());
            tfCreditedTo.setText(poController.getModel().Credited().getCompanyName());
            tfRequestingDepartment.setText(poController.getModel().Department().getDescription());
            tfAmountToAdvance.setText("");
            taRemarks.setText(poController.getModel().getRemarks());
            tfAmountToAdvance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getAdvanceAmount().doubleValue(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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
                    case "btnUpdate":
                        poJSON = poController.OpenTransaction(poController.getModel().getTransactionNo());
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster);
                        break;

                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            //Clear data
                            clearTextFields();
                            poController.resetMaster();
                            poController.initFields();
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
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
                    case "btnRetrieve":
                        retrieveCashAdvance();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                //reshow the highlight
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.OpenTransaction(poController.getModel().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.getModel().getTransactionStatus().equals(CashAdvanceStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Warning((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                pnEditMode = poController.getEditMode();
                            }
                        } else {
                            return;
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
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poController.ConfirmTransaction("");
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
                            if (CashAdvanceStatus.CONFIRMED.equals(poController.getModel().getTransactionStatus())) {
                                poJSON = poController.CancelTransaction("");
                            } else {
                                poJSON = poController.VoidTransaction("");
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
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnVoid", "btnCancel")) {
                    clearTextFields();
                    poController.resetMaster();
                    pnEditMode = EditMode.UNKNOWN;
                }

                if (lsButton.equals("btnRetrieve")) {
                } else {
                    loadRecordMaster();
                }
                initButton(pnEditMode);
            }
        } catch (ParseException | SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrieveCashAdvance() {
        poJSON = new JSONObject();
        poController.setRecordStatus(CashAdvanceStatus.OPEN + CashAdvanceStatus.CONFIRMED);
        poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchVoucherNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
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
        JFXUtil.setDisabled(!lbShow1, apMaster);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);

        switch (poController.getModel().getTransactionStatus()) {
            case CashAdvanceStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                break;
            case CashAdvanceStatus.RELEASED:
            case CashAdvanceStatus.VOID:
            case CashAdvanceStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid);
                break;
        }
    }
}
