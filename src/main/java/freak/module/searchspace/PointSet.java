/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */
package freak.module.searchspace;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import edu.cornell.lassp.houle.RngPack.RandomElement;
import freak.Freak;
import freak.core.control.Schedule;
import freak.core.modulesupport.ClassCollector;
import freak.core.modulesupport.Configurable;
import freak.core.modulesupport.inspector.StringArrayWrapper;
import freak.core.population.Genotype;
import freak.core.searchspace.AbstractSearchSpace;
import freak.core.searchspace.HasDimension;
import freak.module.fitness.pointset.LtSFile;

/**
 * @author ruthe
 *
 */
public class PointSet extends AbstractSearchSpace implements Configurable, HasDimension {

    public static class Point implements Serializable {

        /* The dimension of one single point */
        private int dimension;

        /* The values of this specific point */
        private double[] komponents = null;

        /* This should be the standard constructor */
        public Point(int dimension) {
            this.komponents = new double[dimension];
            this.dimension = dimension;
        }

        public Point(int dimension, RandomElement re) {
            this.komponents = new double[dimension];
            this.dimension = dimension;
            for (int i = 0; i < dimension; i++) {
                komponents[i] = re.gaussian(42);
            }
        }

        public Point(double komponents[]) {
            this.komponents = new double[komponents.length];
            this.dimension = komponents.length;
            for (int i = 0; i < dimension; i++) {
                this.komponents[i] = komponents[i];
            }
        }

        public Point(Collection<Double> komponents) {
            this.komponents = new double[komponents.size()];
            this.dimension = komponents.size();
            int i = 0;
            for (Iterator<Double> it = komponents.iterator(); it.hasNext();) {
                this.komponents[i] = it.next();
                i++;
            }
        }

        /*If the other object is not a point this method will retun false 
         * otherwise it will retun true if all komponents are the same 
         * */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point po = (Point) o;
            if (po.dimension != this.dimension) return false;
            for (int i = 0; i < dimension; i++) {
                if (po.komponents[i] != this.komponents[i]) return false;
            }
            return true;
        }

        public double getK(int i) {
            return komponents[i];
        }

        public void setK(int i, double v) {
            komponents[i] = v;
        }

        public int getDimension() {
            return dimension;
        }

