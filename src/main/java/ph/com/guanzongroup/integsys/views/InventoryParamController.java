package ph.com.guanzongroup.integsys.views;


import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelInvSubUnit;

/**
 * FXML Controller class
 *
 * @author User
 */
public class InventoryParamController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Inventory Parameter";
    LogWrapper logwrapr = new LogWrapper("cas", "cas-err.log");

    private GRiderCAS oApp;
    private int pnEditMode;
    private Inventory oTrans;
    private ParamControllers oParameters;
    private boolean pbLoaded = false;

    private ObservableList<ModelInvSubUnit> data = FXCollections.observableArrayList();
    String category = System.getProperty("store.inventory.industry");

    @FXML
    private AnchorPane AnchorInput, AnchorTable, AnchorMain;

    @FXML
    private GridPane subItemFields;

    @FXML
    private HBox hbButtons;

    @FXML
    private Label lblStatus;

    @FXML
    private Text lblMeasure, lblShelf;

    @FXML
    private TableView tblSubItems;

    @FXML
    private TableColumn index01, index02, index03, index04, index05;

// Buttons
    @FXML
    private Button btnBrowse, btnNew, btnAddSubItem, btnDelSubUnit,
            btnSave, btnUpdate, btnSearch, btnCancel, btnClose;

// Text Fields
    @FXML
    private TextField txtSeeks01, txtSeeks02,
            txtField01, txtField02, txtField03, txtField04, txtField05,
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField13, txtField14, txtField15,
            txtField16, txtField17, txtField18, txtField19, txtField20,
            txtField21, txtField22, txtField23, txtField24, txtField25,
            txtField26, txtField27;

// Combo Boxes
    @FXML
    private ComboBox cmbField01, cmbField02;

// Check Boxes
    @FXML
    private CheckBox chkField01, chkField02, chkField03, chkFiled04;

    ObservableList<String> unitType = FXCollections.observableArrayList(
            "LDU",
            "Regular",
            "Free",
            "Live",
            "Service",
            "RDU",
            "Others"
    );

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

    @FXML
    void chkFiled01_Clicked(MouseEvent event) {
        oTrans.getModel().isSerialized(chkField01.isSelected());
    }

    @FXML
    void chkFiled02_Clicked(MouseEvent event) {
        oTrans.getModel().isComboInventory(chkField02.isSelected());
    }

    @FXML
    void chkFiled03_Clicked(MouseEvent event) {
        oTrans.getModel().isSerialized(chkField03.isSelected());
    }

    @FXML
    void chkFiled04_Clicked(MouseEvent event) {
        boolean isChecked = chkFiled04.isSelected();
        oTrans.getModel().setRecordStatus((isChecked ? "1" : "0"));
    }

    @FXML
    void tblSubUnit_Clicked(MouseEvent event) {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        clearAllFields();
        initializeObject();
        InitTextFields();
        pnEditMode = EditMode.UNKNOWN;
        ClickButton();
        initButton(pnEditMode);
        AnchorTable.setVisible(false);
        pbLoaded = true;
//        System.out.println("categ == " + );
    }

    private void initializeObject() {
        System.out.println("category == " + category);
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oTrans = new Inventory();
        oTrans.setApplicationDriver(oApp);
        oTrans.setWithParentClass(false);
        oTrans.setLogWrapper(logwrapr);
        oTrans.initialize();
        oParameters = new ParamControllers(oApp, logwrapr);

    }

