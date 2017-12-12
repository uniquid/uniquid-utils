package com.uniquid.utils;

import java.net.InetAddress;

import org.junit.Test;

import junit.framework.Assert;

public class IpUtilsTest {
	
	@Test
	public void test() throws Exception {
		
		Assert.assertEquals(0, IpUtils.convertStringIpToLong("0.0.0.0"));
		Assert.assertEquals(16885952, IpUtils.convertStringIpToLong("192.168.1.1"));
		Assert.assertEquals(2832851252L, IpUtils.convertStringIpToLong("52.225.217.168"));
		Assert.assertEquals(2547230516L, IpUtils.convertStringIpToLong("52.167.211.151"));
		Assert.assertEquals(2245714228L, IpUtils.convertStringIpToLong("52.225.218.133"));
		
		InetAddress inetAddress1 = InetAddress.getByAddress(new byte[] {52, (byte) 225, (byte) 217, (byte) 168});
		Assert.assertEquals(inetAddress1, IpUtils.convertIntToInetAddress((int)2832851252L));
		
		InetAddress inetAddress2 = InetAddress.getByAddress(new byte[] {52, (byte) 167, (byte) 211, (byte) 151});
		Assert.assertEquals(inetAddress2, IpUtils.convertIntToInetAddress((int)2547230516L));
		
		InetAddress inetAddress3 = InetAddress.getByAddress(new byte[] {52, (byte) 225, (byte) 218, (byte) 133});
		Assert.assertEquals(inetAddress3, IpUtils.convertIntToInetAddress((int)2245714228L));
		
	}

}