package it.unibo.radarSystem22.domain;

import static org.junit.Assert.*;

import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.unibo.radarSystem22.domain.interfaces.ILed;

public class TestLed {
	  @Before
	  public void up(){
		  DomainSystemConfig.setTheConfiguration();
		  if (DomainSystemConfig.simulation || DomainSystemConfig.simulateLed)
		  	System.out.println("Led test start: simulation mode");
		  else
		    System.out.println("Led test start: real mode");
	  }
	  
	  @After
	  public void down(){
		  System.out.println("Done");
	  }
	  
	  @Test
	  public void testLedMock() throws InterruptedException {
	    ILed led = DeviceFactory.createLed();
	    assertFalse( led.getState() );

	    led.turnOn();
	    assertTrue(  led.getState() );
		Thread.sleep(500);

	    led.turnOff();
	    assertFalse(  led.getState() );
	  }
}
