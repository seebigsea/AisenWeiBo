package org.aisen.weibo.sina.ui.activity.pics;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.m.common.utils.KeyGenerator;
import com.m.common.utils.SystemBarUtils;
import com.m.support.adapter.FragmentPagerAdapter;
import com.m.support.inject.ViewInject;
import com.m.ui.activity.basic.BaseActivity;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.support.bean.PhotosBean;
import org.aisen.weibo.sina.ui.fragment.comment.TimelineCommentFragment;
import org.aisen.weibo.sina.ui.fragment.pics.PictureFragment;
import org.aisen.weibo.sina.ui.fragment.profile.PhotosFragment;
import org.sina.android.bean.PicUrls;
import org.sina.android.bean.StatusContent;

/**
 * 相册
 * 
 * @author Jeff.Wang
 *
 * @date 2014年11月6日
 */
public class PhotosActivity extends BaseActivity implements OnPageChangeListener {

	public static void launch(ABaseFragment from, int index, int requestCode) {
		Intent intent  = new Intent(from.getActivity(), PhotosActivity.class);
		intent.putExtra("index", index);
		from.startActivityForResult(intent, requestCode);
	}
	
	@ViewInject(id = R.id.viewPager)
    ViewPager viewPager;
	@ViewInject(id = R.id.layStatus, click = "launchStatus")
	View layStatus;
    @ViewInject(id = R.id.viewStatus)
    View viewStatus;
	@ViewInject(id = R.id.txtStatus, click = "launchStatus")
	TextView txtStatus;
    @ViewInject(id = R.id.layToolbar)
    ViewGroup layToolbar;
	
	private PhotosBean mBean;
	private int index;

    MyViewPagerAdapter myViewPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.as_ui_pics_pager);
		
		if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mBean = savedInstanceState == null ? PhotosFragment.photos
										   : (PhotosBean) savedInstanceState.getSerializable("bean");
		index = savedInstanceState == null ? getIntent().getIntExtra("index", 0)
										: savedInstanceState.getInt("index", 0);

        myViewPagerAdapter = new MyViewPagerAdapter(getFragmentManager());
		viewPager.setAdapter(myViewPagerAdapter);
		viewPager.setCurrentItem(index);
		viewPager.setOnPageChangeListener(this);
		if (size() > 1 && getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.format("%d/%d", index + 1, size()));
		
		layStatus.setVisibility(View.VISIBLE);
        viewStatus.setVisibility(View.VISIBLE);
		txtStatus.setText(getStatus(index).getText());

        if (Build.VERSION.SDK_INT >= 19) {
            layToolbar.setPadding(0, layToolbar.getPaddingTop() + SystemBarUtils.getStatusBarHeight(this), 0, 0);

            if (SystemBarUtils.hasNavigationBar(this)) {
                layStatus.setPadding(0, 0, 0, SystemBarUtils.getNavigationBarHeight(this));
            }
        }

        getToolbar().setBackgroundColor(Color.TRANSPARENT);
	}

    @Override
    protected int configTheme() {
        return R.style.AppTheme_Pics;
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt("index", index);
		outState.putSerializable("bean", mBean);
	}
	
	private int size() {
		return mBean.getList().size();
	}
	
	private PicUrls getPicture(int index) {
		return mBean.getList().get(index).getPhoto();
	}

    public PicUrls getCurrent() {
        return getPicture(viewPager.getCurrentItem());
    }
	
	private StatusContent getStatus(int index) {
		return mBean.getList().get(index).getStatus();
	}
	
	void launchStatus(View v) {
        TimelineCommentFragment.launch(this, getStatus(index));
	}
	
	protected Fragment newFragment(int position) {
		return PictureFragment.newInstance(getPicture(position));
	}
	
	class MyViewPagerAdapter extends FragmentPagerAdapter {

		public MyViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = getFragmentManager().findFragmentByTag(makeFragmentName(position));
			if (fragment == null) {
				fragment = newFragment(position);
			}
			
			return fragment;
		}

		@Override
		public int getCount() {
			return size();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			
			Fragment fragment = getFragmentManager().findFragmentByTag(makeFragmentName(position));
			if (fragment != null)
				mCurTransaction.remove(fragment);
		}

		@Override
		protected String makeFragmentName(int position) {
			return KeyGenerator.generateMD5(getPicture(position).getThumbnail_pic());
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int index) {
		this.index = index;
		
		txtStatus.setText(getStatus(index).getText());
		
		if (getSupportActionBar() != null)
			getSupportActionBar().setTitle(String.format("%d/%d", index + 1, size()));

        PictureFragment fragment = (PictureFragment) myViewPagerAdapter.getItem(index);
        if (fragment != null)
            fragment.onStripTabRequestData();

		Intent data = new Intent();
		data.putExtra("index", viewPager.getCurrentItem());
		setResult(Activity.RESULT_OK, data);
	}

}
