package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelSOATagging_Detail;
import ph.com.guanzongroup.integsys.model.ModelSOATagging_Main;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatus;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import javafx.animation.PauseTransition;
import ph.com.guanzongroup.cas.cashflow.SOATagging;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;

/**
 * FXML Controller class
 *
 * @author User
 */
public class SOATagging_ConfirmationController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashflowControllers poSOATaggingController;
    private JSONObject poJSON;
    public int pnEditMode;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String pxeModuleName = "";
    private String psIndustryId = "";
    private boolean isGeneral = false;
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";
    private boolean pbEntered = false;
    private String psSearchCompanyId = "";
    private String psSearchSupplierId = "";
    private ObservableList<ModelSOATagging_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelSOATagging_Detail> details_data = FXCollections.observableArrayList();

    private FilteredList<ModelSOATagging_Main> filteredData;
    private FilteredList<ModelSOATagging_Detail> filteredDataDetail;

    private final Map<String, List<String>> highlightedRowsMain = new HashMap<>();
    AtomicReference<Object> lastFocusedTextField = new AtomicReference<>();
    AtomicReference<Object> previousSearchedTextField = new AtomicReference<>();

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail, apMainList;
    @FXML
    private HBox hbButtons, hboxid;
    @FXML
    private Label lblSource, lblStatus;
    @FXML
    private Button btnUpdate, btnSearch, btnSave, btnCancel, btnConfirm, btnVoid, btnReturn, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfSearchCompany, tfSearchReferenceNo, tfSearchSupplier, tfTransactionNo, tfSOANo, tfClient, tfIssuedTo, tfTransactionTotal,
            tfVatAmount, tfNonVatSales, tfZeroVatSales, tfVatExemptSales, tfNetTotal, tfCompany, tfDiscountAmount, tfFreight, tfSourceNo, tfSourceCode, tfReferenceNo, tfCreditAmount, tfDebitAmount, tfAppliedAmtDetail;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetailList, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblSourceNoDetail, tblSourceCodeDetail, tblReferenceNoDetail, tblCreditAmtDetail, tblDebitAmtDetail,
            tblAppliedAmtDetail, tblRowNo, tblSupplier, tblDate, tblReferenceNo;
    @FXML
    private Pagination pgPagination;

    public void setTabTitle(String lsTabTitle, boolean isGeneral) {
        this.pxeModuleName = lsTabTitle;
        this.isGeneral = isGeneral;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        psIndustryId = isGeneral ? "" : psIndustryId;

        poSOATaggingController = new CashflowControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poSOATaggingController.SOATagging().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }

        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();

        Platform.runLater(() -> {
            poSOATaggingController.SOATagging().Master().setIndustryId(psIndustryId);
            poSOATaggingController.SOATagging().setIndustryId(psIndustryId);
            poSOATaggingController.SOATagging().setCompanyId(psCompanyId);
            poSOATaggingController.SOATagging().initFields();
            poSOATaggingController.SOATagging().setWithUI(true);
            poSOATaggingController.SOATagging().validatePayment(true);
            loadRecordSearch();
        });

        pgPagination.setPageCount(1);

        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);

        JFXUtil.initKeyClickObject(apMainAnchor, lastFocusedTextField, previousSearchedTextField);
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
        psCategoryId = fsValue;
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        poJSON = new JSONObject();
        String tabText = "";

        try {
            Object source = event.getSource();
            if (source instanceof Button) {
                Button clickedButton = (Button) source;
                String lsButton = clickedButton.getId();
                switch (lsButton) {
                    case "btnClose":
                        unloadForm appUnload = new unloadForm();
                        if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                            appUnload.unloadForm(apMainAnchor, oApp, pxeModuleName);
                        } else {
                            return;
                        }
                        break;
                    case "btnUpdate":
                        poJSON = poSOATaggingController.SOATagging().OpenTransaction(poSOATaggingController.SOATagging().Master().getTransactionNo());
                        poJSON = poSOATaggingController.SOATagging().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                        psSupplierId = poSOATaggingController.SOATagging().Master().getClientId();
                        psCompanyId = poSOATaggingController.SOATagging().Master().getCompanyId();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField.get() != null)) {
                            if (lastFocusedTextField.get() instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField.get();
                                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", apBrowse, apMaster, apDetail).contains(tf.getId())) {

                                    if (lastFocusedTextField.get() == previousSearchedTextField.get()) {
                                        break;
                                    }
                                    previousSearchedTextField.set(lastFocusedTextField.get());
                                    // Create a simulated KeyEvent for F3 key press
                                    JFXUtil.makeKeyPressed(tf, KeyCode.F3);
                                } else {
                                    ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                                }
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                            }
                        } else {
                            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
                        }
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrieveSOATagging();
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poSOATaggingController.SOATagging().Master().setClientId(psSupplierId);
                            poSOATaggingController.SOATagging().Master().setCompanyId(psCompanyId);
                            poJSON = poSOATaggingController.SOATagging().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                //poSOATaggingController.SOATagging().AddDetail();
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poSOATaggingController.SOATagging().OpenTransaction(poSOATaggingController.SOATagging().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poSOATaggingController.SOATagging().Master().getTransactionStatus().equals(SOATaggingStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poSOATaggingController.SOATagging().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnConfirm":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Are you sure you want to confirm transaction?") == true) {
                            poJSON = poSOATaggingController.SOATagging().ConfirmTransaction("");
                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#C1E1C1", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnVoid":
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to void transaction?") == true) {
                            switch (poSOATaggingController.SOATagging().Master().getTransactionStatus()) {
                                case SOATaggingStatus.OPEN:
                                    poJSON = poSOATaggingController.SOATagging().VoidTransaction("");
                                    break;
                                case SOATaggingStatus.CONFIRMED:
                                    poJSON = poSOATaggingController.SOATagging().CancelTransaction("");
                                    break;
                            }

                            if ("error".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                return;
                            } else {
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAA0A0", highlightedRowsMain);
                            }
                        } else {
                            return;
                        }
                        break;
                    case "btnReturn":
