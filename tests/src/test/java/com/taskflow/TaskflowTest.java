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
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/google-chrome");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
        driver.get(BASE_URL + "/dashboard");
        try {
            // Wait for loading to finish
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
            
            // If redirected to login, login again
            if (driver.getCurrentUrl().endsWith("/") || driver.findElements(By.xpath("//input[@type='email']")).size() > 0) {
                WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
                emailInput.sendKeys("ashtar@gmail.com");
                driver.findElement(By.xpath("//input[@type='password']")).sendKeys("Abc123!@#");
                driver.findElement(By.xpath("//button[@type='submit']")).click();
                wait.until(ExpectedConditions.urlContains("/dashboard"));
            }
        } catch (Exception e) {
            // Already logged in or handled
        }

        // Current UI uses h2 for "Projects" and h1 for "TaskFlow"
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(), 'Projects')]")));
        assertNotNull(header);
    }

    @Test
    @Order(8)
    void test8_verifyNewProjectButtonPresent() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        WebElement newProjectBtn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(), 'New Project')]")));
        assertNotNull(newProjectBtn);
    }

    @Test
    @Order(9)
    void test9_testCreateProjectModalOpens() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        WebElement newProjectBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'New Project')]")));
        newProjectBtn.click();

        // Form should appear with label "Project Name"
        WebElement titleLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text(), 'Project Name')]")));
        assertNotNull(titleLabel);
    }

    @Test
    @Order(10)
    void test10_testCreateNewProject() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        WebElement newProjectBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'New Project')]")));
        newProjectBtn.click();

        WebElement titleInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text(), 'Project Name')]/following-sibling::input")));
        WebElement descInput = driver.findElement(By.xpath("//label[contains(text(), 'Description')]/following-sibling::textarea"));
        WebElement submitBtn = driver.findElement(By.xpath("//button[text()='Create' and @type='submit']"));

        String projName = "Automation Project " + System.currentTimeMillis();
        titleInput.sendKeys(projName);
        descInput.sendKeys("Description for test project");
        submitBtn.click();

        // Project should appear in the list as h3
        WebElement projTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[contains(text(), '" + projName + "')]")));
        assertNotNull(projTitle);
    }

    @Test
    @Order(11)
    void test11_testNavigateToProject() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        
        // Click the first project card (h3)
        WebElement projectCard = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//h3)[1]")));
        projectCard.click();

        wait.until(ExpectedConditions.urlContains("/project/"));
        assertTrue(driver.getCurrentUrl().contains("/project/"));
    }

    @Test
    @Order(12)
    void test12_verifyKanbanColumns() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//h3)[1]"))).click();

        // Verify standard Kanban columns exist
        WebElement todoCol = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[text()='To Do']")));
        WebElement inProgressCol = driver.findElement(By.xpath("//h3[text()='In Progress']"));
        WebElement doneCol = driver.findElement(By.xpath("//h3[text()='Done']"));
        
        assertNotNull(todoCol);
        assertNotNull(inProgressCol);
        assertNotNull(doneCol);
    }

    @Test
    @Order(13)
    void test13_testAddTask() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[text()='Loading...']")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//h3)[1]"))).click();

        // Click + Add Task button in navigation/header
        WebElement addTaskBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Add Task')]")));
        addTaskBtn.click();

        WebElement taskNameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text(), 'Task Name')]/following-sibling::input")));
        taskNameInput.sendKeys("Selenium Task " + System.currentTimeMillis());
        driver.findElement(By.xpath("//label[contains(text(), 'Description')]/following-sibling::textarea")).sendKeys("Task Description");
        driver.findElement(By.xpath("//button[text()='Create' and @type='submit']")).click();

        // Should see some tasks in the To Do column (count might be > 0)
        WebElement todoCount = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[text()='To Do']/following-sibling::span")));
        assertTrue(todoCount.getText().contains("tasks"));
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
