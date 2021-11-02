import math

n=8

pixel=[]
dct=[]
for i in range(8):
    dct.append([0]*8)
    pixel.append([0]*8)

with open('dct_input2.txt') as f:
    lines = f.readlines()
# print(lines)
for i,line in enumerate(lines):
    # pixel[i%8][i//8]=int(line)
    for j,a in enumerate(line.split(' ')):
        pixel[i][j]=int(a)
print(pixel)

for i in range(n):
    for j in range(n):
        tmp=0
        for x in range(n):
            for y in range(n):
                tmp+=math.cos((2*x+1)*i*math.pi/16)*math.cos((2*y+1)*j*math.pi/16)*pixel[x][y]
        res=0
        if i==0 and j==0:
            res=round((tmp/8),1)
            print(res)
        elif i==0 or j==0:
            res=round((tmp/4/math.sqrt(2)),1)
        else:
            res=round((tmp/4),1)
        dct[i][j]=round((res/100),0)
print(dct)
