
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;



public class ImageReader implements MouseListener, MouseMotionListener 
{  
	
	
   public static void main(String[] args) 
   {
	   	String fileName = args[0];
   		//int width = Integer.parseInt(args[1]);
   		//int height = Integer.parseInt(args[2]);
   		double scale = 1.0;
   		if (args.length > 3) {
   			scale = Double.parseDouble(args[3]);
   		}
   		
   		int width = 960;
   		int height = 540;
   		//String fileName = "../image1.rgb";
   		
   		ImageReader ir = new ImageReader(width, height, scale, fileName);
   }
   
   public ImageReader(int width, int height, double scale, String fileName)
   {
	
	    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	    //Reading File
	    try {
		    File file = new File(fileName);
		    InputStream is = new FileInputStream(file);
	
		    long len = file.length();
		    byte[] bytes = new byte[(int)len];
		    
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	    
	    		
	    	int ind = 0;
			for(int y = 0; y < height; y++){
		
				for(int x = 0; x < width; x++){
			 
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
			
			
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    // make a graphics object
	    Graphics2D graphics_img = img.createGraphics();
	    
	    BufferedImage scaledImage;
	    // scale image
	    if (scale != 1.0) {
	    //	graphics_img.scale(scale, scale);
	    	scaledImage = scaleImage(img, width, height, scale);
	    	img = scaledImage;
	    }
	    
	    
	    // Use a label to display the image
	    JFrame frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	    JLabel label = new JLabel(new ImageIcon(img));
	    label.setPreferredSize(new Dimension(width,height));
	    frame.getContentPane().add(label, BorderLayout.CENTER);
	    label.addMouseListener(this);
	    label.addMouseMotionListener(this);

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
   }
   
   // Function calls
   public BufferedImage scaleImage(BufferedImage orig, int oWidth, int oHeight, double scale ) {
	   double newW = oWidth * scale;
	   double newH = oHeight * scale;
	   BufferedImage scaledImg = new BufferedImage((int) newW, (int) newH, BufferedImage.TYPE_INT_RGB);
	   Graphics2D gImg = scaledImg.createGraphics();
	   
	   gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	   gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	   gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   
	   gImg.drawImage(orig, 0, 0, (int) newW, (int) newH, null);
	   gImg.dispose();
	   
	   return scaledImg;
   }
   
   
	public void buttonPressed(String name)
	{
		if (name.equals("Split"))
		{
			//System.out.println("Split");
		} else if (name.equals("Initialize"))
		{
			//System.out.println("Initialize");
		} else if (name.equals("Reset"))
		{
			//System.out.println("Reset");
		} else if (name.equals("Close"))
		{
			//System.out.println("Close");
			System.exit(0);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println(arg0.getX() + " and " + arg0.getY());
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
}