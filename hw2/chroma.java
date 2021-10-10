
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;



public class chroma {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage front_img;
	BufferedImage background_img;
	int width = 960;
	int height = 540;
	BufferedImage img_ans;
	int chroma_key_color=0;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	double screen_width = screenSize.getWidth();
	double screen_height = screenSize.getHeight();
	int [] histogram = new int[6]; //red,yellow,green,light blue, blue, purple
	float [] histogram_average = new float[6]; //red,yellow,green,light blue, blue, purple
	int average_chroma_key_hue=0;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		// System.out.println("screen= "+ screen_width +" , "+screen_height);
		try
		{
			int frameLength = width*height*3;

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
		frame = new JFrame();
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

		lbIm1 = new JLabel(new ImageIcon(background_img));
		frame.getContentPane().removeAll();
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public void read_front_img(String[] args){
		// Read in the specified image
		front_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], front_img);

	}
	public void read_background_img(String[] args){
		// Read in the specified image
		background_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[1], background_img);

	}
	public void color_histogram(float hue){
		int tmp=(int)Math.round(hue/60);
		if (tmp==6){
			histogram[0]++;
			histogram_average[0]=histogram_average[0]+hue;
		}
		else{
			histogram[tmp]++;
			histogram_average[tmp]=histogram_average[tmp]+hue;
		}
	}
	public void front_img_HSV(){
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				int tmp_rgb=0;
				int []rgb_list=new int[3];
				float [] hsv=new float[3];
				tmp_rgb=front_img.getRGB(x,y);

				rgb_list=rgb_parse(tmp_rgb);
				hsv=rgb_to_HSV(rgb_list);
				color_histogram(hsv[0]);
				// System.out.println("R: "+rgb_list[0]+" G: "+rgb_list[1]+" B: "+rgb_list[2]);
				// System.out.println("H: "+hsv[0]+" S: "+hsv[1]+" V: "+hsv[2]);
				// System.out.println("Press Any Key To Continue...");
        // new java.util.Scanner(System.in).nextLine();
			}
		}

		for (int i = 1; i <6; i++){
			if (histogram[i]>histogram[chroma_key_color]){
				chroma_key_color=i;
			}
			average_chroma_key_hue=(int)(histogram_average[chroma_key_color]/histogram[chroma_key_color]);
			// System.out.println(i+": "+ histogram[i]);
		}
		System.out.println("Chroma key color: "+ chroma_key_color+"average hue: "+average_chroma_key_hue);

	}

	public int[] rgb_parse(int rgb){
		int [] return_list = new int[3];
		return_list[0]=rgb >> 16 & 0xff;
		return_list[1]=rgb >> 8 & 0xff;
		return_list[2]=rgb & 0xff;
		return return_list;
	}
	public float[] rgb_to_HSV(int [] rgb_list){
		float hue=0;
		float saturation=0;
		float value=0;
		float red=(float)rgb_list[0]/255;
		float green=(float)rgb_list[1]/255;
		float blue=(float)rgb_list[2]/255;

		float cmax=Math.max(Math.max(red,green),blue);
		float cmin=Math.min(Math.min(red, green),blue);
		float delta=cmax-cmin;
		if (delta==0){
			hue=0;
		}
		else if (red>=blue && red>=green){
			hue=60*(((green-blue)/delta)%6);

		}
		else if(green>=blue && green>=red){
			hue=60*(((blue-red)/delta)+2);

		}
		else if(blue>=red && blue>=green){
			hue=60*(((red-green)/delta)+4);
		}
		if (hue<0){
			hue=360+hue;
		}

		if (cmax!=0){
			saturation=delta/cmax;
		}
		else{
			saturation=0;
		}

		value=cmax;

		float [] hsv=new float[3];
		hsv[0]=hue;
		hsv[1]=saturation;
		hsv[2]=value;

		return hsv;

	}
	public void key_in(){
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				int tmp_rgb=0;
				int []rgb_list=new int[3];
				float [] hsv=new float[3];
				tmp_rgb=front_img.getRGB(x,y);

				rgb_list=rgb_parse(tmp_rgb);
				hsv=rgb_to_HSV(rgb_list);

				int threshold=24;
				if ((hsv[1]<0.12 || hsv[2]<0.2) && chroma_key_color!=1){
					background_img.setRGB(x,y,tmp_rgb);
				}
				else if ((chroma_key_color==0)){
					if (average_chroma_key_hue-threshold<0){
						if(!(hsv[0]<=average_chroma_key_hue+threshold || hsv[0]>360+average_chroma_key_hue-threshold)){
							background_img.setRGB(x,y,tmp_rgb);
						}
					}
					else{
						if (!((hsv[0]>=(average_chroma_key_hue-threshold))&&(hsv[0]<=(average_chroma_key_hue+threshold)))){
							background_img.setRGB(x,y,tmp_rgb);
						}
					}

				}
				else if(chroma_key_color==1){
					if (!((hsv[0]>=(average_chroma_key_hue-threshold))&&(hsv[0]<=(average_chroma_key_hue+threshold))||(hsv[0]==0 || hsv[1]==0))){
						background_img.setRGB(x,y,tmp_rgb);
						// System.out.println("x,y: "+x+","+y);
						// System.out.println("H: "+hsv[0]+" S: "+hsv[1]+" V: "+hsv[2]);
						// System.out.println("Press Any Key To Continue...");
		        // new java.util.Scanner(System.in).nextLine();
					}
				}
				else{
					if (!((hsv[0]>=(average_chroma_key_hue-threshold))&&(hsv[0]<=(average_chroma_key_hue+threshold)))){
						background_img.setRGB(x,y,tmp_rgb);
					}
				}


				// if (!(tmp==6 && chroma_key_color==0)&&(tmp!=chroma_key_color)){
				// 	background_img.setRGB(x,y,tmp_rgb);
				// }

			}
		}
	}
	public static void main(String[] args) {
		chroma ren = new chroma();
		ren.read_front_img(args);
		ren.read_background_img(args);
		ren.front_img_HSV();
		ren.key_in();
		ren.showIms();

	}

}
