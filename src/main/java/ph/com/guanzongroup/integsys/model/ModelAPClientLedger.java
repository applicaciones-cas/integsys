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
public class ModelAPClientLedger {
    
    public SimpleStringProperty index01;
    public SimpleStringProperty index02;
    public SimpleStringProperty index03;
    public SimpleStringProperty index04; 
    public SimpleStringProperty index05;
    public SimpleStringProperty index06; 
    public SimpleStringProperty index07;
    public SimpleStringProperty index08; 
    public SimpleStringProperty index09;
    public SimpleStringProperty index10;
    public SimpleStringProperty index11;
    public SimpleStringProperty index12;
    public SimpleStringProperty index13;
    public SimpleStringProperty index14; 
    public SimpleStringProperty index15;
    public SimpleStringProperty index16; 
    public SimpleStringProperty index17;
    public SimpleStringProperty index18; 
    public SimpleStringProperty index19;
    public SimpleStringProperty index20;
    
    
    ObservableList<String> SoldStat = FXCollections.observableArrayList("YES", "NO");
    ObservableList<String> UnitType = FXCollections.observableArrayList("LDU",
                "Regular",
                "Free",
                "Live",
                "Service",
                "RDU",
                "Others");
    
    public ModelAPClientLedger(String index01,
               String index02,
               String index03,
               String index04,
               String index05,
               String index06,
               String index07,
               String index08,
               String index09,
               String index10,
               String index11,
               String index12,
               String index13,
               String index14,
               String index15,
               String index16,
               String index17,
               String index18,
               String index19,
               String index20){
        
        this.index01 = new SimpleStringProperty(index01);
        this.index02 = new SimpleStringProperty(index02);
        this.index03 = new SimpleStringProperty(index03);
        this.index04 = new SimpleStringProperty(index04);
        this.index05 = new SimpleStringProperty(index05);
        this.index06 = new SimpleStringProperty(index06);
        this.index07 = new SimpleStringProperty(index07);
        this.index08 = new SimpleStringProperty(index08);
        this.index09 = new SimpleStringProperty(index09);
        this.index10 = new SimpleStringProperty(index10);
        this.index11 = new SimpleStringProperty(index11);
        this.index12 = new SimpleStringProperty(index12);
        this.index13 = new SimpleStringProperty(index13);
        this.index14 = new SimpleStringProperty(index14);
        this.index15 = new SimpleStringProperty(index15);
        this.index16 = new SimpleStringProperty(index16);
        this.index17 = new SimpleStringProperty(index17);
        this.index18 = new SimpleStringProperty(index18);
        this.index19 = new SimpleStringProperty(index19);
        this.index20 = new SimpleStringProperty(index20);
    }
    public ModelAPClientLedger(String index01,
               String index02,
               String index03,
               String index04,
               String index05,
               String index06,
               String index07,
               String index08){
        
        this.index01 = new SimpleStringProperty(index01);
        this.index02 = new SimpleStringProperty(index02);
        this.index03 = new SimpleStringProperty(index03);
        this.index04 = new SimpleStringProperty(index04);
        this.index05 = new SimpleStringProperty(index05);
        this.index06 = new SimpleStringProperty(index06);
        this.index07 = new SimpleStringProperty(index07);
        this.index08 = new SimpleStringProperty(index08);
    }
    public ModelAPClientLedger(String index01,
               String index02,
               String index03,
               String index04,
               String index05,
               String index06){
        
        this.index01 = new SimpleStringProperty(index01);
        this.index02 = new SimpleStringProperty(index02);
        this.index03 = new SimpleStringProperty(index03);
        this.index04 = new SimpleStringProperty(index04);
        this.index05 = new SimpleStringProperty(index05);
        this.index06 = new SimpleStringProperty(index06);
    }

   

    

    public String getIndex01(){return index01.get();}
    public void setIndex01(String index01){this.index01.set(index01);}
    
    public String getIndex02(){return index02.get();}
    public void setIndex02(String index02){this.index02.set(index02);}
    
    public String getIndex03(){return index03.get();}
    public void setIndex03(String index03){this.index03.set(index03);}
    
    public String getIndex04(){return index04.get();}
    public void setIndex04(String index04){this.index04.set(index04);}
    
    public String getIndex05(){return index05.get();}
    public void setIndex05(String index05){this.index05.set(index05);}
    
    public String getIndex06(){return index06.get();}
    public void setIndex06(String index06){this.index06.set(index06);}

    public String getIndex07(){return index07.get();}
    public void setIndex07(String index07){this.index07.set(index07);}
    
    public String getIndex08(){return index08.get();}
    public void setIndex08(String index08){this.index08.set(index08);}
    
    public String getIndex09(){return index09.get();}
    public void setIndex09(String index09){this.index09.set(index09);}
   
    public String getIndex10(){return index10.get();}
    public void setIndex10(String index10){this.index10.set(index10);}
    
}
