package ngapham.com.vnocr;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;

public class PictureUtils {

	/**
	 * Get the Bitmap from local file that is scaled down to fit the current Window size
	 */
	@SuppressWarnings("deprecation")
	public static Bitmap getScaledBitmap (Activity act, String picturePath) {
		Display mDisplay = act.getWindowManager().getDefaultDisplay();
		float destWidth = mDisplay.getWidth();
		float destHeight = mDisplay.getHeight();
		
		// Read in the dimensions of the image on disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		// scale down to fit the default display
		int inSampleSize = 1;
		if (srcWidth > destWidth || srcHeight > destHeight) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round(srcHeight / destHeight);
			} else {
				inSampleSize = Math.round(srcWidth / destWidth);
			}
		}
		
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		Bitmap bitMap = BitmapFactory.decodeFile(picturePath, options);
		return bitMap;
	}
}
