package simplify.align;

import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.*; 
import java.util.ArrayList;

import javax.swing.*;

import java.io.*;

import simplify.AlignPair;
import simplify.Alignment;
import simplify.BadAlignmentException;
import simplify.DataAnalyzer;
import simplify.ExamplePairReader;
import simplify.ExamplePair;
import simplify.ParseTree;
import simplify.ParseTreeEntry;
import simplify.Word;

import java.util.Random;

/**
 * This class is visualized word alignments and allows you to modify them.
 * 
 * @author Dave
 * @version 6/1/2011
 *
 */
public class WordAlignmentVisualizer extends JFrame implements ActionListener, MouseListener, WindowListener {
	// Height and width of the window as well as the amount of width for each of 4 objects
	private static final int WINDOW_HEIGHT = 150;
	private static final int WINDOW_WIDTH = 1400; 

	// The initial starting positions for the objects
	private static final int START_X = 5;
	private static final int TOP_Y = 40;
	private static final int BOTTOM_Y = 100;

	// Color for drawing 
	private static final Color DEFAULT_COLOR = Color.BLACK;
	private static final Color INCORRECT_COLOR = Color.RED;
	private static final Color CORRECT_COLOR = Color.GREEN;
	private static final Color UNALIGNED_COLOR = Color.DARK_GRAY;

	// the current data point being processed
	private ExamplePair currentPair;
	
	// the current words/alignment being processed
	private ArrayList<Word> normalWords;
	private ArrayList<Word> simpleWords;
	private Alignment alignment;
	
	// keeps track of the visual objects on the screen
	private VisualText[] topWords;
	private VisualText[] bottomWords;
	
	// next button
	private JButton nextButton;
	private JButton skipButton;
	private JButton badButton;

	// random number generator for random skips
	private Random randGenerator;
	private int randomSkip;
	
	// whether or not to display all alignments or only those we think may have
	// mistakes
	private boolean mistakesOnly = false;
	
	private boolean specialOnly = true;

	// the reader for reading the ExamplePairs
	private ExamplePairReader reader;
	
	// the alignment output if we're modifying and saving the alignments
	private PrintWriter simpleOut;
	private PrintWriter normalOut;
	private PrintWriter alignOut;
	private PrintWriter originalAlignOut;
	
	// used to keep track of the words clicked when trying to add a new alignment
	private int topClickIndex = -1;
	private int bottomClickIndex = -1;
	
	public WordAlignmentVisualizer(String dataFile, String alignFile){
		this(dataFile, alignFile, null, 0);
	}
	
	public WordAlignmentVisualizer(String dataFile, String alignFile, String outputLabel){
		this(dataFile, alignFile, outputLabel, 0);
	}
	
	/**
	 * Create a new word alignment window
	 * 
	 * @param dataFile a file containing pairs of parse trees (normal, then simple), one per line
	 * @param normalFile the normal text
	 * @param simpleFile the simple text
	 * @param alignFile the alignment file
	 * @param randomSkip pick a random number between 0 and randomSkip for examples to skip
	 */
	public WordAlignmentVisualizer(String dataFile, String alignFile, int randomSkip){
		this(dataFile, alignFile, null, randomSkip);
	}
	
	/**
	 * Create a new word alignment window.  Also outputs the alignments modified
	 *  to alignFile on exit.
	 * 
	 * @param dataFile a file containing pairs of parse trees (normal, then simple), one per line
	 * @param normalFile the normal text
	 * @param simpleFile the simple text
	 * @param alignFile the alignment file
	 * @param randomSkip pick a random number between 0 and randomSkip for examples to skip
	 */
	public WordAlignmentVisualizer(String dataFile, String alignFile, String outputLabel, int randomSkip){
		super("Word alignment");

		this.randomSkip = randomSkip;
		
		if( randomSkip > 0 ){
			randGenerator = new Random();
		}
		
		if( outputLabel != null ){
			try {
				alignOut = new PrintWriter(new FileOutputStream(outputLabel + ".newalign"));
				originalAlignOut = new PrintWriter(new FileOutputStream(outputLabel + ".oldalign"));
				simpleOut = new PrintWriter(new FileOutputStream(outputLabel + ".simple"));
				normalOut = new PrintWriter(new FileOutputStream(outputLabel + ".normal"));
			} catch (FileNotFoundException e) {
				System.out.println("Couldn't created output file\n" + e);
			}
		}
		
		// create the example reader and read load the first example
		reader = new ExamplePairReader(dataFile, alignFile);
		loadNext();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		// the next button will be used to click through to the next example
		nextButton = new JButton("next");
		nextButton.addActionListener(this);

		skipButton = new JButton("skip");
		skipButton.addActionListener(this);
		
		badButton = new JButton("badSentAlign");
		badButton.addActionListener(this);
		
		JPanel panel = new JPanel();
		panel.add(badButton);
		panel.add(skipButton);
		panel.add(nextButton);

		Container cont = getContentPane();
		cont.add(panel, BorderLayout.SOUTH);
		validate();

		this.setVisible(true);

		addMouseListener(this);
		addWindowListener(this);
		
		repaint();
	}
	
