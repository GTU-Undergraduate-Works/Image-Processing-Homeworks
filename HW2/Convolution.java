import vpt.ByteImage;
import vpt.Image;

public class Convolution {

    public static Image convolutionTheorem(Image input, double[][] kernel) {

        int width = input.getXDim();
        int height = input.getYDim();
        int newWidth = 1;
        int newHeight = 1;

        while (newWidth <= width) {
            newWidth = 2*newWidth;
        }
        while (newHeight <= height) {
            newHeight = 2*newHeight;
        }
        Image output = new ByteImage(width, height);
        Complex[][] fft1 = fastFouirer2D(paddingWithZerosImage(convertToComplex(input), newHeight, newWidth), false);
        Complex[][] fft2 = fastFouirer2D(paddingWithZerosImage(convertToComplex(kernel), newHeight, newWidth), false);
        Complex[][] multiply = new Complex[newHeight][newWidth];

        for (int i = 0; i < newHeight; ++i) {
            for (int j = 0; j < newWidth; ++j) {
                multiply[i][j] = fft1[i][j].times(fft2[i][j]);
            }
        }

        Complex[][] inverse = fastFouirer2D(multiply, true);
        double[][] arr = normalize(convertToReal(inverse), height, width);

        for (int i = 0; i < height; ++i) {
            for (int j  = 0; j < width; ++j) {
                output.setXYByte(j, i, (int)arr[i][j]);
            }
        }
        return output;
    }


    public static Image convolution2D(Image input, double [][] kernel) throws Exception {

        int width = input.getXDim();
        int height = input.getYDim();
        int kernelWidth = kernel[0].length;
        int kernelHeight = kernel.length;

        if (kernelHeight % 2 != 1 && kernelWidth % 2 != 1)
            throw new Exception("Kernel heigh and widths must be odd");
        kernel = reflection(kernel);
        Image output = new ByteImage(width, height);
        for (int i = 0; i < width; ++i){
            for (int j = 0;j < height; ++j){
                int sum = 0;
                for (int ii = 0; ii < kernelWidth; ++ii) {
                    for (int jj = 0; jj < kernelHeight; ++jj) {
                        if (ii+i < width && jj+j < height)
                            sum += input.getXYByte(ii+i, jj+j) * kernel[jj][ii];
                    }
                }
                output.setXYByte(i, j, sum);
            }
        }
        return output;
    }

    private static Complex[][] paddingWithZerosImage(Complex[][] input, int paddedHeight, int paddedWidth) {

        int height = input.length;
        int width = input[0].length;
        Complex[][] output = new Complex[paddedHeight][paddedWidth];
        Complex ZERO = new Complex(0,0);
        for (int i = 0; i < paddedHeight; ++i) {
            for (int j = 0; j < paddedWidth; ++j) {
                if (i < height && j < width)
                    output[i][j] = input[i][j];
                else
                    output[i][j] = ZERO;
            }
        }
        return output;
    }


