package ch.njol.skript.variables;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FlatFileStorageTest {

    @Test
    void testHexCoding() {
        byte[] bytes = {-0x80, -0x50, -0x01, 0x00, 0x01, 0x44, 0x7F};
        String string = "80B0FF0001447F";
        assertEquals(string, FlatFileStorage.encode(bytes));
        assertArrayEquals(bytes, FlatFileStorage.decode(string));
    }

    @Test
    void testSplitCSV() {
        String[][] values = {
                {"", ""},
                {",", "", ""},
                {",,", "", "", ""},
                {"a", "a"},
                {"a,", "a", ""},
                {",a", "", "a"},
                {",a,", "", "a", ""},
                {" , a , ", "", "a", ""},
                {"a,b,c", "a", "b", "c"},
                {" a , b , c ", "a", "b", "c"},
                {"\"\"", ""},
                {"\",\"", ","},
                {"\"\"\"\"", "\""},
                {"\" \"", " "},
                {"a, \"\"\"\", b, \", c\", d", "a", "\"", "b", ", c", "d"},
                {"a, \"\"\", b, \", c", "a", "\", b, ", "c"},
                {"\"\t\0\"", "\t\0"},
        };
        for (String[] value : values) {
            assertArrayEquals(
                    Arrays.copyOfRange(value, 1, value.length),
                    FlatFileStorage.splitCSV(value[0]),
                    value[0]
            );
        }
    }
}
