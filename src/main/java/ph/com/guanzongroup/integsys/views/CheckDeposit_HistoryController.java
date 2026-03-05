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
public class CheckDeposit_HistoryController implements Initializable, ScreenInterface {
    
    private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Check Deposit History";
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
    private CheckBox cbReverse,cbIsReceived;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apMaster, apDetail, apButton, apTransaction;

    @FXML
    private TextField tfSearchBankAccountNo, tfSearchTransNo, tfTransactionNo,
            tfBankMaster, tfBankAccountNo, tfBankAccountName, tfTotal, tfPayee,
            tfBank, tfCheckAmount, tfCheckTransNo,
            tfCheckNo, tfNote, tfFilterBank;

    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpCheckDate,dpTransactionReferDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnClose, btnBrowse, btnPrint,btnPost,btnHistory;

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
            tblColTransDate, tblColCheckNo, tblColCheckAmount;
    
   
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
        ClearAll();
        initializeObject();
        initButtonsClickActions();
        initTableDetail();
        initTableOnClick();
        initCheckBox();
    }
    
    /**
     * Initializes the TBJ controller and transaction objects.
     */
    private void initializeObject() {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        poGLControllers = new CashflowControllers(poApp, logwrapr);
        poGLControllers.CheckDeposits().setTransactionStatus("0123456");
        poJSON = poGLControllers.CheckDeposits().InitTransaction();
        if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
        }
//            poGLControllers.CheckDeposits().Master().setIndustryId(psIndustryID);
//            lblSource.setText(poGLControllers.CheckDeposits().Master().Company().getCompanyName() + " - " + poGLControllers.CheckDeposits().Master().Industry().getDescription());
    }
    
    private void ClearAll() {
        Arrays.asList(
                tfSearchTransNo, 
                tfTransactionNo,
                tfTotal, 
                tfPayee,
                tfBank, 
                tfCheckAmount, 
                tfCheckTransNo,
                tfCheckNo, 
                tfNote,
                tfBankMaster,
                tfBankAccountNo,
                tfBankAccountName
        ).forEach(TextField::clear);
        cbReverse.setSelected(false);
        detail_data.clear();
        pnSelectedDetail = 0;
        psActiveField = "";
        taRemarks.clear();
        lblStatus.setText( "UNKOWN");
    }
    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnPrint,btnHistory,btnClose,btnPost);
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
                case "btnBrowse":
                    poJSON = poGLControllers.CheckDeposits().SearchTransaction( tfSearchTransNo.getText(),tfBankAccountNo.getText(),dpSearchTransactionDate.getValue());
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }
                    
                    System.out.println("EDIT MODE : " + poGLControllers.CheckDeposits().getEditMode());
                    pnEditMode = poGLControllers.CheckDeposits().getEditMode();
                    loadTableDetail();
                    LoadMaster();
                    LoadDetail();
                    break;
                case "btnHistory":
                     if( poGLControllers.CheckDeposits().Master().getTransactionNo() != null || ! poGLControllers.CheckDeposits().Master().getTransactionNo().isEmpty()){
                        try {
                            poGLControllers.CheckDeposits().ShowStatusHistory();
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                        }

                    }else{
                       ShowMessageFX.Error("Unable to proceed. No transaction is currently loaded.", psFormName, null);
                    }
                    break;
                case "btnPrint":
                   if(poGLControllers.CheckDeposits().Master().getTransactionNo() != null ||
                            poGLControllers.CheckDeposits().Master().getTransactionNo().isEmpty()){
                       
                            poJSON = poGLControllers.CheckDeposits().printDepositSlip();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                return;
                            }
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            poGLControllers.CheckDeposits().OpenTransaction(poGLControllers.CheckDeposits().Master().getTransactionNo());
                   }
                    break;
                    
                case "btnPost":
                    if (poGLControllers.CheckDeposits().Master().getTransactionNo() == null
                            || poGLControllers.CheckDeposits().Master().getTransactionNo().isEmpty()) {

                        ShowMessageFX.Warning("No transaction selected.", psFormName, null);
                        return;
                    }

                    if (!poGLControllers.CheckDeposits().Master().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)
                            || !poGLControllers.CheckDeposits().Master().getPrintStatus().equals(CheckTransferStatus.CONFIRMED)) {

                        ShowMessageFX.Warning(
                                "Posting is not allowed. Please confirm and print the transaction before proceeding.",
                                psFormName, null
                        );
                        return;
                    }

                    poJSON = poGLControllers.CheckDeposits().PostTransaction("");

                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        return;
                    }

                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                    
                    ClearAll();
                    initializeObject();
                    pnEditMode = poGLControllers.CheckDeposits().getEditMode();
                    break;
                

                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            initFields();
