package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField

/** [ViewModel] for content-card state. */
class ContentViewModel(val contentId: String, val htmlContent: CharSequence) :
  StateItemViewModel(ViewType.CONTENT){
  val isAudioPlaying = ObservableField<Boolean>(false)

  fun updateIsAudioPlaying(isPlaying: Boolean){
    isAudioPlaying.set(isPlaying)
  }
}
