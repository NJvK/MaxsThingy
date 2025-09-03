import java.util.LinkedList;
import java.awt.Graphics;
import java.awt.Color;
import java.lang.StringBuilder;
public class Drawer{
	public static void drawStackString(Graphics g, String text, int x, int y){ //splits each new line based on \n escape codes
		String[] toWrite = split(text);
		y= y+(int)(g.getFontMetrics().getHeight()*(41.0/59));
		for (int line=0;line<toWrite.length;line++){
            g.drawString(toWrite[line], x, y + line*(g.getFontMetrics().getHeight()) + 3);    
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
	//Supporting cast of drawStackString and its children
	public static int getLines(Graphics g, String text, int width){
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
	public static String toSingleString(String[] strings){
		StringBuilder temp = new StringBuilder(); //more memory efficent
		for (String line : strings){
			temp.append(line + "\n");
		}
		return temp.toString();
	}
	public static String[] split(Graphics g, String text, int width){
        String[] sendString;
        LinkedList<String> buffer = new LinkedList<String>();
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