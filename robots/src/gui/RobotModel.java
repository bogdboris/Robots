package gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RobotModel
{
    private static final int TRAIL_LIMIT = 2000;

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private volatile double m_targetPositionX = 150;
    private volatile double m_targetPositionY = 100;
    private volatile boolean m_paused = false;
    private volatile double m_speedMultiplier = 1.0;

    private final List<Point> m_robotTrail = new ArrayList<>();

    public RobotModel()
    {
        resetTrail();
    }

    public double getRobotPositionX() { return m_robotPositionX; }
    public double getRobotPositionY() { return m_robotPositionY; }
    public double getRobotDirection() { return m_robotDirection; }
    public double getTargetPositionX() { return m_targetPositionX; }
    public double getTargetPositionY() { return m_targetPositionY; }
    public boolean isPaused() { return m_paused; }
    public double getSpeedMultiplier() { return m_speedMultiplier; }

    public void setRobotState(double x, double y, double direction)
    {
        m_robotPositionX = x;
        m_robotPositionY = y;
        m_robotDirection = direction;
    }

    public void setTargetPosition(double x, double y)
    {
        m_targetPositionX = x;
        m_targetPositionY = y;
    }

    public void setPaused(boolean paused)
    {
        m_paused = paused;
    }

    public void setSpeedMultiplier(double speedMultiplier)
    {
        m_speedMultiplier = speedMultiplier;
    }

    public List<Point> getTrailSnapshot()
    {
        synchronized (m_robotTrail)
        {
            return new ArrayList<>(m_robotTrail);
        }
    }

    public void appendTrailPoint(Point point)
    {
        synchronized (m_robotTrail)
        {
            if (!m_robotTrail.isEmpty())
            {
                Point lastPoint = m_robotTrail.get(m_robotTrail.size() - 1);
                if (lastPoint.distance(point) < 1.0)
                {
                    return;
                }
            }
            m_robotTrail.add(point);
            if (m_robotTrail.size() > TRAIL_LIMIT)
            {
                m_robotTrail.remove(0);
            }
        }
    }

    public void resetTrail()
    {
        synchronized (m_robotTrail)
        {
            m_robotTrail.clear();
            m_robotTrail.add(new Point(round(m_robotPositionX), round(m_robotPositionY)));
        }
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
}
