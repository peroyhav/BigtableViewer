package com.erikmafo.btviewer.ui.queryresult;

import com.erikmafo.btviewer.model.BigtableCell;
import com.erikmafo.btviewer.model.BigtableValueConverter;
import com.erikmafo.btviewer.util.FXMLLoaderUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class CellView extends BorderPane {

    private final ObjectProperty<BigtableCell> cell = new SimpleObjectProperty<>();

    private final BooleanProperty displayTimestamp = new SimpleBooleanProperty();

    private final ObjectProperty<BigtableValueConverter> valueConverter = new SimpleObjectProperty<>();

    @FXML
    private Label valueLabel;

    @FXML
    private Label versionLabel;

    public CellView() {
        FXMLLoaderUtil.loadFxml("/fxml/cell_view.fxml", this);
    }

    @FXML
    public void initialize() {
        valueLabel.textProperty().bind(Bindings
                .createStringBinding(this::getDisplayValue, cell, valueConverter));
        versionLabel.textProperty().bind(Bindings
                .createStringBinding(this::getTimestampDisplayValue, cell));
        versionLabel.visibleProperty().bind(displayTimestamp);
    }

    public void setBigtableCell(BigtableCell bigtableCell) {
        this.cell.set(bigtableCell);
    }

    public boolean getDisplayTimestamp() {
        return displayTimestamp.get();
    }

    @NotNull
    public BooleanProperty displayTimestampProperty() {
        return displayTimestamp;
    }

    @NotNull
    public ObjectProperty<BigtableValueConverter> valueConverterProperty() { return valueConverter; }

    private String getDisplayValue() {

        if (cell.getValue() == null) {
            return "";
        }

        if (valueConverter.getValue() == null) {
            return cell.getValue().getValueAsString();
        }

        return valueConverter.getValue().convert(cell.getValue()).toString();
    }

    private String getTimestampDisplayValue() {
        if (cell.getValue() == null) {
            return "";
        }
        long millis = TimeUnit.MILLISECONDS.convert(cell.get().getTimestamp(), TimeUnit.MICROSECONDS);
        var instant = Instant.ofEpochMilli(millis);
        var dateTime = ZonedDateTime.ofInstant(instant, ZoneId.ofOffset("", ZoneOffset.UTC));

        return String.format("%s", dateTime);
    }
}
