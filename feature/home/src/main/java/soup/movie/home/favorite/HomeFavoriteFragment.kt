package soup.movie.home.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import soup.movie.analytics.EventAnalytics
import soup.movie.ext.assistedViewModels
import soup.movie.home.HomeFragmentDirections
import soup.movie.home.databinding.HomeTabFavoriteBinding
import soup.movie.home.tab.HomeTabFragment
import javax.inject.Inject

class HomeFavoriteFragment : HomeTabFragment() {

    @Inject
    lateinit var analytics: EventAnalytics

    private lateinit var binding: HomeTabFavoriteBinding

    @Inject
    lateinit var viewModelFactory: HomeFavoriteViewModel.Factory
    private val viewModel: HomeFavoriteViewModel by assistedViewModels {
        viewModelFactory.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeTabFavoriteBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.initViewState(viewModel)
        return binding.root
    }

    private fun HomeTabFavoriteBinding.initViewState(viewModel: HomeFavoriteViewModel) {
        val listAdapter = HomeFavoriteListAdapter(root.context) { movie, sharedElements ->
            analytics.clickMovie()
            findNavController().navigate(
                HomeFragmentDirections.actionToDetail(movie),
                ActivityNavigatorExtras(
                    activityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(requireActivity(), *sharedElements)
                )
            )
        }
        listView.apply {
            adapter = listAdapter
            itemAnimator = FadeInAnimator()
        }
        viewModel.contentsUiModel.observe(viewLifecycleOwner) {
            noItemsView.isVisible = it.movies.isEmpty()
            listAdapter.submitList(it.movies)
        }
    }

    override fun scrollToTop() {
        getListView()?.scrollToTopInternal()
    }

    override fun onBackPressed(): Boolean {
        return getListView()?.scrollToTopInternal() ?: false
    }

    private fun getListView(): RecyclerView? {
        return if (::binding.isInitialized) binding.listView else null
    }
}
