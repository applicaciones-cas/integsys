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
public class POCancellation_EntryControllerCar_SP implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "PO Cancellation Entry";
    private String psIndustryID;
    private String psCompanyID;
    private String psCategoryID;
    private Control lastFocusedControl;
    private POCancellation poAppController;
    private ObservableList<Model_PO_Master> paPurchaseOrder;
    private ObservableList<Model_PO_Cancellation_Detail> paTransactionDetail;
    private FilteredList<Model_PO_Master> filteredPurchaseOrder;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail, pgRowSelect;

    private static final int ROWS_PER_PAGE = 50;

    private unloadForm poUnload = new unloadForm();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apMaster, apDetail,
            apButton, apCenter, apTransaction;

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
    private Button btnSearch, btnBrowse, btnNew, btnCancel, btnTag, btnHistory, btnUpdate, btnSave,
            btnRetrieve, btnClose;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView<Model_PO_Cancellation_Detail> tblViewDetails;

    @FXML
    private TableColumn<Model_PO_Cancellation_Detail, String> tblColDetailNo, tblColDetailBarcode, tblColDetailDescription,
            tblColDetailOrderQty, tblColDetailCancelQty, tblColDetailCost, tblColDetailTotal;

    @FXML
    private TableView<Model_PO_Master> tblTransaction;

    @FXML
    private TableColumn<Model_PO_Master, String> tblColNo, tblColTransactionNo, tblColDate, tblColReference, tblColItems;

    @FXML
    private Pagination pgTransaction;

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
            poAppController.setTransactionStatus(POCancellationStatus.OPEN);

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("07");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                poAppController.setCompanyID(psCompanyID);
                poAppController.setCategoryID(psCategoryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);
                System.err.println("Initialize value : Category >" + psCategoryID);
                System.err.println("Initialize value : Company >" + psCompanyID);

                btnNew.fire();
            });
            initializeTableDetail();
            initializeTablePurchase();
            initControlEvents();

        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(POCancellation_EntryControllerCar_SP.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
            }
        }
    }

    @FXML
    void tblTransaction_MouseClicked(MouseEvent e) {
        pnSelectMaster = tblTransaction.getSelectionModel().getSelectedIndex();
        if (pnSelectMaster < 0) {
            return;
        }
        pgRowSelect = pnSelectMaster + pgTransaction.getCurrentPageIndex() * ROWS_PER_PAGE;
        if (pgRowSelect < 0) {
            return;
        }

        if (e.getClickCount() == 2 && !e.isConsumed()) {
            try {
                if (poAppController.getEditMode() != EditMode.ADDNEW
                        && poAppController.getEditMode() != EditMode.UPDATE) {
                    ShowMessageFX.Information("Please enter to update or create a new transaction. Thank you!", psFormName, null);
                    return;
                }
                if (poAppController.getMaster().getSourceNo() != null) {
                    if (!poAppController.getMaster().getSourceNo().isEmpty()) {

                        if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                            return;
                        }
                    }
                }

                e.consume();
                if (!isJSONSuccess(poAppController.replaceDetail(pgRowSelect), psFormName)) {
//                    ShowMessageFX.Information("Failed to add detail", psFormName, null);
                    return;
                }

                getLoadedTransaction();
                initButtonDisplay(poAppController.getEditMode());
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {

                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null));
                }

            }

        }

        return;
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
                case "btnSearch":
                    if (lastFocusedControl == null) {
                        ShowMessageFX.Information(null, psFormName,
                                "Search unavailable. Please ensure a searchable field is selected or focused before proceeding..");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfReferenceNo":
                            if (!isJSONSuccess(poAppController.searchTransactionOrder(tfReferenceNo.getText().trim(), true, false),
                                    "Initialize Search Reference! ")) {
                                return;
                            }
                            loadTransactionMaster();
                            break;
                        case "tfBarcode":
                            if (!isJSONSuccess(poAppController.searchDetailByPO(pnTransactionDetail, tfBarcode.getText().trim(), true),
                                    "Initialize Search Barcode! ")) {
                                return;
                            }
                            loadTransactionMaster();
                            break;
                        case "tfDescription":
                            if (!isJSONSuccess(poAppController.searchDetailByPO(pnTransactionDetail, tfDescription.getText(), false),
                                    "Initialize Search Description! ")) {
                                return;
                            }
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnTransactionDetail);
                            break;

                    }
                    break;

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
                case "btnNew":
                    if (!isJSONSuccess(poAppController.NewTransaction(), "Initialize New Transaction")) {
                        return;
                    }
                    clearAllInputs();
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnUpdate":
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, "");
                        return;
                    }
                    poAppController.OpenTransaction(poAppController.getMaster().getTransactionNo());
                    if (!isJSONSuccess(poAppController.UpdateTransaction(), "Initialize UPdate Transaction")) {
                        return;
                    }

                    if (!isJSONSuccess(poAppController.retrieveDetail(), "Initialize retrieve Purchase Transaction transaction")) {

                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;

                case "btnSave":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, "");
                        return;
                    }

                    if (!isJSONSuccess(poAppController.SaveTransaction(), "Initialize Save Transaction")) {
                        return;
                    }

                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to Confirm transaction?") == true) {
                        if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }

                    }

                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new POController(poApp, poLogWrapper).POCancellation();
                        poAppController.setTransactionStatus("07");

                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {

                            poAppController.setTransactionStatus("07");
                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);
                            poAppController.setCompanyID(psCompanyID);
                            poAppController.setCategoryID(psCategoryID);
                            System.err.println("Initialize value : Industry >" + psIndustryID);
                            System.err.println("Initialize value : Category >" + psCategoryID);
                            System.err.println("Initialize value : Company >" + psCompanyID);

                            clearAllInputs();
                        });
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;

                case "btnHistory":
                    ShowMessageFX.Information(null, psFormName,
                            "This feature is under development and will be available soon.\nThank you for your patience!");
                    break;
                case "btnTag":
                    if (btnTag.getText().toLowerCase().contains("untag")) {
                        untagDetailAll();
                    } else {
                        tagDetailAll();
                    }
                    reloadTableDetail();
                    loadSelectedTransactionDetail(pnTransactionDetail);
                    break;
                case "btnRetrieve":
