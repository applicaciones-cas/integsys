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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.tbjhandler.Services.TBJControllers;
import org.json.simple.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
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
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.tbjhandler.constant.TBJ_Constant;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author user
 */
public class TBJ_Parameter_EntryController implements Initializable,ScreenInterface {
    
    private GRiderCAS poApp;
    private TBJControllers poTBJControllers;
    private String psFormName = "TBJ Parameter";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private int pnSelectedDetail = 0;
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    
    ObservableList<String> AccountType = FXCollections.observableArrayList(
            "Debit",
            "Credit");

    @FXML
    private AnchorPane anchorMain,
            apBrowse,
            apButton;

    @FXML
    private TextField tfSearchTransaction,
            tfSearchSource,
            tfSourceCode,
            tfCategory,
            tfTransactionNo,
            tfFieldName,
            tfAccountTitle,
            tfTableName;

    @FXML
    private TextArea taRemarks;

    @FXML
    private Label lblSource,
            lblStatus;

    @FXML
    private Button btnBrowse,
            btnNew,
            btnUpdate,
            btnSave,
            btnCancel,
            btnClose,
            btnSearch;

    @FXML
    private FontAwesomeIconView btnStatusGlyph;

    @FXML
    private CheckBox cbIsRequired,
            cbIsActive;

    @FXML
    private ComboBox cmbAccountType;

    @FXML
    private TableView<ModelTableDetail> tblDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> index00,
            index01,
            index02,
            index03,
            index04,
            index05,
            index06;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
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

