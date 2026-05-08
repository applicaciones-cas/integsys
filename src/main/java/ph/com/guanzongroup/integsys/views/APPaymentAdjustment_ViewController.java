package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import ph.com.guanzongroup.cas.cashflow.status.APPaymentAdjustmentStatus;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.APPaymentAdjustment;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Team 1
 */
public class APPaymentAdjustment_ViewController implements Initializable {

    private GRiderCAS oApp;
    static APPaymentAdjustment poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psTransactionNo = "";
    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnHistory, btnClose;
    @FXML
    private TextField tfTransactionNo, tfReferenceNo, tfCompany, tfClient, tfIssuedTo, tfCreditAmount, tfDebitAmount;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private TextArea taRemarks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).APPaymentAdjustment();
            poController.initialize(); // Initialize transaction
            initTextFields();
            initDatePickers();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
                try {
                    poJSON = poController.OpenTransaction(psTransactionNo);
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        CommonUtils.closeStage(btnClose);
                    }
                    pnEditMode = poController.getEditMode();
                    initButton(pnEditMode);
                    loadRecordSearch();
                    loadRecordMaster();
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                }
            });
            loadRecordMaster();
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setTransaction(String fsValue) {
        psTransactionNo = fsValue;
    }

    public void loadRecordSearch() {
        try {
            if (poController.getModel().Industry().getDescription() != null && !"".equals(poController.getModel().Industry().getDescription())) {
                lblSource.setText(poController.getModel().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void initTextFields() {
        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate);
    }

    public void clearTextFields() {
        dpTransactionDate.setValue(null);
        JFXUtil.clearTextFields(apMaster);
    }

    public void loadRecordMaster() {
        try {
            tfTransactionNo.setText(poController.getModel().getTransactionNo());
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poController.getModel().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(APPaymentAdjustmentStatus.OPEN, "OPEN");
                statusMap.put(APPaymentAdjustmentStatus.PAID, "PAID");
                statusMap.put(APPaymentAdjustmentStatus.CONFIRMED, "CONFIRMED");
                statusMap.put(APPaymentAdjustmentStatus.RETURNED, "RETURNED");
                statusMap.put(APPaymentAdjustmentStatus.VOID, "VOIDED");
                statusMap.put(APPaymentAdjustmentStatus.CANCELLED, "CANCELLED");

                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN");
                lblStatus.setText(lsStat);
            });
            // Transaction Date
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poController.getModel().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfClient.setText(poController.getModel().Supplier().getCompanyName());
            taRemarks.setText(poController.getModel().getRemarks());
            tfIssuedTo.setText(poController.getModel().Payee().getPayeeName());
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getCreditAmount().doubleValue(), true));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.getModel().getDebitAmount().doubleValue(), true));
            tfReferenceNo.setText(poController.getModel().getReferenceNo());
            tfCompany.setText(poController.getModel().Company().getCompanyName());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnHistory":
                        if (pnEditMode != EditMode.READY && pnEditMode != EditMode.UPDATE) {
                            ShowMessageFX.Warning("No transaction status history to load!", pxeModuleName, null);
                            return;
                        }

                        try {
                            poController.ShowStatusHistory();
                            return;
                        } catch (NullPointerException npe) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(npe), npe);
                            ShowMessageFX.Error("No transaction status history to load!", pxeModuleName, null);
                        } catch (Exception ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                            ShowMessageFX.Error(MiscUtil.getException(ex), pxeModuleName, null);
                        }
                        break;
                    case "btnClose":
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to close this dialog?")) {
                            CommonUtils.closeStage(btnClose);
                        } else {
                            return;
                        }
                        break;
                    default:
                        break;
                }

                loadRecordMaster();
                initButton(pnEditMode);
            }
        } catch (Exception e) {
        }
    }

    private void initButton(int fnValue) {
        //Unkown || Ready
        JFXUtil.setDisabled(true, apMaster);
        JFXUtil.setButtonsVisibility(true, btnClose);
        JFXUtil.setButtonsVisibility(fnValue == EditMode.READY, btnHistory);
    }
}
