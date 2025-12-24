package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.systables.SystemUser;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelUserManagement;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class UserManagement_Controller implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private SystemUser poSysUser;
//    static CashflowControllers poAPPaymentAdjustmentController;
    private JSONObject poJSON;
    public int pnEditMode;
    private LogWrapper poLogWrapper;
    private unloadForm poUnload = new unloadForm();
    
    private String pxeModuleName = "System User Manager";
    private boolean isGeneral = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psSupplierId = "";
    private String psSearchCompanyId = "";
    private String psSearchSupplierId = "";
    private String psTransactionNo = "";
    
    private String psActiveField = "";
    private static final int ROWS_PER_PAGE = 50;
    int pnMain = 0;
    private boolean pbEntered = false;
    private ObservableList<ModelUserManagement> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelUserManagement> filteredData;
    JFXUtil.ReloadableTableTask loadTableMain;
    ObservableList<String> UserLevel = FXCollections.observableArrayList(
            "ENCODER",
            "SUPERVISOR",
            "BH",
            "AH",
            "DH",
            "AUDIT",
            "SYSADMIN",
            "SYSMASTER"
    );
    ObservableList<String> UserType = FXCollections.observableArrayList("Local", "Global");

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster;
    @FXML
    private TextField tfSearchEmployeeName, tfSearchLogInName, tfUserID, tfLogInName, tfPassword, tfEmployeeName, tfProduct;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnClose,btnStatus,btnEyeIcon;
    @FXML
    private FontAwesomeIconView btnStatusGlyph;
    @FXML
    private ComboBox cmbUserLevel, cmbUserType;
    @FXML
    private TableView tblMain;
    @FXML
    private TableColumn tblModule, tblRole, tblUserLevel;
    @FXML
    private Label lblStatus;
    @FXML
    private CheckBox chbAllowLock,chbAllowView, chbLockStatus;
    @FXML
    private PasswordField pfPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        psIndustryId = ""; // general
        poJSON = new JSONObject();
        
        initObject();
        initLoadTable();
        initTextFields();
        initCheckBox();
        clearTextFields();
        initMainGrid();
        initTableOnClick();
        initComboboxes();
        loadRecordMaster();
        pnEditMode = poSysUser.getEditMode();
        initButton(pnEditMode);
        poSysUser.setRecordStatus("1" + "0");
        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());

    }
    private void initObject() {
        try {
            if (oApp == null) {
                poLogWrapper.severe("UserManagement_Controller.Payee: Application driver is not set.");
                return;
            }
            poSysUser = new SystemUser();
            poSysUser.setApplicationDriver(oApp);
            poSysUser.setWithParentClass(false);
            poSysUser.setLogWrapper(poLogWrapper);
            poSysUser.initialize();
            poJSON = poSysUser.newRecord();
            
//            Platform.runLater(() -> btnNew.fire());

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UserManagement_Controller.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        //Company is not autoset
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No Category
    }

    public void loadTableDetailFromMain() {
    }

    public void loadRecordMaster() {
        
        tfUserID.setText(poSysUser.getModel().getUserId());
        tfLogInName.setText(poSysUser.getModel().getLogName());
        tfPassword.setText(poSysUser.getModel().getPassword());
        tfEmployeeName.setText(poSysUser.getModel().getUserName());
        tfProduct.setText(poSysUser.getModel().getProductId());
        switch (poSysUser.getModel().getUserLevel()) {
            case 1:cmbUserLevel.getSelectionModel().select(0);
                break;
            case 2:
                cmbUserLevel.getSelectionModel().select(1);
                break;
            case 4:
                cmbUserLevel.getSelectionModel().select(2);
                break;
            case 8:
                cmbUserLevel.getSelectionModel().select(3);
                break;
            case 16:
                cmbUserLevel.getSelectionModel().select(4);
                break;
            case 32:
                cmbUserLevel.getSelectionModel().select(5);
                break;
            case 64:
                cmbUserLevel.getSelectionModel().select(6);
                break;
            case 128:
                cmbUserLevel.getSelectionModel().select(7);
                break;
            default:
                throw new AssertionError();
        }
        switch (poSysUser.getModel().getUserType()) {
            case "0":
                cmbUserType.getSelectionModel().select(0);
                break;
            case "1":
                cmbUserType.getSelectionModel().select(1);
                break;
            default:
                throw new AssertionError();
        }
        
        switch (poSysUser.getModel().getUserStatus()) {
            case "0":
               lblStatus.setText("INACTIVE");
                break;
            case "1":
                lblStatus.setText("ACTIVE");
                break;
            default:
                lblStatus.setText("UNKNOWN");
            break;
        }
        
        chbAllowLock.setSelected(poSysUser.getModel().getAllowLock());
        chbAllowView.setSelected(poSysUser.getModel().getAllowView());
        chbLockStatus.setSelected("1".equals(poSysUser.getModel().getLockStatus()));
    }


    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                
                switch (lsButton) {
                    case "btnBrowse":
                        String loValue = "";
                        switch (psActiveField) {
                            case "tfSearchEmployeeName":
                                if (!poSysUser.getModel().getUserName().isEmpty() && !tfSearchEmployeeName.getText().isEmpty()){
                                    poJSON = poSysUser.searchRecord(poSysUser.getModel().getUserId(),true);
                                }else{
                                    loValue = tfSearchEmployeeName.getText();
                                    poJSON = poSysUser.searchRecord(loValue, false);
                                }
                                break;
                            case "tfSearchLogInName":
                                if (!poSysUser.getModel().getLogName().isEmpty() && !tfSearchLogInName.getText().isEmpty()){
                                    poJSON = poSysUser.searchRecord(poSysUser.getModel().getUserId(),true);
                                }else{
                                    loValue = tfSearchLogInName.getText();
                                    poJSON = poSysUser.searchRecord(loValue, false);
                                }
                                break;
                            default:
                                loValue = "";
                                poJSON = poSysUser.searchRecord(loValue,true);
                                break;
                        }
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        loadRecordMaster();
                        pnEditMode = poSysUser.getEditMode();
                        initButton(pnEditMode);
                        tfSearchLogInName.clear();
                        tfSearchEmployeeName.clear();
                        break;
                    
                    case "btnNew":
                        poJSON = poSysUser.newRecord();
                        if("error".equals((String)poJSON.get("result"))){
                            ShowMessageFX.Warning((String)poJSON.get("message"), lsButton, lsButton);
                            break;
                        }
                        
                        pnEditMode = poSysUser.getEditMode();
                        initButton(pnEditMode);
                        initTextFields();
                        loadRecordMaster();
                        
                        FontAwesomeIconView eyeIcons = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                        pfPassword.setText(tfPassword.getText());
                        tfPassword.setVisible(false);
                        pfPassword.setVisible(true);
                        eyeIcons.setIcon(FontAwesomeIcon.EYE_SLASH);
                        eyeIcons.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                        btnEyeIcon.setGraphic(eyeIcons);
                        break;
                    case "btnUpdate":
                        poJSON = poSysUser.updateRecord();
                        if("error".equals((String)poJSON.get("result"))){   
                            ShowMessageFX.Warning((String)poJSON.get("message"), lsButton, lsButton);
                            break;
                        }
                        pnEditMode = poSysUser.getEditMode();
                        initButton(pnEditMode);
                        initTextFields();
                        break;
                    case "btnCancel":
                        if (!ShowMessageFX.YesNo("Are you sure you want to cancel this operation? "
                                + "\nAll unsaved information will be discarded.", pxeModuleName, null)) {
                            return;
                        }
                        clearTextFields();
                        tfSearchLogInName.clear();
                        tfSearchEmployeeName.clear();
                        pnEditMode = EditMode.UNKNOWN;
                        initButton(pnEditMode);
                        initTextFields();
                        
                        break;
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Are you sure you want to close this form?", pxeModuleName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                        break;
                    case "btnEyeIcon":
                        if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
                            btnEyeIcon.setDisable(false);
                            FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                            if (pfPassword.isVisible()) {
                                tfPassword.setText(pfPassword.getText());
                                pfPassword.setVisible(false);
                                tfPassword.setVisible(true);
                                eyeIcon.setIcon(FontAwesomeIcon.EYE);
                                eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                                btnEyeIcon.setGraphic(eyeIcon);
                            } else {
                                pfPassword.setText(tfPassword.getText());
                                tfPassword.setVisible(false);
                                pfPassword.setVisible(true);
                                eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
                                eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                                btnEyeIcon.setGraphic(eyeIcon);
                            }
                        }
                break;
                        
                    case "btnStatus":
                        String userID = poSysUser.getModel().getUserId();
                        String userStatus = poSysUser.getModel().getUserStatus();
                        
                        JSONObject poJSON = "0".equals(userStatus)
                                ? poSysUser.activateRecord()
                                : poSysUser.deactivateRecord();

                        if (!"success".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        
                        String msg = "0".equals(userStatus) ? "User activated successfully." : "User deactivated successfully.";
                        ShowMessageFX.Information(msg, pxeModuleName, null);
                        
                        poJSON = poSysUser.openRecord(userID);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        loadRecordMaster();
                        pnEditMode = poSysUser.getEditMode();
                        initButton(pnEditMode);
                        tfSearchLogInName.clear();
                        tfSearchEmployeeName.clear();
                        break;
                        
                    case "btnSave":
                        if (!ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to save?")) {
                            return;
                        }
                        poJSON = poSysUser.saveRecord();
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        clearTextFields();
                        
                        Platform.runLater(() -> btnNew.fire());
                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(UserManagement_Controller.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = txtField.getId();
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnMain;
            
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchEmployeeName":
                            psActiveField = lsID;
                            poJSON = poSysUser.searchRecord(lsValue, false);
                            if("error".equals(poJSON.get("result"))){
                                ShowMessageFX.Warning((String)poJSON.get("message"), lsValue, lsValue);
                            }
                            tfSearchEmployeeName.setText(poSysUser.getModel().getUserName());
                            return;
                        case "tfSearchLogInName":
                            psActiveField = lsID;
                            poJSON = poSysUser.searchRecord(lsValue, false); //replace this line with  >> searchRecordByEmployee("employee name"); <<
                            if("error".equals(poJSON.get("result"))){
                                ShowMessageFX.Warning((String)poJSON.get("message"), lsValue, lsValue);
                            }
                            tfSearchLogInName.setText(poSysUser.getModel().getLogName());
                            return;
                        case "tfEmployeeName":
                            poJSON = poSysUser.searchEmployee(lsValue, false);
                            if("error".equals(poJSON.get("result"))){
                                ShowMessageFX.Warning((String)poJSON.get("message"), lsValue, lsValue);
                            }
                            tfEmployeeName.setText(poSysUser.getModel().getUserName());
                            return;
                    }
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(UserManagement_Controller.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfLogInName":
                        poSysUser.getModel().setLogName(lsValue);
                        break;
                    case "tfPassword":
                        poSysUser.getModel().setPassword(lsValue);
                        break;
//                    case "tfEmployeeName":
//                        break;
                    case "tfProduct":
                        tfProduct.setText(poSysUser.getModel().getProductId());
                        break;
                }
                psActiveField = "";
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchEmployeeName":
                         psActiveField = lsID;
                         break;
                    case "tfSearchLogInName":
                         psActiveField = lsID;
                        break;
                }
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfLogInName, tfPassword,pfPassword, tfEmployeeName, tfProduct);
        JFXUtil.setFocusListener(txtField_Focus, tfSearchEmployeeName, tfSearchLogInName);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
        if(pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN){
             JFXUtil.setDisabled(true, tfLogInName,tfPassword,pfPassword,tfEmployeeName,tfProduct,
                     cmbUserLevel,cmbUserType,chbAllowLock,chbAllowView,chbLockStatus);
             tfEmployeeName.setPromptText("");
        }
        if(pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
             JFXUtil.setDisabled(false, tfLogInName,tfPassword,pfPassword,tfEmployeeName,tfProduct,
                     cmbUserLevel,cmbUserType,chbAllowLock,chbAllowView,chbLockStatus );
             tfEmployeeName.setPromptText("Press F3: Search");
        }
        
    }
    private void initCheckBox() {
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            chbAllowLock.setOnAction(event -> {
               poSysUser.getModel().setAllowLock(chbAllowLock.isSelected());
            });
            chbAllowView.setOnAction(event -> {
                     poSysUser.getModel().setAllowView(chbAllowView.isSelected());
            });
            chbLockStatus.setOnAction(event -> {
                    poSysUser.getModel().setLockStatus(chbLockStatus.isSelected() ? "1" : "0");
            });
        }
    }

    public void clearTextFields() {
        JFXUtil.setValueToNull(null,  cmbUserLevel, cmbUserType);
        JFXUtil.clearTextFields(apMaster);
        cmbUserType.getSelectionModel().select(-1);
        cmbUserLevel.getSelectionModel().select(-1);
        lblStatus.setText("UNKNOWN");
    }

    public void initMainGrid() {
        JFXUtil.setColumnLeft(tblModule, tblRole, tblUserLevel);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMain);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblMain.setItems(filteredData);
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblMain,
                main_data,
                () -> {
                    main_data.clear();
                    Platform.runLater(() -> {
//                        Thread.sleep(100);
//                        if (poController.PurchaseOrderReceiving().getPurchaseOrderReturnCount() > 0) {
                        for (int lnCtr = 0; lnCtr <= 5 - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelUserManagement("",
                                        String.valueOf(""),
                                        String.valueOf("")
                                ));
                            } catch (Exception e) {
                            }

                        }

                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblMain.getSelectionModel().select(0);
                                tblMain.getFocusModel().focus(0);
                                pnMain = tblMain.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblMain.getSelectionModel().select(pnMain);
                            tblMain.getFocusModel().focus(pnMain);

                        }

                    });

                });
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbUserLevel":
                        String userLevelCode = getUserLevelCode((String) selectedValue);
                        if (userLevelCode != null) {
                            poSysUser.getModel().setUserLevel(Integer.valueOf(userLevelCode));
                        }
                        break;
                    case "cmbUserType":
                        poSysUser.getModel().setUserType(String.valueOf(cmbUserType.getSelectionModel().getSelectedIndex()));
                        break;
                }
