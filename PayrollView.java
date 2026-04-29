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

public class PayrollView {

    private final PayrollOperations  ops     = new PayrollOperations();
    private final EmployeeOperations empOps  = new EmployeeOperations();
    private final SalaryOperations   salOps  = new SalaryOperations();

    private TableView<Payroll>      table;
    private ObservableList<Payroll> data = FXCollections.observableArrayList();
    private ArrayList<Employee>     employees;

    // KPI labels
    private Label lblTotal, lblCount, lblAvg;

    public VBox build() {
        try { employees = empOps.getAllEmployees(); }
        catch (DatabaseException e) { employees = new ArrayList<>(); }

        VBox view = new VBox(22);
        view.setMaxWidth(Double.MAX_VALUE); view.setMaxHeight(Double.MAX_VALUE);

        // Header
        HBox header = new HBox(16); header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4, MainApp.pageTitle("Payroll Processing"), MainApp.pageSub("Process monthly payroll and view disbursement history"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnReport = MainApp.outlineBtn("📊 Full Report");
        Button btnProcess = MainApp.primaryBtn("+ Process Payroll");
        btnProcess.setOnAction(e -> showForm());
        btnReport.setOnAction(e -> showReport());
        header.getChildren().addAll(titles, sp, btnReport, btnProcess);

        // KPI cards
        HBox kpis = buildKpis();

        // Filter bar
        HBox filterBar = new HBox(10); filterBar.setAlignment(Pos.CENTER_LEFT);
        Label fLbl = new Label("Filter by Employee:");
        fLbl.setFont(Font.font("Arial", 13)); fLbl.setTextFill(Color.web(MainApp.TXT_MID));
        ComboBox<String> cbFilter = MainApp.combo("All Employees");
        for (Employee e : employees) cbFilter.getItems().add(e.getEmpId() + " – " + e.getEmpName());
        cbFilter.setPrefWidth(260);
        cbFilter.setOnAction(e -> {
            String v = cbFilter.getValue();
            if (v == null || v.equals("All Employees")) loadAll();
            else {
                try {
                    int eid = Integer.parseInt(v.split(" – ")[0]);
                    data.setAll(ops.getPayrollByEmployee(eid));
                    refreshKpis();
                } catch (DatabaseException ex) { MainApp.alert(Alert.AlertType.ERROR,"DB Error",ex.getMessage()); }
            }
        });
        Button btnReset = MainApp.outlineBtn("↺ Reset");
        btnReset.setOnAction(e -> { cbFilter.setValue(null); loadAll(); });
        filterBar.getChildren().addAll(fLbl, cbFilter, btnReset);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setItems(data);
        loadAll();

        view.getChildren().addAll(header, kpis, filterBar, table);
        return view;
    }

    // ── KPI strip ─────────────────────────────────────────────────────────────
    private HBox buildKpis() {
        lblTotal = kpiVal("—"); lblCount = kpiVal("—"); lblAvg = kpiVal("—");
        HBox bar = new HBox(16,
            kpiCard("💸  Total Disbursed",    lblTotal),
            kpiCard("👥  Payroll Records",    lblCount),
            kpiCard("📊  Average Net Salary", lblAvg)
        );
        return bar;
    }
    private Label kpiVal(String t) {
        Label l = new Label(t); l.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        l.setTextFill(Color.web(MainApp.TXT_DARK)); return l;
    }
    private VBox kpiCard(String title, Label val) {
        Label t = new Label(title); t.setFont(Font.font("Arial",11)); t.setTextFill(Color.web(MainApp.TXT_MID));
        VBox c = new VBox(5, val, t);
        c.setPadding(new Insets(18, 26, 18, 26));
        c.setStyle("-fx-background-color:white;-fx-border-color:" + MainApp.BORDER + ";-fx-border-radius:10;-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(26,39,68,0.07),12,0,0,2);");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }
    private void refreshKpis() {
        if (data.isEmpty()) { lblTotal.setText("₹ 0"); lblCount.setText("0"); lblAvg.setText("—"); return; }
        double total = data.stream().mapToDouble(Payroll::getNetSalary).sum();
        lblTotal.setText(String.format("₹ %,.0f", total));
        lblCount.setText(String.valueOf(data.size()));
        lblAvg.setText(String.format("₹ %,.0f", total / data.size()));
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<Payroll> buildTable() {
        TableView<Payroll> tv = (TableView<Payroll>) MainApp.styledTable();
        tv.setRowFactory(r -> {
            TableRow<Payroll> row = new TableRow<>();
            row.selectedProperty().addListener((obs, o, n) ->
                row.setStyle(n ? "-fx-background-color:" + MainApp.SEL_ROW + ";" : ""));
            return row;
        });
        tv.getColumns().addAll(
            col("Pay ID",   p -> String.valueOf(p.getPayrollId()),  65),
            col("Emp ID",   p -> String.valueOf(p.getEmpId()),       65),
            col("Month",    Payroll::getPayMonth,                    85),
            col("Year",     p -> String.valueOf(p.getPayYear()),     60),
            col("Work Days",p -> String.valueOf(p.getWorkingDays()), 75),
            col("OT Hrs",   p -> String.valueOf(p.getOvertimeHours()),65),
            col("Net Salary",p -> String.format("₹ %,.0f", p.getNetSalary()), 120),
            col("Method",   Payroll::getPaymentMethod,              110),
            col("Pay Date", Payroll::getPaymentDate,                100),
            deleteCol()
        );
        return tv;
    }
    private TableColumn<Payroll, String> col(String title,
            java.util.function.Function<Payroll, String> fn, double min) {
        TableColumn<Payroll, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        c.setMinWidth(min); return c;
    }
    private TableColumn<Payroll, String> deleteCol() {
        TableColumn<Payroll, String> c = new TableColumn<>("Action");
        c.setMinWidth(90); c.setMaxWidth(90);
        c.setCellFactory(col -> new TableCell<>() {
            final Button del = MainApp.dangerBtn("Delete");
            { del.setOnAction(e -> {
                Payroll p = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete payroll record #" + p.getPayrollId() + "?", ButtonType.OK, ButtonType.CANCEL);
                confirm.setTitle("Confirm");
                confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) {
                    // No delete method in existing ops — use direct DB via ops pattern
                    MainApp.alert(Alert.AlertType.INFORMATION, "Info", "Delete not implemented in PayrollOperations. Add a deletePayroll(id) method to PayrollOperations to enable this.");
                }});
            }); }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : del);
            }
        });
        return c;
    }

    // ── Process Payroll Form ──────────────────────────────────────────────────
    private void showForm() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Process Monthly Payroll");
        dlg.getDialogPane().setPrefWidth(500);

        Label title = new Label("New Payroll Entry");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(MainApp.TXT_DARK));

        ComboBox<String> cbEmp = MainApp.combo("Select Employee");
        for (Employee e : employees) cbEmp.getItems().add(e.getEmpId() + " – " + e.getEmpName());

        TextField tfDays = MainApp.field("Working Days (e.g. 26)");
        TextField tfOT   = MainApp.field("Overtime Hours (e.g. 4)");
        ComboBox<String> cbMonth = MainApp.combo("Pay Month",
            "January","February","March","April","May","June",
            "July","August","September","October","November","December");
        TextField tfYear  = MainApp.field("Year (e.g. 2026)");
        tfYear.setText("2026");
        ComboBox<String> cbMethod = MainApp.combo("Payment Method",
            "Bank Transfer","Cash","UPI","Cheque");
        TextField tfDate  = MainApp.field("Payment Date (YYYY-MM-DD)");

        // Auto net salary preview when employee is selected
        Label lblSalary = new Label("Salary preview: select an employee");
        lblSalary.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        lblSalary.setTextFill(Color.web(MainApp.TXT_MID));

        final int[] salaryIdHolder = {0};
        final double[] netHolder = {0};

        cbEmp.setOnAction(e -> {
            if (cbEmp.getValue() == null) return;
            int eid = Integer.parseInt(cbEmp.getValue().split(" – ")[0]);
            try {
                Salary s = salOps.getSalaryByEmpId(eid);
                if (s != null) {
                    salaryIdHolder[0] = s.getSalaryId();
                    netHolder[0] = s.getNetPay();
                    lblSalary.setText(String.format("Net Pay from Salary record: ₹ %,.0f", s.getNetPay()));
                    lblSalary.setTextFill(Color.web(MainApp.SUCCESS));
                } else {
                    salaryIdHolder[0] = 0; netHolder[0] = 0;
                    lblSalary.setText("⚠  No salary record for this employee.");
                    lblSalary.setTextFill(Color.web(MainApp.DANGER));
                }
            } catch (DatabaseException ex) { lblSalary.setText("Error: " + ex.getMessage()); }
        });

        GridPane g = new GridPane(); g.setHgap(14); g.setVgap(13);
        g.setPadding(new Insets(14,0,0,0));
        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);

        addRow(g, 0, "Employee *",    cbEmp);
        addRow(g, 1, "Working Days *",tfDays);
        addRow(g, 2, "Overtime Hrs",  tfOT);
        addRow(g, 3, "Month *",       cbMonth);
        addRow(g, 4, "Year *",        tfYear);
        addRow(g, 5, "Method *",      cbMethod);
        addRow(g, 6, "Pay Date",      tfDate);

        VBox body = new VBox(12, title, new Separator(), g, new Separator(), lblSalary);
        body.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(body);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Process Payroll");
        ok.setStyle("-fx-background-color:" + MainApp.ACCENT + ";-fx-text-fill:white;-fx-background-radius:6;");

        dlg.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;
            try {
                if (cbEmp.getValue() == null || cbMonth.getValue() == null || cbMethod.getValue() == null) {
                    MainApp.alert(Alert.AlertType.WARNING,"Validation","Please fill all required fields."); return;
                }
                if (salaryIdHolder[0] == 0) {
                    MainApp.alert(Alert.AlertType.ERROR,"No Salary","Assign a salary to this employee first."); return;
                }
                int eid  = Integer.parseInt(cbEmp.getValue().split(" – ")[0]);
                int days = Integer.parseInt(tfDays.getText().trim());
                int ot   = tfOT.getText().isBlank() ? 0 : Integer.parseInt(tfOT.getText().trim());
                int year = Integer.parseInt(tfYear.getText().trim());

                Payroll p = new Payroll(eid, salaryIdHolder[0], days, ot,
                    cbMonth.getValue(), year, netHolder[0],
                    cbMethod.getValue(), tfDate.getText().trim());

                ops.addPayrollWithTransaction(p); // uses your transaction method
                loadAll();
                MainApp.alert(Alert.AlertType.INFORMATION,"Success",
                    "Payroll processed! ID = " + p.getPayrollId());
            } catch (NumberFormatException ex) {
                MainApp.alert(Alert.AlertType.ERROR,"Input Error","Working days and year must be numbers.");
            } catch (DatabaseException ex) {
                MainApp.alert(Alert.AlertType.ERROR,"DB Error", ex.getMessage());
            }
        });
    }

    // ── Full report dialog ────────────────────────────────────────────────────
    private void showReport() {
        // Reuses printFullPayrollReport logic but shows in a TextArea
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Full Payroll Report");
        dlg.getDialogPane().setPrefWidth(900); dlg.getDialogPane().setPrefHeight(550);

        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setFont(Font.font("Courier New", 12));
        ta.setStyle("-fx-background-color:white; -fx-border-color:" + MainApp.BORDER + ";");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-16s %-12s %-18s %-10s %-5s %-4s %-12s %-12s %-12s%n",
            "ID","Name","Department","Designation","BasicPay","Days","OT","NetSalary","Method","PaidOn"));
        sb.append("=".repeat(110)).append("\n");
        try {
            // We'll pull data from our already-loaded list and supplement with employee info
            for (Payroll p : data) {
                String empName = "—";
                for (Employee e : employees) if (e.getEmpId() == p.getEmpId()) { empName = e.getEmpName(); break; }
                sb.append(String.format("%-5d %-16s %-12s %-18s %-10s %-5d %-4d %-12.0f %-12s %-12s  [%s %d]%n",
                    p.getPayrollId(), empName, "—", "—", "—",
                    p.getWorkingDays(), p.getOvertimeHours(),
                    p.getNetSalary(), p.getPaymentMethod(), p.getPaymentDate(),
                    p.getPayMonth(), p.getPayYear()));
            }
        } catch (Exception e) { sb.append("Error generating report: ").append(e.getMessage()); }
        ta.setText(sb.toString());

        VBox content = new VBox(12,
            new Label("Full Payroll Report — All Records") {{ setFont(Font.font("Georgia", FontWeight.BOLD, 16)); setTextFill(Color.web(MainApp.TXT_DARK)); }},
            ta
        );
        VBox.setVgrow(ta, Priority.ALWAYS);
        content.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    private void loadAll() {
        try { data.setAll(ops.getPayrollByEmployee(0)); } // 0 = won't match; use getAllPayrolls workaround below
        catch (DatabaseException ignored) {}
        // getAllPayrolls not in original ops — pull all employees' payroll
        try {
            ArrayList<Payroll> all = new ArrayList<>();
            for (Employee e : employees) all.addAll(ops.getPayrollByEmployee(e.getEmpId()));
            data.setAll(all);
            refreshKpis();
        } catch (DatabaseException e) { MainApp.alert(Alert.AlertType.ERROR,"DB Error",e.getMessage()); }
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        g.add(MainApp.formLabel(label), 0, row);
        g.add(field, 1, row);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
    }
}
