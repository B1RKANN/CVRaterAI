package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentProfileBinding
import com.cvraterai.myapplication.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        loadProfileData()
    }
    
    private fun setupObservers() {
        viewModel.profileData.observe(viewLifecycleOwner) { profileData ->
            // Update UI with profile data
            binding.tvUserName.text = profileData.name
            binding.tvUserEmail.text = profileData.email
            binding.tvPlanType.text = profileData.planType
            binding.tvCreditsCount.text = "${profileData.userCredit}/20" // Assuming max is 20
            
            // Update progress bar
            binding.progressCredits.max = 20 // Assuming max is 20
            binding.progressCredits.progress = profileData.userCredit
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state (could show/hide a progress indicator)
            // For example: binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                
                // Oturum süresi dolmuşsa login ekranına yönlendir
                if (it.contains("Oturum süresi dolmuş") || it.contains("Login")) {
                    // Kullanıcıyı login sayfasına yönlendir
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
    }
    
    private fun loadProfileData() {
        viewModel.fetchProfileData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}