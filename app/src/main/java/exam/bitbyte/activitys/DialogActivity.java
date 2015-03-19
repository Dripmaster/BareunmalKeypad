package exam.bitbyte.activitys;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import exam.bitbyte.R;

/**
 * Created by AhnSeohyung on 15. 3. 17..
 */
public class DialogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

//        AlertDialog.Builder localBuilder1 = new AlertDialog.Builder(DialogActivity.this);
//        localBuilder1.setMessage("당신의 말, 상대방의 입장에서 생각해보셨나요?\n말은 자신의 인격입니다.");
//        localBuilder1.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
//        localBuilder1.setTitle("연속적인 욕설 감지");
//        localBuilder1.setCancelable(false);
//        localBuilder1.show();

        Button button = (Button) findViewById(R.id.dialogBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            // 영역외 터치시 닫히지 않도록
            return false;

        }
        return super.dispatchTouchEvent(ev);
    }
}
