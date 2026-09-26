package com.zolarm.app.challenge

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.zolarm.app.R
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Wake-up camera challenge:
 * - Detect a face
 * - Require face movement
 * - Require at least one eye open OR mouth open
 * Then capture and complete (parent stops the alarm).
 */
@Composable
fun CameraChallenge(
    modifier: Modifier = Modifier,
    onCaptured: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCaptured by rememberUpdatedState(onCaptured)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        CameraPermissionFallback(modifier) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        return
    }

    var useFrontCamera by remember { mutableStateOf(true) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraBound by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("وجّه وجهك للكاميرا") }
    var faceDetected by remember { mutableStateOf(false) }
    var eyesOrMouthOpen by remember { mutableStateOf(false) }
    var faceMoving by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val captureLock = remember { AtomicBoolean(false) }

    // Tracking for movement
    val lastCx = remember { floatArrayOf(Float.NaN) }
    val lastCy = remember { floatArrayOf(Float.NaN) }
    val moveAccum = remember { floatArrayOf(0f) }
    val openFrames = remember { intArrayOf(0) }
    val moveFrames = remember { intArrayOf(0) }

    fun resetTracking() {
        lastCx[0] = Float.NaN
        lastCy[0] = Float.NaN
        moveAccum[0] = 0f
        openFrames[0] = 0
        moveFrames[0] = 0
        faceDetected = false
        eyesOrMouthOpen = false
        faceMoving = false
        progress = 0f
        captureLock.set(false)
        isCapturing = false
    }

    fun tryCapture() {
        if (!captureLock.compareAndSet(false, true)) return
        if (!cameraBound) {
            captureLock.set(false)
            return
        }
        isCapturing = true
        statusText = "جاري الالتقاط…"
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.close()
                    currentOnCaptured()
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("ZolarmCamera", "capture failed", exception)
                    isCapturing = false
                    captureLock.set(false)
                    statusText = "فشل الالتقاط — أعد المحاولة"
                }
            }
        )
    }

    val detector = remember {
        val opts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        FaceDetection.getClient(opts)
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { detector.close() }
            runCatching { analysisExecutor.shutdown() }
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
        }
    }

    LaunchedEffect(lifecycleOwner, useFrontCamera) {
        cameraBound = false
        resetTracking()
        statusText = "وجّه وجهك للكاميرا"
        try {
            val provider = context.awaitCameraProvider()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                processFrame(
                    imageProxy = imageProxy,
                    detector = detector,
                    lastCx = lastCx,
                    lastCy = lastCy,
                    moveAccum = moveAccum,
                    openFrames = openFrames,
                    moveFrames = moveFrames,
                    onUi = { face, open, moving, prog, msg ->
                        // Post to main for Compose state
                        ContextCompat.getMainExecutor(context).execute {
                            if (isCapturing) return@execute
                            faceDetected = face
                            eyesOrMouthOpen = open
                            faceMoving = moving
                            progress = prog
                            statusText = msg
                            if (face && open && moving && prog >= 1f) {
                                tryCapture()
                            }
                        }
                    }
                )
            }

            val selector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner, selector, preview, imageCapture, analysis
            )
            cameraBound = true
        } catch (t: Throwable) {
            Log.e("ZolarmCamera", "bind failed", t)
            try {
                val provider = context.awaitCameraProvider()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val fallback = if (useFrontCamera) CameraSelector.DEFAULT_BACK_CAMERA
                else CameraSelector.DEFAULT_FRONT_CAMERA
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, fallback, preview, imageCapture)
                useFrontCamera = !useFrontCamera
                cameraBound = true
                statusText = "تبديل الكاميرا — وجّه وجهك"
            } catch (t2: Throwable) {
                statusText = "تعذّر تشغيل الكاميرا"
                cameraBound = false
            }
        }
    }

    val borderColor = when {
        isCapturing -> MaterialTheme.colorScheme.primary
        faceDetected && eyesOrMouthOpen && faceMoving -> Color(0xFFB2FF59)
        faceDetected -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(28.dp))
                .border(3.dp, borderColor, RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.matchParentSize()
            )

            IconButton(
                onClick = {
                    if (!isCapturing) useFrontCamera = !useFrontCamera
                },
                enabled = cameraBound && !isCapturing,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(48.dp)
            ) {
                Icon(Icons.Filled.Cameraswitch, contentDescription = stringResource(R.string.switch_camera))
            }

            Text(
                text = if (useFrontCamera) stringResource(R.string.front_camera)
                else stringResource(R.string.back_camera),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CheckLine("✅ ظهر الوجه", faceDetected)
            CheckLine("✅ حركة الوجه", faceMoving)
            CheckLine("✅ عين مفتوحة أو فم مفتوح", eyesOrMouthOpen)
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { if (!isCapturing) useFrontCamera = !useFrontCamera },
                enabled = cameraBound && !isCapturing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Cameraswitch, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.switch_camera))
            }

            // Manual capture only allowed when conditions met
            Button(
                onClick = { if (faceDetected && eyesOrMouthOpen && faceMoving) tryCapture() },
                enabled = cameraBound && !isCapturing && faceDetected && eyesOrMouthOpen && faceMoving,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(84.dp)
            ) {
                Icon(
                    if (isCapturing) Icons.Filled.Face else Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.capture_photo),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun CheckLine(label: String, ok: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (ok) Color(0xFFB2FF59) else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (ok) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun CameraPermissionFallback(modifier: Modifier, onRequest: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            stringResource(R.string.camera_permission_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRequest) { Text(stringResource(R.string.grant_camera)) }
    }
}

private fun processFrame(
    imageProxy: ImageProxy,
    detector: com.google.mlkit.vision.face.FaceDetector,
    lastCx: FloatArray,
    lastCy: FloatArray,
    moveAccum: FloatArray,
    openFrames: IntArray,
    moveFrames: IntArray,
    onUi: (face: Boolean, open: Boolean, moving: Boolean, progress: Float, msg: String) -> Unit
) {
    val media = imageProxy.image
    if (media == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
    detector.process(image)
        .addOnSuccessListener { faces ->
            try {
                if (faces.isEmpty()) {
                    lastCx[0] = Float.NaN
                    lastCy[0] = Float.NaN
                    moveAccum[0] = 0f
                    openFrames[0] = 0
                    moveFrames[0] = 0
                    onUi(false, false, false, 0f, "لم يُكتشف وجه — انظر للكاميرا")
                    return@addOnSuccessListener
                }
                val face: Face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }!!
                val box = face.boundingBox
                val cx = box.exactCenterX()
                val cy = box.exactCenterY()

                // Movement: accumulate center delta
                var moving = false
                if (!lastCx[0].isNaN()) {
                    val dx = abs(cx - lastCx[0])
                    val dy = abs(cy - lastCy[0])
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist > 8f) {
                        moveAccum[0] += dist
                        moveFrames[0]++
                    }
                }
                lastCx[0] = cx
                lastCy[0] = cy
                // Need sustained movement across frames
                moving = moveAccum[0] > 45f && moveFrames[0] >= 4

                val leftOpen = (face.leftEyeOpenProbability ?: 0f) > 0.5f
                val rightOpen = (face.rightEyeOpenProbability ?: 0f) > 0.5f
                // mouth open approximated via smile probability high OR inverse of both eyes closed
                // ML Kit has smilingProbability; for mouth open we use smile > 0.35 as proxy
                // plus if either eye is clearly open that satisfies the requirement alone
                val mouthOpen = (face.smilingProbability ?: 0f) > 0.35f
                val open = leftOpen || rightOpen || mouthOpen

                if (open) openFrames[0]++ else openFrames[0] = maxOf(0, openFrames[0] - 1)
                val openStable = open && openFrames[0] >= 3

                val progress = when {
                    !openStable && !moving -> 0.15f
                    openStable && !moving -> 0.5f
                    !openStable && moving -> 0.5f
                    openStable && moving -> 1f
                    else -> 0.3f
                }

                val msg = when {
                    openStable && moving -> "تم التحقق — جاري الالتقاط"
                    openStable && !moving -> "حرّك رأسك يميناً أو يساراً"
                    !openStable && moving -> "افتح عيناً واحدة أو افتح فمك"
                    else -> "انظر للكاميرا وحرّك رأسك قليلاً"
                }
                onUi(true, openStable, moving, progress, msg)
            } finally {
                imageProxy.close()
            }
        }
        .addOnFailureListener {
            imageProxy.close()
            onUi(false, false, false, 0f, "تحليل الوجه…")
        }
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try { cont.resume(future.get()) }
                catch (t: Throwable) { cont.resumeWithException(t) }
            },
            ContextCompat.getMainExecutor(this)
        )
    }
