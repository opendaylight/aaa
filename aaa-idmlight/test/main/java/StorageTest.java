package org.opendaylight.aaa.idm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.persistence.OStore;

public class StorageTest {
	
	@Before
	public void before(){
		User user = (User)OStore.newStorable(User.class);
		user.deleteAll();
		Role role = (Role)OStore.newStorable(Role.class);
		role.deleteAll();
	}
	
	public static User buildUser(int eid){
		User user = (User)OStore.newStorable(User.class);
		user.setDescription("My Description-"+eid);
		user.setEmail("Email-"+eid);
		user.setEnabled(true);
		user.setName("Name-"+eid);
		user.setPassword("Password123");
		return user;
	}
	
	public static Role buildRole(int eid){
		Role r = (Role)OStore.newStorable(Role.class);
		r.setDescription("Description-"+eid);
		r.setName("Name-"+eid);
		return r;
	}
	
	@Test
	public void testInsertUser(){
		User before = buildUser(0);
		before = (User)before.write();
		User after = (User)OStore.newStorable(User.class);
		after.setName("Name-0");
		after = (User)after.get();
		Assert.assertEquals(before, after);
	}
	
	public void testInsertRole(){
		Role before = buildRole(0);
		before = (Role)before.write();
		Role after = (Role)OStore.newStorable(Role.class);
		after.setName("Name-0");
		after = (Role)after.get();
		Assert.assertEquals(before, after);	
	}
}
