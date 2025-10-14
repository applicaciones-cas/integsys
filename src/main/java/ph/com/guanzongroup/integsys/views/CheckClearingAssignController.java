package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CheckStatusUpdate;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author User
 */
public class CheckClearingAssignController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = "Check Clearing Assignment";
    private CheckStatusUpdate poCheckStatusUpdateController;

    public int pnEditMode;
    private String psTransactionNo = "";
    private int currentTransactionIndex = 0;
    private long startingCheckNo = -1;
    private int checkNoLength = 6;
    private String originalStatus = "";
    private boolean isInternalSelectionChange = false;
    private EventHandler<ActionEvent> checkStateHandler;
    private String lastValidCheckState = null;

    ObservableList<String> cCheckState = FXCollections.observableArrayList("OPEN", "CLEAR", "HOLD");

    @FXML
    private AnchorPane AnchorMain, apMaster;
    @FXML
    private StackPane StackPane;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnClearAssign, btnClose;
    @FXML
    private AnchorPane AnchorInputs;
    @FXML
    private TextField tfDVNo, tfCheckNo, tfCheckAmount, tfVoucherNo;
    @FXML
    private DatePicker dpTransactionDate, dpCheckDate, dpClearDate;
    @FXML
    private ComboBox<String> cmbCheckState;

    private List<String> transactionNos;

    public void setTransaction(List<String> transactionNos) {
        this.transactionNos = transactionNos;
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setCheckStatusUpdate(CheckStatusUpdate foValue) {
        poCheckStatusUpdateController = foValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CustomCommonUtil.setDropShadow(AnchorMain, StackPane);
        if (transactionNos != null && !transactionNos.isEmpty()) {
            try {
                poCheckStatusUpdateController = new CashflowControllers(oApp, null).CheckStatusUpdate();
                poJSON = poCheckStatusUpdateController.InitTransaction();
                if (!"success".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                loadTransaction(currentTransactionIndex);
            } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                Logger.getLogger(CheckAssignmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTransaction(int startIndex) throws SQLException, GuanzonException, CloneNotSupportedException {
        for (int i = startIndex; i < transactionNos.size(); i++) {
            psTransactionNo = transactionNos.get(i);
            currentTransactionIndex = i; // update current index

            poJSON = poCheckStatusUpdateController.OpenTransaction(psTransactionNo);
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                continue; // try next transaction
            }

            poJSON = poCheckStatusUpdateController.UpdateTransaction();
            if (!"error".equals((String) poJSON.get("result"))) {
                if (poCheckStatusUpdateController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                    poCheckStatusUpdateController.setCheckpayment();
                }
                initAll();
                loadRecordMaster();
                return; // stop here; we loaded a good one
            } else {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
            }
        }

        // If we reach here, none were successfully loaded
        ShowMessageFX.Information(null, pxeModuleName, "No valid transactions left to load.");
        CommonUtils.closeStage(btnClose);
    }

    private void initAll() {
        initButtonsClickActions();
        initDatePicker();
        initComboBox();
        clearFields();
        pnEditMode = poCheckStatusUpdateController.getEditMode();
        initFields(pnEditMode);
        initButton(pnEditMode);
    }

    private void loadRecordMaster() {
        try {
            tfDVNo.setText(poCheckStatusUpdateController.Master().getTransactionNo());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            tfVoucherNo.setText(poCheckStatusUpdateController.Master().getVoucherNo());
            poJSON = poCheckStatusUpdateController.setCheckpayment();
            if ("error".equals((String) poJSON.get("message"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                return;
            }
            loadRecordMasterCheck();
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(CheckStatusUpdateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRecordMasterCheck() {
        tfCheckNo.setText(poCheckStatusUpdateController.CheckPayments().getModel().getCheckNo());
        dpCheckDate.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate() != null
                ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE))
                : null);
        tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poCheckStatusUpdateController.CheckPayments().getModel().getAmount(), true));
        int selectedItem = -1;
        switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
            case "1": //OPEN
                selectedItem = 0;
                break;
            case "2": //CLEAR
                selectedItem = 1;
                break;
            case "5": //HOLD
                selectedItem = 2;
                break;
        }
        cmbCheckState.getSelectionModel().select(selectedItem);
        lastValidCheckState = cmbCheckState.getSelectionModel().getSelectedItem();
        switch (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus()) {
            case CheckStatus.POSTED:
//                    dpClearDate.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate() != null
//                            ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate(), SQLUtil.FORMAT_SHORT_DATE))
//                            : null);
                break;
            case CheckStatus.STOP_PAYMENT:
//                    dpHoldUntil.setValue(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate() != null
//                            ? CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poCheckStatusUpdateController.CheckPayments().getModel().getModifiedDate(), SQLUtil.FORMAT_SHORT_DATE))
//                            : null);
                break;
        }
    }

    private void assignAndCleared() {
        try {
            // Set the updated check payment data
            poJSON = poCheckStatusUpdateController.setCheckpayment();

            poCheckStatusUpdateController.CheckPayments().getModel().setTransactionStatus("2");
            poCheckStatusUpdateController.Master().setModifiedDate(oApp.getServerDate());
            poCheckStatusUpdateController.Master().setModifyingId(oApp.getClientID());
            poCheckStatusUpdateController.Master().CheckPayments().setModifiedDate(oApp.getServerDate());
            poCheckStatusUpdateController.Master().CheckPayments().setModifyingId(oApp.getClientID());

            // Save transaction
            poJSON = poCheckStatusUpdateController.SaveTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                return;
            }

            // Handle multiple or single transaction flow
            if (transactionNos.size() > 1) {
                currentTransactionIndex++;
                if (currentTransactionIndex < transactionNos.size()) {
                    loadTransaction(currentTransactionIndex);
                    assignAndCleared();
                } else {
                    ShowMessageFX.Information(null, pxeModuleName, "All transactions have been cleared check status.");
                    CommonUtils.closeStage(btnClose);
                }
            } else {
                ShowMessageFX.Information(null, pxeModuleName, "Transaction has been cleared check status.");
                CommonUtils.closeStage(btnClose);
            }

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(CheckClearingAssignController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnClearAssign, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnClearAssign":
                    int totalTransactions = transactionNos.size();
                    if (cmbCheckState.getSelectionModel().getSelectedIndex() != 1) {
                        ShowMessageFX.Warning(null, pxeModuleName, "You cannot allowed to assign clear if the selected check state is not clear.");
                        return;
                    }
                    if (totalTransactions > 1) {
                        boolean confirmMultiple = ShowMessageFX.YesNo("You are assigning multiple transactions. Do you want to proceed?", pxeModuleName, null);
                        if (confirmMultiple) {
                            assignAndCleared();
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, "Please select only one transaction to assign individually.");
                        }
                    } else {
                        if (ShowMessageFX.YesNo("Are you sure you want to clear check status?", pxeModuleName, null)) {
                            assignAndCleared();
                        }
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure want to close this form?", pxeModuleName, null)) {
                        CommonUtils.closeStage(btnClose);
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Unknown button action.", pxeModuleName, null);
                    break;
            }
            initFields(pnEditMode);
            initButton(pnEditMode);
        } catch (Exception ex) {
            Logger.getLogger(CheckAssignmentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComboBox() {
        cmbCheckState.setItems(cCheckState);
        cmbCheckState.setOnAction(e -> {
            if (pnEditMode == EditMode.UPDATE && cmbCheckState.getSelectionModel().getSelectedIndex() >= 0) {
                String selectedItem = null;
                switch (cmbCheckState.getSelectionModel().getSelectedItem()) {
                    case "OPEN":
                        selectedItem = "1";
                        break;
                    case "CLEAR":
                        selectedItem = "2";
                        break;
                    case "HOLD":
                        selectedItem = "5";
                        break;
                }
                poCheckStatusUpdateController.CheckPayments().getModel().setTransactionStatus(String.valueOf(selectedItem));
            }
            initFields(pnEditMode);
        });

    }

    private void initDatePicker() {
        dpClearDate.setOnAction(e -> {
        });
    }

    private void clearFields() {
        JFXUtil.setValueToNull(null, dpCheckDate);
        JFXUtil.clearTextFields(apMaster);
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apMaster);
        if (poCheckStatusUpdateController.CheckPayments().getModel().getTransactionStatus().equals(CheckStatus.POSTED)) {
            dpClearDate.setDisable(!lbShow);
            dpClearDate.setValue(LocalDate.now());
        } else {
            dpClearDate.setValue(null);
            dpClearDate.setDisable(true);
        }
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(lbShow, btnClearAssign);
    }
}
