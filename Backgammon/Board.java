import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.util.Random;
import java.util.Scanner;

//Menu Event Handling
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//Class to put the entire board together
public class Board
{
    //GUI
    private JFrame frame;
    private InfoPanel infoPanel;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
	private int firstPressed, secondPressed;
    //LOGIC
    private Slot [] bkBoard = new Slot[28];		//Array references of Slots on the board
	private boolean [] moves;					//List of valid moves updated during each turn
	private boolean [] hitMoves;				//List of hit moves updated during each turn
	private boolean validMovesExist;			//Keeps track of the existence of valid moves each turn
	private boolean d1Used, d2Used, d3Used;		//Keeps track of rolls used during each turn
	private int move1, move2, move3;			//Moves specific to player updated each turn
	private int d1, d2;

    //Create an empty board
    public Board()
    {
        for(int i = 0; i < 28; i++)
        {
            bkBoard[i] = new Slot();
        }
    }

    //Game-play loop
    public void play()
    {
        //Initialize all Panels and Frame
		drawGame();
		
		//Put/draw checkers to the right locations on the board
		setUpBoard();
		
		//Introduction w/ user
		infoPanel.changeText("Welcome to Backgammon!\n");

		infoPanel.changeText("Roll the dice to determine who goes first!");
		infoPanel.changeText("...........");
		infoPanel.changeText("Player 1 rolled the highest value! He goes first!");
		
        while( gameOver() == false )
        {	
			d1 = -1;
			d2 = -1;

			System.out.println("Player 1's turn");
			infoPanel.changeText("Player 1's turn! Please roll the dice.");
			//Doubles not yet supported
			do{
				//If player doesn't need to roll again, and player has clicked the button --- retrieve dice values
				if( controlPanel.needsToRoll() == false && controlPanel.hasPressedButton() == true)
				{
					d1 = controlPanel.getDiceOne();
					d2 = controlPanel.getDiceTwo();
					
					//Handle Doubles
					if( d1 == d2)
					{
						controlPanel.resetRoll();
					}
				}
			}while( d1 == -1 || d2 == -1 );
			System.out.println("Dice roll 1 = " + d1);
			System.out.println("Dice roll 2 = " + d2 + "\n");
			infoPanel.changeText("Dice roll 1 = " + d1);
			infoPanel.changeText("Dice roll 2 = " + d2 + "\n");

			//User takes their turn
			//Note: moved resetButton() to takeTurn() function
			takeTurn( 1, d1, d2 );
			
			//Reset Rolling
			controlPanel.resetRoll();
			d1 = -1;
			d2 = -1;

			System.out.println("\nPlayer 2's turn");
			infoPanel.changeText("Player 2's turn! Please roll the dice.");
			//Doubles not yet supported
			do{
				//If player doesn't need to roll again, and player has clicked the button --- retrieve dice values
				if( (controlPanel.needsToRoll() == false) && (controlPanel.hasPressedButton() == true) )
				{
					d1 = controlPanel.getDiceOne();
					d2 = controlPanel.getDiceTwo();
					
					//Handle Doubles
					if( d1 == d2)
					{
						controlPanel.resetRoll();
					}
				}
			}while( d1 == -1 || d2 == -1 );

			//Printing
			System.out.println("Dice roll 1 = " + d1);
			System.out.println("Dice roll 2 = " + d2 + "\n");
			infoPanel.changeText("Dice roll 1 = " + d1);
			infoPanel.changeText("Dice roll 2 = " + d2 + "\n");

			//User takes their turn
			//Note: Moved resetButton() to takeTurn() function
			takeTurn( 2, d1, d2 );

			//Reset Rolling for next player
			controlPanel.resetRoll();
			d1 = -1;
			d2 = -1;
		}
		
		System.out.println("Game over!");
    }

