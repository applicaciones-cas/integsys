/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderReturn_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
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
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
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
import org.guanzon.cas.purchasing.services.PurchaseOrderReturnControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReturnStatus;
import org.json.simple.JSONObject;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReturn;

/**
 * FXML Controller class
 *
 * @author User
 */
public class PurchaseOrderReturn_HistorySPCarController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = "Purchase Order Return History SPCar";
    static PurchaseOrderReturnControllers poPurchaseReturnController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";

    private ObservableList<ModelPurchaseOrderReturn_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelPurchaseOrderReturn_Detail> filteredDataDetail;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;

    @FXML
    private HBox hbButtons, hboxid;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnBrowse, btnPrint, btnHistory, btnClose;

    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfTransactionNo, tfSupplier, tfReferenceNo,
            tfPOReceivingNo, tfTotal, tfBarcode, tfDescription, tfReturnQuantity, tfBrand, tfModel, tfColor,
            tfInventoryType, tfMeasure, tfCost, tfReceiveQuantity;

    @FXML
    private DatePicker dpTransactionDate;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView tblViewDetails;

    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail,
            tblReceiveQuantityDetail, tblReturnQuantityDetail, tblTotalDetail;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        poPurchaseReturnController = new PurchaseOrderReturnControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poPurchaseReturnController.PurchaseOrderReturn().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        loadRecordMaster();
        loadTableDetail();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poPurchaseReturnController.PurchaseOrderReturn().Master().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().Master().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setCategoryId(psCategoryId);
            poPurchaseReturnController.PurchaseOrderReturn().setWithUI(true);
            loadRecordSearch();

        });
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

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnBrowse":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().searchTransaction(psIndustryId, psCompanyId, psCategoryId, tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                        psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();

                        break;
                    case "btnPrint":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().printRecord(() -> {
                            loadRecordMaster();
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
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                }
                initButton(pnEditMode);

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().searchTransaction(psIndustryId, psCompanyId, psCategoryId, tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                break;
                            } else {
                                psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();
                                pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                                loadRecordMaster();
                                loadTableDetail();
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
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtField_Focus, tfSearchSupplier, tfSearchReferenceNo);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity, tfReturnQuantity);
        CustomCommonUtil.inputDecimalOnly(tfCost);

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
dpTransactionDate);
        

    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblReceiveQuantityDetail,tblReturnQuantityDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelPurchaseOrderReturn_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetails.comparatorProperty());
        tblViewDetails.setItems(sortedData);
    }

    public void clearTextFields() {

        dpTransactionDate.setValue(null);

        JFXUtil.clearTextFields(apMaster, apDetail);

        loadRecordMaster();
        loadTableDetail();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Company().getCompanyName() + " - " + poPurchaseReturnController.PurchaseOrderReturn().Master().Industry().getDescription());

            if (psSupplierId.equals("")) {
                tfSearchSupplier.setText("");
            } else {
                tfSearchSupplier.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Supplier().getCompanyName());
            }

            try {
                if (tfSearchReferenceNo.getText() == null || tfSearchReferenceNo.getText().equals("")) {
                    tfSearchReferenceNo.setText("");
                }
            } catch (Exception e) {
                tfSearchReferenceNo.setText("");
            }

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_ConfirmationController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) {
                return;
            }
            tfBarcode.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getUnitPrce(), true));
             tfReceiveQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(pnDetail).intValue()));
            tfReturnQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue()));

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        try {

            Platform.runLater(() -> {
                boolean lbIsReprint = poPurchaseReturnController.PurchaseOrderReturn().Master().getPrint().equals("1") ? true : false;
                if (lbIsReprint) {
                    btnPrint.setText("Reprint");
                } else {
                    btnPrint.setText("Print");
                }
                
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus();
                String lsStat = "UNKNOWN";
                switch (lsActive) {
                    case PurchaseOrderReturnStatus.POSTED:
                        lsStat = "POSTED";
                        break;
                    case PurchaseOrderReturnStatus.PAID:
                        lsStat = "PAID";
                        break;
                    case PurchaseOrderReturnStatus.CONFIRMED:
                        lsStat = "CONFIRMED";
                        break;
                    case PurchaseOrderReturnStatus.OPEN:
                        lsStat = "OPEN";
                        break;
                    case PurchaseOrderReturnStatus.RETURNED:
                        lsStat = "RETURNED";
                        break;
                    case PurchaseOrderReturnStatus.VOID:
                        lsStat = "VOIDED";
                        lbPrintStat = false;
                        break;
                    case PurchaseOrderReturnStatus.CANCELLED:
                        lsStat = "CANCELLED";
                        break;
                    default:
                        lsStat = "UNKNOWN";
                        break;

                }
                lblStatus.setText(lsStat);
                JFXUtil.setButtonsVisibility(lbPrintStat, btnPrint);
            });

            poPurchaseReturnController.PurchaseOrderReturn().computeFields();

            // Transaction Date
            tfTransactionNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfSupplier.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Supplier().getCompanyName());
            tfReferenceNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getReferenceNo());
            tfPOReceivingNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo());
            taRemarks.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getRemarks());

            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionTotal().doubleValue(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView currentTable = (TableView) event.getSource();
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();
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
                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                    tfReturnQuantity.requestFocus();
                } else {
                    tfBarcode.requestFocus();
                }
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                        tfReturnQuantity.requestFocus();
                    } else {
                        tfBarcode.requestFocus();
                    }
                }
            }
        });

        tblViewDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewDetails); // need to use computed-size the column to work
    }

    public void loadTableDetail() {
        // Setting data to table detail

        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    int lnCtr;
                    details_data.clear();
                    try {

                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId() == null || poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                                }
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) < 0) {
                                poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                            }
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReturnController.PurchaseOrderReturn().getDetailCount(); lnCtr++) {
                            try {
                                lnTotal = poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue();
                            } catch (Exception e) {
                            }

                            details_data.add(
                                    new ModelPurchaseOrderReturn_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getDescription()),
                                               String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(lnCtr).intValue()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true))) //identify total
                                    );
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewDetails.getSelectionModel().select(0);
                                tblViewDetails.getFocusModel().focus(0);
                                pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewDetails.getSelectionModel().select(pnDetail);
                            tblViewDetails.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(PurchaseOrderReturn_HistorySPCarController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                } else {
                    tblViewDetails.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    private void initButton(int fnValue) {
        boolean lbShow = fnValue == EditMode.READY;

        JFXUtil.setDisabled(true, apMaster, apDetail);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(lbShow, btnPrint, btnHistory);

        switch (poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus()) {
            case PurchaseOrderReturnStatus.VOID:
            case PurchaseOrderReturnStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnPrint);
                break;
        }
    }
}