        @Override
        public String toString() {
            String result = "(";
            for (int i = 0; i < dimension - 1; i++) {
                result = result + komponents[i] + " ";
            }
            result = result + komponents[dimension - 1] + ")";
            return result;
        }
    }

    private static class CommonFileFilter implements FileFilter {

        String endtag;

        public CommonFileFilter(String endTag) {
            this.endtag = endTag;
        }

        public boolean accept(File file) {
            return file.getName().endsWith(endtag);
        }
    }

    class CommonFileInfo implements Comparable, Serializable {

        File file;
        String name;

        public CommonFileInfo(File f, int hide) {
            String tspName = f.getName();
            name = tspName.substring(0, tspName.length() - 4).toUpperCase();
            file = f;
        }

        public int compareTo(Object o) {
            return name.compareTo(((CommonFileInfo) o).name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /* (non-Javadoc)
     * @see freak.core.searchspace.SearchSpace#getRandomGenotype()
     */
    String ltsFileListCache[] = null;
    Object[] ltsFileArrayCache = null;
    int m_zLtSChoice = 0;

    public static boolean pointsSetFromR = false;

    private int dimension;
    private int pointDimension;
    protected Point[] m_hPoints;
    private boolean m_zRandomPoints = true;
    //	private int h;
    RandomElement re;

    public PointSet(Schedule schedule) {
        super(schedule);
        dimension = 4;
        pointDimension = 2;
        //		h = 3;
        /** ###TODO
         *  Die Punkte sollten in einer zweiten Iteration nicht mehr zufällig erstellt werden
         *  sondern per Datei eingelesen werden **/
        re = schedule.getRandomElement();
        initRandomPoints();

    }

    private void initRandomPoints() {
        m_hPoints = new Point[dimension];

        for (int i = 0; i < dimension; i++) {
            m_hPoints[i] = new Point(pointDimension, re);
        }

    }

    public void setPoints(Point[] points) {
        m_hPoints = points;
        if (points != null) {
            pointDimension = points[0].getDimension();
            dimension = points.length;
            schedule.callInitialize();
        }
    }

    //	public int getH(){return h;}

    public Genotype getRandomGenotype() {
        // TODO Auto-generated method stub
        RandomElement re = schedule.getRandomElement();

        boolean[] selection = new boolean[dimension];
        for (int i = 0; i < dimension; i++) {
            if (re.choose(2) == 1) selection[i] = true;
        }
        return new PointSetGenotype(m_hPoints, selection);//,h);
    }

    public int getDimension() {
        return dimension;
    }

    /*public int getPointDimension() {
    	return pointDimension;
    }*/

    public Point[] getPoints() {
        return m_hPoints;
    }

    /*	private int computeH(){
    		double alpha = 0.5;
    		return (int)(2.0*(int)(((double)(dimension+pointDimension+1))/2.0)-((double)dimension)+2.0*alpha*(((double)dimension)-(int)(((double)(dimension+pointDimension+1))/2.0)));
    	}
    	
    	public void setPropertyDimension(Integer dim){
    		if (dim.intValue() > 0)
    			dimension = dim.intValue();
    //		h = computeH();  
    		initRandomPoints();
    	}
    	
    	
    	
    	public void setPropertyPointDimension(Integer dim){
    		if (dim.intValue() > 0)
    			pointDimension = dim.intValue();
    //		h = computeH();
    		initRandomPoints();
    	}*/

    private String[] createLTSFileList() {

        if (ltsFileListCache != null) return ltsFileListCache;
        String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        String startedFrom = ClassCollector.getStartedFrom();

        // if started from the jar file
        // NEW CHECK
        if (startedFrom.toLowerCase().endsWith(".jar")) {
            String resDir = startedFrom.substring(0, startedFrom.lastIndexOf("/") + 1) + "resource";
            classpaths = new String[] { resDir };
        }
        Collection auswahl = new LinkedList();
        for (int cp = 0; cp < classpaths.length; cp++) {
            File ltsFolder = new File(classpaths[cp] + "/freak/module/fitness/pointset");
            if (ltsFolder.exists()) {
                File[] files = ltsFolder.listFiles(new CommonFileFilter(".csv"));
                for (int i = 0; i < files.length; i++) {
                    auswahl.add(new CommonFileInfo(files[i], 4));
                }
            }
        }
        Object[] o = auswahl.toArray();
        Arrays.sort(o);
        ltsFileArrayCache = o;
        LtSFile lts = null;
        try {
            lts = new LtSFile(((CommonFileInfo) ltsFileArrayCache[m_zLtSChoice]).file);
        } catch (Exception e) {
            System.out.println(e);
        }
        this.setRandomPoints(false);

        if (!pointsSetFromR) {
            pointDimension = lts.getPointDimension();
            dimension = lts.getDimension();
            m_hPoints = lts.getPointData();
            // notify the schedule that the search space dimension has been
            // changed
            schedule.callInitialize();
        }

        String[] result = new String[o.length];//+1];
        //result[0] = "Random Point Values";
        for (int i = 0; i < o.length; i++) {
            //result[i+1] = o[i].toString();
            result[i] = o[i].toString();
        }
        ltsFileListCache = result;
        return result;

    }

    public synchronized StringArrayWrapper getPropertyPoints() {
        return new StringArrayWrapper(createLTSFileList(), m_zLtSChoice);
    }

    public synchronized void setPropertyPoints(StringArrayWrapper wrapper) {
        // TODO fertigstellen
        int i = wrapper.getIndex();
        // not random 2d points
        LtSFile lts;
        /*		if (i == 0) {
        			lts = null;
        			this.setRandomPoints(true);
        		} else {*/
        try {
            lts = new LtSFile(((CommonFileInfo) ltsFileArrayCache[i]).file);
            //				lts = new LtSFile(((CommonFileInfo)ltsFileArrayCache[i - 1]).file);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        this.setRandomPoints(false);

        if (!pointsSetFromR) {
            pointDimension = lts.getPointDimension();
            dimension = lts.getDimension();
            m_hPoints = lts.getPointData();

            // notify the schedule that the search space dimension has been
            // changed
            schedule.callInitialize();
        }

        //}
        m_zLtSChoice = i;
        //schedule.callInitialize(); // !!!!!!

    }

    public String getShortDescriptionForPoints() {
        return "Data";
    }

    /*	public String getShortDescriptionForH() {
    		return "h";
    	}

    	public String getLongDescriptionForH() {
    		return "Size of the subset of points.";
    	}

    	
    	public Integer getPropertyH(){
    		return new Integer(h);
    	}
    	
    	public void setPropertyH(Integer H){
    		if (H.intValue() > 0)
    			h = H.intValue();
    	}
    	
    	public Integer getPropertyPointDimension(){
    		return new Integer(pointDimension);
    	}*/

    public void setRandomPoints(boolean random) {
        m_zRandomPoints = random;
    }

    /*	public Integer getPropertyDimension(){
    		return new Integer(dimension);
    	}*/

    /* (non-Javadoc)
     * @see freak.core.searchspace.SearchSpace#getSize()
     */
    public double getSize() {
        // TODO Auto-generated method stub
        return Math.pow(2, dimension);
    }

    /* (non-Javadoc)
     * @see freak.core.modulesupport.Module#getDescription()
     */
    public String getDescription() {
        // TODO Auto-generated method stub
        return "The search space of all subsets of a given set of points in R^d. \nThis searchspace has to be mapped to \"Bit String\" in step 3 (\"Select Genotype-Mapper\"), because it has no seperate adaption operators.Ê";
    }

    /*	public String getShortDescriptionForDimension() {
    		return "n";
    	}

    	public String getLongDescriptionForDimension() {
    		return "Number of observations.";
    	}
    		
    	public String getShortDescriptionForPointDimension() {
    		return "d";
    	}

    	public String getLongDescriptionForPointDimension() {
    		return "Dimension of the observations.";
    	}*/

    /* (non-Javadoc)
     * @see freak.core.modulesupport.Module#getName()
     */
    public String getName() {
        return "Subsets of Points";
    }

    /**
     * @return the pointDimension
     */
    public int getPointDimension() {
        return pointDimension;
    }

    /**
     * @param pointsSetFromR the pointsSetFromR to set
     */
    public static void setPointsSetFromR(boolean pointsSetFromR) {
        PointSet.pointsSetFromR = pointsSetFromR;
    }

}
