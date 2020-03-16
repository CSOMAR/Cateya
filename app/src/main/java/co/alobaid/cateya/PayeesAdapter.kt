package co.alobaid.cateya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_payee.view.*

class PayeesAdapter : RecyclerView.Adapter<PayeesAdapter.ViewHolder>() {

    private var payees: List<DataSnapshot> = ArrayList()

    private lateinit var onEditClickAction: (payee: Payee) -> Unit

    private lateinit var onDeleteClickAction: (payee: Payee) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_payee, parent, false))
    }

    override fun getItemCount(): Int {
        return payees.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        payees[position].getValue(Payee::class.java)?.let { holder.bind(it) }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setPayees(payees: List<DataSnapshot>) {
        this.payees = payees
        notifyDataSetChanged()
    }

    fun setOnEditClickAction(onEditClickAction: (payee: Payee) -> Unit) {
        this.onEditClickAction = onEditClickAction
    }

    fun setOnDeleteClickAction(onDeleteClickAction: (payee: Payee) -> Unit) {
        this.onDeleteClickAction = onDeleteClickAction
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal fun bind(payee: Payee) {
            with(itemView) {
                linearLayout.setOnLongClickListener {
                    selectCheckBox.isChecked = true
                    actionsVisibilityObservable.onNext(false)
                    true
                }

                selectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedPayees.add(payee)
                    } else {
                        selectedPayees.remove(payee)
                        if (selectedPayees.isEmpty())
                            actionsVisibilityObservable.onNext(true)
                    }
                }

                nameTextView.text = payee.name
                amountTextView.text = payee.amount.toString()

                actionsVisibilityObservable.subscribe { isVisible: Boolean? ->
                    isVisible?.let {
                        if (it) {
                            selectCheckBox.isChecked = false
                            selectCheckBox.visibility = View.GONE
                            editButton.visibility = View.VISIBLE
                            deleteButton.visibility = View.VISIBLE
                        } else {
                            selectCheckBox.visibility = View.VISIBLE
                            editButton.visibility = View.GONE
                            deleteButton.visibility = View.GONE
                        }
                    }
                }

                editButton.setOnClickListener { onEditClickAction.invoke(payee) }
                deleteButton.setOnClickListener { onDeleteClickAction.invoke(payee) }
            }
        }
    }

    companion object {
        val actionsVisibilityObservable: PublishSubject<Boolean> = PublishSubject.create()
        val selectedPayees: ArrayList<Payee> = ArrayList()
    }

}