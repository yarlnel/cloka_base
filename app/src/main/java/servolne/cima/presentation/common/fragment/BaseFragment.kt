package servolne.cima.presentation.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.viewbinding.ViewBinding
import servolne.cima.presentation.MainActivity
import dagger.android.support.DaggerFragment

abstract class BaseFragment<VB : ViewBinding> constructor(
    private val bindingBlock: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DaggerFragment() {

    lateinit var binding: VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewBinding = bindingBlock(inflater, container, false)
        this.binding = viewBinding

        return viewBinding.root
    }

    fun ultimateOnBackPressed() {
        val mainActivity = requireActivity() as? MainActivity ?: return
        mainActivity.ultimateOnBackPressed()
    }
}