	//For now, the color doesn't matter because of how primitive checker movement is
	//Hopefully can write this function generically enough where using "p" inplace of 1 or 2 will eliminate a lot of redundant code
	private void takeTurn(int p, int d1, int d2)
	{	
		d1Used = false; d2Used = false; d3Used = false;
		int barSlot;
		
		if( p == 1 )
			barSlot = 27;
		else
			barSlot = 26;

		//Moved here to avoid checker stealing
		boardPanel.resetButton();

		//Players turn continues until they've used up each dice roll
		while( d3Used == false )
		{	
			//Listening until player selects a checker of their color on the board
			while ( boardPanel.getSlotPressed() == -1 )
			{
				//Forcing entry
				if( bkBoard[ barSlot ].getCheckerNumInSlot() != 0 )
				{
					while( ( firstPressed = boardPanel.getSlotPressed() ) != barSlot )
					{
						// GUI feedback here
					}
				}
				else
				{
					while( boardPanel.getButtonPressedColor() != p )
					{
						firstPressed = boardPanel.getSlotPressed();
					}
				}
			}
			
			System.out.println("firstPressed = " + firstPressed);
			
			//Changing the moves array (member data) based on what moves are valid
			validMoves( p, d1, d2 );
			boardPanel.highlightMoves( firstPressed, moves, true );
			boardPanel.resetButton();
			
			//Valid moves exist, waiting for a secondPressed that is valid
			//NOTE: player can select their firstPressed to cancel their move if they don't like their options
			if( validMovesExist )
			{	
				do
				{
					//Listening until a slot is selected
					//NOTE: some extra code was needed here due to rPresseds being -1 and out of bounds of moves[]
					while( ( secondPressed = boardPanel.getSlotPressed() ) == -1 );

					//Player wants to cancel their current move
					if( secondPressed == firstPressed )
						break;
					
				} while( moves[ secondPressed ] != true ); //Listening until a valid slot is selected
				
				System.out.println("secondPressed = " + secondPressed);
				
				//If player has canceled their move, start the move over
				if( secondPressed == firstPressed )
				{
					System.out.println("Move has been canceled. Try another slot.");
					boardPanel.highlightMoves( firstPressed, moves, false );
					boardPanel.resetButton();
					continue;
				}
			}
			else
			//If the player has selected a slot with no valid moves, start the move over
			{
				//If "closed out" (will add GUI feedback later)
				if( bkBoard[ barSlot ].getCheckerNumInSlot() != 0 )
				{
					noValidMoves( firstPressed );
					System.out.println("No possible entry moves. Ending turn.");
					return;
				}
				
				noValidMoves( firstPressed );
				System.out.println("No valid moves exist. Resetting.");
				boardPanel.highlightMoves( firstPressed, moves, false );
				boardPanel.resetButton();
				continue;
			}
			
			//Function keeps track of how many dice have been used
			diceUsed( p, d1, d2 );
			
			//Updates Logic/GUI 
			//NOTE: hitMoves[ secondPressed ] returns a boolean that can be used in the update function (worked out nicely this way)
			updateBoard( p, hitMoves[ secondPressed ] );
			
			boardPanel.highlightMoves( firstPressed, moves, false );
			boardPanel.resetButton();
		}
	}
	
