package nemosofts.tamilaudiopro.dialog;

import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class TextSizeDialog {

    private final SPHelper spHelper;
    private Dialog dialog;
    private final Activity ctx;

    public TextSizeDialog(@NonNull Activity ctx) {
        this.ctx = ctx;
        spHelper = new SPHelper(ctx);
    }

    public void showDialog() {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_text_size);

        AtomicInteger textSize = new AtomicInteger(spHelper.getTextSize());
        RadioGroup rg =  dialog.findViewById(R.id.rg);

        if (textSize.get() == 1){
            rg.check(R.id.rd_1);
        } else if (textSize.get() == 2){
            rg.check(R.id.rd_1);
        } else if (textSize.get() == 3){
            rg.check(R.id.rd_3);
        } else if (textSize.get() == 4){
            rg.check(R.id.rd_4);
        } else if (textSize.get() == 5){
            rg.check(R.id.rd_5);
        }

        dialog.findViewById(R.id.rd_1).setOnClickListener(view -> textSize.set(1));
        dialog.findViewById(R.id.rd_2).setOnClickListener(view -> textSize.set(2));
        dialog.findViewById(R.id.rd_3).setOnClickListener(view -> textSize.set(3));
        dialog.findViewById(R.id.rd_4).setOnClickListener(view -> textSize.set(4));
        dialog.findViewById(R.id.rd_5).setOnClickListener(view -> textSize.set(5));
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            spHelper.setTextSize(textSize.get());
            dismissDialog();
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
