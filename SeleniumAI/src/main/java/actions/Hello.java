package actions;

import com.intellij.openapi.actionSystem.AnAction;

public class Hello extends AnAction {
    @Override
    public void actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent e) {
        System.out.println("Hello World");
    }
}
