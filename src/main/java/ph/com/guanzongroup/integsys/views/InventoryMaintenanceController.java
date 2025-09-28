/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;

/**
 * FXML Controller class
 *
 * @author User
 */
public class InventoryMaintenanceController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Inventory Maintenance";
    private GRiderCAS oApp;
    private String lsStockID, lsBrand;
    private int pnEditMode;

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean state = false;
    private boolean pbLoaded = false;
    private int pnInventory = 0;
//    private Inv oTrans;
    private ParamControllers oParameters;

    // Anchor Panes
    @FXML
    private AnchorPane AnchorMain, AnchorTable, AnchorInput;

// StackPane
    @FXML
    public StackPane overlay;

// GridPanes
    @FXML
    private GridPane gridEditable, gridFix;

// Text Fields
    @FXML
    private TextField txtSeeks01, txtSeeks02,
            txtField01, txtField02, txtField03, txtField04, txtField05,
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField13, txtField14, txtField15,
            txtField16, txtField17, txtField18, txtField19, txtField20,
            txtField21, txtField22, txtField23, txtField24, txtField25,
            txtField26, txtField27, txtField28, txtField29, txtField30,
            txtField31, txtField32, txtField33, txtField34,
            txtField261, txtField271;

// ComboBox
    @FXML
    private ComboBox cmbField01;

// CheckBoxes
    @FXML
    private CheckBox chkField01, chkField02, chkField03, chkField04;

// Buttons
    @FXML
    private Button btnBrowse, btnSave, btnUpdate, btnSearch, btnCancel,
            btnLedger, btnSerial, btnClose;

// Labels and Texts
    @FXML
    private Text lblShelf, lblMeasure;
    @FXML
    private Label lblStatus;