//                        poJSON = new JSONObject();
//                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to return transaction?") == true) {
//                            poJSON = poSOATaggingController.SOATagging().ReturnTransaction("");
//                            if ("error".equals((String) poJSON.get("result"))) {
//                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
//                                return;
//                            } else {
//                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));
//                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
//                                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnMain + 1), "#FAC898", highlightedRowsMain);
//                            }
//                        } else {
//                            return;
//                        }
                        return;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnSave", "btnConfirm", "btnReturn", "btnVoid", "btnCancel")) {
                    poSOATaggingController.SOATagging().resetMaster();
                    poSOATaggingController.SOATagging().Detail().clear();
                    pnEditMode = EditMode.UNKNOWN;
                    clearTextFields();

                    poSOATaggingController.SOATagging().Master().setIndustryId(psIndustryId);
                }

                if (JFXUtil.isObjectEqualTo(lsButton, "btnRetrieve")) {
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                }
                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null && !poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo().equals("")) {
                        tfAppliedAmtDetail.requestFocus();
                    } else {
                        tfSourceNo.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException | ParseException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void retrieveSOATagging() {
        poJSON = new JSONObject();
        poSOATaggingController.SOATagging().setTransactionStatus(SOATaggingStatus.OPEN
                + SOATaggingStatus.CONFIRMED);
        poJSON = poSOATaggingController.SOATagging().loadSOATagging(psIndustryId, tfSearchCompany.getText(), tfSearchSupplier.getText(), tfSearchReferenceNo.getText());
        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            loadTableMain();
        }

    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtField.getId());
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            /*Lost Focus*/
            lsValue = lsValue.trim();
            switch (lsID) {
                case "taRemarks"://Remarks
                    poJSON = poSOATaggingController.SOATagging().Master().setRemarks(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    break;
            }
        } else {
            txtField.selectAll();
        }
    };

    // Method to handle focus change and track the last focused TextField
    final ChangeListener<? super Boolean> txtDetail_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfAppliedAmtDetail":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount() != null
                            && !"".equals(poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount())) {
                        if (poSOATaggingController.SOATagging().Detail(pnDetail).getTransactionTotal().doubleValue() < Double.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Applied Amount cannot be greater than the transaction total");
                            poSOATaggingController.SOATagging().Detail(pnDetail).setAppliedAmount(0.0000);
                            tfAppliedAmtDetail.requestFocus();
                            break;
                        }
                    }

                    poJSON = poSOATaggingController.SOATagging().Detail(pnDetail).setAppliedAmount((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    if (pbEntered) {
                        moveNext();
                        pbEntered = false;
                    }
                    break;
            }
            Platform.runLater(() -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                delay.setOnFinished(event -> {
                    loadTableDetail();
                });
                delay.play();
            });
        }

    };

    final ChangeListener<? super Boolean> txtMaster_Focus = (o, ov, nv) -> {
        poJSON = new JSONObject();
        TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTxtFieldID = (txtPersonalInfo.getId());
        String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfSearchSupplier":
                    if (lsValue.equals("")) {
                        psSearchSupplierId = "";
                    }
                    loadRecordSearch();
                    break;
                case "tfSearchCompany":
                    if (lsValue.equals("")) {
                        psSearchCompanyId = "";
                    }
                    loadRecordSearch();
                    break;
                case "tfSearchReferenceNo":
                    break;
                case "tfSOANo":
                    if (!lsValue.isEmpty()) {
                        poJSON = poSOATaggingController.SOATagging().Master().setSOANumber(lsValue);
                    } else {
                        poJSON = poSOATaggingController.SOATagging().Master().setSOANumber("");
                    }
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfReferenceNo.setText("");
                        break;
                    }
                    break;
                case "tfCompany":
                    if (lsValue.isEmpty()) {
                        poJSON = poSOATaggingController.SOATagging().Master().setCompanyId("");
                        psCompanyId = "";
                    }
                    break;
                case "tfClient":
                    if (lsValue.isEmpty()) {
                        poJSON = poSOATaggingController.SOATagging().Master().setClientId("");
                        psSupplierId = "";
                    }

                    break;
                case "tfIssuedTo":
                    if (lsValue.isEmpty()) {
                        poJSON = poSOATaggingController.SOATagging().Master().setIssuedTo("");
                    }
                    break;
                case "tfDiscountAmount":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (Double.valueOf(lsValue) > 0.00) {
                        if (poSOATaggingController.SOATagging().Master().getTransactionTotal().doubleValue() < Double.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Discount amount cannot be greater than the transaction total.");
                            poSOATaggingController.SOATagging().Master().setDiscountAmount(0.0000);
                            tfDiscountAmount.setText("0.0000");
                            tfDiscountAmount.requestFocus();
                            break;
                        }
                    }

                    poJSON = poSOATaggingController.SOATagging().Master().setDiscountAmount((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
                case "tfFreight":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    JFXUtil.removeComma(lsValue);
                    if (Double.valueOf(lsValue) > 0.00) {
                        if (poSOATaggingController.SOATagging().Master().getTransactionTotal().doubleValue() < Double.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Freight amount cannot be greater than the transaction total.");
                            poSOATaggingController.SOATagging().Master().setFreightAmount(0.0000);
                            tfFreight.setText("0.0000");
                            tfFreight.requestFocus();
                            break;
                        }
                    }

                    poJSON = poSOATaggingController.SOATagging().Master().setDiscountAmount((Double.valueOf(lsValue)));
                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                    break;
            }
            if (!JFXUtil.isObjectEqualTo(lsTxtFieldID, "tfSearchSupplier", "tfSearchCompany", "tfSearchReferenceNo")) {
                loadRecordMaster();
            }
        }
    };

    public void moveNext() {
        double ldblAppliedAmt = poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount().doubleValue();
        apDetail.requestFocus();
        double ldblNewValue = poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount().doubleValue();
        if (ldblAppliedAmt != ldblNewValue && (ldblAppliedAmt > 0
                && poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null
                && !"".equals(poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo()))) {
            tfAppliedAmtDetail.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewTransDetailList);
            loadRecordDetail();
            if (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null && !poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo().equals("")) {
                tfAppliedAmtDetail.requestFocus();
            } else {
                tfReferenceNo.requestFocus();
            }
        }
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField txtField = (TextField) event.getSource();
            String lsID = (((TextField) event.getSource()).getId());
            String lsValue = (txtField.getText() == null ? "" : txtField.getText());
            poJSON = new JSONObject();
            int lnRow = pnDetail;

            TableView<?> currentTable = tblViewTransDetailList;
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfReferenceNo":
                        case "tfAppliedAmtDetail":
                            double ldblAppliedAmt = poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount().doubleValue();
                            apDetail.requestFocus();
                            double ldblNewValue = poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount().doubleValue();
                            if (ldblAppliedAmt != ldblNewValue && (ldblAppliedAmt > 0
                                    && poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null
                                    && !"".equals(poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo()))) {
                                tfAppliedAmtDetail.requestFocus();
                            } else {
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                loadRecordDetail();
                                if (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null && !poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo().equals("")) {
                                    tfAppliedAmtDetail.requestFocus();
                                } else {
                                    tfReferenceNo.requestFocus();
                                }
                                event.consume();
                            }
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfReferenceNo":
                        case "tfAppliedAmtDetail":
                            moveNext();
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSearchCompany":
                            poJSON = poSOATaggingController.SOATagging().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchCompany.setText("");
                                psSearchCompanyId = "";
                                break;
                            }
                            psSearchCompanyId = poSOATaggingController.SOATagging().Master().getCompanyId();
                            loadRecordSearch();
                            retrieveSOATagging();
                            return;
                        case "tfSearchSupplier":
                            poJSON = poSOATaggingController.SOATagging().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSearchSupplier.setText("");
                                psSearchSupplierId = "";
                                break;
                            }
                            psSearchSupplierId = poSOATaggingController.SOATagging().Master().getClientId();
                            loadRecordSearch();
                            retrieveSOATagging();
                            return;
                        case "tfSearchReferenceNo":
                            retrieveSOATagging();
                            return;
                        case "tfClient":
                            poJSON = poSOATaggingController.SOATagging().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            psSupplierId = poSOATaggingController.SOATagging().Master().getClientId();
                            poSOATaggingController.SOATagging().Master().Supplier().getCompanyName();
                            loadRecordMaster();
                            return;
                        case "tfCompany":
                            poJSON = poSOATaggingController.SOATagging().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            psCompanyId = poSOATaggingController.SOATagging().Master().getCompanyId();
                            loadRecordMaster();
                            return;
                        case "tfIssuedTo":
                            poJSON = poSOATaggingController.SOATagging().SearchPayee(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                break;
                            }
                            loadRecordMaster();
                            return;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        JFXUtil.setJSONSuccess(poJSON, "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null;
                LocalDate selectedDate = null;
                LocalDate receivingDate = null;
                String lsServerDate = "";
                String lsTransDate = "";
                String lsSelectedDate = "";
                String lsReceivingDate = "";

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (JFXUtil.isObjectEqualTo(inputText, null, "", "01/01/1900")) {
                    return;
                }
                selectedDate = ldtResult.selectedDate;

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poSOATaggingController.SOATagging().getEditMode() == EditMode.ADDNEW
                                || poSOATaggingController.SOATagging().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poSOATaggingController.SOATagging().Master().getTransactionDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText),  SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poSOATaggingController.SOATagging().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
                                pbSuccess = false;

                                if (oApp.getUserLevel() <= UserRight.ENCODER) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Change in Transaction Date Detected\n\n"
                                            + "If YES, please seek approval to proceed with the new selected date.\n"
                                            + "If NO, the previous transaction date will be retained.") == true) {
                                        poJSON = ShowDialogFX.getUserApproval(oApp);
                                        if (!"success".equals((String) poJSON.get("result"))) {
                                            pbSuccess = false;
                                        } else {
                                            
                                            if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                                                poJSON.put("result", "error");
                                                poJSON.put("message", "User is not an authorized approving officer.");
                                                pbSuccess = false;
                                            } else {
                                                poSOATaggingController.SOATagging().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                            }

                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                }
                            }

                            if (pbSuccess) {

                            } else {
                                if ("error".equals((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                }
                            }
                            pbSuccess = false; //Set to false to prevent multiple message box: Conflict with server date vs transaction date validation
                            loadRecordMaster();
                            pbSuccess = true; //Set to original value

                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTableMain() {
        // Setting data to table detail
        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewMainList.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    main_data.clear();
                    JFXUtil.disableAllHighlight(tblViewMainList, highlightedRowsMain);
                    String lsPayeeName = "";
                    String lsTransNo = "";
                    String lsTransDate = "";

                    //retreiving using column index
                    for (int lnCtr = 0; lnCtr <= poSOATaggingController.SOATagging().getSOATaggingCount() - 1; lnCtr++) {

                        lsTransNo = String.valueOf(poSOATaggingController.SOATagging().APPaymentMasterList(lnCtr).getTransactionNo());
                        try {
                            main_data.add(new ModelSOATagging_Main(String.valueOf(lnCtr + 1),
                                    String.valueOf(poSOATaggingController.SOATagging().APPaymentMasterList(lnCtr).Supplier().getCompanyName()),
                                    String.valueOf(poSOATaggingController.SOATagging().APPaymentMasterList(lnCtr).getTransactionDate()),
                                    String.valueOf(poSOATaggingController.SOATagging().APPaymentMasterList(lnCtr).getTransactionNo())
                            ));
                        } catch (SQLException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        } catch (GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                        if (poSOATaggingController.SOATagging().APPaymentMasterList(lnCtr).getTransactionStatus().equals(SOATaggingStatus.CONFIRMED)) {
                            JFXUtil.highlightByKey(tblViewMainList, String.valueOf(lnCtr + 1), "#C1E1C1", highlightedRowsMain);
                        }
                    }

                    if (pnMain < 0 || pnMain
                            >= main_data.size()) {
                        if (!main_data.isEmpty()) {
                            /* FOCUS ON FIRST ROW */
                            JFXUtil.selectAndFocusRow(tblViewMainList, 0);
                            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();

                        }
                    } else {
                        /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                        JFXUtil.selectAndFocusRow(tblViewMainList, pnMain);
                    }

                    JFXUtil.loadTab(pgPagination, main_data.size(), ROWS_PER_PAGE, tblViewMainList, filteredData);
                });

                return null;
            }

            @Override
            protected void succeeded() {
                loading.placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed
                if (main_data == null || main_data.isEmpty()) {
                    tblViewMainList.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewMainList.toFront();
                }
                loading.progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                if (main_data == null || main_data.isEmpty()) {
                    tblViewMainList.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    public void loadRecordSearch() {
        try {
            poSOATaggingController.SOATagging().Master().setIndustryId(psIndustryId);
            
            if(poSOATaggingController.SOATagging().Master().Industry().getDescription() != null && !"".equals(poSOATaggingController.SOATagging().Master().Industry().getDescription())){
                lblSource.setText(poSOATaggingController.SOATagging().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }

            tfSearchSupplier.setText(psSearchSupplierId.equals("") ? "" : poSOATaggingController.SOATagging().Master().Supplier().getCompanyName());
            tfSearchCompany.setText(psSearchCompanyId.equals("") ? "" : poSOATaggingController.SOATagging().Master().Company().getCompanyName());
            JFXUtil.updateCaretPositions(apBrowse);

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSOATaggingController.SOATagging().getDetailCount() - 1) {
                return;
            }

            boolean lbDisable = poSOATaggingController.SOATagging().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;
            JFXUtil.setDisabled(!lbDisable, tfSourceNo, tfReferenceNo);

            String lsReferenceDate = "01/01/1900";
            String lsReferenceNo = "";
            switch (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceCode()) {
                case SOATaggingStatic.PaymentRequest:
                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(pnDetail).PaymentRequestMaster().getSeriesNo();
                    lsReferenceDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Detail(pnDetail).PaymentRequestMaster().getTransactionDate());
                    break;
                case SOATaggingStatic.CachePayable:
                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(pnDetail).CachePayableMaster().getReferNo();
                    lsReferenceDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Detail(pnDetail).CachePayableMaster().getTransactionDate());
                    break;
            }

            tfSourceNo.setText(poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo());
            tfSourceCode.setText(poSOATaggingController.SOATagging().Detail(pnDetail).getSourceCode());
            tfReferenceNo.setText(lsReferenceNo);
            dpReferenceDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(JFXUtil.convertToIsoFormat(lsReferenceDate), "yyyy-MM-dd"));
            tfCreditAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getCreditAmount(), true));
            tfDebitAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getDebitAmount(), true));
            tfAppliedAmtDetail.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(pnDetail).getAppliedAmount(), true));
            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void loadRecordMaster() {
        try {
            Platform.runLater(() -> {
                String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSOATaggingController.SOATagging().Master().getTransactionStatus();
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put(SOATaggingStatus.OPEN, "OPEN");
                statusMap.put(SOATaggingStatus.PAID, "PAID");
                statusMap.put(SOATaggingStatus.CONFIRMED, "CONFIRMED");
                statusMap.put(SOATaggingStatus.RETURNED, "RETURNED");
                statusMap.put(SOATaggingStatus.VOID, "VOIDED");
                statusMap.put(SOATaggingStatus.CANCELLED, "CANCELLED");
                String lsStat = statusMap.getOrDefault(lsActive, "UNKNOWN");
                lblStatus.setText(lsStat);
            });

            poSOATaggingController.SOATagging().computeFields();

            tfTransactionNo.setText(poSOATaggingController.SOATagging().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poSOATaggingController.SOATagging().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));
            tfSOANo.setText(poSOATaggingController.SOATagging().Master().getSOANumber());
            tfCompany.setText(poSOATaggingController.SOATagging().Master().Company().getCompanyName());
            tfClient.setText(poSOATaggingController.SOATagging().Master().Supplier().getCompanyName());
            tfIssuedTo.setText(poSOATaggingController.SOATagging().Master().Payee().getPayeeName());
            taRemarks.setText(poSOATaggingController.SOATagging().Master().getRemarks());

            tfTransactionTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getTransactionTotal(), true));
            tfVatAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getVatAmount(), true));
            tfDiscountAmount.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getDiscountAmount(), true));
            tfFreight.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getFreightAmount(), false));
            tfNonVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(),true)); //As per ma'am she
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getVatExempt(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getNetTotal(), true));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();

            ModelSOATagging_Main selected = (ModelSOATagging_Main) tblViewMainList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                pnMain = pnRowMain;
                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                JFXUtil.highlightByKey(tblViewMainList, String.valueOf(pnRowMain + 1), "#A7C7E7", highlightedRowsMain);

                poJSON = poSOATaggingController.SOATagging().OpenTransaction(poSOATaggingController.SOATagging().APPaymentMasterList(pnMain).getTransactionNo());
                if ("error".equals((String) poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    return;
                }
                loadRecordMaster();
                loadTableDetail();
            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail

        JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
        tblViewTransDetailList.setPlaceholder(loading.loadingPane);
        loading.progressIndicator.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    details_data.clear();
                    int lnCtr;
                    try {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poSOATaggingController.SOATagging().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poSOATaggingController.SOATagging().Detail(lnCtr).getSourceNo() == null || "".equals(poSOATaggingController.SOATagging().Detail(lnCtr).getSourceNo())) {
                                    poSOATaggingController.SOATagging().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poSOATaggingController.SOATagging().getDetailCount() - 1) >= 0) {
                                if (poSOATaggingController.SOATagging().Detail(poSOATaggingController.SOATagging().getDetailCount() - 1).getSourceNo() != null
                                        && !"".equals(poSOATaggingController.SOATagging().Detail(poSOATaggingController.SOATagging().getDetailCount() - 1).getSourceNo())) {
                                    //poSOATaggingController.SOATagging().AddDetail();
                                }
                            }

                            if ((poSOATaggingController.SOATagging().getDetailCount() - 1) < 0) {
                                //poSOATaggingController.SOATagging().AddDetail();
                            }
                        }

                        String lsReferenceNo = "";
                        for (lnCtr = 0; lnCtr < poSOATaggingController.SOATagging().getDetailCount(); lnCtr++) {
                            switch (poSOATaggingController.SOATagging().Detail(lnCtr).getSourceCode()) {
                                case SOATaggingStatic.PaymentRequest:
                                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(lnCtr).PaymentRequestMaster().getSeriesNo();
                                    break;
                                case SOATaggingStatic.CachePayable:
                                    lsReferenceNo = poSOATaggingController.SOATagging().Detail(lnCtr).CachePayableMaster().getReferNo();
                                    break;
                            }

                            details_data.add(
                                    new ModelSOATagging_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poSOATaggingController.SOATagging().Detail(lnCtr).getSourceNo()),
                                            String.valueOf(poSOATaggingController.SOATagging().Detail(lnCtr).getSourceCode()),
                                            String.valueOf(lsReferenceNo),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getCreditAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getDebitAmount(), true)),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Detail(lnCtr).getAppliedAmount(), true))
                                    ));
                            lsReferenceNo = "";
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                JFXUtil.selectAndFocusRow(tblViewTransDetailList, 0);
                                pnDetail = tblViewTransDetailList.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            JFXUtil.selectAndFocusRow(tblViewTransDetailList, pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                } else {
                    tblViewTransDetailList.toFront();
                }
                loading.progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewTransDetailList.setPlaceholder(loading.placeholderLabel);
                }
                loading.progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background

    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
