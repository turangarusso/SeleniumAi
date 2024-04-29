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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import factories.MyToolWindowFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Selenium extends DumbAwareAction {
    String methodUsed = "";

    @Override
    public void actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        String selectedText = caretModel.getCurrentCaret().getSelectedText();

        if (selectedText == null || selectedText.isEmpty()) {
            return;
        }

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
            if (driver != null) {
                System.out.println("Driver initialized successfully!");
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

            var result = searchForElem(driver, selectedText, e);

            if(!result) {
                System.out.println("finding similar elements...");
                var elements = findSimilar(driver, selectedText);
                Project project = e.getProject();
                if (project != null) {
                    ToolWindow myToolWindow = ToolWindowManager.getInstance(project).getToolWindow("SeleniumAI");
                    if (myToolWindow != null) {
                        myToolWindow.show(null);
                        JPanel panel = new JPanel(new BorderLayout());
                        ContentFactory contentFactory = ContentFactory.getInstance();
                        JBList<String> similarElementsList = new JBList<>(elements);
                        panel.add(new JBLabel("Similar Elements: "), BorderLayout.NORTH);
                        panel.add(new JScrollPane(similarElementsList), BorderLayout.CENTER);

                        // Create new panel for JTextField and JButton
                        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));


                        JTextField textField = new JTextField(20);
                        bottomPanel.add(textField);

                        // create JButton
                        JButton button = new JButton("Search css");
                        bottomPanel.add(button);

                        // add ActionListener at btn
                        button.addActionListener(x -> {
                            String text = textField.getText();
                                var elementFound = searchForElem(driver, text, e);
                                if (elementFound) {
                                    myToolWindow.hide(null);
                                    Messages.showMessageDialog(e.getProject(), "Method used: " + methodUsed, "Result", Messages.getInformationIcon());
                                }
                        });

                        // add bottomPanel to panel
                        panel.add(bottomPanel, BorderLayout.SOUTH);

                        Content content = contentFactory.createContent(panel, "Elements", false);
                        myToolWindow.getContentManager().addContent(content);

                    }
                }
            }else{
                //If you find the element, close the driver
                driver.quit();
            }

        }else{
            Messages.showMessageDialog(e.getProject(), "Url cannot be null", "Error", Messages.getInformationIcon());
        }
    }

    public boolean searchForElem(WebDriver driver, String selectedText, com.intellij.openapi.actionSystem.AnActionEvent e) {
        boolean elementExists = false;

        try {
            if(selectedText.startsWith("#")) {
                methodUsed = "By.id( "+ selectedText +" )";
                elementExists = !driver.findElements(By.id(selectedText.substring(1))).isEmpty();
            } else if(selectedText.startsWith(".")) {
                methodUsed = "By.cssSelector( "+ selectedText +" )";
                elementExists = !driver.findElements(By.cssSelector(selectedText)).isEmpty();
            } else if(selectedText.startsWith("//") || selectedText.startsWith("./")){
                methodUsed = "By.xpath( " + selectedText + " )";
                elementExists = !driver.findElements(By.xpath(selectedText)).isEmpty();
            } else {
                methodUsed = "By.className( "+ selectedText +" )";
                elementExists = !driver.findElements(By.className(selectedText)).isEmpty();
            }
        } catch (Exception ex) {
            System.out.println("No elements found with this css");
        }

        if (!elementExists) {
            try {
                elementExists = !driver.findElements(By.id(selectedText)).isEmpty();
                methodUsed = "By.id( " + selectedText + " )";
            } catch (Exception ex) {
                System.out.println("No elements found with this id");
            }
        }

        if (!elementExists) {
            try {
                elementExists = !driver.findElements(By.tagName(selectedText)).isEmpty();
                methodUsed = "By.tagName( " + selectedText + " )";
            } catch (Exception ex) {
                System.out.println("No elements found with this tagName");
            }
        }
        if (!elementExists) {
            try {
                elementExists = !driver.findElements(By.name(selectedText)).isEmpty();
                methodUsed = "By.name( " + selectedText + " )";
            } catch (Exception ex) {
                System.out.println("No elements found with this name");
            }
        }
        if (!elementExists) {
            try {
                elementExists = !driver.findElements(By.xpath(selectedText)).isEmpty();
                methodUsed = "By.xpath( " + selectedText + " )";
            } catch (Exception ex) {
                System.out.println("No elements found with this xpath");
            }
        }
        if (!elementExists) {
            try {
                elementExists = !driver.findElements(By.linkText(selectedText)).isEmpty();
                methodUsed = "By.linkText( " + selectedText + " )";
            } catch (Exception ex) {
                System.out.println("No elements found with this linkText");
            }
        }

        if(elementExists) {
            Messages.showMessageDialog(e.getProject(), "Method used: " + methodUsed, "Element exists: " + elementExists, Messages.getInformationIcon());
            return true;
        }else {
            return false;
        }
    }

    public ArrayList<String> findSimilar(WebDriver driver, String selectedText) {
        // Find similar elements

        List<WebElement> allElements = driver.findElements(By.cssSelector("*"));
        System.out.println("Elements found Size: " + allElements.size());

        ArrayList<String> similarElements = new ArrayList<>();
        for (WebElement element : allElements) {
            String elementClass = element.getAttribute("class");
            if (elementClass != null && (elementClass.contains(selectedText) || calculateMatchPercentage(element.getAttribute("class"), selectedText) > 50)) {
                similarElements.add("ClassName: "+elementClass);
                System.out.println("New similar element found: " + elementClass);
            }
            if(element.getAttribute("id") != null) {
                if (element.getAttribute("id") != null && (element.getAttribute("id").contains(selectedText) || calculateMatchPercentage(element.getAttribute("id"), selectedText) > 70)) {
                    similarElements.add("id: "+element.getAttribute("id"));
                    similarElements.add("Class (of element id "+element.getAttribute("id")+") is:"+element.getAttribute("class"));
                }
            }
//            if(element != null) {
//                if (element.getAttribute("id") != null && (element.getAttribute("id").contains(selectedText) || calculateMatchPercentage(element.getAttribute("id"), selectedText) > 70)) {
//                    similarElements.add("id: "+element.getAttribute("id"));
//                }
//                if (element.getAttribute("name") != null && (element.getAttribute("name").contains(selectedText) || calculateMatchPercentage(element.getAttribute("name"), selectedText) > 70)){
//                    similarElements.add("name: "+element.getAttribute("name"));
//                }
//                if (element.getAttribute("value") != null && (element.getAttribute("value").contains(selectedText) || calculateMatchPercentage(element.getAttribute("value"), selectedText) > 70)){
//                    similarElements.add("value: "+element.getAttribute("value"));
//                }
//                if (element.getAttribute("placeholder") != null && (element.getAttribute("placeholder").contains(selectedText) || calculateMatchPercentage(element.getAttribute("placeholder"), selectedText) > 70)){
//                    similarElements.add("placeholder: "+element.getAttribute("placeholder"));
//                }
//            }
        }
        // Update the list in the tool window
        return similarElements;
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