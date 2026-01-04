package com.unicorn.ksyusha;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== onCreate START ===");
        
        try {
            Log.d(TAG, "Setting content view...");
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Content view set");
            
            Log.d(TAG, "Creating GameView programmatically...");
            // Создаем GameView программно вместо использования в XML
            gameView = new GameView(this);
            FrameLayout container = findViewById(R.id.container);
            if (container != null) {
                container.addView(gameView);
                Log.d(TAG, "GameView created and added to container successfully");
            } else {
                Log.e(TAG, "ERROR: Container not found!");
                Toast.makeText(this, "Ошибка: Контейнер не найден!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR in onCreate", e);
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при запуске: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        
        Log.d(TAG, "=== onCreate END ===");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (gameView != null) {
            gameView.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (gameView != null) {
            gameView.pause();
        }
    }
}
