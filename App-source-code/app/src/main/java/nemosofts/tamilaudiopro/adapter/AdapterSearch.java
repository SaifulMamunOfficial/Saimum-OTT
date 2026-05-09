package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByIDActivity;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.adapter.home.AdapterHomeAlbums;
import nemosofts.tamilaudiopro.adapter.home.AdapterHomeArtist;
import nemosofts.tamilaudiopro.adapter.home.AdapterHomeCat;
import nemosofts.tamilaudiopro.adapter.home.AdapterHomeSongs;
import nemosofts.tamilaudiopro.adapter.home.AdapterPlaylistHome;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchAlbums;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchArtist;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchCategories;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchPlaylist;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchSong;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.interfaces.InterAdListener;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class AdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Helper helper;
    List<ItemPost> arrayList;
    int clickPos = 0;

    public static final int VIEW_PROG = 0;
    public static final int VIEW_SONGS = 2;
    public static final int VIEW_ARTIST = 3;
    public static final int VIEW_ALBUMS = 4;
    public static final int VIEW_CATEGORIES = 5;
    public static final int VIEW_PLAYLIST = 6;

    public AdapterSearch(Context context, List<ItemPost> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        helper = new Helper(context, interAdListener);
    }

    class CategoriesHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterHomeCat adapter;
        TextView title;
        LinearLayout viewAll;

        CategoriesHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class ArtistHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterHomeArtist adapter;
        TextView title;
        LinearLayout viewAll;

        ArtistHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class AlbumsHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterHomeAlbums adapter;
        TextView title;
        LinearLayout viewAll;

        AlbumsHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class PlaylistHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterPlaylistHome adapter;
        TextView title;
        LinearLayout viewAll;

        PlaylistHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class SongsHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        AdapterHomeSongs adapter;
        TextView title;
        LinearLayout viewAll;

        SongsHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(manager);
            rv.setItemAnimator(new DefaultItemAnimator());
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
        if (viewType == VIEW_CATEGORIES) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new CategoriesHolder(itemView);
        } else if (viewType == VIEW_ARTIST) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new ArtistHolder(itemView);
        } else if (viewType == VIEW_ALBUMS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new AlbumsHolder(itemView);
        } else if (viewType == VIEW_PLAYLIST) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new PlaylistHolder(itemView);
        } else if (viewType == VIEW_SONGS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new SongsHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CategoriesHolder categories) {

            categories.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            categories.adapter = new AdapterHomeCat(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListCategories());
            categories.rv.setAdapter(categories.adapter);

            categories.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.categories));
            }));

            categories.viewAll.setOnClickListener(v -> {
                FragmentSearchCategories albums = new FragmentSearchCategories();
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, albums, context.getString(R.string.categories));
                ft.addToBackStack(context.getString(R.string.categories));
                ft.commit();
            });

            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListCategories().isEmpty()){
                categories.viewAll.setVisibility(View.GONE);
                categories.title.setVisibility(View.GONE);
                categories.rv.setVisibility(View.GONE);
            }

        } else if (holder instanceof ArtistHolder artist) {

            if (artist.adapter == null) {
                artist.title.setText(context.getString(R.string.artist));

                artist.adapter = new AdapterHomeArtist(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListArtist());
                artist.rv.setAdapter(artist.adapter);

                artist.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position12) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position12, context.getString(R.string.artist));
                }));

                artist.viewAll.setOnClickListener(v -> {
                    FragmentSearchArtist searchArtist = new FragmentSearchArtist();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, searchArtist, context.getString(R.string.search_artist));
                    ft.addToBackStack(context.getString(R.string.search_artist));
                    ft.commit();
                });

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListArtist().isEmpty()){
                    artist.viewAll.setVisibility(View.GONE);
                    artist.title.setVisibility(View.GONE);
                    artist.rv.setVisibility(View.GONE);
                }
            }

        } else if (holder instanceof AlbumsHolder albums) {

            if (albums.adapter == null) {
                albums.title.setText(context.getString(R.string.albums));

                albums.adapter = new AdapterHomeAlbums(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListAlbums());
                albums.rv.setAdapter(albums.adapter);

                albums.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position13) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position13, context.getString(R.string.albums));
                }));

                albums.viewAll.setOnClickListener(v -> {
                    FragmentSearchAlbums searchAlbums = new FragmentSearchAlbums();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, searchAlbums, context.getString(R.string.search_albums));
                    ft.addToBackStack(context.getString(R.string.search_albums));
                    ft.commit();
                });

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListAlbums().isEmpty()){
                    albums.viewAll.setVisibility(View.GONE);
                    albums.title.setVisibility(View.GONE);
                    albums.rv.setVisibility(View.GONE);
                }
            }
        } else if (holder instanceof PlaylistHolder playlist) {

            if (playlist.adapter == null) {
                playlist.title.setText(context.getString(R.string.playlist));

                playlist.adapter = new AdapterPlaylistHome(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListPlaylist());
                playlist.rv.setAdapter(playlist.adapter);

                playlist.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position14) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position14, context.getString(R.string.playlist));
                }));

                playlist.viewAll.setOnClickListener(v -> {
                    FragmentSearchPlaylist searchPlaylist = new FragmentSearchPlaylist();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, searchPlaylist, context.getString(R.string.playlist));
                    ft.addToBackStack(context.getString(R.string.playlist));
                    ft.commit();
                });

                if (arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListPlaylist().isEmpty()){
                    playlist.viewAll.setVisibility(View.GONE);
                    playlist.title.setVisibility(View.GONE);
                    playlist.rv.setVisibility(View.GONE);
                }
            }
        } else if (holder instanceof SongsHolder songs && songs.adapter == null) {

            songs.title.setText(context.getString(R.string.songs));
            songs.adapter = new AdapterHomeSongs(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs(), new ClickListenerPlayList() {
                @Override
                public void onClick(int position) {
                    if (NetworkUtils.isConnected(context)) {
                        Callback.setIsOnline(true);
                        String addedFrom = "home".concat(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                        if (!Callback.getAddedFrom().equals(addedFrom)) {
                            Callback.getArrayListPlay().clear();
                            Callback.setArrayListPlay(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs());
                            Callback.setAddedFrom(addedFrom);
                            Callback.setIsNewAdded(true);
                        }
                        Callback.setPlayPos(position);

                        helper.showInterAd(position, context.getString(R.string.songs));
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onItemZero() {
                    // this method is empty
                }
            });
            songs.rv.setAdapter(songs.adapter);
            songs.viewAll.setOnClickListener(v -> {
                FragmentSearchSong searchSong = new FragmentSearchSong();
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, searchSong, context.getString(R.string.search_songs));
                ft.addToBackStack(context.getString(R.string.search_songs));
                ft.commit();
            });
            if (arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs().isEmpty()){
                songs.viewAll.setVisibility(View.GONE);
                songs.title.setVisibility(View.GONE);
                songs.rv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return arrayList.get(position) == null;
    }

    @Override
    public int getItemViewType(int position) {
        return switch (arrayList.get(position).getType()) {
            case "songs" -> VIEW_SONGS;
            case "artists" -> VIEW_ARTIST;
            case "albums" -> VIEW_ALBUMS;
            case "playlists" -> VIEW_PLAYLIST;
            case "categories" -> VIEW_CATEGORIES;
            default -> VIEW_PROG;
        };
    }

    InterAdListener interAdListener = new InterAdListener() {
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onClick(int position, @NonNull String type) {
            if (type.equals(context.getString(R.string.songs))) {
                Intent intent = new Intent(context, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                context.startService(intent);
            } else if (type.equals(context.getString(R.string.artist))) {
                Intent intent = new Intent(context, AudioByIDActivity.class);
                intent.putExtra("type", context.getString(R.string.artist));
                intent.putExtra("id", arrayList.get(clickPos).getArrayListArtist().get(position).getId());
                intent.putExtra("name", arrayList.get(clickPos).getArrayListArtist().get(position).getName());
                context.startActivity(intent);
            } else if (type.equals(context.getString(R.string.albums))) {
                Intent intent = new Intent(context, AudioByIDActivity.class);
                intent.putExtra("type", context.getString(R.string.albums));
                intent.putExtra("id", arrayList.get(clickPos).getArrayListAlbums().get(position).getId());
                intent.putExtra("name", arrayList.get(clickPos).getArrayListAlbums().get(position).getName());
                context.startActivity(intent);
            } else if (type.equals(context.getString(R.string.categories))) {
                Intent intent = new Intent(context, AudioByIDActivity.class);
                intent.putExtra("type", context.getString(R.string.categories));
                intent.putExtra("id", arrayList.get(clickPos).getArrayListCategories().get(position).getId());
                intent.putExtra("name", arrayList.get(clickPos).getArrayListCategories().get(position).getName());
                context.startActivity(intent);
            } else if (type.equals(context.getString(R.string.playlist))) {
                Intent intent = new Intent(context, AudioByIDActivity.class);
                intent.putExtra("type", context.getString(R.string.playlist));
                intent.putExtra("id", arrayList.get(clickPos).getArrayListPlaylist().get(position).getId());
                intent.putExtra("name", arrayList.get(clickPos).getArrayListPlaylist().get(position).getName());
                context.startActivity(intent);
            }
        }
    };
}