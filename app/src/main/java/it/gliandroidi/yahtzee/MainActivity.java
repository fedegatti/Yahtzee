package it.gliandroidi.yahtzee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Activity principale, contenente bottoni che permettono di accedere alla modalità singleplayer,
// multiplayer (in tal caso, si passerà ad un'activity intermedia per gestire la connessione bluetooth),
// allo storico delle partite giocate e alla sezione contenente le regole dello Yahtzee.
public class MainActivity extends AppCompatActivity {
    Holder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        holder = new Holder();
    }

    private class Holder implements View.OnClickListener{

        EditText etName;
        Button btnSingle, btnMulti, btnRules, btnHistory;

        Holder() {
            etName=findViewById(R.id.etName);
            btnSingle=findViewById(R.id.btnSingle);
            btnMulti=findViewById(R.id.btnMulti);
            btnHistory=findViewById(R.id.btnHistory);
            btnRules=findViewById(R.id.btnRules);

            btnSingle.setOnClickListener(this);
            btnMulti.setOnClickListener(this);
            btnHistory.setOnClickListener(this);
            btnRules.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (v.getId()== R.id.btnRules){
                Intent intent = new Intent(MainActivity.this, RulesActivity.class);
                startActivity(intent);
            }

            if (v.getId()== R.id.btnSingle){
                if (etName.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, getString(R.string.insert_name), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    intent.putExtra("playerName", etName.getText().toString());
                    intent.putExtra("gameType", 1);
                    startActivity(intent);
                }
            }

            if (v.getId() == R.id.btnMulti) {
                if (etName.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, getString(R.string.insert_name), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                    intent.putExtra("playerName", etName.getText().toString());
                    intent.putExtra("gameType", 2);
                    startActivity(intent);
                }
            }

            if (v.getId()== R.id.btnHistory){
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        }
    }
}
