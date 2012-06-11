package simplify.align;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;

import simplify.AlignPair;

public class VisualText {
	private int x;
	private int y;
	private int width;
	private int height;
	private String text;
	
	public VisualText(String text, int x, int y, Graphics2D g){		
		g.drawString(text, x, y);
		
		// figure out the location of this text
		FontMetrics metrics = g.getFontMetrics();
		
		this.text = text;
		this.x = x;
		this.y = y;
		this.width = metrics.stringWidth(text);
		this.height = metrics.getHeight();
	}
	
	public String getText(){
		return text;
	}
	
	public int getMidX(){
		return x + width/2;
	}
	
	public int getEndX(){
		return x + width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public boolean contains(Point p){
		return p.getX() >= x && p.getX() <= (x + width) &&
			   p.getY() <= y && p.getY() >= (y - height);
	}	
}
