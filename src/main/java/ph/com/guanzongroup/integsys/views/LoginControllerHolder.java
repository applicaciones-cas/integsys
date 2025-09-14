package ph.com.guanzongroup.integsys.views;

public class LoginControllerHolder {
    private static DashboardController mainController;
    private static Boolean lbstatus = false;

    public static void setMainController(DashboardController controller) {
        mainController = controller;
    }

    public static DashboardController getMainController() {
        return mainController;
    }

    public static void setLogInStatus(Boolean lbvalue) {
        lbstatus = lbvalue;
    }

    public static Boolean getLogInStatus() {
        return lbstatus;
    }
}
