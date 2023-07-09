package it.gliandroidi.yahtzee;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

// Funzione che permette di giocare a Yahtzee, sia in modalità singleplayer che multiplayer, dopo l'associazione
// via bluetooth di due dispositivi ad opera di una precedente activity
public class GameActivity extends AppCompatActivity {

    Holder holder;
    // Valori usati per gestire il bluetooth nel multiplayer
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private  String APP_NAME = "Yahtzee";
    final private UUID MY_UUID = UUID.fromString("ad129d9c-bc03-11ea-b3de-0242ac130004");

    final static int STATE_CONNECTION_FAILED = 1;
    final static int STATE_CONNECTED = 2;
    final static int STATE_MESSAGE_RECEIVED= 3;
    final static int STATE_CONNECTING = 4;

    SendReceive sendReceive;
    BluetoothAdapter myBTA = BluetoothAdapter.getDefaultAdapter();

    // Attributo che specifica il "tipo" di gioco; sarà 1 nel caso singleplayer, e 2 nel caso multiplayer;
    // viene passato dalle activity precedenti
    private int gameType;

    // Nome del giocatore
    private String playerName;

    // Nome dello sfidante, ottenuto tramite bluetooth
    private String opponentName;

    // Booleano che indica se è o meno il mio turno (sarà sempre true nel singleplayer)
    private boolean myTurn = true;
    boolean gf;

