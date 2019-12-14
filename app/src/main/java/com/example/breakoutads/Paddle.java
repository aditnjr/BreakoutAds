package com.example.breakoutads;

import android.graphics.RectF;

public class Paddle
{
    private RectF rect;

    private float length;
    private float height;

    private float x;
    private float y;

    private float m;
    private float n;

    private float paddleSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY)
    {
        length = 130;
        height = 20;

        x = screenX / 2;
        y = screenY -20;

        m = screenX;
        n = screenY;


        rect = new RectF(x,y,x + length, y+height);
        paddleSpeed = 350;
    }

    public RectF getRect()
    {
        return rect;
    }

    public void setMovementState(int state)
    {
        paddleMoving = state;
    }

    public void update(long fps)
    {
        if (x > 0 )
        {
            if (paddleMoving == LEFT)
            {
                x = x - paddleSpeed / fps;
            }
        }

        if (x < m)
        {
            if (paddleMoving == RIGHT)
            {
                x = x + paddleSpeed / fps;
            }
        }
        rect.left = x;
        rect.right = x + length;
    }

    //RESET PADDLE TO POSITION
    public void reset (int p, int q)
    {
        //RESET POSISI X/Y PADDLE
        x = p/2 - length/2;
        y = q - 20;

        //RESET POSISI RECTF
        rect.left = p/2 - length/2 ;
        rect.top = q - 20;
        rect.right = p/2 + length/2 ;
        rect.bottom = p - 20 - height;
    }


}