dpTransactionDate, dpReferenceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate);
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfCompany, tfClient, tfIssuedTo, tfSOANo, tfDiscountAmount, tfSearchCompany, tfSearchSupplier);
        JFXUtil.setFocusListener(txtDetail_Focus, tfSourceNo, tfSourceCode, tfReferenceNo, tfAppliedAmtDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfVatAmount, tfDiscountAmount, tfZeroVatSales,tfNonVatSales , tfVatExemptSales, tfAppliedAmtDetail);
    }

    public void initTableOnClick() {
        tblViewTransDetailList.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewTransDetailList.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    if (poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo() != null && !poSOATaggingController.SOATagging().Detail(pnDetail).getSourceNo().equals("")) {
                        tfAppliedAmtDetail.requestFocus();
                    } else {
                        tfReferenceNo.requestFocus();
                    }
                }
            }
        });

        tblViewMainList.setOnMouseClicked(event -> {
            pnMain = tblViewMainList.getSelectionModel().getSelectedIndex();
            if (pnMain >= 0) {
                if (event.getClickCount() == 2) {
                    loadTableDetailFromMain();
                    pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                    initButton(pnEditMode);
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelSOATagging_Main) item).getIndex01(), highlightedRowsMain);
        tblViewTransDetailList.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetailList, tblViewMainList); // need to use computed-size in min-width of the column to work
    }

    private void initButton(int fnValue) {
        boolean lbShow1 = (fnValue == EditMode.UPDATE);
//        boolean lbShow2 = (fnValue == EditMode.READY || fnValue == EditMode.UPDATE);
        boolean lbShow3 = (fnValue == EditMode.READY);
        boolean lbShow4 = (fnValue == EditMode.UNKNOWN || fnValue == EditMode.READY);
        // Manage visibility and managed state of other buttons
        //Update 
        JFXUtil.setButtonsVisibility(lbShow1, btnSearch, btnSave, btnCancel);
        //Ready
        JFXUtil.setButtonsVisibility(lbShow3, btnUpdate, btnHistory, btnConfirm, btnVoid);
        //Unkown || Ready
        JFXUtil.setDisabled(!lbShow1, apMaster, apDetail);
        JFXUtil.setButtonsVisibility(lbShow4, btnClose);
        JFXUtil.setButtonsVisibility(false, btnReturn);

        switch (poSOATaggingController.SOATagging().Master().getTransactionStatus()) {
            case SOATaggingStatus.CONFIRMED:
                JFXUtil.setButtonsVisibility(false, btnConfirm);
                if (poSOATaggingController.SOATagging().Master().isProcessed()) {
                    JFXUtil.setButtonsVisibility(false, btnUpdate, btnVoid);
                } else {
//                    JFXUtil.setButtonsVisibility(lbShow3, btnReturn);
                }
                break;
//            case SOATaggingStatus.POSTED:
            case SOATaggingStatus.PAID:
            case SOATaggingStatus.RETURNED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate , btnVoid); // btnReturn
                break;
            case SOATaggingStatus.VOID:
            case SOATaggingStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnConfirm, btnUpdate, btnVoid); // btnReturn
                break;
        }
    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail);
        JFXUtil.setColumnLeft(tblSourceNoDetail, tblSourceCodeDetail, tblReferenceNoDetail);
        JFXUtil.setColumnRight(tblCreditAmtDetail, tblDebitAmtDetail, tblAppliedAmtDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewTransDetailList);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);
        SortedList<ModelSOATagging_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewTransDetailList.comparatorProperty());
        tblViewTransDetailList.setItems(sortedData);
        tblViewTransDetailList.autosize();
    }

    public void initMainGrid() {
        JFXUtil.setColumnCenter(tblRowNo, tblDate, tblReferenceNo);
        JFXUtil.setColumnLeft(tblSupplier);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewMainList);

        filteredData = new FilteredList<>(main_data, b -> true);
        tblViewMainList.setItems(filteredData);

    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView<?> currentTable = (TableView<?>) event.getSource();
            TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
            switch (currentTable.getId()) {
                case "tblViewTransDetailList":
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

    public void clearTextFields() {
        psSearchCompanyId = "";
        psCompanyId = "";
        psSearchSupplierId = "";
        psSupplierId = "";
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField, dpTransactionDate);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }

}
