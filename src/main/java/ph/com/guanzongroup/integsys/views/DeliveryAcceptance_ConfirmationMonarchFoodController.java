package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
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
import java.util.ArrayList;
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
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import java.util.Arrays;
import javafx.util.StringConverter;
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
import javafx.scene.Node;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.constant.DocumentType;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DeliveryAcceptance_ConfirmationMonarchFoodController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private final String pxeModuleName = "Purchase Order Receiving Confirmation MF";
    static PurchaseOrderReceivingControllers poPurchaseReceivingController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    boolean isPrinted = false;
    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;
    private FilteredList<ModelDeliveryAcceptance_Main> filteredData;
    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;
    Map<String, String> imageinfo_temp = new HashMap<>();

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double scaleFactor = 1.0;
    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;
    boolean lbresetpredicate = false;
    private boolean pbEntered = false;

    private final Map<Integer, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();
    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;

    private ChangeListener<String> detailSearchListener;
    private ChangeListener<String> mainSearchListener;

    @FXML
    private AnchorPane apBrowse, apButton, apMainAnchor, apMaster, apDetail, apAttachments, apAttachmentButtons;

    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo,
            tfTransactionNo, tfSupplier, tfTerm, tfTrucking, tfReferenceNo,
            tfDiscountAmount, tfTotal, tfDiscountRate, tfBrand, tfModel,
            tfDescription, tfBarcode, tfColor, tfMeasure, tfInventoryType, tfCost,
            tfOrderQuantity, tfReceiveQuantity, tfOrderNo, tfSupersede, tfAttachmentNo;

    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnPrint,
            btnReturn, btnHistory, btnRetrieve, btnClose, btnAddAttachment,
            btnRemoveAttachment, btnArrowLeft, btnArrowRight;

    @FXML
    private TableView tblViewOrderDetails, tblViewPuchaseOrder, tblAttachments;

    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail,
            tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail,
            tblRowNo, tblSupplier, tblDate, tblReferenceNo,
            tblRowNoAttachment, tblFileNameAttachment;

    @FXML
    private DatePicker dpReferenceDate, dpTransactionDate, dpExpiryDate;

    @FXML
    private HBox hbButtons;

    @FXML
    private Label lblStatus, lblSource;

    @FXML
    private TextArea taRemarks;

    @FXML
    private Pagination pgPagination;

    @FXML
    private StackPane stackPane1;

    @FXML
    private ImageView imageView;
    @FXML
    private ComboBox cmbAttachmentType;

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
        initAttachmentsGrid();
        initTableOnClick();
        clearTextFields();

        Platform.runLater(() -> {
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setIndustryId(psIndustryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCompanyId(psCompanyId);
            poPurchaseReceivingController.PurchaseOrderReceiving().setCategoryId(psCategoryId);
            poPurchaseReceivingController.PurchaseOrderReceiving().initFields();
            poPurchaseReceivingController.PurchaseOrderReceiving().setWithUI(true);
            loadRecordSearch();
        });

        initAttachmentPreviewPane();
        initStackPaneListener();

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
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().printRecord(() -> {
                            if (isPrinted) {
                                disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                                poPurchaseReceivingController.PurchaseOrderReceiving().resetMaster();
                                poPurchaseReceivingController.PurchaseOrderReceiving().resetOthers();
                                poPurchaseReceivingController.PurchaseOrderReceiving().Detail().clear();
                                imageView.setImage(null);
                                pnEditMode = EditMode.UNKNOWN;
                                clearTextFields();
                                initButton(pnEditMode);

                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCategoryCode(psCategoryId);

                            }
                            Platform.runLater(() -> {
                                try {
                                    if (!isPrinted) {
                                        poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo());
                                        poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
                                    }
                                    loadRecordMaster();
                                    loadTableDetail();
                                    loadTableAttachment();
                                } catch (CloneNotSupportedException ex) {
                                    Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (SQLException ex) {
                                    Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (GuanzonException ex) {
                                    Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, null, ex);
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
                        poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();

                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField != null)) {
                            if (lastFocusedTextField instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField;
                                if (Arrays.asList("tfSupplier", "tfTrucking", "tfTerm", "tfBarcode", "tfSearchSupplier", "tfSearchReferenceNo",
                                        "tfDescription", "tfSupersede").contains(tf.getId())) {

                                    if (lastFocusedTextField == previousSearchedTextField) {

                                        break;
                                    }
                                    previousSearchedTextField = lastFocusedTextField;
                                    // Create a simulated KeyEvent for F3 key press
                                    KeyEvent keyEvent = new KeyEvent(
                                            KeyEvent.KEY_PRESSED,
                                            "",
                                            "",
                                            KeyCode.F3,
                                            false, false, false, false
                                    );
                                    tf.fireEvent(keyEvent);
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
                            disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
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
                        disableAllHighlight(tblViewPuchaseOrder, highlightedRowsMain);
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                                loadTableDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus().equals(PurchaseOrderReceivingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                                highlight(tblViewPuchaseOrder, pnMain + 1, "#C1E1C1", highlightedRowsMain);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                // Print Transaction Prompt
                                loJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                                poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
                                loadRecordMaster();
                                isPrinted = false;
                                if ("success".equals(loJSON.get("result"))) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to print this transaction?")) {
                                        isPrinted = true;
                                        btnPrint.fire();
                                    }
                                }
                                if (!isPrinted) {
                                    disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                                }
                            }
                        } else {
                            return;
                        }

                        break;
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                                highlight(tblViewPuchaseOrder, pnMain + 1, "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            if (PurchaseOrderReceivingStatus.CONFIRMED.equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus())) {
                                poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().CancelTransaction("");
                            } else {
                                poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().VoidTransaction("");
                            }
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                                highlight(tblViewPuchaseOrder, pnMain + 1, "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnReturn":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to return transaction?") == true) {
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().ReturnTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                                highlight(tblViewPuchaseOrder, pnMain + 1, "#FAC898", highlightedRowsMain);
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
                            for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount() - 1; lnCtr++) {
                                if (imgPath2.equals(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName())) {
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
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().addAttachment();
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            }
                            pnAttachment = poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount() - 1;
                            poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setFileName(imgPath2);
                            poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().setSourceNo(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
                            poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(pnAttachment).getModel().getTransactionNo();
                            loadTableAttachment();
                            tblAttachments.getFocusModel().focus(pnAttachment);
                            tblAttachments.getSelectionModel().select(pnAttachment);
                        }
                        break;
                    case "btnRemoveAttachment":
//                        attachment_data.remove(pnAttachment);
//                        if (pnAttachment != 0) {
//                            pnAttachment -= 1;
//                        }
//                        loadTableAttachment();
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

                if (lsButton.equals("btnSave") || lsButton.equals("btnConfirm") || lsButton.equals("btnReturn")
                        || lsButton.equals("btnVoid") || lsButton.equals("btnCancel")) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().resetMaster();
                    poPurchaseReceivingController.PurchaseOrderReceiving().resetOthers();
                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail().clear();
                    imageView.setImage(null);
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poPurchaseReceivingController.PurchaseOrderReceiving().Master().setIndustryId(psIndustryId);
                    poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCompanyId(psCompanyId);
                    poPurchaseReceivingController.PurchaseOrderReceiving().Master().setCategoryCode(psCategoryId);
                }

                if (lsButton.equals("btnPrint") || lsButton.equals("btnAddAttachment") || lsButton.equals("btnRemoveAttachment")
                        || lsButton.equals("btnArrowRight") || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                    loadTableAttachment();
                }

                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                        tfReceiveQuantity.requestFocus();
                    } else {
                        tfBarcode.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException | IOException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void retrievePOR() {
        poJSON = new JSONObject();
        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().loadPurchaseOrderReceiving("confirmation", psCompanyId, psSupplierId, tfSearchReferenceNo.getText());
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
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;

        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSupplier":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setSupplierId("");
                    }
                    break;
                case "tfTrucking":
                    if (lsValue.isEmpty()) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTruckingId("");
                    }
                    break;
                case "tfAreaRemarks":
                    break;
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
            }

            loadRecordMaster();
        }

    };

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtField.getId());
        String lsValue = txtField.getText();

        lastFocusedTextField = txtField;
        previousSearchedTextField = null;

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
                        return;
                    }
                    break;
            }
            loadRecordMaster();
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
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfDescription":
                case "tfBarcode":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setStockId("");
                    }

                    break;
                case "tfSupersede":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setReplaceId("");
                    }

                    break;
                case "tfCost":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }

                    if (Double.parseDouble(lsValue.replace(",", "")) < 0.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid Cost Amount");
                        return;
                    }

                    double ldblOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce().doubleValue();
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                        return;
                    }

                    try {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce(ldblOldVal);
                            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
                            return;
                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                    break;
                case "tfReceiveQuantity":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() != null
                            && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo())) {
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().doubleValue() < Double.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Receive quantity cannot be greater than the order quantity.");
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity(0);
                            tfReceiveQuantity.requestFocus();
                            break;
                        }
                    }

                    double lnOldVal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue();
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }

                    try {
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity(lnOldVal);
                            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity()));
                            return;
                        }
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                    if (pbEntered) {
                        moveNext();
                        pbEntered = false;
                    }
                    break;
            }
            Platform.runLater(() -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                delay.setOnFinished(event -> {
                    loadTableDetail();
                });
                delay.play();
            });
        }
    };

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSearchSupplier":
                    if (lsValue.equals("")) {
                        psSupplierId = "";
                    }
                    break;
                case "tfSearchReferenceNo":
                    break;
                case "tfAttachmentNo":
                    break;
                case "":
                    break;
            }
            if (lsTxtFieldID.equals("tfSearchSupplier")
                    || lsTxtFieldID.equals("tfSearchReferenceNo")) {
                loadRecordSearch();
            }
        }
    };

    public void moveNext() {
        double lnReceiveQty = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
        apDetail.requestFocus();
        double lnNewvalue = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
        if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
            tfReceiveQuantity.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewOrderDetails);
            loadRecordDetail();
            tfOrderNo.setText("");
            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                tfReceiveQuantity.requestFocus();
            } else {
                tfBarcode.requestFocus();
            }
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;

            TableView<?> currentTable = tblViewOrderDetails;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfReceiveQuantity":
                            double lnReceiveQty = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
                            apDetail.requestFocus();
                            double lnNewvalue = Double.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().toString());
                            if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId()))) {
                                tfReceiveQuantity.requestFocus();
                            } else {
                                pnDetail = moveToPreviousRow(currentTable, focusedCell);
                                loadRecordDetail();
                                tfOrderNo.setText("");
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                                    tfReceiveQuantity.requestFocus();
                                } else {
                                    tfBarcode.requestFocus();
                                }
                                event.consume();
                            }
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfReceiveQuantity":
                            moveNext();
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
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                            }
                            retrievePOR();
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTransactionNo(lsValue);
                            retrievePOR();
                            return;
                        case "tfTrucking":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchTrucking(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTrucking.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfTerm":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchTerm(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfTerm.setText("");
                                break;
                            }
                            loadRecordMaster();
                            break;
                        case "tfOrderNo":

                            break;
                        case "tfBarcode":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchBarcode(lsValue, true, pnDetail, true);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfDescription":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchDescription(lsValue, false, pnDetail, true);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfSupersede":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupersede(lsValue, true, pnDetail, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupersede.setText("");
                                break;
                            }
                            loadRecordDetail();
                            break;
                    }
                    break;

                case F4:
                    switch (lsID) {
                        case "tfBarcode":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchBarcode(lsValue, true, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfDescription":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchDescription(lsValue, false, pnDetail, false);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId("");
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReceiveQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReceiveQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfSupersede":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupersede(lsValue, true, pnDetail, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupersede.setText("");
                                break;
                            }
                            loadRecordDetail();
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

                lastFocusedTextField = datePicker;
                previousSearchedTextField = null;
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
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
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

                            if (pbSuccess && ((poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
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
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
                            lsRefDate = sdfFormat.format(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
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
                                poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
                    case "dpExpiryDate":
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isBefore(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "The selected date cannot be earlier than the current date.");
                                pbSuccess = false;
                            }
                            if (pbSuccess) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setExpiryDate(SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE));
                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }

                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordDetail();
                            pbSuccess = true; //Set to original value
                        }
                        break;
                    default:

                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ChangeListener<Boolean> datepicker_Focus = (observable, oldValue, newValue) -> {
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        try {
            if (!newValue) { // Lost focus
                DatePicker datePicker = (DatePicker) ((javafx.beans.property.ReadOnlyBooleanProperty) observable).getBean();
                String lsID = datePicker.getId();
                String inputText = datePicker.getEditor().getText();
                LocalDate currentDate = LocalDate.now();
                LocalDate selectedDate = null;

                lastFocusedTextField = datePicker;
                previousSearchedTextField = null;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (inputText != null && !inputText.trim().isEmpty()) {
                    try {
                        LocalDate parsedDate = LocalDate.parse(inputText, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        datePicker.setValue(parsedDate);
                        datePicker.getEditor().setText(formatter.format(parsedDate));
                        inputText = datePicker.getEditor().getText();
                    } catch (DateTimeParseException ignored) {
                    }
                }

                // Check if the user typed something in the text field
                if (inputText != null && !inputText.trim().isEmpty()) {
                    try {
                        selectedDate = LocalDate.parse(inputText, formatter);
                        datePicker.setValue(selectedDate); // Update the DatePicker with the valid date
                    } catch (Exception ex) {
                        poJSON.put("result", "error");
                        poJSON.put("message", "Invalid date format. Please use MM/dd/yyyy format.");
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        loadRecordMaster();
                        // datePicker.requestFocus();
                        return;
                    }
                } else {
                    selectedDate = datePicker.getValue(); // Fallback to selected date if nothing was typed
                }

                String formattedDate = selectedDate.toString();

                switch (lsID) {
                    case "dpTransactionDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isAfter(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Future dates are not allowed.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setTransactionDate((SQLUtil.toDate(formattedDate, "yyyy-MM-dd")));
                        }
                        break;
                    case "dpReferenceDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isAfter(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "Future dates are not allowed.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Master().setReferenceDate(SQLUtil.toDate(formattedDate, "yyyy-MM-dd"));
                        }
                        break;
                    case "dpExpiryDate":
                        if (selectedDate == null) {
                            break;
                        }
                        if (selectedDate.isBefore(currentDate)) {
                            poJSON.put("result", "error");
                            poJSON.put("message", "The selected date cannot be earlier than the current date.");
                        } else {
                            poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setExpiryDate(SQLUtil.toDate(formattedDate, "yyyy-MM-dd"));
                        }
                        break;
                    default:

                        break;
                }
                datePicker.getEditor().setText(formattedDate);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    // datePicker.requestFocus();
                }
                Platform.runLater(() -> {
                    if (lsID.equals("dpExpiryDate")) {
                        loadRecordDetail();
                    } else {
                        loadRecordMaster();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private void loadTab() {
        int totalPage = (int) (Math.ceil(main_data.size() * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(0);
        changeTableView(0, ROWS_PER_PAGE);
        pgPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            changeTableView(newValue.intValue(), ROWS_PER_PAGE);
            tblViewPuchaseOrder.scrollTo(0);
        });
    }

    private void changeTableView(int index, int limit) {
        tblViewPuchaseOrder.getSelectionModel().clearSelection();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, main_data.size());
        int minIndex = Math.min(toIndex, main_data.size());
        try {
            SortedList<ModelDeliveryAcceptance_Main> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tblViewPuchaseOrder.comparatorProperty());
        } catch (Exception e) {
        }
        try {
            tblViewPuchaseOrder.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        } catch (Exception e) {
        }
    }

    public void loadTableMain() {
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewPuchaseOrder.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
//                Thread.sleep(1000);

                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    main_data.clear();
                    disableAllHighlight(tblViewPuchaseOrder, highlightedRowsMain);
                    String lsMainDate = "";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Define the format

                    try {
                        if (!poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate().equals("")) {
                            Object loDate = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate();
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

                    if (poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() > 0) {
                        //pending
                        //retreiving using column index
                        for (int lnCtr = 0; lnCtr <= poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderReceivingCount() - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelDeliveryAcceptance_Main(String.valueOf(lnCtr + 1),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).Supplier().getCompanyName()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionDate()),
                                        String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionNo())
                                ));
                            } catch (SQLException ex) {
                                Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            } catch (GuanzonException ex) {
                                Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            }

                            if (poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(lnCtr).getTransactionStatus().equals(PurchaseOrderReceivingStatus.CONFIRMED)) {
                                highlight(tblViewPuchaseOrder, lnCtr + 1, "#C1E1C1", highlightedRowsMain);
                            }
                        }

                    }
                    if (pnMain < 0 || pnMain
                            >= main_data.size()) {
                        if (!main_data.isEmpty()) {
                            /* FOCUS ON FIRST ROW */
                            tblViewPuchaseOrder.getSelectionModel().select(0);
                            tblViewPuchaseOrder.getFocusModel().focus(0);
                            pnMain = tblViewPuchaseOrder.getSelectionModel().getSelectedIndex();

                        }
                    } else {
                        /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                        tblViewPuchaseOrder.getSelectionModel().select(pnMain);
                        tblViewPuchaseOrder.getFocusModel().focus(pnMain);
                    }
                    if (poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderCount() < 1) {
                        loadTab();
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrder.setPlaceholder(placeholderLabel);
                } else {
                    tblViewPuchaseOrder.toFront();
                }
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewPuchaseOrder.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poPurchaseReceivingController.PurchaseOrderReceiving().Master().Industry().getDescription());

            if (psSupplierId.equals("")) {
                tfSearchSupplier.setText("");
            } else {
                tfSearchSupplier.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
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
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordAttachment(boolean lbloadImage) {
        try {
            if (attachment_data.size() > 0) {
                tfAttachmentNo.setText(String.valueOf(pnAttachment + 1));
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

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) {
                return;
            }
            boolean lbFields = (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo().equals("") || poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() == null) && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;
            tfBarcode.setDisable(!lbFields);
            tfDescription.setDisable(!lbFields);
            if (lbFields) {
                while (tfBarcode.getStyleClass().contains("DisabledTextField") || tfDescription.getStyleClass().contains("DisabledTextField")) {
                    tfBarcode.getStyleClass().remove("DisabledTextField");
                    tfDescription.getStyleClass().remove("DisabledTextField");
                }
            } else {
                tfBarcode.getStyleClass().add("DisabledTextField");
                tfDescription.getStyleClass().add("DisabledTextField");
            }

            if (oApp.getUserLevel() <= UserRight.ENCODER) {
                tfCost.getStyleClass().add("DisabledTextField");
                tfCost.setDisable(true);
            } else {
                while (tfCost.getStyleClass().contains("DisabledTextField")) {
                    tfCost.getStyleClass().remove("DisabledTextField");
                }
                tfCost.setDisable(false);
            }

            // Expiry Date
            String lsExpiryDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getExpiryDate());
            dpExpiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsExpiryDate, "yyyy-MM-dd"));

            tfBarcode.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Supersede().getBarCode());
            tfBrand.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
