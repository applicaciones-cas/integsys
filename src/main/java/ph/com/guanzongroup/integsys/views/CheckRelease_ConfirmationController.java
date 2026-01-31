/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import ph.com.guanzongroup.cas.cashflow.CheckRelease;
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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
import ph.com.guanzongroup.cas.cashflow.status.CheckReleaseStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Master;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;
import ph.com.guanzongroup.cas.cashflow.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.model.ModelTableMain;

/**
 *
 * @author Guillier
 */
public class CheckRelease_ConfirmationController implements Initializable, ScreenInterface{
    
    private GRiderCAS poApp;
    private JSONObject poJSON;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Release Confirmation";
    private String psIndustryID;
    private Control lastFocusedControl;
    private CheckRelease poAppController;
    
     private unloadForm poUnload = new unloadForm();
     
    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<Model_Check_Payments> laCheckListPayment = FXCollections.observableArrayList();
    private ObservableList<Model_Check_Release_Detail> laCheckListDetail;
    
    private int pnSelectMaster, pnTransactionDetail, pnEditMode;
    
    @FXML
    private CheckBox cbReverse;
    
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
    private TableView<ModelTableMain> tblViewMaster;
    
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

            if (e.getClickCount() == 2 && !e.isConsumed()) {
                e.consume();
                 if (!tfTransNo.getText().isEmpty()) {
                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                        return;
                    }
                }

                //load item's check detail, increase 1 to get the right validation for adding to the list
                if (!isJSONSuccess(poAppController.SearchTransaction(tblColTransNo.getCellData(pnSelectMaster), true), psFormName)) {
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
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            
                CheckBox checkedBox = (CheckBox) source;
                switch (checkedBox.getId()) {
                    case "cbReverse": // this is the id
                        if (poAppController.getEditMode() == EditMode.ADDNEW
                                || poAppController.getEditMode() == EditMode.UPDATE
                                && poAppController.GetMaster().getTransactionStatus().equals(CheckReleaseStatus.OPEN)
                                || poAppController.GetMaster().getTransactionStatus().equals(CheckReleaseStatus.CONFIRMED)) {
                            if (poAppController.Detail(pnTransactionDetail).getSourceNo() != null
                                    || !poAppController.Detail(pnTransactionDetail).getSourceNo().isEmpty()) {
                                if (!checkedBox.isSelected()) {
                                    poAppController.Detail().remove(pnTransactionDetail);
                                    pnTransactionDetail = pnTransactionDetail - 1;
                                }
                            }
                        } else {
                            poAppController.Detail(pnTransactionDetail).isReverse(checkedBox.isSelected());
                        }
                        reloadTableDetail();
                        
                        break;
                }
            
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
                            
                        case "tfSearchCheck":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), false, false), "Initialize Search Check Release Master")) {
                                break;
                            }
                            loadTableMain();
                            break;
                            
                        case "tfSearchCheckRef":
                            if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), true, true), "Initialize Search Check Release Master")) {
                                break;
                            }
                            loadTableMain();
                            break;
                    }
                    break;
                    
                case "btnBrowse":
                    if(lastFocusedControl == null){
                            if (!tfTransNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.SearchTransaction(tfSearchTransNo.getText().toString(), true), "Initialize Search Check Release Master")) {
                                break;
                            }
                            clearAllInputs();
                            getLoadedTransaction();
                            lastFocusedControl = null;
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

                    }
                    break;
                    
                case "btnRetrieve":
                    
                    
                    loadTableMain();
