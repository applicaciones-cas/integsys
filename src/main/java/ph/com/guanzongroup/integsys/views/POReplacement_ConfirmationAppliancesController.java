/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.collections.transformation.SortedList;
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
import javafx.stage.Stage;
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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FXML Controller class
 *
 * @author Team 2
 */
public class POReplacement_ConfirmationAppliancesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static PurchaseOrderReceivingControllers poController;
    public int pnEditMode;
    boolean isPrinted = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private boolean pbEntered = false;

    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    boolean lbresetpredicate = false;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableMain, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    JFXUtil.StageManager stageSerialDialog = new JFXUtil.StageManager();

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount, tfTotal, tfOrderNo, tfBrand, tfModel, tfBarcode, tfDescription, tfModelVariant, tfColor, tfInventoryType, tfMeasure, tfCost, tfOrderQuantity, tfReceiveQuantity, tfAttachmentNo;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnUpdate, btnSearch, btnSerials, btnSave, btnCancel, btnConfirm, btnVoid, btnPrint, btnReturn, btnHistory, btnRetrieve, btnClose, btnAddAttachment, btnRemoveAttachment, btnArrowLeft, btnArrowRight;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails, tblViewMainList, tblAttachments;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail, tblRowNo, tblSupplier, tblDate, tblReferenceNo, tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private Pagination pgPagination;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        poController = new PurchaseOrderReceivingControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poController.PurchaseOrderReceiving().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initLoadTable();
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initAttachmentsGrid();
        initTableOnClick();
        clearTextFields();

        Platform.runLater(() -> {
            poController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
            poController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
            poController.PurchaseOrderReceiving().setIndustryId(psIndustryId);
            poController.PurchaseOrderReceiving().setCompanyId(psCompanyId);
            poController.PurchaseOrderReceiving().setCategoryId(psCategoryId);
            poController.PurchaseOrderReceiving().initFields();
            poController.PurchaseOrderReceiving().setWithUI(true);
            poController.PurchaseOrderReceiving().setPurpose(PurchaseOrderReceivingStatus.Purpose.REPLACEMENT);
            loadRecordSearch();
        });

        initAttachmentPreviewPane();
        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField); // for btnSearch Reference

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

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String tabText = "";

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnPrint":
                        poJSON = poController.PurchaseOrderReceiving().printRecord(() -> {
                            if (isPrinted) {
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                poController.PurchaseOrderReceiving().resetMaster();
                                poController.PurchaseOrderReceiving().resetOthers();
                                poController.PurchaseOrderReceiving().Detail().clear();
                                imageView.setImage(null);
                                pnEditMode = EditMode.UNKNOWN;
                                clearTextFields();
                                initButton(pnEditMode);

                                poController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                                poController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                                poController.PurchaseOrderReceiving().Master().setCategoryCode(psCategoryId);
                            }
                            Platform.runLater(() -> {
                                try {
                                    if (!isPrinted) {
                                        poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo());
                                        poController.PurchaseOrderReceiving().loadAttachments();
                                    }
                                    loadRecordMaster();
                                    loadTableDetail.reload();
                                    loadTableAttachment.reload();
                                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                }
                                isPrinted = false;
                            });

                        });
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            stageSerialDialog.closeDialog();
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnSerials":
                        if (!poController.PurchaseOrderReceiving().Detail(pnDetail).isSerialized()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Selected item is not serialized.");
                            return;
                        }
                        showSerialDialog();
                        return;
                    case "btnUpdate":
                        poJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
                        poJSON = poController.PurchaseOrderReceiving().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        //Populate purhcase receiving serials
                        for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getDetailCount() - 1; lnCtr++) {
                            poController.PurchaseOrderReceiving().getPurchaseOrderReceivingSerial(poController.PurchaseOrderReceiving().Detail(lnCtr).getEntryNo());
                        }

                        poController.PurchaseOrderReceiving().loadAttachments();
                        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
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
                        if (mainSearchListener != null) {
                            JFXUtil.removeTextFieldListener(mainSearchListener, tfOrderNo);
                            mainSearchListener = null; // Clear reference to avoid memory leaks
                        }
                        retrievePOR();
                        JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poController.PurchaseOrderReceiving().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.PurchaseOrderReceiving().AddDetail();
                                loadTableDetail.reload();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poController.PurchaseOrderReceiving().Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poController.PurchaseOrderReceiving().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                // Print Transaction Prompt
                                loJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
                                poController.PurchaseOrderReceiving().loadAttachments();

                                loadRecordMaster();
                                isPrinted = false;
                                if ("success".equals(loJSON.get("result"))) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to print this transaction?")) {
                                        isPrinted = true;
                                        btnPrint.fire();
                                    }
                                }
                                if (!isPrinted) {
                                    JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                }
                            }
                        } else {
                            return;
                        }

                        break;
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poController.PurchaseOrderReceiving().ConfirmTransaction("");
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
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (PurchaseOrderReceivingStatus.CONFIRMED.equals(poController.PurchaseOrderReceiving().Master().getTransactionStatus())) {
                                poJSON = poController.PurchaseOrderReceiving().CancelTransaction("");
                            } else {
                                poJSON = poController.PurchaseOrderReceiving().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnReturn":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to return transaction?") == true) {
                            poJSON = poController.PurchaseOrderReceiving().ReturnTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAC898", highlightedRowsMain);
                            }
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
                            for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName())) {
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

                            poJSON = poController.PurchaseOrderReceiving().addAttachment();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            pnAttachment = poController.PurchaseOrderReceiving().getTransactionAttachmentCount() - 1;
                            poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setFileName(imgPath2);
                            poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setSourceNo(poController.PurchaseOrderReceiving().Master().getTransactionNo());
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

                boolean lbproceed = false;
                if (lsButton.equals("btnSave") || lsButton.equals("btnConfirm") || lsButton.equals("btnReturn")
                        || lsButton.equals("btnVoid") || lsButton.equals("btnCancel")) {
                    if (lsButton.equals("btnSave")) {
                        if (!isPrinted) {
                            lbproceed = true;
                        }
                    } else {
                        lbproceed = true;
                    }
                    if (lbproceed) {
                        poController.PurchaseOrderReceiving().resetMaster();
                        poController.PurchaseOrderReceiving().resetOthers();
                        poController.PurchaseOrderReceiving().Detail().clear();
                        imageView.setImage(null);
                        pnEditMode = EditMode.UNKNOWN;
                        clearTextFields();

                        poController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                        poController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                        poController.PurchaseOrderReceiving().Master().setCategoryCode(psCategoryId);
                    }
                }

                if (lsButton.equals("btnPrint") || lsButton.equals("btnAddAttachment") || lsButton.equals("btnRemoveAttachment")
                        || lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    loadTableAttachment.reload();
                }

                initButton(pnEditMode);
                if (lsButton.equals("btnUpdate")) {
                    if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                        tfReceiveQuantity.requestFocus();
                    } else {
                        tfBrand.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void retrievePOR() {
        poJSON = new JSONObject();
        poJSON = poController.PurchaseOrderReceiving().loadPurchaseOrderReceiving("confirmation", psCompanyId, psSupplierId, tfSearchReferenceNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain.reload();
        }
    }

    public void showSerialDialog() {
        stageSerialDialog.closeDialog();
        poJSON = new JSONObject();
        try {
            if (!poController.PurchaseOrderReceiving().Detail(pnDetail).isSerialized()) {
                return;
            }

            if (poController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue() == 0) {
                ShowMessageFX.Warning(null, pxeModuleName, "Received quantity cannot be empty.");
                return;
            }

            //Populate Purchase Order Receiving Detail
            poJSON = poController.PurchaseOrderReceiving().getPurchaseOrderReceivingSerial(pnDetail + 1);
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }

            DeliveryAcceptance_SerialAppliancesController controller = new DeliveryAcceptance_SerialAppliancesController();
            if (controller != null) {
                controller.setGRider(oApp);
                controller.setObject(poController.PurchaseOrderReceiving());
                controller.setEntryNo(pnDetail + 1);
            }

            try {
                stageSerialDialog.setOnHidden(event -> {
                    moveNext(false, true);
                    Platform.runLater(() -> {
                        loadTableDetail.reload();
                    });
                });
                stageSerialDialog.showDialog((Stage) btnSave.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/DeliveryAcceptance_SerialAppliances.fxml"),
                        controller, "Inventory Serial", true, true, false);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setSupplierId("");
                        }
                        break;
                    case "tfTrucking":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setTruckingId("");
                        }
                        break;
                    case "tfAreaRemarks":
                        break;
                    case "tfTerm":
                        if (lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setTermCode("");
                        }
                        break;
                    case "tfReferenceNo":
                        if (!lsValue.isEmpty()) {
                            poJSON = poController.PurchaseOrderReceiving().Master().setReferenceNo(lsValue);
                        } else {
                            poJSON = poController.PurchaseOrderReceiving().Master().setReferenceNo("");
                        }
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfReferenceNo.setText("");
                            break;
                        }
                        break;
                    case "tfDiscountRate":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PurchaseOrderReceiving().computeDiscount(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.PurchaseOrderReceiving().Master().setDiscountRate((Double.valueOf(lsValue)));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }

                        break;
                    case "tfDiscountAmount":
                        lsValue = JFXUtil.removeComma(lsValue);
                        poJSON = poController.PurchaseOrderReceiving().computeDiscountRate(Double.valueOf(lsValue));
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        poJSON = poController.PurchaseOrderReceiving().Master().setDiscount(Double.valueOf(lsValue));
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
                        poJSON = poController.PurchaseOrderReceiving().Master().setRemarks(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
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
                    case "tfCost":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (Double.parseDouble(lsValue) < 0.00) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Cost Amount");
                            break;
                        }

                        double ldblOldVal = poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue();
                        poJSON = poController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                            return;
                        }

                        try {
                            poJSON = poController.PurchaseOrderReceiving().computeFields();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce(ldblOldVal);
                                tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                                return;
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }

                        break;
                    case "tfReceiveQuantity":
                        lsValue = JFXUtil.removeComma(lsValue);
                        if (poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() != null
                        && !"".equals(poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo())) {
                            if (poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().doubleValue() < Double.valueOf(lsValue)) {
                                ShowMessageFX.Warning(null, pxeModuleName, "Receive quantity cannot be greater than the order quantity.");
                                JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                                break;
                            }
                        }

                        poJSON = poController.PurchaseOrderReceiving().checkPurchaseOrderReceivingSerial(pnDetail + 1, Integer.valueOf(lsValue));
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        }
                        int lnNewVal = Integer.valueOf(lsValue);
                        int lnOldVal = poController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue();

                        poJSON = poController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity((Integer.valueOf(lsValue)));
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                            break;
                        }

                        try {
                            poJSON = poController.PurchaseOrderReceiving().computeFields();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.textFieldMoveNext(tfReceiveQuantity);
                                break;
                            }
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }

                        if (pbEntered) {
                            if (lnNewVal != lnOldVal) {
                                if ((Integer.valueOf(lsValue) > 0
                                && poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                                && !"".equals(poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
                                    showSerialDialog();
                                } else {
                                    moveNext(false, true);
                                }
                            } else {
                                moveNext(false, true);
                            }
                            pbEntered = false;
                        }

                        break;
                }
                JFXUtil.runWithDelay(0.50, () -> {
                    loadTableDetail.reload();
                });
            });
    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /*Lost Focus*/
                switch (lsID) {
                    case "tfSearchSupplier":
                        if (lsValue.equals("")) {
                            psSupplierId = "";
                        }
                        break;
                    case "tfSearchReferenceNo":
                        break;
                    case "tfAttachmentNo":
                        break;
                }
                if (lsID.equals("tfSearchSupplier")
                || lsID.equals("tfSearchReferenceNo")) {
                    loadRecordSearch();
                } else {
                    loadRecordAttachment(true);
                }
            });

    public void moveNext(boolean isUp, boolean continueNext) {
        if (details_data.size() <= 0) {
            return;
        }

        if (continueNext) {
            apDetail.requestFocus();

            pnDetail = isUp ? JFXUtil.moveToPreviousRow(tblViewTransDetails) : JFXUtil.moveToNextRow(tblViewTransDetails);
        }
        loadRecordDetail();
        tfReceiveQuantity.requestFocus();
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
                        case "tfBrand":
                        case "tfReceiveQuantity":
                            moveNext(true, true);
                            event.consume();
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBrand":
                        case "tfReceiveQuantity":
                            moveNext(false, true);
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case BACK_SPACE:
                    switch (lsID) {
                        case "tfOrderNo":
                            if (mainSearchListener != null) {
                                JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                                mainSearchListener = null; // Clear reference to avoid memory leaks
                                initDetailsGrid();
                                initMainGrid();
                                goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                            }
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchSupplier":
                            poJSON = poController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();
                            }
                            retrievePOR();
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            retrievePOR();
                            return;
                        case "tfTrucking":
                            poJSON = poController.PurchaseOrderReceiving().SearchTrucking(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTrucking.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfTerm":
                            poJSON = poController.PurchaseOrderReceiving().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTerm.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfOrderNo":

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
        poJSON.put("result", "success");
        poJSON.put("message", "success");

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
                selectedDate = ldtResult.selectedDate;

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getReferenceDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            referenceDate = LocalDate.parse(lsRefDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && (selectedDate.isBefore(referenceDate))) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Receiving date cannot be before reference date.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
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
                                poController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                        if (poController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poController.PurchaseOrderReceiving().Master().getReferenceDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            transactionDate = LocalDate.parse(lsTransDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

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
                                poController.PurchaseOrderReceiving().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poController.PurchaseOrderReceiving().Master().Industry().getDescription());

            if (psSupplierId.equals("")) {
                tfSearchSupplier.setText("");
            } else {
                tfSearchSupplier.setText(poController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            }
            try {
                if (tfSearchReferenceNo.getText() == null || tfSearchReferenceNo.getText().equals("")) {
                    tfSearchReferenceNo.setText("");
                } else {

                }
            } catch (Exception e) {
                tfSearchReferenceNo.setText("");
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(String.valueOf(pnAttachment + 1));
                String lsAttachmentType = poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
                if (lsAttachmentType.equals("")) {
                    poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setDocumentType(DocumentType.OTHER);
                    lsAttachmentType = poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getDocumentType();
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
            if (pnDetail < 0 || pnDetail > poController.PurchaseOrderReceiving().getDetailCount() - 1) {
                return;
            }
            if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                poController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBrandId());
            }
            JFXUtil.setDisabled(oApp.getUserLevel() <= UserRight.ENCODER, tfCost);

            tfBarcode.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Brand().getDescription()); //TODO
            tfModelVariant.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Variant().getDescription()); //TODO

            tfModel.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
            tfOrderQuantity.setText(String.valueOf(poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().intValue()));
            tfReceiveQuantity.setText(String.valueOf(poController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue()));

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.UPDATE;
        JFXUtil.setDisabled(lbDisable, tfSupplier);

        boolean lbIsReprint = poController.PurchaseOrderReceiving().Master().getPrint().equals("1") ? true : false;
        if (lbIsReprint) {
            btnPrint.setText("Reprint");
        } else {
            btnPrint.setText("Print");
        }
        try {
            JFXUtil.setStatusValue(lblStatus, PurchaseOrderReceivingStatus.class,
                    pnEditMode == EditMode.UNKNOWN ? "-1" : poController.PurchaseOrderReceiving().Master().getTransactionStatus());
            boolean lbPrintStat = pnEditMode == EditMode.READY && poController.PurchaseOrderReceiving().Master().getTransactionStatus() != PurchaseOrderReceivingStatus.VOID;

            btnPrint.setVisible(lbPrintStat);
            btnPrint.setManaged(lbPrintStat);

            if (poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue() > 0.00) {
                poController.PurchaseOrderReceiving().computeDiscount(poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
            } else {
                if (poController.PurchaseOrderReceiving().Master().getDiscount().doubleValue() > 0.00) {
                    poController.PurchaseOrderReceiving().computeDiscountRate(poController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
                }
            }
            poController.PurchaseOrderReceiving().computeFields();

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.PurchaseOrderReceiving().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            //ReferenceDate
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.PurchaseOrderReceiving().Master().getReferenceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            tfTransactionNo.setText(poController.PurchaseOrderReceiving().Master().getTransactionNo());

            tfSupplier.setText(poController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfTrucking.setText(poController.PurchaseOrderReceiving().Master().Trucking().getCompanyName());
            tfTerm.setText(poController.PurchaseOrderReceiving().Master().Term().getDescription());
            tfReferenceNo.setText(poController.PurchaseOrderReceiving().Master().getReferenceNo());
            taRemarks.setText(poController.PurchaseOrderReceiving().Master().getRemarks());

            Platform.runLater(() -> {
                double lnValue = poController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue();
                if (!Double.isNaN(lnValue)) {
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getDiscountRate(), false));
                } else {
                    tfDiscountRate.setText("0.00");
                }
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getDiscount(), true));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Master().getTransactionTotal(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void goToPageBasedOnSelectedRow(String pnRowMain) {
        if (mainSearchListener != null) {
            JFXUtil.removeTextFieldListener(mainSearchListener, tfOrderNo);
            mainSearchListener = null;
        }
        if (detailSearchListener != null) {
            JFXUtil.removeTextFieldListener(detailSearchListener, tfOrderNo);
            detailSearchListener = null;
        }
        filteredDataDetail.setPredicate(null);
        filteredData.setPredicate(null);
        lbresetpredicate = false;
        int realIndex = Integer.parseInt(pnRowMain);

        if (realIndex == -1) {
            return; // Not found
        }
        int targetPage = realIndex / ROWS_PER_PAGE;
        int indexInPage = realIndex % ROWS_PER_PAGE;

        initMainGrid();
        initDetailsGrid();
        int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(targetPage);
        JFXUtil.changeTableView(indexInPage, ROWS_PER_PAGE, tblViewMainList, main_data.size(), filteredData);

        Platform.runLater(() -> {
            if (lbresetpredicate) {
                tblViewMainList.scrollTo(indexInPage);
                lbresetpredicate = false;
            }
        });
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            ModelDeliveryAcceptance_Main selected = (ModelDeliveryAcceptance_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(String.valueOf(pnMain + 1)), "#A7C7E7", highlightedRowsMain);
                poJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                goToPageBasedOnSelectedRow(String.valueOf(pnMain));

            }
            for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getDetailCount() - 1; lnCtr++) {
                poController.PurchaseOrderReceiving().getPurchaseOrderReceivingSerial(poController.PurchaseOrderReceiving().Detail(lnCtr).getEntryNo());
            }
            poController.PurchaseOrderReceiving().loadAttachments();
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

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initLoadTable() {
        loadTableAttachment = new JFXUtil.ReloadableTableTask(
                tblAttachments,
                attachment_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            attachment_data.clear();
                            int lnCtr;
                            for (lnCtr = 0; lnCtr < poController.PurchaseOrderReceiving().getTransactionAttachmentCount(); lnCtr++) {
                                attachment_data.add(
                                        new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName())
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

                }
        );

        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewTransDetails,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        pbEntered = false;
                        // Setting data to table detail
                        JFXUtil.disableAllHighlight(tblViewTransDetails, highlightedRowsDetail);
                        if (lbresetpredicate) {
                            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                            filteredDataDetail.setPredicate(null);
                            lbresetpredicate = false;
                            JFXUtil.removeTextFieldListener(detailSearchListener, tfOrderNo);

                            mainSearchListener = null;
                            filteredData.setPredicate(null);
                            initMainGrid();
                            initDetailsGrid();
                            Platform.runLater(() -> {
                                tfOrderNo.setText("");
                            });
                            goToPageBasedOnSelectedRow(String.valueOf(pnMain));
                        }
                        details_data.clear();
                        int lnCtr;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                String lsBrandId = "";
                                lnCtr = poController.PurchaseOrderReceiving().getDetailCount() - 1;
                                while (lnCtr >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() == null || "".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId())) {
                                        if (poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId() != null
                                                || !"".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId())) {
                                            lsBrandId = poController.PurchaseOrderReceiving().Detail(lnCtr).getBrandId();
                                        }
                                        //remove por detail
                                        poController.PurchaseOrderReceiving().Detail().remove(lnCtr);
                                        //remove por serial
                                        poController.PurchaseOrderReceiving().removePurchaseOrderReceivingSerial(lnCtr + 1);
                                    }
                                    lnCtr--;
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId() != null && !"".equals(poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId())) {
                                        poController.PurchaseOrderReceiving().AddDetail();
                                    }
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) < 0) {
                                    poController.PurchaseOrderReceiving().AddDetail();
                                }

                                //Set brand Id to last row
                                if (!lsBrandId.isEmpty()) {
                                    poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).setBrandId(lsBrandId);
                                }
                                //Check for PO Serial Update Entry No TODO
                            }

                            double lnTotal = 0.00;
                            for (lnCtr = 0; lnCtr < poController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                                if (poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() == null
                                        || "".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo())) {
                                    continue;
                                }
                                try {
                                    lnTotal = poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue();
                                } catch (Exception e) {
                                }

                                if ((!poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                    JFXUtil.highlightByKey(tblViewTransDetails, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                                }

                                String lsBrand = "";
                                if (poController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription() != null) {
                                    lsBrand = poController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription();
                                }
                                details_data.add(
                                        new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().intValue()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                        ));
                            }

                            if (pnDetail < 0 || pnDetail
                                    >= details_data.size()) {
                                if (!details_data.isEmpty()) {
                                    /* FOCUS ON FIRST ROW */
                                    tblViewTransDetails.getSelectionModel().select(0);
                                    tblViewTransDetails.getFocusModel().focus(0);
                                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                                    loadRecordDetail();
                                }
                            } else {
                                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                                tblViewTransDetails.getSelectionModel().select(pnDetail);
                                tblViewTransDetails.getFocusModel().focus(pnDetail);
                                loadRecordDetail();
                            }
                            loadRecordMaster();

                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
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
                        String lsMainDate = "";
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Define the format

                        try {
                            if (!poController.PurchaseOrderReceiving().Master().getTransactionDate().equals("")) {
                                Object loDate = poController.PurchaseOrderReceiving().Master().getTransactionDate();
                                if (loDate == null) {
                                    lsMainDate = LocalDate.now().format(formatter); // Convert to String

                                } else if (loDate instanceof Timestamp) {
                                    Timestamp timestamp = (Timestamp) loDate;
                                    LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();

                                    lsMainDate = localDate.format(formatter);
                                } else if (loDate instanceof Date) {
                                    Date sqlDate = (Date) loDate;
                                    LocalDate localDate = sqlDate.toLocalDate();

                                    lsMainDate = localDate.format(formatter);
                                } else {
                                }
                            }
                        } catch (Exception e) {

                        }
                        int numm = 0;
                        if (poController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() > 0) {

                            //pending
                            //retreiving using column index
                            for (int lnCtr = 0; lnCtr <= poController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() - 1; lnCtr++) {

                                try {
                                    main_data.add(new ModelDeliveryAcceptance_Main(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).Supplier().getCompanyName()),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionDate()),
                                            String.valueOf(poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionNo())
                                    ));
                                    numm = (lnCtr);
                                } catch (SQLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                } catch (GuanzonException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                }

                                if (poController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionStatus().equals(PurchaseOrderReceivingStatus.CONFIRMED)) {
                                    JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                                }
                            }
                        }
                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewMainList.getSelectionModel().select(0);
                                tblViewMainList.getFocusModel().focus(0);
                                pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewMainList.getSelectionModel().select(pnMain);
                            tblViewMainList.getFocusModel().focus(pnMain);
                        }
                        if (poController.PurchaseOrderReceiving().getPurchaseOrderCount() < 1) {
                            JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                        }

                    });
                });

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate);
    }

    public void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount);
        JFXUtil.setFocusListener(txtDetail_Focus, tfOrderNo, tfBrand, tfModel, tfBarcode, tfDescription, tfCost, tfReceiveQuantity);
        JFXUtil.setFocusListener(txtField_Focus, tfSearchSupplier, tfSearchReferenceNo);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);

        JFXUtil.initComboBoxCellDesignColor(cmbAttachmentType, "#FF8201");
        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity);
        CustomCommonUtil.inputDecimalOnly(tfDiscountRate);
        JFXUtil.setCommaFormatter(tfDiscountAmount, tfTotal, tfCost);
        // Combobox
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.setOnAction(event -> {
            if (attachment_data.size() > 0) {
                try {
                    int selectedIndex = cmbAttachmentType.getSelectionModel().getSelectedIndex();
                    poController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setDocumentType("000" + String.valueOf(selectedIndex));
                    cmbAttachmentType.getSelectionModel().select(selectedIndex);
                } catch (Exception e) {
                }
            }
        });
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
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewTransDetails.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        stageSerialDialog.closeDialog();
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        if (poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                            tfReceiveQuantity.requestFocus();
                        } else {
                            tfBrand.requestFocus();
                        }
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
                    pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelDeliveryAcceptance_Main) item).getIndex01(), highlightedRowsMain);
        JFXUtil.applyRowHighlighting(tblViewTransDetails, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblViewMainList, tblAttachments);  // need to use computed-size as min-width on particular column to work

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
                    newIndex = moveDown ? JFXUtil.moveToNextRow(currentTable)
                            : JFXUtil.moveToPreviousRow(currentTable);
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

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow3, btnPrint, btnUpdate, btnHistory, btnConfirm, btnVoid);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setDisabled(!lbShow1, btnAddAttachment, btnRemoveAttachment);

        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apAttachments);
        JFXUtil.setButtonsVisibility(false, btnReturn);

        switch (poController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.CONFIRMED:
                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
                if (poController.PurchaseOrderReceiving().Master().isProcessed()) {
                    JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                } else {
                    //JFXUtil.setButtonsVisibility(lbShow3, btnReturn); // hide 
                }
                break;
            case PurchaseOrderReceivingStatus.POSTED:
            case PurchaseOrderReceivingStatus.PAID:
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
            case PurchaseOrderReceivingStatus.RETURNED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnReturn, btnVoid);
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

    public void initAttachmentsGrid() {
        JFXUtil.setColumnCenter(tblRowNoAttachment);
        JFXUtil.setColumnLeft(tblFileNameAttachment);
        JFXUtil.setColumnsIndexAndDisableReordering(tblAttachments);
        tblAttachments.setItems(attachment_data);
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetails);
        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        autoSearch(tfOrderNo);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetails.comparatorProperty());
        tblViewTransDetails.setItems(sortedData);
        tblViewTransDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
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
        if (JFXUtil.isImageViewOutOfBounds(imageView, stackPane1)) {
            JFXUtil.resetImageBounds(imageView, stackPane1);
        }
    }

    public void clearTextFields() {
        imageinfo_temp.clear();
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField);
        JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
    }

    private void autoSearch(TextField txtField) {
        detailSearchListener = (observable, oldValue, newValue) -> {
            int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
            pgPagination.setPageCount(totalPage);
            filteredDataDetail.setPredicate(orders -> {
                lbresetpredicate = true;
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                if (mainSearchListener != null) {
                    JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                    mainSearchListener = null; // Clear reference to avoid memory leaks
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex02().toLowerCase().contains(lowerCaseFilter);
            });
            // If no results and autoSearchMain is enabled, remove listener and trigger autoSearchMain
            if (filteredDataDetail.isEmpty()) {
                if (main_data.size() > 0) {
                    JFXUtil.removeTextFieldListener(detailSearchListener, txtField);
                    filteredData = new FilteredList<>(main_data, b -> true);
                    autoSearchMain(txtField); // Trigger autoSearchMain if no results
                    tblViewMainList.setItems(filteredData);

                    String currentText = txtField.getText();
                    txtField.setText(currentText + " "); // Add a space
                    txtField.setText(currentText);       // Set back to original
                }
            } else {
                if (filteredDataDetail.size() == details_data.size()) {
                    tblViewTransDetails.getSelectionModel().select(pnDetail);
                    tblViewTransDetails.getFocusModel().focus(pnDetail);
                }
            }
        };
        txtField.textProperty().addListener(detailSearchListener);
    }

    private void autoSearchMain(TextField txtField) {
        mainSearchListener = (observable, oldValue, newValue) -> {
            filteredData.setPredicate(orders -> {
                lbresetpredicate = true;
                if (newValue == null || newValue.isEmpty()) {
                    if (mainSearchListener != null) {
                        JFXUtil.removeTextFieldListener(mainSearchListener, txtField);
                        mainSearchListener = null; // Clear reference to avoid memory leaks
                        initDetailsGrid();
                    }
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex04().toLowerCase().contains(lowerCaseFilter);
            });
            pgPagination.setPageCount(1);
        };
        txtField.textProperty().addListener(mainSearchListener);
    }
}
