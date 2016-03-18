package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Home extends AppCompatActivity {

    TextView statusTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        statusTxt = (TextView)findViewById(R.id.statusTxt);
        CircleView circleView = new CircleView(this);
        //circleView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.homelayout);
        relativeLayout.addView(circleView);
        statusTxt.bringToFront();
    }

    public class CircleView extends View
    {
        public CircleView(Context context)
        {
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
