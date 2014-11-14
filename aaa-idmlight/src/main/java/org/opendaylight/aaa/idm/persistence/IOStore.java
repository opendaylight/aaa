package org.opendaylight.aaa.idm.persistence;

import java.util.List;


public interface IOStore {
	public void createTable(Object connection,Class<? extends IStorable> proxyClass) throws Exception;
	public void write(OStore o) throws Exception;
    public List<IStorable> find(OStore o) throws Exception;
    public IStorable get(OStore o) throws Exception;
    public List<IStorable> delete(OStore o) throws Exception;
}
