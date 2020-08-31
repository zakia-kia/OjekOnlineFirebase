package com.zakia.idn.ojekonlinefirebase.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.zakia.idn.ojekonlinefirebase.MainActivity
import com.zakia.idn.ojekonlinefirebase.R
import com.zakia.idn.ojekonlinefirebase.utils.Constan
import kotlinx.android.synthetic.main.activity_authentikasi_hp.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AuthentikasiHpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentikasi_hp)

        val key = intent.getStringExtra(Constan.key)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_uaser)

        //update realtime database
        auth_submit.onClick {
            if (auth_nomor_hp.text.toString().isNotEmpty()){
                myRef.child(key!!).child("hp").setValue(auth_nomor_hp.text.toString())
                startActivity<MainActivity>()
            }

            else toast("tidak boleh kosong")
        }
    }
}