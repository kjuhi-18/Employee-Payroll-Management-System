import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    // Static data members — shared across all uses (no object needed)
    private static final String URL      = "jdbc:mysql://localhost:3306/payroll_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "kavan396001"; 

    private static Connection conn = null;
    private static boolean schemaInitialized = false;

    public static Connection getConnection() throws DatabaseException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                initializeSchema(conn);
                System.out.println("Connected to database successfully.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to database. " + e.getMessage());
        }
        return conn;
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        if (schemaInitialized) {
            return;
        }

        try (java.sql.Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Department (" +
                "dept_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "dept_name VARCHAR(100) NOT NULL, " +
                "location VARCHAR(100), " +
                "manager_name VARCHAR(100), " +
                "budget DOUBLE DEFAULT 0" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Employee (" +
                "emp_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "emp_name VARCHAR(100) NOT NULL, " +
                "age INT, " +
                "gender VARCHAR(20), " +
                "email VARCHAR(120) UNIQUE, " +
                "phone VARCHAR(30), " +
                "address VARCHAR(255), " +
                "designation VARCHAR(100), " +
                "hire_date VARCHAR(20), " +
                "dept_id INT, " +
                "CONSTRAINT fk_employee_department FOREIGN KEY (dept_id) REFERENCES Department(dept_id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Salary (" +
                "salary_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "emp_id INT NOT NULL, " +
                "basic_pay DOUBLE, " +
                "hra DOUBLE, " +
                "da DOUBLE, " +
                "bonus DOUBLE, " +
                "tax DOUBLE, " +
                "deductions DOUBLE, " +
                "CONSTRAINT fk_salary_employee FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Payroll (" +
                "payroll_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "emp_id INT NOT NULL, " +
                "salary_id INT NOT NULL, " +
                "working_days INT, " +
                "overtime_hours INT, " +
                "pay_month VARCHAR(20), " +
                "pay_year INT, " +
                "net_salary DOUBLE, " +
                "payment_method VARCHAR(50), " +
                "payment_date VARCHAR(20), " +
                "CONSTRAINT fk_payroll_employee FOREIGN KEY (emp_id) REFERENCES Employee(emp_id), " +
                "CONSTRAINT fk_payroll_salary FOREIGN KEY (salary_id) REFERENCES Salary(salary_id)" +
                ")"
            );
        }

        schemaInitialized = true;
    }
}
