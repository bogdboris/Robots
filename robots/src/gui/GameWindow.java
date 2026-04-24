package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;
    private final JButton m_pauseButton;

    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer();

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        m_pauseButton = new JButton("Пауза");
        m_pauseButton.addActionListener((e) -> togglePause());

        JButton clearTrailButton = new JButton("Очистить след");
        clearTrailButton.addActionListener((e) -> m_visualizer.clearTrail());

        JSlider speedSlider = new JSlider(20, 200, 100);
        speedSlider.setToolTipText("Скорость робота (%)");
        speedSlider.addChangeListener((e) -> m_visualizer.setSpeedMultiplier(speedSlider.getValue() / 100.0));

        controlsPanel.add(m_pauseButton);
        controlsPanel.add(clearTrailButton);
        controlsPanel.add(speedSlider);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(controlsPanel, BorderLayout.NORTH);
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }

    private void togglePause()
    {
        boolean pause = "Пауза".equals(m_pauseButton.getText());
        m_visualizer.setPaused(pause);
        m_pauseButton.setText(pause ? "Продолжить" : "Пауза");
    }
}
