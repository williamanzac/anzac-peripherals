package anzac.peripherals.docs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import anzac.peripherals.docs.model.ClassXML;
import anzac.peripherals.docs.model.EventXML;
import anzac.peripherals.docs.model.MethodXML;
import anzac.peripherals.docs.model.ParameterXML;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class ModelGenerator {

	protected static List<ClassXML> generateModel(final RootDoc rootDoc) {
		final List<ClassXML> peripheralClasses = new ArrayList<ClassXML>();
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().contains("Peripheral")) {
					final ClassXML processClass = toClassXML(classDoc);
					peripheralClasses.add(processClass);
				}
			}
		}
		return peripheralClasses;
	}

	private static ClassXML toClassXML(final ClassDoc classDoc) {
		final ClassXML classXML = new ClassXML();
		classXML.setName(classDoc.simpleTypeName());
		classXML.setFullName(classDoc.qualifiedTypeName());
		classXML.setDescription(classDoc.inlineTags());
		classXML.getMethods().addAll(generateMethodModel(classDoc));
		for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
			if (annotationDesc.annotationType().toString().contains("Peripheral")) {
				for (final ElementValuePair pair : annotationDesc.elementValues()) {
					if (pair.element().name().equals("type")) {
						classXML.setType(pair.value().value().toString());
					}
					if (pair.element().name().equals("events")) {
						final AnnotationValue[] events = (AnnotationValue[]) pair.value().value();
						classXML.getEvents().addAll(generateEventModel(events));
					}
				}
			}
		}
		return classXML;
	}

	private static Set<EventXML> generateEventModel(final AnnotationValue[] events) {
		final Set<EventXML> eventXMLs = new HashSet<EventXML>();
		for (final AnnotationValue event : events) {
			final EventXML eventXML = toEventXML(event);
			eventXMLs.add(eventXML);
		}
		return eventXMLs;
	}

	private static EventXML toEventXML(final AnnotationValue event) {
		final EventXML eventXML = new EventXML();
		final FieldDoc fieldDoc = (FieldDoc) event.value();
		eventXML.setName(fieldDoc.name());
		eventXML.setDescription(fieldDoc.inlineTags());
		eventXML.getParameters().addAll(generateParameterModel(fieldDoc));
		return eventXML;
	}

	private static Set<MethodXML> generateMethodModel(final ClassDoc classDoc) {
		final Set<MethodXML> methods = new HashSet<MethodXML>();
		for (final MethodDoc methodDoc : classDoc.methods()) {
			for (final AnnotationDesc annotationDesc : methodDoc.annotations()) {
				if (annotationDesc.annotationType().toString().contains("Peripheral")) {
					final MethodXML methodXML = toMethodXML(methodDoc);
					methods.add(methodXML);
				}
			}
		}
		final ClassDoc superclass = classDoc.superclass();
		if (superclass != null) {
			methods.addAll(generateMethodModel(superclass));
		}
		return methods;
	}

	private static MethodXML toMethodXML(final MethodDoc methodDoc) {
		final MethodXML methodXML = new MethodXML();
		methodXML.setName(methodDoc.name());
		methodXML.setFirstLine(methodDoc.firstSentenceTags());
		methodXML.setDescription(methodDoc.inlineTags());
		methodXML.setReturnType(javaToLUA(methodDoc.returnType()));
		methodXML.getParameters().addAll(generateParameterModel(methodDoc));
		for (final Tag tag : methodDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@return")) {
				methodXML.setReturnDescription(tag.inlineTags());
			}
		}
		return methodXML;
	}

	private static Collection<ParameterXML> generateParameterModel(final MethodDoc methodDoc) {
		final Map<String, ParameterXML> parameters = new HashMap<String, ParameterXML>();
		for (final Tag tag : methodDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@param")) {
				final ParameterXML parameterXML = toParamerterXML(tag);
				parameters.put(parameterXML.getName(), parameterXML);
			}
		}
		for (final Parameter parameter : methodDoc.parameters()) {
			final String name = parameter.name();
			final ParameterXML parameterXML = parameters.get(name);
			if (parameterXML != null) {
				parameterXML.setType(javaToLUA(parameter.type()));
			}
		}
		return parameters.values();
	}

	private static ParameterXML toParamerterXML(final Tag tag) {
		final ParameterXML parameterXML = new ParameterXML();
		final String[] strings = StringUtils.split(tag.text(), "\n");
		final String name = StringUtils.trim(strings[0]);
		parameterXML.setName(name);
		if (strings.length > 1) {
			parameterXML.setDescription(tag.inlineTags());
		}
		return parameterXML;
	}

	private static List<ParameterXML> generateParameterModel(final FieldDoc fieldDoc) {
		final List<ParameterXML> parameters = new ArrayList<ParameterXML>();
		for (final Tag tag : fieldDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@param")) {
				final ParameterXML parameterXML = toParamerterXML(tag);
				parameters.add(parameterXML);
			}
		}
		return parameters;
	}

	private static String javaToLUA(final Type type) {
		final String typeName = type.simpleTypeName();
		if ("map".equalsIgnoreCase(typeName)) {
			return "table";
		} else if ("long".equalsIgnoreCase(typeName) || "int".equalsIgnoreCase(typeName)
				|| "double".equalsIgnoreCase(typeName) || "float".equalsIgnoreCase(typeName)) {
			return "number";
		} else if ("void".equalsIgnoreCase(typeName)) {
			return "";
		} else if (StringUtils.isNotBlank(type.dimension())) {
			return "array";
		}

		// boolean, string
		return typeName.toLowerCase();
	}
}
