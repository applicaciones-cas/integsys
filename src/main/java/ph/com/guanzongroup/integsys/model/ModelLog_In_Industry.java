/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.model;

import org.guanzon.appdriver.base.GRiderCAS;


/**
 *
 * @author User
 */
public class ModelLog_In_Industry {

    private GRiderCAS oApp;
    private String industryID;
    private String industryName;
    private String companyID;

    public ModelLog_In_Industry(String industryID, String industryName) {
        this.industryID = industryID;
        this.industryName = industryName;
    }
    
    public ModelLog_In_Industry(String industryID, String industryName, String companyID) {
        this.industryID = industryID;
        this.industryName = industryName;
        this.companyID = companyID;
    }

    public String getIndustryID() {
        return industryID;
    }

    public String getIndustryName() {
        return industryName;
    }
    
    public String getCompanyID(){
        return companyID;
    }

    @Override
    public String toString() {
        return industryName;
    }

}
