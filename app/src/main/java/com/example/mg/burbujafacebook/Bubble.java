package com.example.mg.burbujafacebook;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

class Bubble implements View.OnTouchListener {
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private boolean isLeft = true;
    private long time_start = 0;
    private boolean isLongclick = false, inBounded = false;
    private int remove_img_width = 0, remove_img_height = 0;
    private Handler handler_longClick;
    private Runnable runnable_longClick;
    private RelativeLayout bubbleView;
    private RelativeLayout messageView;
    private WindowManager windowManager;
    private ImageView imgRemove;
    private RelativeLayout removeView;
    private Context context;
    private  Point sizeWindow = new Point();
    private Class classChat;
    static boolean isActive;
    static Activity activityChat;
    private long timeShowRemoveView;
    private Class classService;
    static String EXTRA_MESSAGE = "extra_message";

    void setBubbleView(RelativeLayout bubbleView) {
        this.bubbleView = bubbleView;
    }

    void setRemoveView(RelativeLayout removeView) {
        this.removeView = removeView;
    }

    void setMessageView(RelativeLayout messageView) {
        this.messageView = messageView;
    }

    void setImgRemove(ImageView imgRemove) {
        this.imgRemove = imgRemove;
    }

    void setSizeWindow(Point sizeWindow) {
        this.sizeWindow = sizeWindow;
    }

    boolean isLeft() {
        return isLeft;
    }

    void setClassChat(Class classChat) {
        this.classChat = classChat;
    }

    public void setClassService(Class classService) {
        this.classService = classService;
    }

    public void setTimeShowRemoveView(long timeShowRemoveView) {
        this.timeShowRemoveView = timeShowRemoveView;
    }

    Bubble(Context context, WindowManager windowManager) {
        this.windowManager = windowManager;
        this.context = context;
        //hilo para detectar que el usuario tardo precionando la burbuja
        handler_longClick = new Handler();
        runnable_longClick = new Runnable() {

            @Override
            public void run() {
                //mostramos la vista para remover la burbuja
                isLongclick = true;
                removeView.setVisibility(View.VISIBLE);
                chatLongclick();
            }
        };
    }
     static int getLayoutParamsX(){
        return 0;
    }
     static int getLayoutParamsY(){
        return 100;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();

        int x_cord = (int) event.getRawX();
        int y_cord = (int) event.getRawY();
        int x_cord_Destination, y_cord_Destination;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                time_start = System.currentTimeMillis();
                if(timeShowRemoveView<=0 ||timeShowRemoveView<=300)
                    timeShowRemoveView=600;

                //Tiempo en el que se va a mostrar la imagen de remove
                handler_longClick.postDelayed(runnable_longClick, timeShowRemoveView);

                remove_img_width = imgRemove.getLayoutParams().width;
                remove_img_height = imgRemove.getLayoutParams().height;

                x_init_cord = x_cord;
                y_init_cord = y_cord;

                x_init_margin = layoutParams.x;
                y_init_margin = layoutParams.y;

                //Si movemos la burbuja desaparecemos el mensaje
                if (messageView != null) {
                    messageView.setVisibility(View.GONE);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int x_diff_move = x_cord - x_init_cord;
                int y_diff_move = y_cord - y_init_cord;

                x_cord_Destination = x_init_margin + x_diff_move;
                y_cord_Destination = y_init_margin + y_diff_move;

                if (isLongclick) {
                    int x_bound_left = sizeWindow.x / 2 - (int) (remove_img_width * 1.5);
                    int x_bound_right = sizeWindow.x / 2 + (int) (remove_img_width * 1.5);
                    int y_bound_top = sizeWindow.y - (int) (remove_img_height * 1.5);

                    if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                        //a inBounded le colocamos true para avisar que la burbuja esta encima de remove
                        inBounded = true;

                        //zoom cuando la brbuja se acerca a remove
                        int x_cord_remove = (int) ((sizeWindow.x - (remove_img_height * 1.2)) / 2);
                        int y_cord_remove = (int) (sizeWindow.y - ((remove_img_width * 1.2) + getStatusBarHeight(context)));

                        if (imgRemove.getLayoutParams().height == remove_img_height) {
                            //zoom cuando la brbuja se acerca a remove
                            imgRemove.getLayoutParams().height = (int) (remove_img_height * 1.2);
                            imgRemove.getLayoutParams().width = (int) (remove_img_width * 1.2);

                            WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                            param_remove.x = x_cord_remove;
                            param_remove.y = y_cord_remove;
                            //Actualizamos vista de remover
                            windowManager.updateViewLayout(removeView, param_remove);
                        }

                        layoutParams.x = x_cord_remove + (Math.abs(removeView.getWidth() - bubbleView.getWidth())) / 2;
                        layoutParams.y = y_cord_remove + (Math.abs(removeView.getHeight() - bubbleView.getHeight())) / 2;
                        //Actualizamos la vista de la burbuja
                        windowManager.updateViewLayout(bubbleView, layoutParams);
                        break;
                    } else {
                        inBounded = false;
                        imgRemove.getLayoutParams().height = remove_img_height;
                        imgRemove.getLayoutParams().width = remove_img_width;

                        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                        int x_cord_remove = (sizeWindow.x - removeView.getWidth()) / 2;
                        int y_cord_remove = sizeWindow.y - (removeView.getHeight() + getStatusBarHeight(context));

                        param_remove.x = x_cord_remove;
                        param_remove.y = y_cord_remove;

                        windowManager.updateViewLayout(removeView, param_remove);
                    }

                }
                layoutParams.x = x_cord_Destination;
                layoutParams.y = y_cord_Destination;

                windowManager.updateViewLayout(bubbleView, layoutParams);
                break;
            case MotionEvent.ACTION_UP:
                isLongclick = false;
                removeView.setVisibility(View.GONE);
                imgRemove.getLayoutParams().height = remove_img_height;
                imgRemove.getLayoutParams().width = remove_img_width;
                handler_longClick.removeCallbacks(runnable_longClick);

                //Si inBounded es true cerrara el chat y se finalizara el servio de la burbuja
                //Esto es porque la burbuja esta encima de remove
                if (inBounded) {
                    closedBubble();
                    break;
                }


                int x_diff = x_cord - x_init_cord;
                int y_diff = y_cord - y_init_cord;

                //Diferenciamos entre el click y el long clic
                if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                    long time_end = System.currentTimeMillis();
                    if ((time_end - time_start) < 300) {
                        chatOnClick();
                    }
                }

