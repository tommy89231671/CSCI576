
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;



public class Motion_Compensation{

	JFrame frame;
	JLabel lbIm1;
	BufferedImage current_img;
	BufferedImage next_img;
	BufferedImage predict_img;
	BufferedImage diff_img;
	int width = 640;
	int height = 320;

	double [][] current_luma=new double [width][height];
	double [][] next_luma=new double [width][height];

	BufferedImage img_ans;

	int chroma_key_color=0;
	// int saturation_th=0;
	// int value_th=0;
	int cnt=0;



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

	public void showpredictIms(){
		//
		// // Read a parameter from command line
		// gray_img(next_img);
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

		lbIm1 = new JLabel(new ImageIcon(predict_img));
		frame.getContentPane().removeAll();
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}
	public void showdiffIms(){
		//
		// // Read a parameter from command line
		// gray_img(next_img);
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

		lbIm1 = new JLabel(new ImageIcon(diff_img));
		frame.getContentPane().removeAll();
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}


	public void read_current_img(String[] args){
		// Read in the specified image
		current_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], current_img);

	}
	public void read_next_img(String[] args){
		// Read in the specified image
		next_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[1], next_img);

	}

	public void gray_img(BufferedImage img){
		BufferedImage result = new BufferedImage(
            img.getWidth(),
            img.getHeight(),
            BufferedImage.TYPE_BYTE_GRAY);
	  Graphics g = result.getGraphics();
	  g.drawImage(img, 0, 0, null);
	  g.dispose();
		img=result;
	}

	public void current_img_yuv(){
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				int tmp_rgb=0;
				int []rgb_list=new int[3];
				tmp_rgb=current_img.getRGB(x,y);

				rgb_list=rgb_parse(tmp_rgb);
				int lum=(int)rgb_to_yuv(rgb_list);
				current_luma[x][y]=rgb_to_yuv(rgb_list);
				int gray = (lum << 16) + (lum << 8) + lum;
				// current_img.setRGB(x,y,gray);
				// System.out.println("Press Any Key To Continue...");
        // new java.util.Scanner(System.in).nextLine();
			}
		}

	}
	public void next_img_yuv(){
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				int tmp_rgb=0;
				int []rgb_list=new int[3];
				tmp_rgb=next_img.getRGB(x,y);

				rgb_list=rgb_parse(tmp_rgb);
				int lum=(int)rgb_to_yuv(rgb_list);

        int gray = (lum << 16) + (lum << 8) + lum;
				next_luma[x][y]=rgb_to_yuv(rgb_list);
				next_img.setRGB(x,y,gray);

			}
		}

	}

	public void predict(String [] args){
		int k_area=Integer.parseInt(args[2]);
		predict_img=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		diff_img=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int j=0; j<(int)(height/16); j++){
			for(int i=0; i<(int)(width/16); i++){
				double min_error=mse(i,j,i,j);
				int mapping_x=i;
				int mapping_y=j;

				for(int kx=i-k_area; kx<i+k_area+1; kx++){
					for(int ky=j-k_area; ky<j+k_area+1; ky++){
						if(kx>=0 && ky>=0 && kx<40 && ky<20){
							double tmp_mse=mse(kx,ky,i,j);
							if (tmp_mse<min_error){
								mapping_x=kx;
								mapping_y=ky;
								min_error=tmp_mse;
							}

						}
					}

				}
				System.out.println("block:("+i+","+j+")"+ "mapping:("+mapping_x+","+mapping_y+")");
				for (int jj=0; jj<16; jj++){
					for (int ii=0; ii<16; ii++){
						int lum=(int)current_luma[mapping_x*16+ii][mapping_y*16+jj];

						int gray = (lum << 16) + (lum << 8) + lum;
						predict_img.setRGB(i*16+ii,j*16+jj,gray);
						int diff_lum=Math.abs((int)next_luma[mapping_x*16+ii][mapping_y*16+jj]-lum);
						int diff_gray = (diff_lum << 16) + (diff_lum << 8) + diff_lum;
						diff_img.setRGB(i*16+ii,j*16+jj,diff_gray);
					}
				}

			}
		}
	}
	public double mse(int b1i, int b1j, int b2i, int b2j){
		double tmp=0;
		// System.out.println((b2i*16)+" "+(b2j*16)+" "+(b1i*16)+" "+(b1j*16));
		// System.out.println((b2i)+" "+(b2j)+" "+(b1i)+" "+(b1j));
		for (int y= 0; y < 16; y++){
			for (int x=0; x< 16; x++){

				tmp+=Math.pow((next_luma[b2i*16+x][b2j*16+y]-current_luma[b1i*16+x][b1j*16+y]),2);
			}
		}
		return tmp/256;
	}

	public int[] rgb_parse(int rgb){
		int [] return_list = new int[3];
		return_list[0]=rgb >> 16 & 0xff;
		return_list[1]=rgb >> 8 & 0xff;
		return_list[2]=rgb & 0xff;
		return return_list;
	}
	public double rgb_to_yuv(int [] rgb_list){
		float hue=0;
		float saturation=0;
		float value=0;
		float red=(float)rgb_list[0]/255;
		float green=(float)rgb_list[1]/255;
		float blue=(float)rgb_list[2]/255;

		double Y=rgb_list[0]*0.299+rgb_list[1]*0.587+rgb_list[2]*0.114;
		return Y;



	}

	public static void main(String[] args) {
		Motion_Compensation mc = new Motion_Compensation();
		mc.read_current_img(args);
		mc.read_next_img(args);
		mc.current_img_yuv();
		mc.next_img_yuv();
		mc.predict(args);
		// ren.current_img_HSV();
		// ren.key_in();
		// ren.antialiasing();
		mc.showpredictIms();
		mc.showdiffIms();

	}

}
