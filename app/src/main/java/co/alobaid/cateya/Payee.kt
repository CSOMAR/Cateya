package co.alobaid.cateya

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Payee(
  var id: String = "",
  var name: String = "",
  var amount: Double = 0.0
) : Parcelable