package com.rulerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rulerlibrary.RulerView;
import com.rulerlibrary.SizeViewValueChangeListener;

public class MainActivity extends AppCompatActivity {

    private RulerView mSizeViewKg;
    private RulerView mSizeViewCm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSizeViewKg = (RulerView) findViewById(R.id.sizeview_kg);
        mSizeViewCm = (RulerView) findViewById(R.id.sizeview_cm);

        mSizeViewKg.setCurrentValue(89);

        mSizeViewKg.setOnValueChangeListener(new SizeViewValueChangeListener() {
            @Override
            public void onValueChange(int value) {

            }
        });

        mSizeViewCm.setOnValueChangeListener(new SizeViewValueChangeListener() {
            @Override
            public void onValueChange(int value) {

            }
        });

    }
}
