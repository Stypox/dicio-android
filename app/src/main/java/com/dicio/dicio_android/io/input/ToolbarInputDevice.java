package com.dicio.dicio_android.io.input;

import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

public class ToolbarInputDevice extends InputDevice {
    private MenuItem textInputItem;

    public void setTextInputItem(MenuItem textInputItem) {
        this.textInputItem = textInputItem;
        SearchView textInputView = (SearchView) textInputItem.getActionView();
        textInputView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                notifyInputReceived(query);
                textInputItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    @Override
    public void tryToGetInput() {
        textInputItem.expandActionView();
    }
}
