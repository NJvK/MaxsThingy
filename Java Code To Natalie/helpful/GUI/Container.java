package helpful.GUI;
import helpful.Rectangle;
import helpful.GUI.Component;
import helpful.datastructures.DLList;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import helpful.Scalable;
public class Container implements Component{
	//a container which contians components inside its bounds, can be smaller than the screen unlike Menu class
	private DLList<Component> comps;
	private Rectangle bounds;
	public boolean isVisible;
	public Container(){
		this.comps = new DLList<Component>();
		this.bounds = new Rectangle(0, 0, 1, 1);
		this.isVisible = true;
	}
	public Container(int x, int y){
		this();
		bounds = new Rectangle(x, y, 1, 1);
	}
	public Container(int x, int y, int width, int height){
		this();
		bounds = new Rectangle(x, y, width, height);
	}
	public Container(int x, int y, int width, int height, Component... components){
		this(x,y,width,height);
		comps.add(components);
	}
	public boolean add(Component comp){
		comps.add(comp);
		Rectangle pos = comp.toRect();
		comp.setPos(bounds.getX() + pos.getX(), bounds.getY() + pos.getY());
		return true;
	}
	public void add(Component... components){
		for (Component comp : components){
			add(comp);
		}
	}
	public boolean remove(Component comp){
		return comps.remove(comp);
	}
	public void draw(Graphics g){
		if (!isVisible) return; //dont draw if not visible
		for (Component comp : comps){
			comp.draw(g);
		}
	}
	public void draw(Scalable s){
		if (!isVisible) return;
		for (Component comp : comps){
			comp.draw(s);
		}
	}
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public void setAllVisible(boolean isVisible){
		this.isVisible = isVisible;
		for (Component comp : comps){
			comp.setVisible(isVisible);
		}
	}
	public Rectangle toRect(){
		return bounds;
	}
	public void click(MouseEvent e){ //does click logic if it exists
		if (!isVisible) return;
		for (Component item : comps){
			item.click(e);
		}
	}
	public void setPos(int x, int y){
		bounds.setX(x);
		bounds.setY(y);
	}
	public void setColors(Color... colors){
		for (Component comp : comps){
			comp.setColors(colors);
		}
	}
}