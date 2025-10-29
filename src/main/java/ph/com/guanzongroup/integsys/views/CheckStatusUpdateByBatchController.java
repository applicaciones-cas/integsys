package ph.com.guanzongroup.integsys.views;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.integsys.model.ModelCheckPrinting;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckStatusUpdateByBatchController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Clear Status";
    private CheckStatusUpdate poCheckStatusUpdateController;
    private DisbursementVoucher poDisbursementController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSearchBankID = "";
    private String psSearchBankAccountID = "";
    private int pnRow = -1;
    private double xOffset = 0;
    private double yOffset = 0;

    private unloadForm poUnload = new unloadForm();

    private ObservableList<ModelCheckPrinting> main_data = FXCollections.observableArrayList();
    private FilteredList<ModelCheckPrinting> filteredMain_Data;

    ArrayList<SelectedITems> getSelectedItems = new ArrayList<>();
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();
    List<String> listOfDVToAssign = new ArrayList<>();

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    @FXML
    private AnchorPane AnchorMain;
    @FXML
    private AnchorPane apBrowse;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount;
    @FXML
    private AnchorPane apButton;
    @FXML
    private Button btnClear, btnRetrieve, btnClose;
    @FXML
    private TableView<ModelCheckPrinting> tblVwMain;
    @FXML
    private TableColumn<ModelCheckPrinting, String> tblRowNo, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblCheckAmount, tblCheckStatus;
    @FXML
    private TableColumn<ModelCheckPrinting, CheckBox> tblCheckBox;
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
            poCheckStatusUpdateController = new CashflowControllers(oApp, null).CheckStatusUpdate();
            poJSON = new JSONObject();
            poJSON = poCheckStatusUpdateController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        poCheckStatusUpdateController.Master().setIndustryID(psIndustryId);
        poCheckStatusUpdateController.Master().setCompanyID(psCompanyId);
        initButtonsClickActions();
        initTextFields();
        initTableMain();
        initTableOnClick();
        initTextFieldsProperty();
        initButtons();
        if (main_data.isEmpty()) {
            Label placeholderLabel = new Label("NO RECORD TO LOAD");
            tblVwMain.setPlaceholder(placeholderLabel);
            pagination.setManaged(false);
            pagination.setVisible(false);
            listOfDVToAssign.clear();
        }
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poCheckStatusUpdateController.Master().Company().getCompanyName() + " - " + poCheckStatusUpdateController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnClear, btnRetrieve, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private JSONObject validateSelectedItem() {
        poJSON = new JSONObject();
        ObservableList<ModelCheckPrinting> selectedItems = FXCollections.observableArrayList();

        for (ModelCheckPrinting item : tblVwMain.getItems()) {
            if (item.getSelect().isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            poJSON.put("message", "No items selected to assign.");
            poJSON.put("result", "error");
            return poJSON;
        }

        int successCount = 0;
        String firstBank = null;
        boolean allSameBank = true;
        String firstStatus = null;

        for (ModelCheckPrinting item : selectedItems) {
            String lsDVNO = item.getIndex03();
            String banks = item.getIndex07();
            String checkStatus = item.getIndex09();
            if (firstBank == null) {
                firstBank = banks; // store the first encountered bank
                firstStatus = checkStatus;
            } else if (!firstBank.equals(banks) && !firstStatus.equals(checkStatus)) {
                allSameBank = false;
                break; // no need to continue checking
            }

            listOfDVToAssign.add(lsDVNO);
            successCount++;
        }
        if (!allSameBank) {
            poJSON.put("message", "Selected items must belong to the same bank and check status.");
            poJSON.put("result", "error");
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }

    private void loadTableMainAndClearSelectedItems() {
        chckSelectAll.setSelected(false);
        getSelectedItems.clear();
        listOfDVToAssign.clear();
        loadTableMain();
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();

            switch (lsButton) {
                case "btnClear":
                    poJSON = validateSelectedItem();
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        break;
                    }
                    if (!listOfDVToAssign.isEmpty()) {
                        loadAssignWindow(listOfDVToAssign);
                        chckSelectAll.setSelected(false);
                        getSelectedItems.clear();
                        listOfDVToAssign.clear();
                    }
                    break;
                case "btnRetrieve":
                    loadTableMainAndClearSelectedItems();
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initTextFields() {
        //Initialise  TextField KeyPressed
        List<TextField> loTxtFieldKeyPressed = Arrays.asList(tfSearchBankName, tfSearchBankAccount);
        loTxtFieldKeyPressed.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case F3:
                        switch (lsID) {
                            case "tfSearchBankName":
                                poCheckStatusUpdateController.setCheckpayment();
                                poJSON = poCheckStatusUpdateController.SearchBanks(lsValue, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankName.setText(poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Banks().getBankName() : "");
                                psSearchBankID = poCheckStatusUpdateController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poCheckStatusUpdateController.setCheckpayment();
                                poJSON = poCheckStatusUpdateController.SearhBankAccount(lsValue, psSearchBankID, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankAccount.setText(poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poCheckStatusUpdateController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
                                psSearchBankAccountID = poCheckStatusUpdateController.CheckPayments().getModel().getBankAcountID();
                                break;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        loadTableMain();
                        event.consume();
                    default:
                        break;
                }

            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableMain() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblVwMain.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;");

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        main_data.clear();
                        plOrderNoFinal.clear();
                        poJSON = poCheckStatusUpdateController.getDisbursement(psSearchBankID, psSearchBankAccountID);
                        if ("success".equals(poJSON.get("result"))) {
                            if (poCheckStatusUpdateController.getDisbursementMasterCount() > 0) {
                                for (int lnCntr = 0; lnCntr < poCheckStatusUpdateController.getDisbursementMasterCount(); lnCntr++) {
                                    String lsCheckStatus;
                                    switch (poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().getTransactionStatus()) {
                                        case CheckStatus.OPEN:
                                            lsCheckStatus = "OPEN";
                                            break;
                                        case CheckStatus.STOP_PAYMENT:
                                            lsCheckStatus = "HOLD";
                                            break;
                                        case CheckStatus.POSTED:
                                            lsCheckStatus = "CLEARED";
                                            break;
                                        default:
                                            lsCheckStatus = "UNKNOWN";
                                            break;
                                    }
                                    main_data.add(new ModelCheckPrinting(
                                            String.valueOf(lnCntr + 1),
                                            "",
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poCheckStatusUpdateController.poDisbursementMaster(lnCntr).getTransactionDate()),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                            poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().getCheckNo(),
                                            CustomCommonUtil.formatDateToShortString(poCheckStatusUpdateController.poDisbursementMaster(lnCntr).getTransactionDate()),
                                            lsCheckStatus,
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckStatusUpdateController.poDisbursementMaster(lnCntr).CheckPayments().getAmount(), true)
                                    ));
                                }
                            }
                        } else {
                            main_data.clear();
                        }
                        showRetainedHighlight(true);
                        if (main_data.isEmpty()) {
                            tblVwMain.setPlaceholder(placeholderLabel);
                            ShowMessageFX.Warning(null, pxeModuleName, "No records found");
                            chckSelectAll.setSelected(false);
                            listOfDVToAssign.clear();
                        }
                        JFXUtil.loadTab(pagination, main_data.size(), ROWS_PER_PAGE, tblVwMain, filteredMain_Data);
                        initButtons();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                return null;
            }

            @Override

            protected void succeeded() {
                btnRetrieve.setDisable(false);
                placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
                if (main_data == null || main_data.isEmpty()) {
                    tblVwMain.setPlaceholder(placeholderLabel);
                    pagination.setManaged(false);
                    pagination.setVisible(false);
                } else {
                    pagination.setPageCount(0);
                    pagination.setVisible(true);
                    pagination.setManaged(true);
                    progressIndicator.setVisible(false);
                    progressIndicator.setManaged(false);
                    tblVwMain.toFront();
                }
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblVwMain.setPlaceholder(placeholderLabel);
                    pagination.setManaged(false);
                    pagination.setVisible(false);
                }
                btnRetrieve.setDisable(false);
                progressIndicator.setVisible(false);
                progressIndicator.setManaged(false);
                tblVwMain.toFront();
            }
        };
        new Thread(task).start(); // Run task in background
    }

    private void initTableMain() {
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblCheckStatus);
        JFXUtil.setColumnRight(tblCheckAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwMain);
        tblCheckBox.setCellValueFactory(new PropertyValueFactory<>("select"));
        tblCheckBox.setCellFactory(col -> new TableCell<ModelCheckPrinting, CheckBox>() {
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item); // show the actual checkbox
                }
            }
        });
        tblVwMain.getItems().forEach(item -> {
            CheckBox selectCheckBox = item.getSelect();
            selectCheckBox.setOnAction(event -> {
                if (tblVwMain.getItems().stream().allMatch(tableItem -> tableItem.getSelect().isSelected())) {
                    chckSelectAll.setSelected(true);
                } else {
                    chckSelectAll.setSelected(false);
                }
            });
        });
        chckSelectAll.setOnAction(event -> {
            boolean newValue = chckSelectAll.isSelected();
            tblVwMain.getItems().forEach(item -> item.getSelect().setSelected(newValue));
        });

        filteredMain_Data = new FilteredList<>(main_data, b -> true);
        tblVwMain.setItems(filteredMain_Data);
    }

    private void showRetainedHighlight(boolean isRetained) {
        if (isRetained) {
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"0".equals(pair.getValue())) {

                    plOrderNoFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        JFXUtil.disableAllHighlight(tblVwMain, highlightedRowsMain);
        plOrderNoPartial.clear();
        for (Pair<String, String> pair : plOrderNoFinal) {
            if (!"0".equals(pair.getValue())) {
                JFXUtil.highlightByKey(tblVwMain, pair.getKey(), "#A7C7E7", highlightedRowsMain);
            }
        }
    }

    private void initTableOnClick() {
        tblVwMain.setOnMouseClicked(event -> {
            if (tblVwMain.getSelectionModel().getSelectedIndex() >= 0 && event.getClickCount() == 2) {
                try {
                    ModelCheckPrinting selected = (ModelCheckPrinting) tblVwMain.getSelectionModel().getSelectedItem();
                    if (selected.getIndex03().isEmpty() && selected.getIndex03() == null) {
                        ShowMessageFX.Warning("Invalid to view no transaction no.", pxeModuleName, null);
                        return;
                    }
                    loadDVWindow(selected.getIndex03());
                } catch (SQLException ex) {
                    Logger.getLogger(CheckStatusUpdateByBatchController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void loadAssignWindow(List<String> fsTransactionNos) throws SQLException, GuanzonException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckClearingAssign.fxml"));

            CheckClearingAssignController loControl = new CheckClearingAssignController();
            loControl.setGRider(oApp);
            loControl.setCheckStatusUpdate(poCheckStatusUpdateController);
            loControl.setTransaction(fsTransactionNos);  // Pass the list here
            fxmlLoader.setController(loControl);

            Parent parent = fxmlLoader.load();
            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();

            loadTableMain();
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    private void loadDVWindow(String fsTransactionNo) throws SQLException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/DisbursementVoucher_View.fxml"));
            DisbursementVoucher_ViewController loControl = new DisbursementVoucher_ViewController();
            loControl.setGRider(oApp);
            loControl.setDisbursement(poDisbursementController);
            loControl.setTransaction(fsTransactionNo);
            fxmlLoader.setController(loControl);
            //load the main interface
            Parent parent = fxmlLoader.load();
            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            //set the main interface as the scene
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);
        }
    }

    private void initTextFieldsProperty() {
        tfSearchBankName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poCheckStatusUpdateController.setCheckpayment();
                        poCheckStatusUpdateController.CheckPayments().getModel().setBankID("");
                        poCheckStatusUpdateController.CheckPayments().getModel().setBankAcountID("");
                        tfSearchBankName.setText("");
                        tfSearchBankAccount.setText("");
                        psSearchBankID = "";
                        psSearchBankAccountID = "";
                        loadTableMain();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
        tfSearchBankAccount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poCheckStatusUpdateController.setCheckpayment();
                        poCheckStatusUpdateController.CheckPayments().getModel().setBankAcountID("");
                        tfSearchBankAccount.setText("");
                        psSearchBankAccountID = "";
                        loadTableMain();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(CheckStatusUpdateByBatchController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnClear);
    }
}
