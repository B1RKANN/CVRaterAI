package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
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
import com.cvraterai.myapplication.ui.auth.LoginState
import com.cvraterai.myapplication.ui.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

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
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    
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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // UI bileşenlerini başlat
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvRegister = view.findViewById(R.id.tvRegister)
        progressBar = view.findViewById(R.id.progressBar)
        
        // Otomatik giriş kontrolü
        if (viewModel.isLoggedIn()) {
            // Otomatik giriş durumunda token'ları logcat'te göster
            Log.d(TAG, "Auto Login - Access Token: ${viewModel.getAccessToken()}")
            Log.d(TAG, "Auto Login - Refresh Token: ${viewModel.getRefreshToken()}")
            println("Auto Login - Access Token: ${viewModel.getAccessToken()}")
            println("Auto Login - Refresh Token: ${viewModel.getRefreshToken()}")
            
            findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
            return
        }
        
        // Giriş butonuna tıklama olayını ayarla
        btnLogin.setOnClickListener {
            // Giriş işlemini gerçekleştir
            performLogin()
        }
        
        // Kayıt ol metnine tıklama olayını ayarla
        tvRegister.setOnClickListener {
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
                    
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
                }
                is LoginState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        viewModel.login(email, password)
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
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