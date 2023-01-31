package org.stypox.dicio.input

import android.view.MenuItem
import androidx.appcompat.widget.SearchView

class ToolbarInputDevice : InputDevice() {
    private var textInputItem: MenuItem? = null

    fun setTextInputItem(textInputItem: MenuItem?) {
        (this.textInputItem?.actionView as? SearchView)?.setOnQueryTextListener(null)
        this.textInputItem = textInputItem
        if (textInputItem != null) {
            textInputItem.setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
                super@ToolbarInputDevice.tryToGetInput(true) // notify we are starting to get input
                false // returning false causes the item to expand
            })
            textInputItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    notifyNoInputReceived()
                    return true
                }
            })
            val textInputView = textInputItem.actionView as SearchView
            textInputView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (query.isEmpty()) {
                        notifyNoInputReceived()
                    } else {
                        notifyInputReceived(query)
                    }
                    textInputItem.collapseActionView()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    // here we could call notifyPartialInputReceived(), but it would be useless
                    // since the user can already see what he is typing elsewhere
                    return true
                }
            })
        }
    }

    override fun cleanup() {
        super.cleanup()
        setTextInputItem(null)
    }

    override fun load() {}

    override fun tryToGetInput(manual: Boolean) {
        textInputItem?.apply {
            super.tryToGetInput(manual)
            expandActionView()
        }
    }

    override fun cancelGettingInput() {
        textInputItem?.apply {
            if (isActionViewExpanded) {
                collapseActionView()
                notifyNoInputReceived()
            }
        }
    }
}