    // Per salvare il risultato, occorrerà interagire con il database
    AppResultDatabase appDB;

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int i) {
        gameType = i;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        playerName = name;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String name) {
        opponentName = name;
    }

    public boolean getMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean b) {
        myTurn = b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        appDB = AppResultDatabase.getInstance(GameActivity.this);
        // Inserisce i dati passati dalle activity precedenti
        Intent intent = getIntent();
        setGameType(intent.getIntExtra("gameType", 0));
        setPlayerName(intent.getStringExtra("playerName"));
        gf = intent.getBooleanExtra("goesFirst", true);

        holder= new Holder();

        BluetoothDevice bdDevice= intent.getParcelableExtra("device");

        if(myBTA.isEnabled() && gameType == 2) {


            if (bdDevice != null ) {
                //FACCIO PARTIRE IL CLIENT
                bdDevice.createBond();

                ClientClass clientClass = new ClientClass(bdDevice);
                clientClass.start();

            } else {
                // FACCIO PARTIRE IL SERVER

                ServerClass serverClass = new ServerClass();
                serverClass.start();

            }
            holder.tvVS.setText(R.string.vs);

        }
    }

    private class Holder implements View.OnClickListener {
        final Button btnRoll, btnConfirm;
        final ImageView ivOnes, ivTwos,ivThrees, ivFours, ivFives, ivSixs, ivTris, ivPoker, ivFull, ivSmallStraight, ivLargeStraight, ivYahtzee, ivChance;
        final ImageView ivDice1, ivDice2, ivDice3, ivDice4, ivDice5;
        final TextView tvVS, tvPlayer, tvBonus, tvScore, tvOpponent, tvOne1,tvOne2, tvTwo1, tvTwo2, tvThree1, tvFour1, tvFive1, tvSix1, tvBonus1, tvTris1, tvPoker1, tvSmall1, tvLarge1, tvFull1, tvYahtzee1, tvChance1,  tvThree2, tvFour2, tvFive2, tvSix2, tvBonus2, tvTris2, tvPoker2, tvSmall2, tvLarge2, tvFull2, tvYahtzee2, tvChance2;

        // Numero di lanci dei dadi già effettuati; arriva ad un massimo di 3, e viene riportato a 0
        // alla fine di ogni turno
        int rolls = 0;

        // Variabile in cui viene salvato il punteggio che dovrà essere confermato
        int point = 0;

        // Numero di turni giocati (massimo 13)
        int turns = 0;

        // Numero di turni giocati dall'avversario
        int oppTurns = 0;

        // Indica se al momento vi è o meno un punteggio che richiede conferma
        boolean pointOk = false;

        // Numero identificativo del punteggio che richiede conferma
        int pointNum = -1;

        // Array che indica per ogni punteggio se esso è già stato giocato o meno
        boolean[] pointsCheck = new boolean[13];

        // Crea un array di entità Dice
        final ArrayList<Dice> dices = new ArrayList<>();

        // Inizializza animazione dei dadi, suoni e vibrazione
        Animation anim;
        final MediaPlayer rollSound = MediaPlayer.create(GameActivity.this, R.raw.dice_roll);
        final MediaPlayer theEntertainer = MediaPlayer.create(GameActivity.this, R.raw.the_entertainer);
        final MediaPlayer lockSound = MediaPlayer.create(GameActivity.this, R.raw.lock_sound);
        final MediaPlayer confirmSound = MediaPlayer.create(GameActivity.this, R.raw.confirmation);
        final MediaPlayer victorySound = MediaPlayer.create(GameActivity.this, R.raw.victory_sound);
        final MediaPlayer loseSound = MediaPlayer.create(GameActivity.this, R.raw.lose_sound);
        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Array di numeri in cui vengono salvati i valori dei dadi attualmente visibili
        final int[] diceNums = new int[5];

        // Inizializza L'entità Scores (Punteggi) del giocatore
        final Scores scores;

        // Inizializza L'entità Scores (Punteggi) dell'avversario
        final Scores oppScores;

        // Indica se si è entrati o meno nella fase conclusiva del gioco (quando sono stati giocati
        // tutti e 13 i turni)
        boolean endGame = false;

        Holder() {

            btnRoll = findViewById(R.id.btnRoll);
            btnConfirm = findViewById(R.id.btnConfirm);
            ivOnes = findViewById(R.id.ivOnes);
            ivTwos = findViewById(R.id.ivTwos);
            ivThrees = findViewById(R.id.ivThrees);
            ivFours = findViewById(R.id.ivFours);
            ivFives = findViewById(R.id.ivFives);
            ivSixs = findViewById(R.id.ivSixs);
            ivTris = findViewById(R.id.ivTris);
            ivPoker = findViewById(R.id.ivPoker);
            ivFull = findViewById(R.id.ivFull);
            ivSmallStraight = findViewById(R.id.ivSmallStraight);
            ivLargeStraight = findViewById(R.id.ivLargeStraight);
            ivYahtzee = findViewById(R.id.ivYahtzee);
            ivChance = findViewById(R.id.ivChance);
            ivDice1=findViewById(R.id.ivDice1);
            ivDice2=findViewById(R.id.ivDice2);
            ivDice3=findViewById(R.id.ivDice3);
            ivDice4=findViewById(R.id.ivDice4);
            ivDice5=findViewById(R.id.ivDice5);
            tvOne1=findViewById(R.id.tvOne1);
            tvOne2=findViewById(R.id.tvOne2);
            tvTwo1=findViewById(R.id.tvTwo1);
            tvTwo2=findViewById(R.id.tvTwo2);
            tvThree1=findViewById(R.id.tvThree1);
            tvThree2=findViewById(R.id.tvThree2);
            tvFour1=findViewById(R.id.tvFour1);
            tvFour2=findViewById(R.id.tvFour2);
            tvFive1=findViewById(R.id.tvFive1);
            tvFive2=findViewById(R.id.tvFive2);
            tvSix1=findViewById(R.id.tvSix1);
            tvSix2=findViewById(R.id.tvSix2);
            tvTris1=findViewById(R.id.tvTris1);
            tvTris2=findViewById(R.id.tvTris2);
            tvPoker1=findViewById(R.id.tvPoker1);
            tvPoker2=findViewById(R.id.tvPoker2);
            tvFull1=findViewById(R.id.tvFull1);
            tvFull2=findViewById(R.id.tvFull2);
            tvSmall1=findViewById(R.id.tvSmall1);
            tvSmall2=findViewById(R.id.tvSmall2);
            tvLarge1=findViewById(R.id.tvLarge1);
            tvLarge2=findViewById(R.id.tvLarge2);
            tvYahtzee1=findViewById(R.id.tvYahtzee1);
            tvYahtzee2=findViewById(R.id.tvYahtzee2);
            tvChance1=findViewById(R.id.tvChance1);
            tvChance2=findViewById(R.id.tvChance2);
            tvBonus1=findViewById(R.id.tvBonus1);
            tvBonus2=findViewById(R.id.tvBonus2);
            tvBonus=findViewById(R.id.tvBonus);
            tvOpponent=findViewById(R.id.tvOpponent);
            tvScore=findViewById(R.id.tvScore);
            tvVS = findViewById(R.id.tvVS);
            tvPlayer = findViewById(R.id.tvPlayer);

            ivOnes.setOnClickListener(this);
            ivTwos.setOnClickListener(this);
            ivThrees.setOnClickListener(this);
            ivFours.setOnClickListener(this);
            ivFives.setOnClickListener(this);
            ivSixs.setOnClickListener(this);
            ivTris.setOnClickListener(this);
            ivPoker.setOnClickListener(this);
            ivFull.setOnClickListener(this);
            ivSmallStraight.setOnClickListener(this);
            ivLargeStraight.setOnClickListener(this);
            ivYahtzee.setOnClickListener(this);
            ivChance.setOnClickListener(this);
            btnRoll.setOnClickListener(this);
            btnConfirm.setOnClickListener(this);
            ivDice1.setOnClickListener(this);
            ivDice2.setOnClickListener(this);
            ivDice3.setOnClickListener(this);
            ivDice4.setOnClickListener(this);
            ivDice5.setOnClickListener(this);

            anim = AnimationUtils.loadAnimation(GameActivity.this, R.anim.shake);
            this.setUpAnimation();

            // Crea i dadi, li mette nell'array e gli dà un iniziale valore placeholder
            if (Locale.getDefault().getDisplayLanguage().equals(Locale.ITALY.getDisplayLanguage())) {
                dices.add(new Dice(0, false, R.drawable.dice_t_caps, R.drawable.dice_t_caps));
                dices.add(new Dice(0, false, R.drawable.dice_i, R.drawable.dice_i));
                dices.add(new Dice(0, false, R.drawable.dice_r, R.drawable.dice_r));
                dices.add(new Dice(0, false, R.drawable.dice_a, R.drawable.dice_a));
                dices.add(new Dice(0, false, R.drawable.dice_exc, R.drawable.dice_exc));
            } else {
                dices.add(new Dice(0, false, R.drawable.dice_r_caps, R.drawable.dice_r_caps));
                dices.add(new Dice(0, false, R.drawable.dice_o, R.drawable.dice_o));
                dices.add(new Dice(0, false, R.drawable.dice_l, R.drawable.dice_l));
                dices.add(new Dice(0, false, R.drawable.dice_l, R.drawable.dice_l));
                dices.add(new Dice(0, false, R.drawable.dice_exc, R.drawable.dice_exc));
            }
            // Mostra i dadi
            for (int i = 0; i < 5; i++) {
                diceNums[i] = dices.get(i).getNumber();
                showDice(i);
            }

            // Per la localizzazione delle icone dei punteggi di piccola e grande scala
            if (Locale.getDefault().getDisplayLanguage().equals(Locale.ITALY.getDisplayLanguage())) {
                ivSmallStraight.setImageResource(R.drawable.point_piccola_scala);
                ivLargeStraight.setImageResource(R.drawable.point_grande_scala);
            }

            // Crea le entità Scores dei giocatori

            scores = new Scores();
            oppScores = new Scores();

            for (int i = 0; i < 13; i++) {
                pointsCheck[i] = false;
            }

            // Fa partire la musica di gioco, con un volume più basso rispetto agli altri effetti sonori
            float vol = (float) (Math.log(50-5)/Math.log(50));
            theEntertainer.setVolume(vol, vol);
            theEntertainer.start();
            theEntertainer.setLooping(true);

            // Prepara la grafica ed i bottoni per il gioco
            btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-rolls) + ")");
            tvScore.setText(getString(R.string.total) + 0);
            tvBonus1.setText(0 + getString(R.string.bonus_max));
            if (getGameType() == 2) {
                tvBonus2.setText(0 + getString(R.string.bonus_max));
                setMyTurn(gf);
                if (getMyTurn() == false) {
                    btnRoll.setText(getString(R.string.opponent_turn));
                    btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorRollFade));
                    btnRoll.setTextColor(getColor(R.color.colorConfirmTextFade));
                }
            }
            tvVS.setText("");
            if (getGameType() == 1) {
                tvPlayer.setText(getString(R.string.player) + getPlayerName());
            } else {
                tvPlayer.setText(getPlayerName());
            }

        }

        @Override
        public void onClick(View v) {

            // Cliccando sulle icone dei punteggi, verrà mostrato un toast che ricorda a cosa corrisponde tale punteggio;
            // dopo il primo lancio, viene inoltro mostrato il punteggio teorico che si avrebbe con i dadi attuali per
            // tale categoria
            if (v.getId() == R.id.ivOnes) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_ones), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[0] == false && endGame == false && getMyTurn() == true) {
                    showPoint("ones");
                }
            }
            if (v.getId() == R.id.ivTwos) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_twos), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[1] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("twos");
                }
            }
            if (v.getId() == R.id.ivThrees) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_threes), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[2] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("threes");
                }
            }
            if (v.getId() == R.id.ivFours) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_fours), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[3] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("fours");
                }
            }
            if (v.getId() == R.id.ivFives) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_fives), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[4] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("fives");
                }
            }
            if (v.getId() == R.id.ivSixs) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_sixs), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[5] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("sixs");
                }
            }
            if (v.getId() == R.id.ivTris) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_tris), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[6] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("tris");
                }
            }
            if (v.getId() == R.id.ivPoker) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_poker), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[7] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("poker");
                }
            }
            if (v.getId() == R.id.ivFull) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_full), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[8] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("full");
                }
            }
            if (v.getId() == R.id.ivSmallStraight) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_small_straight), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[9] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("smallStraight");
                }
            }
            if (v.getId() == R.id.ivLargeStraight) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_large_straight), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[10] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("largeStraight");
                }
            }
            if (v.getId() == R.id.ivYahtzee) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_yahtzee), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[11] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("yahtzee");
                }
            }
            if (v.getId() == R.id.ivChance) {
                Toast.makeText(GameActivity.this, getString(R.string.toast_chance), Toast.LENGTH_SHORT).show();
                if (rolls > 0 && pointsCheck[12] == false && endGame == false  && getMyTurn() == true) {
                    showPoint("chance");
                }
            }

            // I dadi sono cliccabili dopo il primo lancio, per poter essere bloccati/sbloccati a piacimento
            if (v.getId() == R.id.ivDice1) {
                if (rolls > 0 && endGame == false) {
                    if (dices.get(0).getIsLocked() == false) {
                        dices.get(0).lock();
                        lockSound();
                        showDice(0);
                    } else {
                        dices.get(0).unlock();
                        lockSound();
                        showDice(0);
                    }
                }
            }
            if (v.getId() == R.id.ivDice2) {
                if (rolls > 0 && endGame == false) {
                    if (dices.get(1).getIsLocked() == false) {
                        dices.get(1).lock();
                        lockSound();
                        showDice(1);
                    } else {
                        dices.get(1).unlock();
                        lockSound();
                        showDice(1);
                    }
                }
            }
            if (v.getId() == R.id.ivDice3) {
                if (rolls > 0 && endGame == false) {
                    if (dices.get(2).getIsLocked() == false) {
                        dices.get(2).lock();
                        lockSound();
                        showDice(2);
                    } else {
                        dices.get(2).unlock();
                        lockSound();
                        showDice(2);
                    }
                }
            }
            if (v.getId() == R.id.ivDice4) {
                if (rolls > 0 && endGame == false) {
                    if (dices.get(3).getIsLocked() == false) {
                        dices.get(3).lock();
                        lockSound();
                        showDice(3);
                    } else {
                        dices.get(3).unlock();
                        lockSound();
                        showDice(3);
                    }
                }
            }
            if (v.getId() == R.id.ivDice5) {
                if (rolls > 0 && endGame == false) {
                    if (dices.get(4).getIsLocked() == false) {
                        dices.get(4).lock();
                        lockSound();
                        showDice(4);
                    } else {
                        dices.get(4).unlock();
                        lockSound();
                        showDice(4);
                    }
                }
            }

            // Tale bottone fa animare i dadi, ed è usato a fine partita per concludere il gioco.
            // Inoltre, in modalità multiplayer, è usato anche per indicare che non è il proprio turno
            if (v.getId() == R.id.btnRoll) {
                if (rolls < 3 && turns < 13  && getMyTurn() == true) {
                    for (int i = 0; i < 5; i++) {
                        if (dices.get(i).getIsLocked() == false) {
                            dices.get(i).roll();
                            animate(i);
                            if (rollSound.isPlaying()) {
                                rollSound.pause();
                                rollSound.seekTo(0);
                                rollSound.start();
                            } else {
                                rollSound.start();
                            }
                            vib.vibrate(500);
                        }
                        diceNums[i] = dices.get(i).getNumber();
                    }
                    rolls++;
                    btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-rolls) + ")");
                    showPoint("none");
                } else if (turns == 13){
                    if (getGameType() == 1) {
                        victorySound.start();
                        openDialogEndSingle();
                    } else {
                        if (oppTurns == 13) {
                            if (scores.getTotal() > oppScores.getTotal()) {
                                victorySound.start();
                                openDialogEndMulti(0);
                            } else if (scores.getTotal() < oppScores.getTotal()) {
                                loseSound.start();
                                openDialogEndMulti(1);
                            } else {
                                victorySound.start();
                                openDialogEndMulti(2);
                            }
                        } else {
                            Toast.makeText(GameActivity.this, getString(R.string.wait_opp_to_end), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (getMyTurn() == false){
                    Toast.makeText(GameActivity.this, getString(R.string.wait_opp_to_end), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.choose_combination), Toast.LENGTH_LONG).show();
                }
            }

            // Bottone per confermare il punteggio mostrato per una certa categoria; viene attivato solo dopo aver
            // selezionato una categoria da confermare; la conferma segna la fine del turno attuale
            if (v.getId() == R.id.btnConfirm) {
                if (endGame == false  && getMyTurn() == true) {
                    confirmPoint();
                    if (turns < 13 && endTurn == true) {
                        if (getGameType() == 2) {
                            btnRoll.setText(getString(R.string.opponent_turn));
                            btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorRollFade));
                            btnRoll.setTextColor(getColor(R.color.colorConfirmTextFade));
                            setMyTurn(false);
                        } else {
                            btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-rolls) + ")");
                        }
                        if (Locale.getDefault().getDisplayLanguage().equals(Locale.ITALY.getDisplayLanguage())) {
                            dices.set(0, new Dice(0, false, R.drawable.dice_t_caps, R.drawable.dice_t_caps));
                            dices.set(1, new Dice(0, false, R.drawable.dice_i, R.drawable.dice_i));
                            dices.set(2, new Dice(0, false, R.drawable.dice_r, R.drawable.dice_r));
                            dices.set(3, new Dice(0, false, R.drawable.dice_a, R.drawable.dice_a));
                            dices.set(4, new Dice(0, false, R.drawable.dice_exc, R.drawable.dice_exc));
                        } else {
                            dices.set(0, new Dice(0, false, R.drawable.dice_r_caps, R.drawable.dice_r_caps));
                            dices.set(1, new Dice(0, false, R.drawable.dice_o, R.drawable.dice_o));
                            dices.set(2, new Dice(0, false, R.drawable.dice_l, R.drawable.dice_l));
                            dices.set(3, new Dice(0, false, R.drawable.dice_l, R.drawable.dice_l));
                            dices.set(4, new Dice(0, false, R.drawable.dice_exc, R.drawable.dice_exc));
                        }
                        for (int i = 0; i < 5; i++) {
                            diceNums[i] = dices.get(i).getNumber();
                            showDice(i);
                        }
                        endTurn = false;
                    }
                    if (turns == 13) {
                        if (getGameType() == 2 && oppTurns == 13) {
                            btnRoll.setText(getString(R.string.end));
                            btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            endGame = true;
                        } else if (getGameType() == 2 && oppTurns != 13) {
                            btnRoll.setText(getString(R.string.opponent_turn));
                            btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorRollFade));
                            btnRoll.setTextColor(getColor(R.color.colorConfirmTextFade));
                            setMyTurn(false);
                        } else {
                            btnRoll.setText(getString(R.string.end));
                            btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            endGame = true;
                        }
                    }
                }
            }
        }

        boolean endTurn = false;

        // Funzione che scrive il punteggio mostrato per una certa categoria nell'apposito attributo
        // del proprio Score; inoltre, una volta confermato, il punteggio verrà inviato (via bluetooth)
        // all'avversario, in caso di multiplayer
        public void confirmPoint() {
            if (pointOk == true) {
                String btpoint;
                switch (pointNum) {
                    case 0 :
                        pointsCheck[0] = true;
                        scores.putOnes(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvOne1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "a" + "x" + point;
                            } else {
                                btpoint = "a" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 1 :
                        pointsCheck[1] = true;
                        scores.putTwos(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvTwo1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "b" + "x" + point;
                            } else {
                                btpoint = "b" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 2 :
                        pointsCheck[2] = true;
                        scores.putThrees(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvThree1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "c" + "x" + point;
                            } else {
                                btpoint = "c" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 3 :
                        pointsCheck[3] = true;
                        scores.putFours(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvFour1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "d" + "x" + point;
                            } else {
                                btpoint = "d" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 4 :
                        pointsCheck[4] = true;
                        scores.putFives(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvFive1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "e" + "x" + point;
                            } else {
                                btpoint = "e" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 5 :
                        pointsCheck[5] = true;
                        scores.putSixs(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvSix1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "f" + "x" + point;
                            } else {
                                btpoint = "f" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 6 :
                        pointsCheck[6] = true;
                        scores.setTris(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvTris1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "g" + "x" + point;
                            } else {
                                btpoint = "g" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 7 :
                        pointsCheck[7] = true;
                        scores.setPoker(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvPoker1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "h" + "x" + point;
                            } else {
                                btpoint = "h" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 8 :
                        pointsCheck[8] = true;
                        scores.setFull(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvFull1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "i" + "x" + point;
                            } else {
                                btpoint = "i" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 9 :
                        pointsCheck[9] = true;
                        scores.setSmallStraight(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvSmall1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "l" + "x" + point;
                            } else {
                                btpoint = "l" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 10 :
                        pointsCheck[10] = true;
                        scores.setLargeStraight(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvLarge1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "m" + "x" + point;
                            } else {
                                btpoint = "m" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 11 :
                        pointsCheck[11] = true;
                        scores.setYahtzee(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvYahtzee1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "n" + "x" + point;
                            } else {
                                btpoint = "n" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    case 12 :
                        pointsCheck[12] = true;
                        scores.setChance(point);
                        pointOk = false;
                        rolls = 0;
                        turns++;
                        tvChance1.setTextColor(getColor(R.color.colorConfirmText));
                        scores.setTotal(scores.calcTotal());
                        tvScore.setText(getString(R.string.total) + scores.getTotal());
                        if(gameType == 2) {
                            // INVIO PUNTEGGIO AD AVVERSARIO CON AGGIUNTA DI LETTERA IDENTIFICANTE IL TIPO DI COMBINAZIONE
                            if (point >= 10) {
                                btpoint = "o" + "x" + point;
                            } else {
                                btpoint = "o" + point;
                            }
                            sendReceive.write(btpoint.getBytes());
                        }
                        break;
                    default:
                }
                if (confirmSound.isPlaying()) {
                    confirmSound.pause();
                    confirmSound.seekTo(0);
                    confirmSound.start();
                } else {
                    confirmSound.start();
                }
                tvBonus1.setText(scores.getBonus() + getString(R.string.bonus_max));
                if (scores.getBonusReached() == true) {
                    tvBonus.setTextColor(getColor(R.color.colorBonusReached));
                }

                endTurn = true;
                btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorConfirmFade));
                btnConfirm.setTextColor(getColor(R.color.colorConfirmTextFade));
                Log.i("GAME", scores.getOnes() + " " + scores.getTwos() + " " + scores.getThrees() +
                        " " + scores.getFours() + " " + scores.getFives() + " " + scores.getSixs() + "\n" + scores.getBonus() +
                        "\n" + scores.getTris() + " " + scores.getPoker() + " " + scores.getFull() + " " + scores.getSmallStraight() +
                        " " + scores.getLargeStraight() + " " + scores.getYahtzee() + " " + scores.getChance() + "\n" + scores.getTotal());
            }
        }

        // Funzione che mostra nel tabellone il punteggio che si avrebbe con i dadi attuali confermando
        // la categoria di punteggio selezionata; inoltre, salva tale punteggio in una variabile, che sarà
        // poi usata nella conferma. E' qui che viene assegnato l'eventuale bonus di 50 punti dovuto ad
        // un secondo Yahtzee
        public void showPoint(String pointName) {
            int p = 0;
            switch (pointName) {
                case "ones" :
                    p = scores.calcOnes(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvOne1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 0;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "twos" :
                    p = scores.calcTwos(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvTwo1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 1;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "threes" :
                    p = scores.calcThrees(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvThree1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 2;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "fours" :
                    p = scores.calcFours(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvFour1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 3;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "fives" :
                    p = scores.calcFives(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvFive1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 4;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "sixs" :
                    p = scores.calcSixs(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvSix1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 5;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "tris" :
                    p = scores.calcTris(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvTris1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 6;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "poker" :
                    p = scores.calcPoker(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvPoker1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 7;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "full" :
                    p = scores.calcFull(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvFull1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 8;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "smallStraight" :
                    p = scores.calcSmallStraight(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvSmall1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 9;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "largeStraight" :
                    p = scores.calcLargeStraight(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvLarge1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 10;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "yahtzee" :
                    p = scores.calcYahtzee(diceNums);
                    clearPoints();
                    tvYahtzee1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 11;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                case "chance" :
                    p = scores.calcChance(diceNums);
                    if (pointsCheck[11] == true && scores.getYahtzee() > 0 &&
                            diceNums[0] == diceNums[1] && diceNums[0] == diceNums[2] && diceNums[0] == diceNums[3] && diceNums[0] == diceNums[4]) {
                        p = p + 50;
                    }
                    clearPoints();
                    tvChance1.setText(String.valueOf(p));
                    point = p;
                    pointOk = true;
                    pointNum = 12;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimary));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmText));
                    break;
                default:
                    p = 0;
                    clearPoints();
                    point = 0;
                    pointOk = false;
                    pointNum = -1;
                    btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorConfirmFade));
                    btnConfirm.setTextColor(getColor(R.color.colorConfirmTextFade));
            }
        }

        // Semplice funzione che, in caso di cambio di categoria, procede a calcellare tutti i punteggi non confermati
        // dal tabellone
        public void clearPoints() {
            if (pointsCheck[0] == false) {
                tvOne1.setText("");
            }
            if (pointsCheck[1] == false) {
                tvTwo1.setText("");
            }
            if (pointsCheck[2] == false) {
                tvThree1.setText("");
            }
            if (pointsCheck[3] == false) {
                tvFour1.setText("");
            }
            if (pointsCheck[4] == false) {
                tvFive1.setText("");
            }
            if (pointsCheck[5] == false) {
                tvSix1.setText("");
            }
            if (pointsCheck[6] == false) {
                tvTris1.setText("");
            }
            if (pointsCheck[7] == false) {
                tvPoker1.setText("");
            }
            if (pointsCheck[8] == false) {
                tvFull1.setText("");
            }
            if (pointsCheck[9] == false) {
                tvSmall1.setText("");
            }
            if (pointsCheck[10] == false) {
                tvLarge1.setText("");
            }
            if (pointsCheck[11] == false) {
                tvYahtzee1.setText("");
            }
            if (pointsCheck[12] == false) {
                tvChance1.setText("");
            }
        }

        // Gestisce il suono del lock/unlock dei dadi
        private void lockSound() {
            if (lockSound.isPlaying()) {
                lockSound.pause();
                lockSound.seekTo(0);
                lockSound.start();
            } else {
                lockSound.start();
            }
        }

        // Fa partire l'animazione del dadi non bloccati
        private void animate(int i) {
            if (i == 0) {
                ivDice1.startAnimation(anim);
            }
            if (i == 1) {
                ivDice2.startAnimation(anim);
            }
            if (i == 2) {
                ivDice3.startAnimation(anim);
            }
            if (i == 3) {
                ivDice4.startAnimation(anim);
            }
            if (i == 4) {
                ivDice5.startAnimation(anim);
            }
        }

        // Mostra nelle ImageView l'attuale valore effettivo dei dadi
        private void showDice(int i) {
            if (i == 0) {
                ivDice1.setImageResource(dices.get(0).getImage());
            }
            if (i == 1) {
                ivDice2.setImageResource(dices.get(1).getImage());
            }
            if (i == 2) {
                ivDice3.setImageResource(dices.get(2).getImage());
            }
            if (i == 3) {
                ivDice4.setImageResource(dices.get(3).getImage());
            }
            if (i == 4) {
                ivDice5.setImageResource(dices.get(4).getImage());
            }
        }


        // Gestisce l'animazione dei dadi
        private void setUpAnimation() {
            final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    for (int i = 0; i < 5; i++) {
                        if (dices.get(i).getIsLocked() == false) {
                            showDice(i);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            anim.setAnimationListener(animationListener);
        }
    }

    // In caso di pausa, blocca la musica di sottofondo, e la fa ripartire quando l'applicazione riprende

    @Override
    public void onPause() {
        super.onPause();
        holder.theEntertainer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        holder.theEntertainer.start();
    }

    // Tentando di tornare indietro prima della fine della partita, si apre un dialog di conferma
    @Override
    public void onBackPressed() {
        openDialog();
    }

    public void openDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getString(R.string.leave));
        builder.setMessage(getString(R.string.wanna_leave) + "\n" + getString(R.string.result_not_saved));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                holder.theEntertainer.stop();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.create().dismiss();
            }
        });
        builder.show();
    }

    public void openDialogEndSingle() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getString(R.string.the_end));
        builder.setMessage(getString(R.string.end_message) + "\n" + getString(R.string.result_saved));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                holder.theEntertainer.stop();
                saveResult();
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }

    public void openDialogEndMulti(int i) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setTitle(getString(R.string.the_end));
        if (i == 0) {
            builder.setMessage(getString(R.string.you_win) + "\n" + getString(R.string.result_saved));
        } else if (i == 1) {
            builder.setMessage(getString(R.string.you_lose) + "\n" + getString(R.string.result_saved));
        } else {
            builder.setMessage(getString(R.string.its_a_tie) + "\n" + getString(R.string.result_saved));
        }
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                holder.theEntertainer.stop();
                saveResult();
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.show();
    }

    // Funzione richiamata alla fine di ogni partita, permette di salvare il punteggio;
    // crea un'entità Result che verrà poi salvata nel database dei risultati
    public void saveResult() {
        final Result result = new Result();
        result.setPlayerName(getPlayerName());
        result.setGameType(getGameType());
        result.setDate(result.formatDate(new Date()));
        if (getGameType() == 1) {
            result.setOpponentName(null);
        } else {
            result.setOpponentName(getOpponentName());
        }
        result.setTotal(holder.scores.getTotal());
        if (getGameType() == 1) {
            result.setTotalOpponent(0);
        } else {
            result.setTotalOpponent(holder.oppScores.getTotal());
        }
        result.setPoints(result.formatPoints(holder.scores.getOnes(), holder.scores.getTwos(), holder.scores.getThrees(),
                holder.scores.getFours(), holder.scores.getFives(), holder.scores.getSixs(), holder.scores.getTris(),
                holder.scores.getPoker(), holder.scores.getFull(), holder.scores.getSmallStraight(), holder.scores.getLargeStraight(),
                holder.scores.getYahtzee(), holder.scores.getChance()));
        if (getGameType() == 1) {
            result.setPointsOpponent(null);
        } else {
            result.setPointsOpponent(result.formatPoints(holder.oppScores.getOnes(), holder.oppScores.getTwos(), holder.oppScores.getThrees(),
                    holder.oppScores.getFours(), holder.oppScores.getFives(), holder.oppScores.getSixs(), holder.oppScores.getTris(),
                    holder.oppScores.getPoker(), holder.oppScores.getFull(), holder.oppScores.getSmallStraight(), holder.oppScores.getLargeStraight(),
                    holder.oppScores.getYahtzee(), holder.oppScores.getChance()));
        }
        // L'interazione con il database avviene su un altro thread
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDB.resultDAO().insertResult(result);
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                // Verifico in che caso ci si trova per eseguire le istruzioni adeguate
                case STATE_CONNECTION_FAILED:

                    Toast.makeText(GameActivity.this, R.string.conn_failed, Toast.LENGTH_LONG).show();
                    break;
                case STATE_CONNECTED:

                    Toast.makeText(GameActivity.this, R.string.connected, Toast.LENGTH_LONG).show();
                    break;

                case STATE_MESSAGE_RECEIVED:
                    // IN CASO DI MESSAGGIO RICEVUTO LEGGO IL MESSAGGIO DAL BUFFER
                    // IN CASO DI MESSAGGIO RELATIVO AD UNA GIOCATA, LA IDENTIFICO E SETTO IL LAYOUT IN BASE
                    // ALLA GIOCATA RICEVUTA

                    byte[] readBuff= (byte[]) msg.obj;
                    String point= new String(readBuff);

                    // IDENTIFICO IL TIPO DI GIOCATA PRENDENDO IL PRIMO CARATTERA DELLA STRINGA
                    Character c= point.toCharArray()[0];

                    // PRENDO IL PUNTEGGIO RELATIVO ALLA GIOCATA, LEGGENDO DAL BUFFER CON OFFSET DI 1
                    String spoint = point.substring(1);

                    if (c == 'a'){
                        // CASO DADI == 1
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putOnes(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvOne2.setText(String.valueOf(holder.oppScores.getOnes()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'b'){
                        // CASO DADI == 2
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putTwos(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvTwo2.setText(String.valueOf(holder.oppScores.getTwos()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'c'){
                        // CASO DADI == 3
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putThrees(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvThree2.setText(String.valueOf(holder.oppScores.getThrees()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'd'){
                        // CASO DADI == 4
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putFours(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvFour2.setText(String.valueOf(holder.oppScores.getFours()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'e'){
                        // CASO DADI == 5
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putFives(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvFive2.setText(String.valueOf(holder.oppScores.getFives()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'f'){
                        // CASO DADI == 6
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.putSixs(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvSix2.setText(String.valueOf(holder.oppScores.getSixs()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'g'){
                        // CASO TRIS
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setTris(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvTris2.setText(String.valueOf(holder.oppScores.getTris()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'h'){
                        // CASO POKER
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setPoker(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvPoker2.setText(String.valueOf(holder.oppScores.getPoker()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'i'){
                        // CASO FULL
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setFull(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvFull2.setText(String.valueOf(holder.oppScores.getFull()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'l'){
                        // CASO PICCOLA SCALA
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setSmallStraight(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvSmall2.setText(String.valueOf(holder.oppScores.getSmallStraight()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'm'){
                        // CASO GRANDE SCALA
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setLargeStraight(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvLarge2.setText(String.valueOf(holder.oppScores.getLargeStraight()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'n'){
                        // CASO YAHTZEE
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setYahtzee(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvYahtzee2.setText(String.valueOf(holder.oppScores.getYahtzee()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'o'){
                        // CASO CHANCHE
                        if (spoint.charAt(0) == 'x') {
                            spoint = spoint.substring(1, 3);
                        } else {
                            spoint = spoint.substring(0, 1);
                        }
                        holder.oppScores.setChance(Integer.parseInt(spoint));
                        holder.oppScores.setTotal(holder.oppScores.calcTotal());
                        holder.tvBonus2.setText(holder.oppScores.getBonus() + getString(R.string.bonus_max));
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + holder.oppScores.getTotal() + ")");
                        holder.oppTurns++;
                        holder.tvChance2.setText(String.valueOf(holder.oppScores.getChance()));
                        holder.btnRoll.setText(getString(R.string.roll) + "\n(" + getString(R.string.throws_left) + ": " + String.valueOf(3-holder.rolls) + ")");
                        holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorPrimaryDark));
                        holder.btnRoll.setTextColor(getColor(R.color.colorConfirmText));
                        if (holder.confirmSound.isPlaying()) {
                            holder.confirmSound.pause();
                            holder.confirmSound.seekTo(0);
                            holder.confirmSound.start();
                        } else {
                            holder.confirmSound.start();
                        }
                        setMyTurn(true);
                        if (holder.turns == 13) {
                            holder.btnRoll.setText(getString(R.string.end));
                            holder.btnRoll.setBackgroundTintList(ContextCompat.getColorStateList(GameActivity.this, R.color.colorEnd));
                            holder.endGame = true;
                        }
                    }
                    if (c == 'z'){
                        // RICEVO NOME SFIDANTE DA METTE IN TEXTVIEW
                        setOpponentName(spoint);
                        holder.tvOpponent.setText(getOpponentName() + " (Tot.: " + 0 + ")");
                    }
                    Log.i("GAME", holder.oppScores.getOnes() + " " + holder.oppScores.getTwos() + " " + holder.oppScores.getThrees() +
                            " " + holder.oppScores.getFours() + " " + holder.oppScores.getFives() + " " + holder.oppScores.getSixs() + "\n" + holder.oppScores.getBonus() +
                            "\n" + holder.oppScores.getTris() + " " + holder.oppScores.getPoker() + " " + holder.oppScores.getFull() + " " + holder.oppScores.getSmallStraight() +
                            " " + holder.oppScores.getLargeStraight() + " " + holder.oppScores.getYahtzee() + " " + holder.oppScores.getChance() + "\n" + holder.oppScores.getTotal());

                    break;
                case STATE_CONNECTING:
                    // tvMsg.setText("connecting...");
                    break;

            }
            return true;
        }
    });


    // THREAD RICHIAMATO DAL DISPOSITIVO CHE FA DA CLIENT PER FAR PARTIRE LA CONNESSIONE CON IL LATO SERVER
    private class ClientClass extends Thread {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1) {
            device= device1;
            BluetoothSocket bluetoothSocket = null;
            try {
                bluetoothSocket= device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket=bluetoothSocket;
        }

        public void run(){
            myBTA.cancelDiscovery();

            try {
                //IN CASO DI CONNESSIONE AVVENUTA INVIO SUBITO ALL'ALTRO DEVICE IL NOME SCELTO PER IL GIOCO
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive=new SendReceive(socket);
                sendReceive.start();
                String name = "z"+playerName;
                sendReceive.write(name.getBytes());

            } catch (IOException e) {
                try {
                    // RITENTO UNA CONNESSIONE
                    socket.connect();
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();


                } catch (IOException ex) {
                    try {
                        // CONNESSIONE FALLITA QUINDI CHIUDO LA SOCKET
                        Message message = Message.obtain();
                        message.what=STATE_CONNECTION_FAILED;
                        handler.sendMessage(message);
                        socket.close();
                    } catch (IOException exd) {
                        ex.printStackTrace();
                    }
                }
            }
        }


    }
    // THREAD RICHIAMATO DAL DISPOSITIVO CHE FA DA SERVER PER FAR PARTIRE LA CONNESSIONE CON IL LATO CLIENT
    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket=myBTA.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            myBTA.cancelDiscovery();
            BluetoothSocket socket= null;

            while(socket==null){

                // TENTO LA CONNESSIONE
                try {

                    Message message = Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();

                } catch (IOException e) {

                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null){

                    //IN CASO DI CONNESSIONE AVVENUTA INVIO SUBITO ALL'ALTRO DEVICE IL NOME SCELTO PER IL GIOCO
                    Message message = Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive=new SendReceive(socket);
                    sendReceive.start();
                    String name = "z"+playerName;
                    sendReceive.write(name.getBytes());
                    break;
                }
            }
        }
    }
    // THREAD CHE SI OCCUPA DI INVIO E RICEZIONE DEI MESSAGGI TRA SERVER E CLIENT
    private class SendReceive extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket=socket;
            InputStream tempIn= null;
            OutputStream tempOut= null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run() {

            int bytes;

            while(true){

                try {
                    // SE CI SONO NUOVI MESSAGGI RICHIAMO L'HANDLER CHE SI OCCUPA DELLA GESTIONE DI QUESTO
                    byte[] buffer=new byte[1024];
                    bytes= inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1,buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // FUNZIONE CHE SI OCCUPA DELL'INVIO DI MESSAGGI
        public void write(byte[] bytes) {
            try {

                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
