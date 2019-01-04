import vpt.Image;
import vpt.algorithms.io.Load;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;

public class Main {



    public static void main(String[] args) {











    }






    private static double[] histogram(int[][] block) {

        double[] histogram = new double[256];
        for (int i = 0; i < 256; ++i)
            histogram[i] = 0;

        for (int i = 0; i < 9 ; ++i) {
            for (int j = 0; j < 9; ++j) {
                histogram[block[i][j]]++;
            }
        }
        return histogram;

    }


    private static double mean(int[][] block) {
        double sum = 0.0;

        for (int i = 0; i < 9 ; ++i) {
            for (int j = 0; j < 9; ++j) {
               sum += block[i][j];
            }
        }

        return sum/81.0;
    }

    private static double variance(int[][] block) {
        double mean = mean(block);
        double sum = 0.0;
        for (int i = 0; i < 9 ; ++i) {
            for (int j = 0; j < 9; ++j) {
                sum += Math.pow(block[i][j] - mean, 2);
            }
        }
        return mean/81.0;
    }




}
