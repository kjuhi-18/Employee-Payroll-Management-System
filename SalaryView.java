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

public class SalaryView {

    private final SalaryOperations   ops    = new SalaryOperations();
    private final EmployeeOperations empOps = new EmployeeOperations();

    private TableView<Salary>      table;
    private ObservableList<Salary> data = FXCollections.observableArrayList();
    private ArrayList<Employee>    employees;

    public VBox build() {
        try { employees = empOps.getAllEmployees(); }
        catch (DatabaseException e) { employees = new ArrayList<>(); }

        VBox view = new VBox(22);
        view.setMaxWidth(Double.MAX_VALUE); view.setMaxHeight(Double.MAX_VALUE);

        HBox header = new HBox(16); header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4, MainApp.pageTitle("Salary Records"), MainApp.pageSub("Assign and update employee compensation structures"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnAdd = MainApp.primaryBtn("+ Assign Salary");
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
    private TableView<Salary> buildTable() {
        TableView<Salary> tv = (TableView<Salary>) MainApp.styledTable();
        tv.setRowFactory(r -> {
            TableRow<Salary> row = new TableRow<>();
            row.selectedProperty().addListener((obs, o, n) ->
                row.setStyle(n ? "-fx-background-color:" + MainApp.SEL_ROW + ";" : ""));
            return row;
        });
        tv.getColumns().addAll(
            col("Sal ID",     s -> String.valueOf(s.getSalaryId()),    65),
            col("Emp ID",     s -> String.valueOf(s.getEmpId()),        65),
            col("Basic Pay",  s -> fmt(s.getBasicPay()),               110),
            col("HRA",        s -> fmt(s.getHra()),                     90),
            col("DA",         s -> fmt(s.getDa()),                      90),
            col("Bonus",      s -> fmt(s.getBonus()),                   90),
            col("Tax",        s -> fmt(s.getTax()),                     90),
            col("Deductions", s -> fmt(s.getDeductions()),             110),
            col("Gross Pay",  s -> fmt(s.getGrossPay()),               115),
            col("Net Pay",    s -> fmt(s.getNetPay()),                  115),
            actionsCol()
        );
        return tv;
    }

    private String fmt(double v) { return String.format("₹ %,.0f", v); }

    private TableColumn<Salary, String> col(String title,
            java.util.function.Function<Salary, String> fn, double min) {
        TableColumn<Salary, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        c.setMinWidth(min);
        return c;
    }

    private TableColumn<Salary, String> actionsCol() {
        TableColumn<Salary, String> c = new TableColumn<>("Action");
        c.setMinWidth(90); c.setMaxWidth(90);
        c.setCellFactory(col -> new TableCell<>() {
            final Button edit = MainApp.editBtn("Edit");
            { edit.setOnAction(e -> showForm(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : edit);
            }
        });
        return c;
    }

    private void showForm(Salary salary) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(salary == null ? "Assign Salary" : "Edit Salary");
        dlg.getDialogPane().setPrefWidth(500);

        boolean isEdit = salary != null;
        Label title = new Label(isEdit ? "Edit Salary Record" : "Assign Salary to Employee");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(MainApp.TXT_DARK));

        ComboBox<String> cbEmp = MainApp.combo("Select Employee");
        for (Employee e : employees) cbEmp.getItems().add(e.getEmpId() + " – " + e.getEmpName());
        if (isEdit) { cbEmp.setValue(salary.getEmpId() + " – (emp)"); cbEmp.setDisable(true); }

        TextField tfBasic = MainApp.field("Basic Pay");
        TextField tfHRA   = MainApp.field("HRA");
        TextField tfDA    = MainApp.field("DA");
        TextField tfBonus = MainApp.field("Bonus");
        TextField tfTax   = MainApp.field("Tax");
        TextField tfDed   = MainApp.field("Other Deductions");

        // Live net pay preview
        Label lblNet = new Label("Net Pay: —");
        lblNet.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        lblNet.setTextFill(Color.web(MainApp.SUCCESS));

        Runnable recalc = () -> {
            try {
                double net = parse(tfBasic) + parse(tfHRA) + parse(tfDA) + parse(tfBonus)
                             - parse(tfTax) - parse(tfDed);
                lblNet.setText("Net Pay: ₹ " + String.format("%,.0f", net));
            } catch (Exception ignored) { lblNet.setText("Net Pay: —"); }
        };
        for (TextField tf : new TextField[]{tfBasic,tfHRA,tfDA,tfBonus,tfTax,tfDed})
            tf.textProperty().addListener((obs,o,n) -> recalc.run());

        if (isEdit) {
            tfBasic.setText(String.valueOf(salary.getBasicPay()));
            tfHRA.setText(String.valueOf(salary.getHra()));
            tfDA.setText(String.valueOf(salary.getDa()));
            tfBonus.setText(String.valueOf(salary.getBonus()));
            tfTax.setText(String.valueOf(salary.getTax()));
            tfDed.setText(String.valueOf(salary.getDeductions()));
            recalc.run();
        }

        GridPane g = new GridPane(); g.setHgap(14); g.setVgap(13);
        g.setPadding(new Insets(14,0,0,0));
        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);

        addRow(g, 0, "Employee *",  cbEmp);
        addRow(g, 1, "Basic Pay *", tfBasic);
        addRow(g, 2, "HRA",         tfHRA);
        addRow(g, 3, "DA",          tfDA);
        addRow(g, 4, "Bonus",       tfBonus);
        addRow(g, 5, "Tax",         tfTax);
        addRow(g, 6, "Deductions",  tfDed);

        VBox body = new VBox(12, title, new Separator(), g, new Separator(), lblNet);
        body.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(body);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText(isEdit ? "Save Changes" : "Assign Salary");
        ok.setStyle("-fx-background-color:" + MainApp.ACCENT + ";-fx-text-fill:white;-fx-background-radius:6;");

        dlg.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            try {
                int empId;
                if (isEdit) { empId = salary.getEmpId(); }
                else {
                    if (cbEmp.getValue() == null) { MainApp.alert(Alert.AlertType.WARNING,"Validation","Select an employee."); return; }
                    empId = Integer.parseInt(cbEmp.getValue().split(" – ")[0]);
                }
                Salary s = new Salary(salary != null ? salary.getSalaryId() : 0, empId,
                    parse(tfBasic), parse(tfHRA), parse(tfDA), parse(tfBonus), parse(tfTax), parse(tfDed));
                if (isEdit) ops.updateSalary(s);
                else ops.addSalary(s);
                loadAll();
            } catch (NumberFormatException ex) {
                MainApp.alert(Alert.AlertType.ERROR,"Input Error","All salary fields must be valid numbers.");
            } catch (DatabaseException ex) {
                MainApp.alert(Alert.AlertType.ERROR,"DB Error", ex.getMessage());
            }
        });
    }

    private double parse(TextField tf) { return tf.getText().isBlank() ? 0 : Double.parseDouble(tf.getText().trim()); }

    private void loadAll() {
        try { data.setAll(ops.getAllSalaries()); }
        catch (DatabaseException e) { MainApp.alert(Alert.AlertType.ERROR,"DB Error",e.getMessage()); }
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        g.add(MainApp.formLabel(label), 0, row);
        g.add(field, 1, row);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
    }
}
