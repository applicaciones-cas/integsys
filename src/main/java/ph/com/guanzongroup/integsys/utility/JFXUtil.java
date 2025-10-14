package ph.com.guanzongroup.integsys.utility;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollBar;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.poi.ss.formula.functions.T;
import org.json.simple.JSONObject;
import javafx.concurrent.Task;
import javafx.scene.control.OverrunStyle;
import org.guanzon.appdriver.agent.ShowMessageFX;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import ph.com.guanzongroup.integsys.views.ScreenInterface;

/**
 * Date : 4/28/2025
 *
 * @author Aldrich
 */
public class JFXUtil {

    /* To auto resize one column when v scrolllbar appear */
 /* Required to set the min-width property of one column into USE_COMPUTED_SIZE to work*/
    public static void adjustColumnForScrollbar(TableView<?>... tableViews) {
        for (TableView<?> tableView : tableViews) {
            tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (!(newSkin instanceof TableViewSkin<?>)) {
                    return;
                }

                TableViewSkin<?> skin = (TableViewSkin<?>) newSkin;
                VirtualFlow<?> flow = skin.getChildren().stream()
                        .filter(node -> node instanceof VirtualFlow<?>)
                        .map(node -> (VirtualFlow<?>) node)
                        .findFirst().orElse(null);

                if (flow == null) {
                    return;
                }

                ScrollBar vScrollBar = flow.getChildrenUnmodifiable().stream()
                        .filter(node -> node instanceof ScrollBar && ((ScrollBar) node).getOrientation() == Orientation.VERTICAL)
                        .map(node -> (ScrollBar) node)
                        .findFirst().orElse(null);

                if (vScrollBar == null || tableView.getColumns().isEmpty()) {
                    return;
                }

                TableColumn<?, ?> foundColumn = null;
                for (TableColumn<?, ?> column : tableView.getColumns()) {
                    double minWidth = column.getMinWidth();

                    // Safely compare with USE_COMPUTED_SIZE
                    if ((minWidth) == 0) {
                        foundColumn = column;
                        break;
                    }
                }

                if (foundColumn == null) {
                    System.err.println("NO COLUMN WITH minWidth == 0 (USE_COMPUTED_SIZE) found in table: " + tableView.getId());
                    return;
                }

                final TableColumn<?, ?> targetColumn = foundColumn;
                // Optional debug log
                vScrollBar.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(() -> {
                        double scrollBarWidth = newValue ? vScrollBar.getWidth() : 0;
                        double remainingWidth = tableView.getWidth() - scrollBarWidth;

                        double totalFixedWidth = tableView.getColumns().stream()
                                .filter(col -> col != targetColumn)
                                .mapToDouble(TableColumn::getWidth)
                                .sum();

                        double newWidth = Math.max(0, remainingWidth - totalFixedWidth);
                        targetColumn.setPrefWidth(newWidth - 5);
                    });
                });
            });
        }
    }

    /* Dynamic Highlighter for TableView */
 /* Requires TableView, string key (basis value), String Color in hex(e.g. #H1H1H1), Map list for local storage of highlights */
    public static <T> void highlightByKey(TableView<T> table, String key, String color, Map<String, List<String>> highlightMap) {
        List<String> colors = highlightMap.computeIfAbsent(key, k -> new ArrayList<>());
        if (!colors.contains(color)) {
            colors.add(color);
            table.refresh();
        }
    }

    /* To disable particular highlight*/
    public static <T> void disableHighlightByKey(TableView<T> table, String key, Map<String, List<String>> highlightMap) {
        highlightMap.remove(key);
        table.refresh();
    }

    /* To conviniently disable all highlight*/
    public static <T> void disableAllHighlight(TableView<T> table, Map<String, List<String>> highlightMap) {
        highlightMap.clear();
        table.refresh();
    }

    /* To disable highlight by defining color in hex*/
    public static <T> void disableAllHighlightByColor(TableView<T> table, String color, Map<String, List<String>> highlightMap) {
        highlightMap.forEach((key, colors) -> colors.removeIf(c -> c.equals(color)));
        highlightMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        table.refresh();
    }

    /* To make highlighting effective, apply in initialization, called once*/
    public static <T> void applyRowHighlighting(
            final TableView<T> tableView,
            final Function<T, String> keyExtractor,
            final Map<String, List<String>> highlightMap) {

        tableView.setRowFactory(new javafx.util.Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(final TableView<T> tv) {
                return new TableRow<T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setStyle(""); // Reset style
                        } else {
                            String key = keyExtractor.apply(item);
                            if (highlightMap.containsKey(key)) {
                                List<String> colors = highlightMap.get(key);
                                if (!colors.isEmpty()) {
                                    setStyle("-fx-background-color: " + colors.get(colors.size() - 1) + ";");
                                }
                            } else {
                                setStyle(""); // Default
                            }
                        }
                    }
                };
            }
        });
    }

    /* To retain non temporary highlights and remove temporary highlights specifically used for ENTRY form*/
    public static void showRetainedHighlight(boolean isRetained, TableView<?> tblView, String color, List<Pair<String, String>> plPartial, List<Pair<String, String>> plFinal,
            Map<String, List<String>> highlightedRows, boolean resetpartial) {
        if (isRetained) {
            for (Pair<String, String> pair : plPartial) {
                if (!"0".equals(pair.getValue())) {
                    plFinal.add(new Pair<>(pair.getKey(), pair.getValue()));
                }
            }
        }
        if (resetpartial) {
            disableAllHighlightByColor(tblView, color, highlightedRows);
            plPartial.clear();
        }
        for (Pair<String, String> pair : plFinal) {
            if (!"0".equals(pair.getValue())) {
                highlightByKey(tblView, pair.getKey(), color, highlightedRows);
            }
        }
    }

    /* Experimental function unused*/
    public static void setDatePickerNextFocusByEnter(DatePicker... datePickers) {
        for (DatePicker datePicker : datePickers) {
            datePicker.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    Node source = (Node) event.getSource();
                    source.fireEvent(new KeyEvent(
                            KeyEvent.KEY_PRESSED,
                            "",
                            "",
                            KeyCode.TAB,
                            false,
                            false,
                            false,
                            false
                    ));
                    event.consume();
                }
            });
        }
    }

    /* SUGGESTED To modify combobox selection lists color*/
    public static void initComboBoxCellDesignColor(String hexColor, ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            initComboBoxCellDesignColor(comboBox, hexColor);
        }
    }

    /* To modify combobox lists color, includes hover and selected*/
    public static <T> void initComboBoxCellDesignColor(ComboBox<T> comboBox, String hexcolor) {
//      #FF8201
        comboBox.setCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());

                    boolean isSelected = item.equals(comboBox.getValue());

                    // Apply initial style
                    if (isSelected) {
                        setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }

                    hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                        if (isNowHovered && !isEmpty() && getItem() != null) {
                            if (getItem().toString().equals(comboBox.getValue() != null ? comboBox.getValue().toString() : "")) {
                                setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: white;");

                            } else {
                                setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: black;");

                            }
                        } else if (!isEmpty() && getItem() != null) {
                            // If not hovered, reset style based on selection
                            if (getItem().toString().equals(comboBox.getValue() != null ? comboBox.getValue().toString() : "")) {
                                setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: white;");
                            } else {
                                setStyle("");
                            }
                        }
                    });

                    setOnMouseExited(e -> {
                        if (item.equals(comboBox.getValue())) {
                            setStyle("-fx-background-color: " + hexcolor + "; -fx-text-fill: white;");
                        } else {
                            setStyle("");
                        }
                    });
                }
            }
        });
        comboBox.setOnShowing(event -> {
            T selectedItem = comboBox.getValue();
            if (selectedItem != null) {
                // Loop through each item and apply style based on selection
                for (int i = 0; i < comboBox.getItems().size(); i++) {
                    T item = comboBox.getItems().get(i);

                    if (item.equals(selectedItem)) {
                        // Apply the custom background color for selected item in the list
                        comboBox.getItems().set(i, item);
                    } else {
                        // Reset the style for non-selected items
                        comboBox.getItems().set(i, item);
                    }
                }
            }
        });
    }

    /* To format datepicker to multiple nodes; old usage*/
    public static void setDatePickerFormat(DatePicker... datePickers) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (DatePicker datePicker : datePickers) {
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(formatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
                }
            });
        }
    }

    /* To format a datepicker; new usage*/
    public static void setDatePickerFormat(String pattern, DatePicker... datePickers) {
//        "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        for (DatePicker datePicker : datePickers) {
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    return (date != null) ? date.format(formatter) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
                }
            });
        }
    }

    /* To put caret position of a textfield to last character index*/
 /* Requires AnchorPane parent ID of textfields*/
    public static void updateCaretPositions(AnchorPane anchorPane) {
        List<TextField> textFields = getAllTextFields(anchorPane);
        for (TextField textField : textFields) {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                Pos alignment = textField.getAlignment();
                if (alignment == Pos.CENTER_RIGHT || alignment == Pos.BASELINE_RIGHT
                        || alignment == Pos.TOP_RIGHT || alignment == Pos.BOTTOM_RIGHT) {
                    textField.positionCaret(0); // Caret at start
                } else {
                    if (textField.isFocused()) {
                        textField.positionCaret(text.length()); // Caret at end if focused
                    } else {
                        textField.positionCaret(0); // Caret at start if not focused
                    }
                }
            }
        }
    }

    /* JFXUtil private usage */
    private static List<TextField> getAllTextFields(Parent parent) {
        List<TextField> textFields = new ArrayList<>();

        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                textFields.add((TextField) node);
            } else if (node instanceof DatePicker) {
                Node datePickerEditor = ((DatePicker) node).lookup(".text-field");
                if (datePickerEditor instanceof TextField) {
                    textFields.add((TextField) datePickerEditor);
                }
            } else if (node instanceof Parent) {
                textFields.addAll(getAllTextFields((Parent) node));
            }
        }
        return textFields;
    }

    /*Move the current row of TableView to next row and returns next index value*/
 /*Requires TableView*/
    public static int moveToNextRow(TableView table) {
        TableView<?> currentTable = table;
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell != null) {
            int nextRow = (focusedCell.getRow() + 1) % table.getItems().size();
            table.getSelectionModel().select(nextRow);
            return nextRow;
        } else {
            return 0;
        }
    }

    /*Move the current row of TableView to previous row and returns next index value*/
 /*Requires TableView*/
    public static int moveToPreviousRow(TableView table) {
        TableView<?> currentTable = table;
        TablePosition<?, ?> focusedCell = currentTable.getFocusModel().getFocusedCell();
        if (focusedCell != null) {
            int previousRow = (focusedCell.getRow() - 1 + table.getItems().size()) % table.getItems().size();
            table.getSelectionModel().select(previousRow);
            return previousRow;
        } else {
            return 0;
        }

    }

    /* use when pagination is present in tableView */
 /*Updates the pagination count dynamically*/
    public static void loadTab(Pagination pgPagination, int tbldata_list_size, int ROWS_PER_PAGE, TableView tbl, FilteredList filteredData) {
        int totalPage = (int) (Math.ceil(tbldata_list_size * 1.0 / ROWS_PER_PAGE));
        pgPagination.setPageCount(totalPage);
        pgPagination.setCurrentPageIndex(0);
        changeTableView(0, ROWS_PER_PAGE, tbl, tbldata_list_size, filteredData);
        pgPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            changeTableView(newValue.intValue(), ROWS_PER_PAGE, tbl, tbldata_list_size, filteredData);
            tbl.scrollTo(0);
        });
    }

    /* To calculate number of page of the TableView based on table data size*/
    public static void changeTableView(int index, int limit, TableView tbl, int tbldata_list_size, FilteredList filteredData) {
        tbl.getSelectionModel().clearSelection();
        int fromIndex = index * limit;
        int toIndex = Math.min(fromIndex + limit, tbldata_list_size);
        int minIndex = Math.min(toIndex, tbldata_list_size);
        try {
            SortedList<T> sortedData = new SortedList<>(
                    FXCollections.observableArrayList(filteredData.subList(Math.min(fromIndex, minIndex), minIndex)));
            sortedData.comparatorProperty().bind(tbl.comparatorProperty());
        } catch (Exception e) {
        }
        try {
            tbl.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));
        } catch (Exception e) {

        }
    }

    /* used to display pop-up dialog form*/
    public static class StageManager {

        private Stage dialog;
        private EventHandler<WindowEvent> onHiddenHandler; // Store handler
        private final xyOffset xyOffset = new xyOffset();
        Scene scene = null;
        Parent root = null;

        public void showDialog(Stage parentStage, URL fxmlurl,
                Object controller,
                String lsDialogTitle,
                boolean enableWindowDrag,
                boolean enableblock,
                boolean stayOnTop
        ) throws IOException {

            FXMLLoader loader = new FXMLLoader(fxmlurl);
            loader.setController(controller);

            root = loader.load();

            root.setOnMousePressed(event -> {
                xyOffset.x = event.getSceneX();
                xyOffset.y = event.getSceneY();
            });

            if (enableWindowDrag) {
                root.setOnMouseDragged(event -> {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setX(event.getScreenX() - xyOffset.x);
                    stage.setY(event.getScreenY() - xyOffset.y);
                });
            }

            dialog = new Stage();
            dialog.initStyle(StageStyle.UNDECORATED);
            if (enableblock) {
                dialog.initModality(Modality.WINDOW_MODAL);
                if (parentStage != null) {
                    dialog.initOwner(parentStage); // sets the blocking owner
                }
            }

            if (stayOnTop) {
                dialog.setAlwaysOnTop(true);
            }

            scene = new Scene(root);

            dialog.setTitle(lsDialogTitle);
            dialog.setScene(scene);

            // Attach stored onHiddenHandler if available
            if (onHiddenHandler != null) {
                dialog.setOnHidden(onHiddenHandler);
                onHiddenHandler = null; // Clear after assigning
            }

            dialog.show();
            dialog.toFront();
        }

        public void setOnHidden(EventHandler<WindowEvent> handler) {
            onHiddenHandler = handler;
        }

        public Scene getScene() {
            return scene;
        }

        public Parent getRoot() {
            return root;
        }

        public void closeDialog() {
            if (dialog != null) {
                dialog.close();
                dialog = null;
            }
        }
    }

    private static class xyOffset {

        double x, y;
    }

    /* Used for displaying attachment */
 /* Enabling crop in bounds*/
    public static void stackPaneClip(StackPane stackPane1) {
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(
                stackPane1.getWidth() - 8,
                stackPane1.getHeight() - 8
        );
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        clip.setLayoutX(4);
        clip.setLayoutY(4);
        stackPane1.setClip(clip);
    }

    /* Used for displaying attachment */
 /* Detects if image is out of bounds in view*/
    public static boolean isImageViewOutOfBounds(ImageView imageView, StackPane stackPane) {
        Bounds clipBounds = stackPane.getClip().getBoundsInParent();
        Bounds imageBounds = imageView.getBoundsInParent();

        return imageBounds.getMaxX() < clipBounds.getMinX()
                || imageBounds.getMinX() > clipBounds.getMaxX()
                || imageBounds.getMaxY() < clipBounds.getMinY()
                || imageBounds.getMinY() > clipBounds.getMaxY();
    }

    /* Used for displaying attachment */
 /* Revert the position and size of the image to default*/
    public static void resetImageBounds(ImageView imageView, StackPane stackPane1) {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        stackPane1.setAlignment(imageView, javafx.geometry.Pos.CENTER);
    }

    /* Called to display Image by calling its class ImageViewer*/
 /* Allows multiple usage of ImageViewer*/
    public static class ImageViewer {

        public double ldstackPaneWidth = 0;
        public double ldstackPaneHeight = 0;
        public double mouseAnchorX;
        public double mouseAnchorY;
        public double scaleFactor = 1.0;

        public void initAttachmentPreviewPane(StackPane stackPane, ImageView imageView) {
            stackPane.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
                stackPane.setClip(new javafx.scene.shape.Rectangle(
                        newBounds.getMinX(),
                        newBounds.getMinY(),
                        newBounds.getWidth(),
                        newBounds.getHeight()
                ));
            });

            imageView.setOnScroll((ScrollEvent event) -> {
                double delta = event.getDeltaY();
                scaleFactor = Math.max(0.5, Math.min(scaleFactor * (delta > 0 ? 1.1 : 0.9), 5.0));
                imageView.setScaleX(scaleFactor);
                imageView.setScaleY(scaleFactor);
            });

            imageView.setOnMousePressed((MouseEvent event) -> {
                mouseAnchorX = event.getSceneX() - imageView.getTranslateX();
                mouseAnchorY = event.getSceneY() - imageView.getTranslateY();
            });

            imageView.setOnMouseDragged((MouseEvent event) -> {
                double translateX = event.getSceneX() - mouseAnchorX;
                double translateY = event.getSceneY() - mouseAnchorY;
                imageView.setTranslateX(translateX);
                imageView.setTranslateY(translateY);
            });

            stackPane.widthProperty().addListener((observable, oldValue, newWidth) -> {
                ldstackPaneWidth = newWidth.doubleValue();
            });
        }
    }

    /* Used to adjsut image size */
    public static void adjustImageSize(Image image, ImageView imageView, double ldstackPaneWidth, double ldstackPaneHeight) {
        double imageRatio = image.getWidth() / image.getHeight();
        double containerRatio = ldstackPaneWidth / ldstackPaneHeight;

        // Unbind before setting new values
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();

        if (imageRatio > containerRatio) {
            // Image is wider than container → fit width
            imageView.setFitWidth(ldstackPaneWidth);
            imageView.setFitHeight(ldstackPaneWidth / imageRatio);
        } else {
            // Image is taller than container → fit height
            imageView.setFitHeight(ldstackPaneHeight);
            imageView.setFitWidth(ldstackPaneHeight * imageRatio);
        }

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
    }

    /* Used for table column value alignment */
    public static void setColumnCenter(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER;");
        }
    }

    /* Used for table column value alignment */
    public static void setColumnLeft(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER-LEFT;-fx-padding: 0 5 0 5;");
        }
    }

    /* Used for table column value alignment */
    public static void setColumnRight(TableColumn... columns) {
        for (TableColumn column : columns) {
            column.setStyle("-fx-alignment: CENTER-RIGHT;-fx-padding: 0 5 0 5;");
        }
    }

    /* Auto link/set table values & auto disable column re-alignment */
 /*Requires TableView*/
    public static void setColumnsIndexAndDisableReordering(final TableView<?> tableView) {
        int counter = 1;
        for (Object obj : tableView.getColumns()) {
            if (obj instanceof TableColumn) {
                @SuppressWarnings("unchecked")
                final TableColumn<Object, Object> column = (TableColumn<Object, Object>) obj;

                final String indexName = String.format("index%02d", counter++);
                column.setCellValueFactory(new PropertyValueFactory<>(indexName));

                // disable sorting and editing
                column.setSortable(false);
                column.setEditable(false);

                // Directly set cell factory without Label
                column.setCellFactory(col -> {
                    TableCell<Object, Object> cell = new TableCell<Object, Object>() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                String text = item.toString().replaceAll("\\r?\\n", "");
                                setText(text);
                            }
                        }
                    };
                    cell.setWrapText(false);
                    cell.setTextOverrun(OverrunStyle.ELLIPSIS);
                    return cell;
                });
            }
        }

        // disable column reordering
        tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            if (header != null) {
                header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
            }
        });
    }

    /* Clears textFields, textAreas, checkboxes & datepickers by calling its parent anchorpane */
 /* For datepicker it auto set value to null before clearing text input*/
    public static void clearTextFields(AnchorPane... anchorPanes) {
        for (AnchorPane pane : anchorPanes) {
            clearTextInputsRecursive(pane);
        }
    }

    private static void clearTextInputsRecursive(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextInputControl) {
                ((TextInputControl) node).clear();
            } else if (node instanceof DatePicker) {
                DatePicker dp = (DatePicker) node;
                dp.setValue(null);
                if (dp.getEditor() != null) {
                    dp.getEditor().clear();
                }
            } else if (node instanceof CheckBox) {
                ((CheckBox) node).setSelected(false); // uncheck
            } else if (node instanceof Parent) {
                clearTextInputsRecursive((Parent) node);
            }
        }
    }

    /* Used to hide button visibility*/
    public static void setButtonsVisibility(boolean visible, Button... buttons) {
        for (Button btn : buttons) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    public static void AddStyleClass(String lsCssClassName, TextField... textFields) {
        for (TextField tf : textFields) {
            tf.getStyleClass().add(lsCssClassName);
        }
    }

    public static void RemoveStyleClass(String lsCssClassName, TextField... textFields) {
        for (TextField tf : textFields) {
            tf.getStyleClass().remove(lsCssClassName);
        }
    }

    public static boolean isTextFieldContainsStyleClass(String lsCssClassName, TextField... textFields) {
        //used for removal
        for (TextField tf : textFields) {
            if (tf.getStyleClass().contains(lsCssClassName)) {
                return true;
            }
        }
        return false;
    }

    public static void setDisabled(boolean disable, Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(disable);
            if (node instanceof TextField) {
                if (!disable) {
                    while (node.getStyleClass().contains("DisabledTextField")) {
                        node.getStyleClass().remove("DisabledTextField");
                    }
                } else {
                    node.getStyleClass().add("DisabledTextField");
                }
            }
        }
    }

    public static void setFocusListener(ChangeListener<? super Boolean> listener, Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof Control) {
                ((Control) node).focusedProperty().addListener(listener);
            }
        }
    }

    public static String convertToIsoFormat(String dateStr) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter usFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        // Try to parse as ISO format first
        try {
            LocalDate date = LocalDate.parse(dateStr, isoFormatter);
            // If it parses successfully, return as-is
            return dateStr;
        } catch (DateTimeParseException ignore) {
            // Not in ISO format, try MM/dd/yyyy
        }

        // Try to parse as MM/dd/yyyy and convert
        try {
            LocalDate date = LocalDate.parse(dateStr, usFormatter);
            return date.format(isoFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + dateStr);
            return null;
        }
    }

    public static JFXUtilDateResult processDate(String inputText, DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy"); // accepted string
        JSONObject poJSON = new JSONObject();
        LocalDate selectedDate = null;

        if (inputText != null && !inputText.trim().isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(inputText, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                datePicker.setValue(parsedDate);
                datePicker.getEditor().setText(formatter.format(parsedDate));
                inputText = datePicker.getEditor().getText();
            } catch (DateTimeParseException ignored) {
            }
        }

        if (inputText != null && !inputText.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(inputText, formatter);
                datePicker.setValue(selectedDate);
            } catch (Exception ex) {
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid date format. Please use MM/dd/yyyy format.");
                return new JFXUtilDateResult("", selectedDate, poJSON);
            }
        } else {
            selectedDate = datePicker.getValue();
        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return new JFXUtilDateResult(inputText, selectedDate, poJSON);
    }

    public static class JFXUtilDateResult {

        public String inputText;
        public LocalDate selectedDate;
        public JSONObject poJSON;

        public JFXUtilDateResult(String inputText, LocalDate selectedDate, JSONObject poJSON) {
            this.inputText = inputText;
            this.selectedDate = selectedDate;
            this.poJSON = poJSON;
        }
    }

    public static boolean isObjectEqualTo(Object source, Object... others) {
        if (source == null && others != null) {
            for (Object other : others) {
                if (other == null) {
                    return true;
                }
            }
            return false;
        }

        for (Object other : others) {
            if (source != null && source.equals(other)) {
                return true;
            }
        }
        return false;
    }

    public static void setKeyPressedListener(EventHandler<KeyEvent> listener, AnchorPane... anchorPanes) {
        for (AnchorPane pane : anchorPanes) {
            for (Node node : pane.getChildrenUnmodifiable()) {
                if (node instanceof TextField) {
                    ((TextField) node).setOnKeyPressed(listener);
                } else if (node instanceof Parent) {
                    // Recursively check for nested TextFields
                    applyListenerToNestedTextFields((Parent) node, listener);
                }
            }
        }
    }

    private static void applyListenerToNestedTextFields(Parent parent, EventHandler<KeyEvent> listener) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof TextField) {
                ((TextField) child).setOnKeyPressed(listener);
            } else if (child instanceof Parent) {
                applyListenerToNestedTextFields((Parent) child, listener);
            }
        }
    }

    public static List<String> getTextFieldsIDWithPrompt(String lsPromptMsg, AnchorPane... panes) {
        List<String> results = new ArrayList<>();
        for (AnchorPane pane : panes) {
            for (Node node : pane.getChildren()) {
                collectTextFieldIDs(node, lsPromptMsg, results);
            }
        }
        return results;
    }

    private static void collectTextFieldIDs(Node node, String lsprompt, List<String> results) {
        if (node instanceof TextField) {
            TextField tf = (TextField) node;
            String prompt = tf.getPromptText();
            if (prompt != null && prompt.contains(lsprompt) && tf.getId() != null) {
                results.add(tf.getId());
            }
        } else if (node instanceof Pane) {
            Pane pane = (Pane) node;
            for (Node child : pane.getChildren()) {
                collectTextFieldIDs(child, lsprompt, results);
            }
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                collectTextFieldIDs(child, lsprompt, results);
            }
        }
    }

    private static class scrollOffset {

        double y;
        int caretPos;
    }

    public static void setVerticalScroll(TextArea textArea) {
        textArea.applyCss();
        textArea.layout();

        textArea.setStyle(
                "-fx-font-size: 10pt;"
                + "-fx-border-radius: 2px;"
                + "-fx-opacity: 1.0;"
                + "-fx-background-color: grey, white;"
                + "-fx-text-fill: black;"
                + "-fx-border-color: grey;"
        );

        // Access the internal ScrollPane
        ScrollPane scrollPane = (ScrollPane) textArea.lookup(".scroll-pane");
        if (scrollPane != null) {
            scrollPane.setStyle(
                    "-fx-focus-color: -fx-focus-color;"
                    + "-fx-vbar-policy: as-needed;"
                    + "-fx-background-color: transparent;"
                    + "-fx-opacity: 1.0;"
            );

            // Track vertical scroll position
            final scrollOffset xyOffset = new scrollOffset();
            final scrollOffset state = new scrollOffset();

            scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.intValue() == 0) {

                } else {
                    xyOffset.y = newVal.doubleValue();
                    System.out.println(xyOffset.y);
                }
            });
            textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.intValue() <= 0) {

                } else {
                    state.caretPos = newVal.intValue();
                }
            });
            // Restore scroll position on focus lost
            textArea.focusedProperty().addListener((obs, oldVal, isFocused) -> {
                if (isFocused) {
                    textArea.setStyle(
                            "-fx-background-color: orange, white;"
                            + "-fx-text-fill: black;"
                            + "-fx-border-color: orange;"
                    );

                } else {
                    textArea.setStyle(
                            "-fx-background-color: grey, white;"
                            + "-fx-text-fill: black;"
                            + "-fx-border-color: grey;"
                    );
                }
            });
        }

        textArea.getStyleClass().add("custom-text-area");
    }

    public static class LoadScreenComponents {

        public final ProgressIndicator progressIndicator;
        public final StackPane loadingPane;
        public final Label placeholderLabel;

        public LoadScreenComponents(ProgressIndicator pi, StackPane sp, Label lbl) {
            this.progressIndicator = pi;
            this.loadingPane = sp;
            this.placeholderLabel = lbl;
        }
    }

    //JFXUtil.LoadScreenComponents loading = JFXUtil.createLoadingComponents();
    //tblViewDetails.setPlaceholder(loading.loadingPane);
    public static LoadScreenComponents createLoadingComponents() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setStyle("-fx-progress-color: #FF8201;");
        progressIndicator.setVisible(true);

        StackPane loadingPane = new StackPane(progressIndicator);
        loadingPane.setAlignment(Pos.CENTER);

        Label placeholderLabel = new Label("NO RECORD TO LOAD");
        placeholderLabel.setStyle("-fx-font-size: 10px;");

        return new LoadScreenComponents(progressIndicator, loadingPane, placeholderLabel);
    }

    /*Sets in pxeModuleName for dynamic getter of form title*/
 /*Requires controller class*/
    public static String getFormattedClassTitle(Class<?> javaclass) {
        String className = javaclass.getSimpleName();

        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }

        // Handle specific company renaming
        className = className.replace("MonarchFood", "MF");
        className = className.replace("MonarchHospitality", "MH");

        // Replace underscores with space
        className = className.replace("_", " ");

        // Add space before capital letters, but preserve acronyms
        className = className.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        className = className.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        className = className.trim();

        // Special replacements after spacing
        className = className.replace("SP Car", "SPCar");
        className = className.replace("SP MC", "SPMC");

        return className;
    }

    /*V2, allow modification on naming*/
    public static String getFormattedClassTitle(Class<?> javaclass, String lsChangeIdentifier) {
        String className = javaclass.getSimpleName();

        if (className.endsWith("Controller")) {
            className = className.substring(0, className.length() - "Controller".length());
        }

        className = className.replace("MonarchFood", "MF");
        className = className.replace("MonarchHospitality", "MH");

        className = className.replace("_", " ");

        className = className.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        className = className.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        className = className.trim();

        className = className.replace("SP Car", "SPCar");
        className = className.replace("SP MC", "SPMC");

        switch (lsChangeIdentifier) {
            case "PO":
                className = className.replaceAll("\\bPO\\b", "Purchase Order");
                break;
        }
        return className;
    }

    public static String getFormattedFXMLTitle(String fxmlPath) {
        // Extract the FXML file name without extension
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1, fxmlPath.lastIndexOf('.'));

        // Remove common suffixes like "Controller"
        if (fileName.endsWith("Controller")) {
            fileName = fileName.substring(0, fileName.length() - "Controller".length());
        }

        // Handle specific company renaming
        fileName = fileName.replace("MonarchFood", "MF");
        fileName = fileName.replace("MonarchHospitality", "MH");

        // Replace underscores with space
        fileName = fileName.replace("_", " ");

        // Add space before capital letters, keeping acronyms intact
        fileName = fileName.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        fileName = fileName.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        // Trim and apply custom replacements
        fileName = fileName.trim();
        fileName = fileName.replace("SP Car", "SPCar");
        fileName = fileName.replace("SP MC", "SPMC");

        return fileName;
    }

    //JFXUtil.getFormattedClassTitle(this.getClass());
    public static <T> void selectAndFocusRow(TableView<T> tableView, int index) {
        tableView.getSelectionModel().select(index);
        tableView.getFocusModel().focus(index);
    }

    public static void setValueToNull(Object... items) {
        for (Object item : items) {
            if (item instanceof Node) {
                Node node = (Node) item;

                if (node instanceof TextInputControl) {
                    ((TextInputControl) node).clear();
                } else if (node instanceof ComboBox) {
                    ((ComboBox<?>) node).setValue(null);
                } else if (node instanceof CheckBox) {
                    ((CheckBox) node).setSelected(false);
                } else if (node instanceof DatePicker) {
                    ((DatePicker) node).setValue(null);
                } else {
                }
            } else if (item instanceof AtomicReference) {
                ((AtomicReference<?>) item).set(null);
            } else {
            }
        }
    }

    public static String safeString(Object value) {
        return value != null ? value.toString() : "";
    }

    public static TextFieldControlInfo getControlInfo(Observable o) {
        if (o instanceof ReadOnlyProperty) {
            Object bean = ((ReadOnlyProperty<?>) o).getBean();
            if (bean instanceof TextInputControl) {
                TextInputControl control = (TextInputControl) bean;
                String id = control.getId();
                String value = control.getText() != null ? control.getText() : "";
                return new TextFieldControlInfo(id, value, control);
            }
        }
        return null;
    }

    public static class TextFieldControlInfo {

        public final String lsID;
        public final String lsTxtValue;
        public final TextInputControl txtField;

        public TextFieldControlInfo(String id, String value, TextInputControl control) {
            this.lsID = id;
            this.lsTxtValue = value;
            this.txtField = control;
        }
    }

    //JFXUtil.TextFieldControlInfo txtcontrol = JFXUtil.getControlInfo((Observable) o);
    public static void setActionListener(EventHandler<ActionEvent> handler, Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof ComboBoxBase) {
                ((ComboBoxBase<?>) node).setOnAction(handler);
            } else if (node instanceof TextField) {
                ((TextField) node).setOnAction(handler);
            }
        }
    }

    public static void setJSONSuccess(JSONObject json, String message) {
        json.put("result", "success");
        json.put("message", message);
    }

    public static void setJSONError(JSONObject json, String message) {
        json.put("result", "error");
        json.put("message", message);
    }

    public static boolean isJSONSuccess(JSONObject json) {
        return ("success".equals((String) json.get("result"))) ? true : false;
    }

    public static String getJSONMessage(JSONObject json) {
        return (String) json.get("message");
    }

    private static class CommaFormater {

        boolean isUpdating;
        AtomicBoolean isAdjusting;
        int newCaretPos;
    }