                y_cord_Destination = y_init_margin + y_diff;

                int BarHeight = getStatusBarHeight(context);
                if (y_cord_Destination < 0) {
                    y_cord_Destination = 0;
                } else if (y_cord_Destination + (bubbleView.getHeight() + BarHeight) > sizeWindow.y) {
                    y_cord_Destination = sizeWindow.y - (bubbleView.getHeight() + BarHeight);
                }
                layoutParams.y = y_cord_Destination;

                inBounded = false;
                resetPosition(x_cord);

                break;
            default:
                break;
        }
        return true;
    }

    private void closedBubble(){
        if (isActive) {
            activityChat.finish();
        }
        if(classService!=null)
        context.stopService(new Intent(context, classService));
        inBounded = false;
    }

    //Recontruccción de la vista al girar la pantalla de manera hotizontal
    void landscapeBubble() {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
        if (messageView != null) {
            messageView.setVisibility(View.GONE);
        }

        if (layoutParams.y + (bubbleView.getHeight() + getStatusBarHeight(context)) > sizeWindow.y) {
            layoutParams.y = sizeWindow.y - (bubbleView.getHeight() + getStatusBarHeight(context));
            windowManager.updateViewLayout(bubbleView, layoutParams);
        }

        if (layoutParams.x != 0 && layoutParams.x < sizeWindow.x) {
            resetPosition(sizeWindow.x);
        }

    }
    //Recontruccción de la vista al girar la pantalla de manera vertical
    void portraitBubble() {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
        //Si giramos la pantalla el mensaje desaparecera
        if (messageView != null) {
            messageView.setVisibility(View.GONE);
        }

        if (layoutParams.x > sizeWindow.x) {
            resetPosition(sizeWindow.x);
        }

    }

    //Si la posicion actual es menor o igual a la mitad de la pantalla entonces la burbuja se va a la izquierda Sino la burbuja se va a la derecha
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= sizeWindow.x / 2) {
            isLeft = true;
            //Forzar a que la burbuja se mueva a la izquierda
            moveToLeft(x_cord_now);
        } else {
            //Forzar a que la burbuja se mueva a la derecha
            isLeft = false;
            moveToRight(x_cord_now);

        }

    }
    //Codigo para mover la burbuja a la izquierda
    private void moveToLeft(final int x_cord_now) {
        final int x = sizeWindow.x - x_cord_now;
        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
            public void onTick(long t) {
                long step = (500 - t) / 5;
                //Agregamos la animacion de rebote a la burbuja y actualizamos
                mParams.x = 0 - (int) (double) bounceValue(step, x);
                windowManager.updateViewLayout(bubbleView, mParams);
            }
            public void onFinish() {
                mParams.x = 0;
                windowManager.updateViewLayout(bubbleView, mParams);
            }
        }.start();
    }
    //Codigo para mover la burbuja a la derecha
    private void moveToRight(final int x_cord_now) {
        new CountDownTimer(500, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
            public void onTick(long t) {
                long step = (500 - t) / 5;
                //Agregamos la animacion de rebote a la burbuja y actualizamos
                mParams.x = sizeWindow.x + (int) (double) bounceValue(step, x_cord_now) - bubbleView.getWidth();
                windowManager.updateViewLayout(bubbleView, mParams);
            }

            public void onFinish() {
                mParams.x = sizeWindow.x - bubbleView.getWidth();
                windowManager.updateViewLayout(bubbleView, mParams);
            }
        }.start();
    }

    //Animación de rebote
    private double bounceValue(long step, long scale) {
        return scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
    }

    //Mantener el margen en el que se mostrara la burbuja
     private static int getStatusBarHeight(Context context) {
         return (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
    }

    //Metodo donde se ejecuta el codigo al momento de hacer clic sobre la burbuja
    private void chatOnClick() {
        try {
            if (isActive) {
                //Finalizar ventana chat
                activityChat.finish();
            } else {
                //Abrir ventana chat
                if(classChat!=null)
                context.startActivity(new Intent(context, classChat).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Metodo donde se ejecuta el codigo al momento de mantener precionada la burbuja
    private void chatLongclick() {
        if (isActive) {
            //si el chat esta abierto cerrar antes de que aparezca remove
            activityChat.finish();
        }
        //Vizualizar remove burbuja
        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
        int x_cord_remove = (sizeWindow.x - removeView.getWidth()) / 2;
        int y_cord_remove = sizeWindow.y - (removeView.getHeight() + getStatusBarHeight(context));
        param_remove.x = x_cord_remove;
        param_remove.y = y_cord_remove;
        windowManager.updateViewLayout(removeView, param_remove);
    }


    //verificar si el contexto puede dibujar en la parte superior de otras aplicaciones.
    static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }
    //metodo para hacer que la imagen de la burbuja se vea circular
    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }

}
