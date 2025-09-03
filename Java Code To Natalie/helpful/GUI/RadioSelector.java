package helpful.GUI;
import helpful.GUI.Button;
import helpful.Rectangle;
import helpful.Drawer;
import helpful.TextFitter;
import helpful.Scalable;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
public class RadioSelector implements Component{
	private Button[] buttons;
	private TextFitter[] textFitters;
	private int x, y, selection, width, length;
	private Rectangle collide;
	private Color bkgclr;
	private boolean isVisible;
	public RadioSelector(int x, int y, int width, int length, int buttonsHorizontal, int buttonsVertical){
		buttons = new Button[buttonsHorizontal * buttonsVertical];
		textFitters = null;
		int buttonWidth = width / buttonsHorizontal;
		int buttonHeight = length / buttonsVertical;
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new Button(x + (i%buttonsHorizontal) * buttonWidth, y + (i/buttonsHorizontal)*buttonHeight, buttonWidth, buttonHeight);
		}
		this.selection = -1;
		this.x = x;
		this.y = y;
		this.width = Math.max(width, 10); //makes the minimum size 10 x 10
		this.length = Math.max(length, 10);
		this.collide = new Rectangle(x, y, width, length);
		this.bkgclr = Color.white;
		this.isVisible=true;
	}
	public RadioSelector(int x, int y, int width, int length, int buttonsHorizontal, int buttonsVertical, String[] names, Color backgroundColor){
		this(x, y, width, length, buttonsHorizontal, buttonsVertical);
		buttons = new Button[Math.min(buttonsHorizontal * buttonsVertical, names.length)];
		textFitters = new TextFitter[Math.min(buttonsHorizontal * buttonsVertical, names.length)];
		int buttonWidth = width / buttonsHorizontal;
		int buttonHeight = length / buttonsVertical;
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new Button(x + (i%buttonsHorizontal) * buttonWidth, y + (i/buttonsHorizontal)*buttonHeight, buttonWidth, buttonHeight, backgroundColor, names[i]);
			textFitters[i] = new TextFitter(buttonWidth - Math.min(buttonWidth, buttonHeight)-6, buttonHeight, names[i]);
		}
	}
	public RadioSelector(int x, int y, int width, int length, int buttonsHorizontal, int buttonsVertical, Image[] images, Color backgroundColor){
		this(x, y, width, length, buttonsHorizontal, buttonsVertical);
		buttons = new Button[Math.min(buttonsHorizontal * buttonsVertical, images.length)];
		textFitters = null;
		int buttonWidth = width / buttonsHorizontal;
		int buttonHeight = length / buttonsVertical;
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new Button(x + (i%buttonsHorizontal) * buttonWidth, y + (i/buttonsHorizontal)*buttonHeight, buttonWidth, buttonHeight, backgroundColor, images[i]);
		}
	}
	public void draw(Graphics g){
		if (!isVisible) return; //dont draw if not visible
		g.setColor(bkgclr);
		g.fillRect(x, y, width, length);
		for (int i =0;i<buttons.length;i++){
			if (buttons[i].getFontSize() == -1 || buttons[i].getText().equals("")) buttons[i].draw(g); //gets the font size of the button
			
			Rectangle temp = buttons[i].toRectangle();
			if (!buttons[i].getText().equals("") && textFitters != null){ //only draw these things if there is text on the button and the text fitters exist
				g.setColor(bkgclr);
				temp.fill(g);
				g.setColor(Button.getOppositeColor(bkgclr));
				g.setFont(new Font("Arial", Font.PLAIN, textFitters[i].getFontSize(g)));
				//System.out.println(textFitters[i].getFontSize(g) + ", " + textFitters[i].getText());
				Drawer.drawStackString(g, buttons[i].getText(), temp.getX() + 4 + Math.min(temp.getHeight(), temp.getWidth())-4, temp.getY() + 2 + temp.getHeight()/2 - g.getFontMetrics().getHeight()/2);
			}
			//draw the bubble selector on the left of the button
			g.setColor(Color.black);
			g.fillOval(temp.getX() + 2, temp.getY() + 2, Math.min(temp.getHeight(), temp.getWidth())-4, Math.min(temp.getHeight(), temp.getWidth())-4);
			g.setColor(Color.white);
			g.fillOval(temp.getX() + 4, temp.getY() + 4, Math.min(temp.getHeight(), temp.getWidth())-8, Math.min(temp.getHeight(), temp.getWidth())-8);
			if (selection == i){
				g.setColor(Color.black);
				g.fillOval(temp.getX() + 5, temp.getY()+5, Math.min(temp.getHeight(), temp.getWidth())-10, Math.min(temp.getHeight(), temp.getWidth())-10);
			}
		}
	}
	public void draw(Scalable s){
		if (!isVisible) return;
		s.setColor(bkgclr);
		s.fillRect(x, y, width, length);
		for (int i =0;i<buttons.length;i++){
			if (buttons[i].getFontSize() == -1 || buttons[i].getText().equals("")) buttons[i].draw(s); //gets the font size of the button
			
			Rectangle temp = buttons[i].toRectangle();
			if (!buttons[i].getText().equals("") && textFitters != null){ //only draw these things if there is text on the button and the text fitters exist
				s.setColor(bkgclr);
				temp.fill(s);
				s.setColor(Button.getOppositeColor(bkgclr));
				s.setFont(new Font("Arial", Font.PLAIN, textFitters[i].getFontSize(s)));
				//System.out.println(textFitters[i].getFontSize(s) + ", " + textFitters[i].getText());
				Drawer.drawStackString(s, buttons[i].getText(), temp.getX() + 4 + Math.min(temp.getHeight(), temp.getWidth())-4, temp.getY() + 2 + temp.getHeight()/2 - s.getFontMetrics().getHeight()/2);
			}
			//draw the bubble selector on the left of the button
			s.setColor(Color.black);
			s.fillOval(temp.getX() + 2, temp.getY() + 2, Math.min(temp.getHeight(), temp.getWidth())-4, Math.min(temp.getHeight(), temp.getWidth())-4);
			s.setColor(Color.white);
			s.fillOval(temp.getX() + 4, temp.getY() + 4, Math.min(temp.getHeight(), temp.getWidth())-8, Math.min(temp.getHeight(), temp.getWidth())-8);
			if (selection == i){
				s.setColor(Color.black);
				s.fillOval(temp.getX() + 5, temp.getY()+5, Math.min(temp.getHeight(), temp.getWidth())-10, Math.min(temp.getHeight(), temp.getWidth())-10);
			}
		}
	}
	public void click(MouseEvent e){
		if (!collide.intersect(e.getX(), e.getY())) return; //no click
		for (int i=0;i<buttons.length;i++){
			if (buttons[i].isClicked(e)){
				selection = i;
				return;
			}
		}
	}
	public void click(int x, int y){
		if (!collide.intersect(x, y)) return;
		for (int i=0;i<buttons.length;i++){
			if (buttons[i].click(x,y)){
				selection = i;
				return;
			}
		}
	}
	public int getSelection(){
		return selection;
	}
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public Rectangle toRect(){
		return collide;
	}
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
}