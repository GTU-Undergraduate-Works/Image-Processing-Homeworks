import vpt.Image;
import vpt.algorithms.io.Load;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Test1 {







    public static void main(String[] args) {

        Locale.setDefault(new Locale("en", "US"));
        DecimalFormat df = new DecimalFormat("#.######");

        String folderName = "test1";


        try {

            new File(folderName).mkdir();
            boolean first = true;

            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(folderName + "/train.arff")));
            PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(folderName + "/test.arff")));

            ArrayList<Integer> trainClasses = new ArrayList<>();
            ArrayList<double[]> trainVectors = new ArrayList<>();

            ArrayList<Integer> testClasses = new ArrayList<>();
            ArrayList<double[]> testVectors = new ArrayList<>();

            Image train = Load.invoke("Data/train.png");
            Image test = Load.invoke("Data/GT.png");
            Image img = Load.invoke("Data/pan.png");


            int width = train.getXDim();
            int height = train.getYDim();

            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    double[] vector = new double[1];
                    if (train.getXYByte(i,j) != 0) {
                        vector[0] = (double) img.getXYByte(i, j);
                        trainVectors.add(vector);
                        trainClasses.add(train.getXYByte(i,j));
                    }
                    if (test.getXYByte(i,j) != 0) {
                        vector[0] = (double) img.getXYByte(i, j);
                        testVectors.add(vector);
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




        private static void printHeader(PrintWriter pw, int flength){




            pw.println("@RELATION deneme");
            for(int i = 1; i <= flength; i++)
                pw.println("@ATTRIBUTE o" + i +"	REAL");
            pw.println("@ATTRIBUTE o 	{1,2,3,4,5,6}");
            pw.println("@DATA");
        }

}
