package gui;

import java.awt.GraphicsEnvironment;
import java.util.Observer;

import model.RobotModel;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
public class RobotModelTest
{
    public static void main(String[] args)
    {
        turnsShortestWayClockwise();
        turnsShortestWayCounterClockwise();
        staysInsideField();
        notifiesObserversWhenRobotMoves();
        doesNotNotifyWhenPaused();
        doesNotNotifyWhenTargetAlreadyReached();
        removedObserverIsNotCalled();
        formatsCoordinates();
        coordinatesWindowUnsubscribesOnDispose();
        System.out.println("ok");
    }

    private static void turnsShortestWayClockwise()
    {
        RobotModel model = new RobotModel();
        model.setTargetPosition(180, 90, 400, 400);
        model.update(400, 400);
        double direction = model.getRobotDirection();
        check(direction > Math.PI, "короткий поворот по часовой должен увести курс за pi, сейчас " + direction);
    }

    private static void turnsShortestWayCounterClockwise()
    {
        RobotModel model = new RobotModel();
        model.setTargetPosition(180, 110, 400, 400);
        model.update(400, 400);
        double direction = model.getRobotDirection();
        check(direction > 0 && direction < 1, "короткий поворот против часовой должен чуть увеличить курс, сейчас " + direction);
    }

    private static void staysInsideField()
    {
        RobotModel model = new RobotModel();
        int fieldWidth = 200;
        int fieldHeight = 200;
        model.setTargetPosition(10_000, 10_000, fieldWidth, fieldHeight);
        check(model.getTargetPositionX() <= 185, "цель по X должна остаться в поле");
        check(model.getTargetPositionY() <= 185, "цель по Y должна остаться в поле");
        for (int i = 0; i < 10_000; i++)
        {
            model.update(fieldWidth, fieldHeight);
        }
        check(model.getRobotPositionX() >= 15 && model.getRobotPositionX() <= 185,
                "робот вышел за поле по X: " + model.getRobotPositionX());
        check(model.getRobotPositionY() >= 15 && model.getRobotPositionY() <= 185,
                "робот вышел за поле по Y: " + model.getRobotPositionY());
    }

    private static void notifiesObserversWhenRobotMoves()
    {
        RobotModel model = new RobotModel();
        AtomicInteger calls = new AtomicInteger();
        model.addObserver((observable, argument) -> calls.incrementAndGet());
        model.update(400, 400);
        check(calls.get() == 1, "движение должно уведомить подписчика один раз, сейчас " + calls.get());
        check(model.getRobotPositionX() != 100 || model.getRobotDirection() != 0, "робот должен сдвинуться с начальной точки");
    }

    private static void doesNotNotifyWhenPaused()
    {
        RobotModel model = new RobotModel();
        model.setPaused(true);
        AtomicInteger calls = new AtomicInteger();
        model.addObserver((observable, argument) -> calls.incrementAndGet());
        model.update(400, 400);
        check(calls.get() == 0, "на паузе подписчиков уведомлять не нужно");
    }

    private static void doesNotNotifyWhenTargetAlreadyReached()
    {
        RobotModel model = new RobotModel();
        model.setTargetPosition(model.getRobotPositionX(), model.getRobotPositionY(), 400, 400);
        AtomicInteger calls = new AtomicInteger();
        model.addObserver((observable, argument) -> calls.incrementAndGet());
        model.update(400, 400);
        check(calls.get() == 0, "у цели лишних уведомлений быть не должно");
    }

    private static void removedObserverIsNotCalled()
    {
        RobotModel model = new RobotModel();
        AtomicInteger calls = new AtomicInteger();
        Observer observer = (observable, argument) -> calls.incrementAndGet();
        model.addObserver(observer);
        model.deleteObserver(observer);
        model.update(400, 400);
        check(calls.get() == 0, "отписанный наблюдатель не должен получать события");
    }

    private static void formatsCoordinates()
    {
        check("Робот: X = 100.00, Y = 100.50".equals(RobotCoordinatesWindow.formatPosition(100, 100.5)),
                RobotCoordinatesWindow.formatPosition(100, 100.5));
        check("Цель: X = 150.00, Y = 80.00".equals(RobotCoordinatesWindow.formatTarget(150, 80)),
                RobotCoordinatesWindow.formatTarget(150, 80));
        check("Курс: 0.0°".equals(RobotCoordinatesWindow.formatDirection(0)),
                RobotCoordinatesWindow.formatDirection(0));
        check("Курс: 180.0°".equals(RobotCoordinatesWindow.formatDirection(Math.PI)),
                RobotCoordinatesWindow.formatDirection(Math.PI));
    }

    private static void coordinatesWindowUnsubscribesOnDispose()
    {
        if (GraphicsEnvironment.isHeadless())
        {
            return;
        }
        RobotModel model = new RobotModel();
        RobotCoordinatesWindow window = new RobotCoordinatesWindow(model);
        check(model.countObservers() == 1, "окно координат должно подписаться на модель");
        window.dispose();
        check(model.countObservers() == 0, "закрытое окно должно отписаться от модели");
    }

    private static void check(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}