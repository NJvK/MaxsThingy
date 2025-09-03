import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
//events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//Randomness
import java.util.Random;
//File reading / writing
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//Sounds
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Screen extends JPanel implements MouseListener, KeyListener, ActionListener{
	public static void main(String[] args){
		Screen screen = new Screen();
        JFrame frame = new JFrame("Tetris");
		
        frame.add(screen);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.pack();
		frame.setFocusable(true);
		screen.setFocusable(true);
		screen.requestFocusInWindow();
		
        
		frame.setVisible(true);
		
		
		screen.addKeyListener(screen);
		screen.addMouseListener(screen);
		screen.loop();
		
		//System.out.println(screen.checkCollision(new byte[] {9, 3}, (byte)1));
	}
	private ShiftRegister<Byte> pieces; //holds the next pieces to drop
	private byte[][] grid; //the actual grid
	private boolean pause, startMenu, hasHeld, hasScoreboard, gameOverScreen; //if the game is paused or not, if the game is on the start menu or not, if the player has swapped hold pieces with this piece before, if the scoreboard exists or not
	private JButton start, exit, continueButton; //start/continue button, exit/save button
	private JLabel text; //Title, and paused screen text
	private int score, speed, lineClears, levelClears, level, combos, pauseTimer, frameRate; //current score, frames before gravity works/activates, the score needed to progress to the next level, the current level the player is on, the number of combo clears
	private Random ran; //random number generator
	private byte[] pieceCenter; // center of the pieces for rotation purposes
	private byte currentPiece, hold; //the current piece being dropped, the current piece being held
	private volatile boolean blockKeyPresses, snapping; //volatile means all threads can see this variable immediately, used so player cant move piece when being placed in the world - flag, used so player only sends one snap to grid command, used to syncronize the game loop to inputs - flag
	private File scoreBoard;
	private FileInputStream reader;
	private FileOutputStream writer;
	private String[] scoreBoardRaw, scoreNames;
	private int[] scoreValues;
	private char[] tempName;
	
	//piece encoding:
	//[0] rot(0)
	//[1] rot(1) : 3
	//[2] type(0)
	//[3] type(1)
	//[4] type(2) : 7
	
	//Rot data: 0:default 1:rotate left, 2:180 rotation, 3:rotate right
	//Type data: straight:1, backwards L:2, L:3, square:4, s:5, t:6, backwards s:7
	
	/* TO-DO:
	 * Shadow behind dropping piece
	 * shadow is an outline
	 * block the key press when snapping
	 
	 */
	 
	 /* Did: (recently)
	  * 
	  * only swap hold piece once per new piece
	  */
	
	//static pieces with 0 rotation
	private static final byte EMPTY			= 0b000_00_000;
	private static final byte STRAIGHT		= 0b000_00_001;
	private static final byte BACK_L		= 0b000_00_010;
	private static final byte NORMAL_L		= 0b000_00_011;
	private static final byte SQUARE		= 0b000_00_100;
	private static final byte NORMAL_S		= 0b000_00_101;
	private static final byte T_PIECE		= 0b000_00_110;
	private static final byte BACK_S		= 0b000_00_111;
	
	private static final byte SCORE_SIZE 	= 5; //how many scores are saved in the file / leaderboard
	private static final byte NAME_SIZE		= 3; //how big the names are in memory (character count)
	public Screen(){
		pieces = new ShiftRegister<Byte>(5);
		grid = new byte[10][20];
		
		pause = true;
		startMenu = true;
		hasHeld = false;
		blockKeyPresses = false;
		snapping = false;
		gameOverScreen = false;
		
		score = 0;
		speed = 60;
		lineClears = 0;
		levelClears = 10;
		level = 1;
		combos= 0;
		pauseTimer = 0;
		frameRate = 100;
		
		ran = new Random();
		
		pieceCenter = new byte[]{4,0};
		
		tempName = new char[NAME_SIZE]; //holds the characters the user has put for their names
		
		currentPiece = 0;
		hold = STRAIGHT;
		
		setLayout(null);
		setFocusable(true);
		this.requestFocusInWindow();
		
		//Jcrap - focus is stupid
		text = new JLabel("Tetris"); //displays the title screen and centers it
		text.setFont(new Font("Consolas", Font.PLAIN, 45));
		text.setBounds(400-(int)text.getPreferredSize().getWidth()/2, 50, (int)text.getPreferredSize().getWidth(), (int)text.getPreferredSize().getHeight());
		text.setHorizontalAlignment(JLabel.CENTER);
		text.setForeground(Color.white);
		text.setBackground(Color.black);
		text.setOpaque(false);
		add(text);
		
		start = new JButton("Start"); //to start the game
		start.setFont(new Font("Consolas", Font.BOLD, 30));
		start.setBounds(400-(int)start.getPreferredSize().getWidth()/2, 175+(int)text.getBounds().getHeight(), (int)start.getPreferredSize().getWidth(), (int)start.getPreferredSize().getHeight());
		start.setHorizontalAlignment(JButton.CENTER);
		start.setVerticalAlignment(JButton.BOTTOM);
		start.setBackground(Color.green);
		add(start);
		start.addActionListener(this);
		start.setFocusable(false);
		
		exit = new JButton("End Session"); //to leave a game early (Physicly click it, no keybind) or to enter the name selected for highscore purposes
		exit.setFont(new Font("Consolas", Font.BOLD, 15));
		exit.setBounds(175-(int)exit.getPreferredSize().getWidth()/2, 135, (int)exit.getPreferredSize().getWidth(), (int)exit.getPreferredSize().getHeight());
		exit.setHorizontalAlignment(JButton.CENTER);
		add(exit);
		exit.setBackground(Color.red);
		exit.setForeground(Color.yellow);
		exit.addActionListener(this);
		exit.setFocusable(false);
		exit.setVisible(false);
		
		continueButton = new JButton("Continue");
		continueButton.setFont(new Font("Consolas", Font.BOLD, 30));
		continueButton.setBounds(245-(int)continueButton.getPreferredSize().getWidth(), 15 + (int)exit.getBounds().getY() + (int)exit.getBounds().getHeight(), (int)continueButton.getPreferredSize().getWidth(), (int)continueButton.getPreferredSize().getHeight());
		continueButton.setHorizontalAlignment(JButton.CENTER);
		add(continueButton);
		continueButton.setBackground(Color.green);
		continueButton.setForeground(Color.black);
		continueButton.addActionListener(this);
		continueButton.setFocusable(false);
		continueButton.setVisible(false);
		
		//try and get the scoreboard
		scoreBoard = new File("ScoreBoard.dat");
		hasScoreboard = scoreBoard != null ? scoreBoard.canRead() : false;
		
		/*boolean res = writeScores(new String[0], new int[0]);
		System.out.println("Writing : " + res);*/
		
		//boolean res;
		
		scoreBoardRaw = getScores();
		scoreValues = getScores(scoreBoardRaw);
		scoreNames = getNames(scoreBoardRaw);
		
	}
	public Dimension getPreferredSize(){
        return new Dimension(800,600);
    }
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.darkGray);
		g.fillRect(0,0,800,600);
		
		/*g.setColor(Color.white);
		g.drawString(level + " : " + levelClears + " : " + lineClears, 15, 15);*/
		
		if (gameOverScreen){
			//display a name entering screen, high score, and the confirm button
			g.setFont(new Font("Consolas", Font.BOLD, 45));
			int wordWidth = g.getFontMetrics().stringWidth("AAA"); //mono-spaced font - just want the amount of pixels this'll take up
			int totalWidth = g.getFontMetrics().stringWidth("AAA ---- ") + g.getFontMetrics().stringWidth(intToString(score));
			g.setColor(Color.black);
			g.fillRect(400-totalWidth/2, 135-g.getFontMetrics().getHeight()-5, totalWidth, g.getFontMetrics().getHeight()+10);
			
			g.setColor(Color.white);
			
			int i=0; //to place the _ for next character
			
			for (i=0;i<tempName.length;i++){
				if (tempName[i] == (char)0) break;
				g.drawString(tempName[i]+"", 400-totalWidth/2+25 + i*wordWidth/3, 125);
			}
			if (i< tempName.length){
				g.fillRect(400-totalWidth/2+25 + i*wordWidth/3 + 5, 120, wordWidth/3-10, 5);
			}
			g.drawString(intToString(score), 400-totalWidth/2+g.getFontMetrics().stringWidth("AAA ---- "), 125);
			return;
		}
		
		//Draw the backgrounds for various elements
		g.setColor(Color.black);
		g.fillRect(250, 0, 300, 600); //main board
		g.fillRect(100, 0, 150, 120); //hold area
		g.fillRect(550, 0, 150, 480); //next pieces
		g.fillRect(0, 450, 250, 150); //Score area
		
		if (startMenu){
			//Draw the scoreboard
			g.setColor(Color.white);
			g.setFont(new Font("Consolas", Font.PLAIN, 45));
			int startY = (int)start.getBounds().getHeight() + (int)start.getBounds().getY();
			g.drawString("Scoreboard:", 400-g.getFontMetrics().stringWidth("Scoreboard:")/2, startY+=65);
			g.setFont(new Font("Consolas", Font.PLAIN, 25));
			String text = "";
			for (int i=0;i<SCORE_SIZE;i++){
				text = scoreNames[i] + ": " + scoreValues[i];
				g.setColor(Color.white);
				g.drawString(scoreNames[i]+":", 400-g.getFontMetrics().stringWidth(text)/2, startY += g.getFontMetrics().getHeight() + 10);
				g.setColor(getScoreColor(scoreValues[i]));
				g.drawString(scoreValues[i] +"", 400-g.getFontMetrics().stringWidth(text)/2 + g.getFontMetrics().stringWidth(scoreNames[i] + ": "), startY);
			}
			
			return; //don't want a messy board for the start menu
		}
		
		//show the count down until play is resumed
		if (pause && !startMenu && !gameOverScreen && pauseTimer >= 0){
			g.setFont(new Font("Consolas", Font.BOLD, 90));
			g.setColor(Color.white);
			g.drawString(pauseTimer +"", 400-g.getFontMetrics().stringWidth(pauseTimer+""), 300-g.getFontMetrics().getHeight()/2);
			return;
		}
		
		//draw all the squares at each coordinate of the board
		for (int x=0;x<grid.length;x++){
			for (int y=0;y<grid[x].length;y++){
				g.setColor(getColor(grid[x][y]));
				g.fillRect(251 + x*30, 1+y*30, 28, 28);
			}
		}
		
		//Draw the outline showing where the piece would land if dropped
		byte[][] offsets = centerOffsets(unpack(currentPiece));
		byte[] newCenter = new byte[] {pieceCenter[0], pieceCenter[1]};
		drop(newCenter, false);
		g.setColor(Color.lightGray);
		for (int i=0;i<offsets.length;i++){
			g.drawRect(251 + (newCenter[0] + offsets[i][0])*30, 1+(newCenter[1] + offsets[i][1])*30, 28, 28);
			g.drawRect(252 + (newCenter[0] + offsets[i][0])*30, 2+(newCenter[1] + offsets[i][1])*30, 26, 26);
			g.drawRect(253 + (newCenter[0] + offsets[i][0])*30, 3+(newCenter[1] + offsets[i][1])*30, 24, 24);
		}
		
		//Draw the current piece - the one dropping / moving across the board
		g.setColor(getColor(currentPiece).brighter());
		for (int i=0;i<offsets.length;i++){
			g.fillRect(251 + (pieceCenter[0] + offsets[i][0])*30, 1+(pieceCenter[1] + offsets[i][1])*30, 28, 28);
		}
		g.setColor(Color.red);
		g.fillOval(251 + pieceCenter[0]*30, 1+pieceCenter[1]*30, 28, 28);
		
		//Draw the hold piece
		int cenX = 67, cenY = 30;
		if (unpack(hold)[1] == STRAIGHT){
			cenX += 15;
			cenY +=15;
		}
		if (unpack(hold)[1] == SQUARE){
			cenX += 15;
		}
		offsets = centerOffsets(new byte[] {0, unpack(hold)[1]}); //force the piece to a certain offset
		g.setColor(getColor(hold).brighter());
		for (int i=0;i<offsets.length;i++){
			g.fillRect(100 + cenX + (offsets[i][0])*30, 1 + cenY + (offsets[i][1])*30, 28, 28);
		}
		
		//Draw the next pieces
		for (int n=0;n<pieces.size();n++){
			cenX = 63;
			cenY = 30;
			byte piece = pieces.get(n*-1-1);
			if (unpack(piece)[1] == STRAIGHT){
				cenX += 15;
				cenY += 15;
			}
			if (unpack(piece)[1] == SQUARE){
				cenX += 15;
			}
			offsets = centerOffsets(new byte[] {0, unpack(piece)[1]}); //force the piece to a certain offset
			g.setColor(getColor(piece).brighter());
			for (int i=0;i<offsets.length;i++){
				g.fillRect(550 + cenX + (offsets[i][0])*30, n*90 + cenY + (offsets[i][1])*30, 28, 28);
				
			}
		}
		
		//Draw the current score
		//turn into a String with commas after each 3 digits
		String points = intToString(score);
		String combo = "x"+intToString(combos);
		//draw actual text
		g.setColor(Color.white);
		g.setFont(new Font("Consolas", Font.PLAIN, 45));
		g.drawString(points, 245-g.getFontMetrics().stringWidth(points), 490);
		g.drawString(combo,  245-g.getFontMetrics().stringWidth(combo ), 540);
		g.drawString("Level: " + level, 245-g.getFontMetrics().stringWidth("Level: " + level), 590);
	}
	
	//helping functions
	public byte[] unpack(byte piece){
		byte[] send = new byte[2];
		send[0] = (byte)((piece & 0x18)>>3);
		send[1] = (byte)(piece & 0x7);
		return send;
	}
	public byte pack(byte... piece){
		byte send = 0;
		send |= piece[0];
		send<<=3;
		send |= piece[1];
		return send;
	}
	public byte[][] centerOffsets(byte[] piece){
		int[][] send = new int[4][2];
		if (piece[1] == EMPTY) return new byte[4][2]; //empty list, piece is empty
		
		//make math go easier
		/*byte isOdd = (piece[0]%2==1?1:0);
		byte isEven = isOdd*-1+1;*/
		//0 is +x axis, rotate around like theta
		int xDir = piece[0]%2==0?(piece[0] > 1 ? -1:1):0; //0:1, 2:-1
		int yDir = piece[0]%2==1?((piece[0]-1)>1?-1:1):0; //1:1, 3:-1
		
		switch(piece[1]){
			case STRAIGHT:
				send[0] = new int[] {-2*xDir, -2*yDir};
				send[1] = new int[] {-1*xDir, -1*yDir};
				send[2] = new int[] {0, 0};
				send[3] = new int[] {1*xDir, 1*yDir};
				break;
			case BACK_L:
				send[0] = new int[] {-1*xDir, -1*yDir};
				send[1] = new int[] {0, 0};
				send[2] = new int[] {1*xDir, 1*yDir};
				send[3] = new int[] {1*xDir + 1*yDir, -1*yDir + 1*xDir};
				break;
			case NORMAL_L:
				send[0] = new int[] {-1*xDir, -1*yDir};
				send[1] = new int[] {0, 0};
				send[2] = new int[] {1*xDir, 1*yDir};
				send[3] = new int[] {-1*xDir + 1*yDir, 1*yDir + 1*xDir};
				break;
			case SQUARE:
				send[0] = new int[]{0,0};
				send[1] = new int[]{-1,0};
				send[2] = new int[]{-1,1};
				send[3] = new int[]{0, 1};
				break;
			case NORMAL_S:
				send[0] = new int[] {-1*xDir + 1*yDir, 1*yDir + 1*xDir};
				send[1] = new int[] {1*yDir, 1*xDir};
				send[2] = new int[] {0, 0};
				send[3] = new int[] {1*xDir, -1*yDir};
				break;
			case T_PIECE:
				send[0] = new int[] {-1*xDir, -1*yDir};
				send[1] = new int[] {1*yDir, 1*xDir};
				send[2] = new int[] {0, 0};
				send[3] = new int[] {1*xDir, 1*yDir};
				break;
			case BACK_S:
				send[0] = new int[] {1*xDir + 1*yDir, -1*yDir + 1*xDir};
				send[1] = new int[] {1*yDir, 1*xDir};
				send[2] = new int[] {0, 0};
				send[3] = new int[] {-1*xDir, 1*yDir};
				break;
		}
		byte[][] temp = new byte[4][2];
		for (int i=0;i<send.length;i++){
			for (int n=0;n<send[i].length;n++){
				temp[i][n] = (byte)send[i][n];
			}
		}
		return temp; //empty list - couldn't find the correct piece
	}
	public void startGame(){
		grid = new byte[10][20]; //empty the grid
		speed = 30; //set the drop speed to 30
		level = 1; //resets the level to one
		pieces.clear(); //clear the old buffer if it existed
		fillPieces(); //fill the buffer with random pieces
		currentPiece = pieces.pop(); //get the first random piece
		fillPieces(); //replace the piece just removed
		//hide start menu stuff
		start.setVisible(false);
		text.setVisible(false);
		startMenu = false;
		pause = false;
		gameOverScreen = false;//the game isnt over
		for (int i=0;i<tempName.length;i++){//reset the name selection just in case
			tempName[i] = (char)0;
		}
		exit.setBounds(245-(int)exit.getBounds().getWidth(), 135, (int)exit.getBounds().getWidth(), (int)exit.getBounds().getHeight());
		exit.setText("End Session");
		exit.setBackground(Color.red);
		exit.setForeground(Color.yellow);
		repaint();
	}
	public void fillPieces(){
		while (pieces.size() < pieces.capacity()){
			pieces.add((byte)(ran.nextInt(7)+1));
		}
		hasHeld = false;
		updateNext();
	}
	public boolean checkCollision(byte[] center, byte piece){
		byte[][] offsets = centerOffsets(unpack(piece));
		for (int i=0;i<offsets.length;i++){
			if (center[1]+offsets[i][1] < 0 && center[0]+offsets[i][0] >= 0 && center[0]+offsets[i][0] < grid.length) continue;
			if (!inBounds(center, offsets[i])) return false;
			byte temp = grid[offsets[i][0]+center[0]][offsets[i][1]+center[1]];
			if (temp != 0) return false;
		}
		return true;
	}
	public boolean inBounds(byte[] center, byte[] offset){
		return (center[0]+offset[0] < grid.length
				&& center[0] + offset[0] >= 0
				&& center[1] + offset[1] < grid[0].length
				&& center[1] + offset[1] >= 0);
	}
	public boolean inHorizontalBounds(byte[] center, byte[] offset){
		return (center[0]+offset[0] < grid.length
				&& center[0] + offset[0] >= 0);
	}
	public byte rotate(byte piece, int dir){
		byte rot = unpack(piece)[0];
		byte type = unpack(piece)[1];
		int max = 4;
		if (type == STRAIGHT || type == NORMAL_S || type == BACK_S) max = 2;
		if (type == SQUARE) max = 1;
		rot = (byte)ShiftRegister.wrap(rot+dir, max, 0);
		//System.out.println("rotate debug: " + pieceCenter[0] + ", " + pieceCenter[1] + ", " + type + ", " + rot);
		for (int attempt=0;attempt<max;attempt++){
			if (!checkCollision(pieceCenter, pack((byte)ShiftRegister.wrap(rot+dir*attempt, max, 0), type))){
				//try to move the center so the piece fits into the world
				int xDir = 0, yDir = 0;
				for (int i=0;i<8;i++){
					xDir = ((i-2)%4==0?0 : (i>2 ? -1:1)); //1,  1,  0, -1, -1, -1,  0,  1
					yDir = (i%4 == 0 ? 0 : (i>4 ? 1:-1)); //0, -1, -1, -1,  0,  1,  1,  1
					if (checkCollision(new byte[] {(byte)(pieceCenter[0]+xDir), (byte)(pieceCenter[1]+yDir)}, pack((byte)ShiftRegister.wrap(rot+dir*attempt, max, 0), type)) && pieceCenter[1]+yDir >= 0){
						//found an open space in the actual board
						pieceCenter[0] += xDir;
						pieceCenter[1] += yDir;
						return pack((byte)ShiftRegister.wrap(rot+dir*attempt, max, 0), type);
					}
				}
				//no open spaces found, undo the rotation
				rot = (byte)ShiftRegister.wrap(rot-dir, max, 0);
			}
		}
		
		return pack(rot, type);
	}
	public boolean canFall(){
		return canMove(0, 1);
	}
	public boolean removeFullRows(){
		boolean fullRow = false;
		int lineCount = 0;
		for (int y=0;y<grid[0].length;y++){
			fullRow = true;
			for (int x=0;x<grid.length;x++){
				if (grid[x][y] == EMPTY) {
					fullRow = false;
					break;
				}
			}
			if (!fullRow) continue;
			for (int x=0;x<grid.length;x++){
				for (int y2=y;y2>=0;y2--){
					grid[x][y2] = y2==0?EMPTY:grid[x][y2-1];
				}
			}
			lineCount++;
			y--;
		}
		
		switch(lineCount){
			case 4: //will add up to 800*level
				changeScore(300*level);
			case 3: //500*level
				changeScore(200*level);
			case 2: //300*level
				changeScore(200*level);
			case 1://100*level
				changeScore(100*level);
		}
		changeScore(50 * combos * level);
		lineClears += lineCount;
		levelClears-=lineCount;
		if (levelClears <= 0){
			increaseLevel();
			levelClears += 10;
		}
		else if (lineCount > 0) this.playSound("./sounds/Clear.wav");
		
		if (!isGridEmpty()) return (lineCount > 0);
		
		//Perfect clear
		switch(lineCount){
			case 4://2100*level
				changeScore(100*level);
			case 3://1700*level total
				changeScore(300*level);
			case 2://1200*level
				changeScore(200*level);
			case 1://800*level
				changeScore(700*level);
		}
		
		return (lineCount > 0);
		
	}
	public synchronized boolean snapToGrid(){
		snapping = true;
		boolean send = true;
		byte[][] offsets = centerOffsets(unpack(currentPiece));
		for (int i=0;i<offsets.length;i++){
			int x = pieceCenter[0]+offsets[i][0];
			int y = pieceCenter[1]+offsets[i][1];
			if (!inBounds(pieceCenter, offsets[i])) continue;
			if (grid[x][y] != 0){
				send = false;
				//if you get booted early, find out why with this debug statement
				//System.out.println("snapToGrid debug: Gird overlap: (" + x + "," + y + ") = " + grid[x][y] + " for center: (" + pieceCenter[0] + "," + pieceCenter[1] + ")" + currentPiece);
			}
			grid[x][y] = currentPiece;
		}
		this.playSound("./sounds/Click.wav");
		boolean res = removeFullRows();
		if (res) combos++;
		else combos=0;
		updateScore();
		snapping = false;
		return send;
	}
	public void resetPiece(){
		currentPiece = pieces.removeLast();
		fillPieces();
		pieceCenter = new byte[]{4, 0};
		hasHeld = false;
	}
	public String intToString(int value){
		char[] letters = Integer.toString(value).toCharArray();
		char[] toShow = new char[letters.length + (letters.length-1)/3];
		int commas = 0;
		for (int i=0;i<letters.length;i++){
			toShow[toShow.length-(i+commas)-1]= letters[letters.length-1-i];
			if ((i+1)%3==0 && i+(commas)+2 < toShow.length){toShow[toShow.length-(i+(commas++)+1)-1] = ',';}
		}
		return new String(toShow);
	}
	public void changeScore(int amount){
		score += amount;
		updateScore();
	}
	public boolean isGridEmpty(){
		for (int x=0;x<grid.length;x++){
			if (grid[x][grid[0].length-1] != 0) return false;
		}
		return true;
	}
	public void submitScore(){
		insert(new String(tempName), score);
		gameOverScreen = false;
		pause = true;
		startMenu = true;
		exit.setVisible(false);
		continueButton.setVisible(false);
		start.setVisible(true);
		text.setVisible(true);
		score = 0;
		combos = 0;
		
		repaint();
	}
	public void increaseLevel(){
		if (level++ - 1 < 10) speed-=4; //always adds one to level count, if we're below level 10, speed up the game (max speed is 20 frames / (gravity/snaping) rn)
		this.playSound("./sounds/Level Up.wav");
	}
	
	//movement methods
	public boolean movePiece(int xDir, int yDir){
		if (yDir < 0) return false; // cannot move upwards
		if (snapping) return false; //cannot move the piece while it is being snapped to grid
		boolean canMove = canMove(xDir, yDir);
		if (!canMove) return false; //collides with something
		pieceCenter[0] += xDir;
		pieceCenter[1] += yDir;
		return true;
	}
	public boolean canMove(int xDir, int yDir){
		return checkCollision(new byte[] {(byte)(pieceCenter[0]+xDir), (byte)(pieceCenter[1]+yDir)}, currentPiece);
	}
	public void fall(){ //triggered by gravity
		if (canMove(0,1)){ //no ground hit yet
			pieceCenter[1]++;
		}
		updateGame();
	}
	public void drop(byte[] center, boolean affectWorld){
		int y=0;
		for (y=center[1];y<20;y++){
			if (!checkCollision(new byte[] {center[0], (byte)y}, currentPiece)) break;
		}
		y--;
		
		center[1] = (byte)y;
		if (!affectWorld) return; //don't change anything if don't need to
		snapToGrid();
		
		if (y != center[1]) changeScore(8);
		else changeScore(4);
		
		resetPiece();
	}
	public void hold(){
		if (hasHeld) return;
		//swap the two variables
		byte temp = hold;
		hold = currentPiece;
		currentPiece = temp;
		//give the player a piece if they dont have one
		if (currentPiece == EMPTY) currentPiece = pieces.pop();
		//reset the center if this is first time swapping hold on the piece
		if (!hasHeld) pieceCenter = new byte[] {4, 0};
		hasHeld = true;
		updateHold();
	}
	
	//Draw Commands
	public void updateHold(){
		repaint(0L, 100, 0, 150, 120);
	}
	public void updateGame(){
		repaint(0L, 250, 0, 300, 600);
	}
	public void updateNext(){
		repaint(0L, 550, 0, 150, 480);
	}
	public void updateScore(){
		repaint(0L, 0, 450, 250, 150);
	}
	
	//get colors
	public Color getColor(byte piece){ //get Colors for the pieces
		byte t = unpack(piece)[1];
		switch(t){
			case STRAIGHT:
			return new Color(81, 225, 252);
			case BACK_L:
			return new Color(241, 110, 185);
			case NORMAL_L:
			return new Color(246, 146, 48);
			case SQUARE:
			return new Color(254, 248, 76);
			case NORMAL_S:
			return new Color(233, 61, 30);
			case T_PIECE:
			return new Color(148, 54, 146);
			case BACK_S:
			return new Color(121, 174, 61);
			default:
			return Color.darkGray.darker();
		}
	}
	public Color getScoreColor(int score){ //get Colors for the scoreboard
		int grad = (int)(255 * Math.min(Math.max(0, score), 1_000_000) / 1_000_000.0);
		return new Color(grad, 255-grad, grad/2);
	}
	
	//Window events
    public void mouseClicked(MouseEvent e) {
		repaint();
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
	public void keyPressed(KeyEvent e){
		if (gameOverScreen){
			if ((e.getKeyCode() >= 0x41 && e.getKeyCode() < 0x5a)){
				//valid key press
				for (int i=0;i<NAME_SIZE;i++){
					if (tempName[i] != (char)0) continue; //already wrote letters here
					tempName[i] = Character.toUpperCase(e.getKeyChar()); //put the letter in its position
					if (i == NAME_SIZE-1){
						//can now submit
						exit.setBackground(Color.green);
						exit.setForeground(Color.black);
					}
					break; //did it now we can leave
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
				//remove a character
				boolean removed = false;
				for (int i=NAME_SIZE-1;i>=0;i--){
					if (tempName[i] == (char)0) continue; //haven't found the end of the current characters yet
					tempName[i] = (char)0; //remove this character that is not 0 / nothing
					//show that you cannot submit now, a letter was removed
					exit.setBackground(Color.gray);
					exit.setForeground(Color.lightGray);
					removed = true;
					break; //did it so now leave
				}
				if (!removed) this.playSound("./sounds/Error.wav");
			}
			else if (e.getKeyCode() == KeyEvent.VK_ENTER && canSubmit(tempName)){
				//Confirmation this is the name they want to enter
				submitScore();
			}
			else{
				this.playSound("./sounds/Error.wav");
			}
			repaint();
			return;
		}
		if (blockKeyPresses) return;
		switch(e.getKeyCode()){
			case KeyEvent.VK_LEFT: //Left arrow
			case KeyEvent.VK_A: //'a' key
				movePiece(-1, 0);
				break;
			case KeyEvent.VK_RIGHT: //Right arrow
			case KeyEvent.VK_D: //'d' key
				movePiece(1, 0);
				break;
			case KeyEvent.VK_DOWN: //Down arrow
			case KeyEvent.VK_S: //'s' key
				movePiece(0, 1);
				break;
			case KeyEvent.VK_UP: //Up Arrow / normal rotation
			case KeyEvent.VK_W: //'w' key / normal rotation
			case KeyEvent.VK_X: //'x' key / normal rotation
				currentPiece = rotate(currentPiece, 1);
				byte[][] offset = centerOffsets(unpack(currentPiece));
				for (int i=0;i<offset.length;i++){
					//find if we need to add or subtract from the center to keep the piece in bounds
					if (pieceCenter[0]+offset[i][0] >= grid.length) {pieceCenter[0] = (byte)(grid.length - offset[i][0]);break;}
					if (pieceCenter[0]+offset[i][0] <  0) {pieceCenter[0] = (byte)(-1*offset[i][0]);break;}
				}
				break;
			case KeyEvent.VK_Z: //'z' key / anti rotation
				currentPiece = rotate(currentPiece, -1);
				offset = centerOffsets(unpack(currentPiece));
				for (int i=0;i<offset.length;i++){
					//find if we need to add or subtract from the center to keep the piece in bounds
					if (pieceCenter[0]+offset[i][0] >= grid.length) {pieceCenter[0] = (byte)(grid.length - offset[i][0]);break;}
					if (pieceCenter[0]+offset[i][0] <  0) {pieceCenter[0] = (byte)(-1*offset[i][0]);break;}
				}
				break;
			case KeyEvent.VK_SPACE: //Space key
			case KeyEvent.VK_ENTER: //Enter key
				drop(pieceCenter, true);
				break;
			case KeyEvent.VK_C: //'c' key
				hold();
				break;
			case KeyEvent.VK_EQUALS: //'=' key
				score *= 10;
				score++;
				break;
			case KeyEvent.VK_MINUS: //'-' key
				score /= 10;
		}
		//System.out.println("Doing things");
		//System.out.println(e.getExtendedKeyCode() + ", " + e.getKeyCode() + ", " + e.getKeyChar() + ", " + e.getKeyLocation() + ", " + e.isActionKey() + ", " + "<" + e.paramString() + ">");
        //repaint();
    }
    public void keyReleased(KeyEvent e){
    }
    public void keyTyped(KeyEvent e) {
	}
	public void actionPerformed(ActionEvent e){
		if (e.getSource() == start){
			pause = false;
			startMenu = false;
			startGame();
			exit.setVisible(true);
		}
		if (e.getSource() == exit){
			if (gameOverScreen && canSubmit(tempName)){
				submitScore();
			}
			else goToGameOver(true);
		}
		if (e.getSource() == continueButton){
			backToGame();
		}
	}
	
	//Highscore functions
	public boolean canSubmit(char[] letters){
		//returns true if there are no empty spaces in the list
		for (char let : letters){
			if (let == (char)0) return false;
		}
		return true;
	}
	public boolean insert(String name, int score){
		//inserts the name and score into our version of the file's then, saves all the scores to the file
		int i=0;
		for (i=scoreValues.length-1;i>=0;i--){
			if (scoreValues[i] > score){
				if (i == scoreValues.length-1) return false; //not high enough to be in highscores
				break; //i = next higher score compared to user
			}
		}
		i++;
		for (int n=scoreValues.length-1;n>i;n--){
			scoreValues[n]= scoreValues[n-1];
			scoreNames[n] = scoreNames[n-1];
		}
		scoreValues[i] = score;
		scoreNames[i] = name;
		writeScores(scoreNames, scoreValues);
		return true;
	}
	public void goToGameOver(boolean canContinue){
		if (!canContinue) this.playSound("./sounds/Game Over.wav");
		pause = true;
		gameOverScreen = true;
		exit.setText("Submit score");
		exit.setBounds(400-(int)exit.getPreferredSize().getWidth()/2, 140, (int)exit.getPreferredSize().getWidth(), (int)exit.getPreferredSize().getHeight());
		continueButton.setBounds(400-(int)continueButton.getPreferredSize().getWidth()/2, 15 + (int)exit.getBounds().getY() + (int)exit.getBounds().getHeight(), (int)continueButton.getPreferredSize().getWidth(), (int)continueButton.getPreferredSize().getHeight());
		exit.setBackground(Color.gray);
		exit.setForeground(Color.lightGray);
		if (canContinue) continueButton.setVisible(true);
		repaint();
	}
	public void backToGame(){
		pause = true;
		pauseTimer = 3;
		gameOverScreen = false;
		exit.setBounds(245-(int)exit.getBounds().getWidth(), 135, (int)exit.getBounds().getWidth(), (int)exit.getBounds().getHeight());
		exit.setText("End Session");
		exit.setBackground(Color.red);
		exit.setForeground(Color.yellow);
		continueButton.setVisible(false);
		repaint();
	}
	
	//File functions
	public String[] getScores(){
		String[] send = new String[SCORE_SIZE];
		byte[] line = new byte[NAME_SIZE+4];
		try{
			reader = new FileInputStream(scoreBoard);
		}
		catch (IOException e){
			e.printStackTrace();
			for (int i=0;i<send.length;i++){send[i] = "ERROR";}
			return send;
		}
		for (int i=0;i<send.length;i++){
			try{
				reader.read(line);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			send[i] = new String(line, 0, NAME_SIZE);
			send[i] += ": " + toInt(line, NAME_SIZE);
		}
		return send;
	}
	public String[] getNames(String[] rawScoreData){
		String[] send = new String[SCORE_SIZE];
		if (rawScoreData == null) return send; //can't do crap to null
		char[] tempList, tempSend = new char[NAME_SIZE]; //minimize amount of ram needed (very minute difference compared to in the list)
		for (int i=0;i<Math.min(rawScoreData.length, SCORE_SIZE);i++){
			tempList = rawScoreData[i].toCharArray(); //get the char array of the raw score data
			System.arraycopy(tempList, 0, tempSend, 0, NAME_SIZE); //copy only the part that contains the name
			send[i] = new String(tempSend); //add the name to the list
		}
		return send; //send it back
	}
	public int[] getScores(String[] rawScoreData){
		int[] send = new int[SCORE_SIZE];
		if (rawScoreData == null) return send; //can't do crap to null
		char[] tempList, nums = new char[10]; //max size of number
		for (int i=0;i<Math.min(rawScoreData.length, SCORE_SIZE);i++){
			tempList = rawScoreData[i].toCharArray(); //get the letters of the score data
			System.arraycopy(tempList, NAME_SIZE+2, nums, 0, Math.min(tempList.length-NAME_SIZE-2, nums.length)); //get the number from the score data
			send[i] = Integer.parseInt((new String(nums)).trim()); //turn the string into a number
			nums = new char[10]; //reset nums so excess scores don't get carried over to the next person's score
		}
		return send; //send the scores
	}
	public boolean writeScores(String[] names, int[] scores){
		//writes the names and scores to the score board file, has built in failsafes, can pass in empty lists
		if (!hasScoreboard) return false; //cant write to what doesn't exist
		try{
			writer = new FileOutputStream(scoreBoard);
		}
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		byte[] line = new byte[NAME_SIZE+4];
		try{
			for (int i=0;i<SCORE_SIZE;i++){
				//write the name
				char[] letters = new char[] {'A', 'A', 'A'};
				if (names == null ? false : names.length > i) letters = names[i].toCharArray();
				for (int n=0;n<NAME_SIZE;n++){
					line[n] = (letters.length > n ? (byte)Character.toUpperCase(letters[n]) : (byte)' ');
				}
				//write the score (32 bit integer limit maximum ~ 2.1B)
				byte[] scoreBytes = toBytes((2<<SCORE_SIZE-i) * 100_0);
				if (scores == null ? false : scores.length > i) scoreBytes = toBytes(scores[i]);
				System.arraycopy(scoreBytes, 0, line, NAME_SIZE, 4);
				//actually write to the file
				writer.write(line);
			}
		}
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//Sounds
	public void playSound(String fileName){
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource(fileName)));
			clip.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	//Byte translation functions
	public static byte[] toBytes(int... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*4];
		
		for (int n=0;n<nums.length;n++){
			int num = nums[n];
			for (int i=0;i<4;i++){
				send[3-i+4*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static int toInt(byte[] array){
		return toInt(array, 0);
	}
	public static int toInt(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 4) throw new IllegalArgumentException("Not enough bytes to convert into integer, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		int send = 0;
		for (int i = 0;i<4;i++){
			send <<= 8;
			send |= (array[startIndex+i]&0xFF);
		}
		return send;
	}
	
	//Game loop function
	public void loop(){
		long counter = 0;
		while (true){
			try{
				Thread.currentThread().sleep(1000/frameRate);
			}
			catch(InterruptedException e){
				e.printStackTrace();
				return;
			}
			counter++;
			counter %= 174_681_717_210L; //mod by all the prime factors of speeds
			//redraw the current grid / piece position
			updateGame();
			if (pause && !gameOverScreen && !startMenu && counter % frameRate == 0){
				if (pauseTimer <= 0){
					pause = false;
					repaint();
				}
				pauseTimer --;
				updateGame();
				continue; //have the game loop continue after the pause
			}
			//Put the piece into the grid if it cant go any farther
			if (!canFall() && counter % speed == 0 && !pause){ //the piece cant fall anymore
				blockKeyPresses = true;
				
				if (snapping ? false : !snapToGrid()){
					goToGameOver(false);
				}
				
				resetPiece();
				
				blockKeyPresses = false;
			}
			if (counter % speed == 0 && !pause){
				fall(); //gravity
			}
		}
	}
}