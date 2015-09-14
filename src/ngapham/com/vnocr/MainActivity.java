package ngapham.com.vnocr;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// load main Fragment
		if (savedInstanceState == null) {
			FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
			fTransaction.add(R.id.container, new MainFragment());
			fTransaction.commit();
		}
		
		// load advertisements
		loadAds();
	}

	private void loadAds() {

		//Locate the Banner Ad in activity_main.xml
		AdView adView = (AdView) findViewById(R.id.adView);

		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder().build();

		// Load ads into Banner Ads
		adView.loadAd(adRequest);

		// Set Smart Banner
//		adView.setAdSize(AdSize.SMART_BANNER);
	}
}
