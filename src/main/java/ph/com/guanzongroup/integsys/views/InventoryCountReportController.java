package ph.com.guanzongroup.integsys.views;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javax.swing.JButton;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import org.guanzon.appdriver.agent.ShowMessageFX;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.LogWrapper;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.cas.inv.warehouse.status.InventoryCountPrint;
import org.json.simple.JSONObject;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import org.guanzon.cas.parameter.InventoryCountType;
import org.guanzon.cas.parameter.services.ParamControllers;

public class InventoryCountReportController implements Initializable, ScreenInterface {

    private GRiderCAS poApp;
    private LogWrapper poLogWrapper;
    private String psFormName = "Inventory Count Report";
    private String psIndustryID, psCompanyID, psCategoryID;
    private JasperPrint poJasperPrint;
    private JRViewer poJRViewer;

    // Stored IDs from search
    private String psBranchID = "";
    private String psCategorySearchID = "";
    private String psInvCountTypeID = "";

    @FXML
    private AnchorPane AnchorMain, apBrowse, apButton, apReport;

    @FXML
    private Label lblSource;

    @FXML
    private TextField tfSearchBranch, tfSearchCategory, tfSearchInvCountType;

    @FXML
    private DatePicker dpDateFrom, dpDateTo;

    @FXML
    private RadioButton rbSummary;

    @FXML
    private Button btnPrint, btnRetrieve, btnExport, btnExportPDF, btnClose;

    // ─── ScreenInterface ──────────────────────────────────────────────────────
    @Override
    public void setGRider(GRiderCAS foValue) {
        poApp = foValue;
    }

    @Override
    public void setIndustryID(String fsValue) {
        psIndustryID = fsValue;
    }

    @Override
    public void setCompanyID(String fsValue) {
        psCompanyID = fsValue;
    }

    @Override
    public void setCategoryID(String fsValue) {
        psCategoryID = fsValue;
    }

    // ─── Initialize ───────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            poLogWrapper = new LogWrapper(psFormName, psFormName);

