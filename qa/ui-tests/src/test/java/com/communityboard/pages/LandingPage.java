package com.communityboard.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** LandingPage - Stub for compilation */
public class LandingPage {
    private final WebDriver driver;
    
    @FindBy(css = "[data-testid='login-button']")
    private WebElement loginButton;
    
    @FindBy(css = "[data-testid='register-link']")
    private WebElement registerLink;
    
    public LandingPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToLandingPage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/");
    }
    
    public LoginPage clickLoginButton() {
        loginButton.click();
        return new LoginPage(driver);
    }
    
    public RegisterPage clickRegisterLink() {
        registerLink.click();
        return new RegisterPage(driver);
    }
}
