import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends JPanel {

    JFrame frame = new JFrame("alpha 1.0");

    private ScheduledExecutorService executor;
    private long lastTime;

    public static boolean moveUp, moveDown, moveLeft, moveRight;
    public static int mouseX, mouseY;
    public static double playerX, playerY;

    private boolean isResizing = false;
    private int virtualWidth = 1920;
    private int virtualHeight = 1080;

    int coin = 0;

    GM gm = new GM();

    Main() {
        load();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        frame.setPreferredSize((new Dimension(1280,720)));
        setFocusable(true);
        //frame.setUndecorated(true);

        //GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        //graphicsDevice.setFullScreenWindow(frame);

        frame.add(this);
        frame.setVisible(true);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setFocusable(true);
        frame.requestFocus();
        frame.pack();

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) moveUp = true;
                if (key == KeyEvent.VK_S) moveDown = true;
                if (key == KeyEvent.VK_A) moveLeft = true;
                if (key == KeyEvent.VK_D) moveRight = true;
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    save();
                    System.exit(0);
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_W) moveUp = false;
                if (key == KeyEvent.VK_S) moveDown = false;
                if (key == KeyEvent.VK_A) moveLeft = false;
                if (key == KeyEvent.VK_D) moveRight = false;
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //if (e.getButton() == MouseEvent.BUTTON1);
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        this.addComponentListener(new ComponentAdapter() {
              @Override
              public void componentResized(ComponentEvent e) {

                  if (isResizing) return; // 이미 크기 조정 중이면 무시

                  int currentW = getWidth();
                  int currentH = getHeight();

                  // 현재 비율 계산
                  double ratio = (double) currentW / currentH;
                  double targetRatio = 16.0 / 9.0;

                  if (Math.abs(ratio - targetRatio) > 0.01) {
                      isResizing = true;
                      // 비율이 16:9에서 벗어났다면, 16:9를 유지하도록 강제 조정
                      int newH = (int) (currentW / targetRatio); // 가로 기준 세로 조정
                      // int newW = (int) (currentH * targetRatio); // 세로 기준 가로 조정

                      // 크기 재설정. setSize는 내부에서 다시 componentResized를 호출할 수 있으므로 주의.
                      // 간단한 예시로만 이해하시고, 실제 구현 시 무한 루프에 빠지지 않도록 플래그를 사용해야 함.
                      frame.setSize(currentW, newH);

                      isResizing = false;
                  }
              }
          });

        startGameLoop();
    }

    private void startGameLoop() {
        executor = Executors.newSingleThreadScheduledExecutor();
        lastTime = System.nanoTime();

        executor.scheduleAtFixedRate(() -> {
            long now = System.nanoTime();
            double deltaTime = (now - lastTime) / 1_000_000_000.0; // 초 단위
            lastTime = now;

            update(deltaTime);

            SwingUtilities.invokeLater(this::repaint);

        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void update(double deltaTime) {
        if (moveUp) playerY -= 300 * deltaTime;
        if (moveDown) playerY += 300 * deltaTime;
        if (moveLeft) playerX -= 300 * deltaTime;
        if (moveRight) playerX += 300 * deltaTime;
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("coin", String.valueOf(coin));
        try (FileOutputStream out = new FileOutputStream("save.properties")) {
            props.store(out, "User Save");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "저장 실패: " + e.getMessage());
        }
    }

    public void load() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("save.properties")) {
            props.load(in);
            coin = Integer.parseInt(props.getProperty("coin", "10"));
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "세이브 파일 인식 실패");
        }

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D d2 = (Graphics2D) g;

        int windowWidth = getWidth();
        int windowHeight = getHeight();

        // 기준 해상도와 실제 창 크기 비율
        double scaleX = windowWidth / (double) virtualWidth;
        double scaleY = windowHeight / (double) virtualHeight;

        // 두 비율 중 작은 걸 선택 (aspect ratio 유지)
        double scale = Math.min(scaleX, scaleY);

        // 중앙 정렬을 위해 여백 계산
        int xOffset = (int) ((windowWidth - virtualWidth * scale) / 2);
        int yOffset = (int) ((windowHeight - virtualHeight * scale) / 2);

        // 스케일 + 이동 적용
        d2.translate(xOffset, yOffset);
        d2.scale(scale, scale);

        gm.renderWapon(d2);
        gm.renderPlayer(g);
        g.setColor(Color.black);
        g.drawString(mouseX + "/" + mouseY,200, 200);
        ;
    }

    public static void main(String[] args) {
        new Main();
    }
}