package actions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
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

            System.setProperty("webdriver.chrome.driver", "C:\\Users\\Ruxo\\Documents\\ChromeDriver\\chromedriver.exe"); // Replace with the path to your ChromeDriver
            WebDriver driver = new ChromeDriver();
            System.out.println("Attempting to open URL: " + url);
            String methodUsed = "";
            if (driver != null) {
                System.out.println("Driver initialized successfully!");
            }else {
                System.out.println("Driver initialization failed!");
            }

            if(url.startsWith("http://") || url.startsWith("https://")) {
                driver.get(url);
            } else {
                 driver.get("http://" + url);
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
                    } else {
                        methodUsed = "By.className( "+ selectedText.substring(1) +" )";
                        elementExists = !driver.findElements(By.className(selectedText)).isEmpty();
                    }
                }
            } catch (Exception ex) {
                System.out.println("No elements found with this css");
            }

            Messages.showMessageDialog(e.getProject(), "Method used: " + methodUsed, "Element exists: " + elementExists, Messages.getInformationIcon());

            driver.quit();
        }
    }
}
// This is an Open Source Project with GPL-3.0 License
// Selenium AI - An AI powered Selenium WebDriver for IntelliJ IDEA
// Project maintained by Ing Russo Giovanni M.