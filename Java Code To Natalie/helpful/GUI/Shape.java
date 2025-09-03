package helpful.GUI;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import helpful.Rectangle;
import helpful.Scalable;
public class Shape implements Component{
	public static final int RECT = 0, CIRCLE = 1, OVAL = 2, POLYGON = 3, LINE = 4, ARC = 5;
	protected int x, y, width, height, type, startAngle, arcAngle;
	protected int[] xCoords, yCoords;
	protected Color color;
	protected boolean isVisible;
	//general constructors
	public Shape(int x, int y, int width, int height, int type){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.type = type;
		this.color = null;
		this.isVisible = true;
		this.xCoords = null;
		this.yCoords = null;
	}
	public Shape(int x, int y, int width, int height, int type, Color color){
		this(x, y, width, height, type);
		this.color = color;
	}
	//polygon constructors
	public Shape(int[] xCoords, int[] yCoords){
		this(helpful.Math.min(xCoords), helpful.Math.min(yCoords), helpful.Math.max(xCoords)-helpful.Math.min(xCoords), helpful.Math.max(yCoords)-helpful.Math.min(yCoords), POLYGON);
		//sets a rectangle to the smallest x, y coords encompessing the largest area for the farthest away point
		this.xCoords = xCoords;
		this.yCoords = yCoords;
	}
	public Shape(int[] xCoords, int[] yCoords, Color color){
		this(xCoords, yCoords);
		this.color = color;
	}
	//Arc constructors
	public Shape(int x, int y, int width, int height, int startAngle, int arcAngle){
		this(x, y, width, height, ARC);
		this.startAngle = startAngle;
		this.arcAngle = arcAngle;
	}
	public Shape(int x, int y, int width, int height, int startAngle, int arcAngle, Color color){
		this(x, y, width, height, startAngle, arcAngle);
		this.color = color;
	}
	
	//Methods
	public Rectangle toRect(){
		if (type == LINE)
			return new Rectangle(Math.min(x,width), Math.min(y,height), Math.abs(x-width), Math.abs(y-width));
		return new Rectangle(x,y,width,height);
	}
	public void draw(Graphics g){
		if (!isVisible) return; //cant see it
		if (color != null) g.setColor(color);
		switch (type){
			case RECT:
				toRect().fill(g);
				break;
			case CIRCLE:
				g.fillOval(x-width/2, y-width/2, width, width);
				break;
			case OVAL:
				g.fillOval(x, y, width, height);
				break;
			case POLYGON:
				if (xCoords == null || yCoords == null || xCoords.length > yCoords.length) return; //not created correctly
				g.fillPolygon(xCoords, yCoords, xCoords.length);
				break;
			case LINE:
				g.drawLine(x, y, width, height);
				break;
			case ARC:
				g.fillArc(x, y, width, height, startAngle, arcAngle);
				break;
		}
	}
	public void draw(Scalable s){
		if (!isVisible) return; //cant see it
		if (color != null) s.setColor(color);
		switch (type){
			case RECT:
				toRect().fill(s);
				break;
			case CIRCLE:
				s.fillOval(x-width/2, y-width/2, width, width);
				break;
			case OVAL:
				s.fillOval(x, y, width, height);
				break;
			case POLYGON:
				if (xCoords == null || yCoords == null || xCoords.length > yCoords.length) return; //not created correctly
				s.fillPolygon(xCoords, yCoords, xCoords.length);
				break;
			case LINE:
				s.drawLine(x, y, width, height);
				break;
			case ARC:
				s.fillArc(x, y, width, height, startAngle, arcAngle);
				break;
		}
	}
	
	public void debugDraw(Graphics g){
		draw(g);
		g.setColor(Color.white);
		toRect().draw(g);
		if (type >= 0 && type <= ARC) return;
		g.setColor(Color.red);
		g.fillRect(x-5, y-5, width+10, height+10);
		g.setColor(Color.white);
		g.setFont(new Font("Arial", Font.PLAIN, helpful.TextFitter.getFontSize(g, "No Type", width, height)));
		helpful.Drawer.drawStackString(g, "No Type\n(" + x + "," + y + ")", x, y, width);
	}
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public void click(MouseEvent e){ //for the component
		//do Nothing
	}
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
}