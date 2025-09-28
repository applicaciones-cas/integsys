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
import javafx.util.Pair;
import java.util.ArrayList;
import ph.com.guanzongroup.cas.cashflow.SOATagging;
import ph.com.guanzongroup.cas.cashflow.status.SOATaggingStatic;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.ComboBox;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import ph.com.guanzongroup.cas.cashflow.services.CashflowControllers;

/**
 * FXML Controller class
 *
 * @author Aldrich & Arsiela Team 2 06102025
 */
public class SOATagging_EntryController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    static CashflowControllers poSOATaggingController;
    private JSONObject poJSON;
    public int pnEditMode;
    private static final int ROWS_PER_PAGE = 50;
    int pnDetail = 0;
    int pnMain = 0;
    private String pxeModuleName = "";
    private boolean isGeneral = false;
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private boolean pbEntered = false;
    boolean pbKeyPressed = false;
    private ObservableList<ModelSOATagging_Main> main_data = FXCollections.observableArrayList();
    private ObservableList<ModelSOATagging_Detail> details_data = FXCollections.observableArrayList();

    private FilteredList<ModelSOATagging_Main> filteredData;
    private FilteredList<ModelSOATagging_Detail> filteredDataDetail;
    List<Pair<String, String>> plOrderNoPartial = new ArrayList<>();
    List<Pair<String, String>> plOrderNoFinal = new ArrayList<>();

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
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnHistory, btnRetrieve, btnClose;
    @FXML
    private TextField tfTransactionNo, tfSOANo, tfClient, tfIssuedTo, tfTransactionTotal, tfVatAmount, tfNonVatSales, tfZeroVatSales, tfVatExemptSales,
            tfNetTotal, tfCompany, tfDiscountAmount, tfFreight, tfSourceNo, tfSourceCode, tfReferenceNo, tfCreditAmount, tfDebitAmount, tfAppliedAmtDetail;
    @FXML
    private DatePicker dpTransactionDate, dpReferenceDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView tblViewTransDetailList, tblViewMainList;
    @FXML
    private TableColumn tblRowNoDetail, tblSourceNoDetail, tblSourceCodeDetail, tblReferenceNoDetail, tblCreditAmtDetail, tblDebitAmtDetail, tblAppliedAmtDetail, tblRowNo, tblTransType, tblSupplier, tblDate, tblReferenceNo;
    @FXML
    private Pagination pgPagination;
    @FXML
    private ComboBox cmbTransType;
    ObservableList<String> TransactionType = FXCollections.observableArrayList(
            "ALL",
            "Cache Payable",
            "PRF"
    );

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
        initComboBoxes();
        initTextFields();
        initDatePickers();
        initMainGrid();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();

        Platform.runLater(() -> {
            poSOATaggingController.SOATagging().Master().setIndustryId(psIndustryId);
//            poSOATaggingController.SOATagging().Master().setCompanyID(psCompanyId);
            poSOATaggingController.SOATagging().setIndustryId(psIndustryId);
            poSOATaggingController.SOATagging().setCompanyId(psCompanyId);
            poSOATaggingController.SOATagging().setCategoryId(psCategoryId);
            poSOATaggingController.SOATagging().initFields();
            poSOATaggingController.SOATagging().setWithUI(true);
            poSOATaggingController.SOATagging().validatePayment(true);
            loadRecordSearch();
            btnNew.fire();
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
                    case "btnBrowse":
                        poSOATaggingController.SOATagging().setTransactionStatus(SOATaggingStatus.OPEN);
                        poJSON = poSOATaggingController.SOATagging().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                        break;
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
                        break;
                    case "btnSearch":
                        JFXUtil.initiateBtnSearch(pxeModuleName, lastFocusedTextField, previousSearchedTextField, apBrowse, apMaster, apDetail);
                        break;
                    case "btnCancel":
                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                            pnEditMode = EditMode.UNKNOWN;
                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnRetrieve":
                        retrievePayables(false);
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poSOATaggingController.SOATagging().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poSOATaggingController.SOATagging().AddDetail();
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
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                JFXUtil.disableAllHighlightByColor(tblViewMainList, "#A7C7E7", highlightedRowsMain);
                                JFXUtil.showRetainedHighlight(true, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                btnNew.fire();
                            }
                        } else {
                            return;
                        }
                        return;
                    case "btnNew":
                        //Clear data
                        poSOATaggingController.SOATagging().resetMaster();
                        clearTextFields();
                        poJSON = poSOATaggingController.SOATagging().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        poSOATaggingController.SOATagging().initFields();
                        pnEditMode = poSOATaggingController.SOATagging().getEditMode();
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        break;

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

                if (JFXUtil.isObjectEqualTo(lsButton, "btnAddAttachment", "btnRemoveAttachment",
                        "btnArrowRight", "btnArrowLeft", "btnRetrieve", "btnClose")) {
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

    public void loadHighlightFromDetail() {
        try {
            String lsTransNoBasis = "", lsTransType = "";
            for (int lnCtr = 0; lnCtr < poSOATaggingController.SOATagging().getDetailCount(); lnCtr++) {
                switch (poSOATaggingController.SOATagging().Detail(lnCtr).getSourceCode()) {
                    case SOATaggingStatic.PaymentRequest:
                        lsTransNoBasis = poSOATaggingController.SOATagging().Detail(lnCtr).PaymentRequestMaster().getTransactionNo();
                        lsTransType = "PRF";
                        break;
                    case SOATaggingStatic.CachePayable: {
                        lsTransNoBasis = poSOATaggingController.SOATagging().Detail(lnCtr).CachePayableMaster().getTransactionNo();
                        lsTransType = "Cache Payable";
                    }
                    break;
                }
                String lsHighlightbasis = lsTransNoBasis + lsTransType;
                if (!JFXUtil.isObjectEqualTo(poSOATaggingController.SOATagging().Detail(lnCtr).getAppliedAmount(), null, "")) {
                    if (poSOATaggingController.SOATagging().Detail(lnCtr).getAppliedAmount().doubleValue() > 0.0000) {
                        plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "1"));
                    } else {
                        plOrderNoPartial.add(new Pair<>(lsHighlightbasis, "0"));
                    }
                }
            }
            for (Pair<String, String> pair : plOrderNoPartial) {
                if (!"".equals(pair.getKey()) && pair.getKey() != null) {
                    JFXUtil.highlightByKey(tblViewMainList, pair.getKey(), "#A7C7E7", highlightedRowsMain);
                }
            }
            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, false);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void retrievePayables(boolean isInReferenceNo) {
        poJSON = new JSONObject();
        String lsTransType = "ALL";
        switch (cmbTransType.getSelectionModel().getSelectedIndex()) {
            case 1:
                lsTransType = SOATaggingStatic.CachePayable;
                break;
            case 2:
                lsTransType = SOATaggingStatic.PaymentRequest;
                break;
        }

        if (isInReferenceNo) {
            poJSON = poSOATaggingController.SOATagging().loadPayables(tfClient.getText(), tfCompany.getText(), tfIssuedTo.getText(), tfReferenceNo.getText(), lsTransType);
        } else {
            //general
            poJSON = poSOATaggingController.SOATagging().loadPayables(tfClient.getText(), tfCompany.getText(), tfIssuedTo.getText(), "", lsTransType);
        }

        if (!"success".equals((String) poJSON.get("result"))) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        } else {
            Platform.runLater(() -> {
                loadTableMain();
            });
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
            JFXUtil.runWithDelay(0.50, () -> {
                loadTableDetail();
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
                case "tfSOANo":
                    if (!lsValue.isEmpty()) {
                        poJSON = poSOATaggingController.SOATagging().Master().setSOANumber(lsValue);
                    } else {
                        poJSON = poSOATaggingController.SOATagging().Master().setSOANumber("");
                    }
                    if ("error".equals(poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        tfSOANo.setText("");
                        break;
                    }
                    break;
                case "tfCompany":
                    if (lsValue.isEmpty()) {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (poSOATaggingController.SOATagging().Master().getCompanyId() != null && !"".equals(poSOATaggingController.SOATagging().Master().getCompanyId())) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the company name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                            poSOATaggingController.SOATagging().removeDetails();
                                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = poSOATaggingController.SOATagging().Master().setCompanyId("");
                    }
                    break;
                case "tfClient":
                    if (lsValue.isEmpty()) {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (poSOATaggingController.SOATagging().Master().getClientId() != null && !"".equals(poSOATaggingController.SOATagging().Master().getClientId())) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                            poSOATaggingController.SOATagging().removeDetails();
                                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = poSOATaggingController.SOATagging().Master().setClientId("");
                    }
                    break;
                case "tfIssuedTo":
                    if (lsValue.isEmpty()) {
                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            if (poSOATaggingController.SOATagging().Master().getIssuedTo() != null && !"".equals(poSOATaggingController.SOATagging().Master().getIssuedTo())) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    if (!pbKeyPressed) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the payee name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                            poSOATaggingController.SOATagging().removeDetails();
                                            JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                }
                            }
                        }
                        poJSON = poSOATaggingController.SOATagging().Master().setIssuedTo("");
                    }
                    break;
                case "tfDiscountAmount":
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
                    lsValue = JFXUtil.removeComma(lsValue);
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

            System.out.println("Company : " + poSOATaggingController.SOATagging().Master().getCompanyId());
            System.out.println("Supplier : " + poSOATaggingController.SOATagging().Master().getClientId());
            System.out.println("Payee : " + poSOATaggingController.SOATagging().Master().getIssuedTo());
            loadRecordMaster();
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
                        case "tfReferenceNo":
                            retrievePayables(true);
                            break;
                        case "tfClient":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                        poSOATaggingController.SOATagging().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }

                            poJSON = poSOATaggingController.SOATagging().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
                                break;
                            }
                            JFXUtil.runWithDelay(0.50, () -> {
                                if (!"".equals(poSOATaggingController.SOATagging().Master().getClientId())) {
                                    retrievePayables(false);
                                }
                            });
                            loadRecordMaster();
                            return;
                        case "tfCompany":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the company name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                        poSOATaggingController.SOATagging().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }

                            poJSON = poSOATaggingController.SOATagging().SearchCompany(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfClient.setText("");
//                                psSupplierId = "";
                                break;
                            }
                            JFXUtil.runWithDelay(0.50, () -> {
                                if (!"".equals(poSOATaggingController.SOATagging().Master().getCompanyId())) {
                                    retrievePayables(false);
                                }
                            });
                            loadRecordMaster();
                            return;
                        case "tfIssuedTo":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poSOATaggingController.SOATagging().getDetailCount() > 1) {
                                    pbKeyPressed = true;
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the payee name?\nPlease note that doing so will delete all SOA details.\n\nDo you wish to proceed?") == true) {
                                        poSOATaggingController.SOATagging().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        loadRecordMaster();
                                        return;
                                    }
                                    pbKeyPressed = false;
                                }
                            }

                            poJSON = poSOATaggingController.SOATagging().SearchPayee(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfIssuedTo.setText("");
                                break;
                            }
                            JFXUtil.runWithDelay(0.50, () -> {
                                if (!"".equals(poSOATaggingController.SOATagging().Master().getIssuedTo())) {
                                    retrievePayables(false);
                                }
                            });
                            loadRecordMaster();
                            return;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
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
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText), SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                JFXUtil.setJSONError(poJSON, "Future dates are not allowed.");
                                pbSuccess = false;
                            }

                            if (pbSuccess && ((poSOATaggingController.SOATagging().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
                                    || !lsServerDate.equals(lsSelectedDate))) {
                                pbSuccess = false;
                                if (ShowMessageFX.YesNo(null, pxeModuleName, "Change in Transaction Date Detected\n\n"
                                        + "If YES, please seek approval to proceed with the new selected date.\n"
                                        + "If NO, the previous transaction date will be retained.") == true) {
                                    if (oApp.getUserLevel() <= UserRight.ENCODER) {
                                        poJSON = ShowDialogFX.getUserApproval(oApp);
                                        if (!"success".equals((String) poJSON.get("result"))) {
                                            pbSuccess = false;
                                        } else {

                                            if (Integer.parseInt(poJSON.get("nUserLevl").toString()) <= UserRight.ENCODER) {
                                                poJSON.put("result", "error");
                                                poJSON.put("message", "User is not an authorized approving officer.");
                                                pbSuccess = false;
                                            } else {
                                                poSOATaggingController.SOATagging().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                            }
                                        }
                                    }
                                } else {
                                    pbSuccess = false;
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
                    String lsTransNoBasis = "";
                    String lsTransType = "";
                    //retreiving using column index
                    for (int lnCtr = 0; lnCtr <= poSOATaggingController.SOATagging().getPayablesCount() - 1; lnCtr++) {
                        try {
                            switch (poSOATaggingController.SOATagging().PayableType(lnCtr)) {
                                case SOATaggingStatic.PaymentRequest:
                                    lsPayeeName = poSOATaggingController.SOATagging().PaymentRequestList(lnCtr).Payee().getPayeeName();
                                    lsTransNo = poSOATaggingController.SOATagging().PaymentRequestList(lnCtr).getSeriesNo();
                                    lsTransDate = String.valueOf(poSOATaggingController.SOATagging().PaymentRequestList(lnCtr).getTransactionDate());
                                    lsTransNoBasis = poSOATaggingController.SOATagging().PaymentRequestList(lnCtr).getTransactionNo();
                                    lsTransType = "PRF";
                                    break;
                                case SOATaggingStatic.CachePayable:
                                    lsPayeeName = poSOATaggingController.SOATagging().CachePayableList(lnCtr).Client().getCompanyName();
                                    lsTransNo = poSOATaggingController.SOATagging().CachePayableList(lnCtr).getReferNo();
                                    lsTransDate = String.valueOf(poSOATaggingController.SOATagging().CachePayableList(lnCtr).getTransactionDate());
                                    lsTransNoBasis = poSOATaggingController.SOATagging().CachePayableList(lnCtr).getTransactionNo();
                                    lsTransType = "Cache Payable";
                                    break;
                            }
                            String lsHighlightbasis = lsTransNoBasis + lsTransType;
                            main_data.add(new ModelSOATagging_Main(String.valueOf(lnCtr + 1),
                                    lsTransType,
                                    lsPayeeName,
                                    lsTransDate,
                                    lsTransNo,
                                    lsHighlightbasis
                            ));
                        } catch (SQLException | GuanzonException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                        }
                    }

                    loadHighlightFromDetail();
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
            if (poSOATaggingController.SOATagging().Master().Industry().getDescription() != null && !"".equals(poSOATaggingController.SOATagging().Master().Industry().getDescription())) {
                lblSource.setText(poSOATaggingController.SOATagging().Master().Industry().getDescription());
            } else {
                lblSource.setText("General");
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poSOATaggingController.SOATagging().getDetailCount() - 1) {
                return;
            }

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
            boolean lbDisable = lsReferenceNo != null && "".equals(lsReferenceNo);
            JFXUtil.setDisabled(!lbDisable, tfReferenceNo);

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
            boolean lbDisable = pnEditMode == EditMode.UPDATE;
            JFXUtil.setDisabled(lbDisable, tfCompany, tfClient, tfIssuedTo);

            String lsActive = pnEditMode == EditMode.UNKNOWN ? "-1" : poSOATaggingController.SOATagging().Master().getTransactionStatus();
            boolean lbPrintStat = pnEditMode == EditMode.READY && !SOATaggingStatus.VOID.equals(lsActive);
            JFXUtil.setStatusValue(lblStatus, SOATaggingStatus.class, lsActive);
            JFXUtil.setButtonsVisibility(lbPrintStat);

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
            tfNonVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(), true)); //As per ma'am she
            tfZeroVatSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getZeroRatedVat(), true));
            tfVatExemptSales.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getVatExempt(), true));
            tfNetTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poSOATaggingController.SOATagging().Master().getNetTotal(), true));
            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadTableDetailFromMain() {
        try {
            poJSON = new JSONObject();
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                ModelSOATagging_Main selected = (ModelSOATagging_Main) tblViewMainList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int pnRowMain = Integer.parseInt(selected.getIndex01()) - 1;
                    pnMain = pnRowMain;
                    switch (poSOATaggingController.SOATagging().PayableType(pnMain)) {
                        case SOATaggingStatic.PaymentRequest:
                            poJSON = poSOATaggingController.SOATagging().addPayablesToSOADetail(
                                    poSOATaggingController.SOATagging().PaymentRequestList(pnMain).getTransactionNo(),
                                    poSOATaggingController.SOATagging().PayableType(pnMain));
                            break;
                        case SOATaggingStatic.CachePayable:
                            poJSON = poSOATaggingController.SOATagging().addPayablesToSOADetail(
                                    poSOATaggingController.SOATagging().CachePayableList(pnMain).getTransactionNo(),
                                    poSOATaggingController.SOATagging().PayableType(pnMain));
                            break;
                    }

                    if ("error".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    }
                }
                Platform.runLater(() -> {
                    loadTableDetail();
                });
            } else {
                ShowMessageFX.Warning(null, pxeModuleName, "Data can only be viewed when in ADD or UPDATE mode.");
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
                Platform.runLater(() -> {
                    details_data.clear();
                    plOrderNoPartial.clear();

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
                                    poSOATaggingController.SOATagging().AddDetail();
                                }
                            }

                            if ((poSOATaggingController.SOATagging().getDetailCount() - 1) < 0) {
                                poSOATaggingController.SOATagging().AddDetail();
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
                        JFXUtil.showRetainedHighlight(false, tblViewMainList, "#A7C7E7", plOrderNoPartial, plOrderNoFinal, highlightedRowsMain, true);
                        loadHighlightFromDetail();

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
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    } catch (SQLException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    } catch (GuanzonException ex) {
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

    private void initComboBoxes() {
        // Set the items of the ComboBox to the list of genders
        cmbTransType.setItems(TransactionType);
        cmbTransType.getSelectionModel().select(0);
        cmbTransType.setOnAction(event -> {
            retrievePayables(false);
        });
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbTransType);
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy", dpTransactionDate, dpReferenceDate);
        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate, dpReferenceDate);
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtMaster_Focus, tfCompany, tfClient, tfIssuedTo, tfSOANo, tfDiscountAmount);
        JFXUtil.setFocusListener(txtDetail_Focus, tfSourceNo, tfSourceCode, tfReferenceNo, tfAppliedAmtDetail);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);
        JFXUtil.setCommaFormatter(tfVatAmount, tfDiscountAmount, tfZeroVatSales, tfNonVatSales, tfVatExemptSales, tfAppliedAmtDetail);
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
                    initButton(pnEditMode);
                }
            }
        });

        JFXUtil.applyRowHighlighting(tblViewMainList, item -> ((ModelSOATagging_Main) item).getIndex06(), highlightedRowsMain);
        tblViewTransDetailList.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewTransDetailList, tblViewMainList); // need to use computed-size in min-width of the column to work
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

        JFXUtil.setDisabled(!lbShow, taRemarks, apMaster, apDetail);

        switch (poSOATaggingController.SOATagging().Master().getTransactionStatus()) {
            case SOATaggingStatus.PAID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
            case SOATaggingStatus.VOID:
            case SOATaggingStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
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
        JFXUtil.setColumnCenter(tblRowNo, tblTransType, tblDate, tblReferenceNo);
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
        JFXUtil.setValueToNull(previousSearchedTextField, lastFocusedTextField, dpTransactionDate);
        JFXUtil.clearTextFields(apMaster, apDetail, apBrowse);
    }

}
