
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.swing.*;


public class ImageReader implements MouseListener, MouseMotionListener 
{  

   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		wTiles = Integer.parseInt(args[1]); // number of columns
   		hTiles = Integer.parseInt(args[2]); // number of rows
   		scale = 1.0;
   		if (args.length > 3) {
   			scale = Double.parseDouble(args[3]);
   		}
   		
   		int width = 960; // get this code wise later?
   		int height = 540;
   		//String fileName = "../image1.rgb";
   		
   		ImageReader ir = new ImageReader(width, height, scale, fileName);
   		//c.show(original, ORIGINAL);
	    if (vidFlag) {
	    	ir.fps.start();
	    }
   }

   // stores section of image where tile comes from
   class Tile {
	   int width; // width of tile
	   int height; // height of tile
	   int leftCornerX; // x index of left Corner of main image section
	   int leftCornerY; // y index of left Corner of main image section
	   int rightCornerX; // x index of right corner of main image section
	   int rightCornerY; // y index of right corner of main image section
   };
   
   public static JPanel cards; // use card layout 
   // names of "cards"
   final static String ORIGINAL = "ORIGINAL";
   final static String TILED = "TILED";
   final static String PUZZLE = "PUZZLE";
   
   
   //public static BufferedImage img; // image being loaded in
   public static BufferedImage frames[]; // frames 
   public static BufferedImage blankTile; // blank image
   //public static BufferedImage tiles[]; // image divided into tiles
   public static Tile tiles[]; // tiles of images
   public static Tile puzzledTiles[]; // tiles in order of puzzle
   public static CardLayout c;
   public static JFrame frame; // frame for UI
   public static JPanel original;
   public static JPanel tiled;
   public static JPanel puzzle;
   public static int wTiles; // number of columns
   public static int hTiles; // number of rows
   public static double scale;
   public static int blankI; // index of blank tile
   public static boolean vidFlag; 
   public static int frameCount;
   public static int currFrame = 0;
   public static byte[] bytes;
   public static int[] byteIndicies; // keeps indexes where new frames start;
   
   Timer fps;
   
   public ImageReader(int width, int height, double scale, String fileName)
   {
	
	    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	    //Reading File
	    try {
		    File file = new File(fileName);
		    InputStream is = new FileInputStream(file);
	
		    long len = file.length();
		    bytes = new byte[(int) len];
		    
		    
		    System.out.println("file length:"+ len);
		    System.out.println((int) len);
		    
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        	offset += numRead;
	        }
	        System.out.println("bytes[1555200]:" + bytes[1555200]);
	        
	    	// if file longer then height*width*3 or whatever the length is then it is a video!	
	    	// len = 1555200 for single picture
	        frameCount = 1;
	        vidFlag = false;
	        // this is a video!
	        if (len > 1555200) {
	        	//frameCount = 100;
	        	vidFlag = true;
	        	fps = new Timer(100, new refreshFrame());
	        	fps.setInitialDelay(100);
	        }
	        
	        if (vidFlag) {
	        	byteIndicies = new int[100];
	        	for (int b = 0; b < 100; ++b) {
	        		byteIndicies[b] = b * 1555200;
	        	}
	        }
	        
	        
	        frames = new BufferedImage[frameCount];
	        
	        int ind = 0;
	        // do this for each frame! maybe not takes to fucking long
	        for (int f = 0; f < frameCount; ++f) {
	        	//ind = 0;
	        	for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						//System.out.println("i:" + ind);
						//img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
						
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
				frames[f] = img;
				// get next frame from input stream
				if (vidFlag) {
			//		ind = ind + height*width*2 - 1;
//					bytes = new byte[1555200];
//					offset = 0;
//			        numRead = 0;
//			        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
//			        	offset += numRead;
//			        }
				}
	        }
			
			
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    
	    // scale images 
	    for (int f = 0; f < frameCount; ++f) {
		    if (scale != 1.0) {
		    	BufferedImage scaledImage = scaleImage(frames[f], width, height, scale);
		    	frames[f] = scaledImage;
		    }
	    }
	    double scaledW = width * scale;
	    double scaledH = height * scale;
	    width = (int) scaledW;
	    height = (int) scaledH;
	    
	    // Debuggin'
	    System.out.println("image dimensions");
	    System.out.println(width);
	    System.out.println(height);
	    System.out.println("number of tiles");
	    System.out.println(wTiles);
	    System.out.println(hTiles);
	    
	   //splitImage();
	    
	    // Use a label to display the image
	    frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	

	    makePanes(frame.getContentPane());

	    // Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(width, 50));
	    frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		MyButton splitButton = new MyButton("Split");
		buttonPanel.add(splitButton, BorderLayout.WEST);

		MyButton initButton = new MyButton("Initialize");
		buttonPanel.add(initButton, BorderLayout.WEST);
		
		MyButton resetButton = new MyButton("Reset");
		buttonPanel.add(resetButton, BorderLayout.WEST);
		
		MyButton closeButton = new MyButton("Close");
		buttonPanel.add(closeButton, BorderLayout.WEST);	
		
	    frame.pack();
	    frame.setVisible(true); 
	    
//	    c.show(original, ORIGINAL);
//	    
//	    if (vidFlag) {
//	    	fps.start();
//	    }
   }
   
   public void makePanes(Container pane) {
	   
	   cards = new JPanel(new CardLayout());
	   
	   // make original image panel
	   original = new JPanel();
	   JLabel label = new JLabel(new ImageIcon(frames[0]));
	   label.setPreferredSize(new Dimension(frames[0].getWidth(), frames[0].getHeight()));
	   original.add(label, BorderLayout.CENTER);
	   cards.add(original, ORIGINAL);
	   	   
	   pane.add(cards, BorderLayout.CENTER); // add to main pane
	   

   }
   
   
   // Function calls
   // split image into tiles
   public BufferedImage scaleImage(BufferedImage img, int oWidth, int oHeight, double scale ) {
	   double newW = oWidth * scale;
	   double newH = oHeight * scale;
	   BufferedImage scaledImg = new BufferedImage((int) newW, (int) newH, BufferedImage.TYPE_INT_RGB);
	   Graphics2D gImg = scaledImg.createGraphics();
	   
	   gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	   gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	   gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   
	   gImg.drawImage(img, 0, 0, (int) newW, (int) newH, null);
	   gImg.dispose();
	   
	   return scaledImg;
   }
   
   // split image into tiles
   public void splitImage() {
	   int w = frames[0].getWidth();
	   int h = frames[0].getHeight();
	   
	   //BufferedImage mtiles[] = new BufferedImage[wTiles * hTiles];
	   tiles = new Tile[wTiles * hTiles];
	   
	   // dimensions of each tile
	   int tileW = w / wTiles;
	   int tileH = h / hTiles;

	   // Debuggin'
	   System.out.println("tile dimensions");
	   System.out.println(tileW);
	   System.out.println(tileH);
	   
	  int i = 0; // index for tiles array
	   for (int y = 0; y < hTiles; ++y) {
		   for (int x = 0; x < wTiles; ++x) {
			   tiles[i] = new Tile();
			   tiles[i].width = tileW;
			   tiles[i].height = tileH;
			   tiles[i].leftCornerX = x * tileW;
			   tiles[i].leftCornerY = y * tileH;
			   tiles[i].rightCornerX = x * tileW + tileW;
			   tiles[i].rightCornerY = y * tileH + tileH;
			   
			   ++i; // increment i
		   }
	   }
   
	   // split image panel
	   tiled = new JPanel();
	   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
	   tiled.setLayout(tiledLayout);
	   for (i = 0; i < tiles.length; ++i) {
		   BufferedImage tile = new BufferedImage(tiles[i].width, tiles[i].height, BufferedImage.TYPE_INT_RGB);
		   
		   Graphics2D gTile = tile.createGraphics();
		   gTile.drawImage(frames[0], 0, 0, tiles[i].width, tiles[i].height, 
				   tiles[i].leftCornerX, tiles[i].leftCornerY,
				   tiles[i].rightCornerX, tiles[i].rightCornerY, null);
		   gTile.dispose();		   
		   JLabel label = new JLabel(new ImageIcon(tile));
		   label.setPreferredSize(new Dimension(tile.getWidth(), tile.getHeight()));
		   tiled.add(label);   
	   }
	   cards.add(tiled, TILED);
	  // return mTiles;
   }
    
   // jumble image
   public void initPuzzle() {
	   
	   if (tiles == null) {
		   return;
	   }
	   
	   puzzledTiles = (Tile[]) tiles.clone();
	   
	   // shuffle!
	   Collections.shuffle(Arrays.asList(puzzledTiles));
	   
	   // Make blank white tile
	   blankTile = new BufferedImage(tiles[0].width, 
			   tiles[0].height, BufferedImage.TYPE_INT_RGB);
	   for (int y = 0; y < blankTile.getHeight(); ++y) {
		   for (int x = 0; x < blankTile.getWidth(); ++x) {
			   blankTile.setRGB(x, y, 0x00FFFFFF);
		   }
	   }
	    
	   // get a random index 
	   Random rand = new Random();
	   blankI = rand.nextInt(tiles.length);
	   //tiles[blankI] = blankTile;	   
	   
	   // make pane!
	   puzzle = new JPanel();
	   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
	   puzzle.setLayout(tiledLayout);
	   for (int i = 0; i < tiles.length; ++i) {
		   JLabel label;
		   // draw blank tile
		   if (i == blankI) {
			   label = new JLabel(new ImageIcon(blankTile));
			   label.setPreferredSize(new Dimension(blankTile.getWidth(), blankTile.getHeight()));
		   } else {
			   BufferedImage piece = new BufferedImage(puzzledTiles[i].width, puzzledTiles[i].height, BufferedImage.TYPE_INT_RGB);
			   
			   Graphics2D gTile = piece.createGraphics();
			   gTile.drawImage(frames[0], 0, 0, puzzledTiles[i].width, puzzledTiles[i].height, 
					   puzzledTiles[i].leftCornerX, puzzledTiles[i].leftCornerY,
					   puzzledTiles[i].rightCornerX, puzzledTiles[i].rightCornerY, null);
			   gTile.dispose();
			   
			   label = new JLabel(new ImageIcon(piece));
			   label.setPreferredSize(new Dimension(piece.getWidth(), piece.getHeight()));
		   }
		   puzzle.add(label);
//		   label.addMouseListener(this);
//		   label.addMouseMotionListener(this);   
	   }
	   puzzle.addMouseListener(this);
	   puzzle.addMouseMotionListener(this);
	   cards.add(puzzle, PUZZLE);
   }
   
   // Move tiles
   public void moveTiles(int mX, int mY) {
	   /// get tile that was clicked
	   int x = mX / puzzledTiles[0].width;
	   int y = mY / puzzledTiles[0].height;
	   System.out.println("x:" + x);
	   System.out.println("y:" + y);
	   int mIndex = (y * wTiles) + x;
	   
	   System.out.println("blank" + blankI);
	   System.out.println("tile" + mIndex);
	   
	   // check if tile is movable (next to the blank tile)
	   // get possible indexes
	   int left = -1, right = -1, above = -1, below = -1;
	   if (blankI % 4 != 0) { // there can be a tile on the left
		   left = blankI - 1;
	   }
	   if ((blankI + 1) % 4 != 0) { // there can be a tile on the right!
		   right = blankI + 1;
	   }
	   if ((blankI - wTiles) >= 0) { // there can be a tile above!
		   above = blankI - wTiles;
	   }
	   if ((blankI + wTiles) < puzzledTiles.length) { // there can be a tile below!
		   below = blankI + wTiles;
	   }
	   
	   // is this tile that was clicked on a valid tile to move?
	   if (mIndex == left || mIndex == right || mIndex == above || mIndex == below) { 
		   // move the tile
		   swapTiles(mIndex);
		   System.out.println("new blank:" + blankI);
		   
		   // update puzzle
		   puzzle.removeAll();
		   // re-add the images 
		   GridLayout tiledLayout = new GridLayout(hTiles, wTiles, 5, 5);
		   puzzle.setLayout(tiledLayout);
		   for (int i = 0; i < puzzledTiles.length; ++i) {
			   JLabel label;
			   // draw blank tile if index is blank tile index
			   if (i == blankI) {
				   label = new JLabel(new ImageIcon(blankTile));
				   label.setPreferredSize(new Dimension(blankTile.getWidth(), blankTile.getHeight()));
			   } else {
				   BufferedImage piece = new BufferedImage(puzzledTiles[i].width, puzzledTiles[i].height, BufferedImage.TYPE_INT_RGB);
				   
				   Graphics2D gTile = piece.createGraphics();
				   gTile.drawImage(frames[0], 0, 0, puzzledTiles[i].width, puzzledTiles[i].height, 
						   puzzledTiles[i].leftCornerX, puzzledTiles[i].leftCornerY,
						   puzzledTiles[i].rightCornerX, puzzledTiles[i].rightCornerY, null);
				   gTile.dispose();
				   
				   label = new JLabel(new ImageIcon(piece));
				   label.setPreferredSize(new Dimension(piece.getWidth(), piece.getHeight()));
			   }
			   puzzle.add(label); 
		   }
		   puzzle.revalidate();
		   puzzle.repaint();
		   //puzzle.updateUI();
		   puzzle.addMouseListener(this);
		   puzzle.addMouseMotionListener(this);   
	   } 
	   
   }
   
   // swap tiles
   public void swapTiles(int index) {
	   Tile temp = puzzledTiles[blankI];
	   puzzledTiles[blankI] = puzzledTiles[index];
	   puzzledTiles[index] = temp;
	   blankI = index;
   }
   
	public void buttonPressed(String name)
	{
		c = (CardLayout) (cards.getLayout());
		if (name.equals("Split"))
		{
			System.out.println("Split");
			splitImage();
			c.show(cards, TILED);
		} else if (name.equals("Initialize"))
		{
			System.out.println("Initialize");
			initPuzzle();
			c.show(cards,  PUZZLE);
		} else if (name.equals("Reset"))
		{
			System.out.println("Reset");
			c.show(cards, ORIGINAL);
//			if (vidFlag) {
//				videoOriginal();
//			}
		} else if (name.equals("Close"))
		{
			//System.out.println("Close");
			System.exit(0);
		}
	}
	
	// refresh original pane
	public void videoOriginal(int currFrame) {
		// get new picture
    	int ind = byteIndicies[currFrame];
    	System.out.println(currFrame + " is at " + ind);
		BufferedImage img = new BufferedImage(960, 540, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < 540; y++){
			for(int x = 0; x < 960; x++){
				//System.out.println("i:" + ind);
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+540*960];
				byte b = bytes[ind+540*960*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
    	}
		frames[0] = scaleImage(img, 960, 540, scale);

			// update image
			original.removeAll();
		   JLabel label = new JLabel(new ImageIcon(frames[0]));
		   label.setPreferredSize(new Dimension(frames[0].getWidth(), frames[0].getHeight()));
		   original.add(label, BorderLayout.CENTER);
		   cards.add(original, ORIGINAL);
		   original.revalidate();
		   original.repaint();

	}

	public void videoTiled() {
		
	}
	
	public void videoPuzzle() {
		
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println(arg0.getX() + " and " + arg0.getY());
		moveTiles(arg0.getX(), arg0.getY());
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
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println("Moving");
	} 
	
	class MyButton extends JButton {
		MyButton(String label){
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) 
	  				{
						buttonPressed(getText());
					}
				}
			);
		}
		
		MyButton(String label, ImageIcon icon){
			Image img = icon.getImage();
			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaleimg));
			setText(label);
			setFont(new Font("Helvetica", Font.PLAIN, 0));
			addMouseListener(
				new MouseAdapter() {
	  				public void mousePressed(MouseEvent e) {
						buttonPressed(getText());
					}
				}
			);
		}
	}
	
	class refreshFrame implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			++currFrame;
			if (currFrame == 100) {
				currFrame = 0;
			}
			videoOriginal(currFrame);
//			original.removeAll();
//		   JLabel label = new JLabel(new ImageIcon(frames[currFrame]));
//		   label.setPreferredSize(new Dimension(frames[currFrame].getWidth(), frames[currFrame].getHeight()));
//		   original.add(label, BorderLayout.CENTER);
//		   cards.add(original, ORIGINAL);
//		   original.revalidate();
//		   original.repaint();
//	//	   c.show(cards, ORIGINAL);
		   System.out.println("Frame:" + currFrame);
		}
	}
}