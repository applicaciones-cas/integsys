package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelCashAdvance;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.guanzon.appdriver.base.CommonUtils;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CashAdvanceStatus;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class CashAdvance_ByBatchController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Cash Advance By Batch";
    private CashAdvance poController;

    private String psIndustryId = "";
    private String psCategoryId = "";
    private String psCompanyId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    public int pnEditMode;
    int pnMain = 0;
    private unloadForm poUnload = new unloadForm();
    boolean tooltipShown = false;

    private ObservableList<ModelCashAdvance> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCashAdvance> filteredMain_Data;
    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.StageManager stageDV = new JFXUtil.StageManager();
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    JFXUtil.ReloadableTableTask loadTableMain;
    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    private FilteredList<ModelCashAdvance> filteredData;

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchIndustry, tfSearchPayee, tfSearchVoucherNo;
    @FXML
    private Button btnApproved, btnDisapproved, btnRetrieve, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblTransactionNo, tblVoucherNo, tblDate, tblPayeeName, tblCreditedTo, tblRequestingDepartment, tblAmountToAdvance;
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
            poController = new CashflowControllers(oApp, null).CashAdvance();
            poJSON = new JSONObject();
            poJSON = poController.InitTransaction();
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Error(null, pxeModuleName, (String) poJSON.get("message"));
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
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
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

    private void initCheckboxes() {
        JFXUtil.addCheckboxColumns(ModelCashAdvance.class, tblViewMainList, disableRowCheckbox,
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

    public void loadRecordSearch() {
        try {
            poController.Master().setIndustryId(psIndustryId);
            if (poController.Master().Company().getCompanyName() != null && !"".equals(poController.Master().Company().getCompanyName())) {
                lblSource.setText(poController.Master().Company().getCompanyName());
            } else {
                lblSource.setText("");
            }
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchPayee.setText(poController.getSearchPayee());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnDisapproved, btnRetrieve, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();

        switch (lsButton) {
            case "btnApproved":
                handleCashAdvanceAction("approve");
                break;
            case "btnDisapproved":
                handleCashAdvanceAction("disapprove");
                break;
            case "btnRetrieve":
                retrieveCashAdvance();
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
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchPayee, tfSearchVoucherNo, tfSearchIndustry);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                        }
                        break;
                }
                loadTableMain.reload();
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
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    tfSearchIndustry.setText("");
                                    break;
                                }
                                loadRecordSearch();
                                retrieveCashAdvance();
                                return;
                            case "tfSearchPayee":
                                poJSON = poController.SearchPayee(lsValue, false, true);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    tfSearchPayee.setText("");
                                    break;
                                }
                                loadRecordSearch();
                                retrieveCashAdvance();
                                return;
                            case "tfSearchVoucherNo":
                                if (!tooltipShown) {
                                    JFXUtil.showTooltip("NOTE: Results appear directly in the table view, no pop-up dialog.", tfSearchVoucherNo);
                                    tooltipShown = true;
                                }
                                retrieveCashAdvance();
                                return;
                        }
                        event.consume();
                    default:
                        break;
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    public void retrieveCashAdvance() {
        poJSON = new JSONObject();
        poController.setTransactionStatus(CashAdvanceStatus.OPEN + "" + CashAdvanceStatus.CONFIRMED);
        poJSON = poController.loadTransactionList(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchVoucherNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            Platform.runLater(() -> {
                chckSelectAll.setSelected(false);
                checkedItem.clear();
                for (int lnCtr = 0; lnCtr <= poController.getCashAdvanceCount() - 1; lnCtr++) {
                    checkedItem.add("0");
                }
            });
        }
        loadTableMain.reload();
        JFXUtil.runWithDelay(0.50, () -> initButtons());
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblViewMainList,
                main_data,
                () -> {
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(100);
                            main_data.clear();
                            JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);

                            if (poController.getCashAdvanceCount() > 0) {
                                //retreiving using column index
                                for (int lnCtr = 0; lnCtr <= poController.getCashAdvanceCount() - 1; lnCtr++) {
                                    main_data.add(new ModelCashAdvance(String.valueOf(lnCtr + 1),
                                            checkedItem.get(lnCtr),// 0 as unchecked, 1 as checked
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getTransactionNo()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getVoucher()),
                                            CustomCommonUtil.formatDateToShortString(poController.CashAdvanceList(lnCtr).getTransactionDate()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).getPayeeName()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).Credited().getCompanyName()),
                                            String.valueOf(poController.CashAdvanceList(lnCtr).Department().getDescription()),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(String.valueOf(poController.CashAdvanceList(lnCtr).getAdvanceAmount()))
                                    ));

                                    if (poController.CashAdvanceList(lnCtr).getTransactionStatus().equals(CashAdvanceStatus.CONFIRMED)) {
                                        JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                                    }
                                }
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
                            JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                        } catch (InterruptedException | SQLException | GuanzonException ex) {
                            Logger.getLogger(CashAdvance_ConfirmationController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });

                });
    }

    private void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblTransactionNo, tblVoucherNo, tblDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblPayeeName, tblRequestingDepartment);
        JFXUtil.setColumnRight(tblCreditedTo, tblAmountToAdvance);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelCashAdvance selected = (ModelCashAdvance) tblViewMainList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
                    }
                    if (JFXUtil.isObjectEqualTo(selected.getIndex03(), null, "")) {
                        ShowMessageFX.Warning(null, pxeModuleName, "Unable to view transaction.");
                        return;
                    } else {
                        showCashAdvanceWindow(selected.getIndex03());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                }
            }
        });
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnApproved, btnDisapproved);
        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
        JFXUtil.setDisabled(main_data.isEmpty(), chckSelectAll);
    }

    private void handleCashAdvanceAction(String action) {
        try {
            if (checkedItem.stream().anyMatch("1"::equals)) {
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + action + ".");
                return;
            }
            if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + action + " selected item/s?")) {
                return;
            }
            checkedItems.clear();
            for (Object item : tblViewMainList.getItems()) {
                ModelCashAdvance item1 = (ModelCashAdvance) item;
                String lschecked = item1.getIndex02();
                String lsVoucherNO = item1.getIndex03();
                String Remarks = action;
                if (lschecked.equals("1")) {
                    checkedItems.add(lsVoucherNO);
                }
            }
            switch (action) {
                case "approve":
                    poJSON = poController.ConfirmTransaction("", checkedItems);
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
                    poJSON = poController.DisApproveTransaction("", checkedItems);
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
                retrieveCashAdvance();
            });
        } catch (ParseException | SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(CashAdvance_ByBatchController.class.getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void showCashAdvanceWindow(String fsTransactionNo) throws SQLException {
        poJSON = new JSONObject();
        stageDV.closeDialog();

        CashAdvance_ViewController controller = new CashAdvance_ViewController();
        controller.setGRider(oApp);
        controller.setCashAdvance(poController);
        controller.setTransaction(fsTransactionNo);
        try {
            stageDV.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/CashAdvance_View.fxml"), controller,
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
}
