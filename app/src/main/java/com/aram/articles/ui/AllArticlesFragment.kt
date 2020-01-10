package com.aram.articles.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aram.articles.R
import com.aram.articles.database.ArticleDatabase
import com.aram.articles.database.ArticleEntity
import com.aram.articles.databinding.FragmentAllArticlesBinding
import com.aram.articles.viewmodels.AllArticlesViewModel
import com.aram.articles.viewmodels.AllArticlesViewModelFactory


/**
 * A simple [Fragment] subclass.
 */
class AllArticlesFragment : Fragment(), AllArticlesAdapter.OnClickListener {
    private lateinit var binding: FragmentAllArticlesBinding
    private lateinit var viewModel: AllArticlesViewModel
    private lateinit var sharedImageview: ImageView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: AllArticlesAdapter
    val TAG = "LOG"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_all_articles, container, false)
        binding.lifecycleOwner = this
        getingViewModel()
        configRecyclerView()
        showToastMasage()
        swipeToDeleteItem()
        navigateToDeteilsFragment()
        return binding.root
    }

    private fun getingViewModel() {
        val application = requireNotNull(activity).application
        val allArticlesDao = ArticleDatabase.getInstance(application).articlesDao
        val tappedArticleDao = ArticleDatabase.getInstance(application).tappedArticlesDao
        val viewModelFactory =
            AllArticlesViewModelFactory(application, allArticlesDao, tappedArticleDao)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(AllArticlesViewModel::class.java)
        binding.viewModel = viewModel
    }

    private fun configRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        adapter = AllArticlesAdapter(this)
        binding.allArticlesRv.adapter = adapter //AllArticlesAdapter(this)
        binding.allArticlesRv.layoutManager = layoutManager
    }

    private fun showToastMasage() {
        viewModel.toast.observe(this, Observer {
            if (it != null && it && context != null) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                viewModel.displayToastComplete()
            }
        })
    }

    private fun swipeToDeleteItem() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.deleteArticle(adapter.getItemByPosition(viewHolder.adapterPosition))
               // adapter.setingAdapterPosition(viewHolder.adapterPosition)
            }

        }).attachToRecyclerView(binding.allArticlesRv)
    }

    private fun navigateToDeteilsFragment() {
        viewModel.navigateToSelectedArticle.observe(this, Observer {
            if (it != null) {
                // OPTIONAL TASK
                // SHARED ELEMENT TRANSITION ANIMATION
                val extras =
                    FragmentNavigatorExtras(sharedImageview to sharedImageview.transitionName)
                this.findNavController().navigate(
                    AllArticlesFragmentDirections.actionAllArticlesFragmentToArticleDetailsFragment(
                        it,
                        sharedImageview.transitionName
                    ), extras
                )
                viewModel.displayArticleDetailsComplete()
            }
        })
    }

    override fun onItemClick(article: ArticleEntity, imageView: ImageView) {
        sharedImageview = imageView
        viewModel.displayArticleDetails(article)
    }

    override fun onLikeClick(article: ArticleEntity, adapterPosition: Int) {
        viewModel.onLikeClick(article)
    }

    override fun scrollListener(position: Int) {
        viewModel.getNextPage()
    }


}