            Platform.runLater(() -> {
                try {
                    dpDateFrom.setValue(LocalDate.now().withDayOfMonth(1));
                    dpDateTo.setValue(LocalDate.now());
                    lblSource.setText("");
                    rbSummary.setSelected(true);

                    initKeyEvents();
                    initLostFocusEvents();
                    initButtonDisplay(false);

                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
                }
            });

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);
        }
    }

    // ─── Lost Focus Events — clear ID when field is manually cleared ──────────
    private void initLostFocusEvents() {
        tfSearchBranch.focusedProperty().addListener(txtField_Focus);
        tfSearchCategory.focusedProperty().addListener(txtField_Focus);
        tfSearchInvCountType.focusedProperty().addListener(txtField_Focus);
    }

    private final ChangeListener<? super Boolean> txtField_Focus = (o, ov, nv) -> {
        TextField loTextField = (TextField) ((ReadOnlyBooleanPropertyBase) o).getBean();
        String lsID = loTextField.getId();
        String lsValue = loTextField.getText();

        if (!nv) {
            // Lost focus
            if (lsValue == null || lsValue.trim().isEmpty()) {
                switch (lsID) {
                    case "tfSearchBranch":
                        if (tfSearchBranch.getText().isEmpty()) {
                            psBranchID = "";
                            tfSearchBranch.clear();
                        }
                        break;
                    case "tfSearchCategory":
                        if (tfSearchCategory.getText().isEmpty()) {
                            psCategorySearchID = "";
                            tfSearchCategory.clear();
                        }
                        break;
                    case "tfSearchInvCountType":
                        if (tfSearchInvCountType.getText().isEmpty()) {
                            psInvCountTypeID = "";
                            tfSearchInvCountType.clear();
                        }
                        break;
                }
            }
        } else {
            // Gained focus
            loTextField.selectAll();
        }
    };

    // ─── Key Events ───────────────────────────────────────────────────────────
    private void initKeyEvents() {
        tfSearchBranch.setOnKeyPressed(this::handleSearchKeyPress);
        tfSearchCategory.setOnKeyPressed(this::handleSearchKeyPress);
        tfSearchInvCountType.setOnKeyPressed(this::handleSearchKeyPress);
    }

    private void handleSearchKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.F3 || event.getCode() == KeyCode.ENTER) {
            try {
                TextField source = (TextField) event.getSource();
                switch (source.getId()) {
                    case "tfSearchBranch":
                        searchBranch();
                        break;
                    case "tfSearchCategory":
                        searchCategory();
                        break;
                    case "tfSearchInvCountType":
                        searchInventoryCountType();
                        break;
                }
            } catch (GuanzonException | SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                ShowMessageFX.Error(MiscUtil.getException(ex), psFormName, null);
            }
        }
    }

    private void searchBranch() throws GuanzonException, SQLException {
        InventoryCountType loObject = new ParamControllers(poApp, poLogWrapper).InventoryCountType();
        JSONObject loJSON = loObject.searchRecord(tfSearchBranch.getText(), false);
        if (isJSONSuccess(loJSON, psFormName)) {
            tfSearchBranch.setText(loObject.getModel().getDescription());
            psBranchID = loObject.getModel().getInventoryCountID();
        } else {
            tfSearchBranch.clear();
            psBranchID = "";
        }
    }

    private void searchCategory() throws GuanzonException, SQLException {
        InventoryCountType loObject = new ParamControllers(poApp, poLogWrapper).InventoryCountType();
        JSONObject loJSON = loObject.searchRecord(tfSearchCategory.getText(), false);
        if (isJSONSuccess(loJSON, psFormName)) {
            tfSearchCategory.setText(loObject.getModel().getDescription());
            psCategorySearchID = loObject.getModel().getInventoryCountID();
        } else {
            tfSearchCategory.clear();
            psCategorySearchID = "";
        }
    }

    private void searchInventoryCountType() throws GuanzonException, SQLException {
        InventoryCountType loObject = new ParamControllers(poApp, poLogWrapper).InventoryCountType();
        JSONObject loJSON = loObject.searchRecord(tfSearchInvCountType.getText(), false);
        if (isJSONSuccess(loJSON, psFormName)) {
            tfSearchInvCountType.setText(loObject.getModel().getDescription());
            psInvCountTypeID = loObject.getModel().getInventoryCountID();
        } else {
            tfSearchInvCountType.clear();
            psInvCountTypeID = "";
        }
    }

    // ─── Button Handler ───────────────────────────────────────────────────────
    @FXML
    private void cmdButton_Click(ActionEvent event) {
        String btnID = ((Button) event.getSource()).getId();

        switch (btnID) {
            case "btnRetrieve":
                generateAndShowReport();
                break;

            case "btnPrint":
                printReport();
                break;

            case "btnExport":
                exportExcel();
                break;

            case "btnExportPDF":
                exportPDF();
                break;

            case "btnClose":
                if (ShowMessageFX.OkayCancel(null, "Close Tab",
                        "Are you sure you want to close this Tab?")) {
                    unloadForm appUnload = new unloadForm();
                    appUnload.unloadForm(AnchorMain, poApp, psFormName);
                }
                break;
        }
    }

    // ─── Button Display ───────────────────────────────────────────────────────
    private void initButtonDisplay(boolean hasReport) {
        // No report: show only Retrieve and Close
        btnRetrieve.setVisible(true);
        btnRetrieve.setManaged(true);
        btnClose.setVisible(true);
        btnClose.setManaged(true);

        // Show all others only when report is loaded
        btnPrint.setVisible(hasReport);
        btnPrint.setManaged(hasReport);
        btnExport.setVisible(hasReport);
        btnExport.setManaged(hasReport);
        btnExportPDF.setVisible(hasReport);
        btnExportPDF.setManaged(hasReport);
    }

    // ─── Generate and Embed Report ────────────────────────────────────────────
    private void generateAndShowReport() {
        try {
            if (dpDateFrom.getValue() == null || dpDateTo.getValue() == null) {
                ShowMessageFX.Warning(null, psFormName,
                        "Please set the date range before retrieving.");
                return;
            }
            if (dpDateFrom.getValue().isAfter(dpDateTo.getValue())) {
                ShowMessageFX.Warning(null, psFormName,
                        "Date From must not be later than Date To.");
                return;
            }

            StackPane overlay = getOverlayProgress(apReport);
            ProgressIndicator pi = (ProgressIndicator) overlay.getChildren().get(0);
            clearReportPane();
            overlay.setVisible(true);
            pi.setVisible(true);
            initButtonDisplay(false);

            Date ldDateFrom = toDate(dpDateFrom.getValue());
            Date ldDateTo = toDate(dpDateTo.getValue());

            String lsSQL = InventoryCountPrint.PrintReportQuery();
            lsSQL = MiscUtil.addCondition(lsSQL,
                    "InventoryCountMaster.dTransact BETWEEN "
                    + SQLUtil.toSQL(ldDateFrom)
                    + " AND "
                    + SQLUtil.toSQL(ldDateTo));

            if (!psBranchID.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InventoryCountMaster.sBranchCd = " + SQLUtil.toSQL(psBranchID));
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InvMaster.sBranchCd = " + SQLUtil.toSQL(psBranchID));
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InvMaster.sIndstCdx = " + SQLUtil.toSQL(poApp.getIndustry()));
            }
            if (!psCategorySearchID.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InventoryCountMaster.sCategrCd = " + SQLUtil.toSQL(psCategorySearchID));
            }
            if (!psInvCountTypeID.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InventoryCountMaster.sInvCtrID = " + SQLUtil.toSQL(psInvCountTypeID));
            }
            if (!psCategoryID.isEmpty()) {
                lsSQL = MiscUtil.addCondition(lsSQL,
                        "InventoryCountMaster.sCategrCd = " + SQLUtil.toSQL(psCategoryID));
            }

            final String lsFinalSQL = lsSQL;

            java.util.Map<String, Object> loParams = new java.util.HashMap<>();
            loParams.put("sReportNm", "Inventory Count Report");
            loParams.put("sBranchNm", poApp.getBranchName());
            loParams.put("sAddressx", poApp.getAddress());
            loParams.put("sCompnyNm", poApp.getClientName());
            loParams.put("DatePrinted", SQLUtil.dateFormat(
                    poApp.getServerDate(), SQLUtil.FORMAT_TIMESTAMP));
            loParams.put("watermarkImagePath",
                    poApp.getReportPath() + "images\\blank.png");
            loParams.put("sPrintdBy",
                    poApp.getClientName() == null ? "" : poApp.getClientName());

            String lsJasperPath = poApp.getReportPath()
                    + InventoryCountPrint.getJasperReport(psIndustryID) +"Report"+ ".jasper";

            Task<JasperPrint> task = new Task<JasperPrint>() {
                @Override
                protected JasperPrint call() throws Exception {
                    ResultSet loRS = poApp.executeQuery(lsFinalSQL);
                    System.out.println("Report SQL: " + lsFinalSQL);

                    if (MiscUtil.RecordCount(loRS) <= 0) {
                        throw new Exception("No records found for the selected criteria.");
                    }
                    loRS.beforeFirst();
                    JRResultSetDataSource loDataSource = new JRResultSetDataSource(loRS);
                    return JasperFillManager.fillReport(lsJasperPath, loParams, loDataSource);
                }

                @Override
                protected void succeeded() {
                    overlay.setVisible(false);
                    pi.setVisible(false);
                    poJasperPrint = getValue();
                    embedReportInPane(poJasperPrint);
                    initButtonDisplay(true);
                }

                @Override
                protected void failed() {
                    overlay.setVisible(false);
                    pi.setVisible(false);
                    Throwable ex = getException();
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    ShowMessageFX.Warning(null, psFormName, ex.getMessage());
                    initButtonDisplay(false);
                }

                @Override
                protected void cancelled() {
                    overlay.setVisible(false);
                    pi.setVisible(false);
                    initButtonDisplay(false);
                }
            };

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    MiscUtil.getException(e), e);
            ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);
        }
    }

    // ─── Embed JRViewer ───────────────────────────────────────────────────────
    private void embedReportInPane(JasperPrint jasperPrint) {
        Platform.runLater(() -> {
            try {
                clearReportPane();

                poJRViewer = new JRViewer(jasperPrint);

                for (int i = 0; i < poJRViewer.getComponentCount(); i++) {
                    if (poJRViewer.getComponent(i) instanceof JRViewerToolbar) {
                        JRViewerToolbar toolbar = (JRViewerToolbar) poJRViewer.getComponent(i);
                        for (int j = 0; j < toolbar.getComponentCount(); j++) {
                            if (toolbar.getComponent(j) instanceof JButton) {
                                JButton btn = (JButton) toolbar.getComponent(j);
                                if (btn.getToolTipText() != null
                                        && (btn.getToolTipText().equals("Save")
                                        || btn.getToolTipText().equals("Print"))) {
                                    btn.setEnabled(false);
                                    btn.setVisible(false);
                                }
                            }
                        }
                        toolbar.revalidate();
                        toolbar.repaint();
                    }
                }

                SwingNode swingNode = new SwingNode();
                swingNode.setContent(poJRViewer);

                AnchorPane.setTopAnchor(swingNode, 0.0);
                AnchorPane.setBottomAnchor(swingNode, 0.0);
                AnchorPane.setLeftAnchor(swingNode, 0.0);
                AnchorPane.setRightAnchor(swingNode, 0.0);

                apReport.getChildren().add(swingNode);

            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                        MiscUtil.getException(e), e);
                ShowMessageFX.Error(MiscUtil.getException(e), psFormName, null);
            }
        });
    }

    // ─── Print ────────────────────────────────────────────────────────────────
    private void printReport() {
        if (poJasperPrint == null) {
            ShowMessageFX.Warning(null, psFormName,
                    "No report loaded. Please retrieve first.");
            return;
        }
        try {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            if (job.printDialog()) {
                JasperPrintManager.printReport(poJasperPrint, false);
            }
        } catch (JRException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    // ─── Export Excel ─────────────────────────────────────────────────────────
    private void exportExcel() {
        if (poJasperPrint == null) {
            ShowMessageFX.Warning(null, psFormName,
                    "No report loaded. Please retrieve first.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("InventoryCountReport.xlsx");

            File file = fileChooser.showSaveDialog(
                    (Stage) btnClose.getScene().getWindow());
            if (file != null) {
                net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter exporter
                        = new net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter();
                exporter.setExporterInput(
                        new net.sf.jasperreports.export.SimpleExporterInput(poJasperPrint));
                exporter.setExporterOutput(
                        new net.sf.jasperreports.export.SimpleOutputStreamExporterOutput(file));
                exporter.exportReport();
                ShowMessageFX.Information(null, psFormName, "Excel exported successfully.");
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    // ─── Export PDF ───────────────────────────────────────────────────────────
    private void exportPDF() {
        if (poJasperPrint == null) {
            ShowMessageFX.Warning(null, psFormName,
                    "No report loaded. Please retrieve first.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("InventoryCountReport.pdf");

            File file = fileChooser.showSaveDialog(
                    (Stage) btnClose.getScene().getWindow());
            if (file != null) {
                JasperExportManager.exportReportToPdfFile(
                        poJasperPrint, file.getAbsolutePath());
                ShowMessageFX.Information(null, psFormName, "PDF exported successfully.");
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            ShowMessageFX.Error(ex.getMessage(), psFormName, null);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private void clearReportPane() {
        apReport.getChildren().removeIf(node -> !(node instanceof StackPane));
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private StackPane getOverlayProgress(AnchorPane foAnchorPane) {
        for (Node node : foAnchorPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof ProgressIndicator) {
                        return stack;
                    }
                }
            }
        }

        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(50, 50);
        pi.setVisible(false);
        pi.setStyle("-fx-progress-color: orange;");

        StackPane overlay = new StackPane(pi);
        overlay.setPickOnBounds(false);

        AnchorPane.setTopAnchor(overlay, 0.0);
        AnchorPane.setBottomAnchor(overlay, 0.0);
        AnchorPane.setLeftAnchor(overlay, 0.0);
        AnchorPane.setRightAnchor(overlay, 0.0);

        foAnchorPane.getChildren().add(overlay);
        return overlay;
    }

    private boolean isJSONSuccess(JSONObject loJSON, String fsModule) {
        String result = (String) loJSON.get("result");
        if ("error".equals(result)) {
            String message = (String) loJSON.get("message");
            if (message != null) {
                if (Platform.isFxApplicationThread()) {
                    ShowMessageFX.Warning(null, fsModule, message);
                } else {
                    Platform.runLater(() -> ShowMessageFX.Warning(null, fsModule, message));
                }
            }
            return false;
        }
        String message = (String) loJSON.get("message");
        if (message != null) {
            if (Platform.isFxApplicationThread()) {
                ShowMessageFX.Information(null, fsModule, message);
            } else {
                Platform.runLater(() -> ShowMessageFX.Information(null, fsModule, message));
            }
        }
        return true;
    }
}
