package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.views.ScreenInterface;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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
import javafx.geometry.Insets;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.cas.client.account.AP_Client_Master;
import org.guanzon.cas.client.constants.APPaymentConstants;
import org.guanzon.cas.client.model.Model_AP_Client_Ledger;
import org.guanzon.cas.client.services.ClientControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class AccountsPayablexController implements Initializable, ScreenInterface {

    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private Control lastFocusedControl;
    private AP_Client_Master poAppController;

    private String psFormName = "Account Payable";
    private int pnLedger, pnAttachments, currentIndex;

    private ObservableList<Model_AP_Client_Ledger> laLedger;
    private ObservableList<ModelDeliveryAcceptance_Attachment> laAttachments = FXCollections.observableArrayList();

    private HashMap<String, String> imageinfo_temp = new HashMap<>();

    private unloadForm poUnload = new unloadForm();

    private JFXUtil.ReloadableTableTask loadTableAttachment;

    @FXML
    private AnchorPane apMainAnchor, apRecord, apLedger, apAttachments;
    @FXML
    private Label lblStatus;

    @FXML
    private Button btnSearch, btnBrowse, btnCancel, btnUpdate, btnSave,
            btnRetrieve, btnClose;

    @FXML
    private TableView<Model_AP_Client_Ledger> tblLedger;

    @FXML
    private TableColumn<Model_AP_Client_Ledger, String> tblColNo, tblLedgerNo, tblColDate, tblColSourceNo, tblColSourceCode, tblColAmountIn, tblColAmountOut;

    @FXML
    private TextField tfSearchCompanyName, tfSearchClient, tfClientID, tfContactPerson,
            tfCompanyName, tfCreditLimit, tfBegBalanace, tfCategory,
            tfDiscount, tfTerm, tfAvailBalance, tfOutStandingBalance,
            tfAddress, tfContactNo, tfTINNo, tfContactEmail;

    @FXML
    StackPane stackpane;

    @FXML
    private TextField txtAttachmentNo;

    @FXML
    private TableView<ModelDeliveryAcceptance_Attachment> tblAttachments;

    @FXML
    private TableColumn<ModelDeliveryAcceptance_Attachment, String> tblColAttachIndex, tblColAttachFile;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Button btnAttach, btnRemoveFile, btnPrevious, btnNext;

    @FXML
    private DatePicker dpBegBalance, dpClientSince;

    @FXML
    private CheckBox cbVatable, cbHasPermit, cbBackOrder, cbHoldOrder;
    
    @FXML
    private ComboBox<String> cmbPayment, cmbRegistration;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
//        psIndustryID = fsValue;
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

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

            //initlalize and validate record objects from class controller
            //background thread
            Platform.runLater(() -> {
                poAppController.setRecordStatus("10");
            });

            initializeTableLedger();
            initControlEvents();
            initAttachmentsGrid();
            initTableAttachments();
            initAttachmentPreviewPane();

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(AccountsPayablexController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    void tblLedger_Clicked(MouseEvent e) {
        pnLedger = tblLedger.getSelectionModel().getSelectedIndex();
        if (pnLedger < 0) {
            return;
        }
    }

    @FXML
    void tblAttachment_Clicked(MouseEvent e) {
        pnAttachments = tblAttachments.getSelectionModel().getSelectedIndex();

        if (pnAttachments >= 0) {
            int lnRow = Integer.parseInt(laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
            pnAttachments = lnRow;
            imageviewerutil.scaleFactor = 1.0;

            loadRecordAttachment(true);
            JFXUtil.resetImageBounds(imgPreview, stackpane);
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfTerm":
                            if (!isJSONSuccess(poAppController.searchTerm(tfTerm.getText() == null ? "" : tfTerm.getText(), false),
                                    "Initialize Search Category! ")) {
                                return;
                            }
                            loadClientMaster();
                            break;

                    }
                    break;

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfClientID.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                "")) {
                            return;
                        }
                        poAppController.loadAttachments();

                        getLoadedClient();
                        initButtonDisplay(poAppController.getEditMode());
                        return;
                    }

                    switch (lastFocusedControl.getId()) {

                        case "tfSearchClient":
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                    "Initialize Search Client! ")) {
                                return;
                            }
                            clearAllInputs();
                            
                            poAppController.loadAttachments();
                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchCompanyName":
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompanyName.getText(), false),
                                    "Initialize Search Client! ")) {
                                return;
                            }
                            clearAllInputs();
                            
                            poAppController.loadAttachments();
                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;

                        default:
                            if (!tfClientID.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                    "Initialize Search Client! ")) {
                                return;
                            }
                            poAppController.loadAttachments();

                            getLoadedClient();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnUpdate":
                    if (poAppController.getModel().getClientId() == null || poAppController.getModel().getClientId().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    poAppController.openRecord(poAppController.getModel().getClientId());
                    if (!isJSONSuccess(poAppController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    poAppController.loadAttachments();

                    getLoadedClient();
                    initButtonDisplay(poAppController.getEditMode());
                    break;

                case "btnSave":
                    if (tfClientID.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.saveRecord(), "Initialize Save Record")) {
                        return;
                    }
                    
                    poAppController.openRecord(poAppController.getModel().getClientId());
                    if (!isJSONSuccess(poAppController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    clearAllInputs();
                    
                    poAppController.loadAttachments();
                    getLoadedClient();
                    initButtonDisplay(poAppController.getEditMode());

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

                        Platform.runLater(() -> {
//                            poAppController.setRecordStatus("01");
//                            poAppController.setRecordStatus("07");

                            clearAllInputs();
                        });
                        break;
                    }
                    break;

                case "btnRetrieve":
                    loadLedgerList();
                    reloadTableLedger();
                    break;

                case "btnAttach":

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose Image");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.pdf")
                    );
                    java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAttach.getScene().getWindow());
                    if (selectedFile != null) {
                        // Read image from the selected file
                        Path imgPath = selectedFile.toPath();
                        Image loimage = new Image(Files.newInputStream(imgPath));

                        imgPreview.setImage(loimage);

                        //Validate attachment
                        String imgPath2 = selectedFile.getName().toString();
                        for (int lnCtr = 0; lnCtr <= poAppController.getTransactionAttachmentCount() - 1; lnCtr++) {

                            if (imgPath2.equals(poAppController.getAttachmentList().get(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poAppController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {

                                ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                                pnAttachments = lnCtr;

                                loadRecordAttachment(true);
                                return;
                            }
                        }

                        //map selected file details
                        if (imageinfo_temp.containsKey(selectedFile.getName().toString())) {
                            ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                            loadRecordAttachment(true);
                            return;
                        } else {
                            imageinfo_temp.put(selectedFile.getName().toString(), imgPath.toString());
                        }

                        //Limit maximum pages of pdf to add
                        if (imgPath2.toLowerCase().endsWith(".pdf")) {
                            try (PDDocument document = PDDocument.load(selectedFile)) {
                                PDFRenderer pdfRenderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();
                                if (pageCount > 5) {
                                    ShowMessageFX.Warning(null, psFormName, "PDF exceeds maximum allowed pages.");
                                    return;
                                }
                            }
                        }

                        pnAttachments = poAppController.addAttachment(imgPath2);

                        //Copy file to Attachment path
                        poAppController.copyFile(selectedFile.toString());

                        //reload table
                        loadTableAttachment.reload();

                        //focus last row
                        tblAttachments.getFocusModel().focus(pnAttachments);
                        tblAttachments.getSelectionModel().select(pnAttachments);
                    }
                    break;
                case "btnRemoveFile":
                    if (poAppController.getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poAppController.getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poAppController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachments == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }

                    JSONObject loJSON = poAppController.removeAttachment(pnAttachments);
                    if ("error".equals((String) loJSON.get("result"))) {
                        ShowMessageFX.Warning(null, psFormName, (String) loJSON.get("message"));
                        return;
                    }
                    laAttachments.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachments != 0) {
                        pnAttachments -= 1;
                    }
                    imageinfo_temp.clear();
                    loadRecordAttachment(false);

                    loadTableAttachment.reload();
                    if (laAttachments.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnNext":
                    slideImage(1);
                    break;
                case "btnPrevious":
                    slideImage(-1);
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

            //reset edit mode, upon save button
            if (btnID.equalsIgnoreCase("btnSave")) {
                initButtonDisplay(EditMode.UNKNOWN);
                return;
            }
            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(e));
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        lsValue.replace(",", "");

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
                case "tfCreditLimit":
                    if (poAppController.getModel().getClientId() == null
                            || poAppController.getModel().getClientId() == null) {
                        if (Double.parseDouble(lsValue) > 0.0) {
                            tfCreditLimit.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Credit Limit! No Client Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnCreditAmount;
                    try {
                        lnCreditAmount = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnCreditAmount = 0.0; // default if parsing fails
                        poAppController.getModel().setCreditLimit(lnCreditAmount);
                        loadClientMaster();
                        loTextField.requestFocus();
                    }
                    if (lnCreditAmount < 0.00) {
                        return;
                    }

                    poAppController.getModel().setCreditLimit(lnCreditAmount);
                    loadClientMaster();
                    break;

                case "tfDiscount":
                    if (poAppController.getModel().getClientId() == null
                            || poAppController.getModel().getClientId() == null) {
                        if (Double.parseDouble(lsValue) > 0.0) {
                            tfDiscount.setText("0.00");
                            loTextField.requestFocus();
                            ShowMessageFX.Information("Unable to set Discount! No Client Detected", psFormName, null);
                        }
                        return;
                    }
                    double lnDiscount;
                    try {
                        lnDiscount = Double.parseDouble(lsValue);
                    } catch (NumberFormatException e) {
                        lnDiscount = 0.0; // default if parsing fails
                        poAppController.getModel().setDiscount(lnDiscount);
                        loadClientMaster();
                        loTextField.requestFocus();
                    }
                    if (lnDiscount < 0.00) {
                        return;
                    }

                    poAppController.getModel().setDiscount(lnDiscount);
                    loadClientMaster();
                    break;

            }
        } else {
            loTextField.selectAll();
        }

    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchClient":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchClient.getText(), true),
                                        "")) {
                                    return;
                                }
                                clearAllInputs();
                            
                                poAppController.loadAttachments();
                                getLoadedClient();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchCompanyName":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchRecord(tfSearchCompanyName.getText(), false),
                                        "")) {
                                    return;
                                }
                                clearAllInputs();
                            
                                poAppController.loadAttachments();
                                getLoadedClient();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                            case "tfTerm":
                                if (!isJSONSuccess(poAppController.searchTerm(tfTerm.getText() == null ? "" : tfTerm.getText(), false),
                                        "Initialize Search Category! ")) {
                                    return;
                                }
                                loadClientMaster();
                                break;

                        }
                        break;
                }
            }
        } catch (Exception ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

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
                case "dpClientSince":
                    poAppController.getModel().setdateClientSince(ldDateValue);
                    return;

            }
        }
    };

    private void loadClientMaster() {
        try {
            lblStatus.setText(poAppController.getModel().getRecordStatus().equals("0") == false ? "ACTIVE" : "INACTIVE");

            tfClientID.setText(poAppController.getModel().getClientId());

            tfCategory.setText(poAppController.getModel().Category().getDescription());
            tfCompanyName.setText(poAppController.getModel().Client().getCompanyName());
            tfAddress.setText(poAppController.getModel().ClientAddress().getAddress());
            tfContactPerson.setText(poAppController.getModel().ClientInstitutionContact().getContactPersonName());
            tfContactEmail.setText(poAppController.getModel().ClientInstitutionContact().getMailAddress());
            tfContactNo.setText(poAppController.getModel().ClientInstitutionContact().getMobileNo());
            tfTINNo.setText(poAppController.getModel().Client().getTaxIdNumber());
            
            cbHasPermit.setSelected(poAppController.getModel().hasPermit() == null || !poAppController.getModel().hasPermit().equalsIgnoreCase("1") ? false : true);
            cbBackOrder.setSelected(poAppController.getModel().isBackOrder() == null || !poAppController.getModel().isBackOrder().equalsIgnoreCase("1") ? false : true);
            cbVatable.setSelected(poAppController.getModel().getVatable() == null || !poAppController.getModel().getVatable().equalsIgnoreCase("1") ? false : true);
            cbHoldOrder.setSelected(poAppController.getModel().isHoldOrder() == null || !poAppController.getModel().isHoldOrder().equalsIgnoreCase("1") ? false : true);
            
            //registration list
            if (poAppController.getModel().getVatRegstr()== null) {
            }else{
                cmbRegistration.getSelectionModel().select(Integer.parseInt(poAppController.getModel().getVatRegstr()));
            }
            
            //payment method
            if (poAppController.getModel().getPayment() == null) {
            }else{
                cmbPayment.getSelectionModel().select(Integer.parseInt(poAppController.getModel().getPayment()));
            }
            
            dpClientSince.setValue(poAppController.getModel().getdateClientSince() == null ? null : ParseDate(poAppController.getModel().getdateClientSince()));
            dpBegBalance.setValue(poAppController.getModel().getBeginningDate() == null ? null : ParseDate(poAppController.getModel().getBeginningDate()));
            tfDiscount.setText(CommonUtils.NumberFormat(poAppController.getModel().getDiscount(), "###,###,##0.0000"));
            tfTerm.setText(poAppController.getModel().Term().getDescription());
            tfCreditLimit.setText(CommonUtils.NumberFormat(poAppController.getModel().getCreditLimit(), "###,###,##0.0000"));
            tfBegBalanace.setText(CommonUtils.NumberFormat(poAppController.getModel().getBeginningBalance(), "###,###,##0.0000"));
            tfAvailBalance.setText(CommonUtils.NumberFormat(poAppController.getModel().getAccountBalance(), "###,###,##0.0000"));
            tfOutStandingBalance.setText(CommonUtils.NumberFormat(poAppController.getModel().getOBalance(), "###,###,##0.0000"));

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
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

        //has permit
        cbHasPermit.selectedProperty().addListener((obs, oldVal, newVal) -> {
            poAppController.getModel().hasPermit(cbHasPermit.isSelected() == true ? "1" : "0");
        });
        //has back order
        cbBackOrder.selectedProperty().addListener((obs, oldVal, newVal) -> {
            poAppController.getModel().isBackOrder(cbBackOrder.isSelected() == true ? "1" : "0");
        });
        //vatable
        cbVatable.selectedProperty().addListener((obs, oldVal, newVal) -> {
            poAppController.getModel().setVatable(cbVatable.isSelected() == true ? "1" : "0");
        });
        //is hold order
        cbHoldOrder.selectedProperty().addListener((obs, oldVal, newVal) -> {
            poAppController.getModel().isHoldOrder(cbHoldOrder.isSelected() == true ? "1" : "0");
        });
        //vat registration
        cmbRegistration.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                int lnIndex = newIndex.intValue(); // the selected index
                poAppController.getModel().setVatRegstr(String.valueOf(lnIndex));
            }
        });
        //payment method
        cmbPayment.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (newIndex != null && newIndex.intValue() >= 0) {
                int lnIndex = newIndex.intValue(); // the selected index
                poAppController.getModel().setPayment(String.valueOf(lnIndex));
            }
        });
        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
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
            } else if (loControl instanceof CheckBox) {
                ((CheckBox) loControl).setSelected(false);
            }
        }

        //reset image objects
        imageinfo_temp.clear();
        imgPreview.setImage(null);

        initButtonDisplay(poAppController.getEditMode());
        
        cmbRegistration.setItems(APPaymentConstants.regstrList);
        cmbPayment.setItems(APPaymentConstants.paymentList);
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnRetrieve", "btnUpdate");

        apRecord.setDisable(!lbShow);

        //initialize file buttons
        btnAttach.setDisable(!lbShow);
        btnRemoveFile.setDisable(!lbShow);
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

    private void initializeTableLedger() {
        if (laLedger == null) {
            laLedger = FXCollections.observableArrayList();

            tblLedger.setItems(laLedger);

            tblColAmountIn.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColAmountOut.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblLedgerNo.setStyle("-fx-alignment: CENTER; -fx-padding: 0 5 0 0;");

            tblColNo.setCellValueFactory((loModel) -> {
                int index = tblLedger.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDate.setCellValueFactory((loModel) -> {

                return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
            });

            tblColSourceNo.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getSourceNo());
            });
            
            tblLedgerNo.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().getLedgerNo());
            });

            tblColSourceCode.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().TransactionSource().getSourceName());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(DeliverySchedule_EntryController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    poLogWrapper.severe(psFormName + " :" + ex.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColAmountIn.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getAmountOt(), "###,##0.0000"));
            });

            tblColAmountOut.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getAmountIn(), "###,##0.0000"));
            });
            }
        }
    
    private void initAttachmentsGrid() {

        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblColAttachIndex);
        JFXUtil.setColumnLeft(tblColAttachFile);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);

        tblAttachments.setItems(laAttachments);
    }

    private void reloadTableLedger() {
        List<Model_AP_Client_Ledger> rawDetail = poAppController.getLedgerList();
        laLedger.setAll(rawDetail);

        //Restore or select last row
        int indexToSelect = (pnLedger >= 1 && pnLedger < laLedger.size())
                ? pnLedger - 1
                : laLedger.size() - 1;

        tblLedger.getSelectionModel().select(indexToSelect);

        pnLedger = tblLedger.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblLedger.refresh();
    }

    private void loadLedgerList() {

        StackPane overlay = getOverlayProgress(apLedger);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_AP_Client_Ledger>> loadLedger = new Task<ObservableList<Model_AP_Client_Ledger>>() {
            @Override
            protected ObservableList<Model_AP_Client_Ledger> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadLedgerList(),
                        "Initialize : Load of Ledger List")) {
                    return null;
                }

                List<Model_AP_Client_Ledger> rawList = poAppController.getLedgerList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_AP_Client_Ledger> laListLoader = getValue();
                tblLedger.setItems(laListLoader);

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadLedger);
        thread.setDaemon(true);
        thread.start();
    }

    public void initTableAttachments() {

        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                laAttachments,
                () -> {

                    JFXUtil.resetImageBounds(imgPreview, stackpane);
                    Platform.runLater(() -> {

                        try {

                            int lnCtr;
                            int lnCount = 0;

                            //set image to full scale
                            imageviewerutil.scaleFactor = 1.0;

                            //re initialize attachment list
                            laAttachments.clear();
                            for (lnCtr = 0; lnCtr < poAppController.getTransactionAttachmentCount(); lnCtr++) {

                                //skip current iteration, if status is inactive
                                if (RecordStatus.INACTIVE.equals(poAppController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;

                                //add to attachment list
                                laAttachments.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poAppController.getAttachmentList().get(lnCtr).getModel().getFileName()),
                                                String.valueOf(lnCtr)
                                        ));
                            }

                            //reset attachment records if empty
                            if (laAttachments.size() <= 0) {
                                loadRecordAttachment(false);
                            }

                            int lnTempRow = JFXUtil.getDetailRow(laAttachments, pnAttachments, 3); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow >= laAttachments.size()) {

                                if (!laAttachments.isEmpty()) {

                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    int lnRow = Integer.parseInt(laAttachments.get(0).getIndex03());

                                    pnAttachments = lnRow;

                                    loadRecordAttachment(true);
                                }
                            } else {

                                JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
                                int lnRow = Integer.parseInt(laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());

                                pnAttachments = lnRow;

                                loadRecordAttachment(true);
                            }

                            if (laAttachments.size() <= 0) {
                                loadRecordAttachment(false);
                            }

                        } catch (Exception e) {
                            poLogWrapper.severe(e.getMessage());
                        }
                    });
                }
        );
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackpane, imgPreview);
        stackpane.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment.reload();
            loadRecordAttachment(true);
        });
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (laAttachments.size() > 0) {

                txtAttachmentNo.setText(laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());

                String lsAttachmentType = poAppController.getAttachmentList().get(pnAttachments).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poAppController.getAttachmentList().get(pnAttachments).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poAppController.getAttachmentList().get(pnAttachments).getModel().getDocumentType();
                }

                if (lbloadImage) {
                    try {
                        String filePath = (String) laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02());
                        } else {

                            if (poAppController.getAttachmentList().get(pnAttachments).getModel().getImagePath() != null && !"".equals(poAppController.getAttachmentList().get(pnAttachments).getModel().getImagePath())) {
                                filePath2 = poAppController.getAttachmentList().get(pnAttachments).getModel().getImagePath() + "/" + (String) laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            }
                        }

                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            // Clear previous content
                            stackpane.getChildren().clear();
                            if (!isPdf) {

                                // ----- IMAGE VIEW -----
                                Image loimage = new Image(convertedPath);
                                imgPreview.setImage(loimage);

                                //no need to auto adjust the size, image can be scaled manually, besides, this method affects the margins and sizes of forms when maxed value
                                //JFXUtil.adjustImageSize(loimage, imgPreview, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackpane);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackpane.getChildren().add(imgPreview);
                                stackpane.getChildren().addAll(btnPrevious, btnNext);

                                // Align buttons on top
                                StackPane.setAlignment(btnPrevious, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnNext, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnPrevious, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnNext, new Insets(0, 10, 0, 0));
                            } else {
                                // ----- PDF VIEW -----
                                JFXUtil.PDFViewConfig(filePath2, stackpane, btnPrevious, btnNext, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                            }
                        } else {
                            imgPreview.setImage(null);
                        }
                    } catch (Exception e) {
                        imgPreview.setImage(null);
                    }
                }
            } else {

                if (!lbloadImage) {

                    imgPreview.setImage(null);

                    // Clear previous content
                    stackpane.getChildren().clear();

                    // Add ImageView directly to stackPane
                    stackpane.getChildren().add(imgPreview);
                    stackpane.getChildren().addAll(btnPrevious, btnNext);

                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackpane));
                    pnAttachments = 0;
                }
            }
        } catch (Exception e) {
        }
    }

    public void reloadTableAttachments() {

        //reset image objects
        txtAttachmentNo.clear();
        imageinfo_temp.clear();
        imgPreview.setImage(null);

        //reload table attachment
        loadTableAttachment.reload();

    }

    public void slideImage(int direction) {
        if (laAttachments.size() <= 0) {
            return;
        }
        int lnRow = Integer.valueOf(laAttachments.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
        currentIndex = lnRow - 1;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= laAttachments.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imgPreview);
            slideOut.setByX(direction * -400); // Move left or right

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
            int lnIndex = Integer.valueOf(laAttachments.get(newIndex).getIndex01());
            int lnTempRow = JFXUtil.getDetailTempRow(laAttachments, lnIndex, 3);
            pnAttachments = lnTempRow;
            loadRecordAttachment(false);

            // Create a transition animation
            slideOut.setOnFinished(event -> {
                imgPreview.setTranslateX(direction * 400);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imgPreview);
                slideIn.setToX(0);
                slideIn.play();

                loadRecordAttachment(true);
            });

            slideOut.play();
        }
        if (JFXUtil.isImageViewOutOfBounds(imgPreview, stackpane)) {
            JFXUtil.resetImageBounds(imgPreview, stackpane);
        }
    }

    private void getLoadedClient() throws SQLException, GuanzonException, CloneNotSupportedException {
        loadClientMaster();
        reloadTableLedger();
        reloadTableAttachments();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, message));
                }
            }
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
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
}
