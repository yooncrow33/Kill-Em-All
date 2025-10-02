import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class GM {
    public void renderPlayer (Graphics g) {
        g.setColor(new Color(135, 206, 250));
        g.fillOval((int)Main.playerX, (int)Main.playerY, 80, 80);
    }
    public void renderWapon (Graphics2D d2) {
        double angle = Math.atan2(Main.mouseY - (Main.playerY + 40), Main.mouseX - (Main.playerX + 40));
        AffineTransform oldTransform = d2.getTransform();
        d2.setColor(Color.lightGray);
        d2.translate(Main.playerX + 40, Main.playerY + 40);
        d2.rotate(angle);
        d2.fillOval(15, -25, 80, 50);
        d2.setTransform(oldTransform);
    }
}
