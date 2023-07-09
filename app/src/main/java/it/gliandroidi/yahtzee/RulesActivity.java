package it.gliandroidi.yahtzee;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Semplice activity che mostra in una TextView scrollabile le regole dello Yahtzee (prese dalla pagina italiana di Wikipedia)
public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        TextView tvRulestxt;
        tvRulestxt=findViewById(R.id.tvRulestxt);
        tvRulestxt.setMovementMethod(new ScrollingMovementMethod());
    }
}

