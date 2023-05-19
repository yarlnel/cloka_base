package servolne.cima.presentation.utils


import android.view.View
import androidx.annotation.DrawableRes
import servolne.cima.R

infix fun View.onclick(voidLambda: () -> Unit) {
    setOnClickListener { voidLambda.invoke() }
}