//    private static boolean isUpdating = false;
//   private static  AtomicBoolean isAdjusting = new AtomicBoolean(false);
//    private static int newCaretPos = 0;

    public static void setCommaFormatter(TextField... textFields) {

        DecimalFormat finalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        finalFormat.setGroupingUsed(true);
        finalFormat.setMinimumFractionDigits(2);
        finalFormat.setMaximumFractionDigits(2);

        for (TextField textField : textFields) {
            final CommaFormater data = new CommaFormater();
            data.isUpdating = false;
            data.isAdjusting = new AtomicBoolean(false);
            data.newCaretPos = 0;
            // Disables other character
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();
                if (!newText.matches("[\\d,\\.]*")) {
                    return null;
                }

                long dotCount = newText.chars().filter(c -> c == '.').count();
                if (dotCount > 1) {
                    return null;
                }

                return change;
            };
            textField.setTextFormatter(new TextFormatter<>(filter));
            // Real-time formatting
            textField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (data.isAdjusting.get() == true) {
                    return;
                }
                try {
                    if (data.isUpdating) {
                        return;
                    }
                    data.isUpdating = true;
                    String clean = newValue.replaceAll(",", "");
                    if (clean.isEmpty() || clean.equals(".") || clean.matches("0*\\.0*")) {
                        data.isUpdating = false;
                        return;
                    }
                    try {
                        String integerPart = clean;
                        String decimalPart = "";
                        int dotIndex = clean.indexOf(".");
                        if (dotIndex >= 0) {
                            integerPart = clean.substring(0, dotIndex);
                            decimalPart = clean.substring(dotIndex);
                        }
                        long integerVal = integerPart.isEmpty() ? 0 : Long.parseLong(integerPart);
                        String formattedInteger = NumberFormat.getIntegerInstance(Locale.US).format(integerVal);
                        String formatted = formattedInteger + decimalPart;
                        Platform.runLater(() -> {
                            data.isAdjusting.set(true);
                            int originalCaretPos = textField.getCaretPosition();
                            textField.setText(formatted);
                            int offset = formatted.length() - newValue.length();
                            data.newCaretPos = originalCaretPos + offset;
                            data.newCaretPos = Math.max(0, Math.min(formatted.length(), data.newCaretPos));
                            data.isAdjusting.set(false);
                        });
                        Platform.runLater(() -> {
                            textField.positionCaret(data.newCaretPos);
                        });
                    } catch (Exception e) {
                    }
                    data.isUpdating = false;
                } catch (Exception e) {
                    data.isUpdating = false;
                }

            });
        }
    }

    public static class MonthYearPicker {

        public static class Picker {

            public final TextField textField;
            public final Popup popup;
            public final Label yearLabel;
            public final GridPane monthGrid;
            public int selectedYear;
            public int selectedMonth;
            public final Consumer<YearMonth> onDateSelected;

            public Picker(TextField textField, Consumer<YearMonth> onDateSelected) {

                this.textField = textField;
                this.onDateSelected = onDateSelected;
                this.popup = new Popup();
                this.popup.setAutoHide(true);

                selectedYear = YearMonth.now().getYear();
                selectedMonth = YearMonth.now().getMonthValue();

                textField.setPromptText("MM/YYYY");
//                textField.setEditable(false);

                VBox popupContent = new VBox(10);
                popupContent.setPadding(new Insets(10));
                popupContent.getStyleClass().add("popup-content");

                yearLabel = new Label(String.valueOf(selectedYear));
                yearLabel.getStyleClass().add("year-label");

                Button btnPrev = new Button("<");
                Button btnNext = new Button(">");
                btnPrev.getStyleClass().add("year-button");
                btnNext.getStyleClass().add("year-button");

                btnPrev.setOnAction(e -> {
                    selectedYear--;
                    yearLabel.setText(String.valueOf(selectedYear));
                    refreshMonthSelection();
                });

                btnNext.setOnAction(e -> {
                    selectedYear++;
                    yearLabel.setText(String.valueOf(selectedYear));
                    refreshMonthSelection();
                });

                HBox yearControls = new HBox(10, btnPrev, yearLabel, btnNext);
                yearControls.setAlignment(Pos.CENTER);
                yearControls.getStyleClass().add("year-bar");
                monthGrid = new GridPane();
                monthGrid.setHgap(2);
                monthGrid.setVgap(2);
                monthGrid.setAlignment(Pos.CENTER);

                Month[] months = Month.values();
                for (int i = 0; i < months.length; i++) {
                    Button btn = new Button(months[i].getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH));
                    btn.getStyleClass().add("month-button");
                    int monthValue = i + 1;
                    btn.setOnAction(e -> {
                        selectedMonth = monthValue;
                        updateTextFieldAndNotify();
                        refreshMonthSelection();
                        popup.hide();
                        textField.getParent().requestFocus();

                    });
                    monthGrid.add(btn, i % 3, i / 3);
                }

                popupContent.getChildren().addAll(yearControls, monthGrid);
                popup.getContent().add(popupContent);

                // Load CSS
                popupContent.getStylesheets().add(
                        MonthYearPicker.class.getResource("/com/rmj/guanzongroup/sidebarmenus/css/StyleSheet.css").toExternalForm()
                );
                textField.setOnMouseClicked(e -> {
                    if (!popup.isShowing()) {
                        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
                        double x = bounds.getMinX();
                        double y = bounds.getMaxY();
                        popup.show(textField, x - 13, y - 7);
                    } else {
                        popup.hide();
                    }
                });

                popup.setOnHiding(e -> {
                    updateTextFieldAndNotify();
                });

                // Initialize textField with current date
                updateTextFieldAndNotify();
                refreshMonthSelection();
            }

            public void updateTextFieldAndNotify() {
                YearMonth ym = YearMonth.of(selectedYear, selectedMonth);
                textField.setText(String.format("%02d/%d", ym.getMonthValue(), ym.getYear()));
                if (onDateSelected != null) {
                    onDateSelected.accept(ym);
                }
            }

            public void refreshMonthSelection() {
                monthGrid.getChildren().forEach(node -> node.getStyleClass().remove("selected-month"));
                for (javafx.scene.Node node : monthGrid.getChildren()) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        // Get the month value by matching the displayed short text to Month enum correctly
                        String shortMonthName = btn.getText();
                        Month m = Month.from(
                                java.time.format.DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
                                        .parse(shortMonthName)
                        );
                        if (m.getValue() == selectedMonth) {
                            btn.getStyleClass().add("selected-month");
                            break;
                        }
                    }
                }
                yearLabel.setText(String.valueOf(selectedYear));
            }

            public void setYearMonth(YearMonth ym) {
                selectedYear = ym.getYear();
                selectedMonth = ym.getMonthValue();
                updateTextFieldAndNotify();
                refreshMonthSelection();
            }

            public YearMonth getYearMonth() {
                return YearMonth.of(selectedYear, selectedMonth);
            }

            public void clear() {
                selectedYear = 0;
                selectedMonth = 0;
                textField.clear();
                monthGrid.getChildren().forEach(node -> node.getStyleClass().remove("selected-month"));
            }
        }

        public static Picker setupMonthYearPicker(TextField textField, Consumer<YearMonth> onDateSelected) {
            return new Picker(textField, onDateSelected);
        }
    }

    public static void makeKeyPressed(Node targetNode, KeyCode keyCode) {
        if (targetNode == null || keyCode == null) {
            return;
        }
        KeyEvent keyEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "", // character
                "", // text
                keyCode,
                false, // shiftDown
                false, // controlDown
                false, // altDown
                false // metaDown
        );
        targetNode.fireEvent(keyEvent);
    }

    public static void setKeyEventFilter(EventHandler<KeyEvent> handler, Node... nodes) {
        if (handler == null || nodes == null) {
            return;
        }

        for (Node node : nodes) {
            if (node != null) {
                node.addEventFilter(KeyEvent.KEY_PRESSED, handler);
            }
        }
    }

    public static void focusFirstTextField(final AnchorPane anchorPane) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                TextField textField = findFirstTextField(anchorPane);
                if (textField != null) {
                    textField.requestFocus();
                }
            }
        });
    }

    private static TextField findFirstTextField(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField) {
                return (TextField) node;
            } else if (node instanceof Parent) {
                TextField result = findFirstTextField((Parent) node);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static String removeComma(String numberStr) {
        if (numberStr == null || numberStr.equals("")) {
            return "0";
        }
        String result = numberStr.replace(",", "");
        return result.isEmpty() ? "0" : result;
    }

    public static void removeNoByKey(List<Pair<String, String>> plOrderNoPartial, List<Pair<String, String>> plOrderNoFinal, String lsNo) {
        removeFromListByKey(plOrderNoPartial, lsNo);
        removeFromListByKey(plOrderNoFinal, lsNo);
    }

    private static void removeFromListByKey(List<Pair<String, String>> list, String key) {
        Iterator<Pair<String, String>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Pair<String, String> pair = iterator.next();
            if (pair.getKey().equals(key)) {
                iterator.remove();
            }
        }
    }

    private static void setKeyEvent(Scene scene, AtomicReference<Object> lastFocusedTextField, AtomicReference<Object> previousSearchedTextField) {
        scene.focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                if (newNode instanceof Button) {
                } else {
                    lastFocusedTextField.set(newNode);
                    previousSearchedTextField.set(null);
                }
            }
        });
    }

    public static void initKeyClickObject(AnchorPane ap, AtomicReference<Object> lastFocusedTextField, AtomicReference<Object> previousSearchedTextField) {
        AnchorPane root = (AnchorPane) ap;
        Scene scene = root.getScene();
        if (scene != null) {
            setKeyEvent(scene, lastFocusedTextField, previousSearchedTextField);
        } else {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setKeyEvent(newScene, lastFocusedTextField, previousSearchedTextField);
                }
            });
        }
    }

    public static void setCheckboxHoverCursor(Parent... anchorpane) {
        for (Parent container : anchorpane) {
            applyToCheckBoxes(container);
        }
    }

    private static void applyToCheckBoxes(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof CheckBox) {
                final CheckBox checkBox = (CheckBox) node;
                checkBox.hoverProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        checkBox.setCursor(checkBox.isDisabled() ? Cursor.DEFAULT : Cursor.HAND);
                    } else {
                        checkBox.setCursor(Cursor.DEFAULT);
                    }
                });
            } else if (node instanceof Parent) {
                applyToCheckBoxes((Parent) node); // recursively check inner containers
            }
        }
    }

    public static boolean isGeneralFXML(String fxmlPath) {
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1, fxmlPath.lastIndexOf('.'));

        int underscoreIndex = fileName.indexOf('_');

        if (underscoreIndex == -1) {
            return true;
        }

        String suffix = fileName.substring(underscoreIndex + 1);

        String[] generalSuffixes = {
            "Entry", "Confirmation", "History", "Approval"
        };
        for (String general : generalSuffixes) {
            if (suffix.equals(general)) {
                return true;
            }
        }
        return false;
    }

    public static String formatForMessageBox(String message, int maxLinewidth) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() + 1 > maxLinewidth) {
                result.append(line.toString().trim()).append("\n");
                line.setLength(0);
            }
            line.append(word).append(" ");
        }

        if (line.length() > 0) {
            result.append(line.toString().trim());
        }

        return result.toString();
    }

    @FunctionalInterface
    public interface Action<T> {

        T execute();
    }

    public static void executeConditional(boolean condition, Runnable trueAction, Runnable falseAction) {
        if (condition) {
            trueAction.run();
        } else {
            falseAction.run();
        }
    }

    public static void applyHoverFadeToButtons(String firstColorHex, String secondColorHex, Button... buttons) {
        for (Button button : buttons) {
            Node graphic = button.getGraphic();
            if (graphic instanceof FontAwesomeIconView) {
                FontAwesomeIconView icon = (FontAwesomeIconView) graphic;

                button.setOnMouseEntered(e -> animateColorFade(icon, firstColorHex, secondColorHex));
                button.setOnMouseExited(e -> animateColorFade(icon, secondColorHex, firstColorHex));
            }
        }
    }

    private static void animateColorFade(FontAwesomeIconView icon, String fromColorHex, String toColorHex) {
        Color startColor = Color.web(fromColorHex);
        Color endColor = Color.web(toColorHex);

        ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(startColor);
        colorProperty.addListener((obs, oldVal, newVal) -> icon.setFill(newVal));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(colorProperty, startColor)),
                new KeyFrame(Duration.millis(300), new KeyValue(colorProperty, endColor))
        );

        timeline.play();
    }

    public static void applyToggleHoverAnimation(ToggleButton... toggleButtons) {
        for (ToggleButton toggleButton : toggleButtons) {
            FontAwesomeIconView icon = extractFontAwesomeIcon(toggleButton);
            if (icon != null) {
                // Hover
                toggleButton.setOnMouseEntered(e -> scaleIcon(icon, 1.2, 150));
                toggleButton.setOnMouseExited(e -> {
                    if (!toggleButton.isSelected()) {
                        scaleIcon(icon, 1.0, 150);
                    }
                });

                // Toggle click
                toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        playClickBounce(icon);
                    } else {
                        scaleIcon(icon, 1.0, 150);
                    }
                });
            }
        }
    }

    private static FontAwesomeIconView extractFontAwesomeIcon(ToggleButton toggleButton) {
        Node graphic = toggleButton.getGraphic();
        if (graphic instanceof FontAwesomeIconView) {
            return (FontAwesomeIconView) graphic;
        }
        return null;
    }

    private static void scaleIcon(FontAwesomeIconView icon, double scaleTo, double durationMillis) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMillis), icon);
        st.setToX(scaleTo);
        st.setToY(scaleTo);
        st.play();
    }

    private static void playClickBounce(FontAwesomeIconView icon) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(80), icon);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition expand = new ScaleTransition(Duration.millis(150), icon);
        expand.setToX(1.2);
        expand.setToY(1.2);

        shrink.setOnFinished(e -> expand.play());
        shrink.play();
    }

    public static void placeClockInAnchorPane(AnchorPane anchorPane, double size) {
        if (anchorPane == null) {
            return;
        }

        Pane clockGraphic = createClockGraphic(size);
        anchorPane.getChildren().add(clockGraphic);
    }
    static double hourHand1 = 0.27;
    static double minuteHand1 = 0.3;

    private static Pane createClockGraphic(final double size) {
        final Pane clockPane = new Pane();
        final double center = size / 2;
        final double radius = center - 5;

        Circle clockFace = new Circle(center, center, radius);
        clockFace.setFill(Color.TRANSPARENT);
        clockFace.setStroke(Color.BLACK);
        clockFace.setStrokeWidth(2.5);

        final Line hourHand = new Line(center, center, center, center - radius * hourHand1);
        hourHand.setStrokeWidth(1.3);

        final Line minuteHand = new Line(center, center, center, center - radius * minuteHand1);
        minuteHand.setStrokeWidth(1.3);
        minuteHand.setStroke(Color.BLACK);

        clockPane.getChildren().addAll(clockFace, hourHand, minuteHand);

        Timeline clockUpdater = new Timeline(new KeyFrame(Duration.seconds(1),
                new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                updateHands(hourHand, minuteHand, center);
            }
        }
        ));
        clockUpdater.setCycleCount(Timeline.INDEFINITE);
        clockUpdater.play();

        clockPane.setPrefSize(size, size);
        return clockPane;
    }

    private static void updateHands(Line hourHand, Line minuteHand, double center) {
        LocalDateTime now = LocalDateTime.now();
        double hourAngle = (now.getHour() % 12 + now.getMinute() / 60.0) * 30;
        double minuteAngle = now.getMinute() * 6;

        setHandAngle(hourHand, hourAngle, center, center * hourHand1);
        setHandAngle(minuteHand, minuteAngle, center, center * minuteHand1);
    }

    private static void setHandAngle(Line hand, double angle, double center, double length) {
        double radians = Math.toRadians(angle - 90);
        hand.setEndX(center + length * Math.cos(radians));
        hand.setEndY(center + length * Math.sin(radians));
    }

    public static class RowDragLock {

        public boolean isEnabled;

        public RowDragLock(boolean enabled) {
            this.isEnabled = enabled;
        }
    }

    /*Returns new arranged indexes that should be set to dragged row*/
    public static <T> void enableRowDragAndDrop(
            TableView<T> tableView,
            Function<T, StringProperty> index01Getter,
            Function<T, StringProperty> index03Getter,
            Function<T, StringProperty> index04Getter,
            RowDragLock dragLock,
            Consumer<Integer> onDropCallback
    ) {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();

            row.setOnMouseEntered(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.OPEN_HAND);
                }
            });

            row.setOnMouseExited(e -> row.setCursor(Cursor.DEFAULT));

            row.setOnMousePressed(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.CLOSED_HAND);
                }
            });

            row.setOnMouseReleased(e -> {
                if (!row.isEmpty() && dragLock.isEnabled && !isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    row.setCursor(Cursor.OPEN_HAND);
                }
            });

            row.setOnDragDetected(event -> {
                if (!dragLock.isEnabled || row.isEmpty() || isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    return;
                }

                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("drag");
                db.setContent(content);

                tableView.getProperties().put("dragSourceIndex", row.getIndex());
                row.setCursor(Cursor.CLOSED_HAND);
                event.consume();
            });

            row.setOnDragOver(event -> {
                if (!dragLock.isEnabled || event.getGestureSource() == row || row.isEmpty()
                        || isBlankRow(row.getItem(), index01Getter, index03Getter, index04Getter)) {
                    return;
                }

                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);

                    int dragIndex = (int) tableView.getProperties().get("dragSourceIndex");
                    int hoverIndex = row.getIndex();

                    double sceneY = event.getSceneY();
                    double rowY = row.localToScene(row.getBoundsInLocal()).getMinY();
                    double rowHeight = row.getHeight();
                    boolean isTopHalf = (sceneY - rowY) < rowHeight / 2;
                    int targetIndex = isTopHalf ? hoverIndex : hoverIndex + 1;

                    if (targetIndex == dragIndex || targetIndex == dragIndex + 1) {
                        row.setStyle("");
                    } else if (isTopHalf) {
                        row.setStyle("-fx-border-color: #FF8201; -fx-border-width: 2px 0 0 0;");
                    } else {
                        row.setStyle("-fx-border-color: #FF8201; -fx-border-width: 0 0 2px 0;");
                    }

                    tableView.getProperties().put("highlightIndex", targetIndex);
                    event.consume();
                }
            });

            row.setOnDragExited(e -> row.setStyle(""));

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (!db.hasString()) {
                    return;
                }

                int dragIndex = (int) tableView.getProperties().get("dragSourceIndex");
                int dropIndex = (int) tableView.getProperties().getOrDefault("highlightIndex", dragIndex);

                if (dropIndex > dragIndex) {
                    dropIndex--;
                }

                ObservableList<T> items = tableView.getItems();
                T draggedItem = items.remove(dragIndex);

                if (dropIndex >= items.size()) {
                    items.add(draggedItem);
                    dropIndex = items.size() - 1;
                } else {
                    items.add(dropIndex, draggedItem);
                }

                renumberIndex01(items, index01Getter);
                tableView.getSelectionModel().select(dropIndex);
                tableView.getProperties().remove("dragSourceIndex");
                tableView.getProperties().remove("highlightIndex");

                if (onDropCallback != null) {
                    onDropCallback.accept(dropIndex);
                }

                event.setDropCompleted(true);
                event.consume();
            });

            row.setOnDragDone(e -> row.setCursor(Cursor.DEFAULT));

            return row;
        });
    }

    private static <T> void renumberIndex01(ObservableList<T> items, Function<T, StringProperty> index01Getter) {
        for (int i = 0; i < items.size(); i++) {
            index01Getter.apply(items.get(i)).set(String.valueOf(i + 1));
        }
    }

    private static <T> boolean isBlankRow(
            T item,
            Function<T, StringProperty> index01Getter,
            Function<T, StringProperty> index03Getter,
            Function<T, StringProperty> index04Getter
    ) {
        if (item == null) {
            return true;
        }

        String val1 = index01Getter.apply(item).get();
        String val3 = index03Getter.apply(item).get();
        String val4 = index04Getter.apply(item).get();

        return val1 == null || val1.trim().isEmpty()
                || val3 == null || val3.trim().isEmpty()
                || val4 == null || val4.trim().isEmpty();
    }

    public static class Pairs<K, V> {

        public final K key;
        public final V value;

        public Pairs(K ObservableList, V comboBox) {
            this.key = ObservableList;
            this.value = comboBox;
        }
    }

    public static <T> void setComboBoxItems(Pairs<ObservableList<T>, ComboBox<T>>... comboPairs) {
        for (Pairs<ObservableList<T>, ComboBox<T>> pair : comboPairs) {
            ObservableList<T> list = pair.key;
            ComboBox<T> cb = pair.value;

            cb.getItems().clear();
            cb.setItems(list);

            if (!list.isEmpty()) {
                cb.getSelectionModel().select(0); // selects the first item
            }
        }
    }

    public static void setComboBoxActionListener(EventHandler<ActionEvent> listener, ComboBox<?>... comboBoxes) {
        for (ComboBox<?> cb : comboBoxes) {
            cb.setOnAction(listener);
        }
    }

