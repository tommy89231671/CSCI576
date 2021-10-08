import cv2
import random
from scipy.spatial import distance
# import matplotlib as mpl
# mpl.use("Agg")
import matplotlib.pyplot as plt

file_list=["miamibeach.jpg","rubixcube.jpg","lake-forest.jpg","skyclouds.jpg","stagforest.jpg","worldmap.jpg"]


for file in file_list:
    plot_x=[]
    plot_y=[]
    print('start:',file)
    for mr in range(10,52,5):
        img= cv2.imread("../hw1_rgb/"+file)
        missing_img=cv2.imread("../hw1_rgb/"+file)
        height, width, channels = img.shape
        missing_rate=mr/100
        plot_x.append(missing_rate)
        missing_num=int(width*height*missing_rate)

        missing_coor_list=[]
        missing_coor_set=set()
        missing_pixel_list=[]


        for i in range(missing_num):
            rx=random.randint(0,width-1)
            ry=random.randint(0,height-1)
            missing_coor_list.append((ry,rx))
            missing_coor_set.add((ry,rx))
            missing_pixel_list.append(img[ry,rx])
            missing_img[ry,rx]=[0,0,0]


        dst=0
        for k,a in enumerate(missing_coor_list):
            tmpx=a[1]
            tmpy=a[0]
            counter=0
            tmpb=0
            tmpg=0
            tmpr=0
            for i in range(-1,2,1):
                for j in range(-1,2,1):
                    if tmpx+i>=0 and tmpx+i<width and tmpy+j>=0 and tmpy+j<height :
                        if (i==0 and j==0):
                            pass
                        else:
                            bgr=img[tmpy+j,tmpx+i]
                            tmpb+=bgr[0]
                            tmpg+=bgr[1]
                            tmpr+=bgr[2]
                            counter+=1

            missing_img[tmpy,tmpx]=[tmpb//counter,tmpg//counter,tmpr//counter]
            dst += distance.euclidean(tuple(missing_img[tmpy,tmpx]), tuple(missing_pixel_list[k]))
        plot_y.append(dst)
        print(dst)
    plt.plot(plot_x,plot_y)
    print('finish plotting: ',file)
    # plt.show()
    plt.savefig(file[0:-4]+'.png')

    # cv2.imshow('image', missing_img)
    # cv2.waitKey(0)
    # cv2.destroyAllWindows()
