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
public class ModelLog_In_User {

    private GRiderCAS oApp;
    private String userID;
    private String userName;
    private String userPassword;

    public ModelLog_In_User(String userID, String userName, String userPassword) {
        this.userID = userID;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

//    @Override
//    public String toString() {
//        return industryName;
//    }
}
