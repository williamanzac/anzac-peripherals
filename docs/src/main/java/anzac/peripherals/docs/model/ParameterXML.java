package anzac.peripherals.docs.model;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class ParameterXML {
	private String name;
	private String type;
	private Tag[] description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public Tag[] getDescription() {
		return description;
	}

	public void setDescription(Tag[] description) {
		this.description = description;
	}

	public String toXML(final String className) {
		return "<dd><code>" + name + "</code> " + APIDoclet.processText(className, description) + "</dd>";
	}
}
