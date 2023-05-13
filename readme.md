<p align = "center">МИНИСТЕРСТВО НАУКИ И ВЫСШЕГО ОБРАЗОВАНИЯ<br>
РОССИЙСКОЙ ФЕДЕРАЦИИ<br>
ФЕДЕРАЛЬНОЕ ГОСУДАРСТВЕННОЕ БЮДЖЕТНОЕ<br>
ОБРАЗОВАТЕЛЬНОЕ УЧРЕЖДЕНИЕ ВЫСШЕГО ОБРАЗОВАНИЯ<br>
«САХАЛИНСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ»</p>
<br><br><br><br><br><br>
<p align = "center">Институт естественных наук и техносферной безопасности<br>Кафедра информатики<br>Коньков Никита Алексеевич</p>
<br><br><br>
<p align = "center">Лабораторная работа №8<br><strong>«Вывод списков и RecyclerView»</strong><br>01.03.02 Прикладная математика и информатика</p>
<br><br><br><br><br><br><br><br><br><br><br><br>
<p align = "right">Научный руководитель<br>
Соболев Евгений Игоревич</p>
<br><br><br>
<p align = "center">г. Южно-Сахалинск<br>2022 г.</p>
<br><br><br><br><br><br><br><br><br><br><br><br>

<h1 align = "center">Введение</h1>

<p><b>Android Studio</b> — интегрированная среда разработки (IDE) для работы с платформой Android, анонсированная 16 мая 2013 года на конференции Google I/O. В последней версии Android Studio поддерживается Android 4.1 и выше.</p>
<p><b>Kotlin</b> — это кроссплатформенный статически типизированный язык программирования общего назначения высокого уровня. Kotlin предназначен для полного взаимодействия с Java, а версия стандартной библиотеки Kotlin для JVM зависит от библиотеки классов Java, но вывод типов позволяет сделать ее синтаксис более кратким. Kotlin в основном нацелен на JVM, но также компилируется в JavaScript (например, для интерфейсных веб-приложений, использующих React) или собственный код через LLVM (например, для собственных приложений iOS, разделяющих бизнес-логику с приложениями Android). Затраты на разработку языка несет JetBrains, а Kotlin Foundation защищает торговую марку Kotlin.</p>

<br>
<h1 align = "center">Цели и задачи</h1>
<h2 align = "center"><b> Приложение. Авто </b></h2>
<p>Приложение, должно иметь следующие функции:
<ul>
<li>Отображение списка автомобилей с характеристиками (10-12 автомобилей, 3 производителя, 1-3 марки у каждого производителя)</li>
<li>Добавление нового автомобиля</li>
<li>Редактирование деталей автомобиля</li>
</ul>
Желательно
<ul>
<li>Фильтрация по производителю и марке</li>
<li>Сортировка по цене</li>
</ul>
</p>



<h1 align = "center">Решение</h1>

<p>За основу взял код из 14 главы учебника "<b>Android</b> Программирование для профессионалов".</p>

<h2 align = "center">Файл MainActivity.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() ,
    AutoListFragment.Callbacks {
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            if (currentFragment == null) {
                val fragment = AutoListFragment.newInstance()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }

        }

    override fun onAutoSelected(autoId: UUID) {
        val fragment = AutoFragment.newInstance(autoId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
```

<h2 align = "center">Файл AutoRepository.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.auto.database.AutoDatabase
import java.util.UUID
import java.util.concurrent.Executors

private const val DATABASE_NAME = "auto-database"

class AutoRepository private constructor(context: Context) {

    private val database : AutoDatabase = Room.databaseBuilder(
        context.applicationContext,
        AutoDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val autoDao = database.autoDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getAutos(): LiveData<List<Auto>> = autoDao.getAutos()

    fun getAuto(id: UUID): LiveData<Auto?> = autoDao.getAuto(id)

    fun updateAuto(auto: Auto) {
        executor.execute {
            autoDao.updateAuto(auto)
        }
    }

    fun addAuto(auto: Auto) {
        executor.execute {
            autoDao.addAuto(auto)
        }
    }

    companion object {
        private var INSTANCE: AutoRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AutoRepository(context)
            }
        }
        fun get(): AutoRepository {
            return INSTANCE ?:
            throw IllegalStateException("AutoRepository must be initialized")
        }
    }
}
```

<h2 align = "center">Файл AutoListViewModel.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import androidx.lifecycle.ViewModel
import kotlin.random.Random

class AutoListViewModel : ViewModel() {

    private val autoRepository = AutoRepository.get()
    val autoListLiveData = autoRepository.getAutos()

    fun addAuto(auto: Auto) {
        autoRepository.addAuto(auto)
    }

}
```

<h2 align = "center">Файл AutoListFragment.kt</h2>

```kotlin
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
```

<h2 align = "center">Файл AutoIntentApplication.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import android.app.Application

class AutoIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AutoRepository.initialize(this)
    }
}
```

<h2 align = "center">Файл AutoFragment.kt</h2>

```kotlin
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
```

<h2 align = "center">Файл AutoDetailViewModel.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class AutoDetailViewModel() : ViewModel() {
    private val autoRepository = AutoRepository.get()
    private val autoIdLiveData = MutableLiveData<UUID>()
    var autoLiveData: LiveData<Auto?> =
        Transformations.switchMap(autoIdLiveData) { autoId ->
            autoRepository.getAuto(autoId)
        }
    fun loadAuto(autoId: UUID) {
        autoIdLiveData.value = autoId
    }

    fun saveAuto(auto: Auto) {
        autoRepository.updateAuto(auto)
    }
}
```

<h2 align = "center">Файл Auto.kt</h2>

```kotlin
package com.bignerdranch.android.auto

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class Auto (@PrimaryKey
    val id: UUID = UUID.randomUUID(),
                 var mark: String = "",
                 var model: String = "",
                 var price: Int = 0)
{

}
```

<h2 align = "center">Файл БД AutoDao.kt</h2>

```kotlin
package com.bignerdranch.android.auto.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bignerdranch.android.auto.Auto
import java.util.*

@Dao
interface AutoDao {

    @Query("SELECT * FROM auto")
    fun getAutos(): LiveData<List<Auto>>

    @Query("SELECT * FROM auto WHERE id=(:id)")
    fun getAuto(id: UUID): LiveData<Auto?>

    @Update
    fun updateAuto(auto: Auto)

    @Insert
    fun addAuto(auto: Auto)

}
```

<h2 align = "center">Файл БД AutoDatabase.kt</h2>

```kotlin
package com.bignerdranch.android.auto.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.auto.Auto

@Database(entities = [ Auto::class ], version=1)
@TypeConverters(AutoTypeConverters::class)
abstract class AutoDatabase : RoomDatabase() {

    abstract fun autoDao(): AutoDao

}
```

<h2 align = "center">Файл БД AutoTypeConverters.kt</h2>

```kotlin
package com.bignerdranch.android.auto.database

import androidx.room.TypeConverter
import java.util.*

class AutoTypeConverters {

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

}
```

<h1 align = "center">Вывод</h1>
<p>По итогу проделанной лабораторной работы, я научился сортировать БД, стал лучше понимать структуру подобного приложения.</p>