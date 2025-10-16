/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCode.DOWN;
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
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.constant.DocumentType;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import javafx.scene.input.KeyCode;

/**
 * FXML Controller class
 *
 * @author User
 */
public class POReplacement_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    private final String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass(), "PO");
    static PurchaseOrderReceivingControllers poController;
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
    Map<String, String> imageinfo_temp = new HashMap<>();

    private FileChooser fileChooser;
    private int pnAttachment;

    private int currentIndex = 0;
    private final Map<String, List<String>> highlightedRowsDetail = new HashMap<>();
    JFXUtil.ReloadableTableTask loadTableDetail, loadTableAttachment;
    private final JFXUtil.ImageViewer imageviewerutil = new JFXUtil.ImageViewer();
    private ChangeListener<String> detailSearchListener;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apAttachments;
    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount, tfTotal, tfBrand, tfModel, tfDescription, tfBarcode, tfColor, tfMeasure, tfInventoryType, tfCost, tfOrderQuantity, tfReceiveQuantity, tfOrderNo, tfSupersede, tfAttachmentNo;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnPrint, btnHistory, btnClose, btnArrowLeft, btnArrowRight;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpExpiryDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetails, tblAttachments;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail, tblRowNoAttachment, tblFileNameAttachment;
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

        initDetailsGrid();
        initAttachmentsGrid();
        initTableOnClick();
        clearTextFields();
        Platform.runLater(() -> {
            psIndustryId = "";
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
                        poJSON = poController.PurchaseOrderReceiving().searchTransaction(psIndustryId, psCompanyId, tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }

                        pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                        psCompanyId = poController.PurchaseOrderReceiving().Master().getCompanyId();
                        psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();
                        poController.PurchaseOrderReceiving().loadAttachments();
                        break;
                    case "btnPrint":
                        poJSON = poController.PurchaseOrderReceiving().printRecord(() -> {
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

                if (lsButton.equals("btnPrint")
                        || lsButton.equals("btnArrowRight")
                        || lsButton.equals("btnArrowLeft") || lsButton.equals("btnRetrieve")) {

                } else {
                    loadRecordMaster();
                    loadTableDetail.reload();
                    loadTableAttachment.reload();
                }
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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
                            poJSON = poController.PurchaseOrderReceiving().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poJSON = poController.PurchaseOrderReceiving().searchTransaction(psIndustryId, psCompanyId,
                                    tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                return;
                            } else {
                                psSupplierId = poController.PurchaseOrderReceiving().Master().getSupplierId();
                                pnEditMode = poController.PurchaseOrderReceiving().getEditMode();
                                loadRecordMaster();
                                loadTableDetail.reload();
                                loadTableAttachment.reload();
                                initButton(pnEditMode);
                            }
                            loadRecordSearch();
                            return;
                    }
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
        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordSearch() {
        try {
//            lblSource.setText(poController.PurchaseOrderReceiving().Master().Company().getCompanyName() + " - " + poController.PurchaseOrderReceiving().Master().Industry().getDescription());
            lblSource.setText(poController.PurchaseOrderReceiving().Master().Company().getCompanyName());
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
            // Expiry Date
            String lsExpiryDate = CustomCommonUtil.formatDateToShortString(poController.PurchaseOrderReceiving().Detail(pnDetail).getExpiryDate());
            dpExpiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsExpiryDate, "yyyy-MM-dd"));

            tfBarcode.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Supersede().getBarCode());
            tfBrand.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poController.PurchaseOrderReceiving().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getUnitPrce(), true));
            tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getOrderQty().doubleValue()));
            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(pnDetail).getQuantity().doubleValue()));

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

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            poJSON = poController.PurchaseOrderReceiving().OpenTransaction(poController.PurchaseOrderReceiving().Master().getTransactionNo());
            if ("error".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }

            if (poController.PurchaseOrderReceiving().getEditMode() == EditMode.READY || poController.PurchaseOrderReceiving().getEditMode() == EditMode.UPDATE) {
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
            }

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
                        loadRecordMaster();
                        JFXUtil.disableAllHighlight(tblViewTransDetails, highlightedRowsDetail);
                        details_data.clear();
                        int lnCtr;
                        try {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                lnCtr = poController.PurchaseOrderReceiving().getDetailCount() - 1;
                                while (lnCtr >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId() == null || poController.PurchaseOrderReceiving().Detail(lnCtr).getStockId().equals("")) {
                                        poController.PurchaseOrderReceiving().Detail().remove(lnCtr);
                                    }
                                    lnCtr--;
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) >= 0) {
                                    if (poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId() != null && !poController.PurchaseOrderReceiving().Detail(poController.PurchaseOrderReceiving().getDetailCount() - 1).getStockId().equals("")) {
                                        poController.PurchaseOrderReceiving().AddDetail();
                                    }
                                }

                                if ((poController.PurchaseOrderReceiving().getDetailCount() - 1) < 0) {
                                    poController.PurchaseOrderReceiving().AddDetail();
                                }
                            }

                            double lnTotal = 0.0;
                            for (lnCtr = 0; lnCtr < poController.PurchaseOrderReceiving().getDetailCount(); lnCtr++) {
                                if (poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() == null
                                        || "".equals(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo())) {
                                    continue;
                                }
                                try {
                                    lnTotal = poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce().doubleValue() * poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue();
                                } catch (Exception e) {

                                }

                                if ((!poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo().equals("") && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo() != null)
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue() != poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue()
                                        && poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue() != 0) {
                                    JFXUtil.highlightByKey(tblViewTransDetails, String.valueOf(lnCtr + 1), "#FAA0A0", highlightedRowsDetail);
                                }

                                details_data.add(
                                        new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderNo()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getBarCode()),
                                                String.valueOf(poController.PurchaseOrderReceiving().Detail(lnCtr).Inventory().getDescription()),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(lnCtr).getUnitPrce(), true)),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(lnCtr).getOrderQty().doubleValue())),
                                                String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.PurchaseOrderReceiving().Detail(lnCtr).getQuantity().doubleValue())),
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
                        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    });
                });

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpExpiryDate);
    }

    public void initTextFields() {
        tfSearchSupplier.focusedProperty().addListener(txtField_Focus);
        tfSearchReferenceNo.focusedProperty().addListener(txtField_Focus);

        tfAttachmentNo.focusedProperty().addListener(txtField_Focus);

        tfSearchSupplier.setOnKeyPressed(this::txtField_KeyPressed);
        tfSearchReferenceNo.setOnKeyPressed(this::txtField_KeyPressed);

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
                    pnDetail = tblViewTransDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewTransDetails, item -> ((ModelDeliveryAcceptance_Detail) item).getIndex01(), highlightedRowsDetail);
        JFXUtil.setKeyEventFilter(this::tableKeyEvents, tblViewTransDetails, tblAttachments);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetails, tblAttachments);  // need to use computed-size as min-width on particular column to work
    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        // Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnPrint, btnHistory);
        // Unknown || Ready
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail, apAttachments);

        switch (poController.PurchaseOrderReceiving().Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnPrint);
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

    public void clearTextFields() {
        imageinfo_temp.clear();
        JFXUtil.clearTextFields(apMaster, apDetail, apAttachments);
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
