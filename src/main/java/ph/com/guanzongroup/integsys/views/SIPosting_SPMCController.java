/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.integsys.views;

import java.awt.image.BufferedImage;
import java.io.File;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.model.ModelJournalEntry_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.guanzon.appdriver.constant.DocumentType;
import javafx.util.Pair;
import javax.script.ScriptException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.guanzon.appdriver.constant.RecordStatus;
import ph.com.guanzongroup.cas.cashflow.status.JournalStatus;

/**
 *
 * @author Arsiela & Aldrich Team 2
 */
public class SIPosting_SPMCController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static PurchaseOrderReceivingControllers poPurchaseReceivingController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private static final int ROWS_PER_PAGE = 50;
    int pnJEDetail = 0;
    int pnDetail = 0;
    int pnMain = 0;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psBranchId = "";
    private String psSearchSupplierId = "";
    private String psSearchBranchId = "";
    private String openedAttachment = "";
    private boolean pbEntered = false;

    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private ObservableList<ModelJournalEntry_Detail> JEdetails_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    boolean lbSelectTabJE = false;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    private Stage dialogStage = null;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apJEMaster, apJEDetail, apAttachments;
    @FXML
    private HBox hbButtons;
    @FXML
    private Label lblSource, lblStatus, lblJEStatus;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnPost, btnHistory, btnRetrieve, btnClose, btnArrowLeft, btnArrowRight;
    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfSearchReceiveBranch, tfTransactionNo, tfSupplier, tfBranch, tfTrucking, tfReferenceNo,
            tfSINo, tfTerm, tfDiscountRate, tfDiscountAmount, tfFreightAmt, tfVatSales, tfVatAmount, tfZeroVatSales, tfVatExemptSales, tfNetTotal, tfVatRate,
            tfTransactionTotal, tfOrderNo, tfBarcode, tfDescription, tfSupersede, tfOrderQuantity, tfReceiveQuantity, tfCost, tfDiscRateDetail,
            tfAddlDiscAmtDetail, tfSRPAmount, tfJETransactionNo, tfJEAcctCode, tfJEAcctDescription, tfCreditAmt, tfDebitAmt, tfTotalCreditAmt,
            tfTotalDebitAmt, tfAttachmentNo, tfAdvancePayment;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpJETransactionDate, dpReportMonthYear, dpSIDate;
    @FXML
    private CheckBox cbVatInclusive, cbVatable;
    @FXML
    private TextArea taRemarks, taJERemarks;
    @FXML
    private TabPane tabPaneForm;
    @FXML
    private Tab tabSIPosting, tabJE, tabAttachments;
    @FXML
    private TableView tblViewTransDetailList, tblViewMainList, tblViewJEDetails, tblAttachments;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail,
            tblTotalDetail, tblRowNo, tblSupplier, tblDate, tblReferenceNo, tblJERowNoDetail, tblReportMonthDetail, tblJEAcctCodeDetail, tblJEAcctDescriptionDetail,
            tblJECreditAmtDetail, tblJEDebitAmtDetail, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private Pagination pgPagination;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poPurchaseReceivingController = new PurchaseOrderReceivingControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }

        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initJEDetailsGrid();
        initAttachmentsGrid();
        initTableOnClick();
        initTabSelection();
        clearTextFields();

        Platform.runLater(() -> {
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCategoryId(psCategoryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().isFinance(true);
            poPurchaseReceivingController.PurchaseOrderReceiving().initFields();
            poPurchaseReceivingController.PurchaseOrderReceiving().setWithUI(true);
            loadRecordSearch();

            TriggerWindowEvent();
        });

        initAttachmentPreviewPane();

        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
    }

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryId = "02";
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }
    ChangeListener<Scene> WindowKeyEvent = (obs, oldScene, newScene) -> {
        if (newScene != null) {
            setKeyEvent(newScene);
        }
    };

    public void TriggerWindowEvent() {
        root = (AnchorPane) apMainAnchor;
        scene = root.getScene();
        if (scene != null) {
            setKeyEvent(scene);
        } else {
            root.sceneProperty().addListener(WindowKeyEvent);
        }
    }

    public void RemoveWindowEvent() {
        root.sceneProperty().removeListener(WindowKeyEvent);
        scene.setOnKeyPressed(null);
        stageAttachment.closeDialog();
    }

    private void populateJE() {
        try {
            JSONObject pnJSON = new JSONObject();
            JFXUtil.setValueToNull(dpJETransactionDate, dpReportMonthYear);
            JFXUtil.clearTextFields(apJEMaster, apJEDetail);
            pnJSON = poPurchaseReceivingController.PurchaseOrderReceiving().populateJournal();
            if (JFXUtil.isJSONSuccess(pnJSON)) {
                loadTableJEDetail();
            } else {
                lblJEStatus.setText("UNKNOWN");
                JEdetails_data.clear();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initTabSelection() {
        tabJE.setOnSelectionChanged(event -> {
            if (tabJE.isSelected()) {
                lbSelectTabJE = true;
                populateJE();
            }
        });
    }

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                System.out.println("tested key press");

                if (JFXUtil.isObjectEqualTo(poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode(), EditMode.READY, EditMode.UPDATE)) {
                    showAttachmentDialog();
                }
            }
        }
        );
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                if (newNode instanceof Button) {
                } else {
                    lastFocusedTextField.set(newNode);
                    previousSearchedTextField.set(null);
                }
            }
        });
    }

    public void showAttachmentDialog() {
        poJSON = new JSONObject();
        stageAttachment.closeDialog();
        openedAttachment = "";
        if (poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount() <= 0) {
            ShowMessageFX.Warning(null, pxeModuleName, "No transaction attachment to load.");
            return;
        }
        openedAttachment = poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getTransactionNo();
        Map<String, Pair<String, String>> data = new HashMap<>();
        data.clear();
        int lnCount = 0;
        for (int lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount(); lnCtr++) {
            if (RecordStatus.INACTIVE.equals(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                continue;
            }
            lnCount += 1;
            data.put(String.valueOf(lnCount), new Pair<>(String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName()),
                    poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getDocumentType()));
        }
        AttachmentDialogController controller = new AttachmentDialogController();
        controller.setOpenedImage(pnAttachment);
        controller.addData(data);

        try {
            stageAttachment.showDialog((Stage) btnClose.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/AttachmentDialog.fxml"), controller, "Attachment Dialog", false, false, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbVatInclusive":
                    poPurchaseReceivingController.PurchaseOrderReceiving().Master().isVatTaxable(cbVatInclusive.isSelected());
                    //update all detail base on vat inclusive value
                    for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1; lnCtr++) {
                        poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).isVatable(cbVatInclusive.isSelected());
                    }
                    loadTableDetail();
                    loadRecordMaster();
                    break;
                case "cbVatable":
                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).isVatable(cbVatable.isSelected());
                    loadRecordMaster();
                    break;
            }
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":

                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            stageAttachment.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField.get() != null)) {
                            if (lastFocusedTextField.get() instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField.get();
                                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", apBrowse, apMaster, apDetail,
                                        apJEMaster, apJEDetail).contains(tf.getId())) {
                                    if (lastFocusedTextField.get() == previousSearchedTextField.get()) {
                                        break;
                                    }
                                    previousSearchedTextField.set(lastFocusedTextField.get());
                                    // Create a simulated KeyEvent for F3 key press
                                    JFXUtil.makeKeyPressed(tf, KeyCode.F3);
                                } else {
                                    ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                                }
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                            }
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                        }
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        //Retrieve data from purchase order to table main
                        retrievePOR();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().ConfirmTransaction("Confirmed");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }

                        break;
                    case "btnPost":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to post transaction?") == true) {
                            if (!lbSelectTabJE) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Please review and verify all Journal Entry details before posting the transaction.");
                                return;
                            }

                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().PostTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
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
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel", "btnPost")) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().resetMaster();
                    poPurchaseReceivingController.PurchaseOrderReceiving().resetOthers();
                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail().clear();
                    poPurchaseReceivingController.PurchaseOrderReceiving().resetJournal();
                    imageView.setImage(null);
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                    poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
                    loadTableAttachment();

                    Tab currentTab = tabPaneForm.getSelectionModel().getSelectedItem();
                    if (currentTab.getId().equals("tabJE")) {
                        populateJE();
                    }
                }
                initButton(pnEditMode);
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void retrievePOR() {
        poJSON = new JSONObject();
        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().loadUnPostPurchaseOrderReceiving(tfSearchSupplier.getText(), tfSearchReceiveBranch.getText(), tfSearchReferenceNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain();
        }
    }

    final ChangeListener<? super Boolean> txtMaster_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());

        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfTerm":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTermCode("");
                    }
                    break;
                case "tfReferenceNo":
                    if (!lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceNo(lsValue);
                    } else {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceNo("");
                    }
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfReferenceNo.setText("");
                        break;
                    }
                    break;
                case "tfSINo":
                    if (!lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSalesInvoice(lsValue);
                    } else {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSalesInvoice("");
                    }
                    loadTableDetail();
                    loadRecordMaster();
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfSINo.setText("");
                        break;
                    }
                    break;
                case "tfDiscountRate":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscountRate((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
                case "tfDiscountAmount":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscount(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
                case "tfFreightAmt":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    if (Double.valueOf(lsValue.replace(",", "")) > poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionTotal().doubleValue()) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid freight amount");
                        break;
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setFreight(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
                case "tfTaxAmount":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setWithHoldingTax(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
                case "tfVatRate":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    if (Double.valueOf(lsValue.replace(",", "")) > 100.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid vat rate.");
                        break;
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setVatRate((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    break;
//                case "tfTotalCreditAmt":
//                    break;
//                case "tfTotalDebitAmt":
//                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsTxtFieldID, "tfTotalCreditAmt", "tfTotalDebitAmt")) {
                loadRecordJEMaster();
            } else {
                loadRecordMaster();
            }
        }
    };
    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtField.getId());
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            /*Lost Focus*/
            lsValue = lsValue.trim();
            switch (lsID) {
                case "taRemarks"://Remarks
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setRemarks(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    loadRecordMaster();
                    break;
                case "taJERemarks":
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().setRemarks(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    loadRecordJEMaster();
                    break;
            }
        } else {
            txtField.selectAll();
        }
    };
    // Method to handle focus change and track the last focused TextField
    final ChangeListener<? super Boolean> txtDetail_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/

            switch (lsTxtFieldID) {
                case "tfCost":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    double lnNewVal = Double.valueOf(lsValue);
                    double lnOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue();

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    if (pbEntered) {
                        if (lnNewVal != lnOldVal) {
                            if ((Double.valueOf(lsValue) > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
                                moveNext();
                            } else {
                                moveNext();
                            }
                        } else {
                            moveNext();
                        }
                        pbEntered = false;
                    }
                    break;
                case "tfDiscRateDetail":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    if (Double.valueOf(lsValue.replace(",", "")) > 100.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid discount rate.");
                        break;
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setDiscountRate((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "tfAddlDiscAmtDetail":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.0000";
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setDiscountAmount((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "tfJEAcctCode":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setAccountCode(lsValue);
                    }
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "tfJEAcctDescription":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setAccountCode(lsValue);
                    }
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "tfCreditAmt":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.0000";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    lnNewVal = Double.valueOf(lsValue);
                    lnOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getCreditAmount();

                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getDebitAmount() > 0.0000 && Double.valueOf(lsValue) > 0) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                        poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setCreditAmount(0.0000);
                        tfCreditAmt.setText("0.0000");
                        tfCreditAmt.requestFocus();
                        break;
                    } else {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setCreditAmount((Double.valueOf(lsValue)));
                    }
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }

                    if (pbEntered && lnNewVal > 0) { //unique
                        if (lnNewVal != lnOldVal) {
                            if ((Double.valueOf(lsValue) > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode()))) {
                                moveNextJE(false);
                            } else {
                                moveNextJE(false);
                            }
                        } else {
                            moveNextJE(false);
                        }
                        pbEntered = false;
                    }
                    break;
                case "tfDebitAmt":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.0000";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    lnNewVal = Double.valueOf(lsValue);
                    lnOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getDebitAmount();
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getCreditAmount() > 0.0000 && Double.valueOf(lsValue) > 0) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Debit and credit amounts cannot both have values at the same time.");
                        poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setDebitAmount(0.0000);
                        tfDebitAmt.setText("0.0000");
                        tfDebitAmt.requestFocus();
                        break;
                    } else {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setDebitAmount((Double.valueOf(lsValue)));
                    }
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    if (pbEntered) {
                        if (lnNewVal != lnOldVal) {
                            if ((Double.valueOf(lsValue) > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode()))) {
                                moveNextJE(false);
                            } else {
                                moveNextJE(false);
                            }
                        } else {
                            moveNextJE(false);
                        }
                        pbEntered = false;
                    }
                    break;
            }
            if (JFXUtil.isObjectEqualTo(lsTxtFieldID, "tfJEAcctCode", "tfJEAcctDescription", "tfCreditAmt", "tfDebitAmt")) {
                Platform.runLater(() -> {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                    delay.setOnFinished(event -> {
                        loadTableJEDetail();
                    });
                    delay.play();
                });
            } else {
                Platform.runLater(() -> {
                    PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                    delay.setOnFinished(event -> {
                        loadTableDetail();
                    });
                    delay.play();
                });
            }
        }
    };

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSearchSupplier":
                    if (lsValue.equals("")) {
                        psSearchSupplierId = "";
                    }
                    break;
                case "tfSearchReceiveBranch":
                    if (lsValue.equals("")) {
                        psSearchBranchId = "";
                    }
                    break;
                case "tfSearchReferenceNo":
                    break;
            }