//            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(TBJ_ParameterController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initFields() {
       
        if (CheckTransferStatus.CONFIRMED.equals(poGLControllers.CheckDeposits().Master().getTransactionStatus())) {
            apMaster.setDisable(true);
            apDetail.setDisable(false);
            JFXUtil.setDisabledExcept(true,
                    apDetail
                    
            );
        }
        tfTransactionNo.setDisable(true);
        if (CheckTransferStatus.OPEN.equals(poGLControllers.CheckDeposits().Master().getTransactionStatus())
                || pnEditMode == EditMode.READY) {
            
            apMaster.setDisable(false);
            apDetail.setDisable(false);
        }
        
            List<TextField> loTxtField = Arrays.asList(tfCheckTransNo, tfCheckNo,tfSearchBankAccountNo);
            loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
//
//            JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
            JFXUtil.setFocusListener(txtField_Focus, tfNote);
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
            tfTransactionNo.setText(poGLControllers.CheckDeposits().Master().getTransactionNo());

            tfBankMaster.setText(
                    poGLControllers.CheckDeposits().Master().BankAccount().Banks().getBankName() == null ? ""
                    : poGLControllers.CheckDeposits().Master().BankAccount().Banks().getBankName());
            tfBankAccountNo.setText(
                    poGLControllers.CheckDeposits().Master().BankAccount().getAccountNo() == null ? ""
                    : poGLControllers.CheckDeposits().Master().BankAccount().getAccountNo());
            tfBankAccountName.setText(
                    poGLControllers.CheckDeposits().Master().BankAccount().getAccountName()== null ? ""
                    : poGLControllers.CheckDeposits().Master().BankAccount().getAccountName());
            taRemarks.setText(poGLControllers.CheckDeposits().Master().getRemarks() == null ? ""
                    : poGLControllers.CheckDeposits().Master().getRemarks());

            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckDeposits().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            
            dpTransactionReferDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckDeposits().Master().getTransactionReferDate(), SQLUtil.FORMAT_SHORT_DATE)));

            String lsStatus = "";
            switch (poGLControllers.CheckDeposits().Master().getTransactionStatus()) {
                case CheckTransferStatus.VOID:
                    lsStatus = "VOID";
                    break;
                case CheckTransferStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case CheckTransferStatus.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
                case CheckTransferStatus.POSTED:
                    lsStatus = "POSTED";
                    break;
            }
            lblStatus.setText(lsStatus);
        } catch (GuanzonException ex) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void LoadDetail() {
        try {
            tfCheckTransNo.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getTransactionNo()== null ? ""
                    : poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());

            tfBank.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName() == null ? ""
                    : poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().Banks().getBankName());

            tfPayee.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName()== null ? ""
                    : poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().Payee().getPayeeName());
            
            tfNote.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).getRemarks() == null ? ""
                    : poGLControllers.CheckDeposits().Detail(pnSelectedDetail).getRemarks());
            
            tfCheckNo.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getCheckNo() == null ? ""
                    : poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
            
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getAmount(), true));

            
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                    poGLControllers.CheckDeposits().Master().getTransactionTotalDeposit(), true));
            
            dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getCheckDate(), 
                            SQLUtil.FORMAT_SHORT_DATE)));
