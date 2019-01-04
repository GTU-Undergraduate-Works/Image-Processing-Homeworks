import vpt.ByteImage;
import vpt.Image;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class JpegEncoder {

    private final double[][] QUANTIZATION_MATRIX =
            { { 16,11,10,16,24,40,51,61},
                    { 12,12,14,19,26,58,60,55 },
                    { 14,13,16,24,40,57,69,56 },
                    { 14,17,22,29,51,87,80,62 },
                    { 18,22,37,56,68,109,103,77 },
                    { 24,35,55,64,81,104,113,92 },
                    { 49,64,78,87,103,121,120,101 },
                    { 72,92,95,98,112,100,103,99 } };

    private final int[] DCCodeLen = {3,4,5,5,7,8,10,12,14,16,18,20};
    private final String[] DCCode = {"010","011","100","00","101","110","1110","11110","111110","1111110","11111110","111111110"};

    private final int[][] ACcodeLen = {
            {4 ,3 ,4 ,6 ,8 ,10,12,14,18,25,26},
            {0 ,5 ,8 ,10,13,16,22,23,24,25,26},
            {0 ,6 ,10,13,20,21,22,23,24,25,26},
            {0 ,7 ,11,14,20,21,22,23,24,25,26},
            {0 ,7 ,12,19,20,21,22,23,24,25,26},
            {0 ,8 ,12,19,20,21,22,23,24,25,26},
            {0 ,8 ,13,19,20,21,22,23,24,25,26},
            {0 ,9 ,13,19,20,21,22,23,24,25,26},
            {0 ,9 ,17,19,20,21,22,23,24,25,26},
            {0 ,10,18,19,20,21,22,23,24,25,26},
            {0 ,10,18,19,20,21,22,23,24,25,26},
            {0 ,10,18,19,20,21,22,23,24,25,26},
            {0 ,11,18,19,20,21,22,23,24,25,26},
            {0 ,12,18,19,20,21,22,23,24,25,26},
            {0 ,13,18,19,20,21,22,23,24,25,26},
            {12,17,18,19,20,21,22,23,24,25,26}
    };

    private final String[][] ACCode = {
            {"1010",  "00",  "01",  "100",  "1011",  "11010",  "111000",  "1111000",  "1111110110",  "1111111110000010",  "1111111110000011"},
            {"","1100","111001","1111001","111110110","11111110110","1111111110000100","1111111110000101","1111111110000110","1111111110000111","1111111110001000"},
            {"","11011","11111000","1111110111","1111111110001001","1111111110001010","1111111110001011","1111111110001100","1111111110001101","1111111110001110","1111111110001111"},
            {"","111010","111110111","11111110111","1111111110010000","1111111110010001","1111111110010010","1111111110010011","1111111110010100","1111111110010101","1111111110010110"},
            {"","111011","1111111000","1111111110010111","1111111110011000","1111111110011001","1111111110011010","1111111110011011","1111111110011100","1111111110011101","1111111110011110"},
            {"","1111010","1111111001","1111111110011111","1111111110100000","1111111110100001","1111111110100010","1111111110100011","1111111110100100","1111111110100101","1111111110100110"},
            {"","1111011","11111111000","1111111110100111","1111111110101000","1111111110101001","1111111110101010","1111111110101011","1111111110101100","1111111110101101","1111111110101110"},
            {"","11111001","11111111001","1111111110101111","1111111110110000","1111111110110001","1111111110110010","1111111110110011","1111111110110100","1111111110110101","1111111110110110"},
            {"","11111010","111111111000000","1111111110110111","1111111110111000","1111111110111001","1111111110111010","1111111110111011","1111111110111100","1111111110111101","1111111110111110"},
            {"","111111000","1111111110111111","1111111111000000","1111111111000001","1111111111000010","1111111111000011","1111111111000100","1111111111000101","1111111111000110","1111111111000111"},
            {"","111111001","1111111111001000","1111111111001001","1111111111001010","1111111111001011","1111111111001100","1111111111001101","1111111111001110","1111111111001111","1111111111010000"},
            {"","111111010","1111111111010001","1111111111010010","1111111111010011","1111111111010100","1111111111010101","1111111111010110","1111111111010111","1111111111011000","1111111111011001"},
            {"","1111111010","1111111111011010","1111111111011011","1111111111011100","1111111111011101","1111111111011110","1111111111011111","1111111111100000","1111111111100001","1111111111100010"},
            {"","11111111010","1111111111100011","1111111111100100","1111111111100101","1111111111100110","1111111111100111","1111111111101000", "1111111111101001","1111111111101010","1111111111101011"},
            {"","111111110110","1111111111101100","1111111111101101","1111111111101110","1111111111101111","1111111111110000","1111111111110001","1111111111110010","1111111111110011","1111111111110100"},
            {"111111110111","1111111111110101","1111111111110110","1111111111110111","1111111111111000","1111111111111001","1111111111111010","1111111111111011","1111111111111100","1111111111111101","1111111111111110"}
    };

    private int imgWidth;
    private int imgHeight;
    private int imgChannel;
    private Image image;
    private int newDC;

    public JpegEncoder(Image image) {
        this.image = image;
        imgWidth = image.getXDim();
        imgHeight = image.getYDim();
        imgChannel = image.getCDim();
        newDC = 0;

    }


    public void compressImage(String comFileName) {


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(comFileName));

            writer.write(imgWidth + " " + imgHeight + " " + imgChannel + "\n");
            double[][] block;

            for (int c = 0; c < imgChannel; ++c) {
                for (int i = 0; i < imgWidth; i += 8) {
                    for (int j = 0; j < imgHeight; j += 8) {
                        block = getBlock(image, i, j, c);
                        int[] RL = Compress(block);
                        writeToFile(writer, RL);
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(BufferedWriter writer, int[] RL) {


        try {

            for (int i = 0; i < RL.length; i++)
                writer.write(RL[i] + " ");
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public int[] Compress(double[][] block) {
        int[][] quantize = quantize(block);
        int[] zigzag = getZigZagOrder(quantize);
        int[] RL = new int[128];
        int rl = RLE(zigzag,RL);
        return RL;
    }


    public Image decompressImage (String filename) {


        Image output = null;
        try {
            Scanner scanner = new Scanner(new File(filename));
            int width = scanner.nextInt();
            int height = scanner.nextInt();
            int channel = scanner.nextInt();
            int[] RL = new int[128];
            output = new ByteImage(width, height, channel);

            for (int c = 0; c < channel; ++c) {

                for (int i = 0; i < width; i += 8) {
                    for (int j = 0; j < height; j += 8) {

                        for (int a = 0; a < 128; ++a)
                            RL[a] = scanner.nextInt();

                        double[][] dequantize = dequantize(zigzagDecoder(RLEDecode(RL)));

                        for (int k = i, m = 0; k < i + 8; ++k, ++m) {
                            for (int l = j, n = 0; l < j + 8; ++l, ++n) {
                                output.setXYCByte(k, l, c, (int) dequantize[m][n]);
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;

    }


    public String encode(int[] RL, int rl)  {

        ArrayList<Character> code = getDCCode(RL[0]);
        for(int i = 1;i < rl;i += 2)
            code.addAll(getACCode(RL[i],RL[i+1]));
        StringBuilder sb = new StringBuilder();
        for (Character ch: code) {
            sb.append(ch);
        }
        return sb.toString();
    }


    public int RLE(int[] zigzag,int[] RL) {
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
            rl-=2;
        }
        return rl;
    }


    private double[][] getBlock(Image image, int row, int column, int channel) {

        double[][] block = new double[8][8];
        for (int i = row, k = 0; i < row+8; ++i, ++k) {
            for (int j = column, l = 0; j < column+8; ++j, ++l) {
                block[k][l] = image.getXYCByte(i, j, channel)-128;
            }
        }
        return block;
    }

    private ArrayList<Character> getACCode(int n, int a) {

        int cat = getCategory(a);
        int length = ACcodeLen[n][cat];
        char[] code = new char[32];
        String acCode = ACCode[n][cat];
        int size = acCode.length();
        for (int i = 0; i < size; ++i)
            code[i] = acCode.charAt(i);
        int c = a;
        if(a < 0)
            c+=(int)Math.pow(2,cat)-1;
        for(int j = length-1; j > length-cat-1; j--)  {
            if(c%2 == 1)
                code[j] = '1';
            else
                code[j] = '0';
            c/=2;
        }
        //code[length] = 'e';
        ArrayList<Character> tc = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            if (code[i] == '1' || code[i] == '0' || code[i] == 'e')
                tc.add(code[i]);
        }
        return tc;
    }



    private ArrayList<Character> getDCCode(int a) {

        int cat = getCategory(a);
        int length = DCCodeLen[cat];
        char[] code = new char[32];
        String dcCode = DCCode[cat];
        int n = dcCode.length();
        for (int i = 0; i < n; ++i)
            code[i] = dcCode.charAt(i);

        int c = a;
        if (a < 0)
            c += (int) Math.pow(2,cat)-1;
        for(int j = length-1; j > length-cat-1; j--) {

            if(c%2 == 1)
                code[j] = '1';
            else
                code[j] = '0';
            c/=2;
        }
        //code[length] = 'e';
        ArrayList<Character> tc = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            if (code[i] == '1' || code[i] == '0' || code[i] == 'e')
                tc.add(code[i]);
        }
        return tc;
    }


    public int[][] quantize(double[][] input) {


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

    public double[][] dequantize(double[][] input) {

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

    private double[][] getDCTMatrix() {

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

    private int getCategory(int a) {

        if(a==0)
            return 0;
        else if(Math.abs(a)<=1)
            return 1;
        else if(Math.abs(a)<=3)
            return 2;
        else if(Math.abs(a)<=7)
            return 3;
        else if(Math.abs(a)<=15)
            return 4;
        else if(Math.abs(a)<=31)
            return 5;
        else if(Math.abs(a)<=63)
            return 6;
        else if(Math.abs(a)<=127)
            return 7;
        else if(Math.abs(a)<=255)
            return 8;
        else if(Math.abs(a)<=511)
            return 9;
        else if(Math.abs(a)<=1023)
            return 10;
        else if(Math.abs(a)<=2047)
            return 11;
        else if(Math.abs(a)<=4095)
            return 12;
        else if(Math.abs(a)<=8191)
            return 13;
        else if(Math.abs    (a)<=16383)
            return 14;
        else
            return 15;
    }

    public int[] getZigZagOrder(int[][] matrix) {

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



    public void getACValue(ArrayList<Character> input, int pos, int n, int p) {

        int a, length;

        int row = ACCode.length;
        int column = ACCode[0].length;
        String[][] code = new String[row][column];
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < column; ++j) {
                StringBuilder sb = new StringBuilder(ACCode[i][j]);
                sb.append('e');
                code[i][j] = sb.toString();
            }
        }

        for(int k=0;k<16;k++)
            for(int i=0;i<11;i++)
            {
                a = 1;
                length = code[k][i].length();
                for(int j=0;j<length;j++)
                    if(code[k][i].charAt(j)!=input.get(pos+j))
                        a = 0;
                if ((a==1)&&(!(k!=0 && k!=15 && i==0)))
                {
                    pos+=length;
                    length = ACcodeLen[k][i]-length;
                    if(length==0)
                    {
                        n=k;
                        p=0;
                        return;
                    }
                    n = k;
                    p=0;
                    for(int j=0;j<length;j++)
                    {
                        p*=2;
                        if(input.get(pos+j)=='1')
                            p++;
                    }
                    if(input.get(pos)=='0')
                        p = p + 1 - (int)Math.pow(2,length);
                    pos+=length;
                    return;
                }
            }
        return;
        }


    public int getDCValue(ArrayList<Character> input, int pos) {

        int a, length, p;

        String[] code = new String[DCCode.length];
        for (int i = 0; i < DCCode.length; ++i) {
            StringBuilder sb = new StringBuilder(DCCode[i]);
            sb.append('e');
            code[i] = sb.toString();
            System.out.println(code[i]);
        }


        for (int i = 0; i < 12; ++i) {
            a = 1;
            length = code[i].length();
            for (int j = 0; j < length; ++j) {
                if (code[i].charAt(j) != input.get(pos+j))
                    a = 0;
            }
            if (a == 1) {
                pos += length;
                length = DCCodeLen[i] - length;
                if (length == 0)
                    return 0;
                p = 0;
                for (int j = 0; j < length; ++j) {
                    p *= 2;
                    if (input.get(pos+j) == '1')
                        p++;
                    if (input.get(pos) == '0')
                        p = p + 1 - (int)Math.pow(2, length);
                }
            }
        }
        return 0;
    }






    public int[] RLEDecode(int[] RL) {

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

    public double[][] zigzagDecoder(int[] rle)  {


        double[][] decodedArray = new double[8][8];

        int i = 0,j = 0,k = 0,d = 0;
        while(k < 36)  {

            decodedArray[i][j] = rle[k++];
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
            decodedArray[i][j] = rle[k++];
            if((i == 7)&&(j % 2 == 0))  {
                j++;
                d=0;
            }
            else if((j==7)&&(i%2==1))  {
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

    private double[][] multiply(double[][] a, double[][] b) {
        double[][] c = new double[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                for (int k = 0; k < 8; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }

    private double[][] transpose(double[][] a) {

        double[][] b = new double[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                b[j][i] = a[i][j];
        return b;
    }

}
