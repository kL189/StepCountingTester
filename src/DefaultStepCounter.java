import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultStepCounter implements StepCounter {

    public static ArrayList<Double> getPeakValuesFromIndexes(ArrayList<Integer> peakIndexes, ArrayList<Double> mags) {
        ArrayList<Double> peaks = new ArrayList<>();
        for (int i = 0; i < peakIndexes.size(); i++) {
            double val = mags.get(peakIndexes.get(i));
            peaks.add(val);
        }
        return peaks;
    }

    public ArrayList<Double> smoothOut(ArrayList<Double> mags){
        ArrayList<Double> firstSmooth = mags;
        ArrayList<Double> smooth = new ArrayList<>();
        smooth.add(firstSmooth.get(0));
        for (int i = 1; i < firstSmooth.size() - 2; i++) {
            double weightedAvg=firstSmooth.get(i - 1)/4 + firstSmooth.get(i)/2 + firstSmooth.get(i + 1)/4;
            smooth.add(weightedAvg);
        }
        smooth.add(firstSmooth.get(firstSmooth.size()-1));
       /*for (int i = 0; i < 150; i++) {
           firstSmooth=smooth;

       }*/
        return smooth;
    }


    public static double getMagThreshold(ArrayList<Double> mags){
        System.out.println("Avg:"+getMagAvg(mags));
        System.out.println("Median:"+getMagMedian(mags));
        if(Math.abs(getMagAvg(mags)-getMagMedian(mags))<=5){
            return (getMagAvg(mags)+getMagMedian(mags))/2;
        }else{

        }
        return 0;
    }

    public static double getMagAvg(ArrayList<Double> mags){
        double total=0;
        for (int i = 0; i < mags.size(); i++) {
            total+=mags.get(i);
        }
        return total/mags.size();
    }

    public static double getMagMedian(ArrayList<Double> mags){
        ArrayList<Double> sorted = new ArrayList<>();
        double middle = 0;
        for (int i = 0; i < mags.size(); i++) {
            sorted.add(mags.get(i));
        }
        Collections.sort(sorted);
        if (sorted.size()%2 == 0) {
            middle = (sorted.get(sorted.size()/2) + sorted.get((sorted.size()/2)-1))/2;
        } else {
            middle = sorted.get((sorted.size()-1)/2);
        }
        return middle;
    }

    public static double getMidLine(ArrayList<Double> mags){
        double max = mags.get(0);
        double min = mags.get(0);
        for (int i = 0; i < mags.size(); i++) {
            if(mags.get(i)>max){
                max=mags.get(i);
            }
            if(mags.get(i)<min){
                min=mags.get(i);
            }
        }
        return min+((max-min)/2);
    }

    public static ArrayList<Integer> getPeakIndexes(ArrayList<Double> mags) {
        ArrayList<Integer> peakIndexes= new ArrayList<>();

        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i - 1) < mags.get(i) && mags.get(i) > mags.get(i + 1)) {
                if(mags.get(i)>getMagThreshold(mags)){
                    peakIndexes.add(i);
                }
            }
        }
        return peakIndexes;
    }

    public static int countPeaks(ArrayList<Double> mags) {
        int count = 0;
        for (int i = 1; i < mags.size() - 1; i++) {
            if (mags.get(i - 1) < mags.get(i) && mags.get(i) > mags.get(i + 1)) {
                if (mags.get(i) > getMagThreshold(mags)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static ArrayList<Double> calculateMagnitudes(ArrayList<Double> accX, ArrayList<Double> accY, ArrayList<Double> accZ) {
        ArrayList<Double> output = new ArrayList<>();
        if (accX.size() != accY.size() || accX.size() != accZ.size() || accY.size() != accZ.size()) {
            System.out.println("WARNING: x, y, z acceleration lists not the same length");
        }

        for (int i = 0; i < accX.size(); i++) {
            double mag = magnitude(accX.get(i), accY.get(i), accZ.get(i));
            output.add(mag);
        }
        return output;
    }

    private static double magnitude(Double x, Double y, Double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static ArrayList<Double> getColumnAsList(String[] lines, int colNum) {
        ArrayList<Double> output = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {  // start at 1 to skip the header line
            String line = lines[i];

            String[] values = line.split(",");
            String columnValue = values[colNum];
            double valueAsDouble = Double.parseDouble(columnValue.trim());
            output.add(valueAsDouble);
        }

        return output;
    }

    @Override
    public int countSteps(ArrayList<Double> xAcc, ArrayList<Double> yAcc, ArrayList<Double> zAcc, ArrayList<Double> xGyro, ArrayList<Double> yGyro, ArrayList<Double> zGyro) {
        ArrayList<Double> mags = calculateMagnitudes(xAcc, yAcc, zAcc);
        int count = countPeaks(mags);
        return count;
    }

    @Override
    public int countSteps(String csvFileText) {
        String[] lines = csvFileText.split("\n");

        ArrayList<Double> accX = getColumnAsList(lines, 0);
        ArrayList<Double> accY = getColumnAsList(lines, 1);
        ArrayList<Double> accZ = getColumnAsList(lines, 2);
        ArrayList<Double> gyroX = getColumnAsList(lines, 3);
        ArrayList<Double> gyroY = getColumnAsList(lines, 4);
        ArrayList<Double> gyroZ = getColumnAsList(lines, 5);

        return countSteps(accX, accY, accZ, gyroX, gyroY, gyroZ);
    }
}