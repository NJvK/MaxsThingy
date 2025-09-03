package helpful;
import helpful.datastructures.DLList;
import java.awt.Graphics;
import java.awt.Color;
import java.lang.StringBuilder;
public class Drawer{
	//just a few shapes that require a lot of code to create
	public static void drawThickRect(Scalable s, int x, int y, int width, int length, int thickness){
		s.fillRect(x-thickness/2, y-thickness/2, thickness, length); //top left to bottom left
        s.fillRect(x-thickness/2, y-thickness/2 + length, width+thickness, thickness); //bottom left to bottom right
        s.fillRect(x-thickness/2 + width, y-thickness/2, thickness, length); //top right to bottom right
        s.fillRect(x-thickness/2, y-thickness/2, width+thickness, thickness); //top left to top right
	}
	public static void drawThickRect(Graphics g, int x, int y, int width, int length, int thickness){
        g.fillRect(x-thickness/2, y-thickness/2, thickness, length); //top left to bottom left
        g.fillRect(x-thickness/2, y-thickness/2 + length, width+thickness, thickness); //bottom left to bottom right
        g.fillRect(x-thickness/2 + width, y-thickness/2, thickness, length); //top right to bottom right
        g.fillRect(x-thickness/2, y-thickness/2, width+thickness, thickness); //top left to top right
    }
	public static void fillOctogon(Scalable s, int x, int y, int width, int length, boolean isCentered){
		if (isCentered){
			x-=width/2;
			y-=length/2;
		}
		int[] xPoints = new int[] {x, x+width/3, x+width*2/3, x+width, x+width, x+width*2/3, x+width/3, x};
		int[] yPoints = new int[] {y+length/3, y, y, y+length/3, y+length*2/3, y+length, y+length, y+length*2/3};
		s.fillPolygon(xPoints, yPoints, 8);
	}
	public static void fillOctogon(Graphics g, int x, int y, int width, int length, boolean isCentered){
		if (isCentered){
			x-=width/2;
			y-=length/2;
		}
		int[] xPoints = new int[] {x, x+width/3, x+width*2/3, x+width, x+width, x+width*2/3, x+width/3, x};
		int[] yPoints = new int[] {y+length/3, y, y, y+length/3, y+length*2/3, y+length, y+length, y+length*2/3};
		g.fillPolygon(xPoints, yPoints, 8);
	}
	public static void drawStackString(Scalable g, String text, int x, int y){ //splits each new line based on \n escape codes
		String[] toWrite = split(text);
		y= y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
            g.drawString(toWrite[line], x, y + line*(g.getFontMetrics().getHeight()) + 3);    
      	}
	}
	public static void drawStackString(Graphics g, String text, int x, int y){ //splits each new line based on \n escape codes
		String[] toWrite = split(text);
		y= y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
            g.drawString(toWrite[line], x, y + line*(g.getFontMetrics().getHeight()) + 3);    
      	}
	}
	public static void drawLineThick(Scalable g, int x1, int y1, int x2, int y2, int thickness){
        double slope = (y2-y1)/(double)(x2-x1);
        if (Math.abs(slope) < 1){
            for (int i=Math.min(x1, x2);i<Math.max(x1, x2);i++){
                int y = (int)(slope*(i-Math.min(x1, x2))+(x1 < x2 ? y1 : y2));
                g.fillRect(i, y-thickness/2, 1, thickness);
            }
        }
        else{
            for (int i=Math.min(y1,y2);i<Math.max(y1, y2);i++){
                int x = (int)((i-Math.min(y1, y2))/slope+(y1 < y2 ? x1 : x2));
                g.fillRect(x-thickness/2, i, thickness, 1);
            }
        }
    }
	public static void drawLineThick(Graphics g, int x1, int y1, int x2, int y2, int thickness){
        double slope = (y2-y1)/(double)(x2-x1);
        if (Math.abs(slope) < 1){
            for (int i=Math.min(x1, x2);i<Math.max(x1, x2);i++){
                int y = (int)(slope*(i-Math.min(x1, x2))+(x1 < x2 ? y1 : y2));
                g.fillRect(i, y-thickness/2, 1, thickness);
            }
        }
        else{
            for (int i=Math.min(y1,y2);i<Math.max(y1, y2);i++){
                int x = (int)((i-Math.min(y1, y2))/slope+(y1 < y2 ? x1 : x2));
                g.fillRect(x-thickness/2, i, thickness, 1);
            }
        }
    }
	//drawing a stacked string by checking if it ran out of width or \n is used and all its permutations
	public static void drawStackString(Graphics g, String text, int x, int y, int width){
      	String[] toWrite = split(g, text, width);
		y= y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
            g.drawString(toWrite[line], x, y + line*(g.getFontMetrics().getHeight()) + 3);    
      	}
    }
	public static void drawStackString(Scalable g, String text, int x, int y, int width){
      	String[] toWrite = split(g, text, width);
		y= y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
            g.drawString(toWrite[line], x, y + line*(g.getFontMetrics().getHeight()) + 3);    
      	}
    }
	public static void drawCenteredStackString(Graphics g, String text, int centerX, int y, int width){
		String[] toWrite = split(g, text, width);
		y = y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
			g.drawString(toWrite[line], centerX - g.getFontMetrics().stringWidth(toWrite[line])/2, (int)(y - toWrite.length/2.0*g.getFontMetrics().getHeight() + line*(g.getFontMetrics().getHeight())+3 + g.getFontMetrics().getHeight()*(23.0/59)));
		}
	}
	public static void drawCenteredStackString(Scalable g, String text, int centerX, int y, int width){
		String[] toWrite = split(g, text, width);
		y = y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
			g.drawString(toWrite[line], centerX - g.getFontMetrics().stringWidth(toWrite[line])/2, (int)(y - toWrite.length/2.0*g.getFontMetrics().getHeight() + line*(g.getFontMetrics().getHeight())+3 + g.getFontMetrics().getHeight()*(23.0/59)));
		}
	}
	public static void drawBottomUpStackString(Graphics g, String text, int x, int bottomY, int width){
		String[] toWrite = split(g, text, width);
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x, bottomY - (toWrite.length-line)*(g.getFontMetrics().getHeight())-3);
		}
	}
	public static void drawBottomUpStackString(Scalable g, String text, int x, int bottomY, int width){
		String[] toWrite = split(g, text, width);
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x, bottomY - (toWrite.length-line)*(g.getFontMetrics().getHeight())-3);
		}
	}
	public static void drawRightAlignedStackString(Scalable g, String text, int x, int y, int width){
		String[] toWrite = split(g, text, width);
		y = y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x - g.getFontMetrics().stringWidth(toWrite[line]), y + line*(g.getFontMetrics().getHeight())-3);
		}
	}
	public static void drawRightAlignedStackString(Graphics g, String text, int x, int y, int width){
		String[] toWrite = split(g, text, width);
		y = y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x - g.getFontMetrics().stringWidth(toWrite[line]), y + line*(g.getFontMetrics().getHeight())-3);
		}
	}
	public static void drawBottomRightAlignedStackString(Graphics g, String text, int x, int bottomY, int width){
		String[] toWrite = split(g, text, width);
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x + width - g.getFontMetrics().stringWidth(toWrite[line]), bottomY - line*(g.getFontMetrics().getHeight())-3);
		}
	}
	public static void drawBottomRightAlignedStackString(Scalable g, String text, int x, int bottomY, int width){
		String[] toWrite = split(g, text, width);
		for (int line = 0;line<toWrite.length;line++){
			g.drawString(toWrite[line], x + width - g.getFontMetrics().stringWidth(toWrite[line]), bottomY - line*(g.getFontMetrics().getHeight())-3);
		}
	}
	//Supporting cast of drawStackString and its children
	public static int getLines(Graphics g, String text, int width){
		return split(g, text, width).length;
	}
	public static int getLines(Scalable g, String text, int width){
		return split(g, text, width).length;
	}
	public static String[] stackStringOffset(Graphics g, String text, int width, int length, int offset){
		//drawing variable from paintComponent, the text, how wide the text can go, how many lines the text should last, the offset of where to start the string
		if (length < 1) return null;
		String[] send = new String[length];
		String[] temp = split(g, text, width);
		if (offset > temp.length) return null;
		System.arraycopy(temp, offset, send, 0, length);
		return send;
	}
	public static String[] stackStringOffset(Scalable g, String text, int width, int length, int offset){
		//drawing variable from paintComponent, the text, how wide the text can go, how many lines the text should last, the offset of where to start the string
		if (length < 1) return null;
		String[] send = new String[length];
		String[] temp = split(g, text, width);
		if (offset > temp.length) return null;
		System.arraycopy(temp, offset, send, 0, length);
		return send;
	}
	public static String toSingleString(String[] strings){
		StringBuilder temp = new StringBuilder(); //more memory efficent
		for (String line : strings){
			temp.append(line + "\n");
		}
		return temp.toString();
	}
	public static String[] split(Scalable g, String text, int width){
        String[] sendString;
        DLList<String> buffer = new DLList<String>();
        int line = 0, lastI=0;
        String send = "";
        for (int i=0;i<text.length();i++){
            boolean newLine = false;
			
            if (text.charAt(i) != '\n'){
                if (g.getFontMetrics().stringWidth(send + text.charAt(i)) > width){
                    for (int j=send.length()-1;j>=0;j--){
                        if (send.charAt(j) == '-'){
                            send = send.substring(0, j);
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                        else if (send.charAt(j) == ' '){
                            send = send.substring(0, j);
							//weird error occurs on only the first line break like this, the first letter of the next line is cut off
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                    }
                    newLine = true;
					
                }
                else{
                    send += text.charAt(i);
                }
            }
            if (text.charAt(i) == '\n' || newLine || i == text.length()-1){
                buffer.add(send);
				send = "";
				lastI = i;
            }
        }
        sendString = new String[buffer.size()];
		  for (int i=0;i<buffer.size();i++){
		  	sendString[i]=buffer.get(i);
		  }
        return sendString;
    }
	public static String[] split(Graphics g, String text, int width){
        String[] sendString;
        DLList<String> buffer = new DLList<String>();
        int line = 0, lastI=0;
        String send = "";
        for (int i=0;i<text.length();i++){
            boolean newLine = false;
			
            if (text.charAt(i) != '\n'){
                if (g.getFontMetrics().stringWidth(send + text.charAt(i)) > width){
                    for (int j=send.length()-1;j>=0;j--){
                        if (send.charAt(j) == '-'){
                            send = send.substring(0, j);
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                        else if (send.charAt(j) == ' '){
                            send = send.substring(0, j);
							//weird error occurs on only the first line break like this, the first letter of the next line is cut off
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                    }
                    newLine = true;
					
                }
                else{
                    send += text.charAt(i);
                }
            }
            if (text.charAt(i) == '\n' || newLine || i == text.length()-1){
                buffer.add(send);
				send = "";
				lastI = i;
            }
        }
        sendString = new String[buffer.size()];
		  for (int i=0;i<buffer.size();i++){
		  	sendString[i]=buffer.get(i);
		  }
        return sendString;
    }
	public static String[] split(String text){
		return text.split("\n");
	}
}