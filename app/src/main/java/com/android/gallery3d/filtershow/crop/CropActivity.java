/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.crop;

import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.android.gallery3d.exif.ExifInterface;
import com.lightmsg.R;

/**
 * Activity for cropping an image.
 */
public class CropActivity extends Activity {
	private static final String LOGTAG = "CropActivity";
	public static final String CROP_ACTION = "com.android.camera.action.CROP";
	private CropExtras mCropExtras = null;
	private LoadBitmapTask mLoadBitmapTask = null;

	private int mOutputX = 0;
	private int mOutputY = 0;
	private Bitmap mOriginalBitmap = null;
	private RectF mOriginalBounds = null;
	private int mOriginalRotation = 0;
	private Uri mSourceUri = null;
	private CropView mCropView = null;
	private View mSaveButton = null;
	private boolean finalIOGuard = false;

	private static final int SELECT_PICTURE = 1; // request code for picker

	private static final int DEFAULT_COMPRESS_QUALITY = 90;
	/**
	 * The maximum bitmap size we allow to be returned through the intent.
	 * Intents have a maximum of 1MB in total size. However, the Bitmap seems to
	 * have some overhead to hit so that we go way below the limit here to make
	 * sure the intent stays below 1MB.We should consider just returning a byte
	 * array instead of a Bitmap instance to avoid overhead.
	 */
	public static final int MAX_BMAP_IN_INTENT = 750000;

	// Flags
	private static final int DO_SET_WALLPAPER = 1;
	private static final int DO_RETURN_DATA = 1 << 1;
	private static final int DO_EXTRA_OUTPUT = 1 << 2;

	private static final int FLAG_CHECK = DO_SET_WALLPAPER | DO_RETURN_DATA | DO_EXTRA_OUTPUT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setResult(RESULT_CANCELED, new Intent());
		mCropExtras = getExtrasFromIntent(intent);
		if (mCropExtras != null && mCropExtras.getShowWhenLocked()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.crop_activity);
		mCropView = (CropView) findViewById(R.id.cropView);
		
