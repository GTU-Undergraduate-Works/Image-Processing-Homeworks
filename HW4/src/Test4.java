import vpt.ByteImage;
import vpt.Image;
import vpt.algorithms.io.Load;
import vpt.algorithms.mm.gray.GClosing;
import vpt.algorithms.mm.gray.GLaplacian;
import vpt.algorithms.mm.gray.GOCCO;
import vpt.algorithms.mm.gray.GOpening;
import vpt.util.se.FlatSE;
import vpt.util.se.NonFlatSE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Test4 {


    public static void main(String[] args) {


        Locale.setDefault(new Locale("en", "US"));
        DecimalFormat df = new DecimalFormat("#.######");

        String folderName = "test4";


        try {

            new File(folderName).mkdir();
            boolean first = true;

            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(folderName + "/train.arff")));
            PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(folderName + "/test.arff")));

            Image train = Load.invoke("Data/train.png");
            Image test = Load.invoke("Data/GT.png");
            Image img = Load.invoke("Data/pan.png");


            ArrayList<Integer> trainClasses = new ArrayList<>();
            ArrayList<double[]> trainVectors = new ArrayList<>();

            ArrayList<Integer> testClasses = new ArrayList<>();
            ArrayList<double[]> testVectors = new ArrayList<>();

            int width = train.getXDim();
            int height = train.getYDim();

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; ++j) {
                    int[][] patch = new int[9][9];
                    if (train.getXYByte(i,j) != 0) {
                        for (int k = 0, m = i-4; k < 9; ++k, ++m) {
                            for (int l = 0, n = j-4; l < 9; ++l, ++n) {
                                patch[k][l] = img.getXYByte(m,n);
                            }
                        }
                        double[] granulometry = granulometry(patch);
                        trainVectors.add(granulometry);
                        trainClasses.add(train.getXYByte(i,j));
                    }
                    if (test.getXYByte(i,j) != 0) {
                        for (int k = 0, m = i-4; k < 9 ; ++k, ++m) {
                            for (int l = 0, n = j-4; l < 9; ++l, ++n) {
                                int a = Math.min(width-1, Math.max(0,m));
                                int b = Math.min(height-1, Math.max(0,n));
                                patch[k][l] = img.getXYByte(a,b);
                            }
                        }
                        double[] granulometry = granulometry(patch);
                        testVectors.add(granulometry);
                        testClasses.add(test.getXYByte(i,j));
                    }
                }
            }

            int trainLength = trainVectors.size();
            for (int i = 0; i < trainLength; ++i) {

                double[] feature = trainVectors.get(i);

                if(first == true) {
                    printHeader(pw, feature.length);
                    printHeader(pw2, feature.length);

                    first = false;
                }

                for(int j = 0; j < feature.length; j++)
                    pw.print(df.format(feature[j])+",");

                pw.println(trainClasses.get(i));

                System.err.println(i + " train");
            }
            pw.close();




            int testLength = testVectors.size();

            for (int i = 0; i < testLength; ++i) {

                double[] feature = testVectors.get(i);



                for(int j = 0; j < feature.length; j++)
                    pw2.print(df.format(feature[j])+",");

                pw2.println(testClasses.get(i));

                System.err.println(i + " test");
            }
            pw2.close();




        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private static double[] granulometry(int[][] patch) {

        double[] featureVector = new double[21];
        featureVector[0] = volume(patch);
        int j = 1;
        for (int i = 1; i <= 10; i += 1) {
            featureVector[j++] = volume(opening(patch, FlatSE.circle(i)));
            featureVector[j++] = volume(closing(patch, FlatSE.circle(i)));
        }
        return featureVector;
    }

    private static double volume(Image img) {

        int width = img.getXDim();
        int height = img.getYDim();
        double sum = 0.0;

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                sum += img.getXYByte(i,j);
            }
        }
        return sum;
    }

    private static double volume(int[][] patch) {

        int width = patch[0].length;
        int height = patch.length;
        double sum = 0.0;

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                sum += patch[i][j];
            }
        }
        return sum;
    }


    private static Image opening(int[][] patch, FlatSE se) {

        int height = patch.length;
        int width = patch[0].length;

        Image img = new ByteImage(width, height);

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                img.setXYByte(i,j, patch[i][j]);
            }
        }
        return GOpening.invoke(img, se);
    }

    private static Image closing(int[][] patch, FlatSE se) {

        int height = patch.length;
        int width = patch[0].length;

        Image img = new ByteImage(width, height);

        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                img.setXYByte(i,j, patch[i][j]);
            }
        }
        return GClosing.invoke(img, se);
    }

    private static void printHeader(PrintWriter pw, int flength){
        pw.println("@RELATION deneme");
        for(int i = 1; i <= flength; i++)
            pw.println("@ATTRIBUTE o" + i +"	REAL");
        pw.println("@ATTRIBUTE o 	{1,2,3,4,5,6}");
        pw.println("@DATA");
    }


}
