package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
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
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;

public class AdapterDBPlaylist extends RecyclerView.Adapter<AdapterDBPlaylist.MyViewHolder> {

    private final DBHelper dbHelper;
    private final Context context;
    private List<ItemMyPlayList> arrayList;
    private final List<ItemMyPlayList> filteredArrayList;
    private NameFilter filter;
    private final ClickListenerPlayList clickListenerPlayList;
    private final int columnWidth;
    private final Boolean isOnline;
    boolean isDarkMode;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;
        private final ImageView more;
        private final ImageView imageView1;
        private final ImageView imageView2;
        private final ImageView imageView3;
        private final ImageView imageView4;
        private final RelativeLayout rl;

        MyViewHolder(View view) {
            super(view);
            rl = view.findViewById(R.id.rl_my_playlist);
            textView = view.findViewById(R.id.tv_my_playlist);
            more = view.findViewById(R.id.iv_more_my_playlist);
            imageView1 = view.findViewById(R.id.iv_my_playlist1);
            imageView2 = view.findViewById(R.id.iv_my_playlist2);
            imageView3 = view.findViewById(R.id.iv_my_playlist3);
            imageView4 = view.findViewById(R.id.iv_my_playlist4);
        }
    }

    public AdapterDBPlaylist(Context context,
                             List<ItemMyPlayList> arrayList,
                             ClickListenerPlayList clickListenerPlayList,
                             Boolean isOnline) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.isOnline = isOnline;
        this.clickListenerPlayList = clickListenerPlayList;
        dbHelper = new DBHelper(context);
        columnWidth = DeviceUtils.getColumnWidth(context, 2, 5);
        isDarkMode = new ThemeEngine(context).getIsThemeMode();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_my_playlist, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.textView.setText(arrayList.get(position).getName());

        int placeholder = isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light;

        if(Boolean.TRUE.equals(isOnline)) {
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(3))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView1);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(2))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView2);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(1))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView3);
            Picasso.get()
                    .load(arrayList.get(position).getArrayListUrl().get(0))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView4);
        } else {
            Picasso.get()
                    .load(Uri.parse(arrayList.get(position).getArrayListUrl().get(3)))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView1);
            Picasso.get()
                    .load(Uri.parse(arrayList.get(position).getArrayListUrl().get(2)))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView2);
            Picasso.get()
                    .load(Uri.parse(arrayList.get(position).getArrayListUrl().get(1)))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView3);
            Picasso.get()
                    .load(Uri.parse(arrayList.get(position).getArrayListUrl().get(0)))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.imageView4);
        }

        holder.rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, columnWidth));
        holder.rl.setOnClickListener(view -> clickListenerPlayList.onClick(holder.getAbsoluteAdapterPosition()));
        holder.more.setOnClickListener(view -> openOptionPopUp(holder.more, holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public ItemMyPlayList getItem(int pos) {
        return arrayList.get(pos);
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
                ArrayList<ItemMyPlayList> filteredItems = new ArrayList<>();
                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getName();
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
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            arrayList = (ArrayList<ItemMyPlayList>) results.values;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void openOptionPopUp(ImageView imageView, final int pos) {
        ContextThemeWrapper ctw = new ContextThemeWrapper(context, isDarkMode ? R.style.PopupMenuDark : R.style.PopupMenuLight);
        PopupMenu popup = new PopupMenu(ctw, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_playlist, popup.getMenu());
        popup.setForceShowIcon(true);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.popup_option_playlist) {
                dbHelper.removePlayList(arrayList.get(pos).getId(), isOnline);
                arrayList.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, context.getString(R.string.remove_playlist), Toast.LENGTH_SHORT).show();
                if (arrayList.isEmpty()) {
                    clickListenerPlayList.onItemZero();
                }
            }
            return true;
        });
        popup.show();
    }

    public void closeDatabase () {
        try {
            dbHelper.close();
        } catch (Exception e){
            Log.e("AdapterDBPlaylist","closeDatabase", e);
        }
    }
}