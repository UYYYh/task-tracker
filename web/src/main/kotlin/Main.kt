import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLUListElement

fun main() {
    val input = document.getElementById("task-input") as HTMLInputElement
    val addBtn = document.getElementById("add-btn") as HTMLButtonElement
    val list = document.getElementById("task-list") as HTMLUListElement

    addBtn.onclick = {
        val text = input.value.trim()
        if (text.isNotEmpty()) {
            val li = document.createElement("li") as HTMLLIElement
            li.textContent = text
            list.appendChild(li)
            input.value = ""
        }
    }
}
