package soup.movie.ui.main.settings

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_settings.*
import soup.movie.R
import soup.movie.data.helper.getChipLayout
import soup.movie.databinding.FragmentSettingsBinding
import soup.movie.ui.main.BaseTabFragment
import soup.movie.ui.theater.sort.TheaterSortActivity
import soup.movie.util.inflate
import soup.movie.util.log.printRenderLog
import javax.inject.Inject

class SettingsFragment :
        BaseTabFragment<SettingsContract.View, SettingsContract.Presenter>(),
        SettingsContract.View {

    @Inject
    override lateinit var presenter: SettingsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>,
                                             sharedElements: MutableMap<String, View>) {
                names.forEach { name ->
                    theaterGroup.findViewWithTag<View>(name)?.let {
                        sharedElements[name] = it
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            FragmentSettingsBinding.inflate(inflater, container, false).root

    override fun initViewState(ctx: Context) {
        super.initViewState(ctx)
        editTheaterButton.setOnClickListener {
            onTheaterEditClicked()
        }
        usePaletteThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.setUsePaletteTheme(isChecked)
        }
        useWebLinkSwitch.setOnCheckedChangeListener { _, isChecked ->
            presenter.setUseWebLink(isChecked)
        }
    }

    override fun render(viewState: SettingsViewState) {
        viewState.printRenderLog()
        val theaters = viewState.theaterList
        if (theaters.isEmpty()) {
            noTheaterView?.visibility = View.VISIBLE
            theaterGroup?.removeAllViews()
            theaterGroup?.visibility = View.GONE
        } else {
            noTheaterView?.visibility = View.GONE
            theaterGroup?.removeAllViews()
            theaterGroup?.visibility = View.VISIBLE

            theaters.map {
                inflate<Chip>(context!!, it.getChipLayout()).apply {
                    text = it.name
                    transitionName = it.code
                    tag = it.code
                }
            }.forEach { theaterGroup?.addView(it) }
        }
        usePaletteThemeSwitch.isChecked = viewState.usePaletteTheme
        useWebLinkSwitch.isChecked = viewState.useWebLink
    }

    private fun onTheaterEditClicked() {
        val intent = Intent(context, TheaterSortActivity::class.java)
        startActivity(intent, ActivityOptions
                .makeSceneTransitionAnimation(activity, *createTheaterChipPairsForTransition())
                .toBundle())
    }

    private fun createTheaterChipPairsForTransition(): Array<Pair<View, String>> =
            when (theaterGroup) {
                null -> emptyArray()
                else -> mutableListOf<Pair<View, String>>().also { pair ->
                    theaterGroup.run {
                        repeat(childCount) {
                            getChildAt(it)?.run {
                                pair.add(Pair.create(this, transitionName))
                            }
                        }
                    }
                }.toTypedArray()
            }

    companion object {

        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
