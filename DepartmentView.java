import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class DepartmentView {

    private final DepartmentOperations ops = new DepartmentOperations();
    private TableView<Department>      table;
    private ObservableList<Department> data = FXCollections.observableArrayList();

    public VBox build() {
        VBox view = new VBox(22);
        view.setMaxWidth(Double.MAX_VALUE); view.setMaxHeight(Double.MAX_VALUE);

        HBox header = new HBox(16); header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4, MainApp.pageTitle("Departments"), MainApp.pageSub("Manage organisational departments and budgets"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnAdd = MainApp.primaryBtn("+ Add Department");
        btnAdd.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titles, sp, btnAdd);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setItems(data);
        loadAll();

        view.getChildren().addAll(header, table);
        return view;
    }

    @SuppressWarnings("unchecked")
    private TableView<Department> buildTable() {
        TableView<Department> tv = (TableView<Department>) MainApp.styledTable();
        tv.setRowFactory(r -> {
            TableRow<Department> row = new TableRow<>();
            row.selectedProperty().addListener((obs, o, n) ->
                row.setStyle(n ? "-fx-background-color:" + MainApp.SEL_ROW + ";" : ""));
            return row;
        });
        tv.getColumns().addAll(
            col("ID",       d -> String.valueOf(d.getDeptId()),  60),
            col("Name",     Department::getDeptName,            180),
            col("Location", Department::getLocation,            130),
            col("Manager",  Department::getManagerName,         160),
            col("Budget (₹)", d -> String.format("₹ %,.0f", d.getBudget()), 130),
            actionsCol()
        );
        return tv;
    }

    private TableColumn<Department, String> col(String title,
            java.util.function.Function<Department, String> fn, double min) {
        TableColumn<Department, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        c.setMinWidth(min);
        return c;
    }

    private TableColumn<Department, String> actionsCol() {
        TableColumn<Department, String> c = new TableColumn<>("Actions");
        c.setMinWidth(130); c.setMaxWidth(130);
        c.setCellFactory(col -> new TableCell<>() {
            final Button edit = MainApp.editBtn("Edit");
            final Button del  = MainApp.dangerBtn("Delete");
            final HBox   box  = new HBox(6, edit, del);
            { box.setAlignment(Pos.CENTER);
              edit.setOnAction(e -> showForm(getTableView().getItems().get(getIndex())));
              del.setOnAction(e  -> deleteDept(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : box);
            }
        });
        return c;
    }

    private void showForm(Department dept) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(dept == null ? "Add Department" : "Edit Department");
        dlg.getDialogPane().setPrefWidth(460);

        Label title = new Label(dept == null ? "New Department" : "Edit: " + dept.getDeptName());
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(MainApp.TXT_DARK));

        TextField tfName   = MainApp.field("Department Name");
        TextField tfLoc    = MainApp.field("Location");
        TextField tfMgr    = MainApp.field("Manager Name");
        TextField tfBudget = MainApp.field("Budget (e.g. 800000)");

        if (dept != null) {
            tfName.setText(dept.getDeptName());
            tfLoc.setText(dept.getLocation());
            tfMgr.setText(dept.getManagerName());
            tfBudget.setText(String.valueOf(dept.getBudget()));
        }

        GridPane g = new GridPane(); g.setHgap(14); g.setVgap(13);
        g.setPadding(new Insets(14, 0, 0, 0));
        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);

        addRow(g, 0, "Name *",     tfName);
        addRow(g, 1, "Location",   tfLoc);
        addRow(g, 2, "Manager",    tfMgr);
        addRow(g, 3, "Budget (₹)", tfBudget);

        VBox body = new VBox(12, title, new Separator(), g);
        body.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(body);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText(dept == null ? "Add Department" : "Save Changes");
        ok.setStyle("-fx-background-color:" + MainApp.ACCENT + ";-fx-text-fill:white;-fx-background-radius:6;");

        dlg.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            if (tfName.getText().isBlank()) { MainApp.alert(Alert.AlertType.WARNING, "Validation", "Department name is required."); return; }
            double budget = 0;
            try { budget = Double.parseDouble(tfBudget.getText().trim()); } catch (Exception ignored) {}
            try {
                if (dept == null) {
                    ops.addDepartment(new Department(tfName.getText().trim(), tfLoc.getText().trim(), tfMgr.getText().trim(), budget));
                } else {
                    Department d = new Department(dept.getDeptId(), tfName.getText().trim(), tfLoc.getText().trim(), tfMgr.getText().trim(), budget);
                    ops.updateDepartment(d);
                }
                loadAll();
            } catch (DatabaseException ex) { MainApp.alert(Alert.AlertType.ERROR, "DB Error", ex.getMessage()); }
        });
    }

    private void deleteDept(Department dept) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete department '" + dept.getDeptName() + "'?\nThis will fail if employees are linked.", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            try { ops.deleteDepartment(dept.getDeptId()); loadAll(); }
            catch (DatabaseException ex) { MainApp.alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });
    }

    private void loadAll() {
        try { data.setAll(ops.getAllDepartments()); }
        catch (DatabaseException e) { MainApp.alert(Alert.AlertType.ERROR, "DB Error", e.getMessage()); }
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        g.add(MainApp.formLabel(label), 0, row);
        g.add(field, 1, row);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
    }
}
