package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckPrintingRequest;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckPrintRequestStatus;
import ph.com.guanzongroup.integsys.model.ModelDisbursementVoucher_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckPrintRequest_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private static final int ROWS_PER_PAGE = 50;
    private int pnMain = 0;
    private int pnDetail = 0;
    private boolean lsIsSaved = false;
    private final String pxeModuleName = "Check Print Request History";
    private CheckPrintingRequest poCheckPrintingRequestController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psSearchReferNo = "";

    private unloadForm poUnload = new unloadForm();
    private ObservableList<ModelDisbursementVoucher_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelDisbursementVoucher_Detail> filteredDataDetailDV;

    ObservableList<String> cPayeeType = FXCollections.observableArrayList("INDIVIDUAL", "CORPORATION");
    @FXML
    private AnchorPane AnchorMain;
    @FXML
    private AnchorPane apBrowse;
    @FXML
    private Label lblSource;
    @FXML
    private TextField tfSearchReferNo;
    @FXML
    private AnchorPane apButton;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    /*Master*/
    @FXML
    private AnchorPane apMaster;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private TextField tfTransactionNo, tfBankName, tfTotalAmount;
    @FXML
    private TextArea taRemarks;
    @FXML
    private Label lblTransactionStatus;
    @FXML
    private CheckBox chbkUploaded;
    /*Detail*/
    @FXML
    private AnchorPane apDetail;
    @FXML
    private TextField tfReferNo, tfCheckNo, tfCheckAmount, tfPayeeNAme, tfDVNo, tfDVAmount;
    @FXML
    private DatePicker dpDVDate, dpCheckDate;
    @FXML
    private ComboBox<String> cmbPayeeType;
    @FXML
    private TextArea taRemarksDetails;
    @FXML
    private TableView tblVwDetail;
    @FXML
    private TableColumn tblRowNoDetail, tblReferNoDetail, tblDVNo, tblDVDate, tblDVAmount, tblCheckNo, tblCheckDate, tblCheckAmount;

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
        poCheckPrintingRequestController = new CashflowControllers(oApp, null).CheckPrintingRequest();
        poCheckPrintingRequestController.setTransactionStatus(CheckPrintRequestStatus.OPEN + CheckPrintRequestStatus.CONFIRMED + CheckPrintRequestStatus.VOID);
        poJSON = new JSONObject();
        poJSON = poCheckPrintingRequestController.InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initAll();
        Platform.runLater(() -> {
            poCheckPrintingRequestController.Master().setIndustryID(psIndustryId);
            poCheckPrintingRequestController.Master().setCompanyID(psCompanyId);
            poCheckPrintingRequestController.setIndustryID(psIndustryId);
            poCheckPrintingRequestController.setCompanyID(psCompanyId);
            loadRecordSearch();
        });
    }

    private void initAll() {
        initButtonsClickActions();
        initTextFields();
        initComboBox();
        initTableDetail();
        initTableOnClick();
        clearFields();
        initTextFieldsProperty();
        pnEditMode = EditMode.UNKNOWN;
        initFields(pnEditMode);
        initButton(pnEditMode);
    }

    private void loadRecordSearch() {
        try {
            lblSource.setText(poCheckPrintingRequestController.Master().Company().getCompanyName() + " - " + poCheckPrintingRequestController.Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(CheckPrintRequest_HistoryController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse, btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnBrowse":
                    poJSON = poCheckPrintingRequestController.SearchTransaction("", psSearchReferNo);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    clearFields();
                    pnEditMode = poCheckPrintingRequestController.getEditMode();
                    loadTableDetail();
                    break;
                case "btnHistory":
                    if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                        ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                        return;
                    }

                    try {
                        poCheckPrintingRequestController.ShowStatusHistory();
                    } catch (NullPointerException npe) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                        ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this Tab?", "Close Tab", null)) {
                        poUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
                    } else {
                        return;
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Please contact admin to assist about no button available", pxeModuleName, null);
                    break;
            }
            if (lsButton.equals("btnSave") || lsButton.equals("btnConfirm") || lsButton.equals("btnVoid") || lsButton.equals("btnCancel")) {
                poCheckPrintingRequestController.resetMaster();
                poCheckPrintingRequestController.resetOthers();
                poCheckPrintingRequestController.Detail().clear();
                clearFields();
                details_data.clear();
                pnEditMode = EditMode.UNKNOWN;
            }
            initFields(pnEditMode);
            initButton(pnEditMode);
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(DisbursementVoucher_VerificationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMaster() {
        try {
            lblTransactionStatus.setText(getStatus(poCheckPrintingRequestController.Master().getTransactionStatus()));
            tfTransactionNo.setText(poCheckPrintingRequestController.Master().getTransactionNo() != null ? poCheckPrintingRequestController.Master().getTransactionNo() : "");
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckPrintingRequestController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfBankName.setText(poCheckPrintingRequestController.Master().Banks().getBankName() != null ? poCheckPrintingRequestController.Master().Banks().getBankName() : "");
            tfTotalAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingRequestController.Master().getTotalAmount(), true));
            taRemarks.setText(poCheckPrintingRequestController.Master().getRemarks() != null ? poCheckPrintingRequestController.Master().getRemarks() : "");
            chbkUploaded.setSelected(poCheckPrintingRequestController.Master().isUploaded());
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(CheckPrintRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getStatus(String lsValueStatus) {
        String lsStatus;
        switch (lsValueStatus) {
            case CheckPrintRequestStatus.OPEN:
                lsStatus = "OPEN";
                break;
            case CheckPrintRequestStatus.CONFIRMED:
                lsStatus = "CONFIRMED";
                break;
            case CheckPrintRequestStatus.VOID:
                lsStatus = "VOID";
                break;
            default:
                lsStatus = "UNKNOWN";
                break;
        }
        return lsStatus;
    }

    private void loadRecordDetail() {
        if (pnDetail >= 0) {
            try {
                tfReferNo.setText(poCheckPrintingRequestController.Detail(pnDetail).getSourceNo());
                tfCheckNo.setText(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getCheckNo());
                tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingRequestController.Master().getTotalAmount(), true));
                tfPayeeNAme.setText(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().Payee().getPayeeName());
                tfDVNo.setText(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().getTransactionNo());
                tfDVAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().getTransactionTotal(), true));
                dpDVDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
                dpCheckDate.setValue(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getCheckDate() != null
                        ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                        : null);
                cmbPayeeType.getSelectionModel().select(!poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getPayeeType().equals("")
                        ? Integer.valueOf(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getPayeeType()) : -1);
                taRemarksDetails.setText(poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getRemarks() != null ? poCheckPrintingRequestController.Detail(pnDetail).DisbursementMaster().CheckPayments().getRemarks() : "");

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(CheckPrintRequest_EntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTableDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblVwDetail.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);
        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        lnCtr = poCheckPrintingRequestController.getDetailCount() - 1;
                        if (lnCtr >= 0) {
                            if (poCheckPrintingRequestController.Detail(lnCtr).getSourceNo() != null
                                    && !poCheckPrintingRequestController.Detail(lnCtr).getSourceNo().equals("")) {
                                try {
                                    poCheckPrintingRequestController.AddDetail();

                                } catch (CloneNotSupportedException ex) {
                                    Logger.getLogger(CheckPrintRequest_EntryController.class
                                            .getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                    for (lnCtr = 0; lnCtr < poCheckPrintingRequestController.getDetailCount(); lnCtr++) {
                        try {
                            details_data.add(
                                    new ModelDisbursementVoucher_Detail(String.valueOf(lnCtr + 1),
                                            poCheckPrintingRequestController.Detail(lnCtr).getSourceNo(),
                                            poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().getTransactionNo(),
                                            CustomCommonUtil.formatDateToShortString(poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().getTransactionDate()),
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().getNetTotal(), true),
                                            poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().CheckPayments().getCheckNo(),
                                            poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().CheckPayments().getCheckDate() != null
                                            ? CustomCommonUtil.formatDateToShortString(
                                                    poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().CheckPayments().getCheckDate())
                                            : "",
                                            CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckPrintingRequestController.Detail(lnCtr).DisbursementMaster().CheckPayments().getAmount(), true)
                                    ));

                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(CheckPrintRequest_EntryController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (pnDetail < 0 || pnDetail >= details_data.size()) {
                        if (!details_data.isEmpty()) {
                            tblVwDetail.getSelectionModel().select(0);
                            tblVwDetail.getFocusModel().focus(0);
                            pnDetail = tblVwDetail.getSelectionModel().getSelectedIndex();
                        }
                    } else {
                        tblVwDetail.getSelectionModel().select(pnDetail);
                        tblVwDetail.getFocusModel().focus(pnDetail);
                    }
                    loadRecordMaster();
                });
                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblVwDetail.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblVwDetail.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }
        };
        new Thread(task).start();

    }

    private void initTableDetail() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblReferNoDetail, tblDVNo, tblDVDate, tblCheckNo, tblCheckDate);
        JFXUtil.setColumnRight(tblDVAmount, tblCheckAmount);
        JFXUtil.setColumnsIndexAndDisableReordering(tblVwDetail);
        filteredDataDetailDV = new FilteredList<>(details_data, b -> true);

        SortedList<ModelDisbursementVoucher_Detail> sortedData = new SortedList<>(filteredDataDetailDV);
        sortedData.comparatorProperty().bind(tblVwDetail.comparatorProperty());
        tblVwDetail.setItems(sortedData);
        tblVwDetail.autosize();
    }

    private void initTableOnClick() {
        tblVwDetail.setOnMouseClicked(event -> {
            if (!details_data.isEmpty()) {
                if (event.getClickCount() == 1) {
                    pnDetail = tblVwDetail.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    taRemarksDetails.requestFocus();
                    initFields(pnEditMode);
                }
            }
        });

        tblVwDetail.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblVwDetail);
    }

    private void tableKeyEvents(KeyEvent event) {
        if (!details_data.isEmpty()) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblVwDetail":
                    if (focusedCell != null) {
                        switch (event.getCode()) {
                            case TAB:
                            case DOWN:
                                pnDetail = JFXUtil.moveToNextRow(currentTable);
                                break;
                            case UP:
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                break;
                            default:
                                break;
                        }
                        loadRecordDetail();
                        event.consume();
                    }
                    break;
            }
        }
    }

    private void initTextFields() {
        //Initialise  TextField KeyPressed
        tfSearchReferNo.setOnKeyPressed(event -> txtField_KeyPressed(event));
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField) event.getSource();
        String lsID = (((TextField) event.getSource()).getId());
        String lsValue = (txtField.getText() == null ? "" : txtField.getText());
        poJSON = new JSONObject();
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    switch (lsID) {
                        case "tfSearchReferNo":
                            psSearchReferNo = tfSearchReferNo.getText();
                            break;
                    }
                    event.consume();
                    break;
                default:
                    break;

            }
        }
    }

    private void movePrevious() {
        apDetail.requestFocus();

        initFields(pnEditMode);
    }

    private void moveNext() {
        apDetail.requestFocus();

        initFields(pnEditMode);
    }

    private void initComboBox() {
        // Set Items
        cmbPayeeType.setItems(cPayeeType);

    }

    private void clearFields() {
        JFXUtil.setValueToNull(null, dpTransactionDate, dpDVDate, dpCheckDate);
        JFXUtil.setValueToNull(null, cmbPayeeType);
        JFXUtil.clearTextFields(apDetail, apMaster);
        CustomCommonUtil.setSelected(false, chbkUploaded);
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apMaster);
        JFXUtil.setDisabled(true, apDetail);
        if (pnDetail >= 0) {
            if (!tfReferNo.getText().isEmpty()) {
                JFXUtil.setDisabled(!lbShow, apDetail);
            }
        }
    }

    private void initButton(int fnEditMode) {
        JFXUtil.setButtonsVisibility(false, btnHistory);
    }

    private void initTextFieldsProperty() {
        tfSearchReferNo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    psSearchReferNo = "";
                }
            }
        }
        );
    }
}
