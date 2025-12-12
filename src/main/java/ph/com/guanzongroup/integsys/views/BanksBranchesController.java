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
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelResultSet;

public class BanksBranchesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Banks Branches";
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
            pnEditMode = oParameters.BanksBranch().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();

            if (oParameters.BanksBranch().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr);
            oParameters.BanksBranch().setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
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
                        JSONObject poJSON = oParameters.BanksBranch().newRecord();
                        pnEditMode = EditMode.READY;
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = EditMode.ADDNEW;
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
                        poJSON = oParameters.BanksBranch().searchRecord(lsValue, false);
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
                        poJSON = oParameters.BanksBranch().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.BanksBranch().getEditMode();
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
                        oParameters.BanksBranch().getModel().setModifyingId(oApp.getUserID());
                        oParameters.BanksBranch().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.BanksBranch().saveRecord();
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
                        String Status = oParameters.BanksBranch().getModel().getRecordStatus();
                        String id = oParameters.BanksBranch().getModel().getBankID();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    poJsON = oParameters.BanksBranch().activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.BanksBranch().openRecord(id);
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
                                    poJsON = oParameters.BanksBranch().deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.BanksBranch().openRecord(id);
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
                Logger.getLogger(BanksController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtSeeks01.clear();
        cbField01.setSelected(false);
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

        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
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
                            poJson = oParameters.BanksBranch().searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.BanksBranch().getModel().getBranchBankName());
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
        } catch (SQLException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
                        case 04:
                            poJson = oParameters.Banks().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.BanksBranch().getModel().setBankID(oParameters.Banks().getModel().getBankID());
                            txtField03.setText((String) oParameters.InventoryType().getModel().getDescription());
                            break;
                        case 07:
                            poJson = oParameters.TownCity().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.BanksBranch().getModel().setTownID(oParameters.TownCity().getModel().getTownId());
                            txtField04.setText((String) oParameters.Category().getModel().getDescription());
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
        } catch (SQLException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
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
                        oParameters.BanksBranch().getModel().setBankID(lsValue);
                        break;
                    case 2:
                        oParameters.BanksBranch().getModel().setBranchBankCode(lsValue);
                        break;
                    case 3:
                        oParameters.BanksBranch().getModel().setBranchBankName(lsValue);
                        break;
//                    case 4:
//                        oParameters.BanksBranch().getModel().setB(lsValue);
//                        break;
                    case 5:
                        oParameters.BanksBranch().getModel().setContactPerson(lsValue);
                        break;
                    case 6:
                        oParameters.BanksBranch().getModel().setAddress(lsValue);
                        break;
//                    case 7:
//                        oParameters.BanksBranch().getModel().setDescription(lsValue);
//                        break;
                    case 8:
                        oParameters.BanksBranch().getModel().setTelephoneNo(lsValue);
                        break;
                    case 9:
                        oParameters.BanksBranch().getModel().setFaxNo(lsValue);
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
        try {
            boolean lbActive = oParameters.BanksBranch().getModel().getRecordStatus() == "1";

            txtField01.setText(oParameters.BanksBranch().getModel().getBankID());
            txtField02.setText(oParameters.BanksBranch().getModel().getBranchBankName());
            txtField03.setText(oParameters.BanksBranch().getModel().getBranchBankName());
            txtField04.setText(oParameters.BanksBranch().getModel().Banks().getBankName());
            txtField05.setText(oParameters.BanksBranch().getModel().getContactPerson());
            txtField06.setText(oParameters.BanksBranch().getModel().getAddress());
            txtField07.setText(oParameters.BanksBranch().getModel().TownCity().getDescription());
            txtField08.setText(oParameters.BanksBranch().getModel().getTelephoneNo());
            txtField09.setText(oParameters.BanksBranch().getModel().getFaxNo());

            switch (oParameters.BanksBranch().getModel().getRecordStatus()) {
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
        } catch (SQLException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            try {
                oParameters.BanksBranch().getModel().setRecordStatus("1");
            } catch (SQLException ex) {
                Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                oParameters.BanksBranch().getModel().setRecordStatus("0");
            } catch (SQLException ex) {
                Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(BanksBranchesController.class.getName()).log(Level.SEVERE, null, ex);
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