//                    if(lastFocusedControl == null){
//                        if (!isJSONSuccess(poAppController.LoadCheckListByDate(String.valueOf(dpCheckDtFrm.getValue()), String.valueOf(dpCheckDTTo.getValue())),
//                                    "Initialize : Load of Transaction List")) {
//                                return;
//                            }
//                            LoadCheckPayments();
//                            break;
//                    }
//                    switch (lastFocusedControl.getId()) {
//                        case "dpCheckDtFrm":
//                        case "dpCheckDTTo":
//                            if (!isJSONSuccess(poAppController.LoadCheckListByDate(String.valueOf(dpCheckDtFrm.getValue()), String.valueOf(dpCheckDTTo.getValue())),
//                                    "Initialize : Load of Transaction List")) {
//                                return;
//                            }
//                            LoadCheckPayments();
//                            break;
//                    }
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
                                loadTableMain();
                                break;

                            case "tfSearchCheck":
                                loadTableMain();
                                break;
                            
                            case "tfSearchCheckRef":
                                if (!isJSONSuccess(poAppController.SearchCheckTransaction(tfSearchCheck.getText().toString(), true, true), "Initialize Search Check Release Master")) {
                                    break;
                                }
                                loadTableMain();
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
                                tfCheckAmt.setText(String.valueOf(poAppController.Detail(pnTransactionDetail).CheckPayment().getAmount()));
                                tfCheckAmt.requestFocus();
                                return;
                            }
                        }
                        
                        //check set amount, should be '0' (to remove from saving) or exact original amount
                        if ((ldblCheckAmt != poAppController.Detail(pnTransactionDetail).CheckPayment().getAmount()) && ldblCheckAmt > 0) {
                            ShowMessageFX.Information("Check amount should be zero or same as original amount!", "Initalize Check Amount", null);
                            
                            //set back to default amount and put focus
                            tfCheckAmt.setText(String.valueOf(poAppController.Detail(pnTransactionDetail).CheckPayment().getAmount()));
                            tfCheckAmt.requestFocus();
                            return;
                        }
                        
                        poAppController.Detail(pnTransactionDetail).CheckPayment().setAmount(Double.parseDouble(tfCheckAmt.getText().toString()));
                        ComputeTotal();
                        getLoadedTransaction();
                        break;
                        
                    case "tfReceivedBy":
                        poAppController.GetMaster().setReceivedBy(tfReceivedBy.getText().toString());
                        break;
                        
                    case "tfNote":
                        poAppController.Detail(pnTransactionDetail).CheckPayment().setRemarks(tfNote.getText().toString());
                        break;
                    case "tfSearchPayee":
                        loadTableMain();
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
                case "dpCheckDtFrm":
                case "dpCheckDTTo" :
                    loadTableMain();
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

            Model_Check_Payments loCheck = poAppController.Detail(pnTransactionDetail).CheckPayment();

            tfSearchCheckRef.setText(loCheck.getTransactionNo());
            tfPayee.setText(loCheck.Payee().getPayeeName());
            tfParticular.setText(loCheck.Banks().getBankName());
            dpCheckDate.setValue(ParseDate(loCheck.getCheckDate()));
            tfCheckNo.setText(loCheck.getCheckNo());
            tfCheckAmt.setText(String.valueOf(loCheck.getAmount()));
            tfNote.setText(loCheck.getRemarks());
            cbReverse.setSelected(
                    poAppController.Detail(pnTransactionDetail) != null
                    && poAppController.Detail(pnTransactionDetail).isReverse()
            );
            
            
        }catch(Exception e){
            Logger.getLogger(CheckRelease_ConfirmationController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }
    private void initTableMaster() {
        tblColNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblColTransNo.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblColTransDate.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblColCheckNo.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblColCheckAmt.setCellValueFactory(new PropertyValueFactory<>("index05"));

        tblViewMaster.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewMaster.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
    }
    
    private void loadTableMain() {
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
                    poJSON = poAppController.getCheckRelease(tfSearchPayee.getText(), 
                                                                   tfSearchCheck.getText(),
                                                                  dpCheckDtFrm.getValue(),
                                                                  dpCheckDTTo.getValue());
                    
                    if ("success".equals(poJSON.get("result"))) {
                        if (poAppController.getCheckReleaseMasterCount()> 0) {
                            for (int lnCntr = 0; lnCntr < poAppController.getCheckReleaseMasterCount(); lnCntr++) {
                                main_data.add(new ModelTableMain(
                                        String.valueOf(lnCntr + 1),
                                        poAppController.poCheckReleaseMaster(lnCntr).getTransactionNo(),
                                        CustomCommonUtil.formatDateToMMDDYYYY(poAppController.poCheckReleaseMaster(lnCntr).getTransactionDate()),
                                        poAppController.poCheckReleaseMaster(lnCntr).getReceivedBy(),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poAppController.poCheckReleaseMaster(lnCntr).getTransactionTotal(), true),
                                        "", "", "", "", ""));
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
                    ShowMessageFX.Warning("No Record Payment Request to Load.", psFormName, null);
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
                    return new SimpleStringProperty(
                            loModel.getValue().CheckPayment().getCheckDate() == null
                            ? ""
                            : loModel.getValue().CheckPayment().getCheckDate().toString()
                    );
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
            } else if (loControl instanceof CheckBox) {
                CheckBox loControlField = (CheckBox) loControl;
                controllerFocusTracker(loControlField);
            }
        }
        initTableMaster();
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
            dpCheckDtFrm.setValue(today.minusDays(7));
            dpCheckDTTo.setValue(LocalDate.now());
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
