package anzac.peripherals.annotations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("anzac.peripherals.annotations.PeripheralMethod")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class PeripheralMethodDocumentationProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PeripheralMethod.class);
		for (final Element element : elements) {
			final String enclosingName = element.getEnclosingElement().getSimpleName().toString();
			final File output = new File(enclosingName);
			try {
				final FileWriter out = new FileWriter(output, true);
				final BufferedWriter bos = new BufferedWriter(out);
				try {
					final String methodName = element.getSimpleName().toString();
					bos.write(methodName);
					bos.append("(");
					final List<? extends Element> parameterElements = element.getEnclosedElements();
					boolean first = true;
					for (final Element parameterElement : parameterElements) {
						if (!first) {
							bos.write(",");
						}
						first = false;
						final String parameterName = parameterElement.getSimpleName().toString();
						bos.write(parameterName);
					}
					bos.write(")\n");
				} catch (IOException e) {
					processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
				} finally {
					try {
						bos.close();
					} catch (IOException e) {
						processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
					}
				}
			} catch (final FileNotFoundException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Kind.ERROR, e.getLocalizedMessage());
			}
		}
		return false;
	}
}