//            cbReverse.setSelected(
//                poGLControllers.CheckTransfers().Detail(pnSelectedDetail) != null
//                && poGLControllers.CheckTransfers().Detail(pnSelectedDetail).isReverse()

            cbReverse.setSelected(
                poGLControllers.CheckDeposits().Detail(pnSelectedDetail) != null
                && poGLControllers.CheckDeposits().Detail(pnSelectedDetail).isReverse()
        );
            

        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
         System.out.println("EDIT MODE loadTableDetail: " + poGLControllers.CheckDeposits().getEditMode());
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
                    int detailCount = poGLControllers.CheckDeposits().getDetailCount();
                                        
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        if (poGLControllers.CheckDeposits().Detail(detailCount - 1).getSourceNo()!= null
                                && !poGLControllers.CheckDeposits().Detail(detailCount - 1).getSourceNo().isEmpty()) {
                            poGLControllers.CheckDeposits().AddDetail();
                            detailCount++;
                        }
                    }
                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < poGLControllers.CheckDeposits().getDetailCount(); lnCtr++) {
                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poGLControllers.CheckDeposits().Detail(lnCtr) != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getTransactionNo() != null
                                ? poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getTransactionNo()
                                : "",
                                poGLControllers.CheckDeposits().Detail(lnCtr) != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Banks() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Banks().getBankName() != null
                                ? poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Banks().getBankName()
                                : "",
                                poGLControllers.CheckDeposits().Detail(lnCtr) != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Payee() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Payee().getPayeeName() != null
                                ? poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().Payee().getPayeeName()
                                : "",
                                poGLControllers.CheckDeposits().Detail(lnCtr) != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getCheckDate() != null
                                ? CustomCommonUtil.formatDateToMMDDYYYY(
                                        poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getCheckDate()
                                )
                                : "",
                                poGLControllers.CheckDeposits().Detail(lnCtr) != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment() != null
                                && poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getCheckNo() != null
                                ? poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getCheckNo()
                                : "",
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.CheckDeposits().Detail(lnCtr).CheckPayment().getAmount(),true),
                                        "","",""));
                    }
                    Platform.runLater(() -> {
                        detail_data.setAll(detailsList); // Properly update list
                        tblViewDetails.setItems(detail_data);
                        pnSelectedDetail = tblViewDetails.getItems().size() - 1;
                        tblViewDetails.getSelectionModel().clearAndSelect(pnSelectedDetail);
                        tblViewDetails.scrollTo(pnSelectedDetail);
                        LoadDetail();
//                        poJSON = poGLControllers.CheckDeposits().computeMasterFields();
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
    
   
    public void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(this::tblViewDetails_Clicked);
    }
    
    private void initCheckBox() {
        cbReverse.setDisable(true);
        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
            
            cbReverse.setOnAction(event -> {
                if (poGLControllers.CheckDeposits().Master().getTransactionStatus().equals(CheckTransferStatus.OPEN)
                        || poGLControllers.CheckDeposits().Master().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)) {
                    if (poGLControllers.CheckDeposits().Detail(pnSelectedDetail).getSourceNo() != null
                            || !poGLControllers.CheckDeposits().Detail(pnSelectedDetail).getSourceNo().isEmpty()) {
                        if (!cbReverse.isSelected()) {
                            poGLControllers.CheckDeposits().Detail().remove(pnSelectedDetail);
                        }
                    }
                } else {
                     poGLControllers.CheckDeposits().Detail(pnSelectedDetail).isReverse(cbReverse.isSelected());
                }
                
                loadTableDetail();
            });
            
        }
        
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
                cbReverse.setSelected(false);
