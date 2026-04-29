import mysql.connector

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="kavan396001",
    database="payroll_db"
)
cursor = conn.cursor()

# ── Step 1: Clear all tables in reverse dependency order ──────────────────────
print("Clearing existing data...")
cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
for table in ["Payroll", "Salary", "Employee", "Department"]:
    cursor.execute(f"TRUNCATE TABLE {table}")
    print(f"  ✓ {table} cleared")
cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

# ── Step 2: Departments (6 departments) ───────────────────────────────────────
departments = [
    ("HR",          "Pune",      "Amit Sharma",  500000),
    ("IT",          "Mumbai",    "Neha Verma",  1200000),
    ("Finance",     "Delhi",     "Raj Mehta",    800000),
    ("Sales",       "Bangalore", "Priya Singh",  900000),
    ("Marketing",   "Hyderabad", "Karan Patel",  700000),
    ("Operations",  "Chennai",   "Suresh Nair",  600000),
]
cursor.executemany("""
    INSERT INTO Department (dept_name, location, manager_name, budget)
    VALUES (%s, %s, %s, %s)
""", departments)
print(f"\nInserted {cursor.rowcount} departments")

# ── Step 3: Employees (12 employees) ─────────────────────────────────────────
employees = [
    ("Rahul Jain",      25, "Male",   "rahul.jain@company.com",    "9876543210", "Pune",      "HR Executive",         "2022-01-10", 1),
    ("Sneha Kapoor",    28, "Female", "sneha.kapoor@company.com",  "9876543211", "Mumbai",    "Software Engineer",    "2021-03-15", 2),
    ("Arjun Mehta",     30, "Male",   "arjun.mehta@company.com",   "9876543212", "Delhi",     "Senior Accountant",    "2020-07-20", 3),
    ("Riya Sharma",     27, "Female", "riya.sharma@company.com",   "9876543213", "Bangalore", "Sales Manager",        "2019-11-05", 4),
    ("Vikas Gupta",     29, "Male",   "vikas.gupta@company.com",   "9876543214", "Hyderabad", "Marketing Lead",       "2023-02-01", 5),
    ("Pooja Nair",      26, "Female", "pooja.nair@company.com",    "9876543215", "Chennai",   "Operations Analyst",   "2022-06-15", 6),
    ("Deepak Rao",      32, "Male",   "deepak.rao@company.com",    "9876543216", "Mumbai",    "Tech Lead",            "2018-09-10", 2),
    ("Anita Desai",     31, "Female", "anita.desai@company.com",   "9876543217", "Delhi",     "Finance Manager",      "2017-05-20", 3),
    ("Kiran Malhotra",  24, "Male",   "kiran.malhotra@company.com","9876543218", "Pune",      "HR Assistant",         "2023-07-01", 1),
    ("Sunita Pillai",   33, "Female", "sunita.pillai@company.com", "9876543219", "Bangalore", "Sales Executive",      "2016-12-12", 4),
    ("Rohan Kumar",     27, "Male",   "rohan.kumar@company.com",   "9876543220", "Hyderabad", "Digital Marketer",     "2024-01-20", 5),
    ("Meera Iyer",      29, "Female", "meera.iyer@company.com",    "9876543221", "Chennai",   "Logistics Coordinator","2021-08-05", 6),
]
cursor.executemany("""
    INSERT INTO Employee (emp_name, age, gender, email, phone, address, designation, hire_date, dept_id)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
""", employees)
print(f"Inserted {cursor.rowcount} employees")

