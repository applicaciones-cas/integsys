/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author PC
 */
public class ModelInvTableListInformation {
    public StringProperty index01;
    public StringProperty index02;
    public StringProperty index03;
    public StringProperty index04;
  
    

    
    
    public ModelInvTableListInformation(String index01,
            String index02,
            String index03,
            String index04
        
            ) {

        this.index01 = new SimpleStringProperty(index01);
        this.index02 = new SimpleStringProperty(index02);
        this.index03 = new SimpleStringProperty(index03);
        this.index04 = new SimpleStringProperty(index04);
  
       
        
        
    }
    public String getIndex01() {
        return index01.get();
    }

    public void setIndex01(String index01) {
        this.index01.set(index01);
    }

    public String getIndex02() {
        return index02.get();
    }

    public void setIndex02(String index02) {
        this.index02.set(index02);
    }

    public String getIndex03() {
        return index03.get();
    }

    public void setIndex03(String index03) {
        this.index03.set(index03);
    }

    public String getIndex04() {
        return index04.get();
    }

    public void setIndex04(String index04) {
        this.index04.set(index04);
    }

    


}
