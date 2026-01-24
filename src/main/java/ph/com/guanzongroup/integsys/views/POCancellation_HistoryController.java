package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.views.ScreenInterface;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.purchasing.model.Model_PO_Detail;
import org.guanzon.cas.purchasing.model.Model_PO_Master;
import org.json.simple.JSONObject;
import org.guanzon.cas.purchasing.controller.POCancellation;
import org.guanzon.cas.purchasing.status.POCancellationStatus;
import org.guanzon.cas.purchasing.model.Model_PO_Cancellation_Detail;
import org.guanzon.cas.purchasing.services.POController;

/**
 * FXML Controller class
 *
 * @author User
 */
public class POCancellation_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "PO Cancellation History";
    private String psIndustryID;
    private String psCompanyID;
    private String psCategoryID;
    private Control lastFocusedControl;
    private POCancellation poAppController;
    private ObservableList<Model_PO_Cancellation_Detail> paTransactionDetail;
    private int pnTransactionDetail;

    private unloadForm poUnload = new unloadForm();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apMaster, apDetail,
            apButton, apCenter;

    @FXML
    private TextField tfSearchTransaction, tfSearchSupplier, tfSearchReferNo,
            tfTransactionNo, tfSupplier, tfReferenceNo, tfDestination, tfTransactionAmount,
            tfCancelAmount, tfBarcode, tfDescription, tfBrand, tfModel, tfColor, tfCost, tfOrderQty,
            tfServedQty, tfCanceledQty, tfClass, tfAMC, tfROQ, tfCategory, tfInventoryType, tfMeasure,
            tfQuantity;

    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnBrowse, btnHistory, btnClose;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView<Model_PO_Cancellation_Detail> tblViewDetails;

    @FXML
    private TableColumn<Model_PO_Cancellation_Detail, String> tblColDetailNo, tblColDetailBarcode, tblColDetailDescription,
            tblColDetailOrderQty, tblColDetailCancelQty, tblColDetailCost, tblColDetailTotal;

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
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new POController(poApp, poLogWrapper).POCancellation();

            //remove if not general
            psIndustryID = "";
            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("012347");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);
                System.err.println("Initialize value : Category >" + psCategoryID);
                System.err.println("Initialize value : Company >" + psCompanyID);

            });
            initializeTableDetail();
            initControlEvents();

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(POCancellation_HistoryController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
            }
        }
    }

    @FXML
    void ontblDetailClicked(MouseEvent e) {
        try {
            pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1;
            if (pnTransactionDetail <= 0) {
                return;
            }

            loadSelectedTransactionDetail(pnTransactionDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null));
            }

        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfTransactionNo.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransaction.getText(), true, true),
                                "Initialize Search Source No! ")) {
                            return;
                        }

                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        break;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchTransaction":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransaction.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchSupplier":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Supplier", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchSupplier.getText(), false, false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchReferNo":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Reference", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchReferNo.getText(), true, false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        default:
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfTransactionNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;
                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    break;

                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (Exception e) {
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
            }

        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }

    };

    private boolean isAllTag() throws SQLException, GuanzonException, GuanzonException {

        for (int lnRow = 1; lnRow <= poAppController.getDetailCount(); lnRow++) {
            if (poAppController.getDetail(lnRow).getOrderNo() == null || poAppController.getDetail(lnRow).getOrderNo().isEmpty()) {
                continue;
            }
            double lnOrder = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getQuantity()));
            double lnServed = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getReceivedQuantity()));
            double lnprevCancelled = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getCancelledQuantity()));
            double lnCancelled = poAppController.getDetail(lnRow).getQuantity();

            if (lnOrder != lnServed + lnprevCancelled + lnCancelled) {
                return false;
            }
        }
        return true;
    }
    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea loTextField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }

    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea loTxtField = (TextArea) event.getSource();
        String txtFieldID = ((TextArea) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null));
            }

        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchTransaction":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransaction.getText(), true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchSupplier":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Supplier", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchSupplier.getText(), false, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchReferNo":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Reference", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchReferNo.getText(), true, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                        }
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DeliverySchedule_EntryController.class
                    .getName()).log(Level.SEVERE, null, ex);
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null));
            }

        }
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText(poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription());
            lblStatus.setText(POCancellationStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : POCancellationStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            tfTransactionNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            dpReferenceDate.setValue(ParseDate(poAppController.getMaster().PurchaseOrderMaster().getTransactionDate()));
            tfReferenceNo.setText(poAppController.getMaster().PurchaseOrderMaster().getReference());
            tfSupplier.setText(poAppController.getMaster().PurchaseOrderMaster().Supplier().getCompanyName());
            tfDestination.setText(poAppController.getMaster().PurchaseOrderMaster().Branch().getBranchName());
            taRemarks.setText(String.valueOf(poAppController.getMaster().getRemarks()));
            tfTransactionAmount.setText(CommonUtils.NumberFormat(poAppController.getMaster().PurchaseOrderMaster().getTranTotal(), "###,##0.0000"));
            tfCancelAmount.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotal(), "###,##0.0000"));
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
            }

        }
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfBarcode.setText(tblColDetailBarcode.getCellData(tblIndex));
        tfDescription.setText(tblColDetailDescription.getCellData(tblIndex));
        tfOrderQty.setText(tblColDetailOrderQty.getCellData(tblIndex));
        tfQuantity.setText(tblColDetailCancelQty.getCellData(tblIndex));
        tfCost.setText(tblColDetailCost.getCellData(tblIndex));

        Model_Inventory loDetailInventory = poAppController.getDetail(fnRow).Inventory();
        Model_Inv_Master loDetailInventoryMaster = poAppController.getDetail(fnRow).InventoryMaster();
        Model_PO_Detail loDetailPurchase = poAppController.getDetail(fnRow).PurchaseOrderDetail();
        tfCanceledQty.setText(CommonUtils.NumberFormat(loDetailPurchase.getCancelledQuantity(), "###,###,##0.00"));
        tfBrand.setText(loDetailInventory.Brand().getDescription());
        tfModel.setText(loDetailInventory.Model().getDescription());
        tfColor.setText(loDetailInventory.Model().getDescription());
        tfCategory.setText(loDetailInventory.Category().getDescription());
        tfInventoryType.setText(loDetailInventory.InventoryType().getDescription());
        tfMeasure.setText(loDetailInventory.Measure().getDescription());
        tfClass.setText(loDetailInventoryMaster.getInventoryClassification());
