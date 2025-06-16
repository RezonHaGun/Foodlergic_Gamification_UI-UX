package com.maranatha.foodlergic.presentation.predict

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.databinding.FragmentPredictBinding
import com.maranatha.foodlergic.ml.BestEffnetv2Model
import com.maranatha.foodlergic.ml.BestMobilenetv2Model
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.PredictViewModel
import com.maranatha.foodlergic.utils.AllergyLabelUtils
import com.maranatha.foodlergic.utils.ImagePreprocessor
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PredictFragment : Fragment() {
    private var _binding: FragmentPredictBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var preferences: Preference

    private lateinit var photoUri: Uri
    private lateinit var bitmap: Bitmap
    private val viewModel: AllergyViewModel by viewModels()
    private val predictViewModel: PredictViewModel by viewModels()

    private val predictFragmentArgs: PredictFragmentArgs by navArgs()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] != true
                && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] != true
            ) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.previewImage.setImageURI(photoUri)
                bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, photoUri)

                context?.let {
                    classifyImage(it, bitmap)
                }
            }
        }

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.previewImage.setImageURI(it)
                bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, it)
                photoUri = uri
                context?.let {
                    classifyImage(it, bitmap)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        if (!predictFragmentArgs.isAnonymous) {
            viewModel.getUserAllergies()
            observeGetAllergiesFromAPI()
            observeUploadingStatus()
        }else{
            val localAllergies = preferences.getAllergies()
            viewModel.setLocalAllergies(localAllergies)
        }

        binding.cameraButton.setOnClickListener {
            openCamera()
        }

        binding.galleryButton.setOnClickListener {
            openGallery()
        }
        binding.tvEditAllergy.setOnClickListener {
            findNavController().navigate(PredictFragmentDirections.actionPredictFragmentToManageAllergiesFragment(isAnonymous = true))
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(), "com.maranatha.foodlergic.fileprovider", photoFile
            )
            cameraResultLauncher.launch(photoUri)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryResultLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context?.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_", ".jpg", storageDir
        )
    }

    private fun observeGetAllergiesFromAPI() {
        viewModel.userAllergies.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {

                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeUploadingStatus() {
        predictViewModel.status.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    // Show loading state
                }

                is Resource.Success -> {
                    val prediction = result.data
                    if (prediction != null) {
                        val action = PredictFragmentDirections
                            .actionPredictFragmentToPredictResultFragment(
                                prediction.predictedAllergen,
                                prediction.hasAllergy,
                                photoUri.toString()
                            )
                        findNavController().navigate(action)
                        predictViewModel.clearStatus()
                    }
                }

                is Resource.Error -> {
                    // Show error message
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun classifyImage(context: Context, bitmap: Bitmap) {
        try {

            val labels = context.assets.open("label.txt").bufferedReader().readLines()

            // Preprocessing gambar dan mengubahnya menjadi ByteBuffer yang sesuai untuk input model
            val inputBuffer =
                ImagePreprocessor.preprocess(bitmap) // Panggil metode preprocess sesuai kebutuhan

            // Pastikan input image memiliki ukuran 224x224 (ukuran standar untuk banyak model CNN seperti EfficientNet)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // Mengubah gambar ke dalam ByteBuffer dengan normalisasi (nilai piksel 0-255 dibagi dengan 255)
            val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

            // Menggunakan BestEffnetv2Model
            val model = BestEffnetv2Model.newInstance(context)

            // Membuat tensor untuk input
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

            // Melakukan inference pada model
            val outputs = model.process(inputFeature0)

            // Mendapatkan hasil output model
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            // Menentukan index output dengan nilai tertinggi
            val output = outputFeature0.floatArray
            val maxIdx = output.indexOfFirst { it == output.maxOrNull() }
            val label = maxIdx.takeIf { it != -1 }?.let { labels[it] } ?: "Unknown"
            val engLabel = AllergyLabelUtils.getEnglishLabel(label);
            // Menentukan apakah label termasuk dalam alergi
            val isAllergic = viewModel.isAllergic(engLabel)

            // Navigasi berdasarkan status "anonymous"
            if (predictFragmentArgs.isAnonymous) {
                val action = PredictFragmentDirections
                    .actionPredictFragmentToPredictResultFragment(
                        engLabel,
                        isAllergic,
                        photoUri.toString(),
                        true
                    )
                findNavController().navigate(action)
            } else {
                predictViewModel.predictAndSave(context, engLabel, isAllergic, "")
            }

            // Menutup model setelah selesai
            model.close()

        } catch (e: Exception) {
            Log.d("PredictFragment", "Prediction failed: ${e.message}")
        }
    }

    // Fungsi untuk mengubah bitmap menjadi ByteBuffer dengan normalisasi
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Menormalisasi nilai piksel ke dalam rentang 0 hingga 1
        var pixel = 0
        for (i in intValues.indices) {
            val color = intValues[i]
            val r = (color shr 16 and 0xFF) / 255.0f
            val g = (color shr 8 and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f

            byteBuffer.putFloat(r)  // Red channel
            byteBuffer.putFloat(g)  // Green channel
            byteBuffer.putFloat(b)  // Blue channel
        }
        return byteBuffer
    }
}
