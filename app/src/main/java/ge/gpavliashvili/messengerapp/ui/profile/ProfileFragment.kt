package ge.gpavliashvili.messenger.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.databinding.FragmentProfileBinding
import ge.gpavliashvili.messenger.utils.Constants
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.uploadProfileImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupClickListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.cvProfileImage.setOnClickListener {
            openImagePicker()
        }
        
        binding.tvChangePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                val nickname = binding.etNickname.text.toString().trim()
                val profession = binding.etProfession.text.toString().trim()
                viewModel.updateProfile(nickname, profession)
            }
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.apply {
                etNickname.setText(user.nickname)
                etProfession.setText(user.profession)
                etEmail.setText(user.email)
                
                // Load profile image
                if (user.profileImageUrl.isNotEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.ic_person)
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showToast(getString(R.string.profile_updated))
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.profile_update_failed))
            }
        }

        viewModel.imageUploadResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showToast(getString(R.string.image_uploaded))
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.image_upload_failed))
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
                binding.btnSave.isEnabled = false
                binding.btnLogout.isEnabled = false
            } else {
                binding.progressBar.hide()
                binding.btnSave.isEnabled = true
                binding.btnLogout.isEnabled = true
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        val chooser = Intent.createChooser(intent, getString(R.string.select_image))
        imagePickerLauncher.launch(chooser)
    }

    private fun validateForm(): Boolean {
        val nickname = binding.etNickname.text.toString().trim()
        val profession = binding.etProfession.text.toString().trim()

        // Reset errors
        binding.tilNickname.error = null
        binding.tilProfession.error = null

        var isValid = true

        if (nickname.isEmpty()) {
            binding.tilNickname.error = getString(R.string.error_empty_nickname)
            isValid = false
        } else if (!viewModel.isValidNickname(nickname)) {
            binding.tilNickname.error = getString(R.string.error_short_nickname)
            isValid = false
        }

        if (profession.isEmpty()) {
            binding.tilProfession.error = getString(R.string.error_empty_profession)
            isValid = false
        } else if (!viewModel.isValidProfession(profession)) {
            binding.tilProfession.error = getString(R.string.error_empty_profession)
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 