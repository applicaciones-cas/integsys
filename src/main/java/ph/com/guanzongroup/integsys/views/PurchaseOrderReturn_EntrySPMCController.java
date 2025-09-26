/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelPurchaseOrderReturn_Detail;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import ph.com.guanzongroup.integsys.utility.JFXUtil;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.purchasing.services.PurchaseOrderReturnControllers;
import org.guanzon.cas.purchasing.status.PurchaseOrderReturnStatus;
import org.json.simple.JSONObject;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.guanzon.cas.purchasing.controller.PurchaseOrderReturn;
import org.json.simple.parser.ParseException;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.constant.UserRight;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * FXML Controller class
 *
 * @author User
 */
public class PurchaseOrderReturn_EntrySPMCController implements Initializable, ScreenInterface {

    private GRiderCAS oApp;
    private JSONObject poJSON;
    int pnDetail = 0;
    boolean lsIsSaved = false;
    private final String pxeModuleName = "Purchase Order Return Entry SPMC";
    static PurchaseOrderReturnControllers poPurchaseReturnController;
    public int pnEditMode;

    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategoryId = "";
    private String psSupplierId = "";

    private ObservableList<ModelPurchaseOrderReturn_Detail> details_data = FXCollections.observableArrayList();
    private FilteredList<ModelPurchaseOrderReturn_Detail> filteredDataDetail;

    private Object lastFocusedTextField = null;
    private Object previousSearchedTextField = null;
    private boolean pbEntered = false;

    @FXML
    private AnchorPane apMainAnchor, apBrowse, apButton, apMaster, apDetail;

    @FXML
    private HBox hbButtons, hboxid;

    @FXML
    private Label lblSource, lblStatus;

    @FXML
    private Button btnBrowse, btnNew, btnUpdate, btnSearch, btnSave, btnCancel, btnPrint, btnHistory, btnClose;

    @FXML
    private TextField tfTransactionNo, tfSupplier, tfReferenceNo, tfPOReceivingNo, tfTotal, tfBarcode, tfDescription,
            tfReturnQuantity, tfBrand, tfModel, tfColor, tfInventoryType, tfMeasure, tfCost, tfReceiveQuantity;

    @FXML
    private DatePicker dpTransactionDate;

    @FXML
    private TextArea taRemarks;

    @FXML
    private TableView tblViewDetails;

    @FXML
    private TableColumn tblRowNoDetail, tblBarcodeDetail, tblDescriptionDetail, tblCostDetail, tblReceiveQuantityDetail,
            tblReturnQuantityDetail, tblTotalDetail;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        poPurchaseReturnController = new PurchaseOrderReturnControllers(oApp, null);
        poJSON = new JSONObject();
        poJSON = poPurchaseReturnController.PurchaseOrderReturn().InitTransaction(); // Initialize transaction
        if (!"success".equals((String) poJSON.get("result"))) {
            System.err.println((String) poJSON.get("message"));
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
        }
        initTextFields();
        initDatePickers();
        initDetailsGrid();
        initTableOnClick();
        clearTextFields();
        loadRecordMaster();
        loadTableDetail();
        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
        initButton(pnEditMode);

