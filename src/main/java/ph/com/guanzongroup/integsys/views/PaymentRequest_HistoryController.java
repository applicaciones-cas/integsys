/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPRFAttachment;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
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
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.PaymentRequestStatus;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 2 & Team 1
 */
public class PaymentRequest_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private CashflowControllers poGLControllers;
    private String psFormName = "Payment Request History";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";

    private int pnTblDetailRow = -1;
    private String prevPayee = "";
    //attachments
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double scaleFactor = 1.0;
    private int pnAttachment;

    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private ObservableList<ModelPRFAttachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelPRFAttachment.documentType;
    Map<String, String> imageinfo_temp = new HashMap<>();

    @FXML
    private DatePicker dpTransaction;
    @FXML
    private TabPane ImTabPane;
    @FXML
    private Tab tabDetails, tabAttachments;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apAttachments, apAttachmentButtons, apMaster, apDetail;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private TextField tfSearchPayee, tfTransactionNo, tfBranch, tfDepartment, tfPayee, tfSeriesNo, tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount, tfNetAmount, tfSourceNo,
            tfRecurringNo, tfBranchDetail, tfAccountNo, tfEmployee, tfVatAmount;
    @FXML
    private TextArea taRemarks;
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
    private Button btnArrowLeft, btnArrowRight;
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
                    + PaymentRequestStatus.CONFIRMED
                    + PaymentRequestStatus.RETURNED
                    + PaymentRequestStatus.PAID
                    + PaymentRequestStatus.POSTED
                    + PaymentRequestStatus.VOID
                    + PaymentRequestStatus.CANCELLED
            );
            poJSON = poGLControllers.PaymentRequest().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            JFXUtil.setKeyEventFilter(tableKeyEvents, tblVwPRDetail, tblAttachments);
            Platform.runLater((() -> {
                try {
                    poGLControllers.PaymentRequest().Master().setIndustryID(psIndustryID);
                    poGLControllers.PaymentRequest().Master().setCompanyID(psCompanyID);
                    loadRecordSearch();
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }));
            Platform.runLater(() -> setBranchAndDepartment());
            initTableOnClick();
            initAll();

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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTableOnClick() {
        tblVwPRDetail.setOnMouseClicked(this::tblVwDetail_Clicked);
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
        initTextAreaFocus();
        initTextFieldKeyPressed();
        initTextFieldsProperty();
        initCheckBoxActions();
        initTextFieldPattern();
        initTableDetail();
        initAttachmentPreviewPane();
        initStackPaneListener();
        initComboBoxes();
        initButtons(pnEditMode);
        loadTableAttachment();
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
            tfSearchPayee.setText(lsPayee);
            tfSeriesNo.setText(poGLControllers.PaymentRequest().Master().getSeriesNo());
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTranTotal(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getDiscountAmount(), true));
            tfTotalVATableAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getTaxAmount(), true));
            tfNetAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Master().getNetTotal(), true));
            taRemarks.setText(poGLControllers.PaymentRequest().Master().getRemarks());
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
            tfSourceNo.setText(poGLControllers.PaymentRequest().Master().getSourceNo());
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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

                tfRecurringNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getRecurringNo());
                tfBranchDetail.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Branch().getBranchName());
                tfAccountNo.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAccountNo());
                tfEmployee.setText(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().Employee().getCompanyName());
                tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(pnTblDetailRow).RecurringExpensePaymentMonitor().RecurringExpenseSchedule().getAmount(), true));
                computePerDetailTaxAndTotal();
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void computePerDetailTaxAndTotal() {
//        double totalNetPayable = 0.00;
//        double totalTaxAmount = 0.00;
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
        List<Button> buttons = Arrays.asList(btnBrowse, btnHistory, btnClose,
                btnArrowLeft, btnArrowRight);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poGLControllers.PaymentRequest().SearchTransaction("", prevPayee);
                    if (!"error".equals((String) poJSON.get("result"))) {
                        CustomCommonUtil.switchToTab(tabDetails, ImTabPane);
                        pnTblDetailRow = -1;
                        loadRecordMaster();
                        loadTableDetailFromMain();
                        pnEditMode = poGLControllers.PaymentRequest().getEditMode();
                        loadTableDetail();
                    } else {
                        ShowMessageFX.Warning((String) poJSON.get("message"), "Search Information", null);
                    }
                    break;
                case "btnTransHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", psFormName, null);
                        return;
                    }

                    try {
                        poGLControllers.PaymentRequest().ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", psFormName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                    }
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
                case "btnHistory":
                    poGLControllers.PaymentRequest().ShowStatusHistory();
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", psFormName, null);
                    break;
            }
            if (lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft")) {
            } else {
                clearMasterFields();
                clearDetailFields();
                loadRecordMaster();
                loadTableDetail();
                loadTableAttachment();
            }
            initButtons(pnEditMode);

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(PaymentRequest_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            if (poGLControllers.PaymentRequest().getEditMode() == EditMode.READY) {
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
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
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

    public void initAttachmentsGrid() {
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

    public void slideImage(int direction) {
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

    public void resetImageBounds() {
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
        List<TextField> loTxtField = Arrays.asList(tfSearchPayee);

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
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    switch (txtFieldID) {
                        case "tfSearchPayee": {
                            try {
                                poJSON = poGLControllers.PaymentRequest().SearchPayee(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    tfPayee.setText("");
                                    break;
                                }
                                prevPayee = poGLControllers.PaymentRequest().Master().getPayeeID();
                                tfSearchPayee.setText(poGLControllers.PaymentRequest().Master().Payee().getPayeeName());
                            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                    }
                    event.consume();
                    break;
                case UP:
                    event.consume();
                    CommonUtils.SetPreviousFocus((TextField) event.getSource());
                    break;
                case DOWN:
                    event.consume();
                    CommonUtils.SetNextFocus((TextField) event.getSource());
                    break;
                default:
                    break;

            }
        }
    }

    private void initTextFieldPattern() {
        CustomCommonUtil.inputDecimalOnly(
                tfTotalAmount, tfDiscountAmount, tfTotalVATableAmount, tfNetAmount,
                tfAmount, tfDiscRate, tfDiscAmountDetail, tfTaxAmount, tfAmountDetail
        );
    }

    private void initCheckBoxActions() {
        chkbVatable.setOnAction(event -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                double lnAmount = Double.parseDouble(tfAmount.getText().replace(",", ""));
                double lnDiscountAmount = Double.parseDouble(tfDiscAmountDetail.getText().replace(",", ""));
                try {
                    if (chkbVatable.isSelected()) {
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setVatable(Logical.YES);
                        poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, true, 0.12, 0.0000);
                    } else {
                        poGLControllers.PaymentRequest().Detail(pnTblDetailRow).setVatable(Logical.NO);
                        poJSON = poGLControllers.PaymentRequest().computeNetPayableDetails(lnAmount - lnDiscountAmount, false, 0.12, 0.0000);
                    }
                    computePerDetailTaxAndTotal();
                    loadTableDetailAndSelectedRow();

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    private void clearMasterFields() {
        pnTblDetailRow = -1;
        lblStatus.setText("");
        cmbAttachmentType.setValue(null);
        imageView.setImage(null);
        JFXUtil.clearTextFields(apMaster, apAttachments);
    }

    private void clearDetailFields() {
        JFXUtil.clearTextFields(apDetail);
    }

    private void initButtons(int fnEditMode) {
        JFXUtil.setDisabled(true, apMaster, apDetail, apAttachments);
        CustomCommonUtil.setVisible(true, btnBrowse, btnClose);
        CustomCommonUtil.setManaged(true, btnBrowse, btnClose);
        JFXUtil.setButtonsVisibility(fnEditMode == EditMode.READY, btnHistory);
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
                        int detailCount = poGLControllers.PaymentRequest().getDetailCount();
                        int lnRowCount = 0;
                        if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
                            if (poGLControllers.PaymentRequest().Detail(detailCount - 1).getParticularID() != null && !poGLControllers.PaymentRequest().Detail(detailCount - 1).getParticularID().isEmpty()) {
                                poGLControllers.PaymentRequest().AddDetail();
                                detailCount++;
                            }
                        }
                        for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
//                        double totalNetDetailPayable = 0.00;
                            double totalTaxAmount = 0.0000;
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
                                    "0.0000",
                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poGLControllers.PaymentRequest().Detail(lnCtr).getAmount(), true),
                                    "",
                                    String.valueOf(lnCtr)
                            ));
                        }
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
//                        loadRecordMaster();
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
        JFXUtil.setColumnLeft(tblParticular, tblVATable);
        JFXUtil.setColumnRight(tblAmount, tblDiscAmount, tblTaxAmount, tbTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwPRDetail);

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

    private void initTextFieldsProperty() {
        tfSearchPayee.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poGLControllers.PaymentRequest().Master().setPayeeID("");
                        prevPayee = "";
                        tfSearchPayee.setText("");
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
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
        }
    }

    private void tblVwDetail_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            int lnRow = Integer.parseInt(detail_data.get(tblVwPRDetail.getSelectionModel().getSelectedIndex()).getIndex11());
            pnTblDetailRow = lnRow;
            ModelTableDetail selectedItem = tblVwPRDetail.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1) {
                clearDetailFields();
                if (selectedItem != null) {
                    if (pnTblDetailRow >= 0) {
                        loadRecordDetail();
                    }
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
