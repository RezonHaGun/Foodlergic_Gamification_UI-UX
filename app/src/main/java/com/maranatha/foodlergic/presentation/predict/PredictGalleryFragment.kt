package com.maranatha.foodlergic.presentation.predict

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.maranatha.foodlergic.databinding.FragmentPredictGalleryBinding
import com.maranatha.foodlergic.ml.BestEffnetv2Model
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.PredictViewModel
import com.maranatha.foodlergic.utils.AllergyLabelUtils
import com.maranatha.foodlergic.utils.ImagePreprocessor
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

@AndroidEntryPoint
class PredictGalleryFragment : Fragment() {

    private lateinit var photoUri: Uri
    private lateinit var bitmap: Bitmap
    private var _binding: FragmentPredictGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AllergyViewModel by viewModels()
    private val predictViewModel: PredictViewModel by viewModels()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                // Handle the image from gallery
                photoUri = uri
                context?.let {
                    bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                    binding.imagePreview.setImageBitmap(bitmap)
//                    classifyImage(it, bitmap)
                    logAnalyticsEvent(
                        "image_selected",
                        "image_uri",
                        photoUri.toString()
                    )  // Log event when image is selected
                }
            }
        }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
            val readStoragePermissionGranted =
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
            val writeStoragePermissionGranted =
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            // Jika semua izin diberikan
            if (cameraPermissionGranted && readStoragePermissionGranted && writeStoragePermissionGranted) {
                Log.d("PermissionStatus", "Izin penyimpanan dan kamera diberikan")
                openGallery()
            } else {
                Log.d("PermissionStatus", "Izin penyimpanan atau kamera ditolak")
                Toast.makeText(
                    requireContext(),
                    "Izin penyimpanan dan kamera diperlukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPredictGalleryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        viewModel.getUserAllergies()
        observeGetAllergiesFromAPI()
        observeUploadingStatus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                galleryPermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            }
        } else {
            // Check for storage and camera permissions
            val cameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            )
            val readStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            // If all permissions are granted
            if (cameraPermission == PackageManager.PERMISSION_GRANTED &&
                readStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                // Request permissions
                galleryPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }

        binding.PredictButton.setOnClickListener {
            val foodName = binding.FoodEditTextLayout.text.toString().trim()
            if (foodName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter food name", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!::bitmap.isInitialized) {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT)
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
                        val action = PredictGalleryFragmentDirections
                            .actionPredictGalleryFragmentToPredictResultFragment(
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
                    Log.d("rezon-dbg", "error: ${result.message}")
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openGallery() {
        galleryResultLauncher.launch("image/*")
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
            Log.d("rezon", "label:$label ")
            val engLabel = AllergyLabelUtils.getEnglishLabel(label);
            // Menentukan apakah label termasuk dalam alergi
            val isAllergic = viewModel.isAllergic(engLabel)
            Log.d("rezon", "isAllergic: $isAllergic ")

            // Menavigasi berdasarkan status "anonymous"
            val inputText = binding.FoodEditTextLayout.text.toString()
            predictViewModel.predictAndSave(context, engLabel, isAllergic, inputText)


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
