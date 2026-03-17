package com.communityboard.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** HomePage - Stub for compilation */
public class HomePage {
    private final WebDriver driver;
    
    @FindBy(css = "[data-testid='create-post-button']")
    private WebElement createPostButton;
    
    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToHomePage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/home");
    }
    
    public CreatePostPage clickCreatePostButton() {
        createPostButton.click();
        return new CreatePostPage(driver);
    }
}
