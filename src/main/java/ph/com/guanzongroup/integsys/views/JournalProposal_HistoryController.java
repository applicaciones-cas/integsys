package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.JournalProposal;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.JournalProposalStatus;
import ph.com.guanzongroup.integsys.model.ModelJournalEntryProposal_Detail;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class JournalProposal_HistoryController implements Initializable, ScreenInterface {

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
    private int pnDetailJE = 0;

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntryProposal_Detail> journalproposal_data = FXCollections.observableArrayList();
    JFXUtil.ReloadableTableTask loadTableDetail;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apJournalProposalMaster, apJournalProposalDetails;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchDepartment, tfSearchTransactionNo, tfJournalProposalTransactionNo, tfTotalProposalDebitAmount, tfTotalProposalCreditAmount, tfJournalProposalBranch, tfJournalProposalDepartment, tfJournalProposalSrcNo, tfJournalProposalSrcCd, tfJournalProposalAccountCode, tfJournalProposalAccountDescription, tfJournalProposalDebitAmount, tfJournalProposalCreditAmount;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private DatePicker dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear;
    @FXML
    private TextArea taJournalProposalRemarks;
    @FXML
    private CheckBox cbJEProposalReverse;
    @FXML
    private TableView tblVwJournalProposalDetails;
    @FXML
    private TableColumn tblJournalProposalRowNo, tblJournalProposalReportMonthYear, tblJournalProposalAccountCode, tblJournalProposalAccountDescription, tblJournalProposalDebitAmount, tblJournalProposalCreditAmount;

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
            initLoadTable();
            initTextFields();
            initDatePicker();
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
                    lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
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
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear);
        JFXUtil.setActionListener(datepicker_Action, dpJournalProposalTransactionDate, dpJournalProposalReportMonthYear);
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
                            loadTableDetail.reload();
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
                case "btnBrowse":
                    poController.Master().setIndustryCode(psIndustryId);
                    poController.Master().setCompanyId(psCompanyId);
                    poController.Master().setBranchCode(oApp.getBranchCode());
                    poJSON = poController.SearchTransaction(tfSearchDepartment.getText(), tfSearchTransactionNo.getText());
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    pnEditMode = poController.getEditMode();
                    loadTableDetail.reload();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
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
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered, Please contact admin to assist about the unregistered button");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnPost", "btnSave", "btnCancel", "btnVoid")) {
                poController.InitTransaction();
                clearTextFields();
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve", "btnSearch", "btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
            }
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        tfSearchDepartment.setText(poController.getSearchDepartmentName());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMaster() {
        try {
            JFXUtil.setStatusValue(lblStatus, JournalProposalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());
            tfJournalProposalTransactionNo.setText(poController.Master().getTransactionNo());
            dpJournalProposalTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
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
            tfJournalProposalSrcNo.setText(poController.Master().Disbursement().getVoucherNo());
            tfJournalProposalSrcCd.setText(poController.getSourceDesc());

            JFXUtil.updateCaretPositions(apJournalProposalMaster);
        } catch (SQLException | GuanzonException ex) {
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

            JFXUtil.updateCaretPositions(apJournalProposalDetails);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initDetailJEGrid() {
        JFXUtil.setColumnCenter(tblJournalProposalRowNo, tblJournalProposalReportMonthYear);
        JFXUtil.setColumnLeft(tblJournalProposalAccountCode, tblJournalProposalAccountDescription);
        JFXUtil.setColumnRight(tblJournalProposalDebitAmount, tblJournalProposalCreditAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwJournalProposalDetails);
        tblVwJournalProposalDetails.setItems(journalproposal_data);
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
    }

    private void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblVwJournalProposalDetails,
                journalproposal_data,
                () -> {
                    Platform.runLater(() -> {
                        journalproposal_data.clear();
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
                                journalproposal_data.add(
                                        new ModelJournalEntryProposal_Detail(
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
                            int lnTempRow = JFXUtil.getDetailRow(journalproposal_data, pnDetailJE, 07); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= journalproposal_data.size()) {
                                if (!journalproposal_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblVwJournalProposalDetails, 0);
                                    int lnRow = Integer.parseInt(journalproposal_data.get(0).getIndex07());
                                    pnDetailJE = lnRow;
                                    loadRecordDetailJE();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnDetailBIR POINTS TO */
                                JFXUtil.selectAndFocusRow(tblVwJournalProposalDetails, lnTempRow);
                                int lnRow = Integer.parseInt(journalproposal_data.get(tblVwJournalProposalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                                pnDetailJE = lnRow;
                                loadRecordDetailJE();
                            }
                            loadRecordMaster();
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

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = (lsTxtField.getText() == null ? "" : lsTxtField.getText()); //IMPORTANT: as searchRecord does not have null validator in searching instead ""
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        CommonUtils.SetNextFocus(lsTxtField);
                        event.consume();
                        break;
                    case F3:
                        switch (txtFieldID) {
                            //apBrowse
                            case "tfSearchDepartment":
                                poJSON = poController.SearchDepartment(tfSearchDepartment.getText(), false, true);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                loadRecordSearch();
                                break;
                            case "tfSearchTransactionNo":
                                poJSON = poController.SearchTransaction(tfSearchDepartment.getText(), tfSearchTransactionNo.getText());
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                loadRecordSearch();
                                loadTableDetail.reload();
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
            } catch (ExceptionInInitializerError | SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apJournalProposalMaster, apJournalProposalDetails);
        JFXUtil.setCommaFormatter(tfJournalProposalDebitAmount, tfJournalProposalCreditAmount);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwJournalProposalDetails);
        JFXUtil.adjustColumnForScrollbar(tblVwJournalProposalDetails);
    }

    public void initTableOnClick() {
        tblVwJournalProposalDetails.setOnMouseClicked(event -> {
            if (!journalproposal_data.isEmpty() && event.getClickCount() == 1) {
                int lnRow = Integer.parseInt(journalproposal_data.get(tblVwJournalProposalDetails.getSelectionModel().getSelectedIndex()).getIndex07());
                pnDetailJE = lnRow;
                loadRecordDetailJE();
                moveNextJE(false, false);
            }
        });
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwJournalProposalDetails":
                    if (journalproposal_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(journalproposal_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex07())
                            : Integer.parseInt(journalproposal_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex07());
                    pnDetailJE = newIndex;
                    loadRecordDetailJE();
                    break;
            }
        }
    };

    public void moveNextJE(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apJournalProposalDetails.requestFocus();
                pnDetailJE = isUp ? Integer.parseInt(journalproposal_data.get(JFXUtil.moveToPreviousRow(tblVwJournalProposalDetails)).getIndex07())
                        : Integer.parseInt(journalproposal_data.get(JFXUtil.moveToNextRow(tblVwJournalProposalDetails)).getIndex07());
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
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow2 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow2, btnClose);

        JFXUtil.setDisabled(true, apJournalProposalMaster, apJournalProposalDetails);
        JFXUtil.setButtonsVisibility(true, btnBrowse);
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apJournalProposalMaster, apJournalProposalDetails);
    }
}