//            if (JFXUtil.isObjectEqualTo(lsTxtFieldID, "tfSearchSupplier", "tfSearchReceiveBranch", "tfSearchReferenceNo")) {
//                loadRecordSearch();
//            }
        }
    };

    public void moveNext() {
        if (poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() <= 0) {
            return;
        }
        double lnReceiveQty = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
        apDetail.requestFocus();
        double lnNewvalue = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
        if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
            tfCost.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewTransDetailList);
            loadRecordDetail();
            tfOrderNo.setText("");
            tfCost.requestFocus();
        }
    }

    public void moveNextJE(boolean isUp) {
        apJEDetail.requestFocus();
        pnJEDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewJEDetails) : JFXUtil.moveToNextRow(tblViewJEDetails);
        loadRecordJEDetail();
        if (JFXUtil.isObjectEqualTo(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode(), null, "")) {
            tfJEAcctCode.requestFocus();
        } else {
            if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getCreditAmount() > 0) {
                tfCreditAmt.requestFocus();
            } else {
                tfDebitAmt.requestFocus();
            }
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfCost":
                        case "tfDiscRateDetail":
                        case "tfAddlDiscAmtDetail":
                            double lnReceiveQty = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
                            apDetail.requestFocus();
                            double lnNewvalue = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
                            if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
                                tfCost.requestFocus();
                            } else {
                                pnDetail = JFXUtil.moveToPreviousRow(tblViewTransDetailList);
                                loadRecordDetail();
                                tfCost.requestFocus();
                                event.consume();
                            }
                            break;
                        case "tfJEAcctCode":
                        case "tfCreditAmt":
                        case "tfDebitAmt":
                            //focus if either credit or debit
                            // Debit is default to focus
                            moveNextJE(true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfCost":
                        case "tfDiscRateDetail":
                        case "tfAddlDiscAmtDetail":
                            moveNext();
                            event.consume();
                            break;
                        case "tfJEAcctCode":
                        case "tfCreditAmt":
                        case "tfDebitAmt":
                            moveNextJE(false);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchSupplier":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                psSearchSupplierId = "";
                                break;
                            } else {
                                psSearchSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                            }
                            loadRecordSearch();
                            retrievePOR();
                            return;
                        case "tfSearchReceiveBranch":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchBranch(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                psSearchBranchId = "";
                                break;
                            } else {
                                psSearchBranchId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getBranchCode();
                            }
                            loadRecordSearch();
                            retrievePOR();
                            return;
                        case "tfSearchReferenceNo":
                            retrievePOR();
                            return;
                        case "tfTerm":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTerm.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfJEAcctCode":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().SearchAccountCode(pnJEDetail, lsValue, true, poPurchaseReceivingController.PurchaseOrderReceiving().Master().getIndustryId(), null);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfJEAcctCode.setText("");
                                break;
                            }

                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().checkExistAcctCode(pnJEDetail, poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode());
                            if ("error".equals(poJSON.get("result"))) {
                                int lnRow = (int) poJSON.get("row");
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnJEDetail != lnRow) {
                                    pnJEDetail = lnRow;
                                    loadTableJEDetail();
                                    return;
                                }
                                break;
                            }
                            loadTableJEDetail();
                            break;
                        case "tfJEAcctDescription":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().SearchAccountCode(pnJEDetail, lsValue, false, poPurchaseReceivingController.PurchaseOrderReceiving().Master().getIndustryId(), null);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfJEAcctCode.setText("");
                                break;
                            }

                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().checkExistAcctCode(pnJEDetail, poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode());
                            if ("error".equals(poJSON.get("result"))) {
                                int lnRow = (int) poJSON.get("row");
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnJEDetail != lnRow) {
                                    pnJEDetail = lnRow;
                                    loadTableJEDetail();
                                    return;
                                }
                                break;
                            }
                            loadTableJEDetail();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null;
                LocalDate transactionDate = null;
                LocalDate referenceDate = null;
                LocalDate selectedDate = null;
                String lsServerDate = "";
                String lsTransDate = "";
                String lsRefDate = "";
                String lsSelectedDate = "";

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                lsServerDate = sdfFormat.format(oApp.getServerDate());
                lsTransDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
                lsRefDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
                lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        break;
                    case "dpReferenceDate":
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Reference date cannot be later than the receiving date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                        }
                        break;
                    case "dpSIDate":
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isAfter(transactionDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "SI date cannot be later than the receiving date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSalesInvoiceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                        }
                        break;
                    case "dpReportMonthYear":
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            if (pbSuccess) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).setForMonthOf((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (pbSuccess) {
                } else {
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                }
                pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                if (JFXUtil.isObjectEqualTo(datePicker.getId(), "dpJETransactionDate", "dpReportMonthYear")) {
                    loadTableJEDetail();
                } else {
                    loadRecordMaster();
                }
                pbSuccess = true; //Set to original valueF
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableMain() {
        // Setting data to table detail
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewMainList.setPlaceholder(loading.loadingPane);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
//                Thread.sleep(1000);
                Platform.runLater(() -> {
                    main_data.clear();
                    JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);

                    if (poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() > 0) {
                        //pending
                        //retreiving using column index
                        for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelDeliveryAcceptance_Main(String.valueOf(lnCtr + 1),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).Supplier().getCompanyName()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionDate()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getReferenceNo())
                                ));
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }

                            if (poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionStatus().equals(PurchaseOrderReceivingStatus.POSTED)) {
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "C1E1C1", highlightedRowsMain);
                            }
                        }
                    }

                    if (pnMain < 0 || pnMain
                            >= main_data.size()) {
                        if (!main_data.isEmpty()) {
                            /* FOCUS ON FIRST ROW */
                            JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                        }
                    } else {
                        /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                        JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                    }
                    JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewMainList.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewMainList.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewMainList.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poPurchaseReceivingController.PurchaseOrderReceiving().Master().Industry().getDescription());
            tfSearchSupplier.setText(psSearchSupplierId.equals("") ? "" : poPurchaseReceivingController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfSearchReceiveBranch.setText(psSearchBranchId.equals("") ? "" : poPurchaseReceivingController.PurchaseOrderReceiving().Master().Branch().getBranchName());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex01());
                String lsAttachmentType = poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
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
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getImagePath() != null && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getImagePath())) {
                                filePath2 = poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getImagePath() + "/" + (String) attachment_data.get(tblAttachments.getSelectionModel().getSelectedIndex()).getIndex02();
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
                                PDDocument document = PDDocument.load(new File(filePath2));
                                PDFRenderer renderer = new PDFRenderer(document);
                                int pageCount = document.getNumberOfPages();

                                // Container for PDF pages
                                VBox pdfContainer = new VBox(10);
                                pdfContainer.setAlignment(Pos.CENTER); // center pages
                                pdfContainer.setPrefWidth(imageviewerutil.ldstackPaneWidth);

                                for (int i = 0; i < pageCount; i++) {
                                    BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);
                                    Image fxImage = SwingFXUtils.toFXImage(pageImage, null);
                                    ImageView pageView = new ImageView(fxImage);

                                    pageView.setPreserveRatio(true);
                                    pageView.setFitWidth(imageviewerutil.ldstackPaneWidth);
                                    JFXUtil.adjustImageSize(fxImage, pageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);

                                    pdfContainer.getChildren().add(pageView);
                                }

                                // Wrap VBox in a Group for scaling
                                Group pdfGroup = new Group(pdfContainer);

                                // Wrap Group in a StackPane to center content
                                StackPane centerPane = new StackPane(pdfGroup);
                                centerPane.setAlignment(Pos.CENTER);

                                // ScrollPane wraps the centerPane
                                ScrollPane scrollPane = new ScrollPane(centerPane);
                                scrollPane.setPannable(true);
                                scrollPane.setFitToWidth(true);
                                scrollPane.setFitToHeight(true);

                                // Stack PDF and buttons
                                stackPane1.getChildren().setAll(scrollPane, btnArrowLeft, btnArrowRight);
                                StackPane.setAlignment(btnArrowLeft, Pos.CENTER_LEFT);
                                StackPane.setAlignment(btnArrowRight, Pos.CENTER_RIGHT);
                                StackPane.setMargin(btnArrowLeft, new Insets(0, 0, 0, 10));
                                StackPane.setMargin(btnArrowRight, new Insets(0, 10, 0, 0));

                                PauseTransition delay = new PauseTransition(Duration.seconds(2)); // 2-second delay
                                delay.setOnFinished(event -> {
                                    Platform.runLater(() -> {
                                        JFXUtil.stackPaneClip(stackPane1);
                                    });
                                });
                                delay.play();
                                document.close();

                                // ----- ZOOM & PAN -----
                                final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
                                scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
                                    if (event.isControlDown()) {
                                        event.consume(); // stop default scroll behavior

                                        double delta = event.getDeltaY() > 0 ? 1.1 : 0.9; // multiplier for smooth zoom in/out
                                        double oldZoom = zoomFactor.get();
                                        zoomFactor.set(oldZoom * delta); // scale by multiplier

                                        // Apply scale
                                        pdfGroup.setScaleX(zoomFactor.get());
                                        pdfGroup.setScaleY(zoomFactor.get());

                                        // Keep mouse position centered during zoom
                                        Bounds viewportBounds = scrollPane.getViewportBounds();
                                        Bounds contentBounds = pdfGroup.getBoundsInParent();
                                        double mouseX = event.getX();
                                        double mouseY = event.getY();

                                        double hRatio = (scrollPane.getHvalue() * (contentBounds.getWidth() - viewportBounds.getWidth()) + mouseX) / contentBounds.getWidth();
                                        double vRatio = (scrollPane.getVvalue() * (contentBounds.getHeight() - viewportBounds.getHeight()) + mouseY) / contentBounds.getHeight();

                                        Platform.runLater(() -> {
                                            Bounds newBounds = pdfGroup.getBoundsInParent();
                                            double newH = (hRatio * newBounds.getWidth() - mouseX) / (newBounds.getWidth() - viewportBounds.getWidth());
                                            double newV = (vRatio * newBounds.getHeight() - mouseY) / (newBounds.getHeight() - viewportBounds.getHeight());

                                            scrollPane.setHvalue(Double.isNaN(newH) ? 0.5 : Math.min(Math.max(0, newH), 1.0));
                                            scrollPane.setVvalue(Double.isNaN(newV) ? 0.5 : Math.min(Math.max(0, newV), 1.0));
                                        });
                                    }
                                });

                                // Pan with mouse drag
                                final ObjectProperty<Point2D> lastMouse = new SimpleObjectProperty<>();
                                pdfGroup.setOnMousePressed(e -> lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY())));

                                pdfGroup.setOnMouseDragged(e -> {
                                    if (lastMouse.get() != null) {
                                        double deltaX = e.getSceneX() - lastMouse.get().getX();
                                        double deltaY = e.getSceneY() - lastMouse.get().getY();

                                        pdfGroup.setTranslateX(pdfGroup.getTranslateX() + deltaX);
                                        pdfGroup.setTranslateY(pdfGroup.getTranslateY() + deltaY);

                                        lastMouse.set(new Point2D(e.getSceneX(), e.getSceneY()));
                                    }
                                });
                                pdfGroup.setOnMouseReleased(e -> lastMouse.set(null));
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
        } catch (Exception e) {
        }
    }

    public void loadRecordJEDetail() {
        try {
            //DISABLING
            if (!JFXUtil.isObjectEqualTo(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode(), null, "")) {
                JFXUtil.setDisabled(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getEditMode() != EditMode.ADDNEW, tfJEAcctCode, tfJEAcctDescription);
            } else {
                JFXUtil.setDisabled(false, tfJEAcctCode, tfJEAcctDescription);
            }

            tfJEAcctCode.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode());
            tfJEAcctDescription.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).Account_Chart().getDescription());
            String lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getForMonthOf());
            dpReportMonthYear.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd"));
            tfCreditAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getCreditAmount(), true));
            tfDebitAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getDebitAmount(), true));
            JFXUtil.updateCaretPositions(apJEDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) {
                return;
            }
            tfBarcode.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Supersede().getBarCode());
            tfOrderNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo());
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
            Platform.runLater(() -> {
                double lnValue = 0.00;
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getDiscountRate() != null) {
                    lnValue = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getDiscountRate().doubleValue();
                }
                tfDiscRateDetail.setText(String.format("%.2f", Double.isNaN(lnValue) ? 0.00 : lnValue));
            });
            double ldblDiscountRate = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue()
                    * (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getDiscountRate().doubleValue() / 100);
            tfAddlDiscAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getDiscountAmount(), true));
            double lnTotal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue()
                    + poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getDiscountAmount().doubleValue()
                    + ldblDiscountRate;
            tfSRPAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true));
            tfOrderQuantity.setText(String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().intValue()));
            tfReceiveQuantity.setText(String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue()));
            cbVatable.setSelected(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).isVatable());

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordJEMaster() {
        JFXUtil.setStatusValue(lblJEStatus, JournalStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().getTransactionStatus());
        if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().getTransactionNo() != null) {
            tfJETransactionNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().getTransactionNo());
            String lsJETransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().getTransactionDate());
            dpJETransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsJETransactionDate, "yyyy-MM-dd"));

            taJERemarks.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Master().getRemarks());
            double lnTotalDebit = 0;
            double lnTotalCredit = 0;
            for (int lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount(); lnCtr++) {
                lnTotalDebit += poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getDebitAmount();
                lnTotalCredit += poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getCreditAmount();
            }

            tfTotalCreditAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalCredit, true));
            tfTotalDebitAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotalDebit, true));
            JFXUtil.updateCaretPositions(apJEMaster);
        }
    }

    public void loadRecordMaster() {
        try {
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSupplierId(psSupplierId);
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setBranchCode(psBranchId);
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(PurchaseOrderReceivingStatus.POSTED, "POSTED");
                statusMap.put(PurchaseOrderReceivingStatus.PAID, "PAID");
                statusMap.put(PurchaseOrderReceivingStatus.CONFIRMED, "CONFIRMED");
                statusMap.put(PurchaseOrderReceivingStatus.OPEN, "OPEN");
                statusMap.put(PurchaseOrderReceivingStatus.RETURNED, "RETURNED");
                statusMap.put(PurchaseOrderReceivingStatus.VOID, "VOIDED");
                statusMap.put(PurchaseOrderReceivingStatus.CANCELLED, "CANCELLED");

                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN");
                lblStatus.setText(lsStat);
            });

            if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue() > 0.00) {
                poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
            } else {
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue() > 0.00) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
                }
            }

            poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();

            tfTransactionNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfSupplier.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfBranch.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Branch().getBranchName());
            tfTrucking.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Trucking().getCompanyName());

            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
            dpReferenceDate.setValue(JFXUtil.isObjectEqualTo(lsReferenceDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));
            tfReferenceNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceNo());
            String lsSIDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSalesInvoiceDate());
            dpSIDate.setValue(JFXUtil.isObjectEqualTo(lsSIDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsSIDate, "yyyy-MM-dd"));
            tfSINo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSalesInvoice());
            tfTerm.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Term().getDescription());
            taRemarks.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getRemarks());
