package oo.com.iseu.CSlicer;

import java.util.ArrayList;

import org.neo4j.cypher.internal.compiler.v2_2.helpers.StringRenderingSupport;

import oo.com.iseu.UI.Basic.SliceDirection;

public class SliceCriterion {
	public String filePath;
	public int lineNumber;
	public ArrayList<String> interestVars;
	public String traceFilePath;
	public SliceDirection sliceDirection;
	public SliceCriterion(String filePath, int lineNumber, ArrayList<String> interestVars,SliceDirection sliceDirection) {
		super();
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.interestVars = interestVars;
		this.sliceDirection = sliceDirection;
	}
	public SliceCriterion(String filePath, int lineNumber, ArrayList<String> interestVars, String traceFilePath,SliceDirection sliceDirection) {
		super();
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.interestVars = interestVars;
		this.traceFilePath=traceFilePath;
		this.sliceDirection = sliceDirection;
	}
}
