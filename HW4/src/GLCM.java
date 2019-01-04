public class GLCM {


    public static double[] extract(int[][] matrix) {

        double[] featureVector = new double[5];

        int[][] cm0 = createCoOccuranceMatrix(matrix,0);
        double[][] cm0SN = normalizeMatrix(add(cm0, transposeMatrix(cm0)));

       //45°
        int[][] cm45 = createCoOccuranceMatrix(matrix,45);
        double[][] cm45SN = normalizeMatrix(add(cm45, transposeMatrix(cm45)));

       //90°
        int[][] cm90 = createCoOccuranceMatrix(matrix,90);
        double[][] cm90SN = normalizeMatrix(add(cm90, transposeMatrix(cm90)));

        //135°
        int[][] cm135 = createCoOccuranceMatrix(matrix,135);
        double[][] cm135SN = normalizeMatrix(add(cm135, transposeMatrix(cm135)));

        featureVector[0] = (calcContrast(cm0SN) + calcContrast(cm45SN) + calcContrast(cm90SN) + calcContrast(cm135SN)) / 4;
        featureVector[1] = (calcHomogenity(cm0SN) + calcHomogenity(cm45SN) + calcHomogenity(cm90SN) + calcHomogenity(cm135SN)) / 4;
        featureVector[2] = (calcEntropy(cm0SN) + calcEntropy(cm45SN) + calcEntropy(cm90SN) + calcEntropy(cm135SN)) / 4;
        featureVector[3] = (calcEnergy(cm0SN) + calcEnergy(cm45SN) + calcEnergy(cm90SN) + calcEnergy(cm135SN)) / 4;
        featureVector[4] = (calcDissimilarity(cm0SN) + calcDissimilarity(cm45SN) + calcDissimilarity(cm90SN) + calcDissimilarity(cm135SN)) / 4;

        return featureVector;
    }


    private static int[][] createCoOccuranceMatrix(int[][] matrix, int angle) { //distance = 1
        int[][] temp = new int[256][256];
        int startRow = 0;
        int startColumn = 0;
        int endColumn = 0;

        boolean validAngle = true;
        switch (angle) {
            case 0:
                startRow = 0;
                startColumn = 0;
                endColumn = matrix[0].length-2;
                break;
            case 45:
                startRow = 1;
                startColumn = 0;
                endColumn = matrix[0].length-2;
                break;
            case 90:
                startRow = 1;
                startColumn = 0;
                endColumn = matrix[0].length-1;
                break;
            case 135:
                startRow = 1;
                startColumn = 1;
                endColumn = matrix[0].length-1;
                break;
            default:
                validAngle = false;
                break;
        }

        if (validAngle) {
            for (int i = startRow; i < matrix.length; i++) {
                for (int j = startColumn; j <= endColumn; j++) {
                    switch (angle) {
                        case 0:
                            temp[matrix[i][j]][matrix[i][j+1]]++;
                            break;
                        case 45:
                            temp[matrix[i][j]][matrix[i-1][j+1]]++;
                            break;
                        case 90:
                            temp[matrix[i][j]][matrix[i-1][j]]++;
                            break;
                        case 135:
                            temp[matrix[i][j]][matrix[i-1][j-1]]++;
                            break;
                    }
                }
            }
        }
        return temp;
    }

    private static int[][] transposeMatrix(int [][] m){
        int[][] temp = new int[m[0].length][m.length];
        for (int i = 0; i < m.length; i++){
            for (int j = 0; j < m[0].length; j++){
                temp[j][i] = m[i][j];
            }
        }
        return temp;
    }

    private static int[][] add(int [][] m2, int [][] m1){
        int[][] temp = new int[m1[0].length][m1.length];
        for (int i = 0; i < m1.length; i++){
            for (int j = 0; j < m1[0].length; j++){
                temp[j][i] = m1[i][j] + m2[i][j];
            }
        }
        return temp;
    }

    private static int getTotal(int [][] m){
        int temp = 0;
        for (int i = 0; i < m.length; i++){
            for (int j = 0; j < m[0].length; j++){
                temp += m[i][j];
            }
        }
        return temp;
    }

    private static double[][] normalizeMatrix(int [][] m){
        double[][] temp = new double[m[0].length][m.length];
        int total = getTotal(m);
        for (int i = 0; i < m.length; i++){
            for (int j = 0; j < m[0].length; j++){
                temp[j][i] = (double) m[i][j] / total;
            }
        }
        return temp;
    }

    private static double calcContrast(double[][] matrix) {
        double temp = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp += matrix[i][j] * Math.pow(i-j, 2);
            }
        }
        return temp;
    }

    private static double calcHomogenity(double[][] matrix) {
        double temp = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp += matrix[i][j] / (1+Math.pow(i-j, 2));
            }
        }
        return temp;
    }

    private static double calcEntropy(double[][] matrix) {
        double temp = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] != 0) {
                    temp += (matrix[i][j] * Math.log10(matrix[i][j])) * -1;
                }
            }
        }
        return temp;
    }

    private static double calcEnergy(double[][] matrix) {
        double temp = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp += Math.pow(matrix[i][j], 2);
            }
        }
        return temp;
    }

    private static double calcDissimilarity(double[][] matrix) {
        double temp = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp += matrix[i][j] * Math.abs(i-j);
            }
        }
        return temp;
    }
}
