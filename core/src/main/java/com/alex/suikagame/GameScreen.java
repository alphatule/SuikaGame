package com.alex.suikagame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Texture img;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

    private float accumulator = 0f;

    private Array<Fruit> fruits;

    // Preview
    private Fruit.Type nextFruitType;
    private Texture nextFruitTexture;


    @Override
    public void show() {
        batch = new SpriteBatch();

        // Configura la cámara y el mundo físico
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800 / 32f, 480 / 32f); // Tamaño del mundo en metros

        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();

        createFloor(); // Crea el suelo

        fruits = new Array<>();
        spawnFruit(Fruit.Type.CHERRY);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();

                if (a instanceof Fruit && b instanceof Fruit) {
                    Fruit f1 = (Fruit) a;
                    Fruit f2 = (Fruit) b;

                    // Solo si son del mismo tipo
                    if (f1.getType() == f2.getType()) {
                        handleMerge(f1, f2);
                    }
                }
            }

            @Override public void endContact(Contact contact) {}
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                spawnRandomFruit();
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    spawnRandomFruit();
                    return true;
                }
                return false;
            }
        });

        generateNextFruit();

    }

    private void generateNextFruit() {
        Fruit.Type[] types = Fruit.Type.values();
        int maxIndex = 3; // solo frutas pequeñas
        int random = (int)(Math.random() * maxIndex);
        nextFruitType = types[random];
        nextFruitTexture = new Texture("fruits/" + nextFruitType.textureFile);
    }

    private void disposeNextFruit() {
        if (nextFruitTexture != null) {
            nextFruitTexture.dispose();
        }
    }


    private void spawnRandomFruit() {
        // Usar la fruta que estaba en espera
        Fruit.Type type = nextFruitType;

        float x = 400 / 32f;
        float y = 430 / 32f;

        spawnFruitAt(type, x, y);

        // Generar nueva fruta en espera
        disposeNextFruit();
        generateNextFruit();
    }


    private void handleMerge(Fruit f1, Fruit f2) {
        // Evita que se fusione más de una vez por contacto
        if (!fruits.contains(f1, true) || !fruits.contains(f2, true)) return;

        // Crear nueva fruta del siguiente tipo
        Fruit.Type current = f1.getType();
        int nextOrdinal = current.ordinal() + 1;

        if (nextOrdinal < Fruit.Type.values().length) {
            Fruit.Type next = Fruit.Type.values()[nextOrdinal];

            // Posición media de las dos frutas
            float x = (f1.getBody().getPosition().x + f2.getBody().getPosition().x) / 2;
            float y = (f1.getBody().getPosition().y + f2.getBody().getPosition().y) / 2;

            // Eliminar frutas actuales
            world.destroyBody(f1.getBody());
            world.destroyBody(f2.getBody());
            fruits.removeValue(f1, true);
            fruits.removeValue(f2, true);
            f1.dispose();
            f2.dispose();

            // Crear fruta fusionada
            spawnFruitAt(next, x, y);
        }
    }

    private void spawnFruitAt(Fruit.Type type, float x, float y) {
        Fruit fruit = new Fruit(world, type, x, y);
        fruits.add(fruit);
    }



    private void spawnFruit(Fruit.Type type) {
        Fruit fruit = new Fruit(world, type, 400 / 32f, 400 / 32f); // centro pantalla
        fruits.add(fruit);
    }


    private void createFloor() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(new Vector2(400 / 32f, 50 / 32f)); // en metros
        bodyDef.type = BodyDef.BodyType.StaticBody;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(400 / 32f, 10 / 32f); // ancho y alto

        body.createFixture(shape, 0.0f);
        shape.dispose();
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Actualizar mundo físico
        stepWorld(delta);
        camera.update();
        debugRenderer.render(world, camera.combined);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Fruit fruit : fruits) {
            fruit.render(batch);
        }

        if (nextFruitTexture != null) {
            float scale = 6; // aumenta este valor para agrandar

            float size = nextFruitType.size * scale;
            float x = 400 - size / 2;
            float y = 450 - size / 2;

            batch.draw(nextFruitTexture,
                x / 32f, y / 32f,
                size / 32f, size / 32f
            );
        }

        batch.end();
    }

    private void stepWorld(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1/60f) {
            world.step(1/60f, 6, 2);
            accumulator -= 1/60f;
        }
    }


    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        disposeNextFruit();
        batch.dispose();
        img.dispose();
    }
}

