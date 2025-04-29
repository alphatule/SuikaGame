package com.alex.suikagame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Fruit {
    public enum Type {
        CHERRY(1, "cherry.png"),
        STRAWBERRY(2, "strawberry.png"),
        PLUM(3, "plum.png"),
        ORANGE(4, "orange.png"),
        APPLE_GREEN(5, "apple_green.png"),
        BANANA(6, "banana.png"),
        PEAR(7, "pear.png"),
        LEMON(8, "lemon.png"),
        WATERMELON(9, "watermelon.png");

        public final int size;
        public final String textureFile;

        Type(int size, String textureFile) {
            this.size = size;
            this.textureFile = textureFile;
        }
    }

    private Body body;
    private Texture texture;
    private Type type;

    public Fruit(World world, Type type, float x, float y) {
        this.type = type;
        this.texture = new Texture("fruits/" + type.textureFile);

        // Crear cuerpo físico
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x, y);

        body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(type.size * 2 / 32f); // ajusta al tamaño

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.restitution = 0.2f;

        body.createFixture(fixtureDef);
        body.setUserData(this);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        Vector2 pos = body.getPosition();
        float sizeMeters = type.size * 4 / 32f; // tamaño en metros

        batch.draw(texture,
            pos.x - sizeMeters / 2,
            pos.y - sizeMeters / 2,
            sizeMeters,
            sizeMeters
        );
    }


    public Type getType() {
        return type;
    }

    public Body getBody() {
        return body;
    }

    public void dispose() {
        texture.dispose();
    }
}
