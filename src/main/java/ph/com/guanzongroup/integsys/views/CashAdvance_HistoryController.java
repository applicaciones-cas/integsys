package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.cashflow.CashAdvance;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 *
 * @author Team 1 : Aldrich & Arsiela 02032026
 */
public class CashAdvance_HistoryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashAdvance poController;
    private JSONObject poJSON;
    public int pnEditMode;
    private String pxeModuleName = JFXUtil.getFormattedClassTitle(this.getClass());
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psSearchVoucherNo = "";

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private TextField tfSearchIndustry, tfSearchPayee, tfSearchVoucherNo, tfTransactionNo, tfVoucherNo, tfPayee, tfCreditedTo, tfRequestingDepartment, tfAmountToAdvance, tfPettyCash;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Button btnBrowse, btnHistory, btnClose;
    @FXML
    private DatePicker dpAdvanceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private CheckBox cbOtherPayee, cbOtherCreditedTo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            //        psIndustryId = ""; // general
            poJSON = new JSONObject();
            poController = new CashflowControllers(oApp, null).CashAdvance();
            poController.InitTransaction(); // Initialize transaction
            initTextFields();
            initDatePickers();
            clearTextFields();
            pnEditMode = EditMode.UNKNOWN;
            initButton(pnEditMode);

            Platform.runLater(() -> {
                poController.Master().setIndustryId(psIndustryId);
                poController.Master().setCompanyId(psCompanyId);
                poController.setIndustryId(psIndustryId);
                poController.setCompanyId(psCompanyId);
                poController.setWithUI(true);
                loadRecordSearch();
            });
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
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
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        // No Category
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
                        case "tfSearchIndustry":
                            poJSON = poController.SearchIndustry(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchIndustry.setText("");
                                break;
                            }
                            loadRecordSearch();
                            return;
//                        case "tfSearchPayee":
//                            poJSON = poController.SearchPayee(lsValue, false, true);
//                            if ("error".equals(poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                tfSearchPayee.setText("");
//                                break;
//                            }
//                            loadRecordSearch();
//                            return;
                        case "tfSearchPayee":
                        case "tfSearchVoucherNo":
                            poJSON = poController.searchTransaction(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchVoucherNo.getText());
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchVoucherNo.setText("");
                                return;
                            } else {
                                pnEditMode = poController.getEditMode();
                                psSearchVoucherNo = poController.Master().getTransactionNo();
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
        } catch (GuanzonException | SQLException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    public void loadRecordSearch() {
        try {
            poController.Master().setCompanyId(psCompanyId);
            if (poController.Master().Company().getCompanyName() != null && !"".equals(poController.Master().Company().getCompanyName())) {
                lblSource.setText(poController.Master().Company().getCompanyName());
            } else {
                lblSource.setText("");
            }
            tfSearchIndustry.setText(poController.getSearchIndustry());
            tfSearchPayee.setText(poController.getSearchPayee());
            tfSearchVoucherNo.setText(psSearchVoucherNo);
            JFXUtil.updateCaretPositions(apBrowse);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    ChangeListener<Boolean> txtBrowse_Focus = JFXUtil.FocusListener(TextField.class,
            (lsID, lsValue) -> {
                switch (lsID) {
                    case "tfSearchIndustry":
                        if (lsValue.isEmpty()) {
                            poController.setSearchIndustry("");
                        }
                        break;
                    case "tfSearchPayee":
                        if (lsValue.isEmpty()) {
                            poController.setSearchPayee("");
                        }
                        break;
                    case "tfSearchVoucherNo":
                        if (lsValue.isEmpty()) {
                            psSearchVoucherNo = "";
                        }
                        break;
                }
                loadRecordSearch();
            });

    public void initTextFields() {
        JFXUtil.setFocusListener(txtBrowse_Focus, tfSearchIndustry, tfSearchPayee, tfSearchVoucherNo);
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpAdvanceDate);
    }

    public void clearTextFields() {
        JFXUtil.clearTextFields(apMaster);
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
            taRemarks.setText(poController.Master().getRemarks());
            tfAmountToAdvance.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poController.Master().getAdvanceAmount().doubleValue(), true));
            boolean lbPayeeOthers = (poController.Master().getClientId() == null || "".equals(poController.Master().getClientId())) 
                                    && poController.Master().getPayeeName() != null && !"".equals(poController.Master().getPayeeName());
            cbOtherPayee.setSelected(lbPayeeOthers);
            if(poController.Master().CreditedToOthers().getPayeeName() != null && !"".equals(poController.Master().CreditedToOthers().getPayeeName())){
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
                        poJSON = poController.searchTransaction(tfSearchIndustry.getText(), tfSearchPayee.getText(), tfSearchVoucherNo.getText());
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poController.getEditMode();
                        break;
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
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            ShowMessageFX.Error(null, pxeModuleName, MiscUtil.getException(ex));
        }
    }

    private void initButton(int fnValue) {
        //Unkown || Ready
        JFXUtil.setDisabled(true, apMaster);
        JFXUtil.setButtonsVisibility(true, btnClose);
        JFXUtil.setButtonsVisibility(fnValue == EditMode.READY, btnHistory);
    }
}
