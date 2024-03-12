package com.windstrom5.tugasakhir.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.databinding.ActivityIzinBinding
import com.windstrom5.tugasakhir.fragment.AddDinasFragment
import com.windstrom5.tugasakhir.fragment.HistoryAbsenFragment
import com.windstrom5.tugasakhir.fragment.ScanAbsensiFragment
import com.windstrom5.tugasakhir.fragment.ShowQRCodeFragment
import com.windstrom5.tugasakhir.fragment.TrackingFragment
import com.windstrom5.tugasakhir.model.Admin
import com.windstrom5.tugasakhir.model.Pekerja
import com.windstrom5.tugasakhir.model.Perusahaan

class IzinActivity : AppCompatActivity() {
    private lateinit var binding:ActivityIzinBinding
    private var bundle: Bundle? = null
    private var perusahaan : Perusahaan? = null
    private var admin : Admin? = null
    private var pekerja : Pekerja? = null
    private lateinit var fragment : FragmentContainerView
    private var isFirstLaunch = true
    private lateinit var navigation : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIzinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        fragment = binding.content
        navigation = binding.navigation
        navigation.setOnNavigationItemSelectedListener { menuItem ->
            if (!isFirstLaunch) {
                when (menuItem.itemId) {
                    R.id.historyizin -> {
                        replaceFragment(TrackingFragment())
                        true
                    }
                    R.id.adddinas -> {
                        replaceFragment(AddDinasFragment())
                        true
                    }
                    else -> false
                }
            } else {
                // It's the first launch, do nothing or perform any setup needed
                isFirstLaunch = false
                true
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        if(pekerja != null){
            val userBundle = Bundle()
            val intent = Intent(this, UserActivity::class.java)
            userBundle.putParcelable("user", pekerja)
            userBundle.putParcelable("perusahaan", perusahaan)
            intent.putExtra("data", userBundle)
            startActivity(intent)
        }else{
            val userBundle = Bundle()
            val intent = Intent(this, AdminActivity::class.java)
            userBundle.putParcelable("user", admin)
            userBundle.putParcelable("perusahaan", perusahaan)
            intent.putExtra("data", userBundle)
            startActivity(intent)
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        val args = Bundle()

        if (admin != null) {
            args.putParcelable("user", admin)
        } else if (pekerja != null) {
            args.putParcelable("usera", pekerja)
        }
        args.putParcelable("perusahaan",perusahaan)
        fragment.arguments = args

        transaction.replace(R.id.content, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    private fun getBundle() {
        bundle = intent?.getBundleExtra("data")
        if (bundle != null) {
            bundle?.let {
                perusahaan = it.getParcelable("perusahaan")
                val role = it.getString("role")
                if(role == "Admin"){
                    admin = it.getParcelable("user")
                    navigation.visibility = View.GONE
                }else{
                    pekerja = it.getParcelable("user")
                    navigation.visibility = View.VISIBLE
                    navigation.inflateMenu(R.menu.absenuser)
                    if (isFirstLaunch) {
                        replaceFragment(ScanAbsensiFragment())
                        isFirstLaunch = false
                    }
                }
            }
        } else {
            Log.d("Error","Bundle Not Found")
        }
    }
}