package kr.re.keti.mobiussampleapp_v25;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ToggleButton;

import static kr.re.keti.mobiussampleapp_v25.R.layout.activity_start;

public class StartActivity extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_start);
        intent = new Intent(this,MainActivity.class);
        ToggleButton button = (ToggleButton)findViewById(R.id.btn_intent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);

            }
        });

    }

}
