/*
Name: Joshua Khooba
Course: CNT 4714 Spring 2026
Assignment title: Project 3 – A Specialized Accountant Application
Date: March 15, 2026
Class: AccountantApp
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Specialized read-only application for the "theaccountant" MySQL user.
 * Connects only to the operationslog database.
 * Properties files are hardcoded – the user only types credentials for verification.
 */
public class AccountantApp extends JFrame {

    // Hardcoded properties file names
    private static final String DB_PROPS_FILE   = "operationslog.properties";
    private static final String USER_PROPS_FILE = "theaccountant.properties";

    // ── Connection Details Panel ──────────────────────────────────────────────
    private JTextField dbPropsLabel;
    private JTextField userPropsLabel;
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
    private Connection accountantConnection = null;

    // ─────────────────────────────────────────────────────────────────────────

    public AccountantApp() {
        setTitle("SPECIALIZED ACCOUNTANT APPLICATION - (CNT 4714 - SPRING 2026 - PROJECT 3)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        getContentPane().setBackground(Color.LIGHT_GRAY);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeApplication(); }
        });

        add(buildConnectionPanel(), BorderLayout.NORTH);
        add(buildCommandPanel(),    BorderLayout.CENTER);
        add(buildResultsPanel(),    BorderLayout.SOUTH);

        pack();
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── GUI Builders ──────────────────────────────────────────────────────────

    private JPanel buildConnectionPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.LIGHT_GRAY);
        wrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.CYAN),
                "Connection Details",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.CYAN));

        JPanel left = new JPanel(new GridLayout(4, 2, 5, 5));
        left.setBackground(Color.LIGHT_GRAY);
        left.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        left.add(makeLabel("DB URL Properties"));
        dbPropsLabel = new JTextField(DB_PROPS_FILE);
        dbPropsLabel.setEditable(false);
        dbPropsLabel.setBackground(Color.WHITE);
        left.add(dbPropsLabel);

        left.add(makeLabel("User Properties"));
        userPropsLabel = new JTextField(USER_PROPS_FILE);
        userPropsLabel.setEditable(false);
        userPropsLabel.setBackground(Color.WHITE);
        left.add(userPropsLabel);

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

        JPanel right = new JPanel(new GridLayout(4, 1, 5, 5));
        right.setBackground(Color.LIGHT_GRAY);

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
        statusTitle.setForeground(Color.BLACK);
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

        wrapper.add(left,  BorderLayout.CENTER);
        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN),
                "SQL Command Input Window",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.GREEN));

        sqlCommandArea = new JTextArea(6, 60);
        sqlCommandArea.setBackground(Color.WHITE);
        sqlCommandArea.setForeground(Color.BLACK);
        sqlCommandArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(sqlCommandArea), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnRow.setBackground(Color.LIGHT_GRAY);

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
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.MAGENTA),
                "SQL Execution Result Window",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), Color.MAGENTA));

        tableModel   = new DefaultTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setBackground(Color.BLUE.darker());
        resultsTable.setForeground(Color.WHITE);
        resultsTable.setGridColor(Color.GRAY);
        resultsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsTable.getTableHeader().setBackground(Color.GRAY);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setPreferredSize(new Dimension(860, 200));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        btnRow.setBackground(Color.LIGHT_GRAY);

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        return lbl;
    }

    // ── Connection Logic ──────────────────────────────────────────────────────

    private void connectToDatabase() {
        String enteredUser = usernameField.getText().trim();
        String enteredPass = new String(passwordField.getPassword());

        try {
            Properties userProps = loadProperties(USER_PROPS_FILE);
            String fileUser = userProps.getProperty("username", "");
            String filePass = userProps.getProperty("password", "");

            if (!enteredUser.equals(fileUser) || !enteredPass.equals(filePass)) {
                connectionStatusLabel.setText("NO CONNECTION - Credentials Mismatch!");
                connectionStatusLabel.setBackground(Color.RED);
                return;
            }

            Properties dbProps = loadProperties(DB_PROPS_FILE);
            String url    = dbProps.getProperty("db.url");
            String driver = dbProps.getProperty("db.driver");

            Class.forName(driver);

            if (accountantConnection != null && !accountantConnection.isClosed()) {
                accountantConnection.close();
            }

            accountantConnection = DriverManager.getConnection(url, enteredUser, enteredPass);
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
            if (accountantConnection != null && !accountantConnection.isClosed()) {
                accountantConnection.close();
            }
        } catch (SQLException ex) {
            // ignore
        } finally {
            accountantConnection = null;
            connectionStatusLabel.setText("NO CONNECTION ESTABLISHED");
            connectionStatusLabel.setBackground(Color.RED);
        }
    }

    // ── SQL Execution (SELECT only) ───────────────────────────────────────────

    private void executeSQLCommand() {
        if (accountantConnection == null) {
            JOptionPane.showMessageDialog(this,
                    "No database connection established.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = sqlCommandArea.getText().trim();
        if (sql.isEmpty()) return;

        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();

        // Accountant is SELECT only
        if (!sql.toLowerCase().startsWith("select")) {
            JOptionPane.showMessageDialog(this,
                    "Permission denied: theaccountant user may only execute SELECT queries.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement pstmt = accountantConnection.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            ResultSet rs = pstmt.executeQuery();
            displayResults(rs);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults(ResultSet rs) throws SQLException {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            tableModel.addColumn(meta.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[cols];
            for (int i = 1; i <= cols; i++) {
                row[i - 1] = rs.getObject(i);
            }
            tableModel.addRow(row);
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private Properties loadProperties(String filename) throws IOException {
        Properties props = new Properties();
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
            if (accountantConnection != null && !accountantConnection.isClosed()) {
                accountantConnection.close();
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

        SwingUtilities.invokeLater(AccountantApp::new);
    }
}
