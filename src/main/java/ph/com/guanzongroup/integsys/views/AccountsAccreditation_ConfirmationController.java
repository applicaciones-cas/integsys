package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.TransactionStatus;
import org.guanzon.cas.client.account.Account_Accreditation;
import org.guanzon.cas.client.constants.AccountAccreditationStatus;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Guiller & Team 1
 */
public class AccountsAccreditation_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Accounts Accreditation Entry";
    private Control lastFocusedControl;
    private Account_Accreditation poController;

    private unloadForm poUnload = new unloadForm();
    public int pnEditMode;
    ObservableList<String> comboboxlistAccounttype = FXCollections.observableArrayList("Accounts Payable", "Accounts Receivable");
    ObservableList<String> comboboxlistTranstype = FXCollections.observableArrayList("Accreditation", "Black Listing");
    JSONObject poJSON = new JSONObject();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus1, lblStatus11, lblStatus;
    @FXML
    private TextField tfSearchCompany, tfTransactionNo, tfCategory, tfCompany, tfAddress, tfTIN, tfContactPerson, tfContactNo, tfContactEmail, tfContactRole, tfContactDepartment, tfContactPosition;
    @FXML
    private Button btnBrowse, btnSearch, btnConfirm, btnVoid, btnUpdate, btnSave, btnCancel, btnHistory, btnClose, btnAddClompany;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private ComboBox cmbAccountType, cmbTransType;
    @FXML
    private FontAwesomeIconView faAdd;
    @FXML
    private TextArea taRemarks;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
//        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poController = new ClientControllers(poApp, poLogWrapper).AccountAccreditation();

            //initlalize and validate record objects from class controller
            //background thread
            initComboboxes();
            Platform.runLater(() -> {
                poController.setRecordStatus("0");
            });
