package BreakOutGame;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {
    public static void main(String[] args) {
        JFrame f = new JFrame("Break Out Game");
        JPanel p = new JPanel(new BorderLayout());
        Tablero Tablero = new Tablero();
        p.add(Tablero, "Center");
        f.addKeyListener(Tablero);
        f.setContentPane(p);
        f.setDefaultCloseOperation(3);
        f.pack();
        f.setVisible(true);
        f.setLocationRelativeTo((Component)null);
    }
}

// Se pone pausa con la letra "P"
// Con "+" aumentas la velocidad y con "-" disminuyes la velocidad
// Comienzas con 3 Vidas
