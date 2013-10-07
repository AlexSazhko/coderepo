package com.oleksij.pinball.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;




import com.oleksij.pinball.ui.generator.BallGenerator;
import com.oleksij.pinball.ui.generator.BallGenerator.OnBallGeneratedCallback;
import com.oleksij.pinball.ui.shape.Ball;
import com.oleksij.pinball.ui.shape.Padle;

public class Screen extends JPanel {

	private static final long serialVersionUID = 1L;
	private ExecutorService pool;
	private List<Ball> balls;
	private Padle pad;
	private Bar bar;



	// package visibility if GameEngine and Screen are located in 1 package and
	// Screen is using only in this package catcher
	Screen() {
		balls = Collections.synchronizedList(new ArrayList<Ball>());
		pad = new Padle();
		bar = new Bar();
// Ù≥‚Ù≥‚Ù≥
		pool = Executors.newFixedThreadPool(2);

		BallGenerator gen = new BallGenerator();
		gen.setOnBallGeneratedCallback(new BallCatcher());
		

		pool.execute(gen);
		pool.execute(new BallMovementThread());
		pool.execute(new BallPadleThread());
		this.setFocusable(true);
		
		//addKeyListener(new myKeyAdapter());
		addMouseMotionListener(new myMouseListener());
		
		// hide cursor
		Cursor c1 = Toolkit.getDefaultToolkit().createCustomCursor((new ImageIcon(new byte[0])).getImage(), new Point(0,0),	"custom");
		setCursor(c1);

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		synchronized (balls) {
			for (Ball ball : balls) {
				int ballCoordinateX = ball.getBallCoordinateX();
				int ballCoordinateY = ball.getBallCoordinateY();
				
				ball.draw(g, ballCoordinateX, ballCoordinateY);
				
			}	
			
			int padCoordinateX = pad.getPadleCoordinateX();
			int padCoordinateY = pad.getPadleCoordinateY();
			
			pad.draw(g, padCoordinateX, padCoordinateY);
			bar.draw(g);

		}
	}

	private void doGame() {

		synchronized (balls) {
			for (Ball ball : balls) {
				ball.move2();
				ball.collision(pad);	
				Bar.setBallCount(balls);
			}
						
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					repaint();
				}
			});
		}

	}
	
	private void doPadle() {
		synchronized (pad) {
			
			pad.move();
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					repaint();
				}
			});
		}
	}	
	
	private class BallMovementThread implements Runnable {

		@Override
		public void run() {
			
			while (true) {
				doGame();
				//doPadle();
				doRemove();
				try {					
					TimeUnit.MILLISECONDS.sleep(20);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		
	}
	
	private void doRemove() {
		for (int i = 0; i < balls.size(); i++) {
			
			if(!balls.get(i).getIsAlive()) balls.remove(i);
			
		}
		
	}
	
	private class BallPadleThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				doPadle();
				try {
					TimeUnit.MILLISECONDS.sleep(1);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
public class myKeyAdapter extends KeyAdapter{
		
		public void keyPressed(KeyEvent e){
			pad.keyPressed(e);
		}
		
		public void keyReleased(KeyEvent e){
			pad.keyReleased(e);
		}
		
	}

public class myMouseListener extends MouseAdapter{
	public void mouseMoved(MouseEvent e){
		pad.mouseMoved(e);
	}
}
		

	private class BallCatcher implements OnBallGeneratedCallback {

		@Override
		public synchronized void onBallGenerated(Ball ball) {
			synchronized (balls) {
				balls.add(ball);
			}
		}
	}
}