//                loadRecordMaster();
            }
    );
    
    private String getUserLevelCode(String userLevelName) {
        switch (userLevelName) {
            case "ENCODER":    return "1";
            case "SUPERVISOR": return "2";
            case "BH":         return "4";
            case "AH":         return "8";
            case "DH":         return "16";
            case "AUDIT":      return "32";
            case "SYSADMIN":   return "64";
            case "SYSMASTER":  return "128";
            default:           return null;
        }
    }

    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(UserLevel, cmbUserLevel), new JFXUtil.Pairs<>(UserType, cmbUserType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbUserLevel, cmbUserType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbUserLevel, cmbUserType);
    }

    public void initTableOnClick() {
        tblMain.setOnMouseClicked(event -> {
            pnMain = tblMain.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 1) {
                    loadTableDetailFromMain();
//                    pnEditMode = poAPPaymentAdjustmentController.APPaymentAdjustment().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.adjustColumnForScrollbar(tblMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);
        if(fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE) btnEyeIcon.setDisable(false);
        if (lbShow3) {
            FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
            pfPassword.setText(tfPassword.getText());
            tfPassword.setVisible(false);
            pfPassword.setVisible(true);
            eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
            eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
            btnEyeIcon.setGraphic(eyeIcon);
        }
        if((pnEditMode == EditMode.READY ) && !poSysUser.getModel().getUserId().isEmpty()){
            btnStatus.setVisible(true);
            btnStatus.setManaged(true);
            switch (poSysUser.getModel().getUserStatus()) {
                
                case "0": // Inactive → show Activate
                    btnStatus.setText("Activate");
                    btnStatusGlyph.setGlyphName("UNLOCK");
                    break;

                case "1": // Active → show Deactivate
                    btnStatus.setText("Deactivate");
                    btnStatusGlyph.setGlyphName("LOCK");
                    break;

                default:
                    btnStatus.setText("Unknown");
                    btnStatusGlyph.setGlyphName("QUESTION");
                    break;
            }
        }else{
            btnStatus.setVisible(false);
            btnStatus.setManaged(false);
        }
    }

}