	public void paint(Graphics g){
		g.clearRect(0, 0, this.getWidth(), this.getHeight());

		// positions for first drawing 
		int x = START_X; 
		int y = TOP_Y; 

		Graphics2D g2 = (Graphics2D) g; 
		FontMetrics metrics = g2.getFontMetrics();
		
		// the amount of space in between words
		int wordSpace = metrics.stringWidth("  ");		
		
		topWords = new VisualText[normalWords.size()];

		// draw the top (normal) words
		for( int i = 0; i < normalWords.size(); i++ ){
			Word w = normalWords.get(i);
			String text = w.getLabel();
			
			ArrayList<ParseTreeEntry> aligned = w.getAlignment();

			// depending on whether our guess is it's a mistake, change the font color
			if( isMistake(w) ){
				g2.setColor(INCORRECT_COLOR);
			}else if( aligned.size() == 0 ){
				g2.setColor(UNALIGNED_COLOR);
			}else if( aligned.size() == 1 &&
					  aligned.get(0).getLabel().equalsIgnoreCase(w.getLabel()) ){
				g2.setColor(CORRECT_COLOR); 
			}else{
				g2.setColor(DEFAULT_COLOR);
			}
	
			topWords[i] = new VisualText(text, x, y, g2);
			
			x = topWords[i].getEndX() + wordSpace;
		}
		
		g2.setColor(DEFAULT_COLOR);

		x = START_X;
		y = BOTTOM_Y;

		bottomWords = new VisualText[simpleWords.size()];

		// draw the bottom (simple) words
		for( int i = 0; i < simpleWords.size(); i++ ){
			String text = simpleWords.get(i).getLabel();

			bottomWords[i] = new VisualText(text, x, y, g2);
			
			x = bottomWords[i].getEndX() + wordSpace;
		}

		// draw lines corresponding to the current alignment
		for( AlignPair p: alignment ){
			VisualText top = topWords[p.getNormalIndex()];
			VisualText bottom = bottomWords[p.getSimpleIndex()];
			
			Line2D.Double line = new Line2D.Double(top.getMidX(),TOP_Y+2, 
					bottom.getMidX(), BOTTOM_Y-bottom.getHeight()+5);

			g2.draw(line);
		}

		nextButton.repaint();
		skipButton.repaint();
		badButton.repaint();
	}

	/**
	 * Add an alignment between normal word at normalIndex and simple word at simpleIndex
	 * 
	 * @param normalIndex
	 * @param simpleIndex
	 */
	private void addAlignment(int normalIndex, int simpleIndex){
		try {
			AlignPair align = new AlignPair(normalIndex, simpleIndex);
			
			if( !alignment.contains(align) ){

				// put it in the appropriate place in the alignment ArrayList
				alignment.add(align);
			}
		} catch (BadAlignmentException e) {
			throw new RuntimeException(e);
		}
		
		repaint();
	}
	
	/**
	 * Remove the alignment from the normal word at normalIndex and simple word at simpleIndex
	 * 
	 * @param normalIndex
	 * @param simpleIndex
	 */
	private void removeAlignment(int normalIndex, int simpleIndex){
		// update the stored alignment			  
		try {
			alignment.remove(new AlignPair(normalIndex, simpleIndex));
		} catch (BadAlignmentException e) {
			throw new RuntimeException(e);
		}

		repaint();
	}

	/**
	 * Load in the next example in the data stream.
	 */
	private void loadNext(){
		if( mistakesOnly ){
			loadMistake();
		}else if( specialOnly ){
			loadSpecial();
		}else{
			int skipNum = 1;
			
			if( randomSkip > 0 ){
				skipNum = randGenerator.nextInt(randomSkip) + 1;
			}
			
			do{
				currentPair = reader.next();
				skipNum--;
			}while(skipNum > 0 && reader.hasNext() );
			
			normalWords = currentPair.getNormal().getWords();
			simpleWords = currentPair.getSimple().getWords();
			alignment = new Alignment(currentPair.getAlignment());
		}
	}

	/**
	 * Keep going through the examples until we find one that we think is a mistake
	 */
	private void loadMistake(){
		boolean mistake = false;

		while( !mistake && reader.hasNext() ){
			ExamplePair pair = reader.next();

			ParseTree normalTree = pair.getNormal();
			ParseTree simpleTree = pair.getSimple();

			simpleWords = simpleTree.getWords();
			normalWords = normalTree.getWords();
			alignment = pair.getAlignment();

			for( Word w: normalTree.getWords() ){
				if( isMistake(w) ){
					mistake = true;
				}
			}
		}
	}

