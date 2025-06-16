package com.maranatha.foodlergic.utils

import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

object ImagePreprocessor {
    fun preprocess(bitmap: Bitmap): TensorBuffer {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val floatArray = FloatArray(tensorImage.buffer.capacity() / 4)
        tensorImage.buffer.asFloatBuffer().get(floatArray)
        for (i in floatArray.indices) {
            floatArray[i] = floatArray[i] / 255.0f
        }

        return TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32).apply {
            loadArray(floatArray)
        }
    }
}
