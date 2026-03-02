package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.purchasing.status.PurchaseOrderStaticData;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckTransfer_PostingController implements Initializable, ScreenInterface {

   private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Check Transfer Posting";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    
    private int pnTblMainRow = -1;
    private int pnTblMain_Page = 50;
    private TextField activeField;
    private String prevPayee = "";
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    
    private int pnSelectedDetail = 0;
    private String psActiveField = "";
    
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    
    @FXML
    private CheckBox cbIsReceived;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apMaster, apDetail, apButton, apTransaction;

    @FXML
    private TextField tfSearchSource, 
                tfSearchTransNo, 
                tfTransactionNo,
                tfSourceBranch, 
                tfDepartment, 
                tfCheckTransNo, 
                tfTotal,
                tfBank, 
                tfPayee, 
                tfCheckNo,
                tfCheckAmount, 
                tfNote;

    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpCheckDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnBrowse, btnRetrieve , btnPost,btnClose, btnReceived;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView<ModelTableDetail> tblViewDetails;

    @FXML
    private TableColumn<ModelTableDetail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailBank,
            tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount;

    @FXML
    private TableView<ModelTableMain> tblViewMaster;

    @FXML
    private TableColumn<ModelTableMain, String> tblColNo, tblColTransNo,
            tblColTransDate, tblColSourceBranch,tblColDepartment;

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
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }
/**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ClearAll();
        initializeObject();
        initButtonsClickActions();
        initTableMaster();
        initTableDetail();
        initTableOnClick();
        initCheckBox();
        initButtons(poGLControllers.CheckTransfers().getEditMode());
    }
    
    /**
     * Initializes the TBJ controller and transaction objects.
     */
    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        poGLControllers = new CashflowControllers(poApp, logwrapr);
        poGLControllers.CheckTransfers().setTransactionStatus(CheckTransferStatus.POSTED);
        poJSON = poGLControllers.CheckTransfers().InitTransaction();

        if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
        }
//            poGLControllers.CheckTransfers().Master().setIndustryId(psIndustryID);
//            lblSource.setText(poGLControllers.CheckTransfers().Master().Company().getCompanyName() + " - " + poGLControllers.CheckTransfers().Master().Industry().getDescription());
    }
    
    private void ClearAll() {
        Arrays.asList(
                tfSearchSource, 
                tfSearchTransNo, 
                tfTransactionNo,
                tfSourceBranch, 
                tfDepartment, 
                tfCheckTransNo, 
                tfTotal,
                tfBank, 
                tfPayee, 
                tfCheckNo,
                tfCheckAmount, 
                tfNote
        ).forEach(TextField::clear);
        cbIsReceived.setSelected(false);
        taRemarks.clear();
        detail_data.clear();
        pnSelectedDetail = 0;
        psActiveField = "";
    }
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnRetrieve , btnPost,btnClose, btnReceived);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }
    
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
                case "btnRetrieve":
                    loadTableMaster();
                    pnEditMode = EditMode.READY;
                    break;
                    
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poGLControllers.CheckTransfers().ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
                    break;
                case "btnBrowse":
                    poJSON = poGLControllers.CheckTransfers().SearchTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    initButtons(EditMode.READY);
                    break;
                case "btnSearch":
                    String lsValue = "";
                    switch (psActiveField) {
//                        case "tfDestination":
//                            lsValue = tfDestination.getText();
//                            poJSON = poGLControllers.CheckTransfers().SearchDistination(lsValue, false);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
//                            }
//                            tfDestination.setText(poGLControllers.CheckTransfers().Master().Branch().getBranchName());
//                            break;
////                                break;
//
//                        case "tfDepartment":
//                            lsValue = tfDestination.getText();
//                            poJSON = poGLControllers.CheckTransfers().SearchDepartment(lsValue, false);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
//                            }
//                            tfDepartment.setText(poGLControllers.CheckTransfers().Master().Department().getDescription());
//                            return;
//                        case "tfCheckTransNo":
//                            lsValue = tfDestination.getText();
//                            poJSON = poGLControllers.CheckTransfers().SearchChecks(lsValue, "", pnSelectedDetail, false);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
//                            }
//                            tfCheckTransNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());
//                            loadTableDetail();
//                            return;
//                        case "tfCheckNo":
//                            lsValue = tfDestination.getText();
//                            poJSON = poGLControllers.CheckTransfers().SearchChecks("", lsValue, pnSelectedDetail, false);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
//                            }
//                            tfCheckNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
//                            loadTableDetail();
//                            return;
                        default:
                            ShowMessageFX.Warning("Looks like no searchable field is selected. \nPlease choose one to continue.", psFormName, null);
                            break;
                    }
                    
                    break;
                case "btnUpdate":
                    poJSON = poGLControllers.CheckTransfers().UpdateTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    pnEditMode = poGLControllers.CheckTransfers().getEditMode();
                    LoadMaster();
                    LoadDetail();
