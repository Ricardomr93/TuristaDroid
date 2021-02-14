package android.ricardoflor.turistdroid.activities.ui.mySites

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.ricardoflor.turistdroid.R
import android.ricardoflor.turistdroid.apirest.TuristAPI
import android.ricardoflor.turistdroid.bd.image.Image
import android.ricardoflor.turistdroid.bd.image.ImageDTO
import android.ricardoflor.turistdroid.bd.image.ImageMapper
import android.ricardoflor.turistdroid.bd.site.Site
import android.ricardoflor.turistdroid.utils.UtilImage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_site.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs

/**
 * Adapter de la Lista de Sitios
 */
class SiteListAdapter(
    private val listaSitios: MutableList<Site>,
    private val listener: (Site) -> Unit
) :
    RecyclerView.Adapter<SiteListAdapter.SiteViewHolder>() {

    /**
     * Asociamos la vista
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        return SiteViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_site, parent, false)
        )
    }

    /**
     * Procesamos los sitios y las metemos en un Holder
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        var siteName: String = listaSitios[position].name
        var siteLocation: String = listaSitios[position].site
        var siteDate: String = listaSitios[position].date
        var siteRatings: Double = listaSitios[position].rating
        var siteNumVotos: Int = listaSitios[position].votos.size
        var siteMedia: String = String.format("%.1f", (siteRatings / siteNumVotos))

        //Controlamos la longitud para que si llega a una cantidad de caracteres, recortarlo
        if (siteName.length >= 30) {
            siteName = siteName.substring(0, 30)
            holder.siteName.text = "$siteName..."
        } else {
            holder.siteName.text = siteName
        }
        if (siteLocation.length >= 30) {
            siteLocation = siteLocation.substring(0, 30)
            holder.siteLocation.text = "$siteLocation..."
        } else {
            holder.siteLocation.text = siteLocation
        }
        //saca la diferencia entre dos fechas
        val days = compara2Fechas(siteDate)
        if (days < 30) {
            holder.siteDate.text = "$days day ago"
        } else {
            holder.siteDate.text = "More than 30 day ago"
        }
        holder.siteRating.text = siteMedia

        //para mostrar un mensaje u otro segun el numero de votos
        when {
            siteNumVotos < 1 -> {
                holder.likes.text = ""
            }
            siteNumVotos == 1 -> {
                holder.likes.text = "$siteNumVotos vote"
            }
            else -> {
                holder.likes.text = "$siteNumVotos votes"
            }
        }
        //Cargamos una imagen para mostrar en el listado de sitios
        if (listaSitios[position].images.isNotEmpty()) {
            for (img in listaSitios[position].images) {
                Picasso.get().load(img).into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        if (bitmap != null) {
                            holder.siteImage.setImageBitmap(bitmap)
                        }
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })
            }
        }

        // Programamos el clic de cada fila (itemView)
        holder.btnViewSiteFloating
            .setOnClickListener {
                // Devolvemos el sitio
                listener(listaSitios[position])
            }
    }
    

    private fun compara2Fechas(siteDate: String): Int {
        val caNow = Calendar.getInstance()
        caNow.set(Calendar.HOUR, 0)
        caNow.set(Calendar.HOUR_OF_DAY, 0)
        caNow.set(Calendar.MINUTE, 0)
        caNow.set(Calendar.SECOND, 0)
        val day = siteDate.substring(0, 2).toInt()
        val month = siteDate.substring(3, 5).toInt()
        val year = siteDate.substring(6, 10).toInt()
        val caSite = Calendar.getInstance()
        caSite.set(year, month - 1, day)
        caSite.set(Calendar.HOUR, 0)
        caSite.set(Calendar.HOUR_OF_DAY, 0)
        caSite.set(Calendar.MINUTE, 0)
        caSite.set(Calendar.SECOND, 0)
        val nowMil = caNow.timeInMillis
        val siteMil = caSite.timeInMillis
        return (abs(siteMil - nowMil) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Elimina un item de la lista
     *
     * @param position
     */
    fun removeItem(position: Int) {
        listaSitios.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaSitios.size)
    }

    /**
     * Recupera un Item de la lista
     *
     * @param item
     * @param position
     */
    fun restoreItem(item: Site, position: Int) {
        listaSitios.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaSitios.size)
    }

    /**
     * Devuelve el número de items de la lista
     *
     * @return
     */
    override fun getItemCount(): Int {
        return listaSitios.size
    }

    /**
     * Holder que encapsula los objetos a mostrar en la lista
     */
    class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Elementos graficos con los que nos asociamos
        var siteName = itemView.txtSiteName
        var siteLocation = itemView.txtSite2
        var siteDate = itemView.lblDateItem
        var siteRating = itemView.lblRatinItem
        var siteImage = itemView.imgSite
        var likes = itemView.lblLikesItemSite

        // Indicamos el Layout para el click
        //var relativeLayout = itemView.item_site
        var btnViewSiteFloating = itemView.buttomItemSite
    }
}
