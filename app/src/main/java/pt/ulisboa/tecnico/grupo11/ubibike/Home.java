package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;


public class Home extends AppCompatActivity {

    public static TextView statusTxt;

    // crypto data
    private final int KEY_SIZE = 2048;

    static PublicKey publicKey;
    static PrivateKey privateKey;
    static Signature signAlgorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        statusTxt = (TextView) findViewById(R.id.statusTxt);
        CircleView circleView = new CircleView(this);
        //circleView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.homelayout);
        relativeLayout.addView(circleView);
        statusTxt.bringToFront();

        new Thread(new Runnable() {
            public void run() {
                try {
                    // instantiate key generator
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                    keyGen.initialize(KEY_SIZE, random);

                    // generate keys
                    KeyPair pair = keyGen.generateKeyPair();
                    privateKey = pair.getPrivate();
                    publicKey = pair.getPublic();

                    // initialize signing algorithm
                    signAlgorithm = Signature.getInstance("SHA512withRSA");

                    final String keyUrl = Login.serverUrl + "/users/" + Tab.username + "/key";
                    URL url = new URL(keyUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(publicKey.getEncoded());
                    wr.close();
                    conn.getInputStream();

                } catch (Exception e) {
                    Log.e("INIT", e.getMessage(), e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Home.this, "Initialization error",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public class CircleView extends View {
        Paint paint;

        public CircleView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLUE);
            paint.setAlpha(80);
            //canvas.drawPaint(paint);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 250, paint);
        }

        public void setCircleColorGreen()
        {
            paint.setColor(Color.GREEN);
        }

        public void setCircleColorBlue()
        {
            paint.setColor(Color.BLUE);
        }

    }
}
