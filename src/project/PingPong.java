package project;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class PingPong extends JFrame implements Runnable, ActionListener {
	public static final int WIDTH = 680;
	public static final int HEIGHT = 860;
	private size screen;
	public static int score=0;
	static Thread aThread;
	static int oldTime, time;
	
	public PingPong() {
		setTitle("PingPong");
		setSize(WIDTH + 20, HEIGHT + 40);
		screen = new size();
		add(screen);
		
		setVisible(true);
		
		addWindowListener(new WindowHandler());
		
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				switch (keyCode)	 {
				case 65:
					Aplayer.Aplayer_x -= 10;
					break;
				case 68:
					Aplayer.Aplayer_x += 10;
					break;
				case 37: //왼쪽 화살표
					Bplayer.Bplayer_x -= 10;
					break;
				case 39: //오른쪽 화살표
					Bplayer.Bplayer_x += 10;
					break;
				}
			}
		});
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {	}
		
			repaint(); // 새로운 프레임을 그린다.
		}
	}
	
	static class Aplayer extends JComponent {
		int Aplayer_width;
		static int Aplayer_height;
		static int Aplayer_x;
		
		public Aplayer(int w, int h) {
			Aplayer_width = w;
			Aplayer_height = h;
		}
	
		public void paint(Graphics g) {
			g.setColor(Color.BLUE);
		
			if(Aplayer_x < 0) {
				g.fillRect(0, 0, Aplayer_width, Aplayer_height);
				Aplayer_x = 0;
			} else if(Aplayer_x > PingPong.WIDTH-Aplayer_width) {
				g.fillRect(PingPong.WIDTH-Aplayer_width, 0, Aplayer_width, Aplayer_height);
				Aplayer_x = PingPong.WIDTH-Aplayer_width;
			} else {
				g.fillRect(Aplayer_x, 0, Aplayer_width, Aplayer_height);
			}
		}
	
	}
	
	static class Bplayer extends JComponent {
		int Bplayer_width;
		static int Bplayer_height;
		static int Bplayer_x;
	
		public Bplayer(int w, int h) {
			Bplayer_width = w;
			Bplayer_height = h;
		}
		
		public void paint(Graphics g) {
			g.setColor(Color.RED);
			
			if(Bplayer_x < 0) {
				g.fillRect(0, PingPong.HEIGHT-Bplayer_height*2, Bplayer_width, Bplayer_height);
				Bplayer_x = 0;
			} else if(Bplayer_x > PingPong.WIDTH-Bplayer_width) {
				g.fillRect(PingPong.WIDTH-Bplayer_width, PingPong.HEIGHT-Bplayer_height*2, Bplayer_width, Bplayer_height);
				Bplayer_x = PingPong.WIDTH-Bplayer_width;
			} else {
				g.fillRect(Bplayer_x, PingPong.HEIGHT-Bplayer_height*2, Bplayer_width, Bplayer_height);
			}
		}
	}
	 
	
	static class Ball {
		static int ball_x;
		int ball_y;
		int xInc = 1, yInc = 1;
		int ball_size;
	
		public Ball(int d) {
			this.ball_size = d;
			
			ball_x = (int) (Math.random() * (PingPong.WIDTH - d) + 3);
			ball_y = (int) (Math.random() * (PingPong.HEIGHT - d) + 3);
			
			xInc = (int) (Math.random() * 3 + 1);
			yInc = (int) (Math.random() * 3 + 1);
		
		}
	
		public void paint(Graphics g) {
			String name;
			boolean end = false;
			String who = null;
			
			if (ball_y <= 0) {
				aThread.suspend();
				time = (int) System.currentTimeMillis()/1000;
				time = time - oldTime;
				end = true;
				who = "Aplayer";
			}
			else if (ball_y+ball_size >= PingPong.HEIGHT-ball_size-Bplayer.Bplayer_height && !(Bplayer.Bplayer_x < ball_x && ball_x < Bplayer.Bplayer_x+130)) {
				aThread.suspend();
				time = (int) System.currentTimeMillis()/1000;
				time = time - oldTime;
				end = true;
				who = "Bplayer";
			} else {
				if (ball_x <= 0 || ball_x > PingPong.WIDTH - ball_size) xInc = -xInc;
				if (ball_y <= Aplayer.Aplayer_height || ball_y+20 > (PingPong.HEIGHT - ball_size - Bplayer.Bplayer_height)) yInc = -yInc;
				ball_x += xInc;
				ball_y += yInc;
				g.setColor(Color.BLACK);
				g.fillOval(ball_x, ball_y, ball_size, ball_size);
			}
			
			if(end) {
				name = (String) JOptionPane.showInputDialog(null,who+"가 " + time + "초 만에 승리하였습니다." + who +
						"의 이름을 입력해주세요.","PingPong",JOptionPane.PLAIN_MESSAGE,null,null,null);
				try {
	    			GameMain main = new GameMain();
	    			
	    			PreparedStatement ps = main.connection.prepareStatement("INSERT INTO pingpong VALUES(?, ?)");
	    			ps.setString(1, name);
	    			ps.setInt(2, time);
	    			
	    			int res = ps.executeUpdate();
	    			if(res == 1) {
	    				System.out.println("성공");
	    			}
	    			
	    			ps.close();
	    			main.connection.close();
	    			
	    		} catch (ClassNotFoundException e) {
	    			e.printStackTrace();
	    		} catch (SQLException e) {
	    			e.printStackTrace();
	    		}
				aThread.stop();
			}
		}
	}
	
	class size extends JComponent {
	
		public Ball basket[] = new Ball[1];
		public Aplayer Aplayer[] = new Aplayer[1];
		public Bplayer Bplayer[] = new Bplayer[1];
	
		public size() { //크기
			basket[0] = new Ball(30);
			Aplayer[0] = new Aplayer(130,15);
			Bplayer[0] = new Bplayer(130,15);
		}
	
		public void paint(Graphics g) {
		for (Ball b : basket) b.paint(g);
		for (Aplayer r1 : Aplayer) r1.paint(g);
		for (Bplayer r2 : Bplayer) r2.paint(g);
		}
	}
	
	class WindowHandler extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}