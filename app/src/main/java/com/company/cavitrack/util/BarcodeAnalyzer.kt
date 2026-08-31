package com.company.cavitrack.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer, java.io.Closeable {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    
    // Prevent multiple scans from running concurrently
    private val isProcessing = java.util.concurrent.atomic.AtomicBoolean(false)

    override fun close() {
        scanner.close()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && isProcessing.compareAndSet(false, true)) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            onBarcodeScanned(value)
                            // We return after finding the first barcode
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle any errors here (e.g. log them)
                }
                .addOnCompleteListener {
                    isProcessing.set(false)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
