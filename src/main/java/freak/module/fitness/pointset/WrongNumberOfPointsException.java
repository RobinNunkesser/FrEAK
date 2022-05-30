package freak.module.fitness.pointset;

@SuppressWarnings("serial")
public class WrongNumberOfPointsException extends Exception{
	public WrongNumberOfPointsException(int pErrorFitnessValue){
		this.errorFitnessValue = pErrorFitnessValue;
	}
	protected int errorFitnessValue;
	public int getErrorFitnessValue() {
		return errorFitnessValue;
	}
	
}