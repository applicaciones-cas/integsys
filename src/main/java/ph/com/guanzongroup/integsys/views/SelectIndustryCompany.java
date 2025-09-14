package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.guanzon.appdriver.constant.Logical;
import ph.com.guanzongroup.integsys.utilities.CustomCommonUtil;

/**
 * FXML Controller class
 *
 * @author Guanzon
 */
public class SelectIndustryCompany implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private String psFormName = "Filter Industry & Company";
    private String psIndustryID = "";
    private String psOldIndustryID;
    private String psCompanyID = "";
    private String psCategoryID = "";
    private String psOldCategoryID = "";
    private String psOldCompanyID;
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

    public void setOldIndsutryID(String fsOldIndustryID) {
        psOldIndustryID = fsOldIndustryID;
    }

    public void setOldCompanyID(String fsOldCompanyID) {
        psOldCompanyID = fsOldCompanyID;
    }

    public void setOldCategoryID(String fsCategoryID) {
        psOldCategoryID = fsCategoryID;
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
                // Check if category is required and not selected
                if ((psIndustryID.equals("02") || psIndustryID.equals("03") || psIndustryID.equals("04"))
                        && (cmbCategory.getSelectionModel().isEmpty())) {
                    ShowMessageFX.Warning("Please select a category.", psFormName, null);
                    return; // Stop further processing
                }
                // No changes?
                if (ShowMessageFX.YesNo("Are you sure you want to change industry or company?", psFormName, null)) {
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
            ObservableList<Industry> industryOptions = FXCollections.observableArrayList(getAllIndustries());
            ObservableList<Company> companyOptions = FXCollections.observableArrayList(getAllCompanies());
            ObservableList<Category> categoryOptions = FXCollections.observableArrayList(getAllCategoryFromIndustry());
            cmbIndustry.setItems(industryOptions);
            cmbCompany.setItems(companyOptions);
            cmbCategory.setItems(categoryOptions);

            for (Industry industry : industryOptions) {
                if (industry.getIndustryID().equals(psOldIndustryID)) {
                    cmbIndustry.getSelectionModel().select(industry);
                    psIndustryID = psOldIndustryID;
                    break;
                }
            }

            // Select old company
            for (Company company : companyOptions) {
                if (company.getCompanyId().equals(psOldCompanyID)) {
                    cmbCompany.getSelectionModel().select(company);
                    psCompanyID = psOldCompanyID;
                    break;
                }
            }
            cmbIndustry.setOnAction(event -> {
                Industry selectedIndustry = (Industry) cmbIndustry.getSelectionModel().getSelectedItem();
                if (selectedIndustry != null) {
                    setIndustryID(selectedIndustry.getIndustryID());
                    System.out.println("Selected industry id: " + selectedIndustry.getIndustryID());

                    try {
                        List<Category> newCategories = getAllCategoryFromIndustry();
                        ObservableList<Category> updatedCategoryOptions = FXCollections.observableArrayList(newCategories);
                        cmbCategory.setItems(updatedCategoryOptions);
                        cmbCategory.getSelectionModel().clearSelection(); // optional
                    } catch (SQLException ex) {
                        Logger.getLogger(SelectIndustryCompany.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            cmbCompany.setOnAction(event -> {
                Company selectedCompany = (Company) cmbCompany.getSelectionModel().getSelectedItem();
                if (selectedCompany != null) {
                    setCompanyID(selectedCompany.getCompanyId());
                    System.out.println("test company id: " + selectedCompany.getCompanyId());
                }
            });
            cmbCategory.setOnAction(event -> {
                Category selectedCategory = (Category) cmbCategory.getSelectionModel().getSelectedItem();
                if (selectedCategory != null) {
                    setCategoryID(selectedCategory.getCategoryId());
                    System.out.println("test company id: " + selectedCategory.getCategoryId());
                }
            });
        } catch (SQLException ex) {
            Logger.getLogger(SelectIndustryCompany.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<Company> getAllCompanies() throws SQLException {
        List<Company> companies = new ArrayList<>();
        String lsSQL = "SELECT * FROM company";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        ResultSet rs = poApp.executeQuery(lsSQL);

        while (rs.next()) {
            String id = rs.getString("sCompnyID");
            String name = rs.getString("sCompnyNm");
            companies.add(new Company(id, name));
        }

        MiscUtil.close(rs);
        return companies;
    }

    private List<Industry> getAllIndustries() throws SQLException {
        List<Industry> industries = new ArrayList<>();
        String lsSQL = "SELECT * FROM industry";
        lsSQL = MiscUtil.addCondition(lsSQL, "cRecdStat = " + SQLUtil.toSQL(Logical.YES));
        ResultSet loRS = poApp.executeQuery(lsSQL);

        while (loRS.next()) {
            String id = loRS.getString("sIndstCdx");
            String description = loRS.getString("sDescript");
            industries.add(new Industry(id, description));
        }

        MiscUtil.close(loRS);
        return industries;

    }

    private List<Category> getAllCategoryFromIndustry() throws SQLException {
        List<Category> category = new ArrayList<>();
        String lsSQL = null;
        lsSQL = "SELECT * FROM category";
        switch (psIndustryID) {
            case "03": // Vehicle
                lsSQL = MiscUtil.addCondition(lsSQL, " sIndstCdx = " + SQLUtil.toSQL(psIndustryID)
                        + " AND cRecdStat = " + SQLUtil.toSQL(Logical.YES)
                        + " AND (sDescript IN ('Vehicle', 'Spare Parts'))");
                System.out.println("sql:" + lsSQL);
                break;
            case "02": // Motorcycle
                lsSQL = MiscUtil.addCondition(lsSQL, " sIndstCdx = " + SQLUtil.toSQL(psIndustryID)
                        + " AND cRecdStat = " + SQLUtil.toSQL(Logical.YES)
                        + " AND (sDescript IN ('Motorcycle', 'Spare Parts'))");
                break;
            case "04": // Hospitality
                lsSQL = MiscUtil.addCondition(lsSQL, " sIndstCdx = " + SQLUtil.toSQL(psIndustryID)
                        + " AND cRecdStat = " + SQLUtil.toSQL(Logical.YES)
                        + " AND (sDescript IN ('Food Service', 'Hospitality'))");
                break;
            default:
                lsSQL = "";
                return category;
        }

        ResultSet rs = poApp.executeQuery(lsSQL);

        while (rs.next()) {
            String id = rs.getString("sCategrCd");
            String name = rs.getString("sDescript");
            category.add(new Category(id, name));
        }

        MiscUtil.close(rs);
        return category;
    }

    class Industry {

        private String industryID;
        private String industryName;

        public Industry(String industryID, String industryName) {
            this.industryID = industryID;
            this.industryName = industryName;
        }

        public String getIndustryID() {
            return industryID;
        }

        public String getIndustryName() {
            return industryName;
        }

        @Override
        public String toString() {
            return industryName;
        }
    }

    class Company {

        private String companyID;
        private String companyName;

        public Company(String companyID, String companyName) {
            this.companyID = companyID;
            this.companyName = companyName;
        }

        public String getCompanyId() {
            return companyID;
        }

        public String getCompanyName() {
            return companyName;
        }

        @Override
        public String toString() {
            return companyName;
        }
    }

    class Category {

        private String categoryID;
        private String categoryName;

        public Category(String categoryID, String categoryName) {
            this.categoryID = categoryID;
            this.categoryName = categoryName;
        }

        public String getCategoryId() {
            return categoryID;
        }

        public String getCategoryName() {
            return categoryName;
        }

        @Override
        public String toString() {
            return categoryName;
        }
    }
}
