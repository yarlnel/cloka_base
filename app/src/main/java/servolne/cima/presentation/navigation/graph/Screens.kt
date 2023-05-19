package servolne.cima.presentation.navigation.graph

import servolne.cima.presentation.home.HomeFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen

object Screens {


    fun Home() = FragmentScreen {
        HomeFragment()
    }
}