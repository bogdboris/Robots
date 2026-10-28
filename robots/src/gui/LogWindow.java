package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

public class LogWindow extends JInternalFrame implements LogChangeListener
{
    private final Object m_subscriptionLock = new Object();
    private LogWindowSource m_logSource;
    private TextArea m_logContent;
    private volatile boolean m_subscribed;

    public LogWindow(LogWindowSource logSource)
    {
        super("Протокол работы", true, true, true, true);
        m_logSource = logSource;
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        synchronized (m_subscriptionLock)
        {
            m_subscribed = true;
            m_logSource.registerListener(this);
        }
        updateLogContent();
    }

    private void updateLogContent()
    {
        synchronized (m_subscriptionLock)
        {
            if (!m_subscribed)
            {
                return;
            }
            StringBuilder content = new StringBuilder();
            for (LogEntry entry : m_logSource.all())
            {
                content.append(entry.getMessage()).append("\n");
            }
            m_logContent.setText(content.toString());
            m_logContent.invalidate();
        }
    }

    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }

    @Override
    public void dispose()
    {
        synchronized (m_subscriptionLock)
        {
            if (m_subscribed)
            {
                m_logSource.unregisterListener(this);
                m_subscribed = false;
            }
        }
        super.dispose();
    }
} // синхронизировать
