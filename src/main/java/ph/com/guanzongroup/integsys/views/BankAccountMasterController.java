package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.BankAccountMaster;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class BankAccountMasterController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Bank Accounts";
    private int pnEditMode;
    private JSONObject poJSON;
    private BankAccountMaster oCashflow;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private String psActiveField = "";
    private Stage dialogStage = null;
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    ObservableList<String> AccountType = FXCollections.observableArrayList("Saving", "Current","Time Deposit");
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
            btnClose,
            btnLedger;
    @FXML
    private DatePicker dpPicker01;

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
            txtField10,
            txtField11,
            txtField12,
            txtField13,
            txtField14,
            txtField15,
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
        initializeObject();
        pnEditMode = oCashflow.getEditMode();
        initButton(pnEditMode);
        InitTextFields();
        initDatePickerActions();
        initComboboxes();
        initCheckBox();
        ClickButton();
        initTabAnchor();
        if (oCashflow.getEditMode() == EditMode.ADDNEW) {
            initButton(pnEditMode);
            initTabAnchor();
            loadRecord();
        }
        pbLoaded = true;
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oCashflow = new CashflowControllers(oApp, logwrapr).BankAccountMaster();
            oCashflow.setRecordStatus("0123");
            oCashflow.getModel().setIndustryCode(psIndustryID);
            oCashflow.getModel().setCompanyId(psCompanyID);
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
        btnLedger.setOnAction(this::handleButtonAction);
    }
    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(AccountType, cmbField01), new JFXUtil.Pairs<>(SlipType, cmbField02));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbField01, cmbField02);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbField01, cmbField02);
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbField01":
                        oCashflow.getModel().setAccountType(String.valueOf(cmbField01.getSelectionModel().getSelectedIndex()));
                        break;
                    case "cmbField02":
                        if (getSlipType((String) selectedValue) != null) {
                            oCashflow.getModel().setSlipType(getSlipType((String) selectedValue));
                        }
                        break;
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
                        poJSON = oCashflow.newRecord();
                        pnEditMode = EditMode.READY;
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = oCashflow.getEditMode();
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
                                if (!oCashflow.getModel().getAccountNo().isEmpty() && !txtSeeks01.getText().isEmpty()){
                                    poJSON = oCashflow.searchRecordbyAccount(oCashflow.getModel().getBankAccountId(),true);
                                }else{
                                    loValue = txtSeeks01.getText();
                                    poJSON = oCashflow.searchRecordbyAccount(loValue, true);
                                }
                                break;
                            case "02":
                                if (!oCashflow.getModel().getAccountName().isEmpty() && !txtSeeks02.getText().isEmpty()){
                                    poJSON = oCashflow.searchRecordbyAccount(oCashflow.getModel().getBankAccountId(),true);
                                }else{
                                    loValue = txtSeeks02.getText();
                                    poJSON = oCashflow.searchRecordbyAccount(loValue, false);
                                }
                                break;
                            default:
                                loValue = "";
                                poJSON = oCashflow.searchRecord(loValue,true);
                                break;
                        }
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = oCashflow.getEditMode();
                        System.out.print("EDIT MODE ON BROWSE 1: " + pnEditMode);
                        initButton(pnEditMode);
                        loadRecord();
                        initTabAnchor();
                        break;
                    case "btnUpdate":
                        poJSON = oCashflow.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oCashflow.getEditMode();
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
                        oCashflow.getModel().setModifyingId(oApp.getUserID());
                        oCashflow.getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oCashflow.saveRecord();
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
                        String Status = oCashflow.getModel().getRecordStatus();
                        String id = oCashflow.getModel().getBankAccountId();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    poJsON = oCashflow.activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oCashflow.openRecord(id);
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
                                    poJsON = oCashflow.deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oCashflow.openRecord(id);
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
                    case "btnLedger":
                        showLedgerDialog();
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void closeLedgerDialog() {
        if (dialogStage != null && dialogStage.isShowing()) {
            dialogStage.close();
            dialogStage = null;
        } else {
        }
    }
    public void showLedgerDialog() {
        poJSON = new JSONObject();
        try {
//             Check if the dialog is already open
            if (dialogStage != null) {
                if (dialogStage.isShowing()) {
                    dialogStage.toFront();
                    return;
                }
            }
            URL fxmlUrl = getClass().getResource("/ph/com/guanzongroup/integsys/views/BankAccountLedger.fxml");

            if (fxmlUrl == null) {
                System.out.println("FXML NOT FOUND!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/PettyCashLedger.fxml"));
            BankAccountLedgerController controller = new BankAccountLedgerController();
            loader.setController(controller);

            if (controller != null) {
                controller.setGRider(oApp);
                controller.setObject(oCashflow);
            }

            Parent root = loader.load();

            // Handle drag events for the undecorated window
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Cash Fund Ledger");
            dialogStage.setScene(new Scene(root));

            // Clear the reference when closed
            dialogStage.setOnHidden(event -> {
                dialogStage = null;
            });
            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
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
        txtField10.clear();
        txtField11.clear();
        txtField12.clear();
        txtField13.clear();
        txtField14.clear();
        txtField15.clear();
        txtSeeks01.clear();
        cbField01.setSelected(false);
        cmbField01.getSelectionModel().select(-1);
        cmbField02.getSelectionModel().select(-1);

    }
    private void initDatePickerActions() {
        dpPicker01.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (dpPicker01.getValue() != null) {
                    oCashflow.getModel().setBeginningBalanceDate(SQLUtil.toDate(dpPicker01.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
                   
                }
            }
        });
    }
    private void initCheckBox() {
        if(pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN){
             JFXUtil.setDisabled(true, cbField01,cbField02,cbField03,cbField04);
        }
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            cbField02.setOnAction(event -> {
                oCashflow.getModel().setMonitor(cbField02.isSelected());
            });
            cbField03.setOnAction(event -> {
                oCashflow.getModel().setDefault(cbField03.isSelected());
            });
            cbField04.setOnAction(event -> {
                oCashflow.getModel().setBankPrinting(cbField04.isSelected());
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
        btnLedger.setVisible(false);
        btnLedger.setManaged(false);
        btnActivate.setVisible(false);
        btnActivate.setManaged(false);
        btnClose.setVisible(false);
        btnClose.setManaged(false);
        if (fnValue == EditMode.READY) {
            btnLedger.setVisible(true);
            btnLedger.setManaged(true);
            btnActivate.setVisible(true);
            btnActivate.setManaged(true);
            btnClose.setVisible(true);
            btnClose.setManaged(true);
            btnUpdate.setVisible(true);
            btnUpdate.setManaged(true);
        }
        if(fnValue == EditMode.UNKNOWN){
            btnClose.setVisible(true);
            btnClose.setManaged(true);
            btnUpdate.setVisible(false);
            btnUpdate.setManaged(false);
        }
        if(fnValue == EditMode.UPDATE){
            dpPicker01.setDisable(true);
            txtField12.setDisable(true);
            txtField13.setDisable(true);
            txtField14.setDisable(true);
            txtField15.setDisable(true);
        
        }
       
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
        txtField10.focusedProperty().addListener(txtField_Focus);
        txtField11.focusedProperty().addListener(txtField_Focus);
        txtField12.focusedProperty().addListener(txtField_Focus);
        txtField13.focusedProperty().addListener(txtField_Focus);
        txtField14.focusedProperty().addListener(txtField_Focus);
        txtField15.focusedProperty().addListener(txtField_Focus);
        
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
            JSONObject poJSON;
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            psActiveField = String.valueOf(lnIndex);
                            poJSON = oCashflow.searchRecordbyAccount(lsValue, true);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oCashflow.getModel().getAccountNo());
                            pnEditMode = EditMode.READY;
                            break;
                        case 02:
                            psActiveField = String.valueOf(lnIndex);
                            poJSON = oCashflow.searchRecordbyAccount(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks02.clear();
                                break;
                            }
                            txtSeeks02.setText((String) oCashflow.getModel().getAccountName());
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
                        poJSON = oCashflow.getModel().setBankAccountId(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField01.requestFocus();
                            txtField01.selectAll();
                            return;
                        }
                        break;
                    case 3:
                        poJSON = oCashflow.getModel().setAccountNo(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField03.requestFocus();
                            txtField03.selectAll();
                            return;
                        }
                        break;
                    case 4:
                        poJSON = oCashflow.getModel().setAccountCode(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField04.requestFocus();
                            txtField04.selectAll();
                            return;
                        }
                        break;
                    case 5:
                        poJSON = oCashflow.getModel().setAccountName(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField05.requestFocus();
                            txtField05.selectAll();
                            return;
                        }
                        break;
                    case 7:
                        poJSON = oCashflow.getModel().setClearingDays(Integer.parseInt(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField07.requestFocus();
                            txtField07.selectAll();
                            return;
                        }
                        break;
                    case 8:
                        poJSON = oCashflow.getModel().setSignatoryCount(Integer.parseInt(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField08.requestFocus();
                            txtField08.selectAll();
                            return;
                        }
                        break;
                    case 9:
                        poJSON = oCashflow.getModel().setSerialNo(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField09.requestFocus();
                            txtField09.selectAll();
                            return;
                        }
                        break;
                    case 10:
                        poJSON = oCashflow.getModel().setCheckNo(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField10.requestFocus();
                            txtField10.selectAll();
                            return;
                        }
                        break;
                    case 11:
                        poJSON = oCashflow.getModel().setRemarks(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField11.requestFocus();
                            txtField11.selectAll();
                            return;
                        }
                        break;
                    case 12:
                        JFXUtil.inputDecimalOnly(txtField12);
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = oCashflow.getModel().setOutstandingBeginningBalance(Double.parseDouble(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField12.requestFocus();
                            txtField12.selectAll();
                            return;
                        }
                        txtField12.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getOutstandingBeginningBalance(), true));
                        break;
                    case 13:
                        JFXUtil.inputDecimalOnly(txtField13);
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = oCashflow.getModel().setOutstandingBalance(Double.parseDouble(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField13.requestFocus();
                            txtField13.selectAll();
                            return;
                        }
                        txtField13.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getOutstandingBalance(), true));
                        break;
                    case 14:
                        JFXUtil.inputDecimalOnly(txtField14);
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = oCashflow.getModel().setAccountBeginningBalance(Double.parseDouble(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField14.requestFocus();
                            txtField14.selectAll();
                            return;
                        }
                        txtField14.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getAccountBeginningBalance(), true));
                        break;
                    case 15:
                        JFXUtil.inputDecimalOnly(txtField15);
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = oCashflow.getModel().setAccountBalance(Double.parseDouble(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Error((String) poJSON.get("message"), pxeModuleName, null);
                            txtField15.requestFocus();
                            txtField15.selectAll();
                            return;
                        }
                        txtField15.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getAccountBalance(), true));
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), pxeModuleName, null);
                try {
                    if (oApp != null) {

                        oApp.rollbackTrans(); // 🔥 force rollback
                    }
                } catch (SQLException ex1) {
                    Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex1);
                }
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

            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 02:
                            poJSON = oCashflow.SearchBanks(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            
                            txtField02.setText((String) oCashflow.getModel().Banks().getBankName());
                            break;
                            
                        case 06:
                            poJSON = oCashflow.SearchBanksBranch(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            txtField06.setText(oCashflow.getModel().getBranch());
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
            boolean lbActive = oCashflow.getModel().getRecordStatus() == "1";

            txtField01.setText(oCashflow.getModel().getBankAccountId());
            txtField02.setText(oCashflow.getModel().Banks().getBankName());
            txtField03.setText(oCashflow.getModel().getAccountNo());
            txtField04.setText(oCashflow.getModel().getAccountCode());
            txtField05.setText(oCashflow.getModel().getAccountName());
            txtField06.setText(oCashflow.getModel().getBranch());
            txtField07.setText( String.valueOf(oCashflow.getModel().getClearingDays()));
            txtField08.setText( String.valueOf(oCashflow.getModel().getSignatoryCount()));
            txtField09.setText(oCashflow.getModel().getSerialNo());
            txtField10.setText(oCashflow.getModel().getCheckNo());
            txtField11.setText(oCashflow.getModel().getRemarks());
            txtField12.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getOutstandingBeginningBalance(), true));
            txtField13.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getOutstandingBalance(), true));
            txtField14.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getAccountBeginningBalance(), true));
            txtField15.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oCashflow.getModel().getAccountBalance(), true));
            dpPicker01.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(oCashflow.getModel().getBeginningBalanceDate(), SQLUtil.FORMAT_SHORT_DATE)));
            switch (oCashflow.getModel().getRecordStatus()) {
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
            switch (oCashflow.getModel().getAccountType()) {
                case "0":
                    cmbField01.getSelectionModel().select(0);
                    break;
                case "1":
                        cmbField01.getSelectionModel().select(1);
                    break;
                case "2":
                    cmbField01.getSelectionModel().select(2);
                    break;
            }
            
            switch (oCashflow.getModel().getSlipType()) {
                case "PS":
                    cmbField02.getSelectionModel().select(0);
                    break;
                case "DS":
                    cmbField02.getSelectionModel().select(1);
                    break;
            }
            
             cbField02.setSelected(oCashflow.getModel().isMonitor());
             cbField03.setSelected(oCashflow.getModel().isDefault());
             cbField04.setSelected(oCashflow.getModel().isBankPrinting());
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BankAccountMasterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            oCashflow.getModel().setRecordStatus("1");
        } else {
            oCashflow.getModel().setRecordStatus("0");
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
