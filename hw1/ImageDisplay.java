
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;



public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage source_img;
	BufferedImage mode2_source_img;
	int width = 1920;
	int height = 1080;
	BufferedImage img_ans;
	int mouse_x=0;
	int mouse_y=0;
	int mode=0;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	double screen_width = screenSize.getWidth();
	double screen_height = screenSize.getHeight();
	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img, String mode_chr)
	{
		// System.out.println("screen= "+ screen_width +" , "+screen_height);
		try
		{
			int frameLength = width*height*3;
			if (mode_chr.equals("1")){
				mode=1;
			}
			else if(mode_chr.equals("2")){
				mode=2;
			}
			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					// System.out.println("pix:"+pix);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void showIms(){
		//
		// // Read a parameter from command line
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);



		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		lbIm1 = new JLabel(new ImageIcon(img_ans));
		frame.getContentPane().removeAll();
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public void read_source_img(String[] args){
		// Read in the specified image
		source_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		readImageRGB(width, height, args[0], source_img,args[1]);

	}


	public void mode_one(float scale, int aliasing){
		int new_width = (int)Math.round(1920*scale);
		int new_height = (int)Math.round(1080*scale);
		// System.out.println("new_width="+new_width+" new_height="+new_height);

		img_ans = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);

		for (int x=0; x<new_width; x++){
			for (int y=0; y<new_height; y++){

				int mapping_x=(int)Math.round(x/scale);
				int mapping_y=(int)Math.round(y/scale);
				if (mapping_x==width){
					mapping_x--;
				}
				if (mapping_y==height){
					mapping_y--;
				}
				if (aliasing==0){
					img_ans.setRGB(x,y,source_img.getRGB(mapping_x,mapping_y));
				}
				else{
					//anti aliasing by finding adjacent pixels average_rgb

					int average_rgb=0;
					average_rgb=antiali_find_average(mapping_x,mapping_y);
					img_ans.setRGB(x,y,average_rgb);
				}


			}
		}
		frame = new JFrame();
	}

	public void mode_two(float scale, int aliasing){
		frame= new JFrame();
		img_ans=source_img;
		showIms();

		frame.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
							img_ans = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
							mouse_x=(int)(e.getX()/screen_width*(double)width);
							mouse_y=(int)(e.getY()/screen_height*(double)height);

							// mouse_x=(int)(e.getX());
							// mouse_y=(int)(e.getY());
							// img_ans=source_img;

							BufferedImage circleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

							Graphics drawOnCircleImg = circleImage.getGraphics();
							Color circleColor = new Color(0, 255, 0);
							drawOnCircleImg.setColor(circleColor);
							// System.out.println("draw on: "+ mouse_x + " , "+mouse_y);
							drawOnCircleImg.fillOval(mouse_x-50,mouse_y-50, 100, 100);

							// img_ans=circleImage;

							for(int y = 0; y < source_img.getHeight(); y++){

					        for(int x = 0; x < source_img.getWidth(); x++){

					            if(circleImage.getRGB(x, y) == circleColor.getRGB()){
												int tmp_x=mouse_x+Math.round((x-mouse_x)/scale);
												int tmp_y=mouse_y+Math.round((y-mouse_y)/scale);

												if (tmp_x<0){
													tmp_x=0;
												}
												else if (tmp_x>width){
													tmp_x=width;
												}
												if (tmp_y<0){
													tmp_y=0;
												}
												else if (tmp_y>height){
													tmp_x=height;
												}

												if (aliasing==0){

													img_ans.setRGB(x, y,source_img.getRGB(tmp_x,tmp_y));
												}
												else{
													img_ans.setRGB(x, y,antiali_find_average(x,y));
												}
					            }
											else{
													int tmp_rgb=source_img.getRGB(x,y);

													int blue=tmp_rgb & 0xff;
													int green=tmp_rgb >> 8 & 0xff;
													int red=tmp_rgb >> 16 & 0xff;
													Color c=new Color(red, green, blue);
                    			c=c.darker();
													c=c.darker();
													red=c.getRed();
			                    green=c.getGreen();
			                    blue=c.getBlue();
													int pix = 0xff000000 | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);

													img_ans.setRGB(x, y,pix);
											}
					        }
					    }


							// System.out.println(mouse_x+","+mouse_y);//these co-ords are relative to the component


							showIms();
				}
		});

	}

	public int antiali_find_average(int x,int y){
		// int[] tmp_x_array=new int[3];
		// int[] tmp_y_array=new int[3];
		// System.out.println("x:"+(x)+" y:"+(y));
		int counter=0;
		int tmp_rgb=0;
		int tmp_r=0;
		int tmp_g=0;
		int tmp_b=0;

		for (int i=-3;i<4;i++){
			for (int j=-3;j<4;j++){

				if (x+i>=0 && x+i<width){
					if (y+j>=0 && y+j<height){
						tmp_rgb=source_img.getRGB(x+i,y+j);
						tmp_b+=tmp_rgb & 0xff;
						tmp_g+=tmp_rgb >> 8 & 0xff;
						tmp_r+=tmp_rgb >> 16 & 0xff;

						counter++;
					}
				}
			}
		}
		int pix = 0xff000000 | ((tmp_r/counter & 0xff) << 16) | ((tmp_g/counter & 0xff) << 8) | (tmp_b/counter & 0xff);


		return pix;

	}
	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.read_source_img(args);

		//Resize by resize function
		System.out.println(args[1]);
		if (args[1].equals("1")){
			// mode=1;
			ren.mode_one(Float.parseFloat(args[2]),Integer.parseInt(args[3]));
			ren.showIms();
		}
		else if(args[1].equals("2")){
			// mode=2;
			ren.mode_two(Float.parseFloat(args[2]),Integer.parseInt(args[3]));
			// ren.show_interactive_Ims(Float.parseFloat(args[2]),Integer.parseInt(args[3]));

		}

	}

}
