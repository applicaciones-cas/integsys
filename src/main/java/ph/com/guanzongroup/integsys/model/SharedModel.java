/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.guanzon.appdriver.constant.TransactionStatus;

/**
 *
 * @author User
 */
public class SharedModel {
    public static String sharedString = "";
    public static ObservableList<String> TRANSACTION_STATUS = FXCollections.observableArrayList("OPEN", "CLOSED", "POSTED","CANCELLED", "VOID", "UNKNOWN");
   
}
