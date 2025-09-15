package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private TreeView<String> tvLeftSideBar;
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
        inventoryMenuItems();
        toggleLeftSideBarMenuButton("switchInventory", 0);
        toggleSidebarWidth();
    }

    @FXML
    private void switchPurchasing(ActionEvent event) {
        purchasingMenuItems();
        toggleLeftSideBarMenuButton("switchPurchasing", 1);
        toggleSidebarWidth();
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
    
    private void toggleLeftSideBarMenuButton(String buttonId, Integer btnIndex) {
        boolean isNoMenu = false;
        boolean isSameButton = anchorLeftSideBarMenu.isVisible() && lastClickedBtnLeftSideBar.equals(buttonId);

        if (tvLeftSideBar.getRoot() != null) {
            if (!tvLeftSideBar.getRoot().getChildren().isEmpty()) {
                setAnchorPaneVisibleManage(!isSameButton, anchorLeftSideBarMenu);
            } else {
                setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
                ShowMessageFX.Warning(null, "Computerized Accounting System", "No Menu's Available");
                isNoMenu = true;
            }
        } else {
            setAnchorPaneVisibleManage(false, anchorLeftSideBarMenu);
            ShowMessageFX.Warning(null, "Computerized Accounting System", "No Menu's Available");
            isNoMenu = true;
        }

        for (ToggleButton button : toggleBtnLeftUpperSideBar) {
            button.setSelected(false);
        }
        if (!isNoMenu) {
            toggleBtnLeftUpperSideBar[btnIndex].setSelected(!isSameButton);
            lastClickedBtnLeftSideBar = isSameButton ? "" : buttonId;
        }
    }
    
    private void toggleSidebarWidth() {
        if (tvLeftSideBar != null && tvLeftSideBar.getRoot() != null) {
            int calculatedWidth = calculateTreeViewWidth(tvLeftSideBar.getRoot());

            Platform.runLater(() -> {
                anchorLeftSideBarMenu.setPrefWidth(calculatedWidth);
            });
        }
    }
    
    private int calculateTreeViewWidth(TreeItem<String> root) {
        if (root == null) {
            return 200;
        }
        int baseWidth = 200;
        int textPadding = 20;

        int longestTextWidth = getMaxTextWidth(root);

        double parentWidth = anchorLeftSideBarMenu.getParent().getLayoutBounds().getWidth();

        int calculatedWidth = baseWidth + longestTextWidth + textPadding;
        return (int) Math.min(calculatedWidth, parentWidth * 0.9);
    }

    private int getMaxTextWidth(TreeItem<String> item) {
        if (item == null) {
            return 0;
        }

        int maxWidth = getTextWidth(item.getValue());

        for (TreeItem<String> child : item.getChildren()) {
            maxWidth = Math.max(maxWidth, getMaxTextWidth(child));
        }

        return maxWidth;
    }
    
    private int getTextWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int charWidth = 7;
        return text.length() * charWidth;
    }
    
    private void inventoryMenuItems() {
        String jsonString = "["
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Inventory\", \"fxml_path\": \"Inventory\", \"controller_path\": \"sample.controller\", \"menu_id\": \"028\", \"menu_parent\": \"\"},"
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Inventory Maintenance\", \"fxml_path\": \"Inventory/Inventory Maintenance\", \"controller_path\": \"sample.controller\", \"menu_id\": \"45\", \"menu_parent\": \"028\"},"
                //Entry
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Request\", \"fxml_path\": \"Inventory/Request\", \"controller_path\": \"sample.controller\", \"menu_id\": \"029\", \"menu_parent\": \"028\"},"
                //Without ROQ 
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Regular Stocks\", \"fxml_path\": \"Inventory/Request/Regular Stocks\", \"controller_path\": \"sample.controller\", \"menu_id\": \"030\", \"menu_parent\": \"029\"},"
                //Without ROQ Mobile Phones
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMp.fxml\", \"controller_path\": \"InvRequest_EntryMP.controller\", \"menu_id\": \"031\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMp.fxml\", \"controller_path\": \"InvRequest_ConfrimationMP.controller\", \"menu_id\": \"060\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMp.fxml\", \"controller_path\": \"InvRequest_HistoryMP.controller\", \"menu_id\": \"0118\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMPGeneral.fxml\", \"controller_path\": \"InvRequest_EntryMPGeneral.controller\", \"menu_id\": \"0144\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMpGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationMPGeneral.controller\", \"menu_id\": \"0145\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMpGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryMPGeneral.controller\", \"menu_id\": \"0147\", \"menu_parent\": \"0117\"},"
                //Without ROQ
                //Without ROQ Appliances
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryAppliances.fxml\", \"controller_path\": \"InvRequest_EntryAppliancesController.controller\", \"menu_id\": \"032\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationAppliances.fxml\", \"controller_path\": \"InvRequest_ConfirmationAppliances.controller\", \"menu_id\": \"061\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryAppliances.fxml\", \"controller_path\": \"InvRequest_HistoryAppliances.controller\", \"menu_id\": \"0118\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_EntryAppliancesGeneral.controller\", \"menu_id\": \"032\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationAppliancesGeneral.controller\", \"menu_id\": \"062\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryAppliancesGeneral.controller\", \"menu_id\": \"0119\", \"menu_parent\": \"0117\"},"
                //Without ROQ Motorcycle

                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMc.fxml\", \"controller_path\": \"InvRequest_EntryMc.controller\", \"menu_id\": \"034\", \"menu_parent\": \"030\"},"


                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMc.fxml\", \"controller_path\": \"InvRequest_ConfirmationMc.controller\", \"menu_id\": \"063\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMc.fxml\", \"controller_path\": \"InvRequest_HistoryMc.controller\", \"menu_id\": \"0120\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMcSp.fxml\", \"controller_path\": \"InvRequest_EntryMcSp.controller\", \"menu_id\": \"035\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMcSp.fxml\", \"controller_path\": \"InvRequest_ConfirmationMcSp.controller\", \"menu_id\": \"064\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMcSp.fxml\", \"controller_path\": \"InvRequest_HistoryMcSp.controller\", \"menu_id\": \"0121\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMcGeneral.fxml\", \"controller_path\": \"InvRequest_EntryMcGeneral.controller\", \"menu_id\": \"036\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMcGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationMcGeneral.controller\", \"menu_id\": \"065\", \"menu_parent\": \"059\"},"

                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMcGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryMcGeneral.controller\", \"menu_id\": \"0122\", \"menu_parent\": \"0117\"}," 

                //Without ROQ Car
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationCar.fxml\", \"controller_path\": \"InvRequest_ConfirmationCar.controller\", \"menu_id\": \"067\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryCar.fxml\", \"controller_path\": \"InvRequest_EntryCar.controller\", \"menu_id\": \"067\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryCar.fxml\", \"controller_path\": \"InvRequest_HistoryCar.controller\", \"menu_id\": \"0123\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryCarSp.fxml\", \"controller_path\": \"InvRequest_EntryCarSp.controller\", \"menu_id\": \"038\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationCarSp.fxml\", \"controller_path\": \"InvRequest_ConfirmationCarSp.controller\", \"menu_id\": \"068\", \"menu_parent\": \"059\"},"

                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryCarSp.fxml\", \"controller_path\": \"InvRequest_HistoryCarSp.controller\", \"menu_id\": \"0124\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryCarGeneral.fxml\", \"controller_path\": \"InvRequest_EntryCarGeneral.controller\", \"menu_id\": \"039\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationCarGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationCarGeneral.controller\", \"menu_id\": \"069\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryCarGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryCarGeneral.controller\", \"menu_id\": \"0125\", \"menu_parent\": \"0117\"},"
                //Without ROQ Los Pedritos
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryLpFood.fxml\", \"controller_path\": \"InvRequest_EntryLPFood.controller\", \"menu_id\": \"042\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationLpFood.fxml\", \"controller_path\": \"InvRequest_ConfirmationLPFood.controller\", \"menu_id\": \"072\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryLpFood.fxml\", \"controller_path\": \"InvRequest_HistoryLPFood.controller\", \"menu_id\": \"0128\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryLpGeneral.fxml\", \"controller_path\": \"InvRequest_EntryLPGeneral.controller\", \"menu_id\": \"0152\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationLpGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationLPGeneral.controller\", \"menu_id\": \"073\", \"menu_parent\": \"059\"}," //check
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryLpGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryLPGeneral.controller\", \"menu_id\": \"0129\", \"menu_parent\": \"0117\"},"
                //Without ROQ Monarch
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMonarchFood.fxml\", \"controller_path\": \"InvRequest_EntryMonarchFood.controller\", \"menu_id\": \"040\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMonarchFood.fxml\", \"controller_path\": \"InvRequest_ConfirmationMonarchFood.controller\", \"menu_id\": \"070\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMonarchFood.fxml\", \"controller_path\": \"InvRequest_HistoryMonarchFood.controller\", \"menu_id\": \"0126\", \"menu_parent\": \"0117\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_EntryMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_EntryMonarchGeneral.controller\", \"menu_id\": \"041\", \"menu_parent\": \"030\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ConfirmationMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_ConfirmationMonarchGeneral.controller\", \"menu_id\": \"071\", \"menu_parent\": \"059\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_HistoryMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_HistoryMonarchGeneral.controller\", \"menu_id\": \"0127\", \"menu_parent\": \"0117\"},"
                //With ROQ
                //With ROQ mobile phones
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMp.fxml\", \"controller_path\": \"InvRequest_ROQ_EntryMP.controller\", \"menu_id\": \"045\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMp.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMP.controller\", \"menu_id\": \"075\", \"menu_parent\": \"074\"},"
               
                + "  {\"access_level\": \"01\", \"menu_name\": \"Mobile Phones\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMp.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMP.controller\", \"menu_id\": \"0131\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMpGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_EntryMPGeneral.controller\", \"menu_id\": \"0148\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMpGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMPGeneral.controller\", \"menu_id\": \"0149\", \"menu_parent\": \"074\"},"
                
                + "  {\"access_level\": \"01\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMpGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMPGeneral.controller\", \"menu_id\": \"0151\", \"menu_parent\": \"0130\"},"
                //With ROQ Appliances 
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryAppliances.fxml\", \"controller_path\": \"InvRequest_ROQ_EntryAppliances.controller\", \"menu_id\": \"046\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationAppliances.fxml\", \"controller_path\": \"InvRequest_ROQ_ConfirmationAppliances.controller\", \"menu_id\": \"076\", \"menu_parent\": \"074\"},"
       
                + "  {\"access_level\": \"07\", \"menu_name\": \"Appliances\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryAppliances.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryAppliances.controller\", \"menu_id\": \"0132\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_ROQ_EntryAppliancesGeneral.controller\", \"menu_id\": \"047\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_ROQ_ConfirmationAppliancesGeneral.controller\", \"menu_id\": \"077\", \"menu_parent\": \"074\"},"
              
                + "  {\"access_level\": \"07\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryAppliancesGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryAppliancesGeneral.controller\", \"menu_id\": \"0133\", \"menu_parent\": \"0130\"},"
                //With ROQ Motorcycles 
                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMc.fxml\", \"controller_path\": \"InvRequest_Roq_EntryMc.controller\", \"menu_id\": \"048\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMc.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMc.controller\", \"menu_id\": \"078\", \"menu_parent\": \"074\"},"
                
                + "  {\"access_level\": \"02\", \"menu_name\": \"Motorcycle\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMc.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMc.controller\", \"menu_id\": \"0134\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMcSp.fxml\", \"controller_path\": \"InvRequest_ROQ_EntryMcSp.controller\", \"menu_id\": \"49\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMcSp.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMcSp.controller\", \"menu_id\": \"79\", \"menu_parent\": \"074\"},"

                + "  {\"access_level\": \"02\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMcSp.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMcSp.controller\", \"menu_id\": \"135\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMcGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_EntryMcGeneral.controller\", \"menu_id\": \"050\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMcGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMcGeneral.controller\", \"menu_id\": \"080\", \"menu_parent\": \"074\"},"
           
                + "  {\"access_level\": \"02\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMcGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMcGeneral.controller\", \"menu_id\": \"0136\", \"menu_parent\": \"0130\"},"
                //With ROQ Car
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryCar.fxml\", \"controller_path\": \"InvRequest_Roq_EntryCar.controller\", \"menu_id\": \"051\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationCar.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationCar.controller\", \"menu_id\": \"081\", \"menu_parent\": \"074\"},"
        
                + "  {\"access_level\": \"03\", \"menu_name\": \"Car\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryCar.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryCar.controller\", \"menu_id\": \"0137\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryCarSp.fxml\", \"controller_path\": \"InvRequest_ROQ_EntryCarSP.controller\", \"menu_id\": \"052\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationCarSp.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationCarSp.controller\", \"menu_id\": \"082\", \"menu_parent\": \"074\"},"

                + "  {\"access_level\": \"03\", \"menu_name\": \"Spareparts\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryCarSp.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryCarSp.controller\", \"menu_id\": \"0138\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryCarGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_EntryCarGeneral.controller\", \"menu_id\": \"053\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationCarGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationCarGeneral.controller\", \"menu_id\": \"083\", \"menu_parent\": \"074\"},"

                + "  {\"access_level\": \"03\", \"menu_name\": \"General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryCarGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryCarGeneral.controller\", \"menu_id\": \"0139\", \"menu_parent\": \"0130\"},"
                //With ROQ Los Pedritos
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryLpFood.fxml\", \"controller_path\": \"InvRequest_Roq_EntryLPFood.controller\", \"menu_id\": \"056\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationLpFood.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationLpFood.controller\", \"menu_id\": \"086\", \"menu_parent\": \"074\"},"

                + "  {\"access_level\": \"05\", \"menu_name\": \"LP Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryLpFood.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryLPFood.controller\", \"menu_id\": \"0142\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryLpGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_EntryLPGeneral.controller\", \"menu_id\": \"057\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationLpGeneral.fxml\", \"controller_path\": \"InvRequest_ROQ_ConfirmationLpGeneral.controller\", \"menu_id\": \"087\", \"menu_parent\": \"074\"},"

                + "  {\"access_level\": \"05\", \"menu_name\": \"LP General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryLpGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryLPGeneral.controller\", \"menu_id\": \"0143\", \"menu_parent\": \"0130\"},"
                //With ROQ Monarch
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMonarchFood.fxml\", \"controller_path\": \"InvRequest_Roq_EntryMonarchFood.controller\", \"menu_id\": \"054\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMonarchFood.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMonarchFood.controller\", \"menu_id\": \"084\", \"menu_parent\": \"074\"},"
      
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch Food\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMonarchFood.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMonarchFood.controller\", \"menu_id\": \"0140\", \"menu_parent\": \"0130\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_EntryMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_EntryMonarchGeneral.controller\", \"menu_id\": \"055\", \"menu_parent\": \"044\"},"
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_ConfirmationMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_ConfirmationMonarchGeneral.controller\", \"menu_id\": \"085\", \"menu_parent\": \"074\"},"

              
                + "  {\"access_level\": \"04\", \"menu_name\": \"Monarch General\", \"fxml_path\": \"/com/rmj/guanzongroup/sidebarmenus/views/InvRequest_ROQ_HistoryMonarchGeneral.fxml\", \"controller_path\": \"InvRequest_Roq_HistoryMonarchGeneral.controller\", \"menu_id\": \"0141\", \"menu_parent\": \"0130\"},"      
               //System recommended
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"System Recommend\", \"fxml_path\": \"Inventory/Request/System Recommend\", \"controller_path\": \"sample.controller\", \"menu_id\": \"044\", \"menu_parent\": \"029\"},"   
               //new tab: Confirmation
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Confirmation\", \"fxml_path\": \"Inventory/Confirmation\", \"controller_path\": \"sample.controller\", \"menu_id\": \"058\", \"menu_parent\": \"028\"},"
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Regular Stocks\", \"fxml_path\": \"Inventory/Confirmation/Regular Stocks\", \"controller_path\": \"sample.controller\", \"menu_id\": \"059\", \"menu_parent\": \"058\"},"
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"System Recommend\", \"fxml_path\": \"Inventory/Request/System Recommend\", \"controller_path\": \"sample.controller\", \"menu_id\": \"074\", \"menu_parent\": \"058\"},"              
                
               

                //new tab: History
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"History\", \"fxml_path\": \"Inventory/History\", \"controller_path\": \"sample.controller\", \"menu_id\": \"0116\", \"menu_parent\": \"028\"},"
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"Regular Stocks\", \"fxml_path\": \"Inventory/History/Regular Stocks\", \"controller_path\": \"sample.controller\", \"menu_id\": \"0117\", \"menu_parent\": \"0116\"},"
                + "  {\"access_level\": \"01 02 03 04 05 06 07\", \"menu_name\": \"System Recommend\", \"fxml_path\": \"Inventory/History/Request/System Recommend\", \"controller_path\": \"sample.controller\", \"menu_id\": \"0130\", \"menu_parent\": \"0116\"},"
                + "]";

        JSONParser parser = new JSONParser();
        try {
            try {
                flatMenuItems = (JSONArray) parser.parse(new StringReader(jsonString));
                JSONObject purchasingMainMenu = buildHierarchy("028");
                dissectLeftSideBarJSON(purchasingMainMenu.toJSONString());

            } catch (IOException ex) {
                Logger.getLogger(DashboardController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    
    private void dissectLeftSideBarJSON(String fsValue) {
        if (fsValue == null || fsValue.isEmpty()) {
            System.err.println("Invalid JSON string.");
            return;
        }

        JSONParser loParser = new JSONParser();
        try {
            Object parsedJson = loParser.parse(fsValue);
            JSONArray laMaster;

            if (parsedJson instanceof JSONArray) {
                laMaster = (JSONArray) parsedJson;
            } else if (parsedJson instanceof JSONObject) {
                laMaster = new JSONArray();
                laMaster.add(parsedJson);
            } else {
                System.err.println("Invalid JSON format.");
                return;
            }

            TreeItem<String> root = new TreeItem<>("root");
            menuLocationMap.clear();
//            menuIndustryMap.clear();
//            menuCategoryMap.clear();

            for (Object objMaster : laMaster) {
                if (!(objMaster instanceof JSONObject)) {
                    System.err.println("Skipping invalid entry: " + objMaster);
                    continue;
                }

                JSONObject loParent = (JSONObject) objMaster;
                if (!loParent.containsKey("menu_name")) {
                    continue;
                }

                String parentName = String.valueOf(loParent.get("menu_name"));
                String location = loParent.containsKey("fxml_path") ? String.valueOf(loParent.get("fxml_path")) : "";
//                String lsIndustryCode = String.valueOf(loParent.get("industry_code"));
//                String lsCategoryCode = String.valueOf(loParent.get("category_code"));

                TreeItem<String> parentNode = new TreeItem<>(parentName);
                menuLocationMap.put(parentNode, location); // Store location
//                menuIndustryMap.put(parentNode, lsIndustryCode); // Store industry code
//                menuCategoryMap.put(parentNode, lsCategoryCode); // Store category code

                if (loParent.containsKey("child") && loParent.get("child") instanceof JSONArray) {
                    JSONArray laDetail = (JSONArray) loParent.get("child");
                    addChildren(parentNode, laDetail);
                }

                root.getChildren().add(parentNode);
            }

            if (tvLeftSideBar != null) {
                tvLeftSideBar.setRoot(root);
                tvLeftSideBar.setShowRoot(false);

                if (!isListenerLeftAdded) {
                    isListenerLeftAdded = true;
                    tvLeftSideBar.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            handleSelection(newValue);
                        }
                    });
                }
            } else {
                System.err.println("tvLeftSideBar is not initialized.");
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
    private void handleSelection(TreeItem<String> newValue) {
        if (newValue == null || !newValue.isLeaf() || newValue.getValue() == null || newValue.getValue().isEmpty()) {
            System.out.println("Invalid selection or empty value.");
            return;
        }

        // Get the location directly from menuLocationMap
        sformname = menuLocationMap.getOrDefault(newValue, "");
//        psIndustryID = menuIndustryMap.getOrDefault(newValue, "");
//        psCategoryID = menuCategoryMap.getOrDefault(newValue, "");
        if (oApp != null) {
            boolean isNewTab = (checktabs(SetTabTitle(sformname)) == 1);
            if (isNewTab) {
                if (!sformname.isEmpty() && sformname.contains(".fxml")) {

                    System.out.println("industry: " + psIndustryID);
                    System.out.println("category: " + psCategoryID);
                    setScene2(loadAnimate(sformname));
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
    }
    
    public JSONObject buildHierarchy(String menuCode) {
        String userDepartment = psUserIndustryId;
        Map<String, List<JSONObject>> childMap = new HashMap<>();
        JSONObject rootMenuItem = null;

        for (JSONObject item : flatMenuItems) {
            List<String> accessDepartments = getAccessDepartments(item);

            if (!accessDepartments.contains(userDepartment)) {
                continue;
            }

            String menuId = (String) item.get("menu_id");
            String parentId = (String) item.get("menu_parent");

            if (menuCode.equals(menuId) && (parentId == null || parentId.isEmpty())) {
                rootMenuItem = item;
            }

            childMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(item);
        }

        if (rootMenuItem == null) {
            return new JSONObject();
        }

        return buildSubHierarchy(rootMenuItem, childMap, userDepartment);
    }
    
    private JSONObject buildSubHierarchy(JSONObject item, Map<String, List<JSONObject>> childMap, String userDepartment) {
        JSONObject node = new JSONObject();
        node.put("menu_id", item.get("menu_id"));
        node.put("menu_name", item.get("menu_name"));
        node.put("menu_parent", item.get("menu_parent"));
        node.put("fxml_path", item.get("fxml_path"));
        node.put("controller_path", item.get("controller_path"));
        node.put("access_level", item.get("access_level"));

        List<JSONObject> children = childMap.getOrDefault(item.get("menu_id"), Collections.emptyList());

        JSONArray childrenArray = new JSONArray();
        for (JSONObject child : children) {
            List<String> accessDepartments = getAccessDepartments(child);
            if (accessDepartments.contains(userDepartment)) {
                childrenArray.add(buildSubHierarchy(child, childMap, userDepartment));
            }
        }

        if (!childrenArray.isEmpty()) {
            node.put("child", childrenArray);
        } else {
            node.put("child", new JSONArray());
        }

        return node;
    }
    
    private List<String> getAccessDepartments(JSONObject item) {
        Object accessLevelObj = item.get("access_level");

        if (accessLevelObj == null) {
            return Collections.emptyList();
        }

        String accessLevelStr = accessLevelObj.toString().trim();
        if (accessLevelStr.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(accessLevelStr.split("\\s+"));
    }
    
    private void purchasingMenuItems() {
        String jsonString = "[{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchasing\",\"fxml_path\":\"Purchasing\",\"controller_path\":\"purchasing.controller\",\"menu_id\":\"001\",\"menu_parent\":\"\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Entry\",\"fxml_path\":\"Entry\",\"controller_path\":\"entry.controller\",\"menu_id\":\"002\",\"menu_parent\":\"001\"},"
                // Purchase Order Entry
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order\",\"fxml_path\":\"Purchase Order\",\"controller_path\":\"po.controller\",\"menu_id\":\"003\",\"menu_parent\":\"002\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryAppliances.fxml\",\"controller_path\":\"PurchaseOrder_EntryAppliances.controller\",\"menu_id\":\"122\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryCar.fxml\",\"controller_path\":\"PurchaseOrder_EntryCar.controller\",\"menu_id\":\"004\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_Entry.fxml\",\"controller_path\":\"PurchaseOrder_Entry.controller\",\"menu_id\":\"005\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryLP.fxml\",\"controller_path\":\"PurchaseOrder_EntryLP.controller\",\"menu_id\":\"006\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryMonarchFood.fxml\",\"controller_path\":\"PurchaseOrder_EntryMonarchFood.controller\",\"menu_id\":\"007\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrder_EntryMonarchHospitality.controller\",\"menu_id\":\"008\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryMC.fxml\",\"controller_path\":\"PurchaseOrder_EntryMC.controller\",\"menu_id\":\"009\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntryMP.fxml\",\"controller_path\":\"PurchaseOrder_EntryMP.controller\",\"menu_id\":\"010\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntrySPCar.fxml\",\"controller_path\":\"PurchaseOrder_EntrySPCar.controller\",\"menu_id\":\"01 02 03 04 05 06\",\"menu_parent\":\"003\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_EntrySPMC.fxml\",\"controller_path\":\"PurchaseOrder_EntrySPMC.controller\",\"menu_id\":\"012\",\"menu_parent\":\"003\"},"
                // Purchase Order Receiving Entry
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Receiving\",\"fxml_path\":\"Purchase Order Receiving\",\"controller_path\":\"po.controller\",\"menu_id\":\"013\",\"menu_parent\":\"002\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryAppliances.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryAppliances.controller\",\"menu_id\":\"119\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryCar.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryCar.controller\",\"menu_id\":\"014\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_Entry.fxml\",\"controller_path\":\"DeliveryAcceptance_Entry.controller\",\"menu_id\":\"015\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryLP.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryLP.controller\",\"menu_id\":\"016\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryMonarchFood.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryMonarchFood.controller\",\"menu_id\":\"017\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryMonarchHospitality.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryMonarchHospitality.controller\",\"menu_id\":\"018\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryMC.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryMC.controller\",\"menu_id\":\"019\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntryMP.fxml\",\"controller_path\":\"DeliveryAcceptance_EntryMP.controller\",\"menu_id\":\"020\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntrySPCar.fxml\",\"controller_path\":\"DeliveryAcceptance_SPCar.controller\",\"menu_id\":\"021\",\"menu_parent\":\"013\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_EntrySPMC.fxml\",\"controller_path\":\"DeliveryAcceptance_EntrySPMC.controller\",\"menu_id\":\"022\",\"menu_parent\":\"013\"},"
                // Purchase Order Return Entry
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Return\",\"fxml_path\":\"Purchase Order Return\",\"controller_path\":\"po.controller\",\"menu_id\":\"086\",\"menu_parent\":\"002\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryAppliances.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryAppliances.controller\",\"menu_id\":\"087\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryCar.controller\",\"menu_id\":\"088\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_Entry.fxml\",\"controller_path\":\"PurchaseOrderReturn_Entry.controller\",\"menu_id\":\"089\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryLP.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryLP.controller\",\"menu_id\":\"090\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryMonarchFood.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryMonarchFood.controller\",\"menu_id\":\"091\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryMonarchHospitality.controller\",\"menu_id\":\"092\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryMC.controller\",\"menu_id\":\"093\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntryMP.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntryMP.controller\",\"menu_id\":\"094\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntrySPCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_SPCar.controller\",\"menu_id\":\"095\",\"menu_parent\":\"086\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_EntrySPMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_EntrySPMC.controller\",\"menu_id\":\"096\",\"menu_parent\":\"086\"},"
                // Purchase Order Confirmation
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Confirmation\",\"fxml_path\":\"Confirmation\",\"controller_path\":\"confirmation.controller\",\"menu_id\":\"023\",\"menu_parent\":\"001\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order\",\"fxml_path\":\"Purchase Order\",\"controller_path\":\"po.controller\",\"menu_id\":\"024\",\"menu_parent\":\"023\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationAppliances.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationAppliances.controller\",\"menu_id\":\"123\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationCar.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationCar.controller\",\"menu_id\":\"025\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_Confirmation.fxml\",\"controller_path\":\"PurchaseOrder_Confirmation.controller\",\"menu_id\":\"026\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationLP.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationLP.controller\",\"menu_id\":\"027\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationMonarchFood.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationMonarchFood.controller\",\"menu_id\":\"028\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationMonarchHospitality.controller\",\"menu_id\":\"029\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationMC.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationMC.controller\",\"menu_id\":\"030\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationMP.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationMP.controller\",\"menu_id\":\"031\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationSPCar.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationSPCar.controller\",\"menu_id\":\"032\",\"menu_parent\":\"024\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ConfirmationSPMC.fxml\",\"controller_path\":\"PurchaseOrder_ConfirmationSPMC.controller\",\"menu_id\":\"033\",\"menu_parent\":\"024\"},"
                // Purchase Order Receiving Confirmation
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Receiving\",\"fxml_path\":\"Purchase Order Receiving\",\"controller_path\":\"po.controller\",\"menu_id\":\"034\",\"menu_parent\":\"023\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationAppliances.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationAppliances.controller\",\"menu_id\":\"120\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationCar.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationCar.controller\",\"menu_id\":\"035\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_Confirmation.fxml\",\"controller_path\":\"DeliveryAcceptance_Confirmation.controller\",\"menu_id\":\"036\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationLP.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationLP.controller\",\"menu_id\":\"037\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationMonarchFood.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationMonarchFood.controller\",\"menu_id\":\"038\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationMonarchHospitality.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationMonarchHospitality.controller\",\"menu_id\":\"039\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationMC.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationMC.controller\",\"menu_id\":\"040\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationMP.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationMP.controller\",\"menu_id\":\"041\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationSPCar.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationSPCar.controller\",\"menu_id\":\"042\",\"menu_parent\":\"034\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ConfirmationSPMC.fxml\",\"controller_path\":\"DeliveryAcceptance_ConfirmationSPMC.controller\",\"menu_id\":\"043\",\"menu_parent\":\"034\"},"
                // Purchase Order Return Confirmation
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Return\",\"fxml_path\":\"Purchase Order Return\",\"controller_path\":\"po.controller\",\"menu_id\":\"097\",\"menu_parent\":\"023\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationAppliances.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationAppliances.controller\",\"menu_id\":\"098\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationCar.controller\",\"menu_id\":\"099\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_Confirmation.fxml\",\"controller_path\":\"PurchaseOrderReturn_Confirmation.controller\",\"menu_id\":\"100\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationLP.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationLP.controller\",\"menu_id\":\"101\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationMonarchFood.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationMonarchFood.controller\",\"menu_id\":\"102\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationMonarchHospitality.controller\",\"menu_id\":\"103\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationMC.controller\",\"menu_id\":\"104\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationMP.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationMP.controller\",\"menu_id\":\"105\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationSPCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_SPCar.controller\",\"menu_id\":\"106\",\"menu_parent\":\"097\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_ConfirmationSPMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_ConfirmationSPMC.controller\",\"menu_id\":\"107\",\"menu_parent\":\"097\"},"
                // Purchase Order Approval
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Approval\",\"fxml_path\":\"Approval\",\"controller_path\":\"approval.controller\",\"menu_id\":\"044\",\"menu_parent\":\"001\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order\",\"fxml_path\":\"Purchase Order\",\"controller_path\":\"po.controller\",\"menu_id\":\"045\",\"menu_parent\":\"044\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalAppliances.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalAppliances.controller\",\"menu_id\":\"124\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalCar.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalCar.controller\",\"menu_id\":\"046\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_Approval.fxml\",\"controller_path\":\"PurchaseOrder_Approval.controller\",\"menu_id\":\"047\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalLP.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalLP.controller\",\"menu_id\":\"048\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalMonarchFood.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalMonarchFood.controller\",\"menu_id\":\"049\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalMonarchHospitality.controller\",\"menu_id\":\"050\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalMC.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalMC.controller\",\"menu_id\":\"051\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalMP.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalMP.controller\",\"menu_id\":\"052\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalSPCar.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalSPCar.controller\",\"menu_id\":\"053\",\"menu_parent\":\"045\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_ApprovalSPMC.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalSPMC.controller\",\"menu_id\":\"054\",\"menu_parent\":\"045\"},"
                // + "{\"access_level\":\"01 02 03 04 05 06\",\"menu_name\":\"Purchase Order
                // Receiving\",\"fxml_path\":\"Purchase Order
                // Receiving\",\"controller_path\":\"po.controller\",\"menu_id\":\"055\",\"menu_parent\":\"044\"},"
                // +
                // "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalCar.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalCar.controller\",\"menu_id\":\"056\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"01 02 03 04 05
                // 06\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_Approval.fxml\",\"controller_path\":\"DeliveryAcceptance_Approval.controller\",\"menu_id\":\"057\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"05\",\"menu_name\":\"Los
                // Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalLP.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalLP.controller\",\"menu_id\":\"058\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"04\",\"menu_name\":\"Monarch
                // Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalMonarchFood.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalMonarchFood.controller\",\"menu_id\":\"059\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"04\",\"menu_name\":\"Monarch
                // Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalMonarchHospitality.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalMonarchHospitality.controller\",\"menu_id\":\"060\",\"menu_parent\":\"055\"},"
                // +
                // "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalMC.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalMC.controller\",\"menu_id\":\"061\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"01\",\"menu_name\":\"Mobile
                // Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalMP.fxml\",\"controller_path\":\"PurchaseOrder_ApprovalMP.controller\",\"menu_id\":\"062\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts
                // Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalSPCar.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalSPCar.controller\",\"menu_id\":\"063\",\"menu_parent\":\"055\"},"
                // + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts
                // Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_ApprovalSPMC.fxml\",\"controller_path\":\"DeliveryAcceptance_ApprovalSPMC.controller\",\"menu_id\":\"064\",\"menu_parent\":\"055\"},"

                // Purchase Order History
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"History\",\"fxml_path\":\"History\",\"controller_path\":\"history.controller\",\"menu_id\":\"065\",\"menu_parent\":\"001\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order\",\"fxml_path\":\"Purchase Order\",\"controller_path\":\"po.controller\",\"menu_id\":\"066\",\"menu_parent\":\"065\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryAppliances.fxml\",\"controller_path\":\"PurchaseOrder_HistoryAppliances.controller\",\"menu_id\":\"125\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryCar.fxml\",\"controller_path\":\"PurchaseOrder_HistoryCar.controller\",\"menu_id\":\"067\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_History.fxml\",\"controller_path\":\"PurchaseOrder_History.controller\",\"menu_id\":\"068\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryLP.fxml\",\"controller_path\":\"PurchaseOrder_HistoryLP.controller\",\"menu_id\":\"069\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryMonarchFood.fxml\",\"controller_path\":\"PurchaseOrder_HistoryMonarchFood.controller\",\"menu_id\":\"070\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrder_HistoryMonarchHospitality.controller\",\"menu_id\":\"071\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryMC.fxml\",\"controller_path\":\"PurchaseOrder_HistoryMC.controller\",\"menu_id\":\"072\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistoryMP.fxml\",\"controller_path\":\"PurchaseOrder_HistoryMP.controller\",\"menu_id\":\"073\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistorySPCar.fxml\",\"controller_path\":\"PurchaseOrder_HistorySPCar.controller\",\"menu_id\":\"074\",\"menu_parent\":\"066\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrder_HistorySPMC.fxml\",\"controller_path\":\"PurchaseOrder_HistorySPMC.controller\",\"menu_id\":\"075\",\"menu_parent\":\"066\"},"
                // Purchase Order Receiving History
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Receiving\",\"fxml_path\":\"Purchase Order Receiving\",\"controller_path\":\"po.controller\",\"menu_id\":\"076\",\"menu_parent\":\"065\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryAppliances.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryAppliances.controller\",\"menu_id\":\"121\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryCar.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryCar.controller\",\"menu_id\":\"077\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_History.fxml\",\"controller_path\":\"DeliveryAcceptance_History.controller\",\"menu_id\":\"078\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryLP.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryLP.controller\",\"menu_id\":\"079\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryMonarchFood.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryMonarchFood.controller\",\"menu_id\":\"080\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryMonarchHospitality.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryMonarchHospitality.controller\",\"menu_id\":\"081\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryMC.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryMC.controller\",\"menu_id\":\"082\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistoryMP.fxml\",\"controller_path\":\"DeliveryAcceptance_HistoryMP.controller\",\"menu_id\":\"083\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistorySPCar.fxml\",\"controller_path\":\"DeliveryAcceptance_HistorySPCar.controller\",\"menu_id\":\"084\",\"menu_parent\":\"076\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/DeliveryAcceptance_HistorySPMC.fxml\",\"controller_path\":\"DeliveryAcceptance_HistorySPMC.controller\",\"menu_id\":\"085\",\"menu_parent\":\"076\"},"
                // Purchase Order Return History
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"Purchase Order Return\",\"fxml_path\":\"Purchase Order Return\",\"controller_path\":\"po.controller\",\"menu_id\":\"108\",\"menu_parent\":\"065\"},"
                + "{\"access_level\":\"07\",\"menu_name\":\"Appliances\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryAppliances.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryAppliances.controller\",\"menu_id\":\"109\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryCar.controller\",\"menu_id\":\"110\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"01 02 03 04 05 06 07\",\"menu_name\":\"General\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_History.fxml\",\"controller_path\":\"PurchaseOrderReturn_History.controller\",\"menu_id\":\"111\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"05\",\"menu_name\":\"Los Pedritos\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryLP.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryLP.controller\",\"menu_id\":\"112\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Restaurant\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryMonarchFood.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryMonarchFood.controller\",\"menu_id\":\"113\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"04\",\"menu_name\":\"Monarch Hospitality\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryMonarchHospitality.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryMonarchHospitality.controller\",\"menu_id\":\"114\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Motorcycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryMC.controller\",\"menu_id\":\"115\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"01\",\"menu_name\":\"Mobile Phone\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistoryMP.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistoryMP.controller\",\"menu_id\":\"116\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"03\",\"menu_name\":\"Spare Parts Car\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistorySPCar.fxml\",\"controller_path\":\"PurchaseOrderReturn_SPCar.controller\",\"menu_id\":\"117\",\"menu_parent\":\"108\"},"
                + "{\"access_level\":\"02\",\"menu_name\":\"Spare Parts Motorycle\",\"fxml_path\":\"/com/rmj/guanzongroup/sidebarmenus/views/PurchaseOrderReturn_HistorySPMC.fxml\",\"controller_path\":\"PurchaseOrderReturn_HistorySPMC.controller\",\"menu_id\":\"118\",\"menu_parent\":\"108\"}"
                + "]";

        JSONParser parser = new JSONParser();
        try {
            try {
                flatMenuItems = (JSONArray) parser.parse(new StringReader(jsonString));
                JSONObject purchasingMainMenu = buildHierarchy("001");
                dissectLeftSideBarJSON(purchasingMainMenu.toJSONString());

            } catch (IOException ex) {
                Logger.getLogger(DashboardController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
