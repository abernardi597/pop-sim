package example.repel;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import net.popsim.src.simu.*;

public class RWorld extends World {

    public RWorld(Simulation simulation, Context context) {
        super(simulation, context);
    }

    @Override
    public void init() {
        for (int i = 0; i < 1024; i++) {
            REntity ne = new REntity(this);
            ne.setPosition(getWidth() / 2, getHeight() / 2);
            mEntities.add(ne);
        }
        System.out.println("hi - from A");
        mSimulation.getCanvas().setOnKeyTyped(this::handle);
        mSimulation.getCanvas().setOnMouseClicked(this::handle);
        super.init();
    }

    @Override
    public void render(GraphicsContext gfx) {
        gfx.setFill(Color.BLACK);
        gfx.fillRect(0, 0, getWidth(), getHeight());
        super.render(gfx);
    }

    public void handle(KeyEvent event) {
        switch (event.getCharacter()) {
            case "p":
                if (mSimulation.isPaused())
                    mSimulation.resume();
                else
                    mSimulation.pause();
            default:
                System.out.println("Typed: " + event.getCharacter());
        }
    }

    public void handle(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            REntity ne = new REntity(this);
            ne.setPosition(event.getX(), event.getY());
            mEntities.add(ne);
            System.out.println(mEntities.size());
        }
    }
}