package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.APIDoclet;

import com.sun.javadoc.Tag;

public class BlockXML {
	private String name;
	private Tag[] description;
	private String key;
	private ClassXML peripheral;
	private final List<ItemXML> items = new ArrayList<>();
	private String tool;
	private int toolLevel;

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public List<ItemXML> getItems() {
		return items;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(final String tool) {
		this.tool = tool;
	}

	public int getToolLevel() {
		return toolLevel;
	}

	public void setToolLevel(final int toolLevel) {
		this.toolLevel = toolLevel;
	}

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
		if (peripheral != null) {
			builder.appendln(APIDoclet.processText(peripheral.getName(), description));
		}
		builder.appendln("</section>");
		if (!items.isEmpty()) {
			builder.appendln("<section id=\"recipe\">");
			if (items.size() == 1) {
				builder.appendln("<h3>Recipe</h3>");
				builder.append(items.get(0).toRecipeXML());
			} else {
				builder.appendln("<h3>Recipes</h3>");
				for (final ItemXML itemXML : items) {
					builder.append(itemXML.toRecipeXML());
				}
			}
			builder.appendln("</section>");
		}
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
}
