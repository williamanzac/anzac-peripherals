package anzac.peripherals.docs.model;

import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class UpgradeXML {
	private String name;
	private Tag[] description;
	private ClassXML peripheral;
	private String adjective;

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

	public String toXML() {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"description\">");
		builder.appendln(APIDoclet.processText(getName(), description));
		builder.appendln("<dl>");
		builder.appendln("<dt>Adjective</dt>");
		builder.appendln("<dd>" + adjective + "</dd>");
		builder.appendln("</dl>");
		builder.appendln("</section>");
		if (peripheral != null) {
			builder.append(peripheral.toXML());
		}
		return builder.toString();
	}

	public ClassXML getPeripheral() {
		return peripheral;
	}

	public void setPeripheral(ClassXML peripheral) {
		this.peripheral = peripheral;
	}

	public String getAdjective() {
		return adjective;
	}

	public void setAdjective(String adjective) {
		this.adjective = adjective;
	}
}
