package gui;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.RobotModel;

@SuppressWarnings("deprecation")
public class RobotCoordinatesWindow extends JInternalFrame implements Observer
{
    private final RobotModel m_model;
    private final JLabel m_positionLabel = new JLabel();
    private final JLabel m_directionLabel = new JLabel();
    private final JLabel m_targetLabel = new JLabel();
    private volatile boolean m_subscribed;

    public RobotCoordinatesWindow(RobotModel model)
    {
        super("Координаты робота", true, true, true, true);
        m_model = model;

        JPanel panel = new JPanel(new GridLayout(3, 1, 8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(m_positionLabel);
        panel.add(m_directionLabel);
        panel.add(m_targetLabel);
        getContentPane().add(panel);

        m_subscribed = true;
        m_model.addObserver(this);
        refresh();
        pack();
    }

    @Override
    public void update(Observable observable, Object argument)
    {
        EventQueue.invokeLater(this::refreshIfSubscribed);
    }

    @Override
    public void dispose()
    {
        if (m_subscribed)
        {
            m_subscribed = false;
            m_model.deleteObserver(this);
        }
        super.dispose();
    }

    private void refreshIfSubscribed()
    {
        if (m_subscribed)
        {
            refresh();
        }
    }

    private void refresh()
    {
        m_positionLabel.setText(formatPosition(m_model.getRobotPositionX(), m_model.getRobotPositionY()));
        m_directionLabel.setText(formatDirection(m_model.getRobotDirection()));
        m_targetLabel.setText(formatTarget(m_model.getTargetPositionX(), m_model.getTargetPositionY()));
    }

    static String formatPosition(double x, double y)
    {
        return String.format(Locale.US, "Робот: X = %.2f, Y = %.2f", x, y);
    }

    static String formatDirection(double radians)
    {
        return String.format(Locale.US, "Курс: %.1f°", Math.toDegrees(radians));
    }

    static String formatTarget(double x, double y)
    {
        return String.format(Locale.US, "Цель: X = %.2f, Y = %.2f", x, y);
    }
}