import vpt.Image;
import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {


    public static void main(String[] args)  {

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("-------------- WELCOME TO CONVOLUTION PROGRAM --------------------");
            System.out.print("Please enter the path of image to be convolved : ");
            String filePath = scanner.nextLine();
            Image img = Load.invoke(filePath);
            System.out.print("Please enter the path of the kernel file : ");
            filePath = scanner.nextLine();
            double[][] kernel = readFromFileToArray(filePath);
            Display2D.invoke(img, "Original Image");
            Display2D.invoke(Task1.convolution2D(img, kernel), "Convolved by Convolution Algorithm");
            Display2D.invoke(Task1.convolutionTheorem(img, kernel), "Convolved by FFT");

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            System.exit(1);
        }
    }

    private static double[][] readFromFileToArray(String filePath) {

        int row = 0;
        int column = 0;
        ArrayList<Double> arrayList = new ArrayList<>();
        double[][] arr = null;

        try {
            Scanner scanner = new Scanner(new File(filePath));
            row = scanner.nextInt();
            column = scanner.nextInt();
            scanner.nextLine();
            arr = new double[row][column];
            for (int i = 0; i < row; ++i) {
                String[] lines = scanner.nextLine().split(" ");
                for (int j = 0; j < column; ++j) {
                    arr[i][j] = Double.parseDouble(lines[j]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return arr;
    }
}