//                    loadTableDetail();
                    initButtons(pnEditMode);

                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, "Cancel Confirmation", "Are you sure you want to cancel? \nAny data you have entered will not be saved.")) {
                        ClearAll();

                        initializeObject();
                        pnEditMode = poGLControllers.CheckTransfers().getEditMode();
                        initButtons(pnEditMode);
                    }
                    break;
                case "btnSave":
                    poJSON = poGLControllers.CheckTransfers().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                    if (poApp.getUserLevel() > UserRight.ENCODER) {
                        if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm this transaction?")) {
                                poJSON = poGLControllers.CheckTransfers().OpenTransaction(poGLControllers.CheckTransfers().Master().getTransactionNo());
                                poJSON = poGLControllers.CheckTransfers().ConfirmTransaction("");
                          
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return;
                            }
                            ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        }
                    }

                    ClearAll();
                    break;

                case "btnVoid":
                    poJSON = poGLControllers.CheckTransfers().VoidTransaction("");
                       if (!"success".equals((String) poJSON.get("result"))) {
                           ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                           return;
                       }
                       ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                       ClearAll();
                       initializeObject();
                       pnEditMode = poGLControllers.CheckTransfers().getEditMode();
                       initButtons(pnEditMode);
                break;
                case "btnApprove":
                       poJSON = poGLControllers.CheckTransfers().ConfirmTransaction("");
                       if (!"success".equals((String) poJSON.get("result"))) {
                           ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                           return;
                       }
                       ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                       ClearAll();
                       initializeObject();
                       pnEditMode = poGLControllers.CheckTransfers().getEditMode();
                       initButtons(pnEditMode);
                break;
                
                case "btnPrint":
                     poJSON = poGLControllers.CheckTransfers().printTransaction();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Error((String) poJSON.get("message"), psFormName, null);
                    }
                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                break;

                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initButtons(pnEditMode);
            initFields();
//            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initFields() {
        boolean isEditable = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);

        if (CheckTransferStatus.CONFIRMED.equals(poGLControllers.CheckTransfers().Master().getTransactionStatus())) {
            apMaster.setDisable(true);
            apDetail.setDisable(false);
            JFXUtil.setDisabledExcept(true,
                    apDetail
                    
            );
        }
        tfTransactionNo.setDisable(true);
        if (CheckTransferStatus.OPEN.equals(poGLControllers.CheckTransfers().Master().getTransactionStatus())
                || pnEditMode == EditMode.READY) {
            
            apMaster.setDisable(false);
            apDetail.setDisable(false);
        }
        
            List<TextField> loTxtField = Arrays.asList(tfSearchSource,tfSearchTransNo);
            loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
//  
//            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
            JFXUtil.setFocusListener(txtField_Focus, tfSearchSource,tfSearchTransNo);
            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);

