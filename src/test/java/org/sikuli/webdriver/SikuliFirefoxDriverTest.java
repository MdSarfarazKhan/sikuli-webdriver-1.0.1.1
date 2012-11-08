package org.sikuli.webdriver;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SikuliFirefoxDriverTest {
	
	private SikuliFirefoxDriver driver;

	@Before
	public void setUp() throws Exception {		
		this.driver = new SikuliFirefoxDriver();
	}
	
	@After
	public void tearDown() throws Exception {
		this.driver.quit();
	}	
	
	@Test
	public void testFindImageElementOnGoogleMap() throws Exception {
		driver.get("https://maps.google.com/maps?q=denver&hl=en&sll=39.764339,-104.855111&sspn=0.372636,0.724411&hnear=Denver,+Colorado&t=m&z=10");
		ImageElement image = driver.findImageElement(new URL("https://dl.dropbox.com/u/5104407/lakewood.png"));
		image.doubleClick();		
		image = driver.findImageElement(new URL("https://dl.dropbox.com/u/5104407/plus.png"));
		image.click();
	}

}
