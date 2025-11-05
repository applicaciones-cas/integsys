package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
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
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import ph.com.guanzongroup.integsys.model.ModelResultSet;

public class BarangayController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Barangay";
    private int pnEditMode;
    private ParamControllers oParameters;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    private String psPrimary = "";
    private String lbStat = "";
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
    private CheckBox cbField01, cbField02, cbField03;

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
            pnEditMode = oParameters.Barangay().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr);
            oParameters.Barangay().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
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
                        txtField02.requestFocus();
                        JSONObject poJSON = oParameters.Barangay().newRecord();
                        pnEditMode = oParameters.Barangay().getEditMode();
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = oParameters.Barangay().getEditMode();
                            initButton(pnEditMode);
                            initTabAnchor();
                            loadRecord();
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            initTabAnchor();
                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.Barangay().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = EditMode.READY;
                        
                        loadRecord();
                        initTabAnchor();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.Barangay().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.Barangay().getEditMode();
                        initButton(pnEditMode);
                        initTabAnchor();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            initTabAnchor();
                        }
                        break;
                    case "btnSave":
                        oParameters.Barangay().getModel().setModifyingId(oApp.getUserID());
                        oParameters.Barangay().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.Barangay().saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearAllFields();
                        } else {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnActivate":
                        String Status = oParameters.Barangay().getModel().getRecordStatus();
                        String id = oParameters.Barangay().getModel().getBarangayId();
                        JSONObject poJsON;
                        
                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                   oParameters.Barangay().initialize();
                                    poJsON = oParameters.Barangay().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.Barangay().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                            case "1":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this Parameter?") == true) {
                                   ShowMessageFX.Information(String.valueOf(oParameters.Barangay().getEditMode()), "Computerized Accounting System", pxeModuleName);
                                    
                                    poJsON = oParameters.Barangay().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.Barangay().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                        }

                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtSeeks01.clear();
        cbField01.setSelected(false);
        cbField02.setSelected(false);
        cbField03.setSelected(false);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);

        btnCancel.setVisible(lbShow);
        btnCancel.setManaged(lbShow);
        btnSave.setVisible(lbShow);
        btnSave.setManaged(lbShow);
        btnUpdate.setVisible(!lbShow);
        btnUpdate.setManaged(!lbShow);

        btnBrowse.setVisible(!lbShow);
        btnBrowse.setManaged(!lbShow);
        btnNew.setVisible(!lbShow);
        btnNew.setManaged(!lbShow);

        btnClose.setVisible(true);
        btnClose.setManaged(true);
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
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
                    case 1:
                        oParameters.Barangay().getModel().setBarangayId(lsValue);
                        break;
                    case 2:
                        oParameters.Barangay().getModel().setBarangayName(lsValue);
                        break;
                    case 3:
                        oParameters.Barangay().getModel().setTownId(lsValue);
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
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 03:
                            poJson = oParameters.TownCity().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.Barangay().getModel().setTownId(oParameters.TownCity().getModel().getTownId());
                            txtField03.setText((String) oParameters.TownCity().getModel().getDescription());
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
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
                            poJson = oParameters.Barangay().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.Barangay().getModel().getBarangayName());
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
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            boolean lbActive = oParameters.Barangay().getModel().getRecordStatus() == "1";

            psPrimary = oParameters.Barangay().getModel().getBarangayId();

            txtField01.setText(oParameters.Barangay().getModel().getBarangayId());
            txtField02.setText(oParameters.Barangay().getModel().getBarangayName());
            txtField03.setText(oParameters.Barangay().getModel().Town().getDescription());

            cbField02.setSelected(oParameters.Barangay().getModel().hasRoute());
            cbField03.setSelected(oParameters.Barangay().getModel().isBlacklisted());

            switch (oParameters.Barangay().getModel().getRecordStatus()) {
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
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            try {
                oParameters.Barangay().getModel().setRecordStatus("1");
            } catch (SQLException ex) {
                Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                oParameters.Barangay().getModel().setRecordStatus("0");
            } catch (SQLException ex) {
                Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    void cbField02_Clicked(MouseEvent event) {
        try {
            oParameters.Barangay().getModel().hasRoute(cbField02.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField03_Clicked(MouseEvent event) {
        try {
            oParameters.Barangay().getModel().isBlacklisted(cbField03.isSelected());
        } catch (SQLException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BarangayController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTabAnchor() {
        if (AnchorInputs == null) {
            System.err.println("Error: AnchorInput is not initialized.");
            return;
        }

        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        AnchorInputs.setDisable(!isEditable);
    }

}
