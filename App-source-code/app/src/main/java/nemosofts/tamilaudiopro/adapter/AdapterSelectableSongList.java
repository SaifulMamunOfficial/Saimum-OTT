package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;

public class AdapterSelectableSongList extends RecyclerView.Adapter<AdapterSelectableSongList.MyViewHolder> {

    private final List<ItemSong> arrayList;
    private final List<String> arrayListSelectedIDs = new ArrayList<>();
    private int selectedCounter = 0;
    private final ClickListenerPlayList recyclerClickListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView songTitle;
        private final TextView catName;
        private final ImageView imageView;
        private final CheckBox checkBox;
        private final RelativeLayout relativeLayout;

        MyViewHolder(View view) {
            super(view);
            relativeLayout = view.findViewById(R.id.ll_select);
            songTitle = view.findViewById(R.id.tv_songlist_name);
            catName = view.findViewById(R.id.tv_songlist_cat);
            checkBox = view.findViewById(R.id.cb_select);
            imageView = view.findViewById(R.id.iv_songlist);
        }
    }

    public AdapterSelectableSongList(List<ItemSong> arrayList,
                                     ClickListenerPlayList recyclerClickListener) {
        this.arrayList = arrayList;
        this.recyclerClickListener = recyclerClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_selectable_songs, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.songTitle.setText(arrayList.get(position).getTitle());
        Picasso.get()
                .load(Uri.parse(arrayList.get(position).getImageBig()))
                .placeholder(R.drawable.placeholder_folder_night)
                .error(R.drawable.placeholder_folder_night)
                .into(holder.imageView);

        holder.catName.setText(arrayList.get(position).getArtist());

        holder.checkBox.setChecked(arrayListSelectedIDs.contains(arrayList.get(holder.getAbsoluteAdapterPosition()).getId()));

        holder.relativeLayout.setOnClickListener(view -> {
            if(arrayListSelectedIDs.contains(arrayList.get(holder.getAbsoluteAdapterPosition()).getId())) {
                selectedCounter = selectedCounter - 1;
                arrayListSelectedIDs.remove(arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
            } else {
                selectedCounter = selectedCounter + 1;
                arrayListSelectedIDs.add(arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
            }
            notifyItemChanged(holder.getAbsoluteAdapterPosition());
            recyclerClickListener.onClick(0);
        });

        holder.checkBox.setOnClickListener(v -> {
            if(arrayListSelectedIDs.contains(arrayList.get(holder.getAbsoluteAdapterPosition()).getId())) {
                selectedCounter = selectedCounter - 1;
                arrayListSelectedIDs.remove(arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
            } else {
                selectedCounter = selectedCounter + 1;
                arrayListSelectedIDs.add(arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
            }
            recyclerClickListener.onClick(0);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void toggleSelectAll(Boolean isSelect) {
        if(Boolean.TRUE.equals(isSelect)) {
            selectedCounter = arrayList.size();
            for (int i = 0; i < arrayList.size(); i++) {
                arrayListSelectedIDs.add(arrayList.get(i).getId());
            }
        } else {
            arrayListSelectedIDs.clear();
            selectedCounter = 0;
        }
        notifyDataSetChanged();
    }

    public int getSelectedCounts() {
        return selectedCounter;
    }

    public List<ItemSong> getSelectedIDs() {
        List<ItemSong> arrayListSelected = new ArrayList<>();
        for (int i = 0; i < arrayListSelectedIDs.size(); i++) {
            for (int j = 0; j < arrayList.size(); j++) {
                if(arrayListSelectedIDs.get(i).equals(arrayList.get(j).getId())) {
                    arrayListSelected.add(arrayList.get(j));
                }
            }
        }
        return arrayListSelected;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public ItemSong getItem(int pos) {
        return arrayList.get(pos);
    }
}