package org.sikuli.webdriver.examples;


import java.io.File;
import java.net.URL;

import org.sikuli.webdriver.ImageElement;
import org.sikuli.webdriver.SikuliFirefoxDriver;


public class GoogleMapTypeInTextBox {
	
	public static void main(String args[]){
		
		try{
			SikuliFirefoxDriver driver=new SikuliFirefoxDriver(); 
			driver.get("https://maps.google.co.in/");
			ImageElement image;
			//path to the image of the search text box
			URL newURL= new File("C:\\Users\\skhan\\Desktop\\Images\\search.png").toURI().toURL();
			image = driver.findImageElement(newURL);
			image.type("India");
			//path to the image of the search button
			newURL= new File("C:\\Users\\skhan\\Desktop\\Images\\searchButton.png").toURI().toURL();
			image = driver.findImageElement(newURL);
			image.click();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
