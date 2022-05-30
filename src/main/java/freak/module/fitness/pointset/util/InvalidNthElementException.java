package freak.module.fitness.pointset.util;


public class InvalidNthElementException extends Exception{
	int bounds[] = new int [2];
	int nthElement;
	public InvalidNthElementException(int nthElement, int low, int high){
		bounds[0] = low; bounds[1] = high;
		this.nthElement = nthElement;
	} 
	public String getMessage() {
		// TODO Auto-generated method stub
		return super.getMessage() +  "\nInvalid Nth Element:" + this.nthElement + " Index should be between:" + bounds[0] + "(inclusive) and:" + bounds[1] + "(exclusive)";
	}
}
