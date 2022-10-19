import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;

public class DisplayBlobTest {
    public static void main(String ... args) {
        System.setProperty("sun.java2d.opengl", "true");
        
        DisplayBlob.Blob[] blobs = new DisplayBlob.Blob[AudioSystem.getMixerInfo().length];
        
        int h = 0;
        for(Mixer.Info i : AudioSystem.getMixerInfo()) {
            blobs[h] = new DisplayBlob.Blob(BorderFactory.createLineBorder(Color.GRAY, 1), i.getName(), i.getDescription(), i.getVersion(), i.getVendor());
            h++;
        }
        
        DisplayBlob dp = new DisplayBlob(blobs);
        dp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        SwingUtilities.invokeLater(dp::run);
    }
}