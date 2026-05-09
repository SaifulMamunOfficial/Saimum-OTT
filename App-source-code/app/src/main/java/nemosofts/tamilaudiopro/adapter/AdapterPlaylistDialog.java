package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;

public class AdapterPlaylistDialog extends RecyclerView.Adapter<AdapterPlaylistDialog.MyViewHolder> {

    private List<ItemMyPlayList> arrayList;
    private final List<ItemMyPlayList> filteredArrayList;
    private NameFilter filter;
    private final ClickListenerPlayList clickListenerPlayList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textView_playlist_dialog);
        }
    }

    public AdapterPlaylistDialog(List<ItemMyPlayList> arrayList,
                                 ClickListenerPlayList clickListenerPlayList) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_playlist_dialog, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.title.setText(arrayList.get(position).getName());
        holder.title.setOnClickListener(view -> clickListenerPlayList.onClick(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @NonNull
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
        protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
            arrayList = (ArrayList<ItemMyPlayList>) results.values;
            notifyDataSetChanged();
        }
    }
}