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
    public static final int SIGNO = -1;

    public static final int DX = 10;
    public static final int DY = 10;
    private Ellipse2D.Double bola;

    private int bolaX;
    private int bolaY;
    private int direccionXBola = 1;
    private int direccionYBola = 1;

    private Rectangle2D.Double Raqueta;

    private Rectangle2D.Double[] bloques;
    private Color[] coloresBloque;

    private int velocidadXBola = DX;
    private int velocidadYBola = DY;

    private boolean estaEnPausa = false;
    private Instant tiempoInicio;
    private Duration tiempoTotalEnPausa = Duration.ZERO;

    public static final int VELOCIDAD_MINIMA = 1;
    private int vidas;

    public BallRunner(Shape formaBola, Shape Raqueta, Rectangle2D.Double[] bloques, Color[] coloresBloque) {
        bola = (Ellipse2D.Double) formaBola;
        bolaX = 400;
        bolaY = 300;
        bola.x = bolaX;
        bola.y = bolaY;
        this.Raqueta = (Rectangle2D.Double) Raqueta;
        this.bloques = bloques;
        this.coloresBloque = coloresBloque;

        vidas = 3;

        int anchoBloque = 80;
        int altoBloque = 20;
        int espaciadoXBloque = 10;
        int bloquesPorFila = 8;
        int filasBloques = 4;
        int xInicial = (MAX_X - (anchoBloque + espaciadoXBloque) * bloquesPorFila) / 2;
        int yInicial = 50;
        Random Random = new Random();

        for (int i = 0; i < bloques.length; i++) {
            int fila = i / bloquesPorFila;
            int col = i % bloquesPorFila;
            int x = xInicial + col * (anchoBloque + espaciadoXBloque);
            int y = yInicial + fila * (altoBloque + espaciadoXBloque);
            bloques[i] = new Rectangle2D.Double(x, y, anchoBloque, altoBloque);
            coloresBloque[i] = new Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256));
        }
    }

    public void pausarJuego() {
        if (estaEnPausa) {
            estaEnPausa = false;
        } else {
            estaEnPausa = true;
            tiempoInicio = Instant.now();
        }
    }

    public void aumentarVelocidadBola() {
        velocidadXBola += 1;
        velocidadYBola += 1;
    }

    public void disminuirVelocidadBola() {
        if (velocidadXBola > VELOCIDAD_MINIMA || velocidadYBola > VELOCIDAD_MINIMA) {
            velocidadXBola -= 1;
            velocidadYBola -= 1;
        }
    }

    @Override
    public void run() {
        tiempoInicio = Instant.now();

        while (vidas > 0) {
            if (!estaEnPausa) {

                Instant tiempoActual = Instant.now();
                Duration tiempoTranscurrido = Duration.between(tiempoInicio, tiempoActual).minus(tiempoTotalEnPausa);

                long minutos = tiempoTranscurrido.toMinutes();
                long segundos = tiempoTranscurrido.minusMinutes(minutos).getSeconds();

                bolaX = bolaX + (velocidadXBola * direccionXBola);
                bolaY = bolaY + (velocidadYBola * direccionYBola);

                if (bolaX < 0 || bolaX > (MAX_X - 20)) {
                    direccionXBola *= -1;
                }

                if (bolaY < 0) {
                    direccionYBola *= -1;
                } else if (bolaY > (MAX_Y - 20)) {
                    vidas--;
                    if (vidas == 0) {
                        mostrarDialogoFinDeJuego();
                        break;
                    } else {
                        bolaX = 400;
                        bolaY = 300;
                        direccionXBola = 1;
                        direccionYBola = 1;
                    }
                }

                if (bola.intersects(Raqueta.getX(), Raqueta.getY(), Raqueta.getWidth(), Raqueta.getHeight())) {
                    direccionYBola = -direccionYBola;
                    bolaY = (int) (Raqueta.getY() - bola.getHeight());
                    Tablero.actualizarPuntuacion(1);
                }

                for (int i = 0; i < bloques.length; i++) {
                    if (bola.intersects(bloques[i])) {
                        direccionYBola = -direccionYBola;
                        bloques[i] = new Rectangle2D.Double(0, 0, 0, 0);
                        Tablero.actualizarPuntuacion(1);
                    }
                }

                boolean todosBloquesEliminados = true;
                for (int i = 0; i < bloques.length; i++) {
                    if (bloques[i].getWidth() > 0) {
                        todosBloquesEliminados = false;
                        break;
                    }
                }
                if (todosBloquesEliminados) {
                    Tablero.juegoGanado = true;
                }

                bola.x = bolaX;
                bola.y = bolaY;
            }

            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getVidas() {
        return vidas;
    }

    private void mostrarDialogoFinDeJuego() {
        Instant tiempoFinal = Instant.now();
        Duration duracion = Duration.between(tiempoInicio, tiempoFinal);
        long minutos = duracion.toMinutes();
        long segundos = duracion.minusMinutes(minutos).getSeconds();

        String mensaje = "Tiempo jugado: " + minutos + " minutos " + segundos + " segundos\nPuntuación: " + Tablero.puntuacion;
        JOptionPane.showMessageDialog(null, mensaje, "Fin del Juego", JOptionPane.INFORMATION_MESSAGE);
    }
}

class Tablero extends JComponent implements Runnable, KeyListener {
    Dimension tamañoPreferido = null;
    Ellipse2D.Double bola;
    Rectangle2D.Double Raqueta;
    Thread animadorBola;
    Thread refrescar;
    Rectangle2D.Double[] bloques;
    Color[] coloresBloque;
    static JLabel etiquetaPuntuacion;
    static int puntuacion = 0;
    static boolean juegoTerminado = false;
    static boolean juegoGanado = false;

    private BallRunner corredorBola;
    private boolean estaEnPausa = false;
    private JLabel etiquetaVidas;

    public Tablero() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
        bola = new Ellipse2D.Double(400, 300, 20, 20);
        Raqueta = new Rectangle2D.Double(350, 550, 100, 10);

        bloques = new Rectangle2D.Double[32];
        coloresBloque = new Color[32];

        corredorBola = new BallRunner(bola, Raqueta, bloques, coloresBloque);
        animadorBola = new Thread(corredorBola, "HiloBola");
        animadorBola.start();
        refrescar = new Thread(this, "HiloRefrescar");
        refrescar.start();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        etiquetaPuntuacion = new JLabel("Puntuación: " + puntuacion, JLabel.CENTER);
        etiquetaPuntuacion.setFont(new Font("Arial", Font.BOLD, 16));
        etiquetaPuntuacion.setForeground(Color.BLACK);
        add(etiquetaPuntuacion, BorderLayout.SOUTH);

        etiquetaVidas = new JLabel("Vidas: " + corredorBola.getVidas(), JLabel.CENTER);
        etiquetaVidas.setFont(new Font("Arial", Font.BOLD, 16));
        etiquetaVidas.setForeground(Color.BLACK);
        add(etiquetaVidas, BorderLayout.NORTH);
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
        g2.fill(bola);

        g2.setColor(Color.BLACK);
        g2.fill(Raqueta);

        for (int i = 0; i < bloques.length; i++) {
            g2.setColor(coloresBloque[i]);
            g2.fill(bloques[i]);
        }

        if (juegoTerminado) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String mensajeFinDeJuego = "Fin del Juego";
            int anchoTexto = g2.getFontMetrics().stringWidth(mensajeFinDeJuego);
            int x = (getWidth() - anchoTexto) / 2;
            int y = getHeight() / 2;
            g2.drawString(mensajeFinDeJuego, x, y);
        } else if (juegoGanado) {
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String mensajeVictoria = "¡Has Ganado!";
            int anchoTexto = g2.getFontMetrics().stringWidth(mensajeVictoria);
            int x = (getWidth() - anchoTexto) / 2;
            int y = getHeight() / 2;
            g2.drawString(mensajeVictoria, x, y);
        }
    }

    public Dimension getPreferredSize() {
        if (tamañoPreferido == null) {
            return new Dimension(800, 600);
        } else {
            return super.getPreferredSize();
        }
    }

    public void setPreferredSize(Dimension nuevoTamañoPreferido) {
        tamañoPreferido = nuevoTamañoPreferido;
        super.setPreferredSize(nuevoTamañoPreferido);
    }

    @Override
    public void run() {
        while (!juegoTerminado && !juegoGanado) {
            if (!estaEnPausa) {
                repaint();
                SwingUtilities.invokeLater(() -> {
                    etiquetaPuntuacion.setText("Puntuación: " + puntuacion);
                    etiquetaVidas.setText("Vidas: " + corredorBola.getVidas());
                    if (juegoGanado) {
                        mostrarDialogoVictoria();
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

        if (keyCode == KeyEvent.VK_LEFT && Raqueta.getX() > 0) {
            Raqueta.x -= 10;
        } else if (keyCode == KeyEvent.VK_RIGHT && Raqueta.getX() + Raqueta.getWidth() < getWidth()) {
            Raqueta.x += 10;
        } else if (keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_ADD) {
            corredorBola.aumentarVelocidadBola();
        } else if (keyCode == KeyEvent.VK_MINUS || keyCode == KeyEvent.VK_SUBTRACT) {
            corredorBola.disminuirVelocidadBola();
        } else if (keyCode == KeyEvent.VK_R) {
            reiniciarJuego();
        } else if (keyCode == KeyEvent.VK_P) {
            //estaEnPausa = !estaEnPausa;
            corredorBola.pausarJuego();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            corredorBola.pausarJuego();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void actualizarPuntuacion(int puntos) {
        puntuacion += puntos;
        etiquetaPuntuacion.setText("Puntuación: " + puntuacion);
    }

    public static void juegoTerminado() {
        juegoTerminado = true;
        etiquetaPuntuacion.setText("Fin del Juego");
    }

    private void reiniciarJuego() {
        corredorBola = new BallRunner(bola, Raqueta, bloques, coloresBloque);
        animadorBola = new Thread(corredorBola, "HiloBola");
        animadorBola.start();
        puntuacion = 0;
        Arrays.fill(coloresBloque, Color.BLACK);
        Arrays.fill(bloques, new Rectangle2D.Double());
        juegoTerminado = false;
        juegoGanado = false;
        estaEnPausa = false;
        repaint();
    }

    private void mostrarDialogoVictoria() {
        JFrame marcoVictoria = new JFrame("¡Has Ganado!");
        marcoVictoria.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel etiquetaVictoria = new JLabel("¡Has Ganado!", JLabel.CENTER);
        etiquetaVictoria.setFont(new Font("Arial", Font.BOLD, 40));

        marcoVictoria.add(etiquetaVictoria);
        marcoVictoria.setSize(300, 200);
        marcoVictoria.setLocationRelativeTo(null);
        marcoVictoria.setVisible(true);
    }
}
