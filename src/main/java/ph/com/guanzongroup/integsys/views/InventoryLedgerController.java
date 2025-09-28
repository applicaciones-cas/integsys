package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.TableModel;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelInvSerialLedger;

public class InventoryLedgerController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Inventory Ledger";
    private GRiderCAS oApp;
    private int pnEditMode;

    private int pnIndex = -1;
    private int pnRow = 0;

    private boolean pbLoaded = false;
    private boolean state = false;

    private String psCode;
    private String lsStockID, lsBrand;
//    private Inv oTrans;
    private ParamControllers oParameters;
    private InventoryMaintenanceController parentController;

    private LocalDate fromDate, thruDate;

    public int tbl_row = 0;

    private ObservableList<ModelInvSerialLedger> data = FXCollections.observableArrayList();

    public void setParentController(InventoryMaintenanceController cVal) {
        parentController = cVal;
    }

    public static TableModel empModel;
    @FXML
    private AnchorPane AnchorMain;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnRecalculate;
    @FXML
    private DatePicker dpField02;
    @FXML
    private DatePicker dpField01;
    @FXML
    private Button btnLoadLedger;
    @FXML
    private TextField txtField01;
    @FXML
    private TextField txtField02;
    @FXML
    private TextField txtField04;
    @FXML
    private TextField txtField03;
    @FXML
    private TextField txtField05;
    @FXML
    private TextField txtField06;
    @FXML
    private TableView tblInventoryLedger;

    @FXML
    private TableColumn index01;

    @FXML
    private TableColumn index02;

    @FXML
    private TableColumn index03;

    @FXML
    private TableColumn index04;

    @FXML
    private TableColumn index05;

    @FXML
    private TableColumn index06;
    @FXML
    private TableColumn index07;

    @FXML
    private TableColumn index08;

    public void setStockID(String foValue) {
        lsStockID = foValue;
    }

    public void setBranchNme(String foValue) {
        lsBrand = foValue;
    }
    private String fsCode;

