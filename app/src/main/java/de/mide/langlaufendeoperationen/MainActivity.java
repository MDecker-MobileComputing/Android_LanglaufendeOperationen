package de.mide.langlaufendeoperationen;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

    /** Referenz auf Button für Berechnung im UI-Thread. */
    protected Button _button1 = null;

    /** Referenz auf Button für Berechnung in eigenem Thread. */
    protected Button _button2 = null;

    /** Referenz auf Button für Berechnung in AsyncTask. */
    protected Button _button3 = null;

    /**
     * Instanz von Handler-Objekt, die im Main-Thread erzeugt wird, so dass mit der Methode
     * {@link android.os.Handler#post(Runnable)} dieser Instanz ein
     * {@link java.lang.Runnable}-Objekt übergeben werden kann, das im Main-Thread ausgeführt
     * wird (für Manipulationen auf UI aus einem Hintergrund-Thread heraus).
     * Wird für alternative Implementierung der Methode "run()" in Klasse {@link MeinThread}
     * verwendet.
     */
    protected Handler _meinHandler = null;


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
        _editTextInputParameter = findViewById( R.id.textEditFuerInputParameter   );
        _textViewAnzeige        = findViewById( R.id.textViewZumAnzeigen          );
        _button1                = findViewById( R.id.buttonBerechnungImMainThread );
        _button2                = findViewById( R.id.buttonBerechnungInOwnThread  );
        _button3                = findViewById( R.id.buttonBerechnungInAsyncTask  );

        // *** Event-Handler für Buttons definieren ***
        _button1.setOnClickListener( this );
        _button2.setOnClickListener( this );
        _button3.setOnClickListener( this );

        // Handler-Objekt erzeugen, das mit aktuellem Thread (=Main-Thread) verbunden ist;
        // wird in alternativer Implementierung der "run()"-Methode in Klasse MeinThread
        // verwendet.
        _meinHandler = new Handler();
    }


    /**
     * Einzige Methode aus Interface OnClickListener;
     * Event-Handler-Methode für alle drei Buttons!
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
        setzeButtonStatus(false);
        _textViewAnzeige.setText("Berechnung für " + inputZahl + " gestartet ...");

        keyboardEinklappen(view);

        if (view == _button1) {

            long ergebnis = berechnung(inputZahl);
            _textViewAnzeige.setText("Ergebnis berechnet: " + ergebnis);
            setzeButtonStatus(true);

        } else if (view == _button2) {

            MeinThread thread = new MeinThread(inputZahl);
            thread.start();

        } else if (view == _button3) {

            MeinAsyncTask mat = new MeinAsyncTask();
            mat.execute(inputZahl);

        } else {

            String errorMessage = "Interner Fehler: Event-Handler-Methode für unerwartetes View aufgerufen: " + view;
            Log.e(TAG4LOGGING, errorMessage);
            showToast(errorMessage);
        }
    }


    /**
     * Berechnet <i>"inputParameter hoch drei"</i> auf bewusst ineffiziente Weise,
     * nämlich mit einer dreifach gestaffelten Schleife.<br>
     * Je größer der Wert <code>inputParameter</code> ist, desto länger dauert die Berechnung.
     * Der Speicherplatz steigt aber <i>NICHT</i> mit <code>inputParameter</code>.
     * Normalerweise würde man für diese Berechnung die Methode {@link Math#pow(double, double)}
     * verwenden.
     * <br><br>
     *
     * <b>Achtung:</b> Laufzeit wächst kubisch mit Wert von <code>inputParameter</code>!
     *
     * @param inputParameter  Zahl, von der die dritte Potenz berechnet werden soll.
     *
     * @return  Berechnungsergebnis (<code>inputParameter</code> hoch 3),
     *          z.B. "8" für <code>inputParameter=2</code>.
     */
    protected long berechnung(int inputParameter) {

        long result = 0;

        for (int i = 0; i < inputParameter; i++){
            for (int j = 0; j < inputParameter; j++) {
                for (int k = 0; k < inputParameter; k++) {
                    result += 1;
                }
	        }
        }

        return result;
    }


    /**
     * Aktiviert oder deaktiviert alle drei Buttons auf einmal
     * (alle Buttons sollen während der Berechnung deaktiviert werden).
     *
     * @param eingschaltet  <code>true</code> gdw. alle Buttons eingeschaltet werden sollen,
     *                      <code>false</code> wenn alle Buttons ausgeschaltet werden sollen.
     */
    protected void setzeButtonStatus(boolean eingschaltet) {

        _button1.setEnabled( eingschaltet );
        _button2.setEnabled( eingschaltet );
        _button3.setEnabled( eingschaltet );
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

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    /* **************************** */
    /* *** Start innere Klassen *** */
    /* **************************** */

    /**
     * Innere Klasse; enthält alternative Implementierung der run()-Methode (auskommentiert),
     * die ein {@link android.os.Handler}-Objekt verwendet, um ein {@link java.lang.Runnable}
     * mit Code im UI-Thread auszuführen.
     */
    protected class MeinThread extends Thread {

        /** Zahl, von der die dritte Potenz berechnet werden soll. */
        protected int __inputZahl;


        /**
         * Konstruktor; kopiert nur das Argument in eine Member-Variable der inneren Klasse.
         *
         * @param inputZahl Zahl, von der die dritte Potenz berechnet werden soll
         */
        public MeinThread(int inputZahl) {
            __inputZahl = inputZahl;
        }


        /**
         * Methode mit Code, der im Hintergrund/Worker-Thread ausgeführt wird.
         * <br><br>
         *
         * Für Zeitmessung wird {@link System#currentTimeMillis()} statt
         * {@link System#nanoTime()} verwendet, weil diese Methode u.a.
         * nicht durch Systemzeitänderungen (z.B. Synchronisierung der
         * Zeit mit NTP-Server) beeinflusst wird (siehe auch
         * <a href="http://bit.ly/2KfTC3N">http://bit.ly/2KfTC3N</a> ).
         * Eine Sekunde hat 10^9 Nano-Sekunden.
         */
        @Override
        public void run() {

            long zeitpunktStart = System.nanoTime();

            final long ergebnis = berechnung( __inputZahl );

            long zeitpunktEnde  = System.nanoTime();


            final long laufzeitSekunden =
                    (zeitpunktEnde - zeitpunktStart)/ ( 1000 * 1000 * 1000 );

            Runnable meinRunnable = new Runnable() {
                @Override
                public void run() {
                    _textViewAnzeige.setText(
                            "Ergebnis berechnet: " + ergebnis +
                                    "\nLaufzeit: " + laufzeitSekunden + " secs");
                    setzeButtonStatus(true);
                }
            };

            // Runnable-Objekt zur späteren Ausführung an den Main-Thread übergeben
            _textViewAnzeige.post( meinRunnable );
        }


        /**
         * Alternative Variante der run()-Methode.
         * Verwendet Handler-Objekt, um Runnable-Objekt im UI-Thread
         * ausführen zu lassen.
         */
        /*
		@Override
		public void run() {

			long zeitpunktStart = System.nanoTime();
			final long ergebnis = berechnung(__inputZahl);
			long zeitpunktEnde  = System.nanoTime();

			final long laufzeitSekunden =
                          (zeitpunktEnde - zeitpunktStart)/ ( 1000 * 1000 * 1000 );

			Runnable meinRunnable = new Runnable() {
				@Override
				public void run() {
					_textViewAnzeige.setText(
							"Ergebnis berechnet: " + ergebnis +
									"\nLaufzeit: " + laufzeitSekunden + " secs");
					setzeButtonStatus(true);
				}
			};

            boolean erfolg = _meinHandler.post(meinRunnable);
		}
		*/
    };

    /* ***************************** */
    /* *** Start innere Klasse 2 *** */
    /* ***************************** */	
	
    /**
     * Eigene Unterklasse von {@link AsyncTask}; diese Klasse steht –- im Gegensatz
     * zu {@link java.lang.Thread} -- nur Unter Android zur Verfügung und nicht
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

            int input_zahl = params[0];

            final long ergebnis = berechnung( input_zahl );

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

            setzeButtonStatus(true);
        }

    };

    /* **************************** */
    /* *** Ende innere Klassen  *** */
    /* **************************** */
};
