package cc.koosha.konfigurations.core.impl;

import cc.koosha.konfigurations.core.KonfigV;


interface KonfigurationCache {

    <T> void create(KonfigKey key, boolean mustExist);

    <T> T v(KonfigKey key, T def, boolean mustExist);

    boolean update();

}
