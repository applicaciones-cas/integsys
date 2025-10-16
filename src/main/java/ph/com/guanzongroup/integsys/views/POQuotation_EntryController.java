package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelPOQuotation_Detail;
import ph.com.guanzongroup.integsys.model.ModelPOQuotation_Main;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.guanzon.appdriver.constant.DocumentType;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationStatus;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 2
 */
public class POQuotation_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static QuotationControllers poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String openedAttachment = "";
    private boolean pbEntered = false;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    private ObservableList<ModelPOQuotation_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelPOQuotation_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelPOQuotation_Main> filteredData;
    private FilteredList<ModelPOQuotation_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableAttachment;

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageAttachment = new JFXUtil.StageManager();
    AnchorPane root = null;
    Scene scene = null;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apTransactionInfo, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnVoid, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabInformation, tabAttachments;
    @FXML
    private TextField tfTransactionNo, tfReferenceNo, tfBranch, tfDepartment, tfSupplier, tfAddress, tfSourceNo, tfCategory, tfTerm, tfContact, tfGrossAmount, tfDiscRate, tfAddlDiscAmt, tfFreight, tfVATAmount, tfTransactionTotal, tfCompany, tfDescription, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail, tfCost, tfAttachmentNo;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpValidityDate;
    @FXML
    private CheckBox cbVatable, cbReverse;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails, tblAttachments, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblReplacementDetail, tblDescriptionDetail, tblUnitPriceDetail, tblDiscountDetail, tblQuantityDetail, tblTotalDetail, tblRowNoAttachment, tblFileNameAttachment, tblRowNo, tblCompany, tblBranch, tblSupplier, tblDate, tblReferenceNo;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
    @FXML
    private Pagination pgPagination;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        poController = new QuotationControllers(oApp, null);
        poJSON = new JSONObject();
        try {
            poJSON = poController.POQuotation().InitTransaction(); // Initialize transaction
        } catch (Exception e) {
            poJSON.put("message", "Error in Initialize");
        }
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        JFXUtil.checkIfFolderExists(poJSON, System.getProperty("sys.default.path.temp") + "/Attachments//");
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initAttachmentsGrid();
        initTableOnClick();
        clearTextFields();
        initLoadTable();

        Platform.runLater(() -> {
            poController.POQuotation().Master().setIndustryId(psIndustryId);
            poController.POQuotation().Master().setCompanyId(psCompanyId);
            poController.POQuotation().Master().setCategoryCode(psCategoryId);
            poController.POQuotation().setIndustryId(psIndustryId);
            poController.POQuotation().setCompanyId(psCompanyId);
            poController.POQuotation().setCategoryId(psCategoryId);
            poController.POQuotation().initFields();
            poController.POQuotation().setWithUI(true);
            loadRecordSearch();
            btnNew.fire();
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

    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F5) {
                System.out.println("tested key press");

                if (JFXUtil.isObjectEqualTo(poController.POQuotation().getEditMode(), EditMode.ADDNEW, EditMode.READY, EditMode.UPDATE)) {
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
        if (poController.POQuotation().getTransactionAttachmentCount() <= 0) {
            ShowMessageFX.Warning(null, pxeModuleName, "No transaction attachment to load.");
            return;
        }
        openedAttachment = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getTransactionNo();
        Map<String, Pair<String, String>> data = new HashMap<>();
        data.clear();
        for (int lnCtr = 0; lnCtr < poController.POQuotation().getTransactionAttachmentCount(); lnCtr++) {
            data.put(String.valueOf(lnCtr + 1), new Pair<>(String.valueOf(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName()),
                    poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getDocumentType()));
        }
        AttachmentDialogController controller = new AttachmentDialogController();
        controller.setOpenedImage(pnAttachment);
        controller.addData(data);

        try {
            stageAttachment.showDialog((Stage) btnSave.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/AttachmentDialog.fxml"), controller, "Attachment Dialog", false, false, true);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "cbVatable":
                    poController.POQuotation().Master().isVatable(checkedBox.isSelected());
                    loadRecordMaster();
                    break;
                case "cbReverse":
                    if (poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                        if (!checkedBox.isSelected()) {
                            poController.POQuotation().ReverseItem(pnDetail);
                        } else {
                            poController.POQuotation().Detail(pnDetail).isReverse(checkedBox.isSelected());
                        }
                    } else {
                        poController.POQuotation().Detail(pnDetail).isReverse(checkedBox.isSelected());
                    }
                    loadTableDetail.reload();
                    if (checkedBox.isSelected()) {
                        moveNext(false, false);
                    }
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
                    case "btnBrowse":
                        poController.POQuotation().setTransactionStatus(POQuotationStatus.OPEN);
                        poJSON = poController.POQuotation().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        pnEditMode = poController.POQuotation().getEditMode();
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            stageAttachment.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnNew":
                        //Clear data
                        poController.POQuotation().resetMaster();
                        poController.POQuotation().Detail().clear();
                        clearTextFields();

                        poJSON = poController.POQuotation().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.POQuotation().initFields();
                        pnEditMode = poController.POQuotation().getEditMode();
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        break;
                    case "btnUpdate":
                        poJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().Master().getTransactionNo());
                        poJSON = poController.POQuotation().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poController.POQuotation().loadAttachments();
                        pnEditMode = poController.POQuotation().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                            //Clear data
                            poController.POQuotation().resetMaster();
                            poController.POQuotation().Detail().clear();
                            clearTextFields();

                            poController.POQuotation().Master().setIndustryId(psIndustryId);
                            poController.POQuotation().setCompanyId(psCompanyId);
                            poController.POQuotation().Master().setCategoryCode(psCategoryId);
//                            poController.POQuotation().initFields();
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrievePOQuotationRequest();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.POQuotation().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.POQuotation().AddDetail();
                                loadRecordMaster();
                                loadTableDetail.reload();
                                loadTableAttachment.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().Master().getTransactionNo());
                                poController.POQuotation().loadAttachments();
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.POQuotation().Master().getTransactionStatus().equals(POQuotationStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.POQuotation().ConfirmTransaction("Confirmed");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.showRetainedHighlight(true, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                btnNew.fire();
                                return;
                            }
                        } else {
                            return;
                        }
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (POQuotationStatus.CONFIRMED.equals(poController.POQuotation().Master().getTransactionStatus())) {
                                poJSON = poController.POQuotation().CancelTransaction("");
                            } else {
                                poJSON = poController.POQuotation().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                            }

                            btnNew.fire();
                        } else {
                            return;
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
                            // Read image from the selected file
                            Path imgPath = selectedFile.toPath();
                            Image loimage = new Image(Files.newInputStream(imgPath));
                            imageView.setImage(loimage);

                            String imgPath2 = selectedFile.getName().toString();
                            for (int lnCtr = 0; lnCtr <= poController.POQuotation().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName())) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
                                    pnAttachment = lnCtr;
                                    loadRecordAttachment(true);
                                    return;
                                }
                            }
                            if (imageinfo_temp.containsKey(selectedFile.getName().toString())) {
                                ShowMessageFX.Warning(null, pxeModuleName, "File name already exists.");
                                loadRecordAttachment(true);
                                return;
                            } else {
                                imageinfo_temp.put(selectedFile.getName().toString(), imgPath.toString());
                            }
                            poJSON = poController.POQuotation().addAttachment();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            pnAttachment = poController.POQuotation().getTransactionAttachmentCount() - 1;
                            poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setFileName(imgPath2);
                            poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setSourceNo(poController.POQuotation().Master().getTransactionNo());
                            poController.POQuotation().getTransactionAttachmentCount();
                            loadTableAttachment.reload();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        }
                        break;
                    case "btnRemoveAttachment":