//    private Inv poTrans;
    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initializeObject();
        initBrand();
        initDetails();
        handleActionButton();
        pbLoaded = true;
        initTable();

    }

    private void handleActionButton() {
        btnCancel.setOnAction(this::cmdButton_Click);
        btnClose.setOnAction(this::cmdButton_Click);
        btnLoadLedger.setOnAction(this::cmdButton_Click);
        btnRecalculate.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
    }

    private void initBrand() {
        try {
            JSONObject poJson;
            poJson = new JSONObject();
            poJson = oParameters.Brand().searchRecord(lsBrand, true);
            if ("success".equals((String) poJson.get("result"))) {
                txtField03.setText(oParameters.Brand().getModel().getDescription());
            }
        } catch (SQLException ex) {
            Logger.getLogger(InventoryLedgerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(InventoryLedgerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initDetails() {
//        txtField01.setText(poTrans.InvMaster().getModel().Inventory().getBarCode());
//        txtField02.setText(poTrans.InvMaster().getModel().Inventory().getDescription());
//
//        txtField04.setText(poTrans.InvMaster().getModel().Inventory().Model().getDescription());
//        txtField05.setText(poTrans.InvMaster().getModel().Inventory().Color().getDescription());
//        txtField06.setText(poTrans.InvMaster().getModel().Inventory().Measure().getMeasureName());
    }

    private void initializeObject() {
        String category = System.getProperty("store.inventory.industry");
        System.out.println("category == " + category);
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oParameters = new ParamControllers(oApp, logwrapr);
//        oTrans = new Inv(oApp, "", logwrapr);  // Ensure this isn't overwriting necessary data
    }

    public void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();

        JSONObject poJson;
        unloadForm appUnload = new unloadForm();
        switch (lsButton) {
            case "btnClose":  //Close
                if (parentController != null) {
//                    appUnload.useParentController(poTrans.InvMaster().getModel().Inventory().getStockId());
                }
                initializeObject();
                CommonUtils.closeStage(btnClose);
                break;
            case "btnRecalculate":  //Rcalculate
                ShowMessageFX.Information("This feature is currently unavailable.",
                        "Computerized Acounting System", pxeModuleName);
//                if (data.isEmpty()){
//                    ShowMessageFX.Information("Please ensure the ledger is loaded before performing recalculation."
//                            + "Recalculation cannot be completed correctly without loading the ledger first.",
//                            "Computerized Acounting System", pxeModuleName);
//                    break;
//                }else{
//                    try {
//
//                        dpField01.setValue(null);

//                        dpField02.setValue(null);
////                        poJson = poTrans.recalculate(poTrans.InvMaster().getModel().Inventory().getStockId());
//                        if("error".equalsIgnoreCase(poJson.get("result").toString())){
//                            ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
//                        }
//                        ShowMessageFX.Information("Recalculation completed succesfully",
//                            "Computerized Acounting System", pxeModuleName);
//                        poJson = new JSONObject();
////                        poJson = oTrans.InvMaster.OpenInvLedger(poTrans.getModel().getStockID());
//
//                        System.out.println("poJson = " + poJson.toJSONString());
//                        if("error".equalsIgnoreCase(poJson.get("result").toString())){
//                            ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
//                        }
//                        loadLedger();
//
//                    } catch (SQLException ex) {
//                        Logger.getLogger(InventoryLedgerController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
                break;

            case "btnLoadLedger":  //Close
//                CommonUtils.closeStage(btnClose);
                if (isDateEntryOkay()) {
//                    poTrans.OpenInvLedger(lsStockID, fromDate, thruDate);
                    loadLedger();
                    break;
                }
                break;

            case "btnCancel": //OK;
                if (parentController != null) {
//                    appUnload.useParentController(poTrans.InvMaster().getModel().Inventory().getStockId());
                }
                initializeObject();
                CommonUtils.closeStage(btnCancel);
                break;
//
//            default:
//                ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
//                return;
        }
    }

    private boolean isDateEntryOkay() {
        fromDate = dpField01.getValue();
        thruDate = dpField02.getValue();

        // Check if either date is null
        if (fromDate == null || thruDate == null) {
            ShowMessageFX.Information("Date From or Date Thru cannot be null or empty!",
                    "Computerized Accounting System", pxeModuleName);
            return false; // Validation failed
        }

        // Check if fromDate is after thruDate
        if (fromDate.isAfter(thruDate)) {
            ShowMessageFX.Information("'From Date' must be less than or equal to 'Thru Date'.",
                    "Computerized Accounting System", pxeModuleName);
            return false; // Validation failed
        }

        // If validation passes
        return true;
    }

    private void loadLedger() {
//        System.out.println("nagload and ledger");
//        data.clear();
//
//        if (poTrans.getInvLedgerCount() >= 0) {
//            for (int lnCtr = 0; lnCtr < poTrans.getInvLedgerCount(); lnCtr++) {
//                System.out.println("Processing Serial Ledger at Index: " + lnCtr);
//
//                // Debugging individual components
//                System.out.println("Transaction Date: " + poTrans.InvLedger(lnCtr).getTransactionDate());
//                System.out.println("Branch Name: " + poTrans.InvLedger(lnCtr).Branch().getBranchName());
//                System.out.println("Source Code: " + poTrans.InvLedger(lnCtr).getSourceCode());
//                System.out.println("Source No: " + poTrans.InvLedger(lnCtr).getSourceNo());
//
//                data.add(new ModelInvSerialLedger(
//                        String.valueOf(lnCtr + 1),
//                        poTrans.InvLedger(lnCtr).getTransactionDate().toString(),
//                        poTrans.InvLedger(lnCtr).Branch().getBranchName(),
//                        poTrans.InvLedger(lnCtr).getSourceCode(),
//                        poTrans.InvLedger(lnCtr).getSourceNo(),
//                        String.valueOf(poTrans.InvLedger(lnCtr).getQuantityIn()),
//                        String.valueOf(poTrans.InvLedger(lnCtr).getQuantityOut()),
//                        String.valueOf(poTrans.InvLedger(lnCtr).getQuantityOnHand())
//                ));
//            }
//        } else {
//            ShowMessageFX.Information("No Record Found!", "Computerized Acounting System", pxeModuleName);
//        }
    }

    private void initTable() {
        index01.setStyle("-fx-alignment: CENTER;");
        index02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index04.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index05.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index06.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index07.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
        index08.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");

        index01.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index05.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index06.setCellValueFactory(new PropertyValueFactory<>("index06"));
        index07.setCellValueFactory(new PropertyValueFactory<>("index07"));
        index08.setCellValueFactory(new PropertyValueFactory<>("index08"));
        tblInventoryLedger.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblInventoryLedger.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
        tblInventoryLedger.setItems(data);
        tblInventoryLedger.autosize();
    }

}
