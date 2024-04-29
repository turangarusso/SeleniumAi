package factories;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;

public class MyToolWindowFactory implements ToolWindowFactory {
    DefaultListModel<String> listModel = new DefaultListModel<>();


    // Aggiungi un campo statico per l'istanza singleton
    private static MyToolWindowFactory instance;

    // Rendi il costruttore privato per prevenire l'istanziazione diretta
    private MyToolWindowFactory() {
    }

    // Aggiungi un metodo statico per ottenere l'istanza singleton
    public static MyToolWindowFactory getInstance() {
        if (instance == null) {
            instance = new MyToolWindowFactory();
        }
        return instance;
    }


    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        JPanel panel = new JPanel(new BorderLayout());
        TextFieldWithBrowseButton chromeDriverPathField = new TextFieldWithBrowseButton();
        chromeDriverPathField.addBrowseFolderListener("Select", null, project,
                new FileChooserDescriptor(true, false, false, false, false, false));

        chromeDriverPathField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String chromeDriverPath = chromeDriverPathField.getText();
                PropertiesComponent.getInstance().setValue("chromeDriverPath", chromeDriverPath);
            }
        });

        JPanel smallPanel = new JPanel(new BorderLayout());
        smallPanel.add(new JLabel("Chrome Driver Path:"), BorderLayout.WEST);
        smallPanel.add(chromeDriverPathField, BorderLayout.CENTER);
        panel.add(smallPanel, BorderLayout.NORTH);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "Driver", false);
        toolWindow.getContentManager().addContent(content);

        // Add a JList to display the similar elements
        JBList<String> similarElementsList = new JBList<>(listModel);
        panel.add(new JScrollPane(similarElementsList), BorderLayout.CENTER);

    }
    // Add a method to update the list of similar elements
    public void updateSimilarElementsList(ArrayList<String> similarElements) {
        listModel.clear();
        for (String element : similarElements) {
            listModel.addElement(element);
        }
    }
}