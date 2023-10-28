import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.util.Random;

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

    private Rectangle2D.Double[] blocks;
    private Color[] blockColors;

    public BallRunner(Shape shape, Shape paddle, Rectangle2D.Double[] blocks, Color[] blockColors) {
        ball = (Ellipse2D.Double) shape;
        ballX = 320;
        ballY = 240;
        ball.x = ballX;
        ball.y = ballY;
        this.paddle = (Rectangle2D.Double) paddle;
        this.blocks = blocks;
        this.blockColors = blockColors;
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
                ballX = 320;
                ballY = 240;
                ballDireccionX = 1;
                ballDireccionY = 1;
            }

            for (int i = 0; i < blocks.length; i++) {
                if (ball.intersects(blocks[i])) {
                    ballDireccionY *= -1;
                    blocks[i] = new Rectangle2D.Double(0, 0, 0, 0);
                    Board.updateScore(10); // Actualiza la puntuación aquí
                }
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

    Rectangle2D.Double[] blocks;
    Color[] blockColors;

    static JLabel scoreLabel;
    static int score = 0;

    public Board() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        ball = new Ellipse2D.Double(20, 320, 20, 20);
        paddle = new Rectangle2D.Double(250, 450, 100, 10);

        blocks = new Rectangle2D.Double[25];
        blockColors = new Color[25];

        int blockWidth = 80;
        int blockHeight = 20;
        int blockSpacingX = 5;
        int blocksPerRow = 5;
        int blockRows = 5;
        int initialX = (BallRunner.MAX_X - (blockWidth + blockSpacingX) * blocksPerRow) / 2;
        int initialY = 50;
        Random random = new Random();

        for (int i = 0; i < 25; i++) {
            int row = i / blocksPerRow;
            int col = i % blocksPerRow;
            int x = initialX + col * (blockWidth + blockSpacingX);
            int y = initialY + row * (blockHeight + blockSpacingX);
            blocks[i] = new Rectangle2D.Double(x, y, blockWidth, blockHeight);
            blockColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        BallRunner ballRunner = new BallRunner(ball, paddle, blocks, blockColors);
        ballAnimator = new Thread(ballRunner, "BounceThread");
        ballAnimator.start();
        refresh = new Thread(this, "RefreshThread");
        refresh.start();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        scoreLabel = new JLabel("Score: " + score, JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(Color.BLACK);
        add(scoreLabel, BorderLayout.SOUTH);
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

        for (int i = 0; i < blocks.length; i++) {
            g2.setColor(blockColors[i]);
            g2.fill(blocks[i]);
        }
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

    public void run() {
        while (true) {
            repaint();
            SwingUtilities.invokeLater(() -> {
                scoreLabel.setText("Score: " + score);
            });
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


    public static void updateScore(int points) {
        score += points;
        scoreLabel.setText("Score: " + score);
    }
}
