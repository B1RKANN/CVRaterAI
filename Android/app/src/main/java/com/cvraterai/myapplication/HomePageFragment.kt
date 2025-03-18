package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.widget.Button

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HomePageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    
    @Inject
    lateinit var authRepository: AuthRepository

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
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Profil kartına tıklama olayını ayarla
        view.findViewById<CardView>(R.id.cardProfile).setOnClickListener {
            // ProfileFragment'e geçiş yap
            findNavController().navigate(R.id.action_homePageFragment_to_profileFragment)
        }

        // CV yükleme kartına tıklama olayını ayarla
        view.findViewById<CardView>(R.id.cardUploadCV).setOnClickListener {
            // UploadCvFragment'e geçiş yap
            findNavController().navigate(R.id.action_homePageFragment_to_uploadCvFragment)
        }
        
        // Boş durum kartına tıklama olayını ayarla
        view.findViewById<CardView>(R.id.cardEmptyState).setOnClickListener {
            // Geçmiş sayfasına geçiş yap
            findNavController().navigate(R.id.action_homePageFragment_to_historyFragment)
        }
        
        // Geçmişi Görüntüle butonuna tıklama olayını ayarla
        view.findViewById<Button>(R.id.btnViewHistory)?.setOnClickListener {
            // Geçmiş sayfasına geçiş yap
            findNavController().navigate(R.id.action_homePageFragment_to_historyFragment)
        }
        
        // Logout butonuna tıklama olayını ayarla
        view.findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            // Oturumu kapat
            authRepository.logout()
            
            // Kullanıcıya bilgi ver
            Toast.makeText(requireContext(), getString(R.string.logout), Toast.LENGTH_SHORT).show()
            
            // FirstPageFragment'a geri dön
            findNavController().navigate(R.id.action_homePageFragment_to_firstPageFragment)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomePageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}