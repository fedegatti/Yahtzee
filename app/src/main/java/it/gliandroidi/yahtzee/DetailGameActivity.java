package it.gliandroidi.yahtzee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

// Semplice activity che mostra i punti del giocatore (o dei giocatori in caso di partita in multiplayer)
// assegnati a ciascuna categoria di punteggio
public class DetailGameActivity extends AppCompatActivity {

    Holder holder;
    Result result;
    String[] points;
    String[] oppPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        result = data.getParcelableExtra("result");

        // Nella classe Result, i punti sono salvati come una stringa di numeri separati da spazi;
        // con questo comando, si crea un'array di stringhe, con una stringa per ogni punteggio
        points = result.getPoints().split(" ");
        if (result.getGameType() == 2) {
            oppPoints = result.getPointsOpponent().split(" ");
        }

        setContentView(R.layout.activity_detail_game);
        holder = new Holder();
    }

    class Holder {

        TextView tvGameTitle, tvPOnes, tvPTwos, tvPThrees, tvPFours, tvPFives, tvPSixs, tvPBonus, tvPTris, tvPPoker, tvPFull, tvPSmall, tvPLarge, tvPYahtzee, tvPChance, tvPTotal;

        Holder() {

            tvGameTitle = findViewById(R.id.tvGameTitle);
            tvPOnes = findViewById(R.id.tvPOnes);
            tvPTwos = findViewById(R.id.tvPTwos);
            tvPThrees = findViewById(R.id.tvPThrees);
            tvPFours = findViewById(R.id.tvPFours);
            tvPFives = findViewById(R.id.tvPFives);
            tvPSixs = findViewById(R.id.tvPSixs);
            tvPBonus = findViewById(R.id.tvPBonus);
            tvPTris = findViewById(R.id.tvPTris);
            tvPPoker = findViewById(R.id.tvPPoker);
            tvPFull = findViewById(R.id.tvPFull);
            tvPSmall = findViewById(R.id.tvPSmall);
            tvPLarge = findViewById(R.id.tvPLarge);
            tvPYahtzee = findViewById(R.id.tvPYahtzee);
            tvPChance = findViewById(R.id.tvPChance);
            tvPTotal = findViewById(R.id.tvPTotal);

            // Le TextView vengono riempite in modo diverso a seconda dei due casi (singleplayer o multiplayer)
            // Caso multiplayer
            if (result.getGameType() == 2) {
                tvGameTitle.setText(result.getPlayerName() + " " + getString(R.string.vs) + " " + result.getOpponentName() + "\n(" + result.getDate() + ")");
                tvPOnes.setText(getString(R.string.p_ones) + "    " + points[0] + "     (" + getString(R.string.opp) + oppPoints[0] + ")");
                tvPTwos.setText(getString(R.string.p_twos) + "    " + points[1] + "     (" + getString(R.string.opp) + oppPoints[1] + ")");
                tvPThrees.setText(getString(R.string.p_threes) + "    " + points[2] + "     (" + getString(R.string.opp) + oppPoints[2] + ")");
                tvPFours.setText(getString(R.string.p_fours) + "    " + points[3] + "     (" + getString(R.string.opp) + oppPoints[3] + ")");
                tvPFives.setText(getString(R.string.p_fives) + "    " + points[4] + "     (" + getString(R.string.opp) + oppPoints[4] + ")");
                tvPSixs.setText(getString(R.string.p_sixs) + "    " + points[5] + "     (" + getString(R.string.opp) + oppPoints[5] + ")");
                String bonus;
                int bonusTotal = Integer.parseInt(points[0]) + Integer.parseInt(points[1]) + Integer.parseInt(points[2]) + Integer.parseInt(points[3])
                        + Integer.parseInt(points[4]) + Integer.parseInt(points[5]);
                int bonusTotalOpp = Integer.parseInt(oppPoints[0]) + Integer.parseInt(oppPoints[1]) + Integer.parseInt(oppPoints[2]) + Integer.parseInt(oppPoints[3])
                        + Integer.parseInt(oppPoints[4]) + Integer.parseInt(oppPoints[5]);
                if (bonusTotal >= 63) {
                    bonus = String.valueOf(bonusTotal) + getString(R.string.p_bonus_obtained) + "  (+35)";
                } else {
                    bonus = String.valueOf(bonusTotal) + getString(R.string.p_bonus_not_obtained);
                }
                if (bonusTotalOpp >= 63) {
                    bonus = bonus + "\n(" + getString(R.string.opp) + String.valueOf(bonusTotalOpp) + getString(R.string.p_bonus_obtained) + "  (+35)" + ")";
                } else {
                    bonus = bonus + "\n(" + getString(R.string.opp) + String.valueOf(bonusTotalOpp) + getString(R.string.p_bonus_not_obtained) + ")";
                }
                tvPBonus.setText(bonus);
                tvPTris.setText(getString(R.string.p_tris) + "    " + points[6] + "     (" + getString(R.string.opp) + oppPoints[6] + ")");
                tvPPoker.setText(getString(R.string.p_poker) + "    " + points[7] + "     (" + getString(R.string.opp) + oppPoints[7] + ")");
                tvPFull.setText(getString(R.string.p_full) + "    " + points[8] + "     (" + getString(R.string.opp) + oppPoints[8] + ")");
                tvPSmall.setText(getString(R.string.p_small) + "    " + points[9] + "     (" + getString(R.string.opp) + oppPoints[9] + ")");
                tvPLarge.setText(getString(R.string.p_large) + "    " + points[10] + "     (" + getString(R.string.opp) + oppPoints[10] + ")");
                tvPYahtzee.setText(getString(R.string.p_yahtzee) + "    " + points[11] + "     (" + getString(R.string.opp) + oppPoints[11] + ")");
                tvPChance.setText(getString(R.string.p_chance) + "    " + points[12] + "     (" + getString(R.string.opp) + oppPoints[12] + ")");
                tvPTotal.setText(getString(R.string.p_total) + "    " + result.getTotal() + "     (" + getString(R.string.opp) + result.getTotalOpponent() + ")");
            } else {

                // Caso singleplayer
                tvGameTitle.setText(result.getPlayerName() + "\n(" + result.getDate() + ")");
                tvPOnes.setText(getString(R.string.p_ones) + "    " + points[0]);
                tvPTwos.setText(getString(R.string.p_twos) + "    " + points[1]);
                tvPThrees.setText(getString(R.string.p_threes) + "    " + points[2]);
                tvPFours.setText(getString(R.string.p_fours) + "    " + points[3]);
                tvPFives.setText(getString(R.string.p_fives) + "    " + points[4]);
                tvPSixs.setText(getString(R.string.p_sixs) + "    " + points[5]);
                String bonus;
                int bonusTotal = Integer.parseInt(points[0]) + Integer.parseInt(points[1]) + Integer.parseInt(points[2]) + Integer.parseInt(points[3])
                        + Integer.parseInt(points[4]) + Integer.parseInt(points[5]);
                if (bonusTotal >= 63) {
                    bonus = String.valueOf(bonusTotal) + getString(R.string.p_bonus_obtained) + "  (+35)";
                } else {
                    bonus = String.valueOf(bonusTotal) + getString(R.string.p_bonus_not_obtained);
                }
                tvPBonus.setText(bonus);
                tvPTris.setText(getString(R.string.p_tris) + "    " + points[6]);
                tvPPoker.setText(getString(R.string.p_poker) + "    " + points[7]);
                tvPFull.setText(getString(R.string.p_full) + "    " + points[8]);
                tvPSmall.setText(getString(R.string.p_small) + "    " + points[9]);
                tvPLarge.setText(getString(R.string.p_large) + "    " + points[10]);
                tvPYahtzee.setText(getString(R.string.p_yahtzee) + "    " + points[11]);
                tvPChance.setText(getString(R.string.p_chance) + "    " + points[12]);
                tvPTotal.setText(getString(R.string.p_total) + "    " + result.getTotal());
            }
        }
    }
}