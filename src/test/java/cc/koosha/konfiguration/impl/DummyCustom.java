package cc.koosha.konfiguration.impl;


@SuppressWarnings("FieldCanBeLocal")
public class DummyCustom {

    private String str = "";
    private int i = 0;

    public String concat() {

        return this.str + " ::: " + this.i;
    }

}
