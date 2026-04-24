package log;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
    
    private final ArrayDeque<LogEntry> m_messages;
    private final ArrayList<WeakReference<LogChangeListener>> m_listeners;
    private volatile LogChangeListener[] m_activeListeners;
    
    public LogWindowSource(int iQueueLength) 
    {
        m_iQueueLength = Math.max(1, iQueueLength);
        m_messages = new ArrayDeque<LogEntry>(m_iQueueLength);
        m_listeners = new ArrayList<WeakReference<LogChangeListener>>();
    }
    
    public void registerListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            removeCollectedListeners();
            for (WeakReference<LogChangeListener> listenerReference : m_listeners)
            {
                LogChangeListener activeListener = listenerReference.get();
                if (activeListener == listener)
                {
                    return;
                }
            }
            m_listeners.add(new WeakReference<LogChangeListener>(listener));
            m_activeListeners = null;
        }
    }
    
    public void unregisterListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            Iterator<WeakReference<LogChangeListener>> iterator = m_listeners.iterator();
            while (iterator.hasNext())
            {
                LogChangeListener activeListener = iterator.next().get();
                if (activeListener == null || activeListener == listener)
                {
                    iterator.remove();
                }
            }
            m_activeListeners = null;
        }
    }
    
    public void append(LogLevel logLevel, String strMessage)
    {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        synchronized (m_messages)
        {
            if (m_messages.size() >= m_iQueueLength)
            {
                m_messages.removeFirst();
            }
            m_messages.addLast(entry);
        }

        LogChangeListener [] activeListeners = m_activeListeners;
        if (activeListeners == null)
        {
            synchronized (m_listeners)
            {
                removeCollectedListeners();
                if (m_activeListeners == null)
                {
                    List<LogChangeListener> resolvedListeners = new ArrayList<LogChangeListener>(m_listeners.size());
                    for (WeakReference<LogChangeListener> listenerReference : m_listeners)
                    {
                        LogChangeListener listener = listenerReference.get();
                        if (listener != null)
                        {
                            resolvedListeners.add(listener);
                        }
                    }
                    activeListeners = resolvedListeners.toArray(new LogChangeListener [0]);
                    m_activeListeners = activeListeners;
                }
            }
        }
        for (LogChangeListener listener : activeListeners)
        {
            listener.onLogChanged();
        }
    }
    
    public int size()
    {
        synchronized (m_messages)
        {
            return m_messages.size();
        }
    }

    public Iterable<LogEntry> range(int startFrom, int count)
    {
        List<LogEntry> snapshot = snapshotMessages();
        if (startFrom < 0 || startFrom >= snapshot.size())
        {
            return Collections.emptyList();
        }
        int indexTo = Math.min(startFrom + count, snapshot.size());
        return snapshot.subList(startFrom, indexTo);
    }

    public Iterable<LogEntry> all()
    {
        return snapshotMessages();
    }

    private List<LogEntry> snapshotMessages()
    {
        synchronized (m_messages)
        {
            return new ArrayList<LogEntry>(m_messages);
        }
    }

    private void removeCollectedListeners()
    {
        Iterator<WeakReference<LogChangeListener>> iterator = m_listeners.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().get() == null)
            {
                iterator.remove();
            }
        }
    }
}
