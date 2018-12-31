import vpt.ByteImage;
import vpt.Image;

import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;

public class CountNumberOfConnectedComponents {

    private static final int FOREGROUND = 255;
    private static final int BACKGROUND = 0;
    private static final int MAX_LABELS = 80000;
    private static int next_label = 1;




    public static void main(String[] args) {

        Image input = Load.invoke("images/abdomen.png");


        Image output = removeUnwwantedArtifacts(input);
        Display2D.invoke(input,"Input Image");
        Display2D.invoke(output, "Output Image");
        labeling(output);
    }

    public static Image removeUnwwantedArtifacts(Image input) {
        return closing(opening(input));
    }


    public static int[] labeling(Image input) {

        int width = input.getXDim();
        int height = input.getYDim();
        int[] image = convertImage2Dto1D(input);
        int[] result= new int[width*height];
        int[] parent= new int[MAX_LABELS];
        int[] labels= new int[MAX_LABELS];
        int next_region = 1;
        for (int y = 0; y < height; ++y ){
            for (int x = 0; x < width; ++x ){
                if (image[y*width+x] == BACKGROUND) continue;
                int k = 0;
                boolean connected = false;
                if (x > 0 && image[y*width+x-1] == image[y*width+x]) {
                    k = result[y*width+x-1];
                    connected = true;
                }
                if (y > 0 && image[(y-1)*width+x]== image[y*width+x] &&
                        (connected = false || image[(y-1)*width+x] < k )) {
                    k = result[(y-1)*width+x];
                    connected = true;
                }
                if ( !connected ) {
                    k = next_region;
                    next_region++;
                }
                result[y*width+x]= k;
                if ( x> 0 && image[y*width+x-1]== image[y*width+x] && result[y*width+x-1]!= k )
                    union( parent, k, result[y*width+x-1]);
                if ( y> 0 && image[(y-1)*width+x]== image[y*width+x] && result[(y-1)*width+x]!= k )
                    union(parent, k, result[(y-1)*width+x]);
            }
        }
        next_label = 1;
        for (int i = 0; i < width*height; i++ ) {
            if (image[i]!=0) {
                result[i] = find( labels, result[i], parent);

            }
        }
        next_label--;
        System.out.println("There are " + next_label + " connected components in image.");
        return result;
    }


    private static Image filterImage(Image input, int target) {

        int width = input.getXDim();
        int height = input.getYDim();
        Image filteredImage = new ByteImage(width, height);
        int[] output = new int[width*height];

        int targetValue = (target == BACKGROUND) ?BACKGROUND : FOREGROUND;
        int reverseValue = (target == BACKGROUND) ?FOREGROUND : BACKGROUND;

        for(int y = 0; y < height; ++y){
            for(int x = 0; x < width; ++x){
                if(input.getXYByte(x, y) == targetValue){
                    boolean flag = false;
                    for(int j = y - 2; j <= y + 2 && flag == false; j++){
                        for(int i = x - 2; i <= x + 2 && flag == false; i++){
                            if(j >= 0 && j < height && i >= 0 && i < width){
                                if(input.getXYByte(i, j) != targetValue){
                                    flag = true;
                                    output[x+y*width] = reverseValue;
                                }
                            }
                        }
                    }
                    if(flag == false){
                        output[x+y*width] = targetValue;
                    }
                }else{
                    output[x+y*width] = reverseValue;
                }
            }
        }

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int v = output[x+y*width];
                filteredImage.setXYByte(x, y, v);
            }
        }
        return filteredImage;
    }

    private static Image erosion(Image input) {
        return filterImage(input, FOREGROUND);
    }

    private static Image dilation(Image input) {
        return filterImage(input, BACKGROUND);
    }

    private static Image opening(Image input) {
        return dilation(erosion(input));
    }

    private static Image closing(Image input) {
        return erosion(dilation(input));
    }



    private static void union(int[] parent, int x, int y) {

        while (parent[x] > 0)
            x = parent[x];
        while (parent[y] > 0)
            y = parent[y];
        if (x != y) {
            if (x < y)
                parent[x] = y;
            else
                parent[y] = x;
        }
    }

    private static int find( int[] label, int x, int[] parent) {

        while ( parent[x]>0 )
            x = parent[x];
        if ( label[x] == 0 )
            label[x] = next_label++;
        return label[x];
    }

    private static int[] convertImage2Dto1D(Image input) {

        int width = input.getXDim();
        int height = input.getYDim();

        int[] output = new int[width*height];

        int k = 0;
        for (int y = 0;  y < height; ++y) {
            for (int x = 0; x < width; x++) {
                output[k++] = input.getXYByte(x,y);
            }
        }
        return output;
    }
}