//    public static <T> void enableAutoFillOnFocusWithDropdownHighlight(String hexColor, ComboBox<T>... comboBoxes) {
//        for (ComboBox<T> comboBox : comboBoxes) {
//            comboBox.setEditable(false);
//
//            // Only style dropdown list cells — do not change display
//            comboBox.setCellFactory(cb -> new ListCell<T>() {
//                @Override
//                protected void updateItem(T item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                        setStyle("");
//                    } else {
//                        setText(item.toString());
//                        if (comboBox.getSelectionModel().getSelectedItem() != null
//                                && comboBox.getSelectionModel().getSelectedItem().equals(item)) {
//                            setStyle("-fx-background-color: " + hexColor + "; -fx-text-fill: white;");
//                        } else {
//                            setStyle("");
//                        }
//                    }
//                }
//            });
//
//            // On key typed, select the first item starting with that character (case-insensitive)
//            comboBox.addEventFilter(KeyEvent.KEY_TYPED, event -> {
//                if (!comboBox.isFocused()) {
//                    return;
//                }
//
//                String typedChar = event.getCharacter().toLowerCase();
//                ObservableList<T> items = comboBox.getItems();
//
//                for (T item : items) {
//                    if (item != null && item.toString().toLowerCase().startsWith(typedChar)) {
//                        comboBox.getSelectionModel().select(item);
//                        break;
//                    }
//                }
//
//                event.consume();
//            });
//        }
//    }
//
    //sample usage
