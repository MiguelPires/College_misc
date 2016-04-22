package pt.ulisboa.tecnico.grupo11.ubibike;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class Home extends AppCompatActivity/* implements LocationListener */{

    TextView statusTxt;
    private final int ACCEPTED = 1;
    private final int KEY_SIZE = 2048;
    public static final int SIGNATURE_SIZE = 256;

    static PublicKey publicKey;
    static PrivateKey privateKey;
    static Signature signAlgorithm;

    // wifi direct connection data
    private WifiDirectReceiver mReceiver;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;

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

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);
    }

    public class CircleView extends View {
        public CircleView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLUE);
            paint.setAlpha(80);
            //canvas.drawPaint(paint);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 250, paint);
        }
    }
}
