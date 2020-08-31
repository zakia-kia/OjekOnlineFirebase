package com.zakia.idn.ojekonlinefirebase.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.zakia.idn.ojekonlinefirebase.R
import com.zakia.idn.ojekonlinefirebase.model.Users
import com.zakia.idn.ojekonlinefirebase.utils.Constan
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class SignUpActivity : AppCompatActivity() {
    private var aunt : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        aunt = FirebaseAuth.getInstance()

        btn_SignUp.onClick {
            if (et_SignUp_Email.text.isNotEmpty() &&
                et_SignUp_username.text.isNotEmpty() &&
                et_SignUp_Hp.text.isNotEmpty() &&
                et_SignUp_Password.text.isNotEmpty() &&
                et_SignUp_ConfirmPassword.text.isNotEmpty()
            ){
                authUserSignUp(
                    et_SignUp_Email.text.toString(),
                    et_SignUp_Password.text.toString()
                )
            }
        }
    }

    //prosessAunthentication

    private fun authUserSignUp (email: String, pass: String): Boolean?{
        aunt = FirebaseAuth.getInstance()

        var status : Boolean? = null
        val TAG = "tag"

        aunt?.createUserWithEmailAndPassword(email,pass)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (insertUser(
                            et_SignUp_username.text.toString(),
                            et_SignUp_Email.text.toString(),
                            et_SignUp_Hp.text.toString(),
                            task.result?.user!!
                        )){
                        startActivity<LoginActivity>()
                    }
                }else{
                    status = false
                }
            }
        return status
    }

    //proses penambahan data user ke realtime database
    fun insertUser( username: String, email: String, hp: String, users: FirebaseUser): Boolean {
        var user = Users()
        user.uid = users.uid
        user.username = username
        user.email = email
        user.hp = hp

        val database = FirebaseDatabase.getInstance()

        // id yang masuk ke database
        var key = database.reference.push().key

        //name table
        val myRef = database.getReference(Constan.tb_uaser)

        //save ke database
        myRef.child(key!!).setValue(user)

        return true
    }
}