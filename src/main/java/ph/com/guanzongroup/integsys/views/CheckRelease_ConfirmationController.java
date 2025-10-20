/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.check.module.mnv.CheckRelease;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckReleaseStatus;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.services.CheckController;

/**
 *
 * @author Guillier
 */
public class CheckRelease_ConfirmationController implements Initializable, ScreenInterface{
    
    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Release Confirmation";
    private String psIndustryID;
    private Control lastFocusedControl;
    private CheckRelease poAppController;
    
     private unloadForm poUnload = new unloadForm();
    
    private ObservableList<Model_Check_Payments> laCheckListPayment = FXCollections.observableArrayList();
    private ObservableList<Model_Check_Release_Detail> laCheckListDetail;
    
    private int pnSelectMaster, pnTransactionDetail, pnEditMode;
    
    @FXML
    private AnchorPane apMainAnchor, apMaster, apDetail, apCheckDettail, apTransaction;
    
    @FXML
    Label lblStatus, lblSource;
    
    @FXML
    private TextField tfSearchReceived, tfSearchTransNo, tfTransNo, tfReceivedBy, tfTotal, tfSearchCheckRef, tfPayee, tfParticular,
            tfCheckAmt, tfNote, tfSearchPayee, tfSearchCheck, tfCheckNo;
    
    @FXML
    private TextArea taRemarks;
    
    @FXML
    private DatePicker dpTransactionDate, dpCheckDate, dpCheckDtFrm, dpCheckDTTo;
    
    @FXML
    private Button btnSearch, btnBrowse, btnUpdate, btnApprove, btnVoid, btnPrint, btnSave, btnCancel, btnHistory, btnRetrieve, btnClose;
    
    @FXML
    private TableView<Model_Check_Payments> tblViewMaster;
    
    @FXML
    private TableView<Model_Check_Release_Detail> tblViewDetails;
    
    @FXML
    private TableColumn<Model_Check_Payments, String> tblColNo, tblColTransNo, tblColTransDate, tblColCheckNo, tblColCheckAmt;
    
