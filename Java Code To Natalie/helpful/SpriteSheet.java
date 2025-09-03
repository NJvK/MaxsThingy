package helpful;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
public class SpriteSheet{
	private Image[] images;
	public SpriteSheet(int rows, int cols, int count, BufferedImage sheet){ //for when the number of cells is not the multiple of rows*cols
		images = new Image[count];
		int x = sheet.getWidth()/rows;//smaller variables to store the size in each dimension of each cell
		int y = sheet.getHeight()/cols;
		for (int c=0;c<cols;c++){
			for (int r=0;r<rows;r++){
				images[c*rows+r] = (Image)(sheet.getSubimage(x*r, c*y, x, y));
			}
		}
	}
	public SpriteSheet(int rows, int cols, BufferedImage sheet){
		this(rows, cols, rows*cols, sheet);
	}
	public void draw(Graphics g, int x, int y, int index, Color bkgclr){
		g.drawImage(images[index%images.length], x, y, bkgclr, null);
	}
	public void draw(Scalable g, int x, int y, int index, Color bkgclr){
		g.drawImage(images[index%images.length], x, y, bkgclr, null);
	}
	public int size(){
		return images.length;
	}
	public Image get(int index){
		return images[index];
	}
	public int getHeight(int index){
		return images[index].getHeight(null);
	}
	public int getWidth(int index){
		return images[index].getWidth(null);
	}
	public void setAllScale(int width, int height){
		for (int i=0;i<images.length;i++){
			images[i] = images[i].getScaledInstance(width, height, 0);
		}
	}
}