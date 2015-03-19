package exam.bitbyte;

import android.content.Context;
import android.inputmethodservice.Keyboard;

import exam.bitbyte.activitys.MainActivity;

public class BareunmalKeypad extends Keyboard {
	public BareunmalKeypad(Context context, int xmlLayoutResId) {
		super(context, xmlLayoutResId);
		MainActivity keyboardmain = new MainActivity();
	}

	public BareunmalKeypad(Context context, int layoutTemplateResId,
                           CharSequence characters, int columns, int horizontalPadding) {
		super(context, layoutTemplateResId, characters, columns,
				horizontalPadding);
	}
}
