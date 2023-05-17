package com.bignerdranch.android.auto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*


private const val ARG_AUTO_ID = "auto_id"
private const val TAG = "AutoFragment"



class AutoFragment : Fragment() {

    var marks = arrayOf("Lamborghini", "BMW", "Kia", "Toyota")

    var kiaModels = arrayOf("Rio", "K5", "Stinger", "Seltos")
    var bmwModels = arrayOf("I8", "X5", "M5", "E34")
    var toyotaModel = arrayOf("RAV4", "Crown", "Ch-R", "FJ")
    var lamborghiniModels = arrayOf("Aventador", "Urus", "Huracan")

    private lateinit var auto : Auto
    private lateinit var markField: Spinner
    private lateinit var modelField: Spinner
    private lateinit var priceField: EditText
    private lateinit var markAdapter: ArrayAdapter<String>
    private lateinit var modelAdapter: ArrayAdapter<String>
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

        markField = view.findViewById(R.id.auto_mark) as Spinner
        modelField = view.findViewById(R.id.auto_model) as Spinner
        priceField = view.findViewById(R.id.auto_price) as EditText

        markAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, marks)
        markAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        markField.setAdapter(markAdapter);

        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lamborghiniModels)
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelField.setAdapter(modelAdapter);

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

        markField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currentMark = markField.selectedItem as String
                auto.mark = currentMark

                when(auto.mark){
                    "Lamborghini" -> {
                        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lamborghiniModels)
                    }
                    "BMW" -> {
                        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bmwModels)
                    }
                    "Kia" -> {
                        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kiaModels)
                    }
                    "Toyota" -> {
                        modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, toyotaModel)
                    }
                }

                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                modelField.setAdapter(modelAdapter);

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        modelField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val currentModel = modelField.selectedItem as String
                auto.model = currentModel
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

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
        var position = markAdapter.getPosition(auto.mark)
        markField.setSelection(position);
        position = modelAdapter.getPosition(auto.model)
        modelField.setSelection(position);
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