package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.constant.RecordStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckDeposit;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckDepositStatus;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.model.ModelTableMain;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class CheckDepositSupplier_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private CheckDeposit poController;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private int pnDetail = 0;
    private int pnAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private int pnDetailJE = 0;
    private int currentIndex = 0;
    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    Scene scene = null;
    private FileChooser fileChooser;
    Map<String, String> imageinfo_temp = new HashMap<>();
    private boolean pbEntered = false;
    private boolean pbEnteredJE = false;
    private FilteredList<ModelTableMain> filteredData;
    JFXUtil.ReloadableTableTask loadTableMain, loadTableDetail, loadTableDetailJE, loadTableAttachment;
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    private String psSearchDate = "";

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apMaster, apDetail, apAttachments;
    @FXML
    private TextField tfSearchBankAccountNo, tfSearchTransNo, tfTransactionNo, tfBankMaster, tfBankAccountNo, tfBankAccountName, tfTotal, tfCheckTransNo, tfBank, tfPayee, tfNote, tfCheckNo, tfCheckAmount, tfAttachmentNo;
    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpTransactionReferDate, dpCheckDate;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnBrowse, btnPrint, btnHistory, btnClose, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbReverse;
    @FXML
    private TableView tblViewDetail, tblAttachments;
    @FXML
    private TableColumn tblColDetailNo, tblColDetailReference, tblColDetailBank, tblColDetailPayee, tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Tab tabAttachments;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryId = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
            poController = new CashflowControllers(oApp, logwrapr).CheckDeposit();
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            poController.setTransactionStatus("0123");
            initLoadTable();
            initTextFields();
            initDatePicker();
            initDetailGrid();
            initAttachmentsGrid();
            initTableOnClick();
            initTabPane();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
            Platform.runLater(() -> {
                try {
                    poController.isCheckDepositSupplier(true);
                    poController.Master().setIndustryId(psIndustryId);
                    poController.Master().setCompany(psCompanyId);
//                    poController.setIndustryId(psIndustryId);
//                    poController.setCompanyId(psCompanyId);
//                poController.setCategoryID(psCategoryId);
//                    poController.Master().setBranchCode(oApp.getBranchCode());
                    loadRecordSearch();
                    lblSource.setText(poController.getCompanyName() + " - " + poController.Master().Industry().getDescription());
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            });
            initAttachmentPreviewPane();
            JFXUtil.initKeyClickObject(AnchorMain, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private boolean DoesContainValidDetail() {
        if (poController.getDetailCount() <= 0) {
            return false;
        }
        return true;
    }
    String lsValidDisbMessage = "Please provide at least one valid disbursement detail with amount to proceed.";

    public void initTabPane() {
        JFXUtil.onTabSelected(tabPaneMain, tabTitle -> {
            switch (tabTitle) {
                case "Check Deposit":
                    if (pnEditMode == EditMode.UNKNOWN) {
                        pnDetailJE = 0;
                    } else {
                        loadRecordMaster();
                    }
                    break;
                case "Attachments":
                    if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.ADDNEW) {
                        JFXUtil.clearTextFields(apAttachments);
                        if (DoesContainValidDetail()) {
                            try {
                                poController.loadAttachments();
                            } catch (GuanzonException | SQLException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }

//                            }
                            loadTableAttachment.reload();
                        } else {
                            JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                            ShowMessageFX.Warning(null, pxeModuleName, lsValidDisbMessage);
                        }
                    }
                    break;
            }
        });
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpTransactionReferDate, dpCheckDate);
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poController.SearchTransaction(tfSearchTransNo.getText(), tfSearchBankAccountNo.getText(), psSearchDate);
                    if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    } else {
                    }
                    JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                    pnEditMode = poController.getEditMode();
                    break;
                case "btnPrint":
                    poJSON = poController.PrintDepositSlip();
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning(null, pxeModuleName, null);
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(npe));
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                case "btnArrowRight":
                    slideImage(1);
                    break;
                case "btnArrowLeft":
                    slideImage(-1);
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered, Please contact admin to assist about the unregistered button");
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnVoid")) {
                poController.resetTransaction();
                clearTextFields();
                JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                pnEditMode = EditMode.UNKNOWN;
            }

            if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnRetrieve", "btnSearch", "btnUndo", "btnArrowRight", "btnArrowLeft", "btnHistory")) {
            } else {
                loadRecordMaster();
                loadTableDetail.reload();
                loadTableDetailJE.reload();
                loadTableAttachment.reload();
            }
            initButton(pnEditMode);
            if (lsButton.equals("btnUpdate")) {
                moveNext(false, false);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
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
                            if (poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poController.TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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
                                JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

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
                                JFXUtil.PDFViewConfig(filePath2, stackPane1, btnArrowLeft, btnArrowRight, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
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
                    stackPane1.getChildren().clear();
                    stackPane1.getChildren().add(imageView);
                    stackPane1.getChildren().addAll(btnArrowLeft, btnArrowRight);
                    Platform.runLater(() -> JFXUtil.stackPaneClip(stackPane1));
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
        }
    }

    private void loadRecordSearch() {
        tfSearchBankAccountNo.setText(poController.getSearchBankAccountNo());
        JFXUtil.updateCaretPositions(apBrowse);
    }

    private void loadRecordMaster() {
        try {
            poController.computeFields();
            JFXUtil.setStatusValue(lblStatus, CheckDepositStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.Master().getTransactionStatus());

            tfTransactionNo.setText(poController.Master().getTransactionNo());
            tfBankMaster.setText(poController.Master().BankAccount().Banks().getBankName());
            tfBankAccountNo.setText(poController.Master().BankAccount().getAccountNo());
            tfBankAccountName.setText(poController.Master().BankAccount().getAccountName());
            taRemarks.setText(poController.Master().getRemarks());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            dpTransactionReferDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Master().getTransactionReferDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotalDeposit(), false));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            if (poController.Detail(pnDetail).getSourceNo().isEmpty()) {
                JFXUtil.setDisabled(false, tfCheckTransNo, tfCheckNo, cbReverse);
            } else {
                JFXUtil.setDisabled(true, tfCheckTransNo, tfCheckNo, cbReverse);
            }
            tfCheckTransNo.setText(poController.Detail(pnDetail).Disbursement().getVoucherNo());
            tfBank.setText(poController.Detail(pnDetail).CheckPayment().Banks().getBankName());
            tfPayee.setText(poController.Detail(pnDetail).CheckPayment().Payee().getPayeeName());
            tfNote.setText(poController.Detail(pnDetail).getRemarks());
            tfCheckNo.setText(poController.Detail(pnDetail).CheckPayment().getCheckNo());
            tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).CheckPayment().getAmount(), false));
            dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.Detail(pnDetail).CheckPayment().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE)));
            cbReverse.setSelected(poController.Detail(pnDetail) != null && poController.Detail(pnDetail).isReverse());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException | NullPointerException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackPane1, imageView);
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
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
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackPane1)) {
            JFXUtil.resetImageBounds(imageView, stackPane1);
        }
    }

    public void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
    }

    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblColDetailReference,tblColDetailNo, tblColDetailDate, tblColDetailCheckNo);
        JFXUtil.setColumnLeft( tblColDetailBank, tblColDetailPayee);
        JFXUtil.setColumnRight(tblColDetailCheckAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        tblViewDetail.setItems(detail_data);
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
    }

    private void initLoadTable() {
        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                attachment_data,
                () -> {
                    imageviewerutil.scaleFactor = 1.0;
                    JFXUtil.resetImageBounds(imageView, stackPane1);
                    Platform.runLater(() -> {
                        try {
                            attachment_data.clear();
                            int lnCtr;
                            int lnCount = 0;
                            for (lnCtr = 0; lnCtr < poController.getTransactionAttachmentCount(); lnCtr++) {
                                if (RecordStatus.INACTIVE.equals(poController.TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                    continue;
                                }
                                lnCount += 1;
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                                String.valueOf(poController.TransactionAttachmentList(lnCtr).getModel().getFileName()),
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
                });

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                detail_data, () -> {
                    Platform.runLater(() -> {
                        try {
                            detail_data.clear();
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.ReloadDetail();
                            }
                            int OriginalRow = 0;
                            for (int lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                                if (!poController.Detail(lnCtr).isReverse()) {
                                    continue;
                                }
                                OriginalRow += 1;
                                String lsdate = JFXUtil.isObjectEqualTo(poController.Detail(lnCtr).CheckPayment().getCheckDate(), null, "") ? ""
                                        : CustomCommonUtil.formatDateToMMDDYYYY(poController.Detail(lnCtr).CheckPayment().getCheckDate());
                                detail_data.add(new ModelTableDetail(
                                        String.valueOf(OriginalRow),
                                        poController.Detail(lnCtr).Disbursement().getVoucherNo(),
                                        poController.Detail(lnCtr).CheckPayment().Banks().getBankName(),
                                        poController.Detail(lnCtr).CheckPayment().Payee().getPayeeName(),
                                        lsdate,
                                        poController.Detail(lnCtr).CheckPayment().getCheckNo(),
                                        CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).CheckPayment().getAmount(), false),
                                        String.valueOf(lnCtr),
                                        "", ""));
                            }
                            int lnTempRow = JFXUtil.getDetailRow(detail_data, pnDetail, 8); //this method is used only when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= detail_data.size()) {
                                if (!detail_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                    int lnRow = Integer.parseInt(detail_data.get(0).getIndex08());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewDetail, lnTempRow);
                                int lnRow = Integer.parseInt(detail_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex08());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                        }
                    });
                });
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBank":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankAccountNo("");
                        }
                        loadRecordSearch();
                        break;
                }
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = (lsTxtField.getText() == null ? "" : lsTxtField.getText());
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        if (tfNote.isFocused()) {
                            pbEntered = true;
                        }
                        CommonUtils.SetNextFocus(lsTxtField);
                        event.consume();
                        break;
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchBankAccountNo":
                                poJSON = poController.SearchBankAccount(tfSearchBankAccountNo.getText(), false, true);
                                if (!"success".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    JFXUtil.clickTabByTitleText(tabPaneMain, "Check Deposit");
                                    pnEditMode = poController.getEditMode();
                                    loadTableDetail.reload();
                                    initButton(pnEditMode);
                                }
                                break;
                            case "tfSearchTransNo":
                                loadTableMain.reload();
                                break;
                        }
                        break;
                    case UP:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNext(true, true)}
                        });
                        event.consume();
                        break;
                    case DOWN:
                        JFXUtil.altSwitch(txtFieldID, new Object[][]{
                            {new String[]{"tfCheckTransNo", "tfNote", "tfCheckNo"}, (Runnable) () -> moveNext(false, true)}
                        });
                        event.consume();
                        break;
                    default:
                        break;
                }
            } catch (SQLException | GuanzonException | ExceptionInInitializerError  ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, apBrowse);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.setKeyEventFilter(tableKeyEvents, tblViewDetail, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewDetail, tblAttachments);
    }

    public void initTableOnClick() {
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                imageviewerutil.scaleFactor = 1.0;
                int lnRow = Integer.parseInt(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex03());
                pnAttachment = lnRow;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });

        tblViewDetail.setOnMouseClicked(event -> {
            if (!detail_data.isEmpty() && event.getClickCount() == 1) {
                ModelTableDetail selected = (ModelTableDetail) tblViewDetail.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int lnRow = Integer.parseInt(detail_data.get(tblViewDetail.getSelectionModel().getSelectedIndex()).getIndex08());
                    pnDetail = lnRow;
                    loadRecordDetail();
                    moveNext(false, false);
                }
            }
        });
    }

    JFXUtil.TableKeyEvent tableKeyEvents = new JFXUtil.TableKeyEvent() {
        @Override
        protected void onRowMove(TableView<?> currentTable, String currentTableID, boolean isMovedDown) {
            int newIndex = 0;
            switch (currentTableID) {
                case "tblViewDetail":
                    if (!detail_data.isEmpty()) {
                        pnDetail = isMovedDown ? Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex08())
                                : Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex08());
                        loadRecordDetail();
                    }
                    break;
                case "tblAttachments":
                    if (attachment_data.isEmpty()) {
                        return;
                    }
                    newIndex = isMovedDown ? Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03())
                            : Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                    pnAttachment = newIndex;
                    loadRecordAttachment(true);
                    break;
            }
        }
    };

    public void moveNext(boolean isUp, boolean continueNext) {
        try {
            if (continueNext) {
                apDetail.requestFocus();
                pnDetail = isUp ? Integer.parseInt(detail_data.get(JFXUtil.moveToPreviousRow(tblViewDetail)).getIndex11())
                        : Integer.parseInt(detail_data.get(JFXUtil.moveToNextRow(tblViewDetail)).getIndex11());
            }
            loadRecordDetail();
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
                {poController.Detail(pnDetail).getRemarks(), tfCheckTransNo},
                {poController.Detail(pnDetail).CheckPayment().getCheckNo(), tfCheckNo},
                {poController.Detail(pnDetail).getRemarks(), tfNote},}, tfNote); // default
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.READY);
        boolean lbShow2 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);

        JFXUtil.setButtonsVisibility(lbShow1, btnHistory, btnPrint);
        JFXUtil.setButtonsVisibility(lbShow2, btnClose);

        JFXUtil.setDisabled(true, apMaster, apDetail, apAttachments);
        JFXUtil.setButtonsVisibility(true, btnBrowse);
    }

    private void clearTextFields() {
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
    }
}