# ── Step 4: Salary records (one per employee, emp_id 1..12) ──────────────────
salaries = [
    (1,  30000,  5000, 3000, 2000, 2500,  1000),   # Rahul
    (2,  60000, 10000, 5000, 5000, 7000,  2000),   # Sneha
    (3,  40000,  7000, 4000, 3000, 4000,  1500),   # Arjun
    (4,  55000,  9000, 4500, 4000, 6000,  2000),   # Riya
    (5,  50000,  8000, 4200, 3500, 5500,  1800),   # Vikas
    (6,  38000,  6500, 3800, 2500, 3800,  1200),   # Pooja
    (7,  75000, 12000, 6000, 7000, 9000,  2500),   # Deepak
    (8,  70000, 11000, 5800, 6500, 8500,  2400),   # Anita
    (9,  28000,  4500, 2800, 1500, 2200,   900),   # Kiran
    (10, 48000,  8000, 4000, 3200, 5000,  1600),   # Sunita
    (11, 42000,  7000, 3500, 2800, 4200,  1400),   # Rohan
    (12, 36000,  6000, 3200, 2200, 3500,  1100),   # Meera
]
cursor.executemany("""
    INSERT INTO Salary (emp_id, basic_pay, hra, da, bonus, tax, deductions)
    VALUES (%s, %s, %s, %s, %s, %s, %s)
""", salaries)
print(f"Inserted {cursor.rowcount} salary records")

# ── Step 5: Payroll records (both Feb & March 2026 for all 12 employees) ─────
# net_salary = basic_pay + hra + da + bonus - tax - deductions
payroll = [
    # March 2026
    (1,  1,  26, 5, "March", 2026, 36500, "Bank Transfer", "2026-03-31"),
    (2,  2,  25, 3, "March", 2026, 71000, "Bank Transfer", "2026-03-31"),
    (3,  3,  26, 2, "March", 2026, 48500, "Cash",          "2026-03-31"),
    (4,  4,  24, 6, "March", 2026, 64500, "Bank Transfer", "2026-03-31"),
    (5,  5,  26, 4, "March", 2026, 59900, "UPI",           "2026-03-31"),
    (6,  6,  26, 2, "March", 2026, 45800, "Bank Transfer", "2026-03-31"),
    (7,  7,  25, 8, "March", 2026, 88500, "Bank Transfer", "2026-03-31"),
    (8,  8,  26, 5, "March", 2026, 82400, "Bank Transfer", "2026-03-31"),
    (9,  9,  26, 0, "March", 2026, 33700, "UPI",           "2026-03-31"),
    (10, 10, 24, 3, "March", 2026, 56600, "Cash",          "2026-03-31"),
    (11, 11, 26, 2, "March", 2026, 49700, "Bank Transfer", "2026-03-31"),
    (12, 12, 26, 1, "March", 2026, 42800, "UPI",           "2026-03-31"),
    # February 2026
    (1,  1,  24, 3, "February", 2026, 35800, "Bank Transfer", "2026-02-28"),
    (2,  2,  26, 2, "February", 2026, 70500, "Bank Transfer", "2026-02-28"),
    (3,  3,  25, 0, "February", 2026, 47500, "Cash",          "2026-02-28"),
    (4,  4,  26, 4, "February", 2026, 64000, "Bank Transfer", "2026-02-28"),
    (5,  5,  25, 3, "February", 2026, 58900, "UPI",           "2026-02-28"),
    (6,  6,  24, 1, "February", 2026, 45000, "Bank Transfer", "2026-02-28"),
    (7,  7,  26, 6, "February", 2026, 87000, "Bank Transfer", "2026-02-28"),
    (8,  8,  25, 4, "February", 2026, 81500, "Bank Transfer", "2026-02-28"),
    (9,  9,  25, 0, "February", 2026, 33000, "UPI",           "2026-02-28"),
    (10, 10, 26, 2, "February", 2026, 55800, "Cash",          "2026-02-28"),
    (11, 11, 25, 1, "February", 2026, 49000, "Bank Transfer", "2026-02-28"),
    (12, 12, 24, 0, "February", 2026, 41500, "UPI",           "2026-02-28"),
]
cursor.executemany("""
    INSERT INTO Payroll (emp_id, salary_id, working_days, overtime_hours,
                         pay_month, pay_year, net_salary, payment_method, payment_date)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
""", payroll)
print(f"Inserted {cursor.rowcount} payroll records")

conn.commit()
cursor.close()
conn.close()

print("\n✅ Done! Summary:")
print(f"   Departments : {len(departments)}")
print(f"   Employees   : {len(employees)}")
print(f"   Salary rows : {len(salaries)}")
print(f"   Payroll rows: {len(payroll)}")
