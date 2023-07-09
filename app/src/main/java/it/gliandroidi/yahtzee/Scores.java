package it.gliandroidi.yahtzee;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

// Codifica l'entità Scores (Punteggi); all'inizio di una partita, ne viene inizializzata una per ogni giocatore.
// E' utile per gestire tail punteggi, e soprattutto contiene la codifica del calcolo degli stessi.
// Contiene anche un valore booleano che indica se il Bonus dei 63 punti è stato raggiunto o meno.
public class Scores implements Parcelable {
    private int ones = 0;
    private int twos = 0;
    private int threes = 0;
    private int fours = 0;
    private int fives = 0;
    private int sixs = 0;
    private int bonus = 0;
    private boolean bonusReached = false;
    private int tris = 0;
    private int poker = 0;
    private int smallStraight = 0;
    private int largeStraight = 0;
    private int full = 0;
    private int yahtzee = 0;
    private int chance = 0;
    private int total = 0;

    public int getOnes() {
        return this.ones;
    }

    public void setOnes(int num) {
        this.ones = num;
    }

    public int getTwos() {
        return this.twos;
    }

    public void setTwos(int num) {
        this.twos = num;
    }

    public int getThrees() {
        return this.threes;
    }

    public void setThrees(int num) {
        this.threes = num;
    }

    public int getFours() {
        return this.fours;
    }

    public void setFours(int num) {
        this.fours = num;
    }

    public int getFives() {
        return this.fives;
    }

    public void setFives(int num) {
        this.fives = num;
    }

    public int getSixs() {
        return this.sixs;
    }

    public void setSixs(int num) {
        this.sixs = num;
    }

    public int getBonus() {
        return this.bonus;
    }

    public void setBonus(int num) {
        this.bonus = num;
    }

    public boolean getBonusReached() {
        return this.bonusReached;
    }

    public void setBonusReached(boolean bool) {
        this.bonusReached = bool;
    }

    public int getTris() {
        return this.tris;
    }

    public void setTris(int num) {
        this.tris = num;
    }

    public int getPoker() {
        return this.poker;
    }

    public void setPoker(int num) {
        this.poker = num;
    }

    public int getSmallStraight() {
        return this.smallStraight;
    }

    public void setSmallStraight(int num) {
        this.smallStraight = num;
    }

    public int getLargeStraight() {
        return this.largeStraight;
    }

    public void setLargeStraight(int num) {
        this.largeStraight = num;
    }

    public int getFull() {
        return this.full;
    }

    public void setFull(int num) {
        this.full = num;
    }

    public int getYahtzee() {
        return this.yahtzee;
    }

    public void setYahtzee(int num) {
        this.yahtzee = num;
    }

    public int getChance() {
        return this.chance;
    }

    public void setChance(int num) {
        this.chance = num;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int num) {
        this.total = num;
    }

