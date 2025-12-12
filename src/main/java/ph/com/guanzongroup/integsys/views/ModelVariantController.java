package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelResultSet;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class ModelVariantController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Model Variant";
    private int pnEditMode;
    private ParamControllers oParameters;

    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    JSONObject poJSON = new JSONObject();
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster, apBrowse;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnNew, btnSave, btnUpdate, btnCancel, btnClose;
    @FXML
    private Node faActivate;
    @FXML
    private TextField tfModelVariantID, tfDescription, tfYearModel, tfSellPrice, tfColor, tfSearchModelVariantName, tfModel;
    @FXML
    private CheckBox cbActive;

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
            pnEditMode = oParameters.ModelVariant().getEditMode();
            initButton(pnEditMode);
            InitTextFields();
            ClickButton();
            initTabAnchor();

            if (oParameters.ModelVariant().getEditMode() == EditMode.ADDNEW) {
                initButton(pnEditMode);
                initTabAnchor();
                loadRecord();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            oParameters = new ParamControllers(oApp, logwrapr);
            oParameters.ModelVariant().setRecordStatus("0123");
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
                        tfDescription.requestFocus();
                        JSONObject poJSON = oParameters.ModelVariant().newRecord();
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
                        String lsValue = (tfSearchModelVariantName.getText() == null) ? "" : tfSearchModelVariantName.getText();
                        poJSON = oParameters.ModelVariant().searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            tfSearchModelVariantName.clear();
                            break;
                        }
                        pnEditMode = EditMode.READY;
                        loadRecord();
                        initTabAnchor();
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.ModelVariant().updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.ModelVariant().getEditMode();
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
                        oParameters.ModelVariant().getModel().setModifyingId(oApp.getUserID());
                        oParameters.ModelVariant().getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.ModelVariant().saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearAllFields();
                        } else {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;

                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        JFXUtil.clearTextFields(apMaster);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(lbShow, btnCancel, btnSave);
        JFXUtil.setButtonsVisibility(!lbShow, btnUpdate, btnBrowse, btnNew);
    }

    private void InitTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfModelVariantID, tfDescription, tfYearModel, tfSellPrice, tfColor, tfSearchModelVariantName, tfModel);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apBrowse);
        JFXUtil.setCommaFormatter(tfSellPrice);
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchModelVariantName":
                            poJSON = oParameters.ModelVariant().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            } else {

                            }
//                            oParameters.ModelVariant().getModel().setModelId(oParameters.ModelVariant().getDescription());
                            loadRecordBrowse();
                            break;
                        case "tfModel":
                            poJSON = oParameters.Model().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
//                            oParameters.ModelVariant().getModel().setModelId(oParameters.Model().getModel().getModelId());
                            break;
                        case "tfColor":
                            poJSON = oParameters.Color().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oParameters.ModelVariant().getModel().setColorId(oParameters.ModelVariant().getModel().Color().getColorId());
                            break;
                    }
                    loadRecord();
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfSearchModelVariantName":
                            if (lsValue.isEmpty()) {
                                oParameters.ModelVariant().getModel().setVariantId(lsValue);
                            }
                            loadRecordBrowse();
                            break;
                        case "tfModel":
                            if (lsValue.isEmpty()) {
                                oParameters.ModelVariant().getModel().setModelId(lsValue);
                            }
                            break;
                        case "tfModelVariantID":
                            oParameters.ModelVariant().getModel().setVariantId(lsValue);
                            break;
                        case "tfDescription":
                            oParameters.ModelVariant().getModel().setDescription(lsValue);
                            break;
//                        case "tfYearModel":
//                            oParameters.ModelVariant().getModelVariant().setYearModel(lsValue);
//                            break;
                        case "tfSellPrice":
                            lsValue = JFXUtil.removeComma(lsValue);
                            oParameters.ModelVariant().getModel().setSellingPrice(Double.parseDouble(lsValue));
                            break;
                        case "tfColor":
                            if (lsValue.isEmpty()) {
                                oParameters.ModelVariant().getModel().setColorId(lsValue);
                            }
                            break;
                    }
                    loadRecord();
                } catch (Exception e) {
                    System.err.println("Error processing input: " + e.getMessage());
                }
            });

    private void loadRecordBrowse() {
        try {
            tfSearchModelVariantName.setText(oParameters.ModelVariant().getModel().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecord() {
        try {
            tfModelVariantID.setText(oParameters.ModelVariant().getModel().getModelId());
            cbActive.setSelected(oParameters.ModelVariant().getModel().getRecordStatus().equals("1") ? true : false);
            tfDescription.setText(oParameters.ModelVariant().getModel().getDescription());
            tfYearModel.setText(String.valueOf(oParameters.ModelVariant().getModel().getYearModel()));
            tfSellPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(oParameters.ModelVariant().getModel().getSellingPrice()));
            tfColor.setText(oParameters.Color().getModel().getDescription());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        try {
            Object source = event.getSource();
            if (source instanceof CheckBox) {
                CheckBox checkedBox = (CheckBox) source;
                switch (checkedBox.getId()) {
                    case "cbActive": // this is the id
                        if (cbActive.isSelected()) {
                            oParameters.ModelVariant().getModel().setRecordStatus("1");
                        } else {
                            oParameters.ModelVariant().getModel().setRecordStatus("0");
                        }
                        break;
                }
                loadRecord();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
