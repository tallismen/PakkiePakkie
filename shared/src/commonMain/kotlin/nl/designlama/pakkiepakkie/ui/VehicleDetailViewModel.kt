package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIDirections
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.Review
import nl.designlama.pakkiepakkie.data.ReviewRepository
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.datastore.UserVehicleRepository
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneCalculator
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneEstimate
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate

data class VehicleDetailState(
    val loading: Boolean = true,
    val detail: VehicleLicensePlateInfo? = null,
    val my: VehicleLicensePlateInfo? = null,
    val myVehicleKenteken: String? = null,
    val errorMessage: String? = null,
    val detailIsChipped: Boolean = false,
    val myIsChipped: Boolean = false,
    val detailTune: ChippedTuneEstimate? = null,
    val myTune: ChippedTuneEstimate? = null,
    val reviews: List<Review> = emptyList(),
    val myReview: Review? = null,
    val averageRating: Float? = null,
    val reviewSheetVisible: Boolean = false,
    val draftRating: Int = 0,
    val draftText: String = "",
    val reviewSubmitting: Boolean = false,
    val reviewErrorMessage: String? = null,
) : UIState {
    fun isMyVehicle(kenteken: String): Boolean {
        val norm = sanitizeLicensePlate(kenteken)
        val myNorm = myVehicleKenteken?.let { sanitizeLicensePlate(it) }
        return norm.length == 6 && norm == myNorm
    }
}

sealed interface VehicleDetailEvent : UIEvent {
    data object OnSetAsMyVehicle : VehicleDetailEvent
    data object OnClearAsMyVehicle : VehicleDetailEvent
    data object OnToggleDetailChipped : VehicleDetailEvent
    data object OnToggleMyChipped : VehicleDetailEvent
    data object OnOpenReviewSheet : VehicleDetailEvent
    data object OnDismissReviewSheet : VehicleDetailEvent
    data class OnDraftRatingChange(val rating: Int) : VehicleDetailEvent
    data class OnDraftTextChange(val text: String) : VehicleDetailEvent
    data object OnSubmitReview : VehicleDetailEvent
}

