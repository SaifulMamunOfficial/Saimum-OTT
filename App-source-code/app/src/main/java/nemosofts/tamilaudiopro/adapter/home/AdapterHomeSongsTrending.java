package nemosofts.tamilaudiopro.adapter.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.material.EqualizerView;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadService;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.interfaces.RewardAdListener;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class AdapterHomeSongsTrending extends RecyclerView.Adapter<AdapterHomeSongsTrending.MyViewHolder> {

    private final Helper helper;
    private final SPHelper spHelper;
    private final Context context;
    private final List<ItemSong> arrayList;
    private final ClickListenerPlayList clickListenerPlayList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageHelperView poster;
        private final ImageView more;
        private final ImageView playView;
        private final ImageView playView2;
        private final ImageView trending;
        private final TextView title;
        private final TextView catTitle;
        private final EqualizerView equalizer;

        MyViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.iv_recent);
            more = view.findViewById(R.id.iv_recent_more);
            title = view.findViewById(R.id.tv_recent_song);
            catTitle = view.findViewById(R.id.tv_recent_cat);
            equalizer = view.findViewById(R.id.equalizer_view);
            playView = view.findViewById(R.id.iv_play_view);
            playView2 = view.findViewById(R.id.iv_play_view_2);
            trending = view.findViewById(R.id.tv_trending);
        }
    }

    public AdapterHomeSongsTrending(Context context,
                                    List<ItemSong> arrayList,
                                    ClickListenerPlayList clickListenerPlayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
        helper = new Helper(context);
        spHelper = new SPHelper(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_audio, parent, false);
        return new MyViewHolder(itemView);
    }

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.title.setText(arrayList.get(position).getTitle());

        int step = 1;
        for (int i = 1; i < position + 1; i++) {
            step++;
        }

        if (step > 15){
            holder.trending.setVisibility(View.GONE);
        } else {
            holder.trending.setVisibility(View.VISIBLE);
        }

        holder.catTitle.setText("#"+ step);

        Picasso.get()
                .load(arrayList.get(position).getImageBig())
                .resize(300,300)
                .placeholder(R.drawable.placeholder_song_light)
                .error(R.drawable.placeholder_song_light)
                .into(holder.poster);

        if (Callback.getIsOnline() && PlayerService.getIsPlayling()
                && Callback.getPlayPos() <= holder.getAbsoluteAdapterPosition()
                && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(position).getId())) {
            holder.playView.setVisibility(View.GONE);
            holder.playView2.setVisibility(View.GONE);
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();
        } else {
            holder.playView.setVisibility(View.VISIBLE);
            holder.playView2.setVisibility(View.VISIBLE);
            holder.equalizer.setVisibility(View.GONE);
            holder.equalizer.stopBars();
        }

        holder.more.setOnClickListener(v -> openBottomSheet(holder.getAbsoluteAdapterPosition()));
        holder.poster.setOnClickListener(v -> clickListenerPlayList.onClick(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void openBottomSheet(int pos) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.bottom_sheet_audio, null);
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        TextView title = dialog.findViewById(R.id.tv_sheet_text);
        TextView catTitle = dialog.findViewById(R.id.tv_sheet_list_cat);
        ImageHelperView poster = dialog.findViewById(R.id.iv_sheet_post);

        LinearLayout share = dialog.findViewById(R.id.ll_sheet_share);
        LinearLayout youtube = dialog.findViewById(R.id.ll_sheet_youtube);
        LinearLayout download = dialog.findViewById(R.id.ll_sheet_download);
        LinearLayout addQueue = dialog.findViewById(R.id.ll_sheet_add_queue);
        LinearLayout addSong = dialog.findViewById(R.id.ll_sheet_add_song);

        if (poster != null){
            Picasso.get()
                    .load(arrayList.get(pos).getImageBig())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .error(R.drawable.material_design_default)
                    .into(poster);
        }

        Objects.requireNonNull(catTitle).setText(arrayList.get(pos).getArtist());
        Objects.requireNonNull(title).setText(arrayList.get(pos).getTitle());

        if (Boolean.FALSE.equals(Callback.getIsOnline())) {
            Objects.requireNonNull(addQueue).setVisibility(View.GONE);
        }
        if (Boolean.FALSE.equals(spHelper.getIsSongDownload())) {
            Objects.requireNonNull(download).setVisibility(View.GONE);
        }
        if (!helper.isYoutubeAppInstalled()) {
            Objects.requireNonNull(youtube).setVisibility(View.GONE);
        }

        Objects.requireNonNull(addSong).setOnClickListener(view10 -> {
            helper.openPlaylists(arrayList.get(pos), true);
            dialog.dismiss();
        });
        Objects.requireNonNull(addQueue).setOnClickListener(view11 -> {
            Callback.getArrayListPlay().add(arrayList.get(pos));
            GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
            Toast.makeText(context, context.getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        Objects.requireNonNull(download).setOnClickListener(view12 -> handleDownloadClick(dialog, pos));
        Objects.requireNonNull(youtube).setOnClickListener(view6 -> {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", arrayList.get(pos).getTitle());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            dialog.dismiss();
        });
        Objects.requireNonNull(share).setOnClickListener(view3 -> helper.shareSong(arrayList.get(pos), true));
        dialog.show();
    }

    private void handleDownloadClick(BottomSheetDialog dialog, int pos) {
        if (DownloadService.count >= 0 && DownloadService.count < 5) {
            try{
                if (spHelper.getRewardCredit() != 0){
                    spHelper.useRewardCredit(1);
                    Toast.makeText(context, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                    helper.download(arrayList.get(pos));
                    arrayList.get(pos).setDownload(true);
                    notifyItemChanged(pos);
                } else {
                    helper.showRewardAds(pos, new RewardAdListener() {
                        @Override
                        public void onClick(boolean isLoad, int pos2) {
                            if (isLoad){
                                spHelper.addRewardCredit(Callback.getRewardCredit());
                                spHelper.useRewardCredit(1);
                                Toast.makeText(context, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                                helper.download(arrayList.get(pos2));
                                arrayList.get(pos2).setDownload(true);
                                notifyItemChanged(pos2);
                            } else {
                                Toast.makeText(context, "Display Failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPurchases(int pos) {
                            helper.download(arrayList.get(pos));
                            arrayList.get(pos).setDownload(true);
                            notifyItemChanged(pos);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("AdapterHomeSongsTrending", "Error handleDownloadClick", e);
            }
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.please_wait_a_minutes), Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
}