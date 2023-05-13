package com.bignerdranch.android.auto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val ARG_AUTO_ID = "auto_id"
private const val TAG = "AutoFragment"

class AutoFragment : Fragment() {

    private lateinit var auto : Auto
    private lateinit var markField: EditText
    private lateinit var modelField: EditText
    private lateinit var priceField: EditText
    private val autoDetailViewModel: AutoDetailViewModel by lazy {
        ViewModelProviders.of(this).get(AutoDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auto = Auto()
        val autoId: UUID = arguments?.getSerializable(ARG_AUTO_ID) as UUID
        autoDetailViewModel.loadAuto(autoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auto, container, false)

        markField = view.findViewById(R.id.auto_mark) as EditText
        modelField = view.findViewById(R.id.auto_model) as EditText
        priceField = view.findViewById(R.id.auto_price) as EditText

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoDetailViewModel.autoLiveData.observe(
            viewLifecycleOwner,
            Observer { auto ->
                auto?.let {
                    this.auto = auto
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        markField.doAfterTextChanged { auto.mark = it.toString() }
        modelField.doAfterTextChanged { auto.model = it.toString() }
        priceField.doAfterTextChanged {
            auto.price = if (it.toString() == "")
                0
            else
                it.toString().toInt()
        }

    }

    override fun onStop() {
        super.onStop()
        autoDetailViewModel.saveAuto(auto)
    }

    private fun updateUI() {
        markField.setText(auto.mark)
        modelField.setText(auto.model)
        priceField.setText(auto.price.toString())
    }

        companion object {
        fun newInstance(autoId: UUID): AutoFragment {
            val args = Bundle().apply {
                putSerializable(ARG_AUTO_ID, autoId)
            }
            return AutoFragment().apply {
                arguments = args
            }
        }
    }
}