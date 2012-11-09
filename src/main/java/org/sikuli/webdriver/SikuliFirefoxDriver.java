package org.sikuli.webdriver;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.sikuli.api.ImageTarget;
import org.sikuli.api.ScreenLocation;
import org.sikuli.api.ScreenRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SikuliFirefoxDriver extends FirefoxDriver {

	static Logger logger = LoggerFactory.getLogger(SikuliFirefoxDriver.class);

	private static final int DEFAULT_WAIT_TIMEOUT_MSECS = 10000;
	ScreenRegion webdriverRegion;

	public SikuliFirefoxDriver(){
		webdriverRegion = new ScreenRegion();
		try {
			webdriverRegion.setScreen(new WebDriverScreen(this));
		} catch (IOException e) {

		}
	}
	
	static Point getLocation(WebElement webElement){
		return ((Locatable) webElement).getCoordinates().getLocationOnScreen();
	}

	static Rectangle getBounds(WebElement webElement){		
		try{
			Locatable loc = (Locatable) webElement;		
			//Point p = loc.getCoordinates().getLocationOnScreen();
			Point p = loc.getCoordinates().getLocationOnScreen();//getLocationInViewPort();//getLocationOnScreen();
			Rectangle ret = new Rectangle(p.x,p.y, webElement.getSize().width, webElement.getSize().height);
			return ret;
		}catch (Exception ex){
			return new Rectangle(-1,-1,0,0);
		}		
	}

	static boolean isMatched(WebElement webElement, ScreenRegion imageRegion){
		Rectangle imageRegionBounds = new Rectangle(imageRegion.x,imageRegion.y,imageRegion.width,imageRegion.height);
		Rectangle webElementBounds = getBounds(webElement);

		boolean isTwoIntersecting = imageRegionBounds.intersects(webElementBounds);
		return isTwoIntersecting;
	}
	
	
	public WebElement findElementByLocation(int x, int y){
		return (WebElement) ((JavascriptExecutor) this).executeScript("return document.elementFromPoint(" + x + "," + y + ")");
	}

	public ImageElement findImageElement(URL imageUrl) {
		ImageTarget target = new ImageTarget(imageUrl);
		final ScreenRegion imageRegion = webdriverRegion.wait(target, DEFAULT_WAIT_TIMEOUT_MSECS);
		
		if (imageRegion != null){
			logger.debug("image is found at {} {} {} {}", imageRegion.x, imageRegion.y, imageRegion.width, imageRegion.height);
		}else{
			logger.debug("image is not found");
			return null;
		}


		ScreenLocation center = imageRegion.getCenter();
		WebElement foundWebElement = findElementByLocation(center.x, center.y);
		return new DefaultImageElement(this, foundWebElement, 
				imageRegion.x,
				imageRegion.y,
				imageRegion.width,
				imageRegion.height);
	}

	// Implementation that does not involve explicit execution of java script
	private ImageElement findElementByImage(URL imageUrl, By by) {
		ImageTarget target = new ImageTarget(imageUrl);
		final ScreenRegion imageRegion = webdriverRegion.wait(target, DEFAULT_WAIT_TIMEOUT_MSECS);

		
		if (imageRegion != null){
			logger.debug("image is found at {} {} {} {}", imageRegion.x, imageRegion.y, imageRegion.width, imageRegion.height);
		}else{
			logger.debug("image is not found");
			return null;
		}
		
		
		Collection<WebElement> candidateWebElements = by.findElements(this);
		logger.debug("{} candidate web elements", candidateWebElements.size());

		// pre-compute and cache the size and coordinates of all web elements
		candidateWebElements = Lists.transform(ImmutableList.copyOf(candidateWebElements), new Function<WebElement, WebElement>(){
			@Override
			public WebElement apply(WebElement input) {
				return new SizedWebElement(input);
			}
		});
				
		
		Predicate<WebElement> isContainingImageRegion = new Predicate<WebElement>(){
			final Rectangle imageRegionBounds = new Rectangle(imageRegion.x,imageRegion.y,imageRegion.width,imageRegion.height);
			@Override
			public boolean apply(WebElement webElement) {
				Rectangle webElementBounds = getBounds(webElement);
				return webElementBounds.contains(imageRegionBounds);
			}			
		};

		Predicate<WebElement> isLargerThanImageRegion = new Predicate<WebElement>(){
			@Override
			public boolean apply(WebElement webElement) {				
				return webElement.getSize().width > imageRegion.width && webElement.getSize().height > imageRegion.height;
			}			
		};

		candidateWebElements = Collections2.filter(candidateWebElements, isLargerThanImageRegion);
		
		logger.trace("filtering: web elements that are larger than image region");
		logWebElementList(candidateWebElements);		
		
		candidateWebElements = Collections2.filter(candidateWebElements, isContainingImageRegion);
		
		logger.trace("filtering: web elements that are strictly containing the image region");
		logWebElementList(candidateWebElements);
		
		if (candidateWebElements.isEmpty()){
			logger.debug("unable to find a web element containing the image region");
			return null;
		}
		
		WebElement smallestWebElement = Collections.min(candidateWebElements, new Comparator<WebElement>(){
			@Override
			public int compare(WebElement e1, WebElement e2) {
				return e1.getSize().width * e1.getSize().height -
					e2.getSize().width * e2.getSize().height;
						
			}			
		});
		
		
		WebElement bestCandidate = smallestWebElement;
		Point bestCandidateLocation = getLocation(bestCandidate);
		logger.trace("best candidate: {} {} {} {}",
				bestCandidateLocation.x, bestCandidateLocation.y,  
				bestCandidate.getSize().width, bestCandidate.getSize().height);
		
		
		ImageElement imageElement = new DefaultImageElement(this, bestCandidate, 
				imageRegion.x - bestCandidateLocation.x,
				imageRegion.y - bestCandidateLocation.y,
				imageRegion.width,
				imageRegion.height);
		
		return imageElement;
	}
	
	
	static void logWebElementList(Collection<WebElement> webElements){
		for (WebElement webElement : webElements){
			logger.trace("web element: {} {} {} {}",
			getLocation(webElement).x, getLocation(webElement).y,  
			webElement.getSize().width, webElement.getSize().height);
		}

		
	}

}



