package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Category;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Company;
import ph.com.guanzongroup.integsys.model.ModelLog_In_Industry;
import ph.com.guanzongroup.integsys.utility.CustomCommonUtil;

/**
 * FXML Controller class
 *
 * @author Guanzon
 */
public class SelectIndustryCompany implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private String psFormName = "Filter Industry & Company";
    private String psIndustryID = "";
    private String psCompanyID = "";
    private String psCategoryID = "";
    
    private boolean pbIsFromFilter;
    private LogWrapper poLogWrapper;
    
    @FXML
    private AnchorPane apButton;
    @FXML
    private AnchorPane AnchorMain;
    @FXML
    private StackPane StackPane;
    @FXML
    private HBox hbButtons;
    @FXML
    private Button btnOkay, btnClose;
    @FXML
    private ComboBox cmbIndustry, cmbCompany, cmbCategory;
    
    ObservableList<ModelLog_In_Industry> industryOptions = FXCollections.observableArrayList();
    ObservableList<ModelLog_In_Company> companyOptions = FXCollections.observableArrayList();

    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsIndustryID) {
        psIndustryID = fsIndustryID;
    }

    @Override
    public void setCompanyID(String fsCompanyID) {
        psCompanyID = fsCompanyID;
    }

    public void setCategoryID(String fsCategoryID) {
        psCategoryID = fsCategoryID;
    }

    public void isFromFilter(boolean fsIsFromFilter) {
        pbIsFromFilter = fsIsFromFilter;
    }

    public String getSelectedIndustryID() {
        return psIndustryID;
    }

    public String getSelectedCompanyID() {
        return psCompanyID;
    }

    public String getSelectedCategoryID() {
        return psCategoryID;
    }

    public boolean isFromFilter() {
        return pbIsFromFilter;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //switchtab
        CustomCommonUtil.setDropShadow(AnchorMain, StackPane);
        initButtonsClickActions();
        initComboBoxActions();
    }

    private void initButtonsClickActions() {
        btnOkay.setOnAction(this::handleButtonAction);
        btnClose.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        String lsButton = ((Button) event.getSource()).getId();
        switch (lsButton) {
            case "btnOkay":
//                // Check if category is required and not selected
//                if (psIndustryID.equals(System.getProperty("sys.general.industry")) && cmbCategory.getSelectionModel().isEmpty()) {
//                    ShowMessageFX.Warning("Please select a category.", psFormName, null);
//                    return; // Stop further processing
//                }
//                // No changes?
                if (ShowMessageFX.YesNo("Are you sure you want to change connection?", psFormName, null)) {
                    System.setProperty("user.selected.industry", psIndustryID);
                    System.setProperty("user.selected.company", psCompanyID);
                    
                    pbIsFromFilter = true;
                    CommonUtils.closeStage(btnOkay);
                }
                break;
            case "btnClose":
                CommonUtils.closeStage(btnClose);
                break;

            default:
                ShowMessageFX.Warning("Invalid button to click, please notify the admin.", psFormName, null);
                break;
        }
    }

    private void initComboBoxActions() {
        try {
            boolean lbShow = (System.getProperty("user.selected.industry")).equals(System.getProperty("sys.main.industry")) ||
                                (System.getProperty("user.selected.industry")).equals(System.getProperty("sys.general.industry"));

            if (lbShow) {
                psIndustryID = poApp.getIndustry();

                industryOptions = FXCollections.observableArrayList(getAllIndustries());

                cmbIndustry.setItems(industryOptions);
                if (!cmbIndustry.getItems().isEmpty()) {
                    for (int lnCtr = 0; lnCtr <= industryOptions.size() - 1; lnCtr++){
                        if (industryOptions.get(0).getIndustryID().equals(System.getProperty("user.selected.industry"))){
                            cmbIndustry.getSelectionModel().select(lnCtr);
                            psIndustryID = industryOptions.get(lnCtr).getIndustryID();
                            
                            companyOptions = FXCollections.observableArrayList(getAllCompanies(industryOptions.get(lnCtr)));
                        }
                    }
                }
                

                cmbCompany.setItems(companyOptions);
                if (!cmbCompany.getItems().isEmpty()) {
                    for (int lnCtr = 0; lnCtr <= companyOptions.size() - 1; lnCtr++){
                        if (companyOptions.get(lnCtr).getCompanyId().equals(System.getProperty("user.selected.company"))){
                            cmbCompany.getSelectionModel().select(lnCtr);
                            psCompanyID = companyOptions.get(lnCtr).getCompanyId();
                        }
                    }
                }
            } else {
                psIndustryID = System.getProperty("user.selected.industry");
                psCompanyID = System.getProperty("user.selected.company");
            }

            cmbIndustry.setVisible(lbShow);
            cmbCompany.setVisible(lbShow);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private List<ModelLog_In_Company> getAllCompanies(ModelLog_In_Industry industry) throws SQLException {
        List<ModelLog_In_Company> companies = new ArrayList<>();

        String lsSQL = "SELECT * FROM Company"
                + " WHERE cRecdStat = '1'"
                + " ORDER BY sCompnyNm";

        String lsCompany = industry.getCompanyID();
        String[] laCompany = lsCompany.split(";");

        lsCompany = "";
        for (int lnCtr = 0; lnCtr <= laCompany.length - 1; lnCtr++) {
            lsCompany += ", " + SQLUtil.toSQL(laCompany[lnCtr]);
        }

        if (!lsCompany.isEmpty()) {
            lsCompany = "(" + lsCompany.substring(2) + ")";
            lsSQL = MiscUtil.addCondition(lsSQL, "sCompnyID IN " + lsCompany);
        }

        ResultSet rs = poApp.executeQuery(lsSQL);
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

        String lsSQL = "SELECT * FROM Industry"
                        + " WHERE cRecdStat = '1'"
                        + " ORDER BY sDescript";

        if (!psIndustryID.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryID));
        }

        ResultSet loRS = poApp.executeQuery(lsSQL);

        while (loRS.next()) {
            industries.add(new ModelLog_In_Industry(
                    loRS.getString("sIndstCdx"),
                    loRS.getString("sDescript"),
                    loRS.getString("sCompnyID")));
        }
        MiscUtil.close(loRS);

        return industries;
    }

    private List<ModelLog_In_Category> getAllCategoryFromIndustry() throws SQLException {
        List<ModelLog_In_Category> categories = new ArrayList<>();

        String lsSQL = "SELECT * FROM Category" +
                        " WHERE cRecdStat = '1'" +
                        " ORDER BY sDescript";

        if (!psIndustryID.isEmpty()) {
            lsSQL = MiscUtil.addCondition(lsSQL, "sIndstCdx = " + SQLUtil.toSQL(psIndustryID));
        }

        ResultSet loRS = poApp.executeQuery(lsSQL);

        while (loRS.next()) {
            categories.add(new ModelLog_In_Category(
                                loRS.getString("sCategrCd"),
                                loRS.getString("sDescript")));
        }
        MiscUtil.close(loRS);

        return categories;
    }
}
