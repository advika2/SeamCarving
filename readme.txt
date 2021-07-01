Description - implements seam carving, "a content-aware image resizing
technique where the image is reduced in size by one pixel of height (
or width) at a time".

SeamCarver.java contains code written by me that implements the seam carving algorithm.

To test SeamCarver.java-:
ResizeDemo.java can be used for testing the code using one of the two images
given as input (details of the input required to run the program are on the header)

algs4.jar is the library provided to me to do this project

/* *****************************************************************************
 *  Describing algorithm to find a horizontal (or vertical)
 *  seam.
 **************************************************************************** */
To find a vertical seam, I represented the picture as a DAG. I relaxed the
vertices starting from the virtual top vertex in row-major order (in topological
order). I use a distTo[] and edgeTo[] array to store the results of the algorithm.
Then, I compare the distTo values of the elements in the last row to find the
one that leads to the shortest path. I use the edgeTo array to access the vertices
that lead to this path and store and return the column indices in an array.
