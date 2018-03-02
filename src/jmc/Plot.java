package jmc;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jiachen on 05/05/2017
 * Manages a Point ArrayList
 * May 17th TODO: detect greatest integer function asymptotes.
 */
public class Plot {
    private ArrayList<Point> data;
    private ArrayList<Point> coordinates;
    private double lowerBondX;
    private double upperBondX;
    private static final int APL = 5; //Asymptote computation pixel length

    {
        data = new ArrayList<>();
        coordinates = new ArrayList<>();
    }


    Plot(Range range) {
        lowerBondX = range.getLow();
        upperBondX = range.getHigh();
    }

    /**
     * Converts the plot data to actual coordinates on screen.
     * NOTE: by using this method, one should be aware that the y values are scaled.
     * TODO: remove rangeY, should be converted to field and initialized through constructor
     *
     * @param rangeY the range of the y values which is obtained through
     *               the examination of graph window dimension.
     * @param width  the width of the graph
     * @param height the height of the graph.
     */
    void updateCoordinates(Range rangeY, double width, double height) {
        coordinates = new ArrayList<>();
        double lowerBondY = rangeY.getLow(), upperBondY = rangeY.getHigh();
        for (int i = 1; i < data.size(); i++) {
            Point point = data.get(i);
            if (point.getY() > upperBondY && data.get(i - 1).getY() < lowerBondY) {
                point.setY(upperBondY);
                data.get(i - 1).setY(lowerBondY);
            } else if (point.getY() < lowerBondY && data.get(i - 1).getY() > upperBondY) {
                point.setY(lowerBondY);
                data.get(i - 1).setY(upperBondY);
            }
        }

        for (Point point : data) {
            if (!point.isValid()) {
                coordinates.add(point);
                continue;
            }

            double x = Plot.map(point.getX(), lowerBondX, upperBondX, 0, width);
            double y = Plot.map(point.getY(), lowerBondY, upperBondY, 0, height);
            Point coordinate = new Point(point);
            coordinate.setX(x);
            coordinate.setY(y);
            coordinates.add(coordinate);
        }
    }

    public Plot add(Point point) {
        this.data.add(point);
        return this;
    }

    public Plot addAll(Plot plot) {
        this.data.addAll(plot.data);
        return this;
    }

    public void setBoundsX(Range range) {
        this.lowerBondX = range.getLow();
        this.upperBondX = range.getHigh();
    }

    public double getLowerBondX() {
        return lowerBondX;
    }


    public double getUpperBondX() {
        return upperBondX;
    }

    ArrayList<Point> getCoordinates() {
        return coordinates;
    }

    static float map(double d, double l, double u, double l1, double l2) {
        return PApplet.map((float) d, (float) l, (float) u, (float) l1, (float) l2);
    }

    /**
     * Inspects plot data points and note any asymptotes.
     * TODO: invalid asymptotes that don't actually exist would be marked if the graph is only partially visible
     *
     * @param rangeY the valid range of the y values in which a certain point is going to be visible.
     */
    void insertVerticalAsymptote(Range rangeY, Function function) {
        outer:
        for (int i = APL - 1; i < data.size() - APL; i++) {
            for (int q = i - (APL - 1); q <= i + APL; q++) {
                Point point = data.get(q);
                if (!point.isValid() /*!rangeY.isInScope(point.getY())*/) {
                    continue outer;
                }
            }

            data.get(i).setOutOfScope(!rangeY.isInScope(data.get(i).getY()));

            //mark points as asymptotes! Since May 8th, yes!
            if (isIncreasing(i, i + 1) && isDecreasing(i - (APL - 1), i) && isDecreasing(i + 1, i + APL)) {
                //PApplet.println("- tail: " + data.get(i + 1) + " anchor: " + data.get(i));
                data.get(i).setAsAnchor().setY(rangeY.getLow());
                data.get(i + 1).setAsTail().setY(rangeY.getHigh());
            } else if (isDecreasing(i, i + 1) && isIncreasing(i - (APL - 1), i) && isIncreasing(i + 1, i + APL)) {
                //PApplet.println("+ tail: " + data.get(i + 1) + " anchor: " + data.get(i));
                data.get(i).setAsAnchor().setY(rangeY.getHigh());
                data.get(i + 1).setAsTail().setY(rangeY.getLow());
            }
        }
    }

