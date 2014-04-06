package anzac.peripherals.docs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.javadoc.Tag;

public class ClassXML {
	private String name;
	private Tag[] description;
	private String type;
	private final Set<MethodXML> methods = new TreeSet<MethodXML>();
	private final List<EventXML> events = new ArrayList<EventXML>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Tag[] getDescription() {
		return description;
	}

	public void setDescription(Tag[] description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<MethodXML> getMethods() {
		return methods;
	}

	public List<EventXML> getEvents() {
		return events;
	}
}
