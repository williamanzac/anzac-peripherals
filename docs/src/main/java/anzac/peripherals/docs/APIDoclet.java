package anzac.peripherals.docs;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import anzac.peripherals.docs.model.ClassXML;
import anzac.peripherals.docs.model.MethodXML;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class APIDoclet {

	private static final Map<String, ClassXML> peripheralClasses = new HashMap<String, ClassXML>();

	private static String baseUrl;

	public static boolean start(final RootDoc rootDoc) {
		try {
			processOptions(rootDoc.options());
		} catch (final MalformedURLException e) {
			rootDoc.printError(e.getMessage());
			return false;
		}
		final List<ClassXML> model = ModelGenerator.generateModel(rootDoc);
		for (final ClassXML classXML : model) {
			peripheralClasses.put(classXML.getName(), classXML);
		}
		for (final ClassXML classXML : model) {
			try {
				final String document = classXML.toXML();
				final String classFileName = className(classXML);
				final File file = new File(classFileName);
				FileUtils.writeStringToFile(file, document);
			} catch (final Exception e) {
				e.printStackTrace();
				rootDoc.printError(e.getMessage());
				return false;
			}
		}
		return true;
	}

	public static String camelCase(final String input) {
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append(" ");
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static String lowerUnderscore(final String input) {
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append("_");
			}
			c = Character.toLowerCase(c);
			builder.append(c);
		}
		return builder.toString();
	}

	public static String lowerDash(final String input) {
		final StrBuilder builder = new StrBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				builder.append("-");
			}
			c = Character.toLowerCase(c);
			builder.append(c);
		}
		return builder.toString();
	}

	private static void processOptions(final String[][] options) throws MalformedURLException {
		for (final String[] strings : options) {
			if ("-url".equals(strings[0])) {
				baseUrl = strings[1];
			}
		}
	}

	public static String processText(final String name, final Tag[] tags) {
		final StrBuilder strBuilder = new StrBuilder();
		if (tags != null) {
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
		}
		strBuilder.replaceAll("\n", null);
		return strBuilder.toString();
	}

	private static String processSig(final String name, final String text, final boolean link) {
		final StrBuilder builder = new StrBuilder();
		final Pattern methodPattern = Pattern.compile("(.*)#(.*)\\(");
		final Matcher methodMatcher = methodPattern.matcher(text);
		if (methodMatcher.find()) {
			// get Class
			final String strClass = methodMatcher.group(1);
			ClassXML classXML;
			if (StringUtils.isNotBlank(strClass)) {
				classXML = peripheralClasses.get(strClass);
				if (!link) {
					builder.append(className(classXML));
					builder.append(".");
				} else {
					builder.append(baseUrl + classFullFileName(classXML));
				}
			} else {
				classXML = peripheralClasses.get(name);
			}
			if (link) {
				builder.append("#");
			}
			// get method
			final String strMethod = methodMatcher.group(2);
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
		final Pattern eventPattern = Pattern.compile("(PeripheralEvent)#(.*)");
		final Matcher eventMatcher = eventPattern.matcher(text);
		if (eventMatcher.find()) {
			if (link) {
				builder.append("#");
			}
			// get method
			builder.append(eventMatcher.group(2));
			return builder.toString();
		}
		final ClassXML classXML = peripheralClasses.get(text);
		if (classXML != null) {
			if (link) {
				return baseUrl + classFullFileName(classXML);
			}
			return className(classXML);
		}
		return text;
	}

	private static String className(final ClassXML classXML) {
		final String typeName = classXML.getName();
		final int index = typeName.indexOf("TileEntity");
		final String fileName = typeName.substring(0, index);
		return fileName;
	}

	private static String classFullFileName(final ClassXML classXML) {
		final String lowerDash = lowerDash(className(classXML));
		final String pkg = classXML.getFullName().replace("anzac.peripherals", "anzac-peripherals")
				.replace("tiles", "blocks").replace(classXML.getName(), lowerDash).replaceAll("\\.", "/");
		return pkg;
	}

	public static int optionLength(final String option) {
		if ("-url".equals(option)) {
			return 2;
		}
		return 0;
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}
}