	private void updateBoard( int p, boolean hit )
	{
		//Updating the first slot
		bkBoard[ firstPressed ].removeChecker();
		boardPanel.setSlot( firstPressed, bkBoard[ firstPressed ].getCheckerTopColor(), bkBoard[ firstPressed ].getCheckerNumInSlot() );		
		
		if ( hit )
		{
			//Updating bar
			if( p == 1 )
			{
				bkBoard[ 26 ].addChecker( 2 );
				boardPanel.setSlot( 26, 2, bkBoard[ 26 ].getCheckerNumInSlot() );
			}
			else
			{
				bkBoard[ 27 ].addChecker( 1 );
				boardPanel.setSlot( 27, 1, bkBoard[ 27 ].getCheckerNumInSlot() );
			}
			
			//Removing hit checker
			bkBoard[ secondPressed ].removeChecker();
		}
		
		//Updating the second slot
		bkBoard[ secondPressed ].addChecker( p );
		boardPanel.setSlot( secondPressed, bkBoard[ secondPressed ].getCheckerTopColor(), bkBoard[ secondPressed ].getCheckerNumInSlot() );			
	}
	
	
	private void validMoves(int p, int d1, int d2)
	{
		validMovesExist = false;
		moves = new boolean[28];
		hitMoves = new boolean[28];
		int oppColor = 0;
		int min = 1; int max = 24;
		
		//Change min and max available slot based on player's ability to bear off
		if( canBearOff(p) )
		{
			min = 0;
			max = 25;
		}
		
		//Available moves specific to player 1
		if( p == 1 )
		{
			move1 = firstPressed - d1;
			move2 = firstPressed - d2;
			move3 = firstPressed - d1 - d2;
			
			oppColor = 2;
			
			//Need to enter
			if( bkBoard[ 27 ].getCheckerNumInSlot() != 0 )
			{
				move1 = 25 - d1;
				move2 = 25 - d2;
				move3 = 25 - d1 - d2;
			}
		}
		
		//Available moves specific to player 2
		if( p == 2 )
		{
			move1 = firstPressed + d1;
			move2 = firstPressed + d2;
			move3 = firstPressed + d1 + d2;
			oppColor = 1;
			
			//Need to enter
			if( bkBoard[ 26 ].getCheckerNumInSlot() != 0 )
			{
				move1 = d1;
				move2 = d2;
				move3 = d1 + d2;
			}
		}
		
		//Using dice 1
		if( move1 >= min && move1 <= max && d1Used == false )
		{
			//Slot is empty or same color as player
			if( bkBoard[ move1 ].getCheckerTopColor() != oppColor )
			{
				validMovesExist = true;
				moves[ move1 ] = true;
			}
			//Slot is a blot, can be hit
			if( bkBoard[ move1 ].getCheckerTopColor() == oppColor && bkBoard[ move1 ].getCheckerNumInSlot() <= 1 )
			{
				validMovesExist = true;
				moves[ move1 ] = true;
				hitMoves[ move1 ] = true;
			}
		}
		//Using dice 2
		if( move2 >= min && move2 <= max && d2Used == false )
		{
			//Slot is empty or same color as player
			if( bkBoard[ move2 ].getCheckerTopColor() != oppColor )
			{
				validMovesExist = true;
				moves[ move2 ] = true;
			}
			//Slot is a blot, can be hit
			if( bkBoard[ move2 ].getCheckerTopColor() == oppColor && bkBoard[ move2 ].getCheckerNumInSlot() <= 1 )
			{
				validMovesExist = true;
				moves[ move2 ] = true;
				hitMoves[ move2 ] = true;
			}
		}	
		//Using dice 1 and 2 as one move
		if( move3 >= min && move3 <= max && (moves[move1] == true || moves[move2] == true) && d1Used == false && d2Used == false && d3Used == false )
		{
			//Slot is empty or same color as player
			if( bkBoard[ move3 ].getCheckerTopColor() != oppColor )
			{
				validMovesExist = true;
				moves[ move3 ] = true;
			}
			//Slot is a blot, can be hit
			if( bkBoard[ move3 ].getCheckerTopColor() == oppColor && bkBoard[ move3 ].getCheckerNumInSlot() <= 1 )
			{
				validMovesExist = true;
				moves[ move3 ] = true;
				hitMoves[ move3 ] = true;
			}
		}
	}
	
	private boolean canBearOff(int p)
	{
		int min, max, bar;
		
		if( p == 1 )
		{
			min = 7;
			max = 24;
			bar = 27;
		}
		else
		{
			min = 1;
			max = 18;
			bar = 26;
		}
		
		for(int i = min; i <= max; i++)
		{
			if( bkBoard[ i ].getCheckerTopColor() == p )
				return false;
		}
		
		if( bkBoard[ bar ].getCheckerNumInSlot() != 0 )
			return false;
		
		return true;
	}
	
