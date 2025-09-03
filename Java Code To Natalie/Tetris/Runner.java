import javax.swing.JFrame;


public class Runner{
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
		
		if (args.length > 0){
			if (args[0].equals("reset")){
				boolean res = screen.writeScores(new String[0], new int[0]);
				//System.out.println(res ? "Scores reset" : "scores were not changed");
			}
		} //resets the save file, the screen will not update until you reload the project
		
		
		screen.addKeyListener(screen);
		screen.addMouseListener(screen);
		screen.loop();
		
		
		//System.out.println(screen.checkCollision(new byte[] {9, 3}, (byte)1));
	}
}