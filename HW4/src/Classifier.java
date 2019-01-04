import java.text.DecimalFormat;
import java.util.Locale;

import weka.classifiers.Evaluation;

public class Classifier {

    public static void main(String[] args) {

        Locale.setDefault(new Locale("en", "US"));
        DecimalFormat df = new DecimalFormat("#.####");

        String folder = "test5";

        String trainPath = folder + "/train.arff";
        String testPath = folder + "/test.arff";

        evaluateModel(trainPath, testPath);

    }

    private static void evaluateModel(String trainPath, String testPath) {
        // 1NN
        String[] options = {"-t", trainPath, "-T", testPath, "-o", "-v"};				// 1NN

        // Random Forest
        //String[] options = {"-t", trainPath, "-T", testPath, "-o", "-v", "-i", "-I", "100", "-K", "10", "-S", "1"};

        try{
            String results = Evaluation.evaluateModel(new weka.classifiers.lazy.IBk(), options);							// 1NN
            //String results = Evaluation.evaluateModel(new weka.classifiers.trees.RandomForest(), options);				// RF



            String[] lines = results.split("\n");

            for(int j = 0; j < lines.length; j++){

                if(lines[j].startsWith("Kappa")){
                    String[] tokens = lines[j].split("\\s+");
                    double kappa = Double.parseDouble(tokens[2]);
                    System.err.println("Kappa value of classification : " + kappa);

                }
                if (lines[j].startsWith("Correctly")) {
                    String[] tokens = lines[j].split("\\s+");
                    double accuracy = Double.parseDouble(tokens[4]);
                    System.err.println("Overall Accuracy of classification : " + accuracy + " %");
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }


    }

}