	private void loadSpecial(){
		boolean special = false;

		while( !special && reader.hasNext() ){
			ExamplePair pair = reader.next();

			ParseTree normalTree = pair.getNormal();
			ParseTree simpleTree = pair.getSimple();

			simpleWords = simpleTree.getWords();
			normalWords = normalTree.getWords();
			alignment = pair.getAlignment();

			for( Word w: simpleTree.getWords() ){
				// looking for a simple word that is aligned to itself, along with other words
				
				ArrayList<ParseTreeEntry> aligned = w.getAlignment();
				
				if( aligned.size() > 1 ){
					// see if it was aligned to itself
					for( ParseTreeEntry entry: aligned ){
						if( entry.getLabel().equalsIgnoreCase(w.getLabel()) ){
							special = true;
						}
					}
				}
			}
		}
	}		
	
	/**
	 * Determines whether the Word w is an alignment mistake
	 * 
	 * @param w The word to check for mistake
	 * @return whether the Word w is misaligned (a guess...)
	 */
	private boolean isMistake(Word w){
		ArrayList<ParseTreeEntry> aligned = w.getAlignment();

		if( aligned.size() == 0 ){
			// see if it should have been aligned
			if( DataAnalyzer.wordCount(simpleWords, w) > 0 &&
					DataAnalyzer.wordCount(normalWords, w) <= DataAnalyzer.wordCount(simpleWords, w) ){
				return true;
			}else if( aligned.size() == 1 ){
				// see if it's aligned to itself
				if( aligned.get(0).getLabel().equalsIgnoreCase(w.getLabel())){

				}else if( DataAnalyzer.wordCount(simpleWords, w) > 0 &&
						DataAnalyzer.wordCount(normalWords, w) <= DataAnalyzer.wordCount(simpleWords, w) ){
					return true;
				}
			}else{
				// this word is aligned to more than one word
				boolean foundWord = false;

				for( ParseTreeEntry e: aligned ){
					if( e.getLabel().equalsIgnoreCase(w.getLabel()) ){
						foundWord = true;
					}
				}

				if( foundWord ){

				}else if( DataAnalyzer.wordCount(simpleWords, w) > 0 &&
						DataAnalyzer.wordCount(normalWords, w) <= DataAnalyzer.wordCount(simpleWords, w) ){
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * The only button that registered with actionPerformed is the next button.
	 * If we're saving the alignment, write it out and then load the next example.
	 */
	public void actionPerformed(ActionEvent e) {		
		if( alignOut != null ){
			if( e.getSource() != skipButton ){
				// print out the simple parse tree
				simpleOut.println(currentPair.getSimple());

				// print out the normal parse tree
				normalOut.println(currentPair.getNormal());

				// print out the old alignment
				originalAlignOut.println(currentPair.getAlignment());

				if( e.getSource() == nextButton ){	
					// print out the new alignment
					alignOut.println(alignment);
				}else{
					// we skipped this one
					alignOut.println(Alignment.BAD_SENTENCE_ALIGNMENT);
				}
				
				simpleOut.flush();
				normalOut.flush();
				originalAlignOut.flush();
				alignOut.flush();
			}
		}
		
		loadNext();
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		boolean delete = false;
		
		// we'll assume normal clicking is for adding
		if( (e.getButton() == MouseEvent.BUTTON1 &&
			e.isControlDown()) ||
			e.getButton() == MouseEvent.BUTTON3 ){
			
			delete = true;
		}
		
		boolean wordClicked = false;

		// see if we clicked on any words
		for( int i = 0; i < topWords.length; i++ ){
			VisualText v = topWords[i];

			if( v.contains(e.getPoint()) ){
				wordClicked = true;

				if( bottomClickIndex >= 0 ){
					// this is now the second word clicked
					if( delete ){
						removeAlignment(i, bottomClickIndex);
					}else{
						addAlignment(i, bottomClickIndex);
					}
					
					topClickIndex = -1;
					bottomClickIndex = -1;
				}else{
					topClickIndex = i;
				}
			}
		}

		if( !wordClicked ){
			for( int i = 0; i < bottomWords.length; i++ ){
				VisualText v = bottomWords[i];

				if( v.contains(e.getPoint()) ){		
					wordClicked = true;

					if( topClickIndex >= 0 ){
						// this is the second word clicked
						if( delete ){
							removeAlignment(topClickIndex, i);
						}else{
							addAlignment(topClickIndex, i);
						}
						
						topClickIndex = -1;
						bottomClickIndex = -1;
					}else{
						bottomClickIndex = i;
					}
				}
			}
		}

		if( !wordClicked ){
			topClickIndex = -1;
			bottomClickIndex = -1;
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// try and close the file
		try{
			simpleOut.close();
			normalOut.close();
			originalAlignOut.close();
			alignOut.close();
		}catch(Exception e){
			
		}
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}