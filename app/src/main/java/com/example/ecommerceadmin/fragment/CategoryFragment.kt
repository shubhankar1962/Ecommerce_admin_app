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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceadmin.R
import com.example.ecommerceadmin.adapter.CategoryAdapter
import com.example.ecommerceadmin.databinding.FragmentCategoryBinding
import com.example.ecommerceadmin.models.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class CategoryFragment : Fragment() {

    private var imageUrl: Uri? = null

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl = it.data!!.data
            binding.categoryImgPreview.setImageURI(imageUrl)
        }
    }

    private lateinit var binding:FragmentCategoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentCategoryBinding.inflate(layoutInflater)

        getData()
        binding.apply {

            categoryImgPreview.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGalleryActivity.launch(intent)
            }

            categotyUploadBtn.setOnClickListener {
                validateData(binding.categoryName.text.toString())
            }
        }


        return binding.root
    }

    private fun getData() {
        var imgLink:String?
        var catLink:String?
        var list = ArrayList<CategoryModel>()
        val databaseref = FirebaseDatabase.getInstance().getReference("Categories").child("items")
        databaseref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for(data in snapshot.children)
                {
                      imgLink = data.child("pics").getValue(String::class.java)
                      catLink = data.child("cat").getValue(String::class.java)
                    list.add(CategoryModel(catLink,imgLink))
                }
                binding.categoryRecyclerView.adapter = CategoryAdapter(requireContext(),list)
                binding.categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun validateData(categoryName: String) {

        if(categoryName.isEmpty())
        {
            Toast.makeText(requireContext(),"Please provide category name",Toast.LENGTH_SHORT).show()
        }else if (imageUrl == null)
        {
            Toast.makeText(requireContext(),"Please select Image",Toast.LENGTH_SHORT).show()
        }
        else{
            uploadImage(categoryName)
        }
    }

    private fun uploadImage(categoryName: String) {

        val filename = UUID.randomUUID().toString()+".jpg"

        val firebaseStorage = FirebaseStorage.getInstance().reference.child("Category/$filename")
        firebaseStorage.putFile(imageUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {image->
                    Toast.makeText(requireContext(),"successful uploaded",Toast.LENGTH_SHORT).show()
                    savetoDatabase(categoryName,image.toString())

                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(),"something went wrong before store",Toast.LENGTH_SHORT).show()
            }
    }

    private fun savetoDatabase(categoryName: String,url:String){

        val databaseref = FirebaseDatabase.getInstance().getReference("Categories").child("items")
        val imageKey = databaseref.push().key


        val map = HashMap<String, Any>()
        map["pics"] = url
        map["cat"] = categoryName

        binding.categoryImgPreview.setImageDrawable(resources.getDrawable(R.drawable.img_preview))
        binding.categoryName.text = null
        imageKey?.let {
            databaseref.child(it).setValue(map).addOnCompleteListener{databaseTask->
                if(databaseTask.isSuccessful)
                {
                    Toast.makeText(requireContext(), "link saved successfully",Toast.LENGTH_LONG).show()
                }else
                {
                    Toast.makeText(requireContext(), "error in saved link", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }


}