//            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce()));
            tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty()));
            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity()));

            updateCaretPositions(apDetail);
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbDisable = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE;
        if (lbDisable) {

            tfSupplier.getStyleClass().add("DisabledTextField");
        } else {
            while (tfSupplier.getStyleClass().contains("DisabledTextField")) {

                tfSupplier.getStyleClass().remove("DisabledTextField");
            }
        }

        tfSupplier.setDisable(lbDisable);

        boolean lbIsReprint = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getPrint().equals("1") ? true : false;
        if (lbIsReprint) {
            btnPrint.setText("Reprint");
        } else {
            btnPrint.setText("Print");
        }

        try {
            Platform.runLater(() -> {
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus();
                String lsStat = "UNKNOWN";
                switch (lsActive) {
//                case PurchaseOrderReceivingStatus.APPROVED:
//                    lblStatus.setText("APPROVED");
//                    break;
                    case PurchaseOrderReceivingStatus.POSTED:
                        lsStat = "POSTED";
                        break;
                    case PurchaseOrderReceivingStatus.PAID:
                        lsStat = "PAID";
                        break;
                    case PurchaseOrderReceivingStatus.CONFIRMED:
                        lsStat = "CONFIRMED";
                        break;
                    case PurchaseOrderReceivingStatus.OPEN:
                        lsStat = "OPEN";
                        break;
                    case PurchaseOrderReceivingStatus.RETURNED:
                        lsStat = "RETURNED";
                        break;
                    case PurchaseOrderReceivingStatus.VOID:
                        lsStat = "VOIDED";
                        lbPrintStat = false;
                        break;
                    case PurchaseOrderReceivingStatus.CANCELLED:
                        lsStat = "CANCELLED";
                        break;
                    default:
                        lsStat = "UNKNOWN";
                        break;

                }
                lblStatus.setText(lsStat);
                btnPrint.setVisible(lbPrintStat);
                btnPrint.setManaged(lbPrintStat);
            });

            if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue() > 0.00) {
                poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
            } else {
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue() > 0.00) {
                    poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
                }
            }
            poPurchaseReceivingController.PurchaseOrderReceiving().computeFields();

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            //ReferenceDate
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            tfTransactionNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());

            tfSupplier.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Supplier().getCompanyName());
            tfTrucking.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Trucking().getCompanyName());
            tfTerm.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().Term().getDescription());
            tfReferenceNo.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getReferenceNo());
            taRemarks.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getRemarks());

            Platform.runLater(() -> {
                double lnValue = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue();
                if (!Double.isNaN(lnValue)) {
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate(), false));
                } else {
                    tfDiscountRate.setText("0.00");
                }
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount(), true));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionTotal(), true));

            updateCaretPositions(apMaster);
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void updateCaretPositions(AnchorPane anchorPane) {
        List<TextField> textFields = getAllTextFields(anchorPane);
        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                Pos alignment = textField.getAlignment();
                if (alignment == Pos.CENTER_RIGHT || alignment == Pos.BASELINE_RIGHT
                        || alignment == Pos.TOP_RIGHT || alignment == Pos.BOTTOM_RIGHT) {
                    textField.positionCaret(0); // Caret at start
                } else {
                    if (textField.isFocused()) {
                        textField.positionCaret(text.length()); // Caret at end if focused
                    } else {
                        textField.positionCaret(0); // Caret at start if not focused
                    }
                }
            }
        }
    }

    private List<TextField> getAllTextFields(Parent parent) {
        List<TextField> textFields = new ArrayList<>();

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                textFields.add((TextField) node);
            } else if (node instanceof DatePicker) {
                // Try to find the internal TextField of DatePicker
                Node datePickerEditor = ((DatePicker) node).lookup(".text-field");
                if (datePickerEditor instanceof TextField) {
                    textFields.add((TextField) datePickerEditor);
                }
            } else if (node instanceof Parent) {
                textFields.addAll(getAllTextFields((Parent) node));
            }
        }
        return textFields;
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
        changeTableView(targetPage, ROWS_PER_PAGE);

        Platform.runLater(() -> {
            if (lbresetpredicate) {
                tblViewPuchaseOrder.scrollTo(indexInPage);
                lbresetpredicate = false;
            }
        });
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelDeliveryAcceptance_Main selected = (ModelDeliveryAcceptance_Main) tblViewPuchaseOrder.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                disableAllHighlightByColor(tblViewPuchaseOrder, "#A7C7E7", highlightedRowsMain);
                highlight(tblViewPuchaseOrder, pnRowMain + 1, "#A7C7E7", highlightedRowsMain);

                poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().PurchaseOrderReceivingList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                goToPageBasedOnSelectedRow(String.valueOf(pnMain));
            }
            poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
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

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail
        disableAllHighlight(tblViewOrderDetails, highlightedRowsDetail);

        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
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
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() == null || poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getStockId().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId().equals("")) {
                                    poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                                }
                            }

                            if ((poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount() - 1) < 0) {
                                poPurchaseReceivingController.PurchaseOrderReceiving().AddDetail();
                            }
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                            try {

                                lnTotal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue();

                            } catch (Exception e) {

                            }

                            if ((!poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()
                                    && poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                highlight(tblViewOrderDetails, lnCtr + 1, "#FAA0A0", highlightedRowsDetail);
                            }

                            details_data.add(
                                    new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty())),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity())),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true)) //identify total
                                    ));
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewOrderDetails.getSelectionModel().select(0);
                                tblViewOrderDetails.getFocusModel().focus(0);
                                pnDetail = tblViewOrderDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewOrderDetails.getSelectionModel().select(pnDetail);
                            tblViewOrderDetails.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();

                    } catch (SQLException ex) {
                        Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    } catch (GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(DeliveryAcceptance_ConfirmationMonarchFoodController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewOrderDetails.setPlaceholder(placeholderLabel);
                } else {
                    tblViewOrderDetails.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewOrderDetails.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

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
//                Thread.sleep(1000);
                Platform.runLater(() -> {
                    try {
                        attachment_data.clear();
                        int lnCtr;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getTransactionAttachmentCount(); lnCtr++) {
                            attachment_data.add(
                                    new ModelDeliveryAcceptance_Attachment(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().TransactionAttachmentList(lnCtr).getModel().getFileName())
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

    private void setDatePickerFormat(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });
    }

    private void addKeyEventFilter(DatePicker datePicker) {
        datePicker.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Node source = (Node) event.getSource();
                source.fireEvent(new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        false,
                        false,
                        false,
                        false
                ));
                event.consume();
            }
        });
    }

    public void initDatePickers() {

        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpExpiryDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate, dpExpiryDate);
    }

    public void initTextFields() {

        tfSearchSupplier.focusedProperty().addListener(txtField_Focus);
        tfSearchReferenceNo.focusedProperty().addListener(txtField_Focus);
        tfAttachmentNo.focusedProperty().addListener(txtField_Focus);

        tfTrucking.focusedProperty().addListener(txtMaster_Focus);
        taRemarks.focusedProperty().addListener(txtArea_Focus);
        tfReferenceNo.focusedProperty().addListener(txtMaster_Focus);
        tfTerm.focusedProperty().addListener(txtMaster_Focus);
        tfDiscountRate.focusedProperty().addListener(txtMaster_Focus);
        tfDiscountAmount.focusedProperty().addListener(txtMaster_Focus);

        tfBarcode.focusedProperty().addListener(txtDetail_Focus);
        tfSupersede.focusedProperty().addListener(txtDetail_Focus);
        tfDescription.focusedProperty().addListener(txtDetail_Focus);
        tfCost.focusedProperty().addListener(txtDetail_Focus);
        tfReceiveQuantity.focusedProperty().addListener(txtDetail_Focus);

        TextField[] textFields = {
            tfSearchSupplier, tfSearchReferenceNo, tfTransactionNo, tfSupplier, tfTerm,
            tfTrucking, tfReferenceNo, tfDiscountAmount, tfTotal, tfDiscountRate, tfBrand,
            tfModel, tfDescription, tfBarcode, tfColor, tfMeasure, tfInventoryType, tfCost,
            tfOrderQuantity, tfReceiveQuantity, tfOrderNo, tfSupersede, tfAttachmentNo
        };

        for (TextField textField : textFields) {
            textField.setOnKeyPressed(this::txtField_KeyPressed);
        }
        JFXUtil.initComboBoxCellDesignColor(cmbAttachmentType, "#FF8201");
        CustomCommonUtil.inputDecimalOnly(tfReceiveQuantity, tfDiscountRate, tfDiscountAmount, tfCost, tfReceiveQuantity);
        // Combobox
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

    }

    public void initTableOnClick() {
        tblAttachments.setOnMouseClicked(event -> {
            pnAttachment = tblAttachments.getSelectionModel().getSelectedIndex();
            if (pnAttachment >= 0) {
                scaleFactor = 1.0;
                loadRecordAttachment(true);
                resetImageBounds();
            }
        });

        tblViewOrderDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    ModelDeliveryAcceptance_Detail selected = (ModelDeliveryAcceptance_Detail) tblViewOrderDetails.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnDetail = Integer.parseInt(selected.getIndex01()) - 1;
                        loadRecordDetail();
                        if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                            tfReceiveQuantity.requestFocus();
                        } else {
                            tfBarcode.requestFocus();
                        }
                    }
                }
            }
        });

        tblViewPuchaseOrder.setOnMouseClicked(event -> {
            pnMain = tblViewPuchaseOrder.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    tfOrderNo.setText("");
                    loadTableDetailFromMain();
                    pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });

        tblViewPuchaseOrder.setRowFactory(tv -> new TableRow<ModelDeliveryAcceptance_Main>() {
            @Override
            protected void updateItem(ModelDeliveryAcceptance_Main item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    int rowNo = Integer.valueOf(item.getIndex01()); // Get RowNo from the model
                    List<String> colors = highlightedRowsMain.get(rowNo);
                    if (colors != null && !colors.isEmpty()) {
                        setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        tblViewOrderDetails.setRowFactory(tv -> new TableRow<ModelDeliveryAcceptance_Detail>() {
            @Override
            protected void updateItem(ModelDeliveryAcceptance_Detail item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle(""); // Reset for empty rows
                } else {
                    try {
                        int rowNo = Integer.parseInt(item.getIndex01()); // Assuming getIndex01() returns RowNo
                        List<String> colors = highlightedRowsDetail.get(rowNo);
                        if (colors != null && !colors.isEmpty()) {
                            setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";");
                        } else {
                            setStyle(""); // Default style
                        }
                    } catch (NumberFormatException e) {
                        setStyle(""); // Safe fallback if index is invalid
                    }
                }
            }
        });

        tblViewOrderDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        tblAttachments.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewOrderDetails, tblViewPuchaseOrder, tblAttachments);  // need to use computed-size as min-width on particular column to work

    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        btnSearch.setVisible(lbShow1);
        btnSearch.setManaged(lbShow1);
        btnSave.setVisible(lbShow1);
        btnSave.setManaged(lbShow1);
        btnCancel.setVisible(lbShow1);
        btnCancel.setManaged(lbShow1);

        //Ready
        btnPrint.setVisible(lbShow3);
        btnPrint.setManaged(lbShow3);
        btnUpdate.setVisible(lbShow3);
        btnUpdate.setManaged(lbShow3);
        btnHistory.setVisible(lbShow3);
        btnHistory.setManaged(lbShow3);
        btnConfirm.setVisible(lbShow3);
        btnConfirm.setManaged(lbShow3);
        btnVoid.setVisible(lbShow3);
        btnVoid.setManaged(lbShow3);

        //Unkown || Ready
        btnClose.setVisible(lbShow4);
        btnClose.setManaged(lbShow4);

        btnAddAttachment.setDisable(!lbShow1);
        btnRemoveAttachment.setDisable(!lbShow1);

        apMaster.setDisable(!lbShow1);
        apDetail.setDisable(!lbShow1);
        apAttachments.setDisable(!lbShow1);

        btnReturn.setVisible(false);
        btnReturn.setManaged(false);

        switch (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.CONFIRMED:
                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
                if (poPurchaseReceivingController.PurchaseOrderReceiving().Master().isProcessed()) {
                    btnUpdate.setVisible(false);
                    btnUpdate.setManaged(false);
                    btnVoid.setVisible(false);
                    btnVoid.setManaged(false);
                } else {
                    //btnReturn.setVisible(lbShow3);
                    //btnReturn.setManaged(lbShow3);
                }
                break;
            case PurchaseOrderReceivingStatus.POSTED:
            case PurchaseOrderReceivingStatus.PAID:
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
            case PurchaseOrderReceivingStatus.RETURNED:
                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
                btnUpdate.setVisible(false);
                btnUpdate.setManaged(false);
                btnReturn.setVisible(false);
                btnReturn.setManaged(false);
                btnVoid.setVisible(false);
                btnVoid.setManaged(false);
                break;
        }
    }

