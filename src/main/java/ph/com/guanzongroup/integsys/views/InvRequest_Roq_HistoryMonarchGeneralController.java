/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

import ph.com.guanzongroup.integsys.model.ModelInvOrderDetail;
import ph.com.guanzongroup.integsys.model.ModelInvTableListInformation;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import static javafx.scene.input.KeyCode.TAB;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.cas.inv.warehouse.model.Model_Inv_Stock_Request_Detail;
import org.guanzon.cas.inv.warehouse.services.InvWarehouseControllers;
import org.guanzon.cas.inv.warehouse.status.StockRequestStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author User
 */
public class InvRequest_Roq_HistoryMonarchGeneralController implements Initializable, ScreenInterface{
    @FXML
    private String psFormName = "Inv Stock Request ROQ History Monarch General";
    unloadForm poUnload = new unloadForm();
    @FXML
    private AnchorPane AnchorMain;
    private GRiderCAS poApp;
    private String psOldDate = "";
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    private String psReferID = "";
    private String psTransNo = "";
    private InvWarehouseControllers invRequestController;
    private LogWrapper logWrapper;
    private JSONObject poJSON;
    private int pnTblInvDetailRow = -1;

    private int pnTblInformationRow = -1;
    private String brandID, categID;
    private ObservableList<ModelInvOrderDetail> invOrderDetail_data = FXCollections.observableArrayList();
    private ObservableList<ModelInvTableListInformation> tableListInformation_data = FXCollections.observableArrayList();
    private int pnEditMode;

    @FXML
    private TextField tfReservationQTY, tfOrderQuantity, tfTransactionNo, tfReferenceNo,
            tfSearchTransNo, tfSearchReferenceNo, tfBarCode, tfDescription;
    @FXML
    private TableColumn<ModelInvOrderDetail, String> tblBrandDetail,tblBarCodeDetail,tblDescriptionDetail, tblModelDetail, tblVariantDetail, tblColorDetail, tblInvTypeDetail, tblROQDetail, tblClassificationDetail, tblQOHDetail, tblReservationQtyDetail, tblOrderQuantityDetail;
    @FXML
    private TextField tfBrand, tfModel, tfInvType,
            tfVariant, tfColor, tfROQ, tfClassification, tfQOH;
    @FXML
    private Button btnBrowse, btnRetrieve, btnClose;
    @FXML
    private Label lblTransactionStatus, lblSource;
    @FXML
    private TableView<ModelInvOrderDetail> tblViewOrderDetails;
    @FXML
    private DatePicker dpTransactionDate;
    @FXML
    private TextArea taRemarks;
    @FXML
    private TableView<ModelInvTableListInformation> tableListInformation;

