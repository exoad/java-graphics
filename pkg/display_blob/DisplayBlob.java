import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Displays information in a blob like manner.
 *
 * @author Jack Meng
 * @since 1.0
 * @version 1.0
 */
public class DisplayBlob extends JFrame implements Runnable {
    public static class Blob extends JPanel {
        private JLabel name;
        private JEditorPane [] contexts;
        private String blob;
        private String[] presetContexts;
        
        public Blob(Border e,  String blobName, String ... presets) {
            super();
            
            this.blob = blobName;
            this.presetContexts = presets;
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(e);
            
            name = new JLabel(blobName);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            contexts = new JEditorPane[presets.length];
                        
            add(name);
            
            for(int i = 0; i < contexts.length; i++) {
                contexts[i] = new JEditorPane();
                contexts[i].setContentType("text/html");
                contexts[i].setAutoscrolls(true);
                contexts[i].setEditable(false);
                contexts[i].setText(presets[i].toString());
                contexts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                add(contexts[i]);
            }            
        }
    }

    private JPanel masterPanel;
    
    public DisplayBlob(Blob ... presets) {
        super();
        
        setTitle("Display Blob ~ exoad");
        setPreferredSize(new Dimension(400, 550));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        masterPanel = new JPanel();
        masterPanel.setPreferredSize(getPreferredSize());
        masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
        
        JLabel l = new JLabel("Blobs Present: " + presets.length);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        masterPanel.add(l);
        
        for(Blob e : presets) {
            e.setAlignmentX(Component.CENTER_ALIGNMENT);
            e.setPreferredSize(new Dimension(getPreferredSize().width, e.getPreferredSize().height));
            masterPanel.add(e);
        }
                
        JScrollPane jsp = new JScrollPane(masterPanel);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        getContentPane().add(jsp);
    }
    
    
    @Override
    public void run() {
        runRelativeTo(null);
    }
    
    public void runRelativeTo(JComponent j) {
        pack();
        setLocationRelativeTo(j);
        setVisible(true);
    }
}