package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import net.sf.jasperreports.engine.JRException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Payments;
import ph.com.guanzongroup.cas.check.module.mnv.CheckTransfer;
import ph.com.guanzongroup.cas.check.module.mnv.constant.CheckTransferStatus;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.check.module.mnv.models.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.check.module.mnv.services.CheckController;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckTransfer_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Transfer Confirmation";
    private String psIndustryID;
    private Control lastFocusedControl;
    private CheckTransfer poAppController;
    private ObservableList<Model_Check_Transfer_Detail> laTransactionDetail;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail;

    private unloadForm poUnload = new unloadForm();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apMaster, apDetail, apButton, apTransaction;

    @FXML
    private TextField tfSearchDestination, tfSearchTransNo, tfTransactionNo,
            tfDestination, tfDepartment, tfTotal, tfPayee,
            tfBank, tfCheckAmount, tfCheckTransNo,
            tfCheckNo, tfNote;

    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpCheckDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnSearch, btnBrowse, btnUpdate, btnSave, btnCancel, btnPrint, btnApprove, btnVoid,
            btnRetrieve, btnClose;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView<Model_Check_Transfer_Detail> tblViewDetails;

    @FXML
    private TableColumn<Model_Check_Transfer_Detail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailBank,
            tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount;

    @FXML
    private TableView<Model_Check_Transfer_Master> tblViewMaster;

    @FXML
    private TableColumn<Model_Check_Transfer_Master, String> tblColNo, tblColTransNo,
            tblColTransDate, tblColTransDestination;

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
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new CheckController(poApp, poLogWrapper).CheckTransfer();

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("10");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);

            });
            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(CheckTransfer_ConfirmationController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    void ontblMasterClicked(MouseEvent e) {
        pnSelectMaster = tblViewMaster.getSelectionModel().getSelectedIndex();
        if (pnSelectMaster < 0) {
            return;
        }

        if (e.getClickCount() == 2 && !e.isConsumed()) {
            try {

                e.consume();
                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                    return;
                }
                if (!isJSONSuccess(poAppController.searchTransaction(tblColTransNo.getCellData(pnSelectMaster), true, true), psFormName)) {
//                    ShowMessageFX.Information("Failed to add detail", psFormName, null);
                    return;
                }
                getLoadedTransaction();
                initButtonDisplay(poAppController.getEditMode());
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {

                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

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
                        case "tfDestination":
                            if (!isJSONSuccess(poAppController.searchTransactionDestination(tfDestination.getText().trim(), false),
                                    "Initialize Search Destination! ")) {
                                return;
                            }
                            loadTransactionMaster();
                            break;
                        case "tfDepartment":
                            if (!isJSONSuccess(poAppController.searchTransactionDepartment(tfDepartment.getText().trim(), false),
                                    "Initialize Search Department! ")) {
                                return;
                            }
                            loadTransactionMaster();
                            break;
                        case "tfCheckTransNo":
                            if (!isJSONSuccess(poAppController.searchDetailByCheck(pnTransactionDetail, tfCheckTransNo.getText(), true),
                                    "Initialize Search Check! ")) {
                                return;
                            }
                            reloadTableDetail();
                            loadSelectedTransactionDetail(pnTransactionDetail);
                            break;
                        case "tfCheckNo":
                            if (!isJSONSuccess(poAppController.searchDetailByCheck(pnTransactionDetail, tfCheckNo.getText(), false),
                                    "Initialize Search Check! ")) {
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
                        if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                "Initialize Search Source No! ")) {
                            return;
                        }

                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());
                        break;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchTransNo":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchDestination":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchDestination.getText(), false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "dpSearchTransactionDate":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransaction(String.valueOf(dpSearchTransactionDate.getValue()), false),
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
                            if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;
//                case "btnReturn":
//                    if (tfTransactionNo.getText().isEmpty()) {
//                        ShowMessageFX.Information("Please load transaction before proceeding..", null, psFormName);
//                        return;
//                    }
//
//                    if (!poAppController.getMaster().getTransactionStatus().equalsIgnoreCase(CheckTransferStatus.OPEN)) {
//                        ShowMessageFX.Information("Status was already " + CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())).toLowerCase(), null,
//                                "Check Transfer Approval");
//                        return;
//                    }
//
//                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to return transaction?") == true) {
//                        if (!isJSONSuccess(poAppController.ReturnTransaction(), "Initialize Close Transaction")) {
//                            return;
//                        }
//                        getLoadedTransaction();
//                        pnEditMode = poAppController.getEditMode();
//                        break;
//                    }
//                    break;
                case "btnApprove":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null,
                                "Check Transfer Confirmation");
                        return;
                    }

                    if (!poAppController.getMaster().getTransactionStatus().equalsIgnoreCase(CheckTransferStatus.OPEN)) {
                        ShowMessageFX.Information("Status was already " + CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())).toLowerCase(), null, psFormName);
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to confirm transaction?") == true) {
                        if (!isJSONSuccess(poAppController.CloseTransaction(), "Initialize Close Transaction")) {
                            return;
                        }
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;
                case "btnVoid":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", null, psFormName);
                        return;
                    }

                    if (ShowMessageFX.YesNo(null, psFormName, "Are you sure you want to Void/Cancel transaction?") == true) {
                        if (btnVoid.getText().equals("Void")) {
                            if (!isJSONSuccess(poAppController.VoidTransaction(), "Initialize Void Transaction")) {
                                return;
                            }
                        } else {
                            if (!isJSONSuccess(poAppController.CancelTransaction(), "Initialize Cancel Transaction")) {
                                return;
                            }

                        }
                        getLoadedTransaction();
                        pnEditMode = poAppController.getEditMode();
                        break;
                    }
                    break;

                case "btnPrint":

                    if (poAppController.getMaster().getTransactionStatus().equalsIgnoreCase(CheckTransferStatus.OPEN)) {
                        if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to confirm the transaction ?") == true) {
                            if (!isJSONSuccess(poAppController.CloseTransaction(),
                                    "Initialize Close Transaction")) {
                                return;
                            }
                        }
                    }
                    if (poAppController.getMaster().getTransactionNo() == null || poAppController.getMaster().getTransactionNo().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, "");
                        return;
                    }
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to print the transaction ?") == true) {
                        if (!isJSONSuccess(poAppController.printRecord(),
                                "Initialize Print Transaction")) {
                            return;
                        }
                    }
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
                        if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to Print transaction?") == true) {
                            if (!isJSONSuccess(poAppController.printRecord(), "Initialize Print Transaction")) {
                                return;
                            }
                        }
                    }

                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();

                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, psFormName, "Do you want to disregard changes?") == true) {
                        poAppController = new CheckController(poApp, poLogWrapper).CheckTransfer();
                        poAppController.setTransactionStatus("10");

                        if (!isJSONSuccess(poAppController.initTransaction(), "Initialize Transaction")) {
                            unloadForm appUnload = new unloadForm();
                            appUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        }

                        Platform.runLater(() -> {

                            poAppController.setTransactionStatus("10");
                            poAppController.getMaster().setIndustryId(psIndustryID);
                            poAppController.setIndustryID(psIndustryID);

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

                case "btnRetrieve":
                    if (lastFocusedControl == null) {
                        loadTransactionMasterList(tfSearchDestination.getText().trim(), "c.sBranchNm");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchDestination":
                            loadTransactionMasterList(tfSearchDestination.getText().trim(), "c.sBranchNm");
                            break;
                        case "tfSearchTransNo":
                            loadTransactionMasterList(tfSearchTransNo.getText().trim(), "a.sTransNox");
                            break;
                        case "dpSearchTransactionDate":
                            loadTransactionMasterList(String.valueOf(dpSearchTransactionDate.getValue()), "a.dTransact");
                            break;
                        default:
                            loadTransactionMasterList(tfSearchDestination.getText().trim(), "c.sBranchNm");
                            break;
                    }
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

        } catch (CloneNotSupportedException | NumberFormatException | SQLException | JRException | GuanzonException e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
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

                    case "taRemarks":
                        poAppController.getMaster().setRemarks(lsValue);
                        loadTransactionMaster();

                        break;
                    case "tfNote":
                        poAppController.getDetail(pnTransactionDetail).setRemarks(lsValue);
                        loadSelectedTransactionDetail(pnTransactionDetail);

                        break;

                }
            } else {
                loTextField.selectAll();
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    };

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
                            case "tfSearchTransNo":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchTransNo.getText(), true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchDestination":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(tfSearchDestination.getText(), true, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "dpSearchTransactionDate":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransaction(String.valueOf(dpSearchTransactionDate.getValue()), false, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfDestination":
                                if (!isJSONSuccess(poAppController.searchTransactionDestination(tfDestination.getText(), false),
                                        "Initialize Search Destination! ")) {
                                    return;
                                }
                                loadTransactionMaster();
                                break;
                            case "tfDepartment":
                                if (!isJSONSuccess(poAppController.searchTransactionDepartment(tfDepartment.getText(), false),
                                        "Initialize Search Department! ")) {
                                    return;
                                }
                                loadTransactionMaster();
                                break;
                            case "tfCheckTransNo":
                                if (!isJSONSuccess(poAppController.searchDetailByCheck(pnTransactionDetail, tfCheckTransNo.getText(), true),
                                        "Initialize Search Check! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                                break;
                            case "tfCheckNo":
                                if (!isJSONSuccess(poAppController.searchDetailByCheck(pnTransactionDetail, tfCheckNo.getText(), false),
                                        "Initialize Search Check! ")) {
                                    return;
                                }
                                reloadTableDetail();
                                loadSelectedTransactionDetail(pnTransactionDetail);
                                break;

                        }
                        break;
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
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
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText(poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription());
            lblStatus.setText(CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            tfTransactionNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            tfDestination.setText(poAppController.getMaster().BranchDestination().getBranchName());
            tfDepartment.setText(poAppController.getMaster().Department().getDescription());
            taRemarks.setText(String.valueOf(poAppController.getMaster().getRemarks()));
            tfTotal.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotal(), "###,##0.0000"));
            tfDepartment.setDisable(!poAppController.getMaster().BranchDestination().isMainOffice() || !poAppController.getMaster().BranchDestination().isWarehouse());

            if (poAppController.getMaster().getTransactionStatus().equals(CheckTransferStatus.CONFIRMED)) {
                btnVoid.setText("Cancel");
            } else {
                btnVoid.setText("Void");
            }
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfCheckTransNo.setText(tblColDetailReference.getCellData(tblIndex));
        tfBank.setText(tblColDetailBank.getCellData(tblIndex));
        tfPayee.setText(tblColDetailPayee.getCellData(tblIndex));
        tfCheckNo.setText(tblColDetailCheckNo.getCellData(tblIndex));
        tfCheckAmount.setText(tblColDetailCheckAmount.getCellData(tblIndex));

        tfNote.setText(poAppController.getDetail(fnRow).getRemarks());
        dpCheckDate.setValue(ParseDate(poAppController.getDetail(fnRow).CheckPayment().getTransactionDate()));
        recomputeTotal();
    }

    private void recomputeTotal() throws SQLException, GuanzonException {
        double lnTotal = 0.00;
        for (int lnCtr = 1; lnCtr <= poAppController.getDetailCount(); lnCtr++) {
            if (poAppController.getDetail(lnCtr).getSourceNo() == null || poAppController.getDetail(lnCtr).getSourceNo().isEmpty()) {
                continue;
            }
            lnTotal = lnTotal + poAppController.getDetail(lnCtr).CheckPayment().getAmount();
        }
        poAppController.getMaster().setTransactionTotal(lnTotal);
        tfTotal.setText(CommonUtils.NumberFormat(poAppController.getMaster().getTransactionTotal(), "###,##0.0000"));
    }

    private void loadTransactionMasterList(String fsValue, String fsColumn) {
        StackPane overlay = getOverlayProgress(apTransaction);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Check_Transfer_Master>> loadCheckPayment = new Task<ObservableList<Model_Check_Transfer_Master>>() {
            @Override
            protected ObservableList<Model_Check_Transfer_Master> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadTransactionListConfirmation(fsValue, fsColumn),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Check_Transfer_Master> rawList = poAppController.getMasterList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Check_Transfer_Master> laMasterList = getValue();
                tblViewMaster.setItems(laMasterList);

                tblColNo.setCellValueFactory(loModel -> {
                    int index = tblViewMaster.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });
                tblColTransNo.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionNo()));
                });
                tblColTransDate.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
                });

                tblColTransDestination.setCellValueFactory((loModel) -> {
                    try {
                        return new SimpleStringProperty(String.valueOf(loModel.getValue().BranchDestination().getBranchName()));
                    } catch (SQLException | GuanzonException e) {
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                ex.printStackTrace();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadCheckPayment);
        thread.setDaemon(true);
        thread.start();
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
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
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
        initButtonControls(lbShow, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbShow, "btnBrowse", "btnUpdate", "btnPrint", "btnApprove", "btnVoid");

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
            }
        }
    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();

            tblViewDetails.setItems(laTransactionDetail);

            tblColDetailCheckAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailReference.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getTransactionNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailPayee.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Payee().getPayeeName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBank.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Banks().getBankName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            tblColDetailDate.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getCheckDate()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckNo.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getCheckNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckAmount.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(CommonUtils.NumberFormat(loModel.getValue().CheckPayment().getAmount(), "###,##0.0000"));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
        }
    }

    private void reloadTableDetail() {
        List<Model_Check_Transfer_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnTransactionDetail >= 1 && pnTransactionDetail < laTransactionDetail.size())
                ? pnTransactionDetail - 1
                : laTransactionDetail.size() - 1;

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

    boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
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
            }
        }
        return controls;
    }
}
