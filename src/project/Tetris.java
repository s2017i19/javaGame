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

	Square(int x, int y, int c) { //한 블록
		this.x = x;
		this.y = y;
		this.c = c;
	}
		
	boolean InBackground() { //배경 속에 머무르는지 체크
		return ( x >= 0 && x < Tetris.col && y >= 0 && y < Tetris.row );
	}

	boolean IsEqual(Square s) {	//쌓여있는 블록의 경계값 체크
		return x == s.x && y == s.y && c == s.c;
	}
}

public class Tetris extends Frame implements Runnable, ActionListener {
	private JButton start, howto, stop;

	String name;
	Button btn;
	
	static boolean pause = false;
	static int block_size = 25; //블록의 한 조각 크기					
	static final int xoffset = 240;	//배경(검은색) 시작 위치			
	static int col = 12; //배경의 가로 블록 수(열)
	static int row = 23; //배경의 세로 블록 수(행)
	int block_color[][]; //블록 하나의 색깔을 담을 배열
	int preview = (int) (Math.random()*7); //블록을 만드는 경우의 수(0~6) //미리보기 블록
	int block;
	static Square curpiece[] = new Square[4]; //이전 블록
	static Square newblock[] = new Square[4]; //새 블록
	boolean lost; //움직임 가능을 판단		
	boolean neednewblock = true; //새 블록이 필요한지 판단					
	Thread killme = null; //stop 및 스레드 종료를 위해 변수 생성		
	Color colors[];	//색깔 목록	
	int block_score, score = 0; //블록 점수와 총점을 초기화
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
						case 37: //왼쪽 화살표
							move_block(-1, 0, false); //블록을 왼쪽으로 한 칸 움직임
							neednewblock = false; //새로운 블록이 나오지 않도록 함
							repaint(); //새로 그림
							break;
					
						case 39: //오른쪽 화살표
							move_block(1, 0, false); //블록을 오른쪽으로 한 칸
							neednewblock = false;
							repaint();
							break;
					
						case 38: //위쪽 화살표(회전)
							if(!neednewblock && block != 4) { 
								move_block(0,0,true); //좌표 유지하면서 회전
								repaint();
								neednewblock = false;
							}
							break;
							
