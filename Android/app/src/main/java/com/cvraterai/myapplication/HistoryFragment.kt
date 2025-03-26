package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.adapter.EvaluationsAdapter
import com.cvraterai.myapplication.data.JwtUtil
import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import com.cvraterai.myapplication.data.repository.CvEvaluationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: EvaluationsAdapter
    
    @Inject
    lateinit var cvEvaluationRepository: CvEvaluationRepository
    
    @Inject
    lateinit var tokenManager: TokenManager

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
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // UI bileşenlerini bul
        recyclerView = view.findViewById(R.id.rvEvaluations)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        progressBar = view.findViewById(R.id.progressBar)
        
        // Geri butonu
        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }
        
        // RecyclerView için adapteri ayarla
        adapter = EvaluationsAdapter { evaluation ->
            // Değerlendirme detayına git - Önce Information sayfasına yönlendir
            navigateToDetailFlow(evaluation.id)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Değerlendirme geçmişini yükle
        loadEvaluationHistory()
    }
    
    private fun navigateToDetailFlow(evaluationId: Long) {
        // InformationFragment'a yönlendir ve evaluationId'yi aktar
        val bundle = Bundle().apply {
            putLong("evaluationId", evaluationId)
            putBoolean("fromHistory", true) // Geçmişten geldiğini belirt
        }
        findNavController().navigate(R.id.action_historyFragment_to_informationFragment, bundle)
    }
    
    private fun loadEvaluationHistory() {
        // Yükleme göstergesini göster
        showLoading(true)
        
        // Kullanıcı ID'sini alınarak değerlendirme geçmişini yükle
        val userId = getUserId()
        if (userId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val result = cvEvaluationRepository.getUserEvaluations(userId)
                    
                    if (result.isSuccess) {
                        val evaluations = result.getOrNull() ?: emptyList()
                        updateUI(evaluations)
                    } else {
                        // Hata mesajını göster
                        Toast.makeText(requireContext(), "Değerlendirme geçmişi yüklenemedi: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        showEmptyState()
                    }
                } catch (e: Exception) {
                    // Hata mesajını göster
                    Toast.makeText(requireContext(), "Değerlendirme geçmişi yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
                    showEmptyState()
                } finally {
                    showLoading(false)
                }
            }
        } else {
            // Kullanıcı bilgisi yoksa
            Toast.makeText(requireContext(), "Kullanıcı bilgisi alınamadı, lütfen tekrar giriş yapın", Toast.LENGTH_LONG).show()
            showEmptyState()
            showLoading(false)
        }
    }
    
    private fun updateUI(evaluations: List<CvEvaluationResponse>) {
        if (evaluations.isEmpty()) {
            showEmptyState()
        } else {
            showEvaluations(evaluations)
        }
    }
    
    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }
    
    private fun showEvaluations(evaluations: List<CvEvaluationResponse>) {
        emptyStateView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        adapter.updateData(evaluations)
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun getUserId(): Long? {
        val token = tokenManager.getAccessToken() ?: return null
        return JwtUtil.getUserIdFromToken(token)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}