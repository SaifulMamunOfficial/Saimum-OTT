package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.executor.LoadStatus;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.item.ItemNotify;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;


public class AdapterNotify extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ItemNotify> arrayList;
    public final Context ctx;
    private final Helper helper;
    private final SPHelper spHelper;
    private static final int VIEW_ITEM = 1;

    public AdapterNotify(Context ctx, List<ItemNotify> arrayList) {
        this.arrayList = arrayList;
        this.ctx = ctx;
        helper = new Helper(ctx);
        spHelper = new SPHelper(ctx);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView comment;
        private final TextView date;
        private final RelativeLayout relativeLayout;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_not_title);
            comment = view.findViewById(R.id.tv_not_note);
            date = view.findViewById(R.id.tv_not_date);
            relativeLayout = view.findViewById(R.id.rl_not_close);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("StaticFieldLeak")
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_notification, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder myViewHolder) {

            myViewHolder.title.setText(arrayList.get(position).getName());
            myViewHolder.comment.setText(arrayList.get(position).getNot());
            myViewHolder.date.setText(arrayList.get(position).getDate());

            myViewHolder.relativeLayout.setOnClickListener(view -> {
                if (position <= arrayList.size()) {
                    loadRemove(holder.getAbsoluteAdapterPosition());
                }
            });

        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void loadRemove(int pos) {
        if (!NetworkUtils.isConnected(ctx)) {
            Toast.makeText(ctx, ctx.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(ctx);
        LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String status, String message) {
                progressDialog.dismiss();
                if (success.equals("1")) {
                    if (status.equals("1")) {
                        arrayList.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        }, helper.getAPIRequest(Method.METHOD_REMOVE_NOTIFICATION, 0,
                arrayList.get(pos).getId(),"","","",
                spHelper.getUserId(),"","","","","",
                "","",null));
        loadStatus.execute();
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isProgressPos(position) ? 0 : VIEW_ITEM;
    }

    private boolean isProgressPos(int position) {
        return position == arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}