//    private void initButton(int fnValue) {
//
//        boolean lbShow1 = (fnValue == EditMode.UPDATE);
//        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
//        boolean lbShow3 = (fnValue == EditMode.READY);
//        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
//        
//        // Manage visibility and managed state of other buttons
//        btnReturn.setVisible(lbShow2);
//        btnReturn.setManaged(lbShow2);
//
//        btnClose.setVisible(lbShow4);
//        btnClose.setManaged(lbShow4);
//
//        btnSearch.setVisible(lbShow1);
//        btnSearch.setManaged(lbShow1);
//        btnSave.setVisible(lbShow1);
//        btnSave.setManaged(lbShow1);
//        btnCancel.setVisible(lbShow1);
//        btnCancel.setManaged(lbShow1);
//
//        btnUpdate.setVisible(lbShow3);
//        btnUpdate.setManaged(lbShow3);
//        
//        btnPrint.setVisible(lbShow3);
//        btnPrint.setManaged(lbShow3);
//        btnHistory.setVisible(lbShow3);
//        btnHistory.setManaged(lbShow3);
//
//        btnConfirm.setVisible(lbShow3);
//        btnConfirm.setManaged(lbShow3);
//        btnVoid.setVisible(lbShow3);
//        btnVoid.setManaged(lbShow3);
//
//        apMaster.setDisable(!lbShow1);
//        apDetail.setDisable(!lbShow1);
//        apAttachments.setDisable(!lbShow1);
//
//        btnAddAttachment.setDisable(!lbShow1);
//        btnRemoveAttachment.setDisable(!lbShow1);
//        
//        switch (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
//            case PurchaseOrderReceivingStatus.APPROVED: 
//                btnConfirm.setVisible(!lbShow3);
//                btnConfirm.setManaged(!lbShow3);
//                break;
//            case PurchaseOrderReceivingStatus.VOID:
//                btnVoid.setVisible(!lbShow3);
//                btnVoid.setManaged(!lbShow3);
//                break;
//        }
//    }
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

        tblRowNoAttachment.setCellValueFactory(new PropertyValueFactory<ModelDeliveryAcceptance_Attachment, String>("index01"));
        tblFileNameAttachment.setCellValueFactory(new PropertyValueFactory<ModelDeliveryAcceptance_Attachment, String>("index02"));

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
            ModelDeliveryAcceptance_Attachment image = attachment_data.get(newIndex);
            String filePath2 = System.getProperty("sys.default.path.temp") + "/Attachments//" + image.getIndex02();
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

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblOrderNoDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewOrderDetails);
        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        autoSearch(tfOrderNo);

        SortedList<ModelDeliveryAcceptance_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewOrderDetails.comparatorProperty());
        tblViewOrderDetails.setItems(sortedData);
        tblViewOrderDetails.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewPuchaseOrder);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewPuchaseOrder.setItems(filteredData);
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

    private int moveToNextRow(TableView table, TablePosition focusedCell) {
        int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
        table.getSelectionModel().select(nextRow);
        return nextRow;
    }

    private int moveToPreviousRow(TableView table, TablePosition focusedCell) {
        int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
        table.getSelectionModel().select(previousRow);
        return previousRow;
    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblViewOrderDetails":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnDetail = moveToNextRow(currentTable, focusedCell);
                                break;
                            case UP:
                                pnDetail = moveToPreviousRow(currentTable, focusedCell);
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
                                pnAttachment = moveToNextRow(currentTable, focusedCell);
                                break;
                            case UP:
                                pnAttachment = moveToPreviousRow(currentTable, focusedCell);
                                break;

                            default:
                                break;
                        }
                        loadRecordAttachment(true);
                        event.consume();
                    }
                    break;
            }

        }
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

    public void clearTextFields() {
        imageinfo_temp.clear();
        previousSearchedTextField = null;
        lastFocusedTextField = null;
        dpTransactionDate.setValue(null);
        dpReferenceDate.setValue(null);
        dpExpiryDate.setValue(null);

        psSupplierId = "";
        tfSearchSupplier.clear();
        tfSearchReferenceNo.clear();
        tfAttachmentNo.clear();

        tfTransactionNo.clear();

        tfSupplier.clear();
        tfTrucking.clear();
        taRemarks.clear();
        tfReferenceNo.clear();
        tfTerm.clear();
        tfDiscountRate.clear();
        tfDiscountAmount.clear();
        tfTotal.clear();
        tfOrderNo.clear();
        tfBarcode.clear();
        tfSupersede.clear();
        tfDescription.clear();
        tfBrand.clear();
        tfModel.clear();
        tfColor.clear();
        tfInventoryType.clear();
        tfMeasure.clear();
        tfCost.clear();
        tfOrderQuantity.clear();
        tfReceiveQuantity.clear();

        tfAttachmentNo.clear();

//        loadRecordMaster();
//        loadTableDetail();
//        loadTableMain();
    }

    public void generateAttachment() {
        attachment_data.add(new ModelDeliveryAcceptance_Attachment("0", "C:/Users/User/Downloads/a4-blank-template_page-0001.jpg"));

    }

