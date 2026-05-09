package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.item.ItemRating;


public class AdapterRating extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ItemRating> arrayList;
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_PROG = 0;

    public AdapterRating(List<ItemRating> arrayList) {
        this.arrayList = arrayList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView comment;
        ImageHelperView photo;
        RatingBar ratingBar;

        MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.tv_comment_user_name);
            comment = view.findViewById(R.id.tv_comment_user);
            photo = view.findViewById(R.id.iv_comment_photo);
            ratingBar = view.findViewById(R.id.rb_comment_ratingBar);
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
                    .inflate(R.layout.row_comment_rating, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder myViewHolder) {

            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName().isEmpty()){
                myViewHolder.userName.setText("none");
            } else {
                myViewHolder.userName.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getUserName());
            }

            myViewHolder.comment.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getMessage());
            myViewHolder.ratingBar.setRating(Float.parseFloat(arrayList.get(holder.getAbsoluteAdapterPosition()).getRate()));

            Picasso.get()
                    .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getDp().isEmpty() ? "null"
                            : arrayList.get(holder.getAbsoluteAdapterPosition()).getDp())
                    .placeholder(R.drawable.user_photo)
                    .error(R.drawable.user_photo)
                    .into(myViewHolder.photo);
        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
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
        return isProgressPos(position) ? VIEW_PROG : VIEW_ITEM;
    }

    private boolean isProgressPos(int position) {
        return position == arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}