						case 40: //아래쪽 화살표
							while(move_block(0,-1,false)); //맨 아래로 내림
							repaint();
							break;
						}
					}
				});
				addWindowListener(new WindowHandler());
						
				block_color = new int[col][row + 4];					
															
				colors = new Color[8]; //색깔 배열
				
				colors[0] = new Color(40,40,40); //검은색(배경)
				colors[1] = new Color(255,0,0);	//빨간색
				colors[2] = new Color(0,255,0);	//초록색
				colors[3] = new Color(255,255,0); //노란색
				colors[4] = new Color(97,158,255); //하늘색
				colors[5] = new Color(255,121,0); //주황색
				colors[6] = new Color(65,18,155); //보라색
				colors[7] = new Color(0,0,250); //파란색
				
				setVisible(true);
			} //run
		}); //thread 어쩌구
	}; //tetris
	
		public void start() {
			for(int i=0; i< col; i++) {
				for(int j=0; j< row; j++) {
					block_color[i][j] = 0; //검은색(0번째 인덱스)으로 블록 하나하나를 색칠함
				}
			}

			level = 1; //변수 초기화
			score = 0;
			removeline = 0;
			block_score = -1;
			neednewblock = true; //새 블록이 나오게 함
			lost = false;
			repaint();
			(killme = new Thread(this)).start(); //스레드 호출(변수는 나중에 종료시킬 때 사용)
			requestFocus(); //포커스를 맞춤
		} //start
	
		public void run() {
			while (!lost) {
				int time=level;
				try {
					if(neednewblock) {
						time=100;
						Thread.sleep(time);
					}
					Thread.sleep(1000/time);	//스레드를 이용한 속도 조절
				}
				catch (InterruptedException e){	}

				if (neednewblock) {
					if (block_score > 0) {
						level = 1 + removeline/10;	//삭제된 라인 수를 기준으로 레벨을 조절
					}
					removelines();
					newblock();	
					score += block_score;
					preview = (int) (Math.random()*7);
					neednewblock = false;		
				}
				else {
					if(!pause) {
					neednewblock = !move_block(0,-1,false);	// 실제로 스레드를 아래로 이동
					requestFocus();
					}
				}
				repaint();
			}
		killme = null;

		name = (String) JOptionPane.showInputDialog(this,"총 "+score+"점 입니다. "+
		"이름을 입력해주세요.","Tetris",JOptionPane.PLAIN_MESSAGE,null,null,null);

		try {
			GameMain main = new GameMain();
			
			PreparedStatement ps = main.connection.prepareStatement("INSERT INTO tetris VALUES(?, ?)");
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
		
	} //run

	private void newblock() { //새로운 블록을 만듦
		Square old[] = new Square[4];
		old[0]  = old[1] = old[2] = old[3] = new Square(-1, -1, 0);

		int m = col/2;
		
		block = preview; //미리보기에서 보여준 블록을 사용
		switch (block) {
		case 0:
			//■ ■ ■ ■
			block_score = 10; //블록당 점수
			curpiece[0] = new Square(m  , row-1, 1);
			curpiece[1] = new Square(m-1, row-1, 1);
			curpiece[2] = new Square(m+1, row-1, 1);
			curpiece[3] = new Square(m+2, row-1, 1);
			break;
		case 1:
			//   ■ ■ ■
			//     ■
			block_score = 15;
			curpiece[0] = new Square(m  , row-1, 2);
			curpiece[1] = new Square(m-1, row-1, 2);
			curpiece[2] = new Square(m  , row-2, 2);
			curpiece[3] = new Square(m+1, row-1, 2); 
			break;
		case 2:
			//    ■ ■
			//  ■ ■
			block_score = 20;
			curpiece[0] = new Square(m  , row-2, 3 );
			curpiece[1] = new Square(m-1, row-2, 3);
			curpiece[2] = new Square(m  , row-1, 3);
			curpiece[3] = new Square(m+1, row-1, 3);
			break;
		case 3:
			// ■ ■
			//   ■ ■
			block_score = 20;
			curpiece[0] = new Square( m  , row-2, 4);
			curpiece[1] = new Square( m+1, row-2, 4);
			curpiece[2] = new Square( m-1, row-1, 4);
			curpiece[3] = new Square( m  , row-1, 4);
			break;
		case 4:
			//  ■ ■
			//  ■ ■
			block_score = 10;
			curpiece[0] = new Square( m-1, row-2, 5);
			curpiece[1] = new Square( m  , row-2, 5);
			curpiece[2] = new Square( m-1, row-1, 5);
			curpiece[3] = new Square( m  , row-1, 5);
			break;
		case 5:
			//     ■
			// ■ ■ ■
			block_score = 15;
			curpiece[0] = new Square( m  , row-2, 6);
			curpiece[1] = new Square( m-1, row-2, 6);
			curpiece[2] = new Square( m+1, row-2, 6);
			curpiece[3] = new Square( m+1, row-1, 6);
			break;
		case 6:
			// ■
			// ■ ■ ■
			block_score = 15;
			curpiece[0] = new Square( m  , row-2, 7);
			curpiece[1] = new Square( m-1, row-2, 7);
			curpiece[2] = new Square( m+1, row-2, 7);
			curpiece[3] = new Square( m-1, row-1, 7);
			break;
		}
		lost = !moveblock(old, curpiece); //블록이 다 쌓여 더 이상 쌓을 수 없을 때 종료
	}

	public synchronized void paint(Graphics g) {
		super.paint(g);
		try{
			g.setFont(new java.awt.Font("DialogInput",Font.BOLD, 20)); //게임의 상태를 보여주는 글자들을 생성
			int gx = block_size + 40; //graphic의 x
			int gy = block_size*row/4; //graphic의 y	
			
			g.clearRect(gx, gy-25, xoffset-20, 200); //게임 정보 변경을 위해 기존 정보를 지움
						
			g.drawString("Score: " + score, gx-20, gy);			
			g.drawString("Removeline: " + removeline, gx-20, gy+30);
			g.drawString("Level: " + level, gx-20, gy + 60);
			g.drawString("Next Block: ", gx-20, gy + 90);
	
	
			switch (preview) { //미리보기
			case 0:
				// ■ ■ ■ ■
				g.setColor(colors[1]);
				g.fill3DRect(gx+block_size*0, gy+110 + block_size*1, block_size, block_size, true); //true는 볼록하게, false는 오목하게
				g.fill3DRect(gx+block_size*1, gy+110 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+110 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*3, gy+110 + block_size*1, block_size, block_size, true);
				
				break;
			case 1:
				//   ■ ■ ■
				//     ■
				g.setColor(colors[2]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 2:
				//    ■ ■
				//  ■ ■
				g.setColor(colors[3]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				break;
			case 3:
				// ■ ■
				//   ■ ■
				g.setColor(colors[4]);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				break;
			case 4:
				//  ■ ■
				//  ■ ■
				g.setColor(colors[5]);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 5:
				//      ■
				//  ■ ■ ■
				g.setColor(colors[6]);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*0, block_size, block_size, true);
				g.fill3DRect(gx+block_size*0, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*1, gy+120 + block_size*1, block_size, block_size, true);
				g.fill3DRect(gx+block_size*2, gy+120 + block_size*1, block_size, block_size, true);
				break;
			case 6:
				//  ■
				//  ■ ■ ■
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
			if(rotate) { //회전
				int dx = curpiece[i].x - curpiece[0].x;
				int dy = curpiece[i].y - curpiece[0].y;
				
				if(block!=0) { //블록이 0번째(■ ■ ■ ■)가 아닐 때 
					newpos[i] = new Square(curpiece[0].x - dy, curpiece[0].y + dx, curpiece[i].c);
				}
				else {
					newpos[i] = new Square(curpiece[0].x + dy,curpiece[0].y - dx, curpiece[i].c);
				}
			}
			else //회전 X(그냥 아래로 이동)
				newpos[i] = new Square(curpiece[i].x + byx, curpiece[i].y + byy, curpiece[i].c);
		}	
		
		if(!moveblock(curpiece, newpos)) //새 블록이 이전 블록에 쌓일 수 있는지 체크
			return false;											
		
		curpiece = newpos; //새 블록을 이전 블록으로 만듦							
		return true;
	}
	
	private boolean moveblock(Square from[], Square to[]) {
			outerloop:
			for(int i=0; i < to.length; i++) {
				if (!to[i].InBackground()) { //배경 안에 들어와있는지 체크
					return false;
				}
				
				if (block_color[to[i].x][to[i].y] != 0 ) { //쌓인 블록과 겹치는지 체크
					for(int j=0; j<from.length; j++) {
						if (to[i].IsEqual(from[j])) {
							continue outerloop;
						}
					}
					return false;
				}
			}
	
			if(!pause) {
				for (int i=0; i<from.length; i++) {	//이동 전의 블록을 지움
					if (from[i].InBackground())	{
						block_color[from[i].x][from[i].y] = 0;
					}												
				}
				
				for(int i=0; i<to.length; i++) { //이동한 블록의 색을 필드에 채움
					block_color[to[i].x][to[i].y] = to[i].c;
				}
			}
		return true;
	}
			
	private void removelines() {
		outerloop:
			for(int j=0; j<row; j++) {
				for(int i=0; i<col; i++) {
					if(block_color[i][j] == 0) { //쌓인 블록에 빈 공간이 있으면(배경이 검은색이면 빈 공간)
						continue outerloop; //outerloop로 돌아감
					}
				}
				for(int k=j; k< row-1; k++) { //쌓인 블록에 빈 공간 없을 때만 실행됨(continue 되지 않았을 때)
					for(int i=0; i<col; i++) {
						block_color[i][k] = block_color[i][k+1]; //위에 있던 블록을 아래로 옮김(색깔 변경)
					}
				}
				j--; //한 줄 지우기
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
			JOptionPane.showMessageDialog(this, "방향키 ( →:오른족, ←:왼쪽, ↑:회전, ↓:아래쪽 )"
			+"\n점수는 한조각당10~20점이며 라인을 한줄없앨때마다 50점씩 추가된다", "Howto", JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	class WindowHandler extends WindowAdapter {	//테트리스 프로그램 닫기
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.dispose();
		}
	}
} 	

