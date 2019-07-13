package project;

import java.awt.*;

import java.awt.event.*;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.*;

class ButtonPuzzle extends JFrame implements ActionListener {
	Dialog di, helpDi;
	Label diLabel1, diLabel2;
	TextField field;
	String name;
	Button but;
	int oldTime, time;
	
	private JButton btn[];
	private int img[];
	private boolean result=true;
	
	ButtonPuzzle() {
		setTitle("ButtonPuzzle");
		setSize(700, 900);
		makeUI();
		setVisible(true);
		oldTime = (int) System.currentTimeMillis()/1000;
	}
	private void makeUI() {
		btn = new JButton[16];
		img = new int[16];
		setLayout(new GridLayout(4,4));
		
		addWindowListener(new WindowHandler());
		
		for(int i=0; i<15; i++) { //��ġ�� �ʴ� ���� �߻�
			img[i] = (int)(Math.random()*15);
			for(int j=0; j<i; j++) {
				if(img[i] == img[j]) {
					i--;
					break;
				}
			}
		}
		
		for(int i=0; i<16; i++) { //��� ��ư���� ����
			add(btn[i] = new JButton(new ImageIcon("img/btn/"+img[i]+".png")));
			btn[i].setEnabled(true);
			btn[i].addActionListener(this);
		}
		btn[15].setIcon(null);
		btn[15].setEnabled(false); //�� ������ ��ư�� ��Ȱ��ȭ ��Ŵ(��ĭ �ձ�)
	}
	private int[] nb = new int[4];
	private void find(int id) { //id�� ���� �� �̿��� ��ġ Ȯ��
		nb[0] = id-4; //��
		nb[1] = id+4; //�Ʒ�
		if(nb[1]>=16) nb[1] = -1;
		nb[2] = id-1; //����
		if(nb[2]<0 || nb[2]%4==3) nb[2] = -1;
		nb[3] = id+1; //������
		if(nb[3]%4==0) nb[3] = -1;
		
	}
	private void check() {
		for(int i=0; i<15; i++){
			if(img[i] != (i)) result = false;
		}
		if(result) {
			time = (int) System.currentTimeMillis()/1000;
			time = time - oldTime;
			
			name = (String) JOptionPane.showInputDialog(this,time+"�� �ɷȽ��ϴ�. "
					+"�̸��� �Է����ּ���.","Slide Puzzle",JOptionPane.PLAIN_MESSAGE,null,null,null);
			
			try {
    			GameMain main = new GameMain();
    			
    			PreparedStatement ps = main.connection.prepareStatement("INSERT INTO buttonpuzzle VALUES(?, ?)");
    			ps.setString(1, name);
    			ps.setInt(2, time);
    			
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
		}
	}
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton)e.getSource();
		int id;
		for (id=0; id<16; id++) {
			if(b==btn[id]) break;
		}
		find(id); //�̿��� id�� ã��
		check();
		
		for(int i=0; i<4; i++) {
			if(nb[i] >= 0 && !btn[nb[i]].isEnabled()) {
				JButton act, inact;
				
				act = btn[id];
				inact = btn[nb[i]];
				
				inact.setIcon(act.getIcon());
				act.setIcon(null);
				inact.setEnabled(true);
				act.setEnabled(false);
				break; //��Ȱ��ȭ �� �� ã���� break
			}
		}
	}
	class WindowHandler extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Window w = e.getWindow();
			w.dispose();
		}
	}
}