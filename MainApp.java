import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    // ── Design tokens ─────────────────────────────────────────────────────────
    public static final String BG       = "#F5F2ED";
    public static final String SIDEBAR  = "#1A2744";
    public static final String ACCENT   = "#C8601A";
    public static final String WHITE    = "#cc7373";
    public static final String BORDER   = "#DDD8CF";
    public static final String TXT_DARK = "#1A2744";
    public static final String TXT_MID  = "#637085";
    public static final String SUCCESS  = "#2D7A4F";
    public static final String DANGER   = "#B83232";
    public static final String SEL_ROW  = "#FEF3E8";

    private StackPane content;
    private Button    activeBtn;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";");

        content = new StackPane();
        content.setPadding(new Insets(36, 40, 36, 40));

        root.setLeft(buildSidebar());
        root.setCenter(content);

        navigate("employees");

        Scene scene = new Scene(root, 1300, 800);
        stage.setTitle("Payroll Management System — SIT AIML B");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(660);
        stage.show();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(224);
        sb.setStyle("-fx-background-color:" + SIDEBAR + ";");

        // Brand block
        VBox brand = new VBox(5);
        brand.setPadding(new Insets(30, 22, 26, 22));
        brand.setStyle("-fx-border-color: transparent transparent rgba(255,255,255,0.08) transparent; -fx-border-width:0 0 1 0;");
        Label logo = new Label("PAYROLL");
        logo.setFont(Font.font("Georgia", FontWeight.BOLD, 21));
        logo.setTextFill(Color.WHITE);
        Label sub = new Label("Management System");
        sub.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        sub.setTextFill(Color.web("#7A8EA8"));
        Label inst = new Label("Symbiosis Institute of Technology");
        inst.setFont(Font.font("Arial", 10));
        inst.setTextFill(Color.web("#4A5E78"));
        inst.setWrapText(true);
        brand.getChildren().addAll(logo, sub, inst);

        // Nav
        VBox nav = new VBox(3);
        nav.setPadding(new Insets(18, 10, 18, 10));
        Label navHead = sectionLabel("MODULES");
        Button btnEmp  = navBtn("👤   Employees",   "employees");
        Button btnDept = navBtn("🏢   Departments",  "departments");
        Button btnSal  = navBtn("💰   Salary",       "salary");
        Button btnPay  = navBtn("📋   Payroll",      "payroll");
        nav.getChildren().addAll(navHead, btnEmp, btnDept, btnSal, btnPay);

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("AIML B  ·  2026");
        footer.setFont(Font.font("Arial", 10));
        footer.setTextFill(Color.web("#4A5E78"));
        footer.setPadding(new Insets(0, 0, 22, 22));

        sb.getChildren().addAll(brand, nav, spacer, footer);
        return sb;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        l.setTextFill(Color.web("#4A5E78"));
        l.setPadding(new Insets(10, 10, 5, 10));
        return l;
    }

    private Button navBtn(String text, String module) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", 13));
        btn.setPadding(new Insets(10, 14, 10, 14));
        btn.setCursor(Cursor.HAND);
        styleNav(btn, false);
        btn.setOnAction(e -> {
            if (activeBtn != null) styleNav(activeBtn, false);
            styleNav(btn, true);
            activeBtn = btn;
            navigate(module);
        });
        if (module.equals("employees")) { styleNav(btn, true); activeBtn = btn; }
        return btn;
    }

    private void styleNav(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color:" + ACCENT + ";-fx-text-fill:white;-fx-background-radius:8;-fx-border-radius:8;-fx-cursor:hand;");
        } else {
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#7A8EA8;-fx-background-radius:8;-fx-border-radius:8;-fx-cursor:hand;");
            btn.setOnMouseEntered(ev -> { if (!btn.getStyle().contains(ACCENT)) btn.setStyle(btn.getStyle().replace("transparent","rgba(255,255,255,0.06)")); });
            btn.setOnMouseExited(ev  -> { if (!btn.getStyle().contains(ACCENT)) btn.setStyle(btn.getStyle().replace("rgba(255,255,255,0.06)","transparent")); });
        }
    }

    private void navigate(String module) {
        content.getChildren().clear();
        switch (module) {
            case "employees"   -> content.getChildren().add(new EmployeeView().build());
            case "departments" -> content.getChildren().add(new DepartmentView().build());
            case "salary"      -> content.getChildren().add(new SalaryView().build());
            case "payroll"     -> content.getChildren().add(new PayrollView().build());
        }
    }

    // ── Shared UI factory methods (used by all views) ─────────────────────────
    public static Label pageTitle(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        l.setTextFill(Color.web(TXT_DARK));
        return l;
    }
    public static Label pageSub(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Arial", FontPosture.ITALIC, 13));
        l.setTextFill(Color.web(TXT_MID));
        return l;
    }
    public static Button primaryBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + ACCENT + ";-fx-text-fill:white;-fx-font-size:13;-fx-padding:9 22;-fx-background-radius:7;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setStyle(b.getStyle().replace(ACCENT,"#A84E15")));
        b.setOnMouseExited(e  -> b.setStyle(b.getStyle().replace("#A84E15",ACCENT)));
        return b;
    }
    public static Button outlineBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:transparent;-fx-border-color:" + BORDER + ";-fx-border-width:1.5;-fx-border-radius:7;-fx-background-radius:7;-fx-text-fill:" + TXT_DARK + ";-fx-font-size:13;-fx-padding:8 18;-fx-cursor:hand;");
        return b;
    }
    public static Button dangerBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + DANGER + ";-fx-text-fill:white;-fx-font-size:11;-fx-padding:5 12;-fx-background-radius:6;-fx-cursor:hand;");
        return b;
    }
    public static Button editBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + ACCENT + ";-fx-text-fill:white;-fx-font-size:11;-fx-padding:5 12;-fx-background-radius:6;-fx-cursor:hand;");
        return b;
    }
    public static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER + ";-fx-border-width:1.5;-fx-border-radius:7;-fx-background-radius:7;-fx-padding:8 12;-fx-font-size:13;-fx-text-fill:" + TXT_DARK + ";");
        return tf;
    }
    public static ComboBox<String> combo(String prompt, String... opts) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.getItems().addAll(opts);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER + ";-fx-border-width:1.5;-fx-border-radius:7;-fx-background-radius:7;-fx-font-size:13;");
        return cb;
    }
    public static Label formLabel(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web(TXT_MID));
        return l;
    }
    public static VBox card(javafx.scene.Node... nodes) {
        VBox c = new VBox(14);
        c.setPadding(new Insets(22));
        c.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER + ";-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(26,39,68,0.07),14,0,0,3);");
        c.getChildren().addAll(nodes);
        return c;
    }
    public static void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
    public static TableView<?> styledTable() {
        TableView<?> tv = new TableView<>();
        tv.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER + ";-fx-border-radius:10;-fx-background-radius:10;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return tv;
    }

    @Override
    public void stop() { DBConnection.closeConnection(); }

    public static void main(String[] args) { launch(args); }
}
