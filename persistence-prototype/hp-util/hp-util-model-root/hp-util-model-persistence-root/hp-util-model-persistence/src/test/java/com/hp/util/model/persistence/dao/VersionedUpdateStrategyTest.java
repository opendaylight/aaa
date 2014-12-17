/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import org.easymock.EasyMock;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Versionable;
import com.hp.util.common.type.Id;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class VersionedUpdateStrategyTest {

    @Test
    public void testValidateWriteSuccess() {
        VersionedStorable target = EasyMock.createMock(VersionedStorable.class);
        VersionableIdentifiable source = EasyMock.createMock(VersionableIdentifiable.class);

        EasyMock.expect(target.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(source.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(target, source);

        UpdateStrategy<VersionedStorable, VersionableIdentifiable> strategy = new VersionedUpdateStrategy<VersionedStorable, VersionableIdentifiable>();
        strategy.validateWrite(target, source);

        EasyMock.verify(target, source);
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateWriteFailure() {
        VersionedStorable target = EasyMock.createMock(VersionedStorable.class);
        VersionableIdentifiable source = EasyMock.createMock(VersionableIdentifiable.class);

        EasyMock.expect(target.getVersion()).andReturn(Long.valueOf(2));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(source.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(target, source);

        UpdateStrategy<VersionedStorable, VersionableIdentifiable> strategy = new VersionedUpdateStrategy<VersionedStorable, VersionableIdentifiable>();

        strategy.validateWrite(target, source);

        EasyMock.verify(target, source);
    }

    @Test
    public void testValidateReadSuccess() {
        VersionedStorable source = EasyMock.createMock(VersionedStorable.class);
        VersionableIdentifiable target = EasyMock.createMock(VersionableIdentifiable.class);

        EasyMock.expect(source.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(target.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(source, target);

        UpdateStrategy<VersionedStorable, VersionableIdentifiable> strategy = new VersionedUpdateStrategy<VersionedStorable, VersionableIdentifiable>();
        strategy.validateRead(source, target);

        EasyMock.verify(source, target);
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateReadFailure() {
        VersionedStorable source = EasyMock.createMock(VersionedStorable.class);
        VersionableIdentifiable target = EasyMock.createMock(VersionableIdentifiable.class);

        EasyMock.expect(source.getVersion()).andReturn(Long.valueOf(2));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(target.getVersion()).andReturn(Long.valueOf(1));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(source, target);

        UpdateStrategy<VersionedStorable, VersionableIdentifiable> strategy = new VersionedUpdateStrategy<VersionedStorable, VersionableIdentifiable>();

        strategy.validateRead(source, target);

        EasyMock.verify(source, target);
    }

    private class VersionedStorable implements Versionable {
        @Override
        public Long getVersion() {
            return null;
        }
    }

    private static class VersionableIdentifiable implements Identifiable<VersionableIdentifiable, Long>, Versionable {

        @Override
        public Long getVersion() {
            return null;
        }

        @Override
        public <E extends VersionableIdentifiable> Id<E, Long> getId() {
            return null;
        }
    }
}