// Date Picker
    @FXML
    private DatePicker dpField01;

    @FXML
    void chkFiled01_Clicked(MouseEvent event) {

    }

    @FXML
    void chkFiled02_Clicked(MouseEvent event) {

    }

    @FXML
    void chkFiled03_Clicked(MouseEvent event) {

    }

    @FXML
    void chkFiled04_Clicked(MouseEvent event) {

    }

    /**
     * Initializes the controller class.
     */
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clearAllFields();
        initializeObject();
        InitTextFields();
        pnEditMode = EditMode.UNKNOWN;
        ClickButton();
        initTabAnchor();
        initButton(pnEditMode);
        pbLoaded = true;
        overlay.setVisible(false);

    }

    private void initializeObject() {
        String category = System.getProperty("store.inventory.industry");
        System.out.println("category == " + category);
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
//        oTrans = new Inv(oApp, "", logwrapr);
        oParameters = new ParamControllers(oApp, logwrapr);
    }

    /*Handle button click*/
    private void ClickButton() {
        btnCancel.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnLedger.setOnAction(this::handleButtonAction);
        btnSerial.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnBrowse.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
//        Object source = event.getSource();
//        JSONObject poJSON;
//        if (source instanceof Button) {
//            try {
//                Button clickedButton = (Button) source;
//                unloadForm appUnload = new unloadForm();
//                switch (clickedButton.getId()) {
//                    case "btnClose":
//                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
//                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
//                        }
//                        break;
//
//                    case "btnUpdate":
//                        poJSON = oTrans.InvMaster().updateRecord();
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            break;
//                        }
//                        pnEditMode = oTrans.InvMaster().getEditMode();
//
//                        System.err.println("update btn editmode ==" + pnEditMode);
//                        initButton(pnEditMode);
//                        initTabAnchor();
//                        break;
//                    case "btnCancel":
//                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
//                            clearAllFields();
//                            initializeObject();
//                            pnEditMode = EditMode.UNKNOWN;
//                            initButton(pnEditMode);
//                            initTabAnchor();
//                        }
//                        break;
//                    case "btnSave":
//                        oTrans.InvMaster().getModel().setModifyingId(oApp.getUserID());
//                        oTrans.InvMaster().getModel().setModifiedDate(oApp.getServerDate());
//                        JSONObject saveResult = oTrans.InvMaster().saveRecord();
//                        if ("success".equals((String) saveResult.get("result"))) {
//                            System.err.println((String) saveResult.get("message"));
//                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
//                            clearAllFields();
//                            pnEditMode = EditMode.UNKNOWN;
//                            initButton(pnEditMode);
//                            initTabAnchor();
//                            System.out.println("Record saved successfully.");
//                        } else {
//                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
//                            System.out.println("Record not saved successfully.");
//                            System.out.println((String) saveResult.get("message"));
//                        }
//                        break;
//
//                    case "btnBrowse":
//                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
//                        poJSON = oTrans.InvMaster().Inventory().searchRecord(lsValue, false);
//
//                        if ("success".equals((String) poJSON.get("result"))) {
//
//                            String stockId = oTrans.InvMaster().Inventory().getModel().getStockId();
//                            poJSON = oTrans.InvMaster().searchRecord(String.valueOf(stockId), true);
//                            System.out.print("brand sa browse == " + oTrans.InvMaster().getModel().Inventory().getBrandId());
//                            if ("success".equals((String) poJSON.get("result"))) {
//                                pnEditMode = oTrans.InvMaster().getEditMode();
//                                System.out.print("brand sa browse == " + oTrans.InvMaster().Inventory().getModel().Brand().getDescription());
//                                lsBrand = String.valueOf(oTrans.InvMaster().getModel().Inventory().getBrandId());
//
//                                loadInventory();
//
//                            } else {
//                                ShowMessageFX.Information("No Inventory found in your warehouse. Please save the record to create.", "Computerized Acounting System", "Inventory Detail");
//                                oTrans.InvMaster().newRecord();
//                                oTrans.InvMaster().getModel().setStockId(stockId);
//                                oTrans.InvMaster().getModel().setBranchCode(oApp.getBranchCode());
//                                pnEditMode = oTrans.InvMaster().getEditMode();
//                                loadInventory();
//
//                            }
//                        } else {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            txtSeeks01.clear();
//                            break;
//                        }
//                        initButton(pnEditMode);
//                        initTabAnchor();

//                    poJSON = oTrans.searchRecordwithBarrcode(lsValue, false);
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                        txtSeeks01.clear();
//                        break;
//                    }
//                    pnEditMode = oTrans.getEditMode();
////
//                    if (pnEditMode == EditMode.READY) {
//                        txtSeeks01.setText(oTrans.getModel().Inventory().getBarCode());
//                        txtSeeks02.setText(oTrans.getModel().Inventory().getDescription());
//                    } else {
//                        txtSeeks01.clear();
//                        txtSeeks02.clear();
//                    }
//
////                    data.clear();
//                    loadInventory();
//                    String lsValue = (txtSeeks01.getText().toString().isEmpty() ? "" : txtSeeks01.getText().toString());
//                    poJSON = oTrans.SearchInventory(lsValue, true);
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                        txtSeeks01.clear();
//                        break;
//                    }
//                    pnEditMode = oTrans.getEditMode();
//
//                    if (pnEditMode == EditMode.READY) {
//                        txtSeeks01.setText(oTrans.getModel().getBarCodex());
//                        txtSeeks02.setText(oTrans.getModel().getDescript());
//                    } else {
//                        txtSeeks01.clear();
//                        txtSeeks02.clear();
//                    }
//                    initButton(pnEditMode);
//                    System.out.print("\neditmode on browse == " + pnEditMode);
//                    initTabAnchor();
//                    loadInventory();
//        break;
//
//
//case "btnLedger": {
//                        try {
//                            if (pnEditMode == EditMode.READY
//                                    || pnEditMode == EditMode.ADDNEW
//                                    || pnEditMode == EditMode.UPDATE) {
//                                System.out.print("to pass == " + oTrans.InvMaster().Inventory().getModel().getStockId());
//
//                                loadLedger(lsStockID, lsBrand);
//                            }
//                        } catch (SQLException ex) {
//                            Logger.getLogger(InventoryMaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                    break;
//                    case "btnSerial": {
//                        try {
//                            if (pnEditMode == EditMode.READY
//                                    || pnEditMode == EditMode.ADDNEW
//                                    || pnEditMode == EditMode.UPDATE) {
//                                System.out.print("to pass == " + oTrans.InvMaster().Inventory().getModel().getStockId());
//
//                                loadSerial(lsStockID, lsBrand);
//                            }
//                        } catch (SQLException ex) {
//                            Logger.getLogger(InventoryMaintenanceController.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//
////                    if (!txtField01.getText().isEmpty()) {
////                        if (chkField01.isSelected()) {
////                            {
////                                try {
////                                    loadSerial(oTrans.getInvModel().getStockID());
////                                } catch (SQLException ex) {
////                                    Logger.getLogger(InventoryDetailController.class.getName()).log(Level.SEVERE, null, ex);
////                                }
////                            }
////                        } else {
////                            ShowMessageFX.Information("This Inventory is not serialize!", "Computerized Acounting System", pxeModuleName);
////                        }
////                    }
//                    break;
//                }
//            }
//
//catch (SQLException ex) {
//                Logger.getLogger(InventoryMaintenanceController.class
//
//.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    private void loadSerial(String fsCode, String fsBrand) throws SQLException {
        try {
            Stage stage = new Stage();

            overlay.setVisible(true);

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/rmj/guanzongroup/sidebarmenus/views/InventorySerial.fxml"));
//
//            InventorySerialController loControl = new InventorySerialController();
//            loControl.setGRider(oApp);
//            loControl.setFsCode(oTrans);
//            loControl.setStockID(fsCode);
//            loControl.setBranchNme(fsBrand);

//            fxmlLoader.setController(loControl);
            // Load the main interface
            Parent parent = fxmlLoader.load();
            parent.setStyle("-fx-background-color: rgba(0, 0, 0, 1);");

            // Set up dragging
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];

            parent.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });

            parent.setOnMouseDragged(event -> {
                double newX = event.getScreenX() - xOffset[0];
                double newY = event.getScreenY() - yOffset[0];

                // Get the screen bounds
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

                // Calculate the window bounds
                double stageWidth = stage.getWidth();
                double stageHeight = stage.getHeight();

                // Constrain the stage position to the screen bounds
                if (newX < 0) {
                    newX = 0;
                }
                if (newY < 0) {
                    newY = 0;
                }
                if (newX + stageWidth > screenBounds.getWidth()) {
                    newX = screenBounds.getWidth() - stageWidth;
                }
                if (newY + stageHeight > screenBounds.getHeight()) {
                    newY = screenBounds.getHeight() - stageHeight;
                }

                stage.setX(newX);
                stage.setY(newY);
            });

            // Set the main interface as the scene
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Inventory Serial");

            // Add close request handler
            stage.setOnCloseRequest(event -> {
                System.out.println("Stage is closing");
                overlay.setVisible(false);
            });

            stage.setOnHidden(e -> overlay.setVisible(false));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    private void loadLedger(String fsCode, String fsBrand) throws SQLException {
        try {
            Stage stage = new Stage();

            overlay.setVisible(true);

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/rmj/guanzongroup/sidebarmenus/views/InventoryLedger.fxml"));
//
//            InventoryLedgerController loControl = new InventoryLedgerController();
//            loControl.setGRider(oApp);
//            loControl.setFsCode(oTrans);

//            loControl.setStockID(fsCode);
//            loControl.setBranchNme(fsBrand);
//            loControl.setParentController(this);
//            fxmlLoader.setController(loControl);
            // Load the main interface
            Parent parent = fxmlLoader.load();

            // Set up dragging
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];

            parent.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });

            parent.setOnMouseDragged(event -> {
                double newX = event.getScreenX() - xOffset[0];
                double newY = event.getScreenY() - yOffset[0];

                // Get the screen bounds
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

                // Calculate the window bounds
                double stageWidth = stage.getWidth();
                double stageHeight = stage.getHeight();

                // Constrain the stage position to the screen bounds
                if (newX < 0) {
                    newX = 0;
                }
                if (newY < 0) {
                    newY = 0;
                }
                if (newX + stageWidth > screenBounds.getWidth()) {
                    newX = screenBounds.getWidth() - stageWidth;
                }
                if (newY + stageHeight > screenBounds.getHeight()) {
                    newY = screenBounds.getHeight() - stageHeight;
                }

                stage.setX(newX);
                stage.setY(newY);
            });

            Scene scene = new Scene(parent);

            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setTitle("Inventory Ledger");

            // Add close request handler
            stage.setOnCloseRequest(event -> {
                System.out.println("Stage is closing");
                overlay.setVisible(false);
            });

            stage.setOnHidden(e -> {
                System.out.println("Stage is hidden");
                overlay.setVisible(false);
                loadResult(lsStockID, true);
            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            ShowMessageFX.Warning(getStage(), e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    /*USE TO DISABLE ANCHOR BASE ON INITMODE*/
    private void initTabAnchor() {
        System.out.print("EDIT MODE == " + pnEditMode);
        boolean pbValue = pnEditMode == EditMode.ADDNEW
                || pnEditMode == EditMode.UPDATE;

        System.out.print("pbValue == " + pbValue);
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            AnchorInput.setDisable(false);
            gridFix.setDisable(pbValue);
            gridEditable.setDisable(!pbValue);
        }
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN) {
            AnchorInput.setDisable(true);
            gridFix.setDisable(!pbValue);
            gridEditable.setDisable(!pbValue);
        }
        System.out.println("EDIT MODE STAT == " + pnEditMode);
        if (pnEditMode == EditMode.UPDATE || pnEditMode == 2) {
            txtField22.setDisable(true);
        }
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == 0) {
            txtField22.setDisable(false);
        }

    }

    /*TO CONTROL BUTTONS BASE ON INITMODE*/
    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);

        // Set visibility and manageability for buttons based on lbShow
        btnSave.setVisible(lbShow);
        btnSave.setManaged(lbShow);

        btnSearch.setVisible(lbShow);
        btnSearch.setManaged(lbShow);

        btnCancel.setVisible(lbShow);
        btnCancel.setManaged(lbShow);

        btnLedger.setVisible(!lbShow);
        btnLedger.setManaged(!lbShow);

        btnSerial.setVisible(!lbShow);
        btnSerial.setManaged(!lbShow);

        btnClose.setVisible(!lbShow);
        btnClose.setManaged(!lbShow);

        btnUpdate.setVisible(!lbShow);
        btnUpdate.setManaged(!lbShow);

        btnBrowse.setVisible(!lbShow);
        btnBrowse.setManaged(!lbShow);

        txtSeeks01.setDisable(!lbShow);
        txtSeeks02.setDisable(!lbShow);

        if (lbShow) {
            txtSeeks01.clear();
            txtSeeks02.clear();

            txtSeeks01.setDisable(true);
            txtSeeks02.setDisable(true);

            btnUpdate.setManaged(false);
            btnBrowse.setManaged(false);
            btnCancel.setManaged(true);
            btnLedger.setManaged(false);
            btnSerial.setManaged(false);
            btnClose.setManaged(false);
        } else {
            txtSeeks01.setDisable(false);
            txtSeeks02.setDisable(false);

            txtSeeks01.requestFocus();

            btnUpdate.setManaged(true);
            btnBrowse.setManaged(true);
            btnCancel.setManaged(false);
            btnLedger.setManaged(true);
            btnSerial.setManaged(true);
            btnClose.setManaged(true);
        }
    }

    private void loadInventory() {
//        if (pnEditMode == EditMode.READY
//                || pnEditMode == EditMode.ADDNEW
//                || pnEditMode == EditMode.UPDATE) {
//            System.out.println("stoickid == " + (String) oTrans.InvMaster().getModel().getStockId());
//            System.out.println("stoickid == " + (String) oTrans.InvMaster().getModel().Inventory().getStockId());
//            txtField01.setText((String) oTrans.InvMaster().getModel().Inventory().getStockId());
//            txtField02.setText((String) oTrans.InvMaster().getModel().Inventory().getBarCode());
//            txtField03.setText((String) oTrans.InvMaster().getModel().Inventory().getAlternateBarCode());
//            txtField04.setText((String) oTrans.InvMaster().getModel().Inventory().getBriefDescription());
//            txtField05.setText((String) oTrans.InvMaster().getModel().Inventory().getDescription());
////
//            txtField06.setText((String) oTrans.InvMaster().getModel().Inventory().Category().getDescription());
//            txtField07.setText((String) oTrans.InvMaster().getModel().Inventory().CategoryLevel2().getDescription());
//            txtField08.setText((String) oTrans.InvMaster().getModel().Inventory().CategoryLevel3().getDescription());
//            txtField09.setText((String) oTrans.InvMaster().getModel().Inventory().CategoryLevel4().getDescription());
////
//            txtField10.setText((String) oTrans.InvMaster().getModel().Inventory().Brand().getDescription());
//            txtField11.setText((String) oTrans.InvMaster().getModel().Inventory().Model().getDescription());
//            txtField12.setText((String) oTrans.InvMaster().getModel().Inventory().Color().getDescription());
//            txtField13.setText((String) oTrans.InvMaster().getModel().Inventory().Measure().getMeasureName());
////
//            txtField14.setText(CommonUtils.NumberFormat(Double.parseDouble(oTrans.InvMaster().getModel().Inventory().getDiscountRateLevel1().toString()), "#,##0.00"));
//            txtField15.setText(CommonUtils.NumberFormat(Double.parseDouble(oTrans.InvMaster().getModel().Inventory().getDiscountRateLevel2().toString()), "#,##0.00"));
//            txtField16.setText(CommonUtils.NumberFormat(Double.parseDouble(oTrans.InvMaster().getModel().Inventory().getDiscountRateLevel3().toString()), "#,##0.00"));
//            txtField17.setText(CommonUtils.NumberFormat(Double.parseDouble(oTrans.InvMaster().getModel().Inventory().getDealerDiscountRate().toString()), "#,##0.00"));
////
//            txtField26.setText(String.valueOf(oTrans.InvMaster().getModel().Inventory().getMinimumInventoryLevel()));
//            txtField27.setText(String.valueOf(oTrans.InvMaster().getModel().Inventory().getMaximumInventoryLevel()));
//            txtField29.setText(String.valueOf(oTrans.InvMaster().getModel().Inventory().getCost()));
//            txtField30.setText(String.valueOf(oTrans.InvMaster().getModel().Inventory().getSellingPrice()));
//
//            System.out.println("to load == " + oTrans.InvMaster().getModel().Inventory().getInventoryTypeId());
//            ObservableList<String> unitTypes = ObservableListUtil.UNIT_TYPES;
//            cmbField01.setItems(unitTypes);
//            cmbField01.getSelectionModel().select(7);
//
//            lsStockID = oTrans.InvMaster().getModel().getStockId();
////            lsBrand = txtField10.getText();
//            if (pnEditMode == EditMode.ADDNEW) {
//                txtField22.setPromptText("PRESS F3: Search");
//            }
//
//            lblStatus.setText(chkField04.isSelected() ? "ACTIVE" : "INACTIVE");
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Get the object from the model
//            Object dbegInvxx = oTrans.getModel().getDBegInvxx();
//
//            if (dbegInvxx == null) {
//                // If the object is null, set the DatePicker to the current date
//                dpField01.setValue(LocalDate.now());
//            } else if (dbegInvxx instanceof Timestamp) {
//                // If the object is a Timestamp, convert it to LocalDate
//                Timestamp timestamp = (Timestamp) dbegInvxx;
//                LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
//                dpField01.setValue(localDate);
//            } else if (dbegInvxx instanceof Date) {
//                // If the object is a java.sql.Date, convert it to LocalDate
//                Date sqlDate = (Date) dbegInvxx;
//                LocalDate localDate = sqlDate.toLocalDate();
//                dpField01.setValue(localDate);
//            } else {
//                // Handle unexpected types or throw an exception
//                throw new IllegalArgumentException("Expected a Timestamp or Date, but got: " + dbegInvxx.getClass().getName());
//            }
//        initSubItemForm();
//    }
    }

    private void initSubItemForm() {
//        if (!oTrans.getInvModel().getCategCd1().isEmpty()) { // Ensure the string is not empty
//            switch (oTrans.getInvModel().getCategCd1()) {
//                case "0001":
//                case "0002":
//                case "0003":
//                    lblMeasure.setVisible(false);
//                    lblShelf.setVisible(false);
//                    txtField13.setVisible(false);
//                    txtField21.setVisible(false);
//                    break;
//                case "0004":
//                    lblMeasure.setVisible(true);
//                    lblShelf.setVisible(true);
//                    txtField13.setVisible(true);
//                    txtField21.setVisible(true);
//                    break;
//            }
//        }
    }

    private void InitTextFields() {

        // Create an array for text fields with focusedProperty listeners
        TextField[] focusTextFields = {
            txtField01, txtField02, txtField03, txtField04, txtField05,
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField13, txtField14, txtField15,
            txtField16, txtField17, txtField18, txtField19, txtField20,
            txtField21, txtField22, txtField23, txtField24, txtField25,
            txtField26, txtField27
        };

        // Add the listener to each text field in the focusTextFields array
        for (TextField textField : focusTextFields) {
            textField.focusedProperty().addListener(txtField_Focus);
        }

        // Create an array for text fields with setOnKeyPressed handlers
        TextField[] keyPressedTextFields = {
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField22, txtField25
        };

        // Set the same key pressed event handler for each text field in the keyPressedTextFields array
        for (TextField textField : keyPressedTextFields) {
            textField.setOnKeyPressed(this::txtField_KeyPressed);
        }

        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);
        txtSeeks02.setOnKeyPressed(this::txtSeeks_KeyPressed);

        lblStatus.setText(chkField04.isSelected() ? "ACTIVE" : "INACTIVE");
    }

    /*Text seek/search*/
    private void txtSeeks_KeyPressed(KeyEvent event) {
//        TextField txtSeeks = (TextField) event.getSource();
//        int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
//        String lsValue = (txtSeeks.getText() == null ? "" : txtSeeks.getText());
//        JSONObject poJSON;
//        switch (event.getCode()) {
//            case F3:
//            case ENTER:
//                switch (lnIndex) {
//
//                    case 1:
//                        /*search Barrcode*/
//                        System.out.print("LSVALUE OF SEARCH 1 ==== " + lsValue);
//                        poJSON = oTrans.InvMaster().searchRecord(lsValue, true);
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            txtSeeks01.clear();
//                            break;
//                        }
//                        pnEditMode = oTrans.InvMaster().getEditMode();
//
//                        if (pnEditMode == EditMode.READY) {
////                            txtSeeks01.setText(oTrans.getModel().getBarCodex());
////                            txtSeeks02.setText(oTrans.getModel().getDescript());
//                        } else {
//                            txtSeeks01.clear();
//                            txtSeeks02.clear();
//                        }
//                        lsBrand = String.valueOf(oTrans.InvMaster().getModel().Inventory().getBrandId());
//                        initButton(pnEditMode);
//                        System.out.print("\neditmode on browse == " + pnEditMode);
//                        initTabAnchor();
//                        loadInventory();
//
//                        break;
//                    case 2:
////                        System.out.print("LSVALUE OF SEARCH 1 ==== " + lsValue);
//                        poJSON = oTrans.InvMaster().searchRecord(lsValue, false);
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            txtSeeks01.clear();
//                            break;
//                        }
//                        pnEditMode = oTrans.InvMaster().getEditMode();
//
//                        if (pnEditMode == EditMode.READY) {
////                            txtSeeks01.setText(oTrans.getModel().getBarCodex());
////                            txtSeeks02.setText(oTrans.getModel().getDescript());
//                        } else {
//                            txtSeeks01.clear();
//                            txtSeeks02.clear();
//                        }
//                        lsBrand = String.valueOf(oTrans.InvMaster().getModel().Inventory().getBrandId());
//                        initButton(pnEditMode);
//                        System.out.print("\neditmode on browse == " + pnEditMode);
//                        initTabAnchor();
//                        loadInventory();
//                        break;
//                }
//
//        }
//        switch (event.getCode()) {
//            case ENTER:
//                CommonUtils.SetNextFocus(txtSeeks);
//            case DOWN:
//                CommonUtils.SetNextFocus(txtSeeks);
//                break;
//            case UP:
//                CommonUtils.SetPreviousFocus(txtSeeks);
//        }
    }

    /*textfield lost focus*/
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();
        JSONObject jsonObject = new JSONObject();
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lnIndex) {
                case 25:
                    /*Stock ID*/
                    UnaryOperator<TextFormatter.Change> limitText = change -> {
                        String newText = change.getControlNewText();
                        if (newText.length() > 5) {
                            return null; // Disallow the change if it exceeds 5 characters
                        }
                        return change; // Allow the change
                    };

                    // Create a TextFormatter with the UnaryOperator
                    TextFormatter<String> textFormatter = new TextFormatter<>(limitText);

                    // Apply the TextFormatter to the TextField
                    txtField.setTextFormatter(textFormatter);

//                    oTrans.InvMaster().getModel().setBinId(lsValue);
                    break;
            }
        } else {
            txtField.selectAll();
        }
    };

    /*Txtfield search*/
    private void txtField_KeyPressed(KeyEvent event) {
//        TextField txtField = (TextField) event.getSource();
//        int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
//        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
//        JSONObject poJson;
//        switch (event.getCode()) {
//            case F3:
//                switch (lnIndex) {
//                    case 22:
//                        poJson = new JSONObject();
//                        poJson = oParameters.InventoryLocation().searchRecord(lsValue, false);
//                        System.out.println("poJson = " + poJson.toJSONString());
//
//                        oTrans.InvMaster().getModel().setLocationId(oParameters.InventoryLocation().getModel().getLocationId());
//
//                        if ("success".equals(poJson.get("result"))) {
//                            txtField22.setText((String) oParameters.InventoryLocation().getModel().getDescription());
//                            String warehouse = (String) oParameters.InventoryLocation().getModel().getWarehouseId();
//                            oTrans.InvMaster().getModel().setWarehouseId(warehouse);
//
//                            poJson = oParameters.Warehouse().searchRecord(warehouse, true);
//                            if ("success".equals(poJson.get("result"))) {
//                                txtField23.setText(oParameters.Warehouse().getModel().getWarehouseName());
//
//                                String section = (String) oParameters.InventoryLocation().getModel().getSectionId();
//                                poJson = oParameters.Section().searchRecord(section, true);
//                                if ("success".equals(poJson.get("result"))) {
//                                    txtField24.setText(oParameters.Section().getModel().getSectionName());
//                                }
//                            }
//                        } else {
//                            ShowMessageFX.Information(
//                                    (String) poJson.get("message"),
//                                    "Computerized Accounting System",
//                                    pxeModuleName
//                            );
//                        }
//
//                        break;
//                }
//            case ENTER:
//        }
//        switch (event.getCode()) {
//            case ENTER:
//                CommonUtils.SetNextFocus(txtField);
//            case DOWN:
//                CommonUtils.SetNextFocus(txtField);
//                break;
//            case UP:
//                CommonUtils.SetPreviousFocus(txtField);
//        }
    }

    private void clearAllFields() {
        // Arrays of TextFields grouped by sections
        TextField[][] allFields = {
            // Text fields related to specific sections
            {txtSeeks01, txtSeeks02, txtField01, txtField02, txtField03, txtField04,
                txtField05, txtField06, txtField07, txtField08, txtField09, txtField10,
                txtField11, txtField12, txtField13, txtField14, txtField15, txtField16,
                txtField17, txtField18, txtField19, txtField20, txtField21, txtField22,
                txtField23, txtField24, txtField25, txtField26, txtField27, txtField28, txtField29,
                txtField30, txtField31, txtField32, txtField33, txtField34},};
        chkField01.setSelected(false);
        chkField02.setSelected(false);
        chkField03.setSelected(false);
        chkField04.setSelected(false);
        cmbField01.setValue(null);
        cmbField01.setValue(null);

        // Loop through each array of TextFields and clear them
        for (TextField[] fields : allFields) {
            for (TextField field : fields) {
                field.clear();
            }
        }
    }

    private Stage getStage() {
        return (Stage) txtField01.getScene().getWindow();
    }

    public void setOverlay(boolean fbVal) {
        overlay.setVisible(fbVal);
    }

    public void loadResult(String fsValue, boolean fbVal) {
//
//        initializeObject();
//        JSONObject poJson = new JSONObject();
//        overlay.setVisible(false);
//        poJson = oTrans.InvMaster().searchRecord(fsValue, fbVal);
////        poJson = oTrans.openRecord(fsValue);
//        if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
//            ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
//        }
//        initButton(pnEditMode);
//        System.out.print("\neditmode on browse == " + pnEditMode);
//        initTabAnchor();
//        loadInventory();

    }
}
