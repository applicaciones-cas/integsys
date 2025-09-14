package ph.com.guanzongroup.integsys;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.integsys.views.DashboardController;

public class GUI extends Application {

    public final static String pxeMainFormTitle = "Computerized Accounting System";
    public final static String pxeMainForm = "/ph/com/guanzongroup/integsys/views/Dashboard.fxml";
    public static GRiderCAS oApp;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader view = new FXMLLoader();
        view.setLocation(getClass().getResource(pxeMainForm));

        DashboardController controller = new DashboardController();
        controller.setGRider(oApp);

        view.setController(controller);
        Parent parent = view.load();
        Scene scene = new Scene(parent);

        //get the screen size
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
//        stage.getIcons().add(new Image(pxeStageIcon));
        stage.setTitle(pxeMainFormTitle);

        // set stage as maximized but not full screen
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setGRider(GRiderCAS foValue) {
        oApp = foValue;
    }
}