//            cmbAccountType.setItems(AccountType);
//            cmbAccountType.setOnAction(comboBoxActionListener);
//            JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAccountType);
//
//            tblDetails.setOnMouseClicked(this::tblDetails_Clicked);
//            makeClearableReadOnly(tfFieldName);

    }
    
    private void LoadMaster() {
        try {
            tfTransactionNo.setText(poGLControllers.CheckTransfers().Master().getTransactionNo());

            tfSourceBranch.setText(
                    poGLControllers.CheckTransfers().Master().BranchDestination().getBranchName() == null ? ""
                    : poGLControllers.CheckTransfers().Master().BranchDestination().getBranchName() );
            tfDepartment.setText(
                   poGLControllers.CheckTransfers().Master().Department().getDescription() == null ? ""
                    : poGLControllers.CheckTransfers().Master().Department().getDescription());
           
            taRemarks.setText(poGLControllers.CheckTransfers().Master().getRemarks() == null ? ""
                    : poGLControllers.CheckTransfers().Master().getRemarks());
            
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckTransfers().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            

            String lsStatus = "";
            switch (poGLControllers.CheckTransfers().Master().getTransactionStatus()) {
                case CheckTransferStatus.VOID:
                    lsStatus = "VOID";
                    break;
                case CheckTransferStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case CheckTransferStatus.CONFIRMED:
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
            tfCheckTransNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getTransactionNo()== null ? ""
                    : poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());

            tfBank.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName() == null ? ""
                    : poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName());

            tfPayee.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName()== null ? ""
                    : poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName());
            
            tfNote.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).getRemarks() == null ? ""
                    : poGLControllers.CheckTransfers().Detail(pnSelectedDetail).getRemarks());
            
            tfCheckNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getCheckNo() == null ? ""
                    : poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
            
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getAmount(), true));

            
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckTransfers().Master().getTransactionTotal(), true));
            
            dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getCheckDate(), 
                            SQLUtil.FORMAT_SHORT_DATE)));

            cbIsReceived.setSelected(
                poGLControllers.CheckTransfers().Detail(pnSelectedDetail) != null
                && poGLControllers.CheckTransfers().Detail(pnSelectedDetail).isReceived()
        );
            

        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initButtons(int fnEditMode) {
        CustomCommonUtil.setVisible(false,btnBrowse);
        CustomCommonUtil.setManaged(false,btnBrowse);
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        CustomCommonUtil.setVisible(!lbShow, btnClose);
        CustomCommonUtil.setManaged(!lbShow, btnClose );

//        CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel,btnSearch);
//        CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel,btnSearch);

        // Default hide Update
//        CustomCommonUtil.setVisible(false, btnUpdate,btnVoid,btnApprove,btnPrint);
//        CustomCommonUtil.setManaged(false, btnUpdate,btnVoid,btnApprove,btnPrint);

        String lsTransNo = poGLControllers.CheckTransfers()
                .Master()
                .getTransactionNo();

        if (fnEditMode == EditMode.READY
                && lsTransNo != null
                && !lsTransNo.isEmpty()) {

//        CustomCommonUtil.setVisible(true, btnUpdate,btnVoid,btnApprove,btnPrint);
//        CustomCommonUtil.setManaged(true, btnUpdate,btnVoid,btnApprove,btnPrint);
        }

      
    }

    
    private void initTableMaster() {
        tblColNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColTransNo.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColTransDate.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColSourceBranch.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblColDepartment.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblViewMaster.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tblViewMaster.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header = (TableHeaderRow) tblViewMaster.lookup("TableHeaderRow");
                if (header != null) {
                    header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        header.setReordering(false);
                    });
                }
            });
        });
        JFXUtil.applyRowHighlighting(tblViewMaster, item -> ((ModelTableMain) item).getIndex02(), highlightedRowsMain);
    }
    
    private void initTableDetail() {
        tblColDetailNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColDetailReference.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColDetailBank.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColDetailPayee.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblColDetailDate.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblColDetailCheckNo.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblColDetailCheckAmount.setCellValueFactory(new PropertyValueFactory<>("index07"));

        tblViewDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header = (TableHeaderRow) tblViewDetails.lookup("TableHeaderRow");
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

        tblViewDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                try {
                    int detailCount = poGLControllers.CheckTransfers().getDetailCount();
                                        
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if (poGLControllers.CheckTransfers().Detail(detailCount - 1).getSourceNo()!= null
                                && !poGLControllers.CheckTransfers().Detail(detailCount - 1).getSourceNo().isEmpty()) {
                            poGLControllers.CheckTransfers().AddDetail();
                            detailCount++;
                        }
                    }
                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poGLControllers.CheckTransfers().getDetailCount(); lnCtr++) {
                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poGLControllers.CheckTransfers().Detail(lnCtr) != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getTransactionNo() != null
                                ? poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getTransactionNo()
                                : "",
                                poGLControllers.CheckTransfers().Detail(lnCtr) != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Banks() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Banks().getBankName() != null
                                ? poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Banks().getBankName()
                                : "",
                                poGLControllers.CheckTransfers().Detail(lnCtr) != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Payee() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Payee().getPayeeName() != null
                                ? poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().Payee().getPayeeName()
                                : "",
                                poGLControllers.CheckTransfers().Detail(lnCtr) != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getCheckDate() != null
                                ? CustomCommonUtil.formatDateToMMDDYYYY(
                                        poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getCheckDate()
                                )
                                : "",
                                poGLControllers.CheckTransfers().Detail(lnCtr) != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getCheckNo() != null
                                ? poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getCheckNo()
                                : "",
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getAmount(),true),
                                        "","",""));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblViewDetails.setItems(detail_data);
                        pnSelectedDetail = tblViewDetails.getItems().size() - 1;
                        tblViewDetails.getSelectionModel().clearAndSelect(pnSelectedDetail);
                        tblViewDetails.scrollTo(pnSelectedDetail);
                        LoadDetail();
                        JFXUtil.showRetainedHighlight(false, tblViewMaster, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        loadHighlightFromDetail();
//                        poJSON = poGLControllers.CheckTransfers().computeMasterFields();
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
    
    public void loadHighlightFromDetail() {
        try {
            for (int lnCtr = 0; lnCtr < poGLControllers.CheckTransfers().getDetailCount(); lnCtr++) {
                String lsTransNo = !JFXUtil.isObjectEqualTo(poGLControllers.CheckTransfers().Detail(lnCtr).getSourceNo(), null, "") ? poGLControllers.CheckTransfers().Detail(lnCtr).getSourceNo() : "";
                String lsTransType = !JFXUtil.isObjectEqualTo(poGLControllers.CheckTransfers().Detail(lnCtr).getSourceCode(), null, "") ? poGLControllers.CheckTransfers().Detail(lnCtr).getSourceCode() : "";
                String lsHighlightbasis;

                lsHighlightbasis = poGLControllers.CheckTransfers().Detail(lnCtr).getSourceNo();

                if (!JFXUtil.isObjectEqualTo(poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getAmount(), null, "")) {
                    if (poGLControllers.CheckTransfers().Detail(lnCtr).CheckPayment().getAmount() != 0.0000) {
                        plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "1"));
                    } else {
                        plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "0"));
                    }
                }
            }
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"".equals(pair.getKey()) && pair.getKey() != null) {
                    JFXUtil.highlightByKey(tblViewMaster, pair.getKey(), "#A7C7E7", highlightedRowsMain);
                }
            }
            JFXUtil.showRetainedHighlight(false, tblViewMaster, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, false);
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(ex));
        }
    }
    
    public void initTableOnClick() {
        
        tblViewDetails.setOnMouseClicked(this::tblViewDetails_Clicked);
        tblViewMaster.setOnMouseClicked(this::tblViewMaster_Clicked);
    }
    
    private void initCheckBox() {
        cbIsReceived.setDisable(true);
//        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
//            
//            cbIsReceived.setOnAction(event -> {
//                if (poGLControllers.CheckTransfers().Master().getTransactionStatus().equals(CheckTransferStatus.OPEN)
//                        || poGLControllers.CheckTransfers().Master().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)) {
//                    if (poGLControllers.CheckTransfers().Detail(pnSelectedDetail).getSourceNo() != null
//                            || !poGLControllers.CheckTransfers().Detail(pnSelectedDetail).getSourceNo().isEmpty()) {
//                        if (!cbIsReceived.isSelected()) {
//                            poGLControllers.CheckTransfers().Detail().remove(pnSelectedDetail);
//                        }
//                    }
//                } else {
//                     poGLControllers.CheckTransfers().Detail(pnSelectedDetail).isReverse(cbIsReceived.isSelected());
//                }
//                
//                loadTableDetail();
//            });
//            
//        }
        
    }
    private void tblViewDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnSelectedDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblViewDetails.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                tfCheckTransNo.clear();
                tfBank.clear();
                tfPayee.clear();
                tfNote.clear();
                tfCheckNo.clear();
                dpCheckDate.setValue(null);
                cbIsReceived.setSelected(false);
