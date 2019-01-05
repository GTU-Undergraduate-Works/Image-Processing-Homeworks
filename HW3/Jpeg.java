import vpt.ByteImage;
import vpt.Image;
import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class Jpeg {


    private static final double[][] QUANTIZATION_MATRIX = {
            {16,11,10,16,24,40,51,61},
            {12,12,14,19,26,58,60,55},
            {14,13,16,24,40,57,69,56},
            {14,17,22,29,51,87,80,62},
            {18,22,37,56,68,109,103,77},
            {24,35,55,64,81,104,113,92},
            {49,64,78,87,103,121,120,101},
            {72,92,95,98,112,100,103,99}
    };

    public static void main(String[] args) {


        Image img1 = Load.invoke("Images/baboon.png");
        compressImage(img1,  "Outputs/baboon.txt");
        Image img2 = decompressImage("Outputs/baboon.txt");
        Display2D.invoke(img1, "Uncompressed Image Baboon", true);
        Display2D.invoke(img2, "Compressed Image Baboon", true);
        System.out.println("Mean Square Error for Baboon : " + calculateMSE(img1, img2));

        img1 = Load.invoke("Images/barbara.png");
        compressImage(img1,  "Outputs/barbara.txt");
        img2 = decompressImage("Outputs/barbara.txt");
        Display2D.invoke(img1, "Uncompressed Image Barbara", true);
        Display2D.invoke(img2, "Compressed Image Barbara", true);
        System.out.println("Mean Square Error for Barbara : " + calculateMSE(img1, img2));

        img1 = Load.invoke("Images/girl.png");
        compressImage(img1,  "Outputs/girl.txt");
        img2 = decompressImage("Outputs/girl.txt");
        Display2D.invoke(img1, "Uncompressed Image Girl", true);
        Display2D.invoke(img2, "Compressed Image Girl", true);
        System.out.println("Mean Square Error for Girl : " + calculateMSE(img1, img2));

    }

    public static void compressImage(Image input, String comFileName) {

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(comFileName));
            int width = input.getXDim();
            int height = input.getYDim();
            int channel = input.getCDim();
            Image paddedImage = paddingImage(input);
            int newWidth = paddedImage.getXDim();
            int newHeight = paddedImage.getYDim();

            writer.write(width + " " + height + " " + newWidth + " " +  newHeight + " " + channel + "\n");
            for (int c = 0; c < channel; ++c) {
                for (int i = 0; i < newWidth; i += 8) {
                    for (int j = 0; j < newHeight; j += 8) {
                        double[][] block = getBlock(paddedImage, i, j, c);
                        int[] RL = compressBlock(block);
                        for (int k = 0; k < RL.length; k++)
                            writer.write(RL[k] + " ");
                        writer.write("\n");
                    }
                }
            }
            writer.close();


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Image decompressImage (String filename) {

        Image output = null;
        try {
            Scanner scanner = new Scanner(new File(filename));
            int width = scanner.nextInt();
            int height = scanner.nextInt();
            int paddedWidth = scanner.nextInt();
            int paddedHeight = scanner.nextInt();
            int channel = scanner.nextInt();
            int[] RL = new int[128];
            output = new ByteImage(paddedWidth, paddedHeight, channel);
            for (int c = 0; c < channel; ++c) {
                for (int i = 0; i < paddedWidth; i += 8) {
                    for (int j = 0; j < paddedHeight; j += 8) {
                        for (int a = 0; a < 128; ++a)
                            RL[a] = scanner.nextInt();
                        double[][] dequantize = dequantize(zigzagDecode(RLEDecode(RL)));
                        for (int k = i, m = 0; k < i + 8; ++k, ++m) {
                            for (int l = j, n = 0; l < j + 8; ++l, ++n) {
                                output.setXYCByte(k, l, c, (int) dequantize[m][n]);
                            }
                        }
                    }
                }
            }
            output = cropImage(output, width, height);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());;
        }
        return output;
    }

    public static double calculateMSE(Image original, Image decoded) {

        int width = original.getXDim();
        int height = original.getYDim();
        int channel = original.getCDim();
        if (width != decoded.getXDim() &&
                height != decoded.getYDim() &&
                channel != decoded.getCDim())
            throw new InvalidParameterException("Size of two image must be same.");
        double sum = 0.0;
        for (int c = 0; c < channel; ++c) {
            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    sum += Math.pow(original.getXYCByte(i,j,c) - decoded.getXYCByte(i,j,c), 2);
                }
            }

        }
        return sum/(height*width*channel);
    }

    private static int[] compressBlock(double[][] block) {
        int[][] quantize = quantize(block);
        int[] zigzag = getZigZagOrder(quantize);
        return RLE(zigzag);
    }

    private static Image cropImage(Image input, int cropWidth, int cropHeight) {

        int width = input.getXDim();
        int height = input.getYDim();
        int channel = input.getCDim();

        if (cropWidth > width ||cropHeight > height)
            throw new InvalidParameterException("Invalid crop width and height argument: " + cropWidth + " " + cropHeight);

        if (cropWidth == width && cropHeight == height)
            return input;

        Image output = new ByteImage(cropWidth, cropHeight, channel);
        for (int c = 0; c < channel; ++c) {
            for (int i = 0; i < cropWidth; ++i) {
                for (int j = 0; j < cropHeight; ++j)
                    output.setXYCByte(i, j, c, input.getXYCByte(i, j, c));
            }
        }
        return output;
    }

    private static double[][] getBlock(Image image, int row, int column, int channel) {

        double[][] block = new double[8][8];
        for (int i = row, k = 0; i < row+8; ++i, ++k) {
            for (int j = column, l = 0; j < column+8; ++j, ++l) {
                block[k][l] = image.getXYCByte(i, j, channel)-128;
            }
        }
        return block;
    }

    private static Image paddingImage(Image input) {

        int width = input.getXDim();
        int height = input.getYDim();
        int channel = input.getCDim();

        if (width % 8 == 0 && height % 8 == 0)
            return input;
        int newWidth = width + (8 - (width % 8));
        int newHeight = height + (8 - (height % 8));
        Image output = new ByteImage(newWidth, newHeight, channel);
        for (int c = 0; c < channel; ++c) {
            for (int i = 0; i < newWidth; ++i) {
                for (int j = 0; j < newHeight; ++j) {
                    if (i < width && j < height)
                        output.setXYCByte(i, j, c, input.getXYCByte(i, j, c));
                    else
                        output.setXYCByte(i, j, c, 0);
                }
            }
        }
        return output;
    }

    private static int[][] quantize(double[][] input) {

        int[][] output = new int[8][8];
        double[][] dctMatrix = getDCTMatrix();
        double[][] dct = multiply(dctMatrix, multiply(input, transpose(dctMatrix)));
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                output[i][j] = (int) Math.round(dct[i][j] / QUANTIZATION_MATRIX[i][j]);
            }
        }
        return output;
    }

    private static double[][] dequantize(double[][] input) {

        double[][] output = new double[8][8];
        double[][] dctMatrix = getDCTMatrix();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                input[i][j] *= QUANTIZATION_MATRIX[i][j];
            }
        }
        double[][] result = multiply(transpose(dctMatrix), multiply(input, dctMatrix));
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                output[i][j] = Math.round(result[i][j]) + 128;
            }
        }
        return output;
    }


    private static double[][] getDCTMatrix() {

        double[][] matrix = new double[8][8];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (i == 0)
                    matrix[i][j] = 1/Math.sqrt(8);
                else {
                    double radian = ((2*j+1)*i*Math.PI)/16;
                    matrix[i][j] = Math.cos(radian)*0.5;
                }
            }
        }
        return matrix;
    }

    private static int[] getZigZagOrder(int[][] matrix) {

        int[] result = new int[64];
        int t = 0;
        for (int i = 0; i < 15; i++) {
            if (i % 2 == 1) {
                int x = i < 8 ? 0 : i - 7;
                int y = i < 8 ? i : 7;
                while (x < 8 && y >= 0) {
                    result[t++] = matrix[x++][y--];
                }
            } else {
                int x = i < 8 ? i : 8 - 1;
                int y = i < 8 ? 0 : i - 8 + 1;
                while (x >= 0 && y < 8) {
                    result[t++] = matrix[x--][y++];
                }
            }
        }
        return result;
    }

    private static double[][] zigzagDecode(int[] zigzag)  {

        double[][] decodedArray = new double[8][8];
        int i = 0, j = 0, k = 0, d = 0;

        while (k < 36)  {
            decodedArray[i][j] = zigzag[k++];
            if((i == 0) && (j % 2 == 0)) {
                j++;
                d=1;
            }
            else if((j == 0) && (i % 2 == 1))  {
                i++;
                d=0;
            }
            else if(d == 0)  {
                i--;
                j++;
            }
            else {
                i++;
                j--;
            }
        }
        i = 7;
        j = 1;
        while(k < 64) {
            decodedArray[i][j] = zigzag[k++];
            if((i == 7)&&(j % 2 == 0))  {
                j++;
                d=0;
            }
            else if((j == 7) &&(i%2 == 1))  {
                i++;
                d=1;
            }
            else if(d == 0)  {
                i--;
                j++;
            }
            else  {
                i++;
                j--;
            }
        }
        return decodedArray;
    }

    private static int[] RLE(int[] zigzag) {

        int[] RL = new int[128];
        int rl=1;
        int i=1;
        int k = 0;
        RL[0] = zigzag[0];
        while(i < 64) {
            k = 0;
            while((i < 64) && (zigzag[i] == 0) && (k < 15))  {
                i++;
                k++;
            }
            if(i == 64) {
                RL[rl++] = 0;
                RL[rl++] = 0;
            }
            else  {
                RL[rl++] = k;
                RL[rl++] = zigzag[i++];
            }
        }
        if(!(RL[rl-1] == 0 && RL[rl-2] == 0))  {
            RL[rl++] = 0;
            RL[rl++] = 0;
        }
        if((RL[rl-4]==15)&&(RL[rl-3]==0))  {
            RL[rl-4]=0;
        }
        return RL;
    }

    private static int[] RLEDecode(int[] RL) {

        int[] zigzag = new int[64];
        int rl=1;
        int i=1;
        zigzag[0] = RL[0];
        while(i < 64)  {
            if(RL[rl] == 0 && RL[rl+1] == 0)  {
                for(int k = i;k < 64;k++)
                    zigzag[k] = 0;
                return zigzag;
            }
            for(int k = 0;k < RL[rl];k++)
                zigzag[i++] = 0;
            zigzag[i++] = RL[rl+1];
            rl+=2;
        }
        return zigzag;
    }

    private static double[][] multiply(double[][] a, double[][] b) {
        double[][] c = new double[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                for (int k = 0; k < 8; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }

    private static double[][] transpose(double[][] a) {

        double[][] b = new double[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                b[j][i] = a[i][j];
        return b;
    }
}