    // Mentre per i punteggi della seconda colonna bastano i setter per poter essere inseriti nello Score del giocatore,
    // è conveniente inserire quelli della prima colonna attraverso le funzioni put* , che comprendono anche il calcolo
    // del bonus.
    public void putOnes(int num) {
        setOnes(num);
        setBonus(getBonus() + getOnes());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    public void putTwos(int num) {
        setTwos(num);
        setBonus(getBonus() + getTwos());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    public void putThrees(int num) {
        setThrees(num);
        setBonus(getBonus() + getThrees());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    public void putFours(int num) {
        setFours(num);
        setBonus(getBonus() + getFours());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    public void putFives(int num) {
        setFives(num);
        setBonus(getBonus() + getFives());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    public void putSixs(int num) {
        setSixs(num);
        setBonus(getBonus() + getSixs());
        if (getBonus() >= 63 && getBonusReached() == false) {
            setBonusReached(true);
        }
    }

    // Le funzioni per il calcolo dei punteggi prendono come input un array di 5 numeri corrispondenti
    // ai numeri presenti sui 5 dadi di gioco al momento del calcolo

    // Somma tutti i dadi uguali a 1
    public int calcOnes(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 1) {
                    sum = sum + 1;
                }
            }
            return sum;
        }
    }

    // Somma tutti i dadi uguali a 2
    public int calcTwos(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 2) {
                    sum = sum + 2;
                }
            }
            return sum;
        }
    }

    // Somma tutti i dadi uguali a 3
    public int calcThrees(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 3) {
                    sum = sum + 3;
                }
            }
            return sum;
        }
    }

    // Somma tutti i dadi uguali a 4
    public int calcFours(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 4) {
                    sum = sum + 4;
                }
            }
            return sum;
        }
    }

    // Somma tutti i dadi uguali a 5
    public int calcFives(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 5) {
                    sum = sum + 5;
                }
            }
            return sum;
        }
    }

    // Somma tutti i dadi uguali a 6
    public int calcSixs(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 6) {
                    sum = sum + 6;
                }
            }
            return sum;
        }
    }

    // Verifica (usando la funzione checkDups) se ci sono almeno 3 dadi uguali; in caso affermativo,
    // somma tutti i dadi
    public int calcTris(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            if (checkDups(dices, 3) == false) {
                return 0;
            }
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                sum = sum + dices[i];
            }
            return sum;
        }
    }

    // Verifica (usando la funzione checkDups) se ci sono almeno 4 dadi uguali; in caso affermativo,
    // somma tutti i dadi
    public int calcPoker(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            if (checkDups(dices, 4) == false) {
                return 0;
            }
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                sum = sum + dices[i];
            }
            return sum;
        }
    }

    // Verifica se sono presenti esattamente tre dadi di uno stesso tipo; in caso affermativo, procede
    // a controllare se sono presenti esattamente due dadi di uno stesso tipo (diverso dal tipo precedente)
    public int calcFull(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int one = 0, two = 0, three = 0, four = 0, five = 0, six = 0;
            for (int i = 0; i < 5; i++) {
                if (dices[i] == 1) {
                    one = one + 1;
                }
                if (dices[i] == 2) {
                    two = two + 1;
                }
                if (dices[i] == 3) {
                    three = three + 1;
                }
                if (dices[i] == 4) {
                    four = four + 1;
                }
                if (dices[i] == 5) {
                    five = five + 1;
                }
                if (dices[i] == 6) {
                    six = six + 1;
                }
                if (dices[i] <= 0 || dices[i] > 6) {
                    Log.e("SCORES", "Invalid dices!");
                    return 0;
                }
            }
            if (one == 3) {
                if (two == 2 || three == 2 || four == 2 || five == 2 || six == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else if (two == 3) {
                if (one == 2 || three == 2 || four == 2 || five == 2 || six == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else if (three == 3) {
                if (one == 2 || two == 2 || four == 2 || five == 2 || six == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else if (four == 3) {
                if (one == 2 || two == 2 || three == 2 || five == 2 || six == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else if (five == 3) {
                if (one == 2 || two == 2 || three == 2 || four == 2 || six == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else if (six == 3) {
                if (one == 2 || two == 2 || three == 2 || four == 2 || five == 2) {
                    return 25;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

    // Verifica che tra i numeri dei dadi vi siano almeno 4 numeri consecutivi (usando la funzione checkSequence)
    public int calcSmallStraight(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            for (int i = 1; i < 4; i++) {
                if (checkSequence(dices, i, 4) == true) {
                    return 30;
                }
            }
            return 0;
        }
    }

    // Verifica che i numeri dei 5 dadi siano consecutivi (usando la funzione checkSequence)
    public int calcLargeStraight(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            for (int i = 1; i < 3; i++) {
                if (checkSequence(dices, i, 5) == true) {
                    return 40;
                }
            }
            return 0;
        }
    }

    // Verifica che tutti i dadi siano uguali
    public int calcYahtzee(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            if (dices[0] == dices[1] && dices[0] == dices[2] && dices[0] == dices[3] && dices[0] == dices[4] && dices[0] != 0) {
                return 50;
            } else {
                return 0;
            }
        }
    }

    // Somma tutti i dadi
    public int calcChance(int[] dices) {
        if (dices.length != 5) {
            Log.e("SCORES", "Can only calculate with 5 dices!");
            return 0;
        } else {
            int sum = 0;
            for (int i = 0; i < 5; i++) {
                sum = sum + dices[i];
            }
            return sum;
        }
    }

    // Somma tutti i punteggi delle categorie precedenti, ed aggiunge 35 punti se il bonus è stato raggiunto
    public int calcTotal() {
        int sum = 0;
        if (getBonusReached() == true) {
            sum = sum + 35;
        }
        sum = sum + getOnes() + getTwos() + getThrees() + getFours() + getFives() + getSixs() + getTris() + getPoker() + getSmallStraight() + getLargeStraight() + getFull() + getYahtzee() + getChance();
        return sum;
    }

    // Verifica la presenza di un dato numero tra i valori dei dadi
    public boolean checkPresence(int[] dices, int num) {
        for (int i = 0; i < 5; i++) {
            if (dices[i] == num) {
                return true;
            }
        }
        return false;
    }

    // Usando checkPresence, verifica che ci siano sequenze di numeri consecutivi tra i valori dei dadi,
    // specificando un valore di inizio della sequenza e la lunghezza della stessa
    public boolean checkSequence(int[] dices, int start, int length) {
        for (int i = 0; i < length; i++) {
            if (checkPresence(dices, start+i) == false) {
                return false;
            }
        }
        return true;
    }

    // Verifica se sono presenti copie (in numero uguale a quello passato come parametro) di un numero
    // nei valori dei dadi
    public boolean checkDups(int[] dices, int num) {
        int one = 0, two = 0, three = 0, four = 0, five = 0, six = 0;
        for (int i = 0; i < 5; i++) {
            if (dices[i] == 1) {
                one = one + 1;
            }
            if (dices[i] == 2) {
                two = two + 1;
            }
            if (dices[i] == 3) {
                three = three + 1;
            }
            if (dices[i] == 4) {
                four = four + 1;
            }
            if (dices[i] == 5) {
                five = five + 1;
            }
            if (dices[i] == 6) {
                six = six + 1;
            }
            if (dices[i] <= 0 || dices[i] > 6) {
                Log.e("SCORES", "Invalid dices!");
                return false;
            }
        }
        if (one >= num || two >= num || three >= num || four >= num || five >= num || six >= num) {
            return true;
        } else {
            return false;
        }
    }

    // Scores può essere reso come Parcelable per poter essere passato da un'activity ad un'altra (opzione non utilizzata)

    public static final Creator<Scores> CREATOR = new Creator<Scores>() {
        @Override
        public Scores createFromParcel(Parcel in) {
            return new Scores(in);
        }

        @Override
        public Scores[] newArray(int size) {
            return new Scores[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ones);
        dest.writeInt(twos);
        dest.writeInt(threes);
        dest.writeInt(fours);
        dest.writeInt(fives);
        dest.writeInt(sixs);
        dest.writeInt(bonus);
        if (getBonusReached() == true) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(tris);
        dest.writeInt(poker);
        dest.writeInt(smallStraight);
        dest.writeInt(largeStraight);
        dest.writeInt(full);
        dest.writeInt(yahtzee);
        dest.writeInt(chance);
    }

    public Scores() {

    }

    private Scores(Parcel in) {
        ones = in.readInt();
        twos = in.readInt();
        threes = in.readInt();
        fours = in.readInt();
        fives = in.readInt();
        sixs = in.readInt();
        bonus = in.readInt();
        if (in.readInt() == 1) {
            bonusReached = true;
        } else {
            bonusReached = false;
        }
        tris = in.readInt();
        poker = in.readInt();
        smallStraight = in.readInt();
        largeStraight = in.readInt();
        full = in.readInt();
        yahtzee = in.readInt();
        chance = in.readInt();
    }
}
