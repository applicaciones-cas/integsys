package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.cas.client.services.ClientControllers;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

public class AccountChartController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "AccountChart";
    private int pnEditMode;
    private CashflowControllers oParameters;
    private boolean state = false;
    private boolean pbLoaded = false;
    private String psPrimary = "";
    private String lbStat = "";
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
            txtField06,
            txtSeeks01;

    @FXML
    private CheckBox cbField01;

    @FXML
    private ComboBox<String> cmbField01, cmbField02, cmbField03;

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
            pnEditMode = oParameters.AccountChart().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();
            initComboBoxField();

            if (oParameters.AccountChart().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new CashflowControllers(oApp, logwrapr);
            oParameters.AccountChart().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
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
                        JSONObject poJSON = oParameters.AccountChart().newRecord();
                        pnEditMode = oParameters.AccountChart().getEditMode();
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = oParameters.AccountChart().getEditMode();
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
                        poJSON = oParameters.AccountChart().searchRecord(lsValue, false);
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
                        poJSON = oParameters.AccountChart().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.AccountChart().getEditMode();
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
                        oParameters.AccountChart().getModel().setModifyingId(oApp.getUserID());
                        oParameters.AccountChart().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.AccountChart().saveRecord();
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
                        String Status = oParameters.AccountChart().getModel().getRecordStatus();
                        String id = oParameters.AccountChart().getModel().getAccountCode();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    oParameters.AccountChart().initialize();
                                    poJsON = oParameters.AccountChart().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.AccountChart().openRecord(id);
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
                                    ShowMessageFX.Information(String.valueOf(oParameters.AccountChart().getEditMode()), "Computerized Accounting System", pxeModuleName);

                                    poJsON = oParameters.AccountChart().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.AccountChart().openRecord(id);
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
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
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
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
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
                        oParameters.AccountChart().getModel().setAccountCode(lsValue);
                        break;
                    case 2:
                        oParameters.AccountChart().getModel().setDescription(lsValue);
                    case 5:
                        oParameters.AccountChart().getModel().setAccountGroup(lsValue);
                    case 6:
                        oParameters.AccountChart().getModel().setReportGroup(lsValue);
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
            ClientControllers oAPClient;
            switch (event.getCode()) {
                case F3:

                    switch (lnIndex) {

                        case 03:

                            CashflowControllers poAccount = new CashflowControllers(oApp, null);
                            poJson = poAccount.AccountChart().searchRecordByIndustry(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.AccountChart().getModel().setParentAccountCode(poAccount.AccountChart().getModel().getAccountCode());
                            txtField03.setText((String) poAccount.AccountChart().getModel().getDescription());
                            break;
                        case 04:
                            poJson = oParameters.TransactionAccountChart().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.AccountChart().getModel().setGLCode(oParameters.Particular().getModel().getParticularID());
                            txtField03.setText((String) oParameters.TransactionAccountChart().getModel().getDescription());
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
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
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
                            poJson = oParameters.AccountChart().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.AccountChart().getModel().getDescription());
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
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            boolean lbActive = oParameters.AccountChart().getModel().getRecordStatus() == "1";

            psPrimary = oParameters.AccountChart().getModel().getAccountCode();

            txtField01.setText(oParameters.AccountChart().getModel().getAccountCode());
            txtField02.setText(oParameters.AccountChart().getModel().getDescription());
            txtField03.setText(oParameters.AccountChart().getModel().ParentAccountChart().getDescription());
            txtField04.setText(oParameters.AccountChart().getModel().General_Ledger().getDescription());
            txtField05.setText(oParameters.AccountChart().getModel().getAccountGroup());
            txtField06.setText(oParameters.AccountChart().getModel().getReportGroup());

            if (oParameters.AccountChart().getModel().getBaseAccount() != null) {
                cmbField01.getSelectionModel().select(Integer.parseInt(oParameters.AccountChart().getModel().getBaseAccount()));
            }
            if (oParameters.AccountChart().getModel().getAccountType() != null) {
                String acctType = oParameters.AccountChart().getModel().getAccountType();

                int lnIndex;
                switch (acctType) {
                    case "0":
                        lnIndex = 0; // Equity
                    case "1":
                        lnIndex = 1; // Liabilities
                    case "E":
                        lnIndex = 2; // Expenses
                    case "R":
                        lnIndex = 3; // Revenue
                    case "A":
                        lnIndex = 4; // Assets
                    default:
                        lnIndex = 1;
                };

                if (lnIndex >= 0) {
                    cmbField02.getSelectionModel().select(lnIndex);
                }
            }
            if (oParameters.AccountChart().getModel().getBalanceType() != null) {
                cmbField03.getSelectionModel().select(Integer.parseInt(oParameters.AccountChart().getModel().getBalanceType()));
            }
            switch (oParameters.AccountChart().getModel().getRecordStatus()) {
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
            Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            try {
                oParameters.AccountChart().getModel().setRecordStatus("1");
            } catch (SQLException ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                oParameters.AccountChart().getModel().setRecordStatus("0");
            } catch (SQLException ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
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

    private void initComboBoxField() {
        cmbField01.setItems(FXCollections.observableArrayList(
                "Cash",
                "Accrual"
        ));
        cmbField02.setItems(FXCollections.observableArrayList(
                "Equity",
                "Liabilities",
                "Expenses",
                "Revenue",
                "Assets"
        ));
        cmbField03.setItems(FXCollections.observableArrayList(
                "Debit",
                "Black Credit"
        ));

        cmbField01.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    oParameters.AccountChart().getModel().setBaseAccount(String.valueOf(lnIndex));
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        cmbField02.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    String lsValue;
                    switch (lnIndex) {
                        case 0:
                            lsValue = "0";
                            break;
                        case 1:
                            lsValue = "1";
                            break;
                        case 2:
                            lsValue = "E";
                            break;
                        case 3:
                            lsValue = "R";
                            break;
                        case 4:
                            lsValue = "A";
                            break;
                        default:
                            lsValue = "1";
                            break;

                    }
                    oParameters.AccountChart().getModel().setBaseAccount(String.valueOf(lsValue));
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        cmbField03.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                try {
                    int lnIndex = newIndex.intValue(); // the selected index
                    oParameters.AccountChart().getModel().setBalanceType(String.valueOf(lnIndex));
                } catch (SQLException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GuanzonException ex) {
                    Logger.getLogger(AccountChartController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

}
