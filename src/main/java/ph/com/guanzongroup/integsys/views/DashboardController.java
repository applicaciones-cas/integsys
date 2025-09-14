package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.integsys.utilities.JFXUtil;

public class DashboardController implements Initializable {
    private final String pxeModuleName = "Computerized Accounting System";
    private GRiderCAS oApp;
    private String lastClickedBtnLeftSideBar = "";
    private String lastClickedBtnRightSideBar = "";
    private String psDefaultScreenFXML = "/ph/com/guanzongroup/integsys/views/Login.fxml";
    private String psDefaultScreenFXML2 = "/ph/com/guanzongroup/integsys/views/DefaultScreen.fxml";
    private int notificationCount = 0;
    private int cartCount = 0;

    private ToggleGroup toggleGroup;
    private static ToggleButton[] toggleBtnLeftUpperSideBar;
    private static Tooltip[] sideBarLeftUpperToolTip;

    private ToggleGroup toggleGroupLowerBtn;
    private static ToggleButton[] toggleBtnLeftLowerSideBar;
    private static Tooltip[] sideBarLeftLowerToolTip;

    private ToggleGroup toggleGroupRightSideBar;
    private static ToggleButton[] toggleBtnRightSideBar;
    private static Tooltip[] sideBarRightToolTip;
    private Map<TreeItem<String>, String> menuLocationMap = new HashMap<>();
    private boolean isListenerLeftAdded = false;
    private boolean isListenerRightAdded = false;

    private List<JSONObject> flatMenuItems;
    private int userLevel; // User's access level
    private int targetTabIndex = -1;
    private int intIndex = -1;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isFromFilter;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    public String psUserIndustryId = "";
    public String psUserCompanyId = "";
    List<String> tabName = new ArrayList<>();
    String sformname = "";
    boolean lbproceed = false;
    
    @FXML
    private AnchorPane MainAnchor;
    @FXML
    private StackPane MainStack;
    @FXML
    private BorderPane main_container;
    @FXML
    private StackPane top_navbar;
    @FXML
    private Button btnMinimize;
    @FXML
    private Button btnClose;
    @FXML
    private AnchorPane anchorIconMenu;
    @FXML
    private VBox nav_bar;
    @FXML
    private ToggleButton btnInventory;
    @FXML
    private ToggleButton btnPurchasing;
    @FXML
    private ToggleButton btnSales;
    @FXML
    private ToggleButton btnServiceRepair;
    @FXML
    private ToggleButton btnAccountsReceivable;
    @FXML
    private ToggleButton btnGeneralAccounting;
    @FXML
    private ToggleButton btnOthers;
    @FXML
    private ToggleButton btnDelivery;
    @FXML
    private AnchorPane anchorIconMenu1;
    @FXML
    private VBox nav_bar1;
    @FXML
    private ToggleButton btnHelp;
    @FXML
    private ToggleButton btnLogout;
    @FXML
    private Label AppUser;
    @FXML
    private Label lblVersion;
    @FXML
    private AnchorPane apClock;
    @FXML
    private Label DateAndTime;
    @FXML
    private AnchorPane anchorSpacex;
    @FXML
    private AnchorPane anchorSpace;
    @FXML
    private StackPane workingSpace;
    @FXML
    private Pane pane;
    @FXML
    private TabPane tabpane;
    @FXML
    private AnchorPane anchorRightSideBarMenu;
    @FXML
    private TreeView<String> tvRightSideBar;
    @FXML
    private AnchorPane anchorLeftSideBarMenu;
    @FXML
    private TreeView tvLeftSideBar;
    @FXML
    private AnchorPane anchorIconMenu11;
    @FXML
    private VBox nav_bar11;
    @FXML
    private ToggleButton btnSysMonitor;
    @FXML
    private AnchorPane badgeNotification;
    @FXML
    private Label lblNotifCount;
    @FXML
    private ToggleButton btnAddToCart;
    @FXML
    private Label lblAddToCartCount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            setScene(loadAnimateAnchor(psDefaultScreenFXML));
            setPane();
            initMenu();
            ToggleGroupControlUpperLeftSideBar();
            ToggleGroupControlLowerLeftSideBar();
            ToggleGroupControlRightSideBar();
            loadUserInfo();
            setAppVersion("v1.00.01");
            checkDepartment();
            getTime();
            initButtonClickActions();
            notificationChecker();
            setTreeViewStyle(tvLeftSideBar);
            setTreeViewStyle(tvRightSideBar);

