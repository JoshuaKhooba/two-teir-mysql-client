# Two-Tier MySQL Client — Role-Based Database GUI

**Course:** CNT 4714 – Enterprise Computing | Spring 2026  
**Author:** Joshua Khooba  
**Language:** Java (Swing GUI + JDBC)

---

## Overview

This is a two-tier client-server application that connects a Java Swing front end directly to a MySQL database using JDBC. The app supports multiple user roles (root, client1, client2, and accountant), each with different database permissions. SQL commands are entered and executed through the GUI, with results displayed in a scrollable table. All queries are logged to an `operationslog` database table with user and timestamp metadata.

Key features:
- Role-based MySQL access (root, client, accountant)
- Dynamic connection via `.properties` files for DB URL and user credentials
- SQL query execution with live results table
- Operations log — every query is recorded with the executing user and timestamp
- Credentials mismatch detection with error feedback
- Separate `AccountantApp` interface using `CallableStatement` for stored procedure support

---

## Screenshots

### Root User — SQL Command Execution
![Root Command 1](Root%20Commands%20Screenshots/RootCommand1.png)

### Root User — Query Results
![Root Command 2](Root%20Commands%20Screenshots/RootCommand2.png)

### Client 1 — Query Execution
![Client1 Command 1](Client1%20Commands%20Screenshots/Client1Command1.png)

### Client 1 — Results View
![Client1 Command 2](Client1%20Commands%20Screenshots/Client1Command2A.png)

### Accountant — Operations Log View
![Accountant Log](Accountant-OperationsLog%20Screenshots/Screenshot%202026-03-15%20at%2012.39.53%20AM.png)

### Accountant — Log Detail
![Accountant Log 2](Accountant-OperationsLog%20Screenshots/Screenshot%202026-03-15%20at%2012.40.27%20AM.png)

### Credentials Mismatch Error
![Credentials Mismatch](Credentials%20Mismatch%20Screenshot/Mismatch.png)

---

## Project Structure

```
Project 3/
├── Source Folder/
│   ├── Project3App.java                  # Main client GUI application
│   ├── AccountantApp.java                # Accountant-specific GUI (CallableStatement)
│   ├── bikedbscript.sql                  # Creates and populates the bikedb database
│   ├── project3dbscript.sql              # Additional DB setup
│   ├── project3operationslog.sql         # Creates the operationslog table
│   ├── UserCreationScriptProject3.sql    # Creates MySQL users
│   ├── UserPermissionsScriptProject3.sql # Grants role-based permissions
│   ├── project3rootscript.sql            # Root user SQL commands
│   ├── project3client1script.sql         # Client1 SQL commands
│   ├── project3client2script.sql         # Client2 SQL commands
│   ├── bikedb.properties                 # DB connection URL
│   ├── root.properties                   # Root user credentials
│   ├── client1.properties                # Client1 credentials
│   ├── client2.properties                # Client2 credentials
│   ├── operationslog.properties          # Operationslog DB connection
│   └── mysql-connector-j-9.6.0.jar      # MySQL JDBC driver
├── Root Commands Screenshots/
├── Client1 Commands Screenshots/
├── Client2 Commands Screenshots/
├── Accountant-OperationsLog Screenshots/
└── Credentials Mismatch Screenshot/
```

---

## Requirements

- Java JDK 8 or higher
- MySQL Server (local instance)
- MySQL Connector/J (included: `mysql-connector-j-9.6.0.jar`)

---

## How to Download

```bash
git clone https://github.com/joshuakhooba/two-tier-mysql-client.git
cd two-tier-mysql-client
```

---

## Setup

### 1. Initialize the Database

Run the following SQL scripts in order in MySQL Workbench (or your MySQL client):

```
bikedbscript.sql                  → Creates the bikedb database and tables
project3dbscript.sql              → Additional schema setup
project3operationslog.sql         → Creates the operationslog table
UserCreationScriptProject3.sql    → Creates MySQL user accounts
UserPermissionsScriptProject3.sql → Grants permissions per role
```

### 2. Update `.properties` Files

Edit each `.properties` file in `Source Folder/` to match your MySQL setup:

| File                       | Purpose                          |
|----------------------------|----------------------------------|
| `bikedb.properties`        | JDBC URL for bikedb              |
| `root.properties`          | Root user login                  |
| `client1.properties`       | Client1 user login               |
| `client2.properties`       | Client2 user login               |
| `operationslog.properties` | JDBC URL for operationslog DB    |

Example entry:
```properties
db.url=jdbc:mysql://localhost:3306/bikedb
db.user=root
db.password=yourpassword
```

---

## How to Compile & Run

1. Navigate to the source folder:
   ```bash
   cd "Source Folder"
   ```

2. Compile with the JDBC driver on the classpath:
   ```bash
   javac -cp mysql-connector-j-9.6.0.jar *.java
   ```

3. Run the main client app:
   ```bash
   java -cp .:mysql-connector-j-9.6.0.jar Project3App
   ```
   > On Windows, replace `:` with `;` in the classpath.

4. To run the Accountant app:
   ```bash
   java -cp .:mysql-connector-j-9.6.0.jar AccountantApp
   ```

---

## How to Use

1. Select a **DB Properties** file (database URL) and a **User Properties** file (credentials) from the dropdowns.
2. Click **Connect** — the status indicator will turn green if successful, or show an error on mismatch.
3. Type a SQL query into the command area and click **Execute**.
4. Results appear in the table below. Every executed query is automatically logged to the `operationslog` table.
5. Click **Disconnect** when done, or **Clear** to reset the command/results area.
