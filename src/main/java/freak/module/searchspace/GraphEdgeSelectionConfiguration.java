/*
 * This file is part of RFrEAK. For licensing and copyright information
 * please see the file COPYING in the root directory of this
 * distribution or contact <robin.nunkesser@udo.edu>.
 * 
 * This file is a copy of the original file distributed with
 * FrEAK (http://sourceforge.net/projects/freak427/).
 */

package freak.module.searchspace;

import freak.core.modulesupport.Configuration;
import freak.core.modulesupport.inspector.StringArrayWrapper;


public class GraphEdgeSelectionConfiguration extends Configuration {
	public String getDescription() {
		PropertyWrapper type = (PropertyWrapper) simpleConfigs.get("InitializationType");
		PropertyWrapper vertices = (PropertyWrapper) simpleConfigs.get("NumberOfVertices");
		PropertyWrapper edges = (PropertyWrapper) simpleConfigs.get("NumberOfEdges");
		PropertyWrapper keep = (PropertyWrapper) simpleConfigs.get("KeepGraphDuringBatch");
		PropertyWrapper vit = (PropertyWrapper) simpleConfigs.get("VerticesInTriangles");
		
		int t = ((StringArrayWrapper)type.value).getIndex();
		if (t == GraphEdgeSelection.INGOS_GRAPH) {
			int triangles = (((Integer)vit.value).intValue() - 1) / 2;
			return "Vertices: " + vertices.value + ", Triangles: " + triangles;
		} else if (t == GraphEdgeSelection.RANDOM_FIXED_EDGES) {
			return "Vertices: " + vertices.value + ", Edges: " + edges.value + (((Boolean)keep.value).booleanValue() ? ", Keep" : ", Reroll");	
		} else {
			return super.getDescription();
		}
		
	}
}
