package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.guanzon.appdriver.base.GRiderCAS;

public class DefaultScreenController implements Initializable, ScreenInterface {

    @FXML
    public StackPane PreviewPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AnimationPreviewManager manager = new AnimationPreviewManager();
        manager.setAnimationFilePath(System.getProperty("sys.default.path.config") + "/Images/Animation/anim_config.txt");
        manager.createAnimationPane(550, 550);
        manager.setContainer(PreviewPane);
        manager.attachToContainer();
        String urlString = getClass().getResource("/ph/com/guanzongroup/integsys/images/logo.png").toString();
        String cleaned = urlString.replaceFirst("^file:/+", "");
        manager.setDisplayAlternative(cleaned);
        manager.loadAnimation();
    }

    public StackPane getStackPane() {
        return PreviewPane;
    }

    //@Override
    public void setGRider(GRiderCAS foValue) {
    }

    @Override
    public void setIndustryID(String fsValue) {
    }

    @Override
    public void setCompanyID(String fsValue) {
    }

    @Override
    public void setCategoryID(String fsValue) {
    }

}
