/* *****************************************************************************
 *  Describe concisely your algorithm to find a horizontal (or vertical)
 *  seam.
 **************************************************************************** */
To find a vertical seam, I represented the picture as a DAG. I relaxed the
vertices starting from the virtual top vertex in row-major order (in topological
order). I use a distTo[] and edgeTo[] array to store the results of the algorithm.
Then, I compare the distTo values of the elements in the last row to find the
one that leads to the shortest path. I use the edgeTo array to access the vertices
that lead to this path and store and return the column indices in an array.
