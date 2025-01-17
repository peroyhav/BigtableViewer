package com.erikmafo.btviewer.ui.menubar;

import com.erikmafo.btviewer.services.credential.LoadCredentialsPathService;
import com.erikmafo.btviewer.services.credential.SaveCredentialsPathService;
import com.erikmafo.btviewer.ui.shared.CredentialsPathDialog;
import com.sun.javafx.PlatformUtil;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.nio.file.Path;

public class MenuBarController {

    private final LoadCredentialsPathService loadCredentialsPathService;
    private final SaveCredentialsPathService saveCredentialsPathService;

    @FXML
    private MenuItem credentialsMenu;

    @FXML
    private MenuBar menuBar;

    @Inject
    public MenuBarController(
            LoadCredentialsPathService loadCredentialsPathService,
            SaveCredentialsPathService saveCredentialsPathService) {
        this.loadCredentialsPathService = loadCredentialsPathService;
        this.saveCredentialsPathService = saveCredentialsPathService;
    }

    @FXML
    public void initialize() {
        if (PlatformUtil.isMac()) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
    }

    @FXML
    public void onManageCredentialsAction(ActionEvent event) {
        loadCredentialsPathService.setOnSucceeded(e ->
                displaySpecifyCredentialsDialog(loadCredentialsPathService.getValue()));
        loadCredentialsPathService.setOnFailed(e -> {
                    displayErrorInfo("Unable to load credentials.", e);
                    displaySpecifyCredentialsDialog(loadCredentialsPathService.getValue());
                });
        loadCredentialsPathService.restart();
    }

    private void displaySpecifyCredentialsDialog(Path currentPath) {
        CredentialsPathDialog.displayAndAwaitResult(currentPath)
                .whenComplete(this::onCredentialsPathDialogComplete);
    }

    private void onCredentialsPathDialogComplete(@Nullable Path path, @Nullable Throwable throwable) {
        if (throwable != null) {
            displayError(throwable);
        }

        if (path == null) {
            return;
        }

        saveCredentialsPathService.setCredentialsPath(path);
        saveCredentialsPathService.setOnFailed(e -> displayErrorInfo("Unable to save credentials path.", e));
        saveCredentialsPathService.restart();
    }

    private void displayError(@NotNull Throwable throwable) {
        var alert = new Alert(
                Alert.AlertType.ERROR,
                "Something went wrong: " + throwable.getLocalizedMessage(),
                ButtonType.CLOSE);
        alert.showAndWait();
    }

    private void displayErrorInfo(String errorText, @NotNull WorkerStateEvent event) {
        var exception = event.getSource().getException();
        var alert = new Alert(
                Alert.AlertType.ERROR, errorText + " " + exception.getLocalizedMessage(), ButtonType.CLOSE);
        alert.showAndWait();
    }
}
