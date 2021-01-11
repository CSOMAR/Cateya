package co.alobaid.cateya

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

private const val PAYEES = "payees"

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private val payeesAdapter = PayeesAdapter()

    private var editButton: MenuItem? = null
    private var increaseButton: MenuItem? = null
    private var decreaseButton: MenuItem? = null
    private var deleteButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupMenuButtonsVisibility()
        setupDatabase()
        setupRecyclerView()
        setupAddButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)

        editButton = menu?.findItem(R.id.editButton)
        increaseButton = menu?.findItem(R.id.increaseButton)
        decreaseButton = menu?.findItem(R.id.decreaseButton)
        deleteButton = menu?.findItem(R.id.deleteButton)

        editButton?.isVisible = false
        increaseButton?.isVisible = false
        decreaseButton?.isVisible = false
        deleteButton?.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val bundle = Bundle()
        bundle.putBoolean(IS_MULTIPLE_EDIT_MODE, true)

        when (item.itemId) {
            R.id.editButton -> {
                showDialog(bundle) { data ->
                    PayeesAdapter.selectedPayees.forEach { payee: Payee ->
                        payee.amount = data.amount
                        database.child(PAYEES).child(payee.id).setValue(payee)
                    }

                    clearSelection()
                }
            }

            R.id.increaseButton -> {
                showDialog(bundle) { data ->
                    PayeesAdapter.selectedPayees.forEach { payee: Payee ->
                        payee.amount = payee.amount + data.amount
                        database.child(PAYEES).child(payee.id).setValue(payee)
                    }

                    clearSelection()
                }
            }

            R.id.decreaseButton -> {
                showDialog(bundle) { data ->
                    PayeesAdapter.selectedPayees.forEach { payee: Payee ->
                        payee.amount = payee.amount - data.amount
                        database.child(PAYEES).child(payee.id).setValue(payee)
                    }

                    clearSelection()
                }
            }

            R.id.deleteButton -> {
                showDeleteDialog(onDeleteAction = {
                    PayeesAdapter.selectedPayees.forEach { payee: Payee ->
                        database.child(PAYEES).child(payee.id).removeValue()
                    }

                    clearSelection()
                })
            }
        }

        return true
    }

    private fun showDeleteDialog(onDeleteAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                onDeleteAction.invoke()
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    private fun setupMenuButtonsVisibility() {
        PayeesAdapter.actionsVisibilityObservable.subscribe { isVisible: Boolean? ->
            isVisible?.let {
                editButton?.isVisible = !it
                increaseButton?.isVisible = !it
                decreaseButton?.isVisible = !it
                deleteButton?.isVisible = !it
            }
        }
    }

    private fun setupDatabase() {
        database = FirebaseDatabase.getInstance().reference
    }

    private fun setupRecyclerView() {
        setupAdapter()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        recyclerView.adapter = payeesAdapter
    }

    private fun setupAdapter() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val payees = dataSnapshot.child(PAYEES).children.toList()
                payeesAdapter.setPayees(payees)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        payeesAdapter.setOnEditClickAction { payee ->
            val bundle = Bundle()
            bundle.putBoolean(IS_ADD_MODE, false)
            bundle.putParcelable(PAYEE, payee)

            showDialog(bundle) { payee -> database.child(PAYEES).child(payee.id).setValue(payee) }
        }

        payeesAdapter.setOnDeleteClickAction { payee ->
            showDeleteDialog(onDeleteAction = {
                database.child(PAYEES).child(payee.id).removeValue()
            })
        }
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_ADD_MODE, true)

            clearSelection()

            showDialog(bundle) { payee ->
                database.child(PAYEES).push().key?.let {
                    payee.id = it
                    database.child(PAYEES).child(payee.id).setValue(payee)
                }
            }
        }

        setupAutoHideAddButton()
    }

    private fun clearSelection() {
        PayeesAdapter.selectedPayees.clear()
        PayeesAdapter.actionsVisibilityObservable.onNext(true)
    }

    private fun showDialog(bundle: Bundle, onClickAction: (payee: Payee) -> Unit) {
        val dialog = AddOrEditPayeeDialogFragment()
        dialog.arguments = bundle
        dialog.setOnClickAction(onClickAction)
        dialog.show(supportFragmentManager, null)
    }

    private fun setupAutoHideAddButton() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    addButton.hide()
                else
                    addButton.show()
            }
        })
    }

}