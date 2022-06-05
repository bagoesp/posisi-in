package com.bugs.posisiin01.model

data class Sel(
    var x: Int = 0,
    var y: Int = 0,
    var left: Boolean = false,
    var top: Boolean = false,
    var right: Boolean = false,
    var bottom: Boolean = false,
    var g: Int = 0,
    var h: Int = 0,
    var f: Int = 0,
    var parent: Sel? = null
)
