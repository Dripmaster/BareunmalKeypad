package exam.bitbyte;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import exam.bitbyte.activitys.DialogActivity;
import exam.bitbyte.activitys.MainActivity;

public class BareunmalKeypadService extends InputMethodService implements
        KeyboardView.OnKeyboardActionListener {

    //욕설 횟수 카운트 변수
    int timeCount = 0;

    final static String TAG = "Bareunmal";
    //db 연결용 변수
    MySQLiteOpenHelper testHelper;
    SQLiteDatabase db;

    // 키보드 뷰 및 키보드들
    KeyboardView mInputView;
    BareunmalKeypad mHanBoard;
    BareunmalKeypad mHanUpBoard;
    BareunmalKeypad mEngBoard;
    BareunmalKeypad mEngUpBoard;
    BareunmalKeypad mNumBoard[];
    BareunmalKeypad mEditBoard;
    BareunmalKeypad mInitKeyboard;

    // 키보드 모드의 종류들
    final int MODE_HAN = 0;
    final int MODE_ENG = 1;
    final int MODE_NUM = 2;
    final int MODE_EDIT = 3;

    // 진동 및 사운드 관리자
    boolean mbVib = true;
    boolean mbSound = true;
    Vibrator mVib;
    AudioManager mAm;

    // 숫자, 편집 키보드 이전의 키보드
    BareunmalKeypad mPrevMode; // 한영만 가능. Mode 키를 누를 때 복귀
    BareunmalKeypad mPrevEdit; // 모든 모드 가능. Edit 키를 누를 때 복귀
    BareunmalKeypad mPrevNum;

    // 조합키의 상태 및 더블 푸시 시간
    long mLastUpperTime;
    long mLastCapTime;
    final int STATE_NORMAL = 0;
    final int STATE_PUSH = 1;
    final int STATE_LOCK = 2;
    final int DBLPUSHGAP = 300;
    int mUpperState = STATE_NORMAL;
    int mCapitalState = STATE_NORMAL;

    // 숫자 키보드의 개수, 현재 숫자키
    final int mNumBoardSize = 4;
    int mNowNumBoard = 0;

    // 조립 문자열
    StringBuilder mComp = new StringBuilder();

    // 폭 변경 조사를 위한 최후 폭
    private int mLastDisplayWidth;

    // 키보드 생성. 폭이 바뀐 경우만 재생성한다.
    @Override
    public void onInitializeInterface() {
        if (mHanBoard != null) {
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth)
                return;
            mLastDisplayWidth = displayWidth;
        }

        mHanBoard = new BareunmalKeypad(this, R.xml.hangul);
        mHanUpBoard = new BareunmalKeypad(this, R.xml.hangulupper);
        mEngBoard = new BareunmalKeypad(this, R.xml.english);
        mEngUpBoard = new BareunmalKeypad(this, R.xml.englishupper);
        mNumBoard = new BareunmalKeypad[mNumBoardSize];
        mNumBoard[0] = new BareunmalKeypad(this, R.xml.number1);
        mNumBoard[1] = new BareunmalKeypad(this, R.xml.number2);
        mNumBoard[2] = new BareunmalKeypad(this, R.xml.number3);
        mNumBoard[3] = new BareunmalKeypad(this, R.xml.number4);
        mEditBoard = new BareunmalKeypad(this, R.xml.edit);

        mVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    // 입력뷰 생성하고 한글 키보드로 초기화
    @Override
    public View onCreateInputView() {
        mInputView = (KeyboardView) getLayoutInflater().inflate(R.layout.input,
                null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mHanBoard);
        return mInputView;
    }

    // 입력 시작시 초기화
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        // 한글 입력기 초기화
        ResetCompo();

        // inputType에 따라 시작할 키보드 선택
        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_DATETIME:
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_PHONE:
                mInitKeyboard = mNumBoard[0];
                break;
            default:
            case EditorInfo.TYPE_CLASS_TEXT:
                mInitKeyboard = mHanBoard;

                int variation = attribute.inputType
                        & EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    mInitKeyboard = mEngBoard;
                }

                break;
        }
    }

    // 입력 끝 - 키보드를 닫는다.
    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mInitKeyboard = mHanBoard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    // 입력 종류에 따라 조사된 키보드로 설정한다.
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        mInputView.setKeyboard(mInitKeyboard);
        mInputView.closing();
    }

    // 입력중에 선택 영역이나 위치가 바뀌면 조립을 완성한다.
    // 샘플 코드를 따라한 것인데 샘플에 버그가 있는 듯해서 로그 찍어서 관찰한 후 수정해서 사용함.
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        Keyboard current = mInputView.getKeyboard();
        Log.d(TAG, "upsel" + oldSelStart + "," + oldSelEnd + "," + newSelStart
                + "," + newSelEnd + "," + candidatesStart + "," + candidatesEnd);
        if (GetNowMode(current) == MODE_HAN) {
            if (mComp.length() > 0
                    && (oldSelStart != candidatesStart || newSelEnd != candidatesEnd)) {
                // 임시적으로 조립이 풀린 경우는 아무 것도 하지 않는다.
                if (candidatesStart != -1) {
                    ResetCompo();
                    FinishCompo();
                }
            }
        }
    }

    // 홀드 다운 구현을 위해 로그 찍어 봤는데 화면 키보드에 대해서는 호출되지 않는다.
    // 모바일에서는 두 개의 키를 누르는 게 별 실용성이 없어 홀드 다운은 일단 제외함
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keydown" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyup" + keyCode);
        return super.onKeyUp(keyCode, event);
    }

    // 문자 입력을 받았을 때를 처리한다. 기능키 먼저 처리하고 문자키 처리한다.
    public void onKey(int primaryCode, int[] keyCodes) {
        Keyboard current = mInputView.getKeyboard();
        long now;
        boolean doublepush = false;

        Log.d(TAG, "onKey" + primaryCode);

        // 키 입력시 진동 발생시킨다.
		/*
		 * if (mbVib) { mVib.vibrate(1); }
		 */

        // 표준 키보드 소리를 내며 무음 모드이면 알아서 소리가 안남
        if (mbSound) {
            mAm.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }

        // 에러 처리. 커서 이동키로 편집 영역을 벗어나 버튼 등에서 입력하면 다운됨
        // 편집기 위가 아닐 때도 키보드가 닫히지 않으므로 에러 처리한다.
        if (getCurrentInputConnection() == null) {
            return;
        }

        // 펑션키는 일단 무시한다. -2000번 이후로 잠시 넣어 봤는데 보기 좋지 않다.
        if (primaryCode < -2000 && primaryCode > -2100) {
            return;
        }

        // 모드 변경 관련 키에 대한 공통적인 처리
        if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE || primaryCode == -10
                || primaryCode == -13) {
            // 모드 변경전에 한글 조립중이면 조립을 완성한다.
            if (mComp.length() > 0) {
                FinishCompo();
                ResetCompo();
            }

            // 모드 변경시 어퍼 상태, 대문자 상태는 무조건 해제한다. 락 상태이더라도.
            mUpperState = STATE_NORMAL;
            mCapitalState = STATE_NORMAL;
            ToggleUpper();
        }

        switch (primaryCode) {
            case Keyboard.KEYCODE_MODE_CHANGE:
                // 모드 전환
                switch (GetNowMode(current)) {
                    case MODE_HAN:
                        mInputView.setKeyboard(mEngBoard);
                        break;
                    case MODE_ENG:
                        mInputView.setKeyboard(mHanBoard);
                        break;
                    case MODE_NUM:
                        mInputView.setKeyboard(mPrevMode);
                        break;
                    case MODE_EDIT:
                        mInputView.setKeyboard(mPrevMode);
                        break;
                }
                break;
            case -10: // Edit
                if (GetNowMode(current) != MODE_EDIT) {
                    switch (GetNowMode(current)) {
                        case MODE_HAN:
                            mPrevEdit = mPrevMode = mHanBoard;
                            break;
                        case MODE_ENG:
                            mPrevEdit = mPrevMode = mEngBoard;
                            break;
                        case MODE_NUM:
                            mPrevEdit = mNumBoard[0];
                            break;
                    }
                    mInputView.setKeyboard(mEditBoard);
                } else {
                    mInputView.setKeyboard(mPrevEdit);
                }
                break;
            case -13: // Num
                // 이전 모드를 저장한다.
                if (GetNowMode(current) != MODE_NUM) {
                    switch (GetNowMode(current)) {
                        case MODE_HAN:
                            mPrevNum = mPrevMode = mHanBoard;
                            break;
                        case MODE_ENG:
                            mPrevNum = mPrevMode = mEngBoard;
                            break;
                        case MODE_EDIT:
                            mPrevNum = mEditBoard;
                            break;
                    }
                    mNowNumBoard = 0;
                    mInputView.setKeyboard(mNumBoard[mNowNumBoard]);
                } else {
                    mInputView.setKeyboard(mPrevNum);
                }
                break;
            case -11: // Upper
                now = System.currentTimeMillis();
                if (now - mLastUpperTime < DBLPUSHGAP) {
                    doublepush = true;
                    mLastUpperTime = 0;
                } else {
                    mLastUpperTime = now;
                }

                // 더블 푸시했으면 락, 아니면 토글
                if (doublepush) {
                    mUpperState = STATE_LOCK;
                    ToggleUpper();
                } else if (mUpperState == STATE_NORMAL) {
                    mUpperState = STATE_PUSH;
                    ToggleUpper();
                } else {
                    mUpperState = STATE_NORMAL;
                    ToggleUpper();
                }
                break;
            case -12: // Pg1
                if (mNowNumBoard < mNumBoardSize - 1) {
                    mNowNumBoard++;
                } else {
                    mNowNumBoard = 0;
                }
                mInputView.setKeyboard(mNumBoard[mNowNumBoard]);
                break;
            case -14: // Ctrl, Shift, Alt는 그냥 무시
            case -15:
            case -16:
                return;
            case Keyboard.KEYCODE_SHIFT:
                now = System.currentTimeMillis();
                if (now - mLastCapTime < DBLPUSHGAP) {
                    doublepush = true;
                    mLastCapTime = 0;
                } else {
                    mLastCapTime = now;
                }

                if (doublepush) {
                    mCapitalState = STATE_LOCK;
                } else {
                    if (mCapitalState == STATE_NORMAL) {
                        mCapitalState = STATE_PUSH;
                    } else {
                        mCapitalState = STATE_NORMAL;
                    }

                    ToggleCapital();
                }
                break;
            // Upper + Space 외에 별도의 삭제 키를 둠. 모바일에서는 이 키가 필요하다.
            // Upper + Space 기능도 일단 같이 유지한다.
            case Keyboard.KEYCODE_DELETE:
                if (GetNowMode(current) == MODE_HAN) {
                    HangulBs();
                } else {
                    keyDownUp(KeyEvent.KEYCODE_DEL);
                }
                break;
            default:
                onKeyChar(primaryCode);
                break;
        }
    }

    // 기능키가 아닌 일반 문자키 처리
    void onKeyChar(int primaryCode) {
        Keyboard current = mInputView.getKeyboard();

        // 미할당 키이면 아무 것도 입력하지 않고 리턴한다.
        if (primaryCode == -99) {
            return;
        }

        // 모드에 따라 분기
        switch (GetNowMode(current)) {
            case MODE_HAN:
                switch (primaryCode) {
                    case 10:
                        keyDownUp(KeyEvent.KEYCODE_ENTER);
                        break;
                    default:
                        ProcessHangul(primaryCode);
                }
                break;
            case MODE_EDIT:
                // 기능키는 커서 이동 정도만 처리 함
                switch (primaryCode) {
                    case -1013: // left
                        keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
                        break;
                    case -1015: // right
                        keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                        break;
                    case -1006: // up
                        keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
                        break;
                    case -1014: // down
                        keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
                        break;
                    case 32: // 공백키와 Enter는 문자 입력으로 간주한다.
                        break;
                    case 10:
                        getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
                        break;
                }
                break;
            case MODE_ENG:
            case MODE_NUM:
                // 대문자 상태이면 코드를 대문자로 바꾼다.
                if (mCapitalState != STATE_NORMAL) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }

                // 문자를 편집기로 보낸다.
                getCurrentInputConnection().commitText(
                        String.valueOf((char) primaryCode), 1);

                // 대문자 입력했으면 다시 소문자 모드로 복귀. 단 락 상태일 때는 유지
                if (mCapitalState == STATE_PUSH) {
                    mCapitalState = STATE_NORMAL;
                    ToggleCapital();
                }
                break;
        }

        // 어퍼 상태에서 글자 입력했으면 다시 노말로 복귀. 단 락 상태일 때는 유지
        if (mUpperState == STATE_PUSH) {
            mUpperState = STATE_NORMAL;
            ToggleUpper();
        }
    }

    // nUpper값에 따라 키보드 교체
    void ToggleUpper() {
        Keyboard current = mInputView.getKeyboard();

        if (mUpperState == STATE_NORMAL) {
            if (current == mHanUpBoard) {
                mInputView.setKeyboard(mHanBoard);
            }
            if (current == mEngUpBoard) {
                mInputView.setKeyboard(mEngBoard);
            }
        } else {
            if (current == mHanBoard) {
                mInputView.setKeyboard(mHanUpBoard);
            }
            if (current == mEngBoard) {
                mInputView.setKeyboard(mEngUpBoard);
            }
        }
    }

    void ToggleCapital() {
        Keyboard current = mInputView.getKeyboard();

        if (current == mEngBoard || current == mEngUpBoard) {
            if (mCapitalState == STATE_NORMAL) {
                mInputView.setShifted(false);
            } else {
                mInputView.setShifted(true);
            }
        }
    }

    int GetNowMode(Keyboard current) {
        if (current == mHanBoard || current == mHanUpBoard) {
            return MODE_HAN;
        }
        if (current == mEngBoard || current == mEngUpBoard) {
            return MODE_ENG;
        }
        for (int i = 0; i < mNumBoardSize; i++) {
            if (current == mNumBoard[i]) {
                return MODE_NUM;
            }
        }
        if (current == mEditBoard) {
            return MODE_EDIT;
        }

        return MODE_HAN;
    }

    // 키를 눌렀다가 떼는 동작을 하는 도우미 함수
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    // 액션 리스너 인터페이스가 요구하는 메서드들. 빈 채로 둠
    public void onText(CharSequence text) {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }

    public void swipeDown() {
    }

    public void swipeLeft() {
    }

    public void swipeRight() {
    }

    public void swipeUp() {
    }

    // 입력중인 초성, 중성, 종성 개별 음소들. 99면 없다는 뜻이다.
    // 조합형은 비트 연산해서 추출할 수 있지만 유니코드는 어려우므로 별도의 변수에 기록한다.
    // 꼭 추출하려면 가능은 하지만 속도상의 문제도 있고 해서 음소별로 저장했다.
    // 복자음, 복모음도 하나의 변수에 기록해 두고 필요시 분리해서 조사한다.
    int cho, jung, jong;

    // 한글 조립 상태에 대한 상수
    final int H_NONE = 0; // 아무것도 없는 상태 예:
    final int H_CHO = 1; // 초성 하나 입력된 상태. 예:ㄱ, ㄳ
    final int H_JUNGONLY = 2; // 초성없이 중성만 입력된 상태. 예:ㅏ
    final int H_JUNG = 3; // 초성 + 중성. 예:가, 과
    final int H_JONG = 4; // 초성 + 중성 + 종성 예:각, 닭

    // 각 음소별 속성
    // 첨자 0 : 0이면 자음, 1이면 모음
    // 첨자 1 : 초성의 순서값. 99는 낱글자 구성만 가능하고 음절은 불가하다는 뜻
    // : 중성은 음소의 등장 순서가 코드 순서와 일치하므로 -30하면 됨
    // 첨자 2 : 종성의 순서값. 종성이 없을 경우는 0임. 99는 종성으로 쓸 수 없는 문자
    int[][] hanattr = { { 0, 0, 1 }, // 0.ㄱ
            { 0, 1, 2 }, // 1.ㄲ
            { 0, 99, 3 }, // 2.ㄳ
            { 0, 2, 4 }, // 3.ㄴ
            { 0, 99, 5 }, // 4.ㄵ
            { 0, 99, 6 }, // 5.ㄶ
            { 0, 3, 7 }, // 6.ㄷ
            { 0, 4, 99 }, // 7.ㄸ
            { 0, 5, 8 }, // 8.ㄹ
            { 0, 99, 9 }, // 9.ㄺ
            { 0, 99, 10 }, // 10.ㄻ
            { 0, 99, 11 }, // 11.ㄼ
            { 0, 99, 12 }, // 12.ㄽ
            { 0, 99, 13 }, // 13.ㄾ
            { 0, 99, 14 }, // 14.ㄿ
            { 0, 99, 15 }, // 15.ㅀ
            { 0, 6, 16 }, // 16.ㅁ
            { 0, 7, 17 }, // 17.ㅂ
            { 0, 8, 99 }, // 18.ㅃ
            { 0, 99, 18 }, // 19.ㅄ
            { 0, 9, 19 }, // 20.ㅅ
            { 0, 10, 20 }, // 21.ㅆ
            { 0, 11, 21 }, // 22.ㅇ
            { 0, 12, 22 }, // 23.ㅈ
            { 0, 13, 99 }, // 24.ㅉ
            { 0, 14, 23 }, // 25.ㅊ
            { 0, 15, 24 }, // 26.ㅋ
            { 0, 16, 25 }, // 27.ㅌ
            { 0, 17, 26 }, // 28.ㅍ
            { 0, 18, 27 }, // 29.ㅎ
            { 1, 0, 0 }, // 30.ㅏ
            { 1, 0, 0 }, // 31.ㅐ
            { 1, 0, 0 }, // 32.ㅑ
            { 1, 0, 0 }, // 33.ㅒ
            { 1, 0, 0 }, // 34.ㅓ
            { 1, 0, 0 }, // 35.ㅔ
            { 1, 0, 0 }, // 36.ㅕ
            { 1, 0, 0 }, // 37.ㅖ
            { 1, 0, 0 }, // 38.ㅗ
            { 1, 0, 0 }, // 39.ㅘ
            { 1, 0, 0 }, // 40.ㅙ
            { 1, 0, 0 }, // 41.ㅚ
            { 1, 0, 0 }, // 42.ㅛ
            { 1, 0, 0 }, // 43.ㅜ
            { 1, 0, 0 }, // 44.ㅝ
            { 1, 0, 0 }, // 45.ㅞ
            { 1, 0, 0 }, // 46.ㅟ
            { 1, 0, 0 }, // 47.ㅠ
            { 1, 0, 0 }, // 48.ㅡ
            { 1, 0, 0 }, // 49.ㅢ
            { 1, 0, 0 }, // 50.ㅣ
    };

    // 한글 입력을 처리한다.
    void ProcessHangul(int code) {
        boolean bConso;
        int idx;

        // XML 문서의 키 코드를 ㄱ을 베이스로 한 첨자로 변환한다.
        // 이 첨자는 유니코드상 낱글자의 순서값이며 음소의 코드로 사용한다.
        if (code >= 0x3131 && code <= 0x3163) {
            idx = code - 0x3131;
            //MakingCompo(code);
        } else {
            // 한글이 아닌 다른 문자이면 조립을 끝낸다.
            FinishCompo();
            ResetCompo();

            // 문자 입력
            getCurrentInputConnection().commitText(String.valueOf((char) code),
                    1);
            return;
        }

        Log.d(TAG, "State = " + GetHanState() + ",idx = " + idx);

        // 입력된 글자가 자음인지 조사한다.
        if (hanattr[idx][0] == 0) {
            bConso = true;
        } else {
            bConso = false;
        }

        // 조립 상태에 따라 분기한다. 이후 입력된 글자의 자음, 모음 여부와 기존 글자의
        // 복자음, 복모음 여부에 따라 다양하게 분기된다.
        switch (GetHanState()) {
            case H_NONE:
                // 자음이면 자음 단독으로 입력한다. 낱글자 코드를 쓰면 된다.
                if (bConso) {
                    cho = idx;
                    AppendCompo(idx + 0x3131);
                    // 모음이면 모음만으로 글자 구성.
                } else {
                    jung = idx;
                    AppendCompo(idx + 0x3131);
                }
                break;
            case H_CHO:
                if (IsBokJa(cho) == false) {
                    if (bConso) {
                        int bokja = findBokJa(cho, idx);
                        // 복자음 구성 가능하면 복자음만으로 초성 만듬. 예:ㄱ상태에서 ㅅ입력시 ㄳ
                        if (bokja != 99) {
                            cho = bokja;
                            ReplaceCompo(cho + 0x3131);
                            // 복자음 구성되지 않을 경우 - 음절 분리. 예:ㄱ 상태에서 ㄴ입력시
                        } else {
                            // 조립중인 글자 완성하고 새로 입력된 초성으로 단독 글자 만듬
                            FinishCompo();
                            ResetCompo();
                            cho = idx;
                            AppendCompo(cho + 0x3131);
                        }
                    } else {
                        // 모음이면 앞에 입력된 자음과 결합하여 새 글자로 대체. 예:ㄱ 상태에서 ㅏ입력시 가
                        jung = idx;
                        ReplaceCompo(GetHanCode(cho, jung, 99));
                    }
                } else {
                    // 복자음 초성만 입력된 상태에서 또 자음이 들어올 경우. 예:ㄳ 상태에서 ㄴ입력시 ㄳㄴ
                    if (bConso) {
                        // 새로 입력된 초성으로 단독 글자 만듬
                        FinishCompo();
                        ResetCompo();
                        cho = idx;
                        AppendCompo(cho + 0x3131);
                    } else {
                        // 복자음 앞쪽 초성만으로 한 글자 완성. 예:ㄳ상태에서 ㅏ 입력시 ㄱ사
                        ReplaceCompo(GetLeftBokJa(cho) + 0x3131);

                        // 복자음 뒤쪽 글자와 새로 입력된 중성과 조합하여 새 음절 분리
                        FinishCompo();
                        int newcho = GetRightBokJa(cho);
                        ResetCompo();
                        cho = newcho;
                        jung = idx;
                        AppendCompo(GetHanCode(cho, jung, 99));
                    }
                }
                break;
            case H_JUNGONLY:
                // 모음만 입력된 상태에서 자음이 들어오면 음절 분리. 예:ㅏ상태에서 ㄱ입력시 ㅏㄱ
                if (bConso) {
                    // 새로 입력된 초성으로 단독 글자 만듬
                    FinishCompo();
                    ResetCompo();
                    cho = idx;
                    AppendCompo(cho + 0x3131);
                } else {
                    int bokmo = findBokMo(jung, idx);
                    // 복모음 구성 가능하면 복모음으로 대체. 단 초성은 없음. 예:ㅗ상태에서 ㅏ입력시 ㅘ
                    if (bokmo != 99) {
                        jung = bokmo;
                        ReplaceCompo(jung + 0x3131);
                        // 복모음 구성되지 않을 경우 - 음절 분리
                    } else {
                        // 새로 입력된 중성으로 단독 글자 만듬
                        FinishCompo();
                        ResetCompo();
                        jung = idx;
                        AppendCompo(jung + 0x3131);
                    }
                }
                break;
            case H_JUNG:
                // 자음이면 받침으로
                if (bConso) {
                    // 받침이 될 수 없는 글자인 경우 음절 분리. 예:아 상태에서 ㄸ입력시 아ㄸ
                    if (hanattr[idx][2] == 99) {
                        // 조립중인 글자 완성
                        FinishCompo();

                        // 새로 입력된 초성으로 단독 글자 만듬
                        ResetCompo();
                        cho = idx;
                        AppendCompo(cho + 0x3131);
                    } else {
                        jong = idx;
                        ReplaceCompo(GetHanCode(cho, jung, jong));
                    }
                    // 또 모음이면 복모음 구성
                } else {
                    int bokmo = findBokMo(jung, idx);
                    // 복모음 구성 가능하면 복모음으로 대체
                    if (bokmo != 99) {
                        jung = bokmo;
                        ReplaceCompo(GetHanCode(cho, jung, jong));
                        // 복모음 구성되지 않을 경우 - 음절 분리. 예:아 상태에서 ㅓ입력시 아ㅓ
                    } else {
                        // 조립중인 글자 완성
                        FinishCompo();

                        // 중성만으로 글자 구성
                        ResetCompo();
                        jung = idx;
                        AppendCompo(jung + 0x3131);
                    }
                }
                break;
            case H_JONG:
                if (IsBokJa(jong) == false) {
                    // 자음이면 복자음 받침 구성. 또는 새 음절로 분리
                    if (bConso) {
                        int bokja = findBokJa(jong, idx);
                        // 복자음 구성 가능하면 복자음 받침으로 대체. 예:달 상태에서 ㄱ입력시 닭
                        if (bokja != 99) {
                            jong = bokja;
                            ReplaceCompo(GetHanCode(cho, jung, jong));
                            // 복자음 구성되지 않을 경우 - 음절 분리
                        } else {
                            // 조립중인 글자 완성
                            FinishCompo();

                            // 새로 입력된 초성으로 단독 글자 만듬
                            ResetCompo();
                            cho = idx;
                            AppendCompo(cho + 0x3131);
                        }
                        // 모음이면 음절 분리
                    } else {
                        // 기존의 초성, 중성으로 한 글자 완성
                        ReplaceCompo(GetHanCode(cho, jung, 99));
                        FinishCompo();

                        // 이전 글자의 종성을 초성으로 하고 새로 입력된 중성과 조합하여 새 음절 분리
                        int newcho = jong;
                        ResetCompo();
                        cho = newcho;
                        jung = idx;
                        AppendCompo(GetHanCode(cho, jung, 99));
                    }
                } else {
                    // 자음이면 무조건 음절 분리
                    if (bConso) {
                        // 새로 입력된 초성으로 단독 글자 만듬
                        FinishCompo();
                        ResetCompo();
                        cho = idx;
                        AppendCompo(cho + 0x3131);
                        // 모음이면 복모음 하나 떼 와서 음절 분리
                    } else {
                        // 기존의 초성, 중성, 복자음 앞자로 한 글자 완성
                        ReplaceCompo(GetHanCode(cho, jung, GetLeftBokJa(jong)));

                        // 이전 글자의 복자음 뒷자를 초성으로 하고 새로 입력된 중성과 조합하여 새 음절 분리
                        FinishCompo();
                        int newcho = GetRightBokJa(jong);
                        ResetCompo();
                        cho = newcho;
                        jung = idx;
                        AppendCompo(GetHanCode(cho, jung, 99));
                    }
                }
                break;
        }
    }

    // 조립중인 한글 삭제 처리 - 역 오토마타
    void HangulBs() {

        Log.d(TAG, "State = " + GetHanState());

        switch (GetHanState()) {
            case H_NONE:
                keyDownUp(KeyEvent.KEYCODE_DEL);
                DELTMP();
                break;
            case H_CHO:
                if (IsBokJa(cho) == false) {
                    ResetCompo();
                    getCurrentInputConnection().commitText("", 0);
                } else {
                    // 복자음 초성인 경우 앞쪽 초성만 남김. ㄳ -> ㄱ
                    cho = GetLeftBokJa(cho);
                    ReplaceCompo(cho + 0x3131);
                }
                break;
            case H_JUNGONLY:
                // 단모임인 경우는 삭제
                if (IsBokMo(jung) == false) {
                    ResetCompo();
                    keyDownUp(KeyEvent.KEYCODE_DEL);
                    // 복모음이면 앞쪽 모음만 남김. ㅘ -> ㅗ
                } else {
                    jung = GetLeftBokMo(jung);
                    ReplaceCompo(jung + 0x3131);
                }
                break;
            case H_JUNG:
                // 복모음이 아니면 모음 삭제하고 자음만 남긴다. 예:가 -> ㄱ
                if (IsBokMo(jung) == false) {
                    ReplaceCompo(cho + 0x3131);
                    jung = 99;
                    // 복모음이면 뒤쪽 모음을 삭제한다. 예:와 -> 오
                } else {
                    jung = GetLeftBokMo(jung);
                    ReplaceCompo(GetHanCode(cho, jung, 99));
                }
                break;
            case H_JONG:
                // 복자음이 아니면 받침 삭제한다. 예:간 -> 가
                if (IsBokJa(jong) == false) {
                    ReplaceCompo(GetHanCode(cho, jung, 99));
                    jong = 99;
                    // 복자음 받침이면 뒤쪽 받침만 삭제한다. 예:닭 -> 달
                } else {
                    jong = GetLeftBokJa(jong);
                    ReplaceCompo(GetHanCode(cho, jung, jong));
                }
                break;
        }
    }

    // cho, jung, jong 값으로 한글 유니코드 음절 코드를 구한다.
    int GetHanCode(int acho, int ajung, int ajong) {
        int choorder, jungorder, jongorder;

        choorder = hanattr[acho][1];
        if (ajung == 99) {
            jungorder = 99;
        } else {
            jungorder = ajung - 30;
        }

        if (ajong == 99) {
            jongorder = 99;
        } else {
            jongorder = hanattr[ajong][2];
        }

        // 유니코드 "가"자를 베이스로 초성만큼 거리 띄움
        int resultcode = 0xac00 + (choorder * 21 * 28);

        // 중성이 있으면 중성만큼 띄움
        if (jungorder != 99) {
            resultcode += (jungorder * 28);
        }

        // 종성이 있으면 종성만큼 띄움
        if (jongorder != 99) {
            resultcode += jongorder;
        }

        return resultcode;
    }

    // 현재 한글 조립 상태를 조사한다.
    int GetHanState() {
        // 초성이 없는 경우 - 아무것도 없거나 중성만 입력된 경우
        if (cho == 99) {
            if (jung == 99) {
                return H_NONE;
            } else {
                return H_JUNGONLY;
            }
        }

        // 초성은 있는데 중성이 없는 경우
        if (jung == 99)
            return H_CHO;

        // 중성이 있는데 종성이 없는 경우
        if (jong == 99)
            return H_JUNG;

        // 종성까지 다 입력된 경우
        return H_JONG;
    }

    // 복자음 테이블
    // 첨자 0 : 원래 음소
    // 첨자 2 : 새로 입력된 음소
    // 첨자 3 : 대체될 음소
    int arBokJa[][] = { { 0, 20, 2 }, // 앇
            { 3, 23, 4 }, // 앉
            { 3, 29, 5 }, // 않
            { 8, 0, 9 }, // 앍
            { 8, 16, 10 }, // 앎
            { 8, 17, 11 }, // 앏
            { 8, 20, 12 }, // 앐
            { 8, 27, 13 }, // 앑
            { 8, 28, 14 }, // 앒
            { 8, 29, 15 }, // 앓
            { 17, 20, 19 }, // 앖
    };

    // 복자음인지 조사하여 순서값 리턴. 아니면 99 리턴
    int findBokJa(int oldidx, int newidx) {
        int bokja = 99;
        for (int i = 0; i < arBokJa.length; i++) {
            if (oldidx == arBokJa[i][0] && newidx == arBokJa[i][1]) {
                bokja = arBokJa[i][2];
                break;
            }
        }
        return bokja;
    }

    // 복자음인지 조사한다.
    boolean IsBokJa(int code) {
        for (int i = 0; i < arBokJa.length; i++) {
            if (code == arBokJa[i][2]) {
                return true;
            }
        }
        return false;
    }

    // 복자음의 왼쪽을 구한다.
    int GetLeftBokJa(int code) {
        for (int i = 0; i < arBokJa.length; i++) {
            if (code == arBokJa[i][2]) {
                return arBokJa[i][0];
            }
        }
        return 99;
    }

    // 복자음의 오른쪽을 구한다.
    int GetRightBokJa(int code) {
        for (int i = 0; i < arBokJa.length; i++) {
            if (code == arBokJa[i][2]) {
                return arBokJa[i][1];
            }
        }
        return 99;
    }

    // 복모음 테이블
    // 첨자 0 : 원래 음소
    // 첨자 2 : 새로 입력된 음소
    // 첨자 3 : 대체될 음소

    int[][] arBokMo = {
            { 38, 30, 39 }, // ㅗ+ㅏ=ㅘ
            { 38, 50, 41 }, // ㅗ+ㅣ=ㅚ
            { 43, 34, 44 }, // ㅜ+ㅓ=ㅝ
            { 43, 50, 46 }, // ㅜ+ㅣ=ㅟ
            { 38, 31, 40 }, // ㅗ+ㅐ=ㅙ
            { 43, 35, 45 }, // ㅜ+ㅔ=ㅞ
            { 48, 50, 49 }, // ㅡ+ㅣ=ㅢ
    };

    // 복모음인지 조사하여 순서값 리턴. 아니면 99 리턴

    int findBokMo(int oldidx, int newidx) {
        int bokmo = 99;
        for (int i = 0; i < arBokMo.length; i++) {
            if (oldidx == arBokMo[i][0] && newidx == arBokMo[i][1]) {
                bokmo = arBokMo[i][2];
                break;
            }
        }
        return bokmo;
    }

    // 복자음인지 조사한다.
    boolean IsBokMo(int code) {
        for (int i = 0; i < arBokMo.length; i++) {
            if (code == arBokMo[i][2]) {
                return true;
            }
        }
        return false;
    }

    // 복자음의 왼쪽을 구한다.
    int GetLeftBokMo(int code) {
        for (int i = 0; i < arBokMo.length; i++) {
            if (code == arBokMo[i][2]) {
                return arBokMo[i][0];
            }
        }

        return 99;
    }

    // 복자음의 오른쪽을 구한다.
    int GetRightBokMo(int code) {
        for (int i = 0; i < arBokMo.length; i++) {
            if (code == arBokMo[i][2]) {
                return arBokMo[i][0];
            }
        }

        return 99;
    }

    // 한글 입력기의 상태 변수값을 초기화한다.
    void ResetCompo() {
        cho = jung = jong = 99;
        mComp.setLength(0);
    }

    // 조립문자열 뒤에 추가
    void AppendCompo(int code) {
        InputConnection ime = getCurrentInputConnection();
        mComp.append((char) code);
        ime.setComposingText(mComp, 1);
        filteringYork();
    }

    // 조립 문자열 대체
    void ReplaceCompo(int code) {
        InputConnection ime = getCurrentInputConnection();
        mComp.setCharAt(0, (char) code);
        ime.setComposingText(mComp, 1);
        MakingCompo(code);
    }

    // 조립 중인 문자열 확정

    void MakingCompo(int code) { //현재 만들어지고 있는 문자열
        System.out.println("MAKING COMPO : ");
        InputConnection ime = getCurrentInputConnection();
        System.out.println(String.valueOf((char) code));
        System.out.println(ime.getTextBeforeCursor(0, 10));
        filteringYork();
    }

    void FinishCompo() { //입력모드를 종료한다.
        InputConnection ime = getCurrentInputConnection();
        ime.finishComposingText();

    }

    void DELTMP(){ //지워지고 있는동안 현재 남아있는 텍스트창의 텍스트를 print

        InputConnection ime = getCurrentInputConnection();
        if(tmp.length()>1){
            tmp = tmp.substring(tmp.length()-1);
        }
        else
            tmp="";
        System.out.println("DEL " + ime.getTextBeforeCursor((ime.toString()).length()-1, 0));
    }
    void writeLog(String str) {//log 테이블에 욕설 인설트

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        String query = "INSERT INTO log VALUES('" + date + "', '" + str + "');";
        db.execSQL(query);
    }

    void viewLog() {//log테이블 연결해서 필드(컬럼) 갯수로 욕쓴 횟수 봄
        Cursor cursor = db.rawQuery("SELECT count(*) FROM log WHERE 2014-11-23;", null); //욕테이블 갯수
        cursor.moveToFirst();
        int count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));

        timeCount++;

        //타이머
        TimerTask myTask = new TimerTask() {
            public void run() {
                timeCount = 0;
            }
        };
        Timer timer = new Timer();
        timer.schedule(myTask, 10000);  // 10초

        if (timeCount == 4){
            Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        }

        //Toast.makeText(this, "총 "+count+"번의 욕설이 감지되었습니다.", Toast.LENGTH_SHORT).show();
        //노티피케이션
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon);//required
        mBuilder.setContentTitle("바른말 키패드");//required
        mBuilder.setContentText("총 " + count + "번의 욕설이 감지되었습니다.");//required
        mBuilder.setTicker("바른말 키패드 - 욕설이 감지되었습니다.");//optional
        mBuilder.setWhen(0);
        mBuilder.setAutoCancel(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
        //노티피케이션 끝
    }

    //필터링 함수
    String tmp = "";
    String york[];
    String mal[];
    boolean once=true;
    boolean vibration=false;
    boolean emoticon=false;
    void filteringYork () {
        if(once){
            testHelper = new MySQLiteOpenHelper(this);
            db = testHelper.getWritableDatabase(); //DB연결?

            Cursor cursor = db.rawQuery("SELECT count(*) FROM york;", null); //욕테이블 갯수
            cursor.moveToFirst();
            int count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count(*)")));
            york = new String[count]; //갯수만큼 할당
            mal = new String[count];
            cursor = db.rawQuery("SELECT * FROM york;", null);

            int index=0;
            if (cursor.moveToFirst()) {
                do {
                    york[index] = cursor.getString(cursor.getColumnIndex("plain"));
                    mal[index++] = cursor.getString(cursor.getColumnIndex("emoticon"));
                } while (cursor.moveToNext());
            }
            cursor = db.rawQuery("SELECT * FROM settings;", null);
            String name[] = { "", "", "", "", "" },
                    user_selected[] = { "", "", "","", "" };

            int index2 = 0;
            if (cursor.moveToFirst()) {
                do {
                    name[index2] = cursor.getString(cursor.getColumnIndex("name"));
                    user_selected[index2++] = cursor.getString(cursor
                            .getColumnIndex("user_selected"));
                } while (cursor.moveToNext());
            }

            if (user_selected[1].equals("1"))
                vibration=true;
            else
                vibration=false;
            if(user_selected[2].equals("1"))
                emoticon=true;
            else
                emoticon=false;


            //once = false;
        }
        InputConnection ime = getCurrentInputConnection();
        tmp=(String) ime.getTextBeforeCursor((ime.toString()).length()-1, 0);
        System.out.println("FINISH : " + tmp);
        for (int i = 0; i < york.length; i++) {
            if (tmp.matches(".*" + york[i] + ".*")) {
                if(vibration)
                    mVib.vibrate(400);
                if(!emoticon)//emoticon 비활성화
                    break;
                writeLog(york[i]);
                ime.deleteSurroundingText(york[i].length()-1, 0);
                ime.commitText(mal[i], 1);
                tmp = "";
                viewLog();
                FinishCompo();
                break;
            }
        }
    }
	/* end of class */
}