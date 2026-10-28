package log;

import java.util.WeakHashMap;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он 
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено 
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений 
 * ограниченного размера) 
 */
public class LogWindowSource
{
    private int m_iQueueLength;

    private final LogEntryQueue m_messages;
    private final WeakHashMap<LogChangeListener, Boolean> m_listeners; // weakhashmap

    public LogWindowSource(int iQueueLength)
    {
        m_iQueueLength = Math.max(1, iQueueLength);
        m_messages = new LogEntryQueue(m_iQueueLength);
        m_listeners = new WeakHashMap<LogChangeListener, Boolean>();
    }

    public void registerListener(LogChangeListener listener)
    {
        synchronized (m_listeners)
        {
            m_listeners.put(listener, Boolean.TRUE);
        }
    }

    public void unregisterListener(LogChangeListener listener)
    {
        synchronized (m_listeners)
        {
            m_listeners.remove(listener);
        }
    }

    public void append(LogLevel logLevel, String strMessage) //как отдельная коллекция
    {
        m_messages.append(new LogEntry(logLevel, strMessage));

        LogChangeListener[] activeListeners;
        synchronized (m_listeners)
        {
            activeListeners = m_listeners.keySet().toArray(new LogChangeListener[0]);
        }
        for (LogChangeListener listener : activeListeners)
        {
            listener.onLogChanged();
        }
    }

    public int size()
    {
        return m_messages.size();
    }

    public Iterable<LogEntry> range(int startFrom, int count)
    {
        return m_messages.range(startFrom, count);
    }

    public Iterable<LogEntry> all()
    {
        return m_messages.all();
    }
}
