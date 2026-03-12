package com.communityboard.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** LoginPage - Stub for compilation */
public class LoginPage {
    private final WebDriver driver;
    
    @FindBy(css = "[data-testid='login-email-input']")
    private WebElement emailInput;
    
    @FindBy(css = "[data-testid='login-password-input']")
    private WebElement passwordInput;
    
    @FindBy(css = "[data-testid='login-submit-button']")
    private WebElement loginButton;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void login(String email, String password) {
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }
}
