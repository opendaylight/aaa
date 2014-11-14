package org.opendaylight.aaa.idm.persistence;

import java.util.List;

public interface IStorable {
    public IStorable write();
    public IStorable get();
    public IStorable delete();
    public IStorable update();
    public List<IStorable> find();
    public List<IStorable> deleteAll();
}