//                    loadRetrieveFilter();
                    loadTransactionPurchaseList("a.sTransNox", "%");
                    reloadTablePurchase();
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
        try {
            if (lsValue == null) {
                return;
            }

            if (!nv) {
                /*Lost Focus*/
                switch (lsTextFieldID) {
                    case "tfQuantity":
                        if (poAppController.getDetail(pnTransactionDetail).getStockId() == null
                                || poAppController.getDetail(pnTransactionDetail).getStockId().isEmpty()) {
                            if (Double.parseDouble(tfQuantity.getText()) > 0.0) {
                                tfQuantity.setText("0.00");
                                loTextField.requestFocus();
                                ShowMessageFX.Information("Unable to set quantity! No Stock Invetory Detected", psFormName, null);
                            }
                            return;
                        }
                        double lnCancelledQty;
                        try {
                            lnCancelledQty = Double.parseDouble(lsValue);
                        } catch (NumberFormatException e) {
                            lnCancelledQty = 0.0; // default if parsing fails
                            poAppController.getDetail(pnTransactionDetail).setQuantity(lnCancelledQty);
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnTransactionDetail);
                            loTextField.requestFocus();
                        }
                        if (lnCancelledQty < 0.00) {
                            return;
                        }

                        double lnOrder = Double.valueOf(String.valueOf(poAppController.getDetail(pnTransactionDetail).PurchaseOrderDetail().getQuantity()));
                        double lnServed = Double.valueOf(String.valueOf(poAppController.getDetail(pnTransactionDetail).PurchaseOrderDetail().getReceivedQuantity()));
                        double lnprevCancelled = Double.valueOf(String.valueOf(poAppController.getDetail(pnTransactionDetail).PurchaseOrderDetail().getCancelledQuantity()));

                        if ((lnCancelledQty
                                + lnprevCancelled + lnServed)
                                > lnOrder) {

                            lnCancelledQty = lnOrder - (lnprevCancelled + lnServed);
                            ShowMessageFX.Information("Cancelled Quantity exceed Order Qty Detected", psFormName, null);
                            loTextField.setText(String.valueOf(lnCancelledQty));
                        }

                        poAppController.getDetail(pnTransactionDetail).setQuantity(lnCancelledQty);

                        reloadTableDetail();
                        loadSelectedTransactionDetail(pnTransactionDetail);
                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null);
            } else {
                Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(ex), psFormName, null));
            }
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

                case "taRemarks":
                    poAppController.getMaster().setRemarks(lsValue);
                    loadTransactionMaster();

                    break;

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
                            case "tfReferenceNo":
                                if (!isJSONSuccess(poAppController.searchTransactionOrder(tfReferenceNo.getText().trim(), true, false),
                                        "Initialize Search Reference! ")) {
                                    return;
                                }
                                loadTransactionMaster();
                                break;
                            case "tfBarcode":
                                if (!isJSONSuccess(poAppController.searchDetailByPO(pnTransactionDetail, tfBarcode.getText().trim(), true),
                                        "Initialize Search Department! ")) {
                                    return;
                                }
                                loadTransactionMaster();
                                break;
                            case "tfDescription":
                                if (!isJSONSuccess(poAppController.searchDetailByPO(pnTransactionDetail, tfDescription.getText(), false),
                                        "Initialize Search Check! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
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

    private void loadTransactionPurchaseList(String fsColumn, String fsValue) {
        StackPane overlay = getOverlayProgress(apTransaction);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_PO_Master>> loadPurchase = new Task<ObservableList<Model_PO_Master>>() {
            @Override
            protected ObservableList<Model_PO_Master> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadPurchaseOrderList(fsColumn, fsValue),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_PO_Master> rawList = poAppController.getPurchaseOrderList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                reloadTablePurchase();
                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                Logger
                        .getLogger(DeliverySchedule_EntryController.class
                                .getName()).log(Level.SEVERE, null, ex);
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadPurchase);
        thread.setDaemon(true);
        thread.start();
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

        if (isAllTag()) {
            btnTag.setText("Untag All");
        } else {
            btnTag.setText("Tag All");
        }
    }

    private void tagDetailAll() throws SQLException, GuanzonException {
        for (int lnRow = 1; lnRow <= poAppController.getDetailCount(); lnRow++) {
            if (poAppController.getDetail(lnRow).getOrderNo() == null || poAppController.getDetail(lnRow).getOrderNo().isEmpty()) {
                continue;
            }
            double lnOrder = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getQuantity()));
            double lnServed = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getReceivedQuantity()));
            double lnprevCancelled = Double.valueOf(String.valueOf(poAppController.getDetail(lnRow).PurchaseOrderDetail().getCancelledQuantity()));

            poAppController.getDetail(lnRow).setQuantity(lnOrder - (lnServed + lnprevCancelled));
        }
        if (isAllTag()) {
            btnTag.setText("Untag All");
        } else {
            btnTag.setText("Tag All");
        }

    }

    private void untagDetailAll() throws SQLException, GuanzonException {
        for (int lnRow = 1; lnRow <= poAppController.getDetailCount(); lnRow++) {
            if (poAppController.getDetail(lnRow).getOrderNo() == null || poAppController.getDetail(lnRow).getOrderNo().isEmpty()) {
                continue;
            }
            poAppController.getDetail(lnRow).setQuantity(0.0d);
        }

        if (isAllTag()) {
            btnTag.setText("Untag All");
        } else {
            btnTag.setText("Tag All");
        }

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

        pgTransaction.setPageFactory(this::pageTransaction);
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
                        paPurchaseOrder.clear(); // ✅ Clear the filtered data basedata

                    } else if (items instanceof SortedList) {
                        paPurchaseOrder.clear(); //✅ Clear the sorteed  basedata

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
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());

    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnRetrieve", "btnHistory", "btnClose");

        // Show-only based on mode
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnTag", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnNew", "btnUpdate");

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

    private void initializeTablePurchase() {
        if (paPurchaseOrder == null) {
            paPurchaseOrder = FXCollections.observableArrayList();

//            tblTransaction.setItems(laPurchaseOrder);
            tblColNo.setCellValueFactory(loModel -> {
                int index = tblTransaction.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });
            tblColTransactionNo.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionNo()));
            });
            tblColDate.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
            });
            tblColReference.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getReference()));
            });
            tblColItems.setCellValueFactory(loModel -> {
                return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().getEntryNo(), "###,##0.00"));

            });

            filteredPurchaseOrder = new FilteredList<>(paPurchaseOrder, b -> true);
