package servolne.cima.presentation.home

import servolne.cima.databinding.FragmentHomeBinding
import servolne.cima.presentation.common.fragment.BaseFragment
import com.github.terrakok.cicerone.Router
import servolne.cima.presentation.common.backpress.BackPressedStrategyOwner
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::inflate
), BackPressedStrategyOwner {

    @Inject
    lateinit var router: Router

    override fun handleBackPress() {
        requireActivity().finish()
    }
}
