package com.cvraterai.myapplication

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_first_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Mevcut dil ayarını kontrol et ve butonları göster/gizle
        setupLanguageButtons(view)
        
        // Get Started butonuna tıklama olayını ayarla
        view.findViewById<CardView>(R.id.btnGetStarted).setOnClickListener {
            // Step1Fragment'a geçiş yap
            findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
        }
        
        // Türkçe devam et butonuna tıklama olayını ayarla
        view.findViewById<CardView>(R.id.btnTurkceDevamEt).setOnClickListener {
            // Dili Türkçe'ye çevir
            setLocale("tr")
            
            // Kullanıcıya bilgi ver
            Toast.makeText(requireContext(), "Dil Türkçe olarak değiştirildi", Toast.LENGTH_SHORT).show()
            
            // Step1Fragment'a geçiş yap
            findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
        }
        
        // İngilizce devam et butonuna tıklama olayını ayarla
        view.findViewById<CardView>(R.id.btnContinueInEnglish).setOnClickListener {
            // Dili İngilizce'ye çevir
            setLocale("en")
            
            // Kullanıcıya bilgi ver
            Toast.makeText(requireContext(), "Language changed to English", Toast.LENGTH_SHORT).show()
            
            // Step1Fragment'a geçiş yap
            findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
        }
    }
    
    // Mevcut dil ayarına göre butonları göster/gizle
    private fun setupLanguageButtons(view: View) {
        // Mevcut dil ayarını al
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString("language", "en") ?: "en"
        
        // Dil Türkçe ise İngilizce butonunu göster, Türkçe butonunu gizle
        if (currentLanguage == "tr") {
            view.findViewById<CardView>(R.id.btnTurkceDevamEt).visibility = View.GONE
            view.findViewById<CardView>(R.id.btnContinueInEnglish).visibility = View.VISIBLE
        } else {
            // Dil İngilizce ise Türkçe butonunu göster, İngilizce butonunu gizle
            view.findViewById<CardView>(R.id.btnTurkceDevamEt).visibility = View.VISIBLE
            view.findViewById<CardView>(R.id.btnContinueInEnglish).visibility = View.GONE
        }
    }
    
    // Dil değiştirme fonksiyonu
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)
        
        // Uygulama genelinde dil ayarını kaydet
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", languageCode).apply()
        
        // Kaynakları güncelle
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().recreate()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}