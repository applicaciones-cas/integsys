/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.json.simple.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.tbjhandler.constant.TBJ_Constant;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * Controller class for the Document Mapping UI in the Integsys application.
 * <p>
 * This class manages the interaction between the JavaFX UI elements and the
 * underlying business logic implemented in {@link CashflowControllers}.
 * It handles CRUD operations (Create, Read, Update, Delete) for document
 * mapping parameters and their details, including activation and deactivation
 * of records.
 * </p>
 * <p>
 * The controller implements {@link Initializable} to initialize the UI components
 * and {@link ScreenInterface} to receive application context such as GRider instance,
 * Industry ID, Company ID, and Category ID.
 * </p>
 * <p>
 * UI Elements managed include:
 * <ul>
 *   <li>TextFields for master and detail input</li>
 *   <li>CheckBoxes for flags such as active, fixed value, multiple</li>
 *   <li>Buttons for CRUD actions and form navigation</li>
 *   <li>TableView for displaying details</li>
 * </ul>
 * </p>
 * <p>
 * The controller also provides focus listeners, key event handlers, and table
 * click handling to synchronize UI changes with the model.
 * </p>
 *
 * @author Teejei
 * @version 1.0
 * @since 2026-04-07
 */
public class DocumentMappingController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private CashflowControllers poCashflowController;
    private String psFormName = "Documment Mapping";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private int pnSelectedDetail = 0;
    private String psActiveField = "";
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    unloadForm poUnload = new unloadForm();
    @FXML
    private AnchorPane AnchorMain,
            apDetail, apMaster;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnSave,
            btnUpdate,
            btnCancel,
            btnActivate,
            btnDeactivate,
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
            txtField10,
            txtField11,
            txtField12;

    @FXML
    private TextField txtSeeks01,
            txtSeeks02;

    @FXML
    private CheckBox cbField01,
            cbField02,
            cbField03;



    @FXML
    private TableView<ModelTableDetail> tblDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> index00,
        index01,
        index02,
        index03;

    /**
     * Sets the GRider application driver instance.
     *
     * * @param foValue The GRider instance.
     */
    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    /**
     * Sets the Industry ID for the transaction context.
     *
     * * @param fsValue The industry ID.
     */
    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    /**
     * Sets the Company ID for the transaction context.
     *
     * * @param fsValue The company ID.
     */
    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    /**
     * Sets the Category ID for the transaction context.
     *
     * * @param fsValue The category ID.
     */
    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

        /**
     * Initializes the controller after the root element has been completely
     * processed. This method sets up all UI elements, buttons, fields,
     * and table details.
     *
     * @param url            the location used to resolve relative paths
     * @param rb             the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        ClearAll();
        initializeObject();
        initButtonsClickActions();
        initFields();
        initTableDetail();
        initCheckBox();
        btnNew.fire();
    }

        /**
     * Initializes the business logic objects and prepares a new transaction.
     * Loads the Cashflow controller and sets default transaction status.
     */
    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poCashflowController = new CashflowControllers(poApp, logWrapper);
            poJSON = poCashflowController.DocumentMapping().InitTransaction();
            poCashflowController.DocumentMapping().setTransactionStatus("10");
