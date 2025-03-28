package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentLoginBinding
import com.cvraterai.myapplication.ui.auth.LoginState
import com.cvraterai.myapplication.ui.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = "LoginFragment"
    
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivTogglePassword: ImageView
    private var passwordVisible = false
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()

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
    ): View {
        // View binding kullanarak layout inflate et
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // UI bileşenlerini başlat
        etEmail = binding.etEmail
        etPassword = binding.etPassword
        btnLogin = binding.btnLogin
        progressBar = binding.progressBar
        ivTogglePassword = binding.ivTogglePassword
        
        // Şifre görünürlüğü toggle işlevi
        ivTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            togglePasswordVisibility(passwordVisible)
        }
        
        // Otomatik giriş kontrolü
        if (viewModel.isLoggedIn()) {
            // Önce token'ların geçerli olduğundan emin ol
            val hasValidToken = viewModel.ensureValidAccessToken()
            
            // Otomatik giriş durumunda token'ları logcat'te göster
            Log.d(TAG, "Auto Login - Access Token: ${viewModel.getAccessToken()}")
            Log.d(TAG, "Auto Login - Refresh Token: ${viewModel.getRefreshToken()}")
            Log.d(TAG, "Auto Login - Has Valid Token: $hasValidToken")
            println("Auto Login - Access Token: ${viewModel.getAccessToken()}")
            println("Auto Login - Refresh Token: ${viewModel.getRefreshToken()}")
            println("Auto Login - Has Valid Token: $hasValidToken")
            
            if (hasValidToken) {
                findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
                return
            }
        }
        
        // Giriş butonuna tıklama olayını ayarla
        btnLogin.setOnClickListener {
            // Giriş işlemini gerçekleştir
            validateAndLogin()
        }
        
        // Kayıt ol metnine tıklama olayını ayarla
        binding.tvRegister.setOnClickListener {
            // RegisterFragment'a geçiş yap
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        
        // ViewModel'dan gelen durumu gözlemle
        observeLoginState()
    }
    
    private fun observeLoginState() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {
                    showLoading(true)
                }
                is LoginState.Success -> {
                    showLoading(false)
                    
                    // Başarılı giriş durumunda token'ları logcat'te göster
                    val effectiveAccessToken = state.data.getEffectiveAccessToken()
                    val effectiveRefreshToken = state.data.getEffectiveRefreshToken()
                    
                    Log.d(TAG, "Login Success - Effective Access Token: $effectiveAccessToken")
                    Log.d(TAG, "Login Success - Effective Refresh Token: $effectiveRefreshToken")
                    
                    // Ayrıca println ile de gösterelim
                    println("Login Success - Effective Access Token: $effectiveAccessToken")
                    println("Login Success - Effective Refresh Token: $effectiveRefreshToken")
                    
                    // TokenManager'da saklanan token'ları da kontrol edelim
                    Log.d(TAG, "Saved Access Token: ${viewModel.getAccessToken()}")
                    Log.d(TAG, "Saved Refresh Token: ${viewModel.getRefreshToken()}")
                    println("Saved Access Token: ${viewModel.getAccessToken()}")
                    println("Saved Refresh Token: ${viewModel.getRefreshToken()}")
                    
                    // Toast yerine navigate doğrudan yapılıyor
                    findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
                }
                is LoginState.Error -> {
                    showLoading(false)
                    handleLoginError(state.message)
                }
            }
        }
    }
    
    private fun validateAndLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Reset error states
        etEmail.isActivated = false
        etPassword.isActivated = false
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        
        var isValid = true
        
        // Email format doğrulama
        if (email.isEmpty()) {
            etEmail.isActivated = true
            binding.tvEmailError.text = getString(R.string.email_required)
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.isActivated = true
            binding.tvEmailError.text = getString(R.string.email_error)
            binding.tvEmailError.visibility = View.VISIBLE
            isValid = false
        }
        
        // Şifre doğrulama
        if (password.isEmpty()) {
            etPassword.isActivated = true
            binding.tvPasswordError.text = getString(R.string.password_required)
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        } else if (password.length < 6) {
            etPassword.isActivated = true
            binding.tvPasswordError.text = getString(R.string.password_error)
            binding.tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }
        
        if (isValid) {
            // Show loading indicator
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            
            // Attempt login
            viewModel.login(email, password)
        }
    }
    
    private fun handleLoginError(errorMessage: String) {
        // Hide loading indicator
        progressBar.visibility = View.GONE
        btnLogin.isEnabled = true
        
        // Reset previous errors
        etEmail.isActivated = false
        etPassword.isActivated = false
        binding.tvEmailError.visibility = View.GONE
        binding.tvPasswordError.visibility = View.GONE
        
        Log.d(TAG, "Login Error Message: $errorMessage") // Error mesajını log'a yazdır
        
        // İstenilen genel hata gösterimi: Her iki alan da kırmızı olsun ve genel bir hata mesajı gösterilsin
        // Sadece ağ hatası durumunda farklı davranıyoruz
        if (errorMessage.lowercase().contains("network") ||
            errorMessage.lowercase().contains("internet") ||
            errorMessage.lowercase().contains("connection") ||
            errorMessage.lowercase().contains("bağlantı") ||
            errorMessage.lowercase().contains("timeout") ||
            errorMessage.lowercase().contains("zaman aşımı") ||
            errorMessage.lowercase().contains("socket") ||
            errorMessage.lowercase().contains("host")) {
            
            Snackbar.make(requireView(), getString(R.string.network_error), Snackbar.LENGTH_SHORT).show()
        } else {
            // E-posta veya şifre hatası - her iki alanı da aktifleştir
            etEmail.isActivated = true
            etPassword.isActivated = true
            
            // Her iki alandaki hata mesajı gösterilsin
            binding.tvEmailError.text = getString(R.string.wrong_credentials_error)
            binding.tvPasswordError.text = getString(R.string.wrong_credentials_error)
            
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvPasswordError.visibility = View.VISIBLE
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
    }

    private fun togglePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            // Şifreyi göster
            etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Şifreyi gizle
            etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            ivTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        // İmleci metnin sonuna getir
        etPassword.setSelection(etPassword.text.length)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}