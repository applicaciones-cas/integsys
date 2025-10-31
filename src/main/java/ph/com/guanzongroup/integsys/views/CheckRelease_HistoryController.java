package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.cashflow.status.CheckReleaseStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Release_Detail;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;

/**
 *
 * @author User
 */
public class CheckRelease_HistoryController implements Initializable, ScreenInterface{
    
    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Release History";
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
            tfCheckAmt, tfNote, tfCheckNo;
    
    @FXML
    private TextArea taRemarks;
    
    @FXML
    private DatePicker dpTransactionDate, dpCheckDate;
    
    @FXML
    private Button btnPost, btnBrowse, btnPrint, btnClose;
    
    @FXML
    private TableView<Model_Check_Release_Detail> tblViewDetails;
    
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
            Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, e);
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
                    
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        break;
                    }

                    switch (lastFocusedControl.getId()) {
                        
                        case "tfSearchTransNo":
                             if (!isJSONSuccess(poAppController.SearchTransactionPosting(tfSearchTransNo.getText().toString(), true), "Initialize Search Check Release Master")) {
                                break;
                            }
                            getLoadedTransaction();
                            
                            break;
                        
                        case "tfSearchReceived":
                             if (!isJSONSuccess(poAppController.SearchTransactionPosting(tfSearchReceived.getText().toString(), false), "Initialize Search Check Release Master")) {
                                return;
                            }
                            getLoadedTransaction();
                            
                            break;
                    }
                    break;
                    
                case "btnPost":
                    if (tfTransNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", "Inventory Stock Issuance Posting", "");
                        return;
                    }
                    if (!isJSONSuccess(poAppController.ReleaseTransaction(), "Initialize Post Transaction")) {
                        return;
                    }
                    
                    ShowMessageFX.Information("Transaction posted successfully", "Check Release History", null);
                    
                    reloadTableDetail();
                    pnEditMode = poAppController.getEditMode();
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
                    
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                    
            }
        }catch(Exception e){
            Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, e);
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
 
                                if (!isJSONSuccess(poAppController.SearchTransactionPosting(tfSearchTransNo.getText().toString(), true), "Initialize Search Check Release Master")) {
                                    return;
                                }
                                getLoadedTransaction();
                                
                                break;
                        
                            case "tfSearchReceived":

                                if (!isJSONSuccess(poAppController.SearchTransactionPosting(tfSearchReceived.getText().toString(),  false), "Initialize Search Check Release Master")) {
                                    return;
                                }
                                getLoadedTransaction();
                                
                                break;
                        }
                }
            }
        }catch(Exception e){
            Logger.getLogger(CheckRelease_HistoryController.class
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
            
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CheckRelease_HistoryController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
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
        initButtonDisplay();
        
        try {
            dpCheckDate.setValue(ParseDate((Date) poApp.getServerDate()));
        } catch (SQLException ex) {
            Logger.getLogger(CheckDeposit_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initButtonDisplay() {
        boolean lbShow = Integer.parseInt(poAppController.GetMaster().getTransactionStatus()) == 1; //confirm status

        //always show
        initButtonControls(true, "btnBrowse", "btnPrint", "btnClose");
        
        //show base on status
        initButtonControls(lbShow, "btnPost");

        //disable editing of transaction
        apMaster.setDisable(true);
        apDetail.setDisable(true);
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
        clearAllInputs();
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
    
}
