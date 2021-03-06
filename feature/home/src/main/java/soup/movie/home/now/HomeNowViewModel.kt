package soup.movie.home.now

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import soup.movie.home.HomeContentsUiModel
import soup.movie.home.domain.getMovieFilterFlow
import soup.movie.home.tab.HomeContentsViewModel
import soup.movie.model.Movie
import soup.movie.model.repository.MoopRepository
import soup.movie.settings.AppSettings

class HomeNowViewModel @AssistedInject constructor(
    private val appSettings: AppSettings,
    private val repository: MoopRepository
) : ViewModel(), HomeContentsViewModel {

    private val _isLoading = MutableLiveData<Boolean>(false)
    override val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isError = MutableLiveData<Boolean>(false)
    override val isError: LiveData<Boolean>
        get() = _isError

    private val _contentsUiModel = MutableLiveData<HomeContentsUiModel>()
    override val contentsUiModel: LiveData<HomeContentsUiModel>
        get() = _contentsUiModel

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateList()
            repository.getNowMovieList()
                .combine(appSettings.getMovieFilterFlow()) { movieList, movieFilter ->
                    movieList.asSequence()
                        .sortedBy(Movie::score)
                        .filter { movieFilter(it) }
                        .toList()
                }
                .onStart { delay(200) }
                .collect {
                    _contentsUiModel.postValue(HomeContentsUiModel(it))
                }
        }
    }

    override fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            updateList()
        }
    }

    private suspend fun updateList() {
        if (_isLoading.value == true) {
            return
        }
        _isLoading.postValue(true)
        try {
            repository.updateNowMovieList()
            _isLoading.postValue(false)
            _isError.postValue(false)
        } catch (t: Throwable) {
            _isLoading.postValue(false)
            _isError.postValue(true)
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(): HomeNowViewModel
    }
}
