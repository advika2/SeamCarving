/* *****************************************************************************
 *  Description: implements seam carving, "a content-aware image resizing
 * technique where the image is reduced in size by one pixel of height (
 * or width) at a time".
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {

    private Picture toSeam; // current picture
    private int width; // width of picture
    private int height; // height of picture

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException();
        toSeam = new Picture(picture);
        width = toSeam.width();
        height = toSeam.height();
    }

    // current picture
    public Picture picture() {
        return new Picture(toSeam);
    }

    // width of current picture
    public int width() {
        return toSeam.width();
    }

    // height of current picture
    public int height() {
        return toSeam.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x >= width() || x < 0 || y >= height() || y < 0) {
            throw new IllegalArgumentException();
        }
        // calculate x-gradient
        // first right
        int nextCol = x + 1;
        if (x == width() - 1) nextCol = 0;

        // then left
        int prevCol = x - 1;
        if (x == 0) prevCol = width() - 1;

        double dXSquare = calcdSquare(nextCol, y, prevCol, y);


        // now up
        int nextRow = y + 1;
        if (y == height() - 1) nextRow = 0;

        // then down
        int prevRow = y - 1;
        if (y == 0) prevRow = height() - 1;
        double dYSquare = calcdSquare(x, nextRow, x, prevRow);

        return Math.sqrt(dXSquare + dYSquare);
    }

    // calculates x and y energy squared components
    private double calcdSquare(int colOne, int rowOne, int colTwo, int rowTwo) {
        int rgb = toSeam.getRGB(colOne, rowOne);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;

        int rgbTwo = toSeam.getRGB(colTwo, rowTwo);
        int redTwo = (rgbTwo >> 16) & 0xFF;
        int greenTwo = (rgbTwo >> 8) & 0xFF;
        int blueTwo = (rgbTwo) & 0xFF;
        return Math.pow((red - redTwo), 2) + Math.pow((green - greenTwo), 2)
                + Math.pow((blue - blueTwo), 2);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transpose();
        // runs findVerticalSeam on transposed picture
        int[] result = findVerticalSeam();
        transpose();
        return result;
    }

    // transposes given picture
    private void transpose() {
        Picture replaceToSeam = new Picture(height, width);
        for (int i = 0; i < height; i++) {
            // row of replaceToSeam (columns of toSeam)
            for (int j = 0; j < width; j++) {
                replaceToSeam.setRGB(i, j, toSeam.getRGB(j, i));
            }
        }
        toSeam = replaceToSeam;
        width = replaceToSeam.width();
        height = replaceToSeam.height();
    }

    // relaxes given vertex
    private void relax(int i, double[] distTo, int[] edgeTo, double[][] energyMatrix) {
        // for the first row of vertices that have only one edge coming to
        // it
        if (i <= width) {
            distTo[i] = energyMatrix[i - 1][0];
        }
        // if the vertex is rightmost
        else if (i % width == 0) {
            // directly above
            if (distTo[i] > distTo[i - width] + energyMatrix[width - 1]
                    [i / width - 1]) {
                distTo[i] = distTo[i - width] + energyMatrix[width - 1]
                        [i / width - 1];
                edgeTo[i] = i - width;
            }
            // to the left
            if (distTo[i] > distTo[i - width - 1] + energyMatrix[width - 1]
                    [i / width - 1]) {
                distTo[i] = distTo[i - width - 1] + energyMatrix[width - 1]
                        [i / width - 1];
                edgeTo[i] = i - width - 1;
            }
        }
        else {
            // directly above
            if (distTo[i] > distTo[i - width] +
                    energyMatrix[i % width - 1][i / width]) {
                distTo[i] = distTo[i - width] +
                        energyMatrix[i % width - 1][i / width];
                edgeTo[i] = i - width;
            }
            // above left if not the leftmost
            if (i % width != 1 && distTo[i] > distTo[i - width - 1] +
                    energyMatrix[i % width - 1][i / width]) {
                distTo[i] = distTo[i - width - 1] +
                        energyMatrix[i % width - 1][i / width];
                edgeTo[i] = i - width - 1;
            }
            // above right
            if (distTo[i] > distTo[i - width + 1] +
                    energyMatrix[i % width - 1][i / width]) {
                distTo[i] = distTo[i - width + 1] +
                        energyMatrix[i % width - 1][i / width];
                edgeTo[i] = i - width + 1;
            }
        }
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        double[][] energyMatrix = new double[width][height]; // W by H energy matrix

        // storing energies in the energy matrix
        // i for column; j for row
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                energyMatrix[i][j] = energy(i, j);
            }
        }

        int[] storeSeam = new int[height];
        double[] distTo = new double[width * height + 1];
        int[] edgeTo = new int[width * height + 1];
        // initializes each distTo value to infinity
        for (int v = 0; v < width * height + 1; v++)
            distTo[v] = Double.POSITIVE_INFINITY;

        // i = 0 is the virtual top
        distTo[0] = 0.0;
        // relax vertices in topological order
        for (int i = 1; i < width * height + 1; i++) {
            relax(i, distTo, edgeTo, energyMatrix);
        }

        double minDist = Double.POSITIVE_INFINITY;
        int champ = 0;
        // find the smallest path by finding the vertex in last row with smallest
        // distTo value
        for (int i = width * height; i > width * height - width; i--) {
            if (distTo[i] < minDist) {
                minDist = distTo[i];
                champ = i;
            }
        }

        // fill up the storeSeam array with row nos. of shortest paths using
        // edgeTo
        int current = champ;
        for (int i = storeSeam.length - 1; i >= 0; i--) {
            if (current % width == 0) {
                storeSeam[i] = width - 1;
            }
            else {
                storeSeam[i] = current % width - 1;
            }
            current = edgeTo[current];
        }

        return storeSeam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        // columns of replaceMatrix (rows of energyMatrix)
        transpose();
        removeVerticalSeam(seam);
        transpose();
    }

    // checks whether valid seam
    private void check(int[] seam) {
        for (int i = 0; i < seam.length - 1; i++) {
            if (Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException();
            }
            if (seam[i] > width) throw new IllegalArgumentException();
        }
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        // look at corner cases
        if (seam == null) throw new IllegalArgumentException();
        if (seam.length != height) throw new IllegalArgumentException();
        if (width == 1) throw new IllegalArgumentException();
        check(seam);
        Picture newSeam = new Picture(width - 1, height());
        // i is the row no.
        for (int i = 0; i < height(); i++) {
            if (seam[i] == 0) {
                for (int j = 0; j < newSeam.width(); j++) {
                    newSeam.setRGB(j, i, toSeam.getRGB(j + 1, i));
                }
            }
            else {
                // j represents column no.
                for (int j = 0; j < seam[i]; j++) {
                    newSeam.setRGB(j, i, toSeam.getRGB(j, i));
                }
                for (int j = seam[i]; j < newSeam.width(); j++) {
                    newSeam.setRGB(j, i, toSeam.getRGB(j + 1, i));
                }
            }
        }

        toSeam = newSeam;
        width = newSeam.width();
        height = newSeam.height();
    }

    //  unit testing (required)
    public static void main(String[] args) {

        Picture forSeam = new Picture(args[0]);
        SeamCarver test = new SeamCarver(forSeam);
        StdOut.println(test.energy(0, 0));
        StdOut.println(test.width());
        StdOut.println(test.height());
        StdOut.println(test.picture());
        int[] resultOne = test.findHorizontalSeam();
        for (int i = 0; i < resultOne.length; i++) {
            StdOut.println(resultOne[i]);
        }
        int[] resultTwo = test.findVerticalSeam();
        for (int i = 0; i < resultTwo.length; i++) {
            StdOut.println(resultTwo[i]);
        }
        test.removeVerticalSeam(resultTwo);
        test.removeHorizontalSeam(resultTwo);

    }
}
