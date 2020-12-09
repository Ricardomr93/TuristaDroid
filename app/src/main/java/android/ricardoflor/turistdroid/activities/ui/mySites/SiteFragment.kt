package android.ricardoflor.turistdroid.activities.ui.mySites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.ricardoflor.turistdroid.R
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.activity_singin.*
import kotlinx.android.synthetic.main.fragment_site.*

class SiteFragment(modo: Int) : Fragment() {
    private val modo = modo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_site, container, false)
        editCreateMode(root)
        return root
    }

    /**
     * Metodo para abrir el fragment en edicion o en creacion
     */
    fun editCreateMode(root: View) {
        val btn: Button = root.findViewById(R.id.buttonSiteAddUpdate)
        val lSocial: LinearLayout = root.findViewById(R.id.linearLayoutSiteSocial)

        when (modo){
            //Si es 1 - Fragment de Creacion
            1 -> {
                btn.visibility = View.VISIBLE
                btn.text = getString(R.string.add)
                lSocial.visibility = View.INVISIBLE

                addUpdate(btn)
            }

            //Si es 2 - Fragment de Edicion
            2 -> {
                btn.visibility = View.VISIBLE
                btn.text = getString(R.string.update)
                lSocial.visibility = View.INVISIBLE

                addUpdate(btn)
            }

            // Si es 3 - Fragment de Consulta
            3 -> {
                btn.visibility = View.INVISIBLE
                lSocial.visibility = View.VISIBLE
            }
        }

    }

    /**
     * Metodo para aniadir o editar un sitio
     */
    fun addUpdate(btn: Button) {
        btn.setOnClickListener {
            if (modo == 1){
                Toast.makeText(context!!, "AÑADO", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context!!, "EDITO", Toast.LENGTH_SHORT).show()
            }
        }
    }
}