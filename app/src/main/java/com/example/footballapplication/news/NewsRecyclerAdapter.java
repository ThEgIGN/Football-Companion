package com.example.footballapplication.news;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballapplication.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder> {

    ArrayList<News> articleList;
    Context mainContext;

    NewsRecyclerAdapter(ArrayList<News> articleList, Context context) {
        this.mainContext = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainContext).inflate(R.layout.news_recycler_row, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News article = articleList.get(position);
        holder.articleTitle.setText(article.getTitle());
        holder.articleTime.setText(article.getTime());
        Picasso.get().load(article.getImage())
                .error(R.drawable.no_image_placeholder)
                .placeholder(R.drawable.no_image_placeholder)
                .into(holder.articleImage);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mainContext, NewsFullActivity.class);
            intent.putExtra("url", article.getUrl());
            mainContext.startActivity(intent);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<News> articleList) {
        this.articleList.clear();
        this.articleList.addAll(articleList);
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        private final TextView articleTitle, articleTime;
        private final ImageView articleImage;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            articleTitle = itemView.findViewById(R.id.article_title);
            articleTime = itemView.findViewById(R.id.article_time);
            articleImage = itemView.findViewById(R.id.article_image_view);
        }
    }
}
