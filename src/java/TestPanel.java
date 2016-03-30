import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.awt.Dimension;
import java.awt.Toolkit;

public class TestPanel extends JFrame {
    public TestPanel() {
    
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       setTitle("Flight Sim");
       setSize(screenSize.width, screenSize.height -20);
       setLocationRelativeTo(null);
       setDefaultCloseOperation(EXIT_ON_CLOSE);    
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable()
        {
        
            public void run() {                
	        TestPanel ex = new TestPanel();
	        
	        DeanFly aDeanFly = new DeanFly();
	        aDeanFly.init();
	        ex.add(aDeanFly);
                ex.setVisible(true);
            }
        });

    }
}