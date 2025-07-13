package ge.gpavliashvili.messenger.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.databinding.FragmentRegisterBinding
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                val nickname = binding.etNickname.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val profession = binding.etProfession.text.toString().trim()
                
                viewModel.register(nickname, email, password, profession)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showToast(getString(R.string.registration_successful))
                navigateToMain()
            }.onFailure { exception ->
                val errorMessage = when {
                    exception.message?.contains("nickname", ignoreCase = true) == true -> 
                        getString(R.string.error_nickname_exists)
                    exception.message?.contains("email", ignoreCase = true) == true -> 
                        "This email is already registered"
                    else -> exception.message ?: getString(R.string.error_registration_failed)
                }
                showToast(errorMessage)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
                binding.btnRegister.isEnabled = false
                binding.tvLoginLink.isEnabled = false
            } else {
                binding.progressBar.hide()
                binding.btnRegister.isEnabled = true
                binding.tvLoginLink.isEnabled = true
            }
        }
    }

    private fun validateForm(): Boolean {
        val nickname = binding.etNickname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val profession = binding.etProfession.text.toString().trim()

        // Reset errors
        binding.tilNickname.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilProfession.error = null

        var isValid = true

        if (nickname.isEmpty()) {
            binding.tilNickname.error = getString(R.string.error_empty_nickname)
            isValid = false
        } else if (!viewModel.isValidNickname(nickname)) {
            binding.tilNickname.error = getString(R.string.error_short_nickname)
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!viewModel.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (!viewModel.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_short_password)
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

    private fun navigateToMain() {
        try {
            // Check if we can navigate to main from current destination
            val navController = findNavController()
            val currentDestination = navController.currentDestination?.id
            
            if (currentDestination == R.id.mainFragment) {
                // Already on main fragment, no need to navigate
                return
            }
            
            navController.navigate(R.id.action_register_to_main)
        } catch (e: Exception) {
            // If navigation fails, we might already be on the main screen
            // This can happen when MainActivity auto-navigates logged-in users
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 