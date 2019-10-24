package io.koosha.konfiguration;


import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class TypeNameTest {

    @Test
    public void testGetTName() throws Exception {
        assertEquals(TypeName.STRING_ARRAY.getTName(), "string array");
    }

    @Test
    public void testTypeName() throws Exception {
        assertEquals(TypeName.typeName(null, null), "?");

        assertEquals(TypeName.typeName(String.class, null), String.class.getName());

        assertEquals(TypeName.typeName(null, Long.class), Long.class.getName());

        assertEquals(TypeName.typeName(ArrayList.class, List.class),
                     ArrayList.class.getName() + "/" + List.class.getName());
    }

}
