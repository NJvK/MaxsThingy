package helpful;

import java.awt.Graphics;
import java.awt.Color;

public class Rectangle{
	private int x, y, width, height;
	public Rectangle(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	public Rectangle(int x, int y){
		this(x, y, 1, 1);
	}
	public int getX(){
		return this.x;
	}
	public int getY(){
		return this.y;
	}
	public int getWidth(){
		return this.width;
	}
	public int getHeight(){
		return this.height;
	}
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public void setWidth(int width){
		this.width = width;
	}
	public void setHeight(int height){
		this.height = height;
	}
	public boolean intersect(Rectangle other){
		return (this.x < other.getX() + other.getWidth()
			&& this.x + this.width > other.getX()
			&& this.y < other.getY() + other.getHeight()
			&& this.y + this.height > other.getY());
	}
	public boolean intersect(int x, int y){
		return this.intersect(new Rectangle(x, y));
	}
	public boolean[] intersect(Rectangle[] others){
		boolean[] send = new boolean[others.length];
		for (int i=0;i<others.length;i++){
			send[i] = this.intersect(others[i]);
		}
		return send;
	}
	public void draw(Graphics g){
		helpful.Drawer.drawThickRect(g, x, y, width, height, 3);
	}
	public void draw(Scalable s){
		helpful.Drawer.drawThickRect(s, x, y, width, height, 3);
	}
	public void fill(Graphics g){
		g.fillRect(x, y, width, height);
	}
	public void fill(Scalable g){
		g.fillRect(x, y, width, height);
	}
	public String toString(){
		//Debug info
		return "Rectangle at pos: " + x + ", " + y + " size of: " + width + ", " + height;
	}
}