package ngapham.com.vnocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author ngapham
 *
 */
public class MainFragment extends Fragment {

	// controls
	Button btnPickImage, btnChooseLanguage, btnRecognize, btnLogout;
	ImageView imgSource;
	// strings
	private String lang;
	private String picturePath;
	protected String _path;

	protected boolean _taken;

	private ArrayList<String> languageName;
	private ArrayList<String> languageCode;

	// static final string
	private static final String TAG = "MainFragment";
	// Duong dan toi folder trong sdcard
	private static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/ImageToText/";
	private static final int RESULT_LOAD_IMAGE = 1;
	private static final String LANGUAGE_FILE_LANGUAGE = "language";
	private static final String LANGUAGE_FILE_NAME = "name";
	private static final String LANGUAGE_FILE_CODE = "code";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		languageName = new ArrayList<String>();
		languageCode = new ArrayList<String>();
		lang = "eng";	//default
		
		//Dua du lieu vao sdcard
		insertData();
		//Lay ra danh sach language
		readData();
	}

	//Data duoc lay tu assets folder
	protected void insertData() {
		//Tao directory
		String[] paths = new String[] { DATA_PATH, DATA_PATH + getResources().getString(R.string.dataPath_tessFolder) + "/" };
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					//					Log.i(COPY_DATA, "Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					//					Log.i(COPY_DATA, "Created directory " + path + " on sdcard");
				}
			}
		}
		//Copy file
		AssetManager assetManager = getActivity().getAssets();
		String[] listFile;
		try {
			listFile = assetManager.list(getResources().getString(R.string.dataPath_tessFolder));
			for(String file : listFile) {

				if (!(new File(DATA_PATH + getResources().getString(R.string.dataPath_tessFolder) + "/" + file)).exists()) {
					InputStream in = assetManager.open(getResources().getString(R.string.dataPath_tessFolder) + "/" + file);
					OutputStream out = new FileOutputStream(DATA_PATH
							+ getResources().getString(R.string.dataPath_tessFolder) + "/" + file);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					in.close();
					out.close();
					//					Log.i(COPY_DATA, "Copied " + file);
				}
			}
		} catch (IOException e) {
			//			Log.i(COPY_DATA, "Was unable to copy " + lang + " traineddata." + e.toString());
		}

		String file = getResources().getString(R.string.default_picture);
		if (!(new File(DATA_PATH + file)).exists()) {
			try {
				picturePath = DATA_PATH + file;	// default picture to recognize
				InputStream in = assetManager.open(file);
				OutputStream out = new FileOutputStream(DATA_PATH + file);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				//			Log.i(COPY_DATA, "Other files in assets copied: " + file);
			} catch (IOException e) {
				//			Log.i(COPY_DATA, "Was unable to copy other files in assets.");
			}
		}
	}

	//Doc du lieu trong file language.xml
	protected void readData() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;

		try {
			builder = factory.newDocumentBuilder();
			AssetManager assetManager = getActivity().getAssets();
			InputStream in = assetManager.open(getResources().getString(R.string.dataPath_languageFile));
			Document doc = builder.parse(in);

			//Duyet node trong XML
			NodeList nodeList = doc.getElementsByTagName(LANGUAGE_FILE_LANGUAGE);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Element) {			//Kiem tra xem node co phai Element hay khong
					Element language = (Element) node;	//Lay ra tag language
					NodeList listChild = language.getElementsByTagName(LANGUAGE_FILE_NAME);
//					Log.i(TAG, "aaa " + String.valueOf(listChild.item(0).getTextContent()));
					languageName.add(listChild.item(0).getTextContent());
					listChild = language.getElementsByTagName(LANGUAGE_FILE_CODE);
					languageCode.add(listChild.item(0).getTextContent());
				}
			}
		} catch (ParserConfigurationException e1) {
			//			Log.i(READ_DATA, "Cannot create new DocumentBuilder.");
		} catch (IOException e) {
			//			Log.i(READ_DATA, "Cannot open language.xml");
		} catch (SAXException e) {
			//			Log.i(READ_DATA, "Cannot parse XML file");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);

		initControls(rootView);
		// su kien click button
		buttonClicked();
		return rootView;
	}

	private void initControls(View v) {
		btnPickImage = (Button) v.findViewById(R.id.btnPickImage);
		btnChooseLanguage = (Button) v.findViewById(R.id.btnChooseLanguage);
		imgSource = (ImageView) v.findViewById(R.id.imgSource);
		btnRecognize = (Button) v.findViewById(R.id.btnRecognize);
		btnLogout = (Button) v.findViewById(R.id.btnLogout);
		
		picturePath = DATA_PATH + getResources().getString(R.string.default_picture);
	}

	protected void buttonClicked() {
		//Lay anh tu gallery
		btnPickImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_PICK, 
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});

		//Chon language
		btnChooseLanguage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
				alertDialog.setTitle(getResources().getString(R.string.alertDialog_chooseLanguage_title));
				String[] language = languageName.toArray(new String[languageName.size()]);
				alertDialog.setSingleChoiceItems(language, -1, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int item) {
						lang = languageCode.get(item);
						dialog.dismiss();
					}
				});
				alertDialog.create().show();
			}
		});

		//Truyen du lieu sang RecognizeActivity
		btnRecognize.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doOCR(picturePath, lang, DATA_PATH);
			}
		});

		//Thoat
		btnLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
				alertDialog.setTitle(getResources().getString(R.string.alertDialog_logout_title));
				alertDialog.setMessage(getResources().getString(R.string.alertDialog_logout_message));
				alertDialog.setPositiveButton(getResources().getString(R.string.alertDialog_logout_positiveButton), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActivity().finish();
					}
				});
				alertDialog.setNegativeButton(getResources().getString(R.string.alertDialog_logout_negativeButton), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				alertDialog.create().show();
			}
		});
	}

	/**
	 * Nhan anh da chon va resize de hien thi len ImageView
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};
			Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			//picturePath chua duong dan den anh da chon
			picturePath = cursor.getString(columnIndex);
			cursor.close();

			// scaled down to fit the current Window size
			Bitmap scaledBitmap = PictureUtils.getScaledBitmap(getActivity(), picturePath);
			imgSource.setImageBitmap(scaledBitmap); 
		}
	}

	protected void doOCR(String picturePath, String language, String dataPath) {
		if ("".equalsIgnoreCase(picturePath))
		{
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_null_picturePath), Toast.LENGTH_SHORT).show();
		} else if ("".equalsIgnoreCase(language)){
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_null_language), Toast.LENGTH_SHORT).show();
		} else if ("".equalsIgnoreCase(dataPath)){
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_null_dataPath), Toast.LENGTH_SHORT).show();
		} else {
			Bundle args = new Bundle();
			args.putString("picture path", picturePath);
			args.putString("language", language);
			args.putString("data path", dataPath);
			
			RecognizeFragment recogFrament = new RecognizeFragment();
			recogFrament.setArguments(args);
			/*
			FragmentTransaction fTransaction = getActivity().getFragmentManager().beginTransaction();
			fTransaction.addToBackStack(null);
			fTransaction.replace(R.id.container, recogFrament);
			fTransaction.commit();*/
		}
	}
}
