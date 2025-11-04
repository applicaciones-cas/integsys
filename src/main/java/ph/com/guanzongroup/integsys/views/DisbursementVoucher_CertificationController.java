package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import javax.script.ScriptException;
import org.guanzon.appdriver.base.CommonUtils;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * FXML Controller class
 *
 * @author Team 1 & Team 2
 */
public class DisbursementVoucher_CertificationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Disbursement Voucher Certification";
    private DisbursementVoucher poDisbursementController;

    private String psIndustryId = "";
    private String psCategoryId = "";
    private String psCompanyId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    public int pnEditMode;
    int pnMain = 0;
    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelDisbursementVoucher_Main> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Main> filteredMain_Data;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.StageManager stageDV = new JFXUtil.StageManager();
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    JFXUtil.ReloadableTableTask loadTableMain;

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount;
    @FXML
    private Button btnCertify, btnReturn, btnDisapproved, btnRetrieve, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblDVNo, tblDate, tblSupplier, tblPayeeName, tblPaymentForm, tblBankName, tblBankAccount, tblTransAmount;
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
            poDisbursementController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poDisbursementController.setTransactionStatus(DisbursementStatic.VERIFIED);
            poJSON = new JSONObject();
            poDisbursementController.setWithUI(true);
            poJSON = poDisbursementController.InitTransaction();
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initLoadTable();
            initButtonsClickActions();
            initTextFields();
            initMainGrid();
            initTableOnClick();
            initCheckboxes();
            initButtons();
            pagination.setPageCount(1);

            Platform.runLater(() -> {
                poDisbursementController.Master().setIndustryID(psIndustryId);
                poDisbursementController.Master().setCompanyID(psCompanyId);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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

    private void loadRecordSearch() {
        try {
            lblSource.setText(poDisbursementController.Master().Company().getCompanyName() + " - " + poDisbursementController.Master().Industry().getDescription());
            tfSearchBankName.setText(poDisbursementController.CheckPayments().getModel().Banks().getBankName() != null ? poDisbursementController.CheckPayments().getModel().Banks().getBankName() : "");
            tfSearchBankAccount.setText(poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poDisbursementController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnCertify, btnReturn, btnDisapproved, btnRetrieve, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();

        switch (lsButton) {
            case "btnCertify":
                handleDisbursementAction("certify");
                break;
            case "btnReturn":
                handleDisbursementAction("return");
                break;
            case "btnDisapproved":
                handleDisbursementAction("dissapprove");
                break;
            case "btnRetrieve":
                retrieveDisbursement();
                break;
            case "btnClose":
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                    poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                }
                break;
            default:
                ShowMessageFX.Warning("Button is not registered, Please contact admin to assist about the unregistered button", pxeModuleName, null);
                break;
        }
        initButtons();

    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtSearch_Focus, tfSearchBankName, tfSearchBankAccount);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchBankName":
                        if (lsValue.isEmpty()) {
                            poDisbursementController.CheckPayments().getModel().setBankID("");
                            poDisbursementController.CheckPayments().getModel().setBankAcountID("");
                            psSearchBankID = "";
                            psSearchBankAccountID = "";
                        }
                        break;
                    case "tfSearchBankAccount":
                        if (lsValue.isEmpty()) {
                            poDisbursementController.CheckPayments().getModel().setBankAcountID("");
                            psSearchBankAccountID = "";
                        }
                        break;
                }
                loadRecordSearch();
            });

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();

        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                        CommonUtils.SetNextFocus(txtField);
                        event.consume();
                        break;
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poJSON = poDisbursementController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankID = poDisbursementController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poDisbursementController.SearchBankAccount(lsValue, poDisbursementController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankAccountID = poDisbursementController.CheckPayments().getModel().getBankAcountID();
                                break;
                        }
                        event.consume();
                    default:
                        break;
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void retrieveDisbursement() {
        try {
            poJSON = poDisbursementController.loadTransactionList(tfSearchBankName.getText(), tfSearchBankAccount.getText(), "", true);
            if ("error".equals(poJSON.get("result"))) {
                ShowMessageFX.Error(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
            } else {
                Platform.runLater(() -> {
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    for (int lnCntr = 0; lnCntr < poDisbursementController.getMasterList().size(); lnCntr++) {
                        checkedItem.add("0");
                    }

                });
            }
            loadTableMain.reload();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            main_data.clear();
                            if (poDisbursementController.getMasterList().size() > 0) {
                                for (int lnCntr = 0; lnCntr < poDisbursementController.getMasterList().size(); lnCntr++) {
                                    String lsPaymentForm = "";
                                    String lsBankName = "";
                                    String lsBankAccount = "";
                                    String disbursementType = poDisbursementController.getMaster(lnCntr).getDisbursementType();

                                    switch (disbursementType) {
                                        case DisbursementStatic.DisbursementType.CHECK:
                                            lsPaymentForm = "CHECK";
                                            lsBankName = poDisbursementController.getMaster(lnCntr).CheckPayments().Banks().getBankName();
                                            lsBankAccount = poDisbursementController.getMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo();
                                            break;
                                        case DisbursementStatic.DisbursementType.DIGITAL_PAYMENT:
                                            lsPaymentForm = "ONLINE PAYMENT";
//                                            if (otherIndex < poDisbursementController.OtherPayments().getOtherPaymentsCount()) {
//                                                lsBankName = poDisbursementController.OtherPayments().poOtherPayments(otherIndex).Banks().getBankName();
//                                                lsBankAccount = poDisbursementController.OtherPayments().poOtherPayments(otherIndex).getBankAccountID();
//                                            }
                                            break;
                                        case DisbursementStatic.DisbursementType.WIRED:
                                            lsPaymentForm = "BANK TRANSFER";
//                                            if (otherIndex < poDisbursementController.OtherPayments().getOtherPaymentsCount()) {
//                                                lsBankName = poDisbursementController.OtherPayments().poOtherPayments(otherIndex).Banks().getBankName();
//                                                lsBankAccount = poDisbursementController.OtherPayments().poOtherPayments(otherIndex).getBankAccountID();
//                                            }
                                            break;
                                        default:
                                            lsPaymentForm = "";
                                            break;
                                    }

                                    main_data.add(new ModelDisbursementVoucher_Main(
                                            String.valueOf(lnCntr + 1),
                                            checkedItem.get(lnCntr),// 0 as unchecked, 1 as checked
                                            poDisbursementController.getMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poDisbursementController.getMaster(lnCntr).getTransactionDate()),
                                            poDisbursementController.getMaster(lnCntr).Payee().getPayeeName(),
                                            poDisbursementController.getMaster(lnCntr).Payee().getPayeeName(),
                                            lsPaymentForm,
                                            lsBankName,
                                            lsBankAccount,
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poDisbursementController.getMaster(lnCntr).getNetTotal(), true)
                                    ));
                                }
                            } else {
                                checkedItem.clear();
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
                        }
                        initButtons();
                    });
                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblSupplier, tblPayeeName, tblPaymentForm, tblBankName, tblBankAccount);
        JFXUtil.setColumnRight(tblTransAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelDisbursementVoucher_Main selected = (ModelDisbursementVoucher_Main) tblViewMainList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                    }
                    if (JFXUtil.isObjectEqualTo(selected.getIndex03(), null, "")) {
                        ShowMessageFX.Warning("Unable to view transaction.", pxeModuleName, null);
                        return;
                    } else {
                        showDVWindow(selected.getIndex03());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnCertify, btnDisapproved, btnReturn);
        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
        JFXUtil.setDisabled(main_data.isEmpty(), chckSelectAll);
    }

    private void handleDisbursementAction(String action) {
        try {
            if (checkedItem.stream().anyMatch("1"::equals)) {
            } else {
                ShowMessageFX.Information(null, pxeModuleName, "No items were selected to " + action + ".");
                return;
            }

            if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + action + " selected item/s?")) {
                return;
            }

            checkedItems.clear();
            for (Object item : tblViewMainList.getItems()) {
                ModelDisbursementVoucher_Main item1 = (ModelDisbursementVoucher_Main) item;
                String lschecked = item1.getIndex02();
                String lsDVNO = item1.getIndex03();
                String Remarks = action;
                if (lschecked.equals("1")) {
                    checkedItems.add(lsDVNO);
                }
            }

            switch (action) {
                case "certify":
                    poJSON = poDisbursementController.CertifyTransaction(action, checkedItems);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    break;
                case "return":
                    poJSON = poDisbursementController.ReturnTransaction(action, checkedItems);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    break;
                case "disapprove":
                    poJSON = poDisbursementController.DisApproveTransaction("", checkedItems);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        break;
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    break;
                default:
                    throw new AssertionError();
            }
            Platform.runLater(() -> {
                retrieveDisbursement();
                loadTableMain.reload();
            });
        } catch (ParseException | SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showDVWindow(String fsTransactionNo) throws SQLException {
        poJSON = new JSONObject();
        stageDV.closeDialog();

        DisbursementVoucher_ViewController controller = new DisbursementVoucher_ViewController();
        controller.setGRider(oApp);
        controller.setDisbursement(poDisbursementController);
        controller.setTransaction(fsTransactionNo);
        try {
            stageDV.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"), controller,
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
