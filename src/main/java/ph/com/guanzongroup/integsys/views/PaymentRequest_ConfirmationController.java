/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPRFAttachment;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 2 & Team 1
 */
public class PaymentRequest_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Payment Request Confirmation";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";

    private String psTransactionNo = "";
    private String psPayeeID = "";

    private int pnTblMainRow = -1;
    private int pnTblDetailRow = -1;
    private int pnTblMain_Page = 50;
    private TextField activeField;

    //attachments
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double scaleFactor = 1.0;
    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;

    private ObservableList<ModelTableMain> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private ObservableList<ModelPRFAttachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelPRFAttachment.documentType;
    Map<String, String> imageinfo_temp = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSourceTranTotal, tfAdvances, tfSearchTransaction, tfSearchPayee, tfTransactionNo, tfBranch, tfDepartment, tfPayee, tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfNetAmount, tfSourceNo, tfRecurringNo, tfBranchDetail, tfAccountNo, tfEmployee, tfParticular, tfAmount, tfDiscRate, tfDiscAmountDetail, tfAmountDetail, tfAttachmentNo;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnReturn, btnVoid, btnRetrieve, btnHistory, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane ImTabPane;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private DatePicker dpTransaction;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblVwPRDetail, tblAttachments, tblVwPaymentRequest;
    @FXML
    private TableColumn tblRowNoDetail, tblParticular, tblAmount, tblDiscAmount, tbTotalAmount, tblRowNoAttachment, tblFileNameAttachment, tblRowNo, tblBranch, tblPayee, tblTransactionNo;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
    @FXML
    private Pagination pagination;

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
        try {
            poGLControllers = new CashflowControllers(poApp, logWrapper);
            poGLControllers.PaymentRequest().setTransactionStatus(PaymentRequestStatus.OPEN
                    + PaymentRequestStatus.CONFIRMED);
            poGLControllers.PaymentRequest().setWithUI(true);
            poJSON = poGLControllers.PaymentRequest().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwPRDetail, tblAttachments);
            Platform.runLater((() -> {
                try {
                    poGLControllers.PaymentRequest().setIndustryId(psIndustryID);
                    poGLControllers.PaymentRequest().setCompanyId(psCompanyID);
                    poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                    poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                    loadRecordSearch();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }));
            Platform.runLater(() -> setBranchAndDepartment());
            initAll();
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField);
        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setBranchAndDepartment() {
        try {
            poGLControllers.PaymentRequest().Master().setBranchCode(poApp.getBranchCode());
            poGLControllers.PaymentRequest().Master().setDepartmentID(poApp.getDepartment());
            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
            if (poApp.isMainOffice() || poApp.isWarehouse()) {
                String lsDepartment = "";
                if (poGLControllers.PaymentRequest().Master().Department().getDescription() != null) {
                    lsDepartment = poGLControllers.PaymentRequest().Master().Department().getDescription();
                }
                tfDepartment.setText(lsDepartment);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTableOnClick() {
        tblVwPRDetail.setOnMouseClicked(this::tblVwDetail_Clicked);
        tblVwPaymentRequest.setOnMouseClicked(this::tblVwMain_Clicked);
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });
    }

    private void initAll() {
        initTableOnClick();
        initButtonsClickActions();
        initTextFieldFocus();
        initTextAreaFocus();
        initTextFieldKeyPressed();
        initTextFieldsProperty();
        initCheckBoxActions();
        initDatePickerActions();
        initTextFieldPattern();
        initTablePaymentRequest();
        initTableDetail();
        initAttachmentsGrid();
        initAttachmentPreviewPane();
        initStackPaneListener();
        initButtons(pnEditMode);
        initFields(pnEditMode);
        initComboBoxes();
        loadTableAttachment();
    }

    private void loadRecordMaster() {
        try {
            boolean lbShow = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(lbShow, tfBranch, tfDepartment, tfPayee);

            JFXUtil.setStatusValue(lblStatus, PaymentRequestStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poGLControllers.PaymentRequest().Master().getTransactionStatus());

            poGLControllers.PaymentRequest().computeFields();
            tfTransactionNo.setText(poGLControllers.PaymentRequest().Master().getTransactionNo());
            dpTransaction.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poGLControllers.PaymentRequest().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBranch.setText(poGLControllers.PaymentRequest().Master().Branch().getBranchName());
            if (poApp.isMainOffice() || poApp.isWarehouse()) {
                String lsDepartment = "";
                if (poGLControllers.PaymentRequest().Master().Department().getDescription() != null) {
                    lsDepartment = poGLControllers.PaymentRequest().Master().Department().getDescription();
                }
                tfDepartment.setText(lsDepartment);
            }
            String lsPayee = "";
            if (poGLControllers.PaymentRequest().Master().Payee().getPayeeName() != null) {
                lsPayee = poGLControllers.PaymentRequest().Master().Payee().getPayeeName();
            }
            tfPayee.setText(lsPayee);
            tfSeriesNo.setText(poGLControllers.PaymentRequest().Master().getSeriesNo());
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
//            tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getVatAmount(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
            taRemarks.setText(poGLControllers.PaymentRequest().Master().getRemarks());
            tfAdvances.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().PurchaseOrder().getAmountPaid(), true));
            tfSourceNo.setText(poGLControllers.PaymentRequest().Master().getSourceNo());

            tfSourceTranTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().PurchaseOrder().getTranTotal(), true));
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordDetail() {
        try {
            boolean lbShow = !PaymentRequestStatus.OPEN.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus());
            JFXUtil.setDisabled(lbShow, cbReverse);

            boolean lbIsRecurring = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo(), null, "");
            if (lbIsRecurring) {
                JFXUtil.setDisabled(lbIsRecurring, tfParticular);
            } else {
                JFXUtil.setDisabled(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getEditMode() == EditMode.UPDATE, tfParticular);
            }
            if (pnTblDetailRow >= 0) {
                try {
                    tfParticular.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).Particular().getDescription());
                    tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAmount(), true));
                    tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getDiscount())); // rate
                    tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAddDiscount(), true)); // amount
