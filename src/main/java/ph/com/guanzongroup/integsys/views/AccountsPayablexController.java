package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowMessageFX;
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
import ph.com.guanzongroup.cas.sales.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.model.ModelAccountsPayable;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Guiller & Team 1
 */
public class AccountsPayablexController implements Initializable, ScreenInterface {

    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private Control lastFocusedControl;
    private AP_Client_Master poController;
    public int pnEditMode;
    private String psFormName = "Account Payable";
    private int pnMain, pnAttachment, currentIndex;
    JSONObject poJSON = new JSONObject();
    private ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    private ObservableList<ModelAccountsPayable> laAccountsPayable = FXCollections.observableArrayList();

    private HashMap<String, String> imageinfo_temp = new HashMap<>();

    private unloadForm poUnload = new unloadForm();

    private JFXUtil.ReloadableTableTask loadTableAttachment, loadTableMain;
    private FilteredList<ModelAccountsPayable> filteredData;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    @FXML
    private AnchorPane apMainAnchor, apRecord, apLedger, apAttachments, apAttachmentButtons;
    @FXML
    private Button btnBrowse, btnSearch, btnSave, btnUpdate, btnCancel, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TextField tfSearchCompanyName, tfSearchClient, tfClientID, tfCategory, tfCompanyName, tfAddress, tfTINNo, tfContactPerson, tfContactEmail, tfContactNo, tfCreditLimit, tfBegBalanace, tfDiscount, tfTerm, tfAvailBalance, tfOutStandingBalance, tfAttachmentNo;
    @FXML
    private Label lblSource, lblStatus, lblStatus12, lblStatus121;
    @FXML
    private ComboBox cmbRegistration, cmbPayment;
    @FXML
    private CheckBox cbHasPermit, cbBackOrder, cbVatable, cbHoldOrder;
    @FXML
    private DatePicker dpClientSince, dpBegBalance;
    @FXML
    private Pagination pgPagination;
    @FXML
    private TableView tblMain, tblAttachments;
    @FXML
    private TableColumn tblColNo, tblLedgerNo, tblColDate, tblColSourceNo, tblColSourceCode, tblColAmountIn, tblColAmountOut, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Tab tabAttachments;
    @FXML
    private StackPane stackpane;
    @FXML
    private ImageView imageView;

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
            poController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

