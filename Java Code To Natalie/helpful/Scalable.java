package helpful;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageObserver;

import java.awt.event.MouseEvent;

import helpful.Rectangle;

//class to allow for easy drawing to a scaled window
public class Scalable{
	private Graphics toDraw;
	private Rectangle originalCanvas;
	private double scaleX, scaleY;
	private AffineTransform transform;
	private Color bkgColor = null;
	private Font nonScaledFont;
	
	private Point2D tempPoint;
	
	private long flags;
	
	private static final int PRESERVE_ASPECT_RATIO = 0;
	
	
	public Scalable(int width, int height){
		originalCanvas = new Rectangle(0, 0, width, height);
		transform = new AffineTransform();
		transform.setToIdentity(); //sets it to no scale
	}
	
	public void init(Graphics drawingCanvas){ //saves the value for use later in the draw methods, call in your draw loop
		this.toDraw = drawingCanvas;
		if (nonScaledFont == null) nonScaledFont = toDraw.getFont();
		if (getFlag(PRESERVE_ASPECT_RATIO)){
			if (toDraw.getClipBounds().getWidth() / originalCanvas.getWidth() < toDraw.getClipBounds().getHeight() / originalCanvas.getHeight()){
				double scale = toDraw.getClipBounds().getWidth() / originalCanvas.getWidth();
				transform.setToScale(scale, scale);
				scaleX = scale;
				scaleY = scale;
			}
			else{
				double scale = toDraw.getClipBounds().getHeight() / originalCanvas.getHeight();
				transform.setToScale(scale, scale);
				scaleX = scale;
				scaleY = scale;
			}
		}
		else{
			scaleX = toDraw.getClipBounds().getWidth() / originalCanvas.getWidth();
			scaleY = toDraw.getClipBounds().getHeight() / originalCanvas.getHeight();
			transform.setToScale(scaleX, scaleY);
		}
		
		if (bkgColor != null){
			toDraw.setColor(bkgColor);
			toDraw.fillRect(0, 0, (int)(originalCanvas.getWidth()*scaleX), (int)(originalCanvas.getHeight()*scaleY));
		}
	}
	/// things that need scaling
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle){ //drawArc
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x, y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawArc((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), startAngle, arcAngle);
	}
	public void drawLine(int x1, int y1, int x2, int y2){ //drawLine P2D t = transform.transfrom(Point2D.Double(x, y)); x= t.getX(), y=t.getY();
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x1, y1);
		tempPoint = transform.transform(tempPoint, null);
		x1 = (int)tempPoint.getX();
		y1 = (int)tempPoint.getY();
		tempPoint = new Point2D.Double(x2, y2);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawLine(x1, y1, (int)tempPoint.getX(), (int)tempPoint.getY());
	}
	public void drawOval(int x, int y, int width, int height){ //drawOval
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawOval((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints){ //drawPolygon + alts
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		if (nPoints > xPoints.length || nPoints > yPoints.length) throw new IllegalArgumentException("xPoints list and yPoints list both must have a length greater than the nPoints");
		double[] list = new double[nPoints*2];
		for (int i=0;i<list.length;i+=2){
			list[i] = xPoints[i/2];
			list[i+1]=yPoints[i/2];
		}
		transform.transform(list, 0, list, 0, nPoints);
		for (int i=0;i<nPoints;i+=2){
			xPoints[i/2] = (int)list[i];
			yPoints[i/2] = (int)list[i+1];
		}
		toDraw.drawPolygon(xPoints, yPoints, nPoints);
	}
	public void drawPolygon(Polygon poly){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
	}
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints){ //drawPolyline
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		if (nPoints > xPoints.length || nPoints > yPoints.length) throw new IllegalArgumentException("xPoints list and yPoints list both must have a length greater than the nPoints");
		double[] list = new double[nPoints*2];
		for (int i=0;i<list.length;i+=2){
			list[i] = xPoints[i/2];
			list[i+1]=yPoints[i/2];
		}
		transform.transform(list, 0, list, 0, nPoints);
		for (int i=0;i<nPoints;i+=2){
			xPoints[i/2] = (int)list[i];
			yPoints[i/2] = (int)list[i+1];
		}
		toDraw.drawPolyline(xPoints, yPoints, nPoints);
	}
	public void drawRect(int x, int y, int width, int height){ //drawRect
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawRect((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight){ //drawRoundRect
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawRoundRect((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), (int)(arcWidth*scaleX), (int)(arcHeight*scaleY));
	}
	public void drawString(String str, int x, int y){ //drawString(Str, int, int); //Note: will be scaled from bottom left corner instead of top left like all other items
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.drawString(str, (int)tempPoint.getX(), (int)tempPoint.getY());
	}
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle){ //fillArc
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x, y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.fillArc((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), startAngle, arcAngle);
	}
	public void fillOval(int x, int y, int width, int height){ //fillOval
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.fillOval((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints){ //fillPolygon + alts
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		if (nPoints > xPoints.length || nPoints > yPoints.length) throw new IllegalArgumentException("xPoints list and yPoints list both must have a length greater than the nPoints");
		double[] list = new double[nPoints*2];
		for (int i=0;i<list.length;i+=2){
			list[i] = xPoints[i/2];
			list[i+1]=yPoints[i/2];
		}
		transform.transform(list, 0, list, 0, nPoints);
		for (int i=0;i<list.length;i+=2){
			xPoints[i/2] = (int)list[i];
			yPoints[i/2] = (int)list[i+1];
		}
		toDraw.fillPolygon(xPoints, yPoints, nPoints);
	}
	public void fillPolygon(Polygon poly){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);
	}
	public void fillRect(int x, int y, int width, int height){ //fillRect
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.fillRect((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight){ //fillRoundRect
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.fillRoundRect((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), (int)(arcWidth*scaleX), (int)(arcHeight*scaleY));
	}
	public Font getFont(){ //getFont
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.getFont();
	}
	public java.awt.FontMetrics getFontMetrics(){ //getFontMetrics + alts
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.getFontMetrics(nonScaledFont);
	}
	public java.awt.FontMetrics getFontMetrics(Font font){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.getFontMetrics(font);
	}
	public boolean hitClip(int x, int y, int width, int height){ //hitClip
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		return toDraw.hitClip((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void setClip(int x, int y, int width, int height){ //setClip(int, int, int, int)
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		toDraw.setClip((int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY));
	}
	public void setFont(Font font){ //setFont
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		nonScaledFont = font;
		toDraw.setFont(font.deriveFont(transform));
	}
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver io){ //drawImage + alts
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		try{
			return toDraw.drawImage(img.getScaledInstance((int)(img.getWidth(io)*scaleX), (int)(img.getHeight(io)*scaleY), 0), (int)tempPoint.getX(), (int)tempPoint.getY(), bgcolor, io);
		} catch (IllegalArgumentException e){
			return false;
		}
		
	}
	public boolean drawImage(Image img, int x, int y, ImageObserver io){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		try{
			return toDraw.drawImage(img.getScaledInstance((int)(img.getWidth(io)*scaleX), (int)(img.getHeight(io)*scaleY), 0), (int)tempPoint.getX(), (int)tempPoint.getY(), io);
		} catch (IllegalArgumentException e){
			return false;
		}
	}
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver io){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		return toDraw.drawImage(img, (int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), bgcolor, io);
	}
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver io){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		tempPoint = new Point2D.Double(x,y);
		tempPoint = transform.transform(tempPoint, null);
		return toDraw.drawImage(img, (int)tempPoint.getX(), (int)tempPoint.getY(), (int)(width*scaleX), (int)(height*scaleY), io);
	}
	
	public void setBackgroundColor(Color backgroundColor){ //setBackgroundColor
		this.bkgColor = backgroundColor;
	}
	public Color getColor(){ //getColor
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.getColor();
	}
	public void setColor(Color newColor){ //setColor
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		toDraw.setColor(newColor);
	}
	public void setPaintMode(){ //setPaintMode
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		toDraw.setPaintMode();
	}
	public void setXORMode(Color otherColor){ //setXORMode
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		toDraw.setXORMode(otherColor);
	}
	public String toString(){ //toString
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.toString();
	}
	
	public void preserveAspectRatio(boolean value){
		this.setFlag(PRESERVE_ASPECT_RATIO, value);
	}
	
	public Graphics graphics(){
		return toDraw;
	}
	
	public java.awt.Rectangle getClipBounds(){
		if (toDraw == null || transform == null) throw new IllegalArgumentException("You must call init(Graphics) before you can start drawing");
		return toDraw.getClipBounds();
	}
	
	public int[] getCoord(int[] originalCoord){
		if (originalCoord.length < 2) throw new IllegalArgumentException("Coord list must be of length greater than or equal to 2");
		try{
			transform.invert();
		}
		catch (java.awt.geom.NoninvertibleTransformException e){
			return originalCoord;
		}
		
		tempPoint = new Point2D.Double(originalCoord[0], originalCoord[1]);
		tempPoint = transform.transform(tempPoint, null);
		int[] send = new int[] {(int)tempPoint.getX(), (int)tempPoint.getY()};
		
		try{
			transform.invert();
		}
		catch (java.awt.geom.NoninvertibleTransformException e){
			return originalCoord;
		}
		return send;
	}
	public int[] getCoord(MouseEvent e){
		int[] send = new int[] {e.getX(), e.getY()};
		return getCoord(send);
	}
	public void convertMouseEvent(MouseEvent e){
		int[] coord = getCoord(e);
		coord[0] = -e.getX() + coord[0];
		coord[1] = -e.getY() + coord[1];
		e.translatePoint(coord[0], coord[1]);
	}
	
	//bitwise manipulations
	public void setFlag(int flag, boolean value){
		if (value){
			flags |= (1<<flag); //flips flag to 1
			return;
		}
		flags &= ~(1<<flag); //flips flag to 0
		return;
	}
	public boolean getFlag(int flag){
		return ((flags >> flag) &0b1) == 1;
	}

}