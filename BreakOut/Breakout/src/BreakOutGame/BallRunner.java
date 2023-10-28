package BreakOutGame;

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

    private Rectangle2D.Double[] bloques;
    private Color[] ColorBloque;

    private int ballvelocidadX = DX;
    private int ballvelocidadY = DY;

    private boolean pausa = false;
    private Instant TiempoStart;
    private Duration TiempoPausa = Duration.ZERO;

    public static final int MIN_SPEED = 1;
    private int Vidas;

    public BallRunner(Shape shape, Shape paddle, Rectangle2D.Double[] bloques, Color[] ColorBloque) {
        ball = (Ellipse2D.Double) shape;
        ballX = 400;
        ballY = 300;
        ball.x = ballX;
        ball.y = ballY;
        this.paddle = (Rectangle2D.Double) paddle;
        this.bloques = bloques;
        this.ColorBloque = ColorBloque;

        Vidas = 3;

        int blockWidth = 80;
        int blockHeight = 20;
        int bloqueEspacioX = 10;
        int bloquesPerRow = 8;
        int blockRows = 4;
        int initialX = (MAX_X - (blockWidth + bloqueEspacioX) * bloquesPerRow) / 2;
        int initialY = 50;
        Random random = new Random();

        for (int i = 0; i < bloques.length; i++) {
            int row = i / bloquesPerRow;
            int col = i % bloquesPerRow;
            int x = initialX + col * (blockWidth + bloqueEspacioX);
            int y = initialY + row * (blockHeight + bloqueEspacioX);
            bloques[i] = new Rectangle2D.Double(x, y, blockWidth, blockHeight);
            ColorBloque[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
    }

    public void pauseGame() {
        if (pausa) {
            pausa = false;
        } else {
            pausa = true;
            TiempoStart = Instant.now();
        }
    }


    public void increaseBallSpeed() {
        ballvelocidadX += 1;
        ballvelocidadY += 1;
    }

    public void decreaseBallSpeed() {
        if (ballvelocidadX > MIN_SPEED || ballvelocidadY > MIN_SPEED) {
            ballvelocidadX -= 1;
            ballvelocidadY -= 1;
        }
    }

    @Override
    public void run() {
        TiempoStart = Instant.now();

        while (Vidas > 0) {
            if (!pausa) {

                Instant currenTime = Instant.now();
                Duration elapsedTime = Duration.between(TiempoStart,currenTime).minus(TiempoPausa);

                long minutos = elapsedTime.toMinutes();
                long segundos = elapsedTime.minusMinutes(minutos).getSeconds();

                ballX = ballX + (ballvelocidadX * ballDireccionX);
                ballY = ballY + (ballvelocidadY * ballDireccionY);

                if (ballX < 0 || ballX > (MAX_X - 20)) {
                    ballDireccionX *= -1;
                }

                if (ballY < 0) {
                    ballDireccionY *= -1;
                } else if (ballY > (MAX_Y - 20)) {
                    Vidas--;
                    if (Vidas == 0) {
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
                    Board.updatePuntuacion(1);
                }

                for (int i = 0; i < bloques.length; i++) {
                    if (ball.intersects(bloques[i])) {
                        ballDireccionY = -ballDireccionY;
                        bloques[i] = new Rectangle2D.Double(0, 0, 0, 0);
                        Board.updatePuntuacion(1);
                    }
                }

                boolean allbloquesCleared = true;
                for (int i = 0; i < bloques.length; i++) {
                    if (bloques[i].getWidth() > 0) {
                        allbloquesCleared = false;
                        break;
                    }
                }
                if (allbloquesCleared) {
                    Board.JuegoGanado = true;
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

    public int getVidas() {
        return Vidas;
    }

    private void showGameOverDialog() {
        Instant endTime = Instant.now();
        Duration duration = Duration.between(TiempoStart, endTime);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        String message = "Time played: " + minutes + " minutes " + seconds + " seconds\nPuntuacion: " + Board.Puntuacion;
        JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}

class Board extends JComponent implements Runnable, KeyListener {
    Dimension preferredSize = null;
    Ellipse2D.Double ball;
    Rectangle2D.Double paddle;
    Thread ballAnimacion;
    Thread refresh;
    Rectangle2D.Double[] bloques;
    Color[] ColorBloque;
    static JLabel PuntuacionLabel;
    static int Puntuacion = 0;
    static boolean JuegoAcaba = false;
    static boolean JuegoGanado = false;

    private BallRunner ballRunner;
    private boolean pausa = false;
    private JLabel VidasLabel;

    public Board() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        ball = new Ellipse2D.Double(400, 300, 20, 20);
        paddle = new Rectangle2D.Double(350, 550, 100, 10);

        bloques = new Rectangle2D.Double[32];
        ColorBloque = new Color[32];

        ballRunner = new BallRunner(ball, paddle, bloques, ColorBloque);
        ballAnimacion = new Thread(ballRunner, "BounceThread");
        ballAnimacion.start();
        refresh = new Thread(this, "RefreshThread");
        refresh.start();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        PuntuacionLabel = new JLabel("Puntuacion: " + Puntuacion, JLabel.CENTER);
        PuntuacionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        PuntuacionLabel.setForeground(Color.BLACK);
        add(PuntuacionLabel, BorderLayout.SOUTH);

        VidasLabel = new JLabel("Vidas: " + ballRunner.getVidas(), JLabel.CENTER);
        VidasLabel.setFont(new Font("Arial", Font.BOLD, 16));
        VidasLabel.setForeground(Color.BLACK);
        add(VidasLabel, BorderLayout.NORTH);
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

        for (int i = 0; i < bloques.length; i++) {
            g2.setColor(ColorBloque[i]);
            g2.fill(bloques[i]);
        }

        if (JuegoAcaba) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String gameOverMessage = "Game Over";
            int textWidth = g2.getFontMetrics().stringWidth(gameOverMessage);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;
            g2.drawString(gameOverMessage, x, y);
        } else if (JuegoGanado) {
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String winMessage = "You Win!";
            int textWidth = g2.getFontMetrics().stringWidth(winMessage);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;
            g2.drawString(winMessage, x, y);
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
        while (!JuegoAcaba && !JuegoGanado) {
            if (!pausa) {
                repaint();
                SwingUtilities.invokeLater(() -> {
                    PuntuacionLabel.setText("Puntuacion: " + Puntuacion);
                    VidasLabel.setText("Vidas: " + ballRunner.getVidas());
                    if (JuegoGanado){
                        showYouWinDialog();
                    }
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
            //pausa = !pausa;
            ballRunner.pauseGame();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            ballRunner.pauseGame();
    }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void updatePuntuacion(int points) {
        Puntuacion += points;
        PuntuacionLabel.setText("Puntuacion: " + Puntuacion);
    }

    public static void gameOver() {
        JuegoAcaba = true;
        PuntuacionLabel.setText("Game Over");
    }

    private void resetGame() {
        ballRunner = new BallRunner(ball, paddle, bloques, ColorBloque);
        ballAnimacion = new Thread(ballRunner, "BounceThread");
        ballAnimacion.start();
        Puntuacion = 0;
        Arrays.fill(ColorBloque, Color.BLACK);
        Arrays.fill(bloques, new Rectangle2D.Double());
        JuegoAcaba = false;
        JuegoGanado = false;
        pausa = false;
        repaint();
    }

    private void showYouWinDialog() {
        JFrame winFrame = new JFrame("You Win!");
        winFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel winLabel = new JLabel("You Win!", JLabel.CENTER);
        winLabel.setFont(new Font("Arial", Font.BOLD, 40));

        winFrame.add(winLabel);
        winFrame.setSize(300, 200);
        winFrame.setLocationRelativeTo(null);
        winFrame.setVisible(true);
    }
}









