package ph.com.guanzongroup.integsys.views;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.inv.InvMaster;
import org.guanzon.cas.inv.model.Model_Inv_Serial;
import org.json.simple.JSONObject;

public class InventorySerialController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Inventory Serial";
    private InvMaster poAppController;
    private String psIndustryID;
    private ObservableList<Model_Inv_Serial> laRecordSerial;

    ObservableList<String> unitType = FXCollections.observableArrayList(
            "LDU",
            "Regular",
            "Free",
            "Live",
            "Service",
            "RDU",
            "Others"
    );

    @FXML
    private AnchorPane apMainAnchor;

    @FXML
    private Button btnRetrieve, btnOkay, btnClose;
    @FXML
    private TextField tfBarcode, tfDescription, tfModel, tfBrand, tfColor, tfMeasurement;
    @FXML
    private TableView<Model_Inv_Serial> tblSerialLedger;

    @FXML
    private TableColumn<Model_Inv_Serial, String> index01, index02, index03, index04,
            index05, index06, index07;

    @FXML
    private ComboBox cmbUnitType;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
//        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
//        psCategoryID = fsValue;
    }

    public void setInventoryMaster(InvMaster foValue) {
        poAppController = foValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        poLogWrapper = new LogWrapper(psFormName, psFormName);
        initializeTableDetail();
        initControlEvents();
        getLoadedTransaction();
        
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {
                case "btnOkay":  //Close
                    CommonUtils.closeStage(btnOkay);
                    break;

                case "btnClose": //OK
                    CommonUtils.closeStage(btnClose);
                    break;

                case "btnRetrieve": //OK 
                    if (!isJSONSuccess(poAppController.loadSerialList(String.valueOf(cmbUnitType.getSelectionModel().getSelectedIndex())),
                            "Initialize : Load of Serial List")) {
                        return;
                    }
                    reloadTableDetail();
                    break;
                default:
                    ShowMessageFX.Warning(null, psFormName, "Button with name " + btnID + " not registered.");
                    return;

            }
        } catch (CloneNotSupportedException | NumberFormatException | SQLException | GuanzonException e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
//        try {
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }
//        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
//            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
//        }
    };

    private void txtField_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
//        try {
        if (null != event.getCode()) {
            switch (event.getCode()) {

            }
        }
