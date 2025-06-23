package oo.com.iseu.UI.Basic;

import java.io.Serializable;

public class CriterionInformation implements Serializable{
	public String filePath;  //���������·�������������������·����ͬ����׼��ƥ�����
	public int lineNumber;
	public SliceDirection direction;
	public CriterionInformation(String filePath, int lineNumber, SliceDirection direction) {
		super();
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.direction = direction;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + lineNumber;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CriterionInformation other = (CriterionInformation) obj;
		if (direction != other.direction)
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
	
	
}
