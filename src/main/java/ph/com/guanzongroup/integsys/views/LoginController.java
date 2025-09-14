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
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.Logical;
import java.util.Arrays;
import java.util.function.Function;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Company;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Industry;
import ph.com.guanzongroup.integsys.model.ModelLog_In_User;
import ph.com.guanzongroup.integsys.utilities.JFXUtil;

/**
 * FXML Controller class
 *
 * @author Guanzon
 */
public class LoginController implements Initializable, ScreenInterface {

    private final String pxeModuleName = "Log In";
    private ParamControllers poParameter;
    private GRiderCAS oApp;
    private String psIndustryID = "";
    private String psCompanyID = "";
    private boolean isMainOffice = true;
    private boolean isWarehouse = true;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DashboardController mainController = LoginControllerHolder.getMainController();
        mainController.triggervbox();
        poParameter = new ParamControllers(oApp, poLogWrapper);
        tfPassword.textProperty().bindBidirectional(pfPassword.textProperty());
        String year = String.valueOf(Year.now().getValue());
        lblCopyright.setStyle("-fx-font-size: 13px;");
        lblCopyright.setText("© " + year + " Guanzon Group of Companies. All Rights Reserved.");

        initComboBox();
        autoloadRecord();
        initTextFields();

    }

    public static <T> int getComboBoxIndexByProperty(ComboBox<T> comboBox, Function<T, String> propertyGetter, String targetValue) {
        for (int i = 0; i < comboBox.getItems().size(); i++) {
            T item = comboBox.getItems().get(i);
            if (propertyGetter.apply(item).equalsIgnoreCase(targetValue)) {
                return i;
            }
        }
        return -1;
    }

    public void autoloadRecord() {
        // auto load based on config
        cmbCompany.getSelectionModel().select(getComboBoxIndexByProperty(cmbCompany, ModelLog_In_Company::getCompanyName, getCompany()[1])); //  select based on companyID
        psCompanyID = getCompany()[0];

        reloadCmbIndustryItems(); // reload industries based on Company selected
        cmbIndustry.getSelectionModel().select(getComboBoxIndexByProperty(cmbIndustry, ModelLog_In_Industry::getIndustryName, getIndustryName()));
        psIndustryID = oApp.getIndustry();

        //set effect of industry disabled
//        cmbIndustry.setDisable(true);
//        cmbCompany.setDisable(true);
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

    public String[] getCompany() {
        String[] result = new String[2];
        try {
            JSONObject loJSON = new JSONObject();
            loJSON = poParameter.Branch().searchRecord(oApp.getBranchCode(), true);
            if ("success".equals((String) loJSON.get("result"))) {
                isMainOffice = oApp.isMainOffice();
                isWarehouse = oApp.isWarehouse();
                if (!isMainOffice || !isWarehouse) {
                    loJSON = poParameter.Company().searchRecord(poParameter.Branch().getModel().getCompanyId(), true);
                    if (!"success".equals((String) loJSON.get("result"))) {
                        ShowMessageFX.Warning((String) loJSON.get("message"), pxeModuleName, "Company");
                        return result;
                    }
                    psCompanyID = poParameter.Company().getModel().getCompanyId();
                    result[0] = poParameter.Company().getModel().getCompanyId();
                    result[1] = poParameter.Company().getModel().getCompanyName();
                } else {
                    String lsSQL = "SELECT b.sCompnyID,"
                            + " c.sCompnyNm "
                            + " FROM branch b "
                            + " JOIN company c ON b.sCompnyID = c.sCompnyID ";
                    ResultSet loRS = oApp.executeQuery(lsSQL);
                    if (loRS.next()) {
                        result[0] = loRS.getString("sCompnyID");
                        result[1] = loRS.getString("sCompnyNm");
                    }
                    MiscUtil.close(loRS);
                }
            }
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String getIndustryName() {
        String lsIndustryNm = "";
        try {
            String lsSQL = "SELECT sDescript FROM industry ";
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(oApp.getIndustry()));
            ResultSet loRS = oApp.executeQuery(lsSQL);

            if (loRS.next()) {
                lsIndustryNm = loRS.getString("sDescript");
            }

            MiscUtil.close(loRS);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lsIndustryNm;
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
                        System.err.println((String) poJSON.get("message"));
                        ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
                    } else {

                        if (!oApp.logUser("gRider", (String) poJSON.get("userID"))) {
                            System.out.println("Log in status: " + oApp.getMessage() + "\nDefault user logged as " + oApp.getUserID() + "\nUser Level " + oApp.getUserLevel());
                        } else {
                            System.out.println("Log in status: " + oApp.getMessage() + "\nUser logged as " + oApp.getUserID() + "\nUser Level " + oApp.getUserLevel());
                        }
                        DashboardController dashboardController = LoginControllerHolder.getMainController();
                        dashboardController.triggervbox2();
                        dashboardController.setUserIndustry(psIndustryID);
                        dashboardController.setUserCompany(psCompanyID);
                        dashboardController.changeUserInfo(psIndustryID);
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
        } catch (SQLException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GuanzonException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reloadCmbIndustryItems() {
        try {
            industryOptions = FXCollections.observableArrayList(getAllIndustries(psCompanyID));
            cmbIndustry.setItems(industryOptions);
            cmbIndustry.getSelectionModel().select(industryOptions.get(0));
            ModelLog_In_Industry selectedIndustry = (ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem();
            if (selectedIndustry != null) {
                psIndustryID = selectedIndustry.getIndustryID();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
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
                    System.out.println("Company ID: " + psCompanyID);

                    reloadCmbIndustryItems();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
            case "cmbIndustry":
            try {
                ModelLog_In_Industry selectedIndustry = (ModelLog_In_Industry) cmbIndustry.getSelectionModel().getSelectedItem();
                if (selectedIndustry != null) {
                    psIndustryID = selectedIndustry.getIndustryID();
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
            industryOptions = FXCollections.observableArrayList(getAllIndustries(""));
            companyOptions = FXCollections.observableArrayList(getAllCompanies());
            cmbIndustry.setItems(industryOptions);
            cmbCompany.setItems(companyOptions);
        } catch (SQLException ex) {
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
                JFXUtil.setJSONSuccess(poJSON, "success");
                try {
                    // final log
                    users.clear();
                    users = FXCollections.observableArrayList(getAllUsers());

                    for (ModelLog_In_User user : users) {
                        if (user.getUserName().equals(tfUsername.getText().trim()) && user.getUserPassword().equals(tfPassword.getText().trim())) {
                            System.out.println("Login success for user ID: " + user.getUserID());
                            poJSON.put("userID", user.getUserID());
                        }
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
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
//        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
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

    private List<ModelLog_In_Company> getAllCompanies() throws SQLException {
        List<ModelLog_In_Company> companies = new ArrayList<>();
        String lsSQL = "SELECT * FROM company";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        ResultSet rs = oApp.executeQuery(lsSQL);

        while (rs.next()) {
            String id = rs.getString("sCompnyID");
            String name = rs.getString("sCompnyNm");
            companies.add(new ModelLog_In_Company(id, name));
        }
        MiscUtil.close(rs);
        return companies;
    }

    private List<ModelLog_In_Industry> getAllIndustries(String companyid) throws SQLException {
        List<String> result = Arrays.asList(companyid.split(";"));

        List<ModelLog_In_Industry> industries = new ArrayList<>();
        String lsSQL = "SELECT * FROM industry";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        StringBuilder condition = new StringBuilder();
        if (result.size() >= 1) {
            for (int i = 0; i < result.size(); i++) {
                condition.append("sCompnyID LIKE '%").append(result.get(i)).append("%'");
                if (i < result.size() - 1) {
                    condition.append(" OR ");
                }
            }
            lsSQL = MiscUtil.addCondition(lsSQL, condition.toString());
        }
        ResultSet loRS = oApp.executeQuery(lsSQL);

        while (loRS.next()) {
            String id = loRS.getString("sIndstCdx");
            String description = loRS.getString("sDescript");
            industries.add(new ModelLog_In_Industry(id, description));
        }
        MiscUtil.close(loRS);
        return industries;
    }

}
