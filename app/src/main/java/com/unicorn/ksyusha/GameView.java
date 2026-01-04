package com.unicorn.ksyusha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    
    private static final String TAG = "GameView";
    private GameThread gameThread;
    private boolean isGameRunning = false;
    private boolean isGameOver = false;
    private boolean isPaused = true;
    
    // Игрок (единорог Ксюша)
    private float playerX;
    private float playerY;
    private float playerSize = 80;
    private int playerLane = 1; // 0 = лево, 1 = центр, 2 = право
    private float[] lanePositions = new float[3];
    
    // Враги
    private ArrayList<Enemy> enemies;
    private Random random;
    private int enemySpawnTimer = 0;
    private int enemySpawnInterval = 120; // кадры
    
    // Игровые параметры
    private int score = 0;
    private float gameSpeed = 5f;
    private Paint paint;
    private Paint textPaint;
    private Paint playerPaint;
    private Paint enemyPaint;
    
    // Размеры экрана
    private int screenWidth;
    private int screenHeight;
    
    public GameView(Context context) {
        super(context);
        Log.d(TAG, "=== GameView constructor START ===");
        
        try {
            getHolder().addCallback(this);
            setFocusable(true);
            Log.d(TAG, "SurfaceHolder callback set");
            
            paint = new Paint();
            paint.setAntiAlias(true);
            
            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
            
            playerPaint = new Paint();
            playerPaint.setColor(Color.parseColor("#FF69B4")); // Розовый для единорога
            playerPaint.setAntiAlias(true);
            
            enemyPaint = new Paint();
            enemyPaint.setColor(Color.parseColor("#FF4444")); // Красный для врагов
            enemyPaint.setAntiAlias(true);
            
            enemies = new ArrayList<>();
            random = new Random();
            
            Log.d(TAG, "GameView created successfully");
        } catch (Exception e) {
            Log.e(TAG, "ERROR in GameView constructor", e);
            e.printStackTrace();
        }
        
        Log.d(TAG, "=== GameView constructor END ===");
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        // Получаем размеры экрана
        screenWidth = getWidth();
        screenHeight = getHeight();
        
        // Проверка на валидность размеров
        if (screenWidth <= 0 || screenHeight <= 0) {
            // Если размеры еще не установлены, используем значения по умолчанию
            screenWidth = 1080;
            screenHeight = 1920;
            Log.w(TAG, "Using default screen size: " + screenWidth + "x" + screenHeight);
        } else {
            Log.d(TAG, "Screen size: " + screenWidth + "x" + screenHeight);
        }
        
        // Инициализация позиций дорожек
        float laneWidth = screenWidth / 3f;
        lanePositions[0] = laneWidth / 2f;
        lanePositions[1] = laneWidth + laneWidth / 2f;
        lanePositions[2] = laneWidth * 2 + laneWidth / 2f;
        
        // Начальная позиция игрока
        playerX = lanePositions[1];
        playerY = screenHeight - 200;
        
        // Запускаем игру только если еще не запущена
        if (!isGameRunning) {
            startGame();
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: " + width + "x" + height);
        screenWidth = width;
        screenHeight = height;
        
        if (screenWidth > 0 && screenHeight > 0) {
            float laneWidth = screenWidth / 3f;
            lanePositions[0] = laneWidth / 2f;
            lanePositions[1] = laneWidth + laneWidth / 2f;
            lanePositions[2] = laneWidth * 2 + laneWidth / 2f;
            
            playerX = lanePositions[playerLane];
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        stopGame();
    }
    
    private void startGame() {
        Log.d(TAG, "startGame");
        // Останавливаем предыдущий поток, если он существует
        if (gameThread != null && gameThread.isAlive()) {
            stopGame();
        }
        
        isGameRunning = true;
        isGameOver = false;
        isPaused = true;
        score = 0;
        enemies.clear();
        enemySpawnTimer = 0;
        gameSpeed = 5f;
        playerLane = 1;
        if (lanePositions != null && lanePositions.length > 1) {
            playerX = lanePositions[playerLane];
        }
        
        try {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
            Log.d(TAG, "GameThread started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting game thread", e);
            e.printStackTrace();
            isGameRunning = false;
        }
    }
    
    private void stopGame() {
        Log.d(TAG, "stopGame");
        if (gameThread != null) {
            boolean retry = true;
            gameThread.setRunning(false);
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void pause() {
        isPaused = true;
    }
    
    public void resume() {
        if (isGameRunning && !isGameOver) {
            isPaused = false;
        }
    }
    
    public void update() {
        if (isPaused || isGameOver) {
            return;
        }
        
        // Обновление позиции игрока (плавное движение по дорожкам)
        float targetX = lanePositions[playerLane];
        playerX += (targetX - playerX) * 0.2f;
        
        // Спавн врагов
        enemySpawnTimer++;
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnEnemy();
            enemySpawnTimer = 0;
            // Увеличиваем сложность
            enemySpawnInterval = Math.max(60, enemySpawnInterval - 1);
            gameSpeed = Math.min(12f, gameSpeed + 0.05f);
        }
        
        // Обновление врагов
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.y += gameSpeed;
            
            // Удаление врагов за экраном
            if (enemy.y > screenHeight) {
                enemies.remove(i);
                score += 10;
                continue;
            }
            
            // Проверка столкновения
            if (checkCollision(enemy)) {
                isGameOver = true;
                isPaused = true;
            }
        }
    }
    
    private void spawnEnemy() {
        int lane = random.nextInt(3);
        float x = lanePositions[lane];
        float y = -100;
        enemies.add(new Enemy(x, y));
    }
    
    private boolean checkCollision(Enemy enemy) {
        float distance = (float) Math.sqrt(
            Math.pow(playerX - enemy.x, 2) + 
            Math.pow(playerY - enemy.y, 2)
        );
        return distance < (playerSize + enemy.size) / 2f;
    }
    
    public void draw(Canvas canvas) {
        // Размеры экрана могут быть не установлены при первом вызове
        if (screenWidth <= 0 || screenHeight <= 0) {
            screenWidth = getWidth();
            screenHeight = getHeight();
            if (screenWidth <= 0 || screenHeight <= 0) {
                screenWidth = 1080;
                screenHeight = 1920;
            }
            // Обновляем позиции дорожек
            float laneWidth = screenWidth / 3f;
            lanePositions[0] = laneWidth / 2f;
            lanePositions[1] = laneWidth + laneWidth / 2f;
            lanePositions[2] = laneWidth * 2 + laneWidth / 2f;
            if (playerX == 0) {
                playerX = lanePositions[1];
            }
            if (playerY == 0) {
                playerY = screenHeight - 200;
            }
        }
        
        if (canvas == null) {
            return;
        }
        
        // Фон
        canvas.drawColor(Color.parseColor("#87CEEB")); // Небесно-голубой
        
        // Дорожки
        paint.setColor(Color.parseColor("#90EE90")); // Светло-зеленый
        float laneWidth = screenWidth / 3f;
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(i * laneWidth, 0, i * laneWidth, screenHeight, paint);
        }
        
        // Враги
        for (Enemy enemy : enemies) {
            drawEnemy(canvas, enemy.x, enemy.y, enemy.size);
        }
        
        // Игрок (единорог Ксюша)
        drawUnicorn(canvas, playerX, playerY, playerSize);
        
        // Очки
        textPaint.setTextSize(50);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Очки: " + score, screenWidth / 2f, 80, textPaint);
        
        // Экран паузы/старта
        if (isPaused && !isGameOver) {
            paint.setColor(Color.parseColor("#80000000")); // Полупрозрачный черный
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            textPaint.setTextSize(70);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("Нажмите, чтобы начать", screenWidth / 2f, screenHeight / 2f, textPaint);
        }
        
        // Экран окончания игры
        if (isGameOver) {
            paint.setColor(Color.parseColor("#80000000"));
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            textPaint.setTextSize(80);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("Игра окончена!", screenWidth / 2f, screenHeight / 2f - 100, textPaint);
            textPaint.setTextSize(60);
            canvas.drawText("Очки: " + score, screenWidth / 2f, screenHeight / 2f, textPaint);
            textPaint.setTextSize(50);
            canvas.drawText("Нажмите, чтобы начать заново", screenWidth / 2f, screenHeight / 2f + 100, textPaint);
        }
    }
    
    private void drawUnicorn(Canvas canvas, float x, float y, float size) {
        // Тело единорога (розовый овал)
        RectF body = new RectF(x - size/2, y - size/3, x + size/2, y + size/2);
        playerPaint.setColor(Color.parseColor("#FFB6C1")); // Светло-розовый
        canvas.drawOval(body, playerPaint);
        
        // Голова
        RectF head = new RectF(x - size/3, y - size, x + size/3, y - size/3);
        canvas.drawOval(head, playerPaint);
        
        // Рог (золотой треугольник)
        Path horn = new Path();
        horn.moveTo(x, y - size);
        horn.lineTo(x - size/6, y - size * 1.3f);
        horn.lineTo(x + size/6, y - size * 1.3f);
        horn.close();
        paint.setColor(Color.parseColor("#FFD700")); // Золотой
        canvas.drawPath(horn, paint);
        
        // Глаз
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x - size/8, y - size * 0.7f, size/20, paint);
        
        // Грива (радужная)
        int[] colors = {
            Color.parseColor("#FF0000"), // Красный
            Color.parseColor("#FF7F00"), // Оранжевый
            Color.parseColor("#FFFF00"), // Желтый
            Color.parseColor("#00FF00"), // Зеленый
            Color.parseColor("#0000FF"), // Синий
            Color.parseColor("#4B0082"), // Индиго
            Color.parseColor("#9400D3")  // Фиолетовый
        };
        for (int i = 0; i < 5; i++) {
            paint.setColor(colors[i % colors.length]);
            canvas.drawCircle(x - size/4 + i * size/20, y - size * 0.8f, size/15, paint);
        }
    }
    
    private void drawEnemy(Canvas canvas, float x, float y, float size) {
        // Враг - красный круг с шипами
        enemyPaint.setColor(Color.parseColor("#FF4444"));
        canvas.drawCircle(x, y, size/2, enemyPaint);
        
        // Шипы
        paint.setColor(Color.parseColor("#CC0000"));
        for (int i = 0; i < 8; i++) {
            float angle = (float) (i * Math.PI * 2 / 8);
            float spikeX = x + (float) Math.cos(angle) * size/2;
            float spikeY = y + (float) Math.sin(angle) * size/2;
            canvas.drawCircle(spikeX, spikeY, size/8, paint);
        }
        
        // Глаза
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x - size/6, y - size/6, size/12, paint);
        canvas.drawCircle(x + size/6, y - size/6, size/12, paint);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x - size/6, y - size/6, size/20, paint);
        canvas.drawCircle(x + size/6, y - size/6, size/20, paint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                // Перезапуск игры
                startGame();
                return true;
            }
            
            if (isPaused) {
                // Начало игры
                isPaused = false;
                return true;
            }
            
            // Переключение дорожек
            float touchX = event.getX();
            float laneWidth = screenWidth / 3f;
            
            if (touchX < laneWidth) {
                playerLane = 0; // Левая дорожка
            } else if (touchX < laneWidth * 2) {
                playerLane = 1; // Центральная дорожка
            } else {
                playerLane = 2; // Правая дорожка
            }
            
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private class Enemy {
        float x;
        float y;
        float size = 60;
        
        Enemy(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private GameView gameView;
        private boolean running;
        
        public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
            this.surfaceHolder = surfaceHolder;
            this.gameView = gameView;
        }
        
        public void setRunning(boolean running) {
            this.running = running;
        }
        
        @Override
        public void run() {
            Log.d(TAG, "GameThread run() started");
            Canvas canvas;
            int frameCount = 0;
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            gameView.update();
                            gameView.draw(canvas);
                            frameCount++;
                            if (frameCount % 60 == 0) {
                                Log.d(TAG, "GameThread: drawn " + frameCount + " frames");
                            }
                        }
                    } else {
                        if (frameCount == 0) {
                            Log.w(TAG, "GameThread: canvas is null on first frame");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "ERROR in game loop", e);
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            Log.e(TAG, "ERROR unlocking canvas", e);
                        }
                    }
                }
                
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    Log.w(TAG, "GameThread interrupted");
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "GameThread run() ended. Total frames: " + frameCount);
        }
    }
}
