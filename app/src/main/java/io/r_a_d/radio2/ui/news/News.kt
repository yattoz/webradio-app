package io.r_a_d.radio2.ui.news

import java.util.*

class News {
    var title: String = ""
    var text: String = ""
    var header: String = ""
    var author: String = ""
    var date: Date = Date()

    override fun toString() : String
    {
        return "$author | $title | $date | $header"
    }
}