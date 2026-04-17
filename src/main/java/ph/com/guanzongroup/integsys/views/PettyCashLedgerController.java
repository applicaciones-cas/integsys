package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.PettyCash;
import ph.com.guanzongroup.integsys.model.ModelPettyCashLedger;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

/**
 * FXML Controller class
 *
 * @author Team 1 Rsie & Aldrich 04-07-2026
 */
public class PettyCashLedgerController implements Initializable {
    
    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnEntryNo = 0;
    int pnDetail = 0;
    private final String pxeModuleName = "Petty Cash Ledger";
    static PettyCash poController;
    private String psSearchDateFrom = "";
    private String psSearchDateTo = "";
    private ObservableList<ModelPettyCashLedger> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelPettyCashLedger> filteredMain_Data;
    JFXUtil.ReloadableTableTask loadTableDetail;
    private static final int ROWS_PER_PAGE = 50;
    @FXML
    private AnchorPane apButton, apDetail;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button  btnClose;
    @FXML
    private DatePicker dpFrom, dpTo;
    @FXML
    private TableView<ModelPettyCashLedger> tblViewDetail;
    @FXML
    private TableColumn tblRowNoDetail, tblLedgerNo, tblTransDate, tblSourceNo, tblSourceCode, tblDescription, tblDebit, tblCredit;
    @FXML
    private Pagination pagination;

    public void setObject(PettyCash foObject) {
        poController = foObject;
    }

    private Stage getStage() {
        return (Stage) btnClose.getScene().getWindow();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initLoadTable();
        initDetailGrid();
        initDatePickers();
        loadRecordSearch();
        pagination.setPageCount(1);
        Platform.runLater(() -> {
            retrieveLedger();
            PauseTransition delay = new PauseTransition(Duration.seconds(0.05));
            delay.setOnFinished(event -> {
                loadTableDetail.reload();
            });
            delay.play();
        });
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }
    
    private void loadRecordSearch() {
        try {
            //define if both are empty
            if (dpFrom.getEditor().getText().equals("")) {
                String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                JFXUtil.setDateValue(dpFrom, CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                psSearchDateFrom = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
            }
            if (dpTo.getEditor().getText().equals("")) {
                String lsDateTo = CustomCommonUtil.formatDateToShortString(oApp.getServerDate());
                JFXUtil.setDateValue(dpTo, CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
                psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(CustomCommonUtil.parseDateStringToLocalDate(lsDateTo, "yyyy-MM-dd"));
            }

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }


    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            String lsButton = clickedButton.getId();
            switch (lsButton) {
                case "btnClose":
                    CommonUtils.closeStage(btnClose);
                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    break;
            }
        }
    }

    public void initLoadTable() {
        loadTableDetail = new JFXUtil.ReloadableTableTask(
                tblViewDetail,
                details_data,
                () -> {
                    Platform.runLater(() -> {
                        details_data.clear();
                        int lnRowCount = 0;
                        String date = "";
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        for (int lnCtr = 0; lnCtr < poController.getLedgerListCount(); lnCtr++) {
                            try {
                                if (!JFXUtil.isObjectEqualTo(poController.LedgerList(lnCtr).getTransactionDate(), null, "")) {
                                    date = sdf.format(poController.LedgerList(lnCtr).getTransactionDate());
                                }
                                lnRowCount += 1;
                                details_data.add(
                                        new ModelPettyCashLedger(String.valueOf(lnRowCount),
                                                String.valueOf(poController.LedgerList(lnCtr).getLedgerNo()),
                                                date,
                                                String.valueOf(poController.LedgerList(lnCtr).getSourceNo()),
                                                String.valueOf(poController.LedgerList(lnCtr).getSourceCode()),
                                                String.valueOf(poController.LedgerList(lnCtr).PettyCash().getDescription()),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.LedgerList(lnCtr).getDebitAmount(), true),
                                                CustomCommonUtil.setIntegerValueToDecimalFormat(poController.LedgerList(lnCtr).getCreditAmount(), true),
                                                String.valueOf(lnCtr)
                                        ));
                            } catch (SQLException | GuanzonException ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
                            }
                        }
                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewDetail, 0);
                                pnDetail = tblViewDetail.getSelectionModel().getSelectedIndex();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewDetail, pnDetail);
                        }
                        
                        JFXUtil.loadTab(pagination, details_data.size(), ROWS_PER_PAGE, tblViewDetail, filteredMain_Data);
                    });
                });
    }
    
    private void datepicker_Action(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            JFXUtil.setJSONSuccess(poJSON, "success");
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                LocalDate dateNow = LocalDate.now();
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                switch (datePicker.getId()) {
                    case "dpFrom":
                        LocalDate selectedFromDate = dpFrom.getValue();
                        LocalDate toDate = dpTo.getValue();
                        if (toDate != null && selectedFromDate.isAfter(toDate)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'From' date cannot be after the 'To' date.");
                            String lsDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                            dpFrom.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsDateFrom, "yyyy-MM-dd"));
                            psSearchDateFrom = CustomCommonUtil.formatDateToShortString(getFirstDayOfMonth(oApp.getServerDate()));
                            return;
                        }
                        psSearchDateFrom = CustomCommonUtil.formatLocalDateToShortString(selectedFromDate);
                        retrieveLedger();
                        break;
                    case "dpTo":
                        LocalDate selectedToDate = dpTo.getValue();
                        LocalDate fromDate = dpFrom.getValue();
                        if (fromDate != null && selectedToDate.isBefore(fromDate)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Invalid Date, The 'To' date cannot be before the 'From' date.");
                            dpTo.setValue(CustomCommonUtil.parseDateStringToLocalDate(dateNow.toString()));
                            psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(dateNow);
                            return;
                        }
                        psSearchDateTo = CustomCommonUtil.formatLocalDateToShortString(selectedToDate);
                        retrieveLedger();
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private void retrieveLedger() {
        try {
            if (dpFrom.getEditor().getText().equals("") || dpTo.getEditor().getText().equals("")) {
                ShowMessageFX.Warning(null, pxeModuleName, "Please select both the From and To dates to retrieve/reload the disbursement records.");
                loadRecordSearch();
                return;
            }
            poJSON = poController.loadLedger( psSearchDateFrom, psSearchDateTo);
            if ("error".equals(poJSON.get("result"))) {
//                ShowMessageFX.Error(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
            }
            loadTableDetail.reload();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
//            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }
    
    private void initDetailGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblLedgerNo, tblTransDate, tblSourceNo, tblSourceCode, tblDescription, tblDebit, tblCredit);
        JFXUtil.setColumnLeft(tblDescription);
        JFXUtil.setColumnRight(tblDebit, tblCredit);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetail);
        filteredMain_Data = new FilteredList<>(details_data, b -> true);
        tblViewDetail.setItems(filteredMain_Data);
    }

    private void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpFrom, dpTo);
        JFXUtil.setActionListener(this::datepicker_Action, dpFrom, dpTo);
    }
    
    public static Date getFirstDayOfMonth(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Set to first day of the same month and year
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Optional: Reset time to start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