    @FXML
    private TableColumn<ModelInvTableListInformation, String> tblTransactionNo, tblReferenceNo, tblTransactionDate;

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    public Stage getStage() {
        return (Stage) AnchorMain.getScene().getWindow();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            invRequestController = new InvWarehouseControllers(poApp, logWrapper);
            invRequestController.StockRequest().setTransactionStatus(
                    StockRequestStatus.OPEN
            );
            poJSON = invRequestController.StockRequest().InitTransaction();
            if (!"success".equals(poJSON.get("result"))) {
                ShowMessageFX.Warning((String) poJSON.get("message"), "Search Information", null);
            }

            Platform.runLater((() -> {
                try {
                    //set edit mode to new transaction temporily to assign industry and company
                    invRequestController.StockRequest().NewTransaction();
                    invRequestController.StockRequest().Master().setIndustryId(psIndustryID);
                    invRequestController.StockRequest().Master().setCompanyID(psCompanyID);
                    invRequestController.StockRequest().Master().setCategoryId(psCategoryID);

                    initTableList();
                    initTableInvDetail();
                    loadRecordSearch();
                    ;                    //reset the transaction
                    invRequestController.StockRequest().InitTransaction();
                } catch (CloneNotSupportedException e) {
                    ShowMessageFX.Warning((String) e.getMessage(), "Search Information", null);
                }

            }));
            pnEditMode = EditMode.UNKNOWN;
            System.out.print("initReached...");
//                Platform.runLater(() -> btnRetrieve.fire());
            initButtons(pnEditMode);
            initFields(pnEditMode);

            initButtonsClickActions();
            initDatePickerActions();
            initTextFieldsProperty();

            initTextFieldKeyPressed();
            System.out.print("initReached...2");
            tableListInformation.setOnMouseClicked(this::tableListInformation_Clicked);
            tblViewOrderDetails.setOnMouseClicked(this::tblViewOrderDetails_Clicked);
        } catch (ExceptionInInitializerError ex) {
            Logger.getLogger(InvRequest_HistoryCarController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void clearDetailFields() {
        /* Detail Fields*/
        CustomCommonUtil.setText("", tfBrand, tfModel,
                tfColor, tfReservationQTY, tfQOH, tfInvType, tfVariant, tfROQ, tfClassification);
        CustomCommonUtil.setText("0", tfOrderQuantity);
    }

    private void tblViewOrderDetails_Clicked(MouseEvent event) {
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE || pnEditMode == EditMode.READY) {
            pnTblInvDetailRow = tblViewOrderDetails.getSelectionModel().getSelectedIndex();
            ModelInvOrderDetail selectedItem = tblViewOrderDetails.getSelectionModel().getSelectedItem();

            if (event.getClickCount() == 1) {
                clearDetailFields();
                if (selectedItem != null) {
                    if (pnTblInvDetailRow >= 0) {
                        loadDetail();
                        initDetailFocus();
                    }
                }
            }
        }
    }

    private void initDetailFocus() {
        if (pnEditMode == EditMode.UPDATE) {
            if (pnTblInvDetailRow >= 0) {
                if (!tfBrand.getText().isEmpty()) {
                    tfOrderQuantity.requestFocus();
                }
            }
        }
    }

    private void tableListInformation_Clicked(MouseEvent event) {
        poJSON = new JSONObject();
        pnTblInformationRow = tableListInformation.getSelectionModel().getSelectedIndex();
        if (pnTblInformationRow < 0 || pnTblInformationRow >= tableListInformation.getItems().size()) {
            ShowMessageFX.Warning("Please select valid order information.", "Warning", null);
            return;
        }

        if (event.getClickCount() == 2) {
            ModelInvTableListInformation loSelected = (ModelInvTableListInformation) tableListInformation.getSelectionModel().getSelectedItem();
            if (loSelected != null) {
                String lsTransactionNo = loSelected.getIndex01();
                try {
                    poJSON = invRequestController.StockRequest().InitTransaction();
                    if ("success".equals((String) poJSON.get("result"))) {
                        poJSON = invRequestController.StockRequest().OpenTransaction(lsTransactionNo);
                        if ("success".equals((String) poJSON.get("result"))) {
                            loadMaster();
                            initTableInvDetail();
                            loadTableInvDetail();
                            pnTblInvDetailRow = -1;
                            clearDetailFields();
                            pnEditMode = invRequestController.StockRequest().getEditMode();
                        } else {
                            ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                            pnEditMode = EditMode.UNKNOWN;
                        }
                        initButtons(pnEditMode);
                        initFields(pnEditMode);

                    }
                } catch (CloneNotSupportedException | SQLException | GuanzonException ex) {
                    Logger.getLogger(InvRequest_ConfirmationMcController.class
                            .getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Warning("Error loading data: " + ex.getMessage(), psFormName, null);
                }
            }
        }
    }

    private void loadRecordSearch() {
        try {

            lblSource.setText(invRequestController.StockRequest().Master().Company().getCompanyName() );

        } catch (GuanzonException | SQLException ex) {
            Logger.getLogger(InvRequest_EntryMcController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void txtField_KeyPressed(KeyEvent event) {
        TextField sourceField = (TextField) event.getSource();
        String fieldId = sourceField.getId();
        String value = sourceField.getText() == null ? "" : sourceField.getText();
        JSONObject loJSON = new JSONObject();
        try {
            if (event.getCode() == null) {
                return;
            }
            String lsValue = sourceField.getText().trim();
            switch (event.getCode()) {
                case TAB:
                case ENTER:
                case F3:
                    switch (fieldId) {
                        case "tfSearchTransNo":
                            System.out.print("Enter pressed");
                            invRequestController.StockRequest().Master().setIndustryId(psIndustryID);
                            invRequestController.StockRequest().Master().setCompanyID(psCompanyID);
                            invRequestController.StockRequest().Master().setCategoryId(psCategoryID);
                            invRequestController.StockRequest().setTransactionStatus("1024");
                            poJSON = invRequestController.StockRequest().searchTransaction();
                            if (!"error".equals((String) poJSON.get("result"))) {
                                pnTblInvDetailRow = -1;
                                loadMaster();
                                pnEditMode = invRequestController.StockRequest().getEditMode();
                                loadDetail();
                                loadTableList();
                                loadTableInvDetail();
                                initButtons(pnEditMode);
                            } else {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Search Information", null);
                            }
                            break;
                        case "tfSearchReferenceNo":
                            System.out.print("Enter pressed");
                            invRequestController.StockRequest().Master().setIndustryId(psIndustryID);
                            invRequestController.StockRequest().Master().setCompanyID(psCompanyID);
                            invRequestController.StockRequest().Master().setCategoryId(psCategoryID);
                            invRequestController.StockRequest().setTransactionStatus("1024");
                            poJSON = invRequestController.StockRequest().searchTransaction(true);
                            if (!"error".equals((String) poJSON.get("result"))) {
                                pnTblInvDetailRow = -1;
                                loadMaster();
                                pnEditMode = invRequestController.StockRequest().getEditMode();
                                loadDetail();
                                loadTableList();
                                loadTableInvDetail();
                                initButtons(pnEditMode);
                            } else {
                                ShowMessageFX.Warning((String) poJSON.get("message"), "Search Information", null);
                            }
                            break;

                    }
                    event.consume();
                    switch (fieldId) {
                        case "tfSearchTransNo":
                            CommonUtils.SetNextFocus((TextField) event.getSource());
                            break;

                    }
                    event.consume();
                    break;
            }
        } catch (Exception e) {
            ShowMessageFX.Error(getStage(), e.getMessage(), "Error", psFormName);
        }
    }

    private void handleButtonAction(ActionEvent event) {
        System.out.print("handle trigger reached");
        try {
            JSONObject loJSON = new JSONObject();
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnRetrieve":
                    System.out.print("loaded table this is btnRetrieve");
                    invRequestController.StockRequest().Master().setIndustryId(psIndustryID);
                    invRequestController.StockRequest().Master().setCompanyID(psCompanyID);
                    invRequestController.StockRequest().Master().setCategoryId(psCategoryID);
                    invRequestController.StockRequest().setTransactionStatus("1024");
                    loadTableList();
                    break;
                case "btnBrowse":
                    invRequestController.StockRequest().Master().setIndustryId(psIndustryID);
                    invRequestController.StockRequest().Master().setCompanyID(psCompanyID);
                    invRequestController.StockRequest().Master().setCategoryId(psCategoryID);
                    invRequestController.StockRequest().setTransactionStatus("1024");
                    loJSON = invRequestController.StockRequest().searchTransaction();

                    if (!"error".equals((String) loJSON.get("result"))) {
                        tblViewOrderDetails.getSelectionModel().clearSelection(pnTblInvDetailRow);
                        pnTblInvDetailRow = -1;
                        loadMaster();
                        pnEditMode = invRequestController.StockRequest().getEditMode();
                        loadTableInvDetail();
                        loadDetail();

                    } else {
                        ShowMessageFX.Warning((String) loJSON.get("message"), "Browse", null);
                    }
                    break;
                case "btnClose":
                    if (ShowMessageFX.YesNo("Are you sure you want to close this form?", psFormName, null)) {
                        if (poUnload != null) {
                            poUnload.unloadForm(AnchorMain, poApp, psFormName);
                        } else {
                            ShowMessageFX.Warning("Please notify the system administrator to configure the null value at the close button.", "Warning", null);
                        }
                    }
                    break;
            }
            initButtons(pnEditMode);
            initFields(EditMode.UNKNOWN);
        } catch (CloneNotSupportedException | SQLException | GuanzonException e) {
            ShowMessageFX.Error(getStage(), e.getMessage(), "Error", psFormName);

        }
    }
    private void loadTableList() {
        btnRetrieve.setDisable(true);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50); // Set size to 200x200
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER); // Center it

        tableListInformation.setPlaceholder(loadingPane); // Show while loading
        progressIndicator.setVisible(true); // Make sure it's visible

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    tableListInformation_data.clear();
                    JSONObject poJSON = invRequestController.StockRequest().getTableListInformation(psTransNo, psReferID);
                    if ("success".equals(poJSON.get("result"))) {
                        if (invRequestController.StockRequest().getINVMasterCount() > 0) {
                            for (int lnCntr = 0; lnCntr <= invRequestController.StockRequest().getINVMasterCount() - 1; lnCntr++) {
                                tableListInformation_data.add(new ModelInvTableListInformation(
                                        invRequestController.StockRequest().INVMaster(lnCntr).getTransactionNo(),
                                        invRequestController.StockRequest().INVMaster(lnCntr).getReferenceNo(),
                                        SQLUtil.dateFormat(invRequestController.StockRequest().INVMaster(lnCntr).getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE),
                                        ""));
                            }
                        } else {
                            tableListInformation_data.clear();
                        }
                    }

                    Platform.runLater(() -> {
                        if (tableListInformation_data.isEmpty()) {
                            tableListInformation.setPlaceholder(new Label("NO RECORD TO LOAD"));
                            tableListInformation.setItems(FXCollections.observableArrayList(tableListInformation_data));
                        } else {
                            tableListInformation.setItems(FXCollections.observableArrayList(tableListInformation_data));
                        }
                    });

                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(InvRequest_ConfirmationMcController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
                if (tableListInformation_data == null || tableListInformation_data.isEmpty()) {
                    tableListInformation.setPlaceholder(new Label("NO RECORD TO LOAD"));
                }
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                btnRetrieve.setDisable(false);
            }
        };
        new Thread(task).start(); // Run task in background
    }

    private void initTextFieldKeyPressed() {
        List<TextField> loTxtField = Arrays.asList(
                tfOrderQuantity, tfSearchTransNo, tfSearchReferenceNo
        );

        loTxtField.forEach(tf -> tf.setOnKeyPressed(event -> txtField_KeyPressed(event)));
    }

    private void initTextFieldsProperty() {
        tfSearchTransNo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    invRequestController.StockRequest().Master().setTransactionNo("");
                    tfSearchTransNo.setText("");
                    loadTableList();
                }

            }
        });
        tfSearchReferenceNo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.isEmpty()) {
                    invRequestController.StockRequest().Master().setReferenceNo("");
                    tfSearchReferenceNo.setText("");
                    loadTableList();
                }
            }
        });
    }

    

    private void initButtons(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE);
        CustomCommonUtil.setVisible(true, btnRetrieve, btnBrowse, btnClose);
        CustomCommonUtil.setManaged(true, btnRetrieve, btnBrowse, btnClose);

    }

    private void initButtonsClickActions() {
        List<Button> buttons = Arrays.asList(btnBrowse,
                btnRetrieve, btnClose);

        buttons.forEach(button -> button.setOnAction(this::handleButtonAction));
    }

    private void loadMaster() {
        try {

            tfTransactionNo.setText(invRequestController.StockRequest().Master().getTransactionNo());

            String lsStatus = "";
            switch (invRequestController.StockRequest().Master().getTransactionStatus()) {
                case StockRequestStatus.OPEN:
                    lsStatus = "OPEN";
                    break;
                case StockRequestStatus.CONFIRMED:
                    lsStatus = "CONFIRMED";
                    break;
                case StockRequestStatus.PROCESSED:
                    lsStatus = "PROCESSED";
                    break;
                case StockRequestStatus.CANCELLED:
                    lsStatus = "CANCELLED";
                    break;
                case StockRequestStatus.VOID:
                    lsStatus = "VOID";
                    break;
            }
            poJSON = invRequestController.StockRequest().SearchBranch(lsStatus, true);
            lblTransactionStatus.setText(lsStatus);
            dpTransactionDate.setOnAction(null);
            dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                    SQLUtil.dateFormat(invRequestController.StockRequest().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)
            ));

            initDatePickerActions();
            tfReferenceNo.setText(invRequestController.StockRequest().Master().getReferenceNo());

            taRemarks.setText(invRequestController.StockRequest().Master().getRemarks());

        } catch (SQLException | GuanzonException e) {
            ShowMessageFX.Error(getStage(), e.getMessage(), "Error", psFormName);
            System.exit(1);
        }
    }

    private void loadTableInvDetail() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-accent: #FF8201;");

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setStyle("-fx-background-color: transparent;");

        tblViewOrderDetails.setPlaceholder(loadingPane);
        progressIndicator.setVisible(true);

        Task<List<ModelInvOrderDetail>> task = new Task<List<ModelInvOrderDetail>>() {
            @Override
            protected List<ModelInvOrderDetail> call() throws Exception {
                try {
                   int detailCount = invRequestController.StockRequest().getDetailCount();      
                    if ((pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE)) {
                        Model_Inv_Stock_Request_Detail lastDetail = invRequestController.StockRequest().Detail(detailCount - 1);
                        if (lastDetail.getStockId() != null && !lastDetail.getStockId().isEmpty()) {
                            invRequestController.StockRequest().AddDetail();
                            detailCount++;
                        }
                    }   
                   
                    List<ModelInvOrderDetail> detailsList = new ArrayList<>();
                    
                    for (int lnCtr = 0; lnCtr < detailCount; lnCtr++) {
                        Model_Inv_Stock_Request_Detail detail = invRequestController.StockRequest().Detail(lnCtr);
                            
                        detailsList.add(new ModelInvOrderDetail(
                                detail.Inventory().Brand().getDescription(), 
                                detail.Inventory().getDescription(), 
                                detail.Inventory().getBarCode(), 
                                detail.Inventory().Model().getDescription(),
                                detail.Inventory().Variant().getDescription(),
                                detail.Inventory().Color().getDescription(),
                                detail.Inventory().InventoryType().getDescription(),
                                String.valueOf(detail.getRecommendedOrder()),
                                detail.getClassification(),
                                String.valueOf(detail.getQuantityOnHand()),
                                String.valueOf(detail.getReservedOrder()),
                                String.valueOf(detail.getQuantity())
                                
                        ));
                    }
                    
                    Platform.runLater(() -> {
                    invOrderDetail_data.setAll(detailsList); // ObservableList<ModelInvOrderDetail>
                    tblViewOrderDetails.setItems(invOrderDetail_data);
                    reselectLastRow();
                    initFields(pnEditMode);
                });

                    return detailsList;

                } catch (GuanzonException | SQLException ex) {
                    Logger.getLogger(InvRequest_EntryMcSpController.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
            }
        };

        new Thread(task).start();
    }

    private void loadDetail() {
            try {
                if (pnTblInvDetailRow >= 0) {

                    String lsBrand = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Brand().getDescription() != null) {
                        lsBrand = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Brand().getDescription();
                    }
                    tfBrand.setText(lsBrand);
                    
                   String lsDescription = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().getDescription() != null) {
                        lsDescription = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().getDescription();
                    }
                    tfDescription.setText(lsDescription);
                    
                    String lsBarCode = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().getBarCode() != null) {
                        lsBarCode = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().getBarCode();
                    }
                    tfBarCode.setText(lsBarCode);

                    
                    String lsModel = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Model().getDescription() != null) {
                        lsModel = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Model().getDescription();
                    }
                    tfModel.setText(lsModel);

                   

                    String lsVariant = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Variant().getDescription()!= null) {
                        lsVariant = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Variant().getDescription();
                    }
                    tfVariant.setText(lsVariant);

                    String lsColor = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Color().getDescription() != null) {
                        lsColor = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().Color().getDescription();
                    }
                    tfColor.setText(lsColor);
                    
                    String lsInvType = "";
                    
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().InventoryType().getDescription() != null) {
                        lsInvType = invRequestController.StockRequest().Detail(pnTblInvDetailRow).Inventory().InventoryType().getDescription();
                    }
                    tfInvType.setText(lsInvType);
                    
                    String lsROQ = "0";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).getRecommendedOrder() != 0) {
                        lsROQ = String.valueOf(invRequestController.StockRequest().Detail(pnTblInvDetailRow).getRecommendedOrder());
                    }
                    tfROQ.setText(lsROQ);
                    
                    String lsClassification = "";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).getClassification()!=null) {
                        lsClassification = String.valueOf(invRequestController.StockRequest().Detail(pnTblInvDetailRow).getClassification());
                    }
                    tfClassification.setText(lsClassification);
                    
                    String lsOnHand = "0";
                     
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).getQuantityOnHand()!= 0) {
                        lsOnHand = String.valueOf(invRequestController.StockRequest().Detail(pnTblInvDetailRow).getQuantityOnHand());
                    }
                    tfQOH.setText(lsOnHand);
                    
                    String lsReservationQTY = "0";
                    
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).getReservedOrder()!= 0) {
                        lsReservationQTY = String.valueOf(invRequestController.StockRequest().Detail(pnTblInvDetailRow).getReservedOrder());
                    }
                    tfReservationQTY.setText(lsReservationQTY);
                    
                    String lsOrderQuantity = "0.0";
                    if (invRequestController.StockRequest().Detail(pnTblInvDetailRow).getQuantity() != 0) {
                        lsOrderQuantity = String.valueOf(invRequestController.StockRequest().Detail(pnTblInvDetailRow).getQuantity());
                    }
                    tfOrderQuantity.setText(lsOrderQuantity);

                }
            } catch (SQLException | GuanzonException e) {
                ShowMessageFX.Error(getStage(), e.getMessage(), "Error",psFormName);
                System.exit(1);
            }
        }

    private void initDatePickerActions() {
        dpTransactionDate.setOnAction(e -> {
            if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
                LocalDate selectedLocalDate = dpTransactionDate.getValue();
                LocalDate transactionDate = new java.sql.Date(invRequestController.StockRequest().Master().getTransactionDate().getTime()).toLocalDate();
                if (selectedLocalDate == null) {
                    return;
                }
                LocalDate dateNow = LocalDate.now();
                psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                String lsReferNo = tfReferenceNo.getText().trim();
                boolean approved = true;
                if (pnEditMode == EditMode.UPDATE) {
                    psOldDate = CustomCommonUtil.formatLocalDateToShortString(transactionDate);
                    if (selectedLocalDate.isAfter(dateNow)) {
                        ShowMessageFX.Warning("Invalid to future date.", psFormName, null);
                        approved = false;
                    }

                    if (selectedLocalDate.isBefore(transactionDate) && lsReferNo.isEmpty()) {
                        ShowMessageFX.Warning("Invalid to backdate. Please enter a reference number first.", psFormName, null);
                        approved = false;
                    }
                    if (selectedLocalDate.isBefore(transactionDate) && !lsReferNo.isEmpty()) {
                        boolean proceed = ShowMessageFX.YesNo(
                                "You are changing the transaction date\n"
                                + "If YES, seek approval to proceed with the changed date.\n"
                                + "If NO, the transaction date will be remain.",
                                psFormName, null
                        );
                        if (proceed) {
                            if (poApp.getUserLevel() <= UserRight.ENCODER) {
                                poJSON = ShowDialogFX.getUserApproval(poApp);
                                if (!"success".equalsIgnoreCase((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    approved = false;
                                }
                            }
                        } else {
                            approved = false;
                        }
                    }
                }
                if (pnEditMode == EditMode.ADDNEW) {
                    if (selectedLocalDate.isAfter(dateNow)) {
                        ShowMessageFX.Warning("Invalid to future date.", psFormName, null);
                        approved = false;
                    }
                    if (selectedLocalDate.isBefore(dateNow) && lsReferNo.isEmpty()) {
                        ShowMessageFX.Warning("Invalid to backdate. Please enter a reference number first.", psFormName, null);
                        approved = false;
                    }

                    if (selectedLocalDate.isBefore(dateNow) && !lsReferNo.isEmpty()) {
                        boolean proceed = ShowMessageFX.YesNo(
                                "You selected a backdate with a reference number.\n\n"
                                + "If YES, seek approval to proceed with the backdate.\n"
                                + "If NO, the transaction date will be reset to today.",
                                "Backdate Confirmation", null
                        );
                        if (proceed) {
                            if (poApp.getUserLevel() <= UserRight.ENCODER) {
                                poJSON = ShowDialogFX.getUserApproval(poApp);
                                if (!"success".equalsIgnoreCase((String) poJSON.get("result"))) {
                                    ShowMessageFX.Warning((String) poJSON.get("message"), psFormName, null);
                                    approved = false;
                                }
                            }
                        } else {
                            approved = false;
                        }
                    }
                }
                if (approved) {
                    invRequestController.StockRequest().Master().setTransactionDate(
                            SQLUtil.toDate(selectedLocalDate.toString(), SQLUtil.FORMAT_SHORT_DATE));
                } else {
                    if (pnEditMode == EditMode.ADDNEW) {
                        dpTransactionDate.setValue(dateNow);
                        invRequestController.StockRequest().Master().setTransactionDate(
                                SQLUtil.toDate(dateNow.toString(), SQLUtil.FORMAT_SHORT_DATE));
                    } else if (pnEditMode == EditMode.UPDATE) {
                        invRequestController.StockRequest().Master().setTransactionDate(
                                SQLUtil.toDate(psOldDate, SQLUtil.FORMAT_SHORT_DATE));
                    }

                }
                dpTransactionDate.setValue(CustomCommonUtil.parseDateStringToLocalDate(
                        SQLUtil.dateFormat(invRequestController.StockRequest().Master().getTransactionDate(), SQLUtil.FORMAT_SHORT_DATE)));
            }
        }
        );

    }

    private void initFields(int fnEditMode) {
        boolean lbShow = (fnEditMode == EditMode.ADDNEW || fnEditMode == EditMode.UPDATE);
        /*Master Fields */
        CustomCommonUtil.setDisable(!lbShow,
                dpTransactionDate, tfTransactionNo, taRemarks,
                tfReferenceNo);
        CustomCommonUtil.setDisable(!lbShow,
                tfOrderQuantity);
        CustomCommonUtil.setDisable(true,
                tfInvType, tfVariant, tfColor, tfReservationQTY,
                tfQOH, tfROQ, tfClassification, tfModel, tfBrand, tfDescription, tfBarCode);
        if (!tfReferenceNo.getText().isEmpty()) {
            dpTransactionDate.setDisable(!lbShow);
        }

    }

    private void reselectLastRow() {
        if (pnTblInvDetailRow >= 0 && pnTblInvDetailRow < tblViewOrderDetails.getItems().size()) {
            tblViewOrderDetails.getSelectionModel().clearAndSelect(pnTblInvDetailRow);
            tblViewOrderDetails.getSelectionModel().focus(pnTblInvDetailRow); // Scroll to the selected row if needed
        }
    }

    private void initTableList() {

        tblTransactionNo.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblReferenceNo.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblTransactionDate.setCellValueFactory(new PropertyValueFactory<>("index03"));

        tableListInformation.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tableListInformation.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                header.setReordering(false);
            });
        });
    }

    private void initTableInvDetail() {

        tblBrandDetail.setCellValueFactory(new PropertyValueFactory<>("index01"));
        tblDescriptionDetail.setCellValueFactory(new PropertyValueFactory<>("index02"));
        tblBarCodeDetail.setCellValueFactory(new PropertyValueFactory<>("index03"));
        tblModelDetail.setCellValueFactory(new PropertyValueFactory<>("index04"));
        tblVariantDetail.setCellValueFactory(new PropertyValueFactory<>("index05"));
        tblColorDetail.setCellValueFactory(new PropertyValueFactory<>("index06"));
        tblInvTypeDetail.setCellValueFactory(new PropertyValueFactory<>("index07"));
        tblROQDetail.setCellValueFactory(new PropertyValueFactory<>("index08"));
        tblClassificationDetail.setCellValueFactory(new PropertyValueFactory<>("index09"));
        tblQOHDetail.setCellValueFactory(new PropertyValueFactory<>("index10"));
        tblReservationQtyDetail.setCellValueFactory(new PropertyValueFactory<>("index11"));
        tblOrderQuantityDetail.setCellValueFactory(new PropertyValueFactory<>("index12"));
        // Prevent column reordering
        tblViewOrderDetails.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tblViewOrderDetails.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    header.setReordering(false);
                });
            }
        });
    }}

    