//            lblSource.setText(poController.getModel());
            initControlEvents();
            loadRecordMaster();
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(AccountsAccreditation_ConfirmationController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(psFormName, lastFocusedTextField, previousSearchedTextField, apMaster, apBrowse);
                    break;
                case "btnBrowse":
                    if (!isJSONSuccess(poController.searchRecord(tfSearchCompany.getText(), false), "")) {
                        return;
                    }
                    loadRecordMaster();
                    initButtonDisplay(poController.getEditMode());
                    return;

                case "btnAddClompany":
                    poController.addCompany();
                    loadRecordMaster();
                    break;
                case "btnUpdate":
                    if (poController.getModel().getClientId() == null || poController.getModel().getClientId().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    //poController.openRecord(poController.getModel().getClientId());
                    if (!isJSONSuccess(poController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    loadRecordMaster();
                    initButtonDisplay(poController.getEditMode());
                    break;
                case "btnSave":

                    String lotransactioNo = tfTransactionNo.getText();
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }

                    if (ShowMessageFX.OkayCancel(null, psFormName, "Are you sure you want to save client??") == true) {

                        if (!isJSONSuccess(poController.saveRecord(), "Initialize Save Record")) {
                            return;
                        }
                        ShowMessageFX.Information("Client saved successfully!", "Initialize Save Record", null);

                        if (poController.getModel().getRecordStatus().equals("0")) {

                            if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to Confirm transaction?") == true) {

                                if (!isJSONSuccess(poController.openRecord(poController.getModel().getTransactionNo()), "Initialize Open Transaction")) {
                                    return;
                                }

                                if (!isJSONSuccess(poController.CloseTransaction(), "Initialize Close Transaction")) {
                                    return;
                                }
                                ShowMessageFX.Information("Transaction confirmed successfully", null, psFormName);
                            }
                        }
                        pnEditMode = poController.getEditMode();
                        //reset data to avoid transaction errors
                        clearAllInputs();
                    } else {
                        pnEditMode = poController.getEditMode();
                    }
                    break;
                case "btnConfirm":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, psFormName);
                        return;
                    }

                    if (!poController.getModel().getRecordStatus().equalsIgnoreCase(TransactionStatus.STATE_OPEN)) {
                        ShowMessageFX.Information("Status was already tagged", null, psFormName);
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to confirm transaction?") == true) {
                        if (!isJSONSuccess(poController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }
                        ShowMessageFX.Information("Transaction confirmed successfully", null, psFormName);

                        //reset data to avoid transaction errors
                        clearAllInputs();
                        break;
                    }
                    break;
                case "btnVoid":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, psFormName);
                        return;
                    }

                    if (!poController.getModel().getRecordStatus().equalsIgnoreCase(TransactionStatus.STATE_OPEN)) {
                        ShowMessageFX.Information("Status was already tagged", null, psFormName);
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Void/Cancel transaction?") == true) {

                        if (!isJSONSuccess(poController.VoidTransaction(), "Initialize Void Transaction")) {
                            return;
                        }
                        ShowMessageFX.Information("Transaction voided successfully", null, psFormName);

                        //reset data to avoid transaction errors
                        clearAllInputs();
                        break;
                    }
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poController = new ClientControllers(poApp, poLogWrapper).AccountAccreditation();

                        Platform.runLater(() -> {
                            poController.setRecordStatus("01");

                            initButtonDisplay(poController.getEditMode());
                        });
                        loadRecordMaster();
                        break;
                    }
                    break;
                case "btnHistory":

                    if (poController.getEditMode() != EditMode.READY && poController.getEditMode() != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
            }

            //manually reset button, edit mode not initialized on model
            if (btnID.equalsIgnoreCase("btnSave")) {
                initButtonDisplay(pnEditMode);
                return;
            }
            initButtonDisplay(poController.getEditMode());

        } catch (Exception e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchCompany":
                                if (!(tfTransactionNo.getText() == null ? "" : tfTransactionNo.getText()).isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poController.searchRecord(tfSearchCompany.getText(), false),
                                        "")) {
                                    return;
                                }
                                loadRecordMaster();
                                initButtonDisplay(poController.getEditMode());
                                break;

                            case "tfCategory":
                                if (!isJSONSuccess(poController.searchCategory(tfCategory.getText() == null ? "" : tfCategory.getText(), false),
                                        "Initialize Search Category! ")) {
                                    return;
                                }
                                loadRecordMaster();
                                break;

                            case "tfCompany":
                                if (!isJSONSuccess(poController.searchCompany(tfCompany.getText(), false),
                                        "Initialize Search Client! ")) {
                                    return;
                                }
                                loadRecordMaster();
                                initButtonDisplay(poController.getEditMode());
                                break;
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }
    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpTransactionDate":
                    poJSON = poController.getModel().setDateTransact(ldDateValue);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    return;

            }
        }
    };
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "taRemarks":
                            poJSON = poController.getModel().setRemarks(lsValue);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadRecordMaster();
                            break;
                    }
                } catch (Exception ex) {
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                }

            });

    private void loadRecordMaster() {
        try {
            boolean lbShow2 = !JFXUtil.isObjectEqualTo(poController.getModel().Client().getCompanyName(), null, "");
            JFXUtil.setDisabled(lbShow2, tfCompany);
            faAdd.setIcon(lbShow2 ? FontAwesomeIcon.PENCIL_SQUARE_ALT : FontAwesomeIcon.PLUS);

            if (lbShow2) {
                JFXUtil.applyHoverTooltip("Edit company info", btnAddClompany);
            } else {
                JFXUtil.applyHoverTooltip("Add new company", btnAddClompany);
            }

            JFXUtil.setStatusValue(lblStatus, AccountAccreditationStatus.class, poController.getModel().getEditMode() == EditMode.UNKNOWN ? "-1" : poController.getModel().getRecordStatus());

            tfTransactionNo.setText(poController.getModel().getTransactionNo() != null ? poController.getModel().getTransactionNo() : "");
            dpTransactionDate.setValue(ParseDate(poController.getModel().getDateTransact()));
            tfCategory.setText(poController.getModel().Category().getDescription());
            tfCompany.setText(poController.getModel().Client().getCompanyName());

            tfContactPerson.setText(poController.getModel().ClientInstitutionContact().getContactPersonName());
            tfContactRole.setText(poController.getModel().ClientInstitutionContact().ContactRole().getsRoleDesc());

            //set landline no (mobile no is empty), set fax no(landline no is empty), by default set mobile no
            String lsMobile = poController.getModel().ClientInstitutionContact().getMobileNo();
            String lsLandline = poController.getModel().ClientInstitutionContact().getLandlineNo();
            String lsFaxno = poController.getModel().ClientInstitutionContact().getFaxNo();

            tfContactNo.setText(lsMobile == null ? (lsLandline == null ? (lsFaxno == null ? "" : lsFaxno) : lsLandline) : lsMobile);
            tfContactEmail.setText(poController.getModel().ClientInstitutionContact().getMailAddress());
            tfContactDepartment.setText(poController.getModel().ClientInstitutionContact().getsDeprtmnt());
            tfContactPosition.setText(poController.getModel().ClientInstitutionContact().getContactPersonPosition());

            String lshouseno = poController.getModel().ClientAddress().getHouseNo() == null || poController.getModel().ClientAddress().getHouseNo().isEmpty() ? "" : poController.getModel().ClientAddress().getHouseNo() + " ";
            String lsaddress = poController.getModel().ClientAddress().getAddress() == null || poController.getModel().ClientAddress().getAddress().isEmpty() ? "" : poController.getModel().ClientAddress().getAddress();
            String lsbrgy = poController.getModel().ClientAddress().Barangay().getBarangayName() == null || poController.getModel().ClientAddress().Barangay().getBarangayName().isEmpty() ? "" : ", " + poController.getModel().ClientAddress().Barangay().getBarangayName();
            String lscity = poController.getModel().ClientAddress().Town().getDescription() == null || poController.getModel().ClientAddress().Town().getDescription().isEmpty() ? " " : ", " + poController.getModel().ClientAddress().Town().getDescription();
            String lsprovince = poController.getModel().ClientAddress().Town().Province().getDescription() == null || poController.getModel().ClientAddress().Town().Province().getDescription().isEmpty() ? " " : " " + poController.getModel().ClientAddress().Town().Province().getDescription();

            tfAddress.setText(lshouseno + lsaddress + lsbrgy + lscity + lsprovince);

            tfTIN.setText(poController.getModel().Client().getTaxIdNumber() == null ? "" : poController.getModel().Client().getTaxIdNumber());
            taRemarks.setText(poController.getModel().getRemarks());
            cmbAccountType.getSelectionModel().select(Integer.parseInt(poController.getModel().getAccountType()));
            cmbTransType.getSelectionModel().select(Integer.parseInt(poController.getModel().getTransactionType()));

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    private void initControlEvents() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        dpTransactionDate.focusedProperty().addListener(dPicker_Focus);
        clearAllInputs();
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbAccountType":
                        poController.getModel().setAccountType(String.valueOf(selectedIndex));
                        break;
                    case "cmbTransType":
                        poController.getModel().setTransactionType(String.valueOf(selectedIndex));
                        break;
                }
            });

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlistAccounttype, cmbAccountType), new JFXUtil.Pairs<>(comboboxlistTranstype, cmbTransType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbAccountType, cmbTransType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType, cmbTransType);
    }

    private void initButtonDisplay(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY);
        boolean lbShow3 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        boolean lbShow4 = fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN;
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid, btnConfirm);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow1, apMaster);
        JFXUtil.setButtonsVisibility(lbShow4, btnBrowse);
        JFXUtil.setButtonsVisibility(!lbShow4, btnAddClompany);

        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getRecordStatus()) {
            case AccountAccreditationStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                break;
            case AccountAccreditationStatus.VOID:
            case AccountAccreditationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid);
                break;
        }
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void clearAllInputs() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster);
        initButtonDisplay(poController.getEditMode());
    }
}
