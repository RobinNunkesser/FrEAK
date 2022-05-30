/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a modification of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 * Last modification: 06/28/2007
 */

package freak.core.control;

import java.io.*;

/**
 * This class represents all data stored in a saved file. The read and write methods encapsulate the file format.
 * @author  Stefan
 */
public class FreakFile implements Serializable {
	Replay replay;
	Schedule activeSchedule;

	public FreakFile(Replay replay, Schedule activeSchedule) {
		this.replay = replay;
		this.activeSchedule = activeSchedule;
	}

	public void write(OutputStream s) throws IOException {
		new ObjectOutputStream(s).writeObject(this);
	}

	public static FreakFile read(InputStream s) throws IOException, ClassNotFoundException {
		return (FreakFile)new ObjectInputStream(s).readObject();
	}
}
