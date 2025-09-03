package helpful.GUI;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import helpful.Rectangle;
import helpful.datastructures.DLList;
import helpful.Scalable;
public class Menu{
	private DLList<Component> components;
	private DLList<java.awt.Component> awtComps;
	private Button back;
	private DLList<ButtonPair> menuButtons;
	private Menu parent;
	protected boolean isVisible; //also counts as has focus
	public Menu(){
		components = new DLList<Component>();
		awtComps = new DLList<java.awt.Component>();
		back = null;
		menuButtons = new DLList<ButtonPair>();
		parent = null;
		isVisible = true;
	}
	public Menu(Component... components){
		this();
		this.add(components);
	}
	public Menu(boolean visible){
		this();
		this.isVisible = visible;
	}
	public Menu(boolean visible, Component... components){
		this(components);
		this.isVisible = visible;
	}
	public void setBackButton(Button backButton){ //when clicked, changes the visibleMenu to the parent
	if (this.back != null) components.remove(back);
		this.back = backButton;
		for (Component item : components){
			if (item == back) return;
		}
		components.add(back); //will only reach if memory address isn't known to the menu
	}
	public void add(Component item){
		components.add(item);
	}
	public void add(Component... items){
		for (Component item : items){
			item.setVisible(this.isVisible);
			components.add(item);
		}
	}
	public void add(java.awt.Component comp){
		comp.setVisible(this.isVisible);
		awtComps.add(comp);
	}
	public void add(java.awt.Component... comps){
		for (java.awt.Component comp : comps){
			comp.setVisible(this.isVisible);
			awtComps.add(comp);
		}
	}
	public boolean remove(Component item){
		return components.remove(item);
	}
	public boolean remove(java.awt.Component comp){
		return awtComps.remove(comp);
	}
	public void addMenu(Menu menu, Button toMenuButton){ //set a certain button to change to the other menu
		menuButtons.add(new ButtonPair(menu, toMenuButton));
		menu.setParent(this);
		for (Component item : components){
			if (item == toMenuButton) return;
		}
		components.add(toMenuButton);
	}
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
		for (java.awt.Component comp : awtComps){
			comp.setVisible(isVisible);
		}
	}
	public boolean isVisible(){
		return this.isVisible;
	}
	public boolean click(MouseEvent e){
		if (!isVisible) return false;
		if ((back != null ? back.isClicked(e) : false) && parent != null){ //check if we should go to the parent
			this.goTo(parent);
			return true;
		}
		for (ButtonPair item : menuButtons){
			if (item.button.isClicked(e)){
				this.goTo(item.menu);
				return true;
			}
		}
		for (Component item : components){
			item.click(e);
		}
		return false;
	}
	public void goTo(Menu menu){
		this.setVisible(false);
		menu.setVisible(true);
	}
	public void click(MouseWheelEvent e){ //used only by components that are scollable
		if (!isVisible) return;
		for (Component item : components){
			if (item.isScrollable()) item.click(e);
		}
	}
	public void draw(Graphics g){
		if (!isVisible) return; //can't draw if its not visible
		drawCode(g);
		for (Component item : components){
			if (item == back && parent == null) continue; //dont draw the back button if there is no parent for it
			item.draw(g);
		}
	}
	public void draw(Scalable s){
		if (!isVisible) return; //can't draw if its not visible
		drawCode(s);
		for (Component item : components){
			if (item == back && parent == null) continue; //dont draw the back button if there is no parent for it
			item.draw(s);
		}
	}
	public void drawCode(Graphics g){
		//meant for overloading for eaiser draw code
	}
	public void drawCode(Scalable s){
		
	}
	public void debugDraw(Graphics g){
		if (!isVisible) return; //still respecting who has focus
		for (Component item : components){
			if (item == back && parent == null) continue; //dont draw the back button if there is no parent for it
			item.debugDraw(g);
		}
	}
	public void setParent(Menu parent){
		this.parent = parent;
	}
	private class ButtonPair{
		public Button button;
		public Menu menu;
		public ButtonPair(Menu menu, Button button){
			this.button = button;
			this.menu = menu;
		}
	}
}