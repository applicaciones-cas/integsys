package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.sales.t1.services.SalesControllers;
import ph.com.guanzongroup.integsys.model.ModelListParameter;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Maynard / Arsiela
 */
public class SalesagentController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Sales Agent";
    private GRiderCAS oApp;
    private SalesControllers oTrans;
    private JSONObject poJSON;
    private int pnEditMode;
    private int pnListRow;

    private ObservableList<ModelListParameter> ListData = FXCollections.observableArrayList();

    @FXML
    private AnchorPane ChildAnchorPane, apMaster, apSearchMaster;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnNew, btnSave, btnUpdate, btnCancel, btnActivate, btnClose;
    @FXML
    private FontAwesomeIconView faActivate;
    @FXML
    private CheckBox cbActive;
    @FXML
    private TableView tblList;
    @FXML
    private TableColumn tblClientId, tblSalesAgent;
    @FXML
    private TextField tfSalesAgent, tfProfession, tfCompany, tfPosition, tfSearchSalesAgent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        oTrans = new SalesControllers(oApp, null);
        pnEditMode = EditMode.UNKNOWN;
        initButton(pnEditMode);
        initTextFields();
        clearFields();
        loadTableDetail();
    }

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

    @FXML
    void cmdButton_Click(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnNew":
                    poJSON = oTrans.SalesAgent().newRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        pnEditMode = EditMode.UNKNOWN;
                        return;
                    }
                    pnEditMode = oTrans.SalesAgent().getEditMode();
                    break;
                case "btnUpdate":
                    poJSON = oTrans.SalesAgent().updateRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }

                    pnEditMode = oTrans.SalesAgent().getEditMode();
                    break;
                case "btnSave":
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure you want to save the record?") == true) {
                        poJSON = oTrans.SalesAgent().saveRecord();
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                        ShowMessageFX.Information("Record saved successfully", pxeModuleName, null);
                        pnEditMode = EditMode.UNKNOWN;
                    } else {
                        return;
                    }
                    break;

                case "btnCancel":
                    if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to disregard changes?") == true) {
                        pnEditMode = EditMode.UNKNOWN;
                    } else {
                        return;
                    }

                    break;
                case "btnActivate":
                    if (btnActivate.getText().equals("Activate")) {
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Activate this Parameter?") == true) {
                            poJSON = oTrans.SalesAgent().activateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to Deactivate this Parameter?") == true) {
                            poJSON = oTrans.SalesAgent().deactivateRecord();
                            if ("error".equals((String) poJSON.get("result"))) {
                                System.err.println((String) poJSON.get("message"));
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                return;
                            }
                        } else {
                            return;
                        }
                    }

                    ShowMessageFX.Information("Record updated successfully", pxeModuleName, null);
                    pnEditMode = EditMode.UNKNOWN;
                    break;
                case "btnClose":
                    unloadForm appUnload = new unloadForm();
                    if (ShowMessageFX.OkayCancel(null, "Close Tab", "Are you sure you want to close this Tab?") == true) {
                        appUnload.unloadForm(ChildAnchorPane, oApp, pxeModuleName);
                    } else {
                        return;
                    }

                    break;
