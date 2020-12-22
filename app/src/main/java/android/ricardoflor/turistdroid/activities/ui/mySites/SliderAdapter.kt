package android.ricardoflor.turistdroid.activities.ui.mySites

import android.content.Context
import android.content.res.Resources
import android.ricardoflor.turistdroid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_slider_image.view.*

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import io.realm.RealmList

class SliderAdapter: PagerAdapter{

    var context: Context
    var images: RealmList<Bitmap>
    lateinit var inflater: LayoutInflater

    constructor(context: Context, images:  RealmList<Bitmap>): super(){
        this.context = context
        this.images = images
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view  == `object` as RelativeLayout

    override fun getCount(): Int =  images.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var image: ImageView
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View = inflater.inflate(R.layout.item_slider_image, container, false)
        image = view.findViewById(R.id.slider_image)

        image.setImageBitmap(images[position])

        container!!.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container!!.removeView(`object` as RelativeLayout)
    }
}