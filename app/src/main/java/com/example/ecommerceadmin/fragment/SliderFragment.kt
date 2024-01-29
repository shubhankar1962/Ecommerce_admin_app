package com.example.ecommerceadmin.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import com.example.ecommerceadmin.databinding.FragmentSliderBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding
    private var imageUrl:Uri? = null

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl = it.data!!.data
            binding.imgPreview.setImageURI(imageUrl)
        }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSliderBinding.inflate(layoutInflater)

        binding.imgPreview.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)

        }
        binding.uploadImg.setOnClickListener{
            if(imageUrl != null){
                uploadImage(imageUrl!!)
            }else{
                Toast.makeText(requireContext(), "Please seelect photo ",Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun uploadImage(uri: Uri) {

        val filename = UUID.randomUUID().toString()+".jpg"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child("Slider/$filename")
        firebaseStorage.putFile(uri)
            .addOnSuccessListener { 
                it.storage.downloadUrl.addOnSuccessListener {image->
                    savetoDatabase(image.toString())
                    
                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),"something went wrong before store",Toast.LENGTH_SHORT).show()
            }
    }

    private fun savetoDatabase(downloadUrl:String){

    val databaseref = FirebaseDatabase.getInstance().getReference("slider").child("items")
    val imageKey = databaseref.push().key


    val map = HashMap<String, Any>()
    map["pics"] = downloadUrl

    imageKey?.let {
        databaseref.child(it).setValue(map).addOnCompleteListener{databaseTask->
            if(databaseTask.isSuccessful)
            {
                Toast.makeText(requireContext(), "link saved successfully",Toast.LENGTH_SHORT).show()
            }else
            {
                Toast.makeText(requireContext(), "error in saved link", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
//    private fun storeData(image: String){
//
//        val db = Firebase.firestore
//
//        val data = hashMapOf<String,Any>(
//            "img" to image
//        )
//        db.collection("slider").document("item").set(data)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(),"slider updated",Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener{
//                Toast.makeText(requireContext(),"something went wrong",Toast.LENGTH_SHORT).show()
//            }
//    }


}