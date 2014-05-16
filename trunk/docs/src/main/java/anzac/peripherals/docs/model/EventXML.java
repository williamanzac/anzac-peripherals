package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class EventXML {
	private String name;
	private Tag[] description;
	private final List<ParameterXML> parameters = new ArrayList<ParameterXML>();

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

	public List<ParameterXML> getParameters() {
		return parameters;
	}

	public String toSummaryXML() {
		return "<li><a href=\"#" + name + "\">" + name + "</a></li>";
	}

	public String toDetailXML(final String className) {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<a name=\"" + name + "\"></a>");
		builder.appendln("<h4>" + name + "</h4>");
		builder.appendln(APIDoclet.processText(className, description));
		if (!parameters.isEmpty()) {
			builder.appendln("<dl>");
			builder.appendln("<dt>Arguments</dt>");
			for (final ParameterXML arg : parameters) {
				builder.appendln(arg.toXML(className));
			}
			builder.appendln("</dl>");
		}
		return builder.toString();
	}
}
