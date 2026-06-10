package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.JournalProposal;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status. JournalProposalStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class JournalProposal_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JournalProposal poController;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryId = "";
    private String psCompanyId = "";
    private int pnMain = 0;
    private int pnDetail = 0;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private int pnDetailJE = 0;
    private int currentIndex = 0;
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> journal_data = FXCollections.observableArrayList();
    Scene scene = null;
    private boolean pbEnteredJE = false;
    private FilteredList<ModelTableMain> filteredData;
    private boolean tooltipShown = false;
    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetailJE;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private String psSearchDate = "";

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apJournalMaster, apJournalDetails;
    @FXML
    private TextField tfJournalProposalTransactionNo, tfJournalProposalBranch, tfJournalProposalDepartment, tfJournalProposalDVNo, tfTotalProposalDebitAmount, tfTotalProposalCreditAmount, tfJournalProposalAccountCode, tfJournalProposalAccountDescription, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount, tfSearchDepartment, tfsearchTransactionNo;
    @FXML
    private DatePicker dpJournalTransactionDate, dpJournalProposalReportMonthYear;
    @FXML
    private Label lblSource, lblJournalTransactionStatus;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnApprove, btnVoid, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextArea taJournalProposalRemarks;
    @FXML
    private CheckBox cbJEProposalReverse;
    @FXML
    private TableView tblVwJournalDetails, tblViewMain;
    @FXML
    private TableColumn tblJournalRowNo, tblJournalReportMonthYear, tblJournalAccountCode, tblJournalAccountDescription, tblJournalDebitAmount, tblJournalCreditAmount, tblRowNo, tblDate, tblTransNo,tblDVNo,tblDepartment;
    
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poController = new CashflowControllers(oApp, logwrapr).JournalProposal();
            poJSON = new JSONObject();
            poController.setWithParent(false);
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            poController.setTransactionStatus("01");
            initLoadTable();
            initTextFields();
            initDatePicker();
            initMainGrid();
            initDetailJEGrid();
            initTableOnClick();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            Platform.runLater(() -> {
                try {
                    poController.Master().setIndustryCode(psIndustryId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.setIndustryId(psIndustryId);
                    poController.setCompanyId(psCompanyId);
                    loadRecordSearch();
                    lblSource.setText(poController.Master().Company().getCompanyName()+ " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private boolean DoesContainValidDetail() {
        String lsParticular = "";
        if (poController.getDetailCount() <= 0) {
            return false;
        }
        return true;
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpJournalTransactionDate, dpJournalProposalReportMonthYear);
        JFXUtil.setActionListener(datepicker_Action, dpJournalTransactionDate, dpJournalProposalReportMonthYear);
    }

    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                String lsTransDate;
                LocalDate transactionDate;
                switch (datePicker.getId()) {
                    case "dpJournalProposalReportMonthYear":
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lsTransDate = sdfFormat.format(poController.Master().getTransactionDate());
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            
                            if (ldSelectedDate.isAfter(ldCurrentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }
                            
                            if (pbSuccess && (ldSelectedDate.isAfter(transactionDate))) {
                                JFXUtil.setJSONError(poJSON, "Report date cannot be later than the transaction date.");
                                pbSuccess = false;
                            }
                            
                            if (pbSuccess) {
                                poController.Detail(pnDetailJE).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadTableDetailJE.reload();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:
                        break;
                }
            });

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poController.UpdateTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse,  apJournalDetails);
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the transaction?")) {
                        return;
                    }
                    poJSON = poController.SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    poJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                    if ("success".equals(poJSON.get("result"))) {
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                    }
                    if (pnEditMode == EditMode.READY) {
                        if ( JournalProposalStatus.OPEN.equals(poController.Master().getTransactionStatus())) {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) { //requires to review journal entry
                                poJSON = poController.ConfirmTransaction("");
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                }
                            }
                        }
                    }
                    JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                        JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                        pnEditMode = EditMode.UNKNOWN;
                        break;
                    } else {
                        return;
                    }
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning(null, pxeModuleName, "No transaction status history to load!");
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnPost":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to post transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            poJSON = poController.ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    String lsStat = "";
                    switch (poController.Master().getTransactionStatus()) {
                        case  JournalProposalStatus.OPEN:
                            lsStat = "void";
                            break;
                        case  JournalProposalStatus.CONFIRMED:
                            lsStat = "cancel";
                            break;
                    }

                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to " + lsStat + " the transaction?")) {
                        pnEditMode = poController.getEditMode();
                        if (pnEditMode == EditMode.READY) {
                            switch (poController.Master().getTransactionStatus()) {
                                case  JournalProposalStatus.OPEN:
                                    poJSON = poController.VoidTransaction("");
                                    break;
                                case  JournalProposalStatus.CONFIRMED:
                                    poJSON = poController.CancelTransaction("");
                                    break;
                            }

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                pnEditMode = poController.getEditMode();
                                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered, Please contact admin to assist about the unregistered button");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnPost", "btnSave", "btnCancel", "btnVoid")) {
                poController.InitTransaction();
                clearTextFields();
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton,  "btnRetrieve", "btnSearch", "btnHistory")) {
            } else {
                loadRecordMasterJE();
                loadTableDetailJE.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNextJE(false, false);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        tfSearchDepartment.setText(poController.getSearchDepartmentName());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMasterJE() {
        try {
            String lsStat = "";
            switch (poController.Master().getTransactionStatus()) {
                case  JournalProposalStatus.OPEN:
                    lsStat = "Void";
                    break;
                case  JournalProposalStatus.CONFIRMED:
                    lsStat = "Cancel";
                    break;
            }
            btnVoid.setText(lsStat);
            JFXUtil.setStatusValue(lblJournalTransactionStatus, JournalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfJournalProposalTransactionNo.setText(poController.Master().getTransactionNo());
            dpJournalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            double lnTotalDebit = 0;
            double lnTotalCredit = 0;
            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                if (!poController.Detail(lnCtr).isReverse()) {
                    continue;
                }
                lnTotalDebit += poController.Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poController.Detail(lnCtr).getCreditAmount();
            }
            tfTotalProposalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            tfTotalProposalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            taJournalProposalRemarks.setText(poController.Master().getRemarks());
            tfJournalProposalBranch.setText(poController.Master().Branch().getBranchName());
            tfJournalProposalDepartment.setText(poController.Master().Department().getDescription());
            tfJournalProposalDVNo.setText(poController.Master().Disbursement().getVoucherNo());
            
            JFXUtil.updateCaretPositions(apJournalMaster);
        } catch ( SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        } 
    }

    public void loadRecordDetailJE() {
        try {
            if (pnDetailJE < 0 || pnDetailJE > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbShow = poController.Detail(pnDetailJE).getEditMode() == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfJournalProposalAccountCode, tfJournalProposalAccountDescription);

            cbJEProposalReverse.setSelected(poController.Detail(pnDetailJE).isReverse());

            tfJournalProposalAccountCode.setText(poController.Detail(pnDetailJE).getAccountCode());
            tfJournalProposalAccountDescription.setText(poController.Detail(pnDetailJE).Account_Chart().getDescription());
            String lsReportMonth = CustomCommonUtil.formatDateToShortString(poController.Detail(pnDetailJE).getForMonthOf());
            JFXUtil.setDateValue(dpJournalProposalReportMonthYear, CustomCommonUtil.parseDateStringToLocalDate(lsReportMonth, "yyyy-MM-dd"));
            tfJournalProposalDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetailJE).getDebitAmount(), true));
            tfJournalProposalCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetailJE).getCreditAmount(), true));

            JFXUtil.updateCaretPositions(apJournalDetails);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblTransNo,tblDVNo);
        JFXUtil.setColumnLeft(tblDepartment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMain);

        tblViewMain.setItems(main_data);
    }

    private void initDetailJEGrid() {
        JFXUtil.setColumnCenter(tblJournalRowNo, tblJournalReportMonthYear);
        JFXUtil.setColumnLeft(tblJournalAccountCode, tblJournalAccountDescription);
        JFXUtil.setColumnRight(tblJournalDebitAmount, tblJournalCreditAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalDetails);
        tblVwJournalDetails.setItems(journal_data);
    }

    private void loadTableDetailFromMain() {
        poJSON = new JSONObject();

        pnMain = tblViewMain.getSelectionModel().getSelectedIndex();
        ModelTableMain selected = (ModelTableMain) tblViewMain.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                String lsTransactionNo = selected.getIndex02();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, poController.Master().getTransactionNo(), lsTransactionNo)) {
                    return;
                }
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMain, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMain, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
                JFXUtil.clearTextFields(apJournalMaster);
                poJSON = poController.OpenTransaction(lsTransactionNo);
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    poController.InitTransaction();
                    return;
                }
                pnEditMode = poController.getEditMode();
                loadTableDetailJE.reload();
                moveNextJE(false, false);
            } catch (CloneNotSupportedException | SQLException | GuanzonException  ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }

    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbJEProposalReverse":
                    if (poController.Detail(pnDetailJE).getEditMode() == EditMode.ADDNEW) {
                        poController.Detail().remove(pnDetailJE);
                    } else {
                        poController.Detail(pnDetailJE).isReverse(cbJEProposalReverse.isSelected());
                    }
                    loadRecordMasterJE();
                    loadTableDetailJE.reload();
                    if (checkedBox.isSelected()) {
                        moveNextJE(false, false);
                    }
                    break;
            }
        }
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMain,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            JFXUtil.disableAllHighlight(tblViewMain, highlightedRowsMain);
                            poJSON = poController.loadTransactionList(tfSearchDepartment.getText(), tfsearchTransactionNo.getText());
                            if ("success".equals(poJSON.get("result"))) {
                                if (poController.getTransactionList().size() > 0) {
                                    for (int lnCntr = 0; lnCntr < poController.getTransactionList().size() - 1; lnCntr++) {
                                        try {
                                            main_data.add(new ModelTableMain(
                                                    String.valueOf(lnCntr + 1),
                                                    poController.TransactionList(lnCntr).getTransactionNo(),
                                                    CustomCommonUtil.formatDateToShortString(poController.TransactionList(lnCntr).getTransactionDate()),
                                                    poController.TransactionList(lnCntr).Disbursement().getVoucherNo(),
                                                    poController.TransactionList(lnCntr).Department().getDescription(),
                                                    "", "", "", "", ""
                                            ));
                                            if (poController.TransactionList(lnCntr).getTransactionStatus().equals( JournalProposalStatus.VOID)) {
                                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(lnCntr + 1), "#FAA0A0", highlightedRowsMain);
                                            }
                                            if (poController.TransactionList(lnCntr).getTransactionStatus().equals( JournalProposalStatus.CONFIRMED)) {
                                                JFXUtil.highlightByKey(tblViewMain, String.valueOf(lnCntr + 1), "#C1E1C1", highlightedRowsMain);
                                            }
                                        } catch (SQLException | GuanzonException ex) {
                                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                                        }
                                    }
                                } else {
                                    main_data.clear();
                                }
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });

        loadTableDetailJE = new JFXUtil.ReloadableTableTask(
                tblVwJournalDetails,
                journal_data,
                () -> {
                    Platform.runLater(() -> {
                        journal_data.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            String lsReportMonthYear = "";
                            String lsAcctCode = "";
                            String lsAccDesc = "";
                            int lnRowCount = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poController.Detail(lnCtr).getForMonthOf());
                                lsAcctCode = poController.Detail(lnCtr).getAccountCode();
                                lsAccDesc = poController.Detail(lnCtr).Account_Chart().getDescription();
                                if (lsAcctCode == null) {
                                    lsAcctCode = "";
                                }
                                if (lsAccDesc == null) {
                                    lsAccDesc = "";
                                }
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                lnRowCount += 1;
                                journal_data.add(
                                        new ModelJournalEntry_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd")),
                                                String.valueOf(lsAcctCode),
                                                String.valueOf(lsAccDesc),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getDebitAmount(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getCreditAmount(), true)),
                                                String.valueOf(lnCtr)
                                        ));

                                lsReportMonthYear = "";
                                lsAcctCode = "";
                                lsAccDesc = "";
                            }
                            int lnTempRow = JFXUtil.getDetailRow(journal_data, pnDetailJE, 07); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= journal_data.size()) {
                                if (!journal_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwJournalDetails, 0);
                                    int lnRow = Integer.parseInt(journal_data.get(0).getIndex07());
                                    pnDetailJE = lnRow;
                                    loadRecordDetailJE();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwJournalDetails, lnTempRow);
                                int lnRow = Integer.parseInt(journal_data.get(tblVwJournalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailJE = lnRow;
                                loadRecordDetailJE();
                            }
                            loadRecordMasterJE();
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBankAccountNo":
                        if (lsValue.isEmpty()) {
                            poController.setSearchDepartmentName("");
                        }
                        loadRecordSearch();
                        break;
                }

            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taJournalProposalRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMasterJE();
                        break;
                }
            });
    ChangeListener<Boolean> txtDetailJE_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfJournalProposalAccountCode":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfJournalProposalAccountDescription":
                        if (lsValue.isEmpty()) {
                            poController.Detail(pnDetailJE).setAccountCode("");
                            loadTableDetailJE.reload();
                        }
                        break;
                    case "tfJournalProposalDebitAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Detail(pnDetailJE).getCreditAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Detail(pnDetailJE).setDebitAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                            break;
                        } else {
                            poJSON = poController.Detail(pnDetailJE).setDebitAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJE = lnReturned;
                                    loadTableDetailJE.reload();
                                });
                                return;
                            } else {
                            }
                        }
                        break;
                    case "tfJournalProposalCreditAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.Detail(pnDetailJE).getDebitAmount() > 0.0000 && Double.parseDouble(lsValue) > 0) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                            poController.Detail(pnDetailJE).setCreditAmount(0.0000);
                            JFXUtil.textFieldMoveNext(tfJournalProposalCreditAmount);
                            break;
                        } else {
                            poJSON = poController.Detail(pnDetailJE).setCreditAmount((Double.parseDouble(lsValue)));
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.70, () -> {
                                    pnDetailJE = lnReturned;
                                    loadTableDetailJE.reload();
                                });
                                return;
                            }
                        }
                        if (pbEnteredJE) {
                            JFXUtil.runWithDelay(0.50, () -> {
                                loadTableDetailJE.reload();
                                JFXUtil.runWithDelay(0.50, () -> {
                                    moveNextJE(false, true);
                                });
                                pbEnteredJE = false;
                            });
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetailJE.reload();
                });
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = (lsTxtField.getText() == null ? "" : lsTxtField.getText()); //IMPORTANT: as searchRecord does not have null validator in searching instead ""
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        if (tfJournalProposalCreditAmount.isFocused()) {
                            pbEnteredJE = true;
                        }
                        CommonUtils.SetNextFocus(lsTxtField);
                        event.consume();
                        break;
                    case F3:
                        switch (txtFieldID) {
                            //apBrowse
                            case "tfSearchDepartment":
                                poJSON = poController.SearchDepartment(tfSearchDepartment.getText(), true, true);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfsearchTransactionNo":
                                if (!tooltipShown) {
                                    JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", tfsearchTransactionNo);
                                    tooltipShown = true;
                                }
                                loadTableMain.reload();
                                break;
                            //apJournalDetails
                            case "tfJournalProposalAccountCode":
                                poJSON = poController.SearchAccountCode(pnDetailJE, lsValue, true, poController.Master().getIndustryCode(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJE = lnReturned;
                                        loadTableDetailJE.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJE = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                                }
                                break;
                            case "tfJournalProposalAccountDescription":
                                poJSON = poController.SearchAccountCode(pnDetailJE, lsValue, false, poController.Master().getIndustryCode(), null);
                                if ("error".equals(poJSON.get("result"))) {
                                    int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    JFXUtil.runWithDelay(0.70, () -> {
                                        pnDetailJE = lnReturned;
                                        loadTableDetailJE.reload();
                                    });
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    break;
                                } else {
                                    pnDetailJE = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                    loadTableDetailJE.reload();
                                    JFXUtil.textFieldMoveNext(tfJournalProposalDebitAmount);
                                }
                                break;
                        }
                        break;
                    case UP:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNextJE(true, true)},
                            {new String[]{"tfJournalProposalAccountCode", "tfJournalProposalAccountDescription", "tfJournalProposalCreditAmount"}, (Runnable) () -> moveNextJE(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNextJE(false, true)},
                            {new String[]{"tfJournalProposalAccountCode", "tfJournalProposalAccountDescription", "tfJournalProposalCreditAmount"}, (Runnable) () -> moveNextJE(false, true)}
                        });
                        event.consume();
                        break;
                    default:
                        break;
                }
            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            } 
        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus,  taJournalProposalRemarks);
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setFocusListener(txtDetailJE_Focus, apJournalDetails);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apJournalMaster, apJournalDetails);
        JFXUtil.setCommaFormatter(tfJournalProposalDebitAmount, tfJournalProposalCreditAmount);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwJournalDetails, tblViewMain);
        JFXUtil.adjustColumnForScrollbar(tblVwJournalDetails, tblViewMain);
    }

    public void initTableOnClick() {
        tblViewMain.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                pnMain = tblViewMain.getSelectionModel().getSelectedIndex();
                if (pnMain >= 0) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        tblVwJournalDetails.setOnMouseClicked(event -> {
            if (!journal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journal_data.get(tblVwJournalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJE = lnRow;
                loadRecordDetailJE();
                moveNextJE(false, false);
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMain, item -> ((ModelTableMain) item).getIndex01(), highlightedRowsMain);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwJournalDetails":
                    if (journal_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(journal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJE = newIndex;
                    loadRecordDetailJE();
                    break;
            }
        }
    };

    public void moveNextJE(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apJournalDetails.requestFocus();
                pnDetailJE = isUp ? Integer.parseInt(journal_data.get(JFXUtil.moveToPreviousRow(tblVwJournalDetails)).getIndex07())
                        : Integer.parseInt(journal_data.get(JFXUtil.moveToNextRow(tblVwJournalDetails)).getIndex07());
            }
            loadRecordDetailJE();
            if (pnDetailJE < 0 || pnDetailJE > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetailJE).getAccountCode(), tfJournalProposalAccountCode},
                {poController.Detail(pnDetailJE).Account_Chart().getDescription(), tfJournalProposalAccountDescription}, // if null or empty, then requesting focus to the txtfield
                {poController.Detail(pnDetailJE).getDebitAmount(), tfJournalProposalDebitAmount},
                {poController.Detail(pnDetailJE).getCreditAmount(), tfJournalProposalCreditAmount},}, tfJournalProposalCreditAmount); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY);
        boolean lbShow3 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow1, apJournalMaster, apJournalDetails);
        JFXUtil.setButtonsVisibility(true, btnRetrieve);
        JFXUtil.setButtonsVisibility(false, btnApprove);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.Master().getTransactionStatus()) {
            case  JournalProposalStatus.OPEN:
                JFXUtil.setButtonsVisibility(true, btnApprove);
                break;
            case  JournalProposalStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnApprove);
                break;
            case  JournalProposalStatus.VOID:
            case  JournalProposalStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid, btnApprove);
                break;
        }
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apJournalMaster, apJournalDetails);
    }
}
