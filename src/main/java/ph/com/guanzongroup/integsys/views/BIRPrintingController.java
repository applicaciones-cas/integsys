/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelBIRPrinting;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.integsys.model.ModelBIRPrinting;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class BIRPrintingController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "BIR Printing";
    private DisbursementVoucher poController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelBIRPrinting> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelBIRPrinting> filteredMain_Data;

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
    private TextField tfSearchIndustry, tfSearchSupplier;
    @FXML
    private Button btnRetrieve, btnPrintBIR, btnClose;
    @FXML
    private TableView tblViewMainList;
    @FXML
    private TableColumn tblRowNo, tblCheckBox, tblDVNo, tblDVDate, tblSupplier, tblPayeeName, tblPayeeType, tblWTAXAmount;
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
            poController = new CashflowControllers(oApp, null).DisbursementVoucher();
            poController.setTransactionStatus(DisbursementStatic.AUTHORIZED + DisbursementStatic.CERTIFIED);
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
            pagination.setPageCount(1);

            Platform.runLater(() -> {
                poController.Master().setIndustryID(psIndustryId);
                poController.Master().setCompanyID(psCompanyId);
                poController.setIndustryID(psIndustryId);
                poController.setCompanyID(psCompanyId);
                poController.setCategoryID(psCategoryId);
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
        JFXUtil.addCheckboxColumns(ModelBIRPrinting.class, tblViewMainList, disableRowCheckbox,
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
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchSupplier.setText(poController.getSearchPayee());

            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnPrintBIR, btnRetrieve, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnPrintBIR":
                handleDisbursementAction("print br");
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
//        try {
        if (checkedItem.stream().anyMatch("1"::equals)) {
        } else {
            ShowMessageFX.Warning(null, pxeModuleName, "No items were selected to " + action + ".");
            return;
        }

        if (!ShowMessageFX.OkayCancel(null, pxeModuleName, "Are you sure you want to " + action + " selected item/s?")) {
            return;
        }

        String firstPayee = null;
        boolean allSamePayee = true;
        checkedItems.clear();
        for (Object item : tblViewMainList.getItems()) {
            ModelBIRPrinting item1 = (ModelBIRPrinting) item;
            String lschecked = item1.getIndex02();
            String lsDVNO = item1.getIndex03();
            String payeetype = item1.getIndex07();

            if (lschecked.equals("1")) {
                if (firstPayee == null) {
                    firstPayee = payeetype;
                } else if (!firstPayee.equals(payeetype)) {
                    allSamePayee = false;
                    break;
                }
                checkedItems.add(lsDVNO);
                System.out.println("check items : " + checkedItems.get(checkedItems.size() - 1));
            }
        }
        if (!allSamePayee) {
            ShowMessageFX.Warning(null, pxeModuleName, "Selected items must belong to the same bank.");
            return;
        }

        switch (action) {
            case "print br":
                if (!checkedItems.isEmpty()) {
                    poJSON = poController.PrintBIR(checkedItems);
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    } else {
                        ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                    }

                    chckSelectAll.setSelected(false);
                    checkedItem.clear();
                }
                retrieveDisbursement();
                loadTableMain.reload();
                break;
            default:
                throw new AssertionError();
        }

//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtSearch_Focus, tfSearchIndustry, tfSearchSupplier);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
        JFXUtil.adjustColumnForScrollbar(tblViewMainList);
    }

    ChangeListener<Boolean> txtSearch_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {

                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchSupplier":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
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
                            case "tfSearchIndustry":
                                poJSON = poController.SearchIndustry(lsValue, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
                                break;
                            case "tfSearchSupplier":
                                poJSON = poController.SearchSupplier(lsValue, false, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    return;
                                } else {
                                    loadRecordSearch();
                                    retrieveDisbursement();
                                }
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
            poJSON = poController.loadBIRPrintTransactionList(tfSearchIndustry.getText(), tfSearchSupplier.getText());
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
                                for (int lnCntr = 0; lnCntr < poController.getMasterList().size(); lnCntr++) {
                                    String lsPayeeType = poController.CheckPayments().getModel().getPayeeType().equals("0") ? "INDIVIDUAL" : "CORPORATION";
                                    main_data.add(new ModelBIRPrinting(
                                            String.valueOf(lnCntr + 1),
                                            checkedItem.get(lnCntr),
                                            poController.getMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poController.getMaster(lnCntr).getTransactionDate()),
                                            poController.getMaster(lnCntr).Payee().APClient().getCompanyName(),
                                            poController.getMaster(lnCntr).Payee().getPayeeName(),
                                            lsPayeeType,
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getMaster(lnCntr).getWithTaxTotal(), false)
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
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate);
        JFXUtil.setColumnLeft(tblCheckBox, tblSupplier, tblPayeeName, tblPayeeType);
        JFXUtil.setColumnRight(tblWTAXAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);
        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredMain_Data);
    }

    private void initTableOnClick() {
        tblViewMainList.setOnMouseClicked(event -> {
            if (tblViewMainList.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelBIRPrinting selected = (ModelBIRPrinting) tblViewMainList.getSelectionModel().getSelectedItem();
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
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnPrintBIR);
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
                    "Disbursement Dialog", true, true, false);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showAssignWindow(List<String> fsTransactionNos) throws SQLException {
        poJSON = new JSONObject();
        stageAssignment.closeDialog();

        CheckAssignmentController controller = new CheckAssignmentController();
        controller.setGRider(oApp);
        controller.setCheckPrinting(poController);
        controller.setTransaction(fsTransactionNos);
        try {
            stageAssignment.showDialog((Stage) AnchorMain.getScene().getWindow(), getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckAssignment.fxml"), controller,
                    "Check Assignment Dialog", true, true, false);
            stageAssignment.setOnHidden(event -> {
                chckSelectAll.setSelected(false);
                retrieveDisbursement();
                loadTableMain.reload();
                checkedItem.clear();
            });

        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

}
