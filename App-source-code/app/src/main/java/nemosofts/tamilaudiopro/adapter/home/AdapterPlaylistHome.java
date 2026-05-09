package nemosofts.tamilaudiopro.adapter.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;

public class AdapterPlaylistHome extends RecyclerView.Adapter<AdapterPlaylistHome.MyViewHolder> {

    private final List<ItemServerPlayList> arrayList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageHelperView poster;
        private final TextView title;

        MyViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.iv_home_playlist);
            title = view.findViewById(R.id.tv_home_playlist);
        }
    }

    public AdapterPlaylistHome(List<ItemServerPlayList> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_playlist_home, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(arrayList.get(position).getName());
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .resize(300,300)
                .placeholder(R.drawable.material_design_default)
                .error(R.drawable.material_design_default)
                .into(holder.poster);
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