package nemosofts.tamilaudiopro.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.material.EqualizerView;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;

public class AdapterQueueSongList extends RecyclerView.Adapter<AdapterQueueSongList.MyViewHolder> {

    private final Context context;
    private final List<ItemSong> arrayList;
    private final RecyclerItemClickListener listener;
    boolean isDarkMode;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout relativeLayout;
        private final ImageHelperView poster;
        private final EqualizerView equalizer;
        private final TextView audioTitle;
        private final TextView catName;
        private final ImageView option;
        private final ImageView audioListPlay;
        private final ImageView move;

        MyViewHolder(View view) {
            super(view);
            relativeLayout = view.findViewById(R.id.rl_audio_list);
            poster = view.findViewById(R.id.iv_audio_list);
            equalizer = view.findViewById(R.id.equalizer_audio_list);
            audioTitle = view.findViewById(R.id.tv_audio_list_name);
            catName = view.findViewById(R.id.tv_audio_list_cat);
            audioListPlay = view.findViewById(R.id.iv_audio_list_play);
            option = view.findViewById(R.id.iv_list_option);
            move = view.findViewById(R.id.iv_list_move);
        }
    }

    public AdapterQueueSongList(Context context,
                                List<ItemSong> arrayList,
                                RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
        isDarkMode = new ThemeEngine(context).getIsThemeMode();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_playing_queue, parent, false);
        return new MyViewHolder(itemView);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ItemSong item = arrayList.get(position);

        holder.audioTitle.setText(item.getTitle());

        Picasso.get()
                .load(item.getImageBig())
                .resize(200,200)
                .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                .error(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                .into(holder.poster);
        holder.move.setOnClickListener(view -> {
        });

        if (Callback.getPlayPos() <= holder.getAbsoluteAdapterPosition()
                && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(position).getId())) {
            holder.option.setVisibility(View.INVISIBLE);
        } else {
            holder.option.setVisibility(View.VISIBLE);
        }

        if (PlayerService.getIsPlayling()
                && Callback.getPlayPos() <= holder.getAbsoluteAdapterPosition()
                && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(position).getId())) {
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();
            holder.audioListPlay.setVisibility(View.GONE);

            holder.relativeLayout.setBackgroundColor(ColorUtils.colorPrimarySub(context));
            holder.audioTitle.setTextColor(ColorUtils.colorPrimary(context));

        } else {
            holder.equalizer.setVisibility(View.GONE);
            holder.equalizer.stopBars();
            holder.audioListPlay.setVisibility(View.VISIBLE);

            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.audioTitle.setTextColor(ColorUtils.colorTitle(context));
        }

        String artistName="";
        if (item.getArtist() != null) {
            artistName = item.getArtist();
        }
        holder.catName.setText(artistName);

        holder.option.setOnClickListener(view -> {
            try {
                Callback.getArrayListPlay().remove(arrayList.get(holder.getAbsoluteAdapterPosition()));
                GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            } catch (Exception e) {
                Log.e("AdapterQueueSongList","onBindViewHolder", e);
            }
        });

        holder.relativeLayout.setOnClickListener(view ->  listener.onClickListener(item, holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemSong itemSong, int position);
    }
}