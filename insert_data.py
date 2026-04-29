import mysql.connector

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="kavan396001",
    database="payroll_db"
)

cursor = conn.cursor()

cursor.executemany("""
INSERT INTO Department (dept_name, location, manager_name, budget)
VALUES (%s, %s, %s, %s)
""", [
    ("HR", "Pune", "Amit Sharma", 500000),
    ("IT", "Mumbai", "Neha Verma", 1200000),
    ("Finance", "Delhi", "Raj Mehta", 800000),
    ("Sales", "Bangalore", "Priya Singh", 900000),
    ("Marketing", "Hyderabad", "Karan Patel", 700000)
])

cursor.executemany("""
INSERT INTO Employee (emp_name, age, gender, email, phone, address, designation, hire_date, dept_id)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
""", [
    ("Rahul Jain", 25, "Male", "rahul@gmail.com", "9876543210", "Pune", "HR Executive", "2022-01-10", 1),
    ("Sneha Kapoor", 28, "Female", "sneha@gmail.com", "9876543211", "Mumbai", "Software Engineer", "2021-03-15", 2),
    ("Arjun Mehta", 30, "Male", "arjun@gmail.com", "9876543212", "Delhi", "Accountant", "2020-07-20", 3),
    ("Riya Sharma", 27, "Female", "riya@gmail.com", "9876543213", "Bangalore", "Sales Manager", "2019-11-05", 4),
    ("Vikas Gupta", 29, "Male", "vikas@gmail.com", "9876543214", "Hyderabad", "Marketing Lead", "2023-02-01", 5)
])

cursor.executemany("""
INSERT INTO Salary (emp_id, basic_pay, hra, da, bonus, tax, deductions)
VALUES (%s, %s, %s, %s, %s, %s, %s)
""", [
    (1, 30000, 5000, 3000, 2000, 2500, 1000),
    (2, 60000, 10000, 5000, 5000, 7000, 2000),
    (3, 40000, 7000, 4000, 3000, 4000, 1500),
    (4, 55000, 9000, 4500, 4000, 6000, 2000),
    (5, 50000, 8000, 4200, 3500, 5500, 1800)
])

cursor.executemany("""
INSERT INTO Payroll (emp_id, salary_id, working_days, overtime_hours, pay_month, pay_year, net_salary, payment_method, payment_date)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
""", [
    (1, 1, 26, 5, "March", 2026, 36500, "Bank Transfer", "2026-03-31"),
    (2, 2, 25, 3, "March", 2026, 71000, "Bank Transfer", "2026-03-31"),
    (3, 3, 26, 2, "March", 2026, 48500, "Cash", "2026-03-31"),
    (4, 4, 24, 6, "March", 2026, 64500, "Bank Transfer", "2026-03-31"),
    (5, 5, 26, 4, "March", 2026, 59900, "UPI", "2026-03-31")
])

conn.commit()
cursor.close()
conn.close()

print("Mock data successfully inserted into payroll_db!")