            psIndustryID = psUserIndustryId;
            psCompanyID = psUserCompanyId;

            setDropShadowEffectsLeftSideBar(anchorLeftSideBarMenu);
            setDropShadowEffectsRightSideBar(anchorRightSideBarMenu);
            Platform.runLater(() -> {
                AnchorPane root = (AnchorPane) MainAnchor;
                Scene scene = root.getScene();
                if (scene != null) {
                    setKeyEvent(scene);
                } else {
                    root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene != null) {
                            setKeyEvent(newScene);
                        }
                    });
                }
            });

            JFXUtil.applyToggleHoverAnimation(btnInventory, btnPurchasing, btnSales, btnServiceRepair,
                    btnAccountsReceivable, btnGeneralAccounting, btnOthers, btnDelivery, btnHelp, btnLogout,
                    btnSysMonitor, btnAddToCart);
            JFXUtil.applyHoverFadeToButtons("#FFFFFF", "#552B00", btnMinimize, btnClose);
            JFXUtil.placeClockInAnchorPane(apClock, 25);

            monitorMenuItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

    @FXML
    private void switchInventory(ActionEvent event) {
    }

    @FXML
    private void switchPurchasing(ActionEvent event) {
    }

    @FXML
    private void switchSales(ActionEvent event) {
    }

    @FXML
    private void switchServiceRepair(ActionEvent event) {
    }

    @FXML
    private void switchAccountsReceivable(ActionEvent event) {
    }

    @FXML
    private void switchGeneralAccounting(ActionEvent event) {
    }

    @FXML
    private void switchOthers(ActionEvent event) {
    }

    @FXML
    private void switchDelivery(ActionEvent event) {
    }

    @FXML
    private void switchHelp(ActionEvent event) {
    }

    @FXML
    private void switchLogout(ActionEvent event) {
    }

    @FXML
    private void switchSysMonitor(ActionEvent event) {
    }

    @FXML
    private void switchAddToCart(ActionEvent event) {
    }
    
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }

    public void setUserIndustry(String lsIndustryId) {
        psUserIndustryId = lsIndustryId;
    }

    public void setUserCompany(String lsCompanyId) {
        psUserCompanyId = lsCompanyId;
    }
    
    private Pane loadAnimateAnchor(String fxml) {
        ScreenInterface fxObj = getController(fxml);
        fxObj.setGRider(oApp);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fxml));
        fxmlLoader.setController(fxObj);

        Pane root;
        try {
            root = (Pane) fxmlLoader.load();
            FadeTransition ft = new FadeTransition(Duration.millis(1500));
            ft.setNode(root);
            ft.setFromValue(1);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            return root;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }
    
    private ScreenInterface getController(String fxml) {
        switch (fxml){
            case "Login.fxml":
                case "/ph/com/guanzongroup/integsys/views/Login.fxml":
                    LoginControllerHolder locontroller = new LoginControllerHolder();
                    locontroller.setMainController(this);
                    return new LoginController();
                case "/ph/com/guanzongroup/integsys/views/DefaultScreen.fxml":
                    return new DefaultScreenController();
            default:
                return null;
        }
    }
    
    private void setScene(Pane foPane) {
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
        tabpane.setVisible(false);
        tabpane.setManaged(false);
    }

    private void setScene2(TabPane foPane) {
        workingSpace.getChildren().clear();
        workingSpace.getChildren().add(foPane);
    }
    
    private void setPane() {
        pane.setOnMouseClicked(event -> {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }

        });
    }

    private void setAnchorPane() {
        pane.setOnMouseClicked(event -> {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }
        });
    }
    
    private void setAnchorPaneVisibleManage(boolean fbVisibleManage, Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
                node.setVisible(fbVisibleManage);
                node.setManaged(fbVisibleManage);
            }
        }
    }
    
    private void initMenu() {
        setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
        setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
    }
    
    private void ToggleGroupControlLowerLeftSideBar() {
        toggleGroupLowerBtn = new ToggleGroup();
        toggleBtnLeftLowerSideBar = new ToggleButton[]{
            btnHelp,
            btnLogout
        };

        String[] tooltipTexts = {
            "Help",
            "Exit"
        };

        for (int i = 0; i < toggleBtnLeftLowerSideBar.length; i++) {
            toggleBtnLeftLowerSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnLeftLowerSideBar[i].setToggleGroup(toggleGroupLowerBtn);
        }
    }
    
    private void ToggleGroupControlRightSideBar() {
        toggleGroupRightSideBar = new ToggleGroup();
        toggleBtnRightSideBar = new ToggleButton[]{
            btnSysMonitor,
            btnAddToCart
        };

        // Tooltip texts for each button
        String[] tooltipTexts = {
            "Sys Monitor",
            "Add To Cart"
        };

        // Assign tooltips and toggle group in a loop
        for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
            toggleBtnRightSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnRightSideBar[i].setToggleGroup(toggleGroupRightSideBar);
        }
    }
    
    private void ToggleGroupControlUpperLeftSideBar() {
        toggleGroup = new ToggleGroup();
        toggleBtnLeftUpperSideBar = new ToggleButton[]{
            btnInventory,
            btnPurchasing,
            btnSales,
            btnServiceRepair,
            btnAccountsReceivable,
            btnGeneralAccounting,
            btnDelivery,
            btnOthers
        };

        // Tooltip texts for each button
        String[] tooltipTexts = {
            "Inventory",
            "Purchasing",
            "Sales",
            "Service Repair",
            "AR/AP (Accounts Payable and Receivable)",
            "General Accounting",
            "Delivery",
            "Others"
        };

        // Assign tooltips to buttons
        for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
            if (toggleBtnLeftUpperSideBar[i].isVisible()) { // Skip setting tooltip for hidden buttons
                toggleBtnLeftUpperSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
                toggleBtnLeftUpperSideBar[i].setToggleGroup(toggleGroup);
            }
        }
    }
    
    private void loadUserInfo() {
        try {
            if (nav_bar.isDisabled()) {
                AppUser.setText("");
            } else {
                AppUser.setText(oApp.getLogName() + " || " + getAllIndustries(oApp.getIndustry()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getAllIndustries(String industryid) throws SQLException {
        String industryname = "";
        String lsSQL = "SELECT * FROM industry";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        ResultSet loRS = oApp.executeQuery(lsSQL);

        while (loRS.next()) {
            String id = loRS.getString("sIndstCdx");
            String description = loRS.getString("sDescript");

            if (industryid.equals(id)) {
                industryname = description;
            }
        }

        MiscUtil.close(loRS);
        return industryname;
    }
    
    private void setAppVersion(String fsValue) {
        lblVersion.setText("CAS " + fsValue);
    }
    
    private void checkDepartment() {
        if ("022".equals(oApp.getDepartment())) {
            btnSales.setVisible(false);
            btnSales.setManaged(false);
        }
    }
    
    private void getTime() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Calendar cal = Calendar.getInstance();
            int second = cal.get(Calendar.SECOND);

            Date date = new Date();
            String strTimeFormat = "hh:mm:";
            String strDateFormat = "MMMM dd, yyyy";
            String secondFormat = "ss";

            DateFormat timeFormat = new SimpleDateFormat(strTimeFormat + secondFormat);
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

            String formattedTime = timeFormat.format(date);
            String formattedDate = dateFormat.format(date);

            DateAndTime.setText(formattedDate + " || " + formattedTime);
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
    
    private void initButtonClickActions() {
        btnClose.setOnAction(this::handleButtonAction);
        btnMinimize.setOnAction(this::handleButtonAction);
    }
    
    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            switch (clickedButton.getId()) {
                case "btnClose":
                    Platform.exit();
                    break;
                case "btnMinimize":
                    Stage stage = (Stage) btnMinimize.getScene().getWindow();
                    stage.setIconified(true);
                    break;
                // Add more cases for other buttons if needed
            }
        }
    }
    
    private void notificationChecker() {
        ScheduledService<Void> service = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        checkNotifications();
                        return null;
                    }
                };
            }
        };
        service.setPeriod(Duration.minutes(1));
        service.start();
    }
    
    private void checkNotifications() {
        notificationCount += (int) (Math.random() * 5);
        cartCount += (int) (Math.random() * 5);

        Platform.runLater(() -> {
            lblNotifCount.setText(String.valueOf(notificationCount));
            lblAddToCartCount.setText(String.valueOf(cartCount));
        });
    }
    
    private void setTreeViewStyle(TreeView<String> treeView) {
        treeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #DFDFDF; -fx-border-color: #DFDFDF;");
                    if (!getStyleClass().contains("empty-tree-cell")) {
                        getStyleClass().add("empty-tree-cell");
                    }
                } else {
                    setText(item);
                    setGraphic(getTreeItem().getGraphic());
                    setStyle(null);
                    getStyleClass().remove("empty-tree-cell");

                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            TreeItem<String> treeItem = getTreeItem();
                            if (treeItem != null && !treeItem.isLeaf()) {
                                treeItem.setExpanded(!treeItem.isExpanded());
                                event.consume();
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void setDropShadowEffectsLeftSideBar(AnchorPane anchorPane) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setWidth(21.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));

        shadow.setOffsetX(2);
        shadow.setOffsetY(0);

        anchorPane.setEffect(shadow);
    }

    private void setDropShadowEffectsRightSideBar(AnchorPane anchorPane) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(20);
        shadow.setWidth(21.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));

        shadow.setOffsetX(-2);
        shadow.setOffsetY(0);

        // Apply to the AnchorPane
        anchorPane.setEffect(shadow);
    }
    
    private void setKeyEvent(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F12) {
                if (LoginControllerHolder.getLogInStatus()) {
                    //check here if the user level is supervisor
                    //check if the current tab is not entry
                    if (LoginControllerHolder.getLogInStatus()) {
                        Tab currentTab = tabpane.getSelectionModel().getSelectedItem();
                        if (currentTab != null) {
                            try {
                                if (!sformname.contains("PurchaseOrder")) {
                                    if (!sformname.contains("DeliveryAcceptance_History")) {
                                        return;
                                    }
                                }
                                if (oApp.isMainOffice()) {
                                    loadSelectIndustryAndCompany();
                                }
                            } catch (IOException e) {
                                ShowMessageFX.Warning("Unable to load selection window.", "Error", e.getMessage());
                            }
                        }
                    }
                }
            }

        }
        );
    }
    
    private void monitorMenuItems() {
        JSONArray laMaster, laDetail;
        JSONObject loMaster, loDetail;
        laMaster = new JSONArray();
        laDetail = new JSONArray();

        if (!"029".equals(oApp.getDepartment())) {
            loDetail = new JSONObject();
            loDetail.put("parent", "Sales");
            laDetail.add(loDetail);
        }

        loDetail = new JSONObject();
        loDetail.put("parent", "PO Receiving");
        laDetail.add(loDetail);

        loMaster = new JSONObject();
        loMaster.put("parent", "Monthly Payment");
        loMaster.put("child", laDetail);

        laMaster.add(loMaster);

        dissectRightSideBarJSON(laMaster.toJSONString());
    }
    
    private void loadSelectIndustryAndCompany() throws IOException {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/com/rmj/guanzongroup/sidebarmenus/views/SelectIndustryCompany.fxml"));
            SelectIndustryCompany loControl = new SelectIndustryCompany();
            loControl.setGRider(oApp);
            loControl.setOldIndsutryID(psUserIndustryId);
            loControl.setOldCompanyID(psUserCompanyId);
            loControl.setOldCategoryID(psCategoryID);
            fxmlLoader.setController(loControl);

            //get industry of current opend form
            SetTabTitle(sformname);
            String lsOldForm = getFormIndustry(psIndustryID, psCategoryID);
            String lsOldCompany = psCompanyID;
            //load the main interface
            Parent parent = fxmlLoader.load();

            parent.setOnMousePressed((MouseEvent event) -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            parent.setOnMouseDragged((MouseEvent event) -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            //set the main interface as the scene/*
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle("");
            stage.showAndWait();
            if (loControl.isFromFilter()) {
                psIndustryID = loControl.getSelectedIndustryID();
                psCompanyID = loControl.getSelectedCompanyID();
                psCategoryID = loControl.getSelectedCategoryID();
                String lsIndustry = getFormIndustry(psIndustryID, psCategoryID);
                //change form name base on selected industry
                //  /com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationCar.fxml

                System.out.println("OLD : " + sformname);
                String originalString = sformname;
                String updatedString = originalString.replace(lsOldForm + ".fxml", lsIndustry + ".fxml");

                // Print the updated string
                System.out.println(originalString);
                System.out.println(updatedString);
                sformname = updatedString;

                System.out.println("NEW : " + sformname);
                if (oApp != null) {
                    boolean isNewTab = (checktabs(SetTabTitle(sformname)) == 1);
                    if (isNewTab || !lsOldCompany.equals(psCompanyID)) {
                        if (!sformname.isEmpty() && sformname.contains(".fxml")) {
                            setScene2(loadAnimateExchange(sformname));
                        } else {
                            ShowMessageFX.Warning("This form is currently unavailable.", "Computerized Accounting System", pxeModuleName);
                        }
                    } else {
                        ShowMessageFX.Warning("This form is already active.", "Computerized Accounting System", pxeModuleName);
                    }
                    setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
                    for (ToggleButton navButton : toggleBtnLeftUpperSideBar) {
                        navButton.setSelected(false);
                    }
                    pane.requestFocus();
                }

                isFromFilter = loControl.isFromFilter();
            }
        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "Warning", null);
            System.exit(1);

        }
    }
    
    private void dissectRightSideBarJSON(String fsValue) {
        if (fsValue == null || fsValue.isEmpty()) {
            System.err.println("Invalid JSON string.");
            return;
        }

        JSONParser loParser = new JSONParser();
        try {
            JSONArray laMaster = (JSONArray) loParser.parse(fsValue);
            if (laMaster == null) {
                System.err.println("Parsed JSON is empty or invalid.");
                return;
            }

            TreeItem<String> root = new TreeItem<>("root");

            for (Object objMaster : laMaster) {
                JSONObject loParent = (JSONObject) objMaster;
                if (loParent == null || !loParent.containsKey("parent")) {
                    continue; // Skip invalid entries
                }

                TreeItem<String> parentNode = new TreeItem<>(String.valueOf(loParent.get("parent")));

                if (loParent.containsKey("child") && loParent.get("child") instanceof JSONArray) {
                    JSONArray laDetail = (JSONArray) loParent.get("child");
                    addChildren(parentNode, laDetail);
                }

                root.getChildren().add(parentNode);
            }

            if (tvRightSideBar != null) {
                tvRightSideBar.setRoot(root);
                tvRightSideBar.setShowRoot(false);
                if (!isListenerRightAdded) {
                    isListenerRightAdded = true;
                    tvRightSideBar.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null && newValue.isLeaf() && newValue.getValue() != null && !newValue.getValue().isEmpty()) {
                            switch (newValue.getValue()) {
                                case "Sales Replacement":
                                    sformname = "/com/rmj/guanzongroup/sidebarmenus/views/SampleForm2.fxml";
                                    break;
                                case "Additional Give":
                                    sformname = "/com/rmj/guanzongroup/sidebarmenus/views/SampleForm1.fxml";
                                    break;
                                default:
                                    sformname = "";
                                    break;
                            }

                            // Load the corresponding form
                            if (oApp != null) {
                                boolean isNewTab = (checktabs(SetTabTitle(sformname)) == 1);
                                if (isNewTab) {
                                    if (!sformname.isEmpty() && sformname.contains(".fxml")) {
                                        setScene2(loadAnimate(sformname));
                                    } else {
                                        ShowMessageFX.Warning("This form is currently unavailable.", "Computerized Accounting System", pxeModuleName);
                                    }
                                } else {
                                    ShowMessageFX.Warning("This form is already active.", "Computerized Accounting System", pxeModuleName);
                                }
                                setAnchorPaneVisibleManage(false, anchorRightSideBarMenu);
                                for (ToggleButton navButton : toggleBtnRightSideBar) {
                                    navButton.setSelected(false);
                                }
                                pane.requestFocus();
                            }
                        } else {
                            System.out.println("Invalid selection or empty value.");
                        }

                    });
                } else {
                    System.err.println("tvChild1 is not initialized.");
                }
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
    public String SetTabTitle(String menuaction) {
        if (menuaction.contains(".fxml")) {
        }
        
        return null;
    }
    
    private String getFormIndustry(String industryId, String categoryId) {
        String concatName = "";
        switch (industryId) {
            case "01":
                concatName = "MP";
                break;
            case "02":
                if ("0003".equals(categoryId)) {
                    concatName = "MC";   // Motorcycle
                }
                if ("0004".equals(categoryId)) {
                    concatName = "SPMC"; // Spare Parts
                }
            case "03":
                if ("0005".equals(categoryId)) {
                    concatName = "Car";   // Vehicle
                }
                if ("0006".equals(categoryId)) {
                    concatName = "SPCar"; // Spare Parts
                }
                break;
            case "04":
                if ("0021".equals(categoryId)) {
                    concatName = "MonarchFood";  // Food Service
                }
                if ("0009".equals(categoryId)) {
                    concatName = "MonarchHospitality";  // Hospitality
                }
                break;
            case "05":
                concatName = "LP";
                break;
            case "07":
                concatName = "Appliances";
                break;
            case "00":
                concatName = "";
                break;
            default:
                concatName = "";
                break;
        }
        return concatName;
    }
    
    public int checktabs(String tabtitle) {
        for (Tab tab : tabpane.getTabs()) {
            if (tab.getText().equals(tabtitle)) {
                tabpane.getSelectionModel().select(tab);
                return 0;
            }
        }
        return 1;
    }
    
    public void setTabPane() {
        tabpane.setOnMouseClicked(event -> {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            for (int i = 0; i < toggleBtnLeftUpperSideBar.length; i++) {
                toggleBtnLeftUpperSideBar[i].setSelected(false);
            }

            for (int i = 0; i < toggleBtnRightSideBar.length; i++) {
                toggleBtnRightSideBar[i].setSelected(false);
            }
            if (tabpane.getSelectionModel().getSelectedItem() != null) {
                sformname = getFormName(tabpane.getSelectionModel().getSelectedItem().getText());
            }
        });

        tabpane.setOnDragDetected(event -> {
            Dragboard db = tabpane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tabpane.getSelectionModel().getSelectedItem().getText());
            db.setContent(content);
            event.consume();
        });

        tabpane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });

        tabpane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String tabText = db.getString();
                int draggedTabIndex = findTabIndex(tabText);
                double mouseX = event.getX();
                double mouseY = event.getY();
                double tabHeaderHeight = tabpane.lookup(".tab-header-area").getBoundsInParent().getHeight();

                targetTabIndex = (int) (mouseX / 180);
                if (mouseY < tabHeaderHeight) {

                    if (draggedTabIndex != targetTabIndex) {
                        Tab draggedTab = tabpane.getTabs().remove(draggedTabIndex);
                        if (targetTabIndex > tabpane.getTabs().size()) {
                            targetTabIndex = tabpane.getTabs().size();
                        }
                        tabpane.getTabs().add(targetTabIndex, draggedTab);
                        tabpane.getSelectionModel().select(draggedTab);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        tabpane.setOnDragDone(event -> {
            event.consume();
        });

    }
    
    private int findTabIndex(String tabText) {
        ObservableList<Tab> tabs = tabpane.getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getText().equals(tabText)) {
                return i;
            }
        }
        return -1;
    }
    
    public TabPane loadAnimateExchange(String fsFormName) {
        setTabPane();
        setPane();

        ScreenInterface fxObj = getController(fsFormName);
        fxObj.setGRider(oApp);
        fxObj.setCompanyID(psCompanyID);
        fxObj.setIndustryID(psIndustryID);
        fxObj.setCategoryID(psCategoryID);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fsFormName));
        fxmlLoader.setController(fxObj);

        try {
            Node content = fxmlLoader.load();
            Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();

            if (selectedTab != null) {
                // Update title and content of the selected tab
                String newTitle = SetTabTitle(fsFormName);

                // Update tab name in the tracking list
                int index = tabName.indexOf(selectedTab.getText());
                if (index != -1) {
                    tabName.set(index, newTitle);
                }

                selectedTab.setText(newTitle);
                selectedTab.setContent(content);
                selectedTab.setContextMenu(createContextMenu(tabpane, selectedTab, oApp));

                selectedTab.setOnCloseRequest(event -> {
                    if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure, do you want to close tab?")) {
                        tabName.remove(selectedTab.getText());
                        Tabclose();
                    } else {
                        event.consume();
                    }
                });

                selectedTab.setOnSelectionChanged(event -> {
                    ObservableList<Tab> tabs = tabpane.getTabs();
                    for (Tab tab : tabs) {
                        if (tab.getText().equals(selectedTab.getText())) {
                            tabName.remove(selectedTab.getText());
                            tabName.add(selectedTab.getText());
                            break;
                        }
                    }
                });
            }

            return tabpane;

        } catch (IOException e) {
            ShowMessageFX.Warning(e.getMessage(), "FXML Load Error", null);
            return null;
        }
    }
    
    public String getFormName(String fsTabTitle) {
        return null;
    }
    
    public ContextMenu createContextMenu(TabPane tabPane, Tab tab, GRiderCAS oApp) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem closeTabItem = new MenuItem("Close Tab");
        MenuItem closeOtherTabsItem = new MenuItem("Close Other Tabs");
        MenuItem closeAllTabsItem = new MenuItem("Close All Tabs");

        closeTabItem.setOnAction(event -> closeSelectTabs(tabPane, tab));
        closeOtherTabsItem.setOnAction(event -> closeOtherTabs(tabPane, tab));
        closeAllTabsItem.setOnAction(event -> closeAllTabs(tabPane, oApp));

        contextMenu.getItems().add(closeTabItem);
        contextMenu.getItems().add(closeOtherTabsItem);
        contextMenu.getItems().add(closeAllTabsItem);

        tab.setContextMenu(contextMenu);

        closeOtherTabsItem.visibleProperty().bind(Bindings.size(tabPane.getTabs()).greaterThan(1));

        return contextMenu;
    }
    
    private void closeSelectTabs(TabPane tabPane, Tab currentTab) {
        if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure, do you want to close tab?")) {
            // Remove the tab
            if (tabPane.getTabs().removeIf(tab -> tab == currentTab)) {
                if (tabPane.getTabs().isEmpty()) {
                    unloadForm unload = new unloadForm();
                    StackPane myBox = (StackPane) tabPane.getParent();
                    myBox.getChildren().clear();
                    myBox.getChildren().add(unload.getScene(psDefaultScreenFXML2, oApp));
                }
            }
            tabName.remove(currentTab.getText());
        }
    }

    private void closeOtherTabs(TabPane tabPane, Tab currentTab) {
        if (ShowMessageFX.YesNo(null, "Close Other Tab", "Are you sure, do you want to close other tab?")) {
            tabPane.getTabs().removeIf(tab -> tab != currentTab);
            List<String> currentTabNameList = Collections.singletonList(currentTab.getText());
            tabName.retainAll(currentTabNameList);
            for (Tab tab : tabPane.getTabs()) {
                String formName = tab.getText();
            }
        }
    }

    private void closeAllTabs(TabPane tabPane, GRiderCAS oApp) {
        if (tabPane == null) {
            System.out.println("tabPane is null");
            return;
        }

        if (ShowMessageFX.YesNo(null, "Close All Tabs", "Are you sure, do you want to close all tabs?")) {
            if (tabName != null) {
                tabName.clear();
            } else {
                System.out.println("tabName is null");
            }

            while (!tabPane.getTabs().isEmpty()) {
                int tabCount = tabPane.getTabs().size(); // count tabs before removing

                for (int i = 0; i < tabCount; i++) {
                    Tab tab = tabPane.getTabs().remove(0);

                    EventHandler<Event> onClosed = tab.getOnClosed();
                    if (onClosed != null) {
                        lbproceed = true;
                        onClosed.handle(new Event(Event.ANY));
                    }
                }
            }

            unloadForm unload = new unloadForm();

            if (tabPane.getParent() == null) {
                System.out.println("Parent of tabPane is null");
                return;
            }

            StackPane myBox = (StackPane) tabPane.getParent();
            myBox.getChildren().clear();
            myBox.getChildren().add(unload.getScene(psDefaultScreenFXML2, oApp));
        }
    }

    public void Tabclose() {
        int tabsize = tabpane.getTabs().size();
        if (tabsize == 1) {
            setScene(loadAnimateAnchor(psDefaultScreenFXML2));
        }
    }

    public void Tabclose(TabPane tabpane) {
        int tabsize = tabpane.getTabs().size();
        if (tabsize == 1) {
            setScene(loadAnimateAnchor(psDefaultScreenFXML2));
        }
    }
    
    private void addChildren(TreeItem<String> parentNode, JSONArray childrenArray) {
        for (Object obj : childrenArray) {
            JSONObject loDetail = (JSONObject) obj;
            if (loDetail == null || !loDetail.containsKey("menu_name")) {
                continue;
            }

            String parentName = String.valueOf(loDetail.get("menu_name"));
            String location = loDetail.containsKey("fxml_path") ? String.valueOf(loDetail.get("fxml_path")) : "";
//            String lsIndustryCode = String.valueOf(loDetail.get("industry_code"));
//            String lsCategoryCode = String.valueOf(loDetail.get("category_code"));

            TreeItem<String> childNode = new TreeItem<>(parentName);
            menuLocationMap.put(childNode, location);
//            menuIndustryMap.put(childNode, lsIndustryCode); // Store industry code
//            menuCategoryMap.put(childNode, lsCategoryCode); // Store category code

            if (loDetail.containsKey("child") && loDetail.get("child") instanceof JSONArray) {
                JSONArray subChildren = (JSONArray) loDetail.get("child");
                addChildren(childNode, subChildren);
            }

            parentNode.getChildren().add(childNode);
        }
    }
    
    public TabPane loadAnimate(String fsFormName) {
        //set fxml controller class
        if (tabpane.getTabs().isEmpty()) {
            tabpane = new TabPane();
        }
        psIndustryID = psUserIndustryId;
        psCompanyID = psUserCompanyId;

        setTabPane();
        setPane();

        ScreenInterface fxObj = getController(fsFormName);
        fxObj.setGRider(oApp);
        fxObj.setIndustryID(psIndustryID);
        fxObj.setCompanyID(psCompanyID);
        fxObj.setCategoryID(psCategoryID);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(updateFxmlName(fsFormName)));
        fxmlLoader.setController(fxObj);

        Tab newTab = new Tab(SetTabTitle(fsFormName));
        newTab.setContent(new javafx.scene.control.Label("Content of Tab " + fsFormName));
        newTab.setContextMenu(createContextMenu(tabpane, newTab, oApp));
        tabName.add(SetTabTitle(fsFormName));
        try {
            Node content = fxmlLoader.load();
            newTab.setContent(content);
            tabpane.getTabs().add(newTab);
            tabpane.getSelectionModel().select(newTab);

            newTab.setOnCloseRequest(event -> {
                if (ShowMessageFX.YesNo(null, "Close Tab", "Are you sure, do you want to close tab?")) {
                    tabName.remove(newTab.getText());
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    Tabclose();
                } else {
                    event.consume();
                }

            });

            newTab.setOnClosed(event -> {
                if (lbproceed) {
                    SIPostingWindowKeyEvent(newTab, fxObj, true);
                    lbproceed = false;
                }
            });

            newTab.setOnSelectionChanged(event -> {
                ObservableList<Tab> tabs = tabpane.getTabs();
                for (Tab tab : tabs) {
                    if (tab.getText().equals(newTab.getText())) {
                        tabName.remove(newTab.getText());
                        tabName.add(newTab.getText());

                        //applied for specific use//
                        if (newTab.isSelected()) {
                            SIPostingWindowKeyEvent(newTab, fxObj, false);
                        } else {
                            SIPostingWindowKeyEvent(newTab, fxObj, true);
                        }
                        break;
                    }
                }
            });
            return (TabPane) tabpane;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public class ControllerBinding {
        public final String tabName;
        public final Class<? extends ScreenInterface> controllerClass;

        public ControllerBinding(String tabName, Class<? extends ScreenInterface> controllerClass) {
            this.tabName = tabName;
            this.controllerClass = controllerClass;
        }
    }

    ControllerBinding[] controllerArray;

    private void SIPostingWindowKeyEvent(Tab newTab, ScreenInterface fxObj, boolean isRemove) {
        for (ControllerBinding cb : controllerArray) {
            if (cb.tabName.equals(newTab.getText())) {
                try {
                    Object casted = cb.controllerClass.cast(fxObj);
                    Method method = isRemove ? cb.controllerClass.getMethod("RemoveWindowEvent") : cb.controllerClass.getMethod("TriggerWindowEvent");
                    method.invoke(casted);
                } catch (Exception e) {
                    e.printStackTrace(); // Or log nicely
                }
                break;
            }
        }
    }
    
    private String updateFxmlName(String fsFormName) {
        return "";
    }
    
    public void triggervbox() {
        nav_bar.setDisable(true);
        nav_bar11.setDisable(true);

    }

    public void triggervbox2() {
        setAnchorPaneVisibleManage(true, anchorRightSideBarMenu);
        nav_bar.setDisable(false);
        nav_bar11.setDisable(false);
        setScene(loadAnimateAnchor(psDefaultScreenFXML2));

        toggleGroupLowerBtn = new ToggleGroup();
        toggleBtnLeftLowerSideBar = new ToggleButton[]{
            btnHelp,
            btnLogout
        };

        String[] tooltipTexts = {
            "Help",
            "Logout"
        };

        for (int i = 0; i < toggleBtnLeftLowerSideBar.length; i++) {
            toggleBtnLeftLowerSideBar[i].setTooltip(new Tooltip(tooltipTexts[i]));
            toggleBtnLeftLowerSideBar[i].setToggleGroup(toggleGroupLowerBtn);
        }
    }
    
    public void changeUserInfo(String industryid) {
        try {
            AppUser.setText(oApp.getLogName() + " || " + getAllIndustries(industryid));
        } catch (SQLException ex) {
            Logger.getLogger(DashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
