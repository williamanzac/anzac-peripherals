package anzac.peripherals.docs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.model.ClassXML;
import anzac.peripherals.docs.model.EventXML;
import anzac.peripherals.docs.model.MethodXML;
import anzac.peripherals.docs.model.ParameterXML;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class APIDoclet {

	private static final Map<String, ClassXML> peripheralClasses = new HashMap<String, ClassXML>();

	public static boolean start(final RootDoc rootDoc) {
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().contains("Peripheral")) {
					final ClassXML processClass = processClass(classDoc);
					peripheralClasses.put(processClass.getName(), processClass);
				}
			}
		}
		for (final ClassXML classXML : peripheralClasses.values()) {
			final String classFileName = classFileName(classXML);
			final File file = new File(classFileName);
			try {
				final FileWriter fw = new FileWriter(file);
				fw.write("<section id=\"description\">\n");
				fw.write(processText(classXML.getName(), classXML.getDescription()) + "\n");
				fw.write("</section>\n");
				fw.write("<section id=\"recipe\">\n");
				fw.write("<h3>Recipe</h3>\n");
				fw.write("<img alt=\"Recipe for " + classXML.getType()
						+ "\" src=\"http://files.anzacgaming.co.uk/images/recipe_" + classXML.getType().toLowerCase()
						+ ".png\" />\n");
				fw.write("</section>\n");
				fw.write("<section id=\"api\">\n");
				fw.write("<h2>API</h2>\n");
				fw.write("<dl>\n");
				fw.write("<dt>Peripheral Type</dt>\n");
				fw.write("<dd>" + classXML.getType() + "</dd>\n");
				fw.write("</dl>\n");
				fw.write("</section>\n");
				fw.write("<section id=\"summary\">\n");
				fw.write("<h3>Method Summary</h3>\n");
				fw.write("<table class=\"table\">\n");
				fw.write("<thead>\n");
				fw.write("<tr>\n");
				fw.write("<th>Return Type</th>\n");
				fw.write("<th>Method and Description</th>\n");
				fw.write("</tr>\n");
				fw.write("</thead>\n");
				fw.write("<tbody>\n");
				for (final MethodXML methodXML : classXML.getMethods()) {
					fw.write("<tr>\n");
					fw.write("<td>" + methodXML.getReturnType() + "</td>\n");
					fw.write("<td><a href=\"#" + methodXML.toString() + "\">" + methodXML.toString() + "</a>"
							+ processText(classXML.getName(), methodXML.getDescription()) + "</td>\n");
					fw.write("</tr>\n");
				}
				fw.write("</tbody>\n");
				fw.write("</table>\n");
				if (!classXML.getEvents().isEmpty()) {
					fw.write("<h3>Event Summary</h3>\n");
					fw.write("<ul>\n");
					for (final EventXML event : classXML.getEvents()) {
						fw.write("<li><a href=\"#" + event.getName() + "\">" + event.getName() + "</a></li>\n");
					}
					fw.write("</ul>\n");
				}
				fw.write("</section>\n");
				fw.write("<section id=\"detail\">\n");
				fw.write("<h3>Method Detail</h3>\n");
				for (final MethodXML methodXML : classXML.getMethods()) {
					fw.write("<a name=\"" + methodXML.toString() + "\"></a>\n");
					fw.write("<h4>" + methodXML.getName() + "</h4>\n");
					fw.write(processText(classXML.getName(), methodXML.getDescription()) + "\n");
					final boolean notVoid = StringUtils.isNotBlank(methodXML.getReturnType());
					final boolean hasParams = methodXML.getParameters() != null && !methodXML.getParameters().isEmpty();
					if (notVoid || hasParams) {
						fw.write("<dl>\n");
						if (hasParams) {
							fw.write("<dt>Parameters</dt>\n");
							for (final ParameterXML parameterXML : methodXML.getParameters()) {
								fw.write("<dd><code>" + parameterXML.getName() + "</code>"
										+ parameterXML.getDescription() + "</dd>\n");
							}
						}
						if (notVoid) {
							fw.write("<dt>Returns</dt>\n");
							fw.write("<dd>" + methodXML.getReturnDescription() + "</dd>\n");
						}
						fw.write("</dl>\n");
					}
				}

				if (!classXML.getEvents().isEmpty()) {
					fw.write("<h3>Event Detail</h3>\n");
					for (final EventXML event : classXML.getEvents()) {
						fw.write("<a name=\"" + event.getName() + "\"></a>\n");
						fw.write("<h4>" + event.getName() + "</h4>\n");
						fw.write(processText(classXML.getName(), event.getDescription()) + "\n");
						if (!event.getParameters().isEmpty()) {
							fw.write("<dl>\n");
							fw.write("<dt>Arguments</dt>\n");
							for (final ParameterXML arg : event.getParameters()) {
								fw.write("<dd><code>" + arg.getName() + "</code>" + arg.getDescription() + "</dd>\n");
							}
							fw.write("</dl>\n");
						}
					}
				}
				fw.write("</section>\n");
				fw.flush();
				fw.close();
			} catch (final IOException e) {
				rootDoc.printError(e.getMessage());
			}

		}
		return true;
	}

	private static ClassXML processClass(final ClassDoc classDoc) {
		final ClassXML classXML = new ClassXML();
		classXML.setName(classDoc.simpleTypeName());
		classXML.setDescription(classDoc.inlineTags());
		classXML.getMethods().addAll(processMethods(classDoc));
		for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
			if (annotationDesc.annotationType().toString().contains("Peripheral")) {
				for (final ElementValuePair pair : annotationDesc.elementValues()) {
					if (pair.element().name().equals("type")) {
						classXML.setType(pair.value().value().toString());
					}
					if (pair.element().name().equals("events")) {
						final AnnotationValue[] events = (AnnotationValue[]) pair.value().value();
						classXML.getEvents().addAll(processEvents(events));
					}
				}
			}
		}
		return classXML;
	}

	private static String processText(final String name, final Tag[] tags) {
		final StrBuilder strBuilder = new StrBuilder();
		for (final Tag tag : tags) {
			if ("Text".equals(tag.name())) {
				strBuilder.append(tag.text());
			} else if ("@link".equals(tag.name())) {
				strBuilder.append("<a href=\"").append(processSig(name, tag.text(), true)).append("\"><code>")
						.append(processSig(name, tag.text(), false)).append("</code></a>");
			} else if ("@code".equals(tag.name())) {
				strBuilder.append("<code>").append(processSig(name, tag.text(), false)).append("</code>");
			}
		}
		return strBuilder.toString();
	}

	private static String processSig(final String name, final String text, final boolean link) {
		final StrBuilder builder = new StrBuilder();
		final Pattern pattern = Pattern.compile("(.*)#(.*)\\(");
		final Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			// get Class
			final String strClass = matcher.group(1);
			ClassXML classXML;
			if (StringUtils.isNotBlank(strClass)) {
				classXML = peripheralClasses.get(strClass);
			} else {
				classXML = peripheralClasses.get(name);
			}
			if (link) {
				builder.append("#");
			}
			// get method
			final String strMethod = matcher.group(2);
			if (StringUtils.isNotBlank(strMethod)) {
				for (final MethodXML method : classXML.getMethods()) {
					if (method.getName().equals(strMethod)) {
						builder.append(method.toString());
						break;
					}
				}
			}
			return builder.toString();
		}
		return text;
	}

	private static Set<EventXML> processEvents(final AnnotationValue[] events) {
		final Set<EventXML> eventXMLs = new HashSet<EventXML>();
		for (final AnnotationValue event : events) {
			final EventXML eventXML = new EventXML();
			final FieldDoc fieldDoc = (FieldDoc) event.value();
			eventXML.setName(fieldDoc.name());
			eventXML.setDescription(fieldDoc.inlineTags());
			eventXML.getParameters().addAll(processParameters(fieldDoc));
			eventXMLs.add(eventXML);
		}
		return eventXMLs;
	}

	private static String classFileName(final ClassXML classXML) {
		final String typeName = classXML.getName();
		final int index = typeName.indexOf("TileEntity");
		final String fileName = typeName.substring(0, index) + ".php";
		return fileName;
	}

	public static int optionLength(final String option) {
		return 0;
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	private static Set<MethodXML> processMethods(final ClassDoc classDoc) {
		final Set<MethodXML> methods = new HashSet<MethodXML>();
		for (final MethodDoc methodDoc : classDoc.methods()) {
			for (final AnnotationDesc annotationDesc : methodDoc.annotations()) {
				if (annotationDesc.annotationType().toString().contains("Peripheral")) {
					final MethodXML methodXML = new MethodXML();
					methodXML.setName(methodDoc.name());
					methodXML.setDescription(methodDoc.inlineTags());
					methodXML.setReturnType(javaToLUA(methodDoc.returnType().simpleTypeName()));
					methodXML.getParameters().addAll(processParameters(methodDoc));
					for (final Tag tag : methodDoc.tags()) {
						final String text = tag.text();
						final String tagName = tag.name();
						if (tagName.equals("@return")) {
							final String description = StringUtils.trim(text);
							methodXML.setReturnDescription(description);
						}
					}
					methods.add(methodXML);
				}
			}
		}
		final ClassDoc superclass = classDoc.superclass();
		if (superclass != null) {
			methods.addAll(processMethods(superclass));
		}
		return methods;
	}

	private static List<ParameterXML> processParameters(final MethodDoc methodDoc) {
		final List<ParameterXML> parameters = new ArrayList<ParameterXML>();
		for (final Parameter parameter : methodDoc.parameters()) {
			final ParameterXML parameterXML = new ParameterXML();
			final String name = parameter.name();
			parameterXML.setName(name);
			parameterXML.setType(javaToLUA(parameter.type().simpleTypeName()));
			for (final Tag tag : methodDoc.tags()) {
				final String text = tag.text();
				final String tagName = tag.name();
				if (tagName.equals("@param") && text.startsWith(name)) {
					final String substring = text.substring(name.length());
					final String description = StringUtils.trim(substring);
					parameterXML.setDescription(description);
				}
			}
			parameters.add(parameterXML);
		}
		return parameters;
	}

	private static List<ParameterXML> processParameters(final FieldDoc fieldDoc) {
		final List<ParameterXML> parameters = new ArrayList<ParameterXML>();
		for (final Tag tag : fieldDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@param")) {
				final ParameterXML parameterXML = new ParameterXML();
				final String[] strings = StringUtils.split(tag.text(), "\n");
				final String name = StringUtils.trim(strings[0]);
				parameterXML.setName(name);
				final String description = StringUtils.trim(strings[1]);
				parameterXML.setDescription(description);
				parameters.add(parameterXML);
			}
		}
		return parameters;
	}

	private static String javaToLUA(final String type) {
		if ("map".equalsIgnoreCase(type)) {
			return "table";
		} else if ("long".equalsIgnoreCase(type) || "int".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type)) {
			return "number";
		} else if ("void".equalsIgnoreCase(type)) {
			return "";
		}

		// boolean, string
		return type.toLowerCase();
	}
}