//                cmbAccountType.getSelectionModel().clearSelection();

                if (selectedItem != null) {
                    if (pnSelectedDetail >= 0) {
                        LoadDetail();
                        tfCheckTransNo.requestFocus();
                    }
                }
            }
        }
    }
    
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
        (lsID, lsValue) -> {

            try {
                /* Lost Focus */
                switch (lsID) {
                    case "tfBank":
                        psActiveField = lsID;
                        if (lsValue == null || lsValue.trim().isEmpty()) {
//                            tfDestination.clear();
                            poGLControllers.CheckDeposits().Master().setBanks(null);
                            break;
                        }

                        if (poGLControllers.CheckDeposits().Master().Banks().getBankName()!= null) {
                            tfBank.setText(
                                    poGLControllers.CheckDeposits().Master().Banks().getBankName());
                        } else {
                            tfBank.clear();
                        }
                        break;

                    case "tfBankAccountNo":
                        psActiveField = lsID;
                        if (lsValue == null || lsValue.trim().isEmpty()) {
                            tfBankAccountNo.clear();
                            poGLControllers.CheckDeposits().Master().setBankAccount(null);
                            break;
                        }

                        if (poGLControllers.CheckDeposits().Master().getBankAccount()!= null) {
                            tfBankAccountNo.setText(
                                    poGLControllers.CheckDeposits().Master().getBankAccount());
                        } else {
                            tfBankAccountNo.clear();
                        }
                        break;
                    case "tfBankAccountName":
                        psActiveField = lsID;
                        if (lsValue == null || lsValue.trim().isEmpty()) {
                            tfBankAccountName.clear();
                            poGLControllers.CheckDeposits().Master().setBankAccount(null);
                            break;
                        }

                        if (poGLControllers.CheckDeposits().Master().getBankAccount() != null) {
                            tfBankAccountName.setText(
                                    poGLControllers.CheckDeposits().Master().getBankAccount());
                        } else {
                            tfBankAccountName.clear();
                        }
                        break;    
                    
                    case "taRemarks":
                        poGLControllers.CheckDeposits().Master().setRemarks(lsValue.trim());
                        break;
                    case "tfNote":
                        psActiveField = lsID;
                        poGLControllers.CheckDeposits().Detail(pnSelectedDetail).setRemarks(lsValue.trim());
                        break;
                    case "tfCheckNo":
                        psActiveField = lsID;
                        JFXUtil.inputIntegersOnly(tfCheckNo);
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
                    poGLControllers.CheckDeposits().Master().setRemarks(lsValue.trim());
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
                        }
                        break;
                    case F3:
                        switch (txtFieldID) {
                            case "tfBankMaster":
                                poJSON = poGLControllers.CheckDeposits().SearchBanks(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfBankMaster.setText(poGLControllers.CheckDeposits().Master().Banks().getBankName());
                                break;
                            case "tfSearchBankAccountNo":
                                poJSON = poGLControllers.CheckDeposits().SearchBankAccounts(lsValue,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }                             
                                tfSearchBankAccountNo.setText(poGLControllers.CheckDeposits().Master().BankAccount().getAccountNo());
                                return;   
                            case "tfBankAccountName":
                                poJSON = poGLControllers.CheckDeposits().SearchBankAccounts(lsValue,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfBankAccountNo.setText(poGLControllers.CheckDeposits().Master().BankAccount().getAccountNo());
                                tfBankAccountName.setText(poGLControllers.CheckDeposits().Master().BankAccount().getAccountName());
                                return; 
                            case "tfCheckTransNo":
                                poJSON = poGLControllers.CheckDeposits().SearchChecks(lsValue, "",pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfCheckTransNo.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getTransactionNo());
                                loadTableDetail();
                                return;   
                            case "tfCheckNo":
                                poJSON = poGLControllers.CheckDeposits().SearchChecks("", lsValue,pnSelectedDetail,false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), lsValue, lsValue);
                                }
                                tfCheckNo.setText(poGLControllers.CheckDeposits().Detail(pnSelectedDetail).CheckPayment().getCheckNo());
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
