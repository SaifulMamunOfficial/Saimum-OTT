package nemosofts.tamilaudiopro.adapter.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.nemosofts.material.EnchantedViewPager;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByIDActivity;
import nemosofts.tamilaudiopro.activity.MainActivity;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.adapter.HomePagerAdapter;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.fragment.online.FragmentAlbums;
import nemosofts.tamilaudiopro.fragment.online.FragmentArtist;
import nemosofts.tamilaudiopro.fragment.online.FragmentCategories;
import nemosofts.tamilaudiopro.fragment.online.FragmentRecentSongs;
import nemosofts.tamilaudiopro.fragment.online.FragmentSectionSongs;
import nemosofts.tamilaudiopro.fragment.online.FragmentServerPlaylist;
import nemosofts.tamilaudiopro.fragment.online.FragmentTrendingSongs;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.interfaces.InterAdListener;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class AdapterHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    Helper helper;
    List<ItemPost> arrayList;
    int clickPos = 0;

    private static final int VIEW_PROG = 0;
    private static final int VIEW_BANNER = 1;
    private static final int VIEW_SONGS = 2;
    private static final int VIEW_ARTIST = 3;
    private static final int VIEW_ALBUMS = 4;
    private static final int VIEW_CATEGORIES = 5;
    private static final int VIEW_PLAYLIST = 6;
    private static final int VIEW_RECENT = 7;
    private static final int VIEW_SONGS_TRENDING = 8;
    private static final int VIEW_ADS = 9;
    private static final String TAG_HOME = "ishome";

    Boolean ads = true;

    AdapterHomeRecent adapterHomeRecent;
    AdapterHomeSongsTrending adapterHomeSongsTrending;
    AdapterHomeSongs adapterHomeSongs;

    public AdapterHome(Context context, List<ItemPost> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        helper = new Helper(context, interAdListener);
    }

    static class BannerHolder extends RecyclerView.ViewHolder {

        EnchantedViewPager enchantedViewPager;
        HomePagerAdapter homePagerAdapter;

        BannerHolder(View view) {
            super(view);
            enchantedViewPager = view.findViewById(R.id.viewPager_home);
            enchantedViewPager.useAlpha();
            enchantedViewPager.useScale();
            enchantedViewPager.setPageMargin(-5);
        }
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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class TrendingSongsHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        TextView title;
        LinearLayout viewAll;

        TrendingSongsHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class SongsHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        TextView title;
        LinearLayout viewAll;

        SongsHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    class RecentSongsHolder extends RecyclerView.ViewHolder {

        RecyclerView rv;
        TextView title;
        LinearLayout viewAll;

        RecentSongsHolder(View view) {
            super(view);
            rv = view.findViewById(R.id.rv_home_cat);
            title = view.findViewById(R.id.tv_home_title);
            viewAll = view.findViewById(R.id.ll_home_view_all);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(linearLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
        }
    }

    static class LatestAds extends RecyclerView.ViewHolder {

        LinearLayout adView;

        LatestAds(View view) {
            super(view);
            adView = view.findViewById(R.id.ll_adView);
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
        if (viewType == VIEW_BANNER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_banner, parent, false);
            return new BannerHolder(itemView);
        } else if (viewType == VIEW_CATEGORIES) {
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
        } else if (viewType == VIEW_RECENT) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new RecentSongsHolder(itemView);
        } else if (viewType == VIEW_SONGS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new SongsHolder(itemView);
        } else if (viewType == VIEW_SONGS_TRENDING) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_ui_categories, parent, false);
            return new TrendingSongsHolder(itemView);
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_baner_ad, parent, false);
            return new LatestAds(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BannerHolder bannerHolder) {
            if (bannerHolder.homePagerAdapter == null) {
                bannerHolder.enchantedViewPager.setFocusable(false);
                bannerHolder.homePagerAdapter = new HomePagerAdapter(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListBanner());
                bannerHolder.enchantedViewPager.setAdapter(bannerHolder.homePagerAdapter);
                if (bannerHolder.homePagerAdapter.getCount() > 2) {
                    bannerHolder.enchantedViewPager.setCurrentItem(1);
                }
            }
        } else if (holder instanceof CategoriesHolder categoriesHolder) {

            categoriesHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

            categoriesHolder.adapter = new AdapterHomeCat(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListCategories());
            categoriesHolder.rv.setAdapter(categoriesHolder.adapter);

            categoriesHolder.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position1) -> {
                clickPos = holder.getAbsoluteAdapterPosition();
                helper.showInterAd(position1, context.getString(R.string.categories));
            }));

            categoriesHolder.viewAll.setOnClickListener(v -> {
                FragmentCategories albums = new FragmentCategories();
                int bottomMenu;
                if (Boolean.TRUE.equals(arrayList.get(holder.getAbsoluteAdapterPosition()).getIsSections())){
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(TAG_HOME, true);
                    bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                    albums.setArguments(bundle);
                    bottomMenu = 5;
                } else {
                    bottomMenu = 1;
                }
                FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                        .getFragments()
                        .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, albums, context.getString(R.string.categories));
                ft.addToBackStack(context.getString(R.string.categories));
                ft.commit();
                Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.categories));
                ((MainActivity) context).bottomNavigationView(bottomMenu);
            });

        } else if (holder instanceof ArtistHolder artistHolder) {

            if (artistHolder.adapter == null) {
                artistHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

                artistHolder.adapter = new AdapterHomeArtist(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListArtist());
                artistHolder.rv.setAdapter(artistHolder.adapter);

                artistHolder.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position12) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position12, context.getString(R.string.artist));
                }));

                artistHolder.viewAll.setOnClickListener(v -> {
                    FragmentArtist artist = new FragmentArtist();
                    int bottomMenu;
                    if (Boolean.TRUE.equals(arrayList.get(holder.getAbsoluteAdapterPosition()).getIsSections())){
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(TAG_HOME, true);
                        bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                        artist.setArguments(bundle);
                        bottomMenu = 5;
                    } else {
                        bottomMenu = 2;
                    }
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, artist, context.getString(R.string.artist));
                    ft.addToBackStack(context.getString(R.string.artist));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.artist));
                    ((MainActivity) context).bottomNavigationView(bottomMenu);
                });
            }
        } else if (holder instanceof AlbumsHolder albumsHolder) {

            if (albumsHolder.adapter == null) {
                albumsHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

                albumsHolder.adapter = new AdapterHomeAlbums(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListAlbums());
                albumsHolder.rv.setAdapter(albumsHolder.adapter);

                albumsHolder.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position13) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position13, context.getString(R.string.albums));
                }));

                albumsHolder.viewAll.setOnClickListener(v -> {
                    FragmentAlbums albums = new FragmentAlbums();
                    int bottomMenu;
                    if (Boolean.TRUE.equals(arrayList.get(holder.getAbsoluteAdapterPosition()).getIsSections())){
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(TAG_HOME, true);
                        bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                        albums.setArguments(bundle);
                        bottomMenu = 5;
                    } else {
                        bottomMenu = 2;
                    }
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, albums, context.getString(R.string.artist));
                    ft.addToBackStack(context.getString(R.string.artist));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.albums));
                    ((MainActivity) context).bottomNavigationView(bottomMenu);

                });
            }
        } else if (holder instanceof PlaylistHolder playlistHolder) {

            if (playlistHolder.adapter == null) {
                playlistHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());

                playlistHolder.adapter = new AdapterPlaylistHome(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListPlaylist());
                playlistHolder.rv.setAdapter(playlistHolder.adapter);

                playlistHolder.rv.addOnItemTouchListener(new RecyclerItemClickListener(context, (view, position14) -> {
                    clickPos = holder.getAbsoluteAdapterPosition();
                    helper.showInterAd(position14, context.getString(R.string.playlist));
                }));

                playlistHolder.viewAll.setOnClickListener(v -> {
                    FragmentServerPlaylist serverPlaylist = new FragmentServerPlaylist();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(TAG_HOME, true);
                    bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                    serverPlaylist.setArguments(bundle);
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, serverPlaylist, context.getString(R.string.playlist));
                    ft.addToBackStack(context.getString(R.string.playlist));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.playlist));
                    ((MainActivity) context).bottomNavigationView(5);
                });
            }
        } else if (holder instanceof RecentSongsHolder recentSongsHolder) {

            if (adapterHomeRecent == null) {

                recentSongsHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                adapterHomeRecent = new AdapterHomeRecent(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs(), new ClickListenerPlayList() {
                    @Override
                    public void onClick(int position15) {
                        if (NetworkUtils.isConnected(context)) {
                            Callback.setIsOnline(true);
                            String addedFrom = "home".concat(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            if (!Callback.getAddedFrom().equals(addedFrom)) {
                                Callback.getArrayListPlay().clear();
                                Callback.setArrayListPlay(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs());
                                Callback.setAddedFrom(addedFrom);
                                Callback.setIsNewAdded(true);
                            }
                            Callback.setPlayPos(position15);

                            helper.showInterAd(position15, context.getString(R.string.songs));
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onItemZero() {
                        // this method is empty
                    }
                });
                recentSongsHolder.rv.setAdapter(adapterHomeRecent);
                recentSongsHolder.viewAll.setOnClickListener(v -> {
                    FragmentRecentSongs recentSongs = new FragmentRecentSongs();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, recentSongs, context.getString(R.string.recently));
                    ft.addToBackStack(context.getString(R.string.recently));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.recently));
                    ((MainActivity) context).bottomNavigationView(3);
                });
            }
        } else if (holder instanceof SongsHolder songsHolder) {

            if (adapterHomeSongs == null) {

                songsHolder.title.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                adapterHomeSongs = new AdapterHomeSongs(context, arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs(), new ClickListenerPlayList() {
                    @Override
                    public void onClick(int position16) {
                        if (NetworkUtils.isConnected(context)) {
                            Callback.setIsOnline(true);
                            String addedFrom = "home".concat(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            if (!Callback.getAddedFrom().equals(addedFrom)) {
                                Callback.getArrayListPlay().clear();
                                Callback.setArrayListPlay(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs());
                                Callback.setAddedFrom(addedFrom);
                                Callback.setIsNewAdded(true);
                            }
                            Callback.setPlayPos(position16);

                            helper.showInterAd(position16, context.getString(R.string.songs));
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onItemZero() {
                        // this method is empty
                    }
                });
                songsHolder.rv.setAdapter(adapterHomeSongs);

                songsHolder.viewAll.setOnClickListener(v -> {
                    FragmentSectionSongs sectionSongs = new FragmentSectionSongs();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                    sectionSongs.setArguments(bundle);
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, sectionSongs, context.getString(R.string.songs));
                    ft.addToBackStack(context.getString(R.string.songs));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(songsHolder.title.getText().toString());
                    ((MainActivity) context).bottomNavigationView(5);
                });

            }
        } else if (holder instanceof TrendingSongsHolder trendingSongsHolder) {

            if (adapterHomeSongsTrending == null) {

                trendingSongsHolder.title.setText(context.getString(R.string.trending_songs));

                adapterHomeSongsTrending = new AdapterHomeSongsTrending(context,
                        arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs(), new ClickListenerPlayList() {
                    @Override
                    public void onClick(int position17) {
                        if (NetworkUtils.isConnected(context)) {
                            Callback.setIsOnline(true);
                            String addedFrom = "home".concat(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            if (!Callback.getAddedFrom().equals(addedFrom)) {
                                Callback.getArrayListPlay().clear();
                                Callback.setArrayListPlay(arrayList.get(holder.getAbsoluteAdapterPosition()).getArrayListSongs());
                                Callback.setAddedFrom(addedFrom);
                                Callback.setIsNewAdded(true);


                            }
                            Callback.setPlayPos(position17);

                            helper.showInterAd(position17, context.getString(R.string.songs));
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onItemZero() {
                        // this method is empty
                    }
                });
                trendingSongsHolder.rv.setAdapter(adapterHomeSongsTrending);

                trendingSongsHolder.viewAll.setOnClickListener(v -> {
                    FragmentTrendingSongs trending = new FragmentTrendingSongs();
                    FragmentTransaction ft = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(((AppCompatActivity) context).getSupportFragmentManager()
                            .getFragments()
                            .get(((AppCompatActivity) context).getSupportFragmentManager().getBackStackEntryCount()));
                    ft.add(R.id.fragment, trending, context.getString(R.string.trending_songs));
                    ft.addToBackStack(context.getString(R.string.trending_songs));
                    ft.commit();
                    Objects.requireNonNull(((MainActivity) context).getSupportActionBar()).setTitle(context.getString(R.string.trending_songs));
                    ((MainActivity) context).bottomNavigationView(5);
                });
            }
        }
        else if (holder instanceof LatestAds latestAds && Boolean.TRUE.equals(ads)){
            ads = false;
            helper.showBannerAd(latestAds.adView, Callback.PAGE_HOME);
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

    @SuppressLint("NotifyDataSetChanged")
    public void onEqualizerChange() {
        try {
            if (adapterHomeRecent != null){
                adapterHomeRecent.notifyDataSetChanged();
            }
            if (adapterHomeSongsTrending != null){
                adapterHomeSongsTrending.notifyDataSetChanged();
            }
            if (adapterHomeSongs != null){
                adapterHomeSongs.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("AdapterHome", "onEqualizerChange" ,e);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return switch (arrayList.get(position).getType()) {
            case "song" -> VIEW_SONGS;
            case "trending" -> VIEW_SONGS_TRENDING;
            case "recent" -> VIEW_RECENT;
            case "artist" -> VIEW_ARTIST;
            case "album" -> VIEW_ALBUMS;
            case "playlist" -> VIEW_PLAYLIST;
            case "category" -> VIEW_CATEGORIES;
            case "slider" -> VIEW_BANNER;
            case "ads" -> VIEW_ADS;
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
            }else if (type.equals(context.getString(R.string.artist))) {
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