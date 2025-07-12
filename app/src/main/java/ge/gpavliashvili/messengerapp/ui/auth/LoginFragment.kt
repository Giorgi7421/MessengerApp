package ge.gpavliashvili.messenger.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ge.gpavliashvili.messenger.R
import ge.gpavliashvili.messenger.databinding.FragmentLoginBinding
import ge.gpavliashvili.messenger.utils.hide
import ge.gpavliashvili.messenger.utils.show
import ge.gpavliashvili.messenger.utils.showToast

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        setupObservers()
        
        // Check if user is already logged in - but don't navigate here
        // Let MainActivity handle initial navigation
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateForm()) {
                val nickname = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.loginWithNickname(nickname, password)
            }
        }

        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                showToast(getString(R.string.login_button))
                navigateToMain()
            }.onFailure { exception ->
                showToast(exception.message ?: getString(R.string.error_authentication_failed))
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
                binding.btnLogin.isEnabled = false
                binding.btnSignup.isEnabled = false
            } else {
                binding.progressBar.hide()
                binding.btnLogin.isEnabled = true
                binding.btnSignup.isEnabled = true
            }
        }
    }

    private fun validateForm(): Boolean {
        val nickname = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Reset errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var isValid = true

        if (nickname.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_nickname)
            isValid = false
        } else if (!viewModel.isValidNickname(nickname)) {
            binding.tilEmail.error = getString(R.string.error_short_nickname)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (!viewModel.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_short_password)
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
            
            navController.navigate(R.id.action_login_to_main)
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