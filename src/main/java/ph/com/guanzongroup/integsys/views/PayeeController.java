package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.services.ClientControllers;
import ph.com.guanzongroup.cas.cashflow.Payee;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class PayeeController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private CashflowControllers oParameters;
    private final String pxeModuleName = "Payee";
    JSONObject poJSON = new JSONObject();
    private int pnEditMode;
    private boolean pbLoaded = false;
    @FXML
    private AnchorPane AnchorMain, AnchorInputs;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnSave,
            btnUpdate,
            btnCancel,
            btnActivate,
            btnClose;

    @FXML
    private FontAwesomeIconView faActivate;

    @FXML
    private TextField txtField01,
            txtField02,
            txtField03,
            txtField04,
            txtField05,
            txtSeeks01;

    @FXML
    private CheckBox cbField01;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeObject();
            pnEditMode = oParameters.Payee().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            Platform.runLater(() -> {
                btnNew.fire();
            });
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), "Computerized Acounting System", pxeModuleName);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new CashflowControllers(oApp, logwrapr);
            oParameters.Payee().setRecordStatus("0123");
            oParameters.Payee().setWithUI(true);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnActivate.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
    }

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
                        poJSON = oParameters.Payee().newRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            oParameters.Payee().getModel().setRecordStatus(RecordStatus.ACTIVE);
                            txtField02.requestFocus();
                            pnEditMode = oParameters.Payee().getEditMode();
                            initButton(pnEditMode);
                            loadRecord();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.Payee().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = oParameters.Payee().getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.Payee().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.Payee().getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnSave":
                        oParameters.Payee().getModel().setModifyingId(oApp.getUserID());
                        oParameters.Payee().getModel().setModifiedDate(oApp.getServerDate());
                        poJSON = oParameters.Payee().saveRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            Platform.runLater(() -> {
                                btnNew.fire();
                            });
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnActivate":
                        String id = oParameters.Payee().getModel().getPayeeID();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to "+btnActivate.getText().toLowerCase()+" this Parameter?") == true) {
                            if(RecordStatus.ACTIVE.equals(oParameters.Payee().getModel().getRecordStatus())){
                                poJSON = oParameters.Payee().deactivateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                } else {
                                    ShowMessageFX.Information("Parameter deactivated successfully", "Computerized Accounting System", pxeModuleName);
                                }
                            } else {
                                poJSON = oParameters.Payee().activateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                } else {
                                    ShowMessageFX.Information("Parameter activated successfully", "Computerized Accounting System", pxeModuleName);
                                }
                            }
                        }
                        
                        poJSON = oParameters.Payee().openRecord(id);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            break;
                        }
                        
                        pnEditMode = oParameters.Payee().getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;

                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtField05.clear();

        cbField01.setSelected(false);
        txtSeeks01.clear();
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        JFXUtil.setButtonsVisibility(!lbShow, btnNew,btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnCancel,btnSave);
        JFXUtil.setButtonsVisibility(lbShow1, btnUpdate,btnActivate);
        
        //fields
        JFXUtil.setDisabled(!lbShow, txtField02, txtField03, txtField04, txtField05);
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtField05.focusedProperty().addListener(txtField_Focus);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
        txtField05.setOnKeyPressed(this::txtField_KeyPressed);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
    }

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
                    case 2:
                        oParameters.Payee().getModel().setPayeeName(lsValue);
                        break;
                    case 3:
                        if(lsValue.isEmpty()){
                            oParameters.Payee().getModel().setParticularID("");
                        }
                        break;
                    case 4:
                        if(lsValue.isEmpty()){
                            oParameters.Payee().getModel().setAPClientID("");
                        }
                        break;
                    case 5:
                        if(lsValue.isEmpty()){
                            oParameters.Payee().getModel().setClientID("");
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

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJSON = new JSONObject();
            ClientControllers loController;
            switch (event.getCode()) {
                case F3:

                    switch (lnIndex) {

                        case 03:
                            poJson = oParameters.Particular().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.Payee().getModel().setParticularID(oParameters.Particular().getModel().getParticularID());
                            loadRecord();
                            break;

                        case 04:
                            loController = new ClientControllers(oApp, null);
                            poJson = loController.APClientMaster().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.Payee().getModel().setAPClientID(loController.APClientMaster().getModel().getClientId());
                            loadRecord();
                            break;
                        case 05:
                            loController = new ClientControllers(oApp, null);
                            poJson = loController.APClientMaster().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.Payee().getModel().setClientID(loController.APClientMaster().getModel().getClientId());
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJSON = oParameters.Payee().searchRecord(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.Payee().getModel().getPayeeName());
                            pnEditMode = oParameters.Payee().getEditMode();
                            initButton(pnEditMode);
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            txtField01.setText(oParameters.Payee().getModel().getPayeeID());
            txtField02.setText(oParameters.Payee().getModel().getPayeeName());
            txtField03.setText(oParameters.Payee().getModel().Particular().getDescription());
            txtField04.setText(oParameters.Payee().getModel().APClient().getCompanyName());
            txtField05.setText(oParameters.Payee().getModel().Client().getCompanyName());

            switch (oParameters.Payee().getModel().getRecordStatus()) {
                case "1":
                    btnActivate.setText("Deactivate");
                    faActivate.setGlyphName("CLOSE");
                    cbField01.setSelected(true);
                    break;
                case "0":
                    btnActivate.setText("Activate");
                    faActivate.setGlyphName("CHECK");
                    cbField01.setSelected(false);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(MiscUtil.getException(ex), "Computerized Acounting System", pxeModuleName);
        }
    }

}
