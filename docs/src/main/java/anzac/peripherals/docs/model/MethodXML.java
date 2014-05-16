package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class MethodXML implements Comparable<MethodXML> {
	private String name;
	private Tag[] description;
	private Tag[] firstLine;
	private String returnType;
	private final List<ParameterXML> parameters = new ArrayList<ParameterXML>();
	private Tag[] returnDescription;

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

	public Tag[] getFirstLine() {
		return firstLine;
	}

	public void setFirstLine(final Tag[] firstLine) {
		this.firstLine = firstLine;
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

	public Tag[] getReturnDescription() {
		return returnDescription;
	}

	public void setReturnDescription(final Tag[] returnDescription) {
		this.returnDescription = returnDescription;
	}

	@Override
	public int compareTo(final MethodXML that) {
		final int compareTo = this.name.compareTo(that.name);
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
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MethodXML other = (MethodXML) obj;
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

	public String toSummaryXML(final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<tr>");
		builder.appendln("<td>" + returnType + "</td>");
		builder.appendln("<td><a href=\"#" + toString() + "\"><code>" + toString() + "</code></a> "
				+ APIDoclet.processText(className, firstLine) + "</td>");
		builder.appendln("</tr>");
		return builder.toString();
	}

	public String toDetailXML(final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<a name=\"" + toString() + "\"></a>");
		builder.appendln("<h4>" + name + "</h4>");
		builder.appendln(APIDoclet.processText(className, description));
		final boolean notVoid = StringUtils.isNotBlank(returnType);
		final boolean hasParams = parameters != null && !parameters.isEmpty();
		if (notVoid || hasParams) {
			builder.appendln("<dl>");
			if (hasParams) {
				builder.appendln("<dt>Parameters</dt>");
				for (final ParameterXML parameterXML : parameters) {
					builder.appendln(parameterXML.toXML(className));
				}
			}
			if (notVoid) {
				builder.appendln("<dt>Returns</dt>");
				builder.appendln("<dd>" + APIDoclet.processText(className, returnDescription) + "</dd>");
			}
			builder.appendln("</dl>");
		}
		return builder.toString();
	}
}
