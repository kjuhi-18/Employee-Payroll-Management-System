import mysql.connector

conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="kavan396001",
    database="payroll_db"
)

cursor = conn.cursor()

tables = ['Department', 'Employee', 'Salary', 'Payroll']

for table in tables:
    try:
        cursor.execute(f"SELECT COUNT(*) FROM {table}")
        count = cursor.fetchone()[0]
        print(f"Table {table} has {count} records.")
    except Exception as e:
        print(f"Error querying {table}: {e}")

conn.close()
