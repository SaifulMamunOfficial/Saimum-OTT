package nemosofts.tamilaudiopro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.material.EnchantedViewPager;
import androidx.nemosofts.material.EnchantedViewPagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByIDActivity;
import nemosofts.tamilaudiopro.interfaces.InterAdListener;
import nemosofts.tamilaudiopro.item.ItemHomeSlider;
import nemosofts.tamilaudiopro.utils.LoadColor;
import nemosofts.tamilaudiopro.utils.helper.Helper;


public class HomePagerAdapter extends EnchantedViewPagerAdapter {

    private final Context mContext;
    private final LayoutInflater inflater;
    private final List<ItemHomeSlider> arrayList;
    private final Helper helper;
    boolean isDarkMode;

    public HomePagerAdapter(Context context, List<ItemHomeSlider> arrayList) {
        super(arrayList);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = arrayList;
        helper = new Helper(context, interAdListener);
        isDarkMode = new ThemeEngine(context).getIsThemeMode();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View mCurrentView = inflater.inflate(R.layout.item_home_banner, container, false);

        TextView title = mCurrentView.findViewById(R.id.tv_home_banner);
        TextView desc = mCurrentView.findViewById(R.id.tv_home_banner_desc);
        ImageView banner = mCurrentView.findViewById(R.id.iv_home_banner);
        View gradient = mCurrentView.findViewById(R.id.view_home_banner);

        title.setText(arrayList.get(position).getTitle());
        desc.setText(arrayList.get(position).getInfo());

        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(isDarkMode ? R.drawable.placeholder_folder_night : R.drawable.placeholder_folder_light)
                .error(isDarkMode ? R.drawable.placeholder_folder_night : R.drawable.placeholder_folder_light)
                .into(banner);

        new LoadColor(gradient).execute(arrayList.get(position).getImage());

        mCurrentView.setOnClickListener(v -> helper.showInterAd(position, ""));

        mCurrentView.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
        container.addView(mCurrentView);

        return mCurrentView;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    InterAdListener interAdListener = new InterAdListener() {
        @Override
        public void onClick(int position, String type) {
            Intent intent = new Intent(mContext, AudioByIDActivity.class);
            intent.putExtra("type", mContext.getString(R.string.banner));
            intent.putExtra("id",arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getTitle());
            mContext.startActivity(intent);
        }
    };
}