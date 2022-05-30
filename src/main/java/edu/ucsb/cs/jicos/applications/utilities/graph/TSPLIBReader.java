/* ************************************************************************* *
 *                                                                           *
 *        Copyright (c) 2004 Peter Cappello  <cappello@cs.ucsb.edu>          *
 *                                                                           *
 *    Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the          *
 *  "Software"), to deal in the Software without restriction, including      *
 *  without limitation the rights to use, copy, modify, merge, publish,      *
 *  distribute, sublicense, and/or sell copies of the Software, and to       *
 *  permit persons to whom the Software is furnished to do so, subject to    *
 *  the following conditions:                                                *
 *                                                                           *
 *    The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.          *
 *                                                                           *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF       *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.   *
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY     *
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,     *
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE        *
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                   *
 *                                                                           *
 * ************************************************************************* */

/**
 * Created on May 30, 2004, 3:25 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.utilities.graph;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
 
FROM THE TSPLIB FAQ:
 
Q: I get wrong distances for problems of type GEO.

A: There has been some confusion of how to compute the distances. I use the following code.

For converting coordinate input to longitude and latitude in radian:

  PI = 3.141592;

  deg = (int) x[i];
  min = x[i]- deg;
  rad = PI * (deg + 5.0 * min/ 3.0) / 180.0;
 
For computing the geographical distance:

 RRR = 6378.388;

 q1 = cos( longitude[i] - longitude[j] );
 q2 = cos( latitude[i] - latitude[j] );
 q3 = cos( latitude[i] + latitude[j] );
 dij = (int) ( RRR * acos( 0.5*((1.0+q1)*q2 - (1.0-q1)*q3) ) + 1.0); 

Q: What does the function nint() do?

A: The function "int nint (double x)" converts x into int format rounding 
 to the nearest int value, except halfway cases are rounded to the int value 
 larger in magnitude. This corresponds to the Fortran generic intrinsic 
 function nint. But, rounding like "(int) (x+0.5)" should give the same results 
 for TSPLIB problems. 

*/


public class TSPLIBReader {
    
    /** Creates a new instance of TSPLIBReader */
    public TSPLIBReader() {
    }
    
    public static Graph parse(File f) {
        FileReader in = null;        
        try {
            in = new FileReader(f);
        } 
        catch (Exception ignore) { 
            ignore.printStackTrace(); 
            System.exit(1); 
        }
        BufferedReader bin;
        bin = new BufferedReader(in);
//        String input;
//        try {
//            while ((input = bin.readLine()) != null) {
//                System.out.println(input);
//            }
//        }
//        catch (Exception ignore) {
//            ignore.printStackTrace();
//            System.exit(1);
//        }
        return parseHeader(bin);
//        return null;
    }
    
    private static Graph parseHeader(BufferedReader bin) {
        String name = null;
        String comment = null;
        String type = null;
        String display = null;
        int metric = 0;
        int dimension = 0;
        
        Matcher m;
        Pattern pName = Pattern.compile("NAME");
        Pattern pComment = Pattern.compile("COMMENT");
        Pattern pType = Pattern.compile("TYPE");
        Pattern pDimension = Pattern.compile("DIMENSION");
        Pattern pMetric = Pattern.compile("EDGE_WEIGHT_TYPE");
        Pattern pBeginGraph = Pattern.compile("NODE_COORD_SECTION");
        Pattern pDisplay = Pattern.compile("DISPLAY_DATA_TYPE");

        // not yet implemented matches
        Pattern pEdgeWeightFormat = Pattern.compile("EDGE_WEIGHT_FORMAT");
        // Metrics
        Pattern pGeoMetric = Pattern.compile("GEO");
        Pattern pEuc2DMetric = Pattern.compile("EUC_2D");
        Pattern pExplicitMetric = Pattern.compile("EXPLICIT"); // Not yet implemented
        
        String input;
        try {
            while ((input = bin.readLine()) != null) {
                if ((m = pName.matcher(input)).find()) {
                    name = (input.split(":"))[1].trim() ;
                    continue;
                }
                else if ((m = pComment.matcher(input)).find()) {
                    comment = (input.split(":"))[1].trim();
                    continue;
                }
                else if ((m = pDimension.matcher(input)).find()) {
                    dimension = Integer.parseInt((input.split(":"))[1].trim());
                    continue;
                }
                // must parse display before type (regular expression for type succeeds
                else if ((m = pDisplay.matcher(input)).find()) {
                    display = (input.split(":"))[1].trim();
                    continue;
                }
                else if ((m = pMetric.matcher(input)).find()) {
                    if ((m = pGeoMetric.matcher(input)).find()) {
                        metric = Graph.METRIC_GEO;
                    }
                    else if ((m = pEuc2DMetric.matcher(input)).find()) {
                        metric = Graph.METRIC_EUC_2D;                        
                    }
                    else if ((m = pExplicitMetric.matcher(input)).find()) {
                        System.err.println("I don't yet do explicit edge representation");
                        return null;
//                        System.exit(1);
                    }
                    continue;
                }
                else if ((m = pType.matcher(input)).find()) {
                    if (!(type = (input.split(":"))[1].trim()).startsWith("TSP"))  {
                        System.err.println("I don't know how to parse files of type: " + type);
                        return null;
                        // System.exit(1);
                    }
                    continue;
                }
                else if ((m = pBeginGraph.matcher(input)).find()) {
                    return parseGraph(new GraphEuclidean(dimension,metric,name,comment), bin);
                }
            }
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
            System.err.println("continuing from exception");
            return null;
//            System.exit(1);
        }
        System.err.println("Problem in parseHeader");
        return null;
    }
    
    private static Graph parseGraph(GraphEuclidean g, BufferedReader bin) {
        String input = null;
        Matcher m = null;
        Pattern pEnd = Pattern.compile("(DISPLAY_DATA_SECTION)|(EOF)");
        try {
            while ((input = bin.readLine()) != null) {
                if ((m = pEnd.matcher(input)).find()) {
                    return g ;
                }
                else {
                    String strings[] = null;
                    input = input.trim();
                    strings = input.split("\\s+");
                    if (g.getMetric() == GraphEuclidean.METRIC_EUC_2D) {
                      g.setVertex(Integer.parseInt(strings[0])-1, new Vertex(Float.parseFloat(strings[1].trim()),Float.parseFloat(strings[2].trim())));
                    } else {
                      g.setVertex(Integer.parseInt(strings[0])-1, new Vertex(deg2rad(Float.parseFloat(strings[1])),deg2rad(Float.parseFloat(strings[2]))));
                    }
                    
                }                
            }
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
            System.err.println("Continuing from exception");
            return null;
//            System.exit(1);
        }
        System.err.println("Problem in parseGraph");
        return null;
    }
    
    private static float deg2rad(float degrees) {
      float pi = 3.141592f; // precision specified by tsplib
      int deg = (int)degrees;
      float min = (int)degrees - deg;
      return pi * (deg +0.5f * min / 3.0f)/180.0f;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("BEGIN");
        Graph g = parse(new File("/home/ccoakley/research/tsp/tsplib/eil101.tsp"));
        System.out.println("PARSED");
        System.out.println(g);
        System.out.println("DONE");
    }
    
}
