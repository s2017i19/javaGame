package project;

import java.applet.Applet;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import project.PingPong;

public class GameMain extends JFrame implements ActionListener {	
	JButton mole, puzzle, tetris, ping_pong, music;
	public Connection connection;
	Clip clip;
	int i=0;
	
	GameMain() throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:project.db");
		
		setTitle("Game");
		setSize(700,900);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		getContentPane().setBackground(new Color(212,241,252));
		
		mole = new JButton(new ImageIcon("img/mole_btn.png"));
		mole.setBounds(140, 120, 400, 100);
		mole.setBorderPainted(false);
		mole.setFocusPainted(false);
		mole.setContentAreaFilled(false);
		
		puzzle = new JButton(new ImageIcon("img/puzzle_btn.png"));
		puzzle.setBounds(140, 270, 375, 100);
		puzzle.setBorderPainted(false);
		puzzle.setFocusPainted(false);
		puzzle.setContentAreaFilled(false);
		
		tetris = new JButton(new ImageIcon("img/tetris_btn.png"));
		tetris.setBounds(140, 420, 320, 100);
		tetris.setBorderPainted(false);
		tetris.setFocusPainted(false);
		tetris.setContentAreaFilled(false);
		
		ping_pong = new JButton(new ImageIcon("img/ping-pong_btn.png"));
		ping_pong.setBounds(135, 580, 350, 105);
		ping_pong.setBorderPainted(false);
		ping_pong.setFocusPainted(false);
		ping_pong.setContentAreaFilled(false);
		
		music = new JButton(new ImageIcon("img/music.png"));
		music.setBounds(20, 770, 70, 60);
		music.setBorderPainted(false);
		music.setFocusPainted(false);
		music.setContentAreaFilled(false);

		mole.addActionListener(this);
		puzzle.addActionListener(this);
		tetris.addActionListener(this);
		ping_pong.addActionListener(this);
		music.addActionListener(this);
		
		add(mole);
		add(puzzle);
		add(tetris);
		add(ping_pong);
		add(music);
		
		Sound("bgm/game.wav",true);
		
		setVisible(true);
	}
	@Override
	public void actionPerformed (ActionEvent e) {
		if(e.getSource()==mole) {
			new Mole();	
		}
		else if (e.getSource()==puzzle) {
			new ButtonPuzzle();
		}
		else if (e.getSource()==tetris) {
			new Tetris();
		}
		else if (e.getSource()==ping_pong) {
			PingPong pp = new PingPong();
			pp.aThread =  new Thread(new PingPong());
			pp.aThread.start();
			pp.oldTime = (int) System.currentTimeMillis()/1000;
		}
		else if (e.getSource()==music){
			i++;
			if(i%2==0) {
				clip.start();
				music.setIcon(new ImageIcon("img/music.png"));
			}
			else {
				clip.stop();
				music.setIcon(new ImageIcon("img/stop_music.png"));
			}
		}
	}
	
	public void Sound(String file, boolean Loop){
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			if(Loop) clip.loop(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new GameMain();
	}
}
