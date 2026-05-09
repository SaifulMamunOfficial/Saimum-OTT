package nemosofts.tamilaudiopro.adapter.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.material.EqualizerView;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;

public class AdapterHomeRecent extends RecyclerView.Adapter<AdapterHomeRecent.MyViewHolder> {

    Context context;
    List<ItemSong> arrayList;
    ClickListenerPlayList clickListenerPlayList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final ImageView btnPlay;
        private final EqualizerView equalizer;
        private final RelativeLayout relativeLayout;
        private final ImageHelperView poster;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_recent_title);
            btnPlay = view.findViewById(R.id.iv_play_view);
            equalizer = view.findViewById(R.id.equalizer_recent);
            relativeLayout = view.findViewById(R.id.rl_recent);
            poster = view.findViewById(R.id.iv_recently);
        }
    }

    public AdapterHomeRecent(Context context,
                             List<ItemSong> arrayList,
                             ClickListenerPlayList clickListenerPlayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recently, parent, false);
        return new MyViewHolder(itemView);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.title.setText(arrayList.get(position).getTitle());

        Picasso.get()
                .load(arrayList.get(position).getImageBig())
                .resize(300,300)
                .placeholder(R.drawable.placeholder_song_light)
                .error(R.drawable.placeholder_song_light)
                .into(holder.poster);

        if (PlayerService.getIsPlayling()
                && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(position).getId())) {
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();
            holder.btnPlay.setImageResource(R.drawable.ic_pause);
        } else {
            holder.equalizer.setVisibility(View.GONE);
            holder.equalizer.stopBars();
            holder.btnPlay.setImageResource(R.drawable.ic_play);
        }

        holder.relativeLayout.setOnClickListener(view ->  clickListenerPlayList.onClick(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}