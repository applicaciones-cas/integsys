package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1 : Aldrich & Arsiela 02032026
 */
public class CashAdvance_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashAdvance poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance, tfPettyCash;
    @FXML
    private DatePicker dpAdvanceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbOtherPayee, cbOtherCreditedTo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).CashAdvance();
            poController.InitTransaction(); // Initialize transaction
            poController.initFields();
            initTextFields();
            initDatePickers();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
                btnNew.fire();
            });

            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        System.out.println(fsValue);
        this.psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        System.out.println(fsValue);
        this.psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No category
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = txtField.getId();
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfPettyCash":
                            poJSON = poController.SearchPettyCash(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfPettyCash.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfCreditedTo);
                            }
                            loadRecordMaster();
                            break;
                        case "tfPayee":
                            poJSON = poController.SearchPayee(lsValue, false, cbOtherPayee.isSelected());
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
                            poJSON = poController.SearchCreditedTo(lsValue, false, cbOtherCreditedTo.isSelected());
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

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfVoucherNo":
                        poJSON = poController.Master().setVoucher(lsValue);
                        break;
                    case "tfPayee":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.Master().setClientId("");
                            poJSON = poController.Master().setPayeeName("");
                        }
                        break;
                    case "tfCreditedTo":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.Master().setCreditedTo("");
                        }
                        break;
                    case "tfRequestingDepartment":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.Master().setDepartmentRequest("");
                        }
                        break;
                    case "tfAmountToAdvance":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.Master().setAdvanceAmount(Double.valueOf(lsValue));
                        break;
                }
                JFXUtil.runWithDelay(0.8, () -> {
                    loadRecordMaster();
                });
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
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
                            poController.Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
        JFXUtil.setFocusListener(txtMaster_Focus, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
        JFXUtil.setCommaFormatter(tfAmountToAdvance);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAdvanceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpAdvanceDate);
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(lastFocusedTextField, previousSearchedTextField);
        JFXUtil.clearTextFields(apMaster);
    }

    public void loadRecordSearch() {
        try {
            if (poController.Master().Industry().getDescription() != null && !"".equals(poController.Master().Industry().getDescription())) {
                lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            JFXUtil.setDisabled(true, dpAdvanceDate);
            lblStatus.setText(pnEditMode == EditMode.UNKNOWN ? "UNKNOWN" : poController.getStatus(poController.Master().getTransactionStatus()).toUpperCase());
            tfTransactionNo.setText(poController.Master().getTransactionNo());

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTransactionDate());
            dpAdvanceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfVoucherNo.setText(poController.Master().getVoucher());
            tfPettyCash.setText(poController.Master().PettyCash().getPettyCashDescription());
            tfPayee.setText(poController.Master().getPayeeName());
            tfRequestingDepartment.setText(poController.Master().Department().getDescription());
            taRemarks.setText(poController.Master().getRemarks());
            tfAmountToAdvance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getAdvanceAmount().doubleValue(), true));

            boolean lbPayeeOthers = JFXUtil.isObjectEqualTo(poController.Master().getClientId(), null, "")
                    && !JFXUtil.isObjectEqualTo(poController.Master().getPayeeName(), null, "") && pbContinue;
            if (cbOtherPayee.isSelected() && JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE)) {
                if (lbPayeeOthers) {
                    cbOtherPayee.setSelected(true);
                }
            } else {
                cbOtherPayee.setSelected(lbPayeeOthers);
            }

            boolean lbCreditedTo = !JFXUtil.isObjectEqualTo(poController.Master().CreditedToOthers().getPayeeName(), null, "");
            tfCreditedTo.setText(lbCreditedTo ? poController.Master().CreditedToOthers().getPayeeName() : poController.Master().Credited().getCompanyName());
            if (cbOtherCreditedTo.isSelected() && JFXUtil.isObjectEqualTo(pnEditMode, EditMode.ADDNEW, EditMode.UPDATE)) {
                if (lbCreditedTo && pbContinue) {
                    cbOtherCreditedTo.setSelected(true);
                }
            } else {
                if (pbContinue) {
                    cbOtherCreditedTo.setSelected(lbCreditedTo);
                }
            }

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    boolean pbContinue = true;

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbOtherPayee":
                    if (!tfPayee.getText().isEmpty()) {
                        String stat = checkedBox.isSelected() ? "check" : "uncheck";
                        pbContinue = false; // inserted due to lost focus delay; edge case
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Payee name is not empty, Are you sure you want to " + stat + " others?\n") == false) {
                            checkedBox.setSelected(stat.equals("check") ? false : true);
                            break;
                        }
                        poController.Master().setClientId("");
                        poController.Master().setPayeeName("");
                    }
                    pbContinue = true;
                    break;
                case "cbOtherCreditedTo":
                    if (!tfCreditedTo.getText().isEmpty()) {
                        String stat = checkedBox.isSelected() ? "check" : "uncheck";
                        pbContinue = false; // inserted due to lost focus delay; edge case
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Credited To is not empty, Are you sure you want to " + stat + " others?\n") == false) {
                            checkedBox.setSelected(stat.equals("check") ? false : true);
                            break;
                        }
                        poController.Master().setCreditedTo("");
                    }
                    pbContinue = true;
                    break;
            }
            loadRecordMaster();
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poController.setTransactionStatus(CashAdvanceStatus.OPEN);
                        poJSON = poController.searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnNew":
                        //Clear data
                        poController.resetMaster();
                        clearTextFields();

                        poJSON = poController.NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.initFields();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnUpdate":
                        poJSON = poController.UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnVoid":
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void the transaction?") == false) {
                            return;
                        }

                        if (CashAdvanceStatus.CONFIRMED.equals(poController.Master().getTransactionStatus())) {
                            poJSON = poController.CancelTransaction("");
                        } else {
                            poJSON = poController.VoidTransaction("");
                        }
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            clearTextFields();
                            poController.resetMaster();
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
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.OpenTransaction(poController.Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.Master().getTransactionStatus().equals(CashAdvanceStatus.OPEN)) {
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
                                JFXUtil.runWithDelay(0.3, () -> {
                                    btnNew.fire();
                                });
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
                    default:
                        break;
                }
                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
                    poController.resetMaster();
                    clearTextFields();
                    pnEditMode = EditMode.UNKNOWN;
                }
                loadRecordMaster();
                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

//        apMaster.setDisable(!lbShow);
        JFXUtil.setDisabled(!lbShow, taRemarks);
        JFXUtil.setDisabled(lbShow3, apMaster);

        switch (poController.Master().getTransactionStatus()) {
            case CashAdvanceStatus.RELEASED:
            case CashAdvanceStatus.VOID:
            case CashAdvanceStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                break;
        }
    }
}