//                    chkbVatable.setSelected(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isVatable());
                    cbReverse.setSelected(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isReverse());

                    tfRecurringNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getRecurringNo());
                    tfBranchDetail.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Branch().getBranchName());
                    tfAccountNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAccountNo());
                    tfEmployee.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Employee().getCompanyName());
//                    tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getVatAmount(), true));
                    tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getNetTotal(), true));
//                tfTaxAmount.setText("0.00");
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVoid, btnReturn,
                btnRetrieve, btnHistory, btnClose, btnConfirm,
                btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight, btnSearch);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        poJSON = new JSONObject();
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnUpdate":
                    poJSON = poGLControllers.PaymentRequest().UpdateTransaction();
                    pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Warning", null);
                    }
                    poGLControllers.PaymentRequest().loadAttachments();
                    clearDetailFields();
                    pnTblDetailRow = -1;
                    pagination.toFront();
                    break;
                case "btnSearch":
                    JFXUtil.initiateBtnSearch(psFormName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                    break;
                case "btnSave":
                    if (!ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to save?")) {
                        return;
                    }
                    pnTblDetailRow = -1;
                    if (pnEditMode == EditMode.UPDATE) {
                        poGLControllers.PaymentRequest().Master().setModifiedDate(poApp.getServerDate());
                        poGLControllers.PaymentRequest().Master().setModifyingId(poApp.getUserID());
                        for (int i = 0; i < poGLControllers.PaymentRequest().getDetailCount(); i++) {
                            poGLControllers.PaymentRequest().Detail(i).setModifiedDate(poApp.getServerDate());
                        }
                    }
                    poJSON = poGLControllers.PaymentRequest().isDetailHasZeroAmount();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        if ("true".equals((String) poJSON.get("warning"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnTblDetailRow = (int) poJSON.get("tableRow");
                            loadTableDetail();
                            initDetailFocus();
                            return;
                        } else {
                            if (!ShowMessageFX.YesNo((String) poJSON.get("message"), psFormName, null)) {
                                pnTblDetailRow = (int) poJSON.get("tableRow");
                                loadTableDetail();
                                initDetailFocus();
                                return;
                            }
                        }
                    }
                    poJSON = poGLControllers.PaymentRequest().SaveTransaction();
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                        loadTableDetail();
                        return;
                    }
                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);

                    poJSON = poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                    if ("success".equals(poJSON.get("result"))) {
                        if (poGLControllers.PaymentRequest().Master().getTransactionStatus().equals(PaymentRequestStatus.OPEN)) {
                            if (ShowMessageFX.YesNo(null, psFormName, "Do you want to confirm this transaction?")) {
                                if ("success".equals((poJSON = poGLControllers.PaymentRequest().ConfirmTransaction("Confirmed")).get("result"))) {
                                    ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                                    poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                                    tblVwPaymentRequest.refresh();
                                    main_data.get(pnTblMainRow).setIndex05(PaymentRequestStatus.CONFIRMED);
                                    loadTableDetail();
                                } else {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                            }
                        }
                    }
                    loadTableDetail();
                    pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                    pagination.toBack();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to cancel?")) {
                        clearMasterFields();
                        clearDetailFields();
                        detail_data.clear();
                        attachment_data.clear();
                        poJSON = poGLControllers.PaymentRequest().OpenTransaction(poGLControllers.PaymentRequest().Master().getTransactionNo());
                        if ("success".equals((String) poJSON.get("result"))) {
                            pagination.toBack();
                            CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                            pnTblDetailRow = -1;
                            loadRecordMaster();
                            clearDetailFields();
                            pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                            loadTableDetail();
                            pagination.toBack();
                        }
                        poGLControllers.PaymentRequest().loadAttachments();
                    }
                    break;
                case "btnConfirm":
                    poJSON = new JSONObject();
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to confirm transaction?")) {
                        poJSON = poGLControllers.PaymentRequest().ConfirmTransaction("Confirmed");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        poGLControllers.PaymentRequest().resetMaster();
                        poGLControllers.PaymentRequest().resetOthers();
                        poGLControllers.PaymentRequest().Detail().clear();
                        clearMasterFields();
                        clearDetailFields();
                        CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                        detail_data.clear();
                        attachment_data.clear();
                        pnEditMode = EditMode.UNKNOWN;
                        pnTblDetailRow = -1;
                        //this code below use to highlight tblpurchase
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PaymentRequestStatus.CONFIRMED);
                        pagination.toBack();
                    } else {
                        return;
                    }
                    break;
                case "btnRetrieve":
                    loadTableMain();
                    break;
                case "btnHistory":
                    poGLControllers.PaymentRequest().ShowStatusHistory();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
                case "btnAddAttachment":
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
                        for (int lnCtr = 0; lnCtr <= poGLControllers.PaymentRequest().getTransactionAttachmentCount() - 1; lnCtr++) {
                            if (imgPath2.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                    && RecordStatus.ACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                ShowMessageFX.Warning(null, psFormName, "File name already exists.");
                                pnAttachment = lnCtr;
                                loadRecordAttachment(true);
                                return;
                            }
                        }
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

