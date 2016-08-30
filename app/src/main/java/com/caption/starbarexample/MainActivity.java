package com.caption.starbarexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.caption.starbarexample.widget.StarBarView;

public class MainActivity extends AppCompatActivity {

    private StarBarView mStarbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStarbar = (StarBarView) findViewById(R.id.sbv_starbar);

        //拿到当前星星数量
        mStarbar.getStarRating();
    }
}
