package com.example.dentguard.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.dentguard.R
import com.example.dentguard.databinding.ActivityEditProfileBinding
import java.io.File

class editProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var currentPhotoUri: Uri? = null

    // 处理相机拍照结果
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                binding.profileImage.setImageURI(uri)
            }
        }
    }

    // 处理相册选择结果
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentPhotoUri = uri
                binding.profileImage.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setupToolbar()

        // 设置当前用户信息
        setupCurrentUserInfo()

        // 设置点击事件
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCurrentUserInfo() {
        // 从Intent中获取当前用户信息
        intent.getStringExtra("current_name")?.let { name ->
            binding.nameEditText.setText(name)
        }
        // 如果有当前头像的Uri，设置头像
        intent.getStringExtra("current_avatar")?.let { avatarUri ->
            try {
                binding.profileImage.setImageURI(Uri.parse(avatarUri))
                currentPhotoUri = Uri.parse(avatarUri)
            } catch (e: Exception) {
                binding.profileImage.setImageResource(R.drawable.default_avatar)
            }
        }
    }

    private fun setupClickListeners() {
        // 点击头像或文字时显示选择对话框
        binding.profileImage.setOnClickListener { showImagePickerDialog() }
        binding.changeAvatarText.setOnClickListener { showImagePickerDialog() }

        // 保存按钮点击事件
        binding.saveButton.setOnClickListener {
            val newName = binding.nameEditText.text.toString()
            if (newName.isBlank()) {
                Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 返回结果给DashboardFragment
            val resultIntent = Intent().apply {
                putExtra("new_name", newName)
                putExtra("new_avatar", currentPhotoUri?.toString())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("拍照", "从相册选择")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("选择头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoFromCamera()
                    1 -> chooseFromGallery()
                }
            }
            .show()
    }

    private fun takePhotoFromCamera() {
        val photoFile = File(externalCacheDir, "profile_photo.jpg")
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )
        takePicture.launch(currentPhotoUri)
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }
}