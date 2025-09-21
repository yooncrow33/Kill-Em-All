import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class Main extends GameApplication {

    private Entity player = null;
    private boolean canShoot = true;

    private enum ScreenState { MENU, GAME }
    private ScreenState currentState = ScreenState.MENU;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Kill 'Em All");
        settings.setVersion("1.0");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initUI() {
        showMenuScreen();
    }

    private void showMenuScreen() {
        currentState = ScreenState.MENU;

        // 기존 UI 지우기
        FXGL.getGameScene().clearUINodes();

        Text title = FXGL.getUIFactoryService().newText("Kill 'Em All", 48);
        title.setTranslateX(250);
        title.setTranslateY(150);

        Button startBtn = new Button("Start Game");
        startBtn.setOnAction(e -> switchToGame());

        VBox menuBox = new VBox(20, title, startBtn);
        menuBox.setTranslateX(300);
        menuBox.setTranslateY(260);

        FXGL.getGameScene().addUINode(menuBox);

        // 배경도 메뉴용으로 바꾸자
        FXGL.getGameScene().setBackgroundColor(Color.DARKSLATEGRAY);
    }

    private void switchToGame() {
        currentState = ScreenState.GAME;

        // UI와 엔티티 초기화(수동 리셋)
        resetGameManual();

        // 게임 배경
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);

        // 플레이어 생성
        spawnPlayer();

        // 게임용 UI (예: 재시작 버튼, 상태 텍스트)
        Text playingText = FXGL.getUIFactoryService().newText("Game is running! (WASD to move, LMB shoot)", 18);
        playingText.setTranslateX(10);
        playingText.setTranslateY(10);

        Button restartBtn = new Button("Restart");
        restartBtn.setOnAction(e -> {
            // 네 방식대로 강제로 초기화 + 재스폰
            resetGameManual();
            spawnPlayer();
            FXGL.getGameScene().setBackgroundColor(Color.BLACK);
        });

        Button backToMenu = new Button("Back to Menu");
        backToMenu.setOnAction(e -> {
            // 게임 월드를 비우고 메뉴로
            resetGameManual();
            showMenuScreen();
        });

        HBox buttons = new HBox(10, restartBtn, backToMenu);
        buttons.setTranslateX(10);
        buttons.setTranslateY(40);

        FXGL.getGameScene().addUINode(playingText);
        FXGL.getGameScene().addUINode(buttons);
    }

    private void resetGameManual() {
        // 1) 모든 엔티티를 안전하게 삭제 (복사본으로)
        List<com.almasb.fxgl.entity.Entity> copy = new ArrayList<>(FXGL.getGameWorld().getEntities());
        for (com.almasb.fxgl.entity.Entity e : copy) {
            // UI 노드처럼 특정한 엔티티를 남기고 싶으면 여기서 필터링
            e.removeFromWorld();
        }

        // 2) UI 노드 클리어
        FXGL.getGameScene().clearUINodes();

        // 3) 상태 변수 초기화
        player = null;
        canShoot = true;
    }

    private void spawnPlayer() {
        // 기존 플레이어가 있으면 지움
        if (player != null) {
            player.removeFromWorld();
            player = null;
        }

        // 화면 중앙에 원형 플레이어 생성
        player = entityBuilder()
                .at(getAppWidth() / 2.0 - 20, getAppHeight() / 2.0 - 20)
                .viewWithBBox(new Circle(20, Color.DODGERBLUE))
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        // 마우스 좌클릭: 발사
        onBtnDown(MouseButton.PRIMARY, () -> {
            if (currentState != ScreenState.GAME) return null;

            Point2D mousePos = getInput().getMousePositionWorld();
            shootTriangle(mousePos);
            return null;
        });

        // 이동 (직접 translate 사용 — 네 방식)
        onKey(KeyCode.W, () -> {
            if (player != null) player.translateY(-5);
            return null;
        });
        onKey(KeyCode.S, () -> {
            if (player != null) player.translateY(5);
            return null;
        });
        onKey(KeyCode.A, () -> {
            if (player != null) player.translateX(-5);
            return null;
        });
        onKey(KeyCode.D, () -> {
            if (player != null) player.translateX(5);
            return null;
        });

        // 배경 즉시 변경 테스트 (space)
        onKeyDown(KeyCode.SPACE, () -> {
            if (currentState == ScreenState.GAME) {
                FXGL.getGameScene().setBackgroundColor(Color.DARKGREEN);
            }
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        // 플레이어 경계 체크
        if (player != null) {
            if (player.getX() < 0) player.setX(0);
            if (player.getX() > getAppWidth() - 40) player.setX(getAppWidth() - 40);

            if (player.getY() < 0) player.setY(0);
            if (player.getY() > getAppHeight() - 40) player.setY(getAppHeight() - 40);
        }
    }

    private void shootTriangle(Point2D target) {
        if (!canShoot || currentState != ScreenState.GAME || player == null) return;

        canShoot = false;
        FXGL.runOnce(() -> canShoot = true, Duration.seconds(0.1));

        Point2D origin = new Point2D(
                player.getX() + 20, // center x
                player.getY() + 20  // center y
        );

        Point2D direction = target.subtract(origin).normalize().multiply(600);

        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(
                0.0, -20.0,
                10.0, 15.0,
                -10.0, 15.0
        );
        triangle.setFill(Color.RED);

        com.almasb.fxgl.entity.Entity projectile = entityBuilder()
                .at(origin)
                .view(triangle)
                .with(new ProjectileComponent(direction, 1500))
                .buildAndAttach();

        double angle = Math.toDegrees(Math.atan2(direction.getY(), direction.getX())) - 90;
        projectile.setRotation(angle);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