		View mCancelButton = (View) findViewById(R.id.discard);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED, new Intent());
				done();
			}
		});
		
		View mSaveButton = (View) findViewById(R.id.save);
		mSaveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startFinishOutput();
			}
		});

		if (intent.getData() != null) {
			mSourceUri = intent.getData();
			startLoadBitmap(mSourceUri);
		} else {
			pickImage();
		}
	}

	public void onCreateOld(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setResult(RESULT_CANCELED, new Intent());
		mCropExtras = getExtrasFromIntent(intent);
		if (mCropExtras != null && mCropExtras.getShowWhenLocked()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}

		setContentView(R.layout.crop_activity_old);
		mCropView = (CropView) findViewById(R.id.cropView);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setCustomView(R.layout.filtershow_actionbar);

			View mSaveButton = actionBar.getCustomView();
			mSaveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					startFinishOutput();
				}
			});
		}
		if (intent.getData() != null) {
			mSourceUri = intent.getData();
			startLoadBitmap(mSourceUri);
		} else {
			pickImage();
		}
	}

	private void enableSave(boolean enable) {
		if (mSaveButton != null) {
			mSaveButton.setEnabled(enable);
		}
	}

	@Override
	protected void onDestroy() {
		if (mLoadBitmapTask != null) {
			mLoadBitmapTask.cancel(false);
		}
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mCropView.configChanged();
	}

	/**
	 * Opens a selector in Gallery to chose an image for use when none was given
	 * in the CROP intent.
	 */
	private void pickImage() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
				SELECT_PICTURE);
	}

	/**
	 * Callback for pickImage().
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
			mSourceUri = data.getData();
			startLoadBitmap(mSourceUri);
		}
	}

	/**
	 * Gets screen size metric.
	 */
	private int getScreenImageSize() {
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		return (int) Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
	}

	/**
	 * Method that loads a bitmap in an async task.
	 */
	private void startLoadBitmap(Uri uri) {
		if (uri != null) {
			enableSave(false);
			final View loading = findViewById(R.id.loading);
			loading.setVisibility(View.VISIBLE);
			mLoadBitmapTask = new LoadBitmapTask();
			mLoadBitmapTask.execute(uri);
		} else {
			cannotLoadImage();
			done();
		}
	}

	/**
	 * Method called on UI thread with loaded bitmap.
	 */
	private void doneLoadBitmap(Bitmap bitmap, RectF bounds, int orientation) {
		final View loading = findViewById(R.id.loading);
		loading.setVisibility(View.GONE);
		mOriginalBitmap = bitmap;
		mOriginalBounds = bounds;
		mOriginalRotation = orientation;
		if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
			RectF imgBounds = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
			mCropView.initialize(bitmap, imgBounds, imgBounds, orientation);
			if (mCropExtras != null) {
				int aspectX = mCropExtras.getAspectX();
				int aspectY = mCropExtras.getAspectY();
				mOutputX = mCropExtras.getOutputX();
				mOutputY = mCropExtras.getOutputY();
				if (mOutputX > 0 && mOutputY > 0) {
					mCropView.applyAspect(mOutputX, mOutputY);

				}
				float spotX = mCropExtras.getSpotlightX();
				float spotY = mCropExtras.getSpotlightY();
				if (spotX > 0 && spotY > 0) {
					mCropView.setWallpaperSpotlight(spotX, spotY);
				}
				if (aspectX > 0 && aspectY > 0) {
					mCropView.applyAspect(aspectX, aspectY);
				}
			}
			enableSave(true);
		} else {
			Log.w(LOGTAG, "could not load image for cropping");
			cannotLoadImage();
			setResult(RESULT_CANCELED, new Intent());
			done();
		}
	}

	/**
	 * Display toast for image loading failure.
	 */
	private void cannotLoadImage() {
		CharSequence text = getString(R.string.cannot_load_image);
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * AsyncTask for loading a bitmap into memory.
	 *
	 * @see #startLoadBitmap(Uri)
	 * @see #doneLoadBitmap(Bitmap)
	 */
	private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
		int mBitmapSize;
		Context mContext;
		Rect mOriginalBounds;
		int mOrientation;

		public LoadBitmapTask() {
			mBitmapSize = getScreenImageSize();
			mContext = getApplicationContext();
			mOriginalBounds = new Rect();
			mOrientation = 0;
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			Uri uri = params[0];
			Bitmap bmap = loadConstrainedBitmap(uri, mContext, mBitmapSize,
					mOriginalBounds, false);
			mOrientation = getMetadataRotation(mContext, uri);
			return bmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			doneLoadBitmap(result, new RectF(mOriginalBounds), mOrientation);
		}
	}

	protected void startFinishOutput() {
		if (finalIOGuard) {
			return;
		} else {
			finalIOGuard = true;
		}
		enableSave(false);
		Uri destinationUri = null;
		int flags = 0;
		if (mOriginalBitmap != null && mCropExtras != null) {
			if (mCropExtras.getExtraOutput() != null) {
				destinationUri = mCropExtras.getExtraOutput();
				if (destinationUri != null) {
					flags |= DO_EXTRA_OUTPUT;
				}
			}
			if (mCropExtras.getSetAsWallpaper()) {
				flags |= DO_SET_WALLPAPER;
			}
			if (mCropExtras.getReturnData()) {
				flags |= DO_RETURN_DATA;
			}
		}
		if (flags == 0) {
			destinationUri = makeAndInsertUri(this, mSourceUri);
			if (destinationUri != null) {
				flags |= DO_EXTRA_OUTPUT;
			}
		}
		if ((flags & FLAG_CHECK) != 0 && mOriginalBitmap != null) {
			RectF photo = new RectF(0, 0, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight());
			RectF crop = getBitmapCrop(photo);
			startBitmapIO(flags, mOriginalBitmap, mSourceUri, destinationUri, crop,
					photo, mOriginalBounds,
					(mCropExtras == null) ? null : mCropExtras.getOutputFormat(), mOriginalRotation);
			return;
		}
		setResult(RESULT_CANCELED, new Intent());
		done();
		return;
	}

	private void startBitmapIO(int flags, Bitmap currentBitmap, Uri sourceUri, Uri destUri,
			RectF cropBounds, RectF photoBounds, RectF currentBitmapBounds, String format,
			int rotation) {
		if (cropBounds == null || photoBounds == null || currentBitmap == null
				|| currentBitmap.getWidth() == 0 || currentBitmap.getHeight() == 0
				|| cropBounds.width() == 0 || cropBounds.height() == 0 || photoBounds.width() == 0
				|| photoBounds.height() == 0) {
			return; // fail fast
		}
		if ((flags & FLAG_CHECK) == 0) {
			return; // no output options
		}
		if ((flags & DO_SET_WALLPAPER) != 0) {
			Toast.makeText(this, R.string.setting_wallpaper, Toast.LENGTH_LONG).show();
		}

		final View loading = findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		BitmapIOTask ioTask = new BitmapIOTask(sourceUri, destUri, format, flags, cropBounds,
				photoBounds, currentBitmapBounds, rotation, mOutputX, mOutputY);
		ioTask.execute(currentBitmap);
	}

	private void doneBitmapIO(boolean success, Intent intent) {
		final View loading = findViewById(R.id.loading);
		loading.setVisibility(View.GONE);
		if (success) {
			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_CANCELED, intent);
		}
		done();
	}

	private class BitmapIOTask extends AsyncTask<Bitmap, Void, Boolean> {

		private final WallpaperManager mWPManager;
		InputStream mInStream = null;
		OutputStream mOutStream = null;
		String mOutputFormat = null;
		Uri mOutUri = null;
		Uri mInUri = null;
		int mFlags = 0;
		RectF mCrop = null;
		RectF mPhoto = null;
		RectF mOrig = null;
		Intent mResultIntent = null;
		int mRotation = 0;

		// Helper to setup input stream
		private void regenerateInputStream() {
			if (mInUri == null) {
				Log.w(LOGTAG, "cannot read original file, no input URI given");
			} else {
				Utils.closeSilently(mInStream);
				try {
					mInStream = getContentResolver().openInputStream(mInUri);
				} catch (FileNotFoundException e) {
					Log.w(LOGTAG, "cannot read file: " + mInUri.toString(), e);
				}
			}
		}

		public BitmapIOTask(Uri sourceUri, Uri destUri, String outputFormat, int flags,
				RectF cropBounds, RectF photoBounds, RectF originalBitmapBounds, int rotation,
				int outputX, int outputY) {
			mOutputFormat = outputFormat;
			mOutStream = null;
			mOutUri = destUri;
			mInUri = sourceUri;
			mFlags = flags;
			mCrop = cropBounds;
			mPhoto = photoBounds;
			mOrig = originalBitmapBounds;
			mWPManager = WallpaperManager.getInstance(getApplicationContext());
			mResultIntent = new Intent();
			mRotation = (rotation < 0) ? -rotation : rotation;
			mRotation %= 360;
			mRotation = 90 * (int) (mRotation / 90);  // now mRotation is a multiple of 90
			mOutputX = outputX;
			mOutputY = outputY;

			if ((flags & DO_EXTRA_OUTPUT) != 0) {
				if (mOutUri == null) {
					Log.w(LOGTAG, "cannot write file, no output URI given");
				} else {
					try {
						mOutStream = getContentResolver().openOutputStream(mOutUri);
					} catch (FileNotFoundException e) {
						Log.w(LOGTAG, "cannot write file: " + mOutUri.toString(), e);
					}
				}
			}

			if ((flags & (DO_EXTRA_OUTPUT | DO_SET_WALLPAPER)) != 0) {
				regenerateInputStream();
			}
		}

		@Override
		protected Boolean doInBackground(Bitmap... params) {
			boolean failure = false;
			Bitmap img = params[0];

			// Set extra for crop bounds
			if (mCrop != null && mPhoto != null && mOrig != null) {
				RectF trueCrop = CropMath.getScaledCropBounds(mCrop, mPhoto, mOrig);
				Matrix m = new Matrix();
				m.setRotate(mRotation);
				m.mapRect(trueCrop);
				if (trueCrop != null) {
					Rect rounded = new Rect();
					trueCrop.roundOut(rounded);
					mResultIntent.putExtra(CropExtras.KEY_CROPPED_RECT, rounded);
				}
			}

			// Find the small cropped bitmap that is returned in the intent
			if ((mFlags & DO_RETURN_DATA) != 0) {
				assert (img != null);
				Bitmap ret = getCroppedImage(img, mCrop, mPhoto);
				if (ret != null) {
					ret = getDownsampledBitmap(ret, MAX_BMAP_IN_INTENT);
				}
				if (ret == null) {
					Log.w(LOGTAG, "could not downsample bitmap to return in data");
					failure = true;
				} else {
					if (mRotation > 0) {
						Matrix m = new Matrix();
						m.setRotate(mRotation);
						Bitmap tmp = Bitmap.createBitmap(ret, 0, 0, ret.getWidth(),
								ret.getHeight(), m, true);
						if (tmp != null) {
							ret = tmp;
						}
					}
					mResultIntent.putExtra(CropExtras.KEY_DATA, ret);
				}
			}

			// Do the large cropped bitmap and/or set the wallpaper
			if ((mFlags & (DO_EXTRA_OUTPUT | DO_SET_WALLPAPER)) != 0 && mInStream != null) {
				// Find crop bounds (scaled to original image size)
				RectF trueCrop = CropMath.getScaledCropBounds(mCrop, mPhoto, mOrig);
				if (trueCrop == null) {
					Log.w(LOGTAG, "cannot find crop for full size image");
					failure = true;
					return false;
				}
				Rect roundedTrueCrop = new Rect();
				trueCrop.roundOut(roundedTrueCrop);

				if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
					Log.w(LOGTAG, "crop has bad values for full size image");
					failure = true;
					return false;
				}

				// Attempt to open a region decoder
				BitmapRegionDecoder decoder = null;
				try {
					decoder = BitmapRegionDecoder.newInstance(mInStream, true);
				} catch (IOException e) {
					Log.w(LOGTAG, "cannot open region decoder for file: " + mInUri.toString(), e);
				}

				Bitmap crop = null;
				if (decoder != null) {
					// Do region decoding to get crop bitmap
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inMutable = true;
					crop = decoder.decodeRegion(roundedTrueCrop, options);
					decoder.recycle();
				}

				if (crop == null) {
					// BitmapRegionDecoder has failed, try to crop in-memory
					regenerateInputStream();
					Bitmap fullSize = null;
					if (mInStream != null) {
						fullSize = BitmapFactory.decodeStream(mInStream);
					}
					if (fullSize != null) {
						crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left,
								roundedTrueCrop.top, roundedTrueCrop.width(),
								roundedTrueCrop.height());
					}
				}

				if (crop == null) {
					Log.w(LOGTAG, "cannot decode file: " + mInUri.toString());
					failure = true;
					return false;
				}
				if (mOutputX > 0 && mOutputY > 0) {
					Matrix m = new Matrix();
					RectF cropRect = new RectF(0, 0, crop.getWidth(), crop.getHeight());
					if (mRotation > 0) {
						m.setRotate(mRotation);
						m.mapRect(cropRect);
					}
					RectF returnRect = new RectF(0, 0, mOutputX, mOutputY);
					m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
					m.preRotate(mRotation);
					Bitmap tmp = Bitmap.createBitmap((int) returnRect.width(),
							(int) returnRect.height(), Bitmap.Config.ARGB_8888);
					if (tmp != null) {
						Canvas c = new Canvas(tmp);
						c.drawBitmap(crop, m, new Paint());
						crop = tmp;
					}
				} else if (mRotation > 0) {
					Matrix m = new Matrix();
					m.setRotate(mRotation);
					Bitmap tmp = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(),
							crop.getHeight(), m, true);
					if (tmp != null) {
						crop = tmp;
					}
				}
				// Get output compression format
				CompressFormat cf =
						convertExtensionToCompressFormat(getFileExtension(mOutputFormat));

				// If we only need to output to a URI, compress straight to file
				if (mFlags == DO_EXTRA_OUTPUT) {
					if (mOutStream == null
							|| !crop.compress(cf, DEFAULT_COMPRESS_QUALITY, mOutStream)) {
						Log.w(LOGTAG, "failed to compress bitmap to file: " + mOutUri.toString());
						failure = true;
					} else {
						mResultIntent.setData(mOutUri);
					}
				} else {
					// Compress to byte array
					ByteArrayOutputStream tmpOut = new ByteArrayOutputStream(2048);
					if (crop.compress(cf, DEFAULT_COMPRESS_QUALITY, tmpOut)) {

						// If we need to output to a Uri, write compressed
						// bitmap out
						if ((mFlags & DO_EXTRA_OUTPUT) != 0) {
							if (mOutStream == null) {
								Log.w(LOGTAG,
										"failed to compress bitmap to file: " + mOutUri.toString());
								failure = true;
							} else {
								try {
									mOutStream.write(tmpOut.toByteArray());
									mResultIntent.setData(mOutUri);
								} catch (IOException e) {
									Log.w(LOGTAG,
											"failed to compress bitmap to file: "
													+ mOutUri.toString(), e);
									failure = true;
								}
							}
						}

						// If we need to set to the wallpaper, set it
						if ((mFlags & DO_SET_WALLPAPER) != 0 && mWPManager != null) {
							if (mWPManager == null) {
								Log.w(LOGTAG, "no wallpaper manager");
								failure = true;
							} else {
								try {
									mWPManager.setStream(new ByteArrayInputStream(tmpOut
											.toByteArray()));
								} catch (IOException e) {
									Log.w(LOGTAG, "cannot write stream to wallpaper", e);
									failure = true;
								}
							}
						}
					} else {
						Log.w(LOGTAG, "cannot compress bitmap");
						failure = true;
					}
				}
			}
			return !failure; // True if any of the operations failed
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Utils.closeSilently(mOutStream);
			Utils.closeSilently(mInStream);
			doneBitmapIO(result.booleanValue(), mResultIntent);
		}

	}

	private void done() {
		finish();
	}

	protected static Bitmap getCroppedImage(Bitmap image, RectF cropBounds, RectF photoBounds) {
		RectF imageBounds = new RectF(0, 0, image.getWidth(), image.getHeight());
		RectF crop = CropMath.getScaledCropBounds(cropBounds, photoBounds, imageBounds);
		if (crop == null) {
			return null;
		}
		Rect intCrop = new Rect();
		crop.roundOut(intCrop);
		return Bitmap.createBitmap(image, intCrop.left, intCrop.top, intCrop.width(),
				intCrop.height());
	}

	protected static Bitmap getDownsampledBitmap(Bitmap image, int max_size) {
		if (image == null || image.getWidth() == 0 || image.getHeight() == 0 || max_size < 16) {
			throw new IllegalArgumentException("Bad argument to getDownsampledBitmap()");
		}
		int shifts = 0;
		int size = CropMath.getBitmapSize(image);
		while (size > max_size) {
			shifts++;
			size /= 4;
		}
		Bitmap ret = Bitmap.createScaledBitmap(image, image.getWidth() >> shifts,
				image.getHeight() >> shifts, true);
		if (ret == null) {
			return null;
		}
		// Handle edge case for rounding.
		if (CropMath.getBitmapSize(ret) > max_size) {
			return Bitmap.createScaledBitmap(ret, ret.getWidth() >> 1, ret.getHeight() >> 1, true);
		}
		return ret;
	}

	/**
	 * Gets the crop extras from the intent, or null if none exist.
	 */
	protected static CropExtras getExtrasFromIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			return new CropExtras(extras.getInt(CropExtras.KEY_OUTPUT_X, 0),
					extras.getInt(CropExtras.KEY_OUTPUT_Y, 0),
					extras.getBoolean(CropExtras.KEY_SCALE, true) &&
					extras.getBoolean(CropExtras.KEY_SCALE_UP_IF_NEEDED, false),
					extras.getInt(CropExtras.KEY_ASPECT_X, 0),
					extras.getInt(CropExtras.KEY_ASPECT_Y, 0),
					extras.getBoolean(CropExtras.KEY_SET_AS_WALLPAPER, false),
					extras.getBoolean(CropExtras.KEY_RETURN_DATA, false),
					(Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT),
					extras.getString(CropExtras.KEY_OUTPUT_FORMAT),
					extras.getBoolean(CropExtras.KEY_SHOW_WHEN_LOCKED, false),
					extras.getFloat(CropExtras.KEY_SPOTLIGHT_X),
					extras.getFloat(CropExtras.KEY_SPOTLIGHT_Y));
		}
		return null;
	}

	protected static CompressFormat convertExtensionToCompressFormat(String extension) {
		return extension.equals("png") ? CompressFormat.PNG : CompressFormat.JPEG;
	}

	protected static String getFileExtension(String requestFormat) {
		String outputFormat = (requestFormat == null)
				? "jpg"
						: requestFormat;
		outputFormat = outputFormat.toLowerCase();
		return (outputFormat.equals("png") || outputFormat.equals("gif"))
				? "png" // We don't support gif compression.
						: "jpg";
	}

	private RectF getBitmapCrop(RectF imageBounds) {
		RectF crop = mCropView.getCrop();
		RectF photo = mCropView.getPhoto();
		if (crop == null || photo == null) {
			Log.w(LOGTAG, "could not get crop");
			return null;
		}
		RectF scaledCrop = CropMath.getScaledCropBounds(crop, photo, imageBounds);
		return scaledCrop;
	}


	/*
	 * 
	 * For compile start
	 * 
	 */
	
	/*
	 * SaveImage start
	 */

	private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss";
	public static final String DEFAULT_SAVE_DIRECTORY = "EditedOnlinePhotos";

	public static Uri makeAndInsertUri(Context context, Uri sourceUri) {
		long time = System.currentTimeMillis();
		String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(time));
		File saveDirectory = getFinalSaveDirectory(context, sourceUri);
		File file = new File(saveDirectory, filename  + ".JPG");
		return linkNewFileToUri(context, sourceUri, file, time, false);
	}

	private static File getSaveDirectory(Context context, Uri sourceUri) {
		File file = getLocalFileFromUri(context, sourceUri);
		if (file != null) {
			return file.getParentFile();
		} else {
			return null;
		}
	}

	public static File getFinalSaveDirectory(Context context, Uri sourceUri) {
		File saveDirectory = getSaveDirectory(context, sourceUri);
		if ((saveDirectory == null) || !saveDirectory.canWrite()) {
			saveDirectory = new File(Environment.getExternalStorageDirectory(),
					DEFAULT_SAVE_DIRECTORY);
		}
		// Create the directory if it doesn't exist
		if (!saveDirectory.exists())
			saveDirectory.mkdirs();
		return saveDirectory;
	}

	/**
	 * If the <code>sourceUri</code> is a local content Uri, update the
	 * <code>sourceUri</code> to point to the <code>file</code>.
	 * At the same time, the old file <code>sourceUri</code> used to point to
	 * will be removed if it is local.
	 * If the <code>sourceUri</code> is not a local content Uri, then the
	 * <code>file</code> will be inserted as a new content Uri.
	 * @return the final Uri referring to the <code>file</code>.
	 */
	public static Uri linkNewFileToUri(Context context, Uri sourceUri,
			File file, long time, boolean deleteOriginal) {
		File oldSelectedFile = getLocalFileFromUri(context, sourceUri);
		final ContentValues values = getContentValues(context, sourceUri, file, time);

		Uri result = sourceUri;

		// In the case of incoming Uri is just a local file Uri (like a cached
		// file), we can't just update the Uri. We have to create a new Uri.
		boolean fileUri = isFileUri(sourceUri);

		if (fileUri || oldSelectedFile == null || !deleteOriginal) {
			result = context.getContentResolver().insert(
					Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			context.getContentResolver().update(sourceUri, values, null, null);
			if (oldSelectedFile.exists()) {
				oldSelectedFile.delete();
			}
		}
		return result;
	}

	public interface ContentResolverQueryCallback {
		void onCursorResult(Cursor cursor);
	}

	private static void querySourceFromContentResolver(
			ContentResolver contentResolver, Uri sourceUri, String[] projection,
			ContentResolverQueryCallback callback) {
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(sourceUri, projection, null, null,
					null);
			if ((cursor != null) && cursor.moveToNext()) {
				callback.onCursorResult(cursor);
			}
		} catch (Exception e) {
			// Ignore error for lacking the data column from the source.
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static void querySource(Context context, Uri sourceUri, String[] projection,
			ContentResolverQueryCallback callback) {
		ContentResolver contentResolver = context.getContentResolver();
		querySourceFromContentResolver(contentResolver, sourceUri, projection, callback);
	}

	/**
	 * Construct a File object based on the srcUri.
	 * @return The file object. Return null if srcUri is invalid or not a local
	 * file.
	 */
	private static File getLocalFileFromUri(Context context, Uri srcUri) {
		if (srcUri == null) {
			Log.e(LOGTAG, "srcUri is null.");
			return null;
		}

		String scheme = srcUri.getScheme();
		if (scheme == null) {
			Log.e(LOGTAG, "scheme is null.");
			return null;
		}

		final File[] file = new File[1];
		// sourceUri can be a file path or a content Uri, it need to be handled
		// differently.
		if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
			if (srcUri.getAuthority().equals(MediaStore.AUTHORITY)) {
				querySource(context, srcUri, new String[] {
						ImageColumns.DATA
				},
				new ContentResolverQueryCallback() {

					@Override
					public void onCursorResult(Cursor cursor) {
						file[0] = new File(cursor.getString(0));
					}
				});
			}
		} else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
			file[0] = new File(srcUri.getPath());
		}
		return file[0];
	}

	private static ContentValues getContentValues(Context context, Uri sourceUri,
			File file, long time) {
		final ContentValues values = new ContentValues();

		time /= 1000;
		values.put(Images.Media.TITLE, file.getName());
		values.put(Images.Media.DISPLAY_NAME, file.getName());
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATE_TAKEN, time);
		values.put(Images.Media.DATE_MODIFIED, time);
		values.put(Images.Media.DATE_ADDED, time);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, file.getAbsolutePath());
		values.put(Images.Media.SIZE, file.length());
		// This is a workaround to trigger the MediaProvider to re-generate the
		// thumbnail.
		values.put(Images.Media.MINI_THUMB_MAGIC, 0);

		final String[] projection = new String[] {
				ImageColumns.DATE_TAKEN,
				ImageColumns.LATITUDE, ImageColumns.LONGITUDE,
		};

		querySource(context, sourceUri, projection,
				new ContentResolverQueryCallback() {

			@Override
			public void onCursorResult(Cursor cursor) {
				values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));

				double latitude = cursor.getDouble(1);
				double longitude = cursor.getDouble(2);
				// TODO: Change || to && after the default location
				// issue is fixed.
				if ((latitude != 0f) || (longitude != 0f)) {
					values.put(Images.Media.LATITUDE, latitude);
					values.put(Images.Media.LONGITUDE, longitude);
				}
			}
		});
		return values;
	}

	/**
	 * @param sourceUri
	 * @return true if the sourceUri is a local file Uri.
	 */
	private static boolean isFileUri(Uri sourceUri) {
		String scheme = sourceUri.getScheme();
		if (scheme != null && scheme.equals(ContentResolver.SCHEME_FILE)) {
			return true;
		}
		return false;
	}
	
	/*
	 * SaveImage end
	 */
	
	/*
	 * ImageLoader start
	 */
	
    /**
     * Loads a bitmap at a given URI that is downsampled so that both sides are
     * smaller than maxSideLength. The Bitmap's original dimensions are stored
     * in the rect originalBounds.
     *
     * @param uri URI of image to open.
     * @param context context whose ContentResolver to use.
     * @param maxSideLength max side length of returned bitmap.
     * @param originalBounds If not null, set to the actual bounds of the stored bitmap.
     * @param useMin use min or max side of the original image
     * @return downsampled bitmap or null if this operation failed.
     */
    public static Bitmap loadConstrainedBitmap(Uri uri, Context context, int maxSideLength,
            Rect originalBounds, boolean useMin) {
        if (maxSideLength <= 0 || uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        // Get width and height of stored bitmap
        Rect storedBounds = loadBitmapBounds(context, uri);
        if (originalBounds != null) {
            originalBounds.set(storedBounds);
        }
        int w = storedBounds.width();
        int h = storedBounds.height();

        // If bitmap cannot be decoded, return null
        if (w <= 0 || h <= 0) {
            return null;
        }

        // Find best downsampling size
        int imageSide = 0;
        if (useMin) {
            imageSide = Math.min(w, h);
        } else {
            imageSide = Math.max(w, h);
        }
        int sampleSize = 1;
        while (imageSide > maxSideLength) {
            imageSide >>>= 1;
            sampleSize <<= 1;
        }

        // Make sure sample size is reasonable
        if (sampleSize <= 0 ||
                0 >= (int) (Math.min(w, h) / sampleSize)) {
            return null;
        }
        return loadDownsampledBitmap(context, uri, sampleSize);
    }
    
    /**
     * Returns the bounds of the bitmap stored at a given Url.
     */
    public static Rect loadBitmapBounds(Context context, Uri uri) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        loadBitmap(context, uri, o);
        return new Rect(0, 0, o.outWidth, o.outHeight);
    }
    
    /**
     * Loads a bitmap that has been downsampled using sampleSize from a given url.
     */
    public static Bitmap loadDownsampledBitmap(Context context, Uri uri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inSampleSize = sampleSize;
        return loadBitmap(context, uri, options);
    }
    
    /**
     * Returns the bitmap from the given uri loaded using the given options.
     * Returns null on failure.
     */
    public static Bitmap loadBitmap(Context context, Uri uri, BitmapFactory.Options o) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is, null, o);
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "FileNotFoundException for " + uri, e);
        } finally {
            Utils.closeSilently(is);
        }
        return null;
    }
    
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    
    public static final int ORI_NORMAL = ExifInterface.Orientation.TOP_LEFT;
    public static final int ORI_ROTATE_90 = ExifInterface.Orientation.RIGHT_TOP;
    public static final int ORI_ROTATE_180 = ExifInterface.Orientation.BOTTOM_LEFT;
    public static final int ORI_ROTATE_270 = ExifInterface.Orientation.RIGHT_BOTTOM;
    public static final int ORI_FLIP_HOR = ExifInterface.Orientation.TOP_RIGHT;
    public static final int ORI_FLIP_VERT = ExifInterface.Orientation.BOTTOM_RIGHT;
    public static final int ORI_TRANSPOSE = ExifInterface.Orientation.LEFT_TOP;
    public static final int ORI_TRANSVERSE = ExifInterface.Orientation.LEFT_BOTTOM;
    
    /**
     * Returns the rotation of image at the given URI as one of 0, 90, 180,
     * 270.  Defaults to 0.
     */
    public static int getMetadataRotation(Context context, Uri uri) {
        int orientation = getMetadataOrientation(context, uri);
        switch(orientation) {
            case ORI_ROTATE_90:
                return 90;
            case ORI_ROTATE_180:
                return 180;
            case ORI_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }
    
    /**
     * Returns the image's orientation flag.  Defaults to ORI_NORMAL if no valid
     * orientation was found.
     */
    public static int getMetadataOrientation(Context context, Uri uri) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getOrientation");
        }

        // First try to find orientation data in Gallery's ContentProvider.
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                    null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                int ori = cursor.getInt(0);
                switch (ori) {
                    case 90:
                        return ORI_ROTATE_90;
                    case 270:
                        return ORI_ROTATE_270;
                    case 180:
                        return ORI_ROTATE_180;
                    default:
                        return ORI_NORMAL;
                }
            }
        } catch (SQLiteException e) {
            // Do nothing
        } catch (IllegalArgumentException e) {
            // Do nothing
        } catch (IllegalStateException e) {
            // Do nothing
        } finally {
            Utils.closeSilently(cursor);
        }
        ExifInterface exif = new ExifInterface();
        InputStream is = null;
        // Fall back to checking EXIF tags in file or input stream.
        try {
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                String mimeType = getMimeType(uri);
                if (!JPEG_MIME_TYPE.equals(mimeType)) {
                    return ORI_NORMAL;
                }
                String path = uri.getPath();
                exif.readExif(path);
            } else {
                is = context.getContentResolver().openInputStream(uri);
                exif.readExif(is);
            }
            return parseExif(exif);
        } catch (IOException e) {
            Log.w(LOGTAG, "Failed to read EXIF orientation", e);
        } catch (NullPointerException e) {
            Log.w(LOGTAG, "Invalid EXIF data", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.w(LOGTAG, "Failed to close InputStream", e);
            }
        }
        return ORI_NORMAL;
    }
    
    private static int parseExif(ExifInterface exif){
        Integer tagval = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (tagval != null) {
            int orientation = tagval;
            switch(orientation) {
                case ORI_NORMAL:
                case ORI_ROTATE_90:
                case ORI_ROTATE_180:
                case ORI_ROTATE_270:
                case ORI_FLIP_HOR:
                case ORI_FLIP_VERT:
                case ORI_TRANSPOSE:
                case ORI_TRANSVERSE:
                    return orientation;
                default:
                    return ORI_NORMAL;
            }
        }
        return ORI_NORMAL;
    }
    
    /**
     * Returns the Mime type for a Url.  Safe to use with Urls that do not
     * come from Gallery's content provider.
     */
    public static String getMimeType(Uri src) {
        String postfix = MimeTypeMap.getFileExtensionFromUrl(src.toString());
        String ret = null;
        if (postfix != null) {
            ret = MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix);
        }
        return ret;
    }
    
    /*
	 * ImageLoader end
	 */
}