    @FXML
    private TableColumn<Model_Check_Release_Detail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailParticular, tblColDetailCheckDt, tblColDetailCheckNo, tblColDetailAmt;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new CheckController(poApp, poLogWrapper).CheckRelease();

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                return;
            }

            //background thread
            Platform.runLater(() -> {
                
                poAppController.setTransactionStatus("10");
                poAppController.setIndustryID(psIndustryID);
                
                System.err.println("Initialize value : Industry >" + psIndustryID);
            });
            LoadTransactionDetails();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(CheckRelease_ConfirmationController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

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
    }

    @Override
    public void setCategoryID(String fsValue) {
    }
    
    @FXML
    void ontblMasterClicked(MouseEvent e) {
        
        try{
            
            pnSelectMaster = tblViewMaster.getSelectionModel().getSelectedIndex() ;
            if (pnSelectMaster < 0) {
                return;
            }

            if (e.getClickCount() == 1 && !e.isConsumed()) {
                e.consume();

                //check transaction number, if transaction is loaded properly
                if (poAppController.GetMaster().getTransactionNo().isEmpty() || poAppController.GetMaster().getTransactionNo() == null) {
                   ShowMessageFX.Information("Please load transaction!", "Initialize check transaction", null);
                   return;
                }
                
                //do not load transaction if not in update or add mode
                if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN) {
                    return;
                }

                //check selected row's transaction no if not empty, ask user to replace the existing.
                if (tblColDetailReference.getCellData(pnTransactionDetail) != null) {
                    if (!tblColDetailReference.getCellData(pnTransactionDetail).isEmpty()) {
                        if (ShowMessageFX.OkayCancel("Do you want to replace item on row " + String.valueOf(pnTransactionDetail + 1) + "?", "Initialize check transaction", null) == false) {
                            return;
                        }
                    }
                }

                //load item's check detail, increase 1 to get the right validation for adding to the list
                if (!isJSONSuccess(poAppController.LoadCheckTransaction(tblColTransNo.getCellData(pnSelectMaster), pnTransactionDetail + 1), psFormName)) {
                    return;
                }
                ComputeTotal();
                getLoadedTransaction();
            }

        }catch(SQLException | GuanzonException ex){
            Logger.getLogger(CheckRelease_ConfirmationController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    void ontblDetailClicked(MouseEvent e) {
        
        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
        if (pnTransactionDetail < 0) { return; }
        
        if (e.getClickCount() == 1 && !e.isConsumed()) {
            e.consume();

            InitCheckDetail();
        }
    }
    
    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try{
            
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }
                    
                    switch (lastFocusedControl.getId()) {
                        
                        case "tfSearchTransNo":
                            
                            if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {
                        
                                if (poAppController.GetMaster().getTransactionNo() != null) {
                                    
                                    if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {
                                    
                                        if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                            return;
                                        }

                                    }

                                }
                            }

                            if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchTransNo.getText().toString(),  true), "Initialize Search Check Release Master")) {
                                return;
                            }
                            
                            clearAllInputs();
                            getLoadedTransaction();
                            
                            break;
                        
                        case "tfSearchReceived":
                            
                            if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {
                        
                                if (poAppController.GetMaster().getTransactionNo() != null) {
                                    
                                    if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {
                                    
                                        if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                            return;
                                        }

                                    }

                                }
                            }
                            
                            if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchReceived.getText().toString(), false), "Initialize Search Check Release Master")) {
                                return;
                            }
                            
                            clearAllInputs();
                            getLoadedTransaction();
                            
                            break;
                        
                         case "tfSearchPayee":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchPayee.getText().toString(),
                                    true, false), "Initialize Search Check Release Master")) {
                                break;
                            }
                            LoadCheckPayments();
                            break;
                            
                        case "tfSearchCheck":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), false, false), "Initialize Search Check Release Master")) {
                                break;
                            }
                            LoadCheckPayments();
                            break;
                            
                        case "tfSearchCheckRef":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), true, true), "Initialize Search Check Release Master")) {
                                break;
                            }
                            LoadCheckPayments();
                            break;
                    }
                    break;
                    
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }

                    switch (lastFocusedControl.getId()) {
                        
                        case "tfSearchTransNo":
                            
                            if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {
                        
                                if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {

                                    if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                        return;
                                    }

                                }
                            }

                             if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchTransNo.getText().toString(), true), "Initialize Search Check Release Master")) {
                                break;
                            }
                            
                            clearAllInputs();
                            getLoadedTransaction();
                            
                            break;
                        
                        case "tfSearchReceived":
                            
                            if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {
                        
                                if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {

                                    if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                        return;
                                    }

                                }
                            }
                            
                             if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchReceived.getText().toString(), false), "Initialize Search Check Release Master")) {
                                return;
                            }

                            clearAllInputs();
                            getLoadedTransaction();
                            
                            break;
                            
                        case "tfSearchPayee":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchPayee.getText().toString(),
                                    true, false), "Initialize Search Check Release Master")) {
                                break;
                            }
                            LoadCheckPayments();
                            break;
                            
                        case "tfSearchCheck":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), false, false), "Initialize Search Check Release Master")) {
                                break;
                            }
                            LoadCheckPayments();
                            break;
                    }
                    break;
                    
                case "btnRetrieve":
                    
                    switch (lastFocusedControl.getId()) {
                        
                        case "dpCheckDtFrm":
                        case "dpCheckDTTo":
                            if (!isJSONSuccess(poAppController.LoadCheckListByDate(String.valueOf(dpCheckDtFrm.getValue()), String.valueOf(dpCheckDTTo.getValue())),
                                    "Initialize : Load of Transaction List")) {
                                return;
                            }
                            LoadCheckPayments();
                            break;
                    }
                    break;

                case "btnApprove":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, "Check Release Confirmation");
                        return;
                    }

                    //allow only open transactions, for confirmation
                    if (!poAppController.GetMaster().getTransactionStatus().equalsIgnoreCase(CheckReleaseStatus.OPEN)) {
                        ShowMessageFX.Information("Status was already " + CheckReleaseStatus.STATUS.get(Integer.parseInt(poAppController.GetMaster().getTransactionStatus())).toLowerCase(), null, "Issuance Approval");
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to confirm transaction?") == true) {
                        if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }
                        
                        ShowMessageFX.Information("Transaction approved successfully", "Check Release Confirmation", null);
                        
                        clearAllInputs();
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;
                case "btnVoid":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, "Check Release Confirmation");
                        return;
                    }
                    
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Void/Cancel transaction?") == true) {
                        
                        //Update transaction based on button text
                        if (btnVoid.getText().equals("Void")) {
                            if (!isJSONSuccess(poAppController.VoidTransaction(), "Initialize Void Transaction")) {
                                return;
                            }
                        } else {
                            if (!isJSONSuccess(poAppController.CancelTransaction(), "Initialize Cancel Transaction")) {
                                return;
                            }

                        }
                        ShowMessageFX.Information("Transaction voided successfully", "Check Release Confirmation", null);
                        
                        //clear only, do not reload as it will not be reloaded upon the query
                        clearAllInputs();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;

                case "btnPrint":
                    if (poAppController.GetMaster().getTransactionNo() == null || poAppController.GetMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Check Release Confirmation", "");
                        return;
                    }
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to print the transaction ?") == true) {
                        if (!isJSONSuccess(poAppController.PrintRecord(),
                                "Initialize Print Transaction")) {
                            return;
                        }
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;
                    
                case "btnUpdate":
                    if (poAppController.GetMaster().getTransactionNo() == null || poAppController.GetMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Check Release Confirmation", "");
                        return;
                    }
                    poAppController.OpenTransaction(poAppController.GetMaster().getTransactionNo());
                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize UPdate Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    
                    pnEditMode = poAppController.getEditMode();
                    initButtonDisplay(poAppController.getEditMode());
                    break;
                    
                case "btnSave":
                    if (poAppController.GetMaster().getTransactionNo() == null || poAppController.GetMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Initialize Save Transaction", "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.SaveTransaction(), "Initialize Save Transaction")) {
                        return;
                    }
                    ShowMessageFX.Information("Transaction saved successfully", "Check Release Confirmation", null);
                    
                    clearAllInputs();
                    getLoadedTransaction();
                    initButtonDisplay(poAppController.getEditMode());
                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new CheckController(poApp, poLogWrapper).CheckRelease();
                        poAppController.setTransactionStatus("0");

                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {

                            poAppController.setTransactionStatus("0");
                            poAppController.GetMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);

                            clearAllInputs();
                        });
                        break;
                    }
                    break;
                    
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;

                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    break;
                    
            }
        }catch(Exception e){
            Logger.getLogger(DeliverySchedule_EntryController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    
    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        
        try{
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            
                            case "tfSearchTransNo":

                                if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {

                                    if (poAppController.GetMaster().getTransactionNo() != null) {

                                        if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {

                                            if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                                return;
                                            }

                                        }

                                    }
                                }

                                if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchTransNo.getText().toString(),  true), "Initialize Search Check Release Master")) {
                                    return;
                                }

                                clearAllInputs();
                                getLoadedTransaction();

                                break;

                            case "tfSearchReceived":

                                if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UNKNOWN) {

                                    if (poAppController.GetMaster().getTransactionNo() != null) {

                                        if (!poAppController.GetMaster().getTransactionNo().isEmpty()) {

                                            if (ShowMessageFX.OkayCancel(null, "Initialize Search Check Release Master", "Do you want to disregard changes?") == false) {
                                                return;
                                            }

                                        }

                                    }
                                }

                                if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchReceived.getText().toString(), false), "Initialize Search Check Release Master")) {
                                    return;
                                }

                                clearAllInputs();
                                getLoadedTransaction();

                                break;

                            case "tfSearchPayee":
                                if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchPayee.getText().toString(), true, false), "Initialize Search Check Release Master")) {
                                    break;
                                }
                                LoadCheckPayments();
                                break;

                            case "tfSearchCheck":
                                if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), false, false), "Initialize Search Check Release Master")) {
                                    break;
                                }
                                LoadCheckPayments();
                                break;
                            
                            case "tfSearchCheckRef":
                                if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), true, true), "Initialize Search Check Release Master")) {
                                    break;
                                }
                                LoadCheckPayments();
                                break;
                                
                        }
                }
            }
        }catch(Exception e){
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
            
    }
    
    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        
        try {
            if (lsValue == null) {
                return;
            }

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    
                    case "tfCheckAmt":
                        if (poAppController.GetMaster().getTransactionNo().isEmpty()) { //check master transaction no if loaded
                            ShowMessageFX.Information("Please load transaction!", "Initalize Check Amount", null);
                            return;
                        }
                        
                        if (tfCheckNo.getText().isEmpty()) { //validate check no if loaded
                            ShowMessageFX.Information("Please load check detail!", "Initalize Check Amount", null);
                            return;
                        }
                        
                        if (tfCheckAmt.getText().toString().isEmpty() || tfCheckAmt.getText() == null) {
                            ShowMessageFX.Information("Check amount is invalid.", "Initalize Check Amount", null);
                            tfCheckAmt.requestFocus();
                            return;
                        }
                        
                        double ldblCheckAmt = Double.parseDouble(tfCheckAmt.getText().toString());
                        if (ldblCheckAmt < 0) {
                            ShowMessageFX.Information("Check amount is invalid.", "Initalize Check Amount", null);
                            tfCheckAmt.requestFocus();
                            return;
                        }
                        
                        if (ldblCheckAmt == 0) {
                            if (!ShowMessageFX.OkayCancel("Check amount is 0. It will be disregarded on saving detail list. Continue?", "Initalize Check Amount", null)) {
                                
                                //set back to default amount and focus
                                tfCheckAmt.setText(String.valueOf(poAppController.GetDetail(pnTransactionDetail).CheckPayment().getAmount()));
                                tfCheckAmt.requestFocus();
                                return;
                            }
                        }
                        
                        //check set amount, should be '0' (to remove from saving) or exact original amount
                        if ((ldblCheckAmt != poAppController.GetDetail(pnTransactionDetail).CheckPayment().getAmount()) && ldblCheckAmt > 0) {
                            ShowMessageFX.Information("Check amount should be zero or same as original amount!", "Initalize Check Amount", null);
                            
                            //set back to default amount and put focus
                            tfCheckAmt.setText(String.valueOf(poAppController.GetDetail(pnTransactionDetail).CheckPayment().getAmount()));
                            tfCheckAmt.requestFocus();
                            return;
                        }
                        
                        poAppController.GetDetail(pnTransactionDetail).CheckPayment().setAmount(Double.parseDouble(tfCheckAmt.getText().toString()));
                        ComputeTotal();
                        getLoadedTransaction();
                        break;
                        
                    case "tfReceivedBy":
                        poAppController.GetMaster().setReceivedBy(tfReceivedBy.getText().toString());
                        break;
                        
                    case "tfNote":
                        poAppController.GetDetail(pnTransactionDetail).CheckPayment().setRemarks(tfNote.getText().toString());
                        break;
                }
            } else {
                loTextField.selectAll();
            }
        } catch (Exception ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };
    
    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            
            /*Lost Focus*/
            switch (lsDatePickerID) {
                
                case "dpTransactionDate":
                    poAppController.GetMaster().setTransactionDate(Date.from(dpTransactionDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    break;
            }
        }
    };
    
    private void InitTransactionMaster(){
        
        try {
            
            lblSource.setText(poAppController.GetMaster().Industry().getDescription() == null ? "" : poAppController.GetMaster().Industry().getDescription());
            lblStatus.setText(CheckReleaseStatus.STATUS.get(Integer.parseInt(poAppController.GetMaster().getTransactionStatus())) == null ? "STATUS"
                    : CheckReleaseStatus.STATUS.get(Integer.parseInt(poAppController.GetMaster().getTransactionStatus())));
            dpTransactionDate.setValue(ParseDate(poAppController.GetMaster().getTransactionDate()));
            tfTransNo.setText(poAppController.GetMaster().getTransactionNo());
            tfReceivedBy.setText(poAppController.GetMaster().getReceivedBy());
            taRemarks.setText(poAppController.GetMaster().getRemarks());
            tfTotal.setText((String.valueOf(poAppController.GetMaster().getTransactionTotal())));
            
            //initialize button text upon load of status
            if (poAppController.GetMaster().getTransactionStatus().equals(CheckReleaseStatus.CONFIRMED)) {
                btnVoid.setText("Cancel");
            } else {
                btnVoid.setText("Void");
            }
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckRelease_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void InitCheckDetail(){
        
        try{
            
            //return if index selected is less than 0
            if (pnTransactionDetail < 0) { return; }
            
            //deduct 1, if index selected is equal to list size
            if (pnTransactionDetail == poAppController.getDetailCount()) { pnTransactionDetail = pnTransactionDetail - 1; }

            Model_Check_Payments loCheck = poAppController.GetDetail(pnTransactionDetail).CheckPayment();

            tfSearchCheckRef.setText(loCheck.getTransactionNo());
            tfPayee.setText(loCheck.Payee().getPayeeName());
            tfParticular.setText(loCheck.Banks().getBankName());
            dpCheckDate.setValue(ParseDate(loCheck.getCheckDate()));
            tfCheckNo.setText(loCheck.getCheckNo());
            tfCheckAmt.setText(String.valueOf(loCheck.getAmount()));
            tfNote.setText(loCheck.getRemarks());
            
        }catch(Exception e){
            Logger.getLogger(CheckRelease_ConfirmationController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    
    private void LoadCheckPayments(){
        
        StackPane overlay = getOverlayProgress(apTransaction);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);
        
        Task<ObservableList<Model_Check_Payments>> loadCheckPayment = new Task<ObservableList<Model_Check_Payments>>() {
            @Override
            protected ObservableList<Model_Check_Payments> call() throws Exception {
                
                laCheckListPayment.setAll(poAppController.GetCheckPaymentList());
                return laCheckListPayment;
            }
            
            @Override
            protected void succeeded() {
                ObservableList<Model_Check_Payments> laMasterList = getValue();
                tblViewMaster.setItems(laMasterList);

                tblColNo.setCellValueFactory((loModel) -> {
                    int index = tblViewMaster.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });

                tblColTransNo.setCellValueFactory((loModel) -> {
                    try{
                        return new SimpleStringProperty(loModel.getValue().getTransactionNo());
                    }catch(Exception e){
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                tblColTransDate.setCellValueFactory((loModel) -> {
                    try{
                        return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
                    }catch(Exception e){
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                tblColCheckNo.setCellValueFactory((loModel) -> {
                    try{
                        return new SimpleStringProperty(loModel.getValue().getCheckNo());
                    }catch(Exception e){
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                tblColCheckAmt.setCellValueFactory((loModel) -> {
                    try{
                        return new SimpleStringProperty(String.valueOf(loModel.getValue().getAmount()));
                    }catch(Exception e){
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });
                
                overlay.setVisible(false);
                pi.setVisible(false);

            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(CheckRelease_ConfirmationController.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadCheckPayment);
        thread.setDaemon(true);
        thread.start();
 
    }
    
    private void LoadTransactionDetails(){
        
        if (laCheckListDetail == null) {
            
            laCheckListDetail = FXCollections.observableArrayList();
            
            tblViewDetails.setItems(laCheckListDetail);

            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailReference.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getTransactionNo());
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailPayee.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Payee().getPayeeName());
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailParticular.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Banks().getBankName());
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckDt.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getCheckDate()));
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckNo.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getCheckNo());
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailAmt.setCellValueFactory((loModel) -> {
                try{
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getAmount()));
                }catch(Exception e){
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
        }
    }
    
    private void ComputeTotal() throws SQLException, GuanzonException{
        
        double ldbl_total = 0.00;
        for (Model_Check_Release_Detail loPayments : poAppController.GetDetailList()) {
            ldbl_total = ldbl_total + loPayments.CheckPayment().getAmount();
        }
        
        poAppController.GetMaster().setTransactionTotal(ldbl_total);
    }
    
    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
            }
        }

        clearAllInputs();
    }
    
    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            }
        }
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());
        
        try {
            LocalDate today = LocalDate.now();
   
            dpCheckDate.setValue(ParseDate((Date) poApp.getServerDate()));
            dpCheckDtFrm.setValue(today.minusMonths(1));
            dpCheckDTTo.setValue(ParseDate((Date) poApp.getServerDate()));
        } catch (SQLException ex) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnRetrieve", "btnHistory", "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnNew", "btnUpdate", "btnApprove", "btnVoid", "btnPrint");

        apMaster.setDisable(!lbShow);
        apDetail.setDisable(!lbShow);
    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }
    
    private void reloadTableDetail(){
        
        //load check payments
        laCheckListDetail.setAll(poAppController.GetDetailList());
  
        // Restore or select last row
        int indexToSelect = (pnTransactionDetail > 0 && pnTransactionDetail < laCheckListDetail.size())
                ? pnTransactionDetail - 1
                : laCheckListDetail.size() - 1;

        tblViewDetails.getSelectionModel().select(indexToSelect);

        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblViewDetails.refresh();
        
    }
    
    private void getLoadedTransaction(){
        InitTransactionMaster();
        reloadTableDetail();
        InitCheckDetail();
    }
    
    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            poLogWrapper.severe(psFormName + " :" + message);
            Platform.runLater(() -> {
                ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        poLogWrapper.severe(psFormName + " :" + message);
//        Platform.runLater(() -> {
//            if (message != null) {
//                ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
//            }
//        });
        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;

    }
    
    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }
    
    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }
    
}
