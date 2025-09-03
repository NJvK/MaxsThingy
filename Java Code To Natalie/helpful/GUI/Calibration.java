package helpful.GUI;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import helpful.GUI.Button;
import helpful.GUI.Component;
import helpful.datastructures.ShiftRegister;
import helpful.Rectangle;
import helpful.TextFitter;
import helpful.Scalable;
public class Calibration implements Component{
	private Button xP, xM, yP, yM, rP, rM;
	private int focus, rad, xSize, ySize;
	public int dx, dy;
	private ShiftRegister<Integer> locs;
	private boolean isVisible;
	private TextFitter tF, nums;
	Font wordFont, numFont;
	
	public Calibration(){
		xSize = 800;
		ySize = 600;
		xP= new Button(725, 25, 50, 50, "+"); //xSize*29/32, ySize*1/24, xSize/16, ySize/12
		xM= new Button(625, 25, 50, 50, "-"); //xSize*25/32, ySize*1/24, xSize/16, ySize/12
		yP= new Button(725, 100, 50, 50, "+");//xSize*29/32, ySize*1/6 , xSize/16, ySize/12
		yM= new Button(625, 100, 50, 50, "-");//xSize*25/32, ySize*1/6 , xSize/16, ySize/12
		rP= new Button(725, 175, 50, 50, "+");//xSize*29/32, ySize*7/24, xSize/16, ySize/12
		rM= new Button(625, 175, 50, 50, "-");//xSize*25/32, ySize*7/24, xSize/16, ySize/12
		focus = 0;
		dx=-7;
		dy=-47;
		rad=1;
		
		locs = new ShiftRegister<Integer>(10);
		
		tF = new TextFitter(xSize/16, ySize/12, "X-Diff");
		nums = new TextFitter(xSize/16, ySize/12, dy+"");
		
		isVisible = true;
	}
	public Rectangle toRect(){
		return new Rectangle(0, 0, xSize, ySize);
	}
	public void setVisible(boolean isVisible){
		this.isVisible  = isVisible;
	}
	public boolean isVisible(){
		return this.isVisible;
	}
	public void setPos(int x, int y){
		//does nothing
	}
	public void draw(Graphics g){
		if (!isVisible) return; //cannot draw what we cant see
		g.setColor(Color.black);
		g.fillRect(0,0,(int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight());//Cascadia Mono-Blocky, Lucida Console-Blocky, Consolas-UPPER, Courier-SmallBlocks
		wordFont = new Font("Consolas", Font.PLAIN, tF.getFontSize(g, "Consolas"));
		numFont = new Font("Consolas", Font.PLAIN, nums.getFontSize(g, "Consolas"));
		
		g.setColor(Color.white);
		
		for (int i=0;i<locs.size();i+=2){
			g.fillOval(locs.get(i+1)-rad/2, locs.get(i)-rad/2, rad,rad);
		}
		
		g.setColor(Color.darkGray);
		g.fillRect((int)g.getClipBounds().getWidth()*3/4, 0, (int)g.getClipBounds().getWidth()/4, (int)g.getClipBounds().getHeight()/2);
		
		g.setColor(Color.white);
		if (focus != 0){
			g.drawRect((int)g.getClipBounds().getWidth()*25/32, 2+(focus-1)*ySize/8, xSize*3/16, ySize/8);
		}
		
		g.setFont(wordFont);
		g.drawString("X-Diff", xSize*7/8 - g.getFontMetrics().stringWidth("X-Diff")/2, ySize/25);
		g.setFont(numFont);
		g.drawString(dx+"", xSize*7/8-g.getFontMetrics().stringWidth(dx+"")/2, ySize/25+g.getFontMetrics().getHeight());
		
		g.setFont(wordFont);
		g.drawString("Y-Diff", xSize*7/8-g.getFontMetrics().stringWidth("Y-Diff")/2, ySize/6);
		g.setFont(numFont);
		g.drawString(dy+"", xSize*7/8-g.getFontMetrics().stringWidth(dy+"")/2, ySize/6+g.getFontMetrics().getHeight());
		
		g.setFont(wordFont);
		g.drawString("Radius", xSize*7/8-g.getFontMetrics().stringWidth("Radius")/2, ySize*2/7);
		g.setFont(numFont);
		g.drawString(rad+"", xSize*7/8-g.getFontMetrics().stringWidth(rad+"")/2, ySize*2/7+g.getFontMetrics().getHeight());
		
		xP.draw(g);
		xM.draw(g);
		yP.draw(g);
		yM.draw(g);
		rP.draw(g);
		rM.draw(g);
	}
	public void draw(Scalable s){
		if (!isVisible) return; //cannot draw what we cant see
		s.setColor(Color.black);
		s.fillRect(0,0,(int)s.getClipBounds().getWidth(), (int)s.getClipBounds().getHeight());//Cascadia Mono-Blocky, Lucida Console-Blocky, Consolas-UPPER, Courier-SmallBlocks
		wordFont = new Font("Consolas", Font.PLAIN, tF.getFontSize(s, "Consolas"));
		numFont = new Font("Consolas", Font.PLAIN, nums.getFontSize(s, "Consolas"));
		
		s.setColor(Color.white);
		
		for (int i=0;i<locs.size();i+=2){
			s.fillOval(locs.get(i+1)-rad/2, locs.get(i)-rad/2, rad,rad);
		}
		
		s.setColor(Color.darkGray);
		s.fillRect((int)s.getClipBounds().getWidth()*3/4, 0, (int)s.getClipBounds().getWidth()/4, (int)s.getClipBounds().getHeight()/2);
		
		s.setColor(Color.white);
		if (focus != 0){
			s.drawRect((int)s.getClipBounds().getWidth()*25/32, 2+(focus-1)*ySize/8, xSize*3/16, ySize/8);
		}
		
		s.setFont(wordFont);
		s.drawString("X-Diff", xSize*7/8 - s.getFontMetrics().stringWidth("X-Diff")/2, ySize/25);
		s.setFont(numFont);
		s.drawString(dx+"", xSize*7/8-s.getFontMetrics().stringWidth(dx+"")/2, ySize/25+s.getFontMetrics().getHeight());
		
		s.setFont(wordFont);
		s.drawString("Y-Diff", xSize*7/8-s.getFontMetrics().stringWidth("Y-Diff")/2, ySize/6);
		s.setFont(numFont);
		s.drawString(dy+"", xSize*7/8-s.getFontMetrics().stringWidth(dy+"")/2, ySize/6+s.getFontMetrics().getHeight());
		
		s.setFont(wordFont);
		s.drawString("Radius", xSize*7/8-s.getFontMetrics().stringWidth("Radius")/2, ySize*2/7);
		s.setFont(numFont);
		s.drawString(rad+"", xSize*7/8-s.getFontMetrics().stringWidth(rad+"")/2, ySize*2/7+s.getFontMetrics().getHeight());
		
		xP.draw(s);
		xM.draw(s);
		yP.draw(s);
		yM.draw(s);
		rP.draw(s);
		rM.draw(s);
	}
	public void click(MouseEvent e){
		this.mouseClicked(e);
	}
	public void mouseClicked(MouseEvent e) {
		boolean click = false;
		int x = e.getX()+dx;
		int y = e.getY()+dy;
		if (xP.click(x,y)) {dx++;click=true;focus=1;}
		if (xM.click(x,y)) {dx--;click=true;focus=1;}
		if (yP.click(x,y)) {dy++;click=true;focus=2;}
		if (yM.click(x,y)) {dy--;click=true;focus=2;}
		if (rP.click(x,y)) {rad++;if(rad>50)rad=50;click=true;focus=3;}
		if (rM.click(x,y)) {rad--;if(rad<1)rad=1;click=true;focus=3;}
		
		if (!click && (x<600 || y > 230)){
			locs.add(x);
			locs.add(y);
		}
	}
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode() == 61 || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
			//=
			switch(focus){
				case 1:
					dx++;
					break;
				case 2:
					dy++;
					break;
				case 3:
					rad++;
					if (rad > 50) rad=50;
					break;
			}
		}
		if (e.getKeyCode() == 45 || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){ //minus, left arrow, or A
			//-
			switch(focus){
				case 1:
					dx--;
					break;
				case 2:
					dy--;
					break;
				case 3:
					rad--;
					if(rad < 1) rad = 1;
					break;
			}
		}
		if (e.getKeyCode() == 88){focus = 1;} //x
		if (e.getKeyCode() == 89) focus = 2; //y
		if (e.getKeyCode() == 82) focus = 3; //r
		if (e.getKeyCode() == 38 || e.getKeyCode() == KeyEvent.VK_W){ //Up arrow key or w key
			focus--;
			if (focus <= 0) focus = 3;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
			focus++;
			if (focus > 3) focus = 1;
		}
    }
	public void scale(int xSize, int ySize){
		if (this.xSize == xSize && this.ySize == ySize) return; //no point doing bunch of math with no change in results
		
		tF.setBounds(xSize/16, ySize/12);
		nums.setBounds(xSize/16, ySize/12);
		
		this.xSize = xSize;
		this.ySize = ySize;
		
		xP.setDimensions(xSize*29/32, ySize*1/24, xSize/16, ySize/12);
		xM.setDimensions(xSize*25/32, ySize*1/24, xSize/16, ySize/12);
		yP.setDimensions(xSize*29/32, ySize*1/6 , xSize/16, ySize/12);
		yM.setDimensions(xSize*25/32, ySize*1/6 , xSize/16, ySize/12);
		rP.setDimensions(xSize*29/32, ySize*7/24, xSize/16, ySize/12);
		rM.setDimensions(xSize*25/32, ySize*7/24, xSize/16, ySize/12);
	}
}