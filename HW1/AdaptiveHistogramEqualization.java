import vpt.Image;
import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;

public class AdaptiveHistogramEqualization {



    private static final int WINDOW_SIZE = 200;


    public static void main(String[] args) {

        Image input = Load.invoke("images/valve.png");

        Display2D.invoke(input, "Input Image");

        Display2D.invoke(equalize(input) ,"Equalized Image");
    }


    public static Image equalize(Image input) {

        int width = input.getXDim();
        int height = input.getYDim();
        for (int i = 0; i < width; i += WINDOW_SIZE) {
            for (int j = 0; j < height; j += WINDOW_SIZE) {
                localEqualize(input, i, j);
            }
        }
        return input.newInstance(true);
    }

    private static void localEqualize(Image input, int row, int column) {

        int width = input.getXDim();
        int height = input.getYDim();
        int localWidth, localHeight;

        if (row + WINDOW_SIZE >= width)
            localWidth = width - row;
        else
            localWidth = WINDOW_SIZE;
        if (column + WINDOW_SIZE >= height)
            localHeight = height -column;
        else
            localHeight = WINDOW_SIZE;
        int totalLocalPixel = localWidth*localHeight;
        int[] histogram = new int[256];
        for (int i = 0; i < 256; ++i)
            histogram[i] = 0;

        for (int i = row; i < row+ WINDOW_SIZE && i < width; ++i) {
            for (int j = column; j < column+ WINDOW_SIZE && j < height; ++j) {
                histogram[input.getXYByte(i, j)]++;
            }
        }
        int[] chistogram = new int[256];
        chistogram[0] = histogram[0];
        for(int i=1;i<256;i++){
            chistogram[i] = chistogram[i-1] + histogram[i];
        }

        double[] arr = new double[256];
        for(int i=0;i<256;i++){
            arr[i] =  ((chistogram[i]*255.0)/(double) totalLocalPixel);
        }
        for (int i = row; i < row+ WINDOW_SIZE && i < width; ++i) {
            for (int j = column; j < column+ WINDOW_SIZE && j < height; ++j) {
                histogram[input.getXYByte(i, j)]++;
                int nVal = (int) arr[input.getXYByte(i,j)];
                input.setXYByte(i,j,nVal);
            }
        }

    }
}
