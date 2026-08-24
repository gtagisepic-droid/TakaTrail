package com.takatrail;

import java.io.IOException;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.initializeDatabase();
                AuthManager authManager = new AuthManager(databaseManager);
                TransactionManager transactionManager = new TransactionManager(databaseManager);
                FileManager fileManager = new FileManager(databaseManager);
                TakaTrailGUI application = new TakaTrailGUI(authManager, transactionManager,
                        databaseManager, fileManager);
                application.setVisible(true);
            } catch (SQLException | IOException exception) {
                System.err.println("TakaTrail startup failed: " + exception.getMessage());
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "TakaTrail could not initialize its local database.",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception exception) {
                System.err.println("TakaTrail startup failed: " + exception.getMessage());
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "TakaTrail could not start.",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
