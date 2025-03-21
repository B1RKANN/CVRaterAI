package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentRegisterBinding
import com.cvraterai.myapplication.ui.auth.RegisterState
import com.cvraterai.myapplication.ui.auth.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cardSignUp: CardView
    private lateinit var progressBar: ProgressBar
    
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // View binding kullanarak layout inflate et
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View'lara erişim
        etName = binding.etName
        etSurname = binding.etSurname
        etEmail = binding.etEmail
        etPassword = binding.etPassword
        cardSignUp = binding.cardSignUp
        progressBar = binding.progressBar
        
        // Kayıt Ol butonuna tıklama olayını ayarla
        cardSignUp.setOnClickListener {
            validateAndRegister()
        }
        
        // Google ile kayıt
        binding.flGoogle.setOnClickListener {
            // TODO: Google ile kayıt işlemleri
        }
        
        // Giriş Yap butonuna tıklama olayını ayarla
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        
        // RegisterViewModel'i dinle
        observeRegisterState()
    }
    
    private fun observeRegisterState() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    showLoading(true)
                }
                is RegisterState.Success -> {
                    showLoading(false)
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is RegisterState.Error -> {
                    showLoading(false)
                    handleRegistrationError(state.message)
                }
            }
        }
    }
    
    private fun validateAndRegister() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Reset error states
        etName.isActivated = false
        etSurname.isActivated = false
        etEmail.isActivated = false
        etPassword.isActivated = false
        binding.tvNameError.visibility = View.GONE
        binding.tvSurnameError.visibility = View.GONE
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        
        var isValid = true
        
        // Validate name
        if (name.isEmpty()) {
            etName.isActivated = true
            binding.tvNameError.visibility = View.VISIBLE
            isValid = false
        }
        
        // Validate surname
        if (surname.isEmpty()) {
            etSurname.isActivated = true
            binding.tvSurnameError.visibility = View.VISIBLE
            isValid = false
        }
        
        // Validate email
        if (email.isEmpty() || !validateEmail(email)) {
            etEmail.isActivated = true
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        }
        
        // Validate password
        if (password.isEmpty() || password.length < 6) {
            etPassword.isActivated = true
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }
        
        if (isValid) {
            // Show loading indicator
            progressBar.visibility = View.VISIBLE
            cardSignUp.isEnabled = false
            
            // Attempt registration
            viewModel.register(name, surname, email, password)
        }
    }
    
    private fun handleRegistrationError(errorMessage: String) {
        // Hide loading indicator
        progressBar.visibility = View.GONE
        cardSignUp.isEnabled = true
        
        // Reset previous errors
        etEmail.isActivated = false
        etPassword.isActivated = false
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        
        // Show appropriate error based on the error message
        when {
            errorMessage.contains("email", ignoreCase = true) -> {
                etEmail.isActivated = true
                binding.tvEmailError.visibility = View.VISIBLE
            }
            errorMessage.contains("password", ignoreCase = true) -> {
                etPassword.isActivated = true
                binding.tvPasswordError.visibility = View.VISIBLE
            }
            errorMessage.contains("network", ignoreCase = true) || 
            errorMessage.contains("connection", ignoreCase = true) -> {
                Snackbar.make(requireView(), getString(R.string.network_error), Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                // For general errors use a snackbar
                Snackbar.make(requireView(), getString(R.string.general_error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        cardSignUp.isEnabled = !isLoading
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateEmail(email: String): Boolean {
        // Özel regex paterni ile daha katı kontrol
        // @ işareti zorunlu ve sonunda .com olmalı
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$"
        return email.matches(emailPattern.toRegex())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}