package com.communityboard.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * CreatePostPage - Stub for compilation
 * Full implementation in feature/post-crud-ui branch
 */
public class CreatePostPage {
    private final WebDriver driver;
    
    @FindBy(css = "[data-testid='post-title-input']")
    private WebElement titleInput;
    
    public CreatePostPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public void enterTitle(String title) {
        titleInput.clear();
        titleInput.sendKeys(title);
    }
}
