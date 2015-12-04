package org.opendaylight.aaa.idm;

import org.opendaylight.aaa.api.IIDMStore;

public enum ServiceLocator {
    INSTANCE;

    volatile IIDMStore dataStore = null;
    public IIDMStore getStore(){
        return dataStore;
    }

    //mainly for testing
    public void setStore(IIDMStore _store){
        this.dataStore = _store;
    }

    public static final ServiceLocator getInstance(){
        return INSTANCE;
    }
}
