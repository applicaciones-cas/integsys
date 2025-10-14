package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.ENTER;
import javafx.scene.input.KeyEvent;
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
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class APPaymentAdjustment_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashflowControllers poAPPaymentAdjustmentController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private boolean isGeneral = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private String psTransactionNo = "";

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private TextField tfSearchSupplier, tfSearchReferenceNo, tfSearchCompany, tfTransactionNo, tfClient, tfIssuedTo, tfCreditAmount, tfDebitAmount, tfReferenceNo, tfCompany;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private TextArea taRemarks;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        psIndustryId = ""; // general
        poJSON = new JSONObject();
        poAPPaymentAdjustmentController = new CashflowControllers(oApp, null);
        poAPPaymentAdjustmentController.APPaymentAdjustment().initialize(); // Initialize transaction
        initTextFields();
        initDatePickers();
        clearTextFields();
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().setIndustryId(psIndustryId);
            poAPPaymentAdjustmentController.APPaymentAdjustment().setIndustryId(psIndustryId);
            poAPPaymentAdjustmentController.APPaymentAdjustment().setWithUI(true);
            loadRecordSearch();
        });
    }

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
//        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            switch (event.getCode()) {
                case F3:
                    switch (lsID) {
                        case "tfSearchCompany":
                            poJSON = poAPPaymentAdjustmentController.APPaymentAdjustment().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchCompany.setText("");
                                psCompanyId = "";
                                break;
                            } else {
                                psCompanyId = poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getCompanyId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchSupplier":
                            poJSON = poAPPaymentAdjustmentController.APPaymentAdjustment().SearchClient(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSupplierId = "";
                                break;
                            } else {
                                psSupplierId = poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getClientId();
                            }
                            loadRecordSearch();
                            return;
                        case "tfSearchReferenceNo":
                            poAPPaymentAdjustmentController.APPaymentAdjustment().setRecordStatus(APPaymentAdjustmentStatus.OPEN
                                    + "" + APPaymentAdjustmentStatus.CONFIRMED
                                    + "" + APPaymentAdjustmentStatus.PAID
                                    + "" + APPaymentAdjustmentStatus.VOID
                                    + "" + APPaymentAdjustmentStatus.CANCELLED);
                            poJSON = poAPPaymentAdjustmentController.APPaymentAdjustment().searchTransaction(psIndustryId, tfSearchCompany.getText(),
                                    tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchReferenceNo.setText("");
                                return;
                            } else {
//                                psSupplierId = poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getClientId();
                                pnEditMode = poAPPaymentAdjustmentController.APPaymentAdjustment().getEditMode();
                                loadRecordMaster();
                                initButton(pnEditMode);
                            }
                            loadRecordSearch();
                            return;
                    }
                    break;
                default:
                    break;
            }

            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(txtField);
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
            }
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRecordSearch() {
        try {
            if (poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Industry().getDescription() != null && !"".equals(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Industry().getDescription())) {
                lblSource.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
            tfSearchSupplier.setText(psSupplierId.equals("") ? "" : poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Supplier().getCompanyName());
            tfSearchCompany.setText(psCompanyId.equals("") ? "" : poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Company().getCompanyName());
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    final ChangeListener<? super Boolean> txtMaster_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = txtPersonalInfo.getId();
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /* Lost Focus */
            switch (lsTxtFieldID) {
                case "tfSearchSupplier":
                    if (lsValue.equals("")) {
                        psSupplierId = "";
                    }
                    loadRecordSearch();
                    break;
                case "tfSearchCompany":
                    if (lsValue.equals("")) {
                        psCompanyId = "";
                    }
                    loadRecordSearch();
                    break;
                case "tfSearchReferenceNo":
                    break;
            }
        }
    };

    public void initTextFields() {
        JFXUtil.setCommaFormatter(tfDebitAmount, tfCreditAmount);
        JFXUtil.setFocusListener(txtMaster_Focus, tfSearchCompany, tfSearchSupplier);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
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
            tfTransactionNo.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getTransactionNo());
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getTransactionStatus();
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
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfClient.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Supplier().getCompanyName());
            taRemarks.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getRemarks());
            tfIssuedTo.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Payee().getPayeeName());
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getCreditAmount().doubleValue(), true));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getDebitAmount().doubleValue(), true));
            tfReferenceNo.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getReferenceNo());
            tfCompany.setText(poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().Company().getCompanyName());

        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
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
                    case "btnBrowse":
                        poAPPaymentAdjustmentController.APPaymentAdjustment().setRecordStatus(APPaymentAdjustmentStatus.OPEN
                                + "" + APPaymentAdjustmentStatus.CONFIRMED
                                + "" + APPaymentAdjustmentStatus.PAID
                                + "" + APPaymentAdjustmentStatus.VOID
                                + "" + APPaymentAdjustmentStatus.CANCELLED);
                        poJSON = poAPPaymentAdjustmentController.APPaymentAdjustment().searchTransaction(psIndustryId, tfSearchCompany.getText(),
                                tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poAPPaymentAdjustmentController.APPaymentAdjustment().getEditMode();
                        psSupplierId = poAPPaymentAdjustmentController.APPaymentAdjustment().getModel().getClientId();
                        break;
                    case "btnHistory":
                        break;
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
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
    }

}
