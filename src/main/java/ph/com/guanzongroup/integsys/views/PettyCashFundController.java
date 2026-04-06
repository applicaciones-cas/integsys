package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import ph.com.guanzongroup.cas.cashflow.status.PettyCashStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.PettyCash;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1
 */
public class PettyCashFundController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static PettyCash poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private boolean pbEntered = false;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnConfirm, btnSearch, btnSave, btnCancel, btnVoid, btnLedger, btnHistory, btnClose;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private TextField tfCashFundId, tfBranch, tfDepartment, tfCustodian, tfDescription, tfBeginningBalance, tfCurrentBalance;
    @FXML
    private DatePicker dpBegBalAsOf, dpLastTransDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).PettyCash();
            poController.initialize(); // Initialize transaction
            poController.initFields();
            initTextFields();
            initDatePickers();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            loadRecordMaster();
            Platform.runLater(() -> {
                poController.setRecordStatus("0123");
                poController.getModel().setIndustryId(psIndustryId);
                poController.getModel().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setWithParentClass(false);
                poController.setWithUI(true);
                loadRecordSearch();
                btnNew.fire();
            });

            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfBranch":
                            poJSON = poController.SearchBranch(lsValue, false, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                            } else {
                                JFXUtil.textFieldMoveNext(tfDepartment);
                            }
                            loadRecordMaster();
                            break;
                        case "tfDepartment":
                            poJSON = poController.SearchDepartment(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                            } else {
                                JFXUtil.textFieldMoveNext(tfCustodian);
                            }
                            loadRecordMaster();
                            break;
                        case "tfCustodian":
                            poJSON = poController.searchCustodian(lsValue, false);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                            } else {
                                JFXUtil.textFieldMoveNext(tfDescription);
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
                    case "tfBranch":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setBranchCode("");
                        }
                        break;
                    case "tfDepartment":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setDepartment("");
                        }
                        break;
                    case "tfCustodian":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setPettyManager("");
                        }
                        break;
                    case "tfDescription":
                        poJSON = poController.getModel().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfBeginningBalance":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setBeginningBalance(Double.parseDouble(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        switch (poController.getModel().getTransactionStatus()) {
                            case PettyCashStatus.OPEN:
                                poController.getModel().setBalance(poController.getModel().getBeginningBalance());
                                break;
                        }
                        break;
//                    case "tfCurrentBalance":
//                        break;
//                    case "tfCurrentBalance":
//                        lsValue = JFXUtil.removeComma(lsValue);
//                        poJSON = poController.getModel().setBalance(Double.parseDouble(lsValue));
//                        if (!JFXUtil.isJSONSuccess(poJSON)) {
//                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
//                        }
//                        break;
                }
                loadRecordMaster();
            });

    public void initTextFields() {
        JFXUtil.handleDisabledNodeClick(apMaster, pnEditMode, nodeID -> {
            switch (nodeID) {
                case "dpLastTransDate":
                    if (JFXUtil.isObjectEqualTo(poController.getModel().getLastTransactionDate(), null, "")) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Date is set once processed in cash disbursement.");
                    }
                    break;
            }
        });

        JFXUtil.setFocusListener(txtMaster_Focus, tfCashFundId, tfBranch, tfDepartment, tfCustodian, tfDescription, tfBeginningBalance, tfCurrentBalance);
        JFXUtil.setCommaFormatter(tfBeginningBalance, tfCurrentBalance);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpBegBalAsOf, dpLastTransDate);
        JFXUtil.setActionListener(datepicker_Action, dpBegBalAsOf);
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(lastFocusedTextField, previousSearchedTextField);
        JFXUtil.clearTextFields(apMaster);
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.getModel().Company().getCompanyName() + " - " + poController.getModel().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordMaster() {
        try {
            JFXUtil.setDisabled(true, tfCurrentBalance);
            switch (poController.getModel().getTransactionStatus()) {
                case PettyCashStatus.ACTIVE:
                    JFXUtil.setDisabled(true, apMaster);
                    break;
            }
            JFXUtil.setStatusValue(lblStatus, PettyCashStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getTransactionStatus());
            tfCashFundId.setText(poController.getModel().getPettyId());
            tfBranch.setText(poController.getModel().Branch().getBranchName());
            tfDepartment.setText(poController.getModel().Department().getDescription());
            tfCustodian.setText(poController.getModel().Custodian().getCompanyName());
            tfDescription.setText(poController.getModel().getDescription());
            tfBeginningBalance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getBeginningBalance(), false));

            String lsBegBalAsOf = CustomCommonUtil.formatDateToShortString(poController.getModel().getBeginningDate());
            dpBegBalAsOf.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsBegBalAsOf, "yyyy-MM-dd"));

            tfCurrentBalance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getBalance(), false));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
                        poController.setRecordStatus("0123");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfCashFundId.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnNew":
                        //Clear data
//                        poController.resetMaster();
                        clearTextFields();
                        poJSON = poController.newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.initFields();
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnConfirm":
                        String id = poController.getModel().getPettyId();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to activate this record?") == true) {
                            poJSON = poController.ActivateRecord(); //Activate is Confirm
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            poJSON = poController.openRecord(id);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        String id2 = poController.getModel().getPettyId();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to deactivate this record?") == true) {
                            poJSON = poController.DeactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            poJSON = poController.openRecord(id2);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poController.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            //Clear data
                            pnEditMode = EditMode.UNKNOWN;
                            poController.initialize();
                            poController.setRecordStatus("0123");
                            clearTextFields();
                            poController.getModel().setIndustryId(psIndustryId);
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
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save the record?") == true) {
                            poJSON = poController.saveRecord();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.openRecord(poController.getModel().getPettyId());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.getModel().getTransactionStatus().equals(PettyCashStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to activate this record?")) {
                                            loJSON = poController.ActivateRecord();
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Warning(null, pxeModuleName, (String) loJSON.get("message"));
                                            }
                                        }
                                    }
                                }
                                btnNew.fire();
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

                loadRecordMaster();
                initButton(pnEditMode);
            } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }
    boolean pbSuccess = true;
    EventHandler<ActionEvent> datepicker_Action = JFXUtil.DatePickerAction(
            (datePicker, sdfFormat, lsServerDate, ldCurrentDate, lsSelectedDate, ldSelectedDate) -> {
                poJSON = new JSONObject();
                JFXUtil.setJSONSuccess(poJSON, "success");
                switch (datePicker.getId()) {
                    case "dpBegBalAsOf":
                        if (ldSelectedDate.isBefore(ldCurrentDate) && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Back date is not allowed.");
                            pbSuccess = false;
                        }
                        if (pbSuccess) {
                            poController.getModel().setBeginningDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                        } else {
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                        }
                        pbSuccess = false; //Set to false to prevent multiple message box
                        loadRecordMaster();
                        pbSuccess = true; //Set to original value
                        break;
                    default:
                        break;
                }
            });

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setButtonsVisibility(lbShow2, btnVoid, btnConfirm);
//        apMaster.setDisable(!lbShow);
        JFXUtil.setDisabled(lbShow3, apMaster);

        switch (poController.getModel().getTransactionStatus()) {
            case PettyCashStatus.OPEN:
                JFXUtil.setButtonsVisibility(false, btnVoid);
                break;
            case PettyCashStatus.ACTIVE:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnConfirm);
                break;
            case PettyCashStatus.DEACTIVATED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                break;
        }
    }
}
