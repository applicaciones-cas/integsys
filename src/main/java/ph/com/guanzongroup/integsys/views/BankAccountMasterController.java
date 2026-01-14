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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class BankAccountMasterController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Bank Accounts";
    private int pnEditMode;
    private JSONObject poJSON;
    private CashflowControllers oCashflow;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private String psActiveField = "";
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    ObservableList<String> AccountType = FXCollections.observableArrayList("Sample 1", "Sample 2");
    ObservableList<String> SlipType = FXCollections.observableArrayList("Payment Slip", "Deposit Slip");
    
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";

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
            txtField04,
            txtField05,
            txtField06,
            txtField07,
            txtField08,
            txtField09,
            txtSeeks01,
            txtSeeks02;

    @FXML
    private CheckBox cbField01,
            cbField02, 
            cbField03,
            cbField04;
    @FXML
    private ComboBox cmbField01,
            cmbField02;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            initializeObject();
            pnEditMode = oCashflow.BankAccountMaster().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            initComboboxes();
            initCheckBox();
            ClickButton();
            initTabAnchor();

            if (oCashflow.BankAccountMaster().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oCashflow = new CashflowControllers(oApp, logwrapr);
            oCashflow.BankAccountMaster().setRecordStatus("0123");
            oCashflow.BankAccountMaster().getModel().setIndustryCode(psIndustryID);
            oCashflow.BankAccountMaster().getModel().setCompanyId(psCompanyID);
        } catch (SQLException ex) {
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
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
    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(AccountType, cmbField01), new JFXUtil.Pairs<>(SlipType, cmbField02));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbField01, cmbField02);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbField01, cmbField02);
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                try {
                    switch (cmbId) {
                        case "cmbField01":
                            oCashflow.BankAccountMaster().getModel().setAccountType(String.valueOf(cmbField01.getSelectionModel().getSelectedIndex()));
                            break;
                        case "cmbField02":
                            if (getSlipType((String) selectedValue) != null) {
                                oCashflow.BankAccountMaster().getModel().setSlipType(getSlipType((String) selectedValue));
                            }
                            break;
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    );

    private String getSlipType(String slipType) {
        switch (slipType) {
            case "Payment Slip":    return "PS";
            case "Deposit Slip": return "DS";
            default:           return null;
        }
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
                        txtField02.requestFocus();
                        poJSON = oCashflow.BankAccountMaster().newRecord();
                        pnEditMode = EditMode.READY;
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = oCashflow.BankAccountMaster().getEditMode();
                            initButton(pnEditMode);
                            initTabAnchor();
                            loadRecord();
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            initTabAnchor();
                        }
                        break;
                    case "btnBrowse":
                        System.out.print("EDIT MODE ON BROWSE : " + pnEditMode);
                        String loValue = "";
                        switch (psActiveField) {
                            case "01":
                                if (!oCashflow.BankAccountMaster().getModel().getAccountNo().isEmpty() && !txtSeeks01.getText().isEmpty()){
                                    poJSON = oCashflow.BankAccountMaster().searchRecordbyAccount(oCashflow.BankAccountMaster().getModel().getBankAccountId(),true);
                                }else{
                                    loValue = txtSeeks01.getText();
                                    poJSON = oCashflow.BankAccountMaster().searchRecordbyAccount(loValue, true);
                                }
                                break;
                            case "02":
                                if (!oCashflow.BankAccountMaster().getModel().getAccountName().isEmpty() && !txtSeeks02.getText().isEmpty()){
                                    poJSON = oCashflow.BankAccountMaster().searchRecordbyAccount(oCashflow.BankAccountMaster().getModel().getBankAccountId(),true);
                                }else{
                                    loValue = txtSeeks02.getText();
                                    poJSON = oCashflow.BankAccountMaster().searchRecordbyAccount(loValue, false);
                                }
                                break;
                            default:
                                loValue = "";
                                poJSON = oCashflow.BankAccountMaster().searchRecord(loValue,true);
                                break;
                        }
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = oCashflow.BankAccountMaster().getEditMode();
                        System.out.print("EDIT MODE ON BROWSE 1: " + pnEditMode);
                        initButton(pnEditMode);
                        loadRecord();
                        initTabAnchor();
                        break;
                    case "btnUpdate":
                        poJSON = oCashflow.BankAccountMaster().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oCashflow.BankAccountMaster().getEditMode();
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
                        oCashflow.BankAccountMaster().getModel().setModifyingId(oApp.getUserID());
                        oCashflow.BankAccountMaster().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oCashflow.BankAccountMaster().saveRecord();
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
                        String Status = oCashflow.BankAccountMaster().getModel().getRecordStatus();
                        String id = oCashflow.BankAccountMaster().getModel().getBankAccountId();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    poJsON = oCashflow.BankAccountMaster().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oCashflow.BankAccountMaster().openRecord(id);
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
                                    poJsON = oCashflow.BankAccountMaster().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oCashflow.BankAccountMaster().openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                            default:

                                break;

                        }
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtField05.clear();
        txtField06.clear();
        txtField07.clear();
        txtField08.clear();
        txtField09.clear();
        txtSeeks01.clear();
        cbField01.setSelected(false);
        cmbField01.getSelectionModel().select(-1);
        cmbField02.getSelectionModel().select(-1);
    }
    private void initCheckBox() {
        if(pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN){
             JFXUtil.setDisabled(true, cbField01,cbField02,cbField03,cbField04);
        }
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            cbField02.setOnAction(event -> {
                try {
                    oCashflow.BankAccountMaster().getModel().setMonitor(cbField02.isSelected());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            cbField03.setOnAction(event -> {
                try {
                    oCashflow.BankAccountMaster().getModel().setDefault(cbField03.isSelected());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            cbField04.setOnAction(event -> {
                try {
                    oCashflow.BankAccountMaster().getModel().setDefault(cbField04.isSelected());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
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
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtField05.focusedProperty().addListener(txtField_Focus);
        txtField06.focusedProperty().addListener(txtField_Focus);
        txtField07.focusedProperty().addListener(txtField_Focus);
        txtField08.focusedProperty().addListener(txtField_Focus);
        txtField09.focusedProperty().addListener(txtField_Focus);
        
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtSeeks02.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtField02.setOnKeyPressed(this::txtField_KeyPressed);
        txtField06.setOnKeyPressed(this::txtField_KeyPressed);

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
                            psActiveField = String.valueOf(lnIndex);
                            poJson = oCashflow.BankAccountMaster().searchRecordbyAccount(lsValue, true);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oCashflow.BankAccountMaster().getModel().getAccountNo());
                            pnEditMode = EditMode.READY;
                            break;
                        case 02:
                            psActiveField = String.valueOf(lnIndex);
                            poJson = oCashflow.BankAccountMaster().searchRecordbyAccount(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks02.clear();
                                break;
                            }
                            txtSeeks02.setText((String) oCashflow.BankAccountMaster().getModel().getAccountName());
                            pnEditMode = EditMode.READY;
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
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
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
                    case 1:
                        oCashflow.BankAccountMaster().getModel().setBankAccountId(lsValue);
                        break;
                    case 3:
                        oCashflow.BankAccountMaster().getModel().setAccountNo(lsValue);
                        break;
                    case 4:
                        oCashflow.BankAccountMaster().getModel().setAccountCode(lsValue);
                        break;
                    case 5:
                        oCashflow.BankAccountMaster().getModel().setAccountName(lsValue);
                        break;
                    case 7:
                        oCashflow.BankAccountMaster().getModel().setClearingDays(Integer.parseInt(lsValue));
                        break;
                    case 8:
                        oCashflow.BankAccountMaster().getModel().setSignatoryCount(Integer.parseInt(lsValue));
                        break;
                    case 9:
                        oCashflow.BankAccountMaster().getModel().setSerialNo(lsValue);
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
                        case 02:
                            poJson = oCashflow.BankAccountMaster().SearchBanks(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            
                            txtField02.setText((String) oCashflow.BankAccountMaster().getModel().Banks().getBankName());
                            break;
                            
                        case 06:
                            poJson = oCashflow.BankAccountMaster().SearchBanksBranch(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            txtField06.setText(oCashflow.BankAccountMaster().getModel().getBranch());
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
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void loadRecord() {
        try {
            boolean lbActive = oCashflow.BankAccountMaster().getModel().getRecordStatus() == "1";

            txtField01.setText(oCashflow.BankAccountMaster().getModel().getBankAccountId());
            txtField02.setText(oCashflow.BankAccountMaster().getModel().Banks().getBankName());
            txtField03.setText(oCashflow.BankAccountMaster().getModel().getAccountNo());
            txtField04.setText(oCashflow.BankAccountMaster().getModel().getAccountCode());
            txtField05.setText(oCashflow.BankAccountMaster().getModel().getAccountName());
            txtField06.setText(oCashflow.BankAccountMaster().getModel().getBranch());
            txtField07.setText( String.valueOf(oCashflow.BankAccountMaster().getModel().getClearingDays()));
            txtField08.setText( String.valueOf(oCashflow.BankAccountMaster().getModel().getSignatoryCount()));
            txtField09.setText(oCashflow.BankAccountMaster().getModel().getSerialNo());

            switch (oCashflow.BankAccountMaster().getModel().getRecordStatus()) {
                case "0":
                    btnActivate.setText("Deactivate");
                    faActivate.setGlyphName("CLOSE");
                    cbField01.setSelected(false);
                    break;
                case "1":
                    btnActivate.setText("Activate");
                    faActivate.setGlyphName("CHECK");
                    cbField01.setSelected(true);
                    break;
            }
            switch (oCashflow.BankAccountMaster().getModel().getAccountType()) {
                case "0":
                    cmbField01.getSelectionModel().select(0);
                    break;
                case "1":
                    cmbField01.getSelectionModel().select(1);
                    break;
            }
            
            switch (oCashflow.BankAccountMaster().getModel().getSlipType()) {
                case "PS":
                    cmbField02.getSelectionModel().select(0);
                    break;
                case "DS":
                    cmbField02.getSelectionModel().select(1);
                    break;
            }
            
             cbField02.setSelected(oCashflow.BankAccountMaster().getModel().isMonitor());
             cbField03.setSelected(oCashflow.BankAccountMaster().getModel().isDefault());
             cbField04.setSelected(oCashflow.BankAccountMaster().getModel().isBankPrinting());
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            try {
                oCashflow.BankAccountMaster().getModel().setRecordStatus("1");
            } catch (SQLException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                oCashflow.BankAccountMaster().getModel().setRecordStatus("0");
            } catch (SQLException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            }
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
