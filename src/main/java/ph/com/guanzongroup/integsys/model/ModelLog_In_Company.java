/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author User
 */
public class ModelLog_In_Company {

    private String companyID;
    private String companyName;

    public ModelLog_In_Company(String companyID, String companyName) {
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
