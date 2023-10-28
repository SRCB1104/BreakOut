package BreakOutGame;

import java.awt.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java.util.Random;
import java.util.Arrays;
import java.time.Duration;
import java.time.Instant;

public class BallRunner implements Runnable {

    public static final int MAX_X = 800;
    public static final int MAX_Y = 600;
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

    private int ballSpeedX = DX;
    private int ballSpeedY = DY;

    private boolean isPaused = false;
    private Instant startTime;

    public static final int MIN_SPEED = 1;

    public BallRunner(Shape shape, Shape paddle, Rectangle2D.Double[] blocks, Color[] blockColors) {
        ball = (Ellipse2D.Double) shape;
        ballX = 400;
        ballY = 300;
        ball.x = ballX;
        ball.y = ballY;
        this.paddle = (Rectangle2D.Double) paddle;
        this.blocks = blocks;
        this.blockColors = blockColors;

        int blockWidth = 80;
        int blockHeight = 20;
        int blockSpacingX = 10;
        int blocksPerRow = 8;
        int blockRows = 4;
        int initialX = (MAX_X - (blockWidth + blockSpacingX) * blocksPerRow) / 2;
        int initialY = 50;
        Random random = new Random();

        for (int i = 0; i < blocks.length; i++) {
            int row = i / blocksPerRow;
            int col = i % blocksPerRow;
            int x = initialX + col * (blockWidth + blockSpacingX);
            int y = initialY + row * (blockHeight + blockSpacingX);
            blocks[i] = new Rectangle2D.Double(x, y, blockWidth, blockHeight);
            blockColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }

    public void increaseBallSpeed() {
        ballSpeedX += 1;
        ballSpeedY += 1;
    }

    public void decreaseBallSpeed() {
        if (ballSpeedX > MIN_SPEED || ballSpeedY > MIN_SPEED) {
            ballSpeedX -= 1;
            ballSpeedY -= 1;
        }
    }

    @Override
    public void run() {
        startTime = Instant.now();
        int lives = 3;
        while (lives > 0) {
            if (!isPaused) {
                ballX = ballX + (ballSpeedX * ballDireccionX);
                ballY = ballY + (ballSpeedY * ballDireccionY);

                if (ballX < 0 || ballX > (MAX_X - 20)) {
                    ballDireccionX *= -1;
                }

                if (ballY < 0) {
                    ballDireccionY *= -1;
                } else if (ballY > (MAX_Y - 20)) {
                    lives--;
                    if (lives == 0) {
                        showGameOverDialog();
                        break;
                    } else {
                        ballX = 400;
                        ballY = 300;
                        ballDireccionX = 1;
                        ballDireccionY = 1;
                    }
                }

                if (ball.intersects(paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight())) {
                    ballDireccionY = -ballDireccionY;
                    ballY = (int) (paddle.getY() - ball.getHeight());
                    Board.updateScore(1);
                }

                for (int i = 0; i < blocks.length; i++) {
                    if (ball.intersects(blocks[i])) {
                        ballDireccionY = -ballDireccionY;
                        blocks[i] = new Rectangle2D.Double(0, 0, 0, 0);
                        Board.updateScore(1);
                    }
                }

                ball.x = ballX;
                ball.y = ballY;
            }

            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showGameOverDialog() {
        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        String message = "Time played: " + minutes + " minutes " + seconds + " seconds\nScore: " + Board.score;
        JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
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
    static boolean gameIsOver = false;

    private BallRunner ballRunner;

    private boolean isPaused = false;

    public Board() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        ball = new Ellipse2D.Double(400, 300, 20, 20);
        paddle = new Rectangle2D.Double(350, 550, 100, 10);

        blocks = new Rectangle2D.Double[32];
        blockColors = new Color[32];

        ballRunner = new BallRunner(ball, paddle, blocks, blockColors);
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

    @Override
    public void paintComponent(Graphics g) {
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

        if (gameIsOver) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String gameOverMessage = "Game Over";
            int textWidth = g2.getFontMetrics().stringWidth(gameOverMessage);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;
            g2.drawString(gameOverMessage, x, y);
        }
    }

    public Dimension getPreferredSize() {
        if (preferredSize == null) {
            return new Dimension(800, 600);
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
        while (!gameIsOver) {
            if (!isPaused) {
                repaint();
                SwingUtilities.invokeLater(() -> {
                    scoreLabel.setText("Score: " + score);
                });
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT && paddle.getX() > 0) {
            paddle.x -= 10;
        } else if (keyCode == KeyEvent.VK_RIGHT && paddle.getX() + paddle.getWidth() < getWidth()) {
            paddle.x += 10;
        } else if (keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_ADD) {
            ballRunner.increaseBallSpeed();
        } else if (keyCode == KeyEvent.VK_MINUS || keyCode == KeyEvent.VK_SUBTRACT) {
            ballRunner.decreaseBallSpeed();
        } else if (keyCode == KeyEvent.VK_R) {
            resetGame();
        } else if (keyCode == KeyEvent.VK_P) {
            isPaused = !isPaused;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void updateScore(int points) {
        score += points;
        scoreLabel.setText("Score: " + score);
    }

    public static void gameOver() {
        gameIsOver = true;
        scoreLabel.setText("Game Over");
    }

    private void resetGame() {
        ballRunner = new BallRunner(ball, paddle, blocks, blockColors);
        ballAnimator = new Thread(ballRunner, "BounceThread");
        ballAnimator.start();
        score = 0;
        Arrays.fill(blockColors, Color.BLACK);
        Arrays.fill(blocks, new Rectangle2D.Double());
        gameIsOver = false;
        isPaused = false;
        repaint();
    }
}






