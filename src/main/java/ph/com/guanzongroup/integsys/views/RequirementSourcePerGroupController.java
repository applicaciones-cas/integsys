package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.RequirementsSourcePerGroup;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelListParameter;
import ph.com.guanzongroup.integsys.model.ModelResultSet;
import ph.com.guanzongroup.integsys.model.ModelSalesInquiry_Detail;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class RequirementSourcePerGroupController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Requirement Source Per Group";
    private int pnEditMode, pnMain;
    private RequirementsSourcePerGroup oParameters;
    JSONObject poJSON = new JSONObject();
    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
    private int pnRow = 0;
    private ObservableList<ModelResultSet> data = FXCollections.observableArrayList();
    ObservableList<String> ClientType = ModelSalesInquiry_Detail.ClientType;
    ObservableList<String> cPaymentMode = ModelSalesInquiry_Detail.PurchaseType;
    private ObservableList<ModelListParameter> main_data = FXCollections.observableArrayList();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    JFXUtil.ReloadableTableTask loadTableList;
    private FilteredList<ModelListParameter> filteredData;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    @FXML
    private AnchorPane AnchorMain, apMaster, apSearch;
    @FXML
    private HBox hbButtons;

    @FXML
    private Button btnBrowse,btnSearch,
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
            txtSeeks01;

    @FXML
    private CheckBox cbActive, cbRequired;
    
    @FXML
    private ComboBox cmbCustomerType, cmbPaymentMode;
    @FXML
    private TableView tblViewList;
    
    @FXML
    private TableColumn tblRow, tblRequirement, tblCustomerType, tblPaymentMode;
    
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
        initComboBoxes();
        ClickButton();
        initMainGrid();
        initTableOnClick();
        initLoadTable();
        pbLoaded = true;
        JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        Platform.runLater(() -> {
            btnNew.fire();
        });
    }

    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oParameters = new SalesControllers(oApp, logwrapr).RequirementsSourcePerGroup();
        oParameters.setRecordStatus("0123");
    }

    private void ClickButton() {
        btnSearch.setOnAction(this::handleButtonAction);
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
                        poJSON = oParameters.newRecord();
                        if ("success".equals((String) poJSON.get("result"))) {
                            loadRecord();
                            loadList();
                            txtField02.requestFocus();
                            pnEditMode = oParameters.getEditMode();
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButton(pnEditMode);
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = EditMode.READY;
                        loadRecord();
                        
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
                        initButton(pnEditMode);
                        
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
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apMaster, apSearch);
                        break;
                    case "btnActivate":
                        String id = oParameters.getModel().getRequirementId();
                        if(oParameters.getModel().isActive()){
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this Parameter?") == true) {
                                poJSON = oParameters.deactivateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                }
                                poJSON = oParameters.openRecord(id);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                }
                                clearAllFields();
                                loadRecord();
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            }
                        } else {
                            if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                oParameters.initialize();
                                poJSON = oParameters.activateRecord();
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                }
                                poJSON = oParameters.openRecord(id);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                                    break;
                                }
                                clearAllFields();
                                loadRecord();
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Accounting System", pxeModuleName);
                            }
                        }
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
        txtSeeks01.clear();
        cbActive.setSelected(false);
        cbRequired.setSelected(false);
        main_data.clear();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(!lbShow, btnActivate,btnNew,btnUpdate,btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch,btnCancel,btnSave);
        
        //fields
        JFXUtil.setDisabled(!lbShow, txtField02);
    }

    private void InitTextFields() {
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtField02.setOnKeyPressed(this::txtSeeks_KeyPressed);
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
                            poJson = oParameters.searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.getModel().RequirementSource().getDescription());
                            pnEditMode = EditMode.READY;
                            loadRecord();
                            break;
                        case 02:
                            poJson = oParameters.SearchRequirmentSource(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtField02.clear();
                                break;
                            }
                            loadRecord();
                            loadList();
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
                        if(lsValue.isEmpty()){
                            oParameters.getModel().setRequirementCode("");
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

    private void loadRecord() {
        try {
            txtField01.setText(oParameters.getModel().getRequirementId());
            txtField02.setText(oParameters.getModel().RequirementSource().getDescription());
            cbActive.setSelected(oParameters.getModel().isActive());
            cbRequired.setSelected(oParameters.getModel().isRequired());
            if(oParameters.getModel().isActive()){
                btnActivate.setText("Deactivate");
                faActivate.setGlyphName("CLOSE");
            } else {
                btnActivate.setText("Activate");
                faActivate.setGlyphName("CHECK");
            }
            JFXUtil.setCmbValue(cmbPaymentMode, !oParameters.getModel().getPaymentMode().equals("") ? Integer.valueOf(oParameters.getModel().getPaymentMode()) : -1);
            JFXUtil.setCmbValue(cmbCustomerType, !oParameters.getModel().getCustomerGroup().equals("") ? Integer.valueOf(oParameters.getModel().getCustomerGroup()) : -1);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Information(MiscUtil.getException(ex), "Computerized Acounting System", pxeModuleName);
        }
    }

    @FXML
    void checkBox_Clicked(MouseEvent event) {
        oParameters.getModel().isActive(cbActive.isSelected());
        oParameters.getModel().isRequired(cbRequired.isSelected());
    }
    
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbPaymentMode":
                        poJSON = oParameters.getModel().setPaymentMode(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecord();
                        loadList();
                        break;
                    case "cmbCustomerType":
                        poJSON = oParameters.getModel().setCustomerGroup(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Warning(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecord();
                        loadList();
                        break;
                }
            }
    );


    private void initComboBoxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(cPaymentMode, cmbPaymentMode), 
                                new JFXUtil.Pairs<>(ClientType, cmbCustomerType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbPaymentMode, cmbCustomerType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbPaymentMode, cmbCustomerType);

    }
    public void initLoadTable() {
        loadTableList = new JFXUtil.ReloadableTableTask(
                tblViewList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(100);
                            main_data.clear();
                            if (oParameters.getParameterCount() > 0) {
                                //retreiving using column index
                                for (int lnCtr = 0; lnCtr <= oParameters.getParameterCount() - 1; lnCtr++) {
                                    main_data.add(new ModelListParameter(String.valueOf(lnCtr + 1),
                                            String.valueOf(oParameters.ParameterList(lnCtr).RequirementSource().getDescription()),
                                            String.valueOf(oParameters.ParameterList(lnCtr).getCustomerGroup()),
                                            String.valueOf(oParameters.ParameterList(lnCtr).getPaymentMode()),
                                            String.valueOf(oParameters.ParameterList(lnCtr).getRequirementId())
                                    ));
                                }
                            }

                            if (pnMain < 0 || pnMain
                                    >= main_data.size()) {
                                if (!main_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewList, 0);
                                    pnMain = tblViewList.getSelectionModel().getSelectedIndex();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewList, pnMain);
                            }
                        } catch (InterruptedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });

                });

    }
    

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelListParameter selected = (ModelListParameter) tblViewList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String lsTransNo = selected.getIndex05();
                if (!JFXUtil.loadValidation(pnEditMode, pxeModuleName, oParameters.getModel().getRequirementId(), lsTransNo)) {
                    return;
                }

                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;

                JFXUtil.disableAllHighlightByColor(tblViewList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = oParameters.openRecord(oParameters.ParameterList(pnMain).getRequirementId());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                pnEditMode = oParameters.getEditMode();
                initButton(pnEditMode);
                loadRecord();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    public void loadList() {
        poJSON = new JSONObject();
        System.out.println("customer type : " + cmbCustomerType.getSelectionModel().getSelectedIndex());
        System.out.println("payment mode : " + cmbPaymentMode.getSelectionModel().getSelectedIndex());
        poJSON = oParameters.loadParameterList(String.valueOf(cmbCustomerType.getSelectionModel().getSelectedIndex()),String.valueOf(cmbPaymentMode.getSelectionModel().getSelectedIndex()));
        if ("error".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
            return;
        }
        loadTableList.reload();
    }
    public void initTableOnClick() {
        tblViewList.setOnMouseClicked(event -> {
            pnMain = tblViewList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = oParameters.getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewList, item -> ((ModelListParameter) item).getIndex01(), highlightedRowsMain);
        JFXUtil.adjustColumnForScrollbar(tblViewList);
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRow);
        JFXUtil.setColumnLeft(tblRequirement, tblCustomerType, tblPaymentMode);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewList.setItems(filteredData);
    }

}