        Platform.runLater(() -> {
            poPurchaseReturnController.PurchaseOrderReturn().Master().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().Master().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setIndustryId(psIndustryId);
            poPurchaseReturnController.PurchaseOrderReturn().setCompanyId(psCompanyId);
            poPurchaseReturnController.PurchaseOrderReturn().setCategoryId(psCategoryId);
            poPurchaseReturnController.PurchaseOrderReturn().setWithUI(true);
            loadRecordSearch();

            btnNew.fire();
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
        psCompanyId = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryId = fsValue;
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
                        poPurchaseReturnController.PurchaseOrderReturn().setTransactionStatus(PurchaseOrderReturnStatus.RETURNED + "" + PurchaseOrderReturnStatus.OPEN);
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().searchTransaction();
                        if ("error".equalsIgnoreCase((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfTransactionNo.requestFocus();
                            return;
                        }
                        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                        psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();

                        break;
                    case "btnPrint":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().printRecord(() -> {
                            if (lsIsSaved) {
                                Platform.runLater(() -> {
                                    btnNew.fire();
                                });
                            } else {
                                loadRecordMaster();
                            }
                            lsIsSaved = false;
                        });
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
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
                    case "btnNew":
                        //Clear data
                        poPurchaseReturnController.PurchaseOrderReturn().resetMaster();
                        poPurchaseReturnController.PurchaseOrderReturn().Detail().clear();
                        clearTextFields();

                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().NewTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }

                        poPurchaseReturnController.PurchaseOrderReturn().initFields();
                        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();

                        break;
                    case "btnUpdate":
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().UpdateTransaction();
                        if ("error".equals((String) poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            return;
                        }
                        pnEditMode = poPurchaseReturnController.PurchaseOrderReturn().getEditMode();
                        break;
                    case "btnSearch":
                        String lsMessage = "Focus a searchable textfield to search";
                        if ((lastFocusedTextField != null)) {
                            if (lastFocusedTextField instanceof TextField) {
                                TextField tf = (TextField) lastFocusedTextField;
                                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", apMaster, apDetail).contains(tf.getId())) {

                                    if (lastFocusedTextField == previousSearchedTextField) {
                                        break;
                                    }
                                    previousSearchedTextField = lastFocusedTextField;
                                    // Create a simulated KeyEvent for F3 key press
                                    KeyEvent keyEvent = new KeyEvent(
                                            KeyEvent.KEY_PRESSED,
                                            "",
                                            "",
                                            KeyCode.F3,
                                            false, false, false, false
                                    );
                                    tf.fireEvent(keyEvent);
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
                            //Clear data
                            poPurchaseReturnController.PurchaseOrderReturn().resetMaster();
                            poPurchaseReturnController.PurchaseOrderReturn().Detail().clear();
                            clearTextFields();

                            poPurchaseReturnController.PurchaseOrderReturn().Master().setIndustryId(psIndustryId);
                            poPurchaseReturnController.PurchaseOrderReturn().Master().setCompanyId(psCompanyId);
                            poPurchaseReturnController.PurchaseOrderReturn().Master().setCategoryCode(psCategoryId);
                            pnEditMode = EditMode.UNKNOWN;

                            break;
                        } else {
                            return;
                        }
                    case "btnHistory":
                        break;
                    case "btnSave":
                        //Validator
                        poJSON = new JSONObject();
                        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the transaction?") == true) {
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SaveTransaction();
                            if (!"success".equals((String) poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                                return;
                            } else {
                                //reshow the highlight
                                ShowMessageFX.Information(null, pxeModuleName, (String) poJSON.get("message"));

                                // Confirmation Prompt
                                JSONObject loJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
                                if ("success".equals(loJSON.get("result"))) {
                                    if (poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus().equals(PurchaseOrderReturnStatus.OPEN)) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to confirm this transaction?")) {
                                            loJSON = poPurchaseReturnController.PurchaseOrderReturn().ConfirmTransaction("");
                                            if ("success".equals((String) loJSON.get("result"))) {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            } else {
                                                ShowMessageFX.Information((String) loJSON.get("message"), pxeModuleName, null);
                                            }
                                        }
                                    }
                                }

                                // Print Transaction Prompt
                                lsIsSaved = false;
                                loJSON = poPurchaseReturnController.PurchaseOrderReturn().OpenTransaction(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
                                loadRecordMaster();
                                if ("success".equals(loJSON.get("result"))) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to print this transaction?")) {
                                        lsIsSaved = true;
                                        btnPrint.fire();
                                    } else {
                                        btnNew.fire();
                                    }
                                } else {
                                    btnNew.fire();
                                }

                            }
                        } else {
                            return;
                        }
                        break;
                    default:
                        ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                        break;
                }

                if (lsButton.equals("btnPrint")) { //|| lsButton.equals("btnCancel")
                } else {
                    loadRecordMaster();
                    loadTableDetail();
                }
                initButton(pnEditMode);

                if (lsButton.equals("btnUpdate")) {
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId())) {
                        tfReturnQuantity.requestFocus();
                    } else {
                        tfBarcode.requestFocus();
                    }
                }

            }
        } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (ParseException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    final ChangeListener<? super Boolean> txtArea_Focus = (o, ov, nv) -> {
        TextArea txtField = (TextArea) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = (txtField.getId());
        String lsValue = txtField.getText();

        lastFocusedTextField = txtField;
        previousSearchedTextField = null;

        if (lsValue == null) {
            return;
        }
        poJSON = new JSONObject();
        if (!nv) {
            /*Lost Focus*/
            lsValue = lsValue.trim();
            switch (lsID) {

                case "taRemarks"://Remarks
                    poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setRemarks(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                        return;
                    }
                    break;
            }
            loadRecordMaster();
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
        lastFocusedTextField = txtPersonalInfo;
        previousSearchedTextField = null;
        if (lsValue == null) {
            return;
        }
        if (!nv) {
            /*Lost Focus*/
            switch (lsTxtFieldID) {
                case "tfDescription":
                case "tfBarcode":
                    //if value is blank then reset
                    if (lsValue.equals("")) {
                        poJSON = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setStockId("");
                    }

                    break;
                case "tfReturnQuantity":
                    if (lsValue.isEmpty()) {
                        lsValue = "0";
                    }
                    lsValue = JFXUtil.removeComma(lsValue);
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity() != null
                            && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity())) {
                        if (poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(pnDetail).intValue() < Integer.valueOf(lsValue)) {
                            ShowMessageFX.Warning(null, pxeModuleName, "Return quantity cannot be greater than the receive quantity.");
                            poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setQuantity(0);
                            tfReturnQuantity.requestFocus();
                            break;
                        }
                    }

                    poJSON = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).setQuantity((Integer.valueOf(lsValue)));
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
        try {
            poJSON = new JSONObject();
            TextField txtPersonalInfo = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
            String lsTxtFieldID = (txtPersonalInfo.getId());
            String lsValue = (txtPersonalInfo.getText() == null ? "" : txtPersonalInfo.getText());
            lastFocusedTextField = txtPersonalInfo;
            previousSearchedTextField = null;
            if (lsValue == null) {
                return;
            }
            if (!nv) {
                /*Lost Focus*/
                switch (lsTxtFieldID) {
                    case "tfSupplier":
                        if (lsValue.isEmpty()) {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId())) {
                                    if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                            poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setSupplierId("");
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setSourceNo("");
                        }
                        break;
                    case "tfReferenceNo":
                        if (!lsValue.isEmpty()) {
                        } else {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getReferenceNo() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getReferenceNo())) {
                                    if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the reference no?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                            poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().setReferenceNo("");
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setSourceNo("");
                        }
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfReferenceNo.setText("");
                            break;
                        }
                        break;

                    case "tfPOReceivingNo":
                        if (!lsValue.isEmpty()) {
                        } else {
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo())) {
                                    if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                        if (ShowMessageFX.YesNo(null, pxeModuleName,
                                                "Are you sure you want to change the reference no?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                            poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                            loadTableDetail();
                                        } else {
                                            loadRecordMaster();
                                            return;
                                        }
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().Master().setSourceNo("");
                        }
                        if ("error".equals(poJSON.get("result"))) {
                            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                            tfPOReceivingNo.setText("");
                            break;
                        }
                        break;

                }

                loadRecordMaster();

            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    };

    public void moveNext() {
        int lnReceiveQty = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
        apDetail.requestFocus();
        int lnNewvalue = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
        if (lnReceiveQty != lnNewvalue && (lnReceiveQty > 0
                && poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null
                && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId()))) {
            tfReturnQuantity.requestFocus();
        } else {
            pnDetail = JFXUtil.moveToNextRow(tblViewDetails);
            loadRecordDetail();
            if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                tfReturnQuantity.requestFocus();
            } else {
                tfBarcode.requestFocus();
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
            TableView currentTable = tblViewDetails;
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();

            switch (event.getCode()) {
                case TAB:
                case ENTER:
                    pbEntered = true;
                    CommonUtils.SetNextFocus(txtField);
                    event.consume();
                    break;
                case UP:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfReturnQuantity":
                            int lnQty = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
                            apDetail.requestFocus();
                            int lnNewvalue = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue();
                            if (lnQty != lnNewvalue && (lnQty > 0
                                    && poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null
                                    && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId()))) {
                                tfReturnQuantity.requestFocus();
                            } else {
                                pnDetail = JFXUtil.moveToPreviousRow(currentTable);
                                loadRecordDetail();
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                                    tfReturnQuantity.requestFocus();
                                } else {
                                    tfBarcode.requestFocus();
                                }
                                event.consume();
                            }
                            break;
                    }
                    break;
                case DOWN:
                    switch (lsID) {
                        case "tfBarcode":
                        case "tfReturnQuantity":
                            moveNext();
                            event.consume();
                            break;
                        default:
                            break;
                    }
                    break;
                case F3:
                    switch (lsID) {
                        case "tfSupplier":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the supplier name?\nPlease note that doing so will delete all purchase order receiving details.\n\nDo you wish to proceed?") == true) {
                                        poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        return;
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchSupplier(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfSupplier.setText("");
                                psSupplierId = "";
                                break;
                            }
                            psSupplierId = poPurchaseReturnController.PurchaseOrderReturn().Master().getSupplierId();
                            loadRecordMaster();
                            break;
                        case "tfReferenceNo":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the reference no?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                        poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        return;
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchPOReceiving(lsValue, false);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfReferenceNo.setText("");
                            } else {
                                JFXUtil.focusFirstTextField(apDetail);
                            }
                            loadRecordMaster();
                            break;
                        case "tfPOReceivingNo":
                            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() > 1) {
                                    if (ShowMessageFX.YesNo(null, pxeModuleName,
                                            "Are you sure you want to change the po receiving no?\nPlease note that doing so will delete all transaction details.\n\nDo you wish to proceed?") == true) {
                                        poPurchaseReturnController.PurchaseOrderReturn().removeDetails();
                                        loadTableDetail();
                                    } else {
                                        return;
                                    }
                                }
                            }

                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchPOReceiving(lsValue, true);
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                tfPOReceivingNo.setText("");
                            } else {
                                JFXUtil.focusFirstTextField(apDetail);
                            }
                            loadRecordMaster();
                            break;
                        case "tfBarcode":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchBarcode(lsValue, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReturnQuantity.requestFocus();
                                    return;
                                }
                                tfBarcode.setText("");
                                break;
                            }
                            loadTableDetail();

                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReturnQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                        case "tfDescription":
                            poJSON = poPurchaseReturnController.PurchaseOrderReturn().SearchDescription(lsValue, pnDetail);
                            lnRow = (int) poJSON.get("row");
                            if ("error".equals(poJSON.get("result"))) {
                                ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                                if (pnDetail != lnRow) {
                                    pnDetail = lnRow;
                                    loadRecordDetail();
                                    tfReturnQuantity.requestFocus();
                                    return;
                                }
                                tfDescription.setText("");
                                break;
                            }
                            loadTableDetail();
                            Platform.runLater(() -> {
                                PauseTransition delay = new PauseTransition(Duration.seconds(0.50));
                                delay.setOnFinished(event1 -> {
                                    tfReturnQuantity.requestFocus();
                                });
                                delay.play();
                            });
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initTextFields() {
        Platform.runLater(() -> {
            JFXUtil.setVerticalScroll(taRemarks);
        });
        JFXUtil.setFocusListener(txtMaster_Focus, tfSupplier, tfReferenceNo, tfPOReceivingNo);
        JFXUtil.setFocusListener(txtArea_Focus, taRemarks);
        JFXUtil.setFocusListener(txtDetail_Focus, tfBarcode, tfDescription, tfReturnQuantity);

        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apBrowse, apMaster, apDetail);

        CustomCommonUtil.inputIntegersOnly(tfReceiveQuantity, tfReturnQuantity);
        CustomCommonUtil.inputDecimalOnly(tfCost);

    }

    boolean pbSuccess = true;

    private void datepicker_Action(ActionEvent event) {
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "success");

        try {
            Object source = event.getSource();
            if (source instanceof DatePicker) {
                DatePicker datePicker = (DatePicker) source;
                String inputText = datePicker.getEditor().getText();
                SimpleDateFormat sdfFormat = new SimpleDateFormat(SQLUtil.FORMAT_SHORT_DATE);
                LocalDate currentDate = null;
                LocalDate selectedDate = null;
                String lsServerDate = "";
                String lsTransDate = "";
                String lsSelectedDate = "";
                String lsReceivingDate = "";
                LocalDate receivingDate = null;
                lastFocusedTextField = datePicker;
                previousSearchedTextField = null;

                JFXUtil.JFXUtilDateResult ldtResult = JFXUtil.processDate(inputText, datePicker);
                poJSON = ldtResult.poJSON;
                if ("error".equals(poJSON.get("result"))) {
                    ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    loadRecordMaster();
                    return;
                }
                if (inputText == null || "".equals(inputText) || "01/01/1900".equals(inputText)) {
                    return;
                }
                selectedDate = ldtResult.selectedDate;

                switch (datePicker.getId()) {
                    case "dpTransactionDate":
                        if (poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.ADDNEW
                                || poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.UPDATE) {
                            lsServerDate = sdfFormat.format(oApp.getServerDate());
                            lsTransDate = sdfFormat.format(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionDate());
                            lsSelectedDate = sdfFormat.format(SQLUtil.toDate(JFXUtil.convertToIsoFormat(inputText),  SQLUtil.FORMAT_SHORT_DATE));
                            currentDate = LocalDate.parse(lsServerDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                            selectedDate = LocalDate.parse(lsSelectedDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));

                            if (selectedDate.isAfter(currentDate)) {
                                poJSON.put("result", "error");
                                poJSON.put("message", "Future dates are not allowed.");
                                pbSuccess = false;
                            }
                            if (poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo() != null && !"".equals(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo())) {
                                lsReceivingDate = sdfFormat.format(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getTransactionDate());
                                receivingDate = LocalDate.parse(lsReceivingDate, DateTimeFormatter.ofPattern(SQLUtil.FORMAT_SHORT_DATE));
                                if (selectedDate.isBefore(receivingDate)) {
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Transaction date cannot be before the receiving date.");
                                    pbSuccess = false;
                                }
                            } else {
                                if (pbSuccess && !lsServerDate.equals(lsSelectedDate) && pnEditMode == EditMode.ADDNEW) {
                                    poJSON.put("result", "error");
                                    poJSON.put("message", "Select PO Receiving before changing the transaction date.");
                                    pbSuccess = false;
                                }
                            }
                            if (pbSuccess && ((poPurchaseReturnController.PurchaseOrderReturn().getEditMode() == EditMode.UPDATE && !lsTransDate.equals(lsSelectedDate))
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
                                                poPurchaseReturnController.PurchaseOrderReturn().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
                                            }
                                        }
                                    } else {
                                        pbSuccess = false;
                                    }
                                } else {
                                    poPurchaseReturnController.PurchaseOrderReturn().Master().setTransactionDate((SQLUtil.toDate(lsSelectedDate, SQLUtil.FORMAT_SHORT_DATE)));
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
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initDatePickers() {
        JFXUtil.setDatePickerFormat("MM/dd/yyyy",
dpTransactionDate);

        JFXUtil.setActionListener(this::datepicker_Action, dpTransactionDate);

    }

    public void initDetailsGrid() {
        JFXUtil.setColumnCenter(tblRowNoDetail, tblReceiveQuantityDetail, tblReturnQuantityDetail);
        JFXUtil.setColumnLeft(tblBarcodeDetail, tblDescriptionDetail);
        JFXUtil.setColumnRight(tblCostDetail, tblTotalDetail);
        JFXUtil.setColumnsIndexAndDisableReordering(tblViewDetails);

        filteredDataDetail = new FilteredList<>(details_data, b -> true);

        SortedList<ModelPurchaseOrderReturn_Detail> sortedData = new SortedList<>(filteredDataDetail);
        sortedData.comparatorProperty().bind(tblViewDetails.comparatorProperty());
        tblViewDetails.setItems(sortedData);
    }

    public void clearTextFields() {
        previousSearchedTextField = null;
        lastFocusedTextField = null;
        dpTransactionDate.setValue(null);

        JFXUtil.clearTextFields(apMaster, apDetail);

        loadRecordMaster();
        loadTableDetail();
    }

    public void loadRecordSearch() {
        try {
            lblSource.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Company().getCompanyName() + " - " + poPurchaseReturnController.PurchaseOrderReturn().Master().Industry().getDescription());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void loadRecordDetail() {
        try {
            if (pnDetail < 0 || pnDetail > poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) {
                return;
            }
            boolean lbDisable = poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getEditMode() == EditMode.ADDNEW;
            JFXUtil.setDisabled(!lbDisable, tfBarcode, tfDescription);

            tfBarcode.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getBarCode());
            tfDescription.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().getDescription());
            tfBrand.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Brand().getDescription());
            tfModel.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Model().getDescription());
            tfColor.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Color().getDescription());
            tfInventoryType.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().InventoryType().getDescription());
            tfMeasure.setText(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).Inventory().Measure().getDescription());

            tfCost.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getUnitPrce(), true));
            tfReceiveQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(pnDetail).intValue()));
            tfReturnQuantity.setText(String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getQuantity().intValue()));

            JFXUtil.updateCaretPositions(apDetail);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    public void loadRecordMaster() {
        boolean lbDisable = pnEditMode == EditMode.ADDNEW;

        JFXUtil.setDisabled(!lbDisable, tfSupplier, tfReferenceNo, tfPOReceivingNo);

        try {

            Platform.runLater(() -> {
                boolean lbPrintStat = pnEditMode == EditMode.READY;
                String lsActive = poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus();
                String lsStat = "UNKNOWN";
                switch (lsActive) {
                    case PurchaseOrderReturnStatus.POSTED:
                        lsStat = "POSTED";
                        break;
                    case PurchaseOrderReturnStatus.PAID:
                        lsStat = "PAID";
                        break;
                    case PurchaseOrderReturnStatus.CONFIRMED:
                        lsStat = "CONFIRMED";
                        break;
                    case PurchaseOrderReturnStatus.OPEN:
                        lsStat = "OPEN";
                        break;
                    case PurchaseOrderReturnStatus.RETURNED:
                        lsStat = "RETURNED";
                        break;
                    case PurchaseOrderReturnStatus.VOID:
                        lsStat = "VOIDED";
                        lbPrintStat = false;
                        break;
                    case PurchaseOrderReturnStatus.CANCELLED:
                        lsStat = "CANCELLED";
                        break;
                    default:
                        lsStat = "UNKNOWN";
                        break;

                }
                lblStatus.setText(lsStat);
                JFXUtil.setButtonsVisibility(lbPrintStat, btnPrint);
            });

            poPurchaseReturnController.PurchaseOrderReturn().computeFields();

            // Transaction Date
            tfTransactionNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionNo());
            String lsTransactionDate = CustomCommonUtil.formatDateToShortString(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionDate());
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(lsTransactionDate, "yyyy-MM-dd"));

            tfSupplier.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().Supplier().getCompanyName());
            tfReferenceNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().PurchaseOrderReceivingMaster().getReferenceNo());
            tfPOReceivingNo.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getSourceNo());
            taRemarks.setText(poPurchaseReturnController.PurchaseOrderReturn().Master().getRemarks());

            tfTotal.setText(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionTotal().doubleValue(), true));

            JFXUtil.updateCaretPositions(apMaster);
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void tableKeyEvents(KeyEvent event) {
        if (details_data.size() > 0) {
            TableView currentTable = (TableView) event.getSource();
            TablePosition focusedCell = currentTable.getFocusModel().getFocusedCell();
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
                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                    tfReturnQuantity.requestFocus();
                } else {
                    tfBarcode.requestFocus();
                }
                event.consume();
            }
        }
    }

    public void initTableOnClick() {
        tblViewDetails.setOnMouseClicked(event -> {
            if (details_data.size() > 0) {
                if (event.getClickCount() == 1) {  // Detect single click (or use another condition for double click)
                    pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                    loadRecordDetail();
                    if (poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(pnDetail).getStockId().equals("")) {
                        tfReturnQuantity.requestFocus();
                    } else {
                        tfBarcode.requestFocus();
                    }
                }
            }
        });
        tblViewDetails.addEventFilter(KeyEvent.KEY_PRESSED, this::tableKeyEvents);
        JFXUtil.adjustColumnForScrollbar(tblViewDetails); // need to use computed-size in min-width of the column to work
    }

    public void loadTableDetail() {
        pbEntered = false;
        // Setting data to table detail
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        tblViewDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;"); // Adjust the size as needed

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
//                Thread.sleep(1000);
                // contains try catch, for loop of loading data to observable list until loadTab()
                Platform.runLater(() -> {
                    int lnCtr;
                    details_data.clear();
                    try {

                        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                            lnCtr = poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1;
                            while (lnCtr >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId() == null || poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().Detail().remove(lnCtr);
                                }
                                lnCtr--;
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) >= 0) {
                                if (poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId() != null && !poPurchaseReturnController.PurchaseOrderReturn().Detail(poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1).getStockId().equals("")) {
                                    poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                                }
                            }

                            if ((poPurchaseReturnController.PurchaseOrderReturn().getDetailCount() - 1) < 0) {
                                poPurchaseReturnController.PurchaseOrderReturn().AddDetail();
                            }
                        }

                        double lnTotal = 0.0;
                        for (lnCtr = 0; lnCtr < poPurchaseReturnController.PurchaseOrderReturn().getDetailCount(); lnCtr++) {
                            try {
                                lnTotal = poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce().doubleValue() * poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue();
                            } catch (Exception e) {
                            }

                            details_data.add(
                                    new ModelPurchaseOrderReturn_Detail(String.valueOf(lnCtr + 1),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getBarCode()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).Inventory().getDescription()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getUnitPrce(), true)),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().getReceiveQty(lnCtr).intValue()),
                                            String.valueOf(poPurchaseReturnController.PurchaseOrderReturn().Detail(lnCtr).getQuantity().intValue()),
                                            String.valueOf(CustomCommonUtil.setIntegerValueToDecimalFormat(lnTotal, true))) //identify total
                            );
                        }

                        if (pnDetail < 0 || pnDetail
                                >= details_data.size()) {
                            if (!details_data.isEmpty()) {
                                /* FOCUS ON FIRST ROW */
                                tblViewDetails.getSelectionModel().select(0);
                                tblViewDetails.getFocusModel().focus(0);
                                pnDetail = tblViewDetails.getSelectionModel().getSelectedIndex();
                                loadRecordDetail();
                            }
                        } else {
                            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                            tblViewDetails.getSelectionModel().select(pnDetail);
                            tblViewDetails.getFocusModel().focus(pnDetail);
                            loadRecordDetail();
                        }
                        loadRecordMaster();
                    } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
                        Logger.getLogger(PurchaseOrderReturn_EntrySPMCController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                    }

                });

                return null;
            }

            @Override
            protected void succeeded() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                } else {
                    tblViewDetails.toFront();
                }
                progressIndicator.setVisible(false);

            }

            @Override
            protected void failed() {
                if (details_data == null || details_data.isEmpty()) {
                    tblViewDetails.setPlaceholder(placeholderLabel);
                }
                progressIndicator.setVisible(false);
            }

        };
        new Thread(task).start(); // Run task in background
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);

        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSearch, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnPrint, btnHistory);
        JFXUtil.setButtonsVisibility(lbShow3, btnBrowse, btnClose);

//        apMaster.setDisable(!lbShow);
        JFXUtil.setDisabled(!lbShow, dpTransactionDate, taRemarks, apDetail);

        switch (poPurchaseReturnController.PurchaseOrderReturn().Master().getTransactionStatus()) {
            case PurchaseOrderReturnStatus.POSTED:
            case PurchaseOrderReturnStatus.PAID:
                JFXUtil.setButtonsVisibility(false, btnUpdate);
                break;
            case PurchaseOrderReturnStatus.VOID:
            case PurchaseOrderReturnStatus.CANCELLED:
                JFXUtil.setButtonsVisibility(false, btnUpdate, btnPrint);
                break;
        }
    }

}
