package nemosofts.tamilaudiopro.adapter;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.material.EqualizerView;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickDeleteListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class AdapterOFSongList extends RecyclerView.Adapter<AdapterOFSongList.MyViewHolder> {

    private final Context context;
    private List<ItemSong> arrayList;
    private final List<ItemSong> filteredArrayList;
    private final ClickDeleteListenerPlayList clickDeleteListenerPlayList;
    private NameFilter filter;
    private final String type;
    private final Helper helper;
    private final DBHelper dbHelper;
    int posi = 0;
    private static final int DELETE_REQUEST_URI_R = 11;
    private static final int DELETE_REQUEST_URI_Q = 12;
    private static final String TAG_DOWNLOAD = "downloads";
    boolean isDarkMode;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView artist;
        private final EqualizerView equalizer;
        private final ImageView imageView;
        private final ImageView option;
        private final ImageView playOff;
        private final RelativeLayout relativeLayout;

        MyViewHolder(View view) {
            super(view);
            relativeLayout = view.findViewById(R.id.ll_off_songlist);
            title = view.findViewById(R.id.tv_off_songlist_name);
            equalizer = view.findViewById(R.id.equalizer_view_off);
            artist = view.findViewById(R.id.tv_off_songlist_art);
            imageView = view.findViewById(R.id.iv_off_songlist);
            option = view.findViewById(R.id.iv_off_songlist_option);
            playOff = view.findViewById(R.id.iv_audio_play_off);
        }
    }

    public AdapterOFSongList(Context context,
                             List<ItemSong> arrayList,
                             ClickDeleteListenerPlayList clickDeleteListenerPlayList,
                             String type) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.type = type;
        this.clickDeleteListenerPlayList = clickDeleteListenerPlayList;
        helper = new Helper(context);
        dbHelper = new DBHelper(context);
        isDarkMode = new ThemeEngine(context).getIsThemeMode();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offline_songs, parent, false);
        return new MyViewHolder(itemView);
    }

    @OptIn(markerClass = UnstableApi.class)
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.title.setText(arrayList.get(position).getTitle());

        Picasso.get()
                .load(Uri.parse(arrayList.get(holder.getAbsoluteAdapterPosition()).getImageBig()))
                .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                .error(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                .into(holder.imageView);

        if (!Callback.getIsOnline() && PlayerService.getIsPlayling()
                && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(position).getId())) {
            holder.playOff.setVisibility(View.INVISIBLE);
            holder.equalizer.setVisibility(View.VISIBLE);
            holder.equalizer.animateBars();

            holder.relativeLayout.setBackgroundColor(ColorUtils.colorPrimarySub(context));
            holder.title.setTextColor(ColorUtils.colorPrimary(context));

        } else {
            holder.playOff.setVisibility(View.VISIBLE);
            holder.equalizer.setVisibility(View.GONE);
            holder.equalizer.stopBars();
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.title.setTextColor(ColorUtils.colorTitle(context));
        }

        holder.artist.setText(arrayList.get(position).getArtist());

        holder.relativeLayout.setOnClickListener(view -> {
            Callback.setIsDownloaded(type.equals(TAG_DOWNLOAD));
            clickDeleteListenerPlayList.onClick(getPosition(arrayList.get(holder.getAbsoluteAdapterPosition()).getId()));
        });

        holder.option.setOnClickListener(view -> openBottomSheet(holder.getAbsoluteAdapterPosition()));
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

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < filteredArrayList.size(); i++) {
            if (id.equals(filteredArrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openBottomSheet(int pos) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.bottom_sheet_audio_off, null);
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        TextView title = dialog.findViewById(R.id.tv_sheet_off_text);
        TextView catTitle = dialog.findViewById(R.id.tv_sheet_off_list_cat);
        ImageHelperView poster = dialog.findViewById(R.id.iv_sheet_off_post);

        LinearLayout addSong = dialog.findViewById(R.id.ll_sheet_off_add_song);
        LinearLayout addQueue = dialog.findViewById(R.id.ll_sheet_off_add_queue);
        LinearLayout delete = dialog.findViewById(R.id.ll_sheet_off_delete);
        LinearLayout youtube = dialog.findViewById(R.id.ll_sheet_off_youtube);
        LinearLayout share = dialog.findViewById(R.id.ll_sheet_off_share);

        TextView addSongTitle = dialog.findViewById(R.id.tv_off_add_song);

        if (poster != null){
            Picasso.get()
                    .load(arrayList.get(pos).getImageBig())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                    .into(poster);
        }

        Objects.requireNonNull(catTitle).setText(arrayList.get(pos).getArtist());
        Objects.requireNonNull(title).setText(arrayList.get(pos).getTitle());

        if (type.equals(TAG_DOWNLOAD)) {
            Objects.requireNonNull(addSong).setVisibility(View.GONE);
            Objects.requireNonNull(addQueue).setVisibility(View.GONE);
        }
        if (type.equals(context.getString(R.string.playlist))) {
            Objects.requireNonNull(addSongTitle).setText(context.getString(R.string.remove));
            Objects.requireNonNull(delete).setVisibility(View.GONE);
        }
        if (Callback.getIsOnline() || Callback.getIsDownloaded()) {
            Objects.requireNonNull(addQueue).setVisibility(View.GONE);
        }
        if (!helper.isYoutubeAppInstalled()) {
            Objects.requireNonNull(youtube).setVisibility(View.GONE);
        }

        Objects.requireNonNull(addSong).setOnClickListener(view10 -> {
            if (type.equals(context.getString(R.string.playlist))) {
                dbHelper.removeFromPlayList(arrayList.get(pos).getId(), false);
                arrayList.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, context.getString(R.string.remove_from_playlist), Toast.LENGTH_SHORT).show();
                if (arrayList.isEmpty()) {
                    clickDeleteListenerPlayList.onItemZero();
                }
            } else {
                helper.openPlaylists(arrayList.get(pos), false);
            }
            dialog.dismiss();
        });
        Objects.requireNonNull(addQueue).setOnClickListener(view11 -> {
            Callback.getArrayListPlay().add(arrayList.get(pos));
            PlayerService.getInstance().addMediaSource(Uri.parse(arrayList.get(pos).getUrl()));
            GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
            dialog.dismiss();
        });
        Objects.requireNonNull(delete).setOnClickListener(view12 -> {
            openDeleteDialog(pos);
            dialog.dismiss();
        });
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

    private void openDeleteDialog(final int pos) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.dialogTheme);
        dialog.setTitle(context.getString(R.string.delete));
        dialog.setMessage(context.getString(R.string.sure_delete));
        dialog.setPositiveButton(context.getString(R.string.delete), (dialogInterface, i) -> {
            posi = pos;
            if (type.equals(TAG_DOWNLOAD)) {
                final File file = new File(arrayList.get(pos).getUrl());
                final File fileImage = new File(arrayList.get(pos).getImageBig());
                if (file.exists()) {
                    dbHelper.removeFromDownload(arrayList.get(pos).getId());
                    file.delete();
                    fileImage.delete();
                    arrayList.remove(pos);
                    notifyItemRemoved(pos);
                }
                Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int delete = context.getContentResolver().delete(Uri.parse(arrayList.get(pos).getUrl()), null, null);
                    if (delete == 1) {
                        arrayList.remove(pos);
                        notifyItemRemoved(pos);
                        Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_file_delete), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception exception) {
                    clickDeleteListenerPlayList.onDelete(posi, exception, DELETE_REQUEST_URI_R, DELETE_REQUEST_URI_Q);
                }
            }
        });
        dialog.setNegativeButton(context.getString(R.string.cancel), (dialogInterface, i) -> {
        });
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DELETE_REQUEST_URI_R) {
            if (resultCode == RESULT_OK) {
                arrayList.remove(posi);
                notifyItemRemoved(posi);
                Toast.makeText(context, context.getResources().getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == DELETE_REQUEST_URI_Q && (resultCode == RESULT_OK)) {
            int delete = context.getContentResolver().delete(Uri.parse(arrayList.get(posi).getUrl()), null, null);
            if (delete == 1) {
                arrayList.remove(posi);
                notifyItemRemoved(posi);
                Toast.makeText(context, context.getResources().getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.error_file_delete), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (!constraint.toString().isEmpty()) {
                ArrayList<ItemSong> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getTitle();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList = (ArrayList<ItemSong>) results.values;
            notifyDataSetChanged();
        }
    }

    public void closeDatabase () {
        try {
            dbHelper.close();
        } catch (Exception e){
            Log.e("AdapterOFSongList","closeDatabase", e);
        }
    }
}