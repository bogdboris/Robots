package log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class LogEntryQueue
{
    private final int m_limit;
    private final ArrayDeque<LogEntry> m_entries;

    LogEntryQueue(int limit)
    {
        m_limit = Math.max(1, limit);
        m_entries = new ArrayDeque<LogEntry>(m_limit);
    }

    synchronized void append(LogEntry entry)
    {
        if (m_entries.size() >= m_limit)
        {
            m_entries.removeFirst();
        }
        m_entries.addLast(entry);
    }

    synchronized int size()
    {
        return m_entries.size();
    }

    synchronized List<LogEntry> all()
    {
        return new ArrayList<LogEntry>(m_entries);
    }

    synchronized List<LogEntry> range(int startFrom, int count)
    {
        List<LogEntry> snapshot = new ArrayList<LogEntry>(m_entries);
        if (startFrom < 0 || startFrom >= snapshot.size())
        {
            return Collections.emptyList();
        }
        int indexTo = Math.min(startFrom + count, snapshot.size());
        return snapshot.subList(startFrom, indexTo);
    }
}