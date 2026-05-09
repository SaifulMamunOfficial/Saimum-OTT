package nemosofts.tamilaudiopro.fragment.offline;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.adapter.AdapterOFSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickDeleteListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class FragmentOFSongs extends Fragment {

    private static final String TAG = "FragmentOFSongs";
    private Helper helper;
    private RecyclerView rv;
    private AdapterOFSongList adapter;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg = "";
    private SearchView searchView;

    @NonNull
    @Contract(" -> new")
    public static FragmentOFSongs newInstance() {
        return new FragmentOFSongs();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio, container, false);

        helper = new Helper(getActivity(), (position, type) -> openPlayerService());

        errorMsg = getString(R.string.error_no_songs_found);

        progressBar = rootView.findViewById(R.id.pb_audio);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        new LoadOfflineSongs().execute();

        addMenuProvider();
        return rootView;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService() {
        try {
            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_PLAY);
            requireActivity().startService(intent);
        } catch (Exception e) {
            ApplicationUtil.log(TAG,"Error openPlayerService",e);
        }
    }

    private void addMenuProvider() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_search, menu);

                // Configure the search menu item
                MenuItem item = menu.findItem(R.id.menu_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
                searchView = (SearchView) item.getActionView();
                if (searchView != null) {
                    searchView.setOnQueryTextListener(queryTextListener);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item selection if necessary
                return false;
            }
        }, getViewLifecycleOwner());
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextChange(String s) {
            if (adapter != null && (!searchView.isIconified())) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
        }
    };

    class LoadOfflineSongs extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            if (Callback.getArrayListOfflineSongs().isEmpty()) {
                helper.getListOfflineSongs();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                setAdapter();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void setAdapter() {
        adapter = new AdapterOFSongList(getActivity(), Callback.getArrayListOfflineSongs(), new ClickDeleteListenerPlayList() {
            @Override
            public void onClick(int position) {
                Callback.setIsOnline(false);
                if (!Callback.getAddedFrom().equals(TAG)) {
                    Callback.getArrayListPlay().clear();
                    Callback.setArrayListPlay(Callback.getArrayListOfflineSongs());
                    Callback.setAddedFrom(TAG);
                    Callback.setIsNewAdded(true);
                }
                Callback.setPlayPos(position);

                helper.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                // this method is empty
            }

            @Override
            public void onDelete(int pos, Exception exception, int deleteRequestUriR, int deleteRequestUriQ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && exception instanceof RecoverableSecurityException) {
                    try {
                        ArrayList<Uri> arrayListUri = new ArrayList<>();
                        arrayListUri.add(Uri.parse(adapter.getItem(pos).getUrl()));
                        PendingIntent editPendingIntent = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), arrayListUri);

                        startIntentSenderForResult(editPendingIntent.getIntentSender(), deleteRequestUriR, null, 0, 0, 0 , null);
                    } catch (IntentSender.SendIntentException e) {
                        ApplicationUtil.log(TAG,"Error startIntentSenderForResult",e);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && exception instanceof RecoverableSecurityException) {
                    try {
                        startIntentSenderForResult(((RecoverableSecurityException) exception).getUserAction().getActionIntent().getIntentSender(), deleteRequestUriQ, null, 0, 0, 0,    null);
                    } catch (IntentSender.SendIntentException e) {
                        ApplicationUtil.log(TAG,"Error startIntentSenderForResult",e);
                    }
                }
            }
        }, "");
        rv.setAdapter(adapter);
        setEmpty();
    }

    public void setEmpty() {
        if (!Callback.getArrayListOfflineSongs().isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView btnTitle = myView.findViewById(R.id.tv_empty);
            btnTitle.setText(getString(R.string.refresh));

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DownloadActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        try {
            if(adapter != null) {
                adapter.closeDatabase();
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG,"Error closeDatabase",e);
        }
        super.onDestroy();
    }
}