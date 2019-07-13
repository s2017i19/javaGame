package project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import project.GameMain;

public class Mole extends JFrame implements ActionListener {
	private JLabel bg, lb, gmlb;
	private JButton howto, start, setting;
	private Thread th;
	private JButton bm;
	private int rx;
	private int ry;
	int rand;
	private int score=0;
	private boolean click = true, check;
	private JButton bye, h1, h2, h3;
	int i=0;
	int time=1, speed=1;
	String name;
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	
	Mole() {
		setTitle("Mole");
		setSize(700,900);
		addWindowListener(new WindowHandler());
		setLayout(null);
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage("img/mole/hammer.png");
		Cursor c = toolkit.createCustomCursor(image, new Point(0,0), "img");
		setCursor (c);
		
		bg = new JLabel(new ImageIcon("img/mole/mole_bg.png"));
		bg.setBounds(0, 0, 700, 900);
		
		lb = new JLabel();
		lb.setBounds(0, 0, 700, 900);
		
		gmlb = new JLabel();
		gmlb.setBounds(0, 0, 700, 900);
		
		howto = new JButton(new ImageIcon("img/mole/howto.png"));
		howto.setBounds(20, 720, 180, 100);
		howto.setBorderPainted(false);
		howto.setContentAreaFilled(false);
		howto.setFocusPainted(false);
		bg.add(howto);
		
		start = new JButton(new ImageIcon("img/mole/start.png"));
		start.setBounds(240, 720, 180, 100);
		start.setBorderPainted(false);
		start.setContentAreaFilled(false);
		start.setFocusPainted(false);
		bg.add(start);
		
		setting = new JButton(new ImageIcon("img/mole/setting.png"));
		setting.setBounds(460, 720, 180, 100);
		setting.setBorderPainted(false);
		setting.setContentAreaFilled(false);
		setting.setFocusPainted(false);
		bg.add(setting);
		
		howto.addActionListener(this);
		start.addActionListener(this);
		setting.addActionListener(this);
		
		bm = new JButton(new ImageIcon("img/mole/BadMole.png"));
		bm.setBorderPainted(false);
		bm.setContentAreaFilled(false);
		bm.setFocusPainted(false);
		
		bm.addActionListener(this);
		
		lb.add(bm);
		bg.add(lb);
		lb.setVisible(false);
		
		h1 = new JButton(new ImageIcon("img/mole/heart.png"));
		h1.setBounds(490, 10, 60, 50);
		h1.setBorderPainted(false);
		h1.setContentAreaFilled(false);
		h1.setFocusPainted(false);
		bg.add(h1);
		
		h2 = new JButton(new ImageIcon("img/mole/heart.png"));
		h2.setBounds(550, 10, 60, 50);
		h2.setBorderPainted(false);
		h2.setContentAreaFilled(false);
		h2.setFocusPainted(false);
		bg.add(h2);
		
		h3 = new JButton(new ImageIcon("img/mole/heart.png"));
		h3.setBounds(610, 10, 60, 50);
		h3.setBorderPainted(false);
		h3.setContentAreaFilled(false);
		h3.setFocusPainted(false);
		bg.add(h3);
		
		add(bg);
		
		setVisible(true);
	}
	@Override
	public void actionPerformed (ActionEvent e) {
		if(e.getSource()==howto) {
			JOptionPane.showMessageDialog(this, "땅 속 어디선가 두더지가 튀어나온다! 튀어나오는 두더지를 클릭!"
					+"\n코가 남색인 두더지만 없애야 하며 갈수록 빨라진다!", "Howto", JOptionPane.PLAIN_MESSAGE);
		}
		else if (e.getSource()==bm) {
			repaint();
			click = true;
			if(check==false && rand==1) {
				score -= 20;
			}
			else if(check==false && rand!=1) {
				score += 10;
				speed += score;
			}
			check=true;
		}
		else if (e.getSource()==start) {
			repaint();
			start();
		}
		else if (e.getSource()==setting){
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image image;
			Cursor c;
			String tool, background;
			String[] tool_choice = {"망치(기본)", "곡괭이", "뿅망치"};
			String[] bg_choice = {"밭(기본)", "논두렁", "장난감"};
			
			tool = (String) JOptionPane.showInputDialog(null, "도구를 선택하세요", "스킨 설정하기",
					JOptionPane.PLAIN_MESSAGE, null, tool_choice, "선택해주세요.");
			background = (String) JOptionPane.showInputDialog(null, "배경을 선택하세요", "스킨 설정하기",
					JOptionPane.PLAIN_MESSAGE, null, bg_choice, "선택해주세요.");
			
			if(tool.equals("곡괭이")) {
				image = toolkit.getImage("img/mole/pick.png");
				c = toolkit.createCustomCursor(image, new Point(0,0), "img");
				setCursor(c);
			} else if(tool.equals("뿅망치")) {
				image = toolkit.getImage("img/mole/toy_hammer.png");
				c = toolkit.createCustomCursor(image, new Point(0,0), "img");
				setCursor(c);
			} else {
				image = toolkit.getImage("img/mole/hammer.png");
				c = toolkit.createCustomCursor(image, new Point(0,0), "img");
				setCursor(c);
			}
			if(background.equals("논두렁")) {
				bg.setIcon(new ImageIcon("img/mole/field.png"));
			} else if(background.equals("장난감")) {
				bg.setIcon(new ImageIcon("img/mole/toy.png"));
			} else {
				bg.setIcon(new ImageIcon("img/mole/mole_bg.png"));
			}
			repaint();
//			dispose();
//			exec.shutdown();
		}
	}
	
	public void start() {
		exec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				try {
					time = 1000/speed;
                	repaint();
                	chk();
                	
                	rand = (int)(Math.random()*8+1);
                	rx = (int)(Math.random()*480 +1);
        			ry = (int)(Math.random()*500 +40);
        			if (rand == 1) {
        				bm.setIcon(new ImageIcon("img/mole/GoodMole.png"));
        			}
        			else {
        				bm.setIcon(new ImageIcon("img/mole/BadMole.png"));
        			}
        			bm.setBounds(rx, ry, 210, 210);
    				lb.add(bm);
        			lb.setVisible(true);
        			repaint();
                	check = false;
                	click = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    exec.shutdown();
                }
			}
		},0, time, TimeUnit.SECONDS);
		lb.add(bm);
		lb.setVisible(true);
	}
	
	public void chk() {
		if(rand!=1 && i==0 && !click) {
    		i++;
    		h1.setVisible(false);
    		repaint();
    	} else if(rand!=1 && i==1 && !click) {
    		i++;
    		h2.setVisible(false);
    		repaint();
    	} else if(rand!=1 && i==2 && !click){
    		h3.setVisible(false);
    		name = (String) JOptionPane.showInputDialog(this,"총 "+score+"점 입니다. "
					+"이름을 입력해주세요.","Catch Mole",JOptionPane.PLAIN_MESSAGE,null,null,null);
    		try {
    			GameMain main = new GameMain();
    			
    			PreparedStatement ps = main.connection.prepareStatement("INSERT INTO mole VALUES(?, ?)");
    			ps.setString(1, name);
    			ps.setInt(2, score);
    			
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
    		exec.shutdown();
    	}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		g.setFont(new java.awt.Font("DialogInput",Font.BOLD,40));
		
		g.drawString("Score: " + score, 35, 90);
	}
    
	class WindowHandler extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.dispose();
			exec.shutdown();
		}
	}
}