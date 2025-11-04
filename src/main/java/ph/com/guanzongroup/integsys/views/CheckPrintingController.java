/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelCheckPrinting;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckPayments;
import ph.com.guanzongroup.cas.cashflow.CheckPrinting;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * FXML Controller class
 *
 * @author Team 1 & Team 2
 */
public class CheckPrintingController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Printing";
    private DisbursementVoucher poController;
    private CheckPayments poCheckPayments;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    private String psSearchDVDateFrom = "";
    private String psSearchDVDateTo = "";
    private int pnRow = -1;
    private double xOffset = 0;
    private double yOffset = 0;

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelCheckPrinting> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCheckPrinting> filteredMain_Data;

    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

    BooleanProperty disableRowCheckbox = new SimpleBooleanProperty(false);
    JFXUtil.StageManager stageDV = new JFXUtil.StageManager();
    JFXUtil.StageManager stageAssignment = new JFXUtil.StageManager();
    ArrayList<String> checkedItem = new ArrayList<>();
    ArrayList<String> checkedItems = new ArrayList<>();
    JFXUtil.ReloadableTableTask loadTableMain;
    int pnMain = 0;
    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount;
    @FXML
    private DatePicker dpDVDateFrom, dpDVDateTo;
    @FXML
    private Button btnAssign, btnRetrieve, btnPrintDV, btnPrintCheck, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblDVNo, tblDVDate, tblSupplier, tblPayeeName, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblTotalAmount;
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
    //Check Printing In-house
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poController.setTransactionStatus(DisbursementStatic.VERIFIED);
            poJSON = new JSONObject();
            poController.setWithUI(true);
            poJSON = poController.InitTransaction();
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
            initDatePickers();
            pagination.setPageCount(1);

            Platform.runLater(() -> {
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
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
        JFXUtil.addCheckboxColumns(ModelCheckPrinting.class, tblViewMainList, disableRowCheckbox,
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
            lblSource.setText(poController.Master().Company().getCompanyName() + " - " + poController.Master().Industry().getDescription());
            tfSearchBankName.setText(poController.CheckPayments().getModel().Banks().getBankName() != null ? poController.CheckPayments().getModel().Banks().getBankName() : "");
            tfSearchBankAccount.setText(poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnAssign, btnRetrieve, btnClose, btnPrintCheck, btnPrintDV);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnAssign":
                handleDisbursementAction("assign");
                break;
            case "btnPrintCheck":
                handleDisbursementAction("print check");
                break;
            case "btnPrintDV":
                handleDisbursementAction("print dv");
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

            String firstBank = null;
            boolean allSameBank = true;
            checkedItems.clear();
            for (Object item : tblViewMainList.getItems()) {
                ModelCheckPrinting item1 = (ModelCheckPrinting) item;
                String lschecked = item1.getIndex02();
                String lsDVNO = item1.getIndex03();
                String banks = item1.getIndex07();

                if (lschecked.equals("1")) {
                    if (firstBank == null) {
                        firstBank = banks;
                    } else if (!firstBank.equals(banks)) {
                        allSameBank = false;
                        break;
                    }
                    checkedItems.add(lsDVNO);
                }
            }
            if (!allSameBank) {
                ShowMessageFX.Warning(null, pxeModuleName, "Selected items must belong to the same bank.");
                return;
            }

            switch (action) {
                case "assign":
                    if (!checkedItem.isEmpty()) {
                        showAssignWindow(checkedItem);
                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    break;
                case "print check":
                    if (!checkedItem.isEmpty()) {
//                        poJSON = poController.PrintCheck(checkedItem);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            break;
                        }
                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    break;
                case "print dv":
                    if (!checkedItem.isEmpty()) {
//                        poJSON = poController.printTransaction(checkedItem);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        }

                        chckSelectAll.setSelected(false);
                        checkedItem.clear();
                    }
                    break;
                default:
                    throw new AssertionError();
            }
            retrieveDisbursement();
            loadTableMain.reload();
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpDVDateFrom, dpDVDateTo);
        JFXUtil.setActionListener(this::datepicker_Action, dpDVDateFrom, dpDVDateTo);
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
                            poController.CheckPayments().getModel().setBankID("");
                            poController.CheckPayments().getModel().setBankAcountID("");
                            psSearchBankID = "";
                            psSearchBankAccountID = "";
                        }
                        break;
                    case "tfSearchBankAccount":
                        if (lsValue.isEmpty()) {
                            poController.CheckPayments().getModel().setBankAcountID("");
                            psSearchBankAccountID = "";
                        }
                        break;
                }
                loadRecordSearch();
            });

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");
        Object source = event.getSource();
        if (source instanceof DatePicker) {
            LocalDate dateNow = LocalDate.now();
            DatePicker datePicker = (DatePicker) source;
            String inputText = datePicker.getEditor().getText();
            if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                return;
            }
            switch (datePicker.getId()) {
                case "dpDVDateFrom":
                    LocalDate selectedFromDate = dpDVDateFrom.getValue();
                    LocalDate toDate = dpDVDateTo.getValue();
                    if (toDate != null && selectedFromDate.isAfter(toDate)) {
                        ShowMessageFX.Warning("Invalid Date, The 'From' date cannot be after the 'To' date.", pxeModuleName, null);
                        dpDVDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                        psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                        return;
                    }
                    psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
                    retrieveDisbursement();
                    break;
                case "dpDVDateTo":
                    LocalDate selectedToDate = dpDVDateTo.getValue();
                    LocalDate fromDate = dpDVDateFrom.getValue();
                    if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                        ShowMessageFX.Warning("Invalid Date, The 'To' date cannot be before the 'From' date.", pxeModuleName, null);
                        dpDVDateTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                        psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                        return;
                    }
                    psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
                    retrieveDisbursement();
                    break;
                default:
                    break;
            }
        }
    }

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
                                poJSON = poController.SearchBanks(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankID = poController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poJSON = poController.SearchBankAccount(lsValue, poController.CheckPayments().getModel().getBankID(), false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                psSearchBankAccountID = poController.CheckPayments().getModel().getBankAcountID();
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
            poJSON = poController.loadTransactionList(tfSearchBankName.getText(), tfSearchBankAccount.getText(), "", true);
            if ("error".equals(poJSON.get("result"))) {
                ShowMessageFX.Error(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
            } else {
                Platform.runLater(() -> {
                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                    for (int lnCntr = 0; lnCntr < poController.getMasterList().size(); lnCntr++) {
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
                            if (poController.getMasterList().size() > 0) {
                                for (int lnCntr = 0; lnCntr <  poController.getMasterList().size(); lnCntr++) {

                                    main_data.add(new ModelCheckPrinting(
                                            String.valueOf(lnCntr + 1),
                                            checkedItem.get(lnCntr),
                                            poController.getMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCntr).getTransactionDate()),
                                            poController.getMaster(lnCntr).Payee().getPayeeName(),
                                            poController.getMaster(lnCntr).Payee().getPayeeName(),
                                            poController.getMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                            poController.getMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                            poController.getMaster(lnCntr).CheckPayments().getCheckNo(),
                                            CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCntr).getTransactionDate()),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getMaster(lnCntr).getNetTotal(), true)
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
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblCheckNo, tblCheckDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblSupplier, tblPayeeName, tblBankName, tblBankAccount);
        JFXUtil.setColumnRight(tblTotalAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelCheckPrinting selected = (ModelCheckPrinting) tblViewMainList.getSelectionModel().getSelectedItem();
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
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnAssign, btnPrintCheck, btnPrintDV);
        disableRowCheckbox.set(main_data.isEmpty()); // set enable/disable in checkboxes in requirements
        JFXUtil.setDisabled(main_data.isEmpty(), chckSelectAll);
    }

    public void showDVWindow(String fsTransactionNo) throws SQLException {
        poJSON = new JSONObject();
        stageDV.closeDialog();

        DisbursementVoucher_ViewController controller = new DisbursementVoucher_ViewController();
        controller.setGRider(oApp);
        controller.setDisbursement(poController);
        controller.setTransaction(fsTransactionNo);
        try {
            stageDV.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"), controller,
                    "Disbursement Dialog", true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showAssignWindow(List<String> fsTransactionNos) throws SQLException {
        poJSON = new JSONObject();
        stageAssignment.closeDialog();

        CheckAssignmentController controller = new CheckAssignmentController();
        controller.setGRider(oApp);
//        controller.setCheckPrinting(poController);
        controller.setTransaction(fsTransactionNos);
        try {
            stageAssignment.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckAssignment.fxml"), controller,
                    "Check Assignment Dialog", true, false, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

}