// JFXUtil.ReloadableTableTask loadTableDetail;
//        loadTableMain = new JFXUtil.ReloadableTableTask(
//                tblViewTransDetails,
//                details_data,
//                () -> {
//                } );
    public static class ReloadableTableTask {

        private final TableView<?> tableView;
        private final ObservableList<?> data;
        private final Runnable content;

        public ReloadableTableTask(TableView<?> tableView, ObservableList<?> data, Runnable content) {
            this.tableView = tableView;
            this.data = data;
            this.content = content;
        }

        public void reload() {
            LoadScreenComponents loading = createLoadingComponents();
            tableView.setPlaceholder(loading.loadingPane);
            loading.progressIndicator.setVisible(true);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    content.run(); // Caller must wrap with Platform.runLater if needed
                    return null;
                }

                @Override
                protected void succeeded() {
                    if (data == null || data.isEmpty()) {
                        tableView.setPlaceholder(loading.placeholderLabel);
                    } else {
                        tableView.toFront();
                    }
                    loading.progressIndicator.setVisible(false);
                }

                @Override
                protected void failed() {
                    if (data == null || data.isEmpty()) {
                        tableView.setPlaceholder(loading.placeholderLabel);
                    }
                    loading.progressIndicator.setVisible(false);
                }
            };
            new Thread(task).start();
        }
    }

    public static void textFieldMoveNext(Node node) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                delay.setOnFinished(event -> {
                    if (node instanceof DatePicker) {
                        ((DatePicker) node).getEditor().requestFocus();
                    } else if (node instanceof TextInputControl) {
                        ((TextInputControl) node).requestFocus();
                    } else {
                        node.requestFocus();
                    }
                });
                delay.play();
            }
        });
    }

    public static void initiateBtnSearch(
            String pxeModuleName,
            AtomicReference<Object> lastFocusedTextField,
            AtomicReference<Object> previousSearchedTextField,
            AnchorPane... anchorPanes
    ) {
        String lsMessage = "Focus a searchable textfield to search";
        Object lastNode = lastFocusedTextField.get();

        if (lastNode instanceof TextField) {
            TextField tf = (TextField) lastNode;

            boolean isSearchable = false;
            for (AnchorPane ap : anchorPanes) {
                if (JFXUtil.getTextFieldsIDWithPrompt("Press F3: Search", ap).contains(tf.getId())) {
                    isSearchable = true;
                    break;
                }
            }

            if (isSearchable) {
                if (lastNode == previousSearchedTextField.get()) {
                    return;
                }

                previousSearchedTextField.set(lastNode);
                JFXUtil.makeKeyPressed(tf, KeyCode.F3);
            } else {
                ShowMessageFX.Information(null, pxeModuleName, lsMessage);
            }

        } else if (lastNode != null) {
            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
        } else {
            ShowMessageFX.Information(null, pxeModuleName, lsMessage);
        }
    }

