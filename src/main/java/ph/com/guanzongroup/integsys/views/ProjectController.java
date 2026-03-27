package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

/**
 * Controller class for managing Project records in the UI.
 * <p>
 * This class implements {@link Initializable} and {@link ScreenInterface}, and
 * provides functionality for creating, updating, saving, searching, confirming,
 * voiding, and canceling Project records via the GUI.
 * <p>
 * It interacts with {@link ParamControllers} to perform business logic
 * operations and uses {@link GRiderCAS} as the application context.
 * <p>
 * Each method handles specific user actions tied to buttons or text fields,
 * ensuring proper validation, transaction management, and user feedback.
 *
 * @author Teejei De Celis
 * @since 2026-03-27
 */
public class ProjectController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Project";
    private JSONObject poJSON;
    private int pnEditMode;
    private ParamControllers oParameters;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();

    @FXML
    private AnchorPane AnchorMain, AnchorInputs;
    @FXML
    private HBox hbButtons;
    @FXML
    private Label lblStatus;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnSave,
            btnUpdate,
            btnConfirm,
            btnVoid,
            btnCancel,
            btnCancelRecord,
            btnClose;

    @FXML
    private FontAwesomeIconView faActivate;

    @FXML
    private TextField txtField01,
            txtField02,
            txtSeeks01;

    /**
     * Sets the GRider application instance.
     *
     * @param foValue the GRiderCAS instance
     */
    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    /**
     * Sets the current industry ID (unused in this controller).
     *
     * @param fsValue the industry ID
     */

    @Override
    public void setIndustryID(String fsValue) {
    }

    /**
     * Sets the current company ID (unused in this controller).
     *
     * @param fsValue the company ID
     */
    @Override
    public void setCompanyID(String fsValue) {
    }

    /**
     * Sets the current category ID (unused in this controller).
     *
     * @param fsValue the category ID
     */
    @Override
    public void setCategoryID(String fsValue) {
    }

    /**
     * Initializes the controller after the FXML components are loaded.
     * <p>
     * This method initializes the parameter objects, sets the initial edit
     * mode, configures buttons, text fields, and triggers the "New" button to
     * start a new record.
     *
     * @param url the location used to resolve relative paths
     * @param rb the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeObject();
            pnEditMode = oParameters.Project().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            pbLoaded = true;
            btnNew.fire();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the parameter objects for this controller.
     * <p>
     * Instantiates {@link ParamControllers} and sets the default record status.
     *
     * @throws SQLException if a database access error occurs
     * @throws GuanzonException if initialization fails
     */
    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr);
            oParameters.Project().setRecordStatus("0134");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Registers click handlers for all buttons in the form.
     * <p>
     * Each button click is handled by {@link #handleButtonAction(ActionEvent)}.
     */
    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnCancelRecord.setOnAction(this::handleButtonAction);
        btnConfirm.setOnAction(this::handleButtonAction);
        btnVoid.setOnAction(this::handleButtonAction);
    }

    /**
     * Handles all button actions triggered by the user.
     * <p>
     * Executes actions such as New, Browse, Save, Update, Confirm, Void,
     * Cancel, and CancelRecord, including validation, transaction handling, and
     * feedback.
     *
     * @param event the {@link ActionEvent} triggered by a button click
     */
    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField01.requestFocus();
                        poJSON = oParameters.Project().newRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        pnEditMode = oParameters.Project().getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.Project().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtSeeks01.clear();
                            return;
                        }
                        pnEditMode = EditMode.READY;
                        loadRecord();
                        initButton(pnEditMode);
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.Project().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        pnEditMode = oParameters.Project().getEditMode();
                        initButton(pnEditMode);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel editing this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.READY;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnSave":
                        oParameters.Project().getModel().setModifyingId(oApp.getUserID());
                        oParameters.Project().getModel().setModifiedDate(oApp.getServerDate());
                        poJSON = oParameters.Project().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), pxeModuleName, null);
                        btnNew.fire();
                        break;
                    case "btnVoid":
                        if (ShowMessageFX.YesNo("Are you sure you want to void this record?", pxeModuleName, null)) {
                            poJSON = oParameters.Project().VoidRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                break;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            pnEditMode = oParameters.Project().getEditMode();
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnConfirm":
                        if (ShowMessageFX.YesNo("Are you sure you want to confirm this record?", pxeModuleName, null)) {
                            poJSON = oParameters.Project().ConfirmRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                break;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            pnEditMode = oParameters.Project().getEditMode();
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnCancelRecord":
                        if (ShowMessageFX.YesNo("Are you sure you want to cancel this record?", pxeModuleName, null)) {
                            poJSON = oParameters.Project().CancelRecord("");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            pnEditMode = oParameters.Project().getEditMode();
                            clearAllFields();
                            initButton(pnEditMode);
                        }
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException | ParseException ex) {
                Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);

            }
        }
    }

    /**
     * Clears all input fields and resets the status label.
     */
    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtSeeks01.clear();
        lblStatus.setText("UNKNOWN");
    }

    /**
     * Initializes button visibility and state based on the current edit mode.
     *
     * @param fnValue the current edit mode
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if parameter initialization fails
     */
    private void initButton(int fnValue) {
        try {
            // First, hide and unmanage all buttons
            CustomCommonUtil.setVisible(false, btnSave, btnUpdate, btnVoid, btnCancelRecord, btnCancel, btnConfirm,
                    btnBrowse, btnNew, btnClose);
            CustomCommonUtil.setManaged(false, btnSave, btnUpdate, btnVoid, btnCancelRecord, btnCancel, btnConfirm,
                    btnBrowse, btnNew, btnClose);
            txtSeeks01.setDisable(false);
            AnchorInputs.setDisable(true);

            switch (fnValue) {
                case EditMode.ADDNEW:
                case EditMode.UPDATE:
                    // When adding or updating, only show Save and Cancel
                    CustomCommonUtil.setVisible(true, btnSave, btnCancel);
                    CustomCommonUtil.setManaged(true, btnSave, btnCancel);
                    txtSeeks01.setDisable(true);
                    AnchorInputs.setDisable(false);
                    break;

                case EditMode.READY:
                    txtSeeks01.setDisable(false);
                    AnchorInputs.setDisable(true);
                    boolean projectExists = oParameters.Project().getModel().getProjectID() != null
                            && !oParameters.Project().getModel().getProjectID().isEmpty();

                    if (!projectExists) {
                        // If no project selected, only show Browse, New, Close
                        CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnClose);
                        CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnClose);

                        CustomCommonUtil.setVisible(false, btnUpdate, btnConfirm, btnVoid, btnCancelRecord);
                        CustomCommonUtil.setManaged(false, btnUpdate, btnConfirm, btnVoid, btnCancelRecord);
                    } else {
                        // Project exists, show buttons based on status
                        String status = oParameters.Project().getModel().getRecordStatus();

                        switch (status) {
                            case "0": // OPEN
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnUpdate, btnConfirm, btnVoid, btnClose);
                                break;
                            case "1": // CONFIRM
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew,  btnCancelRecord, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew,  btnCancelRecord, btnClose);
                                break;
                            case "3": // VOID
                            case "4": // CANCEL
                                CustomCommonUtil.setVisible(true, btnBrowse, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnClose);
                                break;
                            default:
                                // Fallback: show Browse, New, Close
                                CustomCommonUtil.setVisible(true, btnBrowse, btnNew, btnClose);
                                CustomCommonUtil.setManaged(true, btnBrowse, btnNew, btnClose);
                                break;
                        }
                    }
                    break;

                case EditMode.UNKNOWN:
                default:
                    // Default fallback: show only Browse and Close
                    CustomCommonUtil.setVisible(true, btnBrowse, btnClose);
                    CustomCommonUtil.setManaged(true, btnBrowse, btnClose);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Configures focus listeners and key event handlers for text fields.
     */
    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
    }

    /**
     * Handles key pressed events for search and navigation in text fields.
     *
     * @param event the {@link KeyEvent} triggered by a key press
     */
    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJson = oParameters.Project().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.Project().getModel().getProjectDescription());
                            pnEditMode = EditMode.READY;
                            loadRecord();
                            break;
                    }
                case ENTER:
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Listener for text field focus changes.
     * <p>
     * When focus is lost, updates the Project model with the new value.
     */
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            try {
                switch (lnIndex) {
                    case 1:
                        poJSON = oParameters.Project().getModel().setProjectID(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        break;
                    case 2:
                        oParameters.Project().getModel().setProjectDescription(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            return;
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else {
            txtField.selectAll();
        }
    };

    /**
     * Loads the current Project model data into the form fields.
     *
     * @throws SQLException if a database error occurs
     * @throws GuanzonException if model data retrieval fails
     */
    private void loadRecord() {
        try {
            boolean lbActive = oParameters.Project().getModel().getRecordStatus() == "1";

            txtField01.setText(oParameters.Project().getModel().getProjectID());
            txtField02.setText(oParameters.Project().getModel().getProjectDescription());

            switch (oParameters.Project().getModel().getRecordStatus()) {
                case "0":
                    lblStatus.setText("OPEN");
                    break;
                case "1":
                    lblStatus.setText("CONFIRM");
                    break;
                case "3":
                    lblStatus.setText("CANCEL");
                    break;
                case "4":
                    lblStatus.setText("VOID");
                    break;
                default:
                    lblStatus.setText("UNKNOWN");
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
