package anzac.peripherals.docs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import anzac.peripherals.docs.model.BlockXML;
import anzac.peripherals.docs.model.ClassXML;
import anzac.peripherals.docs.model.EventXML;
import anzac.peripherals.docs.model.ItemXML;
import anzac.peripherals.docs.model.MethodXML;
import anzac.peripherals.docs.model.ParameterXML;
import anzac.peripherals.docs.model.UpgradeXML;

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

	protected static final List<ClassXML> peripheralClasses = new ArrayList<>();
	protected static final List<ItemXML> itemClasses = new ArrayList<>();
	protected static final List<BlockXML> blockClasses = new ArrayList<>();
	protected static final List<UpgradeXML> upgradeClasses = new ArrayList<>();

	protected static void generateModel(final RootDoc rootDoc) {
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().contains("Peripheral")) {
					final ClassXML processClass = toClassXML(classDoc, annotationDesc);
					peripheralClasses.add(processClass);
				} else if (annotationDesc.annotationType().toString().endsWith("Items")) {
					final List<ItemXML> processClasses = toItemXMLs(classDoc, annotationDesc);
					for (final ItemXML itemXML : processClasses) {
						itemClasses.add(itemXML);
					}
				}
			}
		}
		for (final ClassDoc classDoc : rootDoc.classes()) {
			for (final AnnotationDesc annotationDesc : classDoc.annotations()) {
				if (annotationDesc.annotationType().toString().endsWith("Blocks")) {
					final List<BlockXML> processClasses = toBlockXMLs(classDoc, annotationDesc);
					for (final BlockXML blockXML : processClasses) {
						blockClasses.add(blockXML);
					}
				} else if (annotationDesc.annotationType().toString().endsWith("Upgrade")) {
					final UpgradeXML blockXML = toUpgradeXML(classDoc, annotationDesc);
					upgradeClasses.add(blockXML);
				}
			}
		}
	}

	private static UpgradeXML toUpgradeXML(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		String peripheralClass = null;
		String adjective = null;
		for (final ElementValuePair pair : annotationDesc.elementValues()) {
			if (pair.element().name().equals("adjective")) {
				adjective = (String) pair.value().value();
			}
			if (pair.element().name().equals("peripheralType")) {
				peripheralClass = ((ClassDoc) pair.value().value()).simpleTypeName();
			}
		}
		final ClassXML classXML = findClassXML(peripheralClass);
		final UpgradeXML upgradeXML = new UpgradeXML();
		upgradeXML.setPeripheral(classXML);
		upgradeXML.setAdjective(adjective);
		upgradeXML.setName(classDoc.simpleTypeName());
		return upgradeXML;
	}

	private static List<BlockXML> toBlockXMLs(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		final List<BlockXML> blockList = new ArrayList<>();
		boolean hasBlocks = false;
		AnnotationValue[] blocks = null;
		String tileClass = null;
		String peripheralClass = null;
		String itemClass = null;
		int toolLevel = -1;
		String tool = null;
		String key = null;
		for (final ElementValuePair pair : annotationDesc.elementValues()) {
			if (pair.element().name().equals("itemType")) {
				itemClass = ((ClassDoc) pair.value().value()).simpleTypeName();
			}
			if (pair.element().name().equals("tileType")) {
				tileClass = ((ClassDoc) pair.value().value()).simpleTypeName();
			}
			if (pair.element().name().equals("peripheralType")) {
				peripheralClass = ((ClassDoc) pair.value().value()).simpleTypeName();
			}
			if (pair.element().name().equals("key")) {
				key = (String) pair.value().value();
			}
			if (pair.element().name().equals("tool")) {
				tool = (String) pair.value().value();
			}
			if (pair.element().name().equals("toolLevel")) {
				toolLevel = (int) pair.value().value();
			}
			if (pair.element().name().equals("value")) {
				blocks = (AnnotationValue[]) pair.value().value();
				if (blocks.length > 0) {
					hasBlocks = true;
				}
			}
		}
		if (hasBlocks) {
			for (final AnnotationValue value : blocks) {
				blockList.add(toBlockXML(value, key, tool, toolLevel));
			}
		} else {
			final List<ItemXML> itemXMLs = findItemXMLs(itemClass);
			final ClassXML classXML = findClassXML(peripheralClass);
			final BlockXML blockXML = new BlockXML();
			blockXML.getItems().addAll(itemXMLs);
			blockXML.setPeripheral(classXML);
			blockXML.setKey(key);
			blockXML.setTool(tool);
			blockXML.setToolLevel(toolLevel);
			blockXML.setDescription(classDoc.inlineTags());
			blockXML.setName(APIDoclet.camelCase(APIDoclet.className(classXML)));
			blockList.add(blockXML);
		}
		return blockList;
	}

	private static final Pattern blockPattern = Pattern
			.compile("\\/\\*public static final\\*\\/\\s+(.*)\\s+\\/\\* = new BlockType\\((\\d+), \"(.*)\". \"(.*)\", (.*)\\.class, (.*)\\.class\\) \\*\\/");

	private static BlockXML toBlockXML(final AnnotationValue value, String key, final String tool, final int toolLevel) {
		final BlockXML blockXML = new BlockXML();
		final FieldDoc fieldDoc = (FieldDoc) value.value();
		String tileClass = null;
		String peripheralClass = null;
		String name = null;
		try {
			final Field f = fieldDoc.getClass().getSuperclass().getSuperclass().getDeclaredField("tree");
			f.setAccessible(true);
			final String enumConstantDeclaration = f.get(fieldDoc).toString();
			final Matcher matcher = blockPattern.matcher(enumConstantDeclaration);
			if (matcher.matches()) {
				key = matcher.group(3);
				name = matcher.group(4);
				tileClass = matcher.group(5);
				peripheralClass = matcher.group(6);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		blockXML.setKey(key);
		blockXML.setTool(tool);
		blockXML.setToolLevel(toolLevel);
		final ClassXML classXML = findClassXML(peripheralClass);
		if (classXML != null) {
			blockXML.setPeripheral(classXML);
		}
		final ItemXML itemXML = findItemXML(key);
		if (itemXML != null) {
			blockXML.getItems().add(itemXML);
		}
		blockXML.setDescription(fieldDoc.inlineTags());
		blockXML.setName(name);
		return blockXML;
	}

	private static List<ItemXML> findItemXMLs(final String itemClass) {
		if (itemClass == null) {
			return null;
		}
		final List<ItemXML> list = new ArrayList<>();
		for (final Iterator<ItemXML> i = itemClasses.iterator(); i.hasNext();) {
			final ItemXML itemXML = i.next();
			if (itemClass.equals(itemXML.getClassName())) {
				i.remove();
				list.add(itemXML);
			}
		}
		return list;
	}

	private static ItemXML findItemXML(final String key) {
		if (key == null) {
			return null;
		}
		for (final Iterator<ItemXML> i = itemClasses.iterator(); i.hasNext();) {
			final ItemXML itemXML = i.next();
			if (key.equals(itemXML.getKey())) {
				i.remove();
				return itemXML;
			}
		}
		return null;
	}

	private static ClassXML findClassXML(final String tileClass) {
		if (tileClass == null) {
			return null;
		}
		for (final Iterator<ClassXML> i = peripheralClasses.iterator(); i.hasNext();) {
			final ClassXML classXML = i.next();
			if (tileClass.equals(classXML.getName())) {
				return classXML;
			}
		}
		return null;
	}

	private static List<ItemXML> toItemXMLs(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		final List<ItemXML> itemXMLs = new ArrayList<>();
		String key = null;
		AnnotationValue[] items = null;
		for (final ElementValuePair pair : annotationDesc.elementValues()) {
			if (pair.element().name().equals("key")) {
				key = pair.value().value().toString();
			}
			if (pair.element().name().equals("value")) {
				items = (AnnotationValue[]) pair.value().value();
			}
		}
		itemXMLs.addAll(toItemXMLs(classDoc, key, items));
		return itemXMLs;
	}

	private static Collection<? extends ItemXML> toItemXMLs(final ClassDoc classDoc, final String key,
			final AnnotationValue[] items) {
		final List<ItemXML> itemXMLs = new ArrayList<>();
		if (items != null) {
			for (final AnnotationValue value : items) {
				final ItemXML itemXML = toItemXML(classDoc, value);
				itemXMLs.add(itemXML);
			}
		}
		return itemXMLs;
	}

	private static final Pattern itemPattern = Pattern
			.compile("\\/\\*public static final\\*\\/ (.*) \\/\\* = new ItemType\\((\\d+), \"(.*)\", \"(.*)\", \"(.*)\"\\) \\*\\/");

	private static ItemXML toItemXML(final ClassDoc classDoc, final AnnotationValue value) {
		final ItemXML itemXML = new ItemXML();
		final FieldDoc fieldDoc = (FieldDoc) value.value();
		String meta = null;
		String key = null;
		String name = null;
		try {
			final Field f = fieldDoc.getClass().getSuperclass().getSuperclass().getDeclaredField("tree");
			f.setAccessible(true);
			final String enumConstantDeclaration = f.get(fieldDoc).toString();
			final Matcher matcher = itemPattern.matcher(enumConstantDeclaration);
			if (matcher.matches()) {
				meta = matcher.group(2);
				key = matcher.group(3);
				name = matcher.group(4);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		itemXML.setClassName(classDoc.simpleTypeName());
		itemXML.setName(name);
		itemXML.setDescription(fieldDoc.inlineTags());
		itemXML.setKey(key);
		return itemXML;
	}

	private static ClassXML toClassXML(final ClassDoc classDoc, final AnnotationDesc annotationDesc) {
		final ClassXML classXML = new ClassXML();
		classXML.setName(classDoc.simpleTypeName());
		classXML.setFullName(classDoc.qualifiedTypeName());
		classXML.getMethods().addAll(generateMethodModel(classDoc));
		for (final ElementValuePair pair : annotationDesc.elementValues()) {
			if (pair.element().name().equals("type")) {
				classXML.setType(pair.value().value().toString());
			}
			if (pair.element().name().equals("events")) {
				final AnnotationValue[] events = (AnnotationValue[]) pair.value().value();
				classXML.getEvents().addAll(generateEventModel(events));
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
			final MethodXML methodXML = toMethodXML(methodDoc);
			methods.add(methodXML);
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
		for (final AnnotationDesc annotationDesc : methodDoc.annotations()) {
			if (annotationDesc.annotationType().toString().contains("Peripheral")) {
				methodXML.setPeripheralMethod(true);
				break;
			}
		}
		for (final Tag tag : methodDoc.tags()) {
			final String tagName = tag.name();
			if (tagName.equals("@return")) {
				methodXML.setReturnDescription(tag.inlineTags());
				break;
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
			ParameterXML parameterXML = parameters.get(name);
			if (parameterXML == null) {
				parameterXML = new ParameterXML();
				parameterXML.setName(parameter.name());
				parameters.put(parameterXML.getName(), parameterXML);
			}
			parameterXML.setType(javaToLUA(parameter.type()));
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
