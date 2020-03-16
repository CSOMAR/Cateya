package co.alobaid.cateya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_add_or_edit_payee.*

const val IS_ADD_MODE = "isAddMode"
const val IS_MULTIPLE_EDIT_MODE = "isMultipleEditMode"
const val PAYEE = "payee"

class AddOrEditPayeeDialogFragment : DialogFragment() {

    private lateinit var onClickAction: (payee: Payee) -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_or_edit_payee, container, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText.addTextChangedListener(getTextWatcher())
        amountEditText.addTextChangedListener(getTextWatcher())

        arguments?.let {
            val payee: Payee?

            when {
                it.getBoolean(IS_ADD_MODE) -> {
                    payee = Payee()
                    addOrEditButton.text = getString(R.string.add)
                }
                it.getBoolean(IS_MULTIPLE_EDIT_MODE) -> {
                    payee = Payee()
                    nameEditText.visibility = View.GONE
                    addOrEditButton.text = getString(R.string.edit)
                }
                else -> {
                    payee = it.getParcelable(PAYEE)
                    addOrEditButton.text = getString(R.string.edit)

                    nameEditText.setText(payee?.name)
                    amountEditText.setText(payee?.amount.toString())
                }
            }

            addOrEditButton.setOnClickListener {
                payee?.name = nameEditText.text.toString()
                payee?.amount = amountEditText.text.toString().toDouble()
                payee?.let { it1 -> onClickAction.invoke(it1) }
                dismiss()
            }
        }
    }

    fun setOnClickAction(onClickAction: (payee: Payee) -> Unit) {
        this.onClickAction = onClickAction
    }

    private fun getTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkValidInput()
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun checkValidInput() {
        arguments?.let {
            if (it.getBoolean(IS_MULTIPLE_EDIT_MODE))
                addOrEditButton.isEnabled = amountEditText.text.toString().isNotEmpty()
            else
                addOrEditButton.isEnabled = nameEditText.text.toString().isNotEmpty() && amountEditText.text.toString().isNotEmpty()
        }
    }

}