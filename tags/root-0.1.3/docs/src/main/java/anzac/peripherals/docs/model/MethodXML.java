package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sun.javadoc.Tag;

public class MethodXML implements Comparable<MethodXML> {
	private String name;
	private Tag[] description;
	private String returnType;
	private final List<ParameterXML> parameters = new ArrayList<ParameterXML>();
	private String returnDescription;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Tag[] getDescription() {
		return description;
	}

	public void setDescription(final Tag[] description) {
		this.description = description;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(final String returnType) {
		this.returnType = returnType;
	}

	public List<ParameterXML> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		final String parameters = StringUtils.join(getParameters(), ",");
		return name + "(" + parameters + ")";
	}

	public String getReturnDescription() {
		return returnDescription == null ? "" : returnDescription;
	}

	public void setReturnDescription(String returnDescription) {
		this.returnDescription = returnDescription;
	}

	@Override
	public int compareTo(MethodXML that) {
		int compareTo = this.name.compareTo(that.name);
		if (compareTo > 0) {
			return 1;
		} else if (compareTo < 0) {
			return -1;
		}
		if (this.parameters.size() < that.parameters.size()) {
			return -1;
		} else if (this.parameters.size() > that.parameters.size()) {
			return 1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
		MethodXML other = (MethodXML) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}
}
