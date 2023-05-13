package com.bignerdranch.android.auto

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


private const val TAG = "AutoListFragment"

class AutoListFragment : Fragment() {

    interface Callbacks {
        fun onAutoSelected(autoId: UUID)
    }


    private var sortOrder = 0
    private var callbacks: Callbacks? = null
    private lateinit var autoRecyclerView: RecyclerView
    private var adapter: AutoAdapter? = AutoAdapter(emptyList())

    private val autoListViewModel: AutoListViewModel by lazy {
        ViewModelProviders.of(this).get(AutoListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auto_list, container, false)

        autoRecyclerView =
            view.findViewById(R.id.auto_recycler_view) as RecyclerView
        autoRecyclerView.layoutManager = LinearLayoutManager(context)
        autoRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoListViewModel.autoListLiveData.observe(
            viewLifecycleOwner,
            Observer { autos ->
                autos?.let {
                    updateUI(autos)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_auto_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_auto -> {
                val auto = Auto()
                autoListViewModel.addAuto(auto)
                callbacks?.onAutoSelected(auto.id)
                true
            }
            R.id.auto_sort -> {
                sortOrder = (sortOrder + 1) % 3

                autoListViewModel.autoListLiveData.observe(
                    viewLifecycleOwner,
                    Observer { autos ->
                        autos?.let {
                            updateUI(autos)
                        }
                    })
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(): AutoListFragment {
            return AutoListFragment()
        }
    }

    private fun updateUI(autos: List<Auto>) {

        val sortAutos = when(sortOrder){
            0 -> autos
            1 -> autos.sortedBy { it.price }
            else -> autos.sortedByDescending { it.price }
        }

        adapter = AutoAdapter(sortAutos)
        autoRecyclerView.adapter = adapter
    }

    private inner class AutoHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var auto: Auto

        private val markTextView: TextView = itemView.findViewById(R.id.auto_mark)
        private val modelTextView: TextView = itemView.findViewById(R.id.auto_model)
        private val priceTextView: TextView = itemView.findViewById(R.id.auto_price)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(auto: Auto) {
            this.auto = auto
            markTextView.text = this.auto.mark
            modelTextView.text = this.auto.model
            priceTextView.text = this.auto.price.toString() + " Руб."
        }

        override fun onClick(v: View) {
            callbacks?.onAutoSelected(auto.id)
        }
    }

    private inner class AutoAdapter(var autos: List<Auto>)
        : RecyclerView.Adapter<AutoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoHolder {

            val view = layoutInflater.inflate(R.layout.list_item_auto, parent, false)
            return AutoHolder(view)
        }

        override fun onBindViewHolder(holder: AutoHolder, position: Int) {
            val auto = autos[position]
            holder.bind(auto)
        }


        override fun getItemCount() = autos.size
    }

    }