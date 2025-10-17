package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelUserManagement;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class UserManagement_Controller implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
//    static CashflowControllers poAPPaymentAdjustmentController;
    private JSONObject poJSON;
    public int pnEditMode;

    private String pxeModuleName = "User Management";
    private boolean isGeneral = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psSupplierId = "";
    private String psSearchCompanyId = "";
    private String psSearchSupplierId = "";
    private String psTransactionNo = "";
    private static final int ROWS_PER_PAGE = 50;
    int pnMain = 0;
    private boolean pbEntered = false;
    private ObservableList<ModelUserManagement> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelUserManagement> filteredData;
    JFXUtil.ReloadableTableTask loadTableMain;
    ObservableList<String> UserLevel = FXCollections.observableArrayList(
            "ENCODER",
            "SUPERVISOR",
            "BH",
            "AH",
            "DH",
            "AUDIT",
            "SYSADMIN",
            "SYSMASTER"
    );
    ObservableList<String> UserType = FXCollections.observableArrayList("Local", "Global");

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private TextField tfSearchEmployeeName, tfSearchLogInName, tfUserID, tfLogInName, tfPassword, tfEmployeeName, tfProduct;
    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSave, btnCancel, btnClose;
    @FXML
    private ComboBox cmbUserLevel, cmbUserType;
    @FXML
    private TableView tblMain;
    @FXML
    private TableColumn tblModule, tblRole, tblUserLevel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        psIndustryId = ""; // general

        poJSON = new JSONObject();
//        poAPPaymentAdjustmentController = new CashflowControllers(oApp, null);
//        poAPPaymentAdjustmentController.APPaymentAdjustment().initialize(); // Initialize transaction
        initLoadTable();
        initTextFields();
        clearTextFields();
        initMainGrid();
        initTableOnClick();
        initComboboxes();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);

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
        //Company is not autoset
    }

    @Override
    public void setCategoryID(String fsValue) {
        //No Category
    }

    public void loadTableDetailFromMain() {
//        try {
//            poJSON = new JSONObject();
//
//            ModelAPPaymentAdjustment selected = (ModelAPPaymentAdjustment) tblMain.getSelectionModel().getSelectedItem();
//            if (selected != null) {
//                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
//                pnMain = pnRowMain;
//                JFXUtil.disableAllHighlightByColor(tblMain, "#A7C7E7", highlightedRowsMain);
//                JFXUtil.highlightByKey(tblMain, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);
//
//                poJSON = poAPPaymentAdjustmentController.APPaymentAdjustment().OpenTransaction(poAPPaymentAdjustmentController.APPaymentAdjustment().APPaymentAdjustmentList(pnMain).getTransactionNo());
//                if ("error".equals((String) poJSON.get("result"))) {
//                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                    return;
//                }
//                loadRecordMaster();
//            }
//        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
//        }
    }

    public void loadRecordMaster() {
        tfUserID.setText("");
        tfLogInName.setText("");
        tfPassword.setText("");
        tfEmployeeName.setText("");
        tfProduct.setText("");
        cmbUserLevel.getSelectionModel().select(0);
        cmbUserType.getSelectionModel().select(0);
    }

    public void loadRecordSearch() {
        tfSearchEmployeeName.setText("");
        tfSearchLogInName.setText("");
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();

            switch (lsButton) {
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
//                        appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);

                        //should go back to log in or in xml form
                        if (LoginControllerHolder.getLogInStatus()) {
                            LoginControllerHolder.getMainController().Tabclose();
                        } else {
                            LoginControllerHolder.getMainController().TabUserManagement();
                        }
                    } else {
                        return;
                    }
                    break;
            }

        }

    }

    public void retrieveAccount() {
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = txtField.getId();
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        int lnRow = pnMain;

        switch (event.getCode()) {
            case TAB:
            case ENTER:
                pbEntered = true;
                CommonUtils.SetNextFocus(txtField);
                event.consume();
                break;
            case F3:
                switch (lsID) {
                    case "tfSearchEmployeeName":

                        loadRecordSearch();
                        retrieveAccount();
                        return;
                    case "tfSearchLogInName":

                        loadRecordSearch();
                        retrieveAccount();
                        return;
                }
                break;
        }

    }

    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchEmployeeName":
                        if (lsValue.equals("")) {
                            psSearchSupplierId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchLogInName":
                        if (lsValue.equals("")) {
                            psSearchCompanyId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfUserID":
                        break;
                    case "tfLogInName":
                        break;
                    case "tfPassword":
                        break;
                    case "tfEmployeeName":
                        break;
                    case "tfProduct":
                        break;
                }
                loadRecordMaster();
            });

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchEmployeeName":
                        if (lsValue.equals("")) {
                            psSearchSupplierId = "";
                        }
                        loadRecordSearch();
                        break;
                    case "tfSearchLogInName":
                        if (lsValue.equals("")) {
                            psSearchCompanyId = "";
                        }
                        loadRecordSearch();
                        break;
                }
                loadRecordSearch();
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtMaster_Focus, tfLogInName, tfPassword, tfEmployeeName, tfProduct);
        JFXUtil.setFocusListener(txtField_Focus, tfSearchEmployeeName, tfSearchLogInName);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster);
    }

    public void clearTextFields() {
        psSearchCompanyId = "";
        psCompanyId = "";
        psSearchSupplierId = "";
        psSupplierId = "";

        JFXUtil.clearTextFields(apMaster);
    }

    public void initMainGrid() {
        JFXUtil.setColumnLeft(tblModule, tblRole, tblUserLevel);
        JFXUtil.setColumnsIndexAndDisableReordering(tblMain);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblMain.setItems(filteredData);
    }

    public void initLoadTable() {
        loadTableMain = new JFXUtil.ReloadableTableTask(
                tblMain,
                main_data,
                () -> {
                    main_data.clear();
                    Platform.runLater(() -> {
//                        Thread.sleep(100);
//                        if (poController.PurchaseOrderReceiving().getPurchaseOrderReturnCount() > 0) {
                        for (int lnCtr = 0; lnCtr <= 5 - 1; lnCtr++) {
                            try {
                                main_data.add(new ModelUserManagement("",
                                        String.valueOf(""),
                                        String.valueOf("")
                                ));
                            } catch (Exception e) {
                            }

                        }

                        if (pnMain < 0 || pnMain
                                >= main_data.size()) {
                            if (!main_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblMain.getSelectionModel().select(0);
                                tblMain.getFocusModel().focus(0);
                                pnMain = tblMain.getSelectionModel().getSelectedIndex();

                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblMain.getSelectionModel().select(pnMain);
                            tblMain.getFocusModel().focus(pnMain);

                        }

                    });

                });
    }
    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {
                    case "cmbUserLevel":

                        break;
                    case "cmbUserType":
                        break;
                }
                loadRecordMaster();
            }
    );

    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(UserLevel, cmbUserLevel), new JFXUtil.Pairs<>(UserType, cmbUserType));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbUserLevel, cmbUserType);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbUserLevel, cmbUserType);
    }

    public void initTableOnClick() {
        tblMain.setOnMouseClicked(event -> {
            pnMain = tblMain.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 1) {
                    loadTableDetailFromMain();
//                    pnEditMode = poAPPaymentAdjustmentController.APPaymentAdjustment().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });
        JFXUtil.adjustColumnForScrollbar(tblMain);
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

//        JFXUtil.setDisabled(lbShow3, apMaster);
    }

}
