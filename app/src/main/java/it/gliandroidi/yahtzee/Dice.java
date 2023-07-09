package it.gliandroidi.yahtzee;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Random;

// Codifica un'entità Dice (Dado), con attributi quali il numero e le immagini (sia del dado bloccato che libero);
// l'attributo isLocked è utilizzato per gestire la possibilità di bloccare i dadi durante il gioco
public class Dice implements Parcelable {
    int u0 = R.drawable.dice_zero;
    int l0 = R.drawable.dice_zero_lock;
    int u1 = R.drawable.dice_one;
    int l1 = R.drawable.dice_one_lock;
    int u2 = R.drawable.dice_two;
    int l2 = R.drawable.dice_two_lock;
    int u3 = R.drawable.dice_three;
    int l3 = R.drawable.dice_three_lock;
    int u4 = R.drawable.dice_four;
    int l4 = R.drawable.dice_four_lock;
    int u5 = R.drawable.dice_five;
    int l5 = R.drawable.dice_five_lock;
    int u6 = R.drawable.dice_six;
    int l6 = R.drawable.dice_six_lock;
    private int number = 0;
    private boolean isLocked = false;
    private int image = u0;
    private int unlockedImage = u0;
    private int lockedImage = l0;

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int num) {
        if (num < 0 || num > 6) {
            Log.e("DICE", "Invalid number!");
        } else {
            this.number = num;
        }
    }

    public boolean getIsLocked() {
        return this.isLocked;
    }

    public void setIsLocked(boolean lock) {
        this.isLocked = lock;
    }

    public int getImage() {
        return this.image;
    }

    public void setImage(int img) {
        this.image = img;
    }

    public int getUnlockedImage() {
        return this.unlockedImage;
    }

    public void setUnlockedImage(int img) {
        this.unlockedImage = img;
    }

    public int getLockedImage() {
        return this.lockedImage;
    }

    public void setLockedImage(int img) {
        this.lockedImage = img;
    }

    // Blocca il dado, in questo stato, non cambierà valore con il roll
    public void lock() {
        if (this.getIsLocked() == true) {
            Log.e("DICE", "Dice already locked!");
        } else {
            this.setIsLocked(true);
            this.imageSetter(getIsLocked());
        }
    }

    // Sblocca un dado precedentemente bloccato
    public void unlock() {
        if (this.getIsLocked() == false) {
            Log.e("DICE", "Dice already unlocked!");
        } else {
            this.setIsLocked(false);
            this.imageSetter(getIsLocked());
        }
    }

    // Setta l'immagine (effettiva) con una delle due immagini locked e unlocked
    public void imageSetter(boolean lock) {
        if (lock == true) {
            this.setImage(this.getLockedImage());
        } else {
            this.setImage(this.getUnlockedImage());
        }
    }

    // Usata per settare il dado nel caso più semplice, cioè nel costruttore che prende solo un numero come input
    public void setDice(int number) {
        if (number < 0 || number > 6) {
            Log.e("DICE", "Invalid dice number!");
        } else {
            switch(number) {
                case 1:
                    setNumber(1);
                    setIsLocked(false);
                    setUnlockedImage(u1);
                    setLockedImage(l1);
                    imageSetter(getIsLocked());
                    break;
                case 2:
                    setNumber(2);
                    setIsLocked(false);
                    setUnlockedImage(u2);
                    setLockedImage(l2);
                    imageSetter(getIsLocked());
                    break;
                case 3:
                    setNumber(3);
                    setIsLocked(false);
                    setUnlockedImage(u3);
                    setLockedImage(l3);
                    imageSetter(getIsLocked());
                    break;
                case 4:
                    setNumber(4);
                    setIsLocked(false);
                    setUnlockedImage(u4);
                    setLockedImage(l4);
                    imageSetter(getIsLocked());
                    break;
                case 5:
                    setNumber(5);
                    setIsLocked(false);
                    setUnlockedImage(u5);
                    setLockedImage(l5);
                    imageSetter(getIsLocked());
                    break;
                case 6:
                    setNumber(6);
                    setIsLocked(false);
                    setUnlockedImage(u6);
                    setLockedImage(l6);
                    imageSetter(getIsLocked());
                    break;
                default :
                    setNumber(0);
                    setIsLocked(false);
                    setUnlockedImage(u0);
                    setLockedImage(l0);
                    imageSetter(getIsLocked());
            }
            Log.i("DICE", "Image" + getImage());
        }
    }

    // L'azione Roll (Tira) è un'azione propria dell'entità Dice; è usata per lanciare il dado e assegna ad esso
    // un valore randomico compreso tra 1 e 6
    public void roll() {
        int rand = new Random().nextInt(6) + 1;
        if (rand < 1 || rand > 6) {
            Log.e("DICE", "Invalid number!");
        } else {
            this.setDice(rand);
        }
    }

    // Dice può essere reso come Parcelable per poter essere passato da un'activity ad un'altra (opzione non utilizzata)

    public static final Creator<Dice> CREATOR = new Creator<Dice>() {
        @Override
        public Dice createFromParcel(Parcel in) {
            return new Dice(in);
        }

        @Override
        public Dice[] newArray(int size) {
            return new Dice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        if (isLocked == true) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(unlockedImage);
        dest.writeInt(lockedImage);
    }

    public Dice() {

    }

    // Costruttore usato per inizializzare dadi standard (da 1 a 6)
    public Dice(int number) {
        this.setDice(number);
    }

    // Costruttore usato per inizializzare dadi particolari, come quelli raffiguranti lettere
    public Dice(int number, boolean isLocked, int unlockedImage, int lockedImage) {
        this.setNumber(number);
        this.setIsLocked(isLocked);
        this.setUnlockedImage(unlockedImage);
        this.setLockedImage(lockedImage);
        this.imageSetter(this.getIsLocked());
    }

    private Dice(Parcel in) {
        number = in.readInt();
        if (in.readInt() == 1) {
            isLocked = true;
        } else {
            isLocked = false;
        }
        unlockedImage = in.readInt();
        lockedImage = in.readInt();
        if (isLocked == true) {
            image = lockedImage;
        } else {
            image = unlockedImage;
        }
    }
}
