package com.windstrom5.tugasakhir.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.databinding.ActivityUserBinding
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan
import java.util.Calendar

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var tv : TextView
    private var bundle: Bundle? = null
    private var perusahaan : Perusahaan? = null
    private lateinit var tvnamaAdmin : TextView
    private lateinit var tvnamaPerusahaan : TextView
    private lateinit var absen:CardView
    private lateinit var lembur:CardView
    private lateinit var dinas:CardView
    private lateinit var izin:CardView
    private lateinit var company:CardView
    private lateinit var cs:CardView
    private var pekerja : Pekerja? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val namaPerusahaan = perusahaan?.nama
        absen = binding.AbsensiCard
        lembur = binding.LemburCard
        dinas = binding.DinasCard
        izin = binding.IzinCard
        company = binding.CompanyCard
        cs = binding.CustomerServiceCard
        val nama = pekerja?.nama
        tvnamaAdmin = binding.textView2
        tvnamaPerusahaan = binding.textView3
        val text = "© $currentYear $namaPerusahaan. \nAll rights reserved."
        tv = binding.courtesyNoticeTextView
        tv.setText(text)
        tvnamaAdmin.setText(nama)
        tvnamaPerusahaan.setText(namaPerusahaan)
        absen.setOnTouchListener { _, event -> handleCardTouch(absen, event, "AbsensiActivity") }
        lembur.setOnTouchListener { _, event -> handleCardTouch(lembur, event, "LemburActivity") }
        dinas.setOnTouchListener { _, event -> handleCardTouch(dinas, event, "DinasActivity") }
        izin.setOnTouchListener { _, event -> handleCardTouch(izin, event, "IzinActivity") }
        company.setOnTouchListener { _, event -> handleCardTouch(company, event, "CompanyActivity") }
        cs.setOnTouchListener { _, event -> handleCardTouch(cs, event, "CsActivity") }
    }
    private fun handleCardTouch(cardView: CardView, event: MotionEvent, activityName: String): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // User pressed down, change background tint to green
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // User released the click or canceled the click, revert to default color
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.whiteTextColor))
                when (activityName) {
                    "AbsensiActivity" -> {
                        val intent = Intent(this, AbsensiActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "LemburActivity" -> {
                        val intent = Intent(this, LemburActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "DinasActivity"-> {
                        val intent = Intent(this, DinasActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "IzinActivity"-> {
                        val intent = Intent(this, IzinActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "CompanyActivity"-> {
                        val intent = Intent(this, CompanyActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    "CsActivity"-> {
                        val intent = Intent(this, LemburActivity::class.java)
                        startActivityWithExtras(intent)
                    }
                    // Add more cases for other activities
                }
            }
        }
        // Return true to consume the touch event
        return true
    }
    private fun startActivityWithExtras(intent: Intent) {
        val userBundle = Bundle()
        userBundle.putParcelable("user", pekerja)
        Log.d("Testing",pekerja.toString())
        userBundle.putParcelable("perusahaan", perusahaan)
        userBundle.putString("role", "Pekerja")
        intent.putExtra("data", userBundle)
        startActivity(intent)
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
                pekerja = it.getParcelable("user")
                val imageUrl =
                    "http://192.168.1.6:8000/storage/${pekerja?.profile}" // Replace with your Laravel image URL
                val profileImageView = binding.profileB

                Glide.with(this)
                    .load(imageUrl)
                    .into(profileImageView)
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}