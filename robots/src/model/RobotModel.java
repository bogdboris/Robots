package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

@SuppressWarnings("deprecation")
public class RobotModel extends Observable
{
    private static final int TRAIL_LIMIT = 2000;
    private static final int ROBOT_RADIUS = 15;
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.001;

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

    public void setTargetPosition(double x, double y, int fieldWidth, int fieldHeight)
    {
        m_targetPositionX = clampToFieldX(x, fieldWidth);
        m_targetPositionY = clampToFieldY(y, fieldHeight);
        publishChange();
    }

    public void setPaused(boolean paused)
    {
        m_paused = paused;
    }

    public void setSpeedMultiplier(double speedMultiplier)
    {
        m_speedMultiplier = speedMultiplier;
    }

    public void update(int fieldWidth, int fieldHeight)
    {
        if (m_paused)
        {
            return;
        }
        double distance = distance(m_targetPositionX, m_targetPositionY, m_robotPositionX, m_robotPositionY);
        if (distance < 0.5)
        {
            return;
        }
        double velocity = MAX_VELOCITY * m_speedMultiplier;
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angleDiff = asNormalizedRadians(angleToTarget - m_robotDirection);
        if (angleDiff > Math.PI)
        {
            angleDiff -= 2 * Math.PI;
        }

        double angularVelocityLimit = MAX_ANGULAR_VELOCITY * m_speedMultiplier;
        double angularVelocity = applyLimits(angleDiff / 10.0, -angularVelocityLimit, angularVelocityLimit);

        double headingFactor = Math.max(0.2, 1.0 - Math.abs(angleDiff) / Math.PI);
        velocity *= headingFactor;

        moveRobot(velocity, angularVelocity, 10, angularVelocityLimit, fieldWidth, fieldHeight);
        publishChange();
    }

    public List<Point> getTrailSnapshot()
    {
        synchronized (m_robotTrail)
        {
            return new ArrayList<>(m_robotTrail);
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

    private void moveRobot(double velocity, double angularVelocity, double duration, double angularVelocityLimit,
                           int fieldWidth, int fieldHeight)
    {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY * 2.0);
        angularVelocity = applyLimits(angularVelocity, -angularVelocityLimit, angularVelocityLimit);

        double newX = m_robotPositionX + velocity / angularVelocity *
            (Math.sin(m_robotDirection + angularVelocity * duration) - Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
            (Math.cos(m_robotDirection + angularVelocity * duration) - Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }

        newX = clampToFieldX(newX, fieldWidth);
        newY = clampToFieldY(newY, fieldHeight);
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);

        m_robotPositionX = newX;
        m_robotPositionY = newY;
        m_robotDirection = newDirection;
        appendTrailPoint(new Point(round(newX), round(newY)));
    }

    private double clampToFieldX(double x, int fieldWidth)
    {
        int maxX = Math.max(ROBOT_RADIUS, fieldWidth - ROBOT_RADIUS);
        return applyLimits(x, ROBOT_RADIUS, maxX);
    }

    private double clampToFieldY(double y, int fieldHeight)
    {
        int maxY = Math.max(ROBOT_RADIUS, fieldHeight - ROBOT_RADIUS);
        return applyLimits(y, ROBOT_RADIUS, maxY);
    }

    private void appendTrailPoint(Point point)
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

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;
        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    private void publishChange()
    {
        setChanged();
        notifyObservers();
    }
}