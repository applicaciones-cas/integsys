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
import javafx.scene.control.DatePicker;
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
import ph.com.guanzongroup.cas.cashflow.CheckPayments;
import ph.com.guanzongroup.cas.cashflow.CheckPrinting;
import ph.com.guanzongroup.cas.cashflow.Disbursement;
import ph.com.guanzongroup.cas.cashflow.model.SelectedITems;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckPrintingController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private final String pxeModuleName = "Check Printing";
    private CheckPrinting poCheckPrintingController;
    private CheckPayments poCheckPayments;
    private Disbursement poDisbursementController;
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
    private DatePicker dpDVDateFrom, dpDVDateTo;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchBankName, tfSearchBankAccount;
    @FXML
    private AnchorPane apButton;
    @FXML
    private Button btnAssign, btnRetrieve, btnClose, btnPrintCheck, btnPrintDV;
    @FXML
    private TableView<ModelCheckPrinting> tblVwMain;
    @FXML
    private TableColumn<ModelCheckPrinting, String> tblRowNo, tblDVNo, tblDVDate, tblSupplier, tblPayeeName, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate, tblTotalAmount;
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
            poCheckPrintingController = new CashflowControllers(oApp, null).CheckPrinting();
            poJSON = new JSONObject();
            poJSON = poCheckPrintingController.InitTransaction(); // Initialize transaction
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            }
            initAll();
            Platform.runLater(() -> {
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initAll() {
        poCheckPrintingController.Master().setIndustryID(psIndustryId);
        poCheckPrintingController.Master().setCompanyID(psCompanyId);
        loadDate();
        initDatePicker();
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
            lblSource.setText(poCheckPrintingController.Master().Company().getCompanyName() + " - " + poCheckPrintingController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initDatePicker() {
        LocalDate dateNow = LocalDate.now();
        dpDVDateFrom.setOnAction(e -> {
            LocalDate selectedFromDate = dpDVDateFrom.getValue();
            LocalDate toDate = dpDVDateTo.getValue();

            if (toDate != null && selectedFromDate.isAfter(toDate)) {
                ShowMessageFX.Warning("Invalid Date, The 'From' date cannot be after the 'To' date.", pxeModuleName, null);
                dpDVDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                return;
            }

            psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
            loadTableMain();
        });

        dpDVDateTo.setOnAction(e -> {
            LocalDate selectedToDate = dpDVDateTo.getValue();
            LocalDate fromDate = dpDVDateFrom.getValue();

            if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                ShowMessageFX.Warning("Invalid Date, The 'To' date cannot be before the 'From' date.", pxeModuleName, null);
                dpDVDateTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                return;
            }

            psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
            loadTableMain();
        });
    }

    private void loadDate() {
        LocalDate dateNow = LocalDate.now();
        dpDVDateFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
        dpDVDateTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
        psSearchDVDateFrom = CustomCommonUtil.formatLocalDateToShortString(dateNow);
        psSearchDVDateTo = CustomCommonUtil.formatLocalDateToShortString(dateNow);
        loadTableMain();
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnAssign, btnRetrieve, btnClose, btnPrintCheck, btnPrintDV);
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

        for (ModelCheckPrinting item : selectedItems) {
            String lsDVNO = item.getIndex03();
            String banks = item.getIndex07();

            if (firstBank == null) {
                firstBank = banks; // store the first encountered bank
            } else if (!firstBank.equals(banks)) {
                allSameBank = false;
                break; // no need to continue checking
            }

            listOfDVToAssign.add(lsDVNO);
            successCount++;
        }
        if (!allSameBank) {
            
            poJSON.put("message", "Selected items must belong to the same bank.");
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
                case "btnAssign":
                    poJSON =  validateSelectedItem();
                     if ("error".equals(poJSON.get("result"))){
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
                    loadTableMainAndClearSelectedItems() ;
                    break;
                case "btnPrintCheck":
                   poJSON =  validateSelectedItem();
                   if ("error".equals(poJSON.get("result"))){
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                         break;
                   }
                    if (!listOfDVToAssign.isEmpty()) {
                        poJSON = poCheckPrintingController.PrintCheck(listOfDVToAssign);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                             break;
                        }
                        chckSelectAll.setSelected(false);
                        getSelectedItems.clear();
                        listOfDVToAssign.clear();
                    }
                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
//                    ShowMessageFX.YesNo("Do you want to print BIR 2307 Form?", pxeModuleName, null);
                    break;
                case "btnPrintDV":
                    poJSON =  validateSelectedItem();
                     if ("error".equals(poJSON.get("result"))){
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                         break;
                   }
                    if (!listOfDVToAssign.isEmpty()) {
                        poJSON = poCheckPrintingController.printTransaction(listOfDVToAssign);
                        if (!"success".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        }
                        chckSelectAll.setSelected(false);
                        getSelectedItems.clear();
                        listOfDVToAssign.clear();
                    }
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
        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
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
                                poCheckPrintingController.setCheckpayment();
                                poJSON = poCheckPrintingController.SearchBanks(lsValue, true);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankName.setText(poCheckPrintingController.CheckPayments().getModel().Banks().getBankName() != null ? poCheckPrintingController.CheckPayments().getModel().Banks().getBankName() : "");
                                psSearchBankID = poCheckPrintingController.CheckPayments().getModel().getBankID();
                                break;
                            case "tfSearchBankAccount":
                                poCheckPrintingController.setCheckpayment();
                                poJSON = poCheckPrintingController.SearhBankAccountForCheckPrinting(lsValue, psSearchBankID, false);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                    return;
                                }
                                tfSearchBankAccount.setText(poCheckPrintingController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() != null ? poCheckPrintingController.CheckPayments().getModel().Bank_Account_Master().getAccountNo() : "");
                                psSearchBankAccountID = poCheckPrintingController.CheckPayments().getModel().getBankAcountID();
                                break;
                        }
                        CommonUtils.SetNextFocus(txtField);
                        loadTableMain();
                        event.consume();
                    default:
                        break;
                }

            } catch (ExceptionInInitializerError | SQLException | GuanzonException ex) {
                Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
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
                        poJSON = poCheckPrintingController.getDisbursementForCheckPrinting(psSearchBankID, psSearchBankAccountID, psSearchDVDateFrom, psSearchDVDateTo);
                        if ("success".equals(poJSON.get("result"))) {
                            if (poCheckPrintingController.getDisbursementMasterCount() > 0) {
                                int checkIndex = 0;
                                int otherIndex = 0;
                                for (int lnCntr = 0; lnCntr < poCheckPrintingController.getDisbursementMasterCount(); lnCntr++) {
                                    String lsPaymentForm = "";
                                    String lsBankName = "";
                                    String lsBankAccount = "";
                                    String disbursementType = poCheckPrintingController.poDisbursementMaster(lnCntr).getDisbursementType();
                                    switch (disbursementType) {
                                        case DisbursementStatic.DisbursementType.CHECK:
                                            lsPaymentForm = "CHECK";
//                                            if (checkIndex < poCheckPrintingController.CheckPayments().getCheckPaymentsCount()) {
//                                                lsBankName = poCheckPrintingController.CheckPayments().poCheckPayments(checkIndex).Banks().getBankName();
//                                                lsBankAccount = poCheckPrintingController.CheckPayments().poCheckPayments(checkIndex).getBankAcountID();
//                                            }
                                            checkIndex++; // Move to next CHECK payment
                                            break;
                                        default:
                                            lsPaymentForm = "";
                                            break;
                                    }

                                    main_data.add(new ModelCheckPrinting(
                                            String.valueOf(lnCntr + 1),
                                            "",
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poCheckPrintingController.poDisbursementMaster(lnCntr).getTransactionDate()),
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).Payee().getPayeeName(),
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).Payee().getPayeeName(),
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).CheckPayments().Banks().getBankName(),
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).CheckPayments().Bank_Account_Master().getAccountNo(),
                                            poCheckPrintingController.poDisbursementMaster(lnCntr).CheckPayments().getCheckNo(),
                                            CustomCommonUtil.formatDateToShortString(poCheckPrintingController.poDisbursementMaster(lnCntr).getTransactionDate()),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingController.poDisbursementMaster(lnCntr).getNetTotal(), true)
                                    ));
                                }
                            }
                        } else {
                            main_data.clear();
//                            }
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
                        Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
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
        JFXUtil.setColumnCenter(tblRowNo, tblDVNo, tblDVDate, tblSupplier, tblPayeeName, tblBankName, tblBankAccount, tblCheckNo, tblCheckDate);
        JFXUtil.setColumnRight(tblTotalAmount);
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
                    Logger.getLogger(CheckPrintingController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void loadAssignWindow(List<String> fsTransactionNos) throws SQLException, GuanzonException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ph/com/guanzongroup/integsys/views/CheckAssignment.fxml"));

            CheckAssignmentController loControl = new CheckAssignmentController();
            loControl.setGRider(oApp);
            loControl.setCheckPrinting(poCheckPrintingController);
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
//            poCheckPrintingController.resetOthers();
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
                        poCheckPrintingController.setCheckpayment();
                        poCheckPrintingController.CheckPayments().getModel().setBankID("");
                        poCheckPrintingController.CheckPayments().getModel().setBankAcountID("");
                        tfSearchBankName.setText("");
                        tfSearchBankAccount.setText("");
                        psSearchBankID = "";
                        psSearchBankAccountID = "";
                        loadTableMain();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
        tfSearchBankAccount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    try {
                        poCheckPrintingController.setCheckpayment();
                        poCheckPrintingController.CheckPayments().getModel().setBankAcountID("");
                        tfSearchBankAccount.setText("");
                        psSearchBankAccountID = "";
                        loadTableMain();
                    } catch (GuanzonException | SQLException ex) {
                        Logger.getLogger(CheckPrintingController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }

    private void initButtons() {
        JFXUtil.setButtonsVisibility(!main_data.isEmpty(), btnAssign, btnPrintCheck, btnPrintDV);
    }
}
