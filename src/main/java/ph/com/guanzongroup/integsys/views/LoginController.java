package ph.com.guanzongroup.integsys.views;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.agent.systables.SystemUser;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Company;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Industry;
import ph.com.guanzongroup.integsys.model.ModelLog_In_User;
import ph.com.guanzongroup.integsys.utility.JFXUtil;

public class LoginController implements Initializable, ScreenInterface {
    private final String MODULE = "Login";
    
    private GRiderCAS oApp;
    private String psIndustryID = "";
    private String psCompanyID = "";
    
    private boolean isMainOffice = false;
    private boolean isWarehouse = false;
    
    private LogWrapper poLogWrapper;
    private DashboardController dashboardController;
    
    ObservableList<ModelLog_In_Industry> industryOptions = FXCollections.observableArrayList();
    ObservableList<ModelLog_In_Company> companyOptions = FXCollections.observableArrayList();
    ObservableList<ModelLog_In_User> users = FXCollections.observableArrayList();
    
    @FXML
    private TextField tfUsername;
    @FXML
    private Button btnSignIn;
    @FXML
    Label lblCopyright;
    @FXML
    private TextField tfPassword;
    @FXML
    private PasswordField pfPassword;
    @FXML
    private Button btnEyeIcon;
    @FXML
    private ComboBox cmbIndustry, cmbCompany;

