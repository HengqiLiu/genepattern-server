package org.genepattern.data.matrix;

import java.util.*;

/**
 * A one-dimensional matrix used to hold class assignment information.
 * 
 * @author Joshua Gould
 */
public class ClassVector {
	Map classNumber2IndicesMap;

	int[] assignments;

	Map classNumber2LabelMap;

	int classCount;

	/**
	 * Constructs a new class vector from the array of class assignments
	 * 
	 * @param x
	 *            the class assignments
	 */
	public ClassVector(String[] x) {
		this.assignments = new int[x.length];
		this.classNumber2IndicesMap = new HashMap();
		this.classNumber2LabelMap = new HashMap();
		int maxClassNumber = 0;
		Map className2ClassNumberMap = new HashMap();
		for (int i = 0; i < x.length; i++) {
			Integer classNumberInteger = (Integer) className2ClassNumberMap
					.get(x[i]);
			if (classNumberInteger == null) {
				classNumberInteger = new Integer(maxClassNumber++);
				className2ClassNumberMap.put(x[i], classNumberInteger);
				classNumber2IndicesMap.put(classNumberInteger, new ArrayList());
				classNumber2LabelMap.put(classNumberInteger, x[i]);
			}
			assignments[i] = classNumberInteger.intValue();
			List indices = (List) this.classNumber2IndicesMap
					.get(classNumberInteger);
			indices.add(new Integer(i));
		}
		this.classCount = maxClassNumber;
	}

	private ClassVector(Map classNumber2IndicesMap, int[] assignments,
			Map classNumber2LabelMap, int classCount) {
		this.classNumber2IndicesMap = classNumber2IndicesMap;
		this.assignments = assignments;
		this.classNumber2LabelMap = classNumber2LabelMap;
		this.classCount = classCount;
	}

	/**
	 * Constructs a new class vector that is the union of this class vector with
	 * the given class vector
	 * 
	 * @param classVector
	 *            the class vector
	 * @return the union
	 */
	public ClassVector union(ClassVector classVector) {
		int[][] lookup = new int[getClassCount()][classVector.getClassCount()];
		int classNumber = 0;
		for (int i = 0; i < getClassCount(); i++) {
			for (int j = 0; j < classVector.getClassCount(); j++) {
				lookup[i][j] = classNumber++;
			}
		}
		String[] assignments = new String[size()];
		for (int i = 0; i < size(); i++) {
			assignments[i] = "Class "
					+ lookup[getAssignment(i)][classVector.getAssignment(i)];
		}
		return new ClassVector(assignments);
	}

	/**
	 * Gets the number of assignments in this class vector
	 * 
	 * @return the number of assignments
	 */
	public int size() {
		return assignments.length;
	}

	public ClassVector slice(int[] order) {
		int[] newAssignments = new int[order.length];
		Map newClassNumber2IndicesMap = new HashMap();
		Map newClassNumber2LabelMap = new HashMap();
		int numClasses = 0;
		for (int i = 0, length = order.length; i < length; i++) {
			newAssignments[i] = this.assignments[order[i]];
			List newIndices = (List) newClassNumber2IndicesMap.get(new Integer(
					newAssignments[i]));
			if (newIndices == null) {
				newIndices = new ArrayList();
				newClassNumber2IndicesMap.put(new Integer(newAssignments[i]),
						newIndices);
				numClasses++;
				newClassNumber2LabelMap.put(new Integer(newAssignments[i]),
						getClassName(this.assignments[order[i]]));
			}
			newIndices.add(new Integer(i));
		}
		return new ClassVector(newClassNumber2IndicesMap, newAssignments,
				newClassNumber2LabelMap, numClasses);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, length = assignments.length; i < length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(assignments[i]);
		}
		return sb.toString();
	}

	/**
	 * Gets the number of different values taken by the class assignments.
	 * 
	 * @return The number of classes.
	 */
	public int getClassCount() {
		return classCount;
	}

	/**
	 * Gets the class name for the specified class number
	 * 
	 * @param classNumber
	 *            The class number
	 * @return The class name.
	 */
	public String getClassName(int classNumber) {
		return (String) classNumber2LabelMap.get(new Integer(classNumber));
	}

	/**
	 * Gets the class assignment
	 * 
	 * @param index
	 *            The index
	 * @return The assignment
	 */
	public int getAssignment(int index) {
		return assignments[index];
	}

	/**
	 * Allocates a new array containing the class assignments
	 * 
	 * @return The assignments
	 */
	public int[] getAssignments() {
		return (int[]) assignments.clone();
	}

	/**
	 * Gets the indices in the assignments array that have the specified class
	 * number.
	 * 
	 * @param classNumber
	 *            The class number
	 * @return The indices
	 */
	public int[] getIndices(int classNumber) {
		List indices = (List) classNumber2IndicesMap.get(new Integer(
				classNumber));
		if (indices == null) {
			return new int[0];
		}
		int[] _indices = new int[indices.size()];
		for (int i = 0, length = _indices.length; i < length; i++) {
			_indices[i] = (((Integer) indices.get(i))).intValue();
		}

		return _indices;
	}

}

