package project;

import java.awt.*;

import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import project.GameMain;

class Square {
	int x, y, c;

	Square(int x, int y, int c) { //�� ���
		this.x = x;
		this.y = y;
		this.c = c;
	}
		
	boolean InBackground() { //��� �ӿ� �ӹ������� üũ
		return ( x >= 0 && x < Tetris.col && y >= 0 && y < Tetris.row );
	}

	boolean IsEqual(Square s) {	//�׿��ִ� ����� ��谪 üũ
		return x == s.x && y == s.y && c == s.c;
	}
}

public class Tetris extends Frame implements Runnable, ActionListener {
	private JButton start, howto, stop;

	String name;
	Button btn;
	
	static boolean pause = false;
	static int block_size = 25; //����� �� ���� ũ��					
	static final int xoffset = 240;	//���(������) ���� ��ġ			
	static int col = 12; //����� ���� ��� ��(��)
	static int row = 23; //����� ���� ��� ��(��)
	int block_color[][]; //��� �ϳ��� ������ ���� �迭
	int preview = (int) (Math.random()*7); //����� ����� ����� ��(0~6) //�̸����� ���
	int block;
	static Square curpiece[] = new Square[4]; //���� ���
	static Square newblock[] = new Square[4]; //�� ���
	boolean lost; //������ ������ �Ǵ�		
	boolean neednewblock = true; //�� ����� �ʿ����� �Ǵ�					
	Thread killme = null; //stop �� ������ ���Ḧ ���� ���� ����		
	Color colors[];	//���� ���	
	int block_score, score = 0; //��� ������ ������ �ʱ�ȭ
	int level;
	int removeline;							
	
	public Tetris() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setTitle("Tetris");
				setLayout(null);
				setSize(700,900);
				
				howto = new JButton("Howto");
				howto.setBounds(45,340,110,30);
				howto.addActionListener(Tetris.this);
				add(howto);
				
				start = new JButton("Start");
				start.setBounds(45,385,110,30);
				start.addActionListener(Tetris.this);
				add(start);
				
				stop = new JButton("Stop");
				stop.setBounds(45,430,110,30);
				stop.addActionListener(Tetris.this);
				add(stop);
		
				addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						switch (e.getKeyCode())	 {
						case 37: //���� ȭ��ǥ
							move_block(-1, 0, false); //����� �������� �� ĭ ������
							neednewblock = false; //���ο� ����� ������ �ʵ��� ��
							repaint(); //���� �׸�
							break;
					
						case 39: //������ ȭ��ǥ
							move_block(1, 0, false); //����� ���������� �� ĭ
							neednewblock = false;
							repaint();
							break;
					
						case 38: //���� ȭ��ǥ(ȸ��)
							if(!neednewblock && block != 4) { 
								move_block(0,0,true); //��ǥ �����ϸ鼭 ȸ��
								repaint();
								neednewblock = false;
							}
							break;
							
						case 40: //�Ʒ��� ȭ��ǥ
							while(move_block(0,-1,false)); //�� �Ʒ��� ����
							repaint();
							break;
						}
					}
				});
				addWindowListener(new WindowHandler());
						
				block_color = new int[col][row + 4];					
															
				colors = new Color[8]; //���� �迭
				
				colors[0] = new Color(40,40,40); //������(���)
				colors[1] = new Color(255,0,0);	//������
				colors[2] = new Color(0,255,0);	//�ʷϻ�
				colors[3] = new Color(255,255,0); //�����
				colors[4] = new Color(97,158,255); //�ϴû�
				colors[5] = new Color(255,121,0); //��Ȳ��
				colors[6] = new Color(65,18,155); //�����
				colors[7] = new Color(0,0,250); //�Ķ���
				
				setVisible(true);
			} //run
		}); //thread ��¼��
	}; //tetris
	
		public void start() {
			for(int i=0; i< col; i++) {
				for(int j=0; j< row; j++) {
					block_color[i][j] = 0; //������(0��° �ε���)���� ��� �ϳ��ϳ��� ��ĥ��
				}
			}

			level = 1; //���� �ʱ�ȭ
			score = 0;
			removeline = 0;
			block_score = -1;
			neednewblock = true; //�� ����� ������ ��
			lost = false;
			repaint();
			(killme = new Thread(this)).start(); //������ ȣ��(������ ���߿� �����ų �� ���)
			requestFocus(); //��Ŀ���� ����
		} //start
	
		public void run() {
			while (!lost) {
				int time=level;
				try {
					if(neednewblock) {
						time=100;
						Thread.sleep(time);
					}
					Thread.sleep(1000/time);	//�����带 �̿��� �ӵ� ����
				}
				catch (InterruptedException e){	}

				if (neednewblock) {
					if (block_score > 0) {
						level = 1 + removeline/10;	//������ ���� ���� �������� ������ ����
					}
					removelines();
					newblock();	
					score += block_score;
					preview = (int) (Math.random()*7);
					neednewblock = false;		
				}
				else {
					if(!pause) {
					neednewblock = !move_block(0,-1,false);	// ������ �����带 �Ʒ��� �̵�
					requestFocus();
					}
				}
				repaint();
			}
		killme = null;

		name = (String) JOptionPane.showInputDialog(this,"�� "+score+"�� �Դϴ�. "+
		"�̸��� �Է����ּ���.","Tetris",JOptionPane.PLAIN_MESSAGE,null,null,null);

		try {
			GameMain main = new GameMain();
			
			PreparedStatement ps = main.connection.prepareStatement("INSERT INTO tetris VALUES(?, ?)");
			ps.setString(1, name);
			ps.setInt(2, score);
			
			int res = ps.executeUpdate();
			if(res == 1) {
				System.out.println("����");
			}
			
			ps.close();
			main.connection.close();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	} //run

	private void newblock() { //���ο� ����� ����
		Square old[] = new Square[4];
		old[0]  = old[1] = old[2] = old[3] = new Square(-1, -1, 0);

		int m = col/2;
		
		block = preview; //�̸����⿡�� ������ ����� ���
		switch (block) {
		case 0:
			//�� �� �� ��
			block_score = 10; //��ϴ� ����
			curpiece[0] = new Square(m  , row-1, 1);
			curpiece[1] = new Square(m-1, row-1, 1);
			curpiece[2] = new Square(m+1, row-1, 1);
			curpiece[3] = new Square(m+2, row-1, 1);
			break;
		case 1:
			//   �� �� ��
			//     ��
			block_score = 15;
			curpiece[0] = new Square(m  , row-1, 2);
			curpiece[1] = new Square(m-1, row-1, 2);
			curpiece[2] = new Square(m  , row-2, 2);
			curpiece[3] = new Square(m+1, row-1, 2); 
			break;
		case 2:
			//    �� ��
			//  �� ��
			block_score = 20;
			curpiece[0] = new Square(m  , row-2, 3 );
			curpiece[1] = new Square(m-1, row-2, 3);
			curpiece[2] = new Square(m  , row-1, 3);
			curpiece[3] = new Square(m+1, row-1, 3);
			break;
		case 3:
			// �� ��
			//   �� ��
			block_score = 20;
			curpiece[0] = new Square( m  , row-2, 4);
			curpiece[1] = new Square( m+1, row-2, 4);
			curpiece[2] = new Square( m-1, row-1, 4);
			curpiece[3] = new Square( m  , row-1, 4);
			break;
		case 4:
			//  �� ��
			//  �� ��
			block_score = 10;
			curpiece[0] = new Square( m-1, row-2, 5);
			curpiece[1] = new Square( m  , row-2, 5);
			curpiece[2] = new Square( m-1, row-1, 5);
			curpiece[3] = new Square( m  , row-1, 5);
			break;
		case 5:
			//     ��
			// �� �� ��
			block_score = 15;
			curpiece[0] = new Square( m  , row-2, 6);
			curpiece[1] = new Square( m-1, row-2, 6);
			curpiece[2] = new Square( m+1, row-2, 6);
			curpiece[3] = new Square( m+1, row-1, 6);
			break;
		case 6:
			// ��
			// �� �� ��
			block_score = 15;
			curpiece[0] = new Square( m  , row-2, 7);
			curpiece[1] = new Square( m-1, row-2, 7);
			curpiece[2] = new Square( m+1, row-2, 7);
			curpiece[3] = new Square( m-1, row-1, 7);
			break;
		}
		lost = !moveblock(old, curpiece); //����� �� �׿� �� �̻� ���� �� ���� �� ����
	}

	public synchronized void paint(Graphics g) {
		super.paint(g);
		try{
			g.setFont(new java.awt.Font("DialogInput",Font.BOLD, 20)); //������ ���¸� �����ִ� ���ڵ��� ����
			int gx = block_size + 40; //graphic�� x
			int gy = block_size*row/4; //graphic�� y	
			
			g.clearRect(gx, gy-25, xoffset-20, 200); //���� ���� ������ ���� ���� ������ ����
						
			g.drawString("Score: " + score, gx-20, gy);			
			g.drawString("Removeline: " + removeline, gx-20, gy+30);
			g.drawString("Level: " + level, gx-20, gy + 60);
			g.drawString("Next Block: ", gx-20, gy + 90);
	
	
			switch (preview) { //�̸�����
			case 0:
				// �� �� �� ��
				g.setColor(colors[1]);
				g.fill3DRect(gx+block_size*0, gy+110 + block_size*1, block_size, block_size, true); //true�� �����ϰ�, false�� �����ϰ�
				g.fill3DRect(gx+block_size*1, gy+110 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+110 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*3, gy+110 + block_size*1, block_size, block_size, true);
				
				break;
			case 1:
				//   �� �� ��
				//     ��
				g.setColor(colors[2]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 2:
				//    �� ��
				//  �� ��
				g.setColor(colors[3]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				break;
			case 3:
				// �� ��
				//   �� ��
				g.setColor(colors[4]);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				break;
			case 4:
				//  �� ��
				//  �� ��
				g.setColor(colors[5]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 5:
				//      ��
				//  �� �� ��
				g.setColor(colors[6]);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 6:
				//  ��
				//  �� �� ��
				g.setColor(colors[7]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*1, block_size, block_size, true);
				break;
			}

			for(int i=0; i<col; i++) {
				for (int j=0; j<row; j++) {
					g.setColor(colors[block_color[i][row-1-j]]);    
					g.fill3DRect(xoffset+block_size*i, 3*block_size + block_size*j, block_size, block_size, true);
				}
			}
		}
		catch(Exception e)	{ repaint(); }
	}

	public synchronized void update(Graphics g) {
		paint(g);
	}
		
	private synchronized boolean move_block(int byx, int byy, boolean rotate)	{
		Square newpos[] = new Square[4];
		
		for(int i =0; i<4; i++) {
			if(rotate) { //ȸ��
				int dx = curpiece[i].x - curpiece[0].x;
				int dy = curpiece[i].y - curpiece[0].y;
				
				if(block!=0) { //����� 0��°(�� �� �� ��)�� �ƴ� �� 
					newpos[i] = new Square(curpiece[0].x - dy, curpiece[0].y + dx, curpiece[i].c);
				}
				else {
					newpos[i] = new Square(curpiece[0].x + dy,curpiece[0].y - dx, curpiece[i].c);
				}
			}
			else //ȸ�� X(�׳� �Ʒ��� �̵�)
				newpos[i] = new Square(curpiece[i].x + byx, curpiece[i].y + byy, curpiece[i].c);
		}	
		
		if(!moveblock(curpiece, newpos)) //�� ����� ���� ��Ͽ� ���� �� �ִ��� üũ
			return false;											
		
		curpiece = newpos; //�� ����� ���� ������� ����							
		return true;
	}
	
	private boolean moveblock(Square from[], Square to[]) {
			outerloop:
			for(int i=0; i < to.length; i++) {
				if (!to[i].InBackground()) { //��� �ȿ� �����ִ��� üũ
					return false;
				}
				
				if (block_color[to[i].x][to[i].y] != 0 ) { //���� ��ϰ� ��ġ���� üũ
					for(int j=0; j<from.length; j++) {
						if (to[i].IsEqual(from[j])) {
							continue outerloop;
						}
					}
					return false;
				}
			}
	
			if(!pause) {
				for (int i=0; i<from.length; i++) {	//�̵� ���� ����� ����
					if (from[i].InBackground())	{
						block_color[from[i].x][from[i].y] = 0;
					}												
				}
				
				for(int i=0; i<to.length; i++) { //�̵��� ����� ���� �ʵ忡 ä��
					block_color[to[i].x][to[i].y] = to[i].c;
				}
			}
		return true;
	}
			
	private void removelines() {
		outerloop:
			for(int j=0; j<row; j++) {
				for(int i=0; i<col; i++) {
					if(block_color[i][j] == 0) { //���� ��Ͽ� �� ������ ������(����� �������̸� �� ����)
						continue outerloop; //outerloop�� ���ư�
					}
				}
				for(int k=j; k< row-1; k++) { //���� ��Ͽ� �� ���� ���� ���� �����(continue ���� �ʾ��� ��)
					for(int i=0; i<col; i++) {
						block_color[i][k] = block_color[i][k+1]; //���� �ִ� ����� �Ʒ��� �ű�(���� ����)
					}
				}
				j--; //�� �� �����
				removeline++;
				score += 50;
			}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start)	{
			start();
		}
		else if(e.getSource() == stop) {
			if(pause) {
				pause = false;
				stop.setText("stop");
				killme.resume();
			}
			else {
				pause = true;
				stop.setText("restart");
				killme.suspend();
			}
		}
		else if(e.getSource() == howto) {
			JOptionPane.showMessageDialog(this, "����Ű ( ��:������, ��:����, ��:ȸ��, ��:�Ʒ��� )"
			+"\n������ ��������10~20���̸� ������ ���پ��ٶ����� 50���� �߰��ȴ�", "Howto", JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	class WindowHandler extends WindowAdapter {	//��Ʈ���� ���α׷� �ݱ�
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.dispose();
		}
	}
} 	

