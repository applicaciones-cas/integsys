package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.InventoryCountType;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;

public class InventoryCountTypeController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private final String pxeModuleName = "Inventory Count Type";
    private int pnEditMode;
    private InventoryCountType oParameters;
    private boolean pbLoaded = false;

    @FXML
    private AnchorPane AnchorMain, AnchorInputs;

    @FXML
    private Button btnBrowse, btnSearch, btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnClose;

    @FXML
    private FontAwesomeIconView faActivate;

    @FXML
    private TextField txtField01, txtField02, txtField03, txtField04, txtSeeks01;

    @FXML
    private ComboBox<String> cmbField01, cmbField02;

    @FXML
    private CheckBox cbField01, cbField02;

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
        try {
            initializeObject();
            pnEditMode = oParameters.getEditMode();
            initButton(oParameters.getEditMode());
            InitTextFields();
            ClickButton();

            if (oParameters.getEditMode() == EditMode.ADDNEW) {
                loadRecord();
                initButton(oParameters.getEditMode());
            }
            pbLoaded = true;
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeObject() throws GuanzonException, SQLException {
        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
        oParameters = new ParamControllers(oApp, logwrapr).InventoryCountType();
        oParameters.setRecordStatus("0123");
        oParameters.getModel().setIndustryCode(oApp.getIndustry());

    }

    private void ClickButton() {
        btnBrowse.setOnAction(this::handleButtonAction);
        btnSearch.setOnAction(this::handleButtonAction);
        btnNew.setOnAction(this::handleButtonAction);
        btnSave.setOnAction(this::handleButtonAction);
        btnUpdate.setOnAction(this::handleButtonAction);
        btnCancel.setOnAction(this::handleButtonAction);
        btnActivate.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
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
                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                        }
                        break;
                    case "btnNew":
                        clearAllFields();
                        txtField02.requestFocus();
                        JSONObject poJSON = oParameters.newRecord();
                        oParameters.getModel().setIndustryCode(oApp.getIndustry());
                        pnEditMode = oParameters.getEditMode();
                        if ("success".equals((String) poJSON.get("result"))) {
                            loadRecord();
                            initButton(oParameters.getEditMode());
                        } else {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);

                        }
                        break;
                    case "btnBrowse":
                        String lsValue = (txtSeeks01.getText() == null) ? "" : txtSeeks01.getText();
                        poJSON = oParameters.searchRecord(lsValue, false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        loadRecord();
                        initButton(oParameters.getEditMode());
                        break;
                        
                    case "btnSearch":
                        poJSON = oParameters.searchRecord("", false);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            txtSeeks01.clear();
                            break;
                        }
                        loadRecord();
                        initButton(oParameters.getEditMode());
                        break;
                    case "btnUpdate":
                        poJSON = oParameters.updateRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
                            break;
                        }
                        pnEditMode = oParameters.getEditMode();
                        initButton(oParameters.getEditMode());
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
                            clearAllFields();
                            initializeObject();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(oParameters.getEditMode());
                        }
                        break;
                    case "btnSave":
                        oParameters.getModel().setModifyingId(oApp.getUserID());
                        oParameters.getModel().setModifiedDate(oApp.getServerDate());
                        JSONObject saveResult = oParameters.saveRecord();
                        if ("success".equals((String) saveResult.get("result"))) {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                            clearAllFields();
                            Platform.runLater(() -> btnNew.fire());
                        } else {
                            ShowMessageFX.Information((String) saveResult.get("message"), "Computerized Acounting System", pxeModuleName);
                        }
                        break;
                    case "btnActivate":
                        String Status = oParameters.getModel().getRecordStatus();
                        String id = oParameters.getModel().getInventoryCountID();
                        JSONObject poJsON;

                        switch (Status) {
                            case "0":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                                    poJsON = oParameters.ActivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                            case "1":
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this Parameter?") == true) {
                                    poJsON = oParameters.DeactivateRecord();
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    poJsON = oParameters.openRecord(id);
                                    if ("error".equals(poJsON.get("result"))) {
                                        ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                        break;
                                    }
                                    clearAllFields();
                                    loadRecord();
                                    ShowMessageFX.Information((String) poJsON.get("message"), "Computerized Accounting System", pxeModuleName);
                                }
                                break;
                        }

                }
            } catch (SQLException ex) {
                Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (GuanzonException ex) {
                Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearAllFields() {
        txtField01.clear();
        txtField02.clear();
        txtField03.clear();
        txtField04.clear();
        txtSeeks01.clear();
        cbField01.setSelected(false);
        cbField02.setSelected(false);

        //by Period
//        M -> Monthly
//        S -> Semi‑Annual;
//        A -> Annual
//        X -> On Demand
        //Inclusion
//        AI -> All Items;
//        BB -> Bins;
//        RX -> Random
//        C ?-> By Classification			
    }

    public enum Period {
        M("Monthly"),
        S("Semi-Annual"),
        A("Annual"),
        X("On Demand");

        private final String displayName;

        Period(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Period fromDisplay(String displayName) {
            for (Period p : values()) {
                if (p.displayName.equalsIgnoreCase(displayName)) {
                    return p;
                }
            }
            return M; // default
        }

        public static Period fromCode(String code) {
            if (code == null || code.isEmpty()) {
                return M;
            }
            try {
                return valueOf(code.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return M;
            }
        }
    }

    public enum Inclusion {
        AI("All Items"),
        BB("Bins"),
        RX("Random"),
        C("By Classification");

        private final String displayName;

        Inclusion(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Inclusion fromDisplay(String displayName) {
            for (Inclusion i : values()) {
                if (i.displayName.equalsIgnoreCase(displayName)) {
                    return i;
                }
            }
            return AI; // default
        }

        public static Inclusion fromCode(String code) {
            if (code == null || code.isEmpty()) {
                return AI;
            }
            try {
                return valueOf(code.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return AI;
            }
        }

    }

    private void initButton(int fnEditMode) {
        boolean lbEditing = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        String lsTransNo = txtField01.getText();
        boolean lbHasTransaction = lsTransNo != null && !lsTransNo.isEmpty();
        boolean lbIsApproved = lbHasTransaction
                && "1".equals(oParameters.getModel().getInventoryCountID());

        // Always visible
        initButtonControls(true, "btnClose");

        // Editing mode buttons
        initButtonControls(lbEditing, "btnSearch", "btnSave", "btnCancel");
        initButtonControls(!lbEditing, "btnBrowse", "btnNew");

        // Transaction-dependent buttons (only when not editing)
        initButtonControls(!lbEditing && lbHasTransaction, "btnUpdate", "btnActivate");

        // Disable panes during editing
        AnchorInputs.setDisable(!lbEditing);

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
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), pxeModuleName, null);

            }
        }
    }

    private void InitTextFields() {
        txtField01.focusedProperty().addListener(txtField_Focus);
        txtField02.focusedProperty().addListener(txtField_Focus);
        txtField03.focusedProperty().addListener(txtField_Focus);
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtSeeks01.setOnKeyPressed(this::txtSeeks_KeyPressed);

        cmbField01.setItems(FXCollections.observableArrayList(
                Arrays.stream(Period.values())
                        .map(Period::getDisplayName)
                        .collect(Collectors.toList())
        ));

        cmbField02.setItems(FXCollections.observableArrayList(
                Arrays.stream(Inclusion.values())
                        .map(Inclusion::getDisplayName)
                        .collect(Collectors.toList())
        ));
        cmbField01.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        Period selected = Period.fromDisplay(newVal);
//                        System.out.println("Period code: " + selected.name()); // "M", "S", "A", "X"
                        oParameters.getModel().setPeriod(selected.name());
                    }
                }
        );

        cmbField02.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        Inclusion selected = Inclusion.fromDisplay(newVal);
