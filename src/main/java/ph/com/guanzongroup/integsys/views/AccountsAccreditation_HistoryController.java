package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.views.ScreenInterface;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.client.account.Account_Accreditation;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author User
 */
public class AccountsAccreditation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Accounts Accreditation Entry";
    private Control lastFocusedControl;
    private Account_Accreditation poAppController;

    private unloadForm poUnload = new unloadForm();

    @FXML
    private AnchorPane apMainAnchor, apMaster, apBrowse, apButton;
    @FXML
    private Label lblStatus, lblSource;

    @FXML
    private Button btnBrowse, btnClose, btnHistory;

    @FXML
    private TextField tfTransactionNo, tfCategory, tfCompany,
            tfContactPerson, tfAddress, tfSearchCompany, tfTIN;

    @FXML
    private DatePicker dpTransactionDate;

    @FXML
    private TextArea taRemarks;

    @FXML
    private ComboBox<String> cmbAccountType, cmbTransType;

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
            poAppController = new ClientControllers(poApp, poLogWrapper).AccountAccreditation();

            //initlalize and validate record objects from class controller
            //background thread
            Platform.runLater(() -> {
                poAppController.setRecordStatus("01234");
            });

            initControlEvents();
            getLoadedClient();
        } catch (SQLException | GuanzonException | CloneNotSupportedException e) {
            Logger.getLogger(AccountsAccreditation_HistoryController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {

                case "btnBrowse":
                    if (!tfTransactionNo.getText().isEmpty()) {
                        if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                            return;
                        }
                    }
                    if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompany.getText(), false),
                            "")) {
                        return;
                    }

                    getLoadedClient();
                    initButtonDisplay(poAppController.getEditMode());
                    return;

                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
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

            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }

    };

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
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompany.getText(), false),
                                        "")) {
                                    return;
                                }
                                getLoadedClient();
                                initButtonDisplay(poAppController.getEditMode());
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

            }
        }
    };

    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {

                    case "taRemarks":

                }
            } else {
                loTextField.selectAll();
            }
        } catch (Exception ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea loTxtField = (TextArea) event.getSource();
        String txtFieldID = ((TextArea) event.getSource()).getId();
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
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadClientMaster() {
        try {
            lblStatus.setText(poAppController.getModel().getRecordStatus().equals("0") == true ? "OPEN"
                    : poAppController.getModel().getRecordStatus().equals("1") == true ? "CONFIRMED"
                    : poAppController.getModel().getRecordStatus().equals("3") == true ? "CANCELLED" : "VOID");
            lblSource.setText("");

            tfTransactionNo.setText(poAppController.getModel().getTransactionNo() != null ? poAppController.getModel().getTransactionNo() : "");
            dpTransactionDate.setValue(ParseDate(poAppController.getModel().getDateTransact()));
            tfCategory.setText(poAppController.getModel().Category().getDescription());
            tfCompany.setText(poAppController.getModel().Client().getCompanyName());
            tfContactPerson.setText(poAppController.getModel().ClientInstitutionContact().getContactPersonName());
            
            String lshouseno = poAppController.getModel().ClientAddress().getHouseNo() == null || poAppController.getModel().ClientAddress().getHouseNo().isEmpty() ? "" : poAppController.getModel().ClientAddress().getHouseNo() + " ";
            String lsaddress = poAppController.getModel().ClientAddress().getAddress() == null || poAppController.getModel().ClientAddress().getAddress().isEmpty() ? "" : poAppController.getModel().ClientAddress().getAddress();
            String lsbrgy = poAppController.getModel().ClientAddress().Barangay().getBarangayName() == null || poAppController.getModel().ClientAddress().Barangay().getBarangayName().isEmpty() ? "" : ", " + poAppController.getModel().ClientAddress().Barangay().getBarangayName();
            String lscity = poAppController.getModel().ClientAddress().Town().getDescription() == null || poAppController.getModel().ClientAddress().Town().getDescription().isEmpty() ? " " : ", " + poAppController.getModel().ClientAddress().Town().getDescription();
            String lsprovince = poAppController.getModel().ClientAddress().Town().Province().getDescription() == null || poAppController.getModel().ClientAddress().Town().Province().getDescription().isEmpty() ? " " : " " + poAppController.getModel().ClientAddress().Town().Province().getDescription();
            
            tfAddress.setText( lshouseno + lsaddress + lsbrgy + lscity + lsprovince);
            
            taRemarks.setText(poAppController.getModel().getRemarks());
            cmbAccountType.getSelectionModel().select(Integer.parseInt(poAppController.getModel().getAccountType()));
            cmbTransType.getSelectionModel().select(Integer.parseInt(poAppController.getModel().getTransactionType()));

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtArea_KeyPressed);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
            }
        }

        clearAllInputs();

    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            }
        }
        initButtonDisplay(poAppController.getEditMode());
        cmbAccountType.setItems(FXCollections.observableArrayList(
                "Accounts Payable",
                "Accounts Receivable"
        ));
        cmbTransType.setItems(FXCollections.observableArrayList(
                "Accreditation", // empty option
                "Black Listing"
        ));
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnHistory", "btnNew", "btnUpdate");

        apMaster.setDisable(!lbShow);
    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
    }

    private void getLoadedClient() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadClientMaster();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

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
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, fsModule + ": " + message));
                }
            }
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

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }
}
