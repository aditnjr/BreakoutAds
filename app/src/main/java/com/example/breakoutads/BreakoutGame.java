package com.example.breakoutads;

import android.app.Activity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.InterstitialAd;


public class BreakoutGame extends Activity
{

    BreakoutView breakoutView;

    //Declare banner and Interstitial ads
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    boolean gameOver = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        breakoutView = new BreakoutView(this);

        //Sample AdMob ID: ca-app-pub-7764262309445081~3442433675
        MobileAds.initialize(this, new OnInitializationCompleteListener()
        {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        //Create Interstitial ads object
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7764262309445081/6032504195");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        //Interstitial ads events
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Toast.makeText(BreakoutGame.this, "Interstitial ads onAdLoaded()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(BreakoutGame.this,
                        "Interstitial ads onAdFailedToLoad() with error code: " + errorCode,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());


            }
        });

        //Create banner ads object
        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId("ca-app-pub-7764262309445081/8399631320");

        //Banner ads events
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Toast.makeText(BreakoutGame.this, "Banner ads onAdLoaded()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(BreakoutGame.this,
                        "Banner ads onAdFailedToLoad() with error code: " + errorCode,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

        });

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout game = new FrameLayout(this);
        LinearLayout gameWidgets = new LinearLayout(this);

        RelativeLayout relativeLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams(
                AdView.LayoutParams.WRAP_CONTENT,
                AdView.LayoutParams.WRAP_CONTENT);

        adViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        relativeLayout.addView(mAdView, adViewParams);

        //Load banner ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        game.addView(breakoutView);
        game.addView(relativeLayout);

        game.addView(gameWidgets);

        setContentView(game);


    }


    //INNER CLASS
    class BreakoutView extends SurfaceView implements Runnable
    {

        Thread gameThread = null;

        SurfaceHolder ourHolder;
        volatile boolean playing;
        boolean paused = true;
        Canvas canvas;
        Paint paint;

        long fps;

        private long timeThisFrame;

        int screenX;
        int screenY;

        Paddle paddle;
        Ball ball;
        Brick[] bricks = new Brick[200];
        int numBricks = 0;
        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -2;
        int beep3ID = -3;
        int loseLifeID = -1;
        int explodeID = -1;

        int score =0;

        int lives =3;


        //CONSTRUCTOR
        public BreakoutView(Context context)
        {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle (screenX, screenY);
            ball = new Ball(screenX, screenY);
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
            try
            {
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor,0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);
            }
            catch(IOException e)
            {
                Log.e("Error", "Failed to load sound files");
            }
            createBricksAndRestart();
        }

        public void createBricksAndRestart()
        {

            ball.reset(screenX,screenY);
            paddle.reset(screenX, screenY);


            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;

            numBricks = 0;

            for (int column = 0; column < 8; column ++)
            {
                for (int row = 0; row < 3; row ++)
                {
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

            if (lives == 0) {
                score = 0;
                lives = 3;
                gameOver = false;
                playing = true;
            }
        }

        @Override
        public void run()
        {
            while(playing)
            {
                long startFrameTime = System.currentTimeMillis();

                if(!paused)
                {
                    update();
                }

                draw();
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame > 0)
                {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update()
        {
            paddle.update(fps);
            ball.update(fps);

            for(int i = 0; i < numBricks; i++)
            {
                if (bricks[i].getVisibility())
                {
                    if(RectF.intersects(bricks[i].getRect(),ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + 10;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }

            if(RectF.intersects(paddle.getRect(),ball.getRect()))
            {
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().bottom > screenY)
            {
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                lives --;
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

                if(lives == 0)
                {
                    gameOver = true;
                    paused = true;
                    playing = false;
                    draw();
                    try {
                        gameThread.sleep(1000);
                        showInterstitial();
                        createBricksAndRestart();

                    } catch (InterruptedException e) {
                        Log.e("Error:", "joining thread");
                    }

                }

            }

            if(ball.getRect().top < 0)
            {
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().left < 0)
            {
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            if(ball.getRect().right > screenX - 10)
            {
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            //PADDLE NOT OUT OF SCREEN
            if(paddle.getRect().right > screenX || paddle.getRect().left == 0)
            {
                paddle.setMovementState(paddle.STOPPED);
            }

            if(score == numBricks * 10)
            {
                paused = true;
                try {
                    gameThread.sleep(1000);
                    //createBricksAndRestart();
                    showInterstitial();

                    createBricksAndRestart();
                } catch (InterruptedException e) {
                    Log.e("Error:", "joining thread");
                }
                //createBricksAndRestart();
            }

        }

        public void draw(){
            if(ourHolder.getSurface().isValid())
            {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.argb(255,26,128,182));
                paint.setColor(Color.argb(255,255,255,0));

                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawRect(ball.getRect(),paint);
                paint.setColor(Color.argb(255,249,129,0));
                for(int i = 0; i< numBricks; i++)
                {
                    if(bricks[i].getVisibility())
                    {
                        canvas.drawRect(bricks[i].getRect(),paint);
                    }
                }

                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives : " + lives,10,50,paint);

                if(score == numBricks * 10)
                {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE WON!", 10, screenY /2 , paint);
                }

                if(gameOver == true)
                {
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST!", 10, screenY/2,paint);
                    paused = true;
                    playing = false;

                    //showInterstitial();

                    //createBricksAndRestart();
                }


                /*
                paint.setTextSize(60);
                canvas.drawText("FPS:" + fps,10,50,paint);
                */
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        // SHOW UP AND LOAD THE INTERSTITIAL AD
        public void showInterstitial() {
            if(Looper.myLooper() != Looper.getMainLooper()) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        doShowInterstitial();
                    }
                });
            } else {
                doShowInterstitial();
            }
        }
        private void doShowInterstitial() {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                //Log.d(TAG, "Interstitial ad is not loaded yet");
            }
        }

        public void pause(){
            playing = false;
            try
            {
                gameThread.join();
            }
            catch(InterruptedException e)
            {
                Log.e("Error: ", "Joining thread");
            }
        }

        public void resume()
        {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent)
        {
            switch(motionEvent.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN:
                    paused = false;

                    if(motionEvent.getX() > screenX / 2)
                    {
                        paddle.setMovementState(paddle.RIGHT);
                    }

                    else
                    {
                        paddle.setMovementState(paddle.LEFT);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    paddle.setMovementState((paddle.STOPPED));
                    break;
            }
            return true;
        }
    }




    @Override
    protected void onResume()
    {
        super.onResume();
        breakoutView.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        breakoutView.pause();
    }

}
