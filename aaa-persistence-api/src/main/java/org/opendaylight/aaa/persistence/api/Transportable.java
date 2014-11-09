package org.opendaylight.aaa.persistence.api;


import java.io.Serializable;

/**
 * Requirements for all stored entities.
 *
 * @author liemmn
 * @author Mark Mozolewski
 *
 */
public interface Transportable<ID extends Serializable> {
    void setId(ID id);
    ID getId();
}
