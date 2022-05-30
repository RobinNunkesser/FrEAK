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
 */

/*
 * KOptimal.java
 * <BR>My in place k optimal solver.
 * The permuter algorithm came from 
 * http://www.geocities.com/permute_it/01example.html
 *
 * Created on May 13, 2004, 11:52 PM
 *
 * @author  Chris Coakley
 */

package edu.ucsb.cs.jicos.applications.branchandbound.tsp.utilities.upperbounds.cycle;


public class KOptimal
{
    private static final String IMPORTANT = "This is zero based";
    private int max;
    private int k;
    private int elems[];
    private int perms[];
    private int swapfrom;
    private int swapto;
    private boolean inited;
    
    /** Creates a new instance of KOptimal */
    public KOptimal(int k, int numElements)
    {
        System.out.println("KOptimal.KOptimal: " + k + ", " + numElements);
        this.k = k;
        max = numElements-1;
        elems = new int[k];
        perms = new int[k+1];
        reInit();
    }
    
    public void reInit()
    {
        inited = false;
        for (int i=0; i<k; i++)
        {
            elems[i] = i;
        }        
        initPerms();
    }
    
    private void initPerms() {
        for (int i=0; i<perms.length; ++i) {
            // keep this zero based even if the above switches to 1 based.
            perms[i] = i;
        }
        swapfrom = 1;
    }
    
    public boolean isNext()
    {
        initPerms();
        java.util.Arrays.sort(elems);
        // if k = 3, max = 20, elems[0] maxes out at 18 (elems[1] = 19, elems[2] = 20)
        return !(elems[0] == max-k+1);
    }
    
    public int[] getPerm() {
        int[] vals = elems; // alias in case I want to do this another way later
        perms[swapfrom]--;   // decrease index weight by 1
        swapto = swapfrom % 2 * perms[swapfrom]; // to = 0 if from is even, else tp = perms[from]
        // Swap (from,to)
        int temp = elems[swapfrom];
        vals[swapfrom] = vals[swapto];
        vals[swapto] = temp;
        
        return vals;
    }
    
    public boolean hasPermutations() {
        swapfrom = 1;
        while (perms[swapfrom] == 0) {
            perms[swapfrom] = swapfrom;
            swapfrom++;
        }
        if (swapfrom < k) {
            return true;
        }
        else {
            initPerms();
            java.util.Arrays.sort(elems);
            return false;
        }
    }
    
    private boolean init()
    {
        if (inited)
        {
            return false;
        }
        else
        {
            inited = true;
            return true;
        }
    }
    
    public int[] getNextElemsIWontTouchIPromise()
    {
        if (init()) return elems;
        
        if (!isNext()) return new int[0];
        int i;
        for (i=k-1; i>=0; i--)
        {
            // if the second (i=1) item is nineteen and the max is twenty and k = 3, reset
            if (elems[i] == (max - k + i + 1))
            { // 19 == 20 - 3 + 1 + 1
                // this is an error if the guy behind me is at his max
                elems[i] = elems[i-1]+2;
            }
            else
            {
                elems[i] += 1;
                break;
            }
        }
        
        // this fixes the above error
        resetForward(i);
        
        return elems;
    }
    
    private void resetForward(int last)
    {
        // element last changed, reset the elements to the end
        for (int i = last + 1; i < k; i++)
        {
            elems[i] = elems[i-1] + 1;
        }
    }
    
    public int[] getNextElems()
    {
        int noret[] = getNextElemsIWontTouchIPromise();
        int ret[] = new int[noret.length];
        System.arraycopy(noret,0,ret,0,noret.length);
        return ret;
    }
    
//    private static void display(int[] vals, int i, int j) {
//        for (int c = 0; c < vals.length; c++) {
//            System.out.print(vals[c] + " ");
//        }
//        System.out.println("  swapped(" + i + ", " +j+ ")");
//    }    
//    public static void main(String args[]) {
//        int n = 5;
//        int vals[] = new int[n];
//        int p[] = new int[n+1];
//        int i,j,temp;
//        for (int c = 0; c < vals.length; c++) {
//            vals[c] = c+1;
//            p[c] = c;
//        }
//        p[p.length-1] = p.length-1;
//        
//        i=1;
//        while(i < n) {
//            p[i]--;
//            j = i % 2 * p[i];
//            temp = vals[j];
//            vals[j] = vals[i];
//            vals[i] = temp;
//            display(vals,j,i);
//            i=1;
//            while (p[i]==0) {
//                p[i] = i;
//                i++;
//            }            
//        }
//    }
    
}
