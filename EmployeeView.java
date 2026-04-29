import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.ArrayList;

public class EmployeeView {

    private final EmployeeOperations   ops     = new EmployeeOperations();
    private final DepartmentOperations deptOps = new DepartmentOperations();

    private TableView<Employee>      table;
    private ObservableList<Employee> data = FXCollections.observableArrayList();
    private ArrayList<Department>    depts;

    public VBox build() {
        try { depts = deptOps.getAllDepartments(); }
        catch (DatabaseException e) { depts = new ArrayList<>(); }

        VBox view = new VBox(22);
        view.setMaxWidth(Double.MAX_VALUE); view.setMaxHeight(Double.MAX_VALUE);

        // Header row
        HBox header = new HBox(16); header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4, MainApp.pageTitle("Employees"), MainApp.pageSub("Add, edit, search and remove employee records"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnAdd = MainApp.primaryBtn("+ Add Employee");
        btnAdd.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titles, sp, btnAdd);

        // Search bar
        HBox searchBar = new HBox(10); searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField tfSearch = MainApp.field("Search name / email / designation…");
        tfSearch.setPrefWidth(340);
        Button btnSearch  = MainApp.outlineBtn("Search");
        Button btnRefresh = MainApp.outlineBtn("↺  Show All");
        btnSearch.setOnAction(e -> doSearch(tfSearch.getText().trim()));
        tfSearch.setOnAction(e -> doSearch(tfSearch.getText().trim()));
        btnRefresh.setOnAction(e -> { tfSearch.clear(); loadAll(); });
        searchBar.getChildren().addAll(tfSearch, btnSearch, btnRefresh);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setItems(data);
        loadAll();

        view.getChildren().addAll(header, searchBar, table);
        return view;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Employee> buildTable() {
        TableView<Employee> tv = (TableView<Employee>) MainApp.styledTable();
        tv.setRowFactory(r -> {
            TableRow<Employee> row = new TableRow<>();
            row.selectedProperty().addListener((obs, o, n) ->
                row.setStyle(n ? "-fx-background-color:" + MainApp.SEL_ROW + ";" : ""));
            return row;
        });

        tv.getColumns().addAll(
            col("ID",          e -> String.valueOf(e.getEmpId()),  60),
            col("Name",        Employee::getEmpName,              155),
            col("Age",         e -> String.valueOf(e.getAge()),    50),
            col("Gender",      Employee::getGender,                75),
            col("Designation", Employee::getDesignation,          140),
            col("Email",       Employee::getEmail,                185),
            col("Phone",       Employee::getPhone,                110),
            col("Hire Date",   Employee::getHireDate,             100),
            col("Dept ID",     e -> String.valueOf(e.getDeptId()),  65),
            actionsCol()
        );
        return tv;
    }

    private TableColumn<Employee, String> col(String title,
            java.util.function.Function<Employee, String> fn, double min) {
        TableColumn<Employee, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        c.setMinWidth(min);
        return c;
    }

    private TableColumn<Employee, String> actionsCol() {
        TableColumn<Employee, String> c = new TableColumn<>("Actions");
        c.setMinWidth(130); c.setMaxWidth(130);
        c.setCellFactory(col -> new TableCell<>() {
            final Button edit = MainApp.editBtn("Edit");
            final Button del  = MainApp.dangerBtn("Delete");
            final HBox box    = new HBox(6, edit, del);
            { box.setAlignment(Pos.CENTER);
              edit.setOnAction(e -> showForm(getTableView().getItems().get(getIndex())));
              del.setOnAction(e  -> deleteEmp(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : box);
            }
        });
        return c;
    }

    // ── Form dialog ───────────────────────────────────────────────────────────
    private void showForm(Employee emp) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(emp == null ? "Add Employee" : "Edit Employee");
        dlg.getDialogPane().setPrefWidth(560);

        Label title = new Label(emp == null ? "New Employee" : "Edit: " + emp.getEmpName());
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(MainApp.TXT_DARK));

        TextField tfName  = MainApp.field("Full Name");
        TextField tfAge   = MainApp.field("Age");
        TextField tfEmail = MainApp.field("Email");
        TextField tfPhone = MainApp.field("Phone");
        TextField tfAddr  = MainApp.field("Address");
        TextField tfDesig = MainApp.field("Designation");
        TextField tfHire  = MainApp.field("Hire Date (YYYY-MM-DD)");

        ComboBox<String> cbGender = MainApp.combo("Gender", "Male", "Female", "Other");
        ComboBox<String> cbDept   = MainApp.combo("Department");
        for (Department d : depts) cbDept.getItems().add(d.getDeptId() + " – " + d.getDeptName());

        if (emp != null) {
            tfName.setText(emp.getEmpName());
            tfAge.setText(String.valueOf(emp.getAge()));
            tfEmail.setText(emp.getEmail()); tfEmail.setDisable(true); // email is unique key
            tfPhone.setText(emp.getPhone());
            tfAddr.setText(emp.getAddress());
            tfDesig.setText(emp.getDesignation());
            tfHire.setText(emp.getHireDate() != null ? emp.getHireDate() : "");
            cbGender.setValue(emp.getGender());
            for (Department d : depts)
                if (d.getDeptId() == emp.getDeptId()) { cbDept.setValue(d.getDeptId() + " – " + d.getDeptName()); break; }
        }

        GridPane g = grid();
        addRow(g, 0, "Name *",       tfName);
        addRow(g, 1, "Age *",        tfAge);
        addRow(g, 2, "Gender",       cbGender);
        addRow(g, 3, "Email *",      tfEmail);
        addRow(g, 4, "Phone",        tfPhone);
        addRow(g, 5, "Address",      tfAddr);
        addRow(g, 6, "Designation *",tfDesig);
        addRow(g, 7, "Hire Date",    tfHire);
        addRow(g, 8, "Department",   cbDept);

        VBox body = new VBox(12, title, new Separator(), g);
        body.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(body);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText(emp == null ? "Add Employee" : "Save Changes");
        ok.setStyle("-fx-background-color:" + MainApp.ACCENT + ";-fx-text-fill:white;-fx-background-radius:6;");

        dlg.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            try {
                if (tfName.getText().isBlank() || tfAge.getText().isBlank() ||
                    tfEmail.getText().isBlank() || tfDesig.getText().isBlank()) {
                    MainApp.alert(Alert.AlertType.WARNING, "Validation", "Please fill all required (*) fields."); return;
                }
                int age; try { age = Integer.parseInt(tfAge.getText().trim()); }
                catch (NumberFormatException ex) { MainApp.alert(Alert.AlertType.ERROR, "Input", "Age must be a number."); return; }

                int deptId = 0;
                if (cbDept.getValue() != null) deptId = Integer.parseInt(cbDept.getValue().split(" – ")[0]);

                if (emp == null) {
                    Employee e = new Employee(tfName.getText().trim(), age,
                        cbGender.getValue() != null ? cbGender.getValue() : "",
                        tfEmail.getText().trim(), tfPhone.getText().trim(),
                        tfAddr.getText().trim(), tfDesig.getText().trim(),
                        tfHire.getText().trim(), deptId);
                    ops.addEmployee(e);
                } else {
                    Employee e = new Employee(emp.getEmpId(), tfName.getText().trim(), age,
                        cbGender.getValue() != null ? cbGender.getValue() : emp.getGender(),
                        emp.getEmail(), tfPhone.getText().trim(),
                        tfAddr.getText().trim(), tfDesig.getText().trim(),
                        tfHire.getText().trim(), deptId);
                    ops.updateEmployee(e);
                }
                loadAll();
            } catch (DuplicateRecordException ex) {
                MainApp.alert(Alert.AlertType.ERROR, "Duplicate", ex.getMessage());
            } catch (DatabaseException ex) {
                MainApp.alert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
            }
        });
    }

    private void deleteEmp(Employee emp) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + emp.getEmpName() + "? This cannot be undone.", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            try { ops.deleteEmployee(emp.getEmpId()); loadAll(); }
            catch (DatabaseException ex) { MainApp.alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private void loadAll() {
        try { data.setAll(ops.getAllEmployees()); }
        catch (DatabaseException e) { MainApp.alert(Alert.AlertType.ERROR, "DB Error", e.getMessage()); }
    }

    private void doSearch(String kw) {
        if (kw.isEmpty()) { loadAll(); return; }
        try {
            ArrayList<Employee> all = ops.getAllEmployees();
            String k = kw.toLowerCase();
            data.setAll(all.stream().filter(e ->
                e.getEmpName().toLowerCase().contains(k) ||
                e.getEmail().toLowerCase().contains(k) ||
                e.getDesignation().toLowerCase().contains(k)
            ).toList());
        } catch (DatabaseException e) { MainApp.alert(Alert.AlertType.ERROR, "DB Error", e.getMessage()); }
    }

    private GridPane grid() {
        GridPane g = new GridPane(); g.setHgap(14); g.setVgap(13);
        g.setPadding(new Insets(14, 0, 0, 0));
        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        g.add(MainApp.formLabel(label), 0, row);
        g.add(field, 1, row);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
    }
}
