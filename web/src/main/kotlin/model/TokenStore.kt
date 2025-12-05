package model

import kotlinx.browser.localStorage

object TokenStore {
    private const val KEY = "tasktracker_token"

    var token: String?
        get() = localStorage.getItem(KEY)
        set(value) {
            if (value == null) {
                localStorage.removeItem(KEY)
            } else {
                localStorage.setItem(KEY, value)
            }
        }
}
