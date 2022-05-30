/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 */

package freak.rinterface.control;

/*
 * This class attempts to unify some of the flags by reducing them to the main use cases.
 */
public class RFlags {
	
	public static final int NORMAL = 0;
	public static final int QUICK_GPAS = 1;
	public static final int R = 2;

	private static int useCase=RFlags.NORMAL;

	/**
	 * @return the useCase
	 */
	public static int getUseCase() {
		return useCase;
	}

	/**
	 * @param useCase the useCase to set
	 */
	public static void setUseCase(int useCase) {
		RFlags.useCase = useCase;
	}
}
