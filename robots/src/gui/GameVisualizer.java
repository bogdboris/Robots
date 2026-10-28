package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.JPanel;

import model.RobotModel;


//вынести создание модели
public class GameVisualizer extends JPanel
{
    private final RobotModel m_model;
    private GameController m_controller;

    public GameVisualizer(RobotModel model)
    {
        m_model = model;
        setDoubleBuffered(true);
    }

    public void bindController(GameController controller)
    {
        m_controller = controller;
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                m_controller.setTargetPosition(e.getPoint());
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                m_controller.setTargetPosition(e.getPoint());
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                m_controller.setTargetPosition(e.getPoint());
            }
        });
    }
    
    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }
    
    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g; 
        double robotX = m_model.getRobotPositionX();
        double robotY = m_model.getRobotPositionY();
        double robotDirection = m_model.getRobotDirection();
        double targetX = m_model.getTargetPositionX();
        double targetY = m_model.getTargetPositionY();
        List<Point> trail = m_model.getTrailSnapshot();

        drawTrail(g2d, trail);
        drawRobot(g2d, round(robotX), round(robotY), robotDirection);
        drawTarget(g2d, round(targetX), round(targetY));
    }
    
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        AffineTransform t = AffineTransform.getRotateInstance(direction, x, y); 
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, x, y, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, x + 10, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x + 10, y, 5, 5);
    }
    
    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0); 
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }

    private void drawTrail(Graphics2D g, List<Point> trail)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(new Color(70, 130, 180));
        for (int i = 1; i < trail.size(); i++) {
            Point prev = trail.get(i - 1);
            Point curr = trail.get(i);
            g.drawLine(prev.x, prev.y, curr.x, curr.y);
        }
    }

}