//
class SizedWebElement implements WebElement, Locatable {
	final private WebElement webElement;
	final private org.openqa.selenium.Dimension cachedSize;
	final private Coordinates cachedCoodinates;
	SizedWebElement(WebElement webElement){
		this.webElement = webElement;				
		final Coordinates coordinates = ((Locatable) webElement).getCoordinates();
		
		Point locationOnScreen;
		org.openqa.selenium.Dimension size;
		try{
			locationOnScreen = coordinates.getLocationOnScreen();
			size = webElement.getSize();
		}catch(Exception exception){
			locationOnScreen = new Point(-1,-1);
			size = new org.openqa.selenium.Dimension(0,0);
		}
		final Point cachedLocationOnScreen = locationOnScreen;
		cachedSize = size;
		this.cachedCoodinates = new Coordinates(){
			
			@Override
			public Point getLocationOnScreen() {
				return cachedLocationOnScreen;
			}

			@Override
			public Point getLocationInViewPort() {
				return coordinates.getLocationInDOM();
			}

			@Override
			public Point getLocationInDOM() {
				return coordinates.getLocationInDOM();
			}

			@Override
			public Object getAuxiliary() {
				return coordinates.getAuxiliary();
			}			
		};
	}
	
	public void click() {
		webElement.click();		
	}
	public void submit() {
		webElement.submit();
	}
	public void sendKeys(CharSequence... keysToSend) {
		webElement.sendKeys(keysToSend);
	}
	public void clear() {
		webElement.clear();
	}
	public String getTagName() {
		return webElement.getTagName();
	}
	public String getAttribute(String name) {
		return webElement.getAttribute(name);
	}
	public boolean isSelected() {
		return webElement.isSelected();
	}
	public boolean isEnabled() {
		return webElement.isEnabled();
	}
	public String getText() {
		return webElement.getText();
	}
	public List<WebElement> findElements(By by) {
		return webElement.findElements(by);
	}
	public WebElement findElement(By by) {
		return webElement.findElement(by);
	}
	public boolean isDisplayed() {
		return webElement.isDisplayed();
	}
	public Point getLocation() {
		return webElement.getLocation();
	}
	public org.openqa.selenium.Dimension getSize() {
		return cachedSize;
	}
	public String getCssValue(String propertyName) {
		return webElement.getCssValue(propertyName);
	}

	@Override
	public Point getLocationOnScreenOnceScrolledIntoView() {		
		// TODO: we need to check if this is equivalent
		return cachedCoodinates.getLocationOnScreen();
	}

	@Override
	public Coordinates getCoordinates() {
		return cachedCoodinates;
	}
}