	//Slot blinks red
	private void noValidMoves(int numSlot)
	{
		boardPanel.noValidMoves( numSlot, true );
		
		try
		{
			Thread.sleep(250);
		}
		catch( InterruptedException e )
		{
			System.out.println("Sleep interrupted!");
		}
		
		boardPanel.noValidMoves( numSlot, false );		
	}
	
    //Initial board set-up
    private void setUpBoard()
    {
        //Player 1
        //Slot #, Color #, numCheckers, 
        setUpSlotCombined(6, 1, 5);
        setUpSlotCombined(8, 1, 3);
        setUpSlotCombined(13, 1, 5);
        setUpSlotCombined(24, 1, 2);

        //Player 2
        setUpSlotCombined(1, 2, 2);
        setUpSlotCombined(12, 2, 5);
        setUpSlotCombined(17, 2, 3);
        setUpSlotCombined(19, 2, 5);
    }

    //Helper function for combining GUI/Logic in one step for building the board
    private void setUpSlotCombined(int numSlot, int color, int numCheckers)
    {   
        //Valid input values for a slot
        if( bkBoard[numSlot].setNumCheckers(color, numCheckers) )    //LOGIC
        {
            //Draw to Screen
            boardPanel.setSlot(numSlot, color, numCheckers);        //GUI
        }
        else
        {
            //Error
        }
    }
	
	private void diceUsed(int p, int d1, int d2)
	{	
		if( secondPressed == move1 )
		{	
			d1Used = true;
		}	
		if( secondPressed == move2 )
		{
			d2Used = true;
		}
		if( secondPressed == move3 )
		{
			d3Used = true;
		}
		if( d1Used == true && d2Used == true )
		{
			d3Used = true;
		}
	}
	
    //Just a function to show where each array/button slot is located ()
    private void buttonTemplateBoard()
    {
        for(int i = 0; i < bkBoard.length; i++)
        {
            boardPanel.setSlot(i, 1, i); 
        }
    }
	
	private boolean gameOver()
	{
		if( bkBoard[0].getCheckerNumInSlot() == 15 || bkBoard[25].getCheckerNumInSlot() == 15)
			return true;
		else
			return false;
	}

	//Draw game for the User to See
	private void drawGame()
	{
		//Create frame for Backgammon
		frame = new JFrame("Backgammon");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension (800,600));  //Trying to make our game function correctly even if user resized screen

		//Create Functioning Menu located at Top
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		JMenuItem load = new JMenuItem("Load");
		load.setMnemonic(KeyEvent.VK_L);
		JMenuItem save = new JMenuItem("Save");
		save.setMnemonic(KeyEvent.VK_S);

		//Add Listeners
		SaveGame saveGame = new SaveGame();
		save.addActionListener(saveGame);
		LoadGame loadGame = new LoadGame();
		load.addActionListener(loadGame);

		//Add Menu items to frame
		menu.add(load);
		menu.add(save);
		bar.add(menu);
		frame.setJMenuBar(bar);

		//TOP 
		//Display whose turn it is, display if move is valid or not
		infoPanel = new InfoPanel();
		infoPanel.setPreferredSize(new Dimension(800,100));
		frame.add(infoPanel, BorderLayout.NORTH);

		//MIDDLE
		//Game Board, let player push button to move pieces on their turn
		boardPanel = new BoardPanel();
		frame.add(boardPanel, BorderLayout.CENTER);

