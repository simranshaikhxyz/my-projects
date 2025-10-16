import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Random;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class PrayerApp extends JFrame {

    // ================= CONSTANTS =================
    private static final Color BG_PANEL = new Color(249, 240, 222);
    private static final Color BG_LIGHT = new Color(179, 92, 97);
    private static final Color BG_NEUTRAL = new Color(94, 84, 120);
    private static final Color TEXT_DARK = new Color(0, 0, 0);
    private static final Color TEXT_BROWN = new Color(0, 0, 0);
    private static final Color BTN_PRIMARY = new Color(255, 107, 107);
    private static final Color BTN_PRIMARY_HOVER = new Color(230, 57, 70);
    private static final Color BTN_SECONDARY = new Color(194, 68, 96);
    private static final Color BTN_SECONDARY_HOVER = new Color(200, 155, 76);
    private static final Color BTN_TEXT = Color.black;
    private static final Color TABLE_HEADER_BG = new Color(188, 143, 143);
    private static final Color TABLE_HEADER_FG = Color.pink;
    private static final Color SELECTION_BG = new Color(229, 152, 155);
    private static final Color SELECTION_FG = new Color(62, 44, 42);
    private static final Color LINK_COLOR = new Color(230, 57, 70);

    // Database configuration
    private static final String DB_CONFIG_FILE = "C:\\Users\\Lenovo\\simran scala\\javaprojects\\javaproject\\src\\db_config.properties";

    // SQL Queries
    private static final String SQL_AUTHENTICATE_USER = "SELECT * FROM users WHERE username=? AND password=?";
    private static final String SQL_CHECK_USER_EXISTS = "SELECT * FROM users WHERE username=?";
    private static final String SQL_INSERT_USER = "INSERT INTO users(username,password) VALUES(?,?)";
    private static final String SQL_GET_HADITH_COUNT = "SELECT COUNT(*) FROM hadiths";
    private static final String SQL_GET_RANDOM_HADITH = "SELECT * FROM hadiths WHERE id = ?";
    private static final String SQL_GET_TASBIH = "SELECT transliteration, meaning FROM tasbih ORDER BY id";
    private static final String SQL_GET_NAMES_OF_ALLAH = "SELECT transliteration, meaning FROM NamesofAllah ORDER BY id";
    private static final String SQL_GET_DUAS = "SELECT transliteration, meaning FROM duas ORDER BY id";
    private static final String SQL_GET_NAMAZ_RAKAT = "SELECT * FROM namaz ORDER BY id";

    // ================= Constructor =================
    public PrayerApp() {
        initializeMainWindow();
        createLoginForm();
    }

    // ================= Main Window Setup =================
    private void initializeMainWindow() {
        setTitle("Prayer App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridLayout(1, 2));
    }

    // ================= Login Form =================
    private void createLoginForm() {
        JPanel formPanel = createFormPanel();
        JPanel imagePanel = createImagePanel();

        add(formPanel);
        add(imagePanel);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form components
        JLabel lblUsername = createLabel("Username:");
        JTextField txtUsername = new JTextField(15);
        JLabel lblPassword = createLabel("Password:");
        JPasswordField txtPassword = new JPasswordField(15);
        JButton btnLogin = createButton("Login");
        JLabel registerLink = createRegisterLink();

        // Add components to form
        addFormComponent(formPanel, gbc, lblUsername, txtUsername, 0);
        addFormComponent(formPanel, gbc, lblPassword, txtPassword, 1);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(btnLogin, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(registerLink, gbc);

        // Add event listeners
        setupLoginAction(btnLogin, txtUsername, txtPassword);
        setupRegisterAction(registerLink, formPanel, gbc, lblUsername, txtUsername,
                lblPassword, txtPassword, btnLogin);

        return formPanel;
    }

    private JPanel createImagePanel() {
        return new JPanel() {
            private final Image image = loadBackgroundImage();

            private Image loadBackgroundImage() {
                try {
                    // Try multiple ways to load the image
                    String[] possiblePaths = {
                            "C:\\Users\\Lenovo\\simran scala\\javaprojects\\javaproject\\src\\islamic-background.jpg",
                            "islamic-background.jpg",
                            "src/islamic-background.jpg",
                            "images/islamic-background.jpg"
                    };

                    for (String path : possiblePaths) {
                        java.io.File file = new java.io.File(path);
                        if (file.exists()) {
                            return new ImageIcon(path).getImage();
                        }
                    }

                    // Try classpath loading as fallback
                    java.net.URL resource = getClass().getResource("/islamic-background.jpg");
                    if (resource != null) {
                        return new ImageIcon(resource).getImage();
                    }

                    resource = getClass().getResource("islamic-background.jpg");
                    if (resource != null) {
                        return new ImageIcon(resource).getImage();
                    }

                    System.out.println("Background image not found. Using solid color.");
                    return null;
                } catch (Exception e) {
                    System.out.println("Error loading background image: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback background
                    g.setColor(BG_LIGHT);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_DARK);
        return label;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        styleButton(button);
        return button;
    }

    private JLabel createRegisterLink() {
        JLabel registerLink = new JLabel("<HTML><U>Register</U></HTML>");
        registerLink.setForeground(LINK_COLOR);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return registerLink;
    }

    private void addFormComponent(JPanel panel, GridBagConstraints gbc,
                                  JLabel label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    // ================= Event Handlers =================
    private void setupLoginAction(JButton btnLogin, JTextField txtUsername,
                                  JPasswordField txtPassword) {
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showErrorMessage("Please enter username and password");
                return;
            }

            if (authenticateUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                showHomePage();
                dispose();
            } else {
                showErrorMessage("Invalid username or password");
            }
        });
    }

    private void setupRegisterAction(JLabel registerLink, JPanel formPanel,
                                     GridBagConstraints gbc, JLabel lblUsername,
                                     JTextField txtUsername, JLabel lblPassword,
                                     JPasswordField txtPassword, JButton btnLogin) {
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRegistrationForm(formPanel, gbc, lblUsername, txtUsername,
                        lblPassword, txtPassword, btnLogin, registerLink);
            }
        });
    }

    // ================= Database Operations =================
    private boolean authenticateUser(String username, String password) {
        return executeUserQuery(SQL_AUTHENTICATE_USER, username, password);
    }

    private boolean registerUser(String username, String password) {
        try (Connection conn = getDatabaseConnection()) {
            // Check if user exists
            if (executeUserQuery(SQL_CHECK_USER_EXISTS, username, "")) {
                showErrorMessage("Username already exists!");
                return false;
            }

            // Insert new user
            try (PreparedStatement insert = conn.prepareStatement(SQL_INSERT_USER)) {
                insert.setString(1, username);
                insert.setString(2, password);
                insert.executeUpdate();
                return true;
            }

        } catch (Exception ex) {
            handleDatabaseError("registering user", ex);
            return false;
        }
    }

    private boolean executeUserQuery(String query, String username, String password) {
        try (Connection conn = getDatabaseConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            if (!password.isEmpty()) {
                ps.setString(2, password);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception ex) {
            handleDatabaseError("authenticating user", ex);
            return false;
        }
    }

    private Connection getDatabaseConnection() throws Exception {
        Properties props = loadDatabaseProperties();
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                props.getProperty("db.url", "jdbc:mysql://localhost:3306/prayer_db"),
                props.getProperty("db.user", "root"),
                props.getProperty("db.password", "kingsman700")
        );
    }

    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(DB_CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Config file not found, using default database settings");
        }
        return props;
    }

    private void handleDatabaseError(String operation, Exception ex) {
        ex.printStackTrace();
        showErrorMessage("Database error during " + operation + "!");
    }

    // ================= Registration Form =================
    private void showRegistrationForm(JPanel formPanel, GridBagConstraints gbc,
                                      JLabel lblUsername, JTextField txtUsername,
                                      JLabel lblPassword, JPasswordField txtPassword,
                                      JButton btnLogin, JLabel registerLink) {
        formPanel.removeAll();
        gbc.gridwidth = 1;

        // Create registration components
        JLabel lblNewUser = createLabel("Username:");
        JTextField txtNewUser = new JTextField(15);
        JLabel lblNewPass = createLabel("Password:");
        JPasswordField txtNewPass = new JPasswordField(15);
        JButton btnRegister = createButton("Register");
        JButton btnCancel = createCancelButton("Cancel");

        // Add components
        addFormComponent(formPanel, gbc, lblNewUser, txtNewUser, 0);
        addFormComponent(formPanel, gbc, lblNewPass, txtNewPass, 1);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(btnRegister, gbc);
        gbc.gridx = 1;
        formPanel.add(btnCancel, gbc);

        formPanel.revalidate();
        formPanel.repaint();

        // Setup actions
        setupRegistrationActions(btnRegister, btnCancel, txtNewUser, txtNewPass,
                formPanel, gbc, lblUsername, txtUsername,
                lblPassword, txtPassword, btnLogin, registerLink);
    }

    private void setupRegistrationActions(JButton btnRegister, JButton btnCancel,
                                          JTextField txtNewUser, JPasswordField txtNewPass,
                                          JPanel formPanel, GridBagConstraints gbc,
                                          JLabel lblUsername, JTextField txtUsername,
                                          JLabel lblPassword, JPasswordField txtPassword,
                                          JButton btnLogin, JLabel registerLink) {
        btnRegister.addActionListener(e -> {
            String user = txtNewUser.getText().trim();
            String pass = new String(txtNewPass.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showErrorMessage("Please enter username and password");
                return;
            }

            if (registerUser(user, pass)) {
                JOptionPane.showMessageDialog(this,
                        "Registration Successful! You can now login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                btnCancel.doClick();
            }
        });

        btnCancel.addActionListener(e -> {
            restoreLoginForm(formPanel, gbc, lblUsername, txtUsername,
                    lblPassword, txtPassword, btnLogin, registerLink);
        });
    }

    private void restoreLoginForm(JPanel formPanel, GridBagConstraints gbc,
                                  JLabel lblUsername, JTextField txtUsername,
                                  JLabel lblPassword, JPasswordField txtPassword,
                                  JButton btnLogin, JLabel registerLink) {
        formPanel.removeAll();

        addFormComponent(formPanel, gbc, lblUsername, txtUsername, 0);
        addFormComponent(formPanel, gbc, lblPassword, txtPassword, 1);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(btnLogin, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(registerLink, gbc);

        formPanel.revalidate();
        formPanel.repaint();
    }

    // ================= Home Page =================
    private void showHomePage() {
        JFrame home = new JFrame("Prayer Management - Home");
        home.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        home.setSize(800, 500);
        home.setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_PANEL);

        // Create heading label
        JLabel heading = new JLabel("PRAYER MANAGEMENT SYSTEM", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(Color.DARK_GRAY);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(BG_PANEL);
        createHomeButtons(buttonPanel);

        // Add components to main panel
        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        home.add(mainPanel);
        home.setVisible(true);
    }

    private void createHomeButtons(JPanel mainPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Create buttons
        JButton[] buttons = {
                createHomeButton("Tasbi Guide", e -> showTasbiGuide()),
                createHomeButton("Asma-e-Husna", e -> showNamesOfAllah()),
                createHomeButton("Namaz Timings", e -> showNamazTimes()),
                createHomeButton("Duas", e -> showDuas()),
                createHomeButton("Namaz Rakat", e -> showNamazRakat()),
                createHomeButton("Hadith", e -> showHadiths())
        };

        // Add buttons in 2x3 grid
        int buttonIndex = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                gbc.gridx = col;
                gbc.gridy = row;
                mainPanel.add(buttons[buttonIndex++], gbc);
            }
        }
    }

    private JButton createHomeButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        styleButton(button);
        button.setPreferredSize(new Dimension(150, 150));

        // Add image to button (your original logic preserved)
        String imagePath = getImagePath(text);
        if (imagePath != null) {
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
                button.setHorizontalTextPosition(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
            }
        }

        button.addActionListener(action);
        return button;
    }

    private String getImagePath(String buttonText) {
        switch (buttonText) {
            case "Tasbi Guide": return "tasbih.png";
            case "Asma-e-Husna": return "quran.png";
            case "Namaz Timings": return "namaz.png";
            case "Duas": return "duahands.png";
            case "Namaz Rakat": return "prayermat.png";
            case "Hadith": return "hadith.png";
            default: return null;
        }
    }

    // ================= Feature Methods =================
    private void showHadiths() {
        try (Connection conn = getDatabaseConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_HADITH_COUNT)) {

            rs.next();
            int total = rs.getInt(1);

            Random rand = new Random();
            int randomId = rand.nextInt(total) + 1;

            try (PreparedStatement ps = conn.prepareStatement(SQL_GET_RANDOM_HADITH)) {
                ps.setInt(1, randomId);
                try (ResultSet hadithRs = ps.executeQuery()) {
                    if (hadithRs.next()) {
                        displayHadith(hadithRs);
                    }
                }
            }
        } catch (Exception e) {
            handleDatabaseError("loading hadith", e);
        }
    }

    private void displayHadith(ResultSet hadithRs) throws SQLException {
        String title = hadithRs.getString("title");
        String hadith = hadithRs.getString("hadith");
        String reference = hadithRs.getString("reference");
        String quran = hadithRs.getString("quran_ref");

        String message = String.format("%s\n\nHadith: %s\n\nReference: %s\nQuran: %s",
                title, hadith, reference, quran);

        String[] options = {"Show Another", "Close"};
        int choice = JOptionPane.showOptionDialog(null, message, "Random Hadith",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            showHadiths();
        }
    }

    private void showTasbiGuide() {
        String[] arabicNames = {"سُبْحَانَ الله", "اَلْحَمْدُ لِلّٰهِ", "الله أَكْبَر"};
        String[][] data = loadGenericData(arabicNames, SQL_GET_TASBIH, 4);

        if (data == null) return;

        JFrame frame = new JFrame("Tasbi Guide with Counter");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create table
        JTable table = createTasbiTable(data);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(650, 300));

        // Create counter panel
        JPanel counterPanel = createCounterPanel();

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(counterPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String[][] loadGenericData(String[] arabicNames, String query, int columns) {
        String[][] data = new String[arabicNames.length][columns];

        try (Connection conn = getDatabaseConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int index = 0;
            while (rs.next() && index < arabicNames.length) {
                data[index][0] = String.valueOf(index + 1);
                data[index][1] = arabicNames[index];
                data[index][2] = rs.getString("transliteration");
                data[index][3] = rs.getString("meaning");
                index++;
            }
            return data;
        } catch (Exception e) {
            handleDatabaseError("fetching data", e);
            return null;
        }
    }

    private JTable createTasbiTable(String[][] data) {
        String[] columnNames = {"ID", "Arabic", "Transliteration", "Meaning"};
        return createTable(data, columnNames, new int[]{40, 120, 150, 400});
    }

    private JPanel createCounterPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(BG_PANEL);

        JLabel countLabel = new JLabel("0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 30));
        countLabel.setForeground(BTN_PRIMARY);

        JButton addButton = createButton("Count");
        JButton resetButton = createCancelButton("Reset");
        JButton okButton = createButton("OK");
        okButton.setPreferredSize(new Dimension(60, 30));

        final int[] count = {0};
        addButton.addActionListener(e -> {
            count[0]++;
            countLabel.setText(String.valueOf(count[0]));
        });
        resetButton.addActionListener(e -> {
            count[0] = 0;
            countLabel.setText("0");
        });
        okButton.addActionListener(e ->
                SwingUtilities.getWindowAncestor(panel).dispose());

        panel.add(countLabel);
        panel.add(addButton);
        panel.add(resetButton);
        panel.add(okButton);

        return panel;
    }

    private void showNamesOfAllah() {
        String[] arabicNames = {
                "الرَّحْمَن", "الرَّحِيم", "الْمَلِك", "الْقُدُّوس", "السَّلَام", "الْمُؤْمِن",
                "المُهَيْمِن", "العَزِيز", "الْجَبَّار", "المُتَكَبِّر", "الخَالِق", "البَارِئ",
                "المُصَوِّر", "الغَفَّار", "القَهَّار", "الوَهَّاب", "الرَّزَّاق", "الْفَتَّاح",
                "العَلِيم", "القَابِض", "البَاسِط", "الخَافِض", "الرَّافِع", "المُعِز", "المُذِل",
                "السَّمِيع", "البَصِير", "الحَكَم", "العَدْل", "اللَّطِيف", "الخَبِير", "الحَلِيم",
                "العَظِيم", "الغَفُور", "الشَّكُور", "العَلِي", "الْكَبِير", "الحَفِظ", "المُقِيت",
                "الحَسِيب", "الجَلِيل", "الكَرِيم", "الرَّقِيب", "المُجِيب", "الْوَاسِع", "الحَكِيم",
                "الوَدُود", "المَجِيد", "الباعِث", "الشَّهِيد", "الحَق", "الوَكِيل", "القَوِي",
                "المَتِين", "الْوَلِي", "الحَمِيد", "المُحْصِي", "المُبْدِئ", "المُعِيد", "المُحْيِي",
                "المُمِيت", "الحَيّ", "القَيُّوم", "الْوَاجِد", "المَاجِد", "الواحِد", "الأَحَد",
                "الصَّمَد", "القَادِر", "المُقْتَدِر", "المُقَدِّم", "المُؤَخِّر", "الأوَّل", "الآخِر",
                "الظَّاهِر", "الباطِن", "الوَالي", "المُتَعَالِي", "البَرّ", "التَّوَاب", "المُنْتَقِم",
                "العَفُوّ", "الرَّؤوف", "مَالِكُ الْمُلْك", "ذُو الْجَلَالِ وَالإكْرَام", "المُقْسِط",
                "الجَامِع", "الغَنيّ", "المُغْنِي", "المَانِع", "الضَّار", "النَّافِع", "النُّور",
                "الهادي", "البَدِيع", "الباقي", "الوارث", "الرَّشِيد", "الصَّبُور"
        };

        String[][] data = loadGenericData(arabicNames, SQL_GET_NAMES_OF_ALLAH, 4);
        if (data == null) return;

        String[] columnNames = {"ID", "Arabic Name", "Transliteration", "Meaning"};
        JTable table = createTable(data, columnNames, new int[]{40, 120, 150, 600});

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        JOptionPane.showMessageDialog(null, scrollPane, "99 Names of Allah",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JTable createTable(String[][] data, String[] columnNames, int[] columnWidths) {
        JTable table = new JTable(data, columnNames) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        styleTable(table);

        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        return table;
    }

    private void showNamazTimes() {
        JFrame frame = new JFrame("Namaz Timings");
        frame.setSize(350, 300);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BG_PANEL);

        JPanel checkboxPanel = createNamazCheckboxPanel();
        JPanel buttonPanel = createNamazButtonPanel(frame);

        frame.add(checkboxPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createNamazCheckboxPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        panel.setBackground(BG_PANEL);

        String[] prayers = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
        for (String prayer : prayers) {
            JCheckBox checkbox = new JCheckBox(prayer);
            checkbox.setForeground(TEXT_DARK);
            panel.add(checkbox);
        }

        return panel;
    }

    private JPanel createNamazButtonPanel(JFrame parentFrame) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PANEL);

        JButton fetchButton = createButton("Fetch");
        JButton okButton = createButton("OK");

        fetchButton.setPreferredSize(new Dimension(80, 30));
        okButton.setPreferredSize(new Dimension(80, 30));

        fetchButton.addActionListener(e -> {
            String city = JOptionPane.showInputDialog(parentFrame, "Enter city:");
            String country = JOptionPane.showInputDialog(parentFrame, "Enter country:");

            if (city != null && country != null && !city.isEmpty() && !country.isEmpty()) {
                Map<String, String> times = getPrayerTimesMap(city, country);
                displayPrayerTimes(parentFrame, times, city);
            }
        });

        okButton.addActionListener(e -> parentFrame.dispose());

        panel.add(fetchButton);
        panel.add(okButton);

        return panel;
    }

    private Map<String, String> getPrayerTimesMap(String city, String country) {
        Map<String, String> map = new LinkedHashMap<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format(
                    "http://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=2",
                    city, country);

            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            String json = EntityUtils.toString(response.getEntity());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            JsonNode timings = rootNode.path("data").path("timings");

            map.put("Fajr", timings.path("Fajr").asText());
            map.put("Dhuhr", timings.path("Dhuhr").asText());
            map.put("Asr", timings.path("Asr").asText());
            map.put("Maghrib", timings.path("Maghrib").asText());
            map.put("Isha", timings.path("Isha").asText());

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Error fetching prayer times");
        }

        return map;
    }

    private void displayPrayerTimes(JFrame parent, Map<String, String> times, String city) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : times.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        JOptionPane.showMessageDialog(parent, sb.toString(),
                "Prayer Times for " + city, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDuas() {
        String[] arabicTexts = {
                "اَللّٰهُمَّ بِكَ أَصْبَحْنَا",
                "اَللّٰهُمَّ بِكَ أََمْسَيْنَا",
                "بِاسْمِكَ رَبِّ أَمُوتُ وَأَحْيَا",
                "أَسْتَغْفِرُ اللّهَ"
        };

        String[][] data = loadGenericData(arabicTexts, SQL_GET_DUAS, 4);

        if (data != null) {
            displayDuaMessage(data);
        }
    }

    private void displayDuaMessage(String[][] data) {
        StringBuilder message = new StringBuilder();

        for (String[] row : data) {
            message.append(row[0]).append(". ").append(getTitle(Integer.parseInt(row[0]))).append("\n")
                    .append("Arabic: ").append(row[1]).append("\n")
                    .append("Transliteration: ").append(row[2]).append("\n")
                    .append("Meaning: ").append(row[3]).append("\n\n");
        }

        JOptionPane.showMessageDialog(null, message.toString(), "Duas", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getTitle(int count) {
        switch (count) {
            case 1: return "Morning Dua";
            case 2: return "Evening Dua";
            case 3: return "Before Sleep";
            case 4: return "Seeking Forgiveness";
            default: return "";
        }
    }

    private void showNamazRakat() {
        String[][] data = loadNamazRakatData();
        if (data == null) return;

        String[] columnNames = {"ID", "Prayer Name", "Sunnat Before", "Fard",
                "Sunnat After", "Nafl Before", "Witr", "Nafl After", "Total"};

        JTable table = createTable(data, columnNames, new int[]{50, 90, 90, 80, 80, 80, 80, 80, 80});

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 250));

        JFrame frame = new JFrame("Namaz Rakat Table");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(BG_PANEL);
        frame.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG_PANEL);
        JButton okButton = createButton("OK");
        okButton.setPreferredSize(new Dimension(60, 30));
        okButton.addActionListener(e -> frame.dispose());
        buttonPanel.add(okButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String[][] loadNamazRakatData() {
        String[][] data = new String[50][9];
        int rowCount = 0;

        try (Connection conn = getDatabaseConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_NAMAZ_RAKAT)) {

            while (rs.next() && rowCount < data.length) {
                data[rowCount][0] = String.valueOf(rs.getInt("id"));
                data[rowCount][1] = rs.getString("prayer_name");
                data[rowCount][2] = String.valueOf(rs.getInt("sunnat_before"));
                data[rowCount][3] = String.valueOf(rs.getInt("fard"));
                data[rowCount][4] = String.valueOf(rs.getInt("sunnat_after"));
                data[rowCount][5] = String.valueOf(rs.getInt("nafl_before"));
                data[rowCount][6] = String.valueOf(rs.getInt("witr"));
                data[rowCount][7] = String.valueOf(rs.getInt("nafl_after"));
                data[rowCount][8] = String.valueOf(rs.getInt("total"));
                rowCount++;
            }

            String[][] result = new String[rowCount][9];
            System.arraycopy(data, 0, result, 0, rowCount);
            return result;

        } catch (Exception e) {
            handleDatabaseError("fetching namaz rakat data", e);
            return null;
        }
    }

    // ================= Styling Methods =================
    private void styleButton(JButton btn) {
        btn.setBackground(BTN_PRIMARY);
        btn.setForeground(BTN_TEXT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BTN_PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BTN_PRIMARY);
            }
        });
    }

    private JButton createCancelButton(String text) {
        JButton btn = new JButton(text);
        styleCancelButton(btn);
        return btn;
    }

    private void styleCancelButton(JButton btn) {
        btn.setBackground(BTN_SECONDARY);
        btn.setForeground(BTN_TEXT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BTN_SECONDARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BTN_SECONDARY);
            }
        });
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TABLE_HEADER_FG);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(SELECTION_FG);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setReorderingAllowed(false);
    }

    // ================= Utility Methods =================
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================= Main Method =================
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new PrayerApp().setVisible(true);
        });
    }
}