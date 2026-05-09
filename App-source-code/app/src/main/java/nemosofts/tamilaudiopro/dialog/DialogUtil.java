package nemosofts.tamilaudiopro.dialog;

import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;

import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.BillingSubscribeActivity;
import nemosofts.tamilaudiopro.callback.Callback;

public class DialogUtil {

    private static Dialog dialog;

    private DialogUtil() {
        throw new IllegalStateException("Utility class");
    }

    // Dialog
    public static void exitDialog(Activity activity) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_exit);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.exit);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.sure_exit);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> dialog.dismiss());

        dialog.findViewById(R.id.tv_dialog_no).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_dialog_yes).setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        Window window = dialog.getWindow();
        if (window != null){
            window.setBackgroundDrawableResource(android.R.color.transparent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setBackgroundBlurRadius(100);
            }
            window.getAttributes().windowAnimations = R.style.dialogAnimation;
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
        dialog.show();
    }

    public static void maintenanceDialog(Activity activity) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_error);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.maintenance);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.we_are_performing_scheduled);

        // VISIBLE
        TextView titleSub = dialog.findViewById(R.id.tv_dialog_title_sub);
        titleSub.setVisibility(View.VISIBLE);
        titleSub.setText(R.string.temporarily_down_for_maintenance);

        ImageView iconBg = dialog.findViewById(R.id.iv_dialog_icon_bg);
        iconBg.setVisibility(View.VISIBLE);
        iconBg.setImageResource(R.drawable.ic_maintenance);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView no = dialog.findViewById(R.id.tv_dialog_no);
        no.setText(R.string.cancel);
        no.setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView yes = dialog.findViewById(R.id.tv_dialog_yes);
        yes.setVisibility(View.GONE);

        View view = dialog.findViewById(R.id.vw_dialog_bar);
        view.setVisibility(View.GONE);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void upgradeDialog(Activity activity, CancelListener listener) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_error);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.upgrade);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.its_time_to_upgrade);

        // VISIBLE
        TextView titleSub = dialog.findViewById(R.id.tv_dialog_title_sub);
        titleSub.setVisibility(View.VISIBLE);
        titleSub.setText(R.string.upgrade);

        ImageView iconBg = dialog.findViewById(R.id.iv_dialog_icon_bg);
        iconBg.setVisibility(View.VISIBLE);
        iconBg.setImageResource(R.drawable.ic_upgrade_svg);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });

        TextView no = dialog.findViewById(R.id.tv_dialog_no);
        no.setText(R.string.cancel);
        no.setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });

        TextView yes = dialog.findViewById(R.id.tv_dialog_yes);
        yes.setText(R.string.do_it_now);
        yes.setOnClickListener(view -> {
            dialog.dismiss();
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Callback.getAppRedirectUrl())));
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void dModeDialog(Activity activity) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_error);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.developer_mode);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.turn_off_developer_mode);

        // VISIBLE
        TextView titleSub = dialog.findViewById(R.id.tv_dialog_title_sub);
        titleSub.setVisibility(View.VISIBLE);
        titleSub.setText(R.string.developer_mode);

        ImageView iconBg = dialog.findViewById(R.id.iv_dialog_icon_bg);
        iconBg.setVisibility(View.VISIBLE);
        iconBg.setImageResource(R.drawable.ic_coding_development);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView no = dialog.findViewById(R.id.tv_dialog_no);
        no.setText(R.string.try_again_later);
        no.setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView yes = dialog.findViewById(R.id.tv_dialog_yes);
        yes.setVisibility(View.GONE);

        View view = dialog.findViewById(R.id.vw_dialog_bar);
        view.setVisibility(View.GONE);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void vpnDialog(Activity activity) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_error);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.sniffing_detected);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.turn_off_all_sniffers_tools);

        // VISIBLE
        TextView titleSub = dialog.findViewById(R.id.tv_dialog_title_sub);
        titleSub.setVisibility(View.VISIBLE);
        titleSub.setText(R.string.sniffing_detected);

        ImageView iconBg = dialog.findViewById(R.id.iv_dialog_icon_bg);
        iconBg.setVisibility(View.VISIBLE);
        iconBg.setImageResource(R.drawable.ic_vpn_network);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView no = dialog.findViewById(R.id.tv_dialog_no);
        no.setText(R.string.cancel);
        no.setOnClickListener(view -> {
            dialog.dismiss();
            activity.finish();
        });

        TextView yes = dialog.findViewById(R.id.tv_dialog_yes);
        yes.setVisibility(View.GONE);

        View view = dialog.findViewById(R.id.vw_dialog_bar);
        view.setVisibility(View.GONE);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void trashDialog(Activity activity, DeleteListener listener) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_trash);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(R.string.delete);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(R.string.sure_delete);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });

        dialog.findViewById(R.id.tv_dialog_no).setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });
        dialog.findViewById(R.id.tv_dialog_yes).setOnClickListener(view -> {
            dialog.dismiss();
            listener.onDelete();
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void verifyDialog(Activity activity, String titleData, String message, CancelListener listener) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app);

        ImageView icon = dialog.findViewById(R.id.iv_dialog_icon);
        icon.setImageResource(R.drawable.ic_notification);

        TextView title = dialog.findViewById(R.id.tv_dialog_title);
        title.setText(titleData);

        TextView msg = dialog.findViewById(R.id.tv_dialog_msg);
        msg.setText(message);

        // VISIBLE
        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });

        TextView no = dialog.findViewById(R.id.tv_dialog_no);
        no.setText(R.string.ok);
        no.setOnClickListener(view -> {
            dialog.dismiss();
            listener.onCancel();
        });

        TextView yes = dialog.findViewById(R.id.tv_dialog_yes);
        yes.setVisibility(View.GONE);

        View view = dialog.findViewById(R.id.vw_dialog_bar);
        view.setVisibility(View.GONE);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void premiumDialog(Activity activity) {
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_premium);
        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_dialog_no).setOnClickListener(view -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, BillingSubscribeActivity.class));
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void timerStartDialog(Activity activity, TimerStartListener listener) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_pickers);

        NumberPicker hoursPicker = dialog.findViewById(R.id.hours_picker);
        NumberPicker minutePicker = dialog.findViewById(R.id.minute_picker);

        dialog.findViewById(R.id.iv_close_timer).setOnClickListener(view ->  dialog.dismiss());
        dialog.findViewById(R.id.tv_cancel_timer).setOnClickListener(view ->  dialog.dismiss());
        dialog.findViewById(R.id.tv_start_timer).setOnClickListener(view -> {
            String hours = String.valueOf(hoursPicker.getValue());
            String minute = String.valueOf(minutePicker.getValue());
            listener.onStart(hours, minute);
            dialog.dismiss();
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    public static void timerCancelDialog(Activity activity, TimerCancelListener listener) {
        if (activity == null || activity.isFinishing()){
            return;
        }
        if (dialog != null){
            dialog = null;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_cancel);
        dialog.findViewById(R.id.iv_close_timer).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_cancel_timer).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_stop_timer).setOnClickListener(view -> {
            listener.onStopped();
            dialog.dismiss();
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    // Listener
    public interface CancelListener {
        void onCancel();
    }

    public interface DeleteListener {
        void onDelete();
        void onCancel();
    }

    public interface TimerStartListener {
        void onStart(String hours, String minute);
    }

    public interface TimerCancelListener {
        void onStopped();
    }
}
