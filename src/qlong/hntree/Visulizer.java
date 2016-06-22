package qlong.hntree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Visulizer extends JComponent {

    private static final long serialVersionUID = 1L;

    private static class Rec {
        final int x;
        final int y;
        final int w;
        final int h;
        final Color color;

        public Rec(int x, int y, int w, int h, Color color) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = color;
        }
    }


    private final LinkedList<Rec> recs = new LinkedList<Rec>();

    public void addRec(float x, float y, float w, float h, Color color) {
        addRec((int) x, (int) y, (int) w, (int) h, color);
    }

    public void addRec(int x, int y, int w, int h) {
        addRec(x, y, w, h, Color.black);
    }

    public void addRec(int x, int y, int w, int h, Color color) {
        recs.add(new Rec(x, y, w, h, color));
        repaint();
    }

    public void clearLines() {
        recs.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Rec rec : recs) {
            g.setColor(rec.color);
            g.drawRect(rec.x, rec.y, rec.w, rec.h);
        }
    }

    public static void main(String[] args) {
        start();
    }

    public static Visulizer comp;
    public static void start() {
        JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        comp = new Visulizer();
        comp.setPreferredSize(new Dimension(500, 500));
        testFrame.getContentPane().add(comp, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        JButton newLineButton = new JButton("New Line");
        JButton clearButton = new JButton("Clear");
        buttonsPanel.add(newLineButton);
        buttonsPanel.add(clearButton);
        testFrame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        // try a point
        // comp.addRec(0, 0, 20, 200, Color.black);

        newLineButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int x = (int) (Math.random() * 320);
                int y = (int) (Math.random() * 320);
                int w = (int) (Math.random() * 20);
                int h = (int) (Math.random() * 20);
                Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
                comp.addRec(x, y, w, h, randomColor);
            }
        });
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                comp.clearLines();
            }
        });
        testFrame.pack();
        testFrame.setVisible(true);
    }


}