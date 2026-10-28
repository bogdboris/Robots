package gui;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntSupplier;

import model.RobotModel;


//вынести координаты в модель
public class GameController
{
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
        m_model.setTargetPosition(p.x, p.y, m_widthSupplier.getAsInt(), m_heightSupplier.getAsInt());
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
        m_model.update(m_widthSupplier.getAsInt(), m_heightSupplier.getAsInt());
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
