package ch.njol.skript.doc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

class DocumentationSupportCompatibilityTest {

    @Test
    void documentationAnnotationsStayRuntimeVisible() {
        assertEquals("Annotated element", AnnotatedElement.class.getAnnotation(Name.class).value());
        assertArrayEquals(new String[]{"first", "second"}, AnnotatedElement.class.getAnnotation(Description.class).value());
        assertArrayEquals(new String[]{"2.10"}, AnnotatedElement.class.getAnnotation(Since.class).value());
        assertArrayEquals(new String[]{"foo", "bar"}, AnnotatedElement.class.getAnnotation(Keywords.class).value());
        assertArrayEquals(new String[]{"Fabric"}, AnnotatedElement.class.getAnnotation(RequiredPlugins.class).value());
        assertArrayEquals(new String[]{"use entity"}, AnnotatedElement.class.getAnnotation(Events.class).value());

        Example.Examples examples = AnnotatedElement.class.getAnnotation(Example.Examples.class);
        assertNotNull(examples);
        assertEquals(2, examples.value().length);
        assertEquals("first example", examples.value()[0].value());
        assertEquals("second example", examples.value()[1].value());
    }

    @Test
    void documentationGeneratorKeepsTemplateAndOutputDirs() {
        File templateDir = new File("build/docs-template");
        File outputDir = new File("build/docs-output");
        DummyGenerator generator = new DummyGenerator(templateDir, outputDir);

        assertEquals(templateDir, generator.templateDir);
        assertEquals(outputDir, generator.outputDir);
        generator.generate();
        assertTrue(generator.generated);
    }

    @Name("Annotated element")
    @Description({"first", "second"})
    @Since("2.10")
    @Keywords({"foo", "bar"})
    @RequiredPlugins("Fabric")
    @Events("use entity")
    @Example("first example")
    @Example("second example")
    private static final class AnnotatedElement {
    }

    private static final class DummyGenerator extends DocumentationGenerator {

        private boolean generated;

        private DummyGenerator(File templateDir, File outputDir) {
            super(templateDir, outputDir);
        }

        @Override
        public void generate() {
            generated = true;
        }
    }
}
