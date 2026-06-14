/*
Name: Joshua Khooba
Course: CNT 4714 Spring 2026
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 15, 2026
Class: Project3App
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class Project3App extends JFrame {

    // ── Connection Details Panel ──────────────────────────────────────────────
    private JComboBox<String> dbPropertiesCombo;
    private JComboBox<String> userPropertiesCombo;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel connectionStatusLabel;

    // ── SQL Command Panel ─────────────────────────────────────────────────────
    private JTextArea sqlCommandArea;
    private JButton executeButton;
    private JButton clearCommandButton;

    // ── Results Panel ─────────────────────────────────────────────────────────
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JButton clearResultsButton;
    private JButton closeButton;

    // ── JDBC State ────────────────────────────────────────────────────────────
    private Connection userConnection = null;

    // Properties file for the background operations-log connection
    private static final String APP_PROPERTIES_FILE = "project3app.properties";

    // ─────────────────────────────────────────────────────────────────────────

    public Project3App() {
        setTitle("SQL CLIENT APPLICATION - (CNT 4714 - SPRING 2026 - PROJECT 3)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(Color.DARK_GRAY);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeApplication(); }
        });

        add(buildConnectionPanel(), BorderLayout.NORTH);
        add(buildCommandPanel(),    BorderLayout.CENTER);
        add(buildResultsPanel(),    BorderLayout.SOUTH);

        pack();
        setSize(900, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── GUI Builders ──────────────────────────────────────────────────────────

    private JPanel buildConnectionPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.DARK_GRAY);
        outer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.CYAN),
                "Connection Details",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.CYAN));

        // Left: labels + inputs
        JPanel left = new JPanel(new GridLayout(4, 2, 5, 5));
        left.setBackground(Color.DARK_GRAY);

        left.add(makeLabel("DB URL Properties"));
        dbPropertiesCombo = new JComboBox<>(new String[]{
                "project3.properties", "bikedb.properties"});
        styleCombo(dbPropertiesCombo);
        left.add(dbPropertiesCombo);

        left.add(makeLabel("User Properties"));
        userPropertiesCombo = new JComboBox<>(new String[]{
                "root.properties", "client1.properties", "client2.properties"});
        styleCombo(userPropertiesCombo);
        left.add(userPropertiesCombo);

        left.add(makeLabel("Username"));
        usernameField = new JTextField();
        usernameField.setBackground(Color.BLACK);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        left.add(usernameField);

        left.add(makeLabel("Password"));
        passwordField = new JPasswordField();
        passwordField.setBackground(Color.BLACK);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        left.add(passwordField);

        // Right: buttons + status
        JPanel right = new JPanel(new GridLayout(4, 1, 5, 5));
        right.setBackground(Color.DARK_GRAY);

        connectButton = new JButton("Connect to Database");
        connectButton.setBackground(Color.BLUE);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        connectButton.addActionListener(e -> connectToDatabase());

        disconnectButton = new JButton("Disconnect From Database");
        disconnectButton.setBackground(Color.RED);
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setFont(new Font("Arial", Font.BOLD, 12));
        disconnectButton.addActionListener(e -> disconnectFromDatabase());

        JLabel statusTitle = new JLabel("CONNECTION STATUS", SwingConstants.CENTER);
        statusTitle.setForeground(Color.WHITE);
        statusTitle.setFont(new Font("Arial", Font.BOLD, 11));

        connectionStatusLabel = new JLabel("NO CONNECTION ESTABLISHED", SwingConstants.CENTER);
        connectionStatusLabel.setBackground(Color.RED);
        connectionStatusLabel.setForeground(Color.BLACK);
        connectionStatusLabel.setFont(new Font("Arial", Font.BOLD, 11));
        connectionStatusLabel.setOpaque(true);

        right.add(connectButton);
        right.add(disconnectButton);
        right.add(statusTitle);
        right.add(connectionStatusLabel);

        outer.add(left,  BorderLayout.CENTER);
        outer.add(right, BorderLayout.EAST);
        outer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Re-wrap with the titled border
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.DARK_GRAY);
        wrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.CYAN),
                "Connection Details",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.CYAN));
        wrapper.add(outer);
        return wrapper;
    }

    private JPanel buildCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN),
                "SQL Command Input Window",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.GREEN));

        sqlCommandArea = new JTextArea(6, 60);
        sqlCommandArea.setBackground(Color.BLACK);
        sqlCommandArea.setForeground(Color.WHITE);
        sqlCommandArea.setCaretColor(Color.WHITE);
        sqlCommandArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(sqlCommandArea), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnRow.setBackground(Color.DARK_GRAY);

        executeButton = new JButton("Execute SQL Command");
        executeButton.setBackground(Color.GREEN.darker());
        executeButton.setForeground(Color.BLACK);
        executeButton.setFont(new Font("Arial", Font.BOLD, 12));
        executeButton.addActionListener(e -> executeSQLCommand());

        clearCommandButton = new JButton("Clear SQL Command");
        clearCommandButton.setBackground(Color.YELLOW);
        clearCommandButton.setForeground(Color.BLACK);
        clearCommandButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearCommandButton.addActionListener(e -> sqlCommandArea.setText(""));

        btnRow.add(executeButton);
        btnRow.add(clearCommandButton);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.MAGENTA),
                "SQL Execution Result Window",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.MAGENTA));

        tableModel  = new DefaultTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setBackground(Color.BLUE.darker());
        resultsTable.setForeground(Color.WHITE);
        resultsTable.setGridColor(Color.GRAY);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsTable.getTableHeader().setBackground(Color.GRAY);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setPreferredSize(new Dimension(860, 250));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        btnRow.setBackground(Color.DARK_GRAY);

        clearResultsButton = new JButton("Clear Result Window");
        clearResultsButton.setBackground(Color.YELLOW);
        clearResultsButton.setForeground(Color.BLACK);
        clearResultsButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearResultsButton.addActionListener(e -> tableModel.setRowCount(0));

        closeButton = new JButton("Close Application");
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.addActionListener(e -> closeApplication());

        btnRow.add(clearResultsButton);
        btnRow.add(Box.createHorizontalStrut(400));
        btnRow.add(closeButton);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    // ── Helper builders ───────────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        return lbl;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(Color.DARK_GRAY);
        combo.setForeground(Color.WHITE);
    }

    // ── Connection Logic ──────────────────────────────────────────────────────

    private void connectToDatabase() {
        String dbPropsFile   = (String) dbPropertiesCombo.getSelectedItem();
        String userPropsFile = (String) userPropertiesCombo.getSelectedItem();
        String enteredUser   = usernameField.getText().trim();
        String enteredPass   = new String(passwordField.getPassword());

        try {
            // Validate credentials against the selected user properties file
            Properties userProps = loadProperties(userPropsFile);
            String fileUser = userProps.getProperty("username", "");
            String filePass = userProps.getProperty("password", "");

            if (!enteredUser.equals(fileUser) || !enteredPass.equals(filePass)) {
                connectionStatusLabel.setText("NO CONNECTION - Credentials Mismatch!");
                connectionStatusLabel.setBackground(Color.RED);
                return;
            }

            // Load DB connection properties
            Properties dbProps = loadProperties(dbPropsFile);
            String url    = dbProps.getProperty("db.url");
            String driver = dbProps.getProperty("db.driver");

            Class.forName(driver);

            // Close previous connection if any
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
            }

            userConnection = DriverManager.getConnection(url, enteredUser, enteredPass);
            connectionStatusLabel.setText(url);
            connectionStatusLabel.setBackground(Color.GREEN);

        } catch (Exception ex) {
            connectionStatusLabel.setText("NO CONNECTION ESTABLISHED");
            connectionStatusLabel.setBackground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnectFromDatabase() {
        try {
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
            }
        } catch (SQLException ex) {
            // ignore
        } finally {
            userConnection = null;
            connectionStatusLabel.setText("NO CONNECTION ESTABLISHED");
            connectionStatusLabel.setBackground(Color.RED);
        }
    }

    // ── SQL Execution ─────────────────────────────────────────────────────────

    private void executeSQLCommand() {
        if (userConnection == null) {
            JOptionPane.showMessageDialog(this,
                    "No database connection established.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = sqlCommandArea.getText().trim();
        if (sql.isEmpty()) return;

        // Strip trailing semicolons that would cause JDBC errors
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();

        boolean isSelect = sql.toLowerCase().startsWith("select");

        if (isSelect) {
            // Use scrollable result set for SELECT
            try (PreparedStatement pstmt = userConnection.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ResultSet rs = pstmt.executeQuery();
                displayResults(rs);
                logOperation(getCurrentUsername(), true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Use plain prepareStatement for DML/DDL so permission errors surface correctly
            try (PreparedStatement pstmt = userConnection.prepareStatement(sql)) {
                int rows = pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this,
                        "Successful Update..." + rows + " rows updated.",
                        "Successful Update", JOptionPane.INFORMATION_MESSAGE);
                logOperation(getCurrentUsername(), false);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayResults(ResultSet rs) throws SQLException {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        // Add columns
        for (int i = 1; i <= cols; i++) {
            tableModel.addColumn(meta.getColumnName(i));
        }

        // Add rows
        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }
    }

    // ── Operations Logging ────────────────────────────────────────────────────

    /**
     * Silently log the operation to the operationslog database using the
     * project3app credentials stored in project3app.properties.
     *
     * @param loginUser  the MySQL login name (e.g., "root@localhost")
     * @param isQuery    true for SELECT (increments num_queries),
     *                   false for DML/DDL (increments num_updates)
     */
    private void logOperation(String loginUser, boolean isQuery) {
        try {
            Properties appProps = loadProperties(APP_PROPERTIES_FILE);
            String url      = appProps.getProperty("db.url");
            String driver   = appProps.getProperty("db.driver");
            String appUser  = appProps.getProperty("username");
            String appPass  = appProps.getProperty("password");

            Class.forName(driver);
            try (Connection logConn = DriverManager.getConnection(url, appUser, appPass)) {

                // Check if row already exists for this user
                String checkSql = "SELECT login_username FROM operationscount WHERE login_username = ?";
                try (PreparedStatement check = logConn.prepareStatement(checkSql)) {
                    check.setString(1, loginUser);
                    ResultSet rs = check.executeQuery();

                    if (rs.next()) {
                        // Row exists – increment appropriate counter
                        String updateSql = isQuery
                                ? "UPDATE operationscount SET num_queries = num_queries + 1 WHERE login_username = ?"
                                : "UPDATE operationscount SET num_updates = num_updates + 1 WHERE login_username = ?";
                        try (PreparedStatement upd = logConn.prepareStatement(updateSql)) {
                            upd.setString(1, loginUser);
                            upd.executeUpdate();
                        }
                    } else {
                        // New user – insert row
                        String insertSql = "INSERT INTO operationscount (login_username, num_queries, num_updates) VALUES (?, ?, ?)";
                        try (PreparedStatement ins = logConn.prepareStatement(insertSql)) {
                            ins.setString(1, loginUser);
                            ins.setInt(2, isQuery ? 1 : 0);
                            ins.setInt(3, isQuery ? 0 : 1);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Logging errors are silent – never shown to end user
            System.err.println("Logging error: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the current MySQL login name (user@host) from the active connection.
     */
    private String getCurrentUsername() {
        try {
            if (userConnection != null && !userConnection.isClosed()) {
                try (Statement st = userConnection.createStatement();
                     ResultSet rs = st.executeQuery("SELECT USER()")) {
                    if (rs.next()) return rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            // fall through
        }
        return usernameField.getText().trim();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private Properties loadProperties(String filename) throws IOException {
        Properties props = new Properties();
        // Try to load from current directory first, then classpath
        File f = new File(filename);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            }
        } else {
            InputStream is = getClass().getResourceAsStream("/" + filename);
            if (is == null) throw new IOException("Properties file not found: " + filename);
            props.load(is);
        }
        return props;
    }

    private void closeApplication() {
        try {
            if (userConnection != null && !userConnection.isClosed()) {
                userConnection.close();
            }
        } catch (SQLException ex) {
            // ignore
        }
        dispose();
        System.exit(0);
    }

    // ── Main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(Project3App::new);
    }
}