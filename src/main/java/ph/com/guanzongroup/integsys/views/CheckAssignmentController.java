package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javax.script.ScriptException;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.DisbursementVoucher;

public class CheckAssignmentController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = "Check Assignment";
    private DisbursementVoucher poController;

    public int pnEditMode;
    private String psTransactionNo = "";
    private int currentTransactionIndex = 0;
    private boolean isAutoProcessing = false;
    private String originalCheckNo = "";
    @FXML
    private AnchorPane AnchorMain, AnchorInputs, apMaster;
    @FXML
    private StackPane StackPane;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnAssign, btnClose, btnPrintCheck;
    @FXML
    private TextField tfDVNo, tfCheckNo, tfCheckAmount;
    @FXML
    private DatePicker dpCheckDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox chbkApplyToAll;

    private List<String> transactionNos;

    public void setTransaction(List<String> transactionNos) {
        this.transactionNos = transactionNos;
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setCheckPrinting(DisbursementVoucher foValue) {
        poController = foValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poJSON = new JSONObject();
            initButtonsClickActions();
            initTextFields();
            initDatePicker();
            initButton(pnEditMode);
            clearTextFields();
            if (transactionNos != null && !transactionNos.isEmpty()) {
                loadTransaction(currentTransactionIndex);
                initButton(pnEditMode);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof CheckBox) {
            CheckBox checkedBox = (CheckBox) source;
            switch (checkedBox.getId()) {
                case "chbkApplyToAll":
                    if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                        poJSON = poController.Master().setBankPrint(chbkApplyToAll.isSelected() ? "1" : "0");
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                    }
                    break;
            }
        }
    }

    private void cmdButton_Click(ActionEvent event) {
        try {
            poJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnAssign":
                    if (!isAutoProcessing) {
                        isAutoProcessing = true;
                        assignAndProceed();
                    }
                    break;
                case "btnPrintCheck":
                    poJSON = poController.PrintCheck(transactionNos);
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        break;
                    }
                    if (!isAutoProcessing) {
                        isAutoProcessing = true;
                        assignAndProceed();
                    }
                    CommonUtils.closeStage(btnClose);
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure want to close this form?")) {
                        CommonUtils.closeStage(btnClose);
                    }
                    break;
                default:
                    ShowMessageFX.Warning("Unknown button action.", pxeModuleName, null);
                    break;
            }
            initButton(pnEditMode);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    ChangeListener<Boolean> txtMaster_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                try {
                    switch (lsID) {
                        case "tfDVNo":
                            break;
                        case "tfCheckNo":
                            /* 1. Check duplicates ­-- but skip if it’s the same number that was already stored on this very transaction      */
                            // Only bother calling the DB if the user actually changed the value                               
                            //Allow editing
                            if (!lsValue.equals(originalCheckNo)) {
                                poJSON = poController.existCheckNo(lsValue);
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                    tfCheckNo.setText(poController.CheckPayments().getModel().getCheckNo());
                                    break;
                                } else {
                                    poController.CheckPayments().getModel().setCheckNo(lsValue);
                                }
                            }
                            loadRecordMaster();
                            break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            });

    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "taRemarks":
                        poJSON = poController.Master().setRemarks(lsValue);
                        if (!JFXUtil.isJSONSuccess(poJSON)) {
                            ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                        }
                        loadRecordMaster();
                        break;
                }
            });
    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");
        Object source = event.getSource();
        if (source instanceof DatePicker) {
            DatePicker datePicker = (DatePicker) source;
            String inputText = datePicker.getEditor().getText();
            SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
            String lsSelectedDate = "";
            LocalDate selectedDate = null;

            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
            if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                return;
            }
            switch (datePicker.getId()) {
                case "dpCheckDate":
                    poJSON = poController.CheckPayments().getModel().setCheckDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                    if (!JFXUtil.isJSONSuccess(poJSON)) {
                        ShowMessageFX.Information(null, pxeModuleName, JFXUtil.getJSONMessage(poJSON));
                    }
                    loadRecordMaster();
                    break;
                default:
                    break;
            }
        }
    }

    private void loadRecordMaster() {
        JFXUtil.setDisabled(false, dpCheckDate);
        tfDVNo.setText(poController.Master().getTransactionNo());
        tfCheckNo.setText(poController.CheckPayments().getModel().getCheckNo());
        dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(SQLUtil.dateFormat(poController.CheckPayments().getModel().getCheckDate(), SQLUtil.FORMAT_SHORT_DATE)));
        tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getTransactionTotal(), true));
        taRemarks.setText(poController.Master().getRemarks());
        chbkApplyToAll.setSelected(poController.Master().getBankPrint().equals("1") ? true : false);
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnAssign, btnPrintCheck, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    private void initTextFields() {
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfDVNo, tfCheckNo);

        JFXUtil.setCommaFormatter(tfCheckAmount);
        JFXUtil.inputIntegersOnly(tfCheckNo);
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpCheckDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpCheckDate);
    }

    private void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apMaster);
        JFXUtil.setButtonsVisibility(lbShow, btnAssign);
    }

    private void assignAndProceed() {
        try {
            poJSON = poController.SaveTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                isAutoProcessing = false;
                return;
            }

            /* ---------- 2. Decide whether to continue ---------- */
            if (chbkApplyToAll.isSelected()) {          // UPDATE THEM ALL
                currentTransactionIndex++;
                if (currentTransactionIndex < transactionNos.size()) {
                    loadTransaction(currentTransactionIndex);   // load next
                    assignAndProceed();                         // recurse
                } else {
                    ShowMessageFX.Information(null, pxeModuleName, "All transactions have been assigned.");
                    CommonUtils.closeStage(btnClose);
                }
            } else {                                       // ONLY THIS ONE
                ShowMessageFX.Information(null, pxeModuleName, "Transaction has been assigned.");
                Platform.runLater(() -> {
                    CommonUtils.closeStage(btnClose);
                });
            }

        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Warning(null, pxeModuleName, "Unexpected error during assignment.");
            isAutoProcessing = false;
        }
    }

    private void loadTransaction(int index) {
        try {
            if (index >= transactionNos.size()) {
                ShowMessageFX.Information(null, pxeModuleName, "All transactions have been processed.");
                CommonUtils.closeStage(btnClose);
                return;
            }

            psTransactionNo = transactionNos.get(index);
            poJSON = poController.OpenTransaction(psTransactionNo);
            if (!"success".equals((String) poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                loadTransaction(++currentTransactionIndex);
                return;
            }
            poJSON = poController.UpdateTransaction();
            if (!"error".equals((String) poJSON.get("result"))) {
                poJSON = poController.populateCheckNo();
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                    return;
                }
                loadRecordMaster();
                initTextFields();
                pnEditMode = poController.getEditMode();
                initButton(pnEditMode);
            } else {
                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                CommonUtils.closeStage(btnClose);
            }
        } catch (SQLException | GuanzonException | CloneNotSupportedException | ScriptException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

}
