package com.bugs.posisiin01.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bugs.posisiin01.repository.SampelRepository

class SampelViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(SampelRepository::class.java)
            .newInstance(SampelRepository())
    }
}