package cc.koosha.konfiguration.impl;

public class DummyCustom {

    private String str = "";
    private int i = 0;

    public String concat() {

        return this.str + " ::: " + this.i;
    }


    // No getter setter, let's see if jackson can handle.

//    public String _myStr() {
//
//        return this.str;
//    }
//
//    public int _myI() {
//
//        return this.i;
//    }

}
