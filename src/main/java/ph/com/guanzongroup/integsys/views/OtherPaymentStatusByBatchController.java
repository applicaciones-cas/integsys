package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.OtherPaymentStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.OtherPaymentStatus;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class OtherPaymentStatusByBatchController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Other Payment Status By Batch";
    private OtherPaymentStatusUpdate poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    private int pnMain = 0;

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;
    JFXUtil.ReloadableTableTask loadTableMain;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    JFXUtil.StageManager stageDV = new JFXUtil.StageManager();
    JFXUtil.StageManager stageAssign = new JFXUtil.StageManager();
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount, tfSearchIndustry, tfSearchDVNo;
    @FXML
    private Button btnClear, btnRetrieve, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblReferenceNo, tblPostDate, tblPaymentStatus, tblPaymentAmount;
    @FXML
    private CheckBox chckSelectAll;
    @FXML
    private Pagination pagination;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(oApp, null).OtherPaymentStatusUpdate();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initTextFields();
            initMainGrid();
            initTableOnClick();
            initButtons();
            initLoadTable();
            initCheckboxes();
            if (main_data.isEmpty()) {
                checkedItems.clear();
            }
            Platform.runLater(() -> {
                poController.setIndustryID(psIndustryId);
                poController.setCompanyID(psCompanyId);
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "chckSelectAll": // this is the id
                    //set to 1 all of column 2 row data value to enable checked
                    for (int lnCtr = 0; lnCtr < checkedItem.size(); lnCtr++) {
                        if (checkedBox.isSelected()) {
                            checkedItem.set(lnCtr, "1");
                        } else {
                            checkedItem.set(lnCtr, "0");
                        }
                    }
                    loadTableMain.reload();
                    break;
            }
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();

            switch (lsButton) {
                case "btnClear":
                    poJSON = validateSelectedItem();
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    }
                    if (!checkedItems.isEmpty()) {
                        showAssignWindow(checkedItems);
                    }
                    break;
                case "btnRetrieve":
                    chckSelectAll.setSelected(false);
                    checkedItems.clear();
                    loadTableMain.reload();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void loadRecordSearch() {
        try {
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchBankName.setText(poController.getSearchBank());
            tfSearchBankAccount.setText(poController.getSearchBankAccount());
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBankName":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBank("");
                        }
                        break;
                    case "tfSearchBankAccount":
                        if (lsValue.isEmpty()) {
                            poController.setSearchBankAccount("");
                        }
                        break;
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                }
                loadRecordSearch();
                loadTableMain.reload();
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poJSON = poController.SearchBanks(lsValue);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poController.SearchBankAccount(lsValue, poController.getSearchBankId(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    break;
                                }
                                loadRecordSearch();
                                loadTableMain.reload();
                                break;
                            case "tfSearchDVNo":
                                loadTableMain.reload();
                                break;
                        }
                        event.consume();
                        break;
                    default:
                        break;
                }
            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            try {
                                main_data.clear();
                                poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchBankName.getText(), tfSearchBankAccount.getText(), tfSearchDVNo.getText());
                                if ("success".equals(poJSON.get("result"))) {
                                    checkedItem.clear();
                                    if (poController.getOtherPaymentList().size() > 0) {
                                        for (int lnCntr = 0; lnCntr <= poController.getOtherPaymentList().size() - 1; lnCntr++) {
                                            String lsCheckStatus;
                                            switch (poController.getOtherPayment(lnCntr).getTransactionStatus()) {
                                                case OtherPaymentStatus.FLOAT:
                                                    lsCheckStatus = "FLOAT";
                                                    break;
                                                case OtherPaymentStatus.OPEN:
                                                    lsCheckStatus = "OPEN";
                                                    break;
                                                case OtherPaymentStatus.POSTED:
                                                    lsCheckStatus = "CLEARED";
                                                    break;
                                                case OtherPaymentStatus.CANCELLED:
                                                    lsCheckStatus = "CANCELLED";
                                                    break;
                                                default:
                                                    lsCheckStatus = "UNKNOWN";
                                                    break;
                                            }
                                            checkedItem.add("0");
                                            main_data.add(new ModelDisbursementVoucher_Main(
                                                    String.valueOf(lnCntr + 1),
                                                    checkedItem.get(lnCntr),
                                                    poController.getOtherPayment(lnCntr).getVoucherNo(),
                                                    CustomCommonUtil.formatDateToShortString(poController.getOtherPayment(lnCntr).getTransactionDate()),
                                                    poController.getOtherPayment(lnCntr).OtherPayments().Banks().getBankName(),
                                                    poController.getOtherPayment(lnCntr).OtherPayments().Bank_Account_Master().getAccountNo(),
                                                    poController.getOtherPayment(lnCntr).OtherPayments().getReferNox(),
                                                    "POST DATE", //poController.getOtherPayment(lnCntr).Payee().getPayeeName()
                                                    lsCheckStatus,
                                                    CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getOtherPayment(lnCntr).OtherPayments().getTotalAmount(), true),
                                                    poController.getOtherPayment(lnCntr).getTransactionNo()
                                            ));
                                        }
                                    } else {
                                        main_data.clear();
                                        checkedItem.clear();
                                    } 
                                } else {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                }
                                if (main_data.isEmpty()) {
                                    ShowMessageFX.Warning(null, pxeModuleName, "No records found");
                                    checkedItems.clear();
                                    Platform.runLater(() -> {
                                        chckSelectAll.setSelected(false);
                                        checkedItem.clear();
                                    });
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
                                JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredMain_Data);
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                    }
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblReferenceNo, tblPostDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblBankName, tblBankAccount, tblPaymentStatus);
        JFXUtil.setColumnRight(tblPaymentAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelDisbursementVoucher_Main.class, tblViewMainList, disableRowCheckbox,
                (row, rowIndex, colIndex, newVal) -> {
                    boolean lbisTrue = newVal;
                    switch (colIndex) {
                        case 1:
                            checkedItem.set(rowIndex, lbisTrue ? "1" : "0");
                            //set external temporary data of index to save as reference
                            // if detected unchecked then must update
                            pnMain = rowIndex;
                            loadTableMain.reload();
                            break;
                    }
                }, 1);//starts 0,1,2 
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchBankName, tfSearchBankAccount);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
                    if (selected.getIndex11().isEmpty() && selected.getIndex11() == null) {
                        ShowMessageFX.Warning("Unable to view, transaction no. is invalid", pxeModuleName, null);
                        return;
                    }
                    showDVWindow(selected.getIndex11());
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        });
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnClear);
    }

    private JSONObject validateSelectedItem() {
        poJSON = new JSONObject();
        ArrayList<String> banks = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        checkedItems.clear();
        for (Object item : tblViewMainList.getItems()) {
            ModelDisbursementVoucher_Main item1 = (ModelDisbursementVoucher_Main) item;
            String lschecked = item1.getIndex02();
            String lsTransactionNo = item1.getIndex11();
            String lsbanks = item1.getIndex06();
            String lsStatus = item1.getIndex09();
            if (lschecked.equals("1")) {
                checkedItems.add(lsTransactionNo);
                banks.add(lsbanks);
                status.add(lsStatus);
            }
        }
        if (checkedItems.isEmpty()) {
            poJSON.put("message", "No items selected to assign.");
            poJSON.put("result", "error");
            return poJSON;
        }

        int successCount = 0;
        boolean allSameBank = true;
        String firstBank = null, firstStatus = null;

        for (int lnCtr = 0; lnCtr <= checkedItems.size() - 1; lnCtr++) {
            String lsDVNO = (checkedItems.get(lnCtr));
            if (firstBank == null) {
                firstBank = banks.get(lnCtr); // store the first encountered bank
                firstStatus = status.get(lnCtr);
            } else if (!firstBank.equals(banks) && !firstStatus.equals(status)) {
                allSameBank = false;
                break; // no need to continue checking
            }

            checkedItems.add(lsDVNO);
            successCount++;
        }
        if (!allSameBank) {
            poJSON.put("message", "Selected items must belong to the same bank and payment status.");
            poJSON.put("result", "error");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }

    public void showAssignWindow(List<String> fsTransactionNos) throws SQLException {
        poJSON = new JSONObject();
        stageAssign.closeDialog();

        CheckClearingAssignController controller = new CheckClearingAssignController();
        controller.setGRider(oApp);
//        controller.setCheckStatusUpdate(poController);
        controller.setTransaction(fsTransactionNos);  // Pass the list here
        try {
            stageAssign.setOnHidden(event -> {
                chckSelectAll.setSelected(false);
                loadTableMain.reload();
                checkedItem.clear();
            });
            stageAssign.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckClearingAssign.fxml"), controller,
                    "Assign Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void showDVWindow(String fsTransactionNo) throws SQLException {
        poJSON = new JSONObject();
        stageDV.closeDialog();
        DisbursementVoucher loObject = new CashflowControllers(oApp, null).DisbursementVoucher();
        DisbursementVoucher_ViewController controller = new DisbursementVoucher_ViewController();
        controller.setGRider(oApp);
        controller.setDisbursement(loObject);
        controller.setTransaction(fsTransactionNo);
        try {
            stageDV.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"), controller,
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
}
