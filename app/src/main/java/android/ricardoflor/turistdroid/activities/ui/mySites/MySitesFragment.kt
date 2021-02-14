package android.ricardoflor.turistdroid.activities.ui.mySites

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.bd.site.Site
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_my_sites.*
import android.content.DialogInterface
import android.os.*
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.BdController
import android.ricardoflor.turistdroid.bd.site.SiteDTO
import android.ricardoflor.turistdroid.bd.site.SiteMapper
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import android.ricardoflor.turistdroid.MyApplication.Companion.USER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MySitesFragment : Fragment() {

    private var sitios = mutableListOf<Site>()
    private var spinnerOrder: Spinner? = null
    private var spinnerFilter: Spinner? = null

    // Interfaz gráfica
    private lateinit var adapter: SiteListAdapter
    private var paintSweep = Paint()

    // Vibrador
    private var vibrator: Vibrator? = null

    // Cloud Firestore
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    //autenticador
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.fragment_my_sites, container, false)
        spinnerOrder = root.findViewById(R.id.spinnerOrder)
        spinnerFilter = root.findViewById(R.id.spinnerFilter)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializamos la pantalla
        init()
    }

    private fun init() {
        // Iniciamos el swipe para recargar
        iniciarSwipeRecarga()

        // Cargamos los datos por primera vez
        cargaSitios("")

        // Solo si hemos cargado hacemos sl swipeHorizontal
        iniciarSwipeHorizontal()

        // Iniciamos los spinner
        iniciarSpinnerFilter()
        iniciarSpinnerOrder()

        // Mostramos las vistas de listas y adaptador asociado
        my_sites_recicler.layoutManager = LinearLayoutManager(context)

        //Boton flotante anadir
        btnAddSiteFloating.setOnClickListener { addSite() }

        // Obtiene instancia a Vibrator
        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    }

    /**
     * Iniciamos el swipe de recarga
     */
    private fun iniciarSwipeRecarga() {
        my_sites_swipe.setColorSchemeResources(R.color.primary)
        my_sites_swipe.setProgressBackgroundColorSchemeResource(R.color.primary_text)
        my_sites_swipe.setOnRefreshListener {
            spinnerOrder?.setSelection(0)
            spinnerFilter?.setSelection(0)
            cargaSitios("")
        }
    }

    /**
     * Carga los Sitios
     */
    private fun cargaSitios(callFilter: String) {
        sitios = mutableListOf<Site>()

        if (callFilter.equals("")) {
            db.collection("sites").get()
                .addOnSuccessListener { result ->
                    for (item in result) {
                        val lugar = item.toObject(Site::class.java)
                        sitios.add(lugar)
                        cargaAdapter(sitios)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.i("fairebase", "error al cargar sitios")
                    Toast.makeText(context!!, R.string.service_error, Toast.LENGTH_SHORT).show()
                }

        } else if (callFilter.equals("USER")) {
            db.collection("sites").whereEqualTo("userID", auth.currentUser?.uid).get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        cargaAdapter(sitios)

                    } else {
                        for (item in result) {
                            val lugar = item.toObject(Site::class.java)
                            sitios.add(lugar)
                            cargaAdapter(sitios)
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.i("fairebase", "error al cargar sitios")
                    Toast.makeText(context!!, R.string.service_error, Toast.LENGTH_SHORT).show()
                }

        } else {
            db.collection("sites").whereEqualTo("site", callFilter).get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        cargaAdapter(sitios)

                    } else {
                        for (item in result) {
                            val lugar = item.toObject(Site::class.java)
                            sitios.add(lugar)
                            cargaAdapter(sitios)
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    Log.i("fairebase", "error al cargar sitios")
                    Toast.makeText(context!!, R.string.service_error, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargaAdapter(sitios: MutableList<Site>) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            //doInBackground()
            handler.post {
                //onPostExecute
                adapter = SiteListAdapter(sitios) {
                    eventoClicFila(it)
                }

                my_sites_recicler.adapter = adapter
                // Avismos que ha cambiado
                adapter.notifyDataSetChanged()
                my_sites_recicler.setHasFixedSize(true)
                my_sites_swipe.isRefreshing = false
            }
        }
    }

    /**
     * Evento clic asociado a una fila
     * @param site Site
     */
    private fun eventoClicFila(site: Site) {
        if ((activity as NavigationActivity?)!!.isEventoFila) {
            openSite(site, 3)
        }
    }

    /**
     * Realiza el swipe horizontal si es necesario
     */
    private fun iniciarSwipeHorizontal() {

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {
            // Sobreescribimos los métodos
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Analizamos el evento según la dirección
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val site = sitios[position]

                if (auth.currentUser?.uid == site.userID) {
                    // Si pulsamos a la de izquierda o a la derecha
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            // Borramos el elemento
                            if ((activity as NavigationActivity?)!!.isEventoFila) {
                                borrarElemento(position)
                            }
                        }
                        else -> {
                            // Editamos el elemento
                            if ((activity as NavigationActivity?)!!.isEventoFila) {
                                editarElemento(position)
                            }
                        }
                    }
                } else {
                    // Toast.makeText(requireContext(), R.string.no_permiss, Toast.LENGTH_SHORT).show()
                }

                cargaSitios("")
            }

            // Dibujamos los botones
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val position = viewHolder.adapterPosition
                if (position >= 0) {
                    val site = sitios[position]
                    if (auth.currentUser?.uid == site.userID) {
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            val itemView = viewHolder.itemView
                            val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                            val width = height / 3
                            // Si es dirección a la derecha: izquierda->derecha
                            // Pintamos de azul y ponemos el icono
                            if (dX > 0) {
                                // Pintamos el botón izquierdo
                                botonIzquierdo(canvas, dX, itemView, width)
                            } else {
                                // Caso contrario
                                botonDerecho(canvas, dX, itemView, width)
                            }
                        }
                        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    }
                }
            }
        }
        // Añadimos los eventos al RV
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(my_sites_recicler)
    }

    /**
     * Inicia el Spinner Filter
     */
    private fun iniciarSpinnerFilter() {
        val filterBy = resources.getStringArray(R.array.Filters)
        val adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_spinner_item, filterBy
        )
        spinnerFilter!!.adapter = adapter
        spinnerFilter!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterSites(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    /**
     * Metodo que realiza el filtrado de los GETs
     */
    private fun filterSites(pos: Int) {

        when (pos) {
            1 -> { // Filter by ALL SITES
                cargaSitios("")
            }

            2 -> { // Filter by MY SITES
                cargaSitios("USER")
            }

            3 -> { // Filter by CITY
                cargaSitios("City")
            }

            4 -> { // Filter by PARK
                cargaSitios("Park")
            }

            5 -> { // Filter by BAR
                cargaSitios("Bar")
            }

            6 -> { // Filter by MONUMENT
                cargaSitios("Monument")
            }

            7 -> { // Filter by RESTAURANT
                cargaSitios("Restaurant")
            }

            8 -> { // Filter by SHOP
                cargaSitios("Shop")
            }
        }
    }

    /**
     * Inicia el Spinner Order
     */
    private fun iniciarSpinnerOrder() {
        val orderBy = resources.getStringArray(R.array.Orders)
        val adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_spinner_item, orderBy
        )
        spinnerOrder!!.adapter = adapter
        spinnerOrder!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                orderSites(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun orderSites(pos: Int) {
        when (pos) {
            1 -> { // Order by NAME
                this.sitios.sortWith() { uno: Site, dos: Site ->
                    uno.name.toUpperCase().compareTo(dos.name.toUpperCase())
                }
                my_sites_recicler.adapter = adapter
            }

            2 -> { // Order by DATE
                this.sitios.sortWith() { uno: Site, dos: Site ->
                    SimpleDateFormat("dd/MM/yyyy").parse(dos.date)
                        .compareTo(SimpleDateFormat("dd/MM/yyyy").parse(uno.date))
                }
                my_sites_recicler.adapter = adapter
            }

            3 -> { // Order by RATINGS
                this.sitios.sortWith() { uno: Site, dos: Site -> (dos.rating / dos.votos.size).compareTo((uno.rating / uno.votos.size)) }
                my_sites_recicler.adapter = adapter
            }
        }
    }

    private fun borrarElemento(position: Int) {
        val deletedSite = sitios[position]
        adapter.removeItem(position)
        confirmDialog(deletedSite, position)
    }

    /**
     * Metodo que crea un AlertDialog para confirmar el borrado
     */
    private fun confirmDialog(site: Site, position: Int) {
        val dialogo: AlertDialog.Builder = AlertDialog.Builder(activity)
        dialogo.setTitle(R.string.delete_site)
        dialogo.setMessage(R.string.delete_question)
        dialogo.setCancelable(false)
        dialogo.setPositiveButton(R.string.accept,
            DialogInterface.OnClickListener { dialogo1, id -> acceptDelete(site) })
        dialogo.setNegativeButton(R.string.Cancel,
            DialogInterface.OnClickListener { dialogo1, id -> cancelDelete(site, position) })
        dialogo.show()
    }

    fun acceptDelete(site: Site) {

        db.collection("sites").document(site.id).delete()
            .addOnSuccessListener {
                Log.i("fairbase", "successful delsite")
                Toast.makeText(requireContext(), R.string.site_deleted, Toast.LENGTH_SHORT).show()

                vibrate()
                cargaSitios("")
            }

            .addOnFailureListener { e ->
                Toast.makeText(context!!, getText(R.string.service_error).toString(), Toast.LENGTH_LONG).show()
                Log.i("fairbase", e.localizedMessage)
            }
    }

    fun cancelDelete(site: Site, position: Int) {
        Toast.makeText(requireContext(), R.string.Cancel, Toast.LENGTH_SHORT)
        adapter.restoreItem(site, position)
    }

    /**
     * Acción secundaria: Ver/Editar
     * @param position Int
     */
    private fun editarElemento(position: Int) {
        val site = sitios[position]
        openSite(site, 2)

        // Esto es para que no se quede el color
        adapter.removeItem(position)
        adapter.restoreItem(site, position)
    }

    /**
     * Mostramos el elemento izquierdo
     * @param canvas Canvas
     * @param dX Float
     * @param itemView View
     * @param width Float
     */
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de gris y ponemos el icono
        paintSweep.setColor(Color.YELLOW)
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.lapiz_white)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Mostramos el elemento inquierdo
     * @param canvas Canvas
     * @param dX Float
     * @param itemView View
     * @param width Float
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de rojo y ponemos el icono
        paintSweep.color = Color.RED
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.basura_white)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Abre una sitio como Fragment
     * @param site Site
     */
    private fun openSite(site: Site, modo: Int) {
        Log.i("sites", "editar sitio")
        val addSites = SiteFragment(modo, site)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, addSites)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun addSite() {
        Log.i("sites", "nuevo sitio")
        val addSites = SiteFragment(1, null)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, addSites)
        transaction.addToBackStack(null)
        transaction.commit()
        cargaSitios("")
    }

    /**
     * Vibrador
     */
    fun vibrate() {
        //Compruebe si dispositivo tiene un vibrador.
        if (vibrator!!.hasVibrator()) { //Si tiene vibrador

            val pattern = longArrayOf(
                400,  //sleep
                600,  //vibrate
                100, 300, 100, 150, 100, 75
            )
            // con -1 se indica desactivar repeticion del patron
            vibrator!!.vibrate(pattern, -1)

        } else { //no tiene
            //Log.v("VIBRATOR", "Este dispositivo NO puede vibrar");
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BdController.close()
    }

}