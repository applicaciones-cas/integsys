package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.cas.cashflow.CheckTransfer;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.status.CheckTransferStatus;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Detail;
import ph.com.guanzongroup.cas.cashflow.model.Model_Check_Transfer_Master;
import ph.com.guanzongroup.cas.cashflow.services.CheckController;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckTransfer_PostingController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Check Transfer Posting";
    private String psIndustryID;
    private Control lastFocusedControl;
    private CheckTransfer poAppController;
    private ObservableList<Model_Check_Transfer_Detail> laTransactionDetail;
    private int pnSelectMaster, pnEditMode, pnTransactionDetail;

    private unloadForm poUnload = new unloadForm();
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apMaster, apDetail, apButton, apTransaction;

    @FXML
    private TextField tfSearchSource, tfSearchTransNo, tfTransactionNo,
            tfSourceBranch, tfDepartment, tfTotal, tfPayee,
            tfBank, tfCheckAmount, tfCheckTransNo,
            tfCheckNo, tfNote;

    @FXML
    private DatePicker dpSearchTransactionDate, dpTransactionDate, dpCheckDate, dpReceivedDate;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private CheckBox cbIsReceived;
    @FXML
    private Button btnSearch, btnBrowse, btnPost, btnRetrieve, btnClose, btnReceived;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView<Model_Check_Transfer_Detail> tblViewDetails;

    @FXML
    private TableColumn<Model_Check_Transfer_Detail, String> tblColDetailNo, tblColDetailReference, tblColDetailPayee, tblColDetailBank,
            tblColDetailDate, tblColDetailCheckNo, tblColDetailCheckAmount;

    @FXML
    private TableView<Model_Check_Transfer_Master> tblViewMaster;

    @FXML
    private TableColumn<Model_Check_Transfer_Master, String> tblColNo, tblColTransNo,
            tblColTransDate, tblColSourceBranch, tblColDepartment;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);
            poAppController = new CheckController(poApp, poLogWrapper).CheckTransfer();
            poAppController.setTransactionStatus(CheckTransferStatus.CONFIRMED);

            //initlalize and validate transaction objects from class controller
            if (!isJSONSuccess(poAppController.initTransaction(), psFormName)) {
                unloadForm appUnload = new unloadForm();
                appUnload.unloadForm(apMainAnchor, poApp, psFormName);
            }

            //background thread
            Platform.runLater(() -> {
                poAppController.setTransactionStatus("1");
                //initialize logged in category
                poAppController.setIndustryID(psIndustryID);
                System.err.println("Initialize value : Industry >" + psIndustryID);

            });
            initializeTableDetail();
            initControlEvents();
        } catch (SQLException | GuanzonException e) {
            Logger.getLogger(CheckTransfer_PostingController.class.getName()).log(Level.SEVERE, null, e);
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    @FXML
    void ontblMasterClicked(MouseEvent e) {
        pnSelectMaster = tblViewMaster.getSelectionModel().getSelectedIndex();
        if (pnSelectMaster < 0) {
            return;
        }

        if (e.getClickCount() == 2 && !e.isConsumed()) {
            try {
                e.consume();
                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                    return;
                }
                if (!isJSONSuccess(poAppController.searchTransactionPosting(tblColTransNo.getCellData(pnSelectMaster), true, true), psFormName)) {
//                    ShowMessageFX.Information("Failed to add detail", psFormName, null);
                    return;
                }
                getLoadedTransaction();
                initButtonDisplay(poAppController.getEditMode());
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {

                poLogWrapper.severe(psFormName + " :" + ex.getMessage());

            }

        }
        return;
    }

    @FXML
    void ontblDetailClicked(MouseEvent e) {
        try {
            pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1;
            if (pnTransactionDetail <= 0) {
                return;
            }

            loadSelectedTransactionDetail(pnTransactionDetail);
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            //get button id
            String btnID = ((Button) event.getSource()).getId();
            switch (btnID) {

                case "btnBrowse":
                    if (lastFocusedControl == null) {
                        if (!tfTransactionNo.getText().isEmpty()) {
                            if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                return;
                            }
                        }
                        if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchTransNo.getText(), true, true),
                                "Initialize Search Source No! ")) {
                            return;
                        }

                        getLoadedTransaction();
                        initButtonDisplay(poAppController.getEditMode());

                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchTransNo":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "tfSearchSource":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchSource.getText(), true, false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        case "dpSearchTransactionDate":
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransactionPosting(String.valueOf(dpSearchTransactionDate.getValue()), false, false),
                                    "Initialize Search Transaction! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                        default:
                            if (!tfTransactionNo.getText().isEmpty()) {
                                if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                    return;
                                }
                            }
                            if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchTransNo.getText(), true, true),
                                    "Initialize Search Source No! ")) {
                                return;
                            }

                            getLoadedTransaction();
                            initButtonDisplay(poAppController.getEditMode());
                            break;
                    }
                    break;

                case "btnPost":
                    if (tfTransactionNo.getText().isEmpty()) {
                        ShowMessageFX.Information("Please load transaction before proceeding..", psFormName, "");
                        return;
                    }
                    if (dpReceivedDate.getValue() == null) {
                        ShowMessageFX.Information("Please input date received before proceeding..", "Inventory Stock Issuance Posting", "");
                        return;
                    }
                    if (!isJSONSuccess(poAppController.PostTransaction(), "Initialize Post Transaction")) {
                        return;
                    }
                    getLoadedTransaction();
                    pnEditMode = poAppController.getEditMode();
                    break;
                case "btnReceived":
                    if (btnReceived.getText().toLowerCase().contains("unreceived")) {
                        unreceivedDetailAll();
                    } else {
                        receivedDetailAll();
                    }
                    reloadTableDetail();
                    loadSelectedTransactionDetail(pnTransactionDetail);
                    break;

                case "btnRetrieve":
                    if (lastFocusedControl == null) {

                        loadTransactionMasterList(tfSearchTransNo.getText().trim(), "a.sTransNox");
                        return;
                    }

                    switch (lastFocusedControl.getId()) {
                        case "tfSearchSource":
                            loadTransactionMasterList(tfSearchSource.getText().trim(), "LEFT(a.sTransNox,4)");
                            break;
                        case "tfSearchTransNo":
                            loadTransactionMasterList(tfSearchTransNo.getText().trim(), "a.sTransNox");
                            break;
                        case "dpSearchTransactionDate":
                            loadTransactionMasterList(String.valueOf(dpSearchTransactionDate.getValue()), "a.dTransact");
                            break;
                        default:
                            loadTransactionMasterList(tfSearchTransNo.getText().trim(), "a.sTransNox");
                            break;
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(apMainAnchor, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
            }

            initButtonDisplay(poAppController.getEditMode());

        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            e.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + e.getMessage());
        }
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextFieldID = loTextField.getId();
        String lsValue = loTextField.getText();
        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextFieldID) {
                case "taRemarks":
                    poAppController.getMaster().setRemarks(lsValue);
                    loadTransactionMaster();

                    break;

            }
        } else {
            loTextField.selectAll();
        }

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
        try {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case TAB:
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {
                            case "tfSearchTransNo":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchTransNo.getText(), true, true),
                                        "Initialize Search Source No! ")) {
                                    return;
                                }

                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "tfSearchSource":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransactionPosting(tfSearchSource.getText(), true, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;
                            case "dpSearchTransactionDate":
                                if (!tfTransactionNo.getText().isEmpty()) {
                                    if (ShowMessageFX.OkayCancel(null, "Search Transaction! by Trasaction", "Are you sure you want replace loaded Transaction?") == false) {
                                        return;
                                    }
                                }
                                if (!isJSONSuccess(poAppController.searchTransactionPosting(String.valueOf(dpSearchTransactionDate.getValue()), false, false),
                                        "Initialize Search Transaction! ")) {
                                    return;
                                }
                                getLoadedTransaction();
                                initButtonDisplay(poAppController.getEditMode());
                                break;

                        }
                        break;
                }
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            ex.printStackTrace();
            poLogWrapper.severe(psFormName + " :" + ex.getMessage());
        }
    }

    private void loadTransactionMasterList(String fsValue, String fsColumn) {
        StackPane overlay = getOverlayProgress(apTransaction);
        ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
        overlay.setVisible(true);
        pi.setVisible(true);

        Task<ObservableList<Model_Check_Transfer_Master>> loadCheckPayment = new Task<ObservableList<Model_Check_Transfer_Master>>() {
            @Override
            protected ObservableList<Model_Check_Transfer_Master> call() throws Exception {
                if (!isJSONSuccess(poAppController.loadTransactionListPosting(fsValue, fsColumn),
                        "Initialize : Load of Transaction List")) {
                    return null;
                }

                List<Model_Check_Transfer_Master> rawList = poAppController.getMasterList();
                System.out.print("The size of list is " + rawList.size());
                return FXCollections.observableArrayList(new ArrayList<>(rawList));
            }

            @Override
            protected void succeeded() {
                ObservableList<Model_Check_Transfer_Master> laMasterList = getValue();
                tblViewMaster.setItems(laMasterList);

                tblColNo.setCellValueFactory(loModel -> {
                    int index = tblViewMaster.getItems().indexOf(loModel.getValue()) + 1;
                    return new SimpleStringProperty(String.valueOf(index));
                });
                tblColTransNo.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionNo()));
                });
                tblColTransDate.setCellValueFactory(loModel -> {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().getTransactionDate()));
                });

                tblColSourceBranch.setCellValueFactory((loModel) -> {
                    try {
                        return new SimpleStringProperty(String.valueOf(loModel.getValue().Branch().getBranchName()));
                    } catch (SQLException | GuanzonException e) {
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });
                tblColDepartment.setCellValueFactory((loModel) -> {
                    try {
                        return new SimpleStringProperty(loModel.getValue().Department().getDescription() != null ? loModel.getValue().Department().getDescription() : "");
                    } catch (SQLException | GuanzonException e) {
                        poLogWrapper.severe(psFormName, e.getMessage());
                        return new SimpleStringProperty("");
                    }
                });

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
        Thread thread = new Thread(loadCheckPayment);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadTransactionMaster() {
        try {
            lblSource.setText(poAppController.getMaster().Industry().getDescription() == null ? "" : poAppController.getMaster().Industry().getDescription());
            lblStatus.setText(CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())) == null ? "STATUS"
                    : CheckTransferStatus.STATUS.get(Integer.parseInt(poAppController.getMaster().getTransactionStatus())));

            tfTransactionNo.setText(poAppController.getMaster().getTransactionNo());
            dpTransactionDate.setValue(ParseDate(poAppController.getMaster().getTransactionDate()));
            tfSourceBranch.setText(poAppController.getMaster().Branch().getBranchName());
            tfDepartment.setText(poAppController.getMaster().Department().getDescription());
            taRemarks.setText(String.valueOf(poAppController.getMaster().getRemarks()));
            tfTotal.setText(String.valueOf(poAppController.getMaster().getTransactionTotal()));
            dpReceivedDate.setValue(
                    poAppController.getMaster().getReceivedDate() != null ? ParseDate(poAppController.getMaster().getReceivedDate()) : LocalDate.now());

            dpReceivedDate.requestFocus();
        } catch (SQLException | GuanzonException e) {
            poLogWrapper.severe(psFormName, e.getMessage());
        }
    }

    private void loadSelectedTransactionDetail(int fnRow) throws SQLException, GuanzonException, CloneNotSupportedException {

        int tblIndex = fnRow - 1;
        tfCheckTransNo.setText(tblColDetailReference.getCellData(tblIndex));
        tfBank.setText(tblColDetailBank.getCellData(tblIndex));
        tfPayee.setText(tblColDetailPayee.getCellData(tblIndex));
        tfCheckNo.setText(tblColDetailCheckNo.getCellData(tblIndex));
        tfCheckAmount.setText(tblColDetailCheckAmount.getCellData(tblIndex));

        cbIsReceived.setSelected(poAppController.getDetail(fnRow).isReceived());

        if (!isAllReceived()) {
            btnReceived.setText("Received All");
        } else {
            btnReceived.setText("Unreceived All");
        }
        tfNote.setText(poAppController.getDetail(fnRow).getRemarks());
        dpCheckDate.setValue(ParseDate(poAppController.getDetail(fnRow).CheckPayment().getTransactionDate()));

    }

    private void receivedDetailAll() {
        for (int lnCtr = 1; lnCtr <= poAppController.getDetailCount(); lnCtr++) {
            if (poAppController.getDetail(lnCtr).isReceived()) {
                continue;
            }
            poAppController.getDetail(lnCtr).setReceived(true);
        }
        if (!isAllReceived()) {
            btnReceived.setText("Received All");
        } else {
            btnReceived.setText("Unreceived All");
        }

    }

    private void unreceivedDetailAll() {
        for (int lnCtr = 1; lnCtr <= poAppController.getDetailCount(); lnCtr++) {
            if (poAppController.getDetail(lnCtr).isReceived()) {
                continue;
            }
            poAppController.getDetail(lnCtr).setReceived(false);
        }

        if (!isAllReceived()) {
            btnReceived.setText("Received All");
        } else {
            btnReceived.setText("Unreceived All");
        }

    }

    private boolean isAllReceived() {
        for (int lnCtr = 1; lnCtr <= poAppController.getDetailCount(); lnCtr++) {
            if (!poAppController.getDetail(lnCtr).isReceived()) {
                return false;
            }

        }

        return true;
    }

    private void initControlEvents() {
        List<Control> laControls = getAllSupportedControls();

        for (Control loControl : laControls) {
            //add more if required
            if (loControl instanceof TextField) {
                TextField loControlField = (TextField) loControl;
                controllerFocusTracker(loControlField);
                loControlField.setOnKeyPressed(this::txtField_KeyPressed);
                loControlField.focusedProperty().addListener(txtField_Focus);
            } else if (loControl instanceof TableView) {
                TableView loControlField = (TableView) loControl;
                controllerFocusTracker(loControlField);
            } else if (loControl instanceof ComboBox) {
                ComboBox loControlField = (ComboBox) loControl;
                controllerFocusTracker(loControlField);

            } else if (loControl instanceof DatePicker) {
                DatePicker loControlField = (DatePicker) loControl;
                controllerFocusTracker(loControlField);
                loControlField.focusedProperty().addListener(dPicker_Focus);
                loControlField.getEditor().setOnKeyPressed(this::dPicker_KeyPressed);
            }
        }

        cbIsReceived.setOnAction(e
                -> {
            poAppController.getDetail(pnTransactionDetail).setReceived(cbIsReceived.isSelected());
        }
        );

        clearAllInputs();
    }

    private void controllerFocusTracker(Control control) {
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lastFocusedControl = control;
            }
        });
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
        pnEditMode = poAppController.getEditMode();
        initButtonDisplay(poAppController.getEditMode());

    }

    private void initButtonDisplay(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);

        // Always show these buttons
        initButtonControls(true, "btnBrowse,btnRetrieve", "btnHistory", "btnClose");

        apMaster.setDisable(!lbShow);
        apDetail.setDisable(!lbShow);
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
                e.printStackTrace();
                poLogWrapper.severe(psFormName + " :" + e.getMessage());
            }
        }

    }

    private void initializeTableDetail() {
        if (laTransactionDetail == null) {
            laTransactionDetail = FXCollections.observableArrayList();

            tblViewDetails.setItems(laTransactionDetail);

            tblColDetailCheckAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 5 0 0;");

            tblColDetailNo.setCellValueFactory((loModel) -> {
                int index = tblViewDetails.getItems().indexOf(loModel.getValue()) + 1;
                return new SimpleStringProperty(String.valueOf(index));
            });

            tblColDetailReference.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getTransactionNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailPayee.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Payee().getPayeeName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailBank.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().Banks().getBankName());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
            tblColDetailDate.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getCheckDate()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckNo.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(loModel.getValue().CheckPayment().getCheckNo());
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });

            tblColDetailCheckAmount.setCellValueFactory((loModel) -> {
                try {
                    return new SimpleStringProperty(String.valueOf(loModel.getValue().CheckPayment().getAmount()));
                } catch (SQLException | GuanzonException e) {
                    poLogWrapper.severe(psFormName, e.getMessage());
                    return new SimpleStringProperty("");
                }
            });
        }
    }

    private void reloadTableDetail() {
        List<Model_Check_Transfer_Detail> rawDetail = poAppController.getDetailList();
        laTransactionDetail.setAll(rawDetail);

        // Restore or select last row
        int indexToSelect = (pnTransactionDetail >= 1 && pnTransactionDetail < laTransactionDetail.size())
                ? pnTransactionDetail - 1
                : laTransactionDetail.size() - 1;

        tblViewDetails.getSelectionModel().select(indexToSelect);

        pnTransactionDetail = tblViewDetails.getSelectionModel().getSelectedIndex() + 1; // Not focusedIndex
        tblViewDetails.refresh();
    }

    private void getLoadedTransaction() throws SQLException, GuanzonException, CloneNotSupportedException {
//        clearAllInputs();
        loadTransactionMaster();
        reloadTableDetail();
        loadSelectedTransactionDetail(pnTransactionDetail);
        dpReceivedDate.requestFocus();
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            poLogWrapper.severe(psFormName + " :" + message);
            Platform.runLater(() -> {
                ShowMessageFX.Warning(null, psFormName, fsModule + ": " + message);
            });
            return false;
        }
        String message = (String) loJSON.get("message");

        poLogWrapper.severe(psFormName + " :" + message);
        Platform.runLater(() -> {
            if (message != null) {
                ShowMessageFX.Information(null, psFormName, fsModule + ": " + message);
            }
        });
        poLogWrapper.info(psFormName + " : Success on " + fsModule);
        return true;

    }

    private void dPicker_KeyPressed(KeyEvent event) {

        TextField loTxtField = (TextField) event.getSource();
        String loDatePickerID = ((DatePicker) loTxtField.getParent()).getId(); // cautious cast
        String loValue = loTxtField.getText();
        String lsValue = "";
        if (loValue != null && !loValue.isEmpty()) {
            Date toDateValue = SQLUtil.toDate(loValue, "dd/MM/yyyy");
            lsValue = SQLUtil.dateFormat(toDateValue, SQLUtil.FORMAT_SHORT_DATE);

        }

        if (event.getCode() != null) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    event.consume();
                    switch (loDatePickerID) {

                    }
            }
        }
        event.consume();

    }
    final ChangeListener<? super Boolean> dPicker_Focus = (o, ov, nv) -> {
        DatePicker loDatePicker = (DatePicker) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsDatePickerID = loDatePicker.getId();
        LocalDate loValue = loDatePicker.getValue();

        if (loValue == null) {
            return;
        }

        LocalDateTime ldDateTimeValue = loValue.atTime(LocalTime.now());
        Date ldDateValue = Date.from(loValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (!nv) {
            /*Lost Focus*/
            switch (lsDatePickerID) {
                case "dpReceivedDate":
                    poAppController.getMaster().setReceivedDate((ldDateTimeValue));
                    return;

            }
        }
    };

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
