package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
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
public class AccountsAccreditation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Accounts Accreditation Entry";
    private Control lastFocusedControl;
    private Account_Accreditation poController;

    private unloadForm poUnload = new unloadForm();
    ObservableList<String> comboboxlistAccounttype = FXCollections.observableArrayList("Accounts Payable", "Accounts Receivable");
    ObservableList<String> comboboxlistTranstype = FXCollections.observableArrayList("Accreditation", "Black Listing");
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus1, lblStatus11, lblStatus;
    @FXML
    private TextField tfSearchCompany, tfTransactionNo, tfCategory, tfCompany, tfAddress, tfTIN, tfContactPerson, tfContactNo, tfContactEmail, tfContactRole, tfContactDepartment, tfContactPosition;
    @FXML
    private Button btnBrowse, btnHistory, btnClose, btnAddClompany;
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

            initComboboxes();
            Platform.runLater(() -> {
                poController.setRecordStatus("01234");
            });
            lblSource.setText(poController.getCompany());
            initControlEvents();
            loadRecordMaster();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnBrowse":
                    if (!isJSONSuccess(poController.searchRecord(tfSearchCompany.getText(), false),
                            "")) {
                        return;
                    }
                    loadRecordMaster();
                    initButtonDisplay(poController.getEditMode());
                    return;
                case "btnHistory":

                    if (poController.getEditMode() != EditMode.READY && poController.getEditMode() != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(null, psFormName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
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

            initButtonDisplay(poController.getEditMode());
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchCompany":
                                if (!tfTransactionNo.getText().isEmpty()) {
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
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow2 = !JFXUtil.isObjectEqualTo(poController.getModel().Client().getCompanyName(), null, "");
            JFXUtil.setDisabled(lbShow2, tfCompany);
            faAdd.setIcon(lbShow2 ? FontAwesomeIcon.PENCIL_SQUARE_ALT : FontAwesomeIcon.PLUS);
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }

    private void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlistAccounttype, cmbAccountType), new JFXUtil.Pairs<>(comboboxlistTranstype, cmbTransType));
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType, cmbTransType);
    }

    private void initButtonDisplay(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow2 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow2, btnClose);

        JFXUtil.setDisabled(true, apMaster);
        JFXUtil.setButtonsVisibility(true, btnBrowse);
        JFXUtil.setButtonsVisibility(false, btnAddClompany);
    }

    private void initControlEvents() {
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        clearAllInputs();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            poLogWrapper.info("Success on " + fsModule);
            return true;
        }
        // Unknown or null result
        poLogWrapper.warning("Unrecognized result: " + result);
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
        JFXUtil.clearTextFields(apMaster);
        initButtonDisplay(poController.getEditMode());
    }
}