//                            int lnTempRow = JFXUtil.getDetailTempRow(attachment_data,  poGLControllers.PaymentRequest().addAttachment(imgPath2), 3);
//                            pnAttachment = lnTempRow;
                        pnAttachment = poGLControllers.PaymentRequest().addAttachment(imgPath2);
                        //Copy file to Attachment path
                        poGLControllers.PaymentRequest().copyFile(selectedFile.toString());
                        loadTableAttachment();
                        tblAttachments.getFocusModel().focus(pnAttachment);
                        tblAttachments.getSelectionModel().select(pnAttachment);
                    }
                    break;
                case "btnRemoveAttachment":
                    if (poGLControllers.PaymentRequest().getTransactionAttachmentCount() <= 0) {
                        return;
                    } else {
                        for (int lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                if (pnAttachment == lnCtr) {
                                    return;
                                }
                            }
                        }
                    }
                    poJSON = poGLControllers.PaymentRequest().removeAttachment(pnAttachment);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, psFormName, (String) poJSON.get("message"));
                        return;
                    }
                    attachment_data.remove(tblAttachments.getSelectionModel().getSelectedIndex());
                    if (pnAttachment != 0) {
                        pnAttachment -= 1;
                    }
                    imageinfo_temp.clear();
                    loadRecordAttachment(false);
                    loadTableAttachment();
                    if (attachment_data.size() <= 0) {
                        JFXUtil.clearTextFields(apAttachments);
                    }
                    initAttachmentsGrid();
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnReturn":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to return transaction?")) {
                        poJSON = poGLControllers.PaymentRequest().ReturnTransaction("Returned");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PaymentRequestStatus.RETURNED);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to void transaction?")) {
                        if (PaymentRequestStatus.CONFIRMED.equals(poGLControllers.PaymentRequest().Master().getTransactionStatus())) {
                            poJSON = poGLControllers.PaymentRequest().CancelTransaction("");
                        } else {
                            poJSON = poGLControllers.PaymentRequest().VoidTransaction("");
                        }
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PaymentRequestStatus.VOID);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnVoid", "btnReturn")) {
                poGLControllers.PaymentRequest().resetMaster();
                poGLControllers.PaymentRequest().resetOthers();
                poGLControllers.PaymentRequest().Detail().clear();
                poGLControllers.PaymentRequest().setIndustryId(psIndustryID);
                poGLControllers.PaymentRequest().setCompanyId(psCompanyID);
                poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                clearMasterFields();
                clearDetailFields();
                CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                pnEditMode = EditMode.UNKNOWN;
            }
            if (lsButton.equals("btnRetrieve") || lsButton.equals("btnAddAttachment") || lsButton.equals("btnRemoveAttachment")
                    || lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve") || lsButton.equals("btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail();
                loadTableAttachment();
            }
            initButtons(pnEditMode);
            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            if (poGLControllers.PaymentRequest().getEditMode() == EditMode.READY || poGLControllers.PaymentRequest().getEditMode() == EditMode.UPDATE) {
                poGLControllers.PaymentRequest().loadAttachments();
                Platform.runLater(() -> {
                    loadTableDetail();
                });
                imageView.setImage(null);
                stackPaneClip();
                Platform.runLater(() -> {
                    loadTableAttachment();
                });
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            boolean lbShow2 = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(!lbShow2, cmbAttachmentType, btnAddAttachment, btnRemoveAttachment);
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);
                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02());
                        } else {
                            // in server
                            if (poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            } else {
                                filePath2 = System.getProperty("sys.default.path.temp.attachments") + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
                            }
                        }

                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            boolean isPdf = filePath.toLowerCase().endsWith(".pdf");

                            // Clear previous content
                            stackPane1.getChildren().clear();
                            if (!isPdf) {
                                // ----- IMAGE VIEW -----
                                Image loimage = new Image(convertedPath);
                                imageView.setImage(loimage);
                                JFXUtil.adjustImageSize(loimage, imageView, ldstackPaneWidth, ldstackPaneHeight);

                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
                                    });
                                });
                                delay.play();

                                // Add ImageView directly to stackPane
                                stackPane1.getChildren().add(imageView);
                                stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);

                                // Align buttons on top
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);

                                // Optional: add some margin
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                            } else {
                                // ----- PDF VIEW -----
                                JFXUtil.PDFViewConfig(filePath2, stackPane1, btnArrowLeft, btnArrowRight, ldstackPaneWidth, ldstackPaneHeight);
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
                    stackPane1.getChildren().clear();
                    // Add ImageView directly to stackPane
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception ex) {
        }
    }

    private void loadTableAttachment() {
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblAttachments.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                scaleFactor = 1.0;
                JFXUtil.resetImageBounds(imageView, stackPane1);
                Platform.runLater(() -> {
                    try {
                        attachment_data.clear();
                        int lnCtr;
                        int lnCount = 0;
                        for (lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                continue;
                            }
                            lnCount += 1;
                            attachment_data.add(
                                    new ModelPRFAttachment(String.valueOf(lnCount),
                                            String.valueOf(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName()),
                                            String.valueOf(lnCtr)
                                    ));
                        }
                        int lnTempRow = JFXUtil.getDetailRow(attachment_data, pnAttachment, 3); //this method is used only when Reverse is applied
                        if (lnTempRow < 0 || lnTempRow
                                >= attachment_data.size()) {
                            if (!attachment_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                int lnRow = Integer.parseInt(attachment_data.get(0).getIndex03());
                                pnAttachment = lnRow;
                                loadRecordAttachment(true);
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblAttachments, lnTempRow);
                            int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                            pnAttachment = lnRow;
                            loadRecordAttachment(true);
                        }
                        if (attachment_data.size() <= 0) {
                            loadRecordAttachment(false);
                        }
                    } catch (Exception e) {
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (attachment_data == null || attachment_data.isEmpty()) {
                    tblAttachments.setPlaceholder(placeholderLabel);
                } else {
                    tblAttachments.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (attachment_data == null || attachment_data.isEmpty()) {
                    tblAttachments.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

    }

    private void initStackPaneListener() {
        stackPane1.widthProperty().addListener((observable, oldValue, newWidth) -> {
            double computedWidth = newWidth.doubleValue();
            ldstackPaneWidth = computedWidth;

        });
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            ldstackPaneHeight = computedHeight;
            loadTableAttachment();
            loadRecordAttachment(true);
            initAttachmentsGrid();
        });
    }

    private void initAttachmentPreviewPane() {
        stackPane1.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            stackPane1.setClip(new javafx.scene.shape.Rectangle(
                    newBounds.getMinX(),
                    newBounds.getMinY(),
                    newBounds.getWidth(),
                    newBounds.getHeight()
            ));
        });
        imageView.setOnScroll((ScrollEvent event) -> {
            double delta = event.getDeltaY();
            scaleFactor = Math.max(0.5, Math.min(scaleFactor * (delta > 0 ? 1.1 : 0.9), 5.0));
            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);
        });

        imageView.setOnMousePressed((MouseEvent event) -> {
            mouseAnchorX = event.getSceneX() - imageView.getTranslateX();
            mouseAnchorY = event.getSceneY() - imageView.getTranslateY();
        });

        imageView.setOnMouseDragged((MouseEvent event) -> {
            double translateX = event.getSceneX() - mouseAnchorX;
            double translateY = event.getSceneY() - mouseAnchorY;
            imageView.setTranslateX(translateX);
            imageView.setTranslateY(translateY);
        });

        stackPane1.widthProperty().addListener((observable, oldValue, newWidth) -> {
            double computedWidth = newWidth.doubleValue();
            ldstackPaneWidth = computedWidth;

        });
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            ldstackPaneHeight = computedHeight;

            //Placed to get height and width of stack pane in computed size before loading the image
            initStackPaneListener();
            initAttachmentsGrid();
        });

    }

    private void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);

        if (pnAttachment < 0 || pnAttachment >= attachment_data.size()) {
            if (!attachment_data.isEmpty()) {
                /* FOCUS ON FIRST ROW */
                tblAttachments.getSelectionModel().select(0);
                tblAttachments.getFocusModel().focus(0);
                pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            }
        } else {
            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
            tblAttachments.getSelectionModel().select(pnAttachment);
            tblAttachments.getFocusModel().focus(pnAttachment);
        }

    }

    private void slideImage(int direction) {
        if (attachment_data.size() <= 0) {
            return;
        }

        currentIndex = pnAttachment;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            ModelPRFAttachment image = attachment_data.get(newIndex);
            String filePath2 = "D:\\GGC_Maven_Systems\\temp\\attachments\\" + image.getIndex02();
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            tblAttachments.getFocusModel().focus(newIndex);
            tblAttachments.getSelectionModel().select(newIndex);
            pnAttachment = newIndex;
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
        if (isImageViewOutOfBounds(imageView, stackPane1)) {
            resetImageBounds();
        }
    }

    private boolean isImageViewOutOfBounds(ImageView imageView, StackPane stackPane) {
        Bounds clipBounds = stackPane.getClip().getBoundsInParent();
        Bounds imageBounds = imageView.getBoundsInParent();

        return imageBounds.getMaxX() < clipBounds.getMinX()
                || imageBounds.getMinX() > clipBounds.getMaxX()
                || imageBounds.getMaxY() < clipBounds.getMinY()
                || imageBounds.getMinY() > clipBounds.getMaxY();
    }

    private void resetImageBounds() {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        stackPane1.setAlignment(imageView, javafx.geometry.Pos.CENTER);
    }

    private void stackPaneClip() {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                stackPane1.getWidth() - 8, // Subtract 10 for padding (5 on each side)
                stackPane1.getHeight() - 8 // Subtract 10 for padding (5 on each side)
        );
        clip.setArcWidth(8); // Optional: Rounded corners for aesthetics
        clip.setArcHeight(8);
        clip.setLayoutX(4); // Set padding offset for X
        clip.setLayoutY(4); // Set padding offset for Y
        stackPane1.setClip(clip);

    }

    private void initTextFieldFocus() {
//        List<TextField> loTxtField = Arrays.asList(tfAmount, tfDiscRate, tfDiscAmountDetail);
//        loTxtField.forEach(tf -> tf.focusedProperty().addListener(txtField_Focus));
        tfSearchPayee.setOnMouseClicked(e -> activeField = tfSearchPayee);
        tfSearchTransaction.setOnMouseClicked(e -> activeField = tfSearchTransaction);
        tfParticular.setOnMouseClicked(e -> activeField = tfParticular);
    }

    private void initTextAreaFocus() {
        taRemarks.focusedProperty().addListener(txtArea_Focus);
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
                        poGLControllers.PaymentRequest().Master().setRemarks(lsValue);
                        break;
                }
            } else {
                loTextArea.selectAll();
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    };

    private void initTextFieldKeyPressed() {
        List<TextField> loTxtField = Arrays.asList(
                tfParticular, tfSearchTransaction, tfSearchPayee,
                tfAmount, tfDiscRate, tfDiscAmountDetail);

        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

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
                            case "tfSearchTransaction":
                                loadTableMain();
                                break;
                            case "tfSearchPayee":
                                poJSON = poGLControllers.PaymentRequest().SearchPayee(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfSearchPayee.setText("");
                                    break;
                                }
                                psPayeeID = poGLControllers.PaymentRequest().Master().getPayeeID();
                                tfSearchPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
                                if (!tfSearchPayee.getText().isEmpty()) {
                                    loadTableMain();
                                }
                                break;
                            case "tfDepartment":
                                poJSON = poGLControllers.PaymentRequest().SearchDepartment(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfDepartment.setText("");
                                    break;
                                }
                                tfDepartment.setText(poGLControllers.PaymentRequest().Master().Department().getDescription());

                                break;
                            case "tfParticular":
                                if (!tfPayee.getText().isEmpty()) {
                                    if (pnTblDetailRow < 0) {
                                        ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                                        clearDetailFields();
                                        break;
                                    }
                                    poJSON = poGLControllers.PaymentRequest().SearchParticular(lsValue, false, pnTblDetailRow);
                                    if ("error".equals(poJSON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                        tfParticular.setText("");
                                        if (poJSON.get("row") != null) {
                                            Object obj = poJSON.get("row");
                                            int value = Integer.valueOf(String.valueOf(obj));
                                            pnTblDetailRow = value;
                                        } else {
                                            break;
                                        }
                                    } else {
                                        if (poJSON.get("row") != null) {
                                            Object obj = poJSON.get("row");
                                            int value = Integer.valueOf(String.valueOf(obj));
                                            pnTblDetailRow = value;
                                        } else {
                                            JFXUtil.textFieldMoveNext(tfAmountDetail);
                                        }
                                    }
                                    loadTableDetail();
                                    initDetailFocus();

                                } else {
                                    ShowMessageFX.Warning("Please enter Payee field first.", psFormName, null);
                                    tfPayee.requestFocus();
                                }
                                break;
                        }
                        switch (txtFieldID) {
                            case "tfPayee":
                            case "tfDepartment":
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                break;
                            case "tfAmount":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(Double.parseDouble(lsValue));
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    CommonUtils.SetNextFocus((TextField) event.getSource());
                                }
                                loadTableDetail();
                                break;
                            case "tfDiscRate":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().setDiscountRate(Double.parseDouble(lsValue), pnTblDetailRow);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    CommonUtils.SetNextFocus((TextField) event.getSource());
                                }
                                loadTableDetail();
                                break;
                            case "tfDiscAmountDetail":
                                lsValue = JFXUtil.removeComma(lsValue);
                                poJSON = poGLControllers.PaymentRequest().setDiscountAmount(Double.parseDouble(lsValue), pnTblDetailRow);
                                if (!JFXUtil.isJSONSuccess(poJSON)) {
                                    ShowMessageFX.Information(null, psFormName, JFXUtil.getJSONMessage(poJSON));
                                } else {
                                    pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblVwPRDetail)).getIndex11());
                                    initDetailFocus();
                                }
                                loadTableDetail();

                                break;
                        }
                        event.consume();
                        break;
                    case UP:
