package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.WithholdingTax;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.sales.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class WithholdingTaxController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Withholding Tax";
    private int pnEditMode;
    private WithholdingTax poController;
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    JSONObject poJSON = new JSONObject();
    ObservableList<String> comboboxlist = FXCollections.observableArrayList("WTC", "EWT", "FWT", "GMP/WGV", "FBT");

    @FXML
    private AnchorPane AnchorMain, apMaster;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnClose;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private TextField tfTaxRateID, tfTaxDescription, tfAccountName, tfTaxCode, tfTaxRate, tfSearchTaxDescription;
    @FXML
    private CheckBox cbActive;
    @FXML
    private ComboBox cmbTaxType;

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

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poController = new CashflowControllers(oApp, logwrapr).WithholdingTax();
            poController.setRecordStatus("0123");
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeObject();
        pnEditMode = poController.getEditMode();
        initButton(pnEditMode);
        initTextFields();
        initCombobox();
        pbLoaded = true;

        if (poController.getEditMode() == EditMode.ADDNEW) {
            initButton(pnEditMode);
            loadRecordMaster();
        }

    }

    private void loadRecordSearch() {
        tfSearchTaxDescription.setText(poController.getModel().getDescription());
    }

    private void loadRecordMaster() {
        try {

            tfTaxRateID.setText(poController.getModel().getTaxRateId());
            tfTaxDescription.setText(poController.getModel().getDescription());
            tfAccountName.setText(poController.getModel().AccountChart().getDescription());
            tfTaxCode.setText(poController.getModel().getTaxCode());
            JFXUtil.setCmbValue(cmbTaxType, poController.getModel().getTaxType());
            tfTaxRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getTaxRate(), false));

            switch (poController.getModel().getRecordStatus()) {
                case "1":
                    btnActivate.setText("Deactivate");
                    btnActivate.setMinWidth(80);
                    faActivate.setGlyphName("CLOSE");
                    cbActive.setSelected(true);
                    break;
                case "0":
                    btnActivate.setText("Activate");
                    btnActivate.setMinWidth(70);
                    faActivate.setGlyphName("CHECK");
                    cbActive.setSelected(false);
                    break;
            }
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void cmdButton_Click(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        JSONObject poJSON = poController.newRecord();
                        pnEditMode = EditMode.READY;
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = EditMode.ADDNEW;
                            initButton(pnEditMode);
                            loadRecordMaster();
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "btnBrowse":
                        poController.setRecordStatus("0123");
                        poJSON = poController.searchRecord("", false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTaxDescription.clear();
                            break;
                        }
                        tfSearchTaxDescription.setText("");
                        pnEditMode = EditMode.READY;
                        loadRecordMaster();
                        break;
                    case "btnUpdate":
                        poJSON = poController.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        pnEditMode = poController.getEditMode();
                        initButton(pnEditMode);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to disregard changes?")) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                        }
                        break;
                    case "btnSave":
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save?")) {
                            poController.getModel().setModifyingBy(oApp.getUserID());
                            poController.getModel().setModifiedDate(oApp.getServerDate());
                            JSONObject saveResult = poController.saveRecord();
                            if ("success".equals((String) saveResult.get("result"))) {
                                ShowMessageFX.Information(null, pxeModuleName, (String) saveResult.get("message"));
                                pnEditMode = EditMode.UNKNOWN;
                                initButton(pnEditMode);
                                clearAllFields();
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) saveResult.get("message"));
                                return;
                            }
                            Platform.runLater(() -> {
                                btnNew.fire();
                            });
                        }
                        break;
                    case "btnActivate":
                        String Status = poController.getModel().getRecordStatus();
                        String id = poController.getModel().getTaxCode();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this parameter?") == true) {
                                    poJsON = poController.activateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information(null, pxeModuleName, (String) poJsON.get("message"));
                                        return;
                                    } else {
                                        ShowMessageFX.Information(null, pxeModuleName, "Record Activated successfully");
                                    }
                                    tfSearchTaxDescription.setText("");
                                    poController.initialize();
                                    clearAllFields();
                                    loadRecordMaster();
                                }
                                break;
                            case "1":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this parameter?") == true) {
                                    poJsON = poController.deactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information(null, pxeModuleName, (String) poJsON.get("message"));
                                        return;
                                    } else {
                                        ShowMessageFX.Information(null, pxeModuleName, "Record Deactivated successfully");
                                    }
                                    tfSearchTaxDescription.setText("");
                                    poController.initialize();
                                    clearAllFields();
                                    loadRecordMaster();
                                }
                                break;
                        }
                        pnEditMode = EditMode.UNKNOWN;
                        break;
                }
                initButton(pnEditMode);
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbTaxType":
                        poJSON = poController.getModel().setTaxType(selectedValue.toString());
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });

    private void initCombobox() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(comboboxlist, cmbTaxType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbTaxType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbTaxType);
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, AnchorMain);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, AnchorMain);
        JFXUtil.inputDecimalOnly(tfTaxRate);
    }
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfTaxDescription":
                        poJSON = poController.getModel().setDescription(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfAccountName":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setAccountCode(lsValue);
                        }
                        break;
                    case "tfTaxCode":
                        if (lsValue.isEmpty()) {
                            poController.getModel().setTaxCode(lsValue);
                        }
                        break;
                    case "tfTaxRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.getModel().setTaxRate(Double.valueOf(lsValue));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "tfSearchTaxDescription":
                        break;
                }
                if (!lsID.equals("tfSearchTaxDescription")) {
                    loadRecordMaster();

                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case F3:
                        switch (lsID) {
                            //AnchorMain
                            case "tfSearchTaxDescription":
                                poController.setRecordStatus("0123");
                                poJSON = poController.searchRecord(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                }

                                loadRecordSearch();
                                loadRecordMaster();
                                pnEditMode = poController.getEditMode();
                                initButton(pnEditMode);
                                break;
                            case "tfAccountName":
                                poJSON = poController.SearchAccountName(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(tfTaxCode);
                                }
                                loadRecordMaster();
                                break;
                            case "tfTaxCode":
                                poJSON = poController.SearchTaxCode(lsValue, false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    JFXUtil.textFieldMoveNext(cmbTaxType);
                                }
                                loadRecordMaster();
                                break;

                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearAllFields() {
        JFXUtil.clearTextFields(apMaster);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(lbShow, btnCancel, btnSave);
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse, btnNew);
        JFXUtil.setButtonsVisibility(false, btnUpdate);

        btnClose.setVisible(true);
        btnClose.setManaged(true);

        JFXUtil.setDisabled(!lbShow, apMaster);
        JFXUtil.setButtonsVisibility(false, btnActivate);
        if (fnValue != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getRecordStatus()) {
            case "1":
                JFXUtil.setButtonsVisibility(true, btnActivate);
                JFXUtil.setButtonsVisibility(true, btnUpdate);
                break;
            case "0":
                JFXUtil.setButtonsVisibility(true, btnActivate);
                break;
        }

    }

}
