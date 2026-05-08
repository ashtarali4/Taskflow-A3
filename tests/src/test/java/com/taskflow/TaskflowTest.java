package com.taskflow;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskflowTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    // We target pipeline_frontend:3000 which is the internal docker network URL
    // For local testing without docker network, one might need to change this to
    // localhost:8081
    private static final String BASE_URL = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL")
            : "http://pipeline_frontend:3000";

    @BeforeAll
    static void setupClass() {
        // Use the pre-installed chromedriver from the markhobson/maven-chrome Docker image
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--single-process");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void test1_verifyPageTitle() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='TaskFlow']")));
        assertTrue(driver.getPageSource().contains("TaskFlow"));
    }

    @Test
    @Order(2)
    void test2_verifyLoginFormPresent() {
        driver.get(BASE_URL);
        WebElement emailInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
        WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit' and text()='Login']"));

        assertNotNull(emailInput);
        assertNotNull(passwordInput);
        assertNotNull(loginButton);
    }

    @Test
    @Order(3)
    void test3_verifySignUpToggle() {
        driver.get(BASE_URL);
        WebElement signUpTab = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//button[text()='Sign Up' and not(@type='submit')]")));
        signUpTab.click();

        WebElement nameInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='text']"))); // Name field
                                                                                                        // appears
        assertNotNull(nameInput);
    }

    @Test
    @Order(4)
    void test4_testFailedLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
        WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        emailInput.sendKeys("invalid@example.com");
        passwordInput.sendKeys("wrongpassword");
        loginButton.click();

        // Check for error message - we wait for error div to appear
        WebElement errorDiv = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'bg-red-100')]")));
        assertTrue(errorDiv.getText().length() > 0);
    }

    @Test
    @Order(5)
    void test5_testSignUp() {
        driver.get(BASE_URL);
        WebElement signUpTab = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//button[text()='Sign Up' and not(@type='submit')]")));
        signUpTab.click();

        WebElement nameInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='text']")));
        WebElement emailInput = driver.findElement(By.xpath("//input[@type='email']"));
        WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
        WebElement signUpBtn = driver.findElement(By.xpath("//button[@type='submit' and text()='Sign Up']"));

        // Use a unique email to prevent duplication errors if run multiple times
        String uniqueEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        nameInput.sendKeys("Test User");
        emailInput.sendKeys(uniqueEmail);
        passwordInput.sendKeys("testpass123");
        signUpBtn.click();

        WebElement successDiv = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'bg-green-100')]")));
        assertTrue(successDiv.getText().contains("Account created"));
    }

    @Test
    @Order(6)
    void test6_testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
        WebElement passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        // Assume there is a default admin user seeded, or we could use the created one
        // For standard taskflow, typically admin@example.com / password is used
        emailInput.sendKeys("ashtar@gmail.com");
        passwordInput.sendKeys("Abc123!@#");
        loginButton.click();

        // Should redirect to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().endsWith("/dashboard"));
    }

    @Test
    @Order(7)
    void test7_verifyDashboardHeader() {
        // Already logged in from previous test due to local storage (or if we need to
        // login again we would)
        driver.get(BASE_URL + "/dashboard");
        try {
            // If not logged in, login
            WebElement emailInput = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
            emailInput.sendKeys("admin@example.com");
            driver.findElement(By.xpath("//input[@type='password']")).sendKeys("password");
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        } catch (Exception e) {
            // Already logged in
        }

        WebElement header = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(), 'Projects')]")));
        assertNotNull(header);
    }

    @Test
    @Order(8)
    void test8_verifyNewProjectButtonPresent() {
        driver.get(BASE_URL + "/dashboard");
        WebElement newProjectBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'New Project')]")));
        assertNotNull(newProjectBtn);
    }

    @Test
    @Order(9)
    void test9_testCreateProjectModalOpens() {
        driver.get(BASE_URL + "/dashboard");
        WebElement newProjectBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'New Project')]")));
        newProjectBtn.click();

        // Form should appear
        WebElement titleInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Project Title']")));
        assertNotNull(titleInput);
    }

    @Test
    @Order(10)
    void test10_testCreateNewProject() {
        driver.get(BASE_URL + "/dashboard");
        WebElement newProjectBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'New Project')]")));
        newProjectBtn.click();

        WebElement titleInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Project Title']")));
        WebElement descInput = driver.findElement(By.xpath("//textarea[@placeholder='Project Description']"));
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Create Project' and @type='submit']"));

        String projName = "Selenium Test Project " + System.currentTimeMillis();
        titleInput.sendKeys(projName);
        descInput.sendKeys("Description for test project");
        submitBtn.click();

        // Project should appear in the list
        WebElement projTitle = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[contains(text(), '" + projName + "')]")));
        assertNotNull(projTitle);
    }

    @Test
    @Order(11)
    void test11_testNavigateToProject() {
        driver.get(BASE_URL + "/dashboard");
        // Click the first project view button
        WebElement viewBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("(//a[contains(text(), 'View Board')])[1]")));
        viewBtn.click();

        wait.until(ExpectedConditions.urlContains("/project/"));
        assertTrue(driver.getCurrentUrl().contains("/project/"));
    }

    @Test
    @Order(12)
    void test12_testAddColumn() {
        // Assume we are on a project page
        driver.get(BASE_URL + "/dashboard");
        WebElement viewBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("(//a[contains(text(), 'View Board')])[1]")));
        viewBtn.click();

        WebElement addColBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Add Column')]")));
        addColBtn.click();

        WebElement colTitleInput = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Column Title']")));
        colTitleInput.sendKeys("Test Column");

        WebElement submitColBtn = driver.findElement(By.xpath("//button[text()='Add' and @type='submit']"));
        submitColBtn.click();

        WebElement newColHeader = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[contains(text(), 'Test Column')]")));
        assertNotNull(newColHeader);
    }

    @Test
    @Order(13)
    void test13_testAddTask() {
        // Assume we are on a project page with a column
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//a[contains(text(), 'View Board')])[1]")))
                .click();

        // Click + button on first column
        WebElement addTaskBtn = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("(//button[contains(@class, 'hover:bg-gray-300')])[1]")));
        addTaskBtn.click();

        WebElement taskTitle = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Task Title']")));
        taskTitle.sendKeys("Selenium Task");
        driver.findElement(By.xpath("//textarea[@placeholder='Task Description']")).sendKeys("Task Desc");
        driver.findElement(By.xpath("//button[text()='Create Task']")).click();

        WebElement taskCard = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[contains(text(), 'Selenium Task')]")));
        assertNotNull(taskCard);
    }

    @Test
    @Order(14)
    void test14_testLogoutButtonPresent() {
        driver.get(BASE_URL + "/dashboard");
        WebElement logoutBtn = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'Logout')]")));
        assertNotNull(logoutBtn);
    }

    @Test
    @Order(15)
    void test15_testLogoutFunctionality() {
        driver.get(BASE_URL + "/dashboard");
        WebElement logoutBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Logout')]")));
        logoutBtn.click();

        // Should redirect to login page
        wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[text()='Login' and @type='submit']")));
        assertTrue(driver.getCurrentUrl().endsWith("/"));
    }
}
