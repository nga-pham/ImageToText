package ngapham.com.vnocr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.googlecode.tesseract.android.TessBaseAPI;


public class RecognizeFragment extends Fragment {
	private ProgressDialog dialogLoading;
	// EditText to store result
	private EditText txtResult;
	// Button in menu
	private Button btnBack, btnSaveToClipboard;
	// Result text
	private String recognizedText;
	// Error message in case of failed in recognition
	private String ERROR;
	// TAG
	private static final String TAG = "RecognizeActivity";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.activity_recognize, container, false);
		
		initControls(rootView);

		recognizedText = new String();
		ERROR = null;
		
		//Tao dialog loading
		dialogLoading = ProgressDialog.show(this.getActivity(), "", getResources().getString(R.string.recognize_progressDialog));
		new LoadingTask().execute();
		
		//Back
		btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().getFragmentManager().popBackStackImmediate();
			}
		});
		
		//Save text to clipboard
		btnSaveToClipboard.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (recognizedText != null) {
					ClipboardManager myClipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
					ClipData clipdata = ClipData.newPlainText("text", recognizedText);
					myClipboard.setPrimaryClip(clipdata);
					
					ClipData abc = myClipboard.getPrimaryClip();
					ClipData.Item item = abc.getItemAt(0);
					String text = item.getText().toString();
					Toast toast = Toast.makeText(getActivity(), 
							"Text copied to clipboard", Toast.LENGTH_LONG);
					toast.show();
//					Log.i(TAG, "Text copied to clipboard.");
				}
			}
		});
		/*
		//Save text to file
		btnSaveToFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Lay ten file tu ngay thang
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String currentDateTime = sdf.format(new Date());
				String fileName = currentDateTime + ".txt";
				
				//Ghi file 
				if (!(new File(DATA_PATH + fileName)).exists()) {
					try {
						File file = new File(DATA_PATH + fileName);
						Writer writer = new OutputStreamWriter(
			                       new FileOutputStream(DATA_PATH + fileName), "UTF-8");
			            BufferedWriter bufferedWWriter = new BufferedWriter(writer);
			            bufferedWWriter.write(recognizedText);
			            bufferedWWriter.flush();
			            bufferedWWriter.close();
			            
						Toast toast = Toast.makeText(RecognizeActivity.this, 
								"Text saved to: " + DATA_PATH + fileName, Toast.LENGTH_LONG);
						toast.show();
						Log.i("Recognize Text", "Saved");
					} catch (IOException e) {
						Log.i("Recognize Text", "Cannot create file");
					}
				}
				
			}
		});*/
		
		return rootView;
	}

	private void initControls(View v) {
		//Khoi tao cac instance
		txtResult = (EditText) v.findViewById(R.id.txtResult);
		btnBack = (Button) v.findViewById(R.id.btnBack);
		btnSaveToClipboard = (Button) v.findViewById(R.id.btnSaveToClipboard);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	class LoadingTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			//Lay du lieu da truyen di
			Bundle getData = getArguments();
			String picturePath = getData.getString("picture path");
			String language = getData.getString("language");
			String dataPath = getData.getString("data path");
			//Nhan dang
			doOCR(picturePath, language, dataPath);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// Dismiss dialog
			if(dialogLoading != null){
				dialogLoading.dismiss();
			}
			//Display recognizedText 
			if (recognizedText.length() != 0 || null == ERROR) {
				txtResult.setText(recognizedText);
			} else {
				Toast.makeText(getActivity(), ERROR, Toast.LENGTH_SHORT).show();
			}
//			super.onPostExecute(result);
		}

	}

	//Nhan dang ky tu quang hoc
	public void doOCR(String picturePath, String language, String dataPath)
	{
		if (null == picturePath || null == language || null == dataPath) {
			ERROR = getResources().getString(R.string.toast_null_bitmap);
		} else {
			try {
				Bitmap getWidthHeight = BitmapFactory.decodeFile(picturePath);
				final int reqWidth = getWidthHeight.getWidth();
				final int reqHeight = getWidthHeight.getHeight();
				// Load large image to prevent OutOfMemory exception
				Bitmap bitmap = decodeBitmapFromFile(picturePath, reqWidth, reqHeight);
				// Convert to ARGB_8888, required by tess
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				if (null == bitmap) {
//					Log.i(TAG, "bitmap is null");
					ERROR = getResources().getString(R.string.toast_null_bitmap);
				} else {
//					Log.i(TAG, "bitmap is not null");
					//Start tessBase API
					TessBaseAPI baseApi = new TessBaseAPI();
					baseApi.setDebug(true);
					baseApi.init(dataPath, language); // Specify parent directory: i.e, directory + "/tessdata/eng.traineddata"
					baseApi.setImage(bitmap);

					recognizedText = baseApi.getUTF8Text();
					recognizedText.replace("\n", "");

					baseApi.end();
				}
			} catch (Exception e) {
//				Log.e(TAG, "Error in do OCR: " + e.getMessage(), e);
				ERROR = getResources().getString(R.string.toast_null_bitmap);
			} catch (OutOfMemoryError e) {
				ERROR = getResources().getString(R.string.toast_null_bitmap);
			}
		}
	}
	
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	public static Bitmap decodeBitmapFromFile(String picturePath,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(picturePath, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(picturePath, options);
	}
}
