package org.sikuli.webdriver;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.sikuli.api.DefaultScreenRegion;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.ScreenLocation;
import org.sikuli.api.ScreenRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SikuliChromeDriver extends ChromeDriver implements SikuliWebDriver{
	
	static Logger logger = LoggerFactory.getLogger(SikuliChromeDriver.class);

	private static final int DEFAULT_WAIT_TIMEOUT_MSECS = 10000;
	ScreenRegion webdriverRegion;

	public SikuliChromeDriver(){
		super();
	}

	public WebElement findElementByLocation(int x, int y){
		return (WebElement) ((JavascriptExecutor) this).executeScript("return document.elementFromPoint(" + x + "," + y + ")");
	}

	public ImageElement findImageElement(URL imageUrl) {
		
		DefaultScreenRegion webdriverRegion=getWebDriverScreenRegion();
		ImageTarget target = new ImageTarget(imageUrl);
		final ScreenRegion imageRegion = webdriverRegion.wait(target, DEFAULT_WAIT_TIMEOUT_MSECS);
		
		
		if (imageRegion != null){
			Rectangle r = imageRegion.getBounds();
			logger.debug("image is found at {} {} {} {}", r.x, r.y, r.width, r.height);
		}else{
			logger.debug("image is not found");
			return null;
		}


		ScreenLocation center = imageRegion.getCenter();
		WebElement foundWebElement = findElementByLocation(center.getX(), center.getY());
		Rectangle r = imageRegion.getBounds();
		return new DefaultImageElement(this, foundWebElement, 
				r.x,
				r.y,
				r.width,
				r.height);
	}
	
	public DefaultScreenRegion getWebDriverScreenRegion() {
		// TODO Auto-generated method stub
		WebDriverScreen webDriverScreen;
		try {
			webDriverScreen = new WebDriverScreen(this);
		} catch (IOException e1) {
			throw new RuntimeException("Unable to initialize SikuliChromeDriver screen");
		}
		return new DefaultScreenRegion(webDriverScreen);
	}
}