class VehicleDetailViewModel(
    private val vehicleLicenseRepository: VehicleLicenseRepository,
    private val userVehicleRepository: UserVehicleRepository,
    private val reviewRepository: ReviewRepository,
    private val kenteken: String,
) : BaseViewModel<VehicleDetailState, VehicleDetailEvent, UIDirections>() {

    init {
        loadVehicle()
        observeReviews()
        viewModelScope.launch {
            userVehicleRepository.myVehicleKentekenFlow().collectLatest { myK ->
                val myInfo = resolveMyVehicleInfo(myK)
                val myChipped = myK?.let { vehicleLicenseRepository.isChipped(it) } ?: false
                _state.value = _state.value.copy(
                    myVehicleKenteken = myK,
                    my = myInfo,
                    myIsChipped = myChipped,
                    myTune = if (myInfo != null && myChipped) ChippedTuneCalculator.estimate(myInfo) else null,
                )
            }
        }
        viewModelScope.launch {
            val myReview = reviewRepository.getMyReviewForKenteken(kenteken)
            _state.value = _state.value.copy(myReview = myReview)
        }
    }

    private fun observeReviews() {
        viewModelScope.launch {
            reviewRepository.observeReviewsForKenteken(kenteken).collectLatest { reviews ->
                val average = if (reviews.isEmpty()) {
                    null
                } else {
                    reviews.map { it.rating }.average().toFloat()
                }
                _state.value = _state.value.copy(
                    reviews = reviews,
                    averageRating = average,
                )
            }
        }
    }

    private suspend fun resolveMyVehicleInfo(myK: String?): VehicleLicensePlateInfo? {
        val myNorm = myK?.let { sanitizeLicensePlate(it) }?.takeIf { it.length == 6 } ?: return null
        return vehicleLicenseRepository.getCachedEntity(myNorm)
            ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
            ?.toVehicleLicensePlateInfo()
    }

    override fun defaultUIState(): VehicleDetailState = VehicleDetailState()

    override fun onEvent(event: VehicleDetailEvent) {
        super.onEvent(event)
        when (event) {
            VehicleDetailEvent.OnSetAsMyVehicle -> {
                val detail = _state.value.detail ?: return
                viewModelScope.launch {
                    runCatching { userVehicleRepository.setMyVehicle(detail.kenteken) }
                }
            }
            VehicleDetailEvent.OnClearAsMyVehicle -> {
                viewModelScope.launch {
                    runCatching { userVehicleRepository.clearMyVehicle() }
                }
            }
            VehicleDetailEvent.OnToggleDetailChipped -> toggleChipped(forDetail = true)
            VehicleDetailEvent.OnToggleMyChipped -> toggleChipped(forDetail = false)
            VehicleDetailEvent.OnOpenReviewSheet -> {
                val existing = _state.value.myReview
                _state.value = _state.value.copy(
                    reviewSheetVisible = true,
                    draftRating = existing?.rating ?: 0,
                    draftText = existing?.text.orEmpty(),
                    reviewErrorMessage = null,
                    reviewSubmitting = false,
                )
            }
            VehicleDetailEvent.OnDismissReviewSheet -> {
                if (_state.value.reviewSubmitting) return
                _state.value = _state.value.copy(
                    reviewSheetVisible = false,
                    reviewErrorMessage = null,
                )
            }
            is VehicleDetailEvent.OnDraftRatingChange -> {
                _state.value = _state.value.copy(
                    draftRating = event.rating.coerceIn(Review.MIN_RATING, Review.MAX_RATING),
                    reviewErrorMessage = null,
                )
            }
            is VehicleDetailEvent.OnDraftTextChange -> {
                val clipped = event.text.take(Review.MAX_TEXT_LENGTH)
                _state.value = _state.value.copy(
                    draftText = clipped,
                    reviewErrorMessage = null,
                )
            }
            VehicleDetailEvent.OnSubmitReview -> submitReview()
        }
    }

    private fun submitReview() {
        viewModelScope.launch {
            val rating = _state.value.draftRating
            if (rating !in Review.MIN_RATING..Review.MAX_RATING) {
                _state.value = _state.value.copy(
                    reviewErrorMessage = "Kies een beoordeling van 1 tot 5 sterren",
                )
                return@launch
            }
            _state.value = _state.value.copy(reviewSubmitting = true, reviewErrorMessage = null)
            val result = reviewRepository.upsertReview(
                kenteken = kenteken,
                rating = rating,
                text = _state.value.draftText,
            )
            result.onSuccess { saved ->
                _state.value = _state.value.copy(
                    reviewSubmitting = false,
                    reviewSheetVisible = false,
                    myReview = saved,
                    reviewErrorMessage = null,
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    reviewSubmitting = false,
                    reviewErrorMessage = error.message
                        ?: "Beoordeling opslaan is nog niet beschikbaar",
                )
            }
        }
    }

    private fun toggleChipped(forDetail: Boolean) {
        viewModelScope.launch {
            val info = if (forDetail) {
                _state.value.detail ?: return@launch
            } else {
                _state.value.my ?: return@launch
            }
            val currently = if (forDetail) _state.value.detailIsChipped else _state.value.myIsChipped
            val next = !currently
            vehicleLicenseRepository.setChipped(info.kenteken, next)
            val tune = if (next) ChippedTuneCalculator.estimate(info) else null
            if (forDetail) {
                _state.value = _state.value.copy(detailIsChipped = next, detailTune = tune)
            } else {
                _state.value = _state.value.copy(myIsChipped = next, myTune = tune)
            }
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            val norm = sanitizeLicensePlate(kenteken)
            if (norm.length != 6) {
                _state.value = VehicleDetailState(loading = false, errorMessage = "Ongeldig kenteken")
                return@launch
            }
            val entity = vehicleLicenseRepository.getCachedEntity(norm)
            val result = if (entity != null && entity.dataVersion >= VehicleLookupDataVersion.FULL) {
                Result.success(entity.toVehicleLicensePlateInfo())
            } else {
                vehicleLicenseRepository.refreshByKenteken(norm)
            }
            val detail = result.getOrNull()
            if (detail == null) {
                _state.value = VehicleDetailState(
                    loading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Kon voertuig niet laden",
                )
                return@launch
            }
            val myK = userVehicleRepository.myVehicleKentekenFlow().first()
            val detailChipped = vehicleLicenseRepository.isChipped(norm)
            val myChipped = myK?.let { vehicleLicenseRepository.isChipped(it) } ?: false
            val myInfo = resolveMyVehicleInfo(myK)
            val current = _state.value
            _state.value = current.copy(
                loading = false,
                detail = detail,
                my = myInfo,
                myVehicleKenteken = myK,
                errorMessage = null,
                detailIsChipped = detailChipped,
                myIsChipped = myChipped,
                detailTune = if (detailChipped) ChippedTuneCalculator.estimate(detail) else null,
                myTune = if (myInfo != null && myChipped) ChippedTuneCalculator.estimate(myInfo) else null,
            )
        }
    }
}
