/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.cas.purchasing.services.PurchaseOrderReceivingControllers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.model.ModelTableDetail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author user
 */
public class DisbursementVoucherReportsController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private DisbursementVoucher poDisbursementVoucher;
    private String psFormName = "PO Summary Report";
    private LogWrapper logWrapper;
    private int pnEditMode;
    private JSONObject poJSON;
    unloadForm poUnload = new unloadForm();
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    boolean isSummarized = true;
    private String searchPayee = "";
    private String searchAccountNo = "";
    private String searchAccountNme = "";
    private String searchBankName = "";
    private String searchCheckStat = "";
    private LocalDate datefrom ;
    private Boolean isSearching = true;
    
    private static final int ROWS_PER_PAGE = 50;
    private List<ModelTableDetail> allData = new ArrayList<>();
    
    JSONArray data;

    private ObservableList<ModelTableDetail> detail_data = FXCollections.observableArrayList();
    ObservableList<String> DVStatus = FXCollections.observableArrayList("ALL", "OPEN", "VERIFIED", "CERTIFIED", "CANCELLED", "AUTHORIZED", "VOID", "DISAPPROVED", "RETURNED", "RETURNED_I");
    ObservableList<String> CheckStatus = FXCollections.observableArrayList("ALL", "FLOAT", "FLOAT", "POSTED", "CANCELLED", "STALED", "STOP_PAYMENT", "BOUNCED", "VOID");

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton;

    @FXML
    private RadioButton rbPresentation01, rbPresentation02;

    @FXML
    private ToggleGroup presentation;

    @FXML
    private TextField tfSearchBankName,
            tfSearchPayee, tfSearchAccountNo, tfSearchAccountName;

    @FXML
    private DatePicker dpDateFrom, dpDateThru;

    @FXML
    private ComboBox cmbDVStatus, cmbCheckStatus;

    @FXML
    private HBox hbButtons;

    @FXML
    private Button btnPrint, btnRetrieve, btnClose;

    @FXML
    private TableView<ModelTableDetail> tblVwOrderDetails;

    @FXML
    private TableColumn<ModelTableDetail, String>
            index01, index02, index03, index04,
            index05, index06, index07, index08,
            index09, index10, index11, index12,
            index13, index14, index15, index16,
            index17;

    @FXML
    private Pagination pagination;

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
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poDisbursementVoucher = new CashflowControllers(poApp, logWrapper).DisbursementVoucher();
            poJSON = poDisbursementVoucher.InitTransaction();
            poDisbursementVoucher.Master().setIndustryID(psIndustryID);
            poDisbursementVoucher.Master().setCompanyID(psCompanyID);
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
            }
            initButtonsClickActions();
            InitCriterea();
            initComboboxes();
            initDefaultDateRange();
            poDisbursementVoucher.setTransactionStatus("E01234569");
            initTableDetail();
            initPrint();
        } catch (SQLException | GuanzonException e) {
            throw new RuntimeException(e);
        }
    }

    private void initPrint() {
        if (data == null || data.isEmpty()) {
            Platform.runLater(() -> {
                btnPrint.setVisible(false);
                btnPrint.setManaged(false);
            });
        } else {
            Platform.runLater(() -> {
                btnPrint.setVisible(true);
                btnPrint.setManaged(true);
            });
        }
    }

    public void initComboboxes() {
        JFXUtil.setComboBoxItems(new JFXUtil.Pairs<>(DVStatus, cmbDVStatus), new JFXUtil.Pairs<>(CheckStatus, cmbCheckStatus));
        JFXUtil.setComboBoxActionListener(comboBoxActionListener, cmbDVStatus,cmbCheckStatus);
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbDVStatus,cmbCheckStatus);
        cmbDVStatus.getSelectionModel().select(0);
        cmbCheckStatus.getSelectionModel().select(0);
    }

    EventHandler<ActionEvent> comboBoxActionListener = JFXUtil.CmbActionListener(
            (cmbId, selectedIndex, selectedValue) -> {
                switch (cmbId) {

                    case "cmbDVStatus":
                        String transStat = getTranStatus((String) selectedValue);
                        poDisbursementVoucher.setTransactionStatus(String.valueOf(transStat));
                        loadTableMaster();
                        break;
                    case "cmbCheckStatus":
                        searchCheckStat = getCheckTranStatus((String) selectedValue);
                        loadTableMaster();
                        break;
                }
//                loadRecordMaster();
            }
    );

    private void initDefaultDateRange() {

        LocalDate currentDate = LocalDate.now();

        dpDateThru.setValue(currentDate);
        dpDateFrom.setValue(currentDate.minusMonths(1));

        dpDateFrom.setOnAction(datePickerActionListener);
        dpDateThru.setOnAction(datePickerActionListener);
    }

    EventHandler<ActionEvent> datePickerActionListener = event -> {

        DatePicker source = (DatePicker) event.getSource();

        if (dpDateFrom.getValue() == null || dpDateThru.getValue() == null) {
            ShowMessageFX.Warning(null,
                    "Please select both Date From and Date Thru.",
                    "Warning",
                    null);
            return;
        }

        if (dpDateThru.getValue().isBefore(dpDateFrom.getValue())) {

            ShowMessageFX.Warning(null,
                    "Date Thru cannot be earlier than Date From.",
                    "Warning",
                    null);

            // Reset Date Thru to current date
            dpDateThru.setValue(LocalDate.now());

            source.requestFocus();
            return;
        }

        loadTableMaster();
    };

    private String getTranStatus(String status) {
        switch (status) {
            case "ALL":
                return "I0123456789";
            case "OPEN":
                return "0";
            case "VERIFIED":
                return "1";
            case "CERTIFIED":
                return "2";
            case "CANCELLED":
                return "3";
            case "AUTHORIZED":
                return "4";
            case "VOID":
                return "5";
            case "DISAPPROVED":
                return "6";
            case "RETURNED":
                return "9";
            case "RETURNED_I":
                return "I";
            case "CONFIRMED":
                return "7";
            case "APPROVED":
                return "8";
            default:
                return null;
        }
    }

    private String getCheckTranStatus(String status) {
        switch (status) {
            case "ALL":
                return "76543210";
            case "FLOAT":
                return "0";
            case "OPEN":
                return "1";
            case "POSTED":
                return "2";
            case "CANCELLED":
                return "3";
            case "STALED":
                return "4";
            case "STOP_PAYMENT":
                return "5";
            case "BOUNCED":
                return "6";
            case "VOID":
                return "7";
            default:
                return null;
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnPrint, btnClose, btnRetrieve);
        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void handleButtonAction(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnClose":
                if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                    if (poUnload != null) {
                        poUnload.unloadForm(AnchorMain, poApp, psFormName);
                    } else {
                        ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                    }
                }
                break;
            case "btnRetrieve":
                loadTableMaster();
                initPrint();
                break;
            case "btnPrint":
                poJSON = poDisbursementVoucher.printReports(isSummarized, data);
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                }
                break;
        }
    }

    private void InitCriterea() {
        TextField[] textFields = {
            tfSearchBankName,
            tfSearchPayee,
            tfSearchAccountNo,
            tfSearchAccountName
        };

        for (TextField tf : textFields) {
            tf.focusedProperty().addListener(txtField_Focus);
            tf.setOnKeyPressed(this::txtField_KeyPressed);
        }

        rbPresentation01.setSelected(isSummarized);
        rbPresentation02.setSelected(!isSummarized);
        initTableDetail();
        presentation.selectToggle(rbPresentation01);
        if (presentation != null) {
            presentation.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    return;
                }
                RadioButton selected = (RadioButton) newVal;
                System.out.println("Selected: " + selected.getText());
                if (selected == rbPresentation01) {
                    isSummarized = true;
                    poDisbursementVoucher.Master().setSummarized(true);

                } else if (selected == rbPresentation02) {
                    isSummarized = false;
                    poDisbursementVoucher.Master().setSummarized(false);
                }

                clear();
                initTableDetail();
                initPrint();
            });
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField lsTxtField = (TextField) event.getSource();
        String txtFieldID = ((TextField) event.getSource()).getId();
        String lsValue = "";
        if (lsTxtField.getText() == null) {
            lsValue = "";
        } else {
            lsValue = lsTxtField.getText();
        }
        if (null != event.getCode()) {
            try {
                switch (event.getCode()) {
                    case TAB:
                        break;
                    case ENTER:
                    case F3:
                        switch (txtFieldID) {

                            case "tfSearchPayee":
                                isSearching = true;
                                poJSON = poDisbursementVoucher.SearchPayee(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                                tfSearchPayee.setText((String)poJSON.get("payeeName"));
                                searchPayee = (String)poJSON.get("payeeName");
                                break;
                            case "tfSearchAccountNo":
                                isSearching = true;
                                poJSON = poDisbursementVoucher.SearchAccount(lsValue, true);
                                if ("error".equals(poJSON.get("result"))) {
//                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                                tfSearchAccountNo.setText((String)poJSON.get("accountNo"));
                                searchAccountNo = (String)poJSON.get("accountNo");
                                break;
                            case "tfSearchAccountName":
                                isSearching = true;
                                poJSON = poDisbursementVoucher.SearchAccount(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
//                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                                tfSearchAccountName.setText((String)poJSON.get("accountName"));
                                searchAccountNme = (String)poJSON.get("accountName");
                                break;
                            case "tfSearchBankName":
                                isSearching = true;
                                poJSON = poDisbursementVoucher.SearchBank(lsValue, false);
                                if ("error".equals(poJSON.get("result"))) {
//                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    return;
                                }
                                tfSearchBankName.setText((String)poJSON.get("bankName"));
                                searchBankName = (String)poJSON.get("bankName");
                                break;
                        }
                        break;
                    case UP:
                        break;
                    case DOWN:
                        break;
                    default:
                        break;
                        

                }
                loadTableMaster();
            } catch (SQLException | GuanzonException | ExceptionInInitializerError ex) {
                Logger.getLogger(DisbursementVoucherReportsController.class.getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(ex.getMessage(), psFormName, null);
            }
        }
    }

    ChangeListener<Boolean> txtField_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {

//            try {
                /* Lost Focus */
                switch (lsID) {
                    case "tfSearchPayee":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchPayee = "";
                        }
                        break;
                    case "tfSearchAccountNo":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchAccountNo = "";
                        }
                        break;
                    case "tfSearchAccountName":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchAccountNme = "";
                        }
                        break;
                    case "tfSearchBankName":
                        if(lsValue == null || lsValue.isEmpty()){
                            searchBankName = "";
                        }
                        break;
                }
                if (!isSearching) {
                    loadTableMaster();
                }
            });

    private void initTableDetail() {

//        isSummarized = poDisbursementVoucher.Master().isSummarized();
        index01.setCellValueFactory(new PropertyValueFactory<>("index01"));
        index02.setCellValueFactory(new PropertyValueFactory<>("index02"));
        index03.setCellValueFactory(new PropertyValueFactory<>("index03"));
        index04.setCellValueFactory(new PropertyValueFactory<>("index04"));
        index05.setCellValueFactory(new PropertyValueFactory<>("index05"));
        index06.setCellValueFactory(new PropertyValueFactory<>("index06"));
        index07.setCellValueFactory(new PropertyValueFactory<>("index07"));
        index08.setCellValueFactory(new PropertyValueFactory<>("index08"));
        index09.setCellValueFactory(new PropertyValueFactory<>("index09"));
        index10.setCellValueFactory(new PropertyValueFactory<>("index10"));
        index11.setCellValueFactory(new PropertyValueFactory<>("index11"));
        index12.setCellValueFactory(new PropertyValueFactory<>("index12"));
        index13.setCellValueFactory(new PropertyValueFactory<>("index13"));
        index14.setCellValueFactory(new PropertyValueFactory<>("index14"));
        index15.setCellValueFactory(new PropertyValueFactory<>("index15"));
        index16.setCellValueFactory(new PropertyValueFactory<>("index16"));
        index17.setCellValueFactory(new PropertyValueFactory<>("index17"));

        applyTableMode(isSummarized);

        tblVwOrderDetails.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            Platform.runLater(() -> {
                TableHeaderRow header
                        = (TableHeaderRow) tblVwOrderDetails.lookup("TableHeaderRow");

                if (header != null) {
                    header.setReordering(false);
                }
            });
        });
    }

    private void applyTableMode(boolean isSummarized) {

        if (isSummarized) {

            // SUMMARY MODE
            index02.setText("Transaction No");
            index03.setText("Date Trans");
            index04.setText("Payee");
            index05.setText("Bank Acct. No.");
            index06.setText("Bank Acct. Name");
            index07.setText("Bank Name");
            index08.setText("Check No");
            index09.setText("Total");
            index10.setText("Discount");
            index11.setText("wTax");
            index12.setText("Vat Amount");
            index13.setText("Net Total");
            index14.setText("Amount");
            index15.setText("Status");
            index16.setText("is Check Printed");
            index17.setText("Check Status");

            // 🔥 ALIGNMENT
            index02.setStyle("-fx-alignment: CENTER-LEFT;");
            index03.setStyle("-fx-alignment: CENTER;");
            index04.setStyle("-fx-alignment: CENTER-LEFT;");
            index05.setStyle("-fx-alignment: CENTER-LEFT;");
            index06.setStyle("-fx-alignment: CENTER-LEFT;");
            index07.setStyle("-fx-alignment: CENTER-LEFT;");
            index08.setStyle("-fx-alignment: CENTER-LEFT;");
            index09.setStyle("-fx-alignment: CENTER-RIGHT;");
            index10.setStyle("-fx-alignment: CENTER-RIGHT;");
            index11.setStyle("-fx-alignment: CENTER-RIGHT;");
            index12.setStyle("-fx-alignment: CENTER-RIGHT;");
            index13.setStyle("-fx-alignment: CENTER-RIGHT;");
            index14.setStyle("-fx-alignment: CENTER-RIGHT;");// 💰 amount
            index15.setStyle("-fx-alignment: CENTER-LEFT;");
            index16.setStyle("-fx-alignment: CENTER;");
            index17.setStyle("-fx-alignment: CENTER-LEFT;");

            // show
            index12.setVisible(true);
            index13.setVisible(true);
            index14.setVisible(true);
            index15.setVisible(true);
            index16.setVisible(true);
            index17.setVisible(true);

        } else {


            // DETAIL MODE
            index02.setText("Transaction No");
            index03.setText("Date Trans");
            index04.setText("Payee");
            index05.setText("DV Status");
            index06.setText("Check Status.");
            index07.setText("Source Code");
            index08.setText("Source No");
            index09.setText("Amount");
            index10.setText("Amount Applied");
            index11.setText("with Vat");
            index12.setText("Vat Amount");


            // 🔥 ALIGNMENT
            index02.setStyle("-fx-alignment: CENTER-LEFT;");
            index03.setStyle("-fx-alignment: CENTER-LEFT;");
            index04.setStyle("-fx-alignment: CENTER-LEFT;");
            index05.setStyle("-fx-alignment: CENTER;");
            index06.setStyle("-fx-alignment: CENTER-LEFT;");
            index07.setStyle("-fx-alignment: CENTER-LEFT;");
            index08.setStyle("-fx-alignment: CENTER-LEFT;");
            index09.setStyle("-fx-alignment: CENTER-LEFT;");
            index10.setStyle("-fx-alignment: CENTER-LEFT;");
            index11.setStyle("-fx-alignment: CENTER-LEFT;");
            index12.setStyle("-fx-alignment: CENTER-LEFT;");
            // 💰 total
            // hide

            index13.setVisible(false);
            index14.setVisible(false);
            index15.setVisible(false);
            index16.setVisible(false);
            index17.setVisible(false);
        }
    }

    private void loadTableMaster() {
        btnRetrieve.setDisable(true);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        tblVwOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        poJSON = new JSONObject();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    detail_data.clear();
                    if (isSummarized) {
                        poJSON = poDisbursementVoucher
                                .RetriveSummaryReports(true, 
                                          dpDateFrom.getValue(),
                                          dpDateThru.getValue(),
                                          searchPayee,
                                          searchAccountNo,
                                          searchAccountNme,
                                          searchBankName,
                                          searchCheckStat);
                    } else {
                        poJSON = poDisbursementVoucher
                                .RetriveSummaryDetailedReports(false,
                                        dpDateFrom.getValue(),
                                        dpDateThru.getValue(),
                                        searchPayee,
                                        searchAccountNo,
                                        searchAccountNme,
                                        searchBankName,
                                        searchCheckStat);
                    }

                    if ("success".equals(poJSON.get("result"))) {

                        data = (JSONArray) poJSON.get("data");

                        for (int i = 0; i < data.size(); i++) {
                            JSONObject obj = (JSONObject) data.get(i);

                            if (isSummarized) {
                                detail_data.add(new ModelTableDetail(
                                        String.valueOf(i + 1),
                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                        obj.get("dTransact") == null ? "" : obj.get("dTransact").toString(),
                                        obj.get("sPayeeNme") == null ? "" : obj.get("sPayeeNme").toString(),
                                        obj.get("sActNumbr") == null ? "" : obj.get("sActNumbr").toString(),
                                        obj.get("sActNamex") == null ? "" : obj.get("sActNamex").toString(),
                                        obj.get("sBankName") == null ? "" : obj.get("sBankName").toString(),
                                        obj.get("sCheckNox") == null ? "" : obj.get("sCheckNox").toString(),
                                        obj.get("nTranTotl") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nTranTotl").toString()), true),
                                        obj.get("nDiscTotl") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nDiscTotl").toString()), true),
                                        obj.get("nWTaxTotl") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nWTaxTotl").toString()), true),
                                        obj.get("nVATAmtxx") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nVATAmtxx").toString()), true),
                                        obj.get("nNetTotal") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nNetTotal").toString()), true),
                                        obj.get("nAmountxx") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nAmountxx").toString()), true),
                                        obj.get("sStatus") == null ? "" : obj.get("sStatus").toString(),
                                        obj.get("sChkPrint") == null ? "" : obj.get("sChkPrint").toString(),
                                        obj.get("sCheckStatus") == null ? "" : obj.get("sCheckStatus").toString()
                                ));

                            } else {
                                detail_data.add(new ModelTableDetail(
                                        String.valueOf(i + 1),
                                        obj.get("sTransNox") == null ? "" : obj.get("sTransNox").toString(),
                                        obj.get("dTransact") == null ? "" : obj.get("dTransact").toString(),
                                        obj.get("sPayeeNme") == null ? "" : obj.get("sPayeeNme").toString(),
                                        obj.get("sStatus") == null ? "" : obj.get("sStatus").toString(),
                                        obj.get("sCheckStatus") == null ? "" : obj.get("sCheckStatus").toString(),
                                        obj.get("sSourceCode") == null ? "" : obj.get("sSourceCode").toString(),
                                        obj.get("sSourceNo") == null ? "" : obj.get("sSourceNo").toString(),
                                        obj.get("nAmountxx") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nAmountxx").toString()), true),
                                        obj.get("nAmtAppld") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nAmtAppld").toString()), true),
                                        obj.get("cWithVatx") == null ? "" : obj.get("cWithVatx").toString(),
                                        obj.get("nDetVatAm") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nDetVatAm").toString()), true),
                                        obj.get("nTotal") == null ? ""
                                                : CustomCommonUtil.setIntegerValueToDecimalFormat(
                                                Double.parseDouble(obj.get("nTotal").toString()), true)
                                ));
                            }
                        }
                    }

                    Platform.runLater(() -> {
                        if (detail_data.isEmpty()) {
                            tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
                        }
                        tblVwOrderDetails.setItems(detail_data);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
                initPrint();
                if (detail_data == null || detail_data.isEmpty()) {
                    tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
                    ShowMessageFX.Warning("NO RECORD TO LOAD.", psFormName, null);
                }
                setupPagination();
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
            }
        };

        new Thread(task).start();
    }
    private void setupPagination() {

    if (detail_data == null || detail_data.isEmpty()) {
        pagination.setPageCount(0);
        pagination.setPageFactory(null);

        tblVwOrderDetails.setItems(FXCollections.observableArrayList());
        tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));
        return;
    }

    int pageCount = (int) Math.ceil(detail_data.size() * 1.0 / ROWS_PER_PAGE);

    pagination.setPageCount(pageCount);
    pagination.setCurrentPageIndex(0);
    pagination.setPageFactory(this::createPage);
}
    
    private Node createPage(int pageIndex) {

        if (detail_data == null || detail_data.isEmpty()) {
            tblVwOrderDetails.setItems(FXCollections.observableArrayList());
            return new StackPane();
        }

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, detail_data.size());

        ObservableList<ModelTableDetail> pageData
                = FXCollections.observableArrayList(detail_data.subList(fromIndex, toIndex));

        tblVwOrderDetails.setItems(pageData);

        return new StackPane(); // prevents layout re-render/flicker
    }
    private void clear() {

        tfSearchPayee.clear();
        tfSearchBankName.clear();
        tfSearchAccountNo.clear();
        tfSearchAccountName.clear();

        searchPayee = "";
        searchAccountNo = "";
        searchAccountNme = "";
        searchBankName = "";

        cmbDVStatus.getSelectionModel().select(0);

        initDefaultDateRange();

        // reset data source
        detail_data = FXCollections.observableArrayList();
        data = null; 

        // 🔥 STEP 1: clear table FIRST (break binding)
        tblVwOrderDetails.getItems().clear();
        tblVwOrderDetails.refresh();
        tblVwOrderDetails.setPlaceholder(new Label("NO RECORD TO LOAD"));

        // 🔥 STEP 2: fully detach pagination BEFORE resetting
        pagination.setPageFactory(null);
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(1);

        // 🔥 STEP 3: force JavaFX layout refresh (IMPORTANT FIX)
        Platform.runLater(() -> {
            pagination.applyCss();
            pagination.layout();
        });
        
    }

}
