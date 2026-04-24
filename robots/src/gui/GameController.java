package gui;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntSupplier;

public class GameController
{
    private static final int ROBOT_RADIUS = 15;
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.001;

    private final Timer m_timer = new Timer("events generator", true);
    private final RobotModel m_model;
    private final Runnable m_redrawRequest;
    private final IntSupplier m_widthSupplier;
    private final IntSupplier m_heightSupplier;

    public GameController(RobotModel model, Runnable redrawRequest, IntSupplier widthSupplier, IntSupplier heightSupplier)
    {
        m_model = model;
        m_redrawRequest = redrawRequest;
        m_widthSupplier = widthSupplier;
        m_heightSupplier = heightSupplier;

        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                m_redrawRequest.run();
            }
        }, 0, 50);

        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);
    }

    public void setTargetPosition(Point p)
    {
        m_model.setTargetPosition(clampToFieldX(p.x), clampToFieldY(p.y));
    }

    public void setPaused(boolean paused)
    {
        m_model.setPaused(paused);
    }

    public void clearTrail()
    {
        m_model.resetTrail();
    }

    public void setSpeedMultiplier(double speedMultiplier)
    {
        m_model.setSpeedMultiplier(applyLimits(speedMultiplier, 0.2, 2.0));
    }

    private void onModelUpdateEvent()
    {
        if (m_model.isPaused())
        {
            return;
        }
        double robotX = m_model.getRobotPositionX();
        double robotY = m_model.getRobotPositionY();
        double targetX = m_model.getTargetPositionX();
        double targetY = m_model.getTargetPositionY();
        double direction = m_model.getRobotDirection();

        double distance = distance(targetX, targetY, robotX, robotY);
        if (distance < 0.5)
        {
            return;
        }
        double velocity = MAX_VELOCITY * m_model.getSpeedMultiplier();
        double angleToTarget = angleTo(robotX, robotY, targetX, targetY);
        double angleDiff = asNormalizedRadians(angleToTarget - direction);
        if (angleDiff > Math.PI)
        {
            angleDiff -= 2 * Math.PI;
        }

        double angularVelocityLimit = MAX_ANGULAR_VELOCITY * m_model.getSpeedMultiplier();
        double angularVelocity = applyLimits(angleDiff / 10.0, -angularVelocityLimit, angularVelocityLimit);

        double headingFactor = Math.max(0.2, 1.0 - Math.abs(angleDiff) / Math.PI);
        velocity *= headingFactor;

        moveRobot(velocity, angularVelocity, 10, angularVelocityLimit);
    }

    private void moveRobot(double velocity, double angularVelocity, double duration, double angularVelocityLimit)
    {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY * 2.0);
        angularVelocity = applyLimits(angularVelocity, -angularVelocityLimit, angularVelocityLimit);

        double robotX = m_model.getRobotPositionX();
        double robotY = m_model.getRobotPositionY();
        double direction = m_model.getRobotDirection();

        double newX = robotX + velocity / angularVelocity *
            (Math.sin(direction + angularVelocity * duration) - Math.sin(direction));
        if (!Double.isFinite(newX))
        {
            newX = robotX + velocity * duration * Math.cos(direction);
        }
        double newY = robotY - velocity / angularVelocity *
            (Math.cos(direction + angularVelocity * duration) - Math.cos(direction));
        if (!Double.isFinite(newY))
        {
            newY = robotY + velocity * duration * Math.sin(direction);
        }

        newX = clampToFieldX(newX);
        newY = clampToFieldY(newY);
        double newDirection = asNormalizedRadians(direction + angularVelocity * duration);

        m_model.setRobotState(newX, newY, newDirection);
        m_model.appendTrailPoint(new Point(round(newX), round(newY)));
    }

    private double clampToFieldX(double x)
    {
        int maxX = Math.max(ROBOT_RADIUS, m_widthSupplier.getAsInt() - ROBOT_RADIUS);
        return applyLimits(x, ROBOT_RADIUS, maxX);
    }

    private double clampToFieldY(double y)
    {
        int maxY = Math.max(ROBOT_RADIUS, m_heightSupplier.getAsInt() - ROBOT_RADIUS);
        return applyLimits(y, ROBOT_RADIUS, maxY);
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
}
