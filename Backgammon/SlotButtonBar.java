//SlotButtonBar Panel Constructor
import javax.swing.JButton;
//SlotButtonBar Panel paintComponent
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;

public class SlotButtonBar extends SlotButton
{	
	public SlotButtonBar(String text)
    {
		//Call Parent Constructor
		super();
		background = new Color(85, 60, 42);
    }
	
	public void setBkgdColor(Color b)
	{
		background = b;
		repaint();
	}
	
	//No definition needed
	public void setTriColor(Color t)
	{
		
	}
	
	public void setNoValidMoves(boolean flag)
	{
		if ( flag == true )
		{
			background = Color.RED;
		}
		else
		{
			background = new Color(85, 60, 42);
		}
		
		setCheckers( checkerColor, numCheckers );
	}
	
    public void paintComponent(Graphics g)
    {
        //Call superclass's paintcomponent
        super.paintComponent(g);

        //Make bar the same color as the border
        setBackground(background);
		
		Graphics2D g2d = (Graphics2D)g;
		
		//Makes edges smoother
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		int radius = getWidth()/4;
		int x = getWidth()/2 - radius;
		int y = getHeight()/2 - radius;
		
		//Only if changing checkers
		if( settingCheckers == true )
		{	
			Color checker = Color.RED;
			Color outline = Color.RED;
			
			//White checker
			if( checkerColor == 1 )
			{
				outline = new Color(250, 250, 250);
				checker = new Color(220, 220, 200);
			}
			
			//Blue checker
			if( checkerColor == 2 )
			{
				outline = new Color(32, 132, 176);
				checker = new Color(7, 107, 151);
			}

			//Using slightly lighter color for outline for appearance of depth
			g2d.setColor( outline );
			g2d.setStroke( new BasicStroke(3) );
			g2d.drawOval( x, y, radius * 2, radius * 2 );
			//Filling checker and adjusting font
			g2d.setColor( checker );
			g2d.fillOval( x, y, radius * 2, radius * 2 );
			g2d.setColor( new Color(32,32,32) );
			g2d.setFont( new Font("Calibri", Font.BOLD, radius) );
			
			//Adjusting for rare case of double digit checkers on space
			if( numCheckers > 9 )
			{
				g2d.drawString( Integer.toString(numCheckers), x + radius/2, y + radius*4/3 );
			}
			else
			{
				g2d.drawString( Integer.toString(numCheckers), x + radius*13/16, y + radius*4/3 );
			}
		}
		
		if( pressHighlight == true )
		{
			g2d.setColor(Color.GREEN);
			g2d.setStroke (new BasicStroke(4) );
			g2d.drawOval(x, y, radius * 2, radius * 2);
		}
		
		if( highlightingMoves == true )
		{
			g2d.setColor(Color.RED);
			g2d.setStroke (new BasicStroke(4) );
			g2d.drawOval(x, y, radius * 2, radius * 2);
		}
    }
}
