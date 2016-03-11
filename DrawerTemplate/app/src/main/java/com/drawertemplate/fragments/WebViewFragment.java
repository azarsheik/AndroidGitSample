package com.drawertemplate.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.drawertemplate.R;
import com.drawertemplate.utils.AlertDialogUtility;
import com.drawertemplate.utils.Consts;
import com.drawertemplate.utils.NetworkUtility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends Fragment {

    private WebView mWebView;
    private View mProgress;
    private String mWebViewUrl;
    //Flag for indicating whether we still want to redirect the web page on an error or not
    private boolean mRedirectOnError = true;
    private boolean mFailedToLoadWebview = false;

    private static final String TAG = WebViewFragment.class.getSimpleName();

    // Lolipop
    public static final int INPUT_FILE_REQUEST_CODE = 100;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    //Kitkat
    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;


    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment newInstance(String url) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(Consts.AboutConsts.ARG_WEBVIEW_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWebViewUrl = getArguments().getString(Consts.AboutConsts.ARG_WEBVIEW_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWebView = (WebView) view.findViewById(R.id.content_web_view);
        mProgress = view.findViewById(R.id.progress);

        // Handle the webview to load URL
        if (mWebViewUrl != null) {
            // Show the VMob Logo container only for About Us page
            final WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setBuiltInZoomControls(true);
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    // OnPage finished gets called multiple times in older version devices (Pre Lolipop)
                    // when there is an error happens, So handling progress visibility based on the redirect URL
                    if (view.getUrl() != null && (view.getUrl().equalsIgnoreCase(Consts.URLConsts.REDIRECT_URL) ||
                            view.getUrl().equalsIgnoreCase(Consts.URLConsts.EMPTY_PAGE))) {
                        mFailedToLoadWebview = false;
                    }
                    if (!mFailedToLoadWebview) {
                        hideProgress();
                    }
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    mFailedToLoadWebview = true;
                    if (mRedirectOnError) {
                        //Load oliver main page instead of the wrong link
                        mWebView.loadUrl(Consts.URLConsts.REDIRECT_URL);

                        //Don't redirect again
                        mRedirectOnError = false;
                    } else {
                        //We have tried to redirect the web page to the main page, but it failed again: load empty page
                        mWebView.loadUrl(Consts.URLConsts.EMPTY_PAGE);
                    }
                }
            });

            mWebView.setWebChromeClient(new WebChromeClient() {

                // 4.4
                public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                    openFileChooser(uploadMsg);
                }

                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                    openFileChooser(uploadMsg);
                }

                // 5.0
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        WebChromeClient.FileChooserParams fileChooserParams) {
                    if (mFilePathCallback != null) {
                        mFilePathCallback.onReceiveValue(null);
                    }
                    mFilePathCallback = filePathCallback;

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e(TAG, "Unable to create Image File", ex);
                        }

                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("image/*");

                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

                    return true;
                }
            });

            if (NetworkUtility.hasDataOrWifiConnection(getActivity())) {
                showProgress();
                mWebView.loadUrl(mWebViewUrl);
            } else {
                hideProgress();
                AlertDialogUtility.showInternetErrorDialog(getActivity());
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    private void showProgress() {
        mWebView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBack() {
        mWebView.goBack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri[] results = null;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else {
             if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage)
                    return;
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }
}
