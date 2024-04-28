package actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import factories.MyToolWindowFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Selenium extends DumbAwareAction {
    @Override
    public void actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            ToolWindow myToolWindow = ToolWindowManager.getInstance(project).getToolWindow("SeleniumAI");
            if (myToolWindow != null) {
                myToolWindow.show(null);
            }
        }
        System.out.println("Selenium AI Tool window opened!");

        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        String selectedText = caretModel.getCurrentCaret().getSelectedText();

        String url = Messages.showInputDialog(e.getProject(), "Please enter the website URL", "Website URL", Messages.getQuestionIcon());

        if (url != null && !url.isEmpty()) {
            ChromeOptions options = new ChromeOptions();
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable("webdriver", Level.ALL);
            options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

            String chromeDriverPath = PropertiesComponent.getInstance().getValue("chromeDriverPath", "C:\\Users\\Ruxo\\Documents\\ChromeDriver\\chromedriver.exe"); // Use the default path if no path is saved
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);

            WebDriver driver = new ChromeDriver();
            System.out.println("Attempting to open URL: " + url);
            String methodUsed = "";
            if (driver != null) {
                System.out.println("Driver initialized successfully!");
            }else {
                System.out.println("Driver initialization failed!");
                Messages.showMessageDialog(e.getProject(), "Please select the correct chrome driver path", "Error with Driver: ", Messages.getErrorIcon());
                return;
            }

            if(url.startsWith("http://") || url.startsWith("https://")) {
                try {
                    driver.get(url);
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                    Messages.showMessageDialog(e.getProject(), "Please select the correct chrome driver path", "Error with Driver: ", Messages.getErrorIcon());
                }
            } else {
                try {
                    driver.get("http://" + url);
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                    Messages.showMessageDialog(e.getProject(), "Please select the correct chrome driver path", "Error with Driver: ", Messages.getErrorIcon());
                }
            }

            boolean elementExists = false;

            try {
                if (selectedText != null && !selectedText.isEmpty()) {
                    if(selectedText.startsWith("#")) {
                        methodUsed = "By.id( "+ selectedText.substring(1) +" )";
                        elementExists = !driver.findElements(By.id(selectedText.substring(1))).isEmpty();
                    } else if(selectedText.startsWith(".")) {
                        methodUsed = "By.cssSelector( "+ selectedText.substring(1) +" )";
                        elementExists = !driver.findElements(By.cssSelector(selectedText)).isEmpty();
                    } else if(selectedText.startsWith("//") || selectedText.startsWith("./")){
                        methodUsed = "By.xpath( " + selectedText.substring(1) + " )";
                        elementExists = !driver.findElements(By.xpath(selectedText)).isEmpty();
                    } else {
                        methodUsed = "By.className( "+ selectedText.substring(1) +" )";
                        elementExists = !driver.findElements(By.className(selectedText)).isEmpty();
                    }
                }
            } catch (Exception ex) {
                System.out.println("No elements found with this css");
                findSimilar(driver, selectedText);
            }

            ArrayList<String> similarElements = new ArrayList<>();
            similarElements.add("No similar elements found");
            similarElements.add("No similar elements found2");

            Messages.showMessageDialog(e.getProject(), "Method used: " + methodUsed, "Element exists: " + elementExists, Messages.getInformationIcon());
            System.out.println("finding similar elements...");
            findSimilar(driver, selectedText);

            driver.quit();
        }
    }

    public void findSimilar(WebDriver driver, String selectedText) {
        // Find similar elements
        List<WebElement> allElements = driver.findElements(By.className("*"));
        ArrayList<String> similarElements = new ArrayList<>();
        for (WebElement element : allElements) {
            String elementClass = element.getAttribute("class");
            if (elementClass != null && (elementClass.contains(selectedText) || calculateMatchPercentage(elementClass, selectedText) > 70)) {
                similarElements.add(elementClass);
                System.out.println("New similar element found: " + elementClass);
            }
            if(element != null) {
                if (element.getAttribute("id") != null && (element.getAttribute("id").contains(selectedText) || calculateMatchPercentage(element.getAttribute("id"), selectedText) > 70)) {
                    similarElements.add(element.getAttribute("id"));
                }
                if (element.getAttribute("name") != null && (element.getAttribute("name").contains(selectedText) || calculateMatchPercentage(element.getAttribute("name"), selectedText) > 70)){
                    similarElements.add(element.getAttribute("name"));
                }
                if (element.getAttribute("value") != null && (element.getAttribute("value").contains(selectedText) || calculateMatchPercentage(element.getAttribute("value"), selectedText) > 70)){
                    similarElements.add(element.getAttribute("value"));
                }
                if (element.getAttribute("placeholder") != null && (element.getAttribute("placeholder").contains(selectedText) || calculateMatchPercentage(element.getAttribute("placeholder"), selectedText) > 70)){
                    similarElements.add(element.getAttribute("placeholder"));
                }
            }
        }
        // Update the list in the tool window
        MyToolWindowFactory myToolWindowFactory = new MyToolWindowFactory();
        myToolWindowFactory.updateSimilarElementsList(similarElements);
    }

    public static double calculateMatchPercentage(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        int matchCount = 0;

        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                matchCount++;
            }
        }

        if (minLength == 0)
          return 0;

        return (double) matchCount / minLength * 100;
    }
}
// This is an Open Source Project with GPL-3.0 License
// Selenium AI - An AI powered Selenium WebDriver for IntelliJ IDEA
// Project maintained by Ing Russo Giovanni M.