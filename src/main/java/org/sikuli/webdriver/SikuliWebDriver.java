package org.sikuli.webdriver;

import java.net.URL;

import org.openqa.selenium.WebElement;
import org.sikuli.api.DefaultScreenRegion;


public interface SikuliWebDriver {
	
	public DefaultScreenRegion getWebDriverScreenRegion();
	public WebElement findElementByLocation(int x, int y);
	public ImageElement findImageElement(URL url);
}
