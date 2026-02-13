package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.parameter.Banks;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class BanksController implements Initializable, ScreenInterface {
    
    private GRiderCAS oApp;
    private Banks oParameters;
    private final String pxeModuleName = "Banks";
    JSONObject poJSON = new JSONObject();
    private int pnEditMode;
    private boolean pbLoaded = false;

    @FXML
    private AnchorPane AnchorMain, AnchorInputs;
    @FXML
    private HBox hbButtons;

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
        initializeObject();
        pnEditMode = oParameters.getEditMode();
        initButton(pnEditMode);
        InitTextFields();
        ClickButton();
        Platform.runLater(() -> {
            btnNew.fire();
        });
        pbLoaded = true;
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr).Banks();
            oParameters.setRecordStatus("0123");
            oParameters.setWithUI(true);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Information(MiscUtil.getException(ex), "Computerized Acounting System", pxeModuleName);
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
        poJSON = new JSONObject();
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
                        poJSON = oParameters.newRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            oParameters.getModel().setRecordStatus(RecordStatus.ACTIVE);
                            txtField02.requestFocus();
                            pnEditMode = oParameters.getEditMode();
                            initButton(pnEditMode);
                            loadRecord();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
                        initButton(pnEditMode);
                        loadRecord();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
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
                        oParameters.getModel().setModifyingId(oApp.getUserID());
                        oParameters.getModel().setModifiedDate(oApp.getServerDate());
                        poJSON = oParameters.saveRecord();
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
                        String id = oParameters.getModel().getBankID();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to "+btnActivate.getText().toLowerCase()+" this Parameter?") == true) {
                            if(RecordStatus.ACTIVE.equals(oParameters.getModel().getRecordStatus())){
                                poJSON = oParameters.deactivateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                } else {
                                    ShowMessageFX.Information("Parameter deactivated successfully", "Computerized Accounting System", pxeModuleName);
                                }
                            } else {
                                poJSON = oParameters.activateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                } else {
                                    ShowMessageFX.Information("Parameter activated successfully", "Computerized Accounting System", pxeModuleName);
                                }
                            }
                        }
                        
                        poJSON = oParameters.openRecord(id);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            break;
                        }
                        
                        pnEditMode = oParameters.getEditMode();
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
        txtSeeks01.clear();
        cbField01.setSelected(false);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        JFXUtil.setButtonsVisibility(!lbShow, btnNew,btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnCancel,btnSave);
        JFXUtil.setButtonsVisibility(lbShow1, btnUpdate,btnActivate);
        
        //fields
        JFXUtil.setDisabled(!lbShow, txtField02, txtField03);
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJSON = oParameters.searchRecord(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.getModel().getBankName());
                            pnEditMode = oParameters.getEditMode();
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
                        oParameters.getModel().setBankName(lsValue);
                        break;
                    case 3:
                        oParameters.getModel().setBankCode(lsValue);
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
    
    private void loadRecord() {
        txtField01.setText(oParameters.getModel().getBankID());
        txtField02.setText(oParameters.getModel().getBankName());
        txtField03.setText(oParameters.getModel().getBankCode());
        
        switch (oParameters.getModel().getRecordStatus()) {
            case "0":
                btnActivate.setText("Activate");
                faActivate.setGlyphName("CHECK");
                cbField01.setSelected(false);
                break;
            case "1":
                btnActivate.setText("Deactivate");
                faActivate.setGlyphName("CLOSE");
                cbField01.setSelected(true);
                break;
        }
    }
}