// sample usage
//    ChangeListener<Boolean> txtArea_Focus = JFXUtil.FocusListener(TextArea.class,
//            (lsID, lsValue) -> {
//            });
    public static ChangeListener<Boolean> FocusListener(Class<? extends TextInputControl> nodeType, BiConsumer<String, String> onLostFocus) {
        return (observable, oldValue, newValue) -> {
            Object bean = ((ReadOnlyBooleanPropertyBase) observable).getBean();
            if (!nodeType.isInstance(bean)) {
                return;
            }

            TextInputControl control = nodeType.cast(bean);
            String id = control.getId();
            String value = control.getText();

            if (value == null) {
                return;
            }

            if (!newValue) { // Lost focus
                onLostFocus.accept(id, value.trim());
            }
        };
    }

    public static <T> void disableArrowNavigation(TableView<T> table) {
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                event.consume(); // Prevents moving up/down between rows
            }
        });
    }

    public static <T> void addCheckboxColumns(
            Class<T> modelClass,
            TableView<T> table,
            BooleanProperty disableAll,
            TriConsumer<T, Integer, Integer, Boolean> onChange,
            int... columnIndexes) {

        for (int colIndex : columnIndexes) {
            @SuppressWarnings("unchecked")
            TableColumn<T, ?> baseCol = table.getColumns().get(colIndex);

            @SuppressWarnings("unchecked")
            TableColumn<T, Boolean> column = (TableColumn<T, Boolean>) baseCol;

            final int finalColIndex = colIndex;

            column.setCellValueFactory(cellData -> {
                T row = cellData.getValue();
                try {
                    // Expect getters: getIndex01, getIndex02, etc.
                    String getterName = "getIndex" + String.format("%02d", colIndex + 1);
                    Method getter = modelClass.getMethod(getterName);

                    String value = (String) getter.invoke(row);
                    boolean boolVal = "1".equals(value);
                    return new javafx.beans.property.SimpleBooleanProperty(boolVal);

                } catch (Exception e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleBooleanProperty(false);
                }
            });

            column.setCellFactory(new Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>>() {
                @Override
                public TableCell<T, Boolean> call(TableColumn<T, Boolean> param) {
                    return new TableCell<T, Boolean>() {
                        private final CheckBox checkBox = new CheckBox();

                        {
                            // Center checkbox
                            setStyle("-fx-alignment: CENTER;");
                            setCheckboxStyle("#7B8182", checkBox);
                            // Cursor binding
                            checkBox.cursorProperty().bind(
                                    Bindings.when(disableAll)
                                            .then(Cursor.DEFAULT)
                                            .otherwise(Cursor.HAND)
                            );

                            // Disable binding
                            checkBox.disableProperty().bind(disableAll);

                            // Only trigger on user click
                            checkBox.setOnAction(evt -> {
                                if (getTableRow() != null && getTableRow().getItem() != null) {
                                    @SuppressWarnings("unchecked")
                                    T row = (T) getTableRow().getItem();
                                    int rowIndex = getTableRow().getIndex();

                                    try {
                                        // Reflect setter e.g. setIndex01
                                        String setterName = "setIndex" + String.format("%02d", finalColIndex + 1);
                                        Method setter = modelClass.getMethod(setterName, String.class);
                                        setter.invoke(row, checkBox.isSelected() ? "1" : "0");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if (onChange != null) {
                                        onChange.accept(row, rowIndex, finalColIndex, checkBox.isSelected());
                                    }
                                }
                            });
                        }

                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                checkBox.setSelected(item != null && item);
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });
        }
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V, W> {

        void accept(T t, U u, V v, W w);
    }

    public static void clickTabByTitleText(TabPane tabPane, String title) {
        for (Tab tab : tabPane.getTabs()) {
            if (title.equals(tab.getText())) {
                Tab current = tabPane.getSelectionModel().getSelectedItem();
                if (current == tab && !tabPane.getTabs().isEmpty()) {
                    // Temporarily select the first tab (index 0) to force deselection
                    tabPane.getSelectionModel().select(0);
                }
                // Now select the target tab again
                tabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    public static void runWithDelay(double seconds, Runnable action) {
        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(seconds));
            delay.setOnFinished(e -> action.run());
            delay.play();
        });
    }

    public static void setCheckboxStyle(String hexColor, CheckBox... checkBoxes) {
        for (CheckBox cb : checkBoxes) {
            if (!cb.getStyleClass().contains("modern")) {
                cb.getStyleClass().add("modern");
            }
            cb.setStyle("-c-accent: " + hexColor + ";");

            cb.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (newSkin != null) {
                    setupCheckAnimation(cb);
                }
            });
            Platform.runLater(() -> setupCheckAnimation(cb));
        }
    }

    private static void setupCheckAnimation(CheckBox cb) {
        Node mark = cb.lookup(".mark");
        if (mark == null) {
            return;
        }

        ScaleTransition popIn = new ScaleTransition(Duration.millis(140), mark);
        popIn.setFromX(0.6);
        popIn.setFromY(0.6);
        popIn.setToX(1.0);
        popIn.setToY(1.0);

        cb.selectedProperty().addListener((o, was, isNow) -> {
            if (isNow) {
                mark.setScaleX(0.6);
                mark.setScaleY(0.6);
                popIn.stop();
                popIn.playFromStart();
            } else {
                mark.setScaleX(0.0);
                mark.setScaleY(0.0);
            }
        });
    }

    private static void syncMarkScale(CheckBox cb) {
        Node mark = cb.lookup(".mark");
        if (mark != null) {
            if (cb.isSelected()) {
                mark.setScaleX(1.0);
                mark.setScaleY(1.0);
            } else {
                mark.setScaleX(0.0);
                mark.setScaleY(0.0);
            }
        }
    }

    private static final Map<Class<?>, Map<String, String>> cache = new HashMap<>();

    public static String setStatusValue(Node node, Class<?> clazz, String value) {
        String text = getNameByValue(clazz, value);

        Platform.runLater(() -> {
            if (node instanceof Label) {
                ((Label) node).setText(text);
            } else if (node instanceof TextField) {
                ((TextField) node).setText(text);
            } else if (node instanceof TextArea) {
                ((TextArea) node).setText(text);
            } else if (node instanceof Button) {
                ((Button) node).setText(text);
            } else {
                //if null
            }
        });
        return text;
    }

    private static String getNameByValue(Class<?> clazz, String value) {
        if ("-1".equals(value) || "".equals(value)) {
            return "UNKNOWN";
        }
        return buildValueToNameMap(clazz).getOrDefault(value, "UNKNOWN");
    }

    private static Map<String, String> buildValueToNameMap(Class<?> clazz) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }
        Map<String, String> valueToNameMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    Object value = field.get(null);
                    if (value instanceof String) {
                        String fieldName = field.getName();
                        // Special handling: if field name is VOID, change to VOIDED
                        if ("VOID".equals(fieldName)) {
                            fieldName = "VOIDED";
                        }
                        valueToNameMap.put((String) value, fieldName);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        cache.put(clazz, valueToNameMap);
        return valueToNameMap;
    }

//    EventHandler<ActionEvent> comboboxlistener = JFXUtil.CmbActionListener(
//            (cmbId, selectedIndex, selectedValue) -> {
//            }
//    );
    public static <T> EventHandler<ActionEvent> CmbActionListener(ComboBoxListener<T> listener) {
        return event -> {
            @SuppressWarnings("unchecked")
            ComboBox<T> comboBox = (ComboBox<T>) event.getSource();

            String comboId = comboBox.getId() != null ? comboBox.getId() : "NO_ID";
            int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
            T selectedValue = comboBox.getSelectionModel().getSelectedItem();

            listener.onChange(comboId, selectedIndex, selectedValue);
        };
    }

    @FunctionalInterface
    public interface ComboBoxListener<T> {

        void onChange(String comboId, int selectedIndex, T selectedValue);
    }

    public static <T> void setComboBoxValue(EventHandler<ActionEvent> listener, int selection, ComboBox<T>... comboBoxes) {
        for (ComboBox<T> comboBox : comboBoxes) {

            comboBox.setOnAction(null);
            if (!comboBox.getItems().isEmpty()) {
                comboBox.getSelectionModel().select(selection);
            }
            comboBox.setOnAction(listener);
        }
    }

    public static void requestFocusNullField(Object[][] checks, TextField fallback) {
        Stream.of(checks)
                .filter(c -> isObjectEqualTo(c[0], null, ""))
                .map(c -> (TextField) c[1])
                .findFirst()
                .orElse(fallback)
                .requestFocus();
    }

    public static AbstractMap.SimpleEntry<String, Class<? extends ScreenInterface>> returnData(
            Class<? extends ScreenInterface> clazz) {
        return new AbstractMap.SimpleEntry<>(getFormattedClassTitle(clazz), clazz);
    }

    static class BreakLoopException extends RuntimeException {
    }

    public static void ifError(boolean isError, TextField txtField, JSONObject poJSON, String pxeModuleName, Runnable orElse) {
        if (isError) {
            ShowMessageFX.Warning(null, pxeModuleName, (String) poJSON.get("message"));
            txtField.setText("");
            throw new BreakLoopException();
        } else {
            orElse.run();
        }
    }

    public static int getDetailRow(ObservableList<?> dataList, int lnpn, int columnIndex) {
        int result = lnpn - 1;
        try {
            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);
                // Build dynamic getter name like "getIndex07"
                String getterName = String.format("getIndex%02d", columnIndex);
                String value = (String) item.getClass().getMethod(getterName).invoke(item);

                if (String.valueOf(lnpn).equals(value)) {
                    // Always get index01
                    String index01 = (String) item.getClass().getMethod("getIndex01").invoke(item);
                    result = Integer.parseInt(index01) - 1;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getDetailTempRow(ObservableList<?> dataList, int lnpn, int columnIndex) {
        int result = 0;
        try {
            for (int lnCtr = 0; lnCtr < dataList.size(); lnCtr++) {
                Object item = dataList.get(lnCtr);
                // Always search using Index01
                String index01 = (String) item.getClass().getMethod("getIndex01").invoke(item);

                if (String.valueOf(lnpn).equals(index01)) {
                    // Build dynamic getter name for return field sample index07
                    String getterName = String.format("getIndex%02d", columnIndex);
                    String value = (String) item.getClass().getMethod(getterName).invoke(item);
                    result = Integer.parseInt(value);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject checkIfFolderExists(JSONObject poJSON, String lsExportPath ) {
        File folder = new File(lsExportPath);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                // Folder successfully created
                System.out.println("Folder created at: " + folder.getAbsolutePath());
            } else {
                // Failed to create folder
                poJSON.put("result", "error");
                poJSON.put("message", "Failed to create folder. \n\nEnsure the application has write permissions \"");
                return poJSON;
            }
        }
        return poJSON;
    }
    
    /*Alternative version of inputDecimalOnly; restricts to 1 dot, commas not allowed*/
    public static void inputDecimalOnly(TextField... foTxtFields) {
        Pattern pattern = Pattern.compile("\\d*(\\.\\d*)?");
        for (TextField txtField : foTxtFields) {
            if (txtField != null) {
                txtField.setTextFormatter(new TextFormatter<>(change -> {
                    String newText = change.getControlNewText();
                    if (newText.isEmpty()) {
                        return change;
                    }
                    if (newText.contains(",")) {
                        return null;
                    }
                    if (!pattern.matcher(newText).matches()) {
                        return null;
                    }
                    return change;
                }));
            }
        }
    }
    
    /*Removes TextField listener*/
    public static void removeTextFieldListener(ChangeListener<String> searchListener, TextField textField) {
        if (searchListener != null) {
            // Remove the listener if already attached
            textField.textProperty().removeListener(searchListener);
        }
    }
}
