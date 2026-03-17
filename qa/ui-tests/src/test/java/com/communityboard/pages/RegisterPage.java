package com.communityboard.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** RegisterPage - Stub for compilation */
public class RegisterPage {
    private final WebDriver driver;
    
    @FindBy(css = "[data-testid='register-firstname-input']")
    private WebElement firstNameInput;
    
    @FindBy(css = "[data-testid='register-lastname-input']")
    private WebElement lastNameInput;
    
    @FindBy(css = "[data-testid='register-email-input']")
    private WebElement emailInput;
    
    @FindBy(css = "[data-testid='register-password-input']")
    private WebElement passwordInput;
    
    @FindBy(css = "[data-testid='register-confirm-password-input']")
    private WebElement confirmPasswordInput;
    
    @FindBy(css = "[data-testid='register-submit-button']")
    private WebElement submitButton;
    
    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void submitRegistrationForm(String firstName, String lastName, String email, 
                                      String password, String confirmPassword) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        confirmPasswordInput.clear();
        confirmPasswordInput.sendKeys(confirmPassword);
        submitButton.click();
    }
}
