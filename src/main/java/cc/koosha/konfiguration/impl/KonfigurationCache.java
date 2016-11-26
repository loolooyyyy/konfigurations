package cc.koosha.konfiguration.impl;


interface KonfigurationCache {

    <T> void create(KonfigKey key, boolean mustExist);

    <T> T v(KonfigKey key, T def, boolean mustExist);

    boolean update();

}
