package freak.rinterface.control;

import freak.core.control.Schedule;
import freak.module.searchspace.PointSet;
import freak.module.searchspace.logictree.DNFTree;
import freak.module.searchspace.logictree.Data;
import freak.module.searchspace.logictree.RData;
import freak.rinterface.model.RDoubleMatrix;
import freak.rinterface.model.RReturns;
import freak.rinterface.model.SDataFrame;
import freak.rinterface.model.ScheduleConfigurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RFreakTests {

    @BeforeEach
    public void beforeEach() {
        LogRegInterface.setRMode();
        RFlags.setUseCase(RFlags.R);
    }

    @Test
    public void testRobRegEvol() throws Exception {
        double[] values = { 42, 37, 37, 28, 18, 18, 19, 20, 15, 14, 14, 13, 11, 12,  8,  7,  8,  8,  9, 15, 15, 80, 80, 75, 62, 62, 62, 62, 62, 58, 58, 58, 58, 58, 58, 50, 50, 50, 50, 50, 56, 70, 27, 27, 25, 24, 22, 23, 24, 24, 23, 18, 18, 17, 18, 19, 18, 18, 19, 19, 20, 20, 20, 89, 88, 90, 87, 87, 87, 93, 93, 87, 80, 89, 88, 82, 93, 89, 86, 72, 79, 80, 82, 91 };
        int[] dim = {21, 4};
        RDoubleMatrix stackloss = new RDoubleMatrix(values, dim);
        PointSet.setPointsSetFromR(true);
        Schedule schedule = ScheduleConfigurator.getLTSSchedule(stackloss, 0, false, 1, 10000, 0, 0);
        LogRegInterface.rSetSchedule(schedule);
        RFreak.rMain(new String[] {""});
        SDataFrame returnedFrame = RReturns.getDataFrame();
        double crit = RReturns.getResidual();
        double[] coefficients = RReturns.getFittedHyperplane();
        int[] best = RReturns.getChosenIndices();
        Assertions.assertNotNull(returnedFrame);
        Assertions.assertNotNull(crit);
        Assertions.assertNotNull(coefficients);
        Assertions.assertNotNull(best);

    }

    @Test
    public void testGPASInteractions() throws Exception {
        int[] preds = { 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 2, 2, 0, 1, 2, 2, 0, 1, 1, 2, 0, 2, 2, 0, 2, 0, 2, 1, 2, 1, 1, 0, 1, 0, 1, 2, 0, 1, 2, 0, 0, 0, 2, 0, 2, 1, 1, 1, 1, 0, 0, 0, 2, 2, 1, 2, 0, 0, 2, 1, 1, 1, 2, 1, 0, 2, 1, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 0, 1, 1, 0, 1, 2, 2, 2, 0, 1, 0, 2, 1, 0, 1, 2, 0, 2, 1, 0, 2, 1, 1, 1, 1, 0, 2, 2, 0, 0, 2, 1};
        int[] dim = {10, 11};
        String[] columnNames = {"FK", "SNP1", "SNP2", "SNP3", "SNP4", "SNP5", "SNP6", "SNP7", "SNP8", "SNP9", "SNP10"};
        RData trainingData = new RData(preds,dim, columnNames);
        Data.setTrainingData(trainingData);
        Data.setRData(trainingData);
        Data.setRMode();
        ScheduleConfigurator.setInteractionR(1,1000,"test.dot",10,0.1);
        Schedule schedule = ScheduleConfigurator.getCurrentSchedule();
        LogRegInterface.rSetSchedule(schedule);
        RFreak.rMain(new String[] {""});
        SDataFrame returnedFrame = RReturns.getDataFrame();
        DNFTree[] returnedTrees = RReturns.getAllTrees();
        Data.clear();
    }

    @Test
    public void testDiscriminationR() throws Exception {
        int[] preds = { 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 2, 2, 0, 1, 2, 2, 0, 1, 1, 2, 0, 2, 2, 0, 2, 0, 2, 1, 2, 1, 1, 0, 1, 0, 1, 2, 0, 1, 2, 0, 0, 0, 2, 0, 2, 1, 1, 1, 1, 0, 0, 0, 2, 2, 1, 2, 0, 0, 2, 1, 1, 1, 2, 1, 0, 2, 1, 2, 0, 0, 0, 2, 0, 2, 0, 2, 0, 0, 1, 1, 0, 1, 2, 2, 2, 0, 1, 0, 2, 1, 0, 1, 2, 0, 2, 1, 0, 2, 1, 1, 1, 1, 0, 2, 2, 0, 0, 2, 1};
        int[] dim = {10, 11};
        String[] columnNames = {"FK", "SNP1", "SNP2", "SNP3", "SNP4", "SNP5", "SNP6", "SNP7", "SNP8", "SNP9", "SNP10"};
        RData trainingData = new RData(preds,dim, columnNames);
        Data.setTrainingData(trainingData);
        Data.setRData(trainingData);
        Data.setRMode();
        ScheduleConfigurator.setDiscriminationR(1,10000);
        Schedule schedule = ScheduleConfigurator.getCurrentSchedule();
        LogRegInterface.rSetSchedule(schedule);
        RFreak.rMain(new String[] {""});
        SDataFrame returnedFrame = RReturns.getDataFrame();
        DNFTree[] returnedTrees = RReturns.getAllTrees();
        Data.clear();
    }
}
