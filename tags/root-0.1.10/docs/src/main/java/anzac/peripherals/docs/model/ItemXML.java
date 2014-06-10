package anzac.peripherals.docs.model;

import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class ItemXML {
	private String name;
	private String key;
	private Tag[] description;
	private String className;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Tag[] getDescription() {
		return description;
	}

	public void setDescription(Tag[] description) {
		this.description = description;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String toXML() {
		final StrBuilder builder = new StrBuilder();
		builder.appendln("<section id=\"description\">");
		builder.appendln(APIDoclet.processText(name, description));
		builder.appendln("</section>");
		builder.appendln("<section id=\"recipe\">");
		builder.appendln("<h3>Recipe</h3>");
		builder.appendln(toRecipeXML());
		builder.appendln("</section>");
		return builder.toString();
	}

	protected String toRecipeXML() {
		return "<img alt=\"Recipe for " + APIDoclet.camelCaseItemName(name)
				+ "\" src=\"http://files.anzacgaming.co.uk/images/recipe_" + APIDoclet.lowerUnderscoreItemName(name)
				+ ".png\" />";
	}
}
