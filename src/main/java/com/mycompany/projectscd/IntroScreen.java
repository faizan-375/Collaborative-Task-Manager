package com.mycompany.projectscd;

import javax.swing.*;
import java.awt.*;

public class IntroScreen extends JWindow {

    public IntroScreen(Runnable onComplete) {
        // Full screen transparent window concept or just maximized
        // Using JWindow for undecorated splash feel

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);

        setLayout(new BorderLayout());

        // Add the animation panel
        // When animation is done: dispose self -> run the callback (open dashboard)
        IntroPanel anim = new IntroPanel(() -> {
            dispose();
            if (onComplete != null) {
                onComplete.run();
            }
        });

        add(anim, BorderLayout.CENTER);
    }
}