    /**
     * Initializes the controller class.
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
        Platform.runLater(() -> btnNew.fire());
    } 
    private void initializeObject() {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poTBJControllers = new TBJControllers(poApp, logWrapper);
            poJSON = poTBJControllers.TBJParameter().InitTransaction();
            poTBJControllers.TBJParameter().setTransactionStatus("0");
//            poTBJControllers.TBJParameter().Master().setIndustryID(psIndustryID);
//            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(BrandController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initButtons(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        
        CustomCommonUtil.setVisible(!lbShow, btnBrowse, btnClose, btnNew);
        CustomCommonUtil.setManaged(!lbShow, btnBrowse, btnClose, btnNew);

        CustomCommonUtil.setVisible(lbShow, btnSearch, btnSave, btnCancel);
        CustomCommonUtil.setManaged(lbShow, btnSearch, btnSave, btnCancel);

        CustomCommonUtil.setVisible(false, btnUpdate);
        CustomCommonUtil.setManaged(false, btnUpdate);
        if (fnEditMode == EditMode.READY){
            CustomCommonUtil.setVisible(true, btnUpdate);
            CustomCommonUtil.setManaged(true, btnUpdate);
        }
    }
    
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel,  btnClose);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }
    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poTBJControllers.TBJParameter().SearchTransaction("");
                     if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                     
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    break;
                case "btnNew" :
                    ClearAll();
                    poJSON = poTBJControllers.TBJParameter().NewTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poTBJControllers.TBJParameter().getEditMode();
                    LoadMaster();
                    LoadDetail();
                    loadTableDetail();
                    initButtons(pnEditMode);
                    break;
                case "btnUpdate" :
                    poJSON = poTBJControllers.TBJParameter().UpdateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poTBJControllers.TBJParameter().getEditMode();
                    LoadMaster();
                    LoadDetail();
                    loadTableDetail();
                    initButtons(pnEditMode);
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, "Cancel Confirmation", "Are you sure you want to cancel?")) {
                        ClearAll();
                        
                        initializeObject();
                        pnEditMode = poTBJControllers.TBJParameter().getEditMode();
                        initButtons(pnEditMode);   
                    }
                    break;
                case "btnSave":
                    poJSON = poTBJControllers.TBJParameter().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ClearAll();
                    btnNew.fire();
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initButtons(pnEditMode);
            initFields();
//            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_Parameter_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initFields() {

        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!isEditable,
                tfSearchTransaction,
                tfSearchSource,
                tfSourceCode,
                tfCategory,
                tfTransactionNo,
                tfFieldName,
                tfAccountTitle,
                tfTableName,
                cbIsActive,
                cbIsRequired,
                cmbAccountType,
                taRemarks
        );  
        List<TextField> loTxtField = Arrays.asList(tfTableName,tfCategory,tfSourceCode,tfAccountTitle,tfFieldName);
        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
        
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        
        cmbAccountType.setItems(AccountType);
        cmbAccountType.setOnAction(comboBoxActionListener);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType);
        
        tblDetails.setOnMouseClicked(this::tblDetails_Clicked);

    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
            switch (cmbId) {
                case "cmbAccountType":
                    try {
                        String accountType = String.valueOf(selectedValue);
                        if (accountType != null) {
                            poTBJControllers.TBJParameter().Detail(pnSelectedDetail).setAccountType(
                                    String.valueOf(cmbAccountType.getSelectionModel().getSelectedIndex()));
                            LoadDetail();
                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(TBJ_Parameter_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                break;
            }
        }
    );
    
    private void initCheckBox() {
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            cbIsActive.setOnAction(event -> {
                try {
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isActive(cbIsActive.isSelected());
                    LoadDetail();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(TBJ_Parameter_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                } 
            });
            cbIsRequired.setOnAction(event -> {
                try {
                     poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isRequired(cbIsRequired.isSelected());
                     LoadDetail();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(TBJ_Parameter_EntryController.class.getName()).log(Level.SEVERE, null, ex);
                } 
            });
        }
    }
    
    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextAreaID = loTextArea.getId();
        String lsValue = loTextArea.getText();
        if (lsValue == null) {
            return;
        }
        try {
            if (!nv) {
                /*Lost Focus*/
                switch (lsTextAreaID) {
                    case "taRemarks":
                        poTBJControllers.TBJParameter().Master().setRemarks(lsValue);
                        break;
                }
            } else {
                loTextArea.selectAll();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    };

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
                            case "tfCategory":
                                poJSON = poTBJControllers.TBJParameter().SearchCategory(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfCategory.setText(poTBJControllers.TBJParameter().Master().Category().getDescription());
                                return;
                                
                            case "tfSourceCode":
                                poJSON = poTBJControllers.TBJParameter().SearchSourceCode(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfSourceCode.setText(poTBJControllers.TBJParameter().Master().TransactionSource().getSourceName());
                                return;
                                
                            case "tfAccountTitle":
                                poJSON = poTBJControllers.TBJParameter().SearchAccountChart(lsValue, false, pnSelectedDetail);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfAccountTitle.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription());
                                loadTableDetail();
                                return;
                            
                            case "tfTableName":
                                poJSON = poTBJControllers.TBJParameter().SearchSysTable(lsValue, false, pnSelectedDetail);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfTableName.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm());
                                loadTableDetail();
                                return;
                            case "tfFieldName":
                                if(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm()== null ||
                                        poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().isEmpty()) {
                                     ShowMessageFX.Warning("Table Name is not set!", lsValue, lsValue);
                                    return;
                                }
                                poTBJControllers.TBJParameter().show(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().trim().replace(" ", "_"), pnSelectedDetail);
                                tfFieldName.setText(poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getDerivedField(),pnSelectedDetail));
                                loadTableDetail();
                                break;
                                
                        }
                        
                        break;
                    case UP:
                        break;
                    case DOWN:
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(TBJ_Parameter_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void LoadMaster() {
        try {
            tfTransactionNo.setText(poTBJControllers.TBJParameter().Master().getTransactionNo());
            
            tfSourceCode.setText(
                    poTBJControllers.TBJParameter().Master().getSourceCode() == null ? ""
                     : poTBJControllers.TBJParameter().Master().getSourceCode());
            tfCategory.setText(
                    poTBJControllers.TBJParameter().Master().Category().getDescription()== null ? ""
                     : poTBJControllers.TBJParameter().Master().Category().getDescription());
            tfSourceCode.setText(
                    poTBJControllers.TBJParameter().Master().getSourceCode() == null ? ""
                     : poTBJControllers.TBJParameter().Master().getSourceCode());
            
            taRemarks.setText(poTBJControllers.TBJParameter().Master().getRemarks() == null ? ""
                     : poTBJControllers.TBJParameter().Master().getRemarks());
            
            lblStatus.setText("");
            String lsStatus = "";
            switch (poTBJControllers.TBJParameter().Master().getTransactionStatus()) {
                case TBJ_Constant.OPEN:
                    lsStatus = "OPEN";
                    break;
                case TBJ_Constant.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
            }
            lblStatus.setText(lsStatus);
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void LoadDetail() {
        try {
            tfAccountTitle.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription() == null ? ""
                    : poTBJControllers.TBJParameter().Detail(pnSelectedDetail).AccountChart().getDescription());
            
            tfTableName.setText(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm() == null ? ""
                            : poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getTableNm().trim().replace("_", " "));
            
            tfFieldName.setText(poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getDerivedField(),pnSelectedDetail));
                                
            String accountTypeStr = poTBJControllers.TBJParameter().Detail(pnSelectedDetail).getAccountType();

            if (accountTypeStr != null && !accountTypeStr.trim().isEmpty()) {
                try {
                    int getAccountType = Integer.parseInt(accountTypeStr);
                    cmbAccountType.getSelectionModel().select(getAccountType);
                } catch (NumberFormatException e) {
                    // Invalid number, do not select anything
                }
            }
            cbIsActive.setSelected(
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail) != null
                    && poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isActive()
            );
            cbIsRequired.setSelected(
                    poTBJControllers.TBJParameter().Detail(pnSelectedDetail) != null
                    && poTBJControllers.TBJParameter().Detail(pnSelectedDetail).isRequired()
            );
            
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void initTableDetail() {
        index00.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index01.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index05.setCellValueFactory(new PropertyValueFactory<>("index06"));
        index06.setCellValueFactory(new PropertyValueFactory<>("index07"));
        
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
                    int detailCount = poTBJControllers.TBJParameter().getDetailCount();
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if (poTBJControllers.TBJParameter().Detail(detailCount - 1).getAccountNo()!= null
                                && !poTBJControllers.TBJParameter().Detail(detailCount - 1).getAccountNo().isEmpty()) {
                            poTBJControllers.TBJParameter().AddDetail();
                            detailCount++;
                        }
                    }
                    
                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poTBJControllers.TBJParameter().getDetailCount(); lnCtr++) {
                        String accountTypeText = (poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType() == null
                                || poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType().trim().isEmpty())
                                ? ""
                                : poTBJControllers.TBJParameter().Detail(lnCtr).getAccountType().equals("0") ? "Debit" : "Credit";

                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poTBJControllers.TBJParameter().Detail(lnCtr).AccountChart().getDescription(),
                                accountTypeText,
                                poTBJControllers.TBJParameter().Detail(lnCtr).getTableNm(), 
                                poTBJControllers.TBJParameter().getFieldName(poTBJControllers.TBJParameter().Detail(lnCtr).getDerivedField(), lnCtr), 
                                poTBJControllers.TBJParameter().Detail(lnCtr).isRequired() ? "✔" : "✗",
                                poTBJControllers.TBJParameter().Detail(lnCtr).isActive() ? "✔" : "✗",
                                "","",""
                        ));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblDetails.setItems(detail_data);
                        initFields();
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
    private void ClearAll(){
        Arrays.asList(
                tfSearchTransaction,
                tfSearchSource,
                tfSourceCode,
                tfCategory,
                tfTransactionNo,
                tfFieldName,
                tfAccountTitle,
                tfTableName
        ).forEach(TextField::clear);
        cbIsActive.setSelected(false);
        cbIsRequired.setSelected(false);
        cmbAccountType.getSelectionModel().clearSelection();
        detail_data.clear();
    }
     private void tblDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnSelectedDetail = tblDetails.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                tfAccountTitle.clear();
                tfTableName.clear();
                tfFieldName.clear();
                cbIsActive.setSelected(false);
                cbIsRequired.setSelected(false);
                cmbAccountType.getSelectionModel().clearSelection();
                
                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();
                        tfAccountTitle.requestFocus();
                    }
                }
            }
        }
    }
}