//            poCashflowController.DocumentMapping().Master().setIndustryID(psIndustryID);
//            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BrandController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

       /**
     * Initializes the button visibility, state, and edit mode
     * depending on the current {@link EditMode}.
     *
     * @param fnEditMode the current edit mode
     */
    private void initButtons(int fnEditMode) {
        try {
            boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);

            CustomCommonUtil.setVisible(!lbShow, btnBrowse, btnClose, btnNew);
            CustomCommonUtil.setManaged(!lbShow, btnBrowse, btnClose, btnNew);

            CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel, btnActivate,btnDeactivate);
            CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel, btnActivate,btnDeactivate);

            CustomCommonUtil.setVisible(false, btnUpdate, btnActivate,btnDeactivate);
            CustomCommonUtil.setManaged(false, btnUpdate, btnActivate,btnDeactivate);
            if (fnEditMode == EditMode.READY) {
                CustomCommonUtil.setVisible(true, btnUpdate);
                CustomCommonUtil.setManaged(true, btnUpdate);
                if (poCashflowController.DocumentMapping().Master().getTransactionStatus().equals("0")) {
                    CustomCommonUtil.setVisible(true, btnActivate);
                    CustomCommonUtil.setManaged(true, btnActivate);
                    CustomCommonUtil.setVisible(false, btnDeactivate);
                    CustomCommonUtil.setManaged(false, btnDeactivate);
                } else if(poCashflowController.DocumentMapping().Master().getTransactionStatus().equals("1")) {
                    CustomCommonUtil.setVisible(true, btnDeactivate);
                    CustomCommonUtil.setManaged(true, btnDeactivate);
                    CustomCommonUtil.setVisible(false, btnActivate);
                    CustomCommonUtil.setManaged(false, btnActivate);
                }
            }
            if(poCashflowController.DocumentMapping().Master().getTransactionStatus().equals(TBJ_Constant.VOID)){
                CustomCommonUtil.setVisible(false, btnUpdate);
                CustomCommonUtil.setManaged(false, btnUpdate);
            }
            
            if (pnEditMode == EditMode.ADDNEW) {
                JFXUtil.setDisabledExcept(false,
                        apMaster);
                JFXUtil.setDisabledExcept(false,
                        apDetail);
                txtSeeks01.setDisable(true);
                txtSeeks02.setDisable(true);
            } else if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN) {
                JFXUtil.setDisabledExcept(true,
                        apMaster);
                JFXUtil.setDisabledExcept(true,
                        apDetail);
                
                txtSeeks01.setDisable(false);
                txtSeeks02.setDisable(false);
            } else if (pnEditMode == EditMode.UPDATE) {
                JFXUtil.setDisabledExcept(false,
                        apMaster);
                JFXUtil.setDisabledExcept(false,
                        apDetail);
                txtSeeks01.setDisable(true);
                txtSeeks02.setDisable(true);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Handles button click actions including CRUD operations,
     * activation/deactivation, and form closure.
     *
     * @param event the {@link ActionEvent} triggered by a button
     */
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnClose,btnActivate,btnDeactivate);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

        /**
     * Handles button click actions including CRUD operations,
     * activation/deactivation, and form closure.
     *
     * @param event the {@link ActionEvent} triggered by a button
     */
    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                case "btnBrowse":
                    switch (psActiveField) {
                        case "txtSeeks01":
                        case "":
                            poJSON = poCashflowController.DocumentMapping().SearchTransaction(txtSeeks01.getText(), psActiveField);
                            break;
                        case "txtSeeks02":
                            poJSON = poCashflowController.DocumentMapping().SearchTransaction(txtSeeks02.getText(), psActiveField);
                            break;
                        default:
                            poJSON = poCashflowController.DocumentMapping().SearchTransaction(txtSeeks01.getText(), psActiveField);
                            break;
                    }
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poCashflowController.DocumentMapping().getEditMode();
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    initButtons(pnEditMode);
                    Platform.runLater(() -> {
                        if (!tblDetails.getItems().isEmpty()) {
                            tblDetails.getSelectionModel().selectFirst();
                            tblDetails.getFocusModel().focus(0);
                            tblDetails.scrollTo(0);
                        }
                    });
                    break;
                case "btnNew":
                    ClearAll();
                    poJSON = poCashflowController.DocumentMapping().NewTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poCashflowController.DocumentMapping().getEditMode();

                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    initButtons(pnEditMode);
                     Platform.runLater(() -> {
                        if (!tblDetails.getItems().isEmpty()) {
                            tblDetails.getSelectionModel().selectFirst();
                            tblDetails.getFocusModel().focus(0);
                            tblDetails.scrollTo(0);
                        }
                    });
                    break;
                case "btnUpdate":
                    if(poCashflowController.DocumentMapping().Master().getDocumentCode()== null ||
                            poCashflowController.DocumentMapping().Master().getDocumentCode().isEmpty()){
                        ShowMessageFX.Warning("No transaction was loaded. ", psFormName, null);
                        return;
                    }
                    
                    if (poApp.getUserLevel() < UserRight.SYSADMIN) {
                        ShowMessageFX.Warning("User is not allowed to modify the record. ", psFormName, null);
                        return;
                    }
                    
                    poJSON = poCashflowController.DocumentMapping().UpdateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poCashflowController.DocumentMapping().getEditMode();
                    LoadMaster();
                    LoadDetail();
                    loadTableDetail();
                    initButtons(pnEditMode);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo("Are you sure you want to cancel? \nAny data you have entered will not be saved.", psFormName,null )) {
                        ClearAll();
                        initializeObject();
                        pnEditMode = EditMode.UNKNOWN;
                        initButtons(pnEditMode);
                    }
                    break;
                case "btnSave":
                    poJSON = poCashflowController.DocumentMapping().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    ClearAll();
                    detail_data.clear();
                    btnNew.fire();
                    break;

                case "btnActivate":
                    if (!ShowMessageFX.YesNo("Are you sure you want to activate this parameter?", psFormName,null )) {
                        return;
                    }
                    poJSON = poCashflowController.DocumentMapping().ActivateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    ClearAll();
                    detail_data.clear();
                    initializeObject();
                    pnEditMode = EditMode.UNKNOWN;
                    initButtons(pnEditMode);
                break;
                case "btnDeactivate":
                    if (!ShowMessageFX.YesNo("Are you sure you want to deactivate this parameter?", psFormName,null )) {
                        return;
                    }
                     poJSON = poCashflowController.DocumentMapping().DeactivateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    ClearAll();
                    detail_data.clear();
                    initializeObject();
                    pnEditMode = EditMode.UNKNOWN;
                    initButtons(pnEditMode);
                break;

                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, psFormName);
        } catch (ParseException ex) {
            Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        /**
     * Initializes the TextFields, Seek fields, focus listeners, and key events.
     * Handles navigation and searching on key press.
     */
    private void initFields() {
        //        try {
        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!isEditable,
                txtField01,
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
                txtField12
        );
        List<TextField> loTxtField = Arrays.asList(txtSeeks01,
                txtSeeks02);
        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
        JFXUtil.setFocusListener(txtField_Focus, txtField01,
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
                txtField12);
        JFXUtil.setFocusListener(txSeeks_Focus, txtSeeks01,
                txtSeeks02);
        tblDetails.setOnMouseClicked(this::tblDetails_Clicked);

    }
        /**
     * Handles key pressed events for TextFields to perform search
     * or navigation depending on the key code.
     *
     * @param event the {@link KeyEvent} triggered by a TextField
     */
    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        
                    case F3:
                        switch (txtFieldID) {
                            case "txtSeeks01":
                                poJSON = poCashflowController.DocumentMapping().SearchTransaction(txtSeeks01.getText(), "txtSeeks01");
                                break;
                            case "txtSeeks02":
                                poJSON = poCashflowController.DocumentMapping().SearchTransaction(txtSeeks02.getText(), "txtSeeks02");
                                break;
                        }
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            return;
                        }
                        pnEditMode = poCashflowController.DocumentMapping().getEditMode();
                        loadTableDetail();
                        LoadMaster();
                        LoadDetail();
                        Platform.runLater(() -> {
                            if (!tblDetails.getItems().isEmpty()) {
                                tblDetails.getSelectionModel().selectFirst();
                                tblDetails.getFocusModel().focus(0);
                                tblDetails.scrollTo(0);
                            }
                        });
                        break;
                    case UP:
                        break;
                    case DOWN:
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException | ExceptionInInitializerError ex) {
                Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), psFormName, null);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
        
     ChangeListener<Boolean> txSeeks_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "txtSeeks01":
                        psActiveField = lsID;
                        break;
                    case "txtSeeks02":
                        psActiveField = lsID;
                        break;
                }
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                try {
                    switch (lsID) {
                        case "txtField01":
                            poJSON = poCashflowController.DocumentMapping().Master().setDocumentCode(lsValue);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField01.requestFocus();
                                txtField01.selectAll();
                                return;
                            }
                            break;
                        case "txtField02":
                            poJSON = poCashflowController.DocumentMapping().Master().setDesciption(lsValue);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField02.requestFocus();
                                txtField02.selectAll();
                                return;
                            }
                            break;
                        case "txtField03":
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setFieldCode(lsValue);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField03.requestFocus();
                                txtField03.selectAll();
                                return;
                            }
                            break;
                        case "txtField04":
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setFontName(lsValue);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField04.requestFocus();
                                txtField04.selectAll();
                                return;
                            }
                            break;  
                        case "txtField05":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0";
                            }
                            JFXUtil.inputIntegersOnly(txtField05);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setFontSize(Integer.parseInt(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField05.requestFocus();
                                txtField05.selectAll();
                                return;
                            }
                            txtField05.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFontSize()));

                            break;  
                        case "txtField06":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0.00";
                            }
                            JFXUtil.inputDecimalOnly(txtField06);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setTopRow(Double.parseDouble(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField06.requestFocus();
                                txtField06.selectAll();
                                return;
                            }
                            txtField06.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getTopRow(), false));
                            break; 
                        case "txtField07":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0.00";
                            }
                            JFXUtil.inputDecimalOnly(txtField07);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setRowSpace(Double.parseDouble(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField07.requestFocus();
                                txtField07.selectAll();
                                return;
                            }
                            txtField07.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getRowSpace(), false));
                            break;     
                        case "txtField08":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0.00";
                            }
                            JFXUtil.inputDecimalOnly(txtField08);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setLeftColumn(Double.parseDouble(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField08.requestFocus();
                                txtField08.selectAll();
                                return;
                            }
                            txtField08.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getLeftColumn(), false));
                            break;     
                        case "txtField09":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0.00";
                            }
                            JFXUtil.inputDecimalOnly(txtField09);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setColumnSpace(Double.parseDouble(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField09.requestFocus();
                                txtField09.selectAll();
                                return;
                            }
                            txtField09.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getColumnSpace(), false));
                            break;      
                        case "txtField10":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0";
                            }
                            JFXUtil.inputIntegersOnly(txtField10);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setPageLocation(Integer.parseInt(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField10.requestFocus();
                                txtField10.selectAll();
                                return;
                            }
                            txtField10.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getPageLocation()));
                            break;     
                        case "txtField11":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0";
                            }
                            JFXUtil.inputIntegersOnly(txtField11);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setMaxLength(Integer.parseInt(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField11.requestFocus();
                                txtField11.selectAll();
                                return;
                            }
                            txtField11.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getMaxLength()));
                            break;      
                        case "txtField12":
                            if(lsValue == null || lsValue.isEmpty()){
                                lsValue = "0";
                            }
                            JFXUtil.inputIntegersOnly(txtField12);
                            poJSON = poCashflowController.DocumentMapping().Detail(pnSelectedDetail).setMaxRow(Integer.parseInt(lsValue));
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                                txtField12.requestFocus();
                                txtField12.selectAll();
                                return;
                            }
                            txtField12.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getMaxRow()));
                            break;    
                    }
                    loadTableDetail();
                } catch (SQLException | GuanzonException | ExceptionInInitializerError ex) {
                    Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(ex.getMessage(), psFormName, null);
                }
            });
    /**
     * Initializes the CheckBox listeners to synchronize flags
     * with the model detail data.
     */
    private void initCheckBox() {
        
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            cbField02.setOnAction(event -> {
                try {
                    poCashflowController.DocumentMapping().Detail(pnSelectedDetail).isFixValue(cbField02.isSelected());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            cbField03.setOnAction(event -> {
                try {
                    poCashflowController.DocumentMapping().Detail(pnSelectedDetail).isMultiple(cbField03.isSelected());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(DocumentMappingController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    /**
     * Loads the master record values into the form fields.
     */
    private void LoadMaster() {
        try {
            txtField01.setText(poCashflowController.DocumentMapping().Master().getDocumentCode());
            txtField02.setText(poCashflowController.DocumentMapping().Master().getDesciption());
            cbField01.setSelected("1".equals(
                    poCashflowController.DocumentMapping().Master().getTransactionStatus()
            ));
            
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    /**
     * Loads the selected detail record values into the form fields.
     */
    private void LoadDetail() {
        try {
            txtField03.setText(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFieldCode()== null ? ""
                    : poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFieldCode());

            txtField04.setText(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFontName()== null ? ""
                    : poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFontName());

            txtField05.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getFontSize()));
            txtField06.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getTopRow()));
            txtField07.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getRowSpace()));
            txtField08.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getLeftColumn()));
            txtField09.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getColumnSpace()));
            txtField10.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getPageLocation()));
            txtField11.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getMaxLength()));
            txtField12.setText(String.valueOf(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).getMaxRow()));
            
            cbField02.setSelected( poCashflowController.DocumentMapping().Detail(pnSelectedDetail).isFixValue());
            cbField03.setSelected(poCashflowController.DocumentMapping().Detail(pnSelectedDetail).isMultiple());

        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

   /**
     * Initializes the TableView columns and prevents column reordering.
     */
    private void initTableDetail() {
        index00.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index01.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index04"));

        tblDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header = (TableHeaderRow) tblDetails.lookup("TableHeaderRow");
                if (header != null) {
                    header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        header.setReordering(false);
                    });
                }
            });
        });
    }
     /**
     * Populates the TableView with detail records asynchronously.
     * Shows a progress indicator while loading.
     */
    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                try {
                    int detailCount = poCashflowController.DocumentMapping().getDetailCount();
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if ((poCashflowController.DocumentMapping().Detail(detailCount - 1).getFieldCode() != null
                                && !poCashflowController.DocumentMapping().Detail(detailCount - 1).getFieldCode().isEmpty()
                                && poCashflowController.DocumentMapping().Detail(detailCount - 1).getFontName() != null
                                && !poCashflowController.DocumentMapping().Detail(detailCount - 1).getFontName().isEmpty()
                                && poCashflowController.DocumentMapping().Detail(detailCount - 1).getFontSize() > 0)) {

                            poCashflowController.DocumentMapping().AddDetail();
                            detailCount++;
                        }
                    }

                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poCashflowController.DocumentMapping().getDetailCount(); lnCtr++) {
                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poCashflowController.DocumentMapping().Detail(lnCtr).getFieldCode(),
                                poCashflowController.DocumentMapping().Detail(lnCtr).getFontName(),
                                String.valueOf(poCashflowController.DocumentMapping().Detail(lnCtr).getFontSize())
                        ));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblDetails.setItems(detail_data);
                    });
                    return detailsList;
                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PaymentRequest_EntryController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

     /**
     * Clears all form fields and resets state variables.
     */
    private void ClearAll() {
        Arrays.asList(
                txtField01,
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
                txtField12
        ).forEach(TextField::clear);
//        cbIsActive.setSelected(false);
//        cbIsRequired.setSelected(false);
//        cmbAccountType.getSelectionModel().clearSelection();
        detail_data.clear();
        pnSelectedDetail = 0;
        psActiveField = "";
    }

    /**
     * Handles mouse click events on the TableView to load
     * the selected detail record into the form.
     *
     * @param event the {@link MouseEvent} triggered on the table
     */
    private void tblDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnSelectedDetail = tblDetails.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();      
                    }
                }
            }
        }
    }

}