    /**
     * @param fromIndex beginning index, inclusive.
     * @param toIndex   terminating index, inclusive.
     * @return true if indices in data from "fi" to "ti" are increasing
     */
    private boolean isIncreasing(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data.get(i + 1).getY() <= data.get(i).getY()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param fromIndex beginning index, inclusive.
     * @param toIndex   terminating index, inclusive.
     * @return true if indices in data from "fi" to "ti" are decreasing
     */
    private boolean isDecreasing(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data.get(i + 1).getY() >= data.get(i).getY())
                return false;
        }
        return true;
    }


    public String toString() {
        String s = "";
        for (Point point : data) {
            s += point + "\n";
        }
        return s;
    }

    public ArrayList<Point> getData() {
        return data;
    }


    void sort() {
        Point sorted[] = mergeSort(data.toArray(new Point[data.size()]));
        data = new ArrayList<>();
        data.addAll(Arrays.asList(sorted));
    }

    private static Point[] mergeSort(Point[] array) {
        return recursiveMergeSort(0, array.length - 1, array);
    }


    //designed by Jiachen Ren on March 19th
    private static Point[] recursiveMergeSort(int startIndex, int endIndex, Point[] array) {
        // base case
        if (endIndex - startIndex <= 1) {
            if (endIndex == startIndex) return new Point[]{array[endIndex]};
            return array[startIndex].getX() > array[endIndex].getX() ? new Point[]{array[endIndex], array[startIndex]}
                    : new Point[]{array[startIndex], array[endIndex]};
        } else {
            int mid = (startIndex + endIndex) / 2;
            Point[] array_left = recursiveMergeSort(startIndex, mid, array);
            Point[] array_right = recursiveMergeSort(mid + 1, endIndex, array);

            //perform merging
            Point[] merged = new Point[array_left.length + array_right.length];
            int left_index = 0, right_index = 0, curIndex = 0;
            while (left_index < array_left.length && right_index < array_right.length) {
                if (array_left[left_index].getX() > array_right[right_index].getX()) {
                    merged[curIndex] = array_right[right_index];
                    right_index++;
                } else {
                    merged[curIndex] = array_left[left_index];
                    left_index++;
                }
                curIndex++;
            }

            //add the rest of the sorted half list to the merged
            //System.out.println(merged.length);
            for (int i = left_index; i < array_left.length; i++) {
                merged[curIndex] = array_left[i];
                curIndex++;
            }
            for (int i = right_index; i < array_right.length; i++) {
                merged[curIndex] = array_right[i];
                curIndex++;
            }
            return merged;
        }
    }

    double lookUp(double x, double accuracy) {
        return recursiveBinSearch(x, accuracy, 0, data.size());
    }

    /**
     * this method exists for enhanced performance and avoid unnecessary calculations.
     *
     * @param x         the x value to be looked up
     * @param accuracy  an allowed range of difference
     * @param fromIndex start index of the data ArrayList
     * @param toIndex   end index of the data ArrayList
     * @return the double value found using the plotted data
     */
    private double recursiveBinSearch(double x, double accuracy, int fromIndex, int toIndex) {
        int mid = (fromIndex + toIndex) / 2;
        if (mid == fromIndex) return Double.MAX_VALUE;
        if (Math.abs(data.get(mid).getX() - x) <= accuracy) return data.get(mid).getY();
        else if (data.get(mid).getX() - x > 0) return recursiveBinSearch(x, accuracy, fromIndex, toIndex - 1);
        else return recursiveBinSearch(x, accuracy, fromIndex + 1, toIndex);
    }

}
