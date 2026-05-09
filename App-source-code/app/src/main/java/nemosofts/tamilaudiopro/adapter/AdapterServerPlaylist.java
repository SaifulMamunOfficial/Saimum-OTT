package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.AdError;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;

public class AdapterServerPlaylist extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<ItemServerPlayList> arrayList;
    List<ItemServerPlayList> filteredArrayList;
    NameFilter filter;
    int columnWidth = 0;
    private static final int VIEW_PROG = -1;
    private static final int VIEW_ADS = -2;
    Boolean isAdLoaded = false;
    List<NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public AdapterServerPlaylist(Context context, List<ItemServerPlayList> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.filteredArrayList = arrayList;
        columnWidth = DeviceUtils.getColumnWidth(context, 2,5);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rl;
        private final TextView textView;
        private final ImageHelperView imageView;

        MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tv_server_playlist);
            rl = view.findViewById(R.id.rl_server_playlist);
            imageView = view.findViewById(R.id.iv_server_playlist);
        }
    }

    private static class ADViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout nativeAd;
        boolean isAdRequested = false;

        private ADViewHolder(View view) {
            super(view);
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
        } else if (viewType == VIEW_ADS) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_ads, parent, false);
            return new ADViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_server_playlist, parent, false);
            return new MyViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder myViewHolder) {
            myViewHolder.rl.setLayoutParams(new LinearLayout.LayoutParams(columnWidth, columnWidth));
            myViewHolder.textView.setText(arrayList.get(position).getName());
            Picasso.get()
                    .load(arrayList.get(position).getImage())
                    .resize(300,300)
                    .placeholder(R.drawable.material_design_default)
                    .into(myViewHolder.imageView);

        } else if (holder instanceof ADViewHolder adViewHolder) {
            if (adViewHolder.nativeAd.getChildCount() == 0) {
                switch (Callback.getAdNetwork()) {
                    case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {
                        if (Boolean.TRUE.equals(isAdLoaded) && mNativeAdsAdmob.size() >= 5) {

                            int i = ApplicationUtil.getRandomValue(mNativeAdsAdmob.size() - 1);

                            @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) ((Activity) context)
                                    .getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                            populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                            adViewHolder.nativeAd.removeAllViews();
                            adViewHolder.nativeAd.addView(adView);

                            adViewHolder.nativeAd.setVisibility(View.VISIBLE);
                        }
                    }
                    case Callback.AD_TYPE_STARTAPP -> {
                        if (!adViewHolder.isAdRequested) {
                            StartAppNativeAd nativeAd = new StartAppNativeAd(context);

                            nativeAd.loadAd(new NativeAdPreferences()
                                    .setAdsNumber(1)
                                    .setAutoBitmapDownload(true)
                                    .setPrimaryImageSize(2), new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull Ad ad) {
                                    try {
                                        if (!nativeAd.getNativeAds().isEmpty()) {
                                            @SuppressLint("InflateParams") RelativeLayout nativeAdView = (RelativeLayout) ((Activity) context)
                                                    .getLayoutInflater().inflate(R.layout.layout_native_ad_startapp, null);

                                            ImageView icon = nativeAdView.findViewById(R.id.icon);
                                            TextView title = nativeAdView.findViewById(R.id.title);
                                            TextView description = nativeAdView.findViewById(R.id.description);
                                            Button button = nativeAdView.findViewById(R.id.button);

                                            Picasso.get()
                                                    .load(nativeAd.getNativeAds().get(0).getImageUrl())
                                                    .into(icon);
                                            title.setText(nativeAd.getNativeAds().get(0).getTitle());
                                            description.setText(nativeAd.getNativeAds().get(0).getDescription());
                                            button.setText(nativeAd.getNativeAds().get(0).isApp() ? "Install" : "Open");

                                            adViewHolder.nativeAd.removeAllViews();
                                            adViewHolder.nativeAd.addView(nativeAdView);
                                            adViewHolder.nativeAd.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                       ApplicationUtil.log("AdapterServerPlaylist","Error onReceiveAd",e);
                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(Ad ad) {
                                    adViewHolder.isAdRequested = false;
                                }
                            });
                            adViewHolder.isAdRequested = true;
                        }
                    }
                    case Callback.AD_TYPE_APPLOVIN -> {
                        MaxNativeAdLoader nativeAdLoader = getMaxNativeAdLoader(adViewHolder);
                        nativeAdLoader.loadAd();
                    }
                    case Callback.AD_TYPE_WORTISE -> {
                        if (!adViewHolder.isAdRequested) {
                            GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                    context, Callback.getWortiseNativeAdID(), new GoogleNativeAd.Listener() {
                                @Override
                                public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {
                                    // this method is empty
                                }

                                @Override
                                public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull AdError adError) {
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
                                    adViewHolder.nativeAd.removeAllViews();
                                    adViewHolder.nativeAd.addView(adView);

                                    adViewHolder.nativeAd.setVisibility(View.VISIBLE);
                                }
                            });
                            googleNativeAd.load();
                            adViewHolder.isAdRequested = true;
                        }
                    }
                    default -> adViewHolder.nativeAd.setVisibility(View.GONE);
                }
            }
        } else {
            if (getItemCount() <= 10) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    private @NonNull MaxNativeAdLoader getMaxNativeAdLoader(ADViewHolder holder) {
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

    public ItemServerPlayList getItem(int pos) {
        return arrayList.get(pos);
    }

    public void hideHeader() {
        try {
            ProgressViewHolder.progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            ApplicationUtil.log("AdapterServerPlaylist","Error hideHeader",e);
        }
    }

    public boolean isHeader(int position) {
        return position == arrayList.size();
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return VIEW_PROG;
        } else if (arrayList.get(position) == null) {
            return VIEW_ADS;
        } else {
            return position;
        }
    }

    public void addAds(List<NativeAd> arrayListNativeAds) {
        isAdLoaded = true;
        mNativeAdsAdmob.addAll(arrayListNativeAds);
        for (int i = 0; i < arrayList.size(); i++) {
            if(arrayList.get(i) == null) {
                notifyItemChanged(i);
            }
        }
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
                ArrayList<ItemServerPlayList> filteredItems = new ArrayList<>();

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
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList = (ArrayList<ItemServerPlayList>) results.values;
            notifyDataSetChanged();
        }
    }
}