//                cmbAccountType.getSelectionModel().clearSelection();

                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();
                        tfCheckTransNo.requestFocus();
                        if(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).getSourceNo().isEmpty()){
                            tfCheckTransNo.setDisable(false);
                            tfCheckNo.setDisable(false);
                            cbIsReceived.setDisable(true);
                        }else{
                            tfCheckTransNo.setDisable(true);
                            tfCheckNo.setDisable(true);
                            cbIsReceived.setDisable(false);
                        }
                                
                    }
                }
            }
        }
    }
    
    private void tblViewMaster_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {

            // Get clicked row index
            pnTblMainRow = tblViewMaster.getSelectionModel().getSelectedIndex();
            if (pnTblMainRow >= 0) {
                // Add this row to the selection without removing previous ones
                if (!tblViewMaster.getSelectionModel().getSelectedIndices().contains(pnTblMainRow)) {
                    tblViewMaster.getSelectionModel().select(pnTblMainRow);
                }
            }

            // Double-click logic
            if (event.getClickCount() == 2) {
                ModelTableMain loCheckPaym = (ModelTableMain) tblViewMaster.getSelectionModel().getSelectedItem();
                if (loCheckPaym != null) {
                    String lsCheckTransfer = loCheckPaym.getIndex02();

                    try {
                        poJSON = poGLControllers.CheckTransfers().InitTransaction();
                        if ("success".equals((String) poJSON.get("result"))) {
                            poJSON = poGLControllers.CheckTransfers().OpenTransaction(lsCheckTransfer);
                            if ("success".equals((String) poJSON.get("result"))) {
                                ClearAll();
                                loadTableDetail();
                                LoadMaster();
                                LoadDetail();
                                pnEditMode = poGLControllers.CheckTransfers().getEditMode();
                                initButtons(pnEditMode);
                            } else {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                pnEditMode = EditMode.UNKNOWN;
                            }
                            initButtons(pnEditMode);


                        }
                        
                    } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                        Logger.getLogger(PaymentRequest_EntryController.class.getName())
                                .log(Level.SEVERE, null, ex);
                        ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                    }
                }
            }
        }
    }
    private void loadTableMaster() {
        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblViewMaster.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    main_data.clear();
                    poJSON = poGLControllers.CheckTransfers().getCheckTransferForPosting(tfSearchSource.getText(), 
                                                                   tfSearchTransNo.getText(),
                                                                  dpSearchTransactionDate.getValue());
                    
                    if ("success".equals(poJSON.get("result"))) {
                        if (poGLControllers.CheckTransfers().getCheckTransferMasterCount()> 0) {
                            for (int lnCntr = 0; lnCntr < poGLControllers.CheckTransfers().getCheckTransferMasterCount(); lnCntr++) {
                                main_data.add(new ModelTableMain(
                                        String.valueOf(lnCntr + 1),
                                        poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).getTransactionNo() == null ? ""
                                        : poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).getTransactionNo(),
                                        poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).getTransactionDate()== null ? ""
                                        : CustomCommonUtil.formatDateToShortString(
                                                poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).getTransactionDate()),                                      
                                        poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).Branch().getBranchName()== null ? ""
                                        : poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).Branch().getBranchName(),
                                        poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).Department().getDescription()== null ? ""
                                        : poGLControllers.CheckTransfers().poCheckTransferMaster(lnCntr).Department().getDescription(),
                                        "", "", "", "", ""
                                        ));
                            }
                        } else {
                            main_data.clear();
                        }
                    }
                    

                    Platform.runLater(() -> {
                        if (main_data.isEmpty()) {
                            tblViewMaster.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        }
                        tblViewMaster.setItems(FXCollections.observableArrayList(main_data));
                    });

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PurchaseOrder_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false); // ✅ Re-enable the button

                if (main_data == null || main_data.isEmpty()) {
                    tblViewMaster.setPlaceholder(new Label("NO RECORD TO LOAD"));
                    ShowMessageFX.Warning("NO RECORD TO LOAD.", psFormName, null);
                } 
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false); // ✅ Re-enable the button even if failed
            }
        };

        new Thread(task).start();
    }
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
        (lsID, lsValue) -> {

            try {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSourceBranch":
                        psActiveField = lsID;
                        if (lsValue == null || lsValue.trim().isEmpty()) {
                            tfSourceBranch.clear();
                            poGLControllers.CheckTransfers().Master().setDestination(null);
                            break;
                        }

                        if (poGLControllers.CheckTransfers().Master().getDestination() != null) {
                            tfSourceBranch.setText(
                                    poGLControllers.CheckTransfers().Master().Branch().getBranchName());
                        } else {
                            tfSourceBranch.clear();
                        }
                        if(!poGLControllers.CheckTransfers().Master().Branch().isMainOffice()){
                           tfDepartment.setDisable(true);
                        }else{
                            tfDepartment.setDisable(false);
                        }
                        break;

                    case "tfDepartment":
                        psActiveField = lsID;
                        if (lsValue == null || lsValue.trim().isEmpty()) {
                            tfDepartment.clear();
                            poGLControllers.CheckTransfers().Master().setDepartment(null);
                            break;
                        }

                        if (poGLControllers.CheckTransfers().Master().getDepartment() != null) {
                            tfDepartment.setText(
                                    poGLControllers.CheckTransfers().Master().Department().getDescription());
                        } else {
                            tfDepartment.clear();
                        }
                        break;
                    case "taRemarks":
                        poGLControllers.CheckTransfers().Master().setRemarks(lsValue.trim());
                        break;
                    case "tfNote":
                        psActiveField = lsID;
                        poGLControllers.CheckTransfers().Detail(pnSelectedDetail).setRemarks(lsValue.trim());
                        break;
                    case "tfCheckNo":
                        psActiveField = lsID;
                        JFXUtil.inputIntegersOnly(tfCheckNo);
                        break;
                    case "tfSearchTransNo":
                        loadTableMaster();
                        break;
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(CheckTransfer_EntryController.class.getName())
                        .log(Level.SEVERE, null, ex);

                ShowMessageFX.Warning(
                        "Error processing field: " + ex.getMessage(),
                        psFormName,
                        null
                );
            }
        });
    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
        (lsID, lsValue) -> {

            /* Lost Focus */
            switch (lsID) {
                
                case "taRemarks":
                    poGLControllers.CheckTransfers().Master().setRemarks(lsValue.trim());
                    break;
                    
            }
        });

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
                        switch (txtFieldID) {
                            case "tfFilterBank":
                                loadTableMaster();
                                break;
                        }
                        break;
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchSource":
                                poJSON = poGLControllers.CheckTransfers().SearchDistination(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfSearchSource.setText(poGLControllers.CheckTransfers().Master().Branch().getBranchName());
                                loadTableMaster();
                                break;
                            case "tfSourceBranch":
                                poJSON = poGLControllers.CheckTransfers().SearchDistination(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfSourceBranch.setText(poGLControllers.CheckTransfers().Master().Branch().getBranchName());
                                if(!poGLControllers.CheckTransfers().Master().Branch().isWarehouse() 
                                        || !poGLControllers.CheckTransfers().Master().Branch().isMainOffice()){
                                    poGLControllers.CheckTransfers().Master().setDepartment(null);
                                     tfDepartment.clear();
                                }
                                break;
                            case "tfDepartment":
                                poJSON = poGLControllers.CheckTransfers().SearchDepartment(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfDepartment.setText(poGLControllers.CheckTransfers().Master().Department().getDescription());
                                return;
                            case "tfCheckTransNo":
                                poJSON = poGLControllers.CheckTransfers().SearchChecks(lsValue, "",pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfCheckTransNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());
                                loadTableDetail();
                                return;   
                            case "tfCheckNo":
                                poJSON = poGLControllers.CheckTransfers().SearchChecks("", lsValue,pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfCheckNo.setText(poGLControllers.CheckTransfers().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
                                loadTableDetail();
                                return; 
                             
                        }

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
            }
        }
    }
}
