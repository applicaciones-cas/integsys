package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
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
import ph.com.guanzongroup.cas.cashflow.CheckPrinting;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.CheckStatus;
import ph.com.guanzongroup.cas.cashflow.status.DisbursementStatic;

public class CheckAssignmentController implements Initializable {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private final String pxeModuleName = "Check Assignment";
    private CheckPrinting poCheckPrintingController;

    public int pnEditMode;
    private String psTransactionNo = "";
    private int currentTransactionIndex = 0;
    private boolean isAutoProcessing = false;
    private long startingCheckNo = -1;
    private int checkNoLength = 6;
    private String originalCheckNo = "";

    @FXML
    private AnchorPane AnchorMain, apMaster;
    @FXML
    private StackPane StackPane;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnAssign, btnPrintCheck, btnClose;
    @FXML
    private AnchorPane AnchorInputs;
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

    public void setCheckPrinting(CheckPrinting foValue) {
        poCheckPrintingController = foValue;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CustomCommonUtil.setDropShadow(AnchorMain, StackPane);

        if (transactionNos != null && !transactionNos.isEmpty()) {
            try {

                poCheckPrintingController = new CashflowControllers(oApp, null).CheckPrinting();
                poJSON = poCheckPrintingController.InitTransaction();

                if (!"success".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }

                loadTransaction(currentTransactionIndex);
            } catch (Exception ex) {
                Logger.getLogger(CheckAssignmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void loadTransaction(int index) throws SQLException, GuanzonException, CloneNotSupportedException {
        if (index >= transactionNos.size()) {
            ShowMessageFX.Information(null, pxeModuleName, "All transactions have been processed.");
            CommonUtils.closeStage(btnClose);
            return;
        }

        psTransactionNo = transactionNos.get(index);
        poJSON = poCheckPrintingController.OpenTransaction(psTransactionNo);

        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            loadTransaction(++currentTransactionIndex);
            return;
        }

        poJSON = poCheckPrintingController.UpdateTransaction();
        

        if (!"error".equals((String) poJSON.get("result"))) {
            if (poCheckPrintingController.Master().getDisbursementType().equals(DisbursementStatic.DisbursementType.CHECK)) {
                poCheckPrintingController.setCheckpayment();
//                poCheckPrintingController.setBankAccountLedger();
                poCheckPrintingController.setBankAccountCheckNo();

            }
            initAll();
            loadRecordMaster();
        } else {
            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
            CommonUtils.closeStage(btnClose);
        }
    }

    private void assignAndProceed() {
        try {
            String checkNoToCheck = tfCheckNo.getText();
            String currentAssignedCheckNo = poCheckPrintingController.Master().CheckPayments().getCheckNo();

            // Only check for duplicates if the check no has changed (doesn't match the current assigned)
            if (!checkNoToCheck.equals(currentAssignedCheckNo)) {
                poJSON = poCheckPrintingController.checkNoExists(checkNoToCheck);
                if("error".equals((String)poJSON.get("result"))){
                    ShowMessageFX.Warning(null, pxeModuleName,
                        (String) poJSON.get("message"));
                    return;
                }
            }

           
            poCheckPrintingController.Master().CheckPayments().setModifiedDate(oApp.getServerDate());
            poCheckPrintingController.Master().setModifiedDate(oApp.getServerDate());
            /* ---------- 1. Save the CURRENT record ---------- */
             poCheckPrintingController.Master().setModifiedDate(oApp.getServerDate());
             poCheckPrintingController.BankAccountMaster().getModel().setCheckNo(checkNoToCheck);
//            poJSON = poCheckPrintingController.saveBankAccountMaster();
            poJSON = poCheckPrintingController.SaveTransaction();
            

            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning(null, pxeModuleName,
                        (String) poJSON.get("message"));
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
                    ShowMessageFX.Information(null, pxeModuleName,
                            "All transactions have been assigned.");
                    CommonUtils.closeStage(btnClose);
                }
            } else {                                       // ONLY THIS ONE
                ShowMessageFX.Information(null, pxeModuleName,
                        "Transaction has been assigned.");
                CommonUtils.closeStage(btnClose);
            }

        } catch (Exception ex) {
            Logger.getLogger(CheckAssignmentController.class.getName())
                    .log(Level.SEVERE, null, ex);
            ShowMessageFX.Warning("Unexpected error during assignment.",
                    pxeModuleName, null);
            isAutoProcessing = false;
        }
    }

    private void loadRecordMaster() throws SQLException, GuanzonException {
        String initialCheckNo = "";
        boolean fromBankAccount = false;

        tfDVNo.setText(poCheckPrintingController.Master().getTransactionNo());
        if (transactionNos.size() > 1) {
            chbkApplyToAll.setSelected(true);
        }
        if (poCheckPrintingController.Master().CheckPayments().getCheckNo().isEmpty()) {
            initialCheckNo = poCheckPrintingController.BankAccountMaster().getModel().getCheckNo();
            fromBankAccount = true;
        } else {
            initialCheckNo = poCheckPrintingController.Master().CheckPayments().getCheckNo();
        }

        if (fromBankAccount && initialCheckNo.matches("\\d+")) {
            long incremented = Long.parseLong(initialCheckNo) + 1;
            initialCheckNo = String.format("%0" + initialCheckNo.length() + "d", incremented);
        }
        originalCheckNo = initialCheckNo;
        tfCheckNo.setText(initialCheckNo);
        String checkNoValue = tfCheckNo.getText();
        if (currentTransactionIndex == 0 && startingCheckNo == -1 && checkNoValue != null && checkNoValue.matches("\\d+")) {
            startingCheckNo = Long.parseLong(checkNoValue);
            checkNoLength = checkNoValue.length();
            poCheckPrintingController.CheckPayments().getModel().setCheckNo(checkNoValue);
            poCheckPrintingController.BankAccountMaster().getModel().setCheckNo(checkNoValue);
//             poCheckPrintingController.BankAccountLedger().getModel().setSourceNo(checkNoValue);
            poCheckPrintingController.CheckPayments().getModel().setTransactionStatus(CheckStatus.OPEN);
        } else if (startingCheckNo != -1) {
            long currentCheckNo = startingCheckNo + currentTransactionIndex;
            String formatted = String.format("%0" + checkNoLength + "d", currentCheckNo);
            tfCheckNo.setText(formatted);
            poCheckPrintingController.CheckPayments().getModel().setCheckNo(formatted);
            poCheckPrintingController.CheckPayments().getModel().setTransactionStatus(CheckStatus.OPEN);
            poCheckPrintingController.BankAccountMaster().getModel().setCheckNo(formatted);
//            poCheckPrintingController.BankAccountLedger().getModel().setSourceNo(formatted);
            
        }

        dpCheckDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                SQLUtil.dateFormat(poCheckPrintingController.Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)
        ));

        tfCheckAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(
                poCheckPrintingController.Master().getTransactionTotal(), true
        ));

        taRemarks.setText(poCheckPrintingController.Master().getRemarks());

    }

    private void initAll() {
        initButtonsClickActions();
        initTextAreaFields();
        initTextFields();
        initCheckBox();
        initDatePicker();
        clearFields();
        pnEditMode = poCheckPrintingController.getEditMode();
        initFields(pnEditMode);
        initButton(pnEditMode);
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnAssign, btnPrintCheck, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
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
//                    System.out.println("EDIT MODE SA BTN PRINT : " + poCheckPrintingController.Master().CheckPayments().getEditMode());
//                    poCheckPrintingController.Master().CheckPayments().setPrint("1");
//                    poCheckPrintingController.Master().CheckPayments().setDatePrint(oApp.getServerDate());
                    
                    poCheckPrintingController.PrintCheck(transactionNos);
                    if (!isAutoProcessing) {
                        isAutoProcessing = true;
                        assignAndProceed();
                    }
                    CommonUtils.closeStage(btnClose);
//                    poJSON = poCheckPrintingController.SaveTransaction();
//                    if("error".equals(poJSON.get("result"))) {
//                        System.out.println("Message : " + poJSON.get("message"));
//                        break;
//                    }
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

    private void initTextAreaFields() {
        taRemarks.focusedProperty().addListener(txtArea_Focus);
        taRemarks.setOnKeyPressed(this::txtArea_KeyPressed);
    }

    private void initTextFields() {
        tfCheckNo.focusedProperty().addListener(txtField_Focus);
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = txtField.getId();
        String lsValue = txtField.getText();
        try {
            if (!nv && "tfCheckNo".equals(lsID)) {
                if (lsValue != null && lsValue.matches("\\d+")) {

                    /* 1. Check duplicates ­-- but skip if it’s the same number
              that was already stored on this very transaction      */
                    boolean exists = false;

                    // Only bother calling the DB if the user actually changed the value
                    if (!lsValue.equals(originalCheckNo)) {
                        poJSON = poCheckPrintingController.checkNoExists(lsValue);
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                    }

                    /* 2. Parse and apply values (unchanged) */
                    long inputCheckNo = Long.parseLong(lsValue);
                    String masterVal = poCheckPrintingController.BankAccountMaster()
                            .getModel()
                            .getCheckNo();
                    long masterCheckNo = (masterVal != null && masterVal.matches("\\d+"))
                            ? Long.parseLong(masterVal) : 0;

                    startingCheckNo = inputCheckNo - currentTransactionIndex;
                    checkNoLength = lsValue.length();

                    poCheckPrintingController.CheckPayments()
                            .getModel()
                            .setCheckNo(lsValue);

                    /* 3. Update master only if higher or equal (unchanged) */
                    if (inputCheckNo >= masterCheckNo) {
                        poCheckPrintingController.BankAccountMaster()
                                .getModel()
                                .setCheckNo(lsValue);
                        

                    }
                    poCheckPrintingController.CheckPayments().getModel().setTransactionStatus(CheckStatus.OPEN);
                } else {
                    ShowMessageFX.Warning(null, pxeModuleName,
                            "Invalid check number format.");
                    tfCheckNo.requestFocus();
                }
            } else if (nv) {
                txtField.selectAll();
            }

        } catch (Exception ex) {
            Logger.getLogger(CheckAssignmentController.class.getName())
                    .log(Level.SEVERE, null, ex);
            ShowMessageFX.Warning(null, pxeModuleName,
                    "Error checking check number.");
            return;
        }

    };

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtArea = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = txtArea.getId();
        String lsValue = txtArea.getText();
        if (!nv && lsID.equals("taDVRemarks")) {
            poCheckPrintingController.Master().setRemarks(lsValue);
        } else if (nv) {
            txtArea.selectAll();
        }
    };

    private void txtArea_KeyPressed(KeyEvent event) {
        TextArea txtArea = (TextArea) event.getSource();
        switch (event.getCode()) {
            case TAB:
            case ENTER:
            case DOWN:
                CommonUtils.SetNextFocus(txtArea);
                event.consume();
                break;
            case UP:
                CommonUtils.SetPreviousFocus(txtArea);
                event.consume();
                break;
            default:
                break;
        }
    }

    private void initCheckBox() {
//        chbkApplyToAll.setOnAction(event -> {
//            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
//                poCheckPrintingController.Master().setBankPrint(chbkApplyToAll.isSelected() ? "1" : "0");
//            }
//        });
    }

    private void initDatePicker() {
        dpCheckDate.setOnAction(e -> {
            // optional: add logic if needed
        });
    }

    private void clearFields() {
        JFXUtil.setValueToNull(null, dpCheckDate);
        JFXUtil.clearTextFields(apMaster);
        CustomCommonUtil.setSelected(false, chbkApplyToAll);
    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.UPDATE);
        JFXUtil.setDisabled(!lbShow, apMaster);
    }

    private void initButton(int fnEditMode) {
        boolean lbShow = (pnEditMode == EditMode.UPDATE);
        JFXUtil.setButtonsVisibility(lbShow, btnAssign);
    }

}
