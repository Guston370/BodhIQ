package com.mit.bodhiq.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.mit.bodhiq.databinding.FragmentScanReportBinding;
import com.mit.bodhiq.ui.TextResultActivity;
import com.mit.bodhiq.utils.TextRecognitionService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment for scanning medical reports using CameraX
 */
@AndroidEntryPoint
public class ScanReportFragment extends Fragment {
    
    private FragmentScanReportBinding binding;
    private ImageCapture imageCapture;
    private TextRecognitionService textRecognitionService;
    private CompositeDisposable disposables;
    private boolean isFlashOn = false;
    
    // Activity result launchers
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        disposables = new CompositeDisposable();
        textRecognitionService = new TextRecognitionService(requireContext());
        
        setupActivityResultLaunchers();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScanReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupClickListeners();
        checkCameraPermissionAndStart();
    }
    
    private void setupActivityResultLaunchers() {
        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        processImageFromGallery(imageUri);
                    }
                }
            }
        );
    }
    
    private void setupClickListeners() {
        binding.btnCapture.setOnClickListener(v -> capturePhoto());
        binding.btnGallery.setOnClickListener(v -> openGallery());
        binding.btnFlash.setOnClickListener(v -> toggleFlash());
    }
    
    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(requireContext());
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(getContext(), "Error starting camera: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }
    
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();
        
        imageCapture = new ImageCapture.Builder().build();
        
        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
        
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Camera binding failed: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void capturePhoto() {
        if (imageCapture == null) {
            return;
        }
        
        // Create output file
        File photoFile = new File(requireContext().getExternalFilesDir(null), 
            "report_" + System.currentTimeMillis() + ".jpg");
        
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile)
            .build();
        
        showParentProcessing("Capturing image...");
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    Uri imageUri = Uri.fromFile(photoFile);
                    processImageFromCamera(imageUri);
                }
                
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    hideParentProcessing();
                    Toast.makeText(getContext(), "Photo capture failed: " + exception.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryLauncher.launch(Intent.createChooser(intent, "Select Medical Report"));
    }
    
    private void toggleFlash() {
        isFlashOn = !isFlashOn;
        binding.btnFlash.setImageResource(isFlashOn ? 
            com.mit.bodhiq.R.drawable.ic_flash_on : com.mit.bodhiq.R.drawable.ic_flash_off);
        
        // TODO: Implement flash toggle with CameraX
        Toast.makeText(getContext(), isFlashOn ? "Flash On" : "Flash Off", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void processImageFromCamera(Uri imageUri) {
        showParentProcessing("Extracting text...");
        
        disposables.add(
            textRecognitionService.extractTextFromImage(imageUri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    extractedText -> {
                        hideParentProcessing();
                        navigateToTextResult(imageUri, extractedText);
                    },
                    error -> {
                        hideParentProcessing();
                        Toast.makeText(getContext(), "Text extraction failed: " + error.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                )
        );
    }
    
    private void processImageFromGallery(Uri imageUri) {
        showParentProcessing("Extracting text...");
        
        disposables.add(
            textRecognitionService.extractTextFromImage(imageUri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    extractedText -> {
                        hideParentProcessing();
                        navigateToTextResult(imageUri, extractedText);
                    },
                    error -> {
                        hideParentProcessing();
                        Toast.makeText(getContext(), "Text extraction failed: " + error.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                )
        );
    }
    
    private void navigateToTextResult(Uri imageUri, String extractedText) {
        Intent intent = new Intent(getContext(), TextResultActivity.class);
        intent.putExtra("image_uri", imageUri.toString());
        intent.putExtra("extracted_text", extractedText);
        startActivity(intent);
    }
    
    private void showParentProcessing(String message) {
        if (getParentFragment() instanceof ReportsFragment) {
            ((ReportsFragment) getParentFragment()).showProcessing(message);
        }
    }
    
    private void hideParentProcessing() {
        if (getParentFragment() instanceof ReportsFragment) {
            ((ReportsFragment) getParentFragment()).hideProcessing();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposables != null) {
            disposables.clear();
        }
        if (textRecognitionService != null) {
            textRecognitionService.cleanup();
        }
        binding = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
    }
}