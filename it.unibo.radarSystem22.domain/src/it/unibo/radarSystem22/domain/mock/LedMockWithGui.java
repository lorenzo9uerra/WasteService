package it.unibo.radarSystem22.domain.mock;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import it.unibo.radarSystem22.domain.interfaces.ILed;
import it.unibo.radarSystem22.domain.utils.BasicUtils;

import javax.swing.*;


public class LedMockWithGui extends LedMock {
    private static int delta        = 0;

    private Panel p ;
    private Frame frame;
    private final Dimension sizeOn  = new Dimension(300,300);
    private final Dimension sizeOff = new Dimension(200,200);
    public static ILed create(  ){
        return new LedMockWithGui( initFrame(500,500) );
    }
    public void destroyLedGui(  ){
        frame.dispose();
    }
    //Constructor
    public LedMockWithGui( Frame frame ) {
        super();
        //Colors.out("create LedMockWithGui");
        this.frame = frame;
        configure( );
    }
    protected void configure( ){
        p = new Panel();
        p.setSize( sizeOff );
        p.setBackground(Color.red);

        Panel containerPanel = new Panel();
        containerPanel.setSize(frame.getSize());
        frame.setLayout(new BorderLayout());
        frame.add(BorderLayout.CENTER, containerPanel);

        containerPanel.add(p);

//        delta = delta+50;
//        frame.setLocation(delta, delta);
        //p.validate();
        //this.frame.validate();
    }
    @Override //LedMock
    public void turnOn(){
        super.turnOn();
        p.setSize( sizeOn );
        p.setBackground(Color.red);
        p.validate();
    }
    @Override //LedMock
    public void turnOff() {
        super.turnOff();
        p.setSize( sizeOff );
        p.setBackground(Color.GRAY);
        p.validate();
        //p.revalidate();
    }

    //
    public static Frame initFrame(int dx, int dy){
        Frame frame         = new Frame();
        frame.setSize( new Dimension(dx,dy) );
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
        });
        frame.setVisible(true);
        return frame;

    }
    public static Frame initFrame(){
        return initFrame(400,200);
    }
}