//
//            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
//                    poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionTotal().doubleValue(), true));

            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(getGrossTotal(), true));
            Platform.runLater(() -> {
                double lnValue = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue();
                tfDiscountRate.setText(String.format("%.2f", Double.isNaN(lnValue) ? 0.00 : lnValue));
                double lnVat = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getVatRate().doubleValue();
                tfVatRate.setText(String.format("%.2f", Double.isNaN(lnVat) ? 0.00 : lnVat));
            });
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue(), true));
            tfFreightAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getFreight().doubleValue()));

            cbVatInclusive.setSelected(poPurchaseReceivingController.PurchaseOrderReceiving().Master().isVatTaxable());
            tfVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getVatSales().doubleValue(), true));
            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getVatAmount().doubleValue(), true));
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getZeroVatSales().doubleValue(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getVatExemptSales().doubleValue(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().getNetTotal(), true));
            tfAdvancePayment.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().getAdvancePayment(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private double getGrossTotal() {
        double ldblGrossTotal = 0.0000;
        for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1; lnCtr++) {
            ldblGrossTotal = ldblGrossTotal + (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue()
                    * poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue());
        }
        return ldblGrossTotal;
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelDeliveryAcceptance_Main selected = (ModelDeliveryAcceptance_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                lbSelectTabJE = false;

                psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                psBranchId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getBranchCode();
            }

            poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
            if (poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount() > 1) {
                if (!openedAttachment.equals(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo())) {
                    stageAttachment.closeDialog();
                }
            } else {
                stageAttachment.closeDialog();
            }

            Platform.runLater(() -> {
                loadTableDetail();
            });
            tfAttachmentNo.clear();
            cmbAttachmentType.setItems(documentType);

            imageView.setImage(null);
            JFXUtil.stackPaneClip(stackPane1);
            Platform.runLater(() -> {
                loadTableAttachment();
            });

            if (dialogStage != null) {
                if (dialogStage.isShowing()) {
                    dialogStage.close();
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadTableJEDetail() {
//        pbEntered = false;
        // Setting data to table detail

        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewJEDetails.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    JEdetails_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getAccountCode() == null || poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getAccountCode().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }
                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1).getAccountCode() != null
                                        && !poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1).getAccountCode().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Journal().AddDetail();
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1).setForMonthOf(oApp.getServerDate());
                                }
                            }
                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1) < 0) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Journal().AddDetail();
                                poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount() - 1).setForMonthOf(oApp.getServerDate());
                            }
                        }
                        String lsReportMonthYear = "";
                        String lsAcctCode = "";
                        String lsAccDesc = "";
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().Journal().getDetailCount(); lnCtr++) {
                            lsReportMonthYear = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getForMonthOf());
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getAccountCode() != null) {
                                lsAcctCode = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getAccountCode();
                            }
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).Account_Chart().getDescription() != null) {
                                lsAccDesc = poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).Account_Chart().getDescription();
                            }

                            JEdetails_data.add(
                                    new ModelJournalEntry_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(CustomCommonUtil.parseDateStringToLocalDate(lsReportMonthYear, "yyyy-MM-dd")),
                                            String.valueOf(lsAcctCode),
                                            String.valueOf(lsAccDesc),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getCreditAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(lnCtr).getDebitAmount(), true))) //identify total
                            );

                            lsReportMonthYear = "";
                            lsAcctCode = "";
                            lsAccDesc = "";
                        }
                        if (pnJEDetail < 0 || pnJEDetail
                                >= JEdetails_data.size()) {
                            if (!JEdetails_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewJEDetails, 0);
                                pnJEDetail = tblViewJEDetails.getSelectionModel().getSelectedIndex();
                                loadRecordJEDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewJEDetails, pnJEDetail);
                            loadRecordJEDetail();
                        }
                        loadRecordJEMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (JEdetails_data == null || JEdetails_data.isEmpty()) {
                    tblViewJEDetails.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewJEDetails.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (JEdetails_data == null || JEdetails_data.isEmpty()) {
                    tblViewJEDetails.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail
        JFXUtil.disableAllHighlight(tblViewTransDetailList, highlightedRowsDetail);

        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewTransDetailList.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    try {
                        double lnTotal = 0.00;
                        double lnDiscountAmt = 0.00;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                            if (JFXUtil.isObjectEqualTo(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSalesInvoice(), null, "")) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).isVatable(false);
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().isVatTaxable(false);
                                JFXUtil.setDisabled(true, cbVatable, cbVatInclusive);
                            } else {
                                JFXUtil.setDisabled(false, cbVatable, cbVatInclusive);
                            }

                            try {
                                lnTotal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue();
                                lnDiscountAmt = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getDiscountAmount().doubleValue()
                                        + (lnTotal * (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getDiscountRate().doubleValue() / 100));
                            } catch (Exception e) {
                            }

                            if ((!poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().intValue() != poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue()
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue() != 0) {
                                JFXUtil.highlightByKey(tblViewTransDetailList, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                            }

                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() != null && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId())) {
                                details_data.add(
                                        new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().intValue()),
                                                String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue()),
                                                //                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnDiscountAmt, true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                        ));
                            }
                        }
                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewTransDetailList, 0);
                                pnDetail = tblViewTransDetailList.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewTransDetailList, pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewTransDetailList.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    private void loadTableAttachment() {
        imageviewerutil.scaleFactor = 1.0;
        JFXUtil.resetImageBounds(imageView, stackPane1);
        // Setting data to table detail
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblAttachments.setPlaceholder(loading.loadingPane);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                Platform.runLater(() -> {
                    try {
                        attachment_data.clear();
                        int lnCtr;
                        int lnCount = 0;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount(); lnCtr++) {
                            if (RecordStatus.INACTIVE.equals(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getRecordStatus())) {
                                continue;
                            }
                            lnCount += 1;
                            attachment_data.add(
                                    new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCount),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName()),
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
                    tblAttachments.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblAttachments.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (attachment_data == null || attachment_data.isEmpty()) {
                    tblAttachments.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
                dpTransactionDate, dpReferenceDate, dpJETransactionDate, dpReportMonthYear, dpSIDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate, dpJETransactionDate, dpReportMonthYear, dpSIDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtField_Focus, tfSearchReceiveBranch, tfSearchSupplier, tfSearchReferenceNo);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks, taJERemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfReferenceNo, tfSINo, tfTerm, tfDiscountRate, tfDiscountAmount, tfVatRate, tfFreightAmt,
                tfVatSales, tfZeroVatSales, tfVatAmount, tfVatExemptSales, tfTotalCreditAmt, tfTotalDebitAmt);
        JFXUtil.setFocusListener(txtDetail_Focus, tfCost, tfDiscRateDetail, tfAddlDiscAmtDetail, tfSRPAmount,
                tfJEAcctCode, tfJEAcctDescription, tfCreditAmt, tfDebitAmt);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail, apJEDetail, apBrowse);

        JFXUtil.setCommaFormatter(tfDiscountAmount, tfFreightAmt, tfVatSales,
                tfVatAmount, tfZeroVatSales, tfVatExemptSales, tfCost, tfCreditAmt,
                tfDebitAmt, tfAddlDiscAmtDetail, tfSRPAmount);

        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity, tfSINo);
        CustomCommonUtil.inputDecimalOnly(tfDiscountRate, tfDiscRateDetail, tfVatRate);
        // Combobox
        JFXUtil.initComboBoxCellDesignColor(cmbAttachmentType, "#FF8201");
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.setCheckboxHoverCursor(apMaster, apDetail);
    }

    public void initTableOnClick() {
        tblViewJEDetails.setOnMouseClicked(event -> {
            ModelJournalEntry_Detail selected = (ModelJournalEntry_Detail) tblViewJEDetails.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pnJEDetail = Integer.parseInt(selected.getIndex01()) - 1;
                loadRecordJEDetail();
                if (JFXUtil.isObjectEqualTo(poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getAccountCode(), null, "")) {
                    tfJEAcctCode.requestFocus();
                } else {
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Journal().Detail(pnJEDetail).getCreditAmount() > 0) {
                        tfCreditAmt.requestFocus();
                    } else {
                        tfDebitAmt.requestFocus();
                    }
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
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });
        tblViewTransDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewTransDetailList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        tfCost.requestFocus();
                    }
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    tfOrderNo.setText("");
                    loadTableDetailFromMain();
                    pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDeliveryAcceptance_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.applyRowHighlighting(tblViewTransDetailList, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetailList, tblViewJEDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblViewTransDetailList, tblAttachments, tblViewJEDetails);
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(false, btnPost);

        //Unkown || Ready
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apAttachments, apJEMaster, apJEDetail);

        switch (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(lbShow3, btnPost);

                break;
            case PurchaseOrderReceivingStatus.POSTED:
            case PurchaseOrderReceivingStatus.PAID:
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
            case PurchaseOrderReceivingStatus.RETURNED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
        }
    }

    private void initAttachmentPreviewPane() {
        imageviewerutil.initAttachmentPreviewPane(stackPane1, imageView);
        stackPane1.heightProperty().addListener((observable, oldValue, newHeight) -> {
            double computedHeight = newHeight.doubleValue();
            imageviewerutil.ldstackPaneHeight = computedHeight;
            loadTableAttachment();
            loadRecordAttachment(true);
        });
    }

    public void initAttachmentsGrid() {
        /*FOCUS ON FIRST ROW*/
        JFXUtil.setColumnCenter(tblRowNoAttachment, tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
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

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail);
        JFXUtil.setColumnLeft(tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetailList);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetailList.comparatorProperty());
        tblViewTransDetailList.setItems(sortedData);
        tblViewTransDetailList.autosize();
    }

    public void initJEDetailsGrid() {
        JFXUtil.setColumnCenter(tblJERowNoDetail, tblReportMonthDetail);
        JFXUtil.setColumnLeft(tblJEAcctCodeDetail, tblJEAcctDescriptionDetail);
        JFXUtil.setColumnRight(tblJECreditAmtDetail, tblJEDebitAmtDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewJEDetails);

        tblViewJEDetails.setItems(JEdetails_data);
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblViewTransDetailList":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnDetail = JFXUtil.moveToNextRow(currentTable);
                                break;
                            case UP:
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                break;

                            default:
                                break;
                        }
                        loadRecordDetail();
                        event.consume();
                    }
                    break;
                case "tblAttachments":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnAttachment = Integer.parseInt(attachment_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex03());
                                break;
                            case UP:
                                pnAttachment = Integer.parseInt(attachment_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex03());
                                break;

                            default:
                                break;
                        }
                        loadRecordAttachment(true);
                        event.consume();
                    }
                    break;
                case "tblViewJEDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnJEDetail = JFXUtil.moveToNextRow(currentTable);
                                break;
                            case UP:
                                pnJEDetail = JFXUtil.moveToPreviousRow(currentTable);
                                break;

                            default:
                                break;
                        }
                        loadRecordJEDetail();
                        event.consume();
                    }
                    break;
            }
        }
    }

    public void clearTextFields() {
        Platform.runLater(() -> {
            stageAttachment.closeDialog();

            imageinfo_temp.clear();
            JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField, dpTransactionDate, dpReferenceDate, dpReportMonthYear, dpSIDate);
            psSearchSupplierId = "";
            psSearchBranchId = "";
            psSupplierId = "";
            psBranchId = "";
            JFXUtil.clearTextFields(apMaster, apDetail, apJEDetail, apJEMaster, apAttachments);
            cbVatInclusive.setSelected(false);
            cbVatable.setSelected(false);
        });
    }
}
