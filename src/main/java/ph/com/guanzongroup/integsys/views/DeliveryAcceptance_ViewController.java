package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Attachment;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Detail;
import ph.com.guanzongroup.integsys.model.ModelDeliveryAcceptance_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReceivingStatus;
import org.json.simple.JSONObject;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReceiving;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class DeliveryAcceptance_ViewController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private final String pxeModuleName = "Purchase Order Receiving History";
    static PurchaseOrderReceiving poController;
    public int pnEditMode;
    private String psTransactionNo = "";
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";

    private ObservableList<ModelDeliveryAcceptance_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelDeliveryAcceptance_Detail> details_data = FXCollections.observableArrayList();
    private final ObservableList<ModelDeliveryAcceptance_Attachment> attachment_data = FXCollections.observableArrayList();
    ObservableList<String> documentType = ModelDeliveryAcceptance_Attachment.documentType;

    private FilteredList<ModelDeliveryAcceptance_Detail> filteredDataDetail;

    private final Map<Integer, List<String>> highlightedRowsDetail = new HashMap<>();

    private ChangeListener<String> detailSearchListener;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage dialogStage = null;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apContent, apMaster, apDetail;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfSupplier, tfTrucking, tfReferenceNo, tfTerm, tfDiscountRate, tfDiscountAmount, tfTotal, tfBrand, tfModel, tfDescription, tfBarcode, tfColor, tfMeasure, tfInventoryType, tfCost, tfOrderQuantity, tfReceiveQuantity, tfOrderNo, tfSupersede;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate, dpExpiryDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewOrderDetails;
    @FXML
    private TableColumn tblRowNoDetail, tblOrderNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblOrderQuantityDetail, tblReceiveQuantityDetail, tblTotalDetail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            poController = new PurchaseOrderReceivingControllers(oApp, null).PurchaseOrderReceiving();
            poJSON = new JSONObject();

            initDatePickers();

            initDetailsGrid();
            initTableOnClick();
            clearTextFields();

            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                CommonUtils.closeStage(btnClose);
            }
            poJSON = poController.OpenTransaction(psTransactionNo);
            if (!"error".equals((String) poJSON.get("result"))) {

                pnEditMode = poController.getEditMode();
            } else {
                Platform.runLater(() -> {
                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                    CommonUtils.closeStage(btnClose);
                });
            }
            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setCategoryId(psCategoryId);
                poController.initFields();
                poController.setWithUI(true);
                loadRecordSearch();
            });
            loadTableDetail();

            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
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

    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }

    public void setPurchaseOrderReceiving(PurchaseOrderReceiving foValue) {
        poController = foValue;
    }

    public void closeSerialDialog() {
        if (dialogStage != null && dialogStage.isShowing()) {
            dialogStage.close();
            dialogStage = null;
        } else {
        }
    }

    public void showSerialDialog() {
        poJSON = new JSONObject();
        try {
            if (!poController.Detail(pnDetail).isSerialized()) {
                return;
            }

            if (poController.Detail(pnDetail).getQuantity().doubleValue() == 0) {
                return;
            }

            //Populate Purchase Order Receiving Detail
            poJSON = poController.getPurchaseOrderReceivingSerial(pnDetail + 1);
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/com/guanzongroup/integsys/views/DeliveryAcceptance_Serial.fxml"));
            DeliveryAcceptance_SerialController controller = new DeliveryAcceptance_SerialController();
            loader.setController(controller);

            if (controller != null) {
                controller.setGRider(oApp);
                controller.setObject(poController);
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
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String tabText = "";

        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {

                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        closeSerialDialog();
                        appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;

                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                        return;
                    }

                    try {
                        poController.ShowStatusHistory();
                        return;
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
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
                loadTableDetail();
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

    public void loadRecordSearch() {
        try {
            lblSource.setText(poController.Master().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poController.getDetailCount() - 1) {
                return;
            }
            boolean lbFields = (poController.Detail(pnDetail).getOrderNo().equals("") || poController.Detail(pnDetail).getOrderNo() == null);
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
            // Expiry Date
            String lsExpiryDate = CustomCommonUtil.formatDateToShortString(poController.Detail(pnDetail).getExpiryDate());
            dpExpiryDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsExpiryDate, "yyyy-MM-dd"));

            tfBarcode.setText(poController.Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poController.Detail(pnDetail).Inventory().getDescription());
            tfSupersede.setText(poController.Detail(pnDetail).Supersede().getBarCode());
            tfBrand.setText(poController.Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poController.Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poController.Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poController.Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poController.Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getUnitPrce(), true));
            tfOrderQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getOrderQty().doubleValue()));
            tfReceiveQuantity.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(pnDetail).getQuantity().doubleValue()));

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }

    }

    public void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poController.Master().getTransactionStatus();
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
            });

            if (poController.Master().getDiscountRate().doubleValue() > 0.00) {
                poController.computeDiscount(poController.Master().getDiscountRate().doubleValue());
            } else {
                if (poController.Master().getDiscount().doubleValue() > 0.00) {
                    poController.computeDiscountRate(poController.Master().getDiscount().doubleValue());
                }
            }
            poController.computeFields();

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            //ReferenceDate
            String lsReferenceDate = CustomCommonUtil.formatDateToShortString(poController.Master().getReferenceDate());
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsReferenceDate, "yyyy-MM-dd"));

            tfTransactionNo.setText(poController.Master().getTransactionNo());

            tfSupplier.setText(poController.Master().Supplier().getCompanyName());
            tfTrucking.setText(poController.Master().Trucking().getCompanyName());
            tfTerm.setText(poController.Master().Term().getDescription());
            tfReferenceNo.setText(poController.Master().getReferenceNo());
            taRemarks.setText(poController.Master().getRemarks());

            Platform.runLater(() -> {
                double lnValue = poController.Master().getDiscountRate().doubleValue();
                if (!Double.isNaN(lnValue)) {
                    tfDiscountRate.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscountRate(), false));
                } else {
                    tfDiscountRate.setText("0.00");
                }
            });

            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getDiscount(), true));
            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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
                            lnCtr = poController.getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poController.Detail(lnCtr).getStockId() == null || poController.Detail(lnCtr).getStockId().equals("")) {
                                    poController.Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poController.getDetailCount() - 1) >= 0) {
                                if (poController.Detail(poController.getDetailCount() - 1).getStockId() != null && !poController.Detail(poController.getDetailCount() - 1).getStockId().equals("")) {
                                    poController.AddDetail();
                                }
                            }

                            if ((poController.getDetailCount() - 1) < 0) {
                                poController.AddDetail();
                            }
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poController.getDetailCount(); lnCtr++) {
                            try {

                                lnTotal = poController.Detail(lnCtr).getUnitPrce().doubleValue() * poController.Detail(lnCtr).getQuantity().doubleValue();

                            } catch (Exception e) {

                            }

                            if (poController.Detail(lnCtr).getOrderQty().doubleValue() != poController.Detail(lnCtr).getQuantity().doubleValue()) {
                                highlight(tblViewOrderDetails, lnCtr + 1, "#FAA0A0", highlightedRowsDetail);
                            }

                            details_data.add(
                                    new ModelDeliveryAcceptance_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poController.Detail(lnCtr).getOrderNo()),
                                            String.valueOf(poController.Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poController.Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getOrderQty().doubleValue())),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Detail(lnCtr).getQuantity().doubleValue())),
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
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
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

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate, dpExpiryDate);
    }

    public void initTableOnClick() {
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
        JFXUtil.adjustColumnForScrollbar(tblViewOrderDetails);  // need to use computed-size as min-width on particular column to work

    }

    private void initButton(int fnValue) {

        boolean lbShow1 = (fnValue == EditMode.UPDATE);
        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons

        btnHistory.setVisible(lbShow3);
        btnHistory.setManaged(lbShow3);

        //Unkown || Ready
        btnClose.setVisible(lbShow4);
        btnClose.setManaged(lbShow4);

        apMaster.setDisable(!lbShow1);
        apDetail.setDisable(!lbShow1);

        switch (poController.Master().getTransactionStatus()) {
            case PurchaseOrderReceivingStatus.VOID:
            case PurchaseOrderReceivingStatus.CANCELLED:
                break;
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
            }

        }
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster, apDetail);
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
