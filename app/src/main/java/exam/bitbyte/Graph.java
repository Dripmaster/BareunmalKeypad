package exam.bitbyte;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by AhnSeohyung on 14. 11. 24..
 */
public class Graph extends View {
    float Max=0; // 최대값
    int x=100, y=100; //좌표
    float[] recordGRP=new float [7]; // 데이타
    int Myi=0; // 최대값의 배열 번호



    public Graph(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);

        for(int i=0; i<7; i++){
            recordGRP[i]=(int)(Math.random()*100)+1;
            if(recordGRP[i]>Max) {
                Myi=i;
                Max=recordGRP[i];
            }
        }//데이타 랜덤 생성 및 최대값 구하기. Max와 최대값의 배열 번호가 구해짐

        for(int i=0; i<7; i++){
            recordGRP[i]=(recordGRP[i]/Max * 700);
        }//백분율
    }

    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        //167d8e
        paint.setColor(Color.rgb(16+6, 16*7+13, 16*8+14));

        for(int i=0; i<7; i++) {
            canvas.drawRect(x+(98*i), 1600-(y + recordGRP[i]), x + 72+(98*i), 1600-y, paint);
        }//배열 0번째부터 6번째까지 출력. 왼쪽 위 좌표, 왼쪽 아래 좌표, 왼쪽 위 좌표, 왼쪽 아래 좌표
        //1600에서 뺀 이유는 그래프를 뒤집기 위함
    }
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        } // 터치 인식
        invalidate();
        return true;
    }
}