//    /*Handle button click*/
    private void ClickButton() {
        btnCancel.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
        btnBrowse.setOnAction(this::handleButtonAction);
        btnAddSubItem.setOnAction(this::handleButtonAction);
        btnDelSubUnit.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof Button) {
            try {
                Button clickedButton = (Button) source;
                unloadForm appUnload = new unloadForm();
                switch (clickedButton.getId()) {
                    case "btnClose":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField02.requestFocus();
                        JSONObject poJSON;
                        poJSON = oTrans.newRecord();
                        pnEditMode = EditMode.READY;
                        if ("success".equals((String) poJSON.get("result"))) {
                            pnEditMode = EditMode.ADDNEW;
                            initButton(pnEditMode);
                            loadInventory();;
                            initTabAnchor();

                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            System.out.println((String) poJSON.get("message"));
                            initTabAnchor();
                        }
                        break;
                    case "btnBrowse":

                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oTrans.searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        pnEditMode = EditMode.READY;
                        data.clear();
                        loadInventory();
                        break;
                    case "btnUpdate":
                        poJSON = oTrans.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oTrans.getEditMode();
                        System.out.println("EDITMODE sa update= " + pnEditMode);
                        initButton(pnEditMode);
                        initTabAnchor();
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            initTabAnchor();
                        }
                        System.out.println("EDITMODE sa cancel= " + pnEditMode);
                        break;
                    case "btnSave":
                        oTrans.getModel().setModifyingId(oApp.getUserID());
                        oTrans.getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oTrans.saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                            clearAllFields();
                            System.out.println("Record saved successfully.");
                        } else {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            System.out.println("Record not saved successfully.");
                            System.out.println((String) saveResult.get("message"));
                        }

                        break;
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    /*USE TO DISABLE ANCHOR BASE ON INITMODE*/
    private void initTabAnchor() {
        boolean pbValue = (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        AnchorInput.setDisable(!pbValue);
        subItemFields.setDisable(!pbValue);
        AnchorTable.setDisable(!pbValue);
        if (pnEditMode == EditMode.READY || pnEditMode == EditMode.UNKNOWN) {
            AnchorTable.setDisable(pbValue);
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);

        // Manage visibility and managed state of primary buttons
        btnCancel.setVisible(lbShow);
        btnCancel.setManaged(lbShow);
        btnSearch.setVisible(lbShow);
        btnSearch.setManaged(lbShow);
        btnSave.setVisible(lbShow);
        btnSave.setManaged(lbShow);
        btnUpdate.setVisible(lbShow);
        btnUpdate.setManaged(lbShow);

        // Manage visibility and managed state of other buttons
        btnBrowse.setVisible(!lbShow);
        btnBrowse.setManaged(!lbShow);

        btnClose.setVisible(true);
        btnClose.setManaged(true);

        //allows you to check if branch is warehouse
        isWarehouse();

    }

    private boolean isWarehouse() {
//        try {
//            //Only warehouse branch can use the Button New to create Inventorry
//            JSONObject poJson = oParameters.Branch().(oApp.getBranchCode(), true);
//
//            if ("success".equals(poJson.get("result"))) {
//                boolean isWarehouse = oParameters.Branch().getModel().isWarehouse();
//                btnNew.setVisible(isWarehouse);
//                btnNew.setManaged(isWarehouse);
//                return true;
//            }
//            return false;
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return false;
    }

    private void initSubItemForm() {
        if (!oTrans.getModel().getCategoryFirstLevelId().isEmpty()) { // Ensure the string is not empty
            switch (oTrans.getModel().getCategoryFirstLevelId()) {
                case "0001":
                case "0002":
                case "0003":
                    AnchorTable.setVisible(false);
                    lblMeasure.setVisible(false);
                    lblShelf.setVisible(false);
                    txtField13.setVisible(false);
                    txtField21.setVisible(false);
                    break;
                case "0004":
                    AnchorTable.setVisible(true);
                    lblMeasure.setVisible(true);
                    lblShelf.setVisible(true);
                    txtField13.setVisible(true);
                    txtField21.setVisible(true);
                    break;
            }
        }
    }

    private void InitTextFields() {
        // Text fields that require focus listeners
        TextField[] focusTextFields = {
            txtField01, txtField02, txtField03, txtField04, txtField05,
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField13, txtField14, txtField15,
            txtField16, txtField17, txtField18, txtField19, txtField20,
            txtField21, txtField22, txtField23, txtField24, txtField25,
            txtField26, txtField27
        };

        // Add focus listener to each text field
        for (TextField textField : focusTextFields) {
            textField.focusedProperty().addListener(txtField_Focus);
        }

        // Text fields that require key press handlers
        TextField[] keyPressedTextFields = {
            txtField06, txtField07, txtField08, txtField09, txtField10,
            txtField11, txtField12, txtField13, txtField22
        };

        // Add key press event handler to each text field
        for (TextField textField : keyPressedTextFields) {
            textField.setOnKeyPressed(this::txtField_KeyPressed);
        }

    }

//    /*textfield lost focus*/
    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        if (!pbLoaded) {
            return;
        }

        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) { // Lost focus
            try {
                switch (lnIndex) {
                    case 1: // Stock ID
                        oTrans.getModel().setStockId(lsValue);
                        break;
                    case 2: // Barcode
                        oTrans.getModel().setBarCode(lsValue);
                        break;
                    case 3: // Alternate Barcode
                        oTrans.getModel().setAlternateBarCode(lsValue);
                        break;
                    case 4: // Brief Description
                        oTrans.getModel().setBriefDescription(lsValue);
                        break;
                    case 5: // Description
                        oTrans.getModel().setDescription(lsValue);
                        break;
                    case 14: // Discount Level 1
                    case 15: // Discount Level 2
                    case 16: // Discount Level 3
                    case 17: // Dealer Discount
                        double discount = Double.parseDouble(lsValue);
                        if (lnIndex == 14) {
                            oTrans.getModel().setDiscountRateLevel1(discount);
                        }
                        if (lnIndex == 15) {
                            oTrans.getModel().setDiscountRateLevel2(discount);
                        }
                        if (lnIndex == 16) {
                            oTrans.getModel().setDiscountRateLevel3(discount);
                        }
                        if (lnIndex == 17) {
                            oTrans.getModel().setDealerDiscountRate(discount);
                        }
                        txtField.setText(CommonUtils.NumberFormat(discount, "0.00"));
                        break;
                    case 26: // Minimum Inventory Level
                        oTrans.getModel().setMinimumInventoryLevel(Integer.parseInt(lsValue));
                        break;
                    case 27: // Maximum Inventory Level
                        oTrans.getModel().setMaximumInventoryLevel(Integer.parseInt(lsValue));
                        break;
                    case 19: // Selling Price
                        double sellingPrice = Double.parseDouble(lsValue.replace(",", ""));
                        oTrans.getModel().setSellingPrice(sellingPrice);
                        txtField.setText(CommonUtils.NumberFormat(sellingPrice, "#,##0.00"));
                        break;
                    case 20: // Superseded ID
                        oTrans.getModel().setSupersededId(lsValue);
                        break;
                    case 21: // Shelf Life
                        int shelfLife = lsValue.isEmpty() ? 0 : Integer.parseInt(lsValue);
                        oTrans.getModel().setShelfLife(shelfLife);
                        break;
                    default:
                        // Other cases can be handled here if needed.
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else { // Gained focus
            txtField.selectAll();
        }
    };

    /*Text Field with search*/
    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 06:
                            poJson = oParameters.Category().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setCategoryFirstLevelId(oParameters.Category().getModel().getCategoryId());
                            txtField06.setText((String) oParameters.Category().getModel().getDescription());
                            txtField07.requestFocus();
                            break;
                        case 07:
                            poJson = oParameters.CategoryLevel2().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setCategoryIdSecondLevel(oParameters.CategoryLevel2().getModel().getCategoryId());
                            txtField07.setText((String) oParameters.CategoryLevel2().getModel().getDescription());
                            poJson = oParameters.InventoryType().searchRecord(oParameters.CategoryLevel2().getModel().getInventoryTypeCode(), true);
                            if ("success".equals((String) poJson.get("result"))) {
                                cmbField01.setValue(oParameters.InventoryType().getModel().getDescription());
                                oTrans.  getModel().setInventoryTypeId(oParameters.CategoryLevel2().getModel().getInventoryTypeCode());
                            }
                            break;
                        case 8:
                            poJson = oParameters.CategoryLevel3().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setCategoryIdThirdLevel(oParameters.CategoryLevel3().getModel().getCategoryId());
                            txtField08.setText((String) oParameters.CategoryLevel3().getModel().getDescription());
                            break;
                        case 9:
                            poJson = oParameters.CategoryLevel4().searchRecord(lsValue, false);
                            System.out.println("poJson = " + poJson.toJSONString());
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setCategoryIdFourthLevel(oParameters.CategoryLevel4().getModel().getCategoryId());
                            txtField09.setText((String) oParameters.CategoryLevel4().getModel().getDescription());
                            break;
                        case 10:
                            poJson = oParameters.Brand().searchRecord(lsValue, false);
                            System.out.println("poJson = " + poJson.toJSONString());
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setBrandId(oParameters.Brand().getModel().getBrandId());
                            txtField10.setText((String) oParameters.Brand().getModel().getDescription());
                            break;
                        case 11:
                            poJson = oParameters.Model().searchRecord(lsValue, false);
                            System.out.println("poJson = " + poJson.toJSONString());
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setModelId(oParameters.Model().getModel().getModelId());
                            txtField11.setText((String) oParameters.Model().getModel().getDescription());
                            break;
                        case 12:
                            poJson = oParameters.Color().searchRecord(lsValue, false);
                            System.out.println("poJson = " + poJson.toJSONString());
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setColorId(oParameters.Color().getModel().getColorId());
                            txtField12.setText((String) oParameters.Color().getModel().getDescription());
                            break;
                        case 13:

                            poJson = oParameters.Measurement().searchRecord(lsValue, false);
                            System.out.println("poJson = " + poJson.toJSONString());
                            if ("error".equalsIgnoreCase(poJson.get("result").toString())) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                            }
                            oTrans.getModel().setMeasurementId(oParameters.Measurement().getModel().getMeasureId());
                            txtField13.setText((String) oParameters.Measurement().getModel().getDescription());
                            break;
                        case 22:
                            poJson = new JSONObject();
                            break;
                    }
                case ENTER:
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (SQLException ex) {
            Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtSeeks = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtSeeks.getText() == null ? "" : txtSeeks.getText());
            JSONObject poJSON;
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 1:
                            System.out.print("LSVALUE OF SEARCH 1 ==== " + lsValue);
                            poJSON = oTrans.searchRecord(lsValue, true);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText(oTrans.getModel().getBarCode());
                            txtSeeks02.setText(oTrans.getModel().getDescription());
                            pnEditMode = oTrans.getEditMode();
                            loadInventory();

                            break;
                        case 2:
                            poJSON = oTrans.searchRecord(lsValue, false);
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            pnEditMode = oTrans.getEditMode();
                            txtSeeks01.setText(oTrans.getModel().getBarCode());
                            txtSeeks02.setText(oTrans.getModel().getDescription());
                            System.out.print("\neditmode on browse == " + pnEditMode);
                            loadInventory();
                            System.out.println("EDITMODE sa cancel= " + pnEditMode);
                            break;
                    }
                case ENTER:
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtSeeks);
                case DOWN:
                    CommonUtils.SetNextFocus(txtSeeks);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtSeeks);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearAllFields() {
        TextField[][] allFields = {
            {txtSeeks01, txtSeeks02, txtField01, txtField02, txtField03, txtField04,
                txtField05, txtField06, txtField07, txtField08, txtField09, txtField10,
                txtField11, txtField12, txtField13, txtField14, txtField15, txtField16,
                txtField17, txtField18, txtField19, txtField20, txtField21, txtField22,
                txtField23, txtField24, txtField25, txtField26, txtField27},};
        chkField01.setSelected(false);
        chkField02.setSelected(false);
        chkField03.setSelected(false);
        cmbField01.setValue(null);
        cmbField01.setValue(null);
        for (TextField[] fields : allFields) {
            for (TextField field : fields) {
                field.clear();
            }
        }
        data.clear();
    }

    private void loadInventory() {
        if (pnEditMode == EditMode.READY
                || pnEditMode == EditMode.ADDNEW
                || pnEditMode == EditMode.UPDATE) {

            try {
                txtField01.setText((String) oTrans.getModel().getStockId());
                txtField02.setText((String) oTrans.getModel().getBarCode());
                txtField03.setText((String) oTrans.getModel().getAlternateBarCode());
                txtField04.setText((String) oTrans.getModel().getBriefDescription());
                txtField05.setText((String) oTrans.getModel().getDescription());
                txtField06.setText((String) oTrans.getModel().Category().getDescription());
                txtField07.setText((String) oTrans.getModel().CategoryLevel2().getDescription());
                txtField08.setText((String) oTrans.getModel().CategoryLevel3().getDescription());
                txtField09.setText((String) oTrans.getModel().CategoryLevel4().getDescription());

                txtField10.setText((String) oTrans.getModel().Brand().getDescription());
                txtField11.setText((String) oTrans.getModel().Model().getDescription());
                txtField12.setText((String) oTrans.getModel().Color().getDescription());
                txtField13.setText((String) oTrans.getModel().Measure().getDescription());

                txtField14.setText(CommonUtils.NumberFormat(oTrans.getModel().getDiscountRateLevel1(), "#,##0.00"));
                txtField15.setText(CommonUtils.NumberFormat(oTrans.getModel().getDiscountRateLevel2(), "#,##0.00"));
                txtField16.setText(CommonUtils.NumberFormat(oTrans.getModel().getDiscountRateLevel3(), "#,##0.00"));
                txtField17.setText(CommonUtils.NumberFormat(oTrans.getModel().getDealerDiscountRate(), "#,##0.00"));

                txtField26.setText(String.valueOf(oTrans.getModel().getMinimumInventoryLevel()));
                txtField27.setText(String.valueOf(oTrans.getModel().getMaximumInventoryLevel()));
                txtField18.setText(CommonUtils.NumberFormat(oTrans.getModel().getCost(), "#,##0.00"));
                txtField19.setText(CommonUtils.NumberFormat(oTrans.getModel().getSellingPrice(), "#,##0.00"));

                txtField20.setText((String) oTrans.getModel().getSupersededId());
                txtField21.setText(String.valueOf(oTrans.getModel().getShelfLife()));

                chkField01.setSelected("1".equals(oTrans.getModel().isSerialized()));
                chkField02.setSelected("1".equals(oTrans.getModel().isComboInventory()));
                chkField03.setSelected("1".equals(oTrans.getModel().isWithPromo()));
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(InventoryParamController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
