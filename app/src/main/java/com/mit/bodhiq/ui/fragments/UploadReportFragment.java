package com.mit.bodhiq.ui.fragments;

import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mit.bodhiq.databinding.FragmentUploadReportBinding;
import com.mit.bodhiq.ui.TextResultActivity;
import com.mit.bodhiq.utils.TextRecognitionService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment for uploading medical reports from gallery
 */
@AndroidEntryPoint
public class UploadReportFragment extends Fragment {
    
    private FragmentUploadReportBinding binding;
    private TextRecognitionService textRecognitionService;
    private CompositeDisposable disposables;
    private Uri selectedImageUri;
    
    // Activity result launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        disposables = new CompositeDisposable();
        textRecognitionService = new TextRecognitionService(requireContext());
        
        setupActivityResultLauncher();
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupClickListeners();
    }
    
    private void setupActivityResultLauncher() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleSelectedImage(imageUri);
                    }
                }
            }
        );
    }
    
    private void setupClickListeners() {
        binding.cardUploadArea.setOnClickListener(v -> openImagePicker());
        binding.btnSelectImage.setOnClickListener(v -> openImagePicker());
        binding.btnSelectDocument.setOnClickListener(v -> openDocumentPicker());
        binding.btnChangeFile.setOnClickListener(v -> openImagePicker());
        binding.btnProcessFile.setOnClickListener(v -> processSelectedImage());
        binding.btnRemoveFile.setOnClickListener(v -> removeSelectedFile());
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Medical Report Image"));
    }
    
    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Medical Report Document"));
    }
    
    private void handleSelectedImage(Uri imageUri) {
        selectedImageUri = imageUri;
        
        // Determine file type
        String fileName = getFileName(imageUri);
        boolean isPdf = fileName != null && fileName.toLowerCase().endsWith(".pdf");
        
        // Update UI based on file type
        if (isPdf) {
            binding.imagePreview.setVisibility(View.GONE);
            binding.iconFileType.setImageResource(com.mit.bodhiq.R.drawable.ic_picture_as_pdf);
        } else {
            binding.imagePreview.setVisibility(View.VISIBLE);
            binding.iconFileType.setImageResource(com.mit.bodhiq.R.drawable.ic_gallery);
            // Load image with Glide
            Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(binding.imagePreview);
        }
        
        // Update UI elements
        binding.iconUpload.setVisibility(View.GONE);
        binding.textUploadTitle.setText("File Selected");
        binding.textUploadSubtitle.setText("Ready to process");
        binding.btnSelectImage.setVisibility(View.GONE);
        binding.btnSelectDocument.setVisibility(View.GONE);
        binding.layoutFileActions.setVisibility(View.VISIBLE);
        
        // Update file info
        binding.textFileName.setText(fileName != null ? fileName : "Selected file");
        binding.textFileSize.setText(getFileSize(imageUri));
    }
    
    private void removeSelectedFile() {
        selectedImageUri = null;
        
        // Reset UI to initial state
        binding.imagePreview.setVisibility(View.GONE);
        binding.iconUpload.setVisibility(View.VISIBLE);
        binding.textUploadTitle.setText("Upload Medical Report");
        binding.textUploadSubtitle.setText("Select an image or document from your device\nSupports JPG, PNG, PDF formats");
        binding.btnSelectImage.setVisibility(View.VISIBLE);
        binding.btnSelectDocument.setVisibility(View.VISIBLE);
        binding.layoutFileActions.setVisibility(View.GONE);
    }
    
    private String getFileName(Uri uri) {
        // Simple implementation - in a real app you'd use ContentResolver
        String path = uri.getPath();
        if (path != null) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return "Unknown file";
    }
    
    private String getFileSize(Uri uri) {
        // Simple implementation - in a real app you'd get actual file size
        return "File size unknown";
    }
    
    private void processSelectedImage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showParentProcessing("Extracting text from image...");
        
        disposables.add(
            textRecognitionService.extractTextFromImage(selectedImageUri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    extractedText -> {
                        hideParentProcessing();
                        navigateToTextResult(selectedImageUri, extractedText);
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