//        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
//            ex.printStackTrace();
//            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
//        }
    }

    private final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {

            }
        } else {
            loTextField.selectAll();
        }

    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextField loTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (loTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = loTxtField.getText();
        }
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case UP:
                        CommonUtils.SetPreviousFocus((TextField) event.getSource());
                        return;
                    case DOWN:
                        CommonUtils.SetNextFocus(loTxtField);
                        return;

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadRecordList() {
        StackPane overlay = getOverlayProgress(apMainAnchor);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Inv_Serial>> loadInvSerial = new Task<ObservableList<Model_Inv_Serial>>() {
            @Override
            protected ObservableList<Model_Inv_Serial> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadSerialList(String.valueOf(cmbUnitType.getSelectionModel().getSelectedIndex())),
                        "Initialize : Load of Serial List")) {
                    return null;
                }

                List<Model_Inv_Serial> rawList = poAppController.getSerialList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                reloadTableDetail();

                overlay.setVisible(false);
                pi.setVisible(false);
            }

            @Override
            protected void failed() {
                overlay.setVisible(false);
                pi.setVisible(false);
                Throwable ex = getException();
                ex.printStackTrace();
                poLogWrapper.severe(psFormName + " : " + ex.getMessage());
            }

            @Override
            protected void cancelled() {
                overlay.setVisible(false);
                pi.setVisible(false);
            }
        };
        Thread thread = new Thread(loadInvSerial);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadRecord() {
        try {
            tfBarcode.setText(poAppController.getModel().Inventory().getBarCode());
            tfDescription.setText(poAppController.getModel().Inventory().getBarCode());
            tfBrand.setText(poAppController.getModel().Inventory().Brand().getDescription());
            tfModel.setText(poAppController.getModel().Inventory().Model().getDescription());
            tfColor.setText(poAppController.getModel().Inventory().Color().getDescription());
            tfMeasurement.setText(poAppController.getModel().Inventory().Measure().getDescription());

        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TextArea) {
                TextArea loControlField = (TextArea) loControl;
                loControlField.setOnKeyPressed(this::txtArea_KeyPressed);
                loControlField.focusedProperty().addListener(txtArea_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
            }
        }

        clearAllInputs();
    }

    private void clearAllInputs() {

        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            if (loControl instanceof TextField) {
                ((TextField) loControl).clear();
            } else if (loControl instanceof TextArea) {
                ((TextArea) loControl).clear();
            } else if (loControl != null && loControl instanceof TableView) {
                TableView<?> table = (TableView<?>) loControl;
                if (table.getItems() != null) {
                    table.getItems().clear();
                }

            } else if (loControl instanceof DatePicker) {
                ((DatePicker) loControl).setValue(null);
            } else if (loControl instanceof ComboBox) {
                ((ComboBox) loControl).setItems(null);
            }
        }

        cmbUnitType.setItems(unitType);
    }

    private void initializeTableDetail() {
        if (laRecordSerial == null) {
            laRecordSerial = FXCollections.observableArrayList();

            tblSerialLedger.setItems(laRecordSerial);

            index06.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            index07.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");
            index01.setCellValueFactory((loModel) -> {
                int index = tblSerialLedger.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            index02.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getSerialId()));
            });

            index03.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getSerial01()));
            });

            index04.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(String.valueOf(loModel.getValue().getSerial02()));
            });

            index05.setCellValueFactory((loModel) -> {
                String lsValue = "";
                switch (loModel.getValue().getLocation()) {
                    case "0":
                        lsValue = "WAREHOUSE";
                        break;
                    case "1":
                        lsValue = "BRANCH";
                        break;
                    case "2":
                        lsValue = "SUPPLIER";
                        break;
                    case "3":
                        lsValue = "CUSTOMER";
                        break;
                    case "4":
                        lsValue = "ON-TRANSIT";
                        break;
                    case "5":
                        lsValue = "SERVICE";
                        break;
                    default:
                        lsValue = "UNKNOWN";
                        break;

                }
                return new SimpleStringProperty(lsValue);
            });

            index06.setCellValueFactory((loModel) -> {
                return new SimpleStringProperty(loModel.getValue().isSold() ? "YES" : "NO");
            });

            index07.setCellValueFactory((loModel) -> {
                String lsValue = unitType.get(Integer.parseInt(loModel.getValue().getUnitType()));
                return new SimpleStringProperty(lsValue);
            });

        }
    }

    private void reloadTableDetail() {
        List<Model_Inv_Serial> rawDetail = poAppController.getSerialList();
        laRecordSerial.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = laRecordSerial.size() - 1;

        tblSerialLedger.getSelectionModel().select(indexToSelect);
        tblSerialLedger.refresh();
    }

    private void getLoadedTransaction() {
        clearAllInputs();
        loadRecord();
        reloadTableDetail();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        String message = (String) loJSON.get("message");

        System.out.println("isJSONSuccess called. Thread: " + Thread.currentThread().getName());

        if ("error".equalsIgnoreCase(result)) {
            poLogWrapper.severe(psFormName + " : " + message);
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message));
                }
            }
            return false;
        }

        if ("success".equalsIgnoreCase(result)) {
            if (message != null && !message.trim().isEmpty()) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Information(null, psFormName, fsModule + ": " + message));
                }
            }
            poLogWrapper.info(psFormName + " : Success on " + fsModule);
            return true;
        }

        // Unknown or null result
        poLogWrapper.warning(psFormName + " : Unrecognized result: " + result);
        return false;
    }

    private LocalDate ParseDate(Date date) {
        if (date == null) {
            return null;
        }
        Date loDate = new java.util.Date(date.getTime());
        return loDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        ProgressIndicator localIndicator = null;
        StackPane localOverlay = null;

        // Check if overlay already exists
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        localIndicator = (ProgressIndicator) child;
                        localOverlay = stack;
                        break;
                    }
                }
            }
        }

        if (localIndicator == null) {
            localIndicator = new ProgressIndicator();
            localIndicator.setMaxSize(50, 50);
            localIndicator.setVisible(false);
            localIndicator.setStyle("-fx-progress-color: orange;");
        }

        if (localOverlay == null) {
            localOverlay = new StackPane();
            localOverlay.setPickOnBounds(false); // Let clicks through
            localOverlay.getChildren().add(localIndicator);

            AnchorPane.setTopAnchor(localOverlay, 0.0);
            AnchorPane.setBottomAnchor(localOverlay, 0.0);
            AnchorPane.setLeftAnchor(localOverlay, 0.0);
            AnchorPane.setRightAnchor(localOverlay, 0.0);

            foAnchorPane.getChildren().add(localOverlay);
        }

        return localOverlay;
    }

    private List<Control> getAllSupportedControls() {
        List<Control> controls = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (value instanceof TextField
                        || value instanceof TextArea
                        || value instanceof Button
                        || value instanceof TableView
                        || value instanceof DatePicker
                        || value instanceof ComboBox) {
                    controls.add((Control) value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }
        return controls;
    }
}