//        tfAMC.setText(String.valueOf(loDetailInventoryMaster.getAverageMonthlySales()));
        tfServedQty.setText(CommonUtils.NumberFormat(loDetailPurchase.getReceivedQuantity(), "###,###,##0.00"));
        recomputeTotal();

    }

    private void recomputeTotal() throws SQLException, GuanzonException {
        double lnTotal = 0.00;
        for (int lnCtr = 1; lnCtr <= poAppController.getDetailCount(); lnCtr++) {
            if (poAppController.getDetail(lnCtr).getOrderNo() == null || poAppController.getDetail(lnCtr).getOrderNo().isEmpty()) {
                continue;
            }
            lnTotal = lnTotal + (poAppController.getDetail(lnCtr).getUnitPrice() * poAppController.getDetail(lnCtr).getQuantity());
        }
        poAppController.getMaster().setTransactionTotal(lnTotal);
        tfTransactionAmount.setText(CommonUtils.NumberFormat(poAppController.getMaster().PurchaseOrderMaster().getTranTotal(), "###,##0.0000"));
        tfCancelAmount.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotal(), "###,##0.0000"));
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtArea_KeyPressed);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);
            }
        }

        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                Object items = table.getItems();

                if (items != null) {
                    // Handle FilteredList or SortedList safely
                    if (items instanceof FilteredList) {
                        table.getItems().clear(); // ✅ Clear the filtered data basedata

                    } else if (items instanceof SortedList) {
                        table.getItems().clear(); //✅ Clear the sorteed  basedata

                    } else if (items instanceof ObservableList) {
                        ((ObservableList<?>) items).clear(); // ✅ Normal ObservableList
                    }

                    table.refresh(); // Update UI
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            }
        }
        initButtonDisplay(poAppController.getEditMode());

    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnBrowse", "btnHistory", "btnClose");

        apMaster.setDisable(!lbShow);
        apDetail.setDisable(!lbShow);
    }

    private void initButtonControls(boolean visible, String... buttonFxIdsToShow) {
        Set<String> showOnly = new HashSet<>(Arrays.asList(buttonFxIdsToShow));

        for (Field loField : getClass().getDeclaredFields()) {
            loField.setAccessible(true);
            String fieldName = loField.getName(); // fx:id

            // Only touch the buttons listed
            if (!showOnly.contains(fieldName)) {
                continue;
            }
            try {
                Object value = loField.get(this);
                if (value instanceof Button) {
                    Button loButton = (Button) value;
                    loButton.setVisible(visible);
                    loButton.setManaged(visible);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());

                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
                }

            }
        }
    }

    private void initializeTableDetail() {
        if (paTransactionDetail == null) {
            paTransactionDetail = FXCollections.observableArrayList();

            tblViewDetails.setItems(paTransactionDetail);

            tblColDetailOrderQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailCancelQty.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailCost.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            tblColDetailTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailBarcode.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getBarCode());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");

                }
            });

            tblColDetailDescription.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().Inventory().getDescription());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            tblColDetailOrderQty.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().PurchaseOrderDetail().getQuantity(), "###,##0.00"));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("0.00");
                }
            });
            tblColDetailCancelQty.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getQuantity(), "###,##0.00"));
            });

            tblColDetailCost.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getUnitPrice(), "###,##0.0000"));
            });
            tblColDetailTotal.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getUnitPrice() * loModel.getValue().getQuantity(), "###,##0.0000"));
            });

        }
    }

    private void reloadTableDetail() {
        List<Model_PO_Cancellation_Detail> rawDetail = poAppController.getDetailList();
        paTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnTransactionDetail >= 1 && pnTransactionDetail < paTransactionDetail.size())
                ? pnTransactionDetail - 1
                : paTransactionDetail.size() - 1;

        tblViewDetails.getSelectionModel().select(indexToSelect);

        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblViewDetails.refresh();
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnTransactionDetail);
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, fsModule + ": " + message));
                }
            }
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }
}
