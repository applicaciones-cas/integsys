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
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.constant.DocumentType;

/**
 * FXML Controller class
 *
 * @author User
 */
public class DeliveryAcceptance_HistoryAppliancesController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private final String pxeModuleName = "Purchase Order Receiving History Appliances";
    static PurchaseOrderReceivingControllers poPurchaseReceivingController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";

    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;

    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double scaleFactor = 1.0;
    private FileChooser fileChooser;
    private int pnAttachment;

    private double xOffset = 0;
    private double yOffset = 0;
    private int currentIndex = 0;
    double ldstackPaneWidth = 0;
    double ldstackPaneHeight = 0;

    private final Map<Integer, List<String>> highlightedRowsMain = new HashMap<>();
    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();

    private Stage dialogStage = null;
    private ChangeListener<String> detailSearchListener;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apAttachments, apAttachmentButtons;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnPrint, btnHistory, btnClose, btnSerials;
    @FXML
    private Label lblStatus, lblSource;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate,
            tfDiscountAmount, tfTotal, tfOrderNo, tfBrand, tfModel, tfColor, tfInventoryType,
            tfMeasure, tfCost, tfOrderQuantity, tfReceiveQuantity, tfModelVariant, tfBarcode, tfDescription;
    @FXML
    private TextArea taRemarks;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TableView tblViewOrderDetails, tblViewPuchaseOrder;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail,
            tblReceiveQuantityDetail, tblTotalDetail;

    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo;
    @FXML
    private TextField tfAttachmentNo;
    @FXML
    private TableView tblAttachments;
    @FXML
    private TableColumn tblRowNoAttachment, tblFileNameAttachment;
    @FXML
    private StackPane stackPane1;
    @FXML
    private ImageView imageView;
    @FXML
    private ComboBox cmbAttachmentType;
    @FXML
    private Button btnArrowLeft, btnArrowRight;

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

        Object source = event.getSource();
        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().searchTransaction(psIndustryId, psCompanyId, tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }

                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        psCompanyId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getCompanyId();
                        psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                        poPurchaseReceivingController.PurchaseOrderReceiving().loadAttachments();
                        break;
                    case "btnPrint":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().printRecord(() -> {
                            loadTableDetailFromMain();
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
                    case "btnSerials":
                        if (!poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).isSerialized()) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Selected item is not serialized.");
                            return;
                        }
                        showSerialDialog();
                        return;
                    case "btnUpdate":
                        poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode();
                        break;
                    case "btnHistory":
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
                initButton(pnEditMode);

                if (lsButton.equals("btnPrint") || lsButton.equals("btnArrowRight")
                        || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve")) {

                } else {
                    loadRecordMaster();
                    loadTableDetail();
                    loadTableAttachment();
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void showSerialDialog() {
        poJSON = new JSONObject();
        try {
            if (!poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).isSerialized()) {
                return;
            }

            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue() == 0) {
                return;
            }

            //Populate Purchase Order Receiving Detail
            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().getPurchaseOrderReceivingSerial(pnDetail + 1);
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }

//             Check if the dialog is already open
            if (dialogStage != null) {
                if (dialogStage.isShowing()) {
                    dialogStage.toFront();
                    return;
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/DeliveryAcceptance_SerialAppliances.fxml"));
            DeliveryAcceptance_SerialAppliancesController controller = new DeliveryAcceptance_SerialAppliancesController();
            loader.setController(controller);

            if (controller != null) {
                controller.setGRider(oApp);
                controller.setObject(poPurchaseReceivingController.PurchaseOrderReceiving());
                controller.setEntryNo(pnDetail + 1);
                controller.isFinancing(true);
            }

            Parent root = loader.load();

            // Handle drag events for the undecorated window
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Inventory Serial");
            dialogStage.setScene(new Scene(root));

            // Clear the reference when closed
            dialogStage.setOnHidden(event -> dialogStage = null);
            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void closeSerialDialog() {
        if (dialogStage != null && dialogStage.isShowing()) {
            dialogStage.close();
            dialogStage = null;
        } else {
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
                    if (Double.parseDouble(lsValue.replace(",", "")) < 0.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid Discount Rate");
                        return;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscountRate((Double.valueOf(lsValue)));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscount(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscountRate().doubleValue());
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    break;
                case "tfDiscountAmount":
                    if (lsValue.isEmpty()) {
                        lsValue = "0.00";
                    }
                    if (Double.parseDouble(lsValue.replace(",", "")) < 0.00) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid Discount Amount");
                        return;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Master().setDiscount(Double.valueOf(lsValue.replace(",", "")));
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }

                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().computeDiscountRate(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getDiscount().doubleValue());
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
                        ShowMessageFX.Warning(null, pxeModuleName, "Invalid Downpayment Amount");
                        return;
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setUnitPrce((Double.valueOf(lsValue.replace(",", ""))));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }

                    break;
                case "tfReceiveQuantity":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setQuantity((Integer.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    break;
            }
            loadTableDetail();
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
                        psSupplierId = "";
                    }
                    break;
                case "tfSearchReferenceNo":
                    break;
            }
            if (lsTxtFieldID.equals("tfSearchSupplier") || lsTxtFieldID.equals("tfSearchReferenceNo")) {
                loadRecordSearch();
            }
        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lsID) {
                        case "tfSearchSupplier":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getSupplierId();
                            }
                            loadRecordSearch();
                            return;

                        case "tfSearchReferenceNo":
                            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().searchTransaction(psIndustryId, psCompanyId,
                                    tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                return;
                            }
                            loadRecordSearch();
                            break;

                    }
                    loadRecordMaster();
                    loadTableDetail();
                    break;
                default:
                    break;
            }

            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
                        String filePath2 = System.getProperty("sys.default.path.temp") + "/Attachments//" + (String) attachment_data.get(pnAttachment).getIndex02();
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
            boolean lbFields = (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo().equals("") || poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderNo() == null);
            tfBrand.setDisable(!lbFields);
            tfModel.setDisable(!lbFields);
            tfBarcode.setDisable(!lbFields);
            tfDescription.setDisable(!lbFields);
            if (lbFields) {
                while (tfBrand.getStyleClass().contains("DisabledTextField") || tfModel.getStyleClass().contains("DisabledTextField")) {
                    tfBrand.getStyleClass().remove("DisabledTextField");
                    tfModel.getStyleClass().remove("DisabledTextField");
                    tfBarcode.getStyleClass().remove("DisabledTextField");
                    tfDescription.getStyleClass().remove("DisabledTextField");
                }

            } else {
                tfBarcode.getStyleClass().add("DisabledTextField");
                tfDescription.getStyleClass().add("DisabledTextField");
                tfBrand.getStyleClass().add("DisabledTextField");
                tfModel.getStyleClass().add("DisabledTextField");
            }

            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId() != null && !poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getStockId().equals("")) {
                poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).setBrandId(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBrandId());
            }
            tfBarcode.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Brand().getDescription());
            tfModelVariant.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Variant().getDescription());

            tfModel.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
            tfOrderQuantity.setText(String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().intValue()));
            tfReceiveQuantity.setText(String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().intValue()));

        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbIsReprint = poPurchaseReceivingController.PurchaseOrderReceiving().Master().getPrint().equals("1") ? true : false;
        if (lbIsReprint && (PurchaseOrderReceivingStatus.CONFIRMED.equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus())
                || PurchaseOrderReceivingStatus.PAID.equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus())
                || PurchaseOrderReceivingStatus.POSTED.equals(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()))) {
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
                        lbPrintStat = false;
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

        } catch (SQLException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            poJSON = poPurchaseReceivingController.PurchaseOrderReceiving().OpenTransaction(poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionNo());
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }

            if (poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.READY || poPurchaseReceivingController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
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
            }

        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetail() {
        // Setting data to table detail
        loadRecordMaster();
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
                            while (lnCtr > 0) {
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
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReceivingController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                            try {
                                lnTotal = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue();

                            } catch (Exception e) {

                            }

                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()) {
                                highlight(tblViewOrderDetails, lnCtr + 1, "#FAA0A0", highlightedRowsDetail);
                            }

                            String lsBrand = "";
                            if (poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription() != null) {
                                lsBrand = poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Brand().getDescription();
                            }
                            details_data.add(
                                    new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().intValue()),
                                            String.valueOf(poPurchaseReceivingController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().intValue()),
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

                    } catch (SQLException ex) {
                        Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    } catch (GuanzonException ex) {
                        Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(DeliveryAcceptance_HistoryAppliancesController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

    public void initDatePickers() {

        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate);

    }

    public void initTextFields() {

        tfSearchSupplier.focusedProperty().addListener(txtField_Focus);
        tfSearchReferenceNo.focusedProperty().addListener(txtField_Focus);

        tfSearchSupplier.setOnKeyPressed(this::txtField_KeyPressed);
        tfSearchReferenceNo.setOnKeyPressed(this::txtField_KeyPressed);

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
                    pnDetail = tblViewOrderDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
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
        JFXUtil.adjustColumnForScrollbar(tblViewOrderDetails, tblAttachments);  // need to use computed-size as min-width on particular column to work

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

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Ready
        btnPrint.setVisible(lbShow3);
        btnPrint.setManaged(lbShow3);

        btnHistory.setVisible(lbShow3);
        btnHistory.setManaged(lbShow3);

        btnSerials.setVisible(lbShow3);
        btnSerials.setManaged(lbShow3);

        //Unkown || Ready
        btnClose.setVisible(lbShow4);
        btnClose.setManaged(lbShow4);

        apMaster.setDisable(!lbShow1);
        apDetail.setDisable(!lbShow1);
        apAttachments.setDisable(!lbShow1);

        switch (poPurchaseReceivingController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
                btnPrint.setVisible(false);
                btnPrint.setManaged(false);
                break;
        }

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

        dpTransactionDate.setValue(null);
        dpReferenceDate.setValue(null);

        tfSearchSupplier.clear();
        tfSearchReferenceNo.clear();

        tfAttachmentNo.clear();
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.getSelectionModel().select(0);

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
        tfDescription.clear();
        tfModelVariant.clear();
        tfBrand.clear();
        tfModel.clear();
        tfColor.clear();
        tfInventoryType.clear();
        tfMeasure.clear();
        tfCost.clear();
        tfOrderQuantity.clear();
        tfReceiveQuantity.clear();

        tfAttachmentNo.clear();
        cmbAttachmentType.setItems(documentType);
        cmbAttachmentType.getSelectionModel().select(0);
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
            filteredDataDetail.setPredicate(orders -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return orders.getIndex02().toLowerCase().contains(lowerCaseFilter);
            });
            // If no results and autoSearchMain is enabled, remove listener and trigger autoSearchMain
        };
        txtField.textProperty().addListener(detailSearchListener);
    }

}