//            autoSearch(tfSearchTransaction);
//            autoSearch(tfSearchSupplier);
//            autoSearch(tfSearchReferNo);

            // 3. Wrap the FilteredList in a SortedList. 
            SortedList<Model_PO_Master> sortedData = new SortedList<>(filteredPurchaseOrder);

            // 4. Bind the SortedList comparator to the TableView comparator.
            // 	  Otherwise, sorting the TableView would have no effect.
            sortedData.comparatorProperty().bind(tblTransaction.comparatorProperty());
            tblTransaction.setItems(sortedData);
            // 5. Add sorted (and filtered) data to the table.
            tblTransaction.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
                TableHeaderRow header = (TableHeaderRow) tblTransaction.lookup("TableHeaderRow");
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
                header.setDisable(true);
            });
        }
    }

    private void reloadTablePurchase() {
        List<Model_PO_Master> rawDetail = poAppController.getPurchaseOrderList();
        paPurchaseOrder.setAll(rawDetail);
        // Restore or select last row
        int indexToSelect = (pnSelectMaster >= 1 && pnSelectMaster < paPurchaseOrder.size())
                ? pnSelectMaster
                : paPurchaseOrder.size();
        loadPageTranasction();
        tblTransaction.getSelectionModel().select(indexToSelect);

        pnSelectMaster = tblTransaction.getSelectionModel().getSelectedIndex(); // Not focusedIndex
        tblTransaction.refresh();
    }

    private Node pageTransaction(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, paPurchaseOrder.size());
        tblTransaction.setItems(FXCollections.observableArrayList(paPurchaseOrder.subList(fromIndex, toIndex)));
        return tblTransaction;
    }

    private void autoSearch(TextField txtField) {
        String lsTxtFieldID = txtField.getId();
        boolean fsCode = true;
        txtField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPurchaseOrder.setPredicate(master -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare order no. and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                switch (lsTxtFieldID) {
//                   //na
                    default:
                        return true;

                }
            });

            changePageTransactionView(0, ROWS_PER_PAGE);
        });
        loadPageTranasction();
    }

    private void loadPageTranasction() {
        int totalPage = (int) (Math.ceil(paPurchaseOrder.size() * 1.0 / ROWS_PER_PAGE));
        pgTransaction.setPageCount(totalPage);
        pgTransaction.setCurrentPageIndex(0);
        changePageTransactionView(0, ROWS_PER_PAGE);
        pgTransaction.currentPageIndexProperty().addListener(
                (observable, oldValue, newValue) -> changePageTransactionView(newValue.intValue(), ROWS_PER_PAGE));

    }

    private void changePageTransactionView(int index, int limit) {
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, paPurchaseOrder.size());

        int minIndex = Math.min(toIndex, filteredPurchaseOrder.size());
        SortedList<Model_PO_Master> sortedData = new SortedList<>(
                FXCollections.observableArrayList(filteredPurchaseOrder.subList(Math.min(fromIndex, minIndex), minIndex)));
        sortedData.comparatorProperty().bind(tblTransaction.comparatorProperty());
        tblTransaction.setItems(sortedData);
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        reloadTablePurchase();
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

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
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

                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(MiscUtil.getException(e), psFormName, null));
                }
            }
        }
        return controls;
    }
}
