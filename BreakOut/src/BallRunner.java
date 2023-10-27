import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public class BallRunner implements Runnable {

    public static final int MAX_X = 640;
    public static final int MAX_Y = 480;
    public static final int SIGN = -1;

    public static final int DX = 10;
    public static final int DY = 10;
    private Ellipse2D.Double ball;

    private int ballX;
    private int ballY;
    private int ballDireccionX = 1;
    private int ballDireccionY = 1;

    private Rectangle2D.Double paddle;

    public BallRunner(Shape shape, Shape paddle) {
        ball = (Ellipse2D.Double) shape;
        ballX = 320;
        ballY = 240;
        ball.x = ballX;
        ball.y = ballY;
        this.paddle = (Rectangle2D.Double) paddle;
    }

    @Override
    public void run() {
        while (true) {
            ballX = ballX + (DX * ballDireccionX);
            ballY = ballY + (DY * ballDireccionY);

            if (ballX < 0 || ballX > (MAX_X - 20)) {
                ballDireccionX *= -1;
            }

            if (ballY < 0) {
                ballDireccionY *= -1;
            }

            if (ball.intersects(paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight())) {
                ballDireccionY = -1;
            } else if (ballY > (MAX_Y - 20)) {
                // Perdido - reiniciar
                ballX = 320;
                ballY = 240;
                ballDireccionX = 1;
                ballDireccionY = 1;
            }

            ball.x = ballX;
            ball.y = ballY;

            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Board extends JComponent implements Runnable, KeyListener {
    Dimension preferredSize = null;
    Ellipse2D.Double ball;
    Rectangle2D.Double paddle;

    Thread ballAnimator;
    Thread refresh;

    public Board() {
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        ball = new Ellipse2D.Double(20, 320, 20, 20);
        paddle = new Rectangle2D.Double(250, 450, 100, 10);
        BallRunner ballRunner = new BallRunner(ball, paddle);
        ballAnimator = new Thread(ballRunner, "BounceThread");
        ballAnimator.start();
        refresh = new Thread(this, "RefreshThread");
        refresh.start();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());
        }

        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(new BasicStroke(5.0f));

        g2.setColor(Color.RED);
        g2.fill(ball);

        g2.setColor(Color.BLACK);
        g2.fill(paddle);
    }

    public Dimension getPreferredSize() {
        if (preferredSize == null) {
            return new Dimension(640, 480);
        } else {
            return super.getPreferredSize();
        }
    }

    public void setPreferredSize(Dimension newPrefSize) {
        preferredSize = newPrefSize;
        super.setPreferredSize(newPrefSize);
    }

    @Override
    public void run() {
        while (true) {
            repaint();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT && paddle.getX() > 0) {
            paddle.x -= 10;
        } else if (keyCode == KeyEvent.VK_RIGHT && paddle.getX() + paddle.getWidth() < getWidth()) {
            paddle.x += 10;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