            //initlalize and validate record objects from class controller
            //background thread
            pgPagination.setPageCount(1);
            Platform.runLater(() -> {
                poController.setRecordStatus("10");
            });
            initComboboxes();
            initControlEvents();
            initMainGrid();
            initAttachmentsGrid();
            initTableAttachments();
            initAttachmentPreviewPane();
            initTableOnClick();
            JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);

            pnEditMode = EditMode.UNKNOWN;
            initButtonDisplay(pnEditMode);
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(psFormName, lastFocusedTextField, previousSearchedTextField, apMainAnchor, apRecord);
                    break;
                case "btnBrowse":
                    poJSON = poController.searchRecord(tfSearchClient.getText(), true);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                        return;
                    }
                    poController.loadAttachments();
                    getLoadedClient();
                    initButtonDisplay(poController.getEditMode());
                    return;
                case "btnUpdate":
                    if (poController.getModel().getClientId() == null || poController.getModel().getClientId().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    poController.openRecord(poController.getModel().getClientId());
                    if (!isJSONSuccess(poController.updateRecord(), "Initialize Update Record")) {
                        return;
                    }
                    poController.loadAttachments();

                    getLoadedClient();
                    initButtonDisplay(poController.getEditMode());
                    break;

                case "btnSave":
                    if (tfClientID.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load record before proceeding..", psFormName, "");
                        return;
                    }
                    if (!ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save record?")) {
                        return;
                    }
                    if (!isJSONSuccess(poController.saveRecord(), "Initialize Save Record")) {
                        return;
                    }

                    poController.openRecord(poController.getModel().getClientId());
                    if (!isJSONSuccess(poController.updateRecord(), "Initialize Update Record")) {
                        break;
                    }
                    pnEditMode = poController.getEditMode();
                    clearAllInputs();
                    poController.loadAttachments();
                    getLoadedClient();
                    initButtonDisplay(poController.getEditMode());
                    break;
                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poController = new ClientControllers(poApp, poLogWrapper).APClientMaster();

                        Platform.runLater(() -> {
                            poController.setRecordStatus("01");
                            //poController.setRecordStatus("07");

                            clearAllInputs();
                        });
                        break;
                    }
                    break;

                case "btnRetrieve":
                    loadTableMain.reload();
                    break;
                case "btnAddAttachment":
                    FileChooser fileChooser = new FileChooser();
                    fileChooser = new FileChooser();
                    fileChooser.setTitle("Choose Image");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.pdf")
                    );
                    java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAddAttachment.getScene().getWindow());

                    if (selectedFile != null) {
                        // Read image from the selected file
                        Path imgPath = selectedFile.toPath();
                        Image loimage = new Image(Files.newInputStream(imgPath));
                        imageView.setImage(loimage);

                        //Validate attachment
                        String imgPath2 = selectedFile.getName().toString();
                        for (int lnCtr = 0; lnCtr <= poController.getTransactionAttachmentCount() - 1; lnCtr++) {
                            if (imgPath2.equals(poController.getAttachmentList().get(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {
                                ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                                pnAttachment = lnCtr;
                                loadRecordAttachment(true);
                                return;
                            }
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

                        pnAttachment = poController.addAttachment(imgPath2);
                        //Copy file to Attachment path
                        poController.copyFile(selectedFile.toString());
                        loadTableAttachment.reload();
                        tblAttachments.getFocusModel().focus(pnAttachment);
                        tblAttachments.getSelectionModel().select(pnAttachment);
                    }
                    break;
                case "btnRemoveAttachment":
                    if (poController.getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachment == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }

                    JSONObject loJSON = poController.removeAttachment(pnAttachment);
                    if ("error".equals((String) loJSON.get("result"))) {
                        ShowMessageFX.Warning(null, psFormName, (String) loJSON.get("message"));
                        return;
                    }
                    attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachment != 0) {
                        pnAttachment -= 1;
                    }
                    imageinfo_temp.clear();
                    loadRecordAttachment(false);

                    loadTableAttachment.reload();
                    if (attachment_data.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnArrowLeft":
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
                initButtonDisplay(pnEditMode);
                return;
            }
            initButtonDisplay(poController.getEditMode());

        } catch (Exception e) {
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
            ShowMessageFX.Error(null, psFormName, MiscUtil.getException(e));
        }
    }
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfCreditLimit":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (JFXUtil.isObjectEqualTo(poController.getModel().getClientId(), null, "")) {
                            if (Double.parseDouble(lsValue) > 0.0) {
                                tfCreditLimit.setText("0.00");
                                tfCreditLimit.requestFocus();
                                ShowMessageFX.Information("Unable to set Credit Limit! No Client Detected", psFormName, null);
                            }
                            return;
                        }
                        double lnCreditAmount;
                        try {
                            lnCreditAmount = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnCreditAmount = 0.0; // default if parsing fails
                            poJSON = poController.getModel().setCreditLimit(lnCreditAmount);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadRecordMaster();
                            tfCreditLimit.requestFocus();
                        }
                        if (lnCreditAmount < 0.00) {
                            return;
                        }

                        poJSON = poController.getModel().setCreditLimit(lnCreditAmount);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                    case "tfDiscount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (JFXUtil.isObjectEqualTo(poController.getModel().getClientId(), null, "")) {
                            if (Double.parseDouble(lsValue) > 0.0) {
                                tfDiscount.setText("0.00");
                                tfCreditLimit.requestFocus();
                                ShowMessageFX.Information("Unable to set Discount! No Client Detected", psFormName, null);
                            }
                            return;
                        }
                        double lnDiscount;
                        try {
                            lnDiscount = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnDiscount = 0.0; // default if parsing fails
                            poJSON = poController.getModel().setDiscount(lnDiscount);
                            if (!JFXUtil.isJSONSuccess(poJSON)) {
                                ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                            }
                            loadRecordMaster();
                            tfCreditLimit.requestFocus();
                        }
                        if (lnDiscount < 0.00) {
                            return;
                        }
                        poJSON = poController.getModel().setDiscount(lnDiscount);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });

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
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchClient":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by ID", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                poJSON = poController.searchRecord(tfSearchClient.getText(), true);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                    return;
                                }
                                loadRecordSearch();
                                clearAllInputs();
                                poController.loadAttachments();
                                getLoadedClient();
                                initButtonDisplay(poController.getEditMode());
                                break;
                            case "tfSearchCompanyName":
                                if (!tfClientID.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Client! by Name", "Are you sure you want replace loaded Record?") == false) {
                                        return;
                                    }
                                }
                                poJSON = poController.searchRecord(tfSearchCompanyName.getText(), true);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                    return;
                                }
                                loadRecordSearch();
                                clearAllInputs();
                                poController.loadAttachments();
                                getLoadedClient();
                                initButtonDisplay(poController.getEditMode());
                                break;
                            case "tfTerm":
                                poJSON = poController.searchTerm(tfTerm.getText() == null ? "" : tfTerm.getText(), false);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                    return;
                                }
                                loadRecordMaster();
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
                    poJSON = poController.getModel().setdateClientSince(ldDateValue);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMaster();
                    return;

            }
        }
    };

    private void initComboboxes() {
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbRegistration, cmbPayment);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbRegistration, cmbPayment);
        cmbRegistration.setItems(APPaymentConstants.regstrList);
        cmbPayment.setItems(APPaymentConstants.paymentList);
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbRegistration":
                        poJSON = poController.getModel().setVatRegstr(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                    case "cmbPayment":
                        poJSON = poController.getModel().setPayment(String.valueOf(selectedIndex));
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                        }
                        break;
                }
                loadRecordMaster();
            });

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbHasPermit":
                    poJSON = poController.getModel().hasPermit(cbHasPermit.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
                case "cbBackOrder":
                    poJSON = poController.getModel().isBackOrder(cbBackOrder.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
                case "cbVatable":
                    poJSON = poController.getModel().setVatable(cbVatable.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
                case "cbHoldOrder":
                    poJSON = poController.getModel().isHoldOrder(cbHoldOrder.isSelected() == true ? "1" : "0");
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                    }
                    break;
            }
            loadRecordMaster();
        }
    }

    private void initControlEvents() {
        JFXUtil.setFocusListener(txtField_Focus, apRecord);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMainAnchor, apRecord);
        dpClientSince.focusedProperty().addListener(dPicker_Focus);
        dpBegBalance.focusedProperty().addListener(dPicker_Focus);
        JFXUtil.setCommaFormatter(tfAvailBalance, tfOutStandingBalance, tfCreditLimit, tfDiscount);
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblColNo, tblLedgerNo, tblColDate, tblColSourceNo);
        JFXUtil.setColumnLeft(tblColSourceCode);
        JFXUtil.setColumnRight(tblColAmountIn, tblColAmountOut);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMain);

        tblMain.setItems(laAccountsPayable);
        filteredData = new FilteredList<>(laAccountsPayable, b -> true);
    }

    private void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);

        tblAttachments.setItems(attachment_data);
    }

    public void initTableAttachments() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblMain,
                laAccountsPayable,
                () -> {
                    JFXUtil.resetImageBounds(imageView, stackpane);
                    Platform.runLater(() -> {
                        try {
                            List<Model_AP_Client_Ledger> rawList = poController.getLedgerList();
                            int lnCtr;
                            int lnCount = 0;

                            laAccountsPayable.clear();
                            for (lnCtr = 0; lnCtr < 52; lnCtr++) {
                                lnCount += 1;
                                //add to attachment list
                                laAccountsPayable.add(
                                        new ModelAccountsPayable(String.valueOf(lnCount),
                                                String.valueOf(rawList.get(lnCtr).getLedgerNo()),
                                                String.valueOf(rawList.get(lnCtr).getTransactionDate()),
                                                String.valueOf(rawList.get(lnCtr).getSourceNo()),
                                                String.valueOf(rawList.get(lnCtr).TransactionSource().getSourceName()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(rawList.get(lnCtr).getAmountIn(), false)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(rawList.get(lnCtr).getAmountOt(), false))
                                        ));
                            }

                            if (pnMain < 0 || pnMain >= laAccountsPayable.size()) {
                                if (!laAccountsPayable.isEmpty()) {
                                    JFXUtil.selectAndFocusRow(tblMain, 0);
                                    int lnRow = 0;
                                    pnMain = lnRow;

                                }
                            } else {
                                JFXUtil.selectAndFocusRow(tblMain, pnMain);
                                int lnRow = pnMain;
                                pnMain = lnRow;
                            }
                            if (laAccountsPayable.size() <= 0) {
                                loadRecordMaster();
                            }
                            Platform.runLater(() -> {
                                JFXUtil.loadTab(pgPagination, laAccountsPayable.size(), 50, tblMain, filteredData);
                            });
                        } catch (Exception e) {
                            poLogWrapper.severe(e.getMessage());
                        }
                    });
                }
        );
        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                attachment_data,
                () -> {
                    JFXUtil.resetImageBounds(imageView, stackpane);
                    Platform.runLater(() -> {

                        try {

                            int lnCtr;
                            int lnCount = 0;

                            //set image to full scale
                            imageviewerutil.scaleFactor = 1.0;

                            //re initialize attachment list
                            attachment_data.clear();
                            for (lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {

                                //skip current iteration, if status is inactive
                                if (RecordStatus.INACTIVE.equals(poController.getAttachmentList().get(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;

                                //add to attachment list
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poController.getAttachmentList().get(lnCtr).getModel().getFileName()),
                                                String.valueOf(lnCtr)
                                        ));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow >= attachment_data.size()) {
                                if (!attachment_data.isEmpty()) {
                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
                                    pnAttachment = lnRow;
                                    loadRecordAttachment(true);
                                }
                            } else {
                                JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
                                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                                pnAttachment = lnRow;
                                loadRecordAttachment(true);
                            }

                            if (attachment_data.size() <= 0) {
                                loadRecordAttachment(false);
                            }
                        } catch (Exception e) {
                            poLogWrapper.severe(e.getMessage());
                        }
                    });
                }
        );
    }

    private void loadTableDetailFromMain() {
        ModelAccountsPayable selected = (ModelAccountsPayable) tblMain.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                poJSON = poController.openRecord(selected.getIndex02());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, psFormName, (String) poJSON.get("message"));
                    return;
                }
                poController.loadAttachments();
                getLoadedClient();
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void getLoadedClient() throws SQLException, GuanzonException, CloneNotSupportedException {
        loadRecordMaster();
        reloadTableAttachments();
    }

    private void loadRecordSearch() {
        try {
            tfSearchClient.setText(poController.getModel().Client().getCompanyName());
//            tfSearchCompany.setText(poController.getModel().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMaster() {
        try {
            lblStatus.setText(poController.getModel().getRecordStatus().equals("0") == false ? "ACTIVE" : "INACTIVE");

            tfClientID.setText(poController.getModel().getClientId());

            tfCategory.setText(poController.getModel().Category().getDescription());
            tfCompanyName.setText(poController.getModel().Client().getCompanyName());
            tfAddress.setText(poController.getModel().ClientAddress().getAddress());
            tfContactPerson.setText(poController.getModel().ClientInstitutionContact().getContactPersonName());
            tfContactEmail.setText(poController.getModel().ClientInstitutionContact().getMailAddress());
            tfContactNo.setText(poController.getModel().ClientInstitutionContact().getMobileNo());
            tfTINNo.setText(poController.getModel().Client().getTaxIdNumber());

            cbHasPermit.setSelected(poController.getModel().hasPermit() == null || !poController.getModel().hasPermit().equalsIgnoreCase("1") ? false : true);
            cbBackOrder.setSelected(poController.getModel().isBackOrder() == null || !poController.getModel().isBackOrder().equalsIgnoreCase("1") ? false : true);
            cbVatable.setSelected(poController.getModel().getVatable() == null || !poController.getModel().getVatable().equalsIgnoreCase("1") ? false : true);
            cbHoldOrder.setSelected(poController.getModel().isHoldOrder() == null || !poController.getModel().isHoldOrder().equalsIgnoreCase("1") ? false : true);

            //payment method
            if (poController.getModel().getPayment() == null) {
            } else {
                cmbPayment.getSelectionModel().select(Integer.parseInt(poController.getModel().getPayment()));
            }

            dpClientSince.setValue(poController.getModel().getdateClientSince() == null ? null : ParseDate(poController.getModel().getdateClientSince()));
            dpBegBalance.setValue(poController.getModel().getBeginningDate() == null ? null : ParseDate(poController.getModel().getBeginningDate()));
            tfDiscount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getDiscount(), false));
            tfTerm.setText(poController.getModel().Term().getDescription());
            tfCreditLimit.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getCreditLimit(), false));
            tfBegBalanace.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getBeginningBalance(), false));
            tfAvailBalance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getAccountBalance(), false));
            tfOutStandingBalance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getOBalance(), false));

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {

                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());

                String lsAttachmentType = poController.getAttachmentList().get(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.getAttachmentList().get(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.getAttachmentList().get(pnAttachment).getModel().getDocumentType();
                }

                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02());
                        } else {

                            if (poController.getAttachmentList().get(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.getAttachmentList().get(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poController.getAttachmentList().get(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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
                                imageView.setImage(loimage);

                                //no need to auto adjust the size, image can be scaled manually, besides, this method affects the margins and sizes of forms when maxed value
                                //JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackpane);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackpane.getChildren().add(imageView);
                                stackpane.getChildren().addAll(btnArrowLeft, btnArrowRight);

                                // Align buttons on top
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));
                            } else {
                                // ----- PDF VIEW -----
                                JFXUtil.PDFViewConfig(filePath2, stackpane, btnArrowLeft, btnArrowRight, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                            }
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }
            } else {

                if (!lbloadImage) {

                    imageView.setImage(null);

                    // Clear previous content
                    stackpane.getChildren().clear();

                    // Add ImageView directly to stackPane
                    stackpane.getChildren().add(imageView);
                    stackpane.getChildren().addAll(btnArrowLeft, btnArrowRight);

                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackpane));
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
        }
    }

    public void reloadTableAttachments() {
        //reset image objects
        tfAttachmentNo.clear();
        imageinfo_temp.clear();
        imageView.setImage(null);

        //reload table attachment
        loadTableAttachment.reload();

    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackpane, imageView);
        stackpane.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment.reload();
            loadRecordAttachment(true);
        });
    }

    public void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }
        int lnRow = Integer.valueOf(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
        currentIndex = lnRow - 1;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
            int lnIndex = Integer.valueOf(attachment_data.get(newIndex).getIndex01());
            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data, lnIndex, 3);
            pnAttachment = lnTempRow;
            loadRecordAttachment(false);

            // Create a transition animation
            slideOut.setOnFinished(event -> {
                imageView.setTranslateX(direction * 400);
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), imageView);
                slideIn.setToX(0);
                slideIn.play();

                loadRecordAttachment(true);
            });

            slideOut.play();
        }
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackpane)) {
            JFXUtil.resetImageBounds(imageView, stackpane);
        }
    }
    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblAttachments":
                    if (!attachment_data.isEmpty()) {
                        pnAttachment = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                                : Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03());
                        loadRecordAttachment(true);

                    }
                    break;
            }
        }
    };

    public void initTableOnClick() {
        tblMain.setOnMouseClicked(event -> {
            pnMain = tblMain.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poController.getEditMode();
                    initButtonDisplay(pnEditMode);
                }
            }
        });
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                imageviewerutil.scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackpane);
            }
        });

        JFXUtil.setKeyEventFilter(tableKeyEvents, tblAttachments);
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

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
            poLogWrapper.info("Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning("Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(!lbShow, btnBrowse, btnRetrieve);
        JFXUtil.setButtonsVisibility(false, btnUpdate);
        apRecord.setDisable(!lbShow);

        //initialize file buttons
        btnAddAttachment.setDisable(!lbShow);
        btnRemoveAttachment.setDisable(!lbShow);

        if (fnEditMode != EditMode.READY) {
            return;
        }
        switch (poController.getModel().getRecordStatus()) {
            case "0":
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
            case "1":
                JFXUtil.setButtonsVisibility(true, btnUpdate);
                break;
        }
    }

    private void clearAllInputs() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apRecord, apLedger, apAttachments);
    }
}
