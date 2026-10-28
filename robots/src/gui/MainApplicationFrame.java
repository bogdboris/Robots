package gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;
import model.RobotModel;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final Map<String, JInternalFrame> windows = new HashMap<>();

    public MainApplicationFrame(RobotModel robotModel) {
        // Установка размеров и положения главного окна
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);


        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow(robotModel);
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        RobotCoordinatesWindow coordinatesWindow = new RobotCoordinatesWindow(robotModel);
        coordinatesWindow.setLocation(420, 10);
        coordinatesWindow.setSize(280, 160);
        addWindow(coordinatesWindow);


        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });

        loadWindowStates();
    }

    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
        windows.put(frame.getTitle(), frame);
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createExitMenu());
        return menuBar;
    }

    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(systemLookAndFeel);

        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        lookAndFeelMenu.add(crossplatformLookAndFeel);

        return lookAndFeelMenu;
    }

    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug("Новая строка");
        });
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    private JMenu createExitMenu() {
        JMenu exitMenu = new JMenu("Выход");
        exitMenu.setMnemonic(KeyEvent.VK_Q);

        JMenuItem exitItem = new JMenuItem("Выйти", KeyEvent.VK_Q);
        exitItem.addActionListener((event) -> confirmAndExit());
        exitMenu.add(exitItem);

        return exitMenu;
    }

    private void confirmAndExit() {
        Object[] options = {"Да", "Нет"};
        int result = JOptionPane.showOptionDialog(
                this,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (result == JOptionPane.YES_OPTION) {
            saveWindowStates();
            System.exit(0);
        }
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Error setting look and feel: " + e.getMessage());
        }
    }

    private void loadWindowStates() {
        Map<String, WindowState> config = AppConfigManager.loadConfig();

        for (Map.Entry<String, WindowState> entry : config.entrySet()) {
            JInternalFrame window = windows.get(entry.getKey());
            if (window != null) {
                WindowState state = entry.getValue();
                window.setBounds(state.getBounds());
                try {
                    window.setIcon(state.isIcon());
                    window.setMaximum(state.isMaximum());
                } catch (Exception e) {
                    Logger.error("Error restoring window state: " + e.getMessage());
                }
            }
        }
    }

    private void saveWindowStates() {
        Map<String, WindowState> config = new HashMap<>();

        for (Map.Entry<String, JInternalFrame> entry : windows.entrySet()) {
            JInternalFrame window = entry.getValue();
            config.put(entry.getKey(), new WindowState(
                    window.getBounds(),
                    window.isIcon(),
                    window.isMaximum()
            ));
        }

        AppConfigManager.saveConfig(config);
    }
    //интерфейс

    private static class WindowState implements Serializable {
        private final Rectangle bounds;
        private final boolean isIcon;
        private final boolean isMaximum;

        public WindowState(Rectangle bounds, boolean isIcon, boolean isMaximum) {
            this.bounds = bounds;
            this.isIcon = isIcon;
            this.isMaximum = isMaximum;
        }

        public Rectangle getBounds() { return bounds; }
        public boolean isIcon() { return isIcon; }
        public boolean isMaximum() { return isMaximum; }
    }

    private static class AppConfigManager {
        private static final String CONFIG_FILE = Paths.get(
                System.getProperty("user.home"), "window_config.ser").toString();

        public static void saveConfig(Map<String, WindowState> config) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(CONFIG_FILE))) {
                oos.writeObject(config);
            } catch (IOException e) {
                Logger.error("Failed to save window configuration: " + e.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
        public static Map<String, WindowState> loadConfig() {
            File configFile = new File(CONFIG_FILE);
            if (!configFile.exists()) {
                return new HashMap<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(CONFIG_FILE))) {
                return (Map<String, WindowState>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Logger.error("Failed to load window configuration: " + e.getMessage());
                return new HashMap<>();
            }
        }
    }
}