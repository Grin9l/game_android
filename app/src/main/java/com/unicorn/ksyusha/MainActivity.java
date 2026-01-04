package com.unicorn.ksyusha;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            
            gameView = findViewById(R.id.gameView);
            if (gameView == null) {
                Log.e(TAG, "GameView not found in layout!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }
}
