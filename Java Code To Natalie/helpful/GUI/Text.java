package helpful.GUI;
import helpful.TextFitter;
import helpful.Rectangle;
import helpful.Scalable;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

public class Text extends TextFitter implements Component{
	public static final int TOP_LEFT = 0, CENTERED = 1, TOP_RIGHT = 2, BOTTOM_LEFT = 3, BOTTOM_RIGHT = 4;
	protected int x, y, type, textHeight;
	protected boolean isVisible;
	protected String style;
	protected Color textColor, bkgColor;
	public Text(int x, int y, int width, int height, String text){
		super(width, height, text);
		this.x = x;
		this.y = y;
		textHeight = -1;
		this.style = "Arial";
		this.isVisible = true;
		textColor = null;
		this.type = TOP_LEFT;
	}
	public Text(int x, int y, int width, int height, String text, int type){
		this(x, y, width, height, text);
		this.type = type;
	}
	public Text(int x, int y, int width, int height, String text, int type, Color textColor){
		this(x, y, width, height, text, type);
		this.textColor = textColor;
	}
	public Text(int x, int y, int width, int height, String text, Color textColor){
		this(x, y, width, height, text);
		this.textColor = textColor;
	}
	public Text(int x, int y, int width, int height, String text, String style){
		this(x, y, width, height, text);
		this.style = style;
	}
	public void setStyle(String style){
		this.style = style;
	}
	public void setTextColor(Color textColor){
		setColor(textColor);
	}
	public void setColor(Color textColor){
		this.textColor = textColor;
	}
	public Rectangle toRect(){
		return new Rectangle(x, y, width, height);
	}
	public void draw(Graphics g){
		textHeight = g.getFontMetrics().getHeight(); //always try and save it
		if (!isVisible) return;
		if (bkgColor != null){
			g.setColor(bkgColor);
			g.fillRect(x, y, width, height);
		}
		if (textColor != null) g.setColor(textColor);
		g.setFont(new Font(style, Font.PLAIN, getFontSize(g, style)));
		switch (type){
			case TOP_LEFT:
				helpful.Drawer.drawStackString(g, text, x, y-6, width);
				break;
			case CENTERED:
				helpful.Drawer.drawCenteredStackString(g, text, x + width/2, y + height/2-(int)(g.getFontMetrics().getHeight()*(23.0/59)), width);
				break;
			case TOP_RIGHT:
				helpful.Drawer.drawRightAlignedStackString(g, text, x+width, y, width);
				break;
			case BOTTOM_LEFT:
				helpful.Drawer.drawStackString(g, text, x, y+height-6-(int)(g.getFontMetrics().getHeight()*(41.0/59)), width);
				break;
			case BOTTOM_RIGHT:
				helpful.Drawer.drawRightAlignedStackString(g, text, x+width, y+height-(int)(g.getFontMetrics().getHeight()*(41.0/59)), width);
				break;
		}
	}
	public void draw(Scalable s){
		textHeight = s.getFontMetrics().getHeight(); //always try and save it
		if (!isVisible) return;
		if (bkgColor != null){
			s.setColor(bkgColor);
			s.fillRect(x, y, width, height);
		}
		if (textColor != null) s.setColor(textColor);
		s.setFont(new Font(style, Font.PLAIN, getFontSize(s, style)));
		switch (type){
			case TOP_LEFT:
				helpful.Drawer.drawStackString(s, text, x, y-6, width);
				break;
			case CENTERED:
				helpful.Drawer.drawCenteredStackString(s, text, x + width/2, y + height/2-(int)(s.getFontMetrics().getHeight()*(23.0/59)), width);
				break;
			case TOP_RIGHT:
				helpful.Drawer.drawRightAlignedStackString(s, text, x+width, y, width);
				break;
			case BOTTOM_LEFT:
				helpful.Drawer.drawStackString(s, text, x, y+height-6-(int)(s.getFontMetrics().getHeight()*(41.0/59)), width);
				break;
			case BOTTOM_RIGHT:
				helpful.Drawer.drawRightAlignedStackString(s, text, x+width, y+height-(int)(s.getFontMetrics().getHeight()*(41.0/59)), width);
				break;
		}
	}
	public void debugDraw(Graphics g){
		draw(g);
		g.setColor(Color.white);
		toRect().draw(g);
		if (type >= 0 && type <= BOTTOM_RIGHT) return;
		g.setColor(Color.red);
		g.fillRect(x-5, y-5, width+10, height+10);
		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, helpful.TextFitter.getFontSize(g, "No Type", width, height)));
		helpful.Drawer.drawStackString(g, "No Type\n(" + x + "," + y + ")", x, y, width);
	}
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public void click(MouseEvent e){ //required by component
		//do nothing
	}
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
	public void setDims(int x, int y, int width, int height){
		setPos(x, y);
		this.width = width;
		this.height = height;
	}
	public void setColors(Color... colors){
		//sets the colors of the components, requires at least 3 colors in the list
		if (colors.length < 2) return; //don't throw errors, just don't do it
		this.textColor = colors[0];
		this.bkgColor = colors[1];
	}
	public Color[] getColors(){
		return new Color[] {this.textColor, this.bkgColor};
	}
}