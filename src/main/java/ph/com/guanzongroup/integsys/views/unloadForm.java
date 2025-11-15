package ph.com.guanzongroup.integsys.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.guanzon.appdriver.base.GRiderCAS;

public class unloadForm {

    //test
    public void unloadForm(AnchorPane AnchorMain, GRiderCAS oApp, String sTabTitle) {
        // Get the parent of the AnchorMain node (assumes it's the content of a Tab)
        Node tabContent = AnchorMain.getParent();
        Parent tabContentParent = tabContent.getParent();

        // Ensure the parent is a TabPane
        if (tabContentParent instanceof TabPane) {
            TabPane tabpane = (TabPane) tabContentParent;

            // Get the currently selected tab
            Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();

            if (selectedTab != null) {
                // Remove the selected tab
                tabpane.getTabs().remove(selectedTab);

                // If it was the last tab, show default screen
                if (tabpane.getTabs().isEmpty()) {
                    Parent tabPaneParent = tabpane.getParent();
                    if (tabPaneParent instanceof StackPane) {
                        StackPane myBox = (StackPane) tabPaneParent;
                        myBox.getChildren().clear();
                        myBox.getChildren().add(getScene("/com/rmj/guanzongroup/sidebarmenus/views/DefaultScreen.fxml", oApp));
                    }
                }

                // Optional: if you have a tabName tracking list
                // tabName.remove(selectedTab.getText());
            }
        }
    }
    //test

    public AnchorPane getScene(String fsFormName, GRiderCAS oApp) {
        ScreenInterface fxObj = new DefaultScreenController();
        fxObj.setGRider(oApp);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxObj.getClass().getResource(fsFormName));
        fxmlLoader.setController(fxObj);

        AnchorPane root;
        try {
            root = (AnchorPane) fxmlLoader.load();
            FadeTransition ft = new FadeTransition(Duration.millis(1500));
            ft.setNode(root);
            ft.setFromValue(1);
            ft.setToValue(1);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            return root;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }
    private Object parentController;

    public String SetTabTitle(String menuaction) {
        switch (menuaction) {
            /*DIRECTORY*/
            case "/org/guanzon/cas/views/ClientMasterParameter.fxml":
                return "Client Parameter";
            case "/org/guanzon/cas/views/ClientMasterTransactionCompany.fxml":
                return "Client Transactions Company";
            case "/org/guanzon/cas/views/ClientMasterTransactionIndividual.fxml":
                return "Client Transactions Individual";
            case "/org/guanzon/cas/views/NewCustomer.fxml":
                return "Client Transactions Standard";
            case "/org/guanzon/cas/views/FrmAccountsPayable.fxml":
                return "Accounts Payable Clients";
            case "/org/guanzon/cas/views/FrmAccountsAccreditation.fxml":
                return "Accounts Accreditation";
            case "/org/guanzon/cas/views/InventoryParam.fxml":
                return "Inventory Parameter";
            case "/org/guanzon/cas/views/InventoryDetail.fxml":
                return "Inventory Details";
            case "/org/guanzon/cas/views/InventorySerialParam.fxml":
                return "Inventory Serial Parameter";
            case "/org/guanzon/cas/views/PO_Quotation_Request.fxml":
                return "Purchase Quotation Request";

            default:
                return null;
        }
    }

    public void useParentController(String lsValue) {
//        if (parentController instanceof ClientMasterParameterController) {
//            ((ClientMasterParameterController) parentController).loadReturn(lsValue);
//        }
//        else if (parentController instanceof ClientMasterTransactionCompanyController) {
//            ((ClientMasterTransactionCompanyController) parentController).loadReturn(lsValue);
////        }else if (parentController instanceof FrmAccountsPayableController) {
////            ((FrmAccountsPayableController) parentController).loadReturn(lsValue);
////        }else if (parentController instanceof FrmAccountsReceivableController) {
////            ((FrmAccountsReceivableController) parentController).loadReturn(lsValue);
//        }else if (parentController instanceof InventoryDetailController) {
//            ((InventoryDetailController) parentController).loadResult(lsValue,false);
//        }else if (parentController instanceof PO_Quotation_RequestController) {
//            ((PO_Quotation_RequestController) parentController).loadResult(lsValue,false);
//        }
    }

}
