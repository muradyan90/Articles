package com.aram.articles.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.aram.articles.R
import com.aram.articles.databinding.FragmentArticleDetailsBinding
import com.aram.articles.viewmodels.ArticleDetailViewModelFactory
import com.aram.articles.viewmodels.ArticleDetailsViewModel


/**
 * A simple [Fragment] subclass.
 */
class ArticleDetailsFragment : Fragment() {

    private lateinit var binding: FragmentArticleDetailsBinding
    private lateinit var notNullArguments: ArticleDetailsFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_article_details, container, false)
        binding.lifecycleOwner = this

        getingArguments()
        getViewModel()
        binding.detailsWebView.webViewClient = WebViewClient()
        return binding.root
    }

    private fun getingArguments() {
        val bundle = arguments
        if (bundle != null)
            notNullArguments = ArticleDetailsFragmentArgs.fromBundle(bundle)
        // OPTIONAL TASK
        // SHARED ELEMENT TRANSITION ANIMATION
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        ViewCompat.setTransitionName(binding.detailsImg, notNullArguments.transitionName)
        binding.detailsWebView.transitionName = "sharedView"
    }

    private fun getViewModel() {
        val application = requireNotNull(activity).application
        val selectedArticle = notNullArguments.article
        val viewModelFactory = ArticleDetailViewModelFactory(selectedArticle, application)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(ArticleDetailsViewModel::class.java)
        binding.viewModel = viewModel
    }

    // Managing progress bar visibility while loading URL

    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            binding.progressBar.visibility = View.VISIBLE
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.progressBar.visibility = View.GONE
        }
    }
}

