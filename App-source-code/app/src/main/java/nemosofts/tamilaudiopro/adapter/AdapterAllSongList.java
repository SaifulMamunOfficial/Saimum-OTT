package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.material.EqualizerView;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.utils.FormatUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.wortise.ads.AdError;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
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
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class AdapterAllSongList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "AdapterAllSongList";
    Context context;
    List <ItemSong> arrayList;
    List<ItemSong> filteredArrayList;
    ClickListenerPlayList recyclerClickListener;
    NameFilter filter;
    String type;
    Helper helper;
    SPHelper spHelper;
    DBHelper dbHelper;
    boolean isDarkMode;
    private static final int VIEW_PROG = -1;
    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();
    List<NativeAdDetails> nativeAdsStartApp = new ArrayList<>();

    public AdapterAllSongList(Context context,
                              List<ItemSong> arrayList,
                              ClickListenerPlayList recyclerClickListener,
                              String type) {
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        this.context = context;
        this.type = type;
        this.recyclerClickListener = recyclerClickListener;
        helper = new Helper(context);
        spHelper = new SPHelper(context);
        dbHelper = new DBHelper(context);
        isDarkMode = new ThemeEngine(context).getIsThemeMode();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayoutAudio;
        LinearLayout linearLayout;
        ImageHelperView poster;
        EqualizerView equalizer;
        TextView audioTitle;
        TextView catName;
        TextView avgRate;
        TextView audioViews;
        TextView audioDownload;
        RatingBar ratingBar;
        ImageView option;
        ImageView audioPlay;
        ImageView download;
        ImageView btnDownload;
        RelativeLayout nativeAd;

        MyViewHolder(View view) {
            super(view);

            relativeLayoutAudio = view.findViewById(R.id.rl_audio_list);
            poster = view.findViewById(R.id.iv_audio_list);
            equalizer = view.findViewById(R.id.equalizer_audio_list);
            audioTitle = view.findViewById(R.id.tv_audio_list_name);
            catName = view.findViewById(R.id.tv_audio_list_cat);
            ratingBar = view.findViewById(R.id.rb_audio_list);
            avgRate = view.findViewById(R.id.tv_audio_list_avg_rate);
            audioPlay = view.findViewById(R.id.iv_audio_list_play);
            audioViews = view.findViewById(R.id.tv_audio_list_views);
            audioDownload = view.findViewById(R.id.tv_audio_list_download);
            download = view.findViewById(R.id.iv_audio_list_download);
            btnDownload = view.findViewById(R.id.iv_audio_download);
            option = view.findViewById(R.id.iv_list_option);
            linearLayout = view.findViewById(R.id.ll_audio_list_end);

            nativeAd = view.findViewById(R.id.rl_native_ad);
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
        if (viewType == VIEW_PROG) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_progressbar, parent, false);
            return new ProgressViewHolder(v);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_all_audio, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder myViewHolder) {

            myViewHolder.audioViews.setText(FormatUtils.format(Integer.valueOf(arrayList.get(holder.getAbsoluteAdapterPosition()).getViews())));
            myViewHolder.audioDownload.setText(FormatUtils.format(Integer.valueOf(arrayList.get(holder.getAbsoluteAdapterPosition()).getDownloads())));

            myViewHolder.audioTitle.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getTitle());
            Picasso.get()
                    .load(arrayList.get(holder.getAbsoluteAdapterPosition()).getImageBig())
                    .resize(200,200)
                    .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                    .into(myViewHolder.poster);

            myViewHolder.avgRate.setTypeface(myViewHolder.avgRate.getTypeface(), Typeface.BOLD);
            myViewHolder.avgRate.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getAverageRating());
            myViewHolder.ratingBar.setRating(Float.parseFloat(arrayList.get(holder.getAbsoluteAdapterPosition()).getAverageRating()));

            if (PlayerService.getIsPlayling()
                    && Callback.getPlayPos() <= holder.getAbsoluteAdapterPosition()
                    && Callback.getArrayListPlay().get(Callback.getPlayPos()).getId().equals(arrayList.get(holder.getAbsoluteAdapterPosition()).getId())) {
                myViewHolder.audioPlay.setVisibility(View.INVISIBLE);
                myViewHolder.equalizer.setVisibility(View.VISIBLE);
                myViewHolder.equalizer.animateBars();

                myViewHolder.relativeLayoutAudio.setBackgroundColor(ColorUtils.colorPrimarySub(context));
                myViewHolder.audioTitle.setTextColor(ColorUtils.colorPrimary(context));
            } else {
                myViewHolder.audioPlay.setVisibility(View.VISIBLE);
                myViewHolder.equalizer.setVisibility(View.GONE);
                myViewHolder.equalizer.stopBars();

                myViewHolder.relativeLayoutAudio.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                myViewHolder.audioTitle.setTextColor(ColorUtils.colorTitle(context));
            }

            myViewHolder.catName.setText(arrayList.get(holder.getAbsoluteAdapterPosition()).getArtist());

            myViewHolder.relativeLayoutAudio.setOnClickListener(view -> recyclerClickListener.onClick(getPosition(arrayList.get(holder.getAbsoluteAdapterPosition()).getId())));
            myViewHolder.option.setOnClickListener(view -> openBottomSheet(holder.getAbsoluteAdapterPosition()));
            myViewHolder.linearLayout.setOnClickListener(view -> {
            });

            if (Boolean.FALSE.equals(spHelper.getIsSongDownload())){
                myViewHolder.audioDownload.setVisibility(View.GONE);
                myViewHolder.download.setVisibility(View.GONE);
                myViewHolder.btnDownload.setVisibility(View.GONE);
            } else {
                myViewHolder.btnDownload.setOnClickListener(view -> {
                    if (DownloadService.count >= 0 && DownloadService.count < 5) {
                        try{
                            if (spHelper.getRewardCredit() != 0){
                                spHelper.useRewardCredit(1);
                                Toast.makeText(context, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                                helper.download(arrayList.get(holder.getAbsoluteAdapterPosition()));
                                arrayList.get(holder.getAbsoluteAdapterPosition()).setDownload(true);
                                myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_fill);
                            } else {
                                helper.showRewardAds(holder.getAbsoluteAdapterPosition(), new RewardAdListener() {
                                    @Override
                                    public void onClick(boolean isLoad, int pos) {
                                        if (isLoad){
                                            spHelper.addRewardCredit(Callback.getRewardCredit());
                                            spHelper.useRewardCredit(1);
                                            Toast.makeText(context, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                                            helper.download(arrayList.get(pos));
                                            arrayList.get(pos).setDownload(true);
                                            myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_fill);
                                        } else {
                                            Toast.makeText(context, "Display Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onPurchases(int pos) {
                                        helper.download(arrayList.get(pos));
                                        arrayList.get(pos).setDownload(true);
                                        myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_fill);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error DownloadService ",e );
                        }
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.please_wait_a_minutes), Toast.LENGTH_SHORT).show();
                    }
                });

                if (Boolean.TRUE.equals(dbHelper.checkDownload(arrayList.get(holder.getAbsoluteAdapterPosition()).getId()))) {
                    myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_fill);
                } else {
                    if (Boolean.TRUE.equals(arrayList.get(holder.getAbsoluteAdapterPosition()).getIsDownload())){
                        myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_fill);
                    }else {
                        myViewHolder.btnDownload.setImageResource(R.drawable.ic_download_cloud_line);
                    }
                }
            }

            if (Callback.getIsAdsStatus() && isAdLoaded
                    && (holder.getAbsoluteAdapterPosition() != arrayList.size() - 1)
                    && (holder.getAbsoluteAdapterPosition() + 1) % Callback.getNativeAdShow() == 0) {
                try {
                    if (myViewHolder.nativeAd.getChildCount() == 0) {
                        switch (Callback.getAdNetwork()) {
                            case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {
                                if (!mNativeAdsAdmob.isEmpty()) {

                                    int i = ApplicationUtil.getRandomValue(mNativeAdsAdmob.size() - 1);

                                    @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context)
                                            .getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                    myViewHolder.nativeAd.removeAllViews();
                                    myViewHolder.nativeAd.addView(adView);

                                    myViewHolder.nativeAd.setVisibility(View.VISIBLE);
                                }
                            }
                            case Callback.AD_TYPE_STARTAPP -> {
                                int i = ApplicationUtil.getRandomValue(nativeAdsStartApp.size() - 1);
                                @SuppressLint("InflateParams") RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context)
                                        .getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);
                                populateStartAppNativeAdView(nativeAdsStartApp.get(i), nativeAdView);
                                myViewHolder.nativeAd.removeAllViews();
                                myViewHolder.nativeAd.addView(nativeAdView);
                                myViewHolder.nativeAd.setVisibility(View.VISIBLE);
                            }
                            case Callback.AD_TYPE_APPLOVIN -> {
                                MaxNativeAdLoader nativeAdLoader = getMaxNativeAdLoader(myViewHolder);
                                nativeAdLoader.loadAd();
                            }
                            case Callback.AD_TYPE_WORTISE -> {
                                GoogleNativeAd googleNativeAd = new GoogleNativeAd(context, Callback.getWortiseNativeAdID(), new GoogleNativeAd.Listener() {
                                    @Override
                                    public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {
                                        // this method is empty
                                    }

                                    @Override
                                    public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd,
                                                                     @NonNull AdError adError) {
                                        // this method is empty
                                    }

                                    @Override
                                    public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {
                                        // this method is empty
                                    }

                                    @Override
                                    public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull NativeAd nativeAd) {
                                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context)
                                                .getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                        populateUnifiedNativeAdView(nativeAd, adView);
                                        myViewHolder.nativeAd.removeAllViews();
                                        myViewHolder.nativeAd.addView(adView);

                                        myViewHolder.nativeAd.setVisibility(View.VISIBLE);
                                    }
                                });
                                googleNativeAd.load();
                            }
                            default -> myViewHolder.nativeAd.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error NativeAd",e );
                }
            }
        }
    }

    private @NonNull MaxNativeAdLoader getMaxNativeAdLoader(MyViewHolder holder) {
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Callback.getApplovinNativeAdID(), context);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, @NonNull final MaxAd ad) {
                holder.nativeAd.removeAllViews();
                holder.nativeAd.addView(nativeAdView);
                holder.nativeAd.setVisibility(View.VISIBLE);
            }
        });
        return nativeAdLoader;
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position) != null) {
            return position;
        } else {
            return VIEW_PROG;
        }
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.bottom_sheet_audio, null);
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        TextView title = dialog.findViewById(R.id.tv_sheet_text);
        TextView subTitle = dialog.findViewById(R.id.tv_sheet_list_cat);
        ImageHelperView poster = dialog.findViewById(R.id.iv_sheet_post);

        LinearLayout share = dialog.findViewById(R.id.ll_sheet_share);
        LinearLayout youtube = dialog.findViewById(R.id.ll_sheet_youtube);
        LinearLayout download = dialog.findViewById(R.id.ll_sheet_download);
        LinearLayout queue = dialog.findViewById(R.id.ll_sheet_add_queue);
        LinearLayout addSong = dialog.findViewById(R.id.ll_sheet_add_song);
        TextView addSongText = dialog.findViewById(R.id.tv_add_song);

        int placeholder = isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light;

        if (poster != null){
            Picasso.get()
                    .load(arrayList.get(pos).getImageBig())
                    .centerCrop()
                    .resize(300,300)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(poster);
        }

        Objects.requireNonNull(subTitle).setText(arrayList.get(pos).getArtist());
        Objects.requireNonNull(title).setText(arrayList.get(pos).getTitle());

        if (type.equals("playlist")) {
            Objects.requireNonNull(addSongText).setText(context.getString(R.string.remove));
        }
        if (Boolean.FALSE.equals(Callback.getIsOnline())) {
            Objects.requireNonNull(queue).setVisibility(View.GONE);
        }
        if (Boolean.FALSE.equals(spHelper.getIsSongDownload())) {
            Objects.requireNonNull(download).setVisibility(View.GONE);
        }
        if (!helper.isYoutubeAppInstalled()) {
            Objects.requireNonNull(youtube).setVisibility(View.GONE);
        }

        Objects.requireNonNull(addSong).setOnClickListener(view10 -> {
            if ("playlist".equals(type)) {
                dbHelper.removeFromPlayList(arrayList.get(pos).getId(), true);
                arrayList.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, context.getString(R.string.remove_from_playlist), Toast.LENGTH_SHORT).show();
                if (arrayList.isEmpty()) {
                    recyclerClickListener.onItemZero();
                }
            } else {
                helper.openPlaylists(arrayList.get(pos), true);
            }
            dialog.dismiss();
        });
        Objects.requireNonNull(queue).setOnClickListener(view11 -> {
            Callback.getArrayListPlay().add(arrayList.get(pos));
            PlayerService.getInstance().addMediaSource(Uri.parse(arrayList.get(pos).getUrl()));
            GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
            Toast.makeText(context, context.getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        Objects.requireNonNull(download).setOnClickListener(view12 -> {
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
                    Log.e(TAG, "Error DownloadService openBottomSheet",e );
                }
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.please_wait_a_minutes), Toast.LENGTH_SHORT).show();
            }
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

    private void populateStartAppNativeAdView(NativeAdDetails nativeAdDetails, RelativeLayout nativeAdView) {
        ImageView icon = nativeAdView.findViewById(R.id.icon);
        TextView title = nativeAdView.findViewById(R.id.title);
        TextView description = nativeAdView.findViewById(R.id.description);
        Button button = nativeAdView.findViewById(R.id.button);

        icon.setImageBitmap(nativeAdDetails.getImageBitmap());
        title.setText(nativeAdDetails.getTitle());
        description.setText(nativeAdDetails.getDescription());
        button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
    }

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

    public void destroyNativeAds() {
        try {
            for (int i = 0; i < mNativeAdsAdmob.size(); i++) {
                mNativeAdsAdmob.get(i).destroy();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error destroyNativeAds",e);
        }
    }

    public void addAds(NativeAd unifiedNativeAd) {
        mNativeAdsAdmob.add(unifiedNativeAd);
        isAdLoaded = true;
    }

    public void addNativeAds(List<NativeAdDetails> nativeAdDetails) {
        nativeAdsStartApp.addAll(nativeAdDetails);
        isAdLoaded = true;
    }

    public void setNativeAds(boolean isLoaded) {
        isAdLoaded = isLoaded;
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
        protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
            arrayList = (ArrayList<ItemSong>) results.values;
            notifyDataSetChanged();
        }
    }

    public void closeDatabase() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closeDatabase",e);
        }
    }
}