//                        attachment_data.remove(pnAttachment);
//                        if (pnAttachment != 0) {
//                            pnAttachment -= 1;
//                        }
//                        loadTableAttachment.reload();
//                        initAttachmentsGrid();
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

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnCancel")) {
                    poController.POQuotation().resetMaster();
                    poController.POQuotation().resetOthers();
                    poController.POQuotation().Detail().clear();
                    imageView.setImage(null);
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnAddAttachment", "btnRemoveAttachment", "btnSearch")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    poController.POQuotation().loadAttachments();
                    loadTableAttachment.reload();
                }
                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    moveNext(false, false);
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadHighlightFromDetail() {
        try {
            for (int lnCtr = 0; lnCtr < poController.POQuotation().getDetailCount(); lnCtr++) {
                if (poController.POQuotation().Detail(lnCtr).isReverse()) {
                    String lsTransNoBasis = "", lsCompany = "", lsSupplier = "";
                    lsTransNoBasis = poController.POQuotation().Master().getSourceNo();
                    lsCompany = poController.POQuotation().Master().Company().getCompanyName();
                    lsSupplier = poController.POQuotation().Master().Supplier().getCompanyName();
                    String lsHighlightbasis = lsTransNoBasis + lsCompany + lsSupplier;
                    if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(lnCtr).getQuantity(), null, "")) {
                        if (poController.POQuotation().Detail(lnCtr).getQuantity().doubleValue() > 0.0000) {
                            plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "1"));
                        } else {
                            plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "0"));
                        }
                    }
                }
            }
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"".equals(pair.getKey()) && pair.getKey() != null) {
                    JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);
                }
            }
            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, false);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void retrievePOQuotationRequest() {
        poJSON = new JSONObject();
        poJSON = poController.POQuotation().loadPOQuotationRequestSupplierList(tfCompany.getText(), tfBranch.getText(), tfDepartment.getText(), tfSupplier.getText(),
                tfCategory.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    private boolean pbKeyPressed = false;

    private boolean resetTransaction() {
        try {
            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to select another transaction?\nTransaction details will be deleted") == false) {
                    return false;
                } else {
                    poController.POQuotation().resetTransaction();
                    poController.POQuotation().loadAttachments();

                    imageinfo_temp.clear();
                    stageAttachment.closeDialog();

                    Platform.runLater(() -> {
                        loadTableDetail.reload();
                    });
                    tfAttachmentNo.clear();
                    cmbAttachmentType.setItems(documentType);

                    imageView.setImage(null);
                    JFXUtil.stackPaneClip(stackPane1);
                    Platform.runLater(() -> {
                        loadTableAttachment.reload();
                    });
                }
            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        return true;
    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfReferenceNo":
                        poJSON = poController.POQuotation().Master().setReferenceNo(lsValue);
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfCompany":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                                if (!pbKeyPressed) {
                                    if (!resetTransaction()) {
                                        break;
                                    }
                                } else {
                                    loadRecordMaster();
                                    return;
                                }
                            }

                            poJSON = poController.POQuotation().Master().setCompanyId("");
                        }
                        break;
                    case "tfBranch":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                                if (!pbKeyPressed) {
                                    if (!resetTransaction()) {
                                        break;
                                    }
                                } else {
                                    loadRecordMaster();
                                    return;
                                }
                            }
                            poJSON = poController.POQuotation().Master().setBranchCode("");
                        }
                        break;
                    case "tfDepartment":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                                if (!pbKeyPressed) {
                                    if (!resetTransaction()) {
                                        break;
                                    }
                                } else {
                                    loadRecordMaster();
                                    return;
                                }
                            }
                            poController.POQuotation().setSearchDepartment("");
                        }
                        break;
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                                if (!pbKeyPressed) {
                                    if (!resetTransaction()) {
                                        break;
                                    }
                                } else {
                                    loadRecordMaster();
                                    return;
                                }
                            }
                            poJSON = poController.POQuotation().Master().setSupplierId("");
                        }
                        break;
                    case "tfCategory":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                                if (!pbKeyPressed) {
                                    if (!resetTransaction()) {
                                        break;
                                    }
                                } else {
                                    loadRecordMaster();
                                    return;
                                }
                            }
                            poController.POQuotation().setSearchCategory("");
                        }
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotation().Master().setTerm("");
                        }
                        break;
                    case "tfDiscRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().computeDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.POQuotation().Master().setDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfAddlDiscAmt":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().computeDiscount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.POQuotation().Master().setAdditionalDiscountAmount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfFreight":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.valueOf(lsValue) > poController.POQuotation().Master().getTransactionTotal().doubleValue()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid freight amount");
                            break;
                        }

                        poJSON = poController.POQuotation().Master().setFreightAmount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                lsValue = lsValue.trim();
                switch (lsID) {
                    case "taRemarks"://Remarks
                        poJSON = poController.POQuotation().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtDetail_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfDescription":
                        if (lsValue.isEmpty()) {
                            if (poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.ADDNEW) {
                                poController.POQuotation().Detail(pnDetail).setStockId("");
                                poController.POQuotation().Detail(pnDetail).setDescription("");
                                poController.POQuotation().Detail(pnDetail).setReplaceId("");
                                poController.POQuotation().Detail(pnDetail).setReplaceDescription("");
                            }
                        }
                        break;
                    case "tfReplaceId":
                    case "tfReplaceDescription":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.POQuotation().RemovedExcessRequestItem(pnDetail);
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            poController.POQuotation().Detail(pnDetail).setReplaceId("");
                            poController.POQuotation().Detail(pnDetail).setReplaceDescription("");
                        }
                        break;
                    case "tfUnitPrice":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().Detail(pnDetail).setUnitPrice((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        break;
                    case "tfDiscRateDetail":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.valueOf(lsValue) > 100.00) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid discount rate.");
                            break;
                        }

                        poJSON = poController.POQuotation().computeCost(pnDetail, Double.valueOf(lsValue), poController.POQuotation().Detail(pnDetail).getDiscountAmount());
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        poJSON = poController.POQuotation().Detail(pnDetail).setDiscountRate((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "tfAddlDiscAmtDetail":
                        lsValue = JFXUtil.removeComma(lsValue);

                        poJSON = poController.POQuotation().computeCost(pnDetail, poController.POQuotation().Detail(pnDetail).getDiscountRate(), Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        poJSON = poController.POQuotation().Detail(pnDetail).setDiscountAmount((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        break;
                    case "tfQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.POQuotation().Detail(pnDetail).setQuantity((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        if (pbEntered) {
                            moveNext(false, true);
                            pbEntered = false;
                        }
                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        if (continueNext) {
            apDetail.requestFocus();
            boolean lbContinue = true;
            while (lbContinue) {
                pnDetail = isUp ? Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(tblViewTransDetails)).getIndex08())
                        : Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(tblViewTransDetails)).getIndex08());
                if (poController.POQuotation().Detail(pnDetail).isReverse()) {
                    lbContinue = false;
                }
            }
        }
        loadRecordDetail();
        JFXUtil.requestFocusNullField(new Object[][]{ // alternative to if , else if
            {poController.POQuotation().Detail(pnDetail).getDescription(), tfDescription},
            {poController.POQuotation().Detail(pnDetail).getReplaceId(), tfReplaceId}, // if null or empty, then requesting focus to the txtfield
            {poController.POQuotation().Detail(pnDetail).getReplaceDescription(), tfReplaceDescription},
            {poController.POQuotation().Detail(pnDetail).getUnitPrice(), tfCost},
            {poController.POQuotation().Detail(pnDetail).getDiscountRate(), tfDiscRateDetail},
            {poController.POQuotation().Detail(pnDetail).getDiscountAmount(), tfAddlDiscAmtDetail},
            {poController.POQuotation().Detail(pnDetail).getQuantity(), tfQuantity},}, tfQuantity); // default
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
                    if (tfQuantity.isFocused()) {
                        pbEntered = true;
                    }
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfDescription", "tfReplaceId", "tfReplaceDescription", "tfCost", "tfQuantity", "tfDiscRateDetail", "tfUnitPrice", "tfAddlDiscAmtDetail")) {
                        moveNext(true, true);
                        event.consume();
                    }
                    break;
                case DOWN:
                    if (JFXUtil.isObjectEqualTo(lsID, "tfDescription", "tfReplaceId", "tfReplaceDescription", "tfCost", "tfQuantity", "tfDiscRateDetail", "tfUnitPrice", "tfAddlDiscAmtDetail")) {
                        moveNext(false, true);
                        event.consume();
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfCompany":
                            pbKeyPressed = true;
                            if (!resetTransaction()) {
                                return;
                            }
                            pbKeyPressed = false;
                            poJSON = poController.POQuotation().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfBranch);
                            }
                            loadRecordMaster();
                            retrievePOQuotationRequest();
                            return;
                        case "tfBranch":
                            pbKeyPressed = true;
                            if (!resetTransaction()) {
                                return;
                            }
                            pbKeyPressed = false;
                            poJSON = poController.POQuotation().SearchBranch(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfDepartment);
                            }
                            loadRecordMaster();
                            retrievePOQuotationRequest();
                            return;
                        case "tfDepartment":
                            pbKeyPressed = true;
                            if (!resetTransaction()) {
                                return;
                            }
                            pbKeyPressed = false;
                            poJSON = poController.POQuotation().SearchDepartment(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfSupplier);
                            }
                            loadRecordMaster();
                            retrievePOQuotationRequest();
                            return;
                        case "tfSupplier":
                            pbKeyPressed = true;
                            if (!resetTransaction()) {
                                return;
                            }
                            pbKeyPressed = false;
                            poJSON = poController.POQuotation().SearchSupplier(lsValue, false, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(dpValidityDate);
                            }
                            loadRecordMaster();
                            retrievePOQuotationRequest();
                            return;
                        case "tfCategory":
                            pbKeyPressed = true;
                            if (!resetTransaction()) {
                                return;
                            }
                            pbKeyPressed = false;
                            poJSON = poController.POQuotation().SearchCategory(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfTerm);
                            }
                            loadRecordMaster();
                            retrievePOQuotationRequest();
                            return;
                        case "tfTerm":
                            poJSON = poController.POQuotation().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                txtField.setText("");
                                break;
                            } else {
                                JFXUtil.textFieldMoveNext(tfDiscRate);
                            }
                            loadRecordMaster();
                            return;
                        case "tfDescription":
                            poJSON = poController.POQuotation().SearchRequestItem(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfReplaceId);
                                }
                            }
                            return;
                        case "tfReplaceId":
                            poJSON = poController.POQuotation().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 7);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfUnitPrice);
                                }
                            }
                            return;
                        case "tfReplaceDescription":
                            poJSON = poController.POQuotation().SearchInventory(lsValue, false, pnDetail);
                            if ("error".equals(poJSON.get("result"))) {
                                txtField.setText("");
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row"))) + 1;
                                JFXUtil.runWithDelay(0.70, () -> {
                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnTempRow;
                                    loadTableDetail.reload();
                                });
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            } else {
                                int lnReturned = Integer.parseInt(String.valueOf(poJSON.get("row")));
                                JFXUtil.runWithDelay(0.80, () -> {
//                                    int lnTempRow = JFXUtil.getDetailTempRow(details_data, lnReturned, 8);
                                    pnDetail = lnReturned;
                                    loadTableDetail.reload();
                                });
                                loadTableDetail.reload();
                                if (!JFXUtil.isObjectEqualTo(poController.POQuotation().Detail(pnDetail).getDescription(), null, "")) {
                                    JFXUtil.textFieldMoveNext(tfUnitPrice);
                                }
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    switch (datePicker.getId()) {
                        case "dpValidityDate":
                            poJSON = poController.POQuotation().Master().setValidityDate(null);
                            break;
                    }
                    return;
                }
                if (!datePicker.isShowing() && !datePicker.getEditor().isFocused()) {
                    return;
                }
                String lsServerDate = sdfFormat.format(oApp.getServerDate());
                String lsTransDate = sdfFormat.format(poController.POQuotation().Master().getTransactionDate());
                String lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                LocalDate currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                LocalDate selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                LocalDate transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {

                            if (JFXUtil.isObjectEqualTo(poController.POQuotation().Master().getSourceNo(), null, "")) {
                                if (pbSuccess) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "Source No cannot be empty");
                                }
                                pbSuccess = false;
                                loadRecordMaster();
                                pbSuccess = true;
                                return;
                            }
                            String lsPOQuotationRequestTransDate = sdfFormat.format(poController.POQuotation().Master().POQuotationRequest().getTransactionDate());
                            LocalDate POQuotationRequestTransactionDate = LocalDate.parse(lsPOQuotationRequestTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(POQuotationRequestTransactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Transaction date cannot be before the Quotation Request Date");
                                pbSuccess = false;
                            } else if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poController.POQuotation().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
                                if (oApp.getUserLevel() <= UserRight.ENCODER) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Change in Transaction Date Detected\n\n"
                                            + "If YES, please seek approval to proceed with the new selected date.\n"
                                            + "If NO, the previous transaction date will be retained.") == true) {
                                        poJSON = ShowDialogFX.getUserApproval(oApp);
                                        if (!"success".equals((String) poJSON.get("result"))) {
                                            pbSuccess = false;
                                        } else {
                                            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                                poJSON.put("result", "error");
                                                poJSON.put("message", "User is not an authorized approving officer.");
                                                pbSuccess = false;
                                            }
                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                }
                            }

                            if (pbSuccess) {
                                poController.POQuotation().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));

                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpReferenceDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {
                            if (selectedDate.isBefore(transactionDate)) {
                                JFXUtil.setJSONError(poJSON, "Reference Date cannot be before the transaction date.");
                                pbSuccess = false;
                            } else if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            } else {
                                poController.POQuotation().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    case "dpValidityDate":
                        if (poController.POQuotation().getEditMode() == EditMode.ADDNEW
                                || poController.POQuotation().getEditMode() == EditMode.UPDATE) {
                            if (JFXUtil.isObjectEqualTo(poController.POQuotation().Master().getReferenceDate(), null, "")) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Reference Date is empty");
                                loadRecordMaster();
                                return;
                            }
                            String lsReferenceNoDate = sdfFormat.format(poController.POQuotation().Master().getReferenceDate());
                            LocalDate ReferenceDate = LocalDate.parse(lsReferenceNoDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(ReferenceDate)) {
                                JFXUtil.setJSONError(poJSON, "Validity date cannot be before the Reference Date");
                                pbSuccess = false;
                            } else {
                                poController.POQuotation().Master().setValidityDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                            }
                            if (pbSuccess) {
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordSearch() {
        try {
            if (poController.POQuotation().Master().Industry().getDescription() != null && !"".equals(poController.POQuotation().Master().Industry().getDescription())) {
                lblSource.setText(poController.POQuotation().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(String.valueOf(pnAttachment + 1));
                String lsAttachmentType = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                }
                int lnAttachmentType = 0;
                lnAttachmentType = Integer.parseInt(lsAttachmentType);
                cmbAttachmentType.getSelectionModel().select(lnAttachmentType);

                if (lbloadImage) {
                    try {
                        String filePath = (String) attachment_data.get(pnAttachment).getIndex02();
                        String filePath2 = "";
                        if (imageinfo_temp.containsKey((String) attachment_data.get(pnAttachment).getIndex02())) {
                            filePath2 = imageinfo_temp.get((String) attachment_data.get(pnAttachment).getIndex02());
                        } else {
                            // in server
                            filePath2 = System.getProperty("sys.default.path.temp") + "/Attachments//" + (String) attachment_data.get(pnAttachment).getIndex02();

                        }
                        if (filePath != null && !filePath.isEmpty()) {
                            Path imgPath = Paths.get(filePath2);
                            String convertedPath = imgPath.toUri().toString();
                            Image loimage = new Image(convertedPath);
                            imageView.setImage(loimage);
                            JFXUtil.adjustImageSize(loimage, imageView, imageviewerutil.ldstackPaneWidth, imageviewerutil.ldstackPaneHeight);
                            Platform.runLater(() -> {
                                JFXUtil.stackPaneClip(stackPane1);
                            });

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
                    JFXUtil.stackPaneClip(stackPane1);
                    pnAttachment = 0;
                }
            }
        } catch (Exception e) {
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.POQuotation().getDetailCount() - 1) {
                return;
            }
            if (!poController.POQuotation().Detail(pnDetail).isReverse()) {
                JFXUtil.setDisabled(true, tfDescription, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail);
            } else {
                boolean lbIsUpdate = poController.POQuotation().Detail(pnDetail).getEditMode() == EditMode.UPDATE;
                boolean lbReqItem = poController.POQuotation().Detail(pnDetail).getDescription() != null && !"".equals(poController.POQuotation().Detail(pnDetail).getDescription());
                boolean lbRepItem = poController.POQuotation().Detail(pnDetail).getReplaceDescription() != null && !"".equals(poController.POQuotation().Detail(pnDetail).getReplaceDescription());
                boolean lbIsReqMoreThanOne = poController.POQuotation().RequestMultipleItem(poController.POQuotation().Detail(pnDetail).getDescription());
//                boolean lbShow = (lbIsUpdate && (lbReqItem && lbRepItem));
//                JFXUtil.setDisabled(lbShow, tfReplaceId, tfReplaceDescription);

//                if(lbIsUpdate){
//                    JFXUtil.setDisabled(true, tfDescription);
//                } else {
                if (lbRepItem) {
                    JFXUtil.setDisabled(true, tfDescription);
                } else {
                    if ((!lbIsReqMoreThanOne && lbReqItem)) {
                        JFXUtil.setDisabled(true, tfDescription);
                    } else {
                        JFXUtil.setDisabled(false, tfDescription);
                    }
                }
//                }

                JFXUtil.setDisabled(false, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity, tfDiscRateDetail, tfAddlDiscAmtDetail);
            }

            tfDescription.setText(poController.POQuotation().Detail(pnDetail).getDescription());
            tfReplaceId.setText(poController.POQuotation().Detail(pnDetail).ReplacedInventory().getBarCode());
            tfReplaceDescription.setText(poController.POQuotation().Detail(pnDetail).getReplaceDescription());
            tfUnitPrice.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getUnitPrice(), true));

            tfQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getQuantity(), false));
            tfDiscRateDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getDiscountRate(), false));
            tfAddlDiscAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(pnDetail).getDiscountAmount(), true));
            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().getCost(pnDetail), true));
            cbReverse.setSelected(poController.POQuotation().Detail(pnDetail).isReverse());
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordMaster() {
        try {
            boolean lbShow = (JFXUtil.isObjectEqualTo(pnEditMode, EditMode.UPDATE, EditMode.ADDNEW));
            JFXUtil.setDisabled(!lbShow, dpTransactionDate);

            JFXUtil.setStatusValue(lblStatus, POQuotationStatus.class, pnEditMode == EditMode.UNKNOWN ? "-1" : poController.POQuotation().Master().getTransactionStatus());
            poController.POQuotation().computeFields();

            tfTransactionNo.setText(poController.POQuotation().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfReferenceNo.setText(poController.POQuotation().Master().getReferenceNo());
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getReferenceDate());
            dpReferenceDate.setValue(JFXUtil.isObjectEqualTo(lsReferenceDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            String lsValidityDate = CustomCommonUtil.formatDateToShortString(poController.POQuotation().Master().getValidityDate());
            dpValidityDate.setValue(JFXUtil.isObjectEqualTo(lsValidityDate, "1900-01-01") ? null : CustomCommonUtil.parseDateStringToLocalDate(lsValidityDate, "yyyy-MM-dd"));

            tfBranch.setText(poController.POQuotation().Master().Branch().getBranchName());
            tfCompany.setText(poController.POQuotation().Master().Company().getCompanyName());
            tfSupplier.setText(poController.POQuotation().Master().Supplier().getCompanyName());
            tfAddress.setText(poController.POQuotation().Master().Address().getAddress());
            tfContact.setText(poController.POQuotation().Master().Contact().getMobileNo());
            tfSourceNo.setText(poController.POQuotation().Master().getSourceNo());
            tfTerm.setText(poController.POQuotation().Master().Term().getDescription());

            if (poController.POQuotation().Master().getSourceNo() != null && !"".equals(poController.POQuotation().Master().getSourceNo())) {
                tfDepartment.setText(poController.POQuotation().Master().POQuotationRequest().Department().getDescription());
                tfCategory.setText(poController.POQuotation().Master().POQuotationRequest().Category2().getDescription());
            } else {
                tfDepartment.setText(poController.POQuotation().getSearchDepartment());
                tfCategory.setText(poController.POQuotation().getSearchCategory());
            }

            tfGrossAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getGrossAmount(), true));
            tfDiscRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getDiscountRate(), false));
            tfAddlDiscAmt.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getAdditionalDiscountAmount(), true));
            tfFreight.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getFreightAmount(), true));

            cbVatable.setSelected(poController.POQuotation().Master().isVatable());
            tfVATAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getVatAmount(), true));
            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Master().getTransactionTotal(), true));
            taRemarks.setText(poController.POQuotation().Master().getRemarks());
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            if (pnEditMode != EditMode.ADDNEW) {
                ShowMessageFX.Warning(null, pxeModuleName, "Data can only be viewed when in ADD mode.");
                return;
            }

            poJSON = new JSONObject();
            ModelPOQuotation_Main selected = (ModelPOQuotation_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (!resetTransaction()) {
                    return;
                }
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                poJSON = poController.POQuotation().populatePOQuotation(pnRowMain);
//                poJSON = poController.POQuotation().OpenTransaction(poController.POQuotation().POQuotationList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
            }
            imageinfo_temp.clear();
            stageAttachment.closeDialog();

            poController.POQuotation().loadAttachments();
            Platform.runLater(() -> {
                loadTableDetail.reload();
            });
            tfAttachmentNo.clear();
            cmbAttachmentType.setItems(documentType);

            imageView.setImage(null);
            JFXUtil.stackPaneClip(stackPane1);
            Platform.runLater(() -> {
                loadTableAttachment.reload();
            });

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initLoadTable() {
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
                            for (lnCtr = 0; lnCtr < poController.POQuotation().getTransactionAttachmentCount(); lnCtr++) {
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.POQuotation().TransactionAttachmentList(lnCtr).getModel().getFileName())
                                        ));
                            }
                            if (pnAttachment < 0 || pnAttachment
                                    >= attachment_data.size()) {
                                if (!attachment_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblAttachments, 0);
                                    pnAttachment = 0;
                                    loadRecordAttachment(true);
                                } else {
                                    tfAttachmentNo.setText("");
                                    cmbAttachmentType.getSelectionModel().select(0);
                                    loadRecordAttachment(false);
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblAttachments, pnAttachment);
                                loadRecordAttachment(true);
                            }
                        } catch (Exception e) {
                        }
                    });
                }
        );

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    pbEntered = false;
                    Platform.runLater(() -> {
                        int lnCtr;
                        details_data.clear();
                        plOrderNoPartial.clear();
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                poController.POQuotation().ReloadDetail();
                            }
                            JFXUtil.disableAllHighlight(tblViewTransDetails, highlightedRowsDetail);
                            int lnRowCount = 0;
                            for (lnCtr = 0; lnCtr < poController.POQuotation().getDetailCount(); lnCtr++) {
                                if (!poController.POQuotation().Detail(lnCtr).isReverse()) {
                                    JFXUtil.highlightByKey(tblViewTransDetails, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelPOQuotation_Detail(
                                                String.valueOf(lnRowCount),
                                                String.valueOf(poController.POQuotation().Detail(lnCtr).getDescription()),
                                                String.valueOf(poController.POQuotation().Detail(lnCtr).getReplaceDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(lnCtr).getUnitPrice(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().getDiscount(lnCtr), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.POQuotation().Detail(lnCtr).getQuantity(), false)),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(poController.POQuotation().getCost(lnCtr)), true), String.valueOf(lnCtr)
                                        ));
//                                }
                            }

                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                            loadHighlightFromDetail();
                            int lnTempRow = JFXUtil.getDetailRow(details_data, pnDetail, 8); //this method is only used when Reverse is applied
                            if (lnTempRow < 0 || lnTempRow
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    JFXUtil.selectAndFocusRow(tblViewTransDetails, 0);
                                    int lnRow = Integer.parseInt(details_data.get(0).getIndex08());
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                JFXUtil.selectAndFocusRow(tblViewTransDetails, lnTempRow);
                                int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex08());
                                pnDetail = lnRow;
                                loadRecordDetail();
                            }
                            loadRecordMaster();
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        main_data.clear();
                        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                        if (poController.POQuotation().getPOQuotationRequestSupplierCount() > 0) {
                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.POQuotation().getPOQuotationRequestSupplierCount() - 1; lnCtr++) {
                                try {
                                    String lsTransNoBasis = poController.POQuotation().POQuotationRequestSupplierList(lnCtr).getTransactionNo();
                                    String lsCompany = poController.POQuotation().POQuotationRequestSupplierList(lnCtr).Company().getCompanyName();
                                    String lsSupplier = poController.POQuotation().POQuotationRequestSupplierList(lnCtr).Supplier().getCompanyName();
                                    String lsHighlightbasis = lsTransNoBasis + lsCompany + lsSupplier;
                                    main_data.add(new ModelPOQuotation_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.POQuotation().POQuotationRequestSupplierList(lnCtr).Company().getCompanyName()),
                                            String.valueOf(poController.POQuotation().POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().Branch().getBranchName()),
                                            String.valueOf(poController.POQuotation().POQuotationRequestSupplierList(lnCtr).Supplier().getCompanyName()),
                                            String.valueOf(CustomCommonUtil.formatDateToShortString(poController.POQuotation().POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().getTransactionDate())),
                                            String.valueOf(poController.POQuotation().POQuotationRequestSupplierList(lnCtr).getTransactionNo()),
                                            lsHighlightbasis
                                    ));
                                } catch (GuanzonException | SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                }
                            }
                            loadHighlightFromDetail();
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
                });

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpValidityDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate, dpValidityDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfTransactionNo, tfReferenceNo, tfCompany, tfBranch, tfDepartment, tfSupplier,
                tfSourceNo, tfCategory, tfTerm, tfDiscRate, tfAddlDiscAmt, tfFreight);

        JFXUtil.setFocusListener(txtDetail_Focus, tfDescription, tfReplaceId, tfReplaceDescription, tfUnitPrice, tfQuantity,
                tfDiscRateDetail, tfAddlDiscAmtDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apDetail, apBrowse);

        JFXUtil.setCommaFormatter(tfGrossAmount, tfAddlDiscAmt, tfFreight, tfVATAmount, tfTransactionTotal,
                tfUnitPrice, tfQuantity, tfAddlDiscAmtDetail);

        CustomCommonUtil.inputDecimalOnly(tfDiscRate, tfDiscRateDetail);

        // Combobox
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poController.POQuotation().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbAttachmentType);
        JFXUtil.setCheckboxHoverCursor(apMaster, apDetail);
    }

    public void initTableOnClick() {
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                imageviewerutil.scaleFactor = 1.0;
                loadRecordAttachment(true);
                JFXUtil.resetImageBounds(imageView, stackPane1);
            }
        });

        tblViewTransDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    int lnRow = Integer.parseInt(details_data.get(tblViewTransDetails.getSelectionModel().getSelectedIndex()).getIndex08());
                    pnDetail = lnRow;
                    moveNext(false, false);
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
//                    pnEditMode = poController.POQuotation().getEditMode();
//                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelPOQuotation_Main) item).getIndex07(), highlightedRowsMain);
        JFXUtil.applyRowHighlighting(tblViewTransDetails, item -> ((ModelPOQuotation_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList, tblViewTransDetails, tblAttachments);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apMaster, apDetail, apAttachments, apAttachmentButtons);

        JFXUtil.setDisabled(fnValue == EditMode.UPDATE, tfCompany, tfBranch, tfSupplier, tfCategory, tfDepartment);

        switch (poController.POQuotation().Master().getTransactionStatus()) {
            case POQuotationStatus.VOID:
            case POQuotationStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
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
        currentIndex = pnAttachment;
        int newIndex = currentIndex + direction;

        if (newIndex != -1 && (newIndex <= attachment_data.size() - 1)) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), imageView);
            slideOut.setByX(direction * -400); // Move left or right

            JFXUtil.selectAndFocusRow(tblAttachments, newIndex);
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

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblReplacementDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblUnitPriceDetail, tblDiscountDetail, tblQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        tblViewTransDetails.setItems(filteredDataDetail);
        tblViewTransDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblCompany, tblBranch, tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    private void tableKeyEvents(KeyEvent event) {
        TableView<?> currentTable = (TableView<?>) event.getSource();
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell == null) {
            return;
        }
        boolean moveDown = event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.DOWN;
        boolean moveUp = event.getCode() == KeyCode.UP;
        int newIndex = 0;

        if (moveDown || moveUp) {
            switch (currentTable.getId()) {
                case "tblViewTransDetails":
                    if (details_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? Integer.parseInt(details_data.get(JFXUtil.moveToNextRow(currentTable)).getIndex08())
                            : Integer.parseInt(details_data.get(JFXUtil.moveToPreviousRow(currentTable)).getIndex08());
                    pnDetail = newIndex;
                    loadRecordDetail();
                    break;
                case "tblAttachments":
                    if (attachment_data.isEmpty()) {
                        return;
                    }
                    newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable)
                            : JFXUtil.moveToPreviousRow(currentTable);
                    pnAttachment = newIndex;
                    loadRecordAttachment(true);
                    break;

            }
            event.consume();
        }
    }

    public void clearTextFields() {
        Platform.runLater(() -> {
            stageAttachment.closeDialog();
            imageinfo_temp.clear();
            JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
            JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
        });
    }

}
