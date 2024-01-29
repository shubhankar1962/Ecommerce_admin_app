package com.example.ecommerceadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecommerceadmin.R
import com.example.ecommerceadmin.databinding.CategotyItemLayoutBinding
import com.example.ecommerceadmin.models.CategoryModel

class CategoryAdapter(var context: Context,var list:ArrayList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view:View):RecyclerView.ViewHolder(view){
        var binding = CategotyItemLayoutBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.categoty_item_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.binding.catItemTv.text = list[position].cat
        Glide.with(context).load(list[position].image).into(holder.binding.catItemImg)
    }
}