    private static Complex[][] paddingWithZerosFilter(Complex[][] input, int paddedHeight, int paddedWidth) {

        int height = input.length;
        int width = input[0].length;
        Complex[][] output = new Complex[paddedHeight][paddedWidth];

        int spaceHeight = (paddedHeight - height)/2;
        int spaceWidth = (paddedWidth - width)/2;

        Complex ZERO = new Complex(0, 0);
        for (int i = 0; i < paddedHeight; ++i) {
            for (int j = 0; j < paddedWidth; ++j) {
                output[i][j] = ZERO;
            }
        }
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                output[i+spaceHeight][j+spaceWidth] = input[i][j];

            }
        }
        return output;
    }

    private static double[][] reflection(double[][] kernel) {

        int height = kernel.length;
        int width = kernel[0].length;

        int centerWidth = width/2;
        int centerHeight = height/2;

        double[][] output = new double[height][width];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                output[i][j] = kernel[2*centerHeight-i][2*centerWidth-j];

            }
        }
        return output;

    }

    private static Complex[] fastFourier1d(Complex[] x) {

        int size = x.length;
        if (size == 1) return new Complex[] { x[0] };
        if (size % 2 != 0) {
            throw new IllegalArgumentException("size is not a power of 2");
        }
        Complex[] even = new Complex[size/2];
        for (int i = 0; i < size/2; i++) {
            even[i] = x[2*i];
        }
        Complex[] evenFourier = fastFourier1d(even);
        Complex[] odd  = even;
        for (int k = 0; k < size/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] oddFourier = fastFourier1d(odd);
        Complex[] output = new Complex[size];
        for (int i = 0; i < size/2; i++) {
            double ith = -2 * i * Math.PI / size;
            Complex wk = new Complex(Math.cos(ith), Math.sin(ith));
            output[i]       = evenFourier[i].plus(wk.times(oddFourier[i]));
            output[i + size/2] = evenFourier[i].minus(wk.times(oddFourier[i]));
        }
        return output;
    }

    private static Complex[][] fastFouirer2D(Complex[][] input, boolean isInverse) {

        int width = input[0].length;
        int height = input.length;

        Complex[] row = new Complex[width];
        Complex[] column = new Complex[height];
        Complex[] rowOut, colOut;
        Complex[][] output = new Complex[height][width];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                row[i] = input[j][i];

            }
            if (isInverse)
                rowOut = inverseFastFourier1D(row);
            else
                rowOut = fastFourier1d(row);
            for (int i = 0; i < width; ++i)
                output[j][i] = rowOut[i];
        }

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                column[j] = input[j][i];
            }
            if (isInverse)
                colOut = inverseFastFourier1D(column);
            else
                colOut = fastFourier1d(column);
            for (int j = 0; j < height; ++j) {
                output[j][i] = colOut[j];
            }
        }
        return output;
    }

    private static Complex[] inverseFastFourier1D(Complex[] x) {

        int size = x.length;
        Complex[] output = new Complex[size];
        for (int i = 0; i < size; i++) {
            output[i] = x[i].conjugate();
        }
        output = fastFourier1d(output);
        for (int i = 0; i < size; i++) {
            output[i] = output[i].conjugate();
        }
        for (int i = 0; i < size; i++) {
            output[i] = output[i].scale(1.0 / size);
        }
        return output;
    }

    private static Complex[][] convertToComplex(Image input) {

        int height = input.getYDim();
        int width = input.getXDim();
        Complex[][] complexes = new Complex[height][width];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                complexes[i][j] = new Complex(input.getXYByte(j, i), 0);

            }
        }
        return complexes;
    }

    private static Complex[][] convertToComplex(double[][] input) {

        int height = input.length;
        int width = input[0].length;
        Complex[][] complexes = new Complex[height][width];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                complexes[i][j] = new Complex(input[i][j], 0);
            }
        }
        return complexes;
    }

    private static double[][] convertToReal(Complex[][] input) {

        int height = input.length;
        int width = input[0].length;
        double[][] reals = new double[height][width];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                reals[i][j] = input[i][j].getReal();
            }
        }
        return reals;
    }

    private static double[][] normalize(double[][] arr, int height, int width) {

        double max = getMax(arr, height, width);
        double min = getMin(arr, height, width);
        double[][] normalizedArr = new double[arr.length][arr[0].length];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                normalizedArr[i][j] = ((arr[i][j] - min)/(max-min))*255;
            }
        }

        return normalizedArr;
    }

    private static double getMax(double[][] arr, int height, int width) {

        double max = arr[0][0];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (arr[i][j] > max)
                    max = arr[i][j];
            }
        }
        return max;
    }

    private static double getMin(double[][] arr, int height, int width) {

        double min = arr[0][0];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (arr[i][j] < min)
                    min = arr[i][j];
            }
        }
        return min;
    }
}
