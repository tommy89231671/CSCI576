
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.imageio.ImageIO;

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
			File outputfile = new File("image.jpg");
			ImageIO.write(img, "jpg", outputfile);
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


	public void mode_one(float scale, int aliasing, float missing_rate){
		int new_width = (int)Math.round(1920*scale);
		int new_height = (int)Math.round(1080*scale);
		// System.out.println("new_width="+new_width+" new_height="+new_height);

		img_ans = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);

		int min = 0;
    int max = width;
		HashMap<Integer, Integer> hash_x = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> hash_y = new HashMap<Integer, Integer>();

      //Generate random int value from 50 to 100
		long pix_num= 1920*1080;
		int mp=(int)(pix_num*missing_rate);
		int [][] missing_pixel_list= new int[mp][2];
    // System.out.println("Random value in int from "+min+" to "+max+ ":");
		for (int i=0;i< mp;i++){
			int random_x = (int)Math.floor(Math.random()*(width-min+1)+min);
			int random_y = (int)Math.floor(Math.random()*(height-min+1)+min);

			missing_pixel_list[i][0]=random_x;
			missing_pixel_list[i][1]=random_y;


		}
		System.out.println("hashx_size:"+ hash_x.size());

		double dst=0;

		for (int x=0; x<width; x++){
			for (int y=0; y<height; y++){

				if ((hash_x.get(x)==hash_y.get(y))&&hash_x.get(x)!=null){
					int [] return_list = new int[7];
					return_list=antiali_find_average(x,y);
					int average_rgb=return_list[0];
					int new_r=return_list[1];
					int new_g=return_list[2];
					int new_b=return_list[3];
					int source_r=return_list[4];
					int source_g=return_list[5];
					int source_b=return_list[6];
					// average_rgb,new_r,new_g,new_b,source_r,source_g,source_b=antiali_find_average(x,y);

					dst+=Math.sqrt((new_r-source_r)*(new_r-source_r)+(new_r-source_g)*(new_r-source_g)+(new_r-source_b)*(new_r-source_b));
					System.out.println("dst: "+dst);
					img_ans.setRGB(x,y,average_rgb);

				}
				else{
					//anti aliasing by finding adjacent pixels average_rgb

					img_ans.setRGB(x,y,source_img.getRGB(x,y));
				}


			}
		}

		frame = new JFrame();
	}


	public int [] antiali_find_average(int x,int y){
		// int[] tmp_x_array=new int[3];
		// int[] tmp_y_array=new int[3];
		// System.out.println("x:"+(x)+" y:"+(y));
		int counter=0;
		int tmp_rgb=0;
		int tmp_r=0;
		int tmp_g=0;
		int tmp_b=0;
		int source_r=0;
		int source_g=0;
		int source_b=0;
		tmp_rgb=source_img.getRGB(x,y);
		source_b=tmp_rgb & 0xff;
		source_g=tmp_rgb >> 8 & 0xff;
		source_r=tmp_rgb >> 16 & 0xff;
		for (int i=-1;i<2;i++){
			for (int j=-1;j<2;j++){

				if (x+i>=0 && x+i<width){
					if (y+j>=0 && y+j<height){
						if (i!=0 || j!=0){
							tmp_rgb=source_img.getRGB(x+i,y+j);
							tmp_b+=tmp_rgb & 0xff;
							tmp_g+=tmp_rgb >> 8 & 0xff;
							tmp_r+=tmp_rgb >> 16 & 0xff;
							counter++;
						}


					}
				}
			}
		}
		int pix = 0xff000000 | ((tmp_r/counter & 0xff) << 16) | ((tmp_g/counter & 0xff) << 8) | (tmp_b/counter & 0xff);
		int [] return_list= new int[7];
		return_list[0]=pix;
		return_list[1]=tmp_r/counter;
		return_list[2]=tmp_g/counter;
		return_list[3]=tmp_b/counter;
		return_list[4]=source_r;
		return_list[5]=source_g;
		return_list[6]=source_b;



		return return_list;

	}
	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.read_source_img(args);

		//Resize by resize function
		System.out.println(args[1]);
		if (args[1].equals("1")){
			// mode=1;
			ren.mode_one(Float.parseFloat(args[2]),Integer.parseInt(args[3]),Float.parseFloat(args[4]));
			// ren.showIms();
		}
		else if(args[1].equals("2")){
			;
			// mode=2;
			// ren.mode_two(Float.parseFloat(args[2]),Integer.parseInt(args[3]));
			// ren.show_interactive_Ims(Float.parseFloat(args[2]),Integer.parseInt(args[3]));

		}

	}

}
