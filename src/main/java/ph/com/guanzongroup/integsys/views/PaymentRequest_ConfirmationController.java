package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPRFAttachment;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
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
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;

/**
 * FXML Controller class
 *
 * @author User
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

    @FXML
    private TabPane ImTabPane;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apAttachments, apAttachmentButtons;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate,
            btnSearch, btnSave, btnCancel, btnHistory, btnRetrieve, btnClose,
            btnVoid, btnReturn,
            btnConfirm;
    @FXML
    private TextField tfSearchTransaction, tfSearchPayee, tfTransactionNo, tfBranch, tfDepartment, tfPayee, tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount, tfNetAmount;
    @FXML
    private TextArea taRemarks;
    @FXML
    private DatePicker dpTransaction;
    @FXML
    private Label lblStatus, lblSource;
    @FXML
    private TextField tfParticular, tfAmount, tfDiscRate, tfDiscAmountDetail, tfTaxAmount, tfAmountDetail;
    @FXML
    private CheckBox chkbVatable;
    @FXML
    private TableView<ModelTableDetail> tblVwPRDetail;
    @FXML
    private TableColumn<ModelTableDetail, String> tblRowNoDetail, tblParticular, tblAmount, tblDiscAmount, tblVATable, tblTaxAmount, tbTotalAmount;
    @FXML
    private TableView<ModelTableMain> tblVwPaymentRequest;
    @FXML
    private TableColumn<ModelTableMain, String> tblRowNo, tblTransactionNo, tblBranch, tblPayee;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField tfAttachmentNo;
    @FXML
    private ComboBox<String> cmbAttachmentType;
    @FXML
    private TableView<ModelPRFAttachment> tblAttachments;
    @FXML
    private TableColumn<ModelPRFAttachment, String> tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Button btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

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
            tblVwPRDetail.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
            Platform.runLater((() -> {
                try {
                    poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                    poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                    loadRecordSearch();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }));
            Platform.runLater(() -> setBranchAndDepartment());
            initAll();

        } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poGLControllers.PaymentRequest().Master().Company().getCompanyName() + " - " + poGLControllers.PaymentRequest().Master().Industry().getDescription());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTableOnClick() {
        tblVwPRDetail.setOnMouseClicked(this::tblVwDetail_Clicked);
        tblVwPaymentRequest.setOnMouseClicked(this::tblVwMain_Clicked);
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                scaleFactor = 1.0;
                loadRecordAttachment(true);
                resetImageBounds();
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
        initAttachmentPreviewPane();
        initStackPaneListener();
        initButtons(pnEditMode);
        initFields(pnEditMode);
        initComboBoxCellDesign(cmbAttachmentType);
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void loadRecordMaster() {
        try {
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
            tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTaxAmount(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
            taRemarks.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
            lblStatus.setText("");
            String lsStatus = "";
            switch (poGLControllers.PaymentRequest().Master().getTransactionStatus()) {
                case PaymentRequestStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case PaymentRequestStatus.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
                case PaymentRequestStatus.PAID:
                    lsStatus = "PAID";
                    break;
                case PaymentRequestStatus.VOID:
                    lsStatus = "VOID";
                    break;
                case PaymentRequestStatus.POSTED:
                    lsStatus = "POSTED";
                    break;
                case PaymentRequestStatus.CANCELLED:
                    lsStatus = "CANCELLED";
                    break;
                case PaymentRequestStatus.RETURNED:
                    lsStatus = "RETURNED";
                    break;
            }
            lblStatus.setText(lsStatus);
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordDetail() {
        if (pnTblDetailRow >= 0) {
            try {
                String lsParticular = "";
                if (poGLControllers.PaymentRequest().Detail(pnTblDetailRow).Particular().getDescription() != null) {
                    lsParticular = poGLControllers.PaymentRequest().Detail(pnTblDetailRow).Particular().getDescription();
                }
                tfParticular.setText(lsParticular);

                tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAmount(), true));
                tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getDiscount())); // rate
                tfDiscAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAddDiscount(), true)); // amount

                if (poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getVatable().equals("1")) {
                    chkbVatable.setSelected(true);
                } else {
                    chkbVatable.setSelected(false);
                }
                computePerDetailTaxAndTotal();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void computePerDetailTaxAndTotal() {
        //            double totalNetPayable = 0.00;
//            double totalTaxAmount = 0.00;
        double lnAmount = Double.parseDouble(tfAmount.getText().replace(",", ""));
//            double lnDiscountAmount = Double.parseDouble(tfDiscAmountDetail.getText().replace(",", ""));
//            if (chkbVatable.isSelected()) {
//                poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, true, 0.12, 0.00);
//            } else {
//                poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, false, 0.12, 0.00);
//            }
//            totalTaxAmount = Double.parseDouble(poJSON.get("vat").toString());
//            tfTaxAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(totalTaxAmount));
//            totalNetPayable = Double.parseDouble(poJSON.get("netPayable").toString());
        tfAmountDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnAmount, true));
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnUpdate, btnSave, btnCancel, btnVoid, btnReturn,
                btnRetrieve, btnHistory, btnClose, btnConfirm,
                btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight);
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
                    clearDetailFields();
                    pnTblDetailRow = -1;
                    pagination.toFront();
                    break;
                case "btnSearch":
                    if (activeField != null) {
                        String loTextFieldId = activeField.getId();
                        String lsValue = activeField.getText().trim();
                        switch (loTextFieldId) {
                            case "tfParticular":
                                poJSON = poGLControllers.PaymentRequest().SearchParticular(lsValue, false, pnTblDetailRow);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfParticular.setText("");
                                    break;
                                }
                                tfParticular.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID());
                                if (tfParticular.getText().isEmpty()) {
                                    tfParticular.requestFocus();
                                }

                                break;
                            default:
                                System.out.println("Unknown TextField");
                        }
                    }
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
                            loadRecordDetail();
                            initDetailFocus();
                            return;
                        } else {
                            if (!ShowMessageFX.YesNo((String) poJSON.get("message"), psFormName, null)) {
                                pnTblDetailRow = (int) poJSON.get("tableRow");
                                loadTableDetail();
                                loadRecordDetail();
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
                                } else {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                            }
                        }
                    }

                    loadRecordMaster();
                    loadRecordDetail();
                    loadTableDetail();
                    pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                    pagination.toBack();
                    break;
                case "btnCancel":
                    if (ShowMessageFX.YesNo(null, "Cancel Confirmation", "Are you sure you want to cancel?")) {
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
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
                    );
                    java.io.File selectedFile = fileChooser.showOpenDialog((Stage) btnAddAttachment.getScene().getWindow());

                    if (selectedFile != null) {
                        try {
                            // Display image
                            Path imgPath = selectedFile.toPath();
                            Image loimage = new Image(Files.newInputStream(imgPath));
                            imageView.setImage(loimage);

                            // Add attachment in controller
                            poJSON = poGLControllers.PaymentRequest().addAttachment();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, psFormName, (String) poJSON.get("message"));
                                return;
                            }

                            // Save image to a temp directory
                            String imgPath2 = selectedFile.getName();

                            pnAttachment = poGLControllers.PaymentRequest().getTransactionAttachmentCount() - 1;
                            Path destPath = Paths.get("D:\\GGC_Maven_Systems\\temp\\attachments\\" + imgPath2);
                            Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                            Path parentDir = destPath.getParent(); // Get the parent directory path

                            try {
                                if (!Files.exists(parentDir)) {
                                    Files.createDirectories(parentDir); // Create directories if they don't exist
                                    System.out.println("Directories created: " + parentDir);
                                }
                            } catch (IOException e) {
                                System.err.println("Error creating directories: " + e.getMessage());
                            }
                            for (int lnCtr = 0; lnCtr <= poGLControllers.PaymentRequest().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName())) {
                                    ShowMessageFX.Warning(null, psFormName, "File name already exist.");
                                    pnAttachment = lnCtr;
                                    loadRecordAttachment(true);
                                    return;
                                }
                            }
                            pnAttachment = poGLControllers.PaymentRequest().getTransactionAttachmentCount() - 1;
                            poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setFileName(imgPath2);
                            poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().setSourceNo(poGLControllers.PaymentRequest().Master().getTransactionNo());
                            poGLControllers.PaymentRequest().TransactionAttachmentList(pnAttachment).getModel().getTransactionNo();
                            loadTableAttachment();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        } catch (IOException ex) {
                            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
                case "btnRemoveAttachment":
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
                        poGLControllers.PaymentRequest().resetMaster();
                        poGLControllers.PaymentRequest().resetOthers();
                        poGLControllers.PaymentRequest().Detail().clear();
                        detail_data.clear();
                        attachment_data.clear();
                        tblVwPRDetail.getItems().clear();
                        tblAttachments.getItems().clear();
                        tblAttachments.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        clearMasterFields();
                        clearDetailFields();
                        CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                        pnEditMode = EditMode.UNKNOWN;
                        tblVwPaymentRequest.refresh();
                        main_data.get(pnTblMainRow).setIndex05(PaymentRequestStatus.RETURNED);
                    } else {
                        return;
                    }
                    break;
                case "btnVoid":
                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to void transaction?")) {
                        poJSON = poGLControllers.PaymentRequest().VoidTransaction("Voided");
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            break;
                        }
                        ShowMessageFX.Information((String) poJSON.get("message"), psFormName, null);
                        poGLControllers.PaymentRequest().resetMaster();
                        poGLControllers.PaymentRequest().resetOthers();
                        poGLControllers.PaymentRequest().Detail().clear();
                        detail_data.clear();
                        attachment_data.clear();
                        tblVwPRDetail.getItems().clear();
                        tblAttachments.getItems().clear();
                        tblAttachments.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        clearMasterFields();
                        clearDetailFields();
                        CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                        pnEditMode = EditMode.UNKNOWN;
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
            if (lsButton.equals("btnAddAttachment") || lsButton.equals("btnRemoveAttachment")
                    || lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve")) {
            } else {
                loadRecordMaster();
                loadTableDetail();
                loadTableAttachment();
            }
            initButtons(pnEditMode);
            initFields(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            if (poGLControllers.PaymentRequest().getEditMode() == EditMode.READY || poGLControllers.PaymentRequest().getEditMode() == EditMode.UPDATE
                    || poGLControllers.PaymentRequest().getEditMode() == EditMode.UPDATE) {
                poGLControllers.PaymentRequest().loadAttachments();
                Platform.runLater(() -> {
                    loadTableDetail();
                });
                tfAttachmentNo.clear();
                cmbAttachmentType.setItems(documentType);
                imageView.setImage(null);
                stackPaneClip();
                Platform.runLater(() -> {
                    loadTableAttachment();
                });
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(String.valueOf(pnAttachment + 1));
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
                        String filePath = (String) attachment_data.get(pnAttachment).getIndex02();
                        String filePath2 = "D:\\GGC_Maven_Systems\\temp\\attachments\\" + (String) attachment_data.get(pnAttachment).getIndex02();
                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            Image loimage = new Image(convertedPath);
                            imageView.setImage(loimage);
                            adjustImageSize(loimage);
                            stackPaneClip();
                            stackPaneClip(); // dont remove duplicate

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
                    stackPaneClip();
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
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
                Platform.runLater(() -> {
                    try {
                        attachment_data.clear();
                        int lnCtr;
                        for (lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getTransactionAttachmentCount(); lnCtr++) {
                            attachment_data.add(
                                    new ModelPRFAttachment(String.valueOf(lnCtr + 1),
                                            String.valueOf(poGLControllers.PaymentRequest().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                    ));
                        }
                        if (pnAttachment < 0 || pnAttachment
                                >= attachment_data.size()) {
                            if (!attachment_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblAttachments.getSelectionModel().select(0);
                                tblAttachments.getFocusModel().focus(0);
                                pnAttachment = 0;
                                loadRecordAttachment(true);
                            } else {
                                tfAttachmentNo.setText("");
                                cmbAttachmentType.getSelectionModel().select(0);
                                loadRecordAttachment(false);
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblAttachments.getSelectionModel().select(pnAttachment);
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            loadRecordAttachment(true);
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

    private void adjustImageSize(Image image) {
        double imageRatio = image.getWidth() / image.getHeight();
        double containerRatio = ldstackPaneWidth / ldstackPaneHeight;

        // Unbind before setting new values
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();

        if (imageRatio > containerRatio) {
            // Image is wider than container → fit width
            imageView.setFitWidth(ldstackPaneWidth);
            imageView.setFitHeight(ldstackPaneWidth / imageRatio);
        } else {
            // Image is taller than container → fit height
            imageView.setFitHeight(ldstackPaneHeight);
            imageView.setFitWidth(ldstackPaneHeight * imageRatio);
        }

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }

    private void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        tblRowNoAttachment.setStyle("-fx-alignment: CENTER;-fx-padding: 0 5 0 5;");
        tblFileNameAttachment.setStyle("-fx-alignment: CENTER;-fx-padding: 0 5 0 5;");

        tblRowNoAttachment.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblFileNameAttachment.setCellValueFactory(new PropertyValueFactory<>("index02"));

        tblAttachments.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblAttachments.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });

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
        List<TextField> loTxtField = Arrays.asList(tfAmount, tfDiscRate, tfDiscAmountDetail);
        loTxtField.forEach(tf -> tf.focusedProperty().addListener(txtField_Focus));

        tfSearchPayee.setOnMouseClicked(e -> activeField = tfSearchPayee);
        tfSearchTransaction.setOnMouseClicked(e -> activeField = tfSearchTransaction);
        tfParticular.setOnMouseClicked(e -> activeField = tfParticular);
    }

    private void initTextAreaFocus() {
        taRemarks.focusedProperty().addListener(txtArea_Focus);
    }
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
                case "tfDiscRate":
                    break;
                case "tfDiscAmountDetail":
                    break;
                case "tfAmount":
                    break;
            }
        } else {
            loTextField.selectAll();
        }
    };
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
            Logger.getLogger(PaymentRequest_ConfirmationController.class
                    .getName()).log(Level.SEVERE, null, ex);
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
                                    poJSON = poGLControllers.PaymentRequest().SearchParticular(lsValue, true, pnTblDetailRow);
                                    if ("error".equals(poJSON.get("result"))) {
                                        ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                        tfParticular.setText("");
                                        if (poJSON.get("tableRow") != null) {
                                            pnTblDetailRow = (int) poJSON.get("tableRow");
                                        } else {
                                            break;
                                        }
                                    }
                                    loadTableDetail();
                                    loadRecordDetail();
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
                            case "tfDiscRate":
                                setDiscountRate(tfDiscRate.getText());
                                loadTableDetailAndSelectedRow();
                                break;
                            case "tfDiscAmountDetail":
                                setDiscountAmount(tfDiscAmountDetail.getText());
                                loadTableDetailAndSelectedRow();
                                break;
                            case "tfAmount":
                                setAmountToDetail(tfAmount.getText());
                                if (!detail_data.isEmpty() && pnTblDetailRow < detail_data.size() - 1) {
                                    pnTblDetailRow++;
                                }
                                CommonUtils.SetNextFocus((TextField) event.getSource());
                                loadTableDetailAndSelectedRow();
                                break;
                        }
                        event.consume();
                        break;
                    case UP:
                        setAmountToDetail(tfAmount.getText());
                        if (!lsTxtField.equals("tfParticular")) {
                            if (pnTblDetailRow > 0 && !detail_data.isEmpty()) {
                                pnTblDetailRow--;
                            }
                        }

                        if (!lsTxtField.equals("tfParticular") && !lsTxtField.equals("tfAmount")) {
                            CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        }
                        loadTableDetailAndSelectedRow();
                        event.consume();
                        break;
                    case DOWN:
                        setAmountToDetail(tfAmount.getText());
                        if ("tfAmount".equals(lsTxtField.getId())) {
                            if (!detail_data.isEmpty() && pnTblDetailRow < detail_data.size() - 1) {
                                pnTblDetailRow++;
                            }
                        }
                        CommonUtils.SetNextFocus(lsTxtField);
                        loadTableDetailAndSelectedRow();
                        event.consume(); // Consume event after handling focus
                        break;
                    default:
                        break;

                }
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(PaymentRequest_ConfirmationController.class
                        .getName()).log(Level.SEVERE, null, ex);
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
            computePerDetailTaxAndTotal();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
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
            computePerDetailTaxAndTotal();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setAmountToDetail(String fsValue) {
        try {
            if (fsValue == null || fsValue.isEmpty()) {
                fsValue = "0.00";
            }

            double amount = Double.parseDouble(fsValue.replace(",", ""));
            if (amount < 0.0) {
                ShowMessageFX.Warning("Invalid Amount", psFormName, null);
                amount = 0.00;
            }

            if (tfAmount.isFocused() && tfParticular.getText().isEmpty()) {
                ShowMessageFX.Warning("Invalid action, Please enter particular first.", psFormName, null);
                tfParticular.requestFocus();
                return;
            }

            if (pnTblDetailRow < 0) {
                ShowMessageFX.Warning("Invalid row to update.", psFormName, null);
                clearDetailFields();
                int detailCount = poGLControllers.PaymentRequest().getDetailCount();
                pnTblDetailRow = detailCount > 0 ? detailCount - 1 : 0;
                return;
            }

            // Check for duplicate amount and particular
            for (int lnCtr = 0; lnCtr < poGLControllers.PaymentRequest().getDetailCount(); lnCtr++) {
                if (lnCtr == pnTblDetailRow) {
                    continue; // Skip current row
                }
                boolean isSameParticular = poGLControllers.PaymentRequest().Detail(lnCtr).getParticularID()
                        .equals(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID());

                boolean isSameAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAmount() == amount;

                if (isSameParticular && isSameAmount) {
                    // Duplicate found
                    amount = 0.00;
                    poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(amount);
                    tfAmount.setText("0.00");

                    ShowMessageFX.Warning("Amount and Particular already exist in table at row: " + (lnCtr + 1), psFormName, null);
                    pnTblDetailRow = lnCtr;
                    loadTableDetail();
                    loadRecordDetail();
                    initDetailFocus();
                    return;
                }
            }

            // If amount is zero, clear discount fields
            if (amount == 0.00) {
                poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setDiscount(0.00);
                poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAddDiscount(0.00);
            }

            poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setAmount(amount);
            tfAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getAmount(), true));
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            ShowMessageFX.Warning("Invalid numeric input for amount.", psFormName, null);
            tfAmount.setText("0.00");
        }
    }

    private void initTextFieldPattern() {
        CustomCommonUtil.inputDecimalOnly(
                tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount, tfNetAmount,
                tfAmount, tfDiscRate, tfDiscAmountDetail, tfTaxAmount, tfAmountDetail
        );
    }

    private void initDatePickerActions() {
        dpTransaction.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                if (dpTransaction.getValue() != null) {
                    try {
                        poGLControllers.PaymentRequest().Master().setTransactionDate(SQLUtil.toDate(dpTransaction.getValue().toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void initCheckBoxActions() {
        chkbVatable.setOnAction(event -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                double lnAmount = Double.parseDouble(tfAmount.getText().replace(",", ""));
                double lnDiscountAmount = Double.parseDouble(tfDiscAmountDetail.getText().replace(",", ""));
                try {
                    if (chkbVatable.isSelected()) {
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setVatable(Logical.YES);
                        poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, true, 0.12, 0.00);
                    } else {
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setVatable(Logical.NO);
                        poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, false, 0.12, 0.00);
                    }
                    computePerDetailTaxAndTotal();
                    loadTableDetailAndSelectedRow();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                }
                initFields(pnEditMode);
            }
        });
    }

    private void clearMasterFields() {
        pnTblDetailRow = -1;
        dpTransaction.setValue(null);
        taRemarks.clear();
        lblStatus.setText("");
        CustomCommonUtil.setText("", tfTransactionNo, tfSeriesNo, tfAttachmentNo
        );

        cmbAttachmentType.setValue(null);
        imageView.setImage(null);
        CustomCommonUtil.setText("0.0000", tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount,
                tfNetAmount
        );
    }

    private void clearDetailFields() {
        CustomCommonUtil.setText("", tfParticular);
        CustomCommonUtil.setSelected(false, chkbVatable);
        CustomCommonUtil.setText("0.0000", tfAmount, tfDiscAmountDetail,
                tfTaxAmount, tfAmountDetail
        );
        CustomCommonUtil.setText("0.00", tfDiscRate);
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

            btnHistory.setVisible(fnEditMode == EditMode.READY);
            btnHistory.setManaged(fnEditMode == EditMode.READY);
            if (fnEditMode == EditMode.READY) {

                switch (poGLControllers.PaymentRequest().Master().getTransactionStatus()) {
                    case PaymentRequestStatus.OPEN:
                        CustomCommonUtil.setVisible(true, btnConfirm, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnConfirm, btnVoid, btnUpdate);
                        break;
                    case PaymentRequestStatus.CONFIRMED:
                        CustomCommonUtil.setVisible(true, btnReturn, btnVoid, btnUpdate);
                        CustomCommonUtil.setManaged(true, btnReturn, btnVoid, btnUpdate);
                        break;
                }

            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PaymentRequest_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setDisable(!lbShow, tfPayee, tfAmount, taRemarks);
        CustomCommonUtil.setDisable(true, tfParticular, tfPayee, dpTransaction, tfTransactionNo, tfBranch,
                tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount, tfNetAmount,
                chkbVatable, tfDiscRate,
                tfDiscAmountDetail, tfDepartment);
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
                    poJSON = poGLControllers.PaymentRequest().getPaymentRequest(psTransactionNo, psPayeeID);
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
                        tblVwPaymentRequest.setItems(FXCollections.observableArrayList(main_data));
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
        tblRowNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblTransactionNo.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblBranch.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblPayee.setCellValueFactory(new PropertyValueFactory<>("index04"));

        tblVwPaymentRequest.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblVwPaymentRequest.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
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

    private <T> void initComboBoxCellDesign(ComboBox<T> comboBox) {
        comboBox.setCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");  // Reset to default style for non-selected items

                if (empty) {
                    setText(null);
                    setStyle("");  // Reset style if the item is empty
                } else {
                    setText(item.toString());  // Display the item text using its toString method

                    // Check if this item is the selected value
                    if (item.toString().equals(comboBox.getValue().toString())) {
                        // Apply the custom background color for the selected item in the list
                        setStyle("-fx-background-color: #FF8201; -fx-text-fill: white;");
                    } else {
                        setStyle("");  // Reset to default style for non-selected items
                    }
                }
            }
        });

        comboBox.setOnShowing(event -> {
            T selectedItem = comboBox.getValue();
            if (selectedItem != null) {
                // Loop through each item and apply style based on selection
                for (int i = 0; i < comboBox.getItems().size(); i++) {
                    T item = comboBox.getItems().get(i);

                    if (item.equals(selectedItem)) {
                        // Apply the custom background color for selected item in the list
                        comboBox.getItems().set(i, item);
                    } else {
                        // Reset the style for non-selected items
                        comboBox.getItems().set(i, item);
                    }
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
                try {
                    int detailCount = poGLControllers.PaymentRequest().getDetailCount();
                    List<ModelTableDetail> detailsList = new ArrayList<>();
                    for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
//                        double totalNetDetailPayable = 0.00;
//                        double totalTaxAmount = 0.00;
//                        double lnAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAmount().doubleValue();
//                        double lnDiscountAmount = poGLControllers.PaymentRequest().Detail(lnCtr).getAddDiscount().doubleValue();
                        String lsIsVatable = "N";
                        if (poGLControllers.PaymentRequest().Detail(lnCtr).getVatable().equals("1")) {
//                            poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, true, 0.12, 0.00);
                            lsIsVatable = "Y";
                        }
//                        } else {
//                            poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, false, 0.12, 0.00);
//                        }
//                        totalTaxAmount = Double.parseDouble(poJSON.get("vat").toString());
//                        totalNetDetailPayable = Double.parseDouble(poJSON.get("netPayable").toString());
                        detailsList.add(new ModelTableDetail(
                                String.valueOf(lnCtr + 1),
                                poGLControllers.PaymentRequest().Detail(lnCtr).getParticularID(),
                                poGLControllers.PaymentRequest().Detail(lnCtr).Particular().getDescription(),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAmount(), true),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getDiscount()),
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAddDiscount(), true),
                                lsIsVatable,
                                "0.00",
                                CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAmount(), true),
                                ""
                        ));
                    }
                    Platform.runLater(() -> {
                        try {
                            detail_data.setAll(detailsList); // Properly update list
                            tblVwPRDetail.setItems(detail_data);
                            poJSON = poGLControllers.PaymentRequest().computeMasterFields();
                            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
                            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
                            tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTaxAmount(), true));
                            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
                            reselectLastRow();
                            initFields(pnEditMode);

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(PaymentRequest_ConfirmationController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    return detailsList;

                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(PaymentRequest_ConfirmationController.class
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

    private void initTableDetail() {
        tblRowNoDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblParticular.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblAmount.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblDiscAmount.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblVATable.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblTaxAmount.setCellValueFactory(new PropertyValueFactory<>("index08"));
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
    }

    private int moveToNextRow(TableView<?> table, TablePosition<?, ?> focusedCell) {
        if (table.getItems().isEmpty()) {
            return -1; // No movement possible
        }
        int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
        table.getSelectionModel().select(nextRow);
        return nextRow;
    }

    private int moveToPreviousRow(TableView<?> table, TablePosition<?, ?> focusedCell) {
        if (table.getItems().isEmpty()) {
            return -1; // No movement possible
        }
        int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
        table.getSelectionModel().select(previousRow);
        return previousRow;
    }

    private void tableKeyEvents(KeyEvent event) {
        TableView<?> currentTable = (TableView<?>) event.getSource();
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

        if (focusedCell != null && "tblVwPRDetail".equals(currentTable.getId())) {
            switch (event.getCode()) {
                case TAB:
                case DOWN:
                    pnTblDetailRow = pnTblDetailRow;
                    if (pnEditMode != EditMode.ADDNEW || pnEditMode != EditMode.UPDATE) {
                        pnTblDetailRow = moveToNextRow(currentTable, focusedCell);
                    }
                    break;
                case UP:
                    pnTblDetailRow = pnTblDetailRow;
                    if (pnEditMode != EditMode.ADDNEW || pnEditMode != EditMode.UPDATE) {
                        pnTblDetailRow = moveToPreviousRow(currentTable, focusedCell);
                    }
                    break;
                default:
                    return;
            }
            currentTable.getSelectionModel().select(pnTblDetailRow);
            currentTable.getFocusModel().focus(pnTblDetailRow);
            loadRecordDetail();
            initDetailFocus();
            event.consume();
        }
    }

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
                        Logger.getLogger(PaymentRequest_ConfirmationController.class
                                .getName()).log(Level.SEVERE, null, ex);
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
        pnTblMainRow = tblVwPaymentRequest.getSelectionModel().getSelectedIndex();
        if (pnTblMainRow < 0 || pnTblMainRow >= tblVwPaymentRequest.getItems().size()) {
            ShowMessageFX.Warning("Please select valid payment request information.", "Warning", null);
            return;
        }

        if (event.getClickCount() == 2) {
            ModelTableMain loSelectedPaymentRequest = (ModelTableMain) tblVwPaymentRequest.getSelectionModel().getSelectedItem();
            if (loSelectedPaymentRequest != null) {
                String lsTransactionNo = loSelectedPaymentRequest.getIndex02();
                try {
                    poJSON = poGLControllers.PaymentRequest().InitTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        poJSON = poGLControllers.PaymentRequest().OpenTransaction(lsTransactionNo);
                        if ("success".equals((String) poJSON.get("result"))) {
                            CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                            loadRecordMaster();
                            loadTableDetailFromMain();
                            pnTblDetailRow = -1;
                            clearDetailFields();
                            pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButtons(pnEditMode);
                        initFields(pnEditMode);

                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(PurchaseOrder_ConfirmationController.class
                            .getName()).log(Level.SEVERE, null, ex);
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
            pnTblDetailRow = tblVwPRDetail.getSelectionModel().getSelectedIndex();
            ModelTableDetail selectedItem = tblVwPRDetail.getSelectionModel().getSelectedItem();
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
                    boolean isSourceNotEmpty = !poGLControllers.PaymentRequest().Detail(pnTblDetailRow).getParticularID().isEmpty();
                    if (isSourceNotEmpty && !tfParticular.getText().isEmpty()) {
                        tfAmount.requestFocus();
                    } else {
                        if (!tfParticular.getText().isEmpty() && (pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW)) {
                            tfAmount.requestFocus();
                        } else {
                            tfParticular.requestFocus();

                        }
                    }
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(PaymentRequest_ConfirmationController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

}
