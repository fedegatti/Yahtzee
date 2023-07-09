package it.gliandroidi.yahtzee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

// Activity in cui è possibile visualizzare i risultati delle partite portate correttamente a termine,
// sia in singleplayer che in multiplayer
public class HistoryActivity extends AppCompatActivity {

    Holder holder;

    // Occorre interagire con il database dei risultati; con esso, verrà creata una lista da poter inserire
    // in una RecyclerView
    AppResultDatabase appDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        appDB = AppResultDatabase.getInstance(HistoryActivity.this);
        holder = new Holder();
    }

    private class Holder {

        final TextView tvHistorytxt;

        Holder() {

            tvHistorytxt = findViewById(R.id.tvHistorytxt);

            // Viene lanciato un secondo thread per interagire con il database; se tale database è vuoto (lunghezza 0),
            // viene solo visualizzata la stringa 'nessun risultato salvato', altrimenti si procede ad inizializzare
            // la RecyclerView contenente i risultati
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    int size = appDB.resultDAO().size();
                    if (size == 0) {
                        tvHistorytxt.setText(getString(R.string.no_result));
                    } else {
                        tvHistorytxt.setText(getString(R.string.touch_for_details));
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this);
                        RecyclerView rvResults = findViewById(R.id.rvResults);

                        // Con il DAO, si ottengono i risultati sotto forma di lista, che verrà poi invertita, in modo
                        // da far apparire per primi i risultati più recenti
                        RecyclerView.Adapter mAdapter = new MyAdapter(reverseList(appDB.resultDAO().getResultList()));
                        rvResults.setLayoutManager(layoutManager);
                        rvResults.setAdapter(mAdapter);
                    }
                }
            });
        }

        private class MyAdapter extends RecyclerView.Adapter<MyAdapter.Holder2> implements View.OnClickListener {
            List<Result> mDataset;
            MyAdapter(List<Result> myDataset) {
                mDataset = myDataset;
            }
            @NonNull
            @Override
            public Holder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.row_result, parent, false);
                cl.setOnClickListener(this);
                return new Holder2(cl);
            }

            @Override
            public void onBindViewHolder(@NonNull Holder2 holder2, int position) {
                holder2.tvRowResult.setText(formatString(mDataset.get(position)));
                if ((position % 2) == 0) {
                    holder2.clRowResult.setBackgroundColor(getColor(R.color.colorPointsTwo));
                } else {
                    holder2.clRowResult.setBackgroundColor(getColor(R.color.colorPointsThree));
                }
            }

            @Override
            public int getItemCount() {
                return mDataset.size();
            }

            // Cliccando su un elemento della RecyclerView, il risultato (tipo di dato Result) corrispondente
            // sarà passata (come Parcelable) alla activity DetailGameActivity
            @Override
            public void onClick(View v) {
                int position = ((RecyclerView) v.getParent()).getChildAdapterPosition(v);
                Result result = mDataset.get(position);
                Intent intent = new Intent(HistoryActivity.this, DetailGameActivity.class);
                intent.putExtra("result", result);
                HistoryActivity.this.startActivity(intent);
            }

            class Holder2 extends RecyclerView.ViewHolder {
                final ConstraintLayout clRowResult;
                final TextView tvRowResult;

                Holder2(@NonNull View itemView) {
                    super(itemView);
                    clRowResult = itemView.findViewById(R.id.clRowResult);
                    tvRowResult = itemView.findViewById(R.id.tvRowResult);
                }
            }
        }

        // Funzione che prende i dati dall'entità Result e li rilavora in modo da mostrare in un'unica stringa
        // i dati essenziali per tale risultato
        public String formatString(Result result) {
            String player = result.getPlayerName();
            String date = result.getDate();
            String opp;
            String tot;
            if (result.getGameType() == 1) {
                opp = getString(R.string.single_player);
                tot = String.valueOf(result.getTotal());
            } else {
                opp = getString(R.string.vs) + " " + result.getOpponentName();
                String outcome;
                if (result.getTotal() > result.getTotalOpponent()) {
                    outcome = getString(R.string.victory);
                } else if (result.getTotal() < result.getTotalOpponent()) {
                    outcome = getString(R.string.defeat);
                } else {
                    outcome = getString(R.string.tie);
                }
                tot = String.valueOf(result.getTotal()) + "-" + String.valueOf(result.getTotalOpponent()) + " (" + outcome + ")";
            }
            String form = player + " - " + date + " - " + opp + " - " + tot;
            return form;
        }

        // Semplice funzione per invertire l'ordine degli elementi di una lista
        public List<Result> reverseList(List<Result> result) {
            for (int i = 0, j = result.size()-1; i < j; i++) {
                result.add(i, result.remove(j));
            }
            return result;
        }
    }
}