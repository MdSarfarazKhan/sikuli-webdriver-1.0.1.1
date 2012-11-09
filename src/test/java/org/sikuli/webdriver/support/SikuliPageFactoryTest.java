package org.sikuli.webdriver.support;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.sikuli.webdriver.ImageElement;
import org.sikuli.webdriver.SikuliFirefoxDriver;

public class SikuliPageFactoryTest {
	
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
	public void testGoogleMapPage() throws Exception {
		 // Navigate to the right place
        driver.get("http://map.google.com/");

        // Create a new instance of the search page class
        // and initialise any WebElement fields in it.
        GoogleMapPage page = SikuliPageFactory.initElements(driver, GoogleMapPage.class);

        // And now do the search.
        page.searchFor("Denver, CO");
		
	}

}


class GoogleMapPage {
    // Here's the element
	@FindBy(how = How.ID, using = "gbqfq")
    private WebElement q;

    public void searchFor(String text) {
        q.sendKeys(text);
        q.submit();
    }
} 
