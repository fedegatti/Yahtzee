package it.gliandroidi.yahtzee;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.util.Date;

// Codifica la classe Result, usata per salvare i risultati di una partita, assieme ai nomi dei giocatori,
// alla modalità (singleplayer o multiplayer), e alla data a cui la partita risale; è codificata come
// un'entità da inserire in un database (il database dei risultati) secondo la sintassi di Room
@Entity
public class Result implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="_id")
    public int idGame;
    @ColumnInfo(name="playerName")
    private String playerName;
    @ColumnInfo(name="gameType")
    private int gameType;
    @ColumnInfo(name="date")
    private String date;
    @ColumnInfo(name="opponentName")
    private String opponentName;
    @ColumnInfo(name="total")
    private int total;
    @ColumnInfo(name="totalOpponent")
    private int totalOpponent;
    @ColumnInfo(name="points")
    private String points;
    @ColumnInfo(name="pointsOpponent")
    private String pointsOpponent;

    public int getIdGame() {
        return this.idGame;
    }

    public void setIdGame(int id) {
        this.idGame = id;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public int getGameType() {
        return this.gameType;
    }

    public void setGameType(int type) {
        this.gameType = type;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String d) {
        this.date = d;
    }

    public String getOpponentName() {
        return this.opponentName;
    }

    public void setOpponentName(String name) {
        this.opponentName = name;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int tot) {
        this.total = tot;
    }

    public int getTotalOpponent() {
        return this.totalOpponent;
    }

    public void setTotalOpponent(int tot) {
        this.totalOpponent = tot;
    }

    public String getPoints() {
        return this.points;
    }

    public void setPoints(String p) {
        this.points = p;
    }

    public String getPointsOpponent() {
        return this.pointsOpponent;
    }

    public void setPointsOpponent(String p) {
        this.pointsOpponent = p;
    }

    // Funzione che rilavora, sotto forma di stringa, la data (completa di orario) della partita
    public String formatDate(Date date) {
        String dateString = date.toString();
        String[] dateSeparator = dateString.split(" ");
        String day = dateSeparator[2];
        String month;
        switch (dateSeparator[1]) {
            case "Jan" :
                month = "01";
                break;
            case "Feb" :
                month = "02";
                break;
            case "Mar" :
                month = "03";
                break;
            case "Apr" :
                month = "04";
                break;
            case "May" :
                month = "05";
                break;
            case "Jun" :
                month = "06";
                break;
            case "Jul" :
                month = "07";
                break;
            case "Aug" :
                month = "08";
                break;
            case "Sep" :
                month = "09";
                break;
            case "Oct" :
                month = "10";
                break;
            case "Nov" :
                month = "11";
                break;
            case "Dec" :
                month = "12";
                break;
            default:
                month = "00";
        }
        String year = dateSeparator[5];
        String time = dateSeparator[3];
        String dateFormatted = day + "/" + month + "/" + year + " (" + time + ")";
        return dateFormatted;
    }

    // Funzione che organizza in un'unica stringa i punteggi di un giocatore
    public String formatPoints(int ones, int twos, int threes, int fours, int fives, int sixs, int tris, int poker, int full, int smallStraight, int largeStraight, int yahtzee, int chance) {
        String formattedPoints;
        formattedPoints = String.valueOf(ones) + " " + String.valueOf(twos) + " " + String.valueOf(threes) + " " + String.valueOf(fours) + " " +
                String.valueOf(fives) + " " + String.valueOf(sixs) + " " + String.valueOf(tris) + " " + String.valueOf(poker) + " " +String.valueOf(full) + " " +
                String.valueOf(smallStraight) + " " + String.valueOf(largeStraight) + " " + String.valueOf(yahtzee) + " " + String.valueOf(chance);
        return formattedPoints;
    }

    // Result può essere reso come Parcelable per poter essere passato da un'activity ad un'altra

    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idGame);
        dest.writeString(playerName);
        dest.writeInt(gameType);
        dest.writeString(date);
        dest.writeString(opponentName);
        dest.writeInt(total);
        dest.writeInt(totalOpponent);
        dest.writeString(points);
        dest.writeString(pointsOpponent);
    }

    public Result() {

    }

    public Result(String player, int gt, String dat, String opp, int tot, int totOpp, String p, String pOpp) {
        setPlayerName(player);
        setGameType(gt);
        setDate(dat);
        setOpponentName(opp);
        setTotal(tot);
        setTotalOpponent(totOpp);
        setPoints(p);
        setPointsOpponent(pOpp);
    }

    private Result(Parcel in) {
        idGame = in.readInt();
        playerName = in.readString();
        gameType = in.readInt();
        date = in.readString();
        opponentName = in.readString();
        total = in.readInt();
        totalOpponent = in.readInt();
        points = in.readString();
        pointsOpponent = in.readString();
    }
}
