package com.maranatha.foodlergic.presentation.predict

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.databinding.FragmentPredictCameraBinding
import com.maranatha.foodlergic.ml.BestEffnetv2Model
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

@AndroidEntryPoint
class PredictCameraFragment : Fragment() {

    private lateinit var photoUri: Uri
    private lateinit var bitmap: Bitmap
    private var _binding: FragmentPredictCameraBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AllergyViewModel by viewModels()
    private val predictViewModel: PredictViewModel by viewModels()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                context?.let {
                    bitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, photoUri)
                    binding.imagePreview.setImageBitmap(bitmap)
//                    classifyImage(it, bitmap)
                    logAnalyticsEvent(
                        "image_captured",
                        "image_uri",
                        photoUri.toString()
                    )  // Log event when image is captured
                }
            } else {
                Toast.makeText(requireContext(), "Camera cancelled", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
                logAnalyticsEvent(
                    "camera_permission_granted",
                    "status",
                    "granted"
                )  // Log event when permission is granted
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
                ).show()
                logAnalyticsEvent(
                    "camera_permission_denied",
                    "status",
                    "denied"
                )  // Log event when permission is denied
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPredictCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        viewModel.getUserAllergies()
        observeGetAllergiesFromAPI()
        observeUploadingStatus()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
            logAnalyticsEvent(
                "camera_permission_checked",
                "status",
                "granted"
            )  // Log when permission is checked and granted
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.PredictButton.setOnClickListener {
            val foodName = binding.FoodEditTextLayout.text.toString().trim()
            if (foodName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter food name", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!::bitmap.isInitialized) {
                Toast.makeText(requireContext(), "Please take a photo first", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            classifyImage(requireContext(), bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeGetAllergiesFromAPI() {
        viewModel.userAllergies.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> { /* Show loading state */
                }

                is Resource.Success -> { /* Handle success */
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
                is Resource.Loading -> { /* Show loading state */
                }

                is Resource.Success -> {
                    val prediction = result.data
                    if (prediction != null) {
                        val action = PredictCameraFragmentDirections
                            .actionPredictCameraFragmentToPredictResultFragment(
                                prediction.predictedAllergen,
                                prediction.hasAllergy,
                                photoUri.toString(),
                                backToHome = true
                            )
                        findNavController().navigate(action)
                        logAnalyticsEvent(
                            "prediction_made",
                            "predicted_allergen",
                            prediction.predictedAllergen
                        )
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

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.maranatha.foodlergic.fileprovider",
                photoFile
            )
            cameraResultLauncher.launch(photoUri)
            logAnalyticsEvent(
                "camera_opened",
                "status",
                "opened"
            )  // Log event when camera is opened
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to open camera", Toast.LENGTH_SHORT).show()
            logAnalyticsEvent(
                "camera_open_failed",
                "status",
                "failed"
            )  // Log event if camera fails to open
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context?.getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_", ".jpg", storageDir
        )
    }

    private fun classifyImage(context: Context, bitmap: Bitmap) {
        Log.d("rezon", "classifyImage: ")
        try {
            val labels = context.assets.open("label.txt").bufferedReader().readLines()

            val inputBuffer =
                ImagePreprocessor.preprocess(bitmap) // Panggil metode preprocess sesuai kebutuhan

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

            val model = BestEffnetv2Model.newInstance(context)

            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)

            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            val output = outputFeature0.floatArray
            val maxIdx = output.indexOfFirst { it == output.maxOrNull() }
            val label = maxIdx.takeIf { it != -1 }?.let { labels[it] } ?: "Unknown"


            val engLabel = AllergyLabelUtils.getEnglishLabel(label);
            Log.d("rezon", "label:$engLabel ")

            val isAllergic = viewModel.isAllergic(engLabel)
            Log.d("rezon", "isAllergic: $isAllergic ")


            val inputText = binding.FoodEditTextLayout.text.toString()
            predictViewModel.predictAndSave(context, engLabel, isAllergic, inputText)


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

    // Log Firebase Analytics event
    private fun logAnalyticsEvent(eventName: String, paramKey: String, paramValue: String) {
        val bundle = Bundle().apply {
            putString(paramKey, paramValue)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
