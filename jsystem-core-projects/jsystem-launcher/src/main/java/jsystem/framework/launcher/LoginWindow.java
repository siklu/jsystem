package jsystem.framework.launcher;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.commons.lang3.StringUtils;

public class LoginWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JPanel panel;
    private final JButton btnLogin;
    private final JButton btnCancel;
    private final JComboBox<String> comboUserNames;
    private final JPasswordField txtPassword;
    private final JCheckBox chkWithQms;

    private int httpTimeout = 5; // seconds
    private final char defaultEchoChar = '*';
    private final String qmsURL = "http://qms.ceragon.com:5400/api/v1/qms/Autoreport/";
    
    public LoginWindow() {
        super("Login to Jsystem");

        panel = new JPanel(null);
        btnLogin = new JButton("Login");
        btnCancel = new JButton("Cancel");
        comboUserNames = new JComboBox<>(new String[] { "", "siklu" });
        txtPassword = new JPasswordField(15);
        chkWithQms = new JCheckBox("with QMS");

        JLabel lblName = new JLabel("User Name:");
        JLabel lblPassword = new JLabel("Password:");

        setResizable(false);
        setPreferredSize(new Dimension(290, 205));
        setSize(290, 205);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        lblName.setBounds(3, 5, 90, 25);
        comboUserNames.setBounds(85, 10, 190, 20);
        comboUserNames.setMaximumRowCount(8);

        lblPassword.setBounds(3, 40, 90, 25);
        txtPassword.setBounds(85, 45, 190, 20);

        chkWithQms.setBounds(85, 70, 190, 20);

        btnLogin.setBounds(85, 125, 90, 20);
        btnCancel.setBounds(185, 125, 90, 20);

        panel.add(lblName);
        panel.add(comboUserNames);
        panel.add(lblPassword);
        panel.add(txtPassword);
        panel.add(chkWithQms);
        panel.add(btnLogin);
        panel.add(btnCancel);

        setContentPane(panel);
        setVisible(true);

        chkWithQms.addActionListener(e -> {
            boolean withQms = chkWithQms.isSelected();
            comboUserNames.setEditable(withQms);
            txtPassword.setEchoChar(defaultEchoChar);
        });

        btnLogin.addActionListener(e -> handleLogin());

        btnCancel.addActionListener(e -> {
            dispose();
            setVisible(false);
            System.exit(0);
        });

        comboUserNames.addActionListener(e -> txtPassword.requestFocus());

        txtPassword.addActionListener(e -> handleLogin());
    }

    public static void main(String[] args) {
        LoginWindow window = new LoginWindow();
        window.chkWithQms.setSelected(true);
        window.comboUserNames.setEditable(window.chkWithQms.isSelected());
        window.setVisible(true);
    }

    private String getEnteredUserName() {
        Object value;
        if (comboUserNames.isEditable()) {
            value = comboUserNames.getEditor().getItem();
        } else {
            value = comboUserNames.getSelectedItem();
        }
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    private void handleLogin() {
        String insertedPw = new String(txtPassword.getPassword());
        String selectedName = getEnteredUserName();

        if (selectedName == null || selectedName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter/select user name");
            return;
        }

        if (chkWithQms.isSelected()) {
            if (insertedPw.isBlank()) {
                JOptionPane.showMessageDialog(this, "Please enter password");
                return;
            }

            ReturnCode rc = authenticate(qmsURL, selectedName, insertedPw);
            if (rc == ReturnCode.OK) {
                writeCookie(selectedName, insertedPw);
                dispose();
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, rc.getMessage());
                txtPassword.setText("");
                deleteCookie();
                dispose();
                setVisible(false);
            }
            return;
        }

        if (insertedPw.isBlank()) {
            JOptionPane.showMessageDialog(this, "Invalid password. Please try again");
            txtPassword.setText("");
            return;
        }
        dispose();
        setVisible(false);
    }

    private void writeCookie(String selectedName, String password) {
        String currentDir = System.getenv("current_dir");
        if (currentDir == null || currentDir.isBlank()) {
            JOptionPane.showMessageDialog(this, "Failed to write cookie file: env var current_dir is not set");
            return;
        }
        Path cookiePath = Path.of(currentDir, ".siklu.coockie");
        String content = String.format("%s:%s", selectedName, password);

        try {
            Files.writeString(
                    cookiePath,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to write cookie file: " + e.getMessage());
        }
    }

    private void deleteCookie() {
        String currentDir = System.getenv("current_dir");
        if (currentDir == null || currentDir.isBlank()) {
            return;
        }
        Path cookiePath = Path.of(currentDir, ".siklu.coockie");

        try {
            Files.deleteIfExists(cookiePath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to delete cookie file: " + e.getMessage());
        }
    }

    public ReturnCode authenticate(String url, String username, String password) {
        if (url == null || url.isBlank()) {
            return ReturnCode.NOT_EXIST;
        }

        Vector<String> urlParts = new Vector<>();
        if (url.contains("?")) {
            url += "&function=Qms3VerifyPassword";
        } else {
            url += "Qms3VerifyPassword?";
        }

        urlParts.add("flex_user_code=" + username);
        urlParts.add("passwd=" + password);

        url += StringUtils.join(urlParts, "&");
        String ret = sendURL(url);
        if (ret == null || ret.isBlank()) {
            return ReturnCode.NOT_EXIST;
        }

        ReturnCode code = ReturnCode.getByKey(ret.strip());
        return code != null ? code : ReturnCode.NOT_EXIST;
    }

    private String sendURL(String url) {
        try {
            int retryCounter = 3;
            HttpURLConnection uc = null;
            URL qms = new URL(url.replace(' ', '+'));

            while (retryCounter > 0) {
                try {
                    uc = (HttpURLConnection) qms.openConnection();
                    uc.setConnectTimeout(httpTimeout * 1000);
                    uc.setReadTimeout(httpTimeout * 1000);
                    uc.connect();
                    break;
                } catch (IOException e) {
                    retryCounter--;
                    if (retryCounter == 0) {
                        throw e;
                    }
                }
            }

            if (uc == null) {
                return "";
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()))) {
                StringBuilder ret = new StringBuilder();
                String str;
                while ((str = in.readLine()) != null) {
                    ret.append(str).append('\n');
                }
                return ret.toString();
            }
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    enum ReturnCode {
        OK("1", "User is O.K. to report tests.%09[USERNAME]"),
        NOT_EXIST("0", "User does not exist"),
        INCORRECT_PASSWORD("-1", "user's password is not correct"),
        USER_NOT_ACTIVE("-2", "user is not active"),
        NOT_OPERATOR("-3", "user does not have operator permission");

        private final String key;
        private final String msg;

        ReturnCode(String key, String msg) {
            this.key = key;
            this.msg = msg;
        }

        public String getKey() {
            return key;
        }

        public String getMessage() {
            return msg;
        }

        public String toString(String username) {
            String result = getMessage().replaceAll("USERNAME", username).replaceAll(" ", "+");
            return getKey() + "%09" + result;
        }

        public static ReturnCode getByKey(String key) {
            for (ReturnCode code : ReturnCode.values()) {
                if (code.getKey().equals(key)) {
                    return code;
                }
            }
            return null;
        }
    }
}