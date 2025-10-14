///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
// */
//package com.rmj.guanzongroup.sidebarmenus.controller;
//
//import com.rmj.guanzongroup.sidebarmenus.table.model.ModelAddress;
//import com.rmj.guanzongroup.sidebarmenus.table.model.ModelEmail;
//import com.rmj.guanzongroup.sidebarmenus.table.model.ModelInstitutionalContactPerson;
//import com.rmj.guanzongroup.sidebarmenus.table.model.ModelMobile;
//import com.rmj.guanzongroup.sidebarmenus.table.model.ModelSocialMedia;
//import com.sun.javafx.scene.control.skin.TableHeaderRow;
//import java.net.URL;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//import java.util.ResourceBundle;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.beans.property.ReadOnlyBooleanPropertyBase;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.CheckBox;
//import javafx.scene.control.DatePicker;
//import javafx.scene.control.Label;
//import javafx.scene.control.Tab;
//import javafx.scene.control.TabPane;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TextField;
//import javafx.scene.control.cell.PropertyValueFactory;
//import static javafx.scene.input.KeyCode.DOWN;
//import static javafx.scene.input.KeyCode.ENTER;
//import static javafx.scene.input.KeyCode.F3;
//import static javafx.scene.input.KeyCode.UP;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.text.Text;
//import javafx.util.StringConverter;
//import org.guanzon.appdriver.agent.ShowMessageFX;
//import org.guanzon.appdriver.base.CommonUtils;
//import org.guanzon.appdriver.base.GRider;
//import org.guanzon.appdriver.base.GRiderCAS;
//import org.guanzon.appdriver.base.GuanzonException;
//import org.guanzon.appdriver.base.LogWrapper;
//import org.guanzon.appdriver.constant.EditMode;
//import org.guanzon.cas.client.Client;
//import org.guanzon.cas.client.Client_Master;
//import org.guanzon.cas.parameter.Barangay;
//import org.guanzon.cas.parameter.TownCity;
//import org.guanzon.cas.parameter.services.ParamControllers;
//
//import org.json.simple.JSONObject;
//
///**
// * FXML Controller class
// *
// * @author User
// */
//public class ClientMasterParameterController implements Initializable, ScreenInterface {
//
//    private final String pxeModuleName = "Client";
//    private GRiderCAS oApp;
//    private Client oTrans;
//    private ParamControllers oParam;
//    private Client poTrans;
//    private int pnEditMode;
//
//    private String oTransnox = "";
//    String TownID = "";
//    private boolean state = false;
//    private boolean pbLoaded = false;
//
//    @FXML
//    private AnchorPane AnchorMain, anchorPersonal, anchorAddress, anchorMobile, anchorEmail,
//            anchorSocial, anchorContctPerson, anchorOtherInfo;
//
//    @FXML
//    private HBox hbButtons;
//
//    @FXML
//    private TabPane tabpane01;
//
//    @FXML
//    private Tab tabIndex01, tabIndex02, tabIndex03, tabIndex04, tabIndex05, tabIndex06, tabIndex07;
//
//    @FXML
//    private TableView tblAddress, tblMobile, tblEmail, tblSocMed, tblContact;
//
//    @FXML
//    private Label lblAddressStat, lblMobileStat, lblEmailStat, lblSocMedStat, lblContactPersonStat;
//
//    @FXML
//    private Text lblAddressType;
//
//    @FXML
//    private DatePicker txtField07, personalinfo07;
//
//    @FXML
//    private ComboBox cmbField01, txtField12, txtField13, personalinfo09, personalinfo10,
//            cmbMobile02, cmbMobile01, cmbEmail01, cmbSocMed01, cmbSearch;
//
//    @FXML
//    private TextField txtField01, txtField02, txtField03, txtField04, txtField05, txtField06,
//            txtField08, txtField09, txtField10, txtField11, personalinfo01, personalinfo02,
//            personalinfo03, personalinfo04, personalinfo05, personalinfo06, personalinfo08,
//            personalinfo11, personalinfo12, AddressField01, AddressField02, AddressField03,
//            AddressField04, AddressField05, AddressField06, txtMobile01, mailFields01,
//            txtSocial01, txtContact01, txtContact02, txtContact03, txtContact04, txtContact05,
//            txtContact06, txtContact07, txtContact08, txtContact09, personalinfo13,
//            personalinfo14, personalinfo15, txtSeeks99;
//
//    @FXML
//    private TextArea txtSocial02, txtContact10;
//
//    @FXML
//    private Button btnBrowse, btnSave, btnUpdate, btnSearch, btnCancel, btnClose,
//            btnAddAddress, btnDelAddress, btnAddMobile, btnDelMobile, btnAddEmail, btnDelEmail,
//            btnAddSocMed, btnDelSocMed, btnAddInsContact, btnDelContPerson;
//
//    @FXML
//    private CheckBox cbAddress01, cbAddress02, cbAddress03, cbAddress04, cbAddress05, cbAddress06,
//            cbAddress07, cbAddress08, cbMobileNo01, cbMobileNo02, cbEmail01, cbEmail02,
//            cbSocMed01, cbContact01, cbContact02;
//
//    @FXML
//    private TableColumn indexAddress01, indexAddress02, indexAddress03, indexAddress04, indexAddress05,
//            indexMobileNo01, indexMobileNo02, indexMobileNo03, indexMobileNo04,
//            indexEmail01, indexEmail02, indexEmail03, indexSocMed01, indexSocMed02,
//            indexSocMed03, indexSocMed04, indexContact01, indexContact02, indexContact03,
//            indexContact04, indexContact05, indexContact06, indexContact07;
//
//    private ObservableList<ModelMobile> data = FXCollections.observableArrayList();
//    private ObservableList<ModelEmail> email_data = FXCollections.observableArrayList();
//    private ObservableList<ModelSocialMedia> social_data = FXCollections.observableArrayList();
//    private ObservableList<ModelAddress> address_data = FXCollections.observableArrayList();
//    private ObservableList<ModelInstitutionalContactPerson> contact_data = FXCollections.observableArrayList();
//
//    ObservableList<String> mobileType = FXCollections.observableArrayList("Mobile No", "Tel No", "Fax No");
//    ObservableList<String> mobileOwn = FXCollections.observableArrayList("Personal", "Office", "Others");
//    ObservableList<String> EmailOwn = FXCollections.observableArrayList("Personal", "Office", "Others");
//    ObservableList<String> socialTyp = FXCollections.observableArrayList("Facebook", "Instagram", "Twitter");
//
//    // Create a list of genders
//    ObservableList<String> genders = FXCollections.observableArrayList(
//            "Male",
//            "Female",
//            "Other"
//    );
//    // Create a list of civilStatuses
//    ObservableList<String> civilStatuses = FXCollections.observableArrayList(
//            "Single",
//            "Married",
//            "Divorced",
//            "Widowed"
//    );
//
//    // Create a list of clientType
//    ObservableList<String> clientType = FXCollections.observableArrayList(
//            "Company",
//            "Individual"
//    );
//
//    private int pnMobile = 0;
//    private int pnEmail = 0;
//    private int pnSocMed = 0;
//    private int pnAddress = 0;
//    private int pnContact = 0;
//
//    /**
//     * Initializes the controller class.
//     */
//    @Override
//    public void setGRider(GRiderCAS foValue) {
//        oApp = foValue;
//    }
//
//    @Override
//    public void setIndustryID(String fsValue) {
//    }
//
//    @Override
//    public void setCompanyID(String fsValue) {
//    }
//
//    @Override
//    public void setCategoryID(String fsValue) {
//    }
//
//    public void setTransaction(String fsValue) {
//        oTransnox = fsValue;
//    }
//
//    public void setState(boolean fsValue) {
//        state = fsValue;
//    }
//
//    @Override
//    public void initialize(URL url, ResourceBundle rb) {
//        // TODO
//        pnEditMode = EditMode.UNKNOWN;
//        initButton(pnEditMode);
//        initializeObject();
//        initComboBoxes();
//        initCheckBox();
//        ClickButton();
//        initClientType();
//        initTables();
//        initTabAnchor();
//        pbLoaded = true;
//
//    }
//
//    private void initializeObject() {
//        LogWrapper logwrapr = new LogWrapper("CAS", System.getProperty("sys.default.path.temp") + "cas-error.log");
//        oTrans = new Client(oApp, "", logwrapr);
//        oParam = new ParamControllers(oApp, logwrapr);
//        oTrans.Master().setClientType("0");
//        poTrans = new Client(oApp, "", logwrapr);
//        poTrans.Master().setRecordStatus("0123");
//        oTrans.Master().setRecordStatus("0123");
//    }
//
//    private void initButton(int fnValue) {
//        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
//        btnCancel.setVisible(true);
//        btnSearch.setVisible(lbShow);
//        btnSave.setVisible(lbShow);
//
//        btnSave.setManaged(lbShow);
//        btnCancel.setManaged(true);
//        btnSearch.setManaged(lbShow);
//        btnUpdate.setVisible(!lbShow);
//        btnBrowse.setVisible(!lbShow);
//        cmbField01.setDisable(!lbShow);
//
//        txtSeeks99.setDisable(!lbShow);
//        cmbSearch.setDisable(!lbShow);
//
//        if (lbShow) {
//            txtSeeks99.setDisable(lbShow);
//            txtSeeks99.clear();
//            cmbSearch.setDisable(lbShow);
//
//            btnCancel.setVisible(true);
//            btnSearch.setVisible(lbShow);
//            btnSave.setVisible(true);
//            btnUpdate.setVisible(!lbShow);
//            btnBrowse.setVisible(!lbShow);
//            btnBrowse.setManaged(false);
//            btnUpdate.setManaged(false);
//            btnClose.setManaged(false);
//        } else {
//            txtSeeks99.setDisable(lbShow);
//            txtSeeks99.requestFocus();
//            cmbSearch.setDisable(lbShow);
//        }
//        initClientType();
//    }
//
//    void loadReturn(String lsValue) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//
//    private void ClickButton() {
//        btnBrowse.setOnAction(this::handleButtonAction);
//        btnCancel.setOnAction(this::handleButtonAction);
//        btnSave.setOnAction(this::handleButtonAction);
//        btnUpdate.setOnAction(this::handleButtonAction);
//        btnAddMobile.setOnAction(this::handleButtonAction);
//        btnAddSocMed.setOnAction(this::handleButtonAction);
//        btnAddAddress.setOnAction(this::handleButtonAction);
//        btnAddEmail.setOnAction(this::handleButtonAction);
//        btnAddInsContact.setOnAction(this::handleButtonAction);
//        btnClose.setOnAction(this::handleButtonAction);
//
//        btnDelAddress.setOnAction(this::handleButtonAction);
//        btnDelMobile.setOnAction(this::handleButtonAction);
//        btnDelEmail.setOnAction(this::handleButtonAction);
//        btnDelSocMed.setOnAction(this::handleButtonAction);
//        btnDelContPerson.setOnAction(this::handleButtonAction);
//    }
//
//    private void handleButtonAction(ActionEvent event) {
//        Object source = event.getSource();
//
//        if (source instanceof Button) {
//            try {
//                Button clickedButton = (Button) source;
//                unloadForm appUnload = new unloadForm();
//                JSONObject poJSON;
//                poJSON = new JSONObject();
//                switch (clickedButton.getId()) {
//                    case "btnClose":
//                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
//                            appUnload.unloadForm(AnchorMain, oApp, pxeModuleName);
//                        }
//                        break;
//                    case "btnBrowse":
//                        oTrans.Master().setClientType(String.valueOf(cmbSearch.getSelectionModel().getSelectedIndex()));
//                        System.out.println("CLIENT TP  = " + String.valueOf(cmbSearch.getSelectionModel().getSelectedIndex()));
//                        String lsValue = (txtSeeks99.getText() == null) ? "" : txtSeeks99.getText();
//                        poJSON = oTrans.Master().searchRecordWithClientType(lsValue, false);
//                        if ("error".equals((String) poJSON.get("result"))) {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            txtSeeks99.clear();
//                            break;
//                        }
//                        pnEditMode = EditMode.READY;
//                        LoadRecord();
//                        break;
//                    case "btnCancel":
//                        if (ShowMessageFX.YesNo("Do you really want to cancel this record? \nAny data collected will not be kept.", "Computerized Acounting System", pxeModuleName)) {
//                            clearAllFields();
//                            initializeObject();
//                            pnEditMode = EditMode.UNKNOWN;
//                            initButton(pnEditMode);
//                            initTabAnchor();
//                        }
//                        break;
//                    case "btnSave":
////                        if(!personalinfo01.getText().toString().isEmpty()){
////                            oTrans.getModel().setFullName(personalinfo01.getText());
////                        }
//                        poJSON = oTrans.Save();
//                        if ("success".equals((String) poJSON.get("result"))) {
//                            System.err.println((String) poJSON.get("message"));
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            clearAllFields();
//                            pnEditMode = EditMode.READY;
//                            initButton(pnEditMode);
//                            initTabAnchor();
//                            System.out.println("Record saved successfully.");
//                        } else {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            System.out.println("Record not saved successfully.");
//                            System.out.println((String) poJSON.get("message"));
//                        }
//                        break;
//                    case "btnUpdate":
//                        poJSON = oTrans.Update();
//                        if ("success".equals((String) poJSON.get("result"))) {
//
//                            pnEditMode = EditMode.UPDATE;
//                            initButton(pnEditMode);
//                            initTabAnchor();
//                            cmbField01.setDisable(false);
//                        } else {
//                            ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                            System.out.println("Record not saved successfully.");
//                            System.out.println((String) poJSON.get("message"));
//                            initTabAnchor();
//                            LoadRecord();
//                        }
//                        break;
//
//                    /*ADD*/
//                    case "btnAddMobile":
//                        if (oTrans.getMobileCount() > 1) {
//                            JSONObject addObj = oTrans.addMobile();
//                            if ("success".equals((String) addObj.get("result"))) {
//                                oTrans.Mobile(pnMobile).getModel().setClientId(oTrans.Master().getModel().getClientId());
//                                pnMobile = oTrans.getMobileCount() - 1;
//                                MobileRecord();
//                                tblMobile.getSelectionModel().select(pnMobile + 1);
//                            }
//                        }
//                        break;
//                    case "btnAddSocMed":
//                        if (oTrans.getSocMedCount() > 1) {
//                            JSONObject addObj = oTrans.addSocialMedia();
//                            if ("success".equals((String) addObj.get("result"))) {
//                                oTrans.SocialMedia(pnSocMed).getModel().setClientId(oTrans.Master().getModel().getClientId());
//                                pnSocMed = oTrans.getSocMedCount() - 1;
//                                SocMedRecord();
//                                tblSocMed.getSelectionModel().select(pnSocMed + 1);
//                            }
//                        }
//                        break;
//                    case "btnAddAddress":
//                        if (oTrans.getAddressCount() > 1) {
//                            JSONObject addObj = oTrans.addAddress();
//                            if ("success".equals((String) addObj.get("result"))) {
//                                oTrans.Address(pnAddress).getModel().setClientId(oTrans.Master().getModel().getClientId());
//                                pnAddress = oTrans.getAddressCount() - 1;
//                                AddressRecord();
//                                tblAddress.getSelectionModel().select(pnAddress + 1);
//                            }
//                        }
//                        break;
//                    case "btnAddEmail":
//                        if (oTrans.getMailCount() > 1) {
//                            JSONObject addObj = oTrans.addMail();
//                            if ("success".equals((String) addObj.get("result"))) {
//                                oTrans.Mail(pnEmail).getModel().setClientId(oTrans.Master().getModel().getClientId());
//                                pnEmail = oTrans.getMailCount() - 1;
//                                MailRecord();
//                                tblEmail.getSelectionModel().select(pnEmail + 1);
//                            }
//                        }
//                        break;
//                    case "btnAddInsContact":
//                        if (oTrans.getInstitutionContactPCount() > 1) {
//                            JSONObject addObj = oTrans.addInsContactPerson();
//                            if ("success".equals((String) addObj.get("result"))) {
//                                oTrans.InstitutionContactPerson(pnContact).getModel().setClientId(oTrans.Master().getModel().getClientId());
//                                pnContact = oTrans.getInstitutionContactPCount() - 1;
//                                InstitutionContactRecord();
//                                tblContact.getSelectionModel().select(pnContact + 1);
//                            }
//                        }
//                        break;
//
//                    /*DELETE*/
//                    case "btnDelMobile":
//                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove these details? ") == true) {
//                            if (oTrans.getMobileCount() == 0) {
//                                ShowMessageFX.Information("No Record to delete", "Computerized Acounting System", pxeModuleName);
//                            } else {
//                                poJSON = oTrans.deleteMobile(pnMobile);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                                    break;
//                                }
//                                clearMobileFields();
//                                if (oTrans.getMobileCount() <= 0) {
//                                    pnMobile = oTrans.getMobileCount();
//                                } else {
//                                    pnMobile = oTrans.getMobileCount() - 1;
//                                }
//                                MobileRecord();
//                                txtMobile01.requestFocus();
//                            }
//                        }
//                        break;
//                    case "btnDelSocMed":
//                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove these details? ") == true) {
//                            if (oTrans.getSocMedCount() == 0) {
//                                ShowMessageFX.Information("No Record to delete", "Computerized Acounting System", pxeModuleName);
//                            } else {
//                                poJSON = oTrans.deleteSocialMedia(pnSocMed);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                                    break;
//                                }
//                                clearSocialFields();
//                                if (oTrans.getSocMedCount() <= 0) {
//                                    pnSocMed = oTrans.getSocMedCount();
//                                } else {
//                                    pnSocMed = oTrans.getSocMedCount() - 1;
//                                }
//                                SocMedRecord();
//                                txtSocial01.requestFocus();
//                            }
//                        }
//                        break;
//                    case "btnDelAddress":
//                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove these details? ") == true) {
//                            if (oTrans.getAddressCount() == 0) {
//                                ShowMessageFX.Information("No Record to delete", "Computerized Acounting System", pxeModuleName);
//                            } else {
//                                poJSON = oTrans.deleteAddress(pnAddress);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                                    break;
//                                }
//                                clearAddressFields();
//                                if (oTrans.getAddressCount() <= 0) {
//                                    pnAddress = oTrans.getAddressCount();
//                                } else {
//                                    pnAddress = oTrans.getAddressCount() - 1;
//                                }
//                                AddressRecord();
//                                AddressField01.requestFocus();
//                            }
//                        }
//                        break;
//                    case "btnDelEmail":
//                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove these details? ") == true) {
//                            if (oTrans.getMailCount() == 0) {
//                                ShowMessageFX.Information("No Record to delete", "Computerized Acounting System", pxeModuleName);
//                            } else {
//                                poJSON = oTrans.deleteEmail(pnEmail);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                                    break;
//                                }
//                                clearEmailFields();
//                                if (oTrans.getMailCount() <= 0) {
//                                    pnEmail = oTrans.getMailCount();
//                                } else {
//                                    pnEmail = oTrans.getMailCount() - 1;
//                                }
//                                MailRecord();
//                                mailFields01.requestFocus();
//                            }
//                        }
//                        break;
//                    case "btnDelInsContact":
//                        if (ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove these details? ") == true) {
//                            if (oTrans.getInstitutionContactPCount() == 0) {
//                                ShowMessageFX.Information("No Record to delete", "Computerized Acounting System", pxeModuleName);
//                            } else {
//                                poJSON = oTrans.deleteInstitutionContact(pnContact);
//                                if ("error".equals((String) poJSON.get("result"))) {
//                                    ShowMessageFX.Information((String) poJSON.get("message"), "Computerized Acounting System", pxeModuleName);
//                                    break;
//                                }
//                                clearInsContctFields();
//                                if (oTrans.getInstitutionContactPCount() <= 0) {
//                                    pnContact = oTrans.getInstitutionContactPCount();
//                                } else {
//                                    pnContact = oTrans.getInstitutionContactPCount() - 1;
//                                }
//                                InstitutionContactRecord();
//                                txtContact01.requestFocus();
//                            }
//                        }
//                        break;
//                }
//            } catch (SQLException | GuanzonException ex) {
//
//                Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    private void initTabAnchor() {
//        boolean pbValue = pnEditMode == EditMode.ADDNEW
//                || pnEditMode == EditMode.UPDATE;
//        anchorPersonal.setDisable(!pbValue);
//        anchorAddress.setDisable(!pbValue);
//        anchorMobile.setDisable(!pbValue);
//        anchorEmail.setDisable(!pbValue);
//        anchorSocial.setDisable(!pbValue);
//        anchorContctPerson.setDisable(!pbValue);
//        anchorOtherInfo.setDisable(!pbValue);
//    }
//
//    private void initTables() {
//        initTableAddress();
//        initTableMobile();
//        initTableMail();
//        initTableSocMed();
//        initTableInstitutionContact();
//        initAddressInfo();
//    }
//
//    private void LoadRecord() {
//        try {
//            String ID = oTrans.Master().getModel().getClientId();
//
//            oTrans.OpenClientAddress(ID);
//            AddressRecord();
//
//            oTrans.OpenClientMobile(ID);
//            MobileRecord();
//
//            oTrans.OpenClientMail(ID);
//            MailRecord();
//
//            oTrans.OpenClientSocialMedia(ID);
//            SocMedRecord();
//
//            oTrans.OpenClientinstitutionContact(ID);
//            InstitutionContactRecord();
//
//            MasterRecord();
//            personalinfo07.setValue(LocalDate.now());
//        } catch (SQLException | GuanzonException ex) {
//            Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void MasterRecord() {
//        if (pnEditMode == EditMode.READY
//                || pnEditMode == EditMode.ADDNEW
//                || pnEditMode == EditMode.UPDATE) {
//            try {
//                personalinfo01.setText((String) oTrans.Master().getModel().getCompanyName());
//                personalinfo02.setText((String) oTrans.Master().getModel().getLastName());
//                personalinfo03.setText((String) oTrans.Master().getModel().getFirstName());
//                personalinfo04.setText((String) oTrans.Master().getModel().getMiddleName());
//                personalinfo05.setText((String) oTrans.Master().getModel().getSuffixName());
//                personalinfo12.setText((String) oTrans.Master().getModel().getMothersMaidenName());
//                personalinfo13.setText((String) oTrans.Master().getModel().getTaxIdNumber());
//                personalinfo14.setText((String) oTrans.Master().getModel().getLTOClientId());
//                personalinfo15.setText((String) oTrans.Master().getModel().getPhNationalId());
//                personalinfo06.setText((String) oTrans.Master().getModel().Citizenship().getNationality());
//                personalinfo08.setText((String) oTrans.Master().getModel().BirthTown().getDescription());
//
//                JSONObject poJSON;
//                poJSON = new JSONObject();
//                poJSON = poTrans.Master().searchRecordSpouse(oTrans.Master().getModel().getSpouseId(), true);
//                if ("success".equals((String) poJSON.get("result"))) {
//                    personalinfo11.setText((poTrans.Master().getModel().getCompanyName() == null) ? "" : poTrans.Master().getModel().getCompanyName());
//                    txtField06.setText((poTrans.Master().getModel().getCompanyName() == null) ? "" : poTrans.Master().getModel().getCompanyName());
//                }
//
//                if (oTrans.Master().getModel().getGender() != null && !oTrans.Master().getModel().getGender().trim().isEmpty()) {
//                    personalinfo09.getSelectionModel().select(Integer.parseInt((String) oTrans.Master().getModel().getGender()));
//                    txtField13.getSelectionModel().select(personalinfo09.getSelectionModel().getSelectedIndex());
//                }
//
//                if (oTrans.Master().getModel().getCivilStatus() != null && !oTrans.Master().getModel().getCivilStatus().trim().isEmpty()) {
//                    personalinfo10.getSelectionModel().select(Integer.parseInt((String) oTrans.Master().getModel().getCivilStatus()));
//                    txtField12.getSelectionModel().select(personalinfo10.getSelectionModel().getSelectedIndex());
//                }
//
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//                // Parse the formatted date string into a LocalDate object
//                if (oTrans.Master().getModel().getBirthDate() != null && !oTrans.Master().getModel().getBirthDate().toString().trim().isEmpty()) {
//                    LocalDate localbdate = LocalDate.parse(oTrans.Master().getModel().getBirthDate().toString(), formatter);
//
//                    // Set the value of the DatePicker to the parsed LocalDate
//                    personalinfo07.setValue(localbdate);
//                    txtField07.setValue(localbdate);
//                }
//
//                txtField01.setText((String) oTrans.Master().getModel().getClientId());
//                txtField02.setText((String) oTrans.Master().getModel().getCompanyName());
//                txtField05.setText((String) oTrans.Master().getModel().getMothersMaidenName());
//                txtField09.setText((String) oTrans.Master().getModel().Citizenship().getNationality());
//                txtField08.setText((String) oTrans.Master().getModel().BirthTown().getDescription());
//
//                if (!oTrans.Master().getModel().getClientType().trim().isEmpty() && oTrans.Master().getModel().getClientType() != null) {
//                    cmbField01.getSelectionModel().select(Integer.parseInt((String) oTrans.Master().getModel().getClientType()));
//                }
//
//                if (!address_data.isEmpty()) {
//                    for (int lnCtr = 0; lnCtr < oTrans.getAddressCount(); lnCtr++) {
//                        if (oTrans.Address(lnCtr).getModel().isPrimaryAddress()) {
//                            String lsAddress = oTrans.Address(lnCtr).getModel().getHouseNo() + " " + oTrans.Address(lnCtr).getModel().getAddress()
//                                    + " " + (String) oTrans.Address(lnCtr).getModel().Barangay().getBarangayName()
//                                    + ", " + (String) oTrans.Address(lnCtr).getModel().Town().getDescription() + ", " + (String) oTrans.Address(lnCtr).getModel().Town().getZipCode();
//                            txtField03.setText(lsAddress);
//
//                        }
//                    }
//                }
//
//                if (!data.isEmpty()) {
//                    for (int lnCtr = 0; lnCtr < oTrans.getMobileCount(); lnCtr++) {
//                        if (oTrans.Mobile(lnCtr).getModel().isPrimaryMobile()) {
//                            txtField10.setText((String) oTrans.Mobile(lnCtr).getModel().getMobileNo());
//                        }
//                    }
//                }
//
//                if (!email_data.isEmpty()) {
//                    for (int lnctr = 0; lnctr < oTrans.getMailCount(); lnctr++) {
//                        if (oTrans.Mail(lnctr).getModel().isPrimaryEmail()) {
//                            txtField11.setText((String) oTrans.Mail(lnctr).getModel().getMailAddress());
//                        }
//                    }
//                }
//                if (!contact_data.isEmpty()) {
//                    for (int lnctr = 0; lnctr < oTrans.getInstitutionContactPCount(); lnctr++) {
//                        if (oTrans.InstitutionContactPerson(lnctr).getModel().isPrimaryContactPersion()) {
//                            txtField04.setText((String) oTrans.InstitutionContactPerson(lnctr).getModel().getContactPersonName());
//                        }
//                    }
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (GuanzonException ex) {
//                Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    private void initComboBoxes() {
//        // Set the items of the ComboBox to the list of genders
//        personalinfo09.setItems(genders);
//        personalinfo09.getSelectionModel().select(0);
//
//        personalinfo09.setOnAction(event -> {
//            oTrans.Master().getModel().setGender(String.valueOf(personalinfo09.getSelectionModel().getSelectedIndex()));
//        });
//
//        // Set the items of the ComboBox to the list of genders
//        personalinfo10.setItems(civilStatuses);
//        personalinfo10.getSelectionModel().select(0);
//
//        personalinfo10.setOnAction(event -> {
//            oTrans.Master().getModel().setCivilStatus(String.valueOf(personalinfo10.getSelectionModel().getSelectedIndex()));
//        });
//
//        txtField12.setItems(civilStatuses);
//        txtField13.setItems(genders);
//
//        txtField12.getSelectionModel().select(0);
//        txtField13.getSelectionModel().select(0);
//
//        cmbSearch.setItems(clientType);
//        cmbSearch.getSelectionModel().select(0);
//        cmbField01.setItems(clientType);
//        cmbField01.getSelectionModel().select(0);
//
//        cmbField01.setOnAction(event -> {
//            oTrans.Master().getModel().setClientType(String.valueOf(cmbField01.getSelectionModel().getSelectedIndex()));
//            initClientType();
//        });
//
//        cmbMobile01.setItems(mobileOwn);
//        cmbMobile01.getSelectionModel().select(0);
//        cmbMobile01.setOnAction(event -> {
//            oTrans.Mobile(pnMobile).getModel().setOwnershipType(String.valueOf(cmbMobile01.getSelectionModel().getSelectedIndex()));
//        });
//
//        cmbMobile02.setItems(mobileType);
//        cmbMobile02.getSelectionModel().select(0);
//        cmbMobile02.setOnAction(event -> {
//            oTrans.Mobile(pnMobile).getModel().setMobileType(String.valueOf(cmbMobile02.getSelectionModel().getSelectedIndex()));
//        });
//
//        cmbEmail01.setItems(EmailOwn);
//        cmbEmail01.getSelectionModel().select(0);
//        cmbEmail01.setOnAction(event -> {
//            oTrans.Mail(pnEmail).getModel().setOwnershipType(String.valueOf(cmbEmail01.getSelectionModel().getSelectedIndex()));
//        });
//
//        cmbSocMed01.setItems(socialTyp);
//        cmbSocMed01.getSelectionModel().select(0);
//        cmbSocMed01.setOnAction(event -> {
//            oTrans.SocialMedia(pnSocMed).getModel().setSocMedType(String.valueOf(cmbSocMed01.getSelectionModel().getSelectedIndex()));
//        });
//    }
//
//    private void initClientType() {
//        if (cmbField01.getSelectionModel().getSelectedIndex() == 0) {
//            tabIndex03.setDisable(true);
//            tabIndex04.setDisable(true);
//            tabIndex05.setDisable(true);
//            tabIndex06.setDisable(false);
//        } else {
//            tabIndex06.setDisable(true);
//            tabIndex03.setDisable(false);
//            tabIndex04.setDisable(false);
//            tabIndex05.setDisable(false);
//        }
//        Integer lsValue = cmbField01.getSelectionModel().getSelectedIndex();
//        disablefields(lsValue);
//    }
//
//    private void disablefields(int fsValue) {
//        boolean lbShow = (fsValue == 0);
//
//        // Arrays of TextFields grouped by sections
//        TextField[][] allFields = {
//            // Text fields related to specific sections
//            {personalinfo02, personalinfo03, personalinfo04, personalinfo05, personalinfo06, personalinfo08,
//                personalinfo11, personalinfo12, AddressField05, AddressField06, AddressField05},};
//        personalinfo09.setDisable(lbShow);
//        personalinfo10.setDisable(lbShow);
//
//        cbAddress03.setVisible(lbShow);
//        cbAddress04.setVisible(lbShow);
//        cbAddress05.setVisible(lbShow);
//        cbAddress06.setVisible(lbShow);
//        cbAddress07.setVisible(lbShow);
//        cbAddress08.setVisible(lbShow);
//        lblAddressType.setVisible(lbShow);
//        // Loop through each array of TextFields and clear them
//        for (TextField[] fields : allFields) {
//            for (TextField field : fields) {
//                field.setDisable(lbShow);
//            }
//        }
//        personalinfo01.setDisable(!lbShow);
//    }
//
//    private void initAddressInfo() {
//        /*Address FOCUSED PROPERTY*/
//        AddressField01.focusedProperty().addListener(address_Focus);
//        AddressField02.focusedProperty().addListener(address_Focus);
//
//        AddressField03.setOnKeyPressed(this::addressinfo_KeyPressed);
//        AddressField04.setOnKeyPressed(this::addressinfo_KeyPressed);
//    }
//    final ChangeListener<? super Boolean> address_Focus = (o, ov, nv) -> {
//        if (!pbLoaded) {
//            return;
//        }
//        JSONObject loJSON = new JSONObject();
//        TextField AddressField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
//        int lnIndex = Integer.parseInt(AddressField.getId().substring(12, 14));
//        String lsValue = AddressField.getText();
//        if (lsValue == null) {
//            return;
//        }
//        if (!nv) {
//            /*Lost Focus*/
//            switch (lnIndex) {
//                case 1:
//                    /*houseno*/
//                    loJSON = oTrans.Address(pnAddress).getModel().setHouseNo(lsValue);
//                    break;
//                case 2:/*address*/
//                    loJSON = oTrans.Address(pnAddress).getModel().setAddress(lsValue);
//                    break;
//
//            }
//            AddressRecord();
//        } else {
//            AddressField.selectAll();
//        }
//    };
//
//    private void addressinfo_KeyPressed(KeyEvent event) {
//        try {
//            TextField AddressField = (TextField) event.getSource();
//            int lnIndex = Integer.parseInt(((TextField) event.getSource()).getId().substring(12, 14));
//            String lsValue = AddressField.getText();
//            JSONObject poJson;
//
//            switch (event.getCode()) {
//                case F3:
//                    switch (lnIndex) {
//                        case 3:
//                            /*search town*/
//                            poJson = new JSONObject();
//                            poJson = oParam.TownCity().searchRecord(lsValue, false);
//                            System.out.println("poJson = " + poJson.toJSONString());
//                            if ("success".equals((String) poJson.get("result"))) {
//                                AddressField03.setText(oParam.TownCity().getModel().getDescription());
//                                JSONObject loJSON = new JSONObject();
//                                loJSON = oTrans.Address(pnAddress).getModel().setTownId(oParam.TownCity().getModel().getTownId());
//                                System.out.println("TOWN == " + oParam.TownCity().getModel().getTownId());
//                                TownID = oParam.TownCity().getModel().getTownId();
//                            }
//                            break;
//                        case 4:
//                            /*search barangay*/
//                            poJson = new JSONObject();
//                            poJson = oParam.Barangay().searchRecordbyTown(lsValue, TownID, false);
//                            if ("success".equals((String) poJson.get("result"))) {
//                                AddressField04.setText(oParam.Barangay().getModel().getBarangayName());
//                                JSONObject loJSON = new JSONObject();
//                                loJSON = oTrans.Address(pnAddress).getModel().setBarangayId(oParam.Barangay().getModel().getBarangayId());
//                            }
//                            break;
//                    }
//                case ENTER:
//            }
//            AddressRecord();
//
//            switch (event.getCode()) {
//                case ENTER:
//                    CommonUtils.SetNextFocus(AddressField);
//                case DOWN:
//                    CommonUtils.SetNextFocus(AddressField);
//                    break;
//                case UP:
//                    CommonUtils.SetPreviousFocus(AddressField);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (GuanzonException ex) {
//            Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void AddressRecord() {
//        int lnCtr;
//        int lnCtr2 = 0;
//        address_data.clear();
//
//        if (oTrans.getAddressCount() >= 0) {
//            for (lnCtr = 0; lnCtr < oTrans.getAddressCount(); lnCtr++) {
//
//                try {
//                    address_data.add(new ModelAddress(String.valueOf(lnCtr + 1),
//                            (String) oTrans.Address(lnCtr2).getModel().getHouseNo(),
//                            (String) oTrans.Address(lnCtr2).getModel().getAddress(),
//                            (String) oTrans.Address(lnCtr2).getModel().Town().getDescription(),
//                            (String) oTrans.Address(lnCtr2).getModel().Barangay().getBarangayName()
//                    ));
//                    lnCtr2 += 1;
//                } catch (SQLException | GuanzonException ex) {
//                    Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//        if (pnAddress < 0 || pnAddress
//                >= address_data.size()) {
//            if (!address_data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblAddress.getSelectionModel().select(0);
//                tblAddress.getFocusModel().focus(0);
//                pnAddress = tblAddress.getSelectionModel().getSelectedIndex();
//                getSelectedAddress();
//
//            }
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblAddress.getSelectionModel().select(pnAddress);
//            tblAddress.getFocusModel().focus(pnAddress);
//            getSelectedAddress();
//        }
//
//    }
//
//    public void initTableAddress() {
//        indexAddress01.setStyle("-fx-alignment: CENTER;");
//        indexAddress02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexAddress03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexAddress04.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexAddress05.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//
//        indexAddress01.setCellValueFactory(new PropertyValueFactory<>("index01"));
//        indexAddress02.setCellValueFactory(new PropertyValueFactory<>("index02"));
//        indexAddress03.setCellValueFactory(new PropertyValueFactory<>("index03"));
//        indexAddress04.setCellValueFactory(new PropertyValueFactory<>("index04"));
//        indexAddress05.setCellValueFactory(new PropertyValueFactory<>("index05"));
//
//        tblAddress.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            TableHeaderRow header = (TableHeaderRow) tblAddress.lookup("TableHeaderRow");
//            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                header.setReordering(false);
//            });
//        });
//        tblAddress.setItems(address_data);
//        tblAddress.getSelectionModel().select(pnAddress + 1);
//        tblAddress.autosize();
//
//    }
//
//    @FXML
//    private void tblAddress_Clicked(MouseEvent event) {
//        pnAddress = tblAddress.getSelectionModel().getSelectedIndex();
//        if (pnAddress >= 0) {
//            tblAddress.getSelectionModel().clearAndSelect(pnAddress);
//            getSelectedAddress();
//        }
//    }
//
//    private void getSelectedAddress() {
//        if (oTrans.getAddressCount() > 0) {
//            try {
//                boolean isActive = !"0".equals(oTrans.Address(pnAddress).getModel().getRecordStatus());
//                AddressField01.setText(oTrans.Address(pnAddress).getModel().getHouseNo());
//                AddressField02.setText(oTrans.Address(pnAddress).getModel().getAddress());
//                AddressField03.setText(oTrans.Address(pnAddress).getModel().Town().getDescription());
//                AddressField04.setText(oTrans.Address(pnAddress).getModel().Barangay().getBarangayName());
//                AddressField05.setText(String.valueOf(oTrans.Address(pnAddress).getModel().getLatitude()));
//                AddressField06.setText(String.valueOf(oTrans.Address(pnAddress).getModel().getLongitude()));
//                cbAddress01.setSelected(isActive);
//                cbAddress02.setSelected(oTrans.Address(pnAddress).getModel().isPrimaryAddress());
//                cbAddress03.setSelected(oTrans.Address(pnAddress).getModel().isOfficeAddress());
//                cbAddress04.setSelected(oTrans.Address(pnAddress).getModel().isBillingAddress());
//                cbAddress05.setSelected(oTrans.Address(pnAddress).getModel().isShippingAddress());
//                cbAddress06.setSelected(oTrans.Address(pnAddress).getModel().isProvinceAddress());
//                cbAddress07.setSelected(oTrans.Address(pnAddress).getModel().isCurrentAddress());
//                cbAddress08.setSelected(oTrans.Address(pnAddress).getModel().isLTMSAddress());
//                lblAddressStat.setText(isActive ? "ACTIVE" : "INACTIVE");
//            } catch (SQLException ex) {
//                Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (GuanzonException ex) {
//                Logger.getLogger(ClientMasterParameterController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    private void MobileRecord() {
//        int lnCtr2 = 0;
//        data.clear();
//
//        if (oTrans.getMobileCount() >= 0) {
//            for (int lnCtr = 0; lnCtr < oTrans.getMobileCount(); lnCtr++) {
//                data.add(new ModelMobile(String.valueOf(lnCtr + 1),
//                        oTrans.Mobile(lnCtr2).getModel().getMobileNo(),
//                        oTrans.Mobile(lnCtr2).getModel().getOwnershipType(),
//                        oTrans.Mobile(lnCtr2).getModel().getMobileType()
//                ));
//                lnCtr2 += 1;
//            }
//
//        }
//
//        if (pnMobile < 0 || pnMobile
//                >= data.size()) {
//            if (!data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblMobile.getSelectionModel().select(0);
//                tblMobile.getFocusModel().focus(0);
//                pnMobile = 0;
//            }
//            getSelectedMobile();
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblMobile.getSelectionModel().select(pnMobile);
//            tblMobile.getFocusModel().focus(pnMobile);
//            getSelectedMobile();
//        }
//    }
//
//    public void initTableMobile() {
//        indexMobileNo01.setStyle("-fx-alignment: CENTER;");
//        indexMobileNo02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexMobileNo03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexMobileNo04.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//
//        indexMobileNo01.setCellValueFactory(new PropertyValueFactory<>("index01"));
//        indexMobileNo02.setCellValueFactory(new PropertyValueFactory<>("index02"));
//        indexMobileNo03.setCellValueFactory(new PropertyValueFactory<>("index03"));
//        indexMobileNo04.setCellValueFactory(new PropertyValueFactory<>("index04"));
//
//        tblMobile.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            TableHeaderRow header = (TableHeaderRow) tblMobile.lookup("TableHeaderRow");
//            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                header.setReordering(false);
//            });
//        });
//        tblMobile.setItems(data);
//        tblMobile.getSelectionModel().select(pnMobile + 1);
//        tblMobile.autosize();
//    }
////
//
//    @FXML
//    private void tblMobile_Clicked(MouseEvent event) {
//        pnMobile = tblMobile.getSelectionModel().getSelectedIndex();
//        if (pnMobile >= 0) {
//            tblMobile.getSelectionModel().clearAndSelect(pnMobile);
//            getSelectedMobile();
//
//        }
//    }
//
//    private void getSelectedMobile() {
//        if (oTrans.getMobileCount() > 0) {
//            boolean isActive = !"0".equals(oTrans.Mobile(pnMobile).getModel().getRecordStatus());
//            tblMobile.getSelectionModel().clearAndSelect(pnMobile);
//            txtMobile01.setText(oTrans.Mobile(pnMobile).getModel().getMobileNo());
//
//            cbMobileNo02.setSelected(isActive);
//            cbMobileNo01.setSelected(oTrans.Mobile(pnMobile).getModel().isPrimaryMobile());
//
//            if (oTrans.Mobile(pnMobile).getModel().getOwnershipType() != null && !oTrans.Mobile(pnMobile).getModel().getOwnershipType().toString().trim().isEmpty()) {
//                cmbMobile01.getSelectionModel().select(Integer.parseInt(oTrans.Mobile(pnMobile).getModel().getOwnershipType().toString()));
//            }
//            lblMobileStat.setText(isActive ? "ACTIVE" : "INACTIVE");
//        }
//    }
//
//    private void MailRecord() {
//        int lnCtr2 = 0;
//        email_data.clear();
//        if (oTrans.getMailCount() >= 0) {
//            for (int lnCtr = 0; lnCtr < oTrans.getMailCount(); lnCtr++) {
//                email_data.add(new ModelEmail(String.valueOf(lnCtr + 1),
//                        oTrans.Mail(lnCtr2).getModel().getOwnershipType(),
//                        oTrans.Mail(lnCtr2).getModel().getMailAddress()
//                ));
//                lnCtr2 += 1;
//            }
//        }
//        if (pnEmail < 0 || pnEmail
//                >= email_data.size()) {
//            if (!email_data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblEmail.getSelectionModel().select(0);
//                tblEmail.getFocusModel().focus(0);
//                pnEmail = tblEmail.getSelectionModel().getSelectedIndex();
//            }
//            getSelectedMail();
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblEmail.getSelectionModel().select(pnEmail);
//            tblEmail.getFocusModel().focus(pnEmail);
//            getSelectedMail();
//        }
//    }
//
//    public void initTableMail() {
//        indexEmail01.setStyle("-fx-alignment: CENTER;");
//        indexEmail02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexEmail03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//
//        indexEmail01.setCellValueFactory(new PropertyValueFactory<>("index01"));
//        indexEmail02.setCellValueFactory(new PropertyValueFactory<>("index02"));
//        indexEmail03.setCellValueFactory(new PropertyValueFactory<>("index03"));
//
//        tblEmail.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            TableHeaderRow header = (TableHeaderRow) tblEmail.lookup("TableHeaderRow");
//            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                header.setReordering(false);
//            });
//        });
//        tblEmail.setItems(email_data);
//        tblEmail.getSelectionModel().select(pnEmail + 1);
//        tblEmail.autosize();
//    }
//
//    @FXML
//    private void tblEmail_Clicked(MouseEvent event) {
//        pnEmail = tblEmail.getSelectionModel().getSelectedIndex();
//        if (pnEmail >= 0) {
//            tblEmail.getSelectionModel().clearAndSelect(pnEmail);
//            getSelectedMail();
//        }
//    }
//
//    private void getSelectedMail() {
//        if (oTrans.getMailCount() > 0) {
//            boolean isActive = !"0".equals(oTrans.Mail(pnEmail).getModel().getRecordStatus());
//            tblEmail.getSelectionModel().clearAndSelect(pnEmail);
//            mailFields01.setText(oTrans.Mail(pnEmail).getModel().getMailAddress());
//
//            cbEmail02.setSelected(isActive);
//            cbEmail01.setSelected(oTrans.Mail(pnEmail).getModel().isPrimaryEmail());
//
//            if (oTrans.Mail(pnEmail).getModel().getOwnershipType() != null && !oTrans.Mail(pnMobile).getModel().getOwnershipType().toString().trim().isEmpty()) {
//                cmbEmail01.getSelectionModel().select(Integer.parseInt(oTrans.Mail(pnMobile).getModel().getOwnershipType().toString()));
//            }
//            lblEmailStat.setText(isActive ? "ACTIVE" : "INACTIVE");
//        }
//    }
//
//    private void SocMedRecord() {
//        int lnCtr2 = 0;
//        social_data.clear();
//        if (oTrans.getSocMedCount() >= 0) {
//            for (int lnCtr = 0; lnCtr < oTrans.getSocMedCount(); lnCtr++) {
//                social_data.add(new ModelSocialMedia(String.valueOf(lnCtr + 1),
//                        oTrans.SocialMedia(lnCtr2).getModel().getAccount(),
//                        oTrans.SocialMedia(lnCtr2).getModel().getSocMedType(),
//                        oTrans.SocialMedia(lnCtr2).getModel().getRemarks()
//                ));
//                lnCtr2 += 1;
//            }
//        }
//
//        if (pnSocMed < 0 || pnSocMed
//                >= social_data.size()) {
//            if (!social_data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblSocMed.getSelectionModel().select(0);
//                tblSocMed.getFocusModel().focus(0);
//                pnSocMed = tblSocMed.getSelectionModel().getSelectedIndex();
//            }
//            getSelectedSocMed();
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblSocMed.getSelectionModel().select(pnSocMed);
//            tblSocMed.getFocusModel().focus(pnSocMed);
//            getSelectedSocMed();
//
//        }
//    }
//
//    public void initTableSocMed() {
//        indexSocMed01.setStyle("-fx-alignment: CENTER;");
//        indexSocMed02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexSocMed03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//
//        indexSocMed01.setCellValueFactory(new PropertyValueFactory<>("index01"));
//        indexSocMed02.setCellValueFactory(new PropertyValueFactory<>("index02"));
//        indexSocMed03.setCellValueFactory(new PropertyValueFactory<>("index03"));
//
//        tblSocMed.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            TableHeaderRow header = (TableHeaderRow) tblSocMed.lookup("TableHeaderRow");
//            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                header.setReordering(false);
//            });
//        });
//        tblSocMed.setItems(social_data);
//        tblSocMed.getSelectionModel().select(pnSocMed + 1);
//        tblSocMed.autosize();
//    }
//
//    @FXML
//    private void tblSocMed_Clicked(MouseEvent event) {
//        pnSocMed = tblSocMed.getSelectionModel().getSelectedIndex();
//        if (pnSocMed >= 0) {
//            tblSocMed.getSelectionModel().clearAndSelect(pnSocMed);
//            getSelectedSocMed();
//        }
//    }
//
//    private void getSelectedSocMed() {
//        if (oTrans.getSocMedCount() > 0) {
//            boolean isActive = !"0".equals(oTrans.SocialMedia(pnSocMed).getModel().getRecordStatus());
//            tblSocMed.getSelectionModel().clearAndSelect(pnSocMed);
//            txtSocial01.setText(oTrans.SocialMedia(pnSocMed).getModel().getAccount());
//            txtSocial02.setText(oTrans.SocialMedia(pnSocMed).getModel().getRemarks());
//            cbSocMed01.setSelected(isActive);
////            cbSocMed01.setSelected(oTrans.ListMail(pnEmail).isPrimaryEmail());
//
//            if (oTrans.SocialMedia(pnSocMed).getModel().getSocMedType() != null && !oTrans.SocialMedia(pnSocMed).getModel().getSocMedType().toString().trim().isEmpty()) {
//                cmbEmail01.getSelectionModel().select(Integer.parseInt(oTrans.SocialMedia(pnSocMed).getModel().getSocMedType().toString()));
//            }
//            lblSocMedStat.setText(isActive ? "ACTIVE" : "INACTIVE");
//        }
//    }
//
//    private void InstitutionContactRecord() {
//        int lnCtr2 = 0;
//        contact_data.clear();
//        if (oTrans.getInstitutionContactPCount() >= 0) {
//            for (int lnCtr = 0; lnCtr < oTrans.getInstitutionContactPCount(); lnCtr++) {
//                contact_data.add(new ModelInstitutionalContactPerson(
//                        String.valueOf(lnCtr + 1),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getContactPersonName(),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getContactPersonPosition(),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getMobileNo(),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getLandlineNo(),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getFaxNo(),
//                        oTrans.InstitutionContactPerson(lnCtr).getModel().getRemarks()));
//                lnCtr2 += 1;
//
//            }
//        } else {
//            ShowMessageFX.Information("No Record Found!", "Computerized Acounting System", pxeModuleName);
//        }
//
//        if (pnContact < 0 || pnContact
//                >= contact_data.size()) {
//            if (!contact_data.isEmpty()) {
//                /* FOCUS ON FIRST ROW */
//                tblContact.getSelectionModel().select(0);
//                tblContact.getFocusModel().focus(0);
//                pnContact = tblContact.getSelectionModel().getSelectedIndex();
//            }
//            getSelectedContact();
//        } else {
//            /* FOCUS ON THE ROW THAT pnRowDetail POINTS TO */
//            tblContact.getSelectionModel().select(pnContact);
//            tblContact.getFocusModel().focus(pnContact);
//            getSelectedContact();
//
//        }
//    }
//
//    public void initTableInstitutionContact() {
//        indexContact01.setStyle("-fx-alignment: CENTER;");
//        indexContact02.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexContact03.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexContact04.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexContact05.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexContact06.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//        indexContact07.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 0 0 5;");
//
//        indexContact01.setCellValueFactory(new PropertyValueFactory<>("index01"));
//        indexContact02.setCellValueFactory(new PropertyValueFactory<>("index02"));
//        indexContact03.setCellValueFactory(new PropertyValueFactory<>("index03"));
//        indexContact04.setCellValueFactory(new PropertyValueFactory<>("index04"));
//        indexContact05.setCellValueFactory(new PropertyValueFactory<>("index05"));
//        indexContact06.setCellValueFactory(new PropertyValueFactory<>("index06"));
//        indexContact07.setCellValueFactory(new PropertyValueFactory<>("index07"));
//
//        tblContact.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
//            TableHeaderRow header = (TableHeaderRow) tblContact.lookup("TableHeaderRow");
//            header.reorderingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                header.setReordering(false);
//            });
//        });
//        tblContact.setItems(contact_data);
//        tblContact.getSelectionModel().select(pnContact + 1);
//        tblContact.autosize();
//    }
//
//    @FXML
//    private void tblContact_Clicked(MouseEvent event) {
//        pnContact = tblContact.getSelectionModel().getSelectedIndex();
//        if (pnContact >= 0) {
//            tblContact.getSelectionModel().clearAndSelect(pnContact);
//            getSelectedContact();
//        }
//    }
//
//    private void getSelectedContact() {
//        if (oTrans.getSocMedCount() > 0) {
//            boolean isActive = !"0".equals(oTrans.InstitutionContactPerson(pnContact).getModel().getRecordStatus());
//            txtContact01.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getContactPersonName());
//            txtContact02.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getContactPersonPosition());
//            txtContact03.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getSocMedAccount1());
//            txtContact04.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getSocMedAccount2());
//            txtContact05.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getSocMedAccount3());
//            txtContact09.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getMailAddress());
//
//            txtContact06.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getMobileNo());
//            txtContact07.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getLandlineNo());
//            txtContact08.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getFaxNo());
//            txtContact10.setText(oTrans.InstitutionContactPerson(pnContact).getModel().getRemarks());
//
//            cbContact01.setSelected(isActive);
//            cbContact02.setSelected(oTrans.InstitutionContactPerson(pnContact).getModel().isPrimaryContactPersion());
//            lblContactPersonStat.setText(isActive ? "ACTIVE" : "INACTIVE");
//        }
//    }
//
//    private void clearAllFields() {
//        // Arrays of TextFields grouped by sections
//        TextField[][] allFields = {
//            // Text fields related to specific sections
//            {txtSeeks99, txtField01, txtField02, txtField03, txtField04,
//                txtField05, txtField06, txtField10, txtField08, txtField11, txtField09},
//            {personalinfo02, personalinfo03, personalinfo04, personalinfo05,
//                personalinfo12, personalinfo13, personalinfo14, personalinfo15,
//                personalinfo06, personalinfo08, personalinfo11, personalinfo01},};
//        txtField07.setValue(null);
//        personalinfo07.setValue(null);
//        personalinfo09.getSelectionModel().select(0);
//        personalinfo10.getSelectionModel().select(0);
//        txtField12.getSelectionModel().select(0);
//        txtField13.getSelectionModel().select(0);
//        cmbSearch.getSelectionModel().select(0);
//        cmbField01.getSelectionModel().select(0);
//        cmbField01.setDisable(true);
//
//        // Loop through each array of TextFields and clear them
//        for (TextField[] fields : allFields) {
//            for (TextField field : fields) {
//                field.clear();
//            }
//        }
//
//        pnAddress = 0;
//        pnMobile = 0;
//        pnEmail = 0;
//        pnSocMed = 0;
//        pnContact = 0;
//        clearAddressFields();
//        clearMobileFields();
//        clearEmailFields();
////        clearSocialFields();
////        clearInsContctFields();
//        data.clear();
//        email_data.clear();
//        social_data.clear();
//        address_data.clear();
//        contact_data.clear();
//
//    }
//
//    private void clearAddressFields() {
//        TextField[] fields = {AddressField01, AddressField02, AddressField03, AddressField04,
//            AddressField05, AddressField06};
//
//        // Loop through each array of TextFields and clear them
//        for (TextField field : fields) {
//            field.clear();
//        }
//
//        CheckBox[] checkboxs = {cbAddress01, cbAddress02, cbAddress03,
//            cbAddress04, cbAddress05, cbAddress06, cbAddress07, cbAddress08};
//
//        // Loop through each array of TextFields and clear them
//        for (CheckBox checkbox : checkboxs) {
//            checkbox.setSelected(false);
//        }
//
//    }
//
//    private void clearMobileFields() {
//        cmbMobile01.getSelectionModel().select(0);
//        cmbMobile02.getSelectionModel().select(0);
//        txtMobile01.clear();
//        cbMobileNo01.setSelected(false);
//        cbMobileNo02.setSelected(false);
//    }
//
//    private void clearEmailFields() {
//        cmbEmail01.getSelectionModel().select(0);
//        mailFields01.clear();
//        cbEmail01.setSelected(false);
//        cbEmail02.setSelected(false);
//    }
//
//    private void clearSocialFields() {
//        txtSocial01.clear();
//        txtSocial02.clear();
//        cmbSocMed01.getSelectionModel().select(0);
//        cbSocMed01.setSelected(false);
//
//    }
//
//    private void clearInsContctFields() {
//        TextField[] fields = {txtSocial01, txtContact01, txtContact02,
//            txtContact03, txtContact04, txtContact05, txtContact06, txtContact07,
//            txtContact08, txtContact09
//        };
//        // Loop through each array of TextFields and clear them
//        for (TextField field : fields) {
//            field.clear();
//        }
//        txtContact10.clear();
//
//        cbContact01.setSelected(false);
//        cbContact02.setSelected(false);
//    }
//
//    public void initCheckBox() {
//        /*Checkbox Addresss Tab*/
//        cbAddress01.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().setRecordStatus(cbAddress01.isSelected() ? "1" : "0");
//        });
//
//        cbAddress02.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isPrimaryAddress(cbAddress02.isSelected());
//
//        });
//
//        cbAddress03.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isOfficeAddress(cbAddress03.isSelected());
//
//        });
//
//        cbAddress04.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isBillingAddress(cbAddress04.isSelected());
//
//        });
//
//        cbAddress05.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isShippingAddress(cbAddress05.isSelected());
//
//        });
//
//        cbAddress06.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isProvinceAddress(cbAddress06.isSelected());
//
//        });
//
//        cbAddress07.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isCurrentAddress(cbAddress07.isSelected());
//
//        });
//
//        cbAddress08.setOnAction(event -> {
//            oTrans.Address(pnAddress).getModel().isLTMSAddress(cbAddress08.isSelected());
//
//        });
//        /*Checkbox Mobile Tab*/
//        cbMobileNo01.setOnAction(event -> {
//            oTrans.Mobile(pnMobile).getModel().setRecordStatus(cbMobileNo01.isSelected() ? "1" : "0");
//        });
//
//        cbMobileNo02.setOnAction(event -> {
//            oTrans.Mobile(pnMobile).getModel().isPrimaryMobile(cbMobileNo02.isSelected());
//
//        });
//        /*Checkbox Email Tab*/
//        cbEmail01.setOnAction(event -> {
//            oTrans.Mail(pnEmail).getModel().setRecordStatus(cbEmail01.isSelected() ? "1" : "0");
//        });
//
//        cbEmail02.setOnAction(event -> {
//            oTrans.Mail(pnEmail).getModel().isPrimaryEmail(cbEmail02.isSelected());
//
//        });
//        /*Checkbox Social Media Tab*/
//        cbSocMed01.setOnAction(event -> {
//            oTrans.SocialMedia(pnSocMed).getModel().setRecordStatus(cbSocMed01.isSelected() ? "1" : "0");
//        });
//
//        /*Checkbox Contact Person Tab*/
//        cbContact01.setOnAction(event -> {
//            oTrans.InstitutionContactPerson(pnContact).getModel().setRecordStatus(cbContact01.isSelected() ? "1" : "0");
//        });
//
//        cbContact02.setOnAction(event -> {
//            oTrans.InstitutionContactPerson(pnContact).getModel().isPrimaryContactPersion(cbContact02.isSelected());
//
//        });
//    }
//
//}
