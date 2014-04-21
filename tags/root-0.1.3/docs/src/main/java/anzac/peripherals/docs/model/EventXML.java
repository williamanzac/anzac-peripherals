package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;

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

	public void setDescription(Tag[] description) {
		this.description = description;
	}

	public List<ParameterXML> getParameters() {
		return parameters;
	}
}