// Generic method to highlight with specific color
    public <T> void highlight(TableView<T> table, int rowIndex, String color, Map<Integer, List<String>> highlightMap) {
        highlightMap.computeIfAbsent(rowIndex, k -> new ArrayList<>()).add(color);
        table.refresh(); // Refresh to apply changes
    }

// Generic method to remove highlight from a specific row
    public <T> void disableHighlight(TableView<T> table, int rowIndex, Map<Integer, List<String>> highlightMap) {
        highlightMap.remove(rowIndex);
        table.refresh();
    }

// Generic method to remove all highlights
    public <T> void disableAllHighlight(TableView<T> table, Map<Integer, List<String>> highlightMap) {
        highlightMap.clear();
        table.refresh();
    }

// Generic method to remove all highlights of a specific color
    public <T> void disableAllHighlightByColor(TableView<T> table, String color, Map<Integer, List<String>> highlightMap) {
        highlightMap.forEach((key, colors) -> colors.removeIf(c -> c.equals(color)));
        highlightMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        table.refresh();
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
                    tblViewPuchaseOrder.setItems(filteredData);

                    String currentText = txtField.getText();
                    txtField.setText(currentText + " "); // Add a space
                    txtField.setText(currentText);       // Set back to original
                }
            } else {
                if (filteredDataDetail.size() == details_data.size()) {
                    tblViewOrderDetails.getSelectionModel().select(pnDetail);
                    tblViewOrderDetails.getFocusModel().focus(pnDetail);
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