//                        setAmountToDetail(tfAmount.getText());
                        if (JFXUtil.isObjectEqualTo(lsTxtField.getId(), "tfParticular", "tfAmount", "tfDiscRate", "tfDiscAmountDetail")) {
                            pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(tblVwPRDetail)).getIndex11());
                        }
                        loadRecordDetail();
                        initDetailFocus();
                        event.consume();
                        break;
                    case DOWN:
//                        setAmountToDetail(tfAmount.getText());
                        if (JFXUtil.isObjectEqualTo(lsTxtField.getId(), "tfParticular", "tfAmount", "tfDiscRate", "tfDiscAmountDetail")) {
                            pnTblDetailRow = Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblVwPRDetail)).getIndex11());
                        }
                        loadRecordDetail();
                        initDetailFocus();
                        event.consume(); // Consume event after handling focus
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setDiscountRate(String fsValue) {
        try {
            if (fsValue == null || fsValue.isEmpty()) {
                fsValue = "0.00";
            }

            double lnPerDetailTotal = Double.parseDouble(tfAmount.getText().replace(",", ""));
            if (lnPerDetailTotal == 0.00) {
                ShowMessageFX.Warning("You're not allowed to enter discount rate, no amount entered.", psFormName, null);
                fsValue = "0.00";
            }

            double lnDiscountRate = Double.parseDouble(fsValue);
            if (lnDiscountRate < 0.00 || lnDiscountRate > 1.00) {
                ShowMessageFX.Warning("Invalid Discount Rate. Must be between 0.00 and 1.00 (1.00 = 100%)", psFormName, null);
                lnDiscountRate = 0.00;
            }

            double lnDiscountAmount = lnDiscountRate * lnPerDetailTotal;

            // Store rate (e.g., 0.10 = 10%) and amount
            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setDiscount(lnDiscountRate);        // decimal: 1.00 = 100%
            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAddDiscount(lnDiscountAmount);   // amount: 10.00

            tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnDiscountRate));        // show: 0.10
            tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAddDiscount(), true));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setDiscountAmount(String fsValue) {
        try {
            if (fsValue == null || fsValue.isEmpty()) {
                fsValue = "0.00";
            }

            double lnPerDetailTotal = Double.parseDouble(tfAmount.getText().replace(",", ""));

            if (lnPerDetailTotal == 0.00) {
                ShowMessageFX.Warning("You're not allowed to enter discount amount, no amount entered.", psFormName, null);
                fsValue = "0.00";
            }

            double lnDiscountAmount = Double.parseDouble(fsValue.replace(",", ""));
            if (lnDiscountAmount < 0.0 || lnDiscountAmount > lnPerDetailTotal) {
                ShowMessageFX.Warning("Invalid Discount Amount", psFormName, null);
                lnDiscountAmount = 0.00;
            }

            // ✅ Compute discount rate in decimal (e.g., 0.10 for 10%)
            double lnDiscountRate = lnDiscountAmount / lnPerDetailTotal;

            // ✅ Store to model (rate and amount)
            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setDiscount(lnDiscountRate);       // decimal rate: 0.10 = 10%
            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAddDiscount(lnDiscountAmount);  // amount

            // ✅ Display to user
            tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnDiscountRate));        // 0.10
            tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAddDiscount(), true));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void setAmountToDetail(String fsValue) {
//        try {
//            if (fsValue == null || fsValue.isEmpty()) {
//                fsValue = "0.00";
//            }
//
//            double amount = Double.parseDouble(fsValue.replace(",", ""));
//            if (amount < 0.0) {
//                ShowMessageFX.Warning("Invalid Amount", psFormName, null);
//                amount = 0.00;
//            }
//
//            if (tfAmount.isFocused() && tfParticular.getText().isEmpty()) {
//                ShowMessageFX.Warning("Invalid action, Please enter particular first.", psFormName, null);
//                tfParticular.requestFocus();
//                return;
//            }
//
//            if (pnTblDetailRow < 0) {
//                ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
//                clearDetailFields();
//                int detailCount = poGLControllers.PaymentRequest().getDetailCount();
//                pnTblDetailRow = detailCount > 0 ? detailCount - 1 : 0;
//                return;
//            }
//
//            // Check for duplicate amount and particular
//            for (int lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getDetailCount(); lnCtr++) {
//                if (lnCtr == pnTblDetailRow) {
//                    continue; // Skip current row
//                }
//                boolean isSameParticular = poGLControllers.PaymentRequest().Detail(lnCtr).getParticularID()
//                        .equals(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID());
//
//                boolean isSameAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAmount() == amount;
//
//                if (isSameParticular && isSameAmount) {
//                    // Duplicate found
//                    amount = 0.00;
//                    poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(amount);
//                    tfAmount.setText("0.00");
//
//                    ShowMessageFX.Warning("Amount and Particular already exist in table at row: " + (lnCtr + 1), psFormName, null);
//                    pnTblDetailRow = lnCtr;
//                    loadTableDetail();
////                    initDetailFocus();
//                    return;
//                }
//            }
//
//            // If amount is zero, clear discount fields
//            if (amount == 0.00) {
//                poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setDiscount(0.00);
//                poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAddDiscount(0.00);
//            }
//
//            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(amount);
//            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAmount(), true));
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//        } catch (NumberFormatException ex) {
//            ShowMessageFX.Warning("Invalid numeric input for amount.", psFormName, null);
//            tfAmount.setText("0.00");
//        }
//    }
    private void initTextFieldPattern() {
        CustomCommonUtil.inputDecimalOnly(
                tfTotalAmount, tfDiscountAmount, tfNetAmount,
                tfAmount, tfDiscRate, tfDiscAmountDetail);
        JFXUtil.setCommaFormatter(tfAmountDetail);

        JFXUtil.handleDisabledNodeClick(apDetail, pnEditMode, nodeID -> {
            try {
                switch (nodeID) {
                    case "tfParticular":
                        //define if recurring id exists if not define if the editmode of the item is update
                        boolean lbIsRecurring;
                        lbIsRecurring = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getRecurringNo(), null, "");
                        if (lbIsRecurring) {
                            ShowMessageFX.Warning(null, psFormName, "This field is disabled by default as it is linked to Recurring.");
                        } else {
                            ShowMessageFX.Warning(null, psFormName, "This field is disabled by default as the item has already been saved.");
                        }
                        break;
                    case "cbReverse":
                        ShowMessageFX.Warning(null, psFormName,
                                "Reverse is disabled for non-open transactions.");
                        break;
                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void initDatePickerActions() {
        dpTransaction.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (dpTransaction.getValue() != null) {
                    try {
                        poGLControllers.PaymentRequest().Master().setTransactionDate(SQLUtil.toDate(dpTransaction.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void initCheckBoxActions() {
//        chkbVatable.setOnAction(event -> {
//            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                try {
//                    poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isVatable(chkbVatable.isSelected());
//                    loadTableDetail();
//                    initFields(pnEditMode);
//                } catch (SQLException | GuanzonException ex) {
//                    Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
        cbReverse.setOnAction(event -> {
            try {
                if (poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getEditMode() == EditMode.ADDNEW) {
                    poGLControllers.PaymentRequest().Detail().remove(pnTblDetailRow);
                } else {
                    poGLControllers.PaymentRequest().Detail(pnTblDetailRow).isReverse(cbReverse.isSelected());
                }
                loadTableDetail();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void clearMasterFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        pnTblDetailRow = -1;
        lblStatus.setText("");
        imageView.setImage(null);
        JFXUtil.clearTextFields(apMaster, apAttachments);
    }

    private void clearDetailFields() {
        JFXUtil.clearTextFields(apDetail);
    }

    private void initButtons(int fnEditMode) {
        try {
            boolean lbShow = (pnEditMode == EditMode.UPDATE);

            btnClose.setVisible(!lbShow);
            btnClose.setManaged(!lbShow);

            CustomCommonUtil.setVisible(lbShow, btnSave, btnCancel);
            CustomCommonUtil.setManaged(lbShow, btnSave, btnCancel);

            CustomCommonUtil.setVisible(false, btnConfirm, btnReturn, btnVoid, btnUpdate, btnSearch);
            CustomCommonUtil.setManaged(false, btnConfirm, btnReturn, btnVoid, btnUpdate, btnSearch);
            CustomCommonUtil.setVisible(lbShow, btnSearch);
            CustomCommonUtil.setManaged(lbShow, btnSearch);
            JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);
            JFXUtil.setDisabled(!lbShow, apMaster, apDetail, apAttachments);
            if (fnEditMode == EditMode.READY) {

                switch (poGLControllers.PaymentRequest().Master().getTransactionStatus()) {
                    case PaymentRequestStatus.OPEN:
                        CustomCommonUtil.setVisible(true, btnConfirm, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnConfirm, btnVoid, btnUpdate);
                        break;
                    case PaymentRequestStatus.CONFIRMED:
                        CustomCommonUtil.setVisible(true, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnVoid, btnUpdate);
                        break;
                }

            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setDisable(!lbShow, tfPayee, tfAmount, taRemarks);
        CustomCommonUtil.setDisable(true, tfPayee, dpTransaction, tfTransactionNo, tfBranch,
                tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfNetAmount,
                tfDepartment);
        if (poApp.isMainOffice() || poApp.isWarehouse()) {
            tfDepartment.setDisable(!lbShow); //mag open siya pag add new or update sa editmode
        }
        if (tblVwPaymentRequest.getItems().isEmpty()) {
            pagination.setVisible(false);
            pagination.setManaged(false);
        }
    }

    private void loadTableMain() {
        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblVwPaymentRequest.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    main_data.clear();
                    poJSON = poGLControllers.PaymentRequest().getPaymentRequest(tfSearchTransaction.getText(), tfSearchPayee.getText());
                    if ("success".equals(poJSON.get("result"))) {
                        if (poGLControllers.PaymentRequest().getPRFMasterCount() > 0) {
                            for (int lnCntr = 0; lnCntr < poGLControllers.PaymentRequest().getPRFMasterCount(); lnCntr++) {
                                main_data.add(new ModelTableMain(
                                        String.valueOf(lnCntr + 1),
                                        poGLControllers.PaymentRequest().poPRFMaster(lnCntr).getTransactionNo(),
                                        poGLControllers.PaymentRequest().poPRFMaster(lnCntr).Branch().getBranchName(),
                                        poGLControllers.PaymentRequest().poPRFMaster(lnCntr).Payee().getPayeeName(),
                                        poGLControllers.PaymentRequest().poPRFMaster(lnCntr).getTransactionStatus(),
                                        "", "", "", "", ""));
                            }
                        } else {
                            main_data.clear();
                        }
                    }

                    Platform.runLater(() -> {
                        if (main_data.isEmpty()) {
                            tblVwPaymentRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        }
                    });

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false); // ✅ Re-enable the button

                if (main_data == null || main_data.isEmpty()) {
                    tblVwPaymentRequest.setPlaceholder(new Label("NO RECORD TO LOAD"));
                    ShowMessageFX.Warning("No Record Payment Request to Load.", psFormName, null);
                } else {
                    if (pagination != null) {
                        int pageCount = (int) Math.ceil((double) main_data.size() / pnTblMain_Page);
                        pagination.setPageCount(pageCount);
                        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> createPage(newIndex.intValue()));
                    }
                    createPage(0);
                    pagination.setVisible(true);
                    pagination.setManaged(true);
                    tblVwPaymentRequest.toFront();
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

    private Node createPage(int pageIndex) {
        int totalPages = (int) Math.ceil((double) main_data.size() / pnTblMain_Page);
        if (totalPages == 0) {
            totalPages = 1;
        }

        pageIndex = Math.max(0, Math.min(pageIndex, totalPages - 1));
        int fromIndex = pageIndex * pnTblMain_Page;
        int toIndex = Math.min(fromIndex + pnTblMain_Page, main_data.size());

        if (!main_data.isEmpty()) {
            tblVwPaymentRequest.setItems(FXCollections.observableArrayList(main_data.subList(fromIndex, toIndex)));
        }

        if (pagination != null) { // Replace with your actual Pagination variable
            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(pageIndex);
        }
        return tblVwPaymentRequest;
    }

    private void initTablePaymentRequest() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransactionNo);
        JFXUtil.setColumnLeft(tblBranch, tblPayee);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPaymentRequest);
        tblVwPaymentRequest.setItems(FXCollections.observableArrayList(main_data));

        initTableHighlithers();
    }

    private void initTableHighlithers() {
        tblVwPaymentRequest.setRowFactory(tv -> new TableRow<ModelTableMain>() {
            @Override
            protected void updateItem(ModelTableMain item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Assuming empIndex05 corresponds to an employee status
                    String status = item.getIndex05(); // Replace with actual getter
                    switch (status) {
                        case PaymentRequestStatus.CONFIRMED:
                            setStyle("-fx-background-color: #C1E1C1;");
                            break;
                        case PaymentRequestStatus.VOID:
                            setStyle("-fx-background-color: #FAA0A0;");
                            break;
                        case PaymentRequestStatus.RETURNED:
                            setStyle("-fx-background-color: #FAC898;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                    tblVwPaymentRequest.refresh();
                }
            }
        });
    }

    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblVwPRDetail.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelTableDetail>> task = new Task<List<ModelTableDetail>>() {
            @Override
            protected List<ModelTableDetail> call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        detail_data.clear();
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            poGLControllers.PaymentRequest().ReloadDetail();
                        }

                        int detailCount = poGLControllers.PaymentRequest().getDetailCount();
                        int lnRowCount = 0;
                        for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
//                        double totalNetDetailPayable = 0.00;
//                        double totalTaxAmount = 0.00;
//                        double lnAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAmount().doubleValue();
//                        double lnDiscountAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAddDiscount().doubleValue();
                            String lsIsVatable = "N";
                            if (poGLControllers.PaymentRequest().Detail(lnCtr).isVatable()) {
//                            poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, true, 0.12, 0.00);
                                lsIsVatable = "Y";
                            }
//                        } else {
//                            poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, false, 0.12, 0.00);
//                        }
//                        totalTaxAmount = Double.parseDouble(poJSON.get("vat").toString());
//                        totalNetDetailPayable = Double.parseDouble(poJSON.get("netPayable").toString());
                            if (!poGLControllers.PaymentRequest().Detail(lnCtr).isReverse()) {
                                continue;
                            }
                            lnRowCount += 1;
                            detail_data.add(new ModelTableDetail(
                                    String.valueOf(lnRowCount),
                                    poGLControllers.PaymentRequest().Detail(lnCtr).getParticularID(),
                                    poGLControllers.PaymentRequest().Detail(lnCtr).Particular().getDescription(),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAmount(), true),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getDiscount()),
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAddDiscount(), true),
                                    lsIsVatable,
                                    "0.00",
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getNetTotal(), true),
                                    "",
                                    String.valueOf(lnCtr)
                            ));
                        }
                        tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
                        tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
//                        tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTaxAmount(), true));
                        tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
                        reselectLastRow();
                        initFields(pnEditMode);

                        int lnTempRow = JFXUtil.getDetailRow(detail_data, pnTblDetailRow, 11); //this method is used only when Reverse is applied
                        if (lnTempRow < 0 || lnTempRow
                                >= detail_data.size()) {
                            if (!detail_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblVwPRDetail, 0);
                                int lnRow = Integer.parseInt(detail_data.get(0).getIndex11());
                                pnTblDetailRow = lnRow;
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblVwPRDetail, lnTempRow);
                            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex11());
                            pnTblDetailRow = lnRow;
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });

                return null;
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

    private void initTableDetail() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblParticular);
        JFXUtil.setColumnRight(tblAmount, tblDiscAmount, tbTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPRDetail);

        tblRowNoDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblParticular.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblAmount.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblDiscAmount.setCellValueFactory(new PropertyValueFactory<>("index06"));
//        tblVATable.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tbTotalAmount.setCellValueFactory(new PropertyValueFactory<>("index09"));
        // Prevent column reordering
        tblVwPRDetail.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblVwPRDetail.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
            }
        });
        tblVwPRDetail.setItems(detail_data);
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblVwPRDetail":
                    newIndex = isMovedDown
                            ? Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex11()) : Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex11());
                    if (!detail_data.isEmpty()) {
                        pnTblDetailRow = newIndex;
                        loadRecordDetail();
                        initDetailFocus();
                    }
                    break;
                case "tblAttachments":
                    newIndex = isMovedDown
                            ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03()) : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                    if (!attachment_data.isEmpty()) {
                        pnAttachment = newIndex;
                        loadRecordAttachment(true);
                    }
                    break;
            }
        }
    };

    // Method to reselect the last clicked row
    private void reselectLastRow() {
        if (pnTblDetailRow >= 0 && pnTblDetailRow < tblVwPRDetail.getItems().size()) {
            tblVwPRDetail.getSelectionModel().clearAndSelect(pnTblDetailRow);
            tblVwPRDetail.getSelectionModel().focus(pnTblDetailRow); // Scroll to the selected row if needed
        }
    }

    private void initTextFieldsProperty() {
        tfSearchPayee.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poGLControllers.PaymentRequest().Master().setPayeeID("");
                        tfSearchPayee.setText("");
                        psPayeeID = "";
                        loadTableMain();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
        tfSearchTransaction.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    tfSearchTransaction.setText("");
                    psTransactionNo = "";
                    loadTableMain();
                }
            }
        }
        );
    }

    private void tblVwMain_Clicked(MouseEvent event) {
        poJSON = new JSONObject();
        if (event.getClickCount() == 2) {
            pnTblMainRow = tblVwPaymentRequest.getSelectionModel().getSelectedIndex();
            if (pnTblMainRow < 0 || pnTblMainRow >= tblVwPaymentRequest.getItems().size()) {
                ShowMessageFX.Warning("Please select valid payment request information.", "Warning", null);
                return;
            }
            ModelTableMain loSelectedPaymentRequest = (ModelTableMain) tblVwPaymentRequest.getSelectionModel().getSelectedItem();
            if (loSelectedPaymentRequest != null) {
                try {
                    String lsTransactionNo = loSelectedPaymentRequest.getIndex02();
                    if (!JFXUtil.loadValidation(pnEditMode, psFormName, poGLControllers.PaymentRequest().Master().getTransactionNo(), lsTransactionNo)) {
                        return;
                    }
                    poJSON = poGLControllers.PaymentRequest().InitTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        clearMasterFields();
                        clearDetailFields();
                        poJSON = poGLControllers.PaymentRequest().OpenTransaction(lsTransactionNo);
                        if ("success".equals((String) poJSON.get("result"))) {
                            CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                            loadTableDetailFromMain();
                            pnTblDetailRow = -1;
                            pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButtons(pnEditMode);
                        initFields(pnEditMode);

                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                }
            }
        }

    }

    private void loadTableDetailAndSelectedRow() {
        if (pnTblDetailRow >= 0) {
            Platform.runLater(() -> {
                // Run a delay after the UI thread is free
                PauseTransition delay = new PauseTransition(Duration.millis(10));
                delay.setOnFinished(event -> {
                    Platform.runLater(() -> { // Run UI updates in the next cycle
                        loadTableDetail();
                    });
                });
                delay.play();
            });
            loadRecordDetail();
            initDetailFocus();
        }
    }

    private void tblVwDetail_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex11());
            pnTblDetailRow = lnRow;
            ModelTableDetail selectedItem = (ModelTableDetail) tblVwPRDetail.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                clearDetailFields();
                if (selectedItem != null) {
                    if (pnTblDetailRow >= 0) {
                        loadRecordDetail();
                        initDetailFocus();
                    }
                }
            }
        }
    }

    private void initDetailFocus() {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {

            if (pnTblDetailRow >= 0) {
                try {
                    boolean isSourceNotEmpty = !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "");
                    if (isSourceNotEmpty && !JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "")) {
                        tfAmount.requestFocus();
                    } else {
                        if (!JFXUtil.isObjectEqualTo(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID(), null, "")
                                && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                            tfAmount.requestFocus();
                        } else {
                            tfParticular.requestFocus();
                        }
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private void initComboBoxes() {
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