//                case "btnBrowse":
//                    poJSON = oTrans.SalesAgent().searchRecord(tfSearchSalesman.getText(), false);
//                    pnEditMode = EditMode.READY;
//                    if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
//                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
//                        tfSearchSalesman.requestFocus();
//                        return;
//                    } else {
//                        loadRecord();
//                    }
//                    break;
                default:
                    ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");
                    return;
            }

            if (lsButton.equals("btnSave") || lsButton.equals("btnCancel") || lsButton.equals("btnActivate")) {
                oTrans.SalesAgent().resetModel();
                clearFields();
            }

            initButton(pnEditMode);
            loadRecord();
            loadTableDetail();

        } catch (SQLException | GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    private void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        boolean lbShow2 = fnValue == EditMode.READY;
        boolean lbShow3 = (fnValue == EditMode.READY || fnValue == EditMode.UNKNOWN);
        // Manage visibility and managed state of other buttons
        JFXUtil.setButtonsVisibility(!lbShow, btnNew);
        JFXUtil.setButtonsVisibility(lbShow, btnSave, btnCancel);
        JFXUtil.setButtonsVisibility(lbShow2, btnUpdate, btnActivate);
        JFXUtil.setButtonsVisibility(lbShow3, btnClose);

        JFXUtil.setDisabled(!lbShow, apMaster);
    }

    private void initTextFields() {
        /*textFields FOCUSED PROPERTY*/
        JFXUtil.setFocusListener(txtField_Focus, tfSalesAgent, tfProfession, tfCompany, tfPosition);
        /*textFields KeyPressed PROPERTY*/
        JFXUtil.setKeyPressedListener(this::txtField_KeyPressed, apMaster, apSearchMaster);
        JFXUtil.disableArrowNavigation(tblList);
    }

    private void txtField_KeyPressed(KeyEvent event) {
        try {
            TextField textField = (TextField) event.getSource();
            String lsTextField = textField.getId();
            String lsValue = textField.getText();
            switch (event.getCode()) {
                case F3:
                    switch (lsTextField) {
                        case "tfSearchSalesAgent":
                            /*Browse Primary*/
                            poJSON = oTrans.SalesAgent().searchRecord(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                tfSearchSalesAgent.requestFocus();
                            } else {
                                loadRecord();
                            }
                            break;
                        case "tfSalesAgent":
                            /*search employee*/
                            poJSON = oTrans.SalesAgent().SearchClient(lsValue, false);
                            if ("error".equalsIgnoreCase(poJSON.get("result").toString())) {
                                ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                                tfSalesAgent.requestFocus();
                            } else {
                                loadRecord();
                            }
                            break;
                    }
            }
            switch (event.getCode()) {
                case ENTER:
                    CommonUtils.SetNextFocus(textField);
                case DOWN:
                    CommonUtils.SetNextFocus(textField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(textField);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField txtField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsTextField = txtField.getId();
        String lsValue = txtField.getText();

        if (lsValue == null) {
            return;
        }

        if (!nv) {
            /*Lost Focus*/
            switch (lsTextField) {
                case "tfSalesAgent":
                    if (lsValue.isEmpty()) {
                        poJSON = oTrans.SalesAgent().getModel().setClientId("");
                        if ("error".equals((String) poJSON.get("result"))) {
                            System.err.println((String) poJSON.get("message"));
                            ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                            return;
                        }
                    }
                    break;
                case "tfProfession":
                    poJSON = oTrans.SalesAgent().getModel().setProfession(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    break;
                case "tfCompany":
                    poJSON = oTrans.SalesAgent().getModel().setCompany(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    break;
                case "tfPosition":
                    poJSON = oTrans.SalesAgent().getModel().setPosition(lsValue);
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning((String) poJSON.get("message"), pxeModuleName, null);
                        return;
                    }
                    break;
            }
        } else {
            txtField.selectAll();
        }
    };

    private void loadRecord() {
        try {
            boolean lbDisable = pnEditMode == EditMode.ADDNEW;
            JFXUtil.setDisabled(!lbDisable, tfSalesAgent);

            boolean lbActive = oTrans.SalesAgent().getModel().getRecordStatus();
            cbActive.setSelected(lbActive);
            if (lbActive) {
                btnActivate.setText("Deactivate");
                faActivate.setGlyphName("CLOSE");
            } else {
                btnActivate.setText("Activate");
                faActivate.setGlyphName("CHECK");
            }

            tfSalesAgent.setText(oTrans.SalesAgent().getModel().Client().getCompanyName());
            tfProfession.setText(oTrans.SalesAgent().getModel().getProfession());
            tfCompany.setText(oTrans.SalesAgent().getModel().getCompany());
            tfPosition.setText(oTrans.SalesAgent().getModel().getPosition());
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }

    private void clearFields() {
        btnActivate.setText("Activate");
        cbActive.setSelected(false);

        JFXUtil.clearTextFields(apMaster);
    }

    private void loadTableDetail() {
        int lnCtr;
        ListData.clear();
        try {

            poJSON = oTrans.SalesAgent().loadModelList();
            if ("error".equals((String) poJSON.get("result"))) {
                System.err.println((String) poJSON.get("message"));
                ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);

                return;
            }

            int lnItem = oTrans.SalesAgent().getModelCount();
            if (lnItem <= 0) {
                return;
            }
            for (lnCtr = 0; lnCtr <= lnItem - 1; lnCtr++) {
                ListData.add(new ModelListParameter(
                        (String) oTrans.SalesAgent().ModelList(lnCtr).getClientId(),
                        (String) oTrans.SalesAgent().ModelList(lnCtr).Client().getCompanyName(),
                        "",
                        ""));

            }
            if (pnListRow < 0 || pnListRow
                    >= ListData.size()) {
                if (!ListData.isEmpty()) {
                    /* FOCUS ON FIRST ROW */
                    JFXUtil.selectAndFocusRow(tblList, 0);
                    pnListRow = tblList.getSelectionModel().getSelectedIndex();
                    loadRecord();
                }
            } else {
                /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
                JFXUtil.selectAndFocusRow(tblList, pnListRow);
                loadRecord();
            }
            initListGrid();

        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void initListGrid() {
        JFXUtil.setColumnLeft(tblClientId, tblSalesAgent);
        JFXUtil.setColumnsIndexAndDisableReordering(tblList);

        tblList.setItems(ListData);
        tblList.autosize();
    }

    @FXML
    void tblList_Clicked(MouseEvent event) {
        try {
            pnListRow = tblList.getSelectionModel().getSelectedIndex();
            if (pnListRow >= 0) {
                oTrans.SalesAgent().openRecord(ListData.get(pnListRow).getIndex01());
                loadRecord();
                pnEditMode = oTrans.SalesAgent().getEditMode();
                initButton(pnEditMode);
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

}
