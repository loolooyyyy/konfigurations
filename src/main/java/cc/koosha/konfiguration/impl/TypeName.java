package cc.koosha.konfiguration.impl;

/**
 * To create consistent error messages, name of types are taken from here.
 */
enum TypeName {

    BOOLEAN,
    INT,
    STRING,
    DOUBLE,
    LONG,
    LIST,
    MAP,
    SET,
    STRING_ARRAY("string array"),;

    private final String tName;

    TypeName() {

        this.tName = this.name().toLowerCase();
    }

    TypeName(String tName) {

        this.tName = tName;
    }

    public String getTName() {

        return this.tName;
    }

    static String typeName(Class<?> base, Class<?> aux) {

        if (base == null && aux == null)
            return "?";
        if (base == null)
            return aux.getName();
        if (aux == null)
            return base.getName();
        else
            return base.getName() + " / " + aux.getName();
    }

}
