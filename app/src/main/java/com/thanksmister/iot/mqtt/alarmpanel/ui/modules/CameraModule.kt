/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.*
import android.hardware.camera2.CameraAccessException.CAMERA_ERROR
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.thanksmister.iot.mqtt.alarmpanel.R
import timber.log.Timber
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager



/**
 * Module to take photo and email when alarm is disabled if camera available.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraModule(base: Context?, private var backgroundHandler: Handler, private var callback: CallbackListener?) : ContextWrapper(base), LifecycleObserver {

    private var mImageReader: ImageReader? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var hasCamera:Boolean = false
    private var rotation: Float = 0F;

    interface CallbackListener {
        fun onCameraComplete(bitmap: Bitmap)
        fun onCameraException(message: String)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @SuppressLint("MissingPermission")
    fun onStart() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getFrontFacingCameraId(manager)
        if (cameraId == null) {
            Timber.e("No cameras found")
            hasCamera = false
            return
        }
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMAGES)
        mImageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
        try {
            manager.openCamera(cameraId, mStateCallback, backgroundHandler)
            hasCamera = true;
        } catch (cae: Exception) {
            Timber.d("Camera access exception"  + cae)
            if(callback != null) {
                callback!!.onCameraException("Camera access exception" + cae);
            }
            hasCamera = false;
        }
    }

    private fun getFrontFacingCameraId(manager: CameraManager): String? {
        try {
            val camIds = manager.cameraIdList
            for (cameraId in camIds) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId
            }
        } catch (e: CameraAccessException) {
            Timber.e("Cam access exception getting ids: " + e.message)
        }
        return null
    }

    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        val imageBuffer = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuffer.remaining())
        imageBuffer.get(imageBytes)
        image.close()
        val bitmap = getBitmapFromByteArray(imageBytes)
        callback?.onCameraComplete(bitmap);
    }

    fun takePicture(rotation: Float) {
        this.rotation = rotation
        if(hasCamera) {
            mCameraDevice?.createCaptureSession(
                    arrayListOf(mImageReader?.surface),
                    mSessionCallback,
                    null)
        }
    }

    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        //For some reason the bitmap is rotated the incorrect way
        matrix.postRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun triggerImageCapture() {
        if(hasCamera) {
            val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(mImageReader!!.surface)
            captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            captureBuilder?.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
            mCaptureSession?.capture(captureBuilder?.build(), mCaptureCallback, null)
        }
    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            Timber.d("Partial result")
        }

        override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            Timber.d("Capture session failed")
            if(callback != null) {
                callback!!.onCameraException(getString(R.string.text_camera_failed_session));
            }
        }

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            session?.close()
            mCaptureSession = null
            Timber.d("Capture session closed")
        }
    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession?) {
            Timber.d("Failed to configure camera")
            callback!!.onCameraException(getString(R.string.text_camera_failed_configuration));
        }

        override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
            if (mCameraDevice == null) {
                return
            }
            mCaptureSession = cameraCaptureSession
            triggerImageCapture()
        }
    }

    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onError(cameraDevice: CameraDevice, code: Int) {
            Timber.d("Camera device error, closing")
            cameraDevice.close()
            callback!!.onCameraException(getString(R.string.text_error_camera_device));
        }

        override fun onOpened(cameraDevice: CameraDevice) {
            Timber.d("Opened camera.")
            mCameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Timber.d("Camera disconnected, closing")
            cameraDevice.close()
        }

        override fun onClosed(camera: CameraDevice) {
            Timber.d("Closed camera, releasing")
            mCameraDevice = null
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        mCameraDevice?.close()
    }

    companion object InstanceHolder {
        val IMAGE_WIDTH = 640
        val IMAGE_HEIGHT = 480
        val MAX_IMAGES = 1
    }
}