		//BOTTOM
		//Help Pop-up Panel, Roll Dice Sub-panel, whatever else
		controlPanel = new ControlPanel();
		controlPanel.setPreferredSize(new Dimension(800,150));
		frame.add(controlPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private class SaveGame implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			//Create file-saving panel
			JFileChooser chooseFile = new JFileChooser();
			
			//Put choosefile into the game's directory
			chooseFile.setCurrentDirectory( new File(".").getAbsoluteFile());

			int buttonChosen = chooseFile.showSaveDialog(frame);
			if(buttonChosen == chooseFile.APPROVE_OPTION)
			{
				//Get user's input for save file
				File saveFile = chooseFile.getSelectedFile();
				Formatter output = null;

				try
				{
					output = new Formatter(saveFile);

					//String to store all our game's data
					StringBuilder saveString = new StringBuilder();

					//Retrieve all checker locations (same format as setUpSlotCombined)
					for(int i = 0; i < bkBoard.length; i++)
					{
						saveString.append(i + " " + bkBoard[i].getCheckerTopColor() + " " + bkBoard[i].getCheckerNumInSlot() + "\n");
					}

					//If player needs to roll and what values are currently rolled if they dont
					saveString.append( "Current Player" + " " + controlPanel.needsToRoll() + "\n");
					saveString.append( controlPanel.getDiceOne() + " " + controlPanel.getDiceTwo() + " " + d1Used + " " + d2Used);

					//Print to file
					output.format(saveString.toString());
					infoPanel.changeText("\nGame Saved!....");
				}
				catch(FileNotFoundException exception)
				{
					infoPanel.changeText("\nFile not found, please try to save again!");
				}
				finally
				{
					if(output != null)
					{
						output.close();
					}
				}
			}
			else
			{
				//Player didnt save game
			}
		}
	}

	private class LoadGame implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			//Create file-saving panel
			JFileChooser chooseFile = new JFileChooser();
			
			//Put choosefile into the game's directory
			chooseFile.setCurrentDirectory( new File(".").getAbsoluteFile());

			int buttonChosen = chooseFile.showOpenDialog(frame);
			if(buttonChosen == chooseFile.APPROVE_OPTION)
			{
				//Get user's input for load file
				File loadFile = chooseFile.getSelectedFile();
				Scanner input = null;

				try
				{
					input = new Scanner(loadFile);
					System.out.println(bkBoard.length);
					for(int setUpBoard = 0; setUpBoard < bkBoard.length; setUpBoard++)
					{
						// System.out.println( input.nextInt() + " " + input.nextInt() + " " + input.nextInt());
						setUpSlotCombined( input.nextInt(), input.nextInt() , input.nextInt() );
					}

					infoPanel.changeText("===== Game Loaded ====\n\n");

					input.next(); //Placeholder
					input.next(); //Placeholder

					//Check of player rolled before saving
					if( input.next().equals("false"))
					{
						//Dice values
						d1 = input.nextInt();
						controlPanel.setDiceOne(d1);
						d2 = input.nextInt();
						controlPanel.setDiceTwo(d2);

						//Info for User
						infoPanel.changeText("\nIt is currently Player X's turn. You rolled before you saved!\n");

						//Rolled
						controlPanel.alreadyRolled();
					}
					else
					{
						//Put previous dice values on die, but 
						//reset d1 and d2
						d1 = -1;
						controlPanel.setDiceOne(input.nextInt());
						d2 = -1;
						controlPanel.setDiceTwo(input.nextInt());

						//Info for User
						infoPanel.changeText("\nIt is currently Player X's turn. You need to roll!\n");

						//Did not roll
						controlPanel.resetRoll();
					}

					//Dice Used
					if( input.next().equals("true") )
					{
						d1Used = true;
					}
					else
					{
						d1Used = false;
					}

					if( input.next().equals("true") )
					{
						d2Used = true;
					}
					else
					{
						d2Used = false;
					}
				}
				catch( FileNotFoundException exception)
				{
					infoPanel.changeText("\nFile not found, please try to load again!");
				}
				finally
				{
					if(input != null)
					{
						input.close();
					}

				}
			}
		}

	}


}