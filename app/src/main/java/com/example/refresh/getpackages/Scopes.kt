package com.example.refresh.getpackages

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope

//coroutines scopes from package manager API

val mainScope = MainScope()

val bgScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