//                        System.out.println("Inclusion code: " + selected.name()); // "AI", "BB", "RX", "C"
                        oParameters.getModel().setIncluded(selected.name());
                    }
                }
        );

    }

    private void txtSeeks_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            JSONObject poJson;
            poJson = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lnIndex) {
                        case 01:
                            poJson = oParameters.searchRecord(lsValue, false);
                            if ("error".equals((String) poJson.get("result"))) {
                                ShowMessageFX.Information((String) poJson.get("message"), "Computerized Acounting System", pxeModuleName);
                                txtSeeks01.clear();
                                break;
                            }
                            txtSeeks01.setText((String) oParameters.getModel().getDescription());
                            pnEditMode = EditMode.READY;
                            loadRecord();
                            initButton(oParameters.getEditMode());
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
            Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(InventoryCountTypeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {

        TextField txtField = (TextField) event.getSource();
        int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(8, 10));
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        JSONObject poJson;
        poJson = new JSONObject();
        switch (event.getCode()) {
            case F3:

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
    }
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

        if (!nv) {
            try {
                switch (lnIndex) {
                    case 1:
                        oParameters.getModel().setInventoryCountID(lsValue);
                        break;
                    case 2:
                        oParameters.getModel().setDescription(lsValue);
                        break;
                    case 4:
                        int lnValue = 0;
                        try {
                            lnValue = Integer.parseInt(lsValue);
                        } catch (NumberFormatException ex) {
                            txtField.requestFocus();
                            txtField.setText("0");
                        }
                        oParameters.getModel().setQuantity(lnValue);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing input: " + e.getMessage());
            }
        } else {
            txtField.selectAll();
        }
    };

    private void loadRecord() {
        try {
            txtField01.setText(oParameters.getModel().getInventoryCountID());
            txtField02.setText(oParameters.getModel().getDescription());
            txtField03.setText(oParameters.getModel().Department().getDescription());
            txtField04.setText(String.valueOf(oParameters.getModel().getQuantity()));

            cmbField01.getSelectionModel().select(
                    Period.fromCode(oParameters.getModel().getPeriod()).getDisplayName()
            );
            cmbField02.getSelectionModel().select(
                    Inclusion.fromCode(oParameters.getModel().getIncluded()).getDisplayName()
            );

            switch (oParameters.getModel().getRecordStatus()) {
                case "1":
                    btnActivate.setText("Deactivate");
                    faActivate.setGlyphName("CLOSE");
                    cbField01.setSelected(true);
                    break;
                case "0":
                    btnActivate.setText("Activate");
                    faActivate.setGlyphName("CHECK");
                    cbField01.setSelected(false);
                    break;
            }
            switch (oParameters.getModel().getAllowBalanceForward()) {
                case "1":
                    cbField02.setSelected(true);
                    break;
                case "0":
                    cbField02.setSelected(false);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            ShowMessageFX.Information(ex.getMessage(), "Computerized Acounting System", pxeModuleName);

        }
    }

    @FXML
    void cbField01_Clicked(MouseEvent event) {
        if (cbField01.isSelected()) {
            oParameters.getModel().setRecordStatus("1");
        } else {
            oParameters.getModel().setRecordStatus("0");
        }
    }

}
