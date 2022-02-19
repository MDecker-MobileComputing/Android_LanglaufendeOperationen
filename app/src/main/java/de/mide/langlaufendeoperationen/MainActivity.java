package de.mide.langlaufendeoperationen;

import static android.app.Activity.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * App zur Demonstration was passiert, wenn ein Event-Handler zu lange
 * zur Abarbeitung braucht ... und was man dagegen tun kann, nämlich
 * das Auslagern von langlaufenden Operation in einen Hintergrund-Thread.
 * <br><br>
 *
 * Mit der App <i>Fortschrittsanzeige</i> gibt es eine Variante dieser
 * App, die die Berechnung mit einer Fortschrittsanzeige durchführt.
 * <br><br>
 *
 * This project is licensed under the terms of the BSD 3-Clause License.
 */
public class MainActivity extends Activity implements OnClickListener {

    /** Tag für Log-Messages der ganzen App. */
    protected static final String TAG4LOGGING = "LangeBerechnung";

    /** UI-Element für Eingabe der Zahl (Input-Parameter). */
    protected EditText _editTextInputParameter = null;

    /** Textview unten auf Activity zur Anzeige Ergebnis oder sonstige Dinge. */
    protected TextView _textViewAnzeige = null;

    /** Referenz auf Button für Berechnung in AsyncTask. */
    protected Button _button = null;

    /** Element für prozentuale Fortschrittsanzeige (von 0% bis 100%). */
    protected ProgressBar _progressBar = null;


    /**
     * Lifecycle-Method für Setup der UI:
     * Lädt Layout-Datei und füllt die Referenzen auf UI-Elemente in die
     * zugehörigen Member-Variablen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        setTitle( "Lange Berechnung" );


        // *** Referenzen auf einige Widgets abgreifen ***
        _editTextInputParameter = findViewById( R.id.textEditFuerInputParameter  );
        _textViewAnzeige        = findViewById( R.id.textViewZumAnzeigen         );
        _button                 = findViewById( R.id.buttonBerechnungInAsyncTask );
        _progressBar            = findViewById( R.id.fortschrittsanzeige         );

        // *** Event-Handler für Button definieren ***
        _button.setOnClickListener( this );
    }


    /**
     * Einzige Methode aus Interface OnClickListener; Event-Handler-Methode für alle drei Buttons!
     *
     * @param view  Button, der das Event ausgelöst hat.
     */
    @Override
    public void onClick(View view) {

        /* *** Zuerst wird überprüft, ob zulässige Zahl eingegeben *** */
        String inputString = _editTextInputParameter.getText().toString();
        if (inputString == null || inputString.trim().length() == 0) {

            showToast("Bitte Zahl in das Textfeld eingeben!");
            return;
        }

        int inputZahl = Integer.parseInt(inputString);
	    
	    long erwartetesErgebnis = (long) Math.pow(inputZahl, 3); // "inputZahl hoch 3"
        Log.d(TAG4LOGGING, "Erwartetes Ergebnis: " + erwartetesErgebnis);


        /* *** Eigentliche Berechnung durchführen *** */
        _button.setEnabled(false);
        _textViewAnzeige.setText("Berechnung für " + inputZahl + " gestartet ...");

        keyboardEinklappen(view);

        MeinAsyncTask mat = new MeinAsyncTask();
        mat.execute(inputZahl);
    }


    /**
     * Convenience-Methode: Zeigt <i>nachricht</i> mit einem langem Toast an.
     *
     * @param nachricht  Text, der mit Toast-Objekt dargestellt werden soll.
     */
    protected void showToast(String nachricht) {
	    
        Toast.makeText(this, nachricht, Toast.LENGTH_LONG).show();
    }


    /**
     * Virtuelles Keyboard wieder "einklappen";
     * Lösung nach
     * <a href="https://stackoverflow.com/a/17789187/1364368">https://stackoverflow.com/a/17789187/1364368</a>.
     *
     * @param view  UI-Element, von dem Keyboard eingeblendet wurde.
     */
    public void keyboardEinklappen(View view) {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /* **************************** */
    /* *** Start innere Klasse  *** */
    /* **************************** */
	
    /**
     * Eigene Unterklasse von {@link AsyncTask}; diese Klasse steht –- im Gegensatz
     * zu {@link java.lang.Thread} -- nur unter Android zur Verfügung und nicht
     * in "normalem" Java.
     */
    public class MeinAsyncTask extends AsyncTask<Integer, String, Long> {

        /**
         * Diese Methode wird NICHT im Main-Thread ausgeführt, sondern in einem
    	 * Hintergrund-/Worker-Thread.
         * Die Argumente werden beim Aufruf der Methode <code>execute()</code>
	     * übergeben.
         *
         * @param params  Muss genau ein Argument enthalten, nämlich die Zahl, von
         *                der die dritte Potenz zu berechnen ist; wird beim Aufruf
    	 *                der Methode <code>execute</code> übergeben.
         */
        @Override
        protected Long doInBackground(Integer... params) {

            int inputParameter = params[0];

            long ergebnis = 0;

            for (int i = 0; i < inputParameter; i++){

                for (int j = 0; j < inputParameter; j++) {

                    for (int k = 0; k < inputParameter; k++) {

                        ergebnis += 1;
                    }
                }
            }

            return ergebnis;
        }


        /**
         * Wird nach Abarbeitung von doInBackground() aufgerufen, wird im
         * Main-Thread ausgeführt, kann also auf UI-Elemente zugreifen.
         *
         * @param ergebnis  Ergebnis der Berechnung, das auf der UI dargestellt werden soll.
         */
        @Override
        protected void onPostExecute(Long ergebnis) {

            _textViewAnzeige.setText(
                    "Ergebnis in AsyncTask berechnet: " + ergebnis );

            _button.setEnabled(true);
        }

    };

    /* *************************** */
    /* *** Ende innere Klasse  *** */
    /* *************************** */
};
