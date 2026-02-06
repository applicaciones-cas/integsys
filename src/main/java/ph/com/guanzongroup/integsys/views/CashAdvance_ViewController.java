/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class CashAdvance_ViewController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    private int pnDetail = 0;
    private int pnDetailBIR = 0;
    private int pnAttachment = 0;
    private final String pxeModuleName = "Cash Advance View";
    private CashAdvance poController;
    public int pnEditMode;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psTransactionNo = "";

    @FXML
    private AnchorPane AnchorMain, apButton, apMaster;
    @FXML
    private StackPane StackPane;
    @FXML
    private Button btnHistory, btnClose;
    @FXML
    private HBox hboxid;
    @FXML
    private Label lblStatus;
    @FXML
    private TextField tfTransactionNo, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance, tfPettyCash;
    @FXML
    private DatePicker dpAdvanceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbOtherPayee, cbOtherCreditedTo;

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

    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }

    public void setCashAdvance(CashAdvance foValue) {
        poController = foValue;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!psTransactionNo.isEmpty()) {
            try {
                poController = new CashflowControllers(oApp, null).CashAdvance();
                poJSON = new JSONObject();
                initButtonsClickActions();
                initDatePicker();
                clearTextFields();
                btnClose.setOnAction(this::cmdButton_Click);
                poController.InitTransaction(); // Initialize transaction
//                if (!"success".equals((String) poJSON.get("result"))) {
//                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                    CommonUtils.closeStage(btnClose);
//                }
                poJSON = poController.OpenTransaction(psTransactionNo);
                if (!"error".equals((String) poJSON.get("result"))) {
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                    loadRecordMaster();
                } else {
                    Platform.runLater(() -> {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        CommonUtils.closeStage(btnClose);
                    });
                }
            } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
            }
        }
    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnHistory, btnClose);
        buttons.forEach(button -> button.setOnAction(this::cmdButton_Click));
    }

    @FXML
    private void cmdCheckBox_Click(ActionEvent event) {

    }

    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnHistory":
                if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                    ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                    return;
                }

                try {
                    poController.ShowStatusHistory();
                } catch (NullPointerException npe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                    ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                }
                break;
            case "btnClose":
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this Tab?")) {
                    CommonUtils.closeStage(btnClose);
                } else {
                    return;
                }
                break;
            default:
                ShowMessageFX.Warning(null, pxeModuleName, "Button is not registered");
                break;
        }
        if (JFXUtil.isObjectEqualTo(lsButton, "btnArrowRight", "btnArrowLeft", "btnHistory")) {
        } else {
            loadRecordMaster();
            initButton(pnEditMode);
        }
    }

    public void loadRecordMaster() {
        try {
            lblStatus.setText(poController.getStatus(poController.Master().getTransactionStatus()).toUpperCase());
            tfTransactionNo.setText(poController.Master().getTransactionNo());

            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.Master().getTransactionDate());
            dpAdvanceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfVoucherNo.setText(poController.Master().getVoucher());
            tfPettyCash.setText(poController.Master().PettyCash().getPettyCashDescription());
            tfPayee.setText(poController.Master().getPayeeName());
            tfRequestingDepartment.setText(poController.Master().Department().getDescription());
            tfAmountToAdvance.setText("");
            taRemarks.setText(poController.Master().getRemarks());
            tfAmountToAdvance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getAdvanceAmount().doubleValue(), true));
            boolean lbPayeeOthers = (poController.Master().getClientId() == null || "".equals(poController.Master().getClientId()))
                    && poController.Master().getPayeeName() != null && !"".equals(poController.Master().getPayeeName());
            cbOtherPayee.setSelected(lbPayeeOthers);
            if (poController.Master().CreditedToOthers().getPayeeName() != null && !"".equals(poController.Master().CreditedToOthers().getPayeeName())) {
                tfCreditedTo.setText(poController.Master().CreditedToOthers().getPayeeName());
                cbOtherCreditedTo.setSelected(true);
            } else {
                tfCreditedTo.setText(poController.Master().Credited().getCompanyName());
                cbOtherCreditedTo.setSelected(false);
            }
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initDatePicker() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAdvanceDate);
    }

    private void initButton(int fnEditMode) {
        JFXUtil.setDisabled(true, apMaster);
    }

    private void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
    }
}
