package com.uniquid.registry;

import java.util.Random;

import org.junit.Test;

import com.uniquid.registry.exception.RegistryException;
import com.uniquid.registry.impl.RegistryDAOImpl;

import junit.framework.Assert;

public class RegistryDAOImplTest {
	
	public static final String REGISTRY_URL = "http://appliance4.uniquid.co:8080";
	
	@Test
	public void test() throws Exception {
		
		Random random = new Random();
		
		long randomLong = random.nextLong();
		
		RegistryDAO registryDao = new RegistryDAOImpl(REGISTRY_URL);
		
		String provider = registryDao.retrieveProviderName(String.valueOf(randomLong));
		
		Assert.assertNull(provider);
		
		registryDao.insertMapping("Provider_" + randomLong, String.valueOf(randomLong));
		
		provider = registryDao.retrieveProviderName(String.valueOf(randomLong));
		
		Assert.assertEquals("Provider_" + randomLong, provider);
		
		try {
			registryDao.insertMapping("Provider_" + randomLong, String.valueOf(randomLong));
			Assert.fail();
		} catch (RegistryException ex) {
			// expected
		}
		
	}

}