    @Override
    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
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
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DashboardController mainController = LoginControllerHolder.getMainController();
        mainController.triggervbox();
               
        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());
        
        String year = String.valueOf(Year.now().getValue());
        
        lblCopyright.setStyle("-fx-font-size: 13px;");
        lblCopyright.setText("© " + year + " Guanzon Group of Companies. All Rights Reserved.");

        initComboBox();
        initTextFields();
    }
    
    EventHandler<KeyEvent> tabKeyHandler = event -> {
        if (event.getCode() != KeyCode.TAB) {
            return;
        }
        Node source = (Node) event.getSource();
        String fieldId = source.getId();
        event.consume(); // prevent default focus traversal
        switch (fieldId) {
            case "tfUsername":
                if (pfPassword.isVisible()) {
                    pfPassword.requestFocus();
                } else {
                    tfPassword.requestFocus();
                }
                break;
            case "pfPassword":
            case "tfPassword":
                cmbIndustry.requestFocus();
                break;
            case "cmbIndustry":
                cmbCompany.requestFocus();
                break;
            case "cmbCompany":
                tfUsername.requestFocus();
                break;
        }
    };

    public void initTextFields() {
        tfUsername.setOnKeyPressed(tabKeyHandler);
        pfPassword.setOnKeyPressed(tabKeyHandler);
        tfPassword.setOnKeyPressed(tabKeyHandler);
        cmbCompany.setOnKeyPressed(tabKeyHandler);
        cmbIndustry.setOnKeyPressed(tabKeyHandler);
    }

    public void setMainController(DashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void cmdButton_Click(ActionEvent event) {
        try {
            String lsButton = ((Button) event.getSource()).getId();
            switch (lsButton) {
                case "btnSignIn":
                    JSONObject poJSON = ValidateLogin();
                    
                    if (!"success".equals((String) poJSON.get("result"))) {
                        ShowMessageFX.Warning((String) poJSON.get("message"), MODULE, null);
                    } else {
                        if (!oApp.logUser("gRider", (String) poJSON.get("userId"))) {
                            ShowMessageFX.Warning(oApp.getMessage(), "Warning", null);
                            return;
                        }
                        
                        DashboardController dashboardController = LoginControllerHolder.getMainController();
                        dashboardController.triggervbox2();
                        
                        dashboardController.setUserIndustry(psIndustryID);
                        dashboardController.setUserCompany(psCompanyID);
                        dashboardController.changeUserInfo();
                        
                        //set the orignal industry and company
                        System.setProperty("sys.industry", psIndustryID);
                        System.setProperty("sys.company", psCompanyID);
                        
                        LoginControllerHolder.setLogInStatus(true);
                    }
                    break;
                case "btnEyeIcon":
                    FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                    if (pfPassword.isVisible()) {
                        tfPassword.setText(pfPassword.getText());
                        pfPassword.setVisible(false);
                        tfPassword.setVisible(true);
                        eyeIcon.setIcon(FontAwesomeIcon.EYE);
                        eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                        btnEyeIcon.setGraphic(eyeIcon);
                    } else {
                        pfPassword.setText(tfPassword.getText());
                        tfPassword.setVisible(false);
                        pfPassword.setVisible(true);
                        eyeIcon.setIcon(FontAwesomeIcon.EYE_SLASH);
                        eyeIcon.setStyle("-fx-fill: gray; -glyph-size: 20; ");
                        btnEyeIcon.setGraphic(eyeIcon);
                    }
                    break;
                default:
                    break;
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
    }

    public void reloadCompnanyItems() throws SQLException{
        companyOptions = FXCollections.observableArrayList(getAllCompanies((ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem()));
        cmbCompany.setItems(companyOptions);
        
        if (!companyOptions.isEmpty()){
            cmbCompany.getSelectionModel().select(companyOptions.get(0));
        } else {
            cmbCompany.getSelectionModel().select(companyOptions.get(-1));
        }
    }

    EventHandler<ActionEvent> comboBoxHandler = event -> {
        ComboBox<?> source = (ComboBox<?>) event.getSource();
        String id = source.getId();
        switch (id) {
            case "cmbCompany":
                try {
                    ModelLog_In_Company selectedCompany = (ModelLog_In_Company) cmbCompany.getSelectionModel().getSelectedItem();
                    if (selectedCompany != null) {
                        psCompanyID = selectedCompany.getCompanyId();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
                }
                break;
            case "cmbIndustry":
                try {
                    ModelLog_In_Industry selectedIndustry = (ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem();
                    if (selectedIndustry != null) {
                        psIndustryID = selectedIndustry.getIndustryID();
                        
                        reloadCompnanyItems();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            break;
        }
    };

    private void initComboBox() {
        JFXUtil.initComboBoxCellDesignColor("#FF8201", cmbCompany, cmbIndustry);
        
        loadComboBoxItems();
        
        JFXUtil.setActionListener(comboBoxHandler, cmbIndustry, cmbCompany);
    }

    private void loadComboBoxItems() {
        try {
            boolean lbShow = oApp.getIndustry().equals(System.getProperty("sys.main.industry")) ||
                                oApp.getIndustry().equals(System.getProperty("sys.general.industry"));
            
            if (lbShow){
                industryOptions = FXCollections.observableArrayList(getAllIndustries());
                companyOptions = FXCollections.observableArrayList(getAllCompanies(industryOptions.get(0)));

                cmbIndustry.setItems(industryOptions);
                if (!cmbIndustry.getItems().isEmpty()){
                    cmbIndustry.getSelectionModel().select(0);
                    psIndustryID = industryOptions.get(0).getIndustryID();
                }

                cmbCompany.setItems(companyOptions);
                if (!cmbCompany.getItems().isEmpty()){
                    cmbCompany.getSelectionModel().select(0);
                    psCompanyID = companyOptions.get(0).getCompanyId();
                }
            } else {
                psIndustryID = oApp.getIndustry();
                psCompanyID = oApp.getCompnyId();
            }
            
            cmbIndustry.setVisible(lbShow);
            cmbCompany.setVisible(lbShow);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public JSONObject ValidateLogin() {
        JSONObject poJSON = new JSONObject();
        boolean isUsernameFilled = tfUsername.getText().trim().isEmpty();

        if (pfPassword.isVisible()) {
            tfPassword.setText(pfPassword.getText());
        } else {
            pfPassword.setText(tfPassword.getText());
        }

        boolean isPasswordFilled = tfPassword.getText().trim().isEmpty();
        if (!isUsernameFilled && !isPasswordFilled) {
            if (psIndustryID.equals("") || psCompanyID.equals("")) {
                JFXUtil.setJSONError(poJSON, "Please fill all the fields");
            } else {
                try {
                    SystemUser sysuser = new SystemUser();
                    sysuser.setApplicationDriver(oApp);
                    sysuser.setLogWrapper(null);

                    JSONObject loJSON = sysuser.isValidUser(tfUsername.getText().trim(), tfPassword.getText().trim(), oApp.getProductID());

                    if ("success".equals((String) loJSON.get("result"))) return loJSON;
                } catch (SQLException | GuanzonException e) {
                    JFXUtil.setJSONError(poJSON, e.getMessage());
                }
            }
        } else {
            JFXUtil.setJSONError(poJSON, "Please fill all the fields");
        }

        return poJSON;
    }

    private List<ModelLog_In_User> getAllUsers() throws SQLException {
        List<ModelLog_In_User> user = new ArrayList<>();
        String lsSQL = "SELECT * FROM xxxsysuser";
        ResultSet rs = oApp.executeQuery(lsSQL);

        while (rs.next()) {
            String userID = rs.getString("sUserIDxx");
            String username = rs.getString("sUserName");
            String userpassword = rs.getString("sPassword");
            user.add(new ModelLog_In_User(userID, username, userpassword));
        }
        MiscUtil.close(rs);
        return user;
    }

    private List<ModelLog_In_Company> getAllCompanies(ModelLog_In_Industry industry) throws SQLException {
        List<ModelLog_In_Company> companies = new ArrayList<>();
        
        String lsSQL = "SELECT * FROM Company" +
                        " WHERE cRecdStat = '1'" +
                        " ORDER BY sCompnyNm";
        
        String lsCompany = industry.getCompanyID();
        String [] laCompany  = lsCompany.split(";");
        
        lsCompany = "";
        for (int lnCtr = 0; lnCtr <= laCompany.length - 1; lnCtr++) {
            lsCompany += ", " + SQLUtil.toSQL(laCompany[lnCtr]);
        }
        
        if (!lsCompany.isEmpty()){
            lsCompany = "(" + lsCompany.substring(2) + ")";
            lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyID IN " + lsCompany);
        }

        ResultSet rs = oApp.executeQuery(lsSQL);
        while (rs.next()) {
            String id = rs.getString("sCompnyID");
            String name = rs.getString("sCompnyNm");
            companies.add(new ModelLog_In_Company(id, name));
        }
        
        MiscUtil.close(rs);
        
        return companies;
    }

    private List<ModelLog_In_Industry> getAllIndustries() throws SQLException {
        List<ModelLog_In_Industry> industries = new ArrayList<>();
        
        String lsSQL = "SELECT * FROM Industry" +
                        " WHERE cRecdStat = '1'" +
                        " ORDER BY sDescript";

        ResultSet loRS = oApp.executeQuery(lsSQL);

        while (loRS.next()) {
            industries.add(new ModelLog_In_Industry(
                    loRS.getString("sIndstCdx"), 
                    loRS.getString("sDescript"), 
                    loRS.getString("sCompnyID")));
        }
        MiscUtil.close(loRS);
        
        return industries;
    }
}
