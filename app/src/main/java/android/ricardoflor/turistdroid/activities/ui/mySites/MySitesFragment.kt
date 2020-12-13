package android.ricardoflor.turistdroid.activities.ui.mySites

import android.app.AlertDialog
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.activities.NavigationActivity
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.bd.site.SiteController
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
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Toast


class MySitesFragment : Fragment() {

    private lateinit var mySitesViewModel: MySitesViewModel
    private var sitios = mutableListOf<Site>()

    // Interfaz gráfica
    private lateinit var adapter: SiteListAdapter
    private lateinit var tarea: TareaCargarSitio // Tarea en segundo plano
    private var paintSweep = Paint()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_sites, container, false)

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
        cargaSitios()

        // Solo si hemos cargado hacemos sl swipeHorizontal
        iniciarSwipeHorizontal()

        // Mostramos las vistas de listas y adaptador asociado
        my_sites_recicler.layoutManager = LinearLayoutManager(context)

        //Boton flotante anadir
        btnAddSiteFloating.setOnClickListener { addSite() }
    }

    /**
     * Iniciamos el swipe de recarga
     */
    private fun iniciarSwipeRecarga() {
        my_sites_swipe.setColorSchemeResources(R.color.primary)
        my_sites_swipe.setProgressBackgroundColorSchemeResource(R.color.primary_text)
        my_sites_swipe.setOnRefreshListener {
            cargaSitios()
        }
    }

    /**
     * Carga los Sitios
     */
    private fun cargaSitios() {
        sitios = mutableListOf<Site>()
        tarea = TareaCargarSitio()
        tarea.execute()
    }

    /**
     * Tarea asíncrona para la carga de los sitios
     */
    inner class TareaCargarSitio : AsyncTask<String?, Void?, Void?>() {

        override fun doInBackground(vararg p0: String?): Void? {
            try {
                val lista: MutableList<Site>? = SiteController.selectAllSite()

                if (lista != null) {
                    for (it in lista) {
                        sitios.add(it)
                    }
                }
            } catch (e: Exception) {
            }
            return null
        }

        /**
         * Procedimiento a realizar al terminar
         * Cargamos la lista
         *
         * @param args
         */
        override fun onPostExecute(args: Void?) {
            adapter = SiteListAdapter(sitios) {
                eventoClicFila(it)
            }

            my_sites_recicler.adapter = adapter
            // Avismos que ha cambiado
            adapter.notifyDataSetChanged()
            my_sites_recicler.setHasFixedSize(true)
            my_sites_swipe.isRefreshing = false
        }

        /**
         * Evento clic asociado a una fila
         * @param site Site
         */
        private fun eventoClicFila(site: Site) {
            if ((activity as NavigationActivity?)!!.isClicEventoFila) {
                openSite(site, 3)
            }
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
                // Si pulsamos a la de izquierda o a la derecha
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        // Borramos el elemento
                        borrarElemento(position)
                    }
                    else -> {
                        // Editamos el elemento
                        editarElemento(position)
                    }
                }
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
        // Añadimos los eventos al RV
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(my_sites_recicler)
    }

    //TODO BORRAR DE BD Y MENSAJE DE CONFIRMACION!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private fun borrarElemento(position: Int) {
        // Acciones
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
        try {
            // Borramos el sitio de Base de Datos
            SiteController.deleteSite(site)
            Toast.makeText(requireContext(), R.string.site_deleted, Toast.LENGTH_SHORT).show()

            Log.i("sites", print(sitios).toString())

        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
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
        paintSweep.setColor(Color.DKGRAY)
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sweep_detalles)
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
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sweep_